package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 刀道持续类被动效果通用骨架：
 * <ul>
 *     <li>统一处理：开关检查、持续扣费、道痕倍率获取、激活态写入 ActivePassives</li>
 *     <li>子类只关心：激活态时每秒逻辑、失活/卸下时的清理逻辑</li>
 * </ul>
 */
public abstract class AbstractDaoDaoSustainedEffect implements IGuEffect {

    protected static final double DEFAULT_COST_PER_SECOND = 0.0;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;

    protected AbstractDaoDaoSustainedEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType
    ) {
        this.usageId = Objects.requireNonNull(usageId, "usageId");
        this.daoType = daoType;
    }

    @Override
    public final String getUsageId() {
        return usageId;
    }

    @Override
    public final void onSecond(
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
            setActive(user, false);
            onInactive(user, stack, usageInfo);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
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
            onInactive(user, stack, usageInfo);
            return;
        }

        setActive(user, true);
        final double multiplier = daoType == null
            ? 1.0
            : DaoHenCalculator.calculateSelfMultiplier(user, daoType);
        onSustainSecond(user, stack, usageInfo, multiplier);
    }

    @Override
    public final void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null) {
            return;
        }
        setActive(user, false);
        onInactive(user, stack, usageInfo);
    }

    protected final boolean isActive(final LivingEntity user) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(
            user
        );
        return actives != null && actives.isActive(usageId);
    }

    protected abstract void onSustainSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo,
        double daoHenMultiplier
    );

    protected abstract void onInactive(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    );

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
