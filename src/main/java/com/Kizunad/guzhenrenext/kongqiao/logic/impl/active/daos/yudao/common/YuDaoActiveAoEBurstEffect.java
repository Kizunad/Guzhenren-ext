package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 宇道通用主动：范围崩裂/定空。
 * <p>
 * 对半径内目标造成法术伤害（可为 0），并施加单一 debuff，可选击退。
 * </p>
 */
public class YuDaoActiveAoEBurstEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_EXTRA_MAGIC_DAMAGE = "extra_magic_damage";
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final String META_KNOCKBACK_STRENGTH = "knockback_strength";

    private static final int DEFAULT_COOLDOWN_TICKS = 400;
    private static final double DEFAULT_RADIUS = 6.0;
    private static final int DEFAULT_EFFECT_DURATION_TICKS = 80;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double DEFAULT_KNOCKBACK_STRENGTH = 0.0;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;
    private static final double VERTICAL_PUSH = 0.05;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> debuff;

    public YuDaoActiveAoEBurstEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType,
        final String nbtCooldownKey,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
        this.daoType = daoType;
        this.nbtCooldownKey = Objects.requireNonNull(nbtCooldownKey, "nbtCooldownKey");
        this.debuff = debuff;
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

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final double damage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_MAGIC_DAMAGE, 0.0)
        );
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
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

        final AABB box = user.getBoundingBox().inflate(scaledRadius);
        for (LivingEntity target : user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user
        )) {
            final double multiplier = daoType == null
                ? 1.0
                : DaoHenCalculator.calculateMultiplier(user, target, daoType);
            final double scaledDamage = damage * Math.max(0.0, multiplier);
            if (scaledDamage > 0.0) {
                target.hurt(user.damageSources().playerAttack(player), (float) scaledDamage);
            }
            final int scaledDuration = scaleDuration(duration, multiplier);
            if (debuff != null && scaledDuration > 0) {
                target.addEffect(
                    new MobEffectInstance(
                        debuff,
                        scaledDuration,
                        amplifier,
                        true,
                        true
                    )
                );
            }
            final double scaledKnockback = knockbackStrength * Math.max(0.0, multiplier);
            if (scaledKnockback > 0.0) {
                applyKnockback(user, target, scaledKnockback);
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

    private static int scaleDuration(final int baseDuration, final double multiplier) {
        if (baseDuration <= 0) {
            return 0;
        }
        final double scaled = baseDuration * Math.max(0.0, multiplier);
        if (scaled <= 0.0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(scaled));
    }
}
