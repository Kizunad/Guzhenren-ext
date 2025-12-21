package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 刀道主动：刀光直线穿行（普通伤害为主，避免护甲穿透型法术伤害过强）。
 */
public final class DaoDaoActiveBladeLightPierceEffect implements IGuEffect {

    public static final String META_RANGE = "range";
    public static final String META_BEAM_RADIUS = "beam_radius";
    public static final String META_DAMAGE = "damage";
    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 6.0;
    private static final double DEFAULT_BEAM_RADIUS = 0.6;
    private static final int DEFAULT_COOLDOWN_TICKS = 200;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> debuffs;

    public DaoDaoActiveBladeLightPierceEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> debuffs
    ) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
        this.nbtCooldownKey = Objects.requireNonNull(
            nbtCooldownKey,
            "nbtCooldownKey"
        );
        this.debuffs = debuffs == null ? List.of() : List.copyOf(debuffs);
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || usageInfo == null) {
            return false;
        }
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    "冷却中，剩余 " + remain + "t"
                ),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(
                user,
                DaoHenHelper.DaoType.DAO_DAO
            )
        );
        final double range = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        ) * Math.max(0.0, selfMultiplier);
        final double beamRadius = Math.max(
            0.1,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BEAM_RADIUS,
                DEFAULT_BEAM_RADIUS
            )
        );

        final Vec3 start = player.getEyePosition();
        final Vec3 end = start.add(player.getLookAngle().normalize().scale(range));
        final AABB box = new AABB(start, end).inflate(beamRadius);

        final List<LivingEntity> candidates = player.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, 0.0)
        );
        for (LivingEntity target : candidates) {
            if (distanceToSegmentSqr(start, end, target.position())
                > (beamRadius * beamRadius)) {
                continue;
            }

            final double multiplier = Math.max(
                0.0,
                DaoHenCalculator.calculateMultiplier(
                    user,
                    target,
                    DaoHenHelper.DaoType.DAO_DAO
                )
            );
            if (baseDamage > 0.0) {
                target.hurt(
                    user.damageSources().playerAttack(player),
                    (float) (baseDamage * multiplier)
                );
            }

            applyDebuffs(target, usageInfo, selfMultiplier);
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }

    private void applyDebuffs(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (target == null || usageInfo == null) {
            return;
        }
        if (debuffs.isEmpty()) {
            return;
        }

        for (EffectSpec spec : debuffs) {
            if (spec == null || spec.effect == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey,
                    spec.defaultDurationTicks
                )
            );
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey,
                    spec.defaultAmplifier
                )
            );
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                selfMultiplier
            );
            if (scaledDuration <= 0) {
                continue;
            }
            target.addEffect(
                new MobEffectInstance(
                    spec.effect,
                    scaledDuration,
                    amplifier,
                    true,
                    true
                )
            );
        }
    }

    private static double distanceToSegmentSqr(
        final Vec3 a,
        final Vec3 b,
        final Vec3 p
    ) {
        if (a == null || b == null || p == null) {
            return Double.MAX_VALUE;
        }
        final Vec3 ab = b.subtract(a);
        final Vec3 ap = p.subtract(a);
        final double denom = ab.lengthSqr();
        if (denom <= 0.0) {
            return ap.lengthSqr();
        }
        final double t = Math.max(
            0.0,
            Math.min(1.0, ap.dot(ab) / denom)
        );
        final Vec3 closest = a.add(ab.scale(t));
        return p.distanceToSqr(closest);
    }

    public static final class EffectSpec {
        private final Holder<MobEffect> effect;
        private final String durationKey;
        private final int defaultDurationTicks;
        private final String amplifierKey;
        private final int defaultAmplifier;

        public EffectSpec(
            final Holder<MobEffect> effect,
            final String durationKey,
            final int defaultDurationTicks,
            final String amplifierKey,
            final int defaultAmplifier
        ) {
            this.effect = effect;
            this.durationKey = durationKey;
            this.defaultDurationTicks = defaultDurationTicks;
            this.amplifierKey = amplifierKey;
            this.defaultAmplifier = defaultAmplifier;
        }
    }
}
