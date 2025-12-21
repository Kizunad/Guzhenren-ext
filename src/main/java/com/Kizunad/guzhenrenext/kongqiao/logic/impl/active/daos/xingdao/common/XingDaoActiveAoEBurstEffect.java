package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 星道通用主动：范围爆闪/定空。
 * <p>
 * - 资源消耗：真元折算 +（念头/精力/魂魄任选其一即可）。
 * - 伤害：优先使用物理伤害（受护甲影响），法术伤害仅作为兼容兜底。
 * - 控制：debuff 持续时间随星道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class XingDaoActiveAoEBurstEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
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
    private final String nbtCooldownKey;
    private final Holder<MobEffect> debuff;

    public XingDaoActiveAoEBurstEffect(
        final String usageId,
        final String nbtCooldownKey,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
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
        final double extraPhysicalDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        final double extraMagicDamage = Math.max(
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

        final AABB box = user.getBoundingBox().inflate(radius);
        for (LivingEntity target : user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user
        )) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                target,
                DaoHenHelper.DaoType.XING_DAO
            );
            if (extraPhysicalDamage > 0.0) {
                final DamageSource source = buildPhysicalDamageSource(user);
                target.hurt(
                    source,
                    (float) (extraPhysicalDamage * multiplier)
                );
            } else if (extraMagicDamage > 0.0) {
                target.hurt(
                    user.damageSources().magic(),
                    (float) (extraMagicDamage * multiplier)
                );
            }
            if (debuff != null && duration > 0) {
                final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                    duration,
                    multiplier
                );
                if (scaledDuration > 0) {
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
            }
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
