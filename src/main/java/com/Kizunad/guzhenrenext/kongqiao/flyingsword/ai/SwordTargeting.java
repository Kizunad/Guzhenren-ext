package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 飞剑目标获取（集中式）。
 * <p>
 * 从 {@code FlyingSwordEntity} 中提取，统一 GUARD/HUNT 的目标扫描逻辑。
 * </p>
 */
public final class SwordTargeting {

    private SwordTargeting() {}

    /**
     * 获取主人最近攻击的目标（优先响应"帮我打他"）。
     */
    @Nullable
    public static LivingEntity resolveOwnerAttackTarget(
        @Nullable LivingEntity owner
    ) {
        if (owner == null) {
            return null;
        }

        // 主人最近攻击到的目标
        LivingEntity lastHurt = owner.getLastHurtMob();
        if (lastHurt != null && lastHurt.isAlive()) {
            return lastHurt;
        }

        // 如果主人被骑乘控制，尝试取骑乘者的攻击目标
        Entity controller = owner.getControllingPassenger();
        if (controller instanceof LivingEntity living) {
            LivingEntity controllerTarget = living.getLastHurtMob();
            if (controllerTarget != null && controllerTarget.isAlive()) {
                return controllerTarget;
            }
        }

        return null;
    }

    /**
     * GUARD 模式目标获取。
     * <p>
     * 优先响应主人攻击目标，其次扫描主人周围的敌对生物。
     * </p>
     */
    @Nullable
    public static LivingEntity acquireTargetGuard(
        FlyingSwordEntity sword,
        LivingEntity owner,
        @Nullable LivingEntity cachedTarget
    ) {
        if (
            owner == null || !(sword.level() instanceof ServerLevel serverLevel)
        ) {
            return null;
        }

        final double range = FlyingSwordConstants.GUARD_RANGE;

        // 优先锁定主人最近攻击目标
        LivingEntity ownerTarget = resolveOwnerAttackTarget(owner);
        if (isValidTarget(sword, owner, ownerTarget, range)) {
            return ownerTarget;
        }

        // 非扫描 tick 返回缓存
        if (!shouldScanThisTick(sword)) {
            return isValidTarget(sword, owner, cachedTarget, range)
                ? cachedTarget
                : null;
        }

        // 扫描主人周围
        return scanNearestHostile(
            serverLevel,
            sword,
            owner,
            owner.position(),
            range,
            true
        );
    }

    /**
     * HUNT 模式目标获取。
     * <p>
     * 更激进：以飞剑自身为中心扫描，范围更大。
     * </p>
     */
    @Nullable
    public static LivingEntity acquireTargetHunt(
        FlyingSwordEntity sword,
        LivingEntity owner,
        @Nullable LivingEntity cachedTarget
    ) {
        if (
            owner == null || !(sword.level() instanceof ServerLevel serverLevel)
        ) {
            return null;
        }

        final double range = FlyingSwordConstants.HUNT_RANGE;

        // 优先锁定主人最近攻击目标
        LivingEntity ownerTarget = resolveOwnerAttackTarget(owner);
        if (isValidTarget(sword, owner, ownerTarget, range)) {
            return ownerTarget;
        }

        // 非扫描 tick 返回缓存
        if (!shouldScanThisTick(sword)) {
            return isValidTarget(sword, owner, cachedTarget, range)
                ? cachedTarget
                : null;
        }

        // 扫描飞剑周围（HUNT 以飞剑为中心）
        return scanNearestHostile(
            serverLevel,
            sword,
            owner,
            sword.position(),
            range,
            false
        );
    }

    /**
     * 扫描最近的敌对目标。
     *
     * @param level       服务端世界
     * @param sword       飞剑实体
     * @param owner       主人
     * @param center      扫描中心
     * @param range       扫描范围
     * @param hostileOnly 是否仅限敌对生物（GUARD 为 true）
     * @return 最近的有效目标，或 null
     */
    @Nullable
    private static LivingEntity scanNearestHostile(
        ServerLevel level,
        FlyingSwordEntity sword,
        LivingEntity owner,
        net.minecraft.world.phys.Vec3 center,
        double range,
        boolean hostileOnly
    ) {
        // 垂直范围为水平范围的一半
        final double verticalRangeFactor = 0.5;
        AABB box = new AABB(center, center).inflate(
            range,
            range * verticalRangeFactor,
            range
        );

        Predicate<LivingEntity> filter = entity -> {
            if (!isValidTarget(sword, owner, entity, range)) {
                return false;
            }
            if (hostileOnly) {
                return isHostile(entity);
            }
            return true;
        };

        List<LivingEntity> candidates = level.getEntitiesOfClass(
            LivingEntity.class,
            box,
            filter
        );

        return candidates
            .stream()
            .min(Comparator.comparingDouble(e -> e.distanceToSqr(sword)))
            .orElse(null);
    }

    /**
     * 目标有效性检查（通用）。
     */
    public static boolean isValidTarget(
        FlyingSwordEntity sword,
        @Nullable LivingEntity owner,
        @Nullable LivingEntity candidate,
        double range
    ) {
        if (owner == null || candidate == null) {
            return false;
        }
        if (candidate.isRemoved() || candidate.isDeadOrDying()) {
            return false;
        }
        if (candidate == owner) {
            return false;
        }
        // 不攻击其他玩家（PvP 后续单独处理）
        if (candidate instanceof Player) {
            return false;
        }
        // 不攻击其他飞剑
        if (candidate instanceof FlyingSwordEntity) {
            return false;
        }
        // 距离检查（相对于主人）
        if (candidate.distanceToSqr(owner) > range * range) {
            return false;
        }
        // 视线检查
        return (
            owner.hasLineOfSight(candidate) || sword.hasLineOfSight(candidate)
        );
    }

    /**
     * 判断实体是否为敌对生物。
     */
    public static boolean isHostile(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof Enemy) {
            return true;
        }
        // Mob 的 category 判定
        if (entity instanceof Mob mob) {
            return !mob.getType().getCategory().isFriendly();
        }
        return false;
    }

    /**
     * 是否为扫描 tick（节流）。
     */
    private static boolean shouldScanThisTick(FlyingSwordEntity sword) {
        return (
            sword.tickCount % FlyingSwordConstants.TARGET_SCAN_INTERVAL_TICKS ==
            0
        );
    }
}
