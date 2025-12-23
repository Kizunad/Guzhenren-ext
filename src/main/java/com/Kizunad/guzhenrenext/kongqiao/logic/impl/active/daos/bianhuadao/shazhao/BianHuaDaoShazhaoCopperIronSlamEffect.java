package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 变化道主动杀招【铜皮铁撞】：短暂以铜皮铁骨护体，并以蛮躯震退周身敌人。
 * <p>
 * 说明：伤害为普通伤害源，受护甲影响；抗性与伤吸为非伤害效果，使用 {@link DaoHenEffectScalingHelper}
 * 做倍率裁剪，避免离谱膨胀。
 * </p>
 */
public class BianHuaDaoShazhaoCopperIronSlamEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_bian_hua_copper_iron_slam";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private static final String META_RADIUS = "radius";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final String META_RESISTANCE_DURATION_TICKS =
        "resistance_duration_ticks";
    private static final String META_RESISTANCE_AMPLIFIER =
        "resistance_amplifier";
    private static final String META_ABSORPTION_DURATION_TICKS =
        "absorption_duration_ticks";
    private static final String META_ABSORPTION_AMPLIFIER =
        "absorption_amplifier";

    private static final double DEFAULT_RADIUS = 3.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 5;

    private static final double MIN_RADIUS = 1.0;
    private static final double MAX_RADIUS = 16.0;

    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double KNOCKBACK_UPWARD_PUSH = 0.1;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_bian_hua_copper_iron_slam_cd_until";

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public boolean onActivate(final ServerPlayer player, final ShazhaoData data) {
        if (player == null || data == null || player.level().isClientSide()) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            player,
            NBT_COOLDOWN_KEY
        );
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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DAO_TYPE
        );
        applySelfBuffs(player, data, selfMultiplier);

        final double rawRadius = ShazhaoMetadataHelper.getDouble(
            data,
            META_RADIUS,
            DEFAULT_RADIUS
        );
        final double radius = clampRadius(rawRadius);

        final double baseDamage = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, 0.0)
        );
        final double baseKnockback = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_KNOCKBACK_STRENGTH, 0.0)
        );

        if (baseDamage > 0.0 || baseKnockback > 0.0) {
            applyAreaImpact(player, data, radius, baseDamage, baseKnockback);
        }

        applyCooldown(player, data);
        return true;
    }

    private static void applySelfBuffs(
        final ServerPlayer player,
        final ShazhaoData data,
        final double selfMultiplier
    ) {
        final int resistanceDurationTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_RESISTANCE_DURATION_TICKS, 0)
        );
        final int absorptionDurationTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_ABSORPTION_DURATION_TICKS, 0)
        );

        if (resistanceDurationTicks > 0) {
            final int scaled = DaoHenEffectScalingHelper.scaleDurationTicks(
                resistanceDurationTicks,
                selfMultiplier
            );
            if (scaled > 0) {
                final int amplifier = Math.max(
                    0,
                    ShazhaoMetadataHelper.getInt(
                        data,
                        META_RESISTANCE_AMPLIFIER,
                        0
                    )
                );
                player.addEffect(
                    new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        scaled,
                        amplifier,
                        true,
                        true
                    )
                );
            }
        }

        if (absorptionDurationTicks > 0) {
            final int scaled = DaoHenEffectScalingHelper.scaleDurationTicks(
                absorptionDurationTicks,
                selfMultiplier
            );
            if (scaled > 0) {
                final int amplifier = Math.max(
                    0,
                    ShazhaoMetadataHelper.getInt(
                        data,
                        META_ABSORPTION_AMPLIFIER,
                        0
                    )
                );
                player.addEffect(
                    new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        scaled,
                        amplifier,
                        true,
                        true
                    )
                );
            }
        }
    }

    private static void applyAreaImpact(
        final ServerPlayer player,
        final ShazhaoData data,
        final double radius,
        final double baseDamage,
        final double baseKnockback
    ) {
        final DamageSource source =
            PhysicalDamageSourceHelper.buildPhysicalDamageSource(player);
        final AABB box = player.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = player.level()
            .getEntitiesOfClass(LivingEntity.class, box, e -> e != null && e != player);

        for (LivingEntity target : targets) {
            if (target == null || isAlly(player, target)) {
                continue;
            }
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                player,
                target,
                DAO_TYPE
            );
            if (baseDamage > 0.0) {
                target.hurt(source, (float) (baseDamage * multiplier));
            }
            if (baseKnockback > 0.0) {
                final double knockback = DaoHenEffectScalingHelper.scaleValue(
                    baseKnockback,
                    multiplier
                );
                applyKnockback(player, target, knockback);
            }
        }
    }

    private static void applyKnockback(
        final LivingEntity user,
        final LivingEntity target,
        final double strength
    ) {
        if (strength <= 0.0) {
            return;
        }
        final Vec3 delta = target.position().subtract(user.position());
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, KNOCKBACK_UPWARD_PUSH, dir.z * strength);
        target.hurtMarked = true;
    }

    private static boolean isAlly(final LivingEntity user, final LivingEntity other) {
        if (other == user) {
            return true;
        }
        return user.isAlliedTo(other);
    }

    private static double clampRadius(final double radius) {
        final double r = Math.max(MIN_RADIUS, radius);
        return Math.min(r, MAX_RADIUS);
    }

    private static void applyCooldown(
        final ServerPlayer player,
        final ShazhaoData data
    ) {
        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks <= 0) {
            return;
        }
        GuEffectCooldownHelper.setCooldownUntilTick(
            player,
            NBT_COOLDOWN_KEY,
            player.tickCount + cooldownTicks
        );
    }
}

