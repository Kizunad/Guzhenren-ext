package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.PhysicalDamageSourceHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 毒道主动：范围毒雾爆发 - 对周围目标造成普通伤害并施加减益（毒道道痕缩放）。
 * <p>
 * 用途：控场/压制为主，伤害为普通伤害（受护甲影响）。
 * </p>
 */
public class DuDaoActiveAoEBurstEffect implements IGuEffect {

    public static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    public static final String META_RADIUS = "radius";
    public static final String META_EXTRA_PHYSICAL_DAMAGE = "extra_physical_damage";
    public static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    public static final String META_EFFECT_AMPLIFIER = "effect_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 240;
    private static final double DEFAULT_RADIUS = 5.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double MAX_RADIUS = 24.0;

    private static final int DEFAULT_EFFECT_DURATION_TICKS = 60;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final Holder<MobEffect> debuff;

    public DuDaoActiveAoEBurstEffect(
        final String usageId,
        final String nbtCooldownKey,
        final Holder<MobEffect> debuff
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
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

        final int remain = GuEffectCooldownHelper.getRemainingTicks(user, nbtCooldownKey);
        if (remain > 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.DU_DAO
        );

        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            MIN_RADIUS,
            MAX_RADIUS
        );

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_EXTRA_PHYSICAL_DAMAGE, 0.0)
        );
        final DamageSource source = PhysicalDamageSourceHelper.buildPhysicalDamageSource(user);

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            duration,
            selfMultiplier
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );

        final AABB box = user.getBoundingBox().inflate(radius);
        for (LivingEntity target : user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && e != user
        )) {
            if (baseDamage > 0.0) {
                final double m = DaoHenCalculator.calculateMultiplier(
                    user,
                    target,
                    DaoHenHelper.DaoType.DU_DAO
                );
                target.hurt(source, (float) (baseDamage * m));
            }
            if (debuff != null && scaledDuration > 0) {
                target.addEffect(
                    new MobEffectInstance(debuff, scaledDuration, amplifier, true, true)
                );
            }
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_COOLDOWN_TICKS, DEFAULT_COOLDOWN_TICKS)
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
}

