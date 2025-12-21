package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 星道通用主动：群体扶持。
 * <p>
 * - 资源消耗：真元折算 +（念头/精力/魂魄任选其一即可）。
 * - 效果强度：治疗与增益持续时间随星道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class XingDaoActiveAllySupportEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RADIUS = "radius";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_CLEANSE_NEGATIVE = "cleanse_negative";

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final double DEFAULT_RADIUS = 8.0;
    private static final double DEFAULT_HEAL_AMOUNT = 0.0;

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public XingDaoActiveAllySupportEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
        this.effects = effects;
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
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HEAL_AMOUNT,
                DEFAULT_HEAL_AMOUNT
            )
        );
        final boolean cleanseNegative = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_CLEANSE_NEGATIVE,
            false
        );

        final AABB box = user.getBoundingBox().inflate(radius);
        for (LivingEntity ally : user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && (e == user || user.isAlliedTo(e))
        )) {
            final double multiplier = DaoHenCalculator.calculateMultiplier(
                user,
                ally,
                DaoHenHelper.DaoType.XING_DAO
            );
            final double healAmount = DaoHenEffectScalingHelper.scaleValue(
                baseHeal,
                multiplier
            );
            if (healAmount > 0.0) {
                ally.heal((float) healAmount);
            }
            if (cleanseNegative) {
                cleanseNegativeEffects(ally);
            }
            applyBuffs(ally, usageInfo, multiplier);
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

    private void applyBuffs(
        final LivingEntity ally,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : effects) {
            if (spec == null || spec.effect() == null) {
                continue;
            }
            final int duration = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.durationKey(),
                    spec.defaultDurationTicks()
                )
            );
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                multiplier
            );
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey(),
                    spec.defaultAmplifier()
                )
            );
            if (scaledDuration > 0) {
                ally.addEffect(
                    new MobEffectInstance(
                        spec.effect(),
                        scaledDuration,
                        amplifier,
                        true,
                        true
                    )
                );
            }
        }
    }

    private static void cleanseNegativeEffects(final LivingEntity ally) {
        final List<MobEffectInstance> toRemove = new ArrayList<>();
        for (MobEffectInstance inst : ally.getActiveEffects()) {
            if (inst == null || inst.getEffect() == null) {
                continue;
            }
            final MobEffect effect = inst.getEffect().value();
            if (effect != null && !effect.isBeneficial()) {
                toRemove.add(inst);
            }
        }
        for (MobEffectInstance inst : toRemove) {
            ally.removeEffect(inst.getEffect());
        }
    }
}
