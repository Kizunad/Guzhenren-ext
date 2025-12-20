package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 一转随意蛊：被动【随意赐福】。
 * <p>
 * 每秒消耗少量真元，随机赐予自身一种短暂增益，提供“灵活但不稳定”的体验。
 * </p>
 */
public class SuiYiGuRandomBlessingEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_BUFF_DURATION_TICKS = "buff_duration_ticks";
    private static final String META_BUFF_AMPLIFIER = "buff_amplifier";

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 2.5;
    private static final int DEFAULT_BUFF_DURATION_TICKS = 40;
    private static final int DEFAULT_BUFF_AMPLIFIER = 0;

    private final String usageId;

    public SuiYiGuRandomBlessingEffect(final String usageId) {
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

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BUFF_DURATION_TICKS,
                DEFAULT_BUFF_DURATION_TICKS
            )
        );
        if (duration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BUFF_AMPLIFIER,
                DEFAULT_BUFF_AMPLIFIER
            )
        );

        final Holder<MobEffect> effect = pickRandomEffect(user);
        if (effect == null) {
            return;
        }
        user.addEffect(
            new MobEffectInstance(effect, duration, amplifier, true, true)
        );
    }

    private static Holder<MobEffect> pickRandomEffect(final LivingEntity user) {
        final int roll = user.getRandom().nextInt(3);
        if (roll == 0) {
            return MobEffects.MOVEMENT_SPEED;
        }
        if (roll == 1) {
            return MobEffects.DIG_SPEED;
        }
        return MobEffects.REGENERATION;
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
