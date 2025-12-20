package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
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
    private static final String META_NIANTOU_COST = "niantou_cost";
    private static final String META_JINGLI_COST = "jingli_cost";
    private static final String META_HUNPO_COST = "hunpo_cost";
    private static final String META_ZHENYUAN_BASE_COST = "zhenyuan_base_cost";

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

        final double niantouCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST, 0.0)
        );
        if (niantouCost > 0.0 && NianTouHelper.getAmount(user) < niantouCost) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("念头不足。"),
                true
            );
            return false;
        }

        final double jingliCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST, 0.0)
        );
        if (jingliCost > 0.0 && JingLiHelper.getAmount(user) < jingliCost) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("精力不足。"),
                true
            );
            return false;
        }

        final double hunpoCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST, 0.0)
        );
        if (hunpoCost > 0.0 && HunPoHelper.getAmount(user) < hunpoCost) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("魂魄不足。"),
                true
            );
            return false;
        }

        final double zhenyuanBaseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_BASE_COST, 0.0)
        );
        final double zhenyuanCost = ZhenYuanHelper.calculateGuCost(
            user,
            zhenyuanBaseCost
        );
        if (zhenyuanCost > 0.0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("真元不足。"),
                true
            );
            return false;
        }

        if (niantouCost > 0.0) {
            NianTouHelper.modify(user, -niantouCost);
        }
        if (jingliCost > 0.0) {
            JingLiHelper.modify(user, -jingliCost);
        }
        if (hunpoCost > 0.0) {
            HunPoHelper.modify(user, -hunpoCost);
        }
        if (zhenyuanCost > 0.0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }

        if (effects != null) {
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
