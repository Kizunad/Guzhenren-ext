package com.Kizunad.guzhenrenext.xianqiao.block;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoBlockEntities;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 仙窍核心方块实体。
 * <p>
 * 当前职责：
 * 1) 记录并持久化核心所属玩家 UUID；
 * 2) 作为菜单提供者对外暴露仙窍状态快照；
 * 3) 在菜单尚未实现前保持 createMenu 返回 null，确保编译通过。
 * </p>
 */
public class ApertureCoreBlockEntity extends BlockEntity implements MenuProvider {

    /** NBT key：核心归属者 UUID。 */
    private static final String KEY_OWNER_UUID = "ownerUUID";

    /** 菜单字段总数。 */
    private static final int MENU_DATA_FIELDS = 7;

    /** 菜单字段索引：当前半径。 */
    private static final int MENU_DATA_RADIUS = 0;

    /** 菜单字段索引：时间倍率（放大 100 倍）。 */
    private static final int MENU_DATA_TIME_SPEED = 1;

    /** 菜单字段索引：灾劫刻低 16 位。 */
    private static final int MENU_DATA_TRIBULATION_LOW = 2;

    /** 菜单字段索引：灾劫刻高 16 位。 */
    private static final int MENU_DATA_TRIBULATION_HIGH = 3;

    /** 菜单字段索引：好感度（放大 100 倍）。 */
    private static final int MENU_DATA_FAVORABILITY = 4;

    /** 菜单字段索引：转数。 */
    private static final int MENU_DATA_TIER = 5;

    /** 菜单字段索引：冻结状态（0/1）。 */
    private static final int MENU_DATA_FROZEN = 6;

    /** 数值缩放：保留两位小数。 */
    private static final int SCALE_HUNDRED = 100;

    /** 位运算掩码：提取低 16 位。 */
    private static final int LOWER_16_MASK = 0xFFFF;

    /** 位移位数：提取高 16 位。 */
    private static final int HIGH_16_SHIFT = 16;

    @Nullable
    private UUID ownerUUID;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            @Nullable ApertureInfo apertureInfo = getOwnedApertureInfo();
            if (apertureInfo == null) {
                return 0;
            }
            return switch (index) {
                case MENU_DATA_RADIUS -> apertureInfo.currentRadius();
                case MENU_DATA_TIME_SPEED -> (int) (apertureInfo.timeSpeed() * SCALE_HUNDRED);
                case MENU_DATA_TRIBULATION_LOW -> (int) (apertureInfo.nextTribulationTick() & LOWER_16_MASK);
                case MENU_DATA_TRIBULATION_HIGH ->
                    (int) ((apertureInfo.nextTribulationTick() >>> HIGH_16_SHIFT) & LOWER_16_MASK);
                case MENU_DATA_FAVORABILITY -> (int) (apertureInfo.favorability() * SCALE_HUNDRED);
                case MENU_DATA_TIER -> apertureInfo.tier();
                case MENU_DATA_FROZEN -> apertureInfo.isFrozen() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // 仙窍核心菜单仅做展示，不提供客户端反向写入。
        }

        @Override
        public int getCount() {
            return MENU_DATA_FIELDS;
        }
    };

    public ApertureCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(XianqiaoBlockEntities.APERTURE_CORE.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.guzhenrenext.xianqiao.aperture_hub");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null;
    }

    /**
     * 查找并缓存该核心的归属者 UUID。
     *
     * @return 归属玩家 UUID；若当前无法匹配则返回 null
     */
    @Nullable
    public UUID findOwnerUUID() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return ownerUUID;
        }
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        Map<UUID, ApertureInfo> allApertures = worldData.getAllApertures();
        for (Map.Entry<UUID, ApertureInfo> entry : allApertures.entrySet()) {
            if (entry.getValue().center().equals(worldPosition)) {
                ownerUUID = entry.getKey();
                return ownerUUID;
            }
        }
        return ownerUUID;
    }

    @Nullable
    public UUID getOwnerUUID() {
        if (ownerUUID == null) {
            return findOwnerUUID();
        }
        return ownerUUID;
    }

    public ContainerData getMenuData() {
        return menuData;
    }

    @Nullable
    private ApertureInfo getOwnedApertureInfo() {
        UUID owner = getOwnerUUID();
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return ApertureWorldData.get(serverLevel).getAperture(owner);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID == null) {
            findOwnerUUID();
        }
        if (ownerUUID != null) {
            tag.putUUID(KEY_OWNER_UUID, ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID(KEY_OWNER_UUID)) {
            ownerUUID = tag.getUUID(KEY_OWNER_UUID);
        } else {
            ownerUUID = null;
        }
    }
}
