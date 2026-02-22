package com.Kizunad.guzhenrenext.xianqiao.alchemy.blockentity;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.material.MaterialPropertyResolver;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuData;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuItem;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.menu.AlchemyFurnaceMenu;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoBlockEntities;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureBoundaryService;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 炼丹炉方块实体。
 * <p>
 * 仅恢复菜单与炼制服务所需的最小容器能力。
 * </p>
 */
public class AlchemyFurnaceBlockEntity extends BlockEntity implements MenuProvider, Container {

    /** 主材槽位。 */
    public static final int SLOT_MAIN = 0;

    /** 辅材槽位 1。 */
    public static final int SLOT_AUX_1 = 1;

    /** 辅材槽位 2。 */
    public static final int SLOT_AUX_2 = 2;

    /** 辅材槽位 3。 */
    public static final int SLOT_AUX_3 = 3;

    /** 辅材槽位 4。 */
    public static final int SLOT_AUX_4 = 4;

    /** 产出槽位。 */
    public static final int SLOT_OUTPUT = 5;

    /** 转运蛊控制槽位。 */
    public static final int SLOT_TRANSFER_CONTROL = 6;

    /** 背包槽位总数。 */
    public static final int INVENTORY_SIZE = 7;

    /** NBT：物品列表存档键。 */
    private static final String KEY_ITEMS = "items";

    /** NBT：批量炼制模式开关存档键。 */
    private static final String KEY_BULK_MODE = "bulk_mode";

    /** NBT：批次大小存档键。 */
    private static final String KEY_BATCH_SIZE = "batch_size";

    /** 转运执行间隔（tick）。 */
    private static final long TRANSFER_INTERVAL_TICKS = 100L;

    /** 附近集群 NPC 扫描半径（方块）。 */
    private static final double TRANSFER_SCAN_RADIUS_BLOCKS = 8.0D;

    /** 单次路由最多搬运的不同物资条目数，避免单 tick 负载过高。 */
    private static final int MAX_TRANSFER_ENTRIES_PER_CYCLE = 16;

    /** 集群 NPC 背包中储物蛊槽位。 */
    private static final int CLUSTER_STORAGE_GU_SLOT = 0;

    /** 仙窍边界命中缓冲（方块）。与 Cluster/ResourceController 语义保持一致。 */
    private static final int APERTURE_BOUNDARY_BUFFER = 16;

    /** 批量炼制模式默认值：关闭。 */
    private static final boolean DEFAULT_BULK_MODE = false;

    /** 批次大小默认值：单次仅炼制 1 份。 */
    private static final int DEFAULT_BATCH_SIZE = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    /**
     * 是否启用批量炼制模式。
     * <p>
     * 该字段仅用于后续菜单/业务读取，不影响当前背包槽位行为。
     * </p>
     */
    private boolean bulkMode = DEFAULT_BULK_MODE;

    /**
     * 批量炼制的批次大小。
     * <p>
     * 该值最小为 1，防止出现 0 或负数导致业务含义不明确。
     * </p>
     */
    private int batchSize = DEFAULT_BATCH_SIZE;

    public AlchemyFurnaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(XianqiaoBlockEntities.ALCHEMY_FURNACE.get(), pos, blockState);
    }

    /**
     * 炼丹炉服务端 tick。
     *
     * @param level 当前维度
     * @param pos 当前方块坐标
     * @param state 当前方块状态
     * @param blockEntity 当前方块实体
     */
    public static void serverTick(
        Level level,
        BlockPos pos,
        BlockState state,
        AlchemyFurnaceBlockEntity blockEntity
    ) {
        if (level.isClientSide || state.isAir()) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        blockEntity.tickTransferRouting(serverLevel);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.guzhenrenext.xianqiao.alchemy_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AlchemyFurnaceMenu(containerId, playerInventory, this, new SimpleContainerData(0));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag itemsTag = new CompoundTag();
        ContainerHelper.saveAllItems(itemsTag, items, registries);
        tag.put(KEY_ITEMS, itemsTag);
        tag.putBoolean(KEY_BULK_MODE, bulkMode);
        tag.putInt(KEY_BATCH_SIZE, batchSize);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        clearContent();
        bulkMode = DEFAULT_BULK_MODE;
        batchSize = DEFAULT_BATCH_SIZE;
        if (tag.contains(KEY_ITEMS)) {
            ContainerHelper.loadAllItems(tag.getCompound(KEY_ITEMS), items, registries);
        }
        if (tag.contains(KEY_BULK_MODE)) {
            bulkMode = tag.getBoolean(KEY_BULK_MODE);
        }
        if (tag.contains(KEY_BATCH_SIZE)) {
            batchSize = Math.max(DEFAULT_BATCH_SIZE, tag.getInt(KEY_BATCH_SIZE));
        }
    }

    public boolean isBulkMode() {
        return bulkMode;
    }

    public void setBulkMode(boolean enabled) {
        bulkMode = enabled;
        setChanged();
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int size) {
        batchSize = Math.max(DEFAULT_BATCH_SIZE, size);
        setChanged();
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
        if (slot == SLOT_OUTPUT) {
            return false;
        }
        if (slot == SLOT_TRANSFER_CONTROL) {
            return isTransferGuItem(stack);
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    /**
     * 执行最小无线转运路由：仅在仙窍维度、且转运蛊激活时周期触发。
     */
    private void tickTransferRouting(ServerLevel serverLevel) {
        if (!isInApertureDimension(serverLevel)) {
            return;
        }
        if (!isTransferGuActivated()) {
            return;
        }
        if (serverLevel.getGameTime() % TRANSFER_INTERVAL_TICKS != 0L) {
            return;
        }
        @Nullable UUID furnaceOwnerUUID = passesTransferSecurityHook(serverLevel);
        if (furnaceOwnerUUID == null) {
            return;
        }

        AABB scanBox = new AABB(worldPosition).inflate(TRANSFER_SCAN_RADIUS_BLOCKS);
        List<ClusterNpcEntity> nearbyClusters = serverLevel.getEntitiesOfClass(ClusterNpcEntity.class, scanBox);
        int transferredEntries = 0;
        for (ClusterNpcEntity cluster : nearbyClusters) {
            if (transferredEntries >= MAX_TRANSFER_ENTRIES_PER_CYCLE) {
                break;
            }
            if (!isClusterOwnedBy(cluster, serverLevel, furnaceOwnerUUID)) {
                continue;
            }
            transferredEntries += routeFromClusterStorage(cluster, MAX_TRANSFER_ENTRIES_PER_CYCLE - transferredEntries);
        }
    }

    /**
     * 转运安全检查：仅当炼丹炉位置能解析到有效仙窍 owner 时才允许继续。
     * <p>
     * 这里采用与 ClusterNpcEntity / ResourceControllerBlockEntity 一致的判定策略：
     * 1) 基于方块位置命中仙窍边界；
     * 2) 若严格命中失败，再用 chunk + buffer 做缓冲命中；
     * 3) 命中则返回 owner UUID，未命中则返回 null（fail-closed）。
     * </p>
     */
    @Nullable
    private UUID passesTransferSecurityHook(ServerLevel serverLevel) {
        return resolveApertureOwnerUUIDByPosition(serverLevel, worldPosition);
    }

    /**
     * 校验集群 owner 是否与炼丹炉 owner 一致。
     * <p>
     * 任一上下文不可解析（cluster owner 为 null）都视为不可信来源并拒绝路由，
     * 仅在 UUID 完全相等时放行，确保物流不会跨 owner 泄漏。
     * </p>
     */
    private boolean isClusterOwnedBy(ClusterNpcEntity cluster, ServerLevel serverLevel, UUID furnaceOwnerUUID) {
        @Nullable UUID clusterOwnerUUID = resolveApertureOwnerUUIDByPosition(serverLevel, cluster.blockPosition());
        return clusterOwnerUUID != null && clusterOwnerUUID.equals(furnaceOwnerUUID);
    }

    /**
     * 按“边界命中 + 缓冲命中”解析方块位置归属的仙窍 owner UUID。
     * <p>
     * 返回约定：
     * 1) 命中某个 owner 的 aperture 上下文 -> 返回该 owner UUID；
     * 2) 未命中或上下文无效 -> 返回 null。
     * </p>
     */
    @Nullable
    private UUID resolveApertureOwnerUUIDByPosition(ServerLevel serverLevel, BlockPos position) {
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        int chunkX = SectionPos.blockToSectionCoord(position.getX());
        int chunkZ = SectionPos.blockToSectionCoord(position.getZ());
        for (Map.Entry<UUID, ApertureInfo> entry : worldData.getAllApertures().entrySet()) {
            ApertureInfo info = entry.getValue();
            if (info == null) {
                continue;
            }
            boolean insideAperture = ApertureBoundaryService.containsBlock(info, position);
            boolean insideBufferedAperture = ApertureBoundaryService.containsChunkWithBlockBuffer(
                info,
                chunkX,
                chunkZ,
                APERTURE_BOUNDARY_BUFFER
            );
            if (insideAperture || insideBufferedAperture) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 处理单个 ClusterNpcEntity 的储物蛊库存路由。
     */
    private int routeFromClusterStorage(ClusterNpcEntity cluster, int remainingBudget) {
        if (remainingBudget <= 0) {
            return 0;
        }
        ItemStack storageStack = cluster.getInventory().getItem(CLUSTER_STORAGE_GU_SLOT);
        if (storageStack.isEmpty() || !(storageStack.getItem() instanceof StorageGuItem storageGuItem)) {
            return 0;
        }
        StorageGuData.StorageGuHandler handler = storageGuItem.getStorageHandler(storageStack);
        Map<ResourceLocation, Long> snapshot = handler.snapshot();
        int movedEntries = 0;
        for (Map.Entry<ResourceLocation, Long> entry : snapshot.entrySet()) {
            if (movedEntries >= remainingBudget) {
                break;
            }
            movedEntries += routeSingleEntry(handler, entry.getKey(), entry.getValue());
        }
        return movedEntries;
    }

    /**
     * 路由单个物资条目，保证“目标满时不丢失”。
     */
    private int routeSingleEntry(StorageGuData.StorageGuHandler handler, ResourceLocation itemId, long availableCount) {
        if (availableCount <= 0L) {
            return 0;
        }
        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            return 0;
        }
        ItemStack probeStack = new ItemStack(BuiltInRegistries.ITEM.get(itemId));
        if (probeStack.isEmpty() || isTransferGuItem(probeStack)) {
            return 0;
        }

        SlotPlan slotPlan = planTargetSlot(probeStack, availableCount);
        if (slotPlan.slotIndex() < 0 || slotPlan.moveCount() <= 0) {
            return 0;
        }

        long extractedLong = handler.extract(itemId, slotPlan.moveCount());
        if (extractedLong <= 0L) {
            return 0;
        }
        int extracted = toSafeInt(extractedLong);
        int placed = placeIntoSlot(slotPlan.slotIndex(), probeStack, extracted);
        if (placed < extracted) {
            handler.insert(itemId, (long) extracted - placed);
        }
        return placed > 0 ? 1 : 0;
    }

    /**
     * 按“主材优先，其次辅材”规划目标槽位与最大可搬运量。
     */
    private SlotPlan planTargetSlot(ItemStack probeStack, long availableCount) {
        int availableInt = toSafeInt(availableCount);
        int mainRoom = calculateSlotRoom(SLOT_MAIN, probeStack);
        if (mainRoom > 0 && canRouteToMainSlot(probeStack)) {
            return new SlotPlan(SLOT_MAIN, Math.min(mainRoom, availableInt));
        }
        for (int slot = SLOT_AUX_1; slot <= SLOT_AUX_4; slot++) {
            int room = calculateSlotRoom(slot, probeStack);
            if (room > 0 && canRouteToAuxSlot(probeStack)) {
                return new SlotPlan(slot, Math.min(room, availableInt));
            }
        }
        return SlotPlan.EMPTY;
    }

    /**
     * 计算目标槽位还能容纳的数量。
     */
    private int calculateSlotRoom(int slot, ItemStack incoming) {
        if (slot < SLOT_MAIN || slot > SLOT_AUX_4) {
            return 0;
        }
        if (!canPlaceItem(slot, incoming)) {
            return 0;
        }
        ItemStack current = getItem(slot);
        int maxStackSize = Math.min(incoming.getMaxStackSize(), getMaxStackSize());
        if (current.isEmpty()) {
            return maxStackSize;
        }
        if (!ItemStack.isSameItemSameComponents(current, incoming)) {
            return 0;
        }
        int room = maxStackSize - current.getCount();
        return Math.max(room, 0);
    }

    /**
     * 将数量写入指定输入槽位，并返回实际写入数量。
     */
    private int placeIntoSlot(int slot, ItemStack probeStack, int amount) {
        if (amount <= 0) {
            return 0;
        }
        ItemStack current = getItem(slot);
        if (current.isEmpty()) {
            ItemStack placedStack = probeStack.copyWithCount(amount);
            setItem(slot, placedStack);
            return amount;
        }
        if (!ItemStack.isSameItemSameComponents(current, probeStack)) {
            return 0;
        }
        int room = calculateSlotRoom(slot, probeStack);
        int placed = Math.min(room, amount);
        if (placed <= 0) {
            return 0;
        }
        current.grow(placed);
        setItem(slot, current);
        return placed;
    }

    /**
     * 判断物品是否可作为主材（与炼丹主材解析保持一致）。
     */
    private boolean canRouteToMainSlot(ItemStack stack) {
        return MaterialPropertyResolver.resolve(stack).isPresent();
    }

    /**
     * 判断物品是否可作为辅材。
     * 当前最小实现：除转运蛊外，非空物品均允许进入辅材槽。
     */
    private boolean canRouteToAuxSlot(ItemStack stack) {
        return !stack.isEmpty() && !isTransferGuItem(stack);
    }

    /**
     * 仅当主材槽是 transfer_gu 时视为无线转运激活。
     */
    private boolean isTransferGuActivated() {
        return isTransferGuItem(getItem(SLOT_TRANSFER_CONTROL));
    }

    private boolean isTransferGuItem(ItemStack stack) {
        return !stack.isEmpty() && stack.is(XianqiaoItems.TRANSFER_GU.get());
    }

    private boolean isInApertureDimension(ServerLevel level) {
        return level.dimension().equals(DaoMarkDiffusionService.APERTURE_DIMENSION);
    }

    private int toSafeInt(long count) {
        return (int) Math.min(count, Integer.MAX_VALUE);
    }

    /**
     * 槽位规划结果。
     */
    private record SlotPlan(int slotIndex, int moveCount) {
        private static final SlotPlan EMPTY = new SlotPlan(-1, 0);
    }
}
