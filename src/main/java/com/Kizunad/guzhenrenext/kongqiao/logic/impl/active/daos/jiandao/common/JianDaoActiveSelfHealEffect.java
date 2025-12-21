package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoBoostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class JianDaoActiveSelfHealEffect implements IGuEffect {

    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_JINGLI_GAIN = "jingli_gain";
    private static final String META_HUNPO_GAIN = "hunpo_gain";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 0;

    private final String usageId;
    private final String nbtCooldownKey;

    public JianDaoActiveSelfHealEffect(
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
        if (user == null) {
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

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.JIAN_DAO)
                * JianDaoBoostHelper.getJianXinMultiplier(user)
        );
        final double suiren = JianDaoBoostHelper.consumeSuiRenMultiplierIfActive(
            user
        );
        final double scale = DaoHenEffectScalingHelper.clampMultiplier(
            selfMultiplier * Math.max(1.0, suiren)
        );

        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (heal > 0.0) {
            user.heal((float) DaoHenEffectScalingHelper.scaleValue(heal, scale));
        }

        final double niantou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_GAIN, 0.0)
        );
        if (niantou > 0.0) {
            NianTouHelper.modify(user, DaoHenEffectScalingHelper.scaleValue(niantou, scale));
        }

        final double jingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_GAIN, 0.0)
        );
        if (jingli > 0.0) {
            JingLiHelper.modify(user, DaoHenEffectScalingHelper.scaleValue(jingli, scale));
        }

        final double hunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_GAIN, 0.0)
        );
        if (hunpo > 0.0) {
            HunPoHelper.modify(user, DaoHenEffectScalingHelper.scaleValue(hunpo, scale));
        }

        final double zhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_GAIN, 0.0)
        );
        if (zhenyuan > 0.0) {
            ZhenYuanHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(zhenyuan, scale)
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
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }

        return true;
    }
}
