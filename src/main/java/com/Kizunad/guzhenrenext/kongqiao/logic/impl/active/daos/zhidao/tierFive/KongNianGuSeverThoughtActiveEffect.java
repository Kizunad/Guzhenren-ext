package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 五转空念蛊：主动【断思】。
 * <p>
 * 清空自身念头以求“心如止水”，并清理常见负面状态、获得短暂防护。
 * </p>
 */
public class KongNianGuSeverThoughtActiveEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_ZHENYUAN_BASE_COST = "zhenyuan_base_cost";
    private static final String META_DURATION_TICKS = "duration_ticks";
    private static final String META_AMPLIFIER = "amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 600;
    private static final int DEFAULT_DURATION_TICKS = 200;
    private static final int DEFAULT_AMPLIFIER = 0;

    private static final List<Holder<MobEffect>> COMMON_NEGATIVE_EFFECTS =
        List.of(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.BLINDNESS,
            MobEffects.DARKNESS
        );

    private final String usageId;
    private final String cooldownKey;

    public KongNianGuSeverThoughtActiveEffect(final String usageId) {
        this.usageId = usageId;
        this.cooldownKey =
            "GuzhenrenExtCooldown_" + usageId + "_sever_thought";
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
            cooldownKey
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

        if (zhenyuanCost > 0.0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }

        final double currentNianTou = NianTouHelper.getAmount(user);
        if (currentNianTou > 0.0) {
            NianTouHelper.modify(user, -currentNianTou);
        }

        for (Holder<MobEffect> effect : COMMON_NEGATIVE_EFFECTS) {
            if (user.hasEffect(effect)) {
                user.removeEffect(effect);
            }
        }

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_AMPLIFIER,
                DEFAULT_AMPLIFIER
            )
        );
        if (duration > 0) {
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    duration,
                    amplifier,
                    true,
                    true
                )
            );
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    duration,
                    0,
                    true,
                    true
                )
            );
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
                cooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }
}

