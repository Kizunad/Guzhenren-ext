package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血道通用主动：自我增益（血走加速/血战爆发/铁血沉重等）。
 * <p>
 * 持续时间随血道道痕倍率缩放，但会做裁剪避免无限延长。
 * </p>
 */
public class XueDaoActiveSelfBuffEffect implements IGuEffect {

    public record EffectSpec(
        Holder<MobEffect> effect,
        String durationKey,
        int defaultDurationTicks,
        String amplifierKey,
        int defaultAmplifier
    ) {}

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 200;

    private final String usageId;
    private final String nbtCooldownKey;
    private final List<EffectSpec> effects;

    public XueDaoActiveSelfBuffEffect(
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
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );
        for (EffectSpec spec : effects) {
            applyEffect(user, usageInfo, selfMultiplier, spec);
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

    private static void applyEffect(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier,
        final EffectSpec spec
    ) {
        if (spec == null || spec.effect() == null) {
            return;
        }
        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                spec.durationKey(),
                spec.defaultDurationTicks()
            )
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDuration,
            multiplier
        );
        if (duration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                spec.amplifierKey(),
                spec.defaultAmplifier()
            )
        );
        user.addEffect(
            new MobEffectInstance(spec.effect(), duration, amplifier, true, true)
        );
    }
}

