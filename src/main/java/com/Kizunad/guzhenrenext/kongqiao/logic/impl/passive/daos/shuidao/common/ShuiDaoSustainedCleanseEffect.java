package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 水道通用被动：持续维持（多资源）+ 自身净化（清除负面效果）。
 * <p>
 * 用于“洁泽”等偏辅助蛊虫：每秒检查并清除少量有害效果，受水道道痕影响（上限保护）。
 * </p>
 *
 * <p>metadata:</p>
 * <ul>
 *   <li>zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second</li>
 *   <li>max_remove（每秒最多清除数量，基础值）</li>
 * </ul>
 */
public class ShuiDaoSustainedCleanseEffect implements IGuEffect {

    private static final String META_MAX_REMOVE = "max_remove";

    private static final double DEFAULT_COST = 0.0;
    private static final int DEFAULT_MAX_REMOVE = 1;
    private static final int MAX_REMOVE_CAP = 8;

    private final String usageId;

    public ShuiDaoSustainedCleanseEffect(final String usageId) {
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

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
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

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            setActive(user, false);
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHUI_DAO
        );
        final double clamped = DaoHenEffectScalingHelper.clampMultiplier(selfMultiplier);

        final int baseMaxRemove = Math.max(
            0,
            UsageMetadataHelper.getInt(usageInfo, META_MAX_REMOVE, DEFAULT_MAX_REMOVE)
        );
        final int maxRemove = (int) UsageMetadataHelper.clamp(
            Math.round(baseMaxRemove * clamped),
            0,
            MAX_REMOVE_CAP
        );
        if (maxRemove <= 0) {
            setActive(user, true);
            return;
        }

        cleanseHarmful(user, maxRemove);
        setActive(user, true);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        setActive(user, false);
    }

    private static void cleanseHarmful(final LivingEntity target, final int maxRemove) {
        if (target == null || maxRemove <= 0) {
            return;
        }
        final List<Holder<net.minecraft.world.effect.MobEffect>> toRemove =
            new ArrayList<>();
        for (MobEffectInstance inst : target.getActiveEffects()) {
            if (inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                toRemove.add(inst.getEffect());
            }
            if (toRemove.size() >= maxRemove) {
                break;
            }
        }
        for (Holder<net.minecraft.world.effect.MobEffect> effect : toRemove) {
            target.removeEffect(effect);
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

