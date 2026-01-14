package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.effects.FlyingSwordEffects;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordExpCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthTuning;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.integration.domain.SwordSpeedModifiers;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 飞剑战斗驱动（集中式）。
 * <p>
 * 从 {@code FlyingSwordEntity} 中提取，统一战斗追击与攻击逻辑。
 * </p>
 * <p>
 * Phase 3 更新：
 * <ul>
 *     <li>使用品质/等级系统计算伤害</li>
 *     <li>攻击成功后获取经验</li>
 *     <li>支持击杀/精英/Boss 额外经验</li>
 * </ul>
 * </p>
 */
 public final class SwordCombatOps {
 
     private static final String IMPRINT_PROC_DOMAIN_PREFIX = "imprint/";
     private static final String IMPRINT_PROC_DOMAIN_JIANDAO = IMPRINT_PROC_DOMAIN_PREFIX + "jiandao";
     private static final String IMPRINT_PROC_DOMAIN_YANDAO = IMPRINT_PROC_DOMAIN_PREFIX + "yandao";
     private static final String IMPRINT_PROC_DOMAIN_LEIDAO = IMPRINT_PROC_DOMAIN_PREFIX + "leidao";
 
     private SwordCombatOps() {}


    /**
     * 执行战斗追击 tick。
     * <p>
     * 驱动飞剑向目标移动，到达攻击距离后尝试攻击。
     * </p>
     *
     * @param sword          飞剑实体
     * @param owner          主人
     * @param target         目标
     * @param chaseSpeedScale 追击速度倍率（相对于 speedMax）
     */
    public static void tickCombatPursuit(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        double chaseSpeedScale
    ) {
        if (target == null || target.isRemoved() || target.isDeadOrDying()) {
            return;
        }

        // 目标位置（瞄准目标中心偏高处）
        Vec3 targetPos = target
            .position()
            .add(
                0.0,
                target.getBbHeight() * SwordGrowthTuning.TARGET_HEIGHT_RATIO,
                0.0
            );
        Vec3 swordPos = sword.position();
        Vec3 delta = targetPos.subtract(swordPos);

        // 计算追击速度（使用有效速度，包含品质加成）
        FlyingSwordAttributes attrs = sword.getSwordAttributes();
        double speed = Math.max(
            0.0,
            attrs.getEffectiveSpeedMax() * Math.max(0.0, chaseSpeedScale)
        );

        // 应用速度向目标移动
        applyVelocityTowards(sword, delta, speed);

        // 攻击判定
        double attackRange = FlyingSwordConstants.ATTACK_RANGE;
        if (delta.lengthSqr() <= attackRange * attackRange) {
            tryAttack(sword, owner, target);
        }
    }

    /**
     * 尝试攻击目标。
     *
     * @param sword  飞剑实体
     * @param owner  主人（伤害来源）
     * @param target 目标
     */
    public static void tryAttack(
        FlyingSwordEntity sword,
        @Nullable LivingEntity owner,
        LivingEntity target
    ) {
        if (owner == null || owner.isRemoved()) {
            return;
        }
        if (target == null || target.isRemoved() || target.isDeadOrDying()) {
            return;
        }
        if (!FlyingSwordCooldownOps.isAttackReady(sword)) {
            return;
        }
        if (!(sword.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 记录目标攻击前的生命值
        float targetHealthBefore = target.getHealth();

        // 计算伤害（使用品质/等级系统）
        float damage = calculateDamage(sword, owner);

        // 伤害来源：归属于主人的 mobAttack
        DamageSource source = serverLevel.damageSources().mobAttack(owner);

        // 执行攻击
        boolean success = target.hurt(source, damage);
        if (success) {
            // 设置攻击冷却（使用属性系统的冷却值）
            int cooldown = sword.getSwordAttributes().attackCooldown;
            FlyingSwordCooldownOps.setAttackCooldown(sword, cooldown);

            // 计算实际造成的伤害
            float actualDamage = targetHealthBefore - target.getHealth();
            if (actualDamage < 0) {
                actualDamage = damage; // 如果目标死亡，使用预期伤害
            }

            // 检查是否击杀
            boolean isKill = target.isDeadOrDying();

            // 处理攻击成功回调（包含经验获取）
            onAttackSuccess(sword, owner, target, actualDamage, isKill);
        }
    }

    /**
     * 计算飞剑伤害。
     * <p>
     * 使用品质/等级系统计算最终伤害。
     * </p>
     *
     * @param sword 飞剑实体
     * @param owner 主人
     * @return 最终伤害值
     */
     private static void applyImprintPassiveMultipliers(FlyingSwordAttributes attrs) {
        if (attrs == null) {
            return;
        }

        attrs.resetMultipliers();

        var imprint = attrs.getImprint();
        if (imprint == null || imprint.isEmpty()) {
            return;
        }

        String mainDao = imprint.getMainDao();
        int mainPoints = imprint.getMark(mainDao);

        double damageBonus = 0.0;
        if (mainPoints > 0) {
            damageBonus += Math.min(
                SwordGrowthTuning.IMPRINT_MAIN_DAMAGE_BONUS_CAP,
                Math.sqrt(mainPoints) * SwordGrowthTuning.IMPRINT_MAIN_DAMAGE_SQRT_COEF
            );
        }

        int bestSub = 0;
        int secondSub = 0;
        for (var entry : imprint.getMarks().entrySet()) {
            if (entry == null) {
                continue;
            }
            String key = entry.getKey();
            if (key == null || key.isBlank() || key.equals(mainDao)) {
                continue;
            }
            Integer v = entry.getValue();
            if (v == null || v <= 0) {
                continue;
            }
            int points = v;
            if (points > bestSub) {
                secondSub = bestSub;
                bestSub = points;
            } else if (points > secondSub) {
                secondSub = points;
            }
        }

        if (bestSub > 0) {
            damageBonus += Math.min(
                SwordGrowthTuning.IMPRINT_SUB_DAMAGE_BONUS_CAP,
                Math.sqrt(bestSub) * SwordGrowthTuning.IMPRINT_SUB_DAMAGE_SQRT_COEF
            );
        }

        double speedBonus = 0.0;
        if (secondSub > 0) {
            speedBonus += Math.min(
                SwordGrowthTuning.IMPRINT_SUB_SPEED_BONUS_CAP,
                Math.sqrt(secondSub) * SwordGrowthTuning.IMPRINT_SUB_SPEED_SQRT_COEF
            );
        }

        attrs.setDamageMultiplier(1.0 + Math.max(0.0, damageBonus));
        attrs.setSpeedMultiplier(1.0 + Math.max(0.0, speedBonus));
    }

    public static float calculateDamage(
         FlyingSwordEntity sword,
         LivingEntity owner
     ) {
        FlyingSwordAttributes attrs = sword.getSwordAttributes();

        applyImprintPassiveMultipliers(attrs);

        // 获取有效伤害（包含品质、等级、临时修正）
        double baseDamage = attrs.getEffectiveDamage();


        // 速度加成（速度越快伤害越高）
        double speedRatio =
            sword.getDeltaMovement().length() / attrs.getEffectiveSpeedMax();
        double speedBonus =
            1.0 +
            Math.min(
                SwordGrowthTuning.SPEED_DAMAGE_BONUS_CAP,
                speedRatio * SwordGrowthTuning.SPEED_DAMAGE_BONUS_COEF
            );

        return (float) (baseDamage * speedBonus);
    }

    /**
     * 攻击成功回调。
     * <p>
     * 处理：
     * <ul>
     *     <li>经验获取</li>
     *     <li>粒子效果</li>
     *     <li>音效</li>
     *     <li>击杀特效</li>
     * </ul>
     * </p>
     *
     * @param sword  飞剑实体
     * @param owner  主人
     * @param target 目标
     * @param damage 实际造成的伤害
     * @param isKill 是否击杀
     */
    private static void onAttackSuccess(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        float damage,
        boolean isKill
    ) {
        FlyingSwordAttributes attrs = sword.getSwordAttributes();

        // 播放攻击命中特效
        FlyingSwordEffects.playHitEffect(sword, target, damage);

        applyImprintProcs(sword, owner, target, damage);

        boolean killed = isKill || target.isDeadOrDying();

        // 如果击杀，播放击杀特效
        if (killed) {
            FlyingSwordEffects.playKillEffect(sword, target);
        }

        // 计算基础经验获取
        int baseExpGain = SwordExpCalculator.calculateExpGain(
            damage,
            target,
            killed,
            attrs.getQuality()
        );

        int expGain = baseExpGain;

        // 添加经验（自动升级）
        if (expGain > 0) {
            SwordGrowthData.ExpAddResult result = attrs.addExperience(expGain);

            // 如果升级了，播放升级效果
            if (result.levelsGained > 0) {
                onLevelUp(sword, owner, result);
            }
        }
    }

    /**
     * 升级回调。
     *
     * @param sword  飞剑实体
     * @param owner  主人
     * @param result 升级结果
     */
    private static void applyImprintProcs(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        float actualDamage
    ) {
        if (sword == null || owner == null || target == null) {
            return;
        }
        if (owner.level().isClientSide()) {
            return;
        }

        FlyingSwordAttributes attrs = sword.getSwordAttributes();
        if (attrs == null) {
            return;
        }

        var imprint = attrs.getImprint();
        if (imprint == null || imprint.isEmpty()) {
            return;
        }

        int tier = imprint.getTier();
        if (tier <= 0) {
            return;
        }

        String mainDao = imprint.getMainDao();
        if (
            mainDao == null
                || mainDao.isBlank()
                || mainDao.equals(com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper.DaoType.GENERIC.getKey())
        ) {
            return;
        }

        if (
            mainDao.equals(
                com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper.DaoType.JIAN_DAO.getKey()
            )
        ) {
            applyJianDaoProc(sword, owner, target, tier);
            return;
        }
        if (
            mainDao.equals(
                com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper.DaoType.HUO_DAO.getKey()
            )
        ) {
            applyYanDaoProc(sword, owner, target, tier);
            return;
        }
        if (
            mainDao.equals(
                com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper.DaoType.LEI_DAO.getKey()
            )
        ) {
            applyLeiDaoProc(sword, owner, target, actualDamage, tier);
        }
    }

    private static void applyLeiDaoProc(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        float actualDamage,
        int tier
    ) {
        int cd = com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps
            .get(sword, IMPRINT_PROC_DOMAIN_LEIDAO);
        if (cd > 0) {
            return;
        }

        double chance = Math.min(
            SwordGrowthTuning.IMPRINT_PROC_CHANCE_CAP,
            SwordGrowthTuning.IMPRINT_PROC_BASE_CHANCE +
                SwordGrowthTuning.IMPRINT_PROC_CHANCE_PER_TIER * tier
        );
        if (owner.getRandom().nextDouble() > chance) {
            return;
        }

        net.minecraft.world.entity.LivingEntity secondary = findChainTarget(
            sword,
            owner,
            target
        );
        if (secondary == null) {
            return;
        }

        float chainDamage = (float) Math.max(
            0.0,
            actualDamage * SwordGrowthTuning.IMPRINT_LEIDAO_CHAIN_DAMAGE_RATIO
        );
        if (chainDamage <= 0.0f) {
            return;
        }

        if (owner.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.damagesource.DamageSource source =
                serverLevel.damageSources().mobAttack(owner);
            secondary.hurt(source, chainDamage);
        }

        int cooldownTicks = Math.max(
            SwordGrowthTuning.IMPRINT_PROC_MIN_COOLDOWN_TICKS,
            SwordGrowthTuning.IMPRINT_PROC_BASE_COOLDOWN_TICKS -
                SwordGrowthTuning.IMPRINT_PROC_COOLDOWN_REDUCTION_PER_TIER * tier
        );
        com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps
            .set(sword, IMPRINT_PROC_DOMAIN_LEIDAO, cooldownTicks);
    }

    @Nullable
    private static net.minecraft.world.entity.LivingEntity findChainTarget(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity primary
    ) {
        if (sword == null || owner == null || primary == null) {
            return null;
        }

        if (!(owner.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return null;
        }

        net.minecraft.world.phys.AABB box = primary
            .getBoundingBox()
            .inflate(SwordGrowthTuning.IMPRINT_LEIDAO_CHAIN_RANGE);
        net.minecraft.world.entity.LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (net.minecraft.world.entity.LivingEntity candidate : level.getEntitiesOfClass(
            net.minecraft.world.entity.LivingEntity.class,
            box
        )) {
            if (candidate == null) {
                continue;
            }
            if (candidate == primary || candidate == owner) {
                continue;
            }
            if (candidate.isRemoved() || candidate.isDeadOrDying()) {
                continue;
            }
            if (!owner.canAttack(candidate)) {
                continue;
            }

            double d = candidate.distanceToSqr(primary);
            if (d < bestDist) {
                bestDist = d;
                best = candidate;
            }
        }

        return best;
    }

    private static void applyYanDaoProc(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        int tier
    ) {
        int cd = com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps
            .get(sword, IMPRINT_PROC_DOMAIN_YANDAO);
        if (cd > 0) {
            return;
        }

        double chance = Math.min(
            SwordGrowthTuning.IMPRINT_PROC_CHANCE_CAP,
            SwordGrowthTuning.IMPRINT_PROC_BASE_CHANCE +
                SwordGrowthTuning.IMPRINT_PROC_CHANCE_PER_TIER * tier
        );
        if (owner.getRandom().nextDouble() > chance) {
            return;
        }

        int seconds = SwordGrowthTuning.IMPRINT_YANDAO_BASE_BURN_SECONDS + Math.max(0, tier);
        target.igniteForSeconds(seconds);

        int cooldownTicks = Math.max(
            SwordGrowthTuning.IMPRINT_PROC_MIN_COOLDOWN_TICKS,
            SwordGrowthTuning.IMPRINT_PROC_BASE_COOLDOWN_TICKS -
                SwordGrowthTuning.IMPRINT_PROC_COOLDOWN_REDUCTION_PER_TIER * tier
        );
        com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps
            .set(sword, IMPRINT_PROC_DOMAIN_YANDAO, cooldownTicks);
    }

    private static void applyJianDaoProc(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        int tier
    ) {
        int cd = com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps
            .get(sword, IMPRINT_PROC_DOMAIN_JIANDAO);
        if (cd > 0) {
            return;
        }

        double chance = Math.min(
            SwordGrowthTuning.IMPRINT_PROC_CHANCE_CAP,
            SwordGrowthTuning.IMPRINT_PROC_BASE_CHANCE +
                SwordGrowthTuning.IMPRINT_PROC_CHANCE_PER_TIER * tier
        );
        if (owner.getRandom().nextDouble() > chance) {
            return;
        }

        double base = SwordGrowthTuning.BASE_DAMAGE * (
            SwordGrowthTuning.IMPRINT_JIANDAO_BASE_BONUS_DAMAGE_MULTIPLIER +
                SwordGrowthTuning.IMPRINT_JIANDAO_BONUS_DAMAGE_PER_TIER * tier
        );
        float bonusDamage = (float) Math.max(0.0, base);

        if (owner.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.damagesource.DamageSource source =
                serverLevel.damageSources().mobAttack(owner);
            target.hurt(source, bonusDamage);
        }

        int cooldownTicks = Math.max(
            SwordGrowthTuning.IMPRINT_PROC_MIN_COOLDOWN_TICKS,
            SwordGrowthTuning.IMPRINT_PROC_BASE_COOLDOWN_TICKS -
                SwordGrowthTuning.IMPRINT_PROC_COOLDOWN_REDUCTION_PER_TIER * tier
        );
        com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps
            .set(sword, IMPRINT_PROC_DOMAIN_JIANDAO, cooldownTicks);
    }

    private static void onLevelUp(
         FlyingSwordEntity sword,
         LivingEntity owner,
         SwordGrowthData.ExpAddResult result
     ) {
        // 播放升级特效（粒子 + 音效）
        FlyingSwordEffects.playLevelUpEffect(
            sword,
            result.levelsGained,
            result.newLevel
        );

        // 发送升级消息给玩家
        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    String.format(
                        "§a[飞剑升级] %s → Lv.%d | 伤害:%.1f 速度:%.2f",
                        attrs.getQuality().getDisplayName(),
                        result.newLevel,
                        attrs.damage,
                        attrs.speedMax
                    )
                ),
                true
            );
        }
    }

    /**
     * 向目标方向应用速度。
     * <p>
     * 封装飞剑的速度插值与移动逻辑。
     * </p>
     */
    private static void applyVelocityTowards(
        FlyingSwordEntity sword,
        Vec3 delta,
        double baseSpeed
    ) {
        if (delta == null) {
            return;
        }

        double distance = delta.length();
        if (distance < FlyingSwordConstants.MIN_DISTANCE) {
            sword.setDeltaMovement(Vec3.ZERO);
            return;
        }

        var attrs = sword.getSwordAttributes();

        // 领域速度加成
        double domainScale = SwordSpeedModifiers.computeDomainSpeedScale(sword);

        // 目标速度（使用有效速度）
        double effectiveMaxSpeed = attrs.getEffectiveSpeedMax() * domainScale;
        double targetSpeed = Math.max(
            baseSpeed,
            Math.min(distance, effectiveMaxSpeed)
        );

        // 期望速度向量
        Vec3 desiredVel = delta.normalize().scale(targetSpeed);

        // 当前速度
        Vec3 current = sword.getDeltaMovement();

        // 加速度插值（使用有效加速度）
        double lerpFactor = Math.min(1.0, attrs.getEffectiveAccel());
        Vec3 newVel = current.lerp(desiredVel, lerpFactor);

        // 速度上限
        if (newVel.length() > effectiveMaxSpeed) {
            newVel = newVel.normalize().scale(effectiveMaxSpeed);
        }

        // 应用速度并移动
        sword.setDeltaMovement(newVel);
        sword.move(
            net.minecraft.world.entity.MoverType.SELF,
            sword.getDeltaMovement()
        );

        // 更新朝向
        if (newVel.lengthSqr() > FlyingSwordConstants.LOOK_EPSILON) {
            sword.setYRot(
                (float) (Math.atan2(newVel.z, newVel.x) *
                        FlyingSwordConstants.RAD_TO_DEG) -
                    FlyingSwordConstants.LOOK_ROTATE_DEG_OFFSET
            );
        }
    }

    /**
     * 检查飞剑是否距离主人过远（用于 HUNT 模式脱缰判定）。
     */
    public static boolean isTooFarFromOwner(
        FlyingSwordEntity sword,
        LivingEntity owner,
        double leashRange
    ) {
        if (owner == null) {
            return true;
        }
        double distSq = sword.distanceToSqr(owner);
        return distSq > leashRange * leashRange;
    }
}
