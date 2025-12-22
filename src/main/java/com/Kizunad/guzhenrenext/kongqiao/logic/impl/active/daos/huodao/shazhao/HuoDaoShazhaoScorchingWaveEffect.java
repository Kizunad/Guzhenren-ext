package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.shazhao;

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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * 火/炎道主动杀招【灼浪横扫】：小范围爆发的普通伤害 + 可选击退与短暂负面效果。
 */
public class HuoDaoShazhaoScorchingWaveEffect implements IShazhaoActiveEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_active_huo_dao_scorching_wave";

    private static final String META_RADIUS = "radius";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_PHYSICAL_DAMAGE = "physical_damage";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_BURN_SECONDS = "burn_seconds";

    private static final String META_SLOWNESS_DURATION_TICKS =
        "slowness_duration_ticks";
    private static final String META_SLOWNESS_AMPLIFIER = "slowness_amplifier";
    private static final String META_WEAKNESS_DURATION_TICKS =
        "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";

    private static final double DEFAULT_RADIUS = 5.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 10;
    private static final int MAX_BURN_SECONDS = 15;
    private static final double MIN_KNOCKBACK_DISTANCE = 0.0001;
    private static final double KNOCKBACK_UPWARD_PUSH = 0.1;

    private static final String NBT_COOLDOWN_KEY =
        "guzhenrenext_shazhao_active_huo_dao_scorching_wave_cd_until";

    private static final List<EffectSpec> EFFECTS = List.of(
        new EffectSpec(
            MobEffects.MOVEMENT_SLOWDOWN,
            META_SLOWNESS_DURATION_TICKS,
            META_SLOWNESS_AMPLIFIER
        ),
        new EffectSpec(
            MobEffects.WEAKNESS,
            META_WEAKNESS_DURATION_TICKS,
            META_WEAKNESS_AMPLIFIER
        )
    );

    private record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        String amplifierKey
    ) {}

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

        final double radius = Math.max(
            1.0,
            ShazhaoMetadataHelper.getDouble(data, META_RADIUS, DEFAULT_RADIUS)
        );
        final double baseDamage = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_PHYSICAL_DAMAGE, 0.0)
        );
        final double knockbackStrength = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_KNOCKBACK_STRENGTH, 0.0)
        );

        final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(
            player
        );

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
                DaoHenHelper.DaoType.HUO_DAO
            );
            if (baseDamage > 0.0) {
                target.hurt(source, (float) (baseDamage * multiplier));
            }
            applyDebuffs(target, data, multiplier);
            applyBurn(target, data, multiplier);
            applyKnockback(player, target, knockbackStrength);
        }

        final int cooldownTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(data, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                player,
                NBT_COOLDOWN_KEY,
                player.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private static void applyDebuffs(
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        for (EffectSpec spec : EFFECTS) {
            if (spec == null || spec.effect == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                ShazhaoMetadataHelper.getInt(data, spec.durationKey, 0)
            );
            if (duration <= 0) {
                continue;
            }
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                multiplier
            );
            if (scaledDuration <= 0) {
                continue;
            }
            final int amplifier = Math.max(
                0,
                ShazhaoMetadataHelper.getInt(data, spec.amplifierKey, 0)
            );
            target.addEffect(
                new MobEffectInstance(spec.effect, scaledDuration, amplifier, true, true)
            );
        }
    }

    private static void applyBurn(
        final LivingEntity target,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double burnBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_BURN_SECONDS, 0.0)
        );
        if (burnBase <= 0.0) {
            return;
        }
        final double burnSeconds = DaoHenEffectScalingHelper.scaleValue(
            burnBase,
            multiplier
        );
        final int seconds = (int) Math.min(
            MAX_BURN_SECONDS,
            Math.round(Math.max(0.0, burnSeconds))
        );
        if (seconds > 0) {
            target.igniteForSeconds(seconds);
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
        final double dx = target.getX() - user.getX();
        final double dz = target.getZ() - user.getZ();
        final double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist <= MIN_KNOCKBACK_DISTANCE) {
            return;
        }
        target.push(
            (dx / dist) * strength,
            KNOCKBACK_UPWARD_PUSH,
            (dz / dist) * strength
        );
        target.hurtMarked = true;
    }

    private static boolean isAlly(final LivingEntity user, final LivingEntity other) {
        if (other == user) {
            return true;
        }
        return user.isAlliedTo(other);
    }
}
