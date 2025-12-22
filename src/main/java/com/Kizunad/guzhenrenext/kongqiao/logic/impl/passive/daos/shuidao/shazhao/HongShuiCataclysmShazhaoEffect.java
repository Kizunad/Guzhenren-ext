package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
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
import net.minecraft.world.phys.Vec3;

/**
 * 水道主动杀招：洪水倾覆。
 * <p>
 * 以自身为中心引发洪潮：范围普通伤害 + 击退 + 迟滞，
 * 并按命中数量为自身提供少量续航（真元/念头/魂魄）。
 * </p>
 */
public class HongShuiCataclysmShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_hong_shui_cataclysm";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.SHUI_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_SLOW_DURATION_TICKS = "slow_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slow_amplifier";
    private static final String META_SELF_ZHENYUAN_PER_HIT = "self_zhenyuan_per_hit";
    private static final String META_SELF_NIANTOU_PER_HIT = "self_niantou_per_hit";
    private static final String META_SELF_HUNPO_PER_HIT = "self_hunpo_per_hit";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 8.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE = 12000.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.55;
    private static final double MAX_DAMAGE_PER_TARGET = 80000.0;
    private static final double MAX_KNOCKBACK = 1.5;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 220;
    private static final int DEFAULT_SLOW_AMPLIFIER = 1;
    private static final int DEFAULT_COOLDOWN_TICKS = 3200;

    private static final double DEFAULT_PER_HIT = 0.0;
    private static final double MAX_TOTAL_RESTORE = 600.0;

    private static final double MIN_VALUE = 0.0;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.10;

    private static final String COOLDOWN_KEY = DaoCooldownKeys.active(SHAZHAO_ID);

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(player, COOLDOWN_KEY);
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!ShazhaoCostHelper.tryConsumeOnce(player, player, data)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(player, DAO_TYPE);
        final double baseRadius = Math.max(
            MIN_RADIUS,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = baseRadius * Math.max(MIN_VALUE, selfMultiplier);

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
            ShazhaoMetadataHelper.getInt(
                data,
                META_SLOW_DURATION_TICKS,
                DEFAULT_SLOW_DURATION_TICKS
            )
        );
        final int slowAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_SLOW_AMPLIFIER, DEFAULT_SLOW_AMPLIFIER)
        );

        final double perHitZhenyuan = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_ZHENYUAN_PER_HIT, DEFAULT_PER_HIT)
        );
        final double perHitNianTou = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_NIANTOU_PER_HIT, DEFAULT_PER_HIT)
        );
        final double perHitHunpo = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_HUNPO_PER_HIT, DEFAULT_PER_HIT)
        );

        final AABB area = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );

        double totalZhenyuan = 0.0;
        double totalNianTou = 0.0;
        double totalHunpo = 0.0;

        for (LivingEntity target : targets) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(player, target, DAO_TYPE);
            final double damage = ShazhaoMetadataHelper.clamp(
                baseDamage * Math.max(MIN_VALUE, multiplier),
                MIN_VALUE,
                MAX_DAMAGE_PER_TARGET
            );
            if (damage > MIN_VALUE) {
                target.hurt(
                    PhysicalDamageSourceHelper.buildPhysicalDamageSource(player),
                    (float) damage
                );
            }

            final int slowTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseSlowTicks, multiplier);
            if (slowTicks > 0) {
                target.addEffect(
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, slowAmplifier, true, true)
                );
            }

            final double knockback = baseKnockback * DaoHenEffectScalingHelper.clampMultiplier(multiplier);
            if (knockback > MIN_VALUE) {
                applyKnockback(player, target, knockback);
            }

            totalZhenyuan += perHitZhenyuan;
            totalNianTou += perHitNianTou;
            totalHunpo += perHitHunpo;
        }

        final double scale = DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);
        restoreResources(player, totalZhenyuan, totalNianTou, totalHunpo, scale);

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private static void restoreResources(
        final ServerPlayer player,
        final double zhenyuan,
        final double niantou,
        final double hunpo,
        final double multiplier
    ) {
        final double scaledZhenyuan = DaoHenEffectScalingHelper.scaleValue(zhenyuan, multiplier);
        final double scaledNianTou = DaoHenEffectScalingHelper.scaleValue(niantou, multiplier);
        final double scaledHunpo = DaoHenEffectScalingHelper.scaleValue(hunpo, multiplier);

        final double total = Math.max(MIN_VALUE, scaledZhenyuan)
            + Math.max(MIN_VALUE, scaledNianTou)
            + Math.max(MIN_VALUE, scaledHunpo);
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
        if (scaledHunpo > MIN_VALUE) {
            HunPoHelper.modify(player, scaledHunpo * factor);
        }
    }

    private static void applyKnockback(
        final LivingEntity user,
        final LivingEntity target,
        final double strength
    ) {
        final Vec3 delta = target.position().subtract(user.position());
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, VERTICAL_PUSH, dir.z * strength);
        target.hurtMarked = true;
    }
}

