package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
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
 * 力道主动杀招：霸王震岳。
 * <p>
 * 以自身为中心震荡四方：范围普通伤害 + 击退 + 虚弱，并按命中数量提供续航（真元/精力/魂魄）与短暂护盾。
 * </p>
 */
public class BaWangZhenYueShazhaoEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_li_dao_ba_wang_zhen_yue";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LI_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_DAMAGE = "damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final String META_WEAKNESS_DURATION_TICKS =
        "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";

    private static final String META_SELF_ZHENYUAN_PER_HIT = "self_zhenyuan_per_hit";
    private static final String META_SELF_JINGLI_PER_HIT = "self_jingli_per_hit";
    private static final String META_SELF_HUNPO_PER_HIT = "self_hunpo_per_hit";
    private static final String META_SELF_ABSORPTION_PER_HIT =
        "self_absorption_per_hit";
    private static final String META_SELF_ABSORPTION_MAX = "self_absorption_max";

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RADIUS = 7.5;
    private static final double MIN_RADIUS = 1.0;
    private static final double DEFAULT_DAMAGE = 16000.0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.60;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_DAMAGE_PER_TARGET = 160000.0;
    private static final double MAX_KNOCKBACK_STRENGTH = 2.0;

    private static final int DEFAULT_WEAKNESS_DURATION_TICKS = 200;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;
    private static final int DEFAULT_COOLDOWN_TICKS = 3200;

    private static final double DEFAULT_PER_HIT = 0.0;
    private static final double MAX_TOTAL_RESTORE = 800.0;
    private static final double MAX_ABSORPTION_MAX = 80.0;

    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.12;

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
        final double radius = baseRadius * DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);

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
            MAX_KNOCKBACK_STRENGTH
        );

        final int baseWeakTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WEAKNESS_DURATION_TICKS,
                DEFAULT_WEAKNESS_DURATION_TICKS
            )
        );
        final int weakAmplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WEAKNESS_AMPLIFIER,
                DEFAULT_WEAKNESS_AMPLIFIER
            )
        );

        final SustainSpec sustain = readSustainSpec(data);

        final AABB area = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != player && !e.isAlliedTo(player)
        );

        double totalZhenyuan = 0.0;
        double totalJingli = 0.0;
        double totalHunpo = 0.0;
        int hits = 0;

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

            final int weakTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseWeakTicks, multiplier);
            if (weakTicks > 0) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.WEAKNESS,
                        weakTicks,
                        weakAmplifier,
                        true,
                        true
                    )
                );
            }

            final double knockback = baseKnockback * DaoHenEffectScalingHelper.clampMultiplier(multiplier);
            applyKnockback(player.position(), target, knockback);

            totalZhenyuan += sustain.zhenyuanPerHit;
            totalJingli += sustain.jingliPerHit;
            totalHunpo += sustain.hunpoPerHit;
            hits += 1;
        }

        final double scale = DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);
        restoreResources(player, totalZhenyuan, totalJingli, totalHunpo, scale);
        applyAbsorptionShield(player, hits, selfMultiplier, sustain);

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

    private static SustainSpec readSustainSpec(final ShazhaoData data) {
        final double zhenyuanPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_ZHENYUAN_PER_HIT, DEFAULT_PER_HIT)
        );
        final double jingliPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_JINGLI_PER_HIT, DEFAULT_PER_HIT)
        );
        final double hunpoPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_HUNPO_PER_HIT, DEFAULT_PER_HIT)
        );
        final double absorptionPerHit = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SELF_ABSORPTION_PER_HIT, DEFAULT_PER_HIT)
        );
        final double absorptionMax = ShazhaoMetadataHelper.clamp(
            Math.max(
                MIN_VALUE,
                ShazhaoMetadataHelper.getDouble(data, META_SELF_ABSORPTION_MAX, DEFAULT_PER_HIT)
            ),
            MIN_VALUE,
            MAX_ABSORPTION_MAX
        );
        return new SustainSpec(
            zhenyuanPerHit,
            jingliPerHit,
            hunpoPerHit,
            absorptionPerHit,
            absorptionMax
        );
    }

    private static void restoreResources(
        final LivingEntity user,
        final double zhenyuan,
        final double jingli,
        final double hunpo,
        final double multiplier
    ) {
        final double scaledZhenyuan = DaoHenEffectScalingHelper.scaleValue(zhenyuan, multiplier);
        final double scaledJingli = DaoHenEffectScalingHelper.scaleValue(jingli, multiplier);
        final double scaledHunpo = DaoHenEffectScalingHelper.scaleValue(hunpo, multiplier);

        final double total = Math.max(MIN_VALUE, scaledZhenyuan)
            + Math.max(MIN_VALUE, scaledJingli)
            + Math.max(MIN_VALUE, scaledHunpo);
        if (total <= MIN_VALUE) {
            return;
        }

        final double factor = total <= MAX_TOTAL_RESTORE
            ? 1.0
            : (MAX_TOTAL_RESTORE / total);

        if (scaledZhenyuan > MIN_VALUE) {
            ZhenYuanHelper.modify(user, scaledZhenyuan * factor);
        }
        if (scaledJingli > MIN_VALUE) {
            JingLiHelper.modify(user, scaledJingli * factor);
        }
        if (scaledHunpo > MIN_VALUE) {
            HunPoHelper.modify(user, scaledHunpo * factor);
        }
    }

    private static void applyAbsorptionShield(
        final ServerPlayer player,
        final int hits,
        final double selfMultiplier,
        final SustainSpec spec
    ) {
        if (player == null || spec == null || hits <= 0) {
            return;
        }
        final double scaledAbsorptionPerHit = DaoHenEffectScalingHelper.scaleValue(
            spec.absorptionPerHit(),
            selfMultiplier
        );
        final double addAbsorption = Math.max(MIN_VALUE, scaledAbsorptionPerHit) * Math.max(0, hits);
        if (addAbsorption <= MIN_VALUE || spec.absorptionMax() <= MIN_VALUE) {
            return;
        }
        final float next = (float) ShazhaoMetadataHelper.clamp(
            player.getAbsorptionAmount() + addAbsorption,
            MIN_VALUE,
            spec.absorptionMax()
        );
        player.setAbsorptionAmount(next);
    }

    private static void applyKnockback(
        final Vec3 center,
        final LivingEntity target,
        final double strength
    ) {
        if (center == null || target == null || strength <= MIN_VALUE) {
            return;
        }
        final Vec3 delta = target.position().subtract(center);
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, VERTICAL_PUSH, dir.z * strength);
        target.hurtMarked = true;
    }

    private record SustainSpec(
        double zhenyuanPerHit,
        double jingliPerHit,
        double hunpoPerHit,
        double absorptionPerHit,
        double absorptionMax
    ) {}
}
