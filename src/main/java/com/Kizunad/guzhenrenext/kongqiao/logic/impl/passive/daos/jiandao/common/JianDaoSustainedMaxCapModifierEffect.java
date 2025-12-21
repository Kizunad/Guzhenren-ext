package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class JianDaoSustainedMaxCapModifierEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND;
    private static final String META_NIANTOU_COST_PER_SECOND =
        GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND;
    private static final String META_JINGLI_COST_PER_SECOND =
        GuEffectCostHelper.META_JINGLI_COST_PER_SECOND;
    private static final String META_HUNPO_COST_PER_SECOND =
        GuEffectCostHelper.META_HUNPO_COST_PER_SECOND;

    private static final String META_MAX_ZHENYUAN_BONUS = "max_zhenyuan_bonus";
    private static final String META_MAX_JINGLI_BONUS = "max_jingli_bonus";
    private static final String META_MAX_HUNPO_BONUS = "max_hunpo_bonus";

    private static final double DEFAULT_COST_PER_SECOND = 0.0;
    private static final double MAX_CAP_BONUS = 900.0;

    private final String usageId;

    public JianDaoSustainedMaxCapModifierEffect(final String usageId) {
        this.usageId = usageId;
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            removeAll(user);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            removeAll(user);
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.JIAN_DAO)
                * JianDaoBoostHelper.getJianXinMultiplier(user)
        );

        applyMaxCaps(user, usageInfo, selfMultiplier);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }
        removeAll(user);
    }

    private void applyMaxCaps(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final double maxZhenYuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MAX_ZHENYUAN_BONUS, 0.0)
        );
        final double maxJingLi = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MAX_JINGLI_BONUS, 0.0)
        );
        final double maxHunPo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MAX_HUNPO_BONUS, 0.0)
        );

        if (maxZhenYuan > 0.0) {
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                usageId,
                UsageMetadataHelper.clamp(
                    DaoHenEffectScalingHelper.scaleValue(maxZhenYuan, multiplier),
                    0.0,
                    MAX_CAP_BONUS
                )
            );
        } else {
            GuzhenrenVariableModifierService.removeModifier(
                user,
                GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                usageId
            );
        }

        if (maxJingLi > 0.0) {
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                usageId,
                UsageMetadataHelper.clamp(
                    DaoHenEffectScalingHelper.scaleValue(maxJingLi, multiplier),
                    0.0,
                    MAX_CAP_BONUS
                )
            );
        } else {
            GuzhenrenVariableModifierService.removeModifier(
                user,
                GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                usageId
            );
        }

        if (maxHunPo > 0.0) {
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                usageId,
                UsageMetadataHelper.clamp(
                    DaoHenEffectScalingHelper.scaleValue(maxHunPo, multiplier),
                    0.0,
                    MAX_CAP_BONUS
                )
            );
        } else {
            GuzhenrenVariableModifierService.removeModifier(
                user,
                GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                usageId
            );
        }
    }

    private void removeAll(final LivingEntity user) {
        GuzhenrenVariableModifierService.removeModifier(
            user,
            GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
            usageId
        );
        GuzhenrenVariableModifierService.removeModifier(
            user,
            GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
            usageId
        );
        GuzhenrenVariableModifierService.removeModifier(
            user,
            GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
            usageId
        );
    }
}
