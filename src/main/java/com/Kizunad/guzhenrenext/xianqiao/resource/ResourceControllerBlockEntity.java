package com.Kizunad.guzhenrenext.xianqiao.resource;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 资源控制器方块实体。
 * <p>
 * 负责完成资源点核心逻辑：
 * 1) 周期性检测 3×3×3 范围内组件数量；
 * 2) 读取时道灵气计算效率；
 * 3) 按效率推进进度并产出到内部库存。
 * </p>
 */
public class ResourceControllerBlockEntity extends BlockEntity implements MenuProvider, Container {

    /** 库存槽位总数（产出槽 + 输入槽）。 */
    public static final int INVENTORY_SIZE = 2;

    /** 产出槽索引（只出不进）。 */
    public static final int SLOT_OUTPUT = 0;

    /** 输入槽索引（只进不出，作为运行前置条件）。 */
    public static final int SLOT_INPUT = 1;

    /** 结构重检间隔（tick）。 */
    private static final int STRUCTURE_RECHECK_INTERVAL = 100;

    /** 结构扫描半径（3×3×3）。 */
    private static final int STRUCTURE_SCAN_RADIUS = 1;

    /** 结构形成所需最小组件数量。 */
    private static final int MIN_COMPONENTS_REQUIRED = 4;

    /** 每个组件提供的基础速度加成。 */
    private static final float BASE_SPEED_PER_COMPONENT = 0.08F;

    /** 保底基础速度（结构形成后即有）。 */
    private static final float BASE_SPEED_MIN = 0.2F;

    /** 单轮产出所需进度。 */
    private static final float MAX_PROGRESS = 10000.0F;

    /** 灵气不足时效率。 */
    private static final float LOW_EFFICIENCY = 0.1F;

    /** 灵气达标时效率。 */
    private static final float NORMAL_EFFICIENCY = 1.0F;

    /** 灵气超额时效率。 */
    private static final float BONUS_EFFICIENCY = 1.5F;

    /** 达标阈值。 */
    private static final int REQUIRED_AURA = 200;

    /** 奖励阈值。 */
    private static final int BONUS_AURA = 1200;

    /** 进度写回缩放（保留三位小数）。 */
    private static final int PROGRESS_SCALE = 1000;

    /** 百分比缩放。 */
    private static final int PERCENT_SCALE = 100;

    /** 判定仙窍归属的额外缓冲。 */
    private static final int APERTURE_RADIUS_BUFFER = 16;

    /** 每个区块边长（用于区块内坐标匹配）。 */
    private static final int CHUNK_SIZE = 16;

    /** 每轮产出数量。 */
    private static final int OUTPUT_COUNT_PER_CYCLE = 1;

    /** 组件扫描总维度长度（3x3x3）。 */
    private static final int COMPONENT_SCAN_DIMENSION = 3;

    /** 菜单数据字段数量。 */
    private static final int MENU_DATA_FIELDS = 6;

    /** 菜单数据字段索引：进度值。 */
    private static final int MENU_DATA_PROGRESS = 0;

    /** 菜单数据字段索引：进度总量。 */
    private static final int MENU_DATA_PROGRESS_MAX = 1;

    /** 菜单数据字段索引：结构状态。 */
    private static final int MENU_DATA_FORMED = 2;

    /** 菜单数据字段索引：效率百分比。 */
    private static final int MENU_DATA_EFFICIENCY = 3;

    /** 菜单数据字段索引：当前灵气。 */
    private static final int MENU_DATA_AURA = 4;

    /** 菜单数据字段索引：剩余 tick。 */
    private static final int MENU_DATA_REMAINING = 5;

    /** NBT key：进度。 */
    private static final String KEY_PROGRESS = "progress";

    /** NBT key：结构状态。 */
    private static final String KEY_FORMED = "isFormed";

    /** NBT key：缓存组件数。 */
    private static final String KEY_COMPONENT_COUNT = "componentCount";

    /** NBT key：结构重检倒计时。 */
    private static final String KEY_STRUCTURE_COOLDOWN = "structureCooldown";

    /** NBT key：库存。 */
    private static final String KEY_ITEMS = "items";

    /** NBT key：进度缩放（用于兼容版本升级）。 */
    private static final String KEY_PROGRESS_SCALE = "progressScale";

    /** 组件方块全限定引用（避免重复长行）。 */
    private static final net.neoforged.neoforge.registries.DeferredBlock<
        com.Kizunad.guzhenrenext.xianqiao.resource.ResourceComponentBlock
    > COMPONENT_BLOCK = com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks.TIME_FIELD_COMPONENT;

    /** 底座结构要求：控制器下方必须是资源组件方块。 */
    private static final net.neoforged.neoforge.registries.DeferredBlock<
        com.Kizunad.guzhenrenext.xianqiao.resource.ResourceComponentBlock
    > RESOURCE_COMPONENT_BLOCK = com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks.RESOURCE_COMPONENT;

    /** 默认产出物。 */
    private static final ItemStack OUTPUT_TEMPLATE = new ItemStack(Items.CLOCK);

    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private float progress;

    private boolean isFormed;

    private int componentCount;

    private int structureRecheckCooldown;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case MENU_DATA_PROGRESS -> (int) (progress * PROGRESS_SCALE);
                case MENU_DATA_PROGRESS_MAX -> (int) (MAX_PROGRESS * PROGRESS_SCALE);
                case MENU_DATA_FORMED -> isFormed ? 1 : 0;
                case MENU_DATA_EFFICIENCY -> (int) (getCurrentEfficiency() * PERCENT_SCALE);
                case MENU_DATA_AURA -> getCurrentAura();
                case MENU_DATA_REMAINING -> getEstimatedRemainingTicks();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == MENU_DATA_PROGRESS) {
                progress = (float) value / PROGRESS_SCALE;
            }
        }

        @Override
        public int getCount() {
            return MENU_DATA_FIELDS;
        }
    };

    public ResourceControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(XianqiaoBlockEntities.RESOURCE_CONTROLLER.get(), pos, blockState);
        structureRecheckCooldown = STRUCTURE_RECHECK_INTERVAL;
    }

    /**
     * 服务端 tick 入口。
     */
    public static void serverTick(
        Level level,
        BlockPos pos,
        BlockState state,
        ResourceControllerBlockEntity blockEntity
    ) {
        if (level.isClientSide) {
            return;
        }
        if (state.isAir()) {
            return;
        }
        blockEntity.tickServer();
    }

    private void tickServer() {
        if (structureRecheckCooldown <= 0) {
            validateStructure();
            structureRecheckCooldown = STRUCTURE_RECHECK_INTERVAL;
        } else {
            structureRecheckCooldown--;
        }

        if (!isFormed) {
            return;
        }

        if (items.get(SLOT_INPUT).isEmpty()) {
            return;
        }

        float efficiency = getCurrentEfficiency();
        float timeMultiplier = getApertureTimeSpeed();
        float baseSpeed = BASE_SPEED_MIN + componentCount * BASE_SPEED_PER_COMPONENT;
        progress += baseSpeed * efficiency * timeMultiplier;

        while (progress >= MAX_PROGRESS) {
            if (!outputItems()) {
                progress = MAX_PROGRESS;
                break;
            }
            progress -= MAX_PROGRESS;
        }
        setChanged();
    }

    /**
     * 缓存结构状态：仅统计 3×3×3 范围内组件数量。
     */
    private void validateStructure() {
        if (level == null) {
            isFormed = false;
            componentCount = 0;
            return;
        }

        if (!checkStructure()) {
            isFormed = false;
            componentCount = 0;
            if (progress > 0) {
                progress = 0;
            }
            return;
        }

        if (COMPONENT_SCAN_DIMENSION != STRUCTURE_SCAN_RADIUS * 2 + 1) {
            throw new IllegalStateException("组件扫描维度常量配置错误");
        }

        int count = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int offsetX = -STRUCTURE_SCAN_RADIUS; offsetX <= STRUCTURE_SCAN_RADIUS; offsetX++) {
            for (int offsetY = -STRUCTURE_SCAN_RADIUS; offsetY <= STRUCTURE_SCAN_RADIUS; offsetY++) {
                for (int offsetZ = -STRUCTURE_SCAN_RADIUS; offsetZ <= STRUCTURE_SCAN_RADIUS; offsetZ++) {
                    if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
                        continue;
                    }
                    mutablePos.set(
                        worldPosition.getX() + offsetX,
                        worldPosition.getY() + offsetY,
                        worldPosition.getZ() + offsetZ
                    );
                    if (level.getBlockState(mutablePos).is(COMPONENT_BLOCK.get())) {
                        count++;
                    }
                }
            }
        }
        componentCount = count;
        isFormed = count >= MIN_COMPONENTS_REQUIRED;
        if (!isFormed && progress > 0) {
            progress = 0;
        }
    }

    private boolean checkStructure() {
        if (level == null) {
            return false;
        }
        BlockPos belowPos = worldPosition.below();
        return level.getBlockState(belowPos).is(RESOURCE_COMPONENT_BLOCK.get());
    }

    private int getCurrentAura() {
        if (level == null) {
            return 0;
        }
        return DaoMarkApi.getAura(level, worldPosition, DaoType.TIME);
    }

    private float getCurrentEfficiency() {
        int aura = getCurrentAura();
        if (aura < REQUIRED_AURA) {
            return LOW_EFFICIENCY;
        }
        if (aura > BONUS_AURA) {
            return BONUS_EFFICIENCY;
        }
        return NORMAL_EFFICIENCY;
    }

    private int getEstimatedRemainingTicks() {
        if (!isFormed) {
            return 0;
        }
        float baseSpeed = BASE_SPEED_MIN + componentCount * BASE_SPEED_PER_COMPONENT;
        float speed = baseSpeed * getCurrentEfficiency() * getApertureTimeSpeed();
        if (speed <= 0) {
            return 0;
        }
        float remaining = MAX_PROGRESS - progress;
        if (remaining <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(remaining / speed));
    }

    /**
     * 获取仙窍时间倍率。
     * <p>
     * 通过遍历该维度在线玩家的仙窍记录，寻找当前控制器坐标所在的仙窍区域。
     * 找不到时返回 1，确保逻辑稳定。
     * </p>
     */
    private float getApertureTimeSpeed() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 1.0F;
        }

        int controllerChunkX = SectionPos.blockToSectionCoord(worldPosition.getX());
        int controllerChunkZ = SectionPos.blockToSectionCoord(worldPosition.getZ());
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        List<? extends Player> players = serverLevel.players();
        for (Player player : players) {
            ApertureInfo info = worldData.getAperture(player.getUUID());
            if (info == null) {
                continue;
            }
            int allowedRadius = info.currentRadius() + APERTURE_RADIUS_BUFFER;
            long maxDistanceSquared = (long) allowedRadius * allowedRadius;
            long deltaX = worldPosition.getX() - info.center().getX();
            long deltaZ = worldPosition.getZ() - info.center().getZ();
            long horizontalDistanceSquared = deltaX * deltaX + deltaZ * deltaZ;
            if (horizontalDistanceSquared <= maxDistanceSquared) {
                int ownerCenterChunkX = SectionPos.blockToSectionCoord(info.center().getX());
                int ownerCenterChunkZ = SectionPos.blockToSectionCoord(info.center().getZ());
                int minChunkX = ownerCenterChunkX - (allowedRadius / CHUNK_SIZE) - 1;
                int maxChunkX = ownerCenterChunkX + (allowedRadius / CHUNK_SIZE) + 1;
                int minChunkZ = ownerCenterChunkZ - (allowedRadius / CHUNK_SIZE) - 1;
                int maxChunkZ = ownerCenterChunkZ + (allowedRadius / CHUNK_SIZE) + 1;
                boolean chunkMatched = controllerChunkX >= minChunkX
                    && controllerChunkX <= maxChunkX
                    && controllerChunkZ >= minChunkZ
                    && controllerChunkZ <= maxChunkZ;
                if (!chunkMatched) {
                    continue;
                }
                return info.timeSpeed();
            }
        }
        return 1.0F;
    }

    /**
     * 向内部库存投放产物。
     *
     * @return true 表示成功入库；false 表示槽位已满
     */
    private boolean outputItems() {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            ItemStack created = OUTPUT_TEMPLATE.copy();
            created.setCount(OUTPUT_COUNT_PER_CYCLE);
            items.set(SLOT_OUTPUT, created);
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, OUTPUT_TEMPLATE)) {
            return false;
        }
        if (output.getCount() >= output.getMaxStackSize()) {
            return false;
        }
        output.grow(OUTPUT_COUNT_PER_CYCLE);
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.guzhenrenext.xianqiao.resource_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ResourceControllerMenu(containerId, playerInventory, this, menuData);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat(KEY_PROGRESS, progress);
        tag.putBoolean(KEY_FORMED, isFormed);
        tag.putInt(KEY_COMPONENT_COUNT, componentCount);
        tag.putInt(KEY_STRUCTURE_COOLDOWN, structureRecheckCooldown);
        tag.putInt(KEY_PROGRESS_SCALE, PROGRESS_SCALE);
        CompoundTag itemsTag = new CompoundTag();
        ContainerHelper.saveAllItems(itemsTag, items, registries);
        tag.put(KEY_ITEMS, itemsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        clearItemList();
        progress = tag.getFloat(KEY_PROGRESS);
        int loadedProgressScale = tag.getInt(KEY_PROGRESS_SCALE);
        if (loadedProgressScale > 0 && loadedProgressScale != PROGRESS_SCALE) {
            progress = progress * loadedProgressScale / PROGRESS_SCALE;
        }
        isFormed = tag.getBoolean(KEY_FORMED);
        componentCount = tag.getInt(KEY_COMPONENT_COUNT);
        structureRecheckCooldown = tag.getInt(KEY_STRUCTURE_COOLDOWN);
        if (tag.contains(KEY_ITEMS)) {
            CompoundTag itemsTag = tag.getCompound(KEY_ITEMS);
            ContainerHelper.loadAllItems(itemsTag, items, registries);
        }
        normalizeInventoryAfterLoad();
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= INVENTORY_SIZE) {
            return ItemStack.EMPTY;
        }
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) {
            return;
        }
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_INPUT;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        clearItemList();
        setChanged();
    }

    private void clearItemList() {
        for (int index = 0; index < items.size(); index++) {
            items.set(index, ItemStack.EMPTY);
        }
    }

    private void normalizeInventoryAfterLoad() {
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index) == null) {
                items.set(index, ItemStack.EMPTY);
            }
        }
        if (items.get(SLOT_INPUT).isEmpty()) {
            items.set(SLOT_INPUT, ItemStack.EMPTY);
        }
    }
}
