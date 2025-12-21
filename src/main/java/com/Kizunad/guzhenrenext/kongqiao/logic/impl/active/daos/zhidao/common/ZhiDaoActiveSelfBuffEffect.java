package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
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
 * 智道通用主动：自我增益（药水效果）+ 冷却 + 多资源消耗。
 */
public class ZhiDaoActiveSelfBuffEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 200;

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

    public ZhiDaoActiveSelfBuffEffect(
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

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.ZHI_DAO
        );
        if (effects != null) {
            for (EffectSpec spec : effects) {
                if (spec == null || spec.effect() == null) {
                    continue;
                }
                final int durationBase = Math.max(
                    0,
                    UsageMetadataHelper.getInt(
                        usageInfo,
                        spec.durationKey(),
                        spec.defaultDurationTicks()
                    )
                );
                final int duration = Math.max(
                    0,
                    (int) Math.round(durationBase * multiplier)
                );
                final int amplifier = Math.max(
                    0,
                    UsageMetadataHelper.getInt(
                        usageInfo,
                        spec.amplifierKey(),
                        spec.defaultAmplifier()
                    )
                );
                if (duration > 0) {
                    user.addEffect(
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
}
