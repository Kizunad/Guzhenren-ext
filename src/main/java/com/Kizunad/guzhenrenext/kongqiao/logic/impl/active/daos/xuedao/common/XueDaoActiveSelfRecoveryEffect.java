package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血道通用主动：自我恢复（回血/回真元/回念头/回精力/回魂魄）。
 * <p>
 * 说明：回复属于非伤害类效果，倍率按道痕裁剪后缩放，避免在高道痕下膨胀到离谱数值。
 * </p>
 */
public class XueDaoActiveSelfRecoveryEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_JINGLI_GAIN = "jingli_gain";
    private static final String META_HUNPO_GAIN = "hunpo_gain";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";

    private static final int DEFAULT_COOLDOWN_TICKS = 260;
    private static final double MAX_SELF_HEAL = 100.0;

    private final String usageId;
    private final String nbtCooldownKey;

    public XueDaoActiveSelfRecoveryEffect(
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
        applyRecovery(user, usageInfo, selfMultiplier);

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

    private static void applyRecovery(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (baseHeal > 0.0) {
            user.heal((float) UsageMetadataHelper.clamp(
                DaoHenEffectScalingHelper.scaleValue(baseHeal, multiplier),
                0.0,
                MAX_SELF_HEAL
            ));
        }

        final double baseNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_GAIN, 0.0)
        );
        if (baseNianTou > 0.0) {
            NianTouHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseNianTou, multiplier)
            );
        }

        final double baseJingLi = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_GAIN, 0.0)
        );
        if (baseJingLi > 0.0) {
            JingLiHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseJingLi, multiplier)
            );
        }

        final double baseHunPo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_GAIN, 0.0)
        );
        if (baseHunPo > 0.0) {
            HunPoHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseHunPo, multiplier)
            );
        }

        final double baseZhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_GAIN, 0.0)
        );
        if (baseZhenyuan > 0.0) {
            ZhenYuanHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseZhenyuan, multiplier)
            );
        }
    }
}

