package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * 三转养生蛊：被动【养生滋补】。
 * <p>
 * 设计目标：提供稳定的生存向收益（生命上限与缓慢回血），并以真元消耗做长期权衡。
 * </p>
 */
public class YangShengGuHealthNourishEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:yangshenggu_passive_health_nourish";

    private static final String META_MAX_HEALTH_BONUS = "max_health_bonus";
    private static final String META_REGEN_AMPLIFIER = "regen_amplifier";
    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";

    private static final double DEFAULT_MAX_HEALTH_BONUS = 6.0;
    private static final int DEFAULT_REGEN_AMPLIFIER = 0;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 180.0;

    private static final ResourceLocation MAX_HEALTH_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:yangshenggu_max_health");

    private static final int EFFECT_REFRESH_TICKS = 25;

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
            deactivate(user);
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
            deactivate(user);
            return;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
        }

        final double maxHealthBonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_MAX_HEALTH_BONUS,
                DEFAULT_MAX_HEALTH_BONUS
            )
        );
        applyMaxHealth(user, maxHealthBonus);

        final int regenAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_REGEN_AMPLIFIER,
                DEFAULT_REGEN_AMPLIFIER
            )
        );
        user.addEffect(
            new MobEffectInstance(
                MobEffects.REGENERATION,
                EFFECT_REFRESH_TICKS,
                regenAmplifier,
                true,
                false,
                true
            )
        );

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
        deactivate(user);
    }

    private static void deactivate(final LivingEntity user) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.remove(USAGE_ID);
        }
        removeMaxHealth(user);
    }

    private static void applyMaxHealth(
        final LivingEntity user,
        final double amount
    ) {
        final AttributeInstance attr = user.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(MAX_HEALTH_MODIFIER_ID);
        if (existing == null || Double.compare(existing.amount(), amount) != 0) {
            if (existing != null) {
                attr.removeModifier(MAX_HEALTH_MODIFIER_ID);
            }
            if (Double.compare(amount, 0.0) != 0) {
                attr.addTransientModifier(
                    new AttributeModifier(
                        MAX_HEALTH_MODIFIER_ID,
                        amount,
                        AttributeModifier.Operation.ADD_VALUE
                    )
                );
            }
        }
    }

    private static void removeMaxHealth(final LivingEntity user) {
        final AttributeInstance attr = user.getAttribute(Attributes.MAX_HEALTH);
        if (attr == null) {
            return;
        }
        if (attr.getModifier(MAX_HEALTH_MODIFIER_ID) != null) {
            attr.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }
    }
}

