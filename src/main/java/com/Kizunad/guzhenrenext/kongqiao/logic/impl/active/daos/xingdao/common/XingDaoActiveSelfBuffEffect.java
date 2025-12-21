package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 星道通用主动：自我护体/爆发增益。
 * <p>
 * - 资源消耗：真元折算 +（念头/精力/魂魄任选其一即可）。
 * - 治疗与增益持续时间：随星道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class XingDaoActiveSelfBuffEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_HEAL_AMOUNT = "heal_amount";

    private static final int DEFAULT_COOLDOWN_TICKS = 400;
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

    public XingDaoActiveSelfBuffEffect(
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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.XING_DAO
        );

        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HEAL_AMOUNT,
                DEFAULT_HEAL_AMOUNT
            )
        );
        final double healAmount = DaoHenEffectScalingHelper.scaleValue(
            baseHeal,
            selfMultiplier
        );
        if (healAmount > 0.0) {
            user.heal((float) healAmount);
        }

        applyBuffs(user, usageInfo, selfMultiplier);

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
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
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
                selfMultiplier
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
                user.addEffect(
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
}
