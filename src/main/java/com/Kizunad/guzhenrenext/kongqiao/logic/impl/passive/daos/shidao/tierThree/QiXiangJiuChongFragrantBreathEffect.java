package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * 三转七香酒虫：被动【七香提炼】。
 * <p>
 * 设计目标：稳定移速收益（跑图/追击），以真元持续供养香息作为代价。</p>
 */
public class QiXiangJiuChongFragrantBreathEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:qi_xiang_jiu_chong_passive_fragrant_breath";

    private static final String META_SPEED_BONUS = "speed_bonus";

    private static final double DEFAULT_SPEED_BONUS = 0.08;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 120.0;

    private static final ResourceLocation SPEED_MODIFIER_ID =
        ResourceLocation.parse("guzhenren:qi_xiang_jiu_chong_speed");

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
            deactivate(user);
            return;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double speedBonus = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_SPEED_BONUS,
                DEFAULT_SPEED_BONUS
            ),
            0.0,
            1.0
        );
        applySpeed(user, UsageMetadataHelper.clamp(speedBonus * multiplier, 0.0, 1.0));

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
        removeSpeed(user);
    }

    private static void applySpeed(final LivingEntity user, final double amount) {
        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        final AttributeModifier existing = attr.getModifier(SPEED_MODIFIER_ID);
        if (existing == null || Double.compare(existing.amount(), amount) != 0) {
            if (existing != null) {
                attr.removeModifier(SPEED_MODIFIER_ID);
            }
            if (Double.compare(amount, 0.0) != 0) {
                attr.addTransientModifier(
                    new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        amount,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                );
            }
        }
    }

    private static void removeSpeed(final LivingEntity user) {
        final AttributeInstance attr = user.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        if (attr.getModifier(SPEED_MODIFIER_ID) != null) {
            attr.removeModifier(SPEED_MODIFIER_ID);
        }
    }
}
