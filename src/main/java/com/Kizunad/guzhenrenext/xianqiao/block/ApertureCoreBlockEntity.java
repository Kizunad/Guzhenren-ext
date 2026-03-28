package com.Kizunad.guzhenrenext.xianqiao.block;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.AscensionAttemptState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInitializationState;
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

    private static final String KEY_BOUND_SPIRIT_UUID = "boundSpiritUUID";

    /** 菜单字段总数。 */
    private static final int MENU_DATA_FIELDS = 17;

    /** 菜单字段索引：边界最小 chunk X。 */
    private static final int MENU_DATA_MIN_CHUNK_X = 0;

    /** 菜单字段索引：边界最大 chunk X。 */
    private static final int MENU_DATA_MAX_CHUNK_X = 1;

    /** 菜单字段索引：边界最小 chunk Z。 */
    private static final int MENU_DATA_MIN_CHUNK_Z = 2;

    /** 菜单字段索引：边界最大 chunk Z。 */
    private static final int MENU_DATA_MAX_CHUNK_Z = 3;

    /** 菜单字段索引：时间倍率（放大 100 倍）。 */
    private static final int MENU_DATA_TIME_SPEED = 4;

    /** 菜单字段索引：灾劫刻低 16 位。 */
    private static final int MENU_DATA_TRIBULATION_LOW = 5;

    /** 菜单字段索引：灾劫刻高 16 位。 */
    private static final int MENU_DATA_TRIBULATION_HIGH = 6;

    /** 菜单字段索引：好感度（放大 100 倍）。 */
    private static final int MENU_DATA_FAVORABILITY = 7;

    /** 菜单字段索引：转数。 */
    private static final int MENU_DATA_TIER = 8;

    /** 菜单字段索引：冻结状态（0/1）。 */
    private static final int MENU_DATA_FROZEN = 9;

    private static final int MENU_DATA_INIT_PHASE = 10;

    private static final int MENU_DATA_ATTEMPT_STAGE = 11;

    private static final int MENU_DATA_HEAVEN_QI_SCORE = 12;

    private static final int MENU_DATA_HUMAN_QI_SCORE = 13;

    private static final int MENU_DATA_EARTH_QI_SCORE = 14;

    private static final int MENU_DATA_BALANCE_SCORE = 15;

    private static final int MENU_DATA_ASCENSION_FLAGS = 16;

    /** 数值缩放：保留两位小数。 */
    private static final int SCALE_HUNDRED = 100;

    /** 位运算掩码：提取低 16 位。 */
    private static final int LOWER_16_MASK = 0xFFFF;

    /** 位移位数：提取高 16 位。 */
    private static final int HIGH_16_SHIFT = 16;

    private static final int FLAG_FIVE_TURN_PEAK = 1;

    private static final int FLAG_SHIFT_READY_TO_CONFIRM = 1;

    private static final int FLAG_SHIFT_CONFIRMED_THRESHOLD = 2;

    private static final int FLAG_SHIFT_CAN_ENTER_CONFIRMED = 3;

    private static final int FLAG_SHIFT_SNAPSHOT_FROZEN = 4;

    private static final int FLAG_SHIFT_FROZEN_SNAPSHOT_PLAYER_INITIATED = 5;

    private static final int FLAG_READY_TO_CONFIRM = 1 << FLAG_SHIFT_READY_TO_CONFIRM;

    private static final int FLAG_CONFIRMED_THRESHOLD = 1 << FLAG_SHIFT_CONFIRMED_THRESHOLD;

    private static final int FLAG_CAN_ENTER_CONFIRMED = 1 << FLAG_SHIFT_CAN_ENTER_CONFIRMED;

    private static final int FLAG_SNAPSHOT_FROZEN = 1 << FLAG_SHIFT_SNAPSHOT_FROZEN;

    private static final int FLAG_FROZEN_SNAPSHOT_PLAYER_INITIATED =
        1 << FLAG_SHIFT_FROZEN_SNAPSHOT_PLAYER_INITIATED;

    @Nullable
    private UUID ownerUUID;

    @Nullable
    private UUID boundSpiritUUID;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            @Nullable ApertureInfo apertureInfo = getOwnedApertureInfo();
            @Nullable ProjectionState projectionState = getProjectionState();
            if (apertureInfo == null && projectionState == null) {
                return 0;
            }
            return switch (index) {
                case MENU_DATA_MIN_CHUNK_X -> apertureInfo == null ? 0 : apertureInfo.minChunkX();
                case MENU_DATA_MAX_CHUNK_X -> apertureInfo == null ? 0 : apertureInfo.maxChunkX();
                case MENU_DATA_MIN_CHUNK_Z -> apertureInfo == null ? 0 : apertureInfo.minChunkZ();
                case MENU_DATA_MAX_CHUNK_Z -> apertureInfo == null ? 0 : apertureInfo.maxChunkZ();
                case MENU_DATA_TIME_SPEED ->
                    apertureInfo == null ? 0 : (int) (apertureInfo.timeSpeed() * SCALE_HUNDRED);
                case MENU_DATA_TRIBULATION_LOW ->
                    apertureInfo == null ? 0 : (int) (apertureInfo.nextTribulationTick() & LOWER_16_MASK);
                case MENU_DATA_TRIBULATION_HIGH ->
                    apertureInfo == null
                        ? 0
                        : (int) ((apertureInfo.nextTribulationTick() >>> HIGH_16_SHIFT) & LOWER_16_MASK);
                case MENU_DATA_FAVORABILITY ->
                    apertureInfo == null ? 0 : (int) (apertureInfo.favorability() * SCALE_HUNDRED);
                case MENU_DATA_TIER -> apertureInfo == null ? 0 : apertureInfo.tier();
                case MENU_DATA_FROZEN -> apertureInfo != null && apertureInfo.isFrozen() ? 1 : 0;
                case MENU_DATA_INIT_PHASE -> projectionState == null ? 0 : projectionState.initPhaseOrdinal();
                case MENU_DATA_ATTEMPT_STAGE -> projectionState == null ? 0 : projectionState.attemptStageOrdinal();
                case MENU_DATA_HEAVEN_QI_SCORE ->
                    projectionState == null ? 0 : projectionState.heavenQiScorePercent();
                case MENU_DATA_HUMAN_QI_SCORE ->
                    projectionState == null ? 0 : projectionState.humanQiScorePercent();
                case MENU_DATA_EARTH_QI_SCORE ->
                    projectionState == null ? 0 : projectionState.earthQiScorePercent();
                case MENU_DATA_BALANCE_SCORE ->
                    projectionState == null ? 0 : projectionState.balanceScorePercent();
                case MENU_DATA_ASCENSION_FLAGS -> projectionState == null ? 0 : projectionState.flags();
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
        return new ApertureHubMenu(containerId, playerInventory, menuData);
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

    @Nullable
    public UUID getBoundSpiritUUID() {
        return boundSpiritUUID;
    }

    public void setBoundSpiritUUID(@Nullable UUID spiritUUID) {
        boundSpiritUUID = spiritUUID;
        setChanged();
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

    @Nullable
    private ProjectionState getProjectionState() {
        UUID owner = getOwnerUUID();
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        ApertureInitializationState initializationState = worldData.getInitializationState(owner);
        AscensionAttemptState attemptState = worldData.getAscensionAttemptState(owner);
        return toProjectionState(initializationState, attemptState);
    }

    private static ProjectionState toProjectionState(
        ApertureInitializationState initializationState,
        AscensionAttemptState attemptState
    ) {
        int flags = 0;
        if (attemptState.fiveTurnPeak()) {
            flags |= FLAG_FIVE_TURN_PEAK;
        }
        if (attemptState.readyToConfirm()) {
            flags |= FLAG_READY_TO_CONFIRM;
        }
        if (attemptState.confirmedThresholdMet()) {
            flags |= FLAG_CONFIRMED_THRESHOLD;
        }
        if (attemptState.canEnterConfirmed()) {
            flags |= FLAG_CAN_ENTER_CONFIRMED;
        }
        if (attemptState.snapshotFrozen()) {
            flags |= FLAG_SNAPSHOT_FROZEN;
            if (attemptState.frozenSnapshotPlayerInitiated()) {
                flags |= FLAG_FROZEN_SNAPSHOT_PLAYER_INITIATED;
            }
        }
        return new ProjectionState(
            initializationState.initPhase().ordinal(),
            attemptState.stage().ordinal(),
            attemptState.heavenQiScorePercent(),
            attemptState.humanQiScorePercent(),
            attemptState.earthQiScorePercent(),
            attemptState.balanceScorePercent(),
            flags
        );
    }

    private record ProjectionState(
        int initPhaseOrdinal,
        int attemptStageOrdinal,
        int heavenQiScorePercent,
        int humanQiScorePercent,
        int earthQiScorePercent,
        int balanceScorePercent,
        int flags
    ) {
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
        if (boundSpiritUUID != null) {
            tag.putUUID(KEY_BOUND_SPIRIT_UUID, boundSpiritUUID);
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
        if (tag.hasUUID(KEY_BOUND_SPIRIT_UUID)) {
            boundSpiritUUID = tag.getUUID(KEY_BOUND_SPIRIT_UUID);
        } else {
            boundSpiritUUID = null;
        }
    }
}
