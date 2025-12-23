package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 力道被动杀招：石钢护身。
 * <p>
 * 每秒维持扣费：提供护甲/护甲韧性/抗击退加成，并短刷新伤害抗性；数值随【力】道痕动态变化。
 * </p>
 */
public class ShiGangAegisShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_li_dao_shi_gang_aegis";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LI_DAO;

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS = "armor_toughness_bonus";
    private static final String META_KNOCKBACK_RESISTANCE_BONUS =
        "knockback_resistance_bonus";
    private static final String META_RESISTANCE_DURATION_TICKS =
        "resistance_duration_ticks";
    private static final String META_RESISTANCE_AMPLIFIER = "resistance_amplifier";

    private static final double DEFAULT_ARMOR_BONUS = 80.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 40.0;
    private static final double DEFAULT_KNOCKBACK_RESISTANCE_BONUS = 0.15;

    private static final int DEFAULT_RESISTANCE_DURATION_TICKS = 60;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 0;

    private static final double MIN_VALUE = 0.0;
    private static final double MAX_ARMOR_BONUS = 600.0;
    private static final double MAX_ARMOR_TOUGHNESS_BONUS = 300.0;
    private static final double MAX_KNOCKBACK_RESISTANCE_BONUS = 1.0;

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_li_dao_shi_gang_aegis_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_li_dao_shi_gang_aegis_toughness"
        );
    private static final ResourceLocation KNOCKBACK_RESISTANCE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_li_dao_shi_gang_aegis_knockback_resistance"
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

        applyArmor(user, data, selfMultiplier);
        applyResistance(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        removeAttributeModifier(user, Attributes.ARMOR, ARMOR_MODIFIER_ID);
        removeAttributeModifier(
            user,
            Attributes.ARMOR_TOUGHNESS,
            TOUGHNESS_MODIFIER_ID
        );
        removeAttributeModifier(
            user,
            Attributes.KNOCKBACK_RESISTANCE,
            KNOCKBACK_RESISTANCE_MODIFIER_ID
        );
    }

    private static void applyArmor(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double baseArmor = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_ARMOR_BONUS, DEFAULT_ARMOR_BONUS)
        );
        final double armorBonus = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseArmor, multiplier),
            MIN_VALUE,
            MAX_ARMOR_BONUS
        );
        applyAttributeModifier(
            user,
            Attributes.ARMOR,
            ARMOR_MODIFIER_ID,
            armorBonus
        );

        final double baseToughness = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_ARMOR_TOUGHNESS_BONUS,
                DEFAULT_ARMOR_TOUGHNESS_BONUS
            )
        );
        final double toughnessBonus = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseToughness, multiplier),
            MIN_VALUE,
            MAX_ARMOR_TOUGHNESS_BONUS
        );
        applyAttributeModifier(
            user,
            Attributes.ARMOR_TOUGHNESS,
            TOUGHNESS_MODIFIER_ID,
            toughnessBonus
        );

        final double baseKnockbackResist = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_KNOCKBACK_RESISTANCE_BONUS,
                DEFAULT_KNOCKBACK_RESISTANCE_BONUS
            )
        );
        final double knockbackResist = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseKnockbackResist, multiplier),
            MIN_VALUE,
            MAX_KNOCKBACK_RESISTANCE_BONUS
        );
        applyAttributeModifier(
            user,
            Attributes.KNOCKBACK_RESISTANCE,
            KNOCKBACK_RESISTANCE_MODIFIER_ID,
            knockbackResist
        );
    }

    private static void applyResistance(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final int baseTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_RESISTANCE_DURATION_TICKS,
                DEFAULT_RESISTANCE_DURATION_TICKS
            )
        );
        final int durationTicks = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseTicks,
            multiplier
        );
        if (durationTicks <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_RESISTANCE_AMPLIFIER,
                DEFAULT_RESISTANCE_AMPLIFIER
            )
        );
        user.addEffect(
            new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                durationTicks,
                amplifier,
                true,
                true
            )
        );
    }

    private static void applyAttributeModifier(
        final LivingEntity user,
        final Holder<Attribute> attrHolder,
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
        final Holder<Attribute> attrHolder,
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

