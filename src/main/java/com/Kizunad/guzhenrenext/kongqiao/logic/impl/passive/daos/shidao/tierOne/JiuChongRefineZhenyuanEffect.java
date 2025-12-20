package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 一转酒虫：被动【酒酿精元】。
 * <p>
 * 设计目标：把“饱食/饱和”转化为真元续航，体现食道的“以食养元”。
 * <ul>
 *   <li>条件：需要最低饱食度，否则不运作。</li>
 *   <li>代价：每秒增加饱食消耗（exhaustion），用于表达“以食供蛊”。</li>
 *   <li>收益：每秒回复真元。</li>
 * </ul>
 * </p>
 */
public class JiuChongRefineZhenyuanEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:jiu_chong_passive_refine_zhenyuan";

    private static final String META_MIN_HUNGER = "min_hunger";
    private static final String META_SAT_COST = "sat_cost";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";

    private static final int DEFAULT_MIN_HUNGER = 6;
    private static final double DEFAULT_SAT_COST = 0.2;
    private static final double DEFAULT_ZHENYUAN_GAIN = 6.0;

    private static final float EXHAUSTION_PER_SATURATION = 4.0F;

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

        if (!(user instanceof Player player)) {
            return;
        }

        final int minHunger = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_MIN_HUNGER,
                DEFAULT_MIN_HUNGER
            )
        );
        if (player.getFoodData().getFoodLevel() < minHunger) {
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
        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                0.0
            )
        ) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        final double satCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_SAT_COST,
                DEFAULT_SAT_COST
            )
        );
        if (satCost > 0.0) {
            player.causeFoodExhaustion(
                (float) (satCost * EXHAUSTION_PER_SATURATION)
            );
        }

        final double gain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_GAIN,
                DEFAULT_ZHENYUAN_GAIN
            )
        );
        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        if (gain > 0.0) {
            ZhenYuanHelper.modify(user, gain * multiplier);
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
