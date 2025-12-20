package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.QiyunHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 骨道通用被动：持续性维持（多资源）+ 每秒资源回复/增减 + 生命回复。
 * <p>
 * 通过 metadata 配置（均为“每秒”）：<br>
 * - zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - niantou_regen_per_second / jingli_regen_per_second / hunpo_regen_per_second / qiyun_regen_per_second<br>
 * - health_regen_per_second<br>
 * </p>
 */
public class GuDaoSustainedRegenEffect implements IGuEffect {

    private static final String META_NIANTOU_REGEN_PER_SECOND =
        "niantou_regen_per_second";
    private static final String META_JINGLI_REGEN_PER_SECOND =
        "jingli_regen_per_second";
    private static final String META_HUNPO_REGEN_PER_SECOND =
        "hunpo_regen_per_second";
    private static final String META_QIYUN_REGEN_PER_SECOND =
        "qiyun_regen_per_second";
    private static final String META_HEALTH_REGEN_PER_SECOND =
        "health_regen_per_second";

    private static final double DEFAULT_COST = 0.0;

    private final String usageId;

    public GuDaoSustainedRegenEffect(final String usageId) {
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
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            setActive(user, false);
            return;
        }

        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );

        if (!GuEffectCostHelper.tryConsumeSustain(
            user,
            niantouCostPerSecond,
            jingliCostPerSecond,
            hunpoCostPerSecond,
            zhenyuanBaseCostPerSecond
        )) {
            setActive(user, false);
            return;
        }
        setActive(user, true);

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.GU_DAO
        );

        final double niantou = UsageMetadataHelper.getDouble(
            usageInfo,
            META_NIANTOU_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(niantou, 0.0) != 0) {
            NianTouHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(niantou, selfMultiplier)
            );
        }

        final double jingli = UsageMetadataHelper.getDouble(
            usageInfo,
            META_JINGLI_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(jingli, 0.0) != 0) {
            JingLiHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(jingli, selfMultiplier)
            );
        }

        final double hunpo = UsageMetadataHelper.getDouble(
            usageInfo,
            META_HUNPO_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(hunpo, 0.0) != 0) {
            HunPoHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(hunpo, selfMultiplier)
            );
        }

        final double qiyun = UsageMetadataHelper.getDouble(
            usageInfo,
            META_QIYUN_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(qiyun, 0.0) != 0) {
            QiyunHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(qiyun, selfMultiplier)
            );
        }

        final double healthRegen = UsageMetadataHelper.getDouble(
            usageInfo,
            META_HEALTH_REGEN_PER_SECOND,
            0.0
        );
        if (healthRegen > 0.0) {
            user.heal(
                (float) DaoHenEffectScalingHelper.scaleValue(
                    healthRegen,
                    selfMultiplier
                )
            );
        }
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives == null) {
            return;
        }
        if (active) {
            actives.add(usageId);
            return;
        }
        actives.remove(usageId);
    }
}
