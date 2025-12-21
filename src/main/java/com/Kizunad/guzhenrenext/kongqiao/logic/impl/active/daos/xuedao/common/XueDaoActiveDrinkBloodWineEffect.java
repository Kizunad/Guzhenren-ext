package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoBloodWineStackEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血凝剑蛊主动：饮下血酒，获得短时爆发增益。
 * <p>
 * 说明：增益强度与持续时间会受“本次消耗的血酒比例”影响，
 * 同时接入血道道痕倍率（并做裁剪）。
 * </p>
 */
public class XueDaoActiveDrinkBloodWineEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_CONSUME_AMOUNT = "consume_amount";
    private static final String META_MIN_REQUIRED = "min_required";

    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BASE_AMPLIFIER = "base_amplifier";
    private static final String META_MAX_BONUS_AMPLIFIER = "max_bonus_amplifier";

    private static final int DEFAULT_COOLDOWN_TICKS = 500;
    private static final double DEFAULT_CONSUME_AMOUNT = 400.0;
    private static final double DEFAULT_MIN_REQUIRED = 1.0;
    private static final int DEFAULT_BUFF_DURATION_TICKS = 180;
    private static final int DEFAULT_BASE_AMPLIFIER = 0;
    private static final int DEFAULT_MAX_BONUS_AMPLIFIER = 2;

    private static final Holder<MobEffect> EFFECT_STRENGTH =
        MobEffects.DAMAGE_BOOST;
    private static final Holder<MobEffect> EFFECT_REGEN =
        MobEffects.REGENERATION;

    private final String usageId;
    private final String nbtCooldownKey;

    public XueDaoActiveDrinkBloodWineEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
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
        if (user == null || stack == null || usageInfo == null) {
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

        final CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        final double current = Math.max(
            0.0,
            tag.getDouble(XueDaoBloodWineStackEffect.KEY_BLOOD_WINE)
        );
        final double minRequired = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MIN_REQUIRED, DEFAULT_MIN_REQUIRED)
        );
        if (current < minRequired) {
            player.displayClientMessage(Component.literal("血酒不足。"), true);
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double consumeAmount = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_CONSUME_AMOUNT,
                DEFAULT_CONSUME_AMOUNT
            )
        );
        if (consumeAmount <= 0.0) {
            return false;
        }

        final double consume = Math.min(current, consumeAmount);
        tag.putDouble(
            XueDaoBloodWineStackEffect.KEY_BLOOD_WINE,
            Math.max(0.0, current - consume)
        );
        ItemStackCustomDataHelper.setCustomDataTag(stack, tag);

        final double ratio = UsageMetadataHelper.clamp(consume / consumeAmount, 0.0, 1.0);
        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );

        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BUFF_DURATION_TICKS,
                DEFAULT_BUFF_DURATION_TICKS
            )
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            (int) Math.round(baseDuration * ratio),
            selfMultiplier
        );
        if (duration > 0) {
            final int baseAmp = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_BASE_AMPLIFIER,
                    DEFAULT_BASE_AMPLIFIER
                )
            );
            final int maxBonus = Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_MAX_BONUS_AMPLIFIER,
                    DEFAULT_MAX_BONUS_AMPLIFIER
                )
            );
            final int amp = Math.max(0, baseAmp + (int) Math.round(maxBonus * ratio));

            if (EFFECT_STRENGTH != null) {
                user.addEffect(new MobEffectInstance(EFFECT_STRENGTH, duration, amp, true, true));
            }
            if (EFFECT_REGEN != null) {
                user.addEffect(new MobEffectInstance(EFFECT_REGEN, duration, Math.max(0, amp - 1), true, true));
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
