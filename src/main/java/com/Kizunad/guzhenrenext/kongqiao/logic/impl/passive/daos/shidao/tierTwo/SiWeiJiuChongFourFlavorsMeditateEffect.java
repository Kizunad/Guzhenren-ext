package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 二转四味酒虫：被动【四味调息】。
 * <p>
 * 设计目标：食道与智道系统资源做一次轻微联动：同时提供真元与念头的低量续航。
 * </p>
 */
public class SiWeiJiuChongFourFlavorsMeditateEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:si_wei_jiu_chong_passive_four_flavors_meditate";

    private static final String META_ZHENYUAN_REGEN = "zhenyuan_regen";
    private static final String META_NIANTOU_REGEN = "niantou_regen";

    private static final double DEFAULT_ZHENYUAN_REGEN = 4.0;
    private static final double DEFAULT_NIANTOU_REGEN = 0.05;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 72.0;

    @Override
    public String getUsageId() {
        return USAGE_ID;
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
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
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
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double zhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_REGEN,
                DEFAULT_ZHENYUAN_REGEN
            )
        );
        if (zhenyuan > 0.0) {
            ZhenYuanHelper.modify(user, zhenyuan * multiplier);
        }

        final double niantou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_REGEN,
                DEFAULT_NIANTOU_REGEN
            )
        );
        if (niantou > 0.0) {
            NianTouHelper.modify(user, niantou * multiplier);
        }

        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.add(USAGE_ID);
        }
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.remove(USAGE_ID);
        }
    }
}
