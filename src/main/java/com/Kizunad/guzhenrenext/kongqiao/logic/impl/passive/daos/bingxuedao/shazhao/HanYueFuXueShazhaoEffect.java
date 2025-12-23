package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 冰雪道主动杀招：寒月覆雪。
 * <p>
 * 以自身为中心引动寒月雪潮：范围普通伤害 + 迟滞/虚弱 + 冻结，
 * 并按命中数量为自身提供少量续航（真元/念头/精力/魂魄）与吸收护盾。
 * </p>
 */
public class HanYueFuXueShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bing_xue_dao_han_yue_fu_xue";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_WEAKNESS_DURATION_TICKS = "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";

    private static final String META_FREEZE_TICKS = "freeze_ticks";
    private static final String META_MAX_FROZEN_TICKS = "max_frozen_ticks";

    private static final String META_SELF_ZHENYUAN_PER_HIT = "self_zhenyuan_per_hit";
    private static final String META_SELF_NIANTOU_PER_HIT = "self_niantou_per_hit";
    private static final String META_SELF_JINGLI_PER_HIT = "self_jingli_per_hit";
    private static final String META_SELF_HUNPO_PER_HIT = "self_hunpo_per_hit";
    private static final String META_SELF_ABSORPTION_PER_HIT = "self_absorption_per_hit";
    private static final String META_SELF_ABSORPTION_MAX = "self_absorption_max";

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_RADIUS = 9.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE = 18000.0;
    private static final double MAX_DAMAGE_PER_TARGET = 120000.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.45;
    private static final double MAX_KNOCKBACK = 1.5;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 260;
    private static final int DEFAULT_SLOW_AMPLIFIER = 1;
    private static final int DEFAULT_WEAKNESS_DURATION_TICKS = 220;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;

    private static final int DEFAULT_FREEZE_TICKS = 200;
    private static final int DEFAULT_MAX_FROZEN_TICKS = 1400;

    private static final double DEFAULT_PER_HIT = 0.0;
    private static final double MAX_TOTAL_RESTORE = 800.0;

    private static final double DEFAULT_SELF_ABSORPTION_PER_HIT = 0.0;
    private static final double DEFAULT_SELF_ABSORPTION_MAX = 40.0;
    private static final double MAX_SELF_ABSORPTION_PER_HIT = 8.0;
    private static final double MAX_SELF_ABSORPTION_MAX = 120.0;
    private static final double MAX_TOTAL_ABSORPTION_ADD = 80.0;

    private static final double VERTICAL_PUSH = 0.10;

    private static final int DEFAULT_COOLDOWN_TICKS = 3600;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null || player.level().isClientSide()) {
            return false;
        }

        if (!ensureNotCoolingDown(player)) {
            return false;
        }
        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE);
        final Config config = readConfig(data);
        final double radius = calculateRadius(config.baseRadius(), selfMultiplier);
        final List<LivingEntity> targets = findTargets(player, radius);

        final Totals totals = applyEffects(player, targets, config);

        final double scale = DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);
        restoreResources(
            player,
            totals.zhenyuan(),
            totals.niantou(),
            totals.jingli(),
            totals.hunpo(),
            scale
        );
        applyAbsorption(player, totals.absorption(), config.absorptionMaxBase(), scale);

        applyCooldown(player, config.cooldownTicks());
        return true;
    }

    private static boolean ensureNotCoolingDown(final ServerPlayer player) {
        final int remain = GuEffectCooldownHelper.getRemainingTicks(player, COOLDOWN_KEY);
        if (remain <= 0) {
            return true;
        }
        player.displayClientMessage(Component.literal("冷却中，剩余 " + remain + "t"), true);
        return false;
    }

    private static Config readConfig(final ShazhaoData data) {
        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double baseDamage = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_DAMAGE, DEFAULT_DAMAGE)
        );
        final double baseKnockback = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_KNOCKBACK_STRENGTH,
                    DEFAULT_KNOCKBACK_STRENGTH
                )
            ),
            MIN_VALUE,
            MAX_KNOCKBACK
        );

        final int baseSlowTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_DURATION_TICKS, DEFAULT_SLOW_DURATION_TICKS)
        );
        final int slowAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, DEFAULT_SLOW_AMPLIFIER)
        );
        final int baseWeakTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WEAKNESS_DURATION_TICKS,
                DEFAULT_WEAKNESS_DURATION_TICKS
            )
        );
        final int weaknessAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WEAKNESS_AMPLIFIER,
                DEFAULT_WEAKNESS_AMPLIFIER
            )
        );
        final int baseFreezeTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_FREEZE_TICKS, DEFAULT_FREEZE_TICKS)
        );
        final int maxFrozenTicks = Math.max(
            1,
            ShazhaoMetadataHelper.getInt(data, META_MAX_FROZEN_TICKS, DEFAULT_MAX_FROZEN_TICKS)
        );

        final double perHitZhenyuan = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_ZHENYUAN_PER_HIT, DEFAULT_PER_HIT)
        );
        final double perHitNianTou = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_NIANTOU_PER_HIT, DEFAULT_PER_HIT)
        );
        final double perHitJingLi = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_JINGLI_PER_HIT, DEFAULT_PER_HIT)
        );
        final double perHitHunPo = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_HUNPO_PER_HIT, DEFAULT_PER_HIT)
        );
        final double perHitAbsorption = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    META_SELF_ABSORPTION_PER_HIT,
                    DEFAULT_SELF_ABSORPTION_PER_HIT
                )
            ),
            MIN_VALUE,
            MAX_SELF_ABSORPTION_PER_HIT
        );
        final double absorptionMaxBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_SELF_ABSORPTION_MAX,
                DEFAULT_SELF_ABSORPTION_MAX
            )
        );

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        return new Config(
            baseRadius,
            baseDamage,
            baseKnockback,
            baseSlowTicks,
            slowAmplifier,
            baseWeakTicks,
            weaknessAmplifier,
            baseFreezeTicks,
            maxFrozenTicks,
            perHitZhenyuan,
            perHitNianTou,
            perHitJingLi,
            perHitHunPo,
            perHitAbsorption,
            absorptionMaxBase,
            cooldownTicks
        );
    }

    private static double calculateRadius(final double baseRadius, final double selfMultiplier) {
        return Math.max(MIN_RADIUS, baseRadius)
            * DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);
    }

    private static List<LivingEntity> findTargets(final ServerPlayer player, final double radius) {
        final AABB area = player.getBoundingBox().inflate(radius);
        return player.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );
    }

    private static Totals applyEffects(
        final ServerPlayer player,
        final List<LivingEntity> targets,
        final Config config
    ) {
        double totalZhenyuan = 0.0;
        double totalNianTou = 0.0;
        double totalJingLi = 0.0;
        double totalHunPo = 0.0;
        double totalAbsorption = 0.0;

        for (LivingEntity target : targets) {
            applyToTarget(player, target, config);
            totalZhenyuan += config.perHitZhenyuan();
            totalNianTou += config.perHitNianTou();
            totalJingLi += config.perHitJingLi();
            totalHunPo += config.perHitHunPo();
            totalAbsorption += config.perHitAbsorption();
        }

        return new Totals(totalZhenyuan, totalNianTou, totalJingLi, totalHunPo, totalAbsorption);
    }

    private static void applyToTarget(
        final ServerPlayer player,
        final LivingEntity target,
        final Config config
    ) {
        if (player == null || target == null || config == null) {
            return;
        }
        final double multiplier = DaoHenCalculator.calculateMultiplier(player, target, DAO_TYPE);

        final double damage = ShazhaoMetadataHelper.clamp(
            config.baseDamage() * Math.max(MIN_VALUE, multiplier),
            MIN_VALUE,
            MAX_DAMAGE_PER_TARGET
        );
        if (damage > MIN_VALUE) {
            target.hurt(PhysicalDamageSourceHelper.buildPhysicalDamageSource(player), (float) damage);
        }

        final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(config.baseSlowTicks(), multiplier);
        if (slowTicks > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slowTicks,
                    config.slowAmplifier(),
                    true,
                    true
                )
            );
        }
        final int weakTicks = DaoHenEffectScalingHelper.scaleDurationTicks(config.baseWeakTicks(), multiplier);
        if (weakTicks > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    weakTicks,
                    config.weaknessAmplifier(),
                    true,
                    true
                )
            );
        }

        final int freezeTicks = DaoHenEffectScalingHelper.scaleDurationTicks(config.baseFreezeTicks(), multiplier);
        if (freezeTicks > 0) {
            BingXueDaoShazhaoEffectHelper.addFreezeTicks(target, freezeTicks, config.maxFrozenTicks());
        }

        final double knockback = config.baseKnockback()
            * DaoHenEffectScalingHelper.clampMultiplier(multiplier);
        if (knockback > MIN_VALUE) {
            BingXueDaoShazhaoEffectHelper.applyKnockback(
                player.position(),
                target,
                knockback,
                VERTICAL_PUSH
            );
        }
    }

    private static void applyCooldown(final ServerPlayer player, final int cooldownTicks) {
        if (player == null || cooldownTicks <= 0) {
            return;
        }
        GuEffectCooldownHelper.setCooldownUntilTick(
            player,
            COOLDOWN_KEY,
            player.tickCount + cooldownTicks
        );
    }

    private static void restoreResources(
        final ServerPlayer player,
        final double zhenyuan,
        final double niantou,
        final double jingli,
        final double hunpo,
        final double multiplier
    ) {
        final double scaledZhenyuan = DaoHenEffectScalingHelper.scaleValue(zhenyuan, multiplier);
        final double scaledNianTou = DaoHenEffectScalingHelper.scaleValue(niantou, multiplier);
        final double scaledJingLi = DaoHenEffectScalingHelper.scaleValue(jingli, multiplier);
        final double scaledHunPo = DaoHenEffectScalingHelper.scaleValue(hunpo, multiplier);

        final double total = Math.max(MIN_VALUE, scaledZhenyuan)
            + Math.max(MIN_VALUE, scaledNianTou)
            + Math.max(MIN_VALUE, scaledJingLi)
            + Math.max(MIN_VALUE, scaledHunPo);
        if (total <= MIN_VALUE) {
            return;
        }

        final double factor = total <= MAX_TOTAL_RESTORE
            ? 1.0
            : (MAX_TOTAL_RESTORE / total);

        if (scaledZhenyuan > MIN_VALUE) {
            ZhenYuanHelper.modify(player, scaledZhenyuan * factor);
        }
        if (scaledNianTou > MIN_VALUE) {
            NianTouHelper.modify(player, scaledNianTou * factor);
        }
        if (scaledJingLi > MIN_VALUE) {
            JingLiHelper.modify(player, scaledJingLi * factor);
        }
        if (scaledHunPo > MIN_VALUE) {
            HunPoHelper.modify(player, scaledHunPo * factor);
        }
    }

    private static void applyAbsorption(
        final ServerPlayer player,
        final double absorption,
        final double absorptionMaxBase,
        final double multiplier
    ) {
        final double scaledAbsorption = DaoHenEffectScalingHelper.scaleValue(absorption, multiplier);
        final double clampedAbsorption = ShazhaoMetadataHelper.clamp(
            scaledAbsorption,
            MIN_VALUE,
            MAX_TOTAL_ABSORPTION_ADD
        );
        if (clampedAbsorption <= MIN_VALUE) {
            return;
        }
        final double maxScaled = DaoHenEffectScalingHelper.scaleValue(absorptionMaxBase, multiplier);
        final double max = ShazhaoMetadataHelper.clamp(maxScaled, MIN_VALUE, MAX_SELF_ABSORPTION_MAX);
        if (max <= MIN_VALUE) {
            return;
        }
        BingXueDaoShazhaoEffectHelper.addAbsorption(player, clampedAbsorption, max);
    }

    private record Config(
        double baseRadius,
        double baseDamage,
        double baseKnockback,
        int baseSlowTicks,
        int slowAmplifier,
        int baseWeakTicks,
        int weaknessAmplifier,
        int baseFreezeTicks,
        int maxFrozenTicks,
        double perHitZhenyuan,
        double perHitNianTou,
        double perHitJingLi,
        double perHitHunPo,
        double perHitAbsorption,
        double absorptionMaxBase,
        int cooldownTicks
    ) {}

    private record Totals(
        double zhenyuan,
        double niantou,
        double jingli,
        double hunpo,
        double absorption
    ) {}
}
