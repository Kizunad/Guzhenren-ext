package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

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

/**
 * 血道被动：持续维持扣费 + 提升 Guzhenren 资源上限/容量（高转专用）。
 * <p>
 * 支持字段：
 * <ul>
 *   <li>最大真元：{@code zuida_zhenyuan}</li>
 *   <li>最大精力：{@code zuida_jingli}</li>
 *   <li>最大魂魄：{@code zuida_hunpo}</li>
 *   <li>魂魄抗性上限：{@code hunpo_kangxing_shangxian}</li>
 *   <li>念头容量：{@code niantou_rongliang}</li>
 * </ul>
 * </p>
 * <p>
 * 为避免数量级膨胀到几千/几万，单效果加成做上限裁剪。
 * </p>
 */
public class XueDaoSustainedMaxCapModifierEffect implements IGuEffect {

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
    private static final String META_MAX_HUNPO_RESISTANCE_BONUS =
        "max_hunpo_resistance_bonus";
    private static final String META_NIANTOU_CAPACITY_BONUS =
        "niantou_capacity_bonus";

    private static final double DEFAULT_COST_PER_SECOND = 0.0;
    private static final double MAX_CAP_BONUS = 900.0;

    private final String usageId;

    public XueDaoSustainedMaxCapModifierEffect(final String usageId) {
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
        if (user == null || usageInfo == null) {
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
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
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
        applyVar(
            user,
            usageInfo,
            multiplier,
            GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
            META_MAX_ZHENYUAN_BONUS
        );
        applyVar(
            user,
            usageInfo,
            multiplier,
            GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
            META_MAX_JINGLI_BONUS
        );
        applyVar(
            user,
            usageInfo,
            multiplier,
            GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
            META_MAX_HUNPO_BONUS
        );
        applyVar(
            user,
            usageInfo,
            multiplier,
            GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
            META_MAX_HUNPO_RESISTANCE_BONUS
        );
        applyVar(
            user,
            usageInfo,
            multiplier,
            GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
            META_NIANTOU_CAPACITY_BONUS
        );
    }

    private void applyVar(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier,
        final String variableKey,
        final String metaKey
    ) {
        final double baseBonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, metaKey, 0.0)
        );
        if (baseBonus > 0.0) {
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                variableKey,
                usageId,
                UsageMetadataHelper.clamp(
                    DaoHenEffectScalingHelper.scaleValue(baseBonus, multiplier),
                    0.0,
                    MAX_CAP_BONUS
                )
            );
        } else {
            GuzhenrenVariableModifierService.removeModifier(user, variableKey, usageId);
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
        GuzhenrenVariableModifierService.removeModifier(
            user,
            GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
            usageId
        );
        GuzhenrenVariableModifierService.removeModifier(
            user,
            GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
            usageId
        );
    }
}

