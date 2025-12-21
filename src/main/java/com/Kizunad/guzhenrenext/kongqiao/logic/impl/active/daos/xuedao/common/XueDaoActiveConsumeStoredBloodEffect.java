package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoBloodGourdStorageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血道主动：消耗“血液存储”（血葫芦）并转化为回复。
 * <p>
 * 说明：回血/回资源按比例受“本次可用存储”影响，避免存储不足时仍满额收益。
 * </p>
 */
public class XueDaoActiveConsumeStoredBloodEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_CONSUME_AMOUNT = "consume_amount";
    private static final String META_MIN_REQUIRED = "min_required";

    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_JINGLI_GAIN = "jingli_gain";
    private static final String META_HUNPO_GAIN = "hunpo_gain";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";

    private static final int DEFAULT_COOLDOWN_TICKS = 400;
    private static final double DEFAULT_CONSUME_AMOUNT = 5000.0;
    private static final double DEFAULT_MIN_REQUIRED = 1.0;

    private static final double MAX_SELF_HEAL = 100.0;

    private final String usageId;
    private final String nbtCooldownKey;

    public XueDaoActiveConsumeStoredBloodEffect(
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
            tag.getDouble(
                XueDaoBloodGourdStorageEffect.KEY_BLOOD_STORAGE
            )
        );
        final double minRequired = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MIN_REQUIRED, DEFAULT_MIN_REQUIRED)
        );
        if (current < minRequired) {
            player.displayClientMessage(Component.literal("血葫芦中血液不足。"), true);
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
            XueDaoBloodGourdStorageEffect.KEY_BLOOD_STORAGE,
            Math.max(0.0, current - consume)
        );
        ItemStackCustomDataHelper.setCustomDataTag(stack, tag);

        final double ratio = UsageMetadataHelper.clamp(consume / consumeAmount, 0.0, 1.0);
        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );

        applyHeal(user, usageInfo, selfMultiplier, ratio);
        applyResource(user, usageInfo, selfMultiplier, ratio);

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
        final double selfMultiplier,
        final double ratio
    ) {
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (baseHeal <= 0.0) {
            return;
        }
        final double heal = UsageMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseHeal * ratio, selfMultiplier),
            0.0,
            MAX_SELF_HEAL
        );
        if (heal > 0.0) {
            user.heal((float) heal);
        }
    }

    private static void applyResource(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double selfMultiplier,
        final double ratio
    ) {
        final double baseNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_GAIN, 0.0)
        );
        if (baseNianTou > 0.0) {
            NianTouHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseNianTou * ratio, selfMultiplier)
            );
        }

        final double baseJingLi = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_GAIN, 0.0)
        );
        if (baseJingLi > 0.0) {
            JingLiHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseJingLi * ratio, selfMultiplier)
            );
        }

        final double baseHunPo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_GAIN, 0.0)
        );
        if (baseHunPo > 0.0) {
            HunPoHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseHunPo * ratio, selfMultiplier)
            );
        }

        final double baseZhenYuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_GAIN, 0.0)
        );
        if (baseZhenYuan > 0.0) {
            ZhenYuanHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseZhenYuan * ratio, selfMultiplier)
            );
        }
    }
}
