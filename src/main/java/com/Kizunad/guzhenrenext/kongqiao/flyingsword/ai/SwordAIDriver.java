package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.motion.SwordMotionDriver;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 飞剑 AI 驱动入口。
 * <p>
 * 负责根据当前 AI 模式调度对应的行为逻辑。
 * 从 {@code FlyingSwordEntity.tick()} 中提取，统一 AI 驱动。
 * </p>
 * <p>
 * 设计原则：
 * <ul>
 *     <li>AI 驱动不直接修改实体状态，而是通过 Motion/Combat 模块间接操作</li>
 *     <li>目标缓存由调用方维护，Driver 只负责获取和传递</li>
 *     <li>每 tick 调用一次 {@link #tickAI}</li>
 * </ul>
 * </p>
 */
public final class SwordAIDriver {

    private SwordAIDriver() {}

    /**
     * AI tick 主入口。
     * <p>
     * 应在 {@code FlyingSwordEntity.tick()} 的服务端分支中调用。
     * </p>
     *
     * @param sword        飞剑实体
     * @param owner        主人（已验证非 null 且存活）
     * @param cachedTarget 缓存的目标（可为 null）
     * @return 本 tick 的目标（用于外部缓存更新）
     */
    @Nullable
    public static LivingEntity tickAI(
            FlyingSwordEntity sword,
            LivingEntity owner,
            @Nullable LivingEntity cachedTarget
    ) {
        if (sword == null || owner == null || owner.isRemoved()) {
            return null;
        }

        SwordAIMode mode = sword.getAIModeEnum();

        return switch (mode) {
            case ORBIT -> {
                tickOrbit(sword, owner);
                yield null;
            }
            case HOVER -> {
                tickHover(sword, owner);
                yield null;
            }
            case GUARD -> tickGuard(sword, owner, cachedTarget);
            case HUNT -> tickHunt(sword, owner, cachedTarget);
            case RECALL -> {
                tickRecall(sword, owner);
                yield null;
            }
        };
    }

    /**
     * ORBIT：环绕模式。
     * <p>
     * 围绕主人旋转飞行，不主动攻击。
     * </p>
     */
    private static void tickOrbit(FlyingSwordEntity sword, LivingEntity owner) {
        SwordMotionDriver.tickOrbit(sword, owner);
    }

    /**
     * HOVER：悬停模式。
     * <p>
     * 在主人头顶静止悬停。
     * </p>
     */
    private static void tickHover(FlyingSwordEntity sword, LivingEntity owner) {
        SwordMotionDriver.tickHover(sword, owner);
    }

    /**
     * GUARD：防御模式。
     * <p>
     * 守护主人周围，攻击进入范围的敌对目标。
     * 无目标时回退到环绕。
     * </p>
     *
     * @return 当前目标（用于缓存）
     */
    @Nullable
    private static LivingEntity tickGuard(
            FlyingSwordEntity sword,
            LivingEntity owner,
            @Nullable LivingEntity cachedTarget
    ) {
        LivingEntity target = SwordTargeting.acquireTargetGuard(sword, owner, cachedTarget);

        if (target == null) {
            // 无目标，回退到环绕
            SwordMotionDriver.tickOrbit(sword, owner);
            return null;
        }

        // 有目标，进入战斗追击
        SwordCombatOps.tickCombatPursuit(
                sword,
                owner,
                target,
                FlyingSwordConstants.GUARD_CHASE_SPEED_SCALE
        );

        return target;
    }

    /**
     * HUNT：狩猎模式。
     * <p>
     * 主动搜索并追击目标，范围更大、更激进。
     * 距离主人过远时回退到环绕（脱缰保护）。
     * </p>
     *
     * @return 当前目标（用于缓存）
     */
    @Nullable
    private static LivingEntity tickHunt(
            FlyingSwordEntity sword,
            LivingEntity owner,
            @Nullable LivingEntity cachedTarget
    ) {
        // 脱缰检查
        if (SwordCombatOps.isTooFarFromOwner(sword, owner, FlyingSwordConstants.HUNT_LEASH_RANGE)) {
            SwordMotionDriver.tickOrbit(sword, owner);
            return null;
        }

        LivingEntity target = SwordTargeting.acquireTargetHunt(sword, owner, cachedTarget);

        if (target == null) {
            // 无目标，回退到环绕
            SwordMotionDriver.tickOrbit(sword, owner);
            return null;
        }

        // 有目标，进入战斗追击
        SwordCombatOps.tickCombatPursuit(
                sword,
                owner,
                target,
                FlyingSwordConstants.HUNT_CHASE_SPEED_SCALE
        );

        return target;
    }

    /**
     * RECALL：召回模式。
     * <p>
     * 返回主人身边，到达后存入存储。
     * </p>
     */
    private static void tickRecall(FlyingSwordEntity sword, LivingEntity owner) {
        Vec3 target = owner.position().add(
                0.0,
                owner.getBbHeight() * FlyingSwordConstants.OWNER_HEIGHT_ORBIT,
                0.0
        );

        Vec3 delta = target.subtract(sword.position());
        double distSq = delta.lengthSqr();
        double finishDistSq = FlyingSwordConstants.RECALL_FINISH_DISTANCE
                * FlyingSwordConstants.RECALL_FINISH_DISTANCE;

        if (distSq <= finishDistSq) {
            // 到达，执行召回完成
            if (owner instanceof Player player) {
                FlyingSwordController.finishRecall(sword, player);
            } else {
                sword.discard();
            }
            return;
        }

        // 尚未到达，继续移动
        SwordMotionDriver.tickRecall(sword, owner);
    }

    /**
     * 检查是否应该从战斗模式回退。
     * <p>
     * 用于外部判断（如 UI 显示状态）。
     * </p>
     */
    public static boolean shouldFallbackToOrbit(
            FlyingSwordEntity sword,
            LivingEntity owner,
            @Nullable LivingEntity target
    ) {
        if (target == null || target.isRemoved() || target.isDeadOrDying()) {
            return true;
        }

        SwordAIMode mode = sword.getAIModeEnum();

        if (mode == SwordAIMode.HUNT) {
            return SwordCombatOps.isTooFarFromOwner(sword, owner, FlyingSwordConstants.HUNT_LEASH_RANGE);
        }

        return false;
    }
}
