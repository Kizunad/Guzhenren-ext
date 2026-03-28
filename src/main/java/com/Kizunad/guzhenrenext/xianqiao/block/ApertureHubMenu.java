package com.Kizunad.guzhenrenext.xianqiao.block;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.command.ApertureCommand;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.InitPhase;
import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureEntryFlowService;
import com.Kizunad.guzhenrenext.xianqiao.entry.XianqiaoUiProjection;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 仙窍中枢菜单（Aperture Hub）。
 * <p>
 * 该菜单仅负责服务端到客户端的数据同步与展示，不承载任何物品槽位：
 * 1) 通过 {@link ContainerData} 同步仙窍核心的 10 个状态字段；
 * 2) 不提供背包交互，因此 {@code quickMoveStack} 恒返回空；
 * 3) 使用玩家打开菜单时的位置作为有效性参考点，避免无效远距离操作。
 * </p>
 */
public class ApertureHubMenu extends AbstractContainerMenu {

    /** 字段索引：边界最小 chunk X。 */
    private static final int DATA_MIN_CHUNK_X = 0;

    /** 字段索引：边界最大 chunk X。 */
    private static final int DATA_MAX_CHUNK_X = 1;

    /** 字段索引：边界最小 chunk Z。 */
    private static final int DATA_MIN_CHUNK_Z = 2;

    /** 字段索引：边界最大 chunk Z。 */
    private static final int DATA_MAX_CHUNK_Z = 3;

    /** 字段索引：时间倍率百分值（原值 ×100）。 */
    private static final int DATA_TIME_SPEED_PERCENT = 4;

    /** 字段索引：灾劫刻低 16 位。 */
    private static final int DATA_TRIBULATION_LOW = 5;

    /** 字段索引：灾劫刻高 16 位。 */
    private static final int DATA_TRIBULATION_HIGH = 6;

    /** 字段索引：好感度百分值（原值 ×100）。 */
    private static final int DATA_FAVORABILITY_PERCENT = 7;

    /** 字段索引：转数。 */
    private static final int DATA_TIER = 8;

    /** 字段索引：冻结状态（0/1）。 */
    private static final int DATA_FROZEN = 9;

    private static final int DATA_INIT_PHASE = 10;

    private static final int DATA_ATTEMPT_STAGE = 11;

    private static final int DATA_HEAVEN_QI_SCORE = 12;

    private static final int DATA_HUMAN_QI_SCORE = 13;

    private static final int DATA_EARTH_QI_SCORE = 14;

    private static final int DATA_BALANCE_SCORE = 15;

    private static final int DATA_ASCENSION_FLAGS = 16;

    /** 容器数据总字段数。 */
    private static final int DATA_FIELDS = 17;

    public static final int BUTTON_ASCENSION_ENTRY = 0;

    private static final int FLAG_SHIFT_READY_TO_CONFIRM = 1;

    private static final int FLAG_SHIFT_CONFIRMED_THRESHOLD = 2;

    private static final int FLAG_SHIFT_CAN_ENTER_CONFIRMED = 3;

    private static final int FLAG_SHIFT_SNAPSHOT_FROZEN = 4;

    private static final int FLAG_SHIFT_FROZEN_SNAPSHOT_PLAYER_INITIATED = 5;

    private static final int FLAG_FIVE_TURN_PEAK = 1;

    private static final int FLAG_READY_TO_CONFIRM = 1 << FLAG_SHIFT_READY_TO_CONFIRM;

    private static final int FLAG_CONFIRMED_THRESHOLD = 1 << FLAG_SHIFT_CONFIRMED_THRESHOLD;

    private static final int FLAG_CAN_ENTER_CONFIRMED = 1 << FLAG_SHIFT_CAN_ENTER_CONFIRMED;

    private static final int FLAG_SNAPSHOT_FROZEN = 1 << FLAG_SHIFT_SNAPSHOT_FROZEN;

    private static final int FLAG_FROZEN_SNAPSHOT_PLAYER_INITIATED = 1 << FLAG_SHIFT_FROZEN_SNAPSHOT_PLAYER_INITIATED;

    /** 远程校验：允许交互的最大距离平方（8 格 -> 64）。 */
    private static final double MAX_VALID_DISTANCE_SQR = 64.0D;

    /** 位运算：提取低 16 位掩码。 */
    private static final long LOW_16_MASK = 0xFFFFL;

    /** 位运算：高位左移/还原使用的位数。 */
    private static final int HIGH_16_SHIFT = 16;

    private final ContainerData data;

    /** 菜单打开参考点 X 坐标。 */
    private final double originX;

    /** 菜单打开参考点 Y 坐标。 */
    private final double originY;

    /** 菜单打开参考点 Z 坐标。 */
    private final double originZ;

    /**
     * 客户端构造：使用空数据容器占位，等待服务端自动同步。
     *
     * @param containerId 菜单实例 ID
     * @param playerInventory 玩家背包
     */
    public ApertureHubMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainerData(DATA_FIELDS));
    }

    /**
     * 服务端构造：注入真实的菜单同步数据。
     *
     * @param containerId 菜单实例 ID
     * @param playerInventory 玩家背包
     * @param data 由方块实体提供的实时 ContainerData
     */
    public ApertureHubMenu(int containerId, Inventory playerInventory, ContainerData data) {
        super(XianqiaoMenus.APERTURE_HUB.get(), containerId);
        this.data = data;
        this.originX = playerInventory.player.getX();
        this.originY = playerInventory.player.getY();
        this.originZ = playerInventory.player.getZ();
        checkContainerDataCount(data, DATA_FIELDS);
        addDataSlots(data);
    }

    /**
     * 网络构造：客户端收到开菜单包时调用。
     * <p>
     * 按协议先读取方块坐标，确保网络流与服务端写入顺序一致。
     * 当前客户端菜单逻辑不直接使用该坐标，因此仅读取后丢弃。
     * </p>
     *
     * @param containerId 菜单实例 ID
     * @param inventory 玩家背包
     * @param buf 网络缓冲
     * @return 菜单实例
     */
    public static ApertureHubMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        buf.readBlockPos();
        return new ApertureHubMenu(containerId, inventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(originX, originY, originZ) <= MAX_VALID_DISTANCE_SQR;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_ASCENSION_ENTRY) {
            if (player.level().isClientSide) {
                return true;
            }
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return false;
            }
            ApertureEntryFlowService.EntryResult result = ApertureCommand.executeUnifiedAscensionEntry(
                serverPlayer,
                AscensionAttemptEntryChannel.PLAYER_INITIATED_ENTRY
            );
            if (!result.success() && result.failureMessage() != null) {
                serverPlayer.sendSystemMessage(result.failureMessage());
            }
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    /** 获取边界最小 chunk X。 */
    public int getMinChunkX() {
        return data.get(DATA_MIN_CHUNK_X);
    }

    /** 获取边界最大 chunk X。 */
    public int getMaxChunkX() {
        return data.get(DATA_MAX_CHUNK_X);
    }

    /** 获取边界最小 chunk Z。 */
    public int getMinChunkZ() {
        return data.get(DATA_MIN_CHUNK_Z);
    }

    /** 获取边界最大 chunk Z。 */
    public int getMaxChunkZ() {
        return data.get(DATA_MAX_CHUNK_Z);
    }

    /** 获取 X 轴 chunk 跨度（闭区间，含两端）。 */
    public int getChunkSpanX() {
        return getMaxChunkX() - getMinChunkX() + 1;
    }

    /** 获取 Z 轴 chunk 跨度（闭区间，含两端）。 */
    public int getChunkSpanZ() {
        return getMaxChunkZ() - getMinChunkZ() + 1;
    }

    /**
     * 获取时间倍率百分值（例如 125 表示 1.25x）。
     */
    public int getTimeSpeedPercent() {
        return data.get(DATA_TIME_SPEED_PERCENT);
    }

    /**
     * 获取下一次灾劫触发刻（由高低 16 位合并）。
     */
    public long getTribulationTick() {
        long low = data.get(DATA_TRIBULATION_LOW) & LOW_16_MASK;
        long high = data.get(DATA_TRIBULATION_HIGH) & LOW_16_MASK;
        return (high << HIGH_16_SHIFT) | low;
    }

    /**
     * 获取好感度百分值（例如 8750 表示 87.50%）。
     */
    public int getFavorabilityPercent() {
        return data.get(DATA_FAVORABILITY_PERCENT);
    }

    /**
     * 获取仙窍转数。
     */
    public int getTier() {
        return data.get(DATA_TIER);
    }

    /**
     * 当前是否处于冻结状态。
     */
    public boolean isFrozen() {
        return data.get(DATA_FROZEN) != 0;
    }

    public InitPhase getInitPhase() {
        int ordinal = data.get(DATA_INIT_PHASE);
        InitPhase[] values = InitPhase.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return InitPhase.UNINITIALIZED;
        }
        return values[ordinal];
    }

    public AscensionAttemptStage getAttemptStage() {
        int ordinal = data.get(DATA_ATTEMPT_STAGE);
        AscensionAttemptStage[] values = AscensionAttemptStage.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return AscensionAttemptStage.CULTIVATION_PROGRESS;
        }
        return values[ordinal];
    }

    public int getHeavenQiScorePercent() {
        return data.get(DATA_HEAVEN_QI_SCORE);
    }

    public int getHumanQiScorePercent() {
        return data.get(DATA_HUMAN_QI_SCORE);
    }

    public int getEarthQiScorePercent() {
        return data.get(DATA_EARTH_QI_SCORE);
    }

    public int getBalanceScorePercent() {
        return data.get(DATA_BALANCE_SCORE);
    }

    public boolean isFiveTurnPeak() {
        return hasFlag(FLAG_FIVE_TURN_PEAK);
    }

    public boolean isReadyToConfirm() {
        return hasFlag(FLAG_READY_TO_CONFIRM);
    }

    public boolean isConfirmedThresholdMet() {
        return hasFlag(FLAG_CONFIRMED_THRESHOLD);
    }

    public boolean canEnterConfirmed() {
        return hasFlag(FLAG_CAN_ENTER_CONFIRMED);
    }

    public boolean isSnapshotFrozen() {
        return hasFlag(FLAG_SNAPSHOT_FROZEN);
    }

    public boolean isFrozenSnapshotPlayerInitiated() {
        return hasFlag(FLAG_FROZEN_SNAPSHOT_PLAYER_INITIATED);
    }

    public XianqiaoUiProjection.ProjectionInput getProjectionInput() {
        return new XianqiaoUiProjection.ProjectionInput(
            getInitPhase(),
            getAttemptStage(),
            getHeavenQiScorePercent(),
            getHumanQiScorePercent(),
            getEarthQiScorePercent(),
            getBalanceScorePercent(),
            isFiveTurnPeak(),
            isReadyToConfirm(),
            isConfirmedThresholdMet(),
            canEnterConfirmed(),
            isSnapshotFrozen(),
            isFrozenSnapshotPlayerInitiated()
        );
    }

    public XianqiaoUiProjection.ProjectionSnapshot getUiProjection() {
        return XianqiaoUiProjection.project(getProjectionInput());
    }

    private boolean hasFlag(int flag) {
        return (data.get(DATA_ASCENSION_FLAGS) & flag) != 0;
    }
}
