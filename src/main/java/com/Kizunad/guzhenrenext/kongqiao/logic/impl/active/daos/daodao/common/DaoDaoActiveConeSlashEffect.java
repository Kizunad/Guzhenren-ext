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
 * 刀道主动：前方扇形拔刀斩（普通伤害为主，附带少量控制）。
 */
public final class DaoDaoActiveConeSlashEffect implements IGuEffect {

    public static final String META_RANGE = "range";
    public static final String META_RADIUS = "radius";
    public static final String META_CONE_DEGREES = "cone_degrees";
    public static final String META_DAMAGE = "damage";
    public static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_RANGE = 4.5;
    private static final double DEFAULT_RADIUS = 4.5;
    private static final int DEFAULT_CONE_DEGREES = 90;
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double DEFAULT_KNOCKBACK = 0.25;
    private static final int DEFAULT_COOLDOWN_TICKS = 180;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> debuffs;

    public DaoDaoActiveConeSlashEffect(
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
        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final int coneDegrees = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_CONE_DEGREES,
                DEFAULT_CONE_DEGREES
            )
        );
        final double coneHalfCos = Math.cos(
            (coneDegrees * DEG_TO_RAD) / 2.0
        );

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_DAMAGE, 0.0)
        );
        final double knockback = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_STRENGTH,
                DEFAULT_KNOCKBACK
            )
        );

        final Vec3 origin = user.position();
        final Vec3 look = user.getLookAngle().normalize();
        final AABB box = new AABB(origin, origin).inflate(radius);

        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        for (LivingEntity target : targets) {
            final Vec3 toTarget = target.position().subtract(origin);
            if (toTarget.lengthSqr() > (radius * radius)) {
                continue;
            }
            final Vec3 dir = toTarget.normalize();
            if (look.dot(dir) < coneHalfCos) {
                continue;
            }
            if (user.distanceTo(target) > range) {
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

            if (knockback > 0.0) {
                final Vec3 push = dir.scale(knockback);
                target.push(push.x, 0.0, push.z);
                target.hurtMarked = true;
            }
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
        if (target == null || usageInfo == null || debuffs.isEmpty()) {
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
