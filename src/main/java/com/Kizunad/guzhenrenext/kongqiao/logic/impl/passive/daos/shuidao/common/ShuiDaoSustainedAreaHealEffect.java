package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 水道通用被动：持续性维持（多资源）+ 群体疗愈/回复（随水道道痕动态变化）。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - radius（作用半径）<br>
 * - heal_amount（每秒治疗生命值）<br>
 * - zhenyuan_restore / jingli_restore / hunpo_restore（每秒回复对应资源）<br>
 * </p>
 */
public class ShuiDaoSustainedAreaHealEffect implements IGuEffect {

    private static final String META_RADIUS = "radius";
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_ZHENYUAN_RESTORE = "zhenyuan_restore";
    private static final String META_JINGLI_RESTORE = "jingli_restore";
    private static final String META_HUNPO_RESTORE = "hunpo_restore";

    private static final double DEFAULT_COST = 0.0;
    private static final double DEFAULT_AMOUNT = 0.0;
    private static final double DEFAULT_RADIUS = 0.0;

    private final String usageId;

    public ShuiDaoSustainedAreaHealEffect(final String usageId) {
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

        final double baseRadius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, DEFAULT_AMOUNT)
        );
        final double baseZhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_ZHENYUAN_RESTORE, DEFAULT_AMOUNT)
        );
        final double baseJingli = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_RESTORE, DEFAULT_AMOUNT)
        );
        final double baseHunpo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_RESTORE, DEFAULT_AMOUNT)
        );

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHUI_DAO
        );

        final double radius = Math.max(0.0, baseRadius * selfMultiplier);
        final double heal = baseHeal * selfMultiplier;
        final double zhenyuan = baseZhenyuan * selfMultiplier;
        final double jingli = baseJingli * selfMultiplier;
        final double hunpo = baseHunpo * selfMultiplier;

        final List<LivingEntity> targets = listTargets(user, radius);
        for (LivingEntity target : targets) {
            if (Double.compare(heal, 0.0) > 0) {
                target.heal((float) heal);
            }
            if (Double.compare(zhenyuan, 0.0) > 0) {
                ZhenYuanHelper.modify(target, zhenyuan);
            }
            if (Double.compare(jingli, 0.0) > 0) {
                JingLiHelper.modify(target, jingli);
            }
            if (Double.compare(hunpo, 0.0) > 0) {
                HunPoHelper.modify(target, hunpo);
            }
        }

        setActive(user, true);
    }

    private static List<LivingEntity> listTargets(
        final LivingEntity user,
        final double radius
    ) {
        if (radius <= 0.0) {
            return List.of(user);
        }
        final AABB box = user.getBoundingBox().inflate(radius);
        return user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e -> e.isAlive() && (e == user || e.isAlliedTo(user))
        );
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

