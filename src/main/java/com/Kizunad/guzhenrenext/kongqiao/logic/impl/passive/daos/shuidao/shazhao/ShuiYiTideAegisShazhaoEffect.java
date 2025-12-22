package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 水道被动杀招：潮衣护体。
 * <p>
 * 每秒维持扣费，提供护甲/护甲韧性，并刷新水下呼吸。
 * </p>
 */
public class ShuiYiTideAegisShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_shui_yi_tide_aegis";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.SHUI_DAO;

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS = "armor_toughness_bonus";
    private static final String META_WATER_BREATHING_DURATION_TICKS =
        "water_breathing_duration_ticks";

    private static final double DEFAULT_ARMOR_BONUS = 3.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 1.0;
    private static final int DEFAULT_WATER_BREATHING_TICKS = 260;

    private static final double MIN_VALUE = 0.0;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_shui_yi_tide_aegis_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_shui_yi_tide_aegis_toughness"
        );

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public void onSecond(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }

        if (!ShazhaoCostHelper.tryConsumeSustain(user, data)) {
            onInactive(user);
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DAO_TYPE)
        );

        final double baseArmor = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_ARMOR_BONUS, DEFAULT_ARMOR_BONUS)
        );
        final double baseToughness = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_ARMOR_TOUGHNESS_BONUS,
                DEFAULT_ARMOR_TOUGHNESS_BONUS
            )
        );

        applyAttributeModifier(
            user,
            Attributes.ARMOR,
            ARMOR_MODIFIER_ID,
            baseArmor * selfMultiplier
        );
        applyAttributeModifier(
            user,
            Attributes.ARMOR_TOUGHNESS,
            TOUGHNESS_MODIFIER_ID,
            baseToughness * selfMultiplier
        );

        final int baseTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_WATER_BREATHING_DURATION_TICKS,
                DEFAULT_WATER_BREATHING_TICKS
            )
        );
        final int durationTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseTicks,
            selfMultiplier
        );
        if (durationTicks > 0) {
            user.addEffect(
                new MobEffectInstance(MobEffects.WATER_BREATHING, durationTicks, 0, true, true)
            );
        }
    }

    @Override
    public void onInactive(final LivingEntity user) {
        removeAttributeModifier(user, Attributes.ARMOR, ARMOR_MODIFIER_ID);
        removeAttributeModifier(user, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER_ID);
    }

    private static void applyAttributeModifier(
        final LivingEntity user,
        final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attrHolder,
        final ResourceLocation modifierId,
        final double amount
    ) {
        if (user == null || attrHolder == null || modifierId == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attrHolder);
        if (attr == null) {
            return;
        }

        final double clamped = Math.max(MIN_VALUE, amount);
        final AttributeModifier existing = attr.getModifier(modifierId);
        if (existing != null && Double.compare(existing.amount(), clamped) == 0) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(modifierId);
        }
        if (clamped > MIN_VALUE) {
            attr.addTransientModifier(
                new AttributeModifier(
                    modifierId,
                    clamped,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }
    }

    private static void removeAttributeModifier(
        final LivingEntity user,
        final net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attrHolder,
        final ResourceLocation modifierId
    ) {
        if (user == null || attrHolder == null || modifierId == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attrHolder);
        if (attr != null && attr.getModifier(modifierId) != null) {
            attr.removeModifier(modifierId);
        }
    }
}

