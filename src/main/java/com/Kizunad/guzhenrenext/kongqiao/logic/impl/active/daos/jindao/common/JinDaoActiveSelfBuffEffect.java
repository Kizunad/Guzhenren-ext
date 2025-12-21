package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 金道主动：自我增益/恢复（金道道痕缩放）。
 */
public class JinDaoActiveSelfBuffEffect implements IGuEffect {

    public static class EffectSpec {
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

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_RESTORE_JINGLI = "restore_jingli";
    private static final String META_RESTORE_HUNPO = "restore_hunpo";
    private static final String META_RESTORE_NIANTOU = "restore_niantou";

    private static final int DEFAULT_COOLDOWN_TICKS = 20 * 10;
    private static final double MAX_HEAL = 100.0;
    private static final double MAX_RESTORE = 100.0;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public JinDaoActiveSelfBuffEffect(
        final String usageId,
        final String nbtCooldownKey,
        final List<EffectSpec> effects
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
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
            DaoHenHelper.DaoType.JIN_DAO
        );

        applyHeal(user, usageInfo, selfMultiplier);
        applyResourceRestore(user, usageInfo, selfMultiplier);
        applyEffects(user, usageInfo, selfMultiplier);

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

    private static void applyHeal(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double healBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (healBase <= 0.0) {
            return;
        }
        final double heal = Math.min(
            MAX_HEAL,
            DaoHenEffectScalingHelper.scaleValue(healBase, selfMultiplier)
        );
        if (heal > 0.0) {
            user.heal((float) heal);
        }
    }

    private static void applyResourceRestore(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        final double restoreJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_JINGLI, 0.0)
        );
        if (restoreJingli > 0.0) {
            final double amount = Math.min(
                MAX_RESTORE,
                DaoHenEffectScalingHelper.scaleValue(restoreJingli, selfMultiplier)
            );
            if (amount > 0.0) {
                JingLiHelper.modify(user, amount);
            }
        }

        final double restoreHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_HUNPO, 0.0)
        );
        if (restoreHunpo > 0.0) {
            final double amount = Math.min(
                MAX_RESTORE,
                DaoHenEffectScalingHelper.scaleValue(restoreHunpo, selfMultiplier)
            );
            if (amount > 0.0) {
                HunPoHelper.modify(user, amount);
            }
        }

        final double restoreNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RESTORE_NIANTOU, 0.0)
        );
        if (restoreNianTou > 0.0) {
            final double amount = Math.min(
                MAX_RESTORE,
                DaoHenEffectScalingHelper.scaleValue(restoreNianTou, selfMultiplier)
            );
            if (amount > 0.0) {
                NianTouHelper.modify(user, amount);
            }
        }
    }

    private void applyEffects(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier
    ) {
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (EffectSpec spec : effects) {
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
            if (duration <= 0) {
                continue;
            }
            final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
                duration,
                selfMultiplier
            );
            if (scaledDuration <= 0) {
                continue;
            }
            final int amplifier = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    spec.amplifierKey,
                    spec.defaultAmplifier
                )
            );
            user.addEffect(
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
}

