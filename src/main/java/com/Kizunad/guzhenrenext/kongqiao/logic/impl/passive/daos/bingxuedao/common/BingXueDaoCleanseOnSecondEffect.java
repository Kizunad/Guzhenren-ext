package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道通用被动：每秒驱散负面效果（清热解毒）。
 */
public class BingXueDaoCleanseOnSecondEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";
    private static final String META_MAX_REMOVED_PER_SECOND =
        "max_removed_per_second";

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 0.0;
    private static final int DEFAULT_MAX_REMOVED_PER_SECOND = 1;

    private final String usageId;

    public BingXueDaoCleanseOnSecondEffect(final String usageId) {
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

        final int maxRemoved = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_MAX_REMOVED_PER_SECOND,
                DEFAULT_MAX_REMOVED_PER_SECOND
            )
        );
        if (maxRemoved <= 0) {
            setActive(user, false);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_NIANTOU_COST_PER_SECOND, 0.0)
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_JINGLI_COST_PER_SECOND, 0.0)
        );
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HUNPO_COST_PER_SECOND, 0.0)
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

        final List<Holder<MobEffect>> toRemove = collectHarmfulEffects(user);
        int removed = 0;
        for (Holder<MobEffect> effect : toRemove) {
            if (effect == null) {
                continue;
            }
            if (removed >= maxRemoved) {
                break;
            }
            user.removeEffect(effect);
            removed++;
        }

        setActive(user, removed > 0);
    }

    private static List<Holder<MobEffect>> collectHarmfulEffects(
        final LivingEntity user
    ) {
        final List<Holder<MobEffect>> result = new ArrayList<>();
        for (MobEffectInstance inst : user.getActiveEffects()) {
            if (inst == null) {
                continue;
            }
            final Holder<MobEffect> effect = inst.getEffect();
            if (
                effect != null
                    && effect.value().getCategory() == MobEffectCategory.HARMFUL
            ) {
                result.add(effect);
            }
        }
        return result;
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
