package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordClusterAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.ClusterSynergyHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.effects.FlyingSwordEffects;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordExpCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthTuning;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import static com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordDaoProcRegistry.ProcSpec;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordDaoProcRegistry;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.integration.domain.SwordSpeedModifiers;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.FlyingSwordCooldownOps;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
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
 
     private static final int DEFAULT_MOB_EFFECT_AMPLIFIER = 0;
     private static final int TIER_DURATION_TICKS_PER_LEVEL = 10;
 
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

        float synergyAttackMultiplier = resolveClusterSynergyAttackMultiplier(sword, owner);

        return (float) (baseDamage * speedBonus * synergyAttackMultiplier);
    }

    /**
     * 解析集群共鸣攻击倍率。
     * <p>
     * 仅在“玩家主人 + 服务端 + 已激活飞剑集合”条件下参与计算，
     * 其余场景回退为 1.0，不影响原有战斗路径。
     * </p>
     */
    private static float resolveClusterSynergyAttackMultiplier(
        FlyingSwordEntity sword,
        LivingEntity owner
    ) {
        if (sword == null || owner == null) {
            return 1.0F;
        }
        if (!(owner instanceof Player player)) {
            return 1.0F;
        }
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return 1.0F;
        }

        FlyingSwordClusterAttachment cluster =
            KongqiaoAttachments.getFlyingSwordCluster(player);
        if (cluster == null) {
            return 1.0F;
        }

        List<FlyingSwordEntity> ownedSwords =
            FlyingSwordController.getPlayerSwords(serverLevel, player);
        List<FlyingSwordEntity> activeSwords = new ArrayList<>();
        for (FlyingSwordEntity ownedSword : ownedSwords) {
            if (ownedSword == null) {
                continue;
            }
            UUID swordUuid = ownedSword.getUUID();
            if (cluster.hasActiveSword(swordUuid)) {
                activeSwords.add(ownedSword);
            }
        }

        return ClusterSynergyHelper.evaluate(activeSwords).attackMultiplier();
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
        if (mainDao == null || mainDao.isBlank()) {
            return;
        }

        ProcSpec spec = FlyingSwordDaoProcRegistry.get(mainDao);
        if (spec == null) {
            return;
        }

        String domain = IMPRINT_PROC_DOMAIN_PREFIX + spec.procId();
        if (FlyingSwordCooldownOps.get(sword, domain) > 0) {
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

        applyDaoProcEffect(sword, owner, target, actualDamage, tier, spec);

        int cooldownTicks = Math.max(
            SwordGrowthTuning.IMPRINT_PROC_MIN_COOLDOWN_TICKS,
            SwordGrowthTuning.IMPRINT_PROC_BASE_COOLDOWN_TICKS -
                SwordGrowthTuning.IMPRINT_PROC_COOLDOWN_REDUCTION_PER_TIER * tier
        );
        FlyingSwordCooldownOps.set(sword, domain, cooldownTicks);
    }

    private static void applyDaoProcEffect(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        float actualDamage,
        int tier,
        ProcSpec spec
    ) {
        if (spec == null) {
            return;
        }

        switch (spec.type()) {
            case BONUS_DAMAGE -> applyBonusDamageProc(sword, owner, target, actualDamage, tier, spec);
            case IGNITE -> applyIgniteProc(target, tier);
            case CHAIN_DAMAGE -> applyChainDamageProc(sword, owner, target, actualDamage, tier, spec);
            case APPLY_EFFECT -> applyMobEffectProc(target, tier, spec);
            case KNOCKBACK -> applyKnockbackProc(owner, target, tier, spec);
            case DURABILITY_RESTORE -> applyDurabilityRestoreProc(sword, tier, spec);
            case AOE_DAMAGE -> applyAreaDamageProc(owner, target, actualDamage, tier, spec);
            case CLEANSE_TARGET -> applyCleanseTargetProc(target);
            default -> {
                return;
            }
        }
    }

    private static void applyBonusDamageProc(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        float actualDamage,
        int tier,
        ProcSpec spec
    ) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float bonus;
        if ("jiandao".equals(spec.procId())) {
            bonus = computeJianDaoBonusDamage(tier);
        } else if ("jindao".equals(spec.procId())) {
            bonus = computeJinDaoBonusDamage(actualDamage, tier);
        } else if ("tiandao".equals(spec.procId())) {
            bonus = computeTianDaoBonusDamage(target, tier);
        } else if ("yudao".equals(spec.procId())) {
            bonus = computeYuDaoBonusDamage(actualDamage, tier);
        } else {
            bonus = computeDefaultBonusDamage(actualDamage, tier);
        }

        if (bonus <= 0.0f) {
            return;
        }

        DamageSource source = serverLevel.damageSources().mobAttack(owner);
        target.hurt(source, bonus);
    }

    private static final float JINDAO_DAMAGE_RATIO_BASE = 0.2f;
    private static final float JINDAO_DAMAGE_RATIO_PER_TIER = 0.05f;

    private static final float TIANYAO_MAX_HEALTH_RATIO_CAP = 0.06f;
    private static final float TIANYAO_MAX_HEALTH_RATIO_BASE = 0.02f;
    private static final float TIANYAO_MAX_HEALTH_RATIO_PER_TIER = 0.01f;

    private static final float YUDAO_DAMAGE_RATIO_BASE = 0.15f;
    private static final float YUDAO_DAMAGE_RATIO_PER_TIER = 0.05f;

    private static final float DEFAULT_DAMAGE_RATIO_BASE = 0.1f;
     private static final float DEFAULT_DAMAGE_RATIO_PER_TIER = 0.03f;

    private static final double KNOCKBACK_STRENGTH_PER_TIER =
        com.Kizunad.guzhenrenext.kongqiao.flyingsword.imprint.FlyingSwordImprintProcTuning.POWER_KNOCKBACK_PER_TIER;


    private static final double DOUBLE_EPS = 1.0e-6;

    private static float computeJianDaoBonusDamage(int tier) {
        double base = SwordGrowthTuning.BASE_DAMAGE * (
            SwordGrowthTuning.IMPRINT_JIANDAO_BASE_BONUS_DAMAGE_MULTIPLIER +
                SwordGrowthTuning.IMPRINT_JIANDAO_BONUS_DAMAGE_PER_TIER * tier
        );
        return (float) Math.max(0.0, base);
    }

    private static float computeJinDaoBonusDamage(float actualDamage, int tier) {
        return (float) Math.max(
            0.0,
            actualDamage * (JINDAO_DAMAGE_RATIO_BASE + JINDAO_DAMAGE_RATIO_PER_TIER * tier)
        );
    }

    private static float computeTianDaoBonusDamage(LivingEntity target, int tier) {
        float max = target.getMaxHealth();
        return (float) Math.max(
            0.0,
            Math.min(
                max * TIANYAO_MAX_HEALTH_RATIO_CAP,
                max * (TIANYAO_MAX_HEALTH_RATIO_BASE + TIANYAO_MAX_HEALTH_RATIO_PER_TIER * tier)
            )
        );
    }

    private static float computeYuDaoBonusDamage(float actualDamage, int tier) {
        return (float) Math.max(
            0.0,
            actualDamage * (YUDAO_DAMAGE_RATIO_BASE + YUDAO_DAMAGE_RATIO_PER_TIER * tier)
        );
    }

    private static float computeDefaultBonusDamage(float actualDamage, int tier) {
        return (float) Math.max(
            0.0,
            actualDamage * (DEFAULT_DAMAGE_RATIO_BASE + DEFAULT_DAMAGE_RATIO_PER_TIER * tier)
        );
    }

    private static void applyIgniteProc(LivingEntity target, int tier) {
        int seconds = SwordGrowthTuning.IMPRINT_YANDAO_BASE_BURN_SECONDS + Math.max(0, tier);
        target.igniteForSeconds(seconds);
    }

    private static void applyChainDamageProc(
        FlyingSwordEntity sword,
        LivingEntity owner,
        LivingEntity target,
        float actualDamage,
        int tier,
        ProcSpec spec
    ) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity secondary = findNearestChainTarget(
            serverLevel,
            owner,
            target,
            Math.max(0.0, spec.range())
        );
        if (secondary == null) {
            return;
        }

        float ratio = (float) Math.max(0.0, spec.power());
        float chainDamage = (float) Math.max(0.0, actualDamage * ratio);
        if (chainDamage <= 0.0f) {
            return;
        }

        DamageSource source = serverLevel.damageSources().mobAttack(owner);
        secondary.hurt(source, chainDamage);
    }

    @Nullable
    private static LivingEntity findNearestChainTarget(
        ServerLevel level,
        LivingEntity owner,
        LivingEntity primary,
        double range
    ) {
        AABB box = primary.getBoundingBox().inflate(range);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box)) {
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

    private static void applyMobEffectProc(
        LivingEntity target,
        int tier,
        ProcSpec spec
    ) {
        int duration = Math.max(
            1,
            spec.durationTicks() + tier * TIER_DURATION_TICKS_PER_LEVEL
        );
        int amplifier = Math.max(DEFAULT_MOB_EFFECT_AMPLIFIER, spec.amplifier());

        var effect = switch (spec.targetEffect()) {
            case SLOW -> MobEffects.MOVEMENT_SLOWDOWN;
            case WEAKNESS -> MobEffects.WEAKNESS;
            case POISON -> MobEffects.POISON;
            case WITHER -> MobEffects.WITHER;
            case GLOWING -> MobEffects.GLOWING;
            case DARKNESS -> MobEffects.DARKNESS;
            case BLINDNESS -> MobEffects.BLINDNESS;
            case HUNGER -> MobEffects.HUNGER;
            case LEVITATION -> MobEffects.LEVITATION;
            default -> MobEffects.WEAKNESS;
        };

        if (spec.targetEffect() == null) {
            return;
        }

        target.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
    }

    private static void applyKnockbackProc(
        LivingEntity owner,
        LivingEntity target,
        int tier,
        ProcSpec spec
    ) {
        double strength = Math.max(0.0, spec.power()) + tier * KNOCKBACK_STRENGTH_PER_TIER;

        Vec3 dir = target.position().subtract(owner.position());
        double len = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        if (len <= DOUBLE_EPS) {
            return;
        }
        target.knockback(strength, dir.x / len, dir.z / len);
    }

    private static void applyDurabilityRestoreProc(
        FlyingSwordEntity sword,
        int tier,
        ProcSpec spec
    ) {
        double amount = Math.max(0.0, spec.power()) + tier;
        sword.getSwordAttributes().restoreDurability(amount);
    }

    private static void applyAreaDamageProc(
        LivingEntity owner,
        LivingEntity primary,
        float actualDamage,
        int tier,
        ProcSpec spec
    ) {
        if (!(owner.level() instanceof ServerLevel level)) {
            return;
        }

        double range = Math.max(0.0, spec.range());
        float damage = (float) Math.max(0.0, actualDamage * Math.max(0.0, spec.power()));
        if (damage <= 0.0f) {
            return;
        }

        AABB box = primary.getBoundingBox().inflate(range);
        DamageSource source = level.damageSources().mobAttack(owner);

        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, box)) {
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
            candidate.hurt(source, damage);
        }
    }

    private static void applyCleanseTargetProc(LivingEntity target) {
        java.util.List<MobEffectInstance> toRemove = new java.util.ArrayList<>();
        for (MobEffectInstance inst : target.getActiveEffects()) {
            if (inst == null || inst.getEffect() == null) {
                continue;
            }
            var effect = inst.getEffect().value();
            if (effect != null && !effect.isBeneficial()) {
                toRemove.add(inst);
            }
        }
        for (MobEffectInstance inst : toRemove) {
            target.removeEffect(inst.getEffect());
        }
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
