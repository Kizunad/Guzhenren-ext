package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 基地接管状态服务。
 * <p>
 * 负责依据 Round 10.1 的接管入口，将基地标记为可接管状态并落盘。
 * 当前仅实现“净化阵法完成”入口，其他入口将在后续回合补充。
 * </p>
 */
public final class BastionCaptureService {

    private BastionCaptureService() {
    }

    /**
     * 尝试通过净化阵法完成将基地标记为可接管状态。
     * <p>
     * 仅在配置启用 capture 且当前尚未处于可接管状态时更新。超时窗口按配置设置；
     * capturableTimeoutTicks 为 0 表示无限期可接管。
     * </p>
     *
     * @param level   服务器世界
     * @param bastion 目标基地数据
     * @return 是否成功标记为可接管
     */
    public static boolean tryMarkCapturableViaPurification(ServerLevel level, BastionData bastion) {
        if (level == null || bastion == null) {
            return false;
        }

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.CaptureConfig capture = typeConfig.capture();

        // 兼容策略：未启用接管系统则不标记，保持旧存档行为。
        if (capture == null || !capture.enabled()) {
            return false;
        }

        BastionData.CaptureState currentCapture = bastion.captureState() == null
            ? BastionData.CaptureState.DEFAULT
            : bastion.captureState();

        // 已经可接管则不重复标记，避免无谓落盘。
        if (currentCapture.capturable()) {
            return false;
        }

        long timeout = Math.max(0L, capture.capturableTimeoutTicks());
        long untilGameTime = timeout > 0 ? level.getGameTime() + timeout : 0L;

        BastionData updated = bastion.withCapturable(true, BastionData.CaptureReason.PURIFICATION_READY, untilGameTime);
        BastionSavedData.get(level).updateBastion(updated);
        return true;
    }

    /**
     * 尝试通过击杀 Boss 将基地标记为可接管状态。
     * <p>
     * 注意：根据 F1 规则，击杀 Boss 只能进入"可接管状态"；
     * 最终接管仍需净化阵法完成（调用 {@link #tryFinalizeCapture}）。
     * </p>
     *
     * @param level  服务器世界
     * @param bastion 目标基地数据
     * @return 是否成功标记为可接管
     */
    public static boolean tryMarkCapturableViaBossDefeat(ServerLevel level, BastionData bastion) {
        if (level == null || bastion == null) {
            return false;
        }

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.CaptureConfig capture = typeConfig.capture();

        // 兼容策略：未启用接管系统则不标记，保持旧存档行为。
        if (capture == null || !capture.enabled()) {
            return false;
        }

        BastionData.CaptureState currentCapture = bastion.captureState() == null
            ? BastionData.CaptureState.DEFAULT
            : bastion.captureState();

        // 已经可接管则不重复标记，避免无谓落盘。
        if (currentCapture.capturable()) {
            return false;
        }

        long timeout = Math.max(0L, capture.capturableTimeoutTicks());
        long untilGameTime = timeout > 0 ? level.getGameTime() + timeout : 0L;

        BastionData updated = bastion.withCapturable(true, BastionData.CaptureReason.BOSS_DEFEATED, untilGameTime);
        BastionSavedData.get(level).updateBastion(updated);
        return true;
    }

    /**
     * 尝试最终占领基地。
     * <p>
     * 前置条件：基地处于 capturable 状态且窗口未超时。
     * 占领后设置 captured=true, capturedBy=玩家UUID，清理 capturable 状态。
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 目标基地
     * @param player  占领者
     * @return 是否成功占领
     */
    public static boolean tryFinalizeCapture(ServerLevel level, BastionData bastion, ServerPlayer player) {
        if (level == null || bastion == null || player == null) {
            return false;
        }

        BastionData.CaptureState captureState = bastion.captureState();
        if (captureState == null) {
            captureState = BastionData.CaptureState.DEFAULT;
        }

        // 检查可接管状态
        if (!captureState.capturable()) {
            return false;
        }

        // 检查窗口超时
        long gameTime = level.getGameTime();
        if (captureState.capturableUntilGameTime() > 0
            && gameTime > captureState.capturableUntilGameTime()) {
            return false;
        }

        // 检查并消耗念头
        double currentNiantou = NianTouHelper.getAmount(player);
        if (currentNiantou < CaptureConstants.CAPTURE_NIANTOU_COST) {
            return false;
        }
        NianTouHelper.modify(player, -CaptureConstants.CAPTURE_NIANTOU_COST);

        // 设置为已占领
        BastionData updated = bastion.withCaptured(player.getUUID(), gameTime);
        BastionSavedData.get(level).updateBastion(updated);

        // 转换该基地所有守卫的阵营
        convertGuardiansToFriendly(level, bastion, player.getUUID());

        return true;
    }

    /** 占领相关常量。 */
    private static final class CaptureConstants {
        /** 占领基地所需念头消耗。 */
        static final double CAPTURE_NIANTOU_COST = 100.0;
        /** 搜索守卫的水平半径。 */
        static final int GUARDIAN_SEARCH_RADIUS = 64;
        /** 搜索守卫的垂直半径。 */
        static final int GUARDIAN_SEARCH_HEIGHT = 32;

        private CaptureConstants() {
        }
    }

    /**
     * 转换基地守卫为友方（不攻击占领者）。
     */
    private static void convertGuardiansToFriendly(ServerLevel level, BastionData bastion, UUID ownerId) {
        final int minRadius = CaptureConstants.GUARDIAN_SEARCH_RADIUS;
        final int halfHeight = CaptureConstants.GUARDIAN_SEARCH_HEIGHT;
        BlockPos core = bastion.corePos();
        int radius = Math.max(bastion.growthRadius(), minRadius);
        AABB box = new AABB(
            core.getX() - radius, core.getY() - halfHeight, core.getZ() - radius,
            core.getX() + radius, core.getY() + halfHeight, core.getZ() + radius
        );

        level.getEntitiesOfClass(Mob.class, box, mob ->
            BastionGuardianData.belongsToBastion(mob, bastion.id())
        ).forEach(mob -> {
            BastionGuardianData.markAsCaptured(mob, ownerId);
            if (mob.getTarget() instanceof Player target && target.getUUID().equals(ownerId)) {
                mob.setTarget(null);
            }
        });
    }

    // TODO Round 34：在净化阵法完成的事件回调中调用此方法，
    // 例如在净化阵法完成后：BastionCaptureService.tryMarkCapturableViaPurification(level, bastionData)

    // TODO Round 34：在 Boss 死亡事件回调中调用此方法，
    // 例如在 Boss 击杀后：BastionCaptureService.tryMarkCapturableViaBossDefeat(level, bastionData)
}
