package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouCapacityHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.QiyunHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 智道通用被动：持续性维持（真元）+ 每秒资源回复/增减。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second: 真元维持基础消耗（走标准公式换算）<br>
 * - niantou_regen_per_second: 念头回复/减少<br>
 * - zhida_niantou_regen_per_second: 智道念头回复/减少<br>
 * - jingli_regen_per_second: 精力回复/减少<br>
 * - hunpo_regen_per_second: 魂魄回复/减少<br>
 * - qiyun_regen_per_second: 气运回复/减少（按上限截断）<br>
 * - health_regen_per_second: 生命回复（只做正向）<br>
 * </p>
 */
public class ZhiDaoSustainedRegenEffect implements IGuEffect {

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 0.0;

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_REGEN_PER_SECOND =
        "niantou_regen_per_second";
    private static final String META_ZHIDA_NIANTOU_REGEN_PER_SECOND =
        "zhida_niantou_regen_per_second";
    private static final String META_JINGLI_REGEN_PER_SECOND =
        "jingli_regen_per_second";
    private static final String META_HUNPO_REGEN_PER_SECOND =
        "hunpo_regen_per_second";
    private static final String META_QIYUN_REGEN_PER_SECOND =
        "qiyun_regen_per_second";
    private static final String META_HEALTH_REGEN_PER_SECOND =
        "health_regen_per_second";

    private final String usageId;

    public ZhiDaoSustainedRegenEffect(final String usageId) {
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

        final double baseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double cost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (cost > 0.0 && !ZhenYuanHelper.hasEnough(user, cost)) {
            setActive(user, false);
            return;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
        }
        setActive(user, true);

        final double niantou = UsageMetadataHelper.getDouble(
            usageInfo,
            META_NIANTOU_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(niantou, 0.0) != 0) {
            NianTouHelper.modify(user, niantou);
        }

        final double zhidaNianTou = UsageMetadataHelper.getDouble(
            usageInfo,
            META_ZHIDA_NIANTOU_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(zhidaNianTou, 0.0) != 0) {
            NianTouCapacityHelper.modifyZhiDaoNianTou(user, zhidaNianTou);
        }

        final double jingli = UsageMetadataHelper.getDouble(
            usageInfo,
            META_JINGLI_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(jingli, 0.0) != 0) {
            JingLiHelper.modify(user, jingli);
        }

        final double hunpo = UsageMetadataHelper.getDouble(
            usageInfo,
            META_HUNPO_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(hunpo, 0.0) != 0) {
            HunPoHelper.modify(user, hunpo);
        }

        final double qiyun = UsageMetadataHelper.getDouble(
            usageInfo,
            META_QIYUN_REGEN_PER_SECOND,
            0.0
        );
        if (Double.compare(qiyun, 0.0) != 0) {
            QiyunHelper.modify(user, qiyun);
        }

        final double healthRegen = UsageMetadataHelper.getDouble(
            usageInfo,
            META_HEALTH_REGEN_PER_SECOND,
            0.0
        );
        if (healthRegen > 0.0) {
            user.heal((float) healthRegen);
        }
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(
            user
        );
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
