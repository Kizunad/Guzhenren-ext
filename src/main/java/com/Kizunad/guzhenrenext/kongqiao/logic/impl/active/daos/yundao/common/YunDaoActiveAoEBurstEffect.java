package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yundao.common;

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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 云道主动：云潮爆发（以自身为中心）。
 * <p>
 * 以普通伤害为主，并可对范围内敌对目标施加一组负面效果，附带轻微推斥。
 * </p>
 * <p>
 * 约束：
 * <ul>
 *   <li>真元消耗必须走标准折算（由 {@link GuEffectCostHelper} 统一处理）。</li>
 *   <li>伤害/范围/控制时长等需接入云道道痕算法。</li>
 *   <li>法术伤害穿甲过强，云道默认不做法术伤害。</li>
 * </ul>
 * </p>
 */
public class YunDaoActiveAoEBurstEffect implements IGuEffect {

    private static final String META_RADIUS = "radius";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";

    private static final double DEFAULT_RADIUS = 4.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 260;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_EFFECT_DURATION_TICKS = TICKS_PER_SECOND * 30;

    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.06;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public YunDaoActiveAoEBurstEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
        this.daoType = daoType;
        this.nbtCooldownKey = Objects.requireNonNull(nbtCooldownKey, "nbtCooldownKey");
        this.effects = effects == null ? List.of() : List.copyOf(effects);
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
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final double extraPhysicalDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        final double knockbackStrength = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_KNOCKBACK_STRENGTH,
                DEFAULT_KNOCKBACK_STRENGTH
            )
        );

        final double selfMultiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(user, daoType);
        final double scaledRadius = radius * Math.max(0.0, selfMultiplier);

        final AABB area = user.getBoundingBox().inflate(scaledRadius);
        for (LivingEntity target : user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        )) {
            final double multiplier = daoType == null
                ? 1.0
                : DaoHenCalculator.calculateMultiplier(user, target, daoType);

            if (extraPhysicalDamage > 0.0) {
                final double scaledDamage =
                    extraPhysicalDamage * Math.max(0.0, multiplier);
                if (scaledDamage > 0.0) {
                    target.hurt(
                        buildPhysicalDamageSource(user),
                        (float) scaledDamage
                    );
                }
            }

            applyEffects(target, usageInfo, selfMultiplier);

            if (knockbackStrength > 0.0) {
                final double strength = DaoHenEffectScalingHelper.scaleValue(
                    knockbackStrength,
                    multiplier
                );
                if (strength > 0.0) {
                    applyKnockback(user, target, strength);
                }
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

    private void applyEffects(
        final LivingEntity target,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (effects.isEmpty()) {
            return;
        }

        for (EffectSpec spec : effects) {
            if (spec == null || spec.effect() == null) {
                continue;
            }
            final int baseDuration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            final int duration = Math.min(
                DaoHenEffectScalingHelper.scaleDurationTicks(
                    baseDuration,
                    selfMultiplier
                ),
                MAX_EFFECT_DURATION_TICKS
            );
            if (duration <= 0) {
                continue;
            }
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
            );
            target.addEffect(
                new MobEffectInstance(
                    spec.effect(),
                    duration,
                    amplifier,
                    true,
                    true
                )
            );
        }
    }

    private static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof Player player) {
            return user.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
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
