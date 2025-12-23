package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 冰雪道被动杀招：冰肌玉骨。
 * <p>
 * 每秒维持扣费，提供护甲/护甲韧性与短暂火抗/抗性，并提升少量资源上限（高转向）；数值随【冰雪】道痕动态变化。
 * </p>
 */
public class BingJiYuGuShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bing_xue_dao_bing_ji_yu_gu";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_ARMOR_BONUS = "armor_bonus";
    private static final String META_ARMOR_TOUGHNESS_BONUS = "armor_toughness_bonus";
    private static final String META_FIRE_RESISTANCE_DURATION_TICKS = "fire_resistance_duration_ticks";
    private static final String META_RESISTANCE_DURATION_TICKS = "resistance_duration_ticks";
    private static final String META_RESISTANCE_AMPLIFIER = "resistance_amplifier";

    private static final String META_MAX_JINGLI_BONUS = "max_jingli_bonus";
    private static final String META_NIANTOU_CAPACITY_BONUS = "niantou_capacity_bonus";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_ARMOR_BONUS = 40.0;
    private static final double DEFAULT_ARMOR_TOUGHNESS_BONUS = 18.0;
    private static final double MAX_ARMOR_BONUS = 220.0;
    private static final double MAX_ARMOR_TOUGHNESS_BONUS = 120.0;

    private static final int DEFAULT_FIRE_RESISTANCE_DURATION_TICKS = 80;
    private static final int DEFAULT_RESISTANCE_DURATION_TICKS = 60;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 0;
    private static final int MAX_DURATION_TICKS = 320;

    private static final double DEFAULT_CAP_BONUS = 0.0;
    private static final double MAX_CAP_BONUS = 360.0;

    private static final List<CapSpec> CAPS = List.of(
        new CapSpec(GuzhenrenVariableModifierService.VAR_MAX_JINGLI, META_MAX_JINGLI_BONUS),
        new CapSpec(GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY, META_NIANTOU_CAPACITY_BONUS)
    );

    private static final ResourceLocation ARMOR_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bing_xue_dao_bing_ji_yu_gu_armor"
        );
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bing_xue_dao_bing_ji_yu_gu_toughness"
        );

    @Override
    public String getShazhaoId() {
        return SHAZHAO_ID;
    }

    @Override
    public void onSecond(final LivingEntity user, final ShazhaoData data) {
        if (user == null || data == null || user.level().isClientSide()) {
            return;
        }

        if (!ShazhaoCostHelper.tryConsumeSustain(user, data)) {
            onInactive(user);
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DAO_TYPE)
        );

        applyAttributes(user, data, selfMultiplier);
        applyCaps(user, data, selfMultiplier);
        refreshEffects(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        if (user == null || user.level().isClientSide()) {
            return;
        }
        BingXueDaoShazhaoEffectHelper.removeModifier(user, Attributes.ARMOR, ARMOR_MODIFIER_ID);
        BingXueDaoShazhaoEffectHelper.removeModifier(
            user,
            Attributes.ARMOR_TOUGHNESS,
            TOUGHNESS_MODIFIER_ID
        );
        for (CapSpec cap : CAPS) {
            GuzhenrenVariableModifierService.removeModifier(user, cap.variableKey(), SHAZHAO_ID);
        }
    }

    private static void applyAttributes(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double armorBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_ARMOR_BONUS, DEFAULT_ARMOR_BONUS)
        );
        final double armorScaled = DaoHenEffectScalingHelper.scaleValue(armorBase, multiplier);
        final double armor = ShazhaoMetadataHelper.clamp(armorScaled, MIN_VALUE, MAX_ARMOR_BONUS);
        BingXueDaoShazhaoEffectHelper.applyTransientModifier(
            user,
            Attributes.ARMOR,
            ARMOR_MODIFIER_ID,
            armor,
            AttributeModifier.Operation.ADD_VALUE
        );

        final double toughnessBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_ARMOR_TOUGHNESS_BONUS,
                DEFAULT_ARMOR_TOUGHNESS_BONUS
            )
        );
        final double toughnessScaled = DaoHenEffectScalingHelper.scaleValue(toughnessBase, multiplier);
        final double toughness = ShazhaoMetadataHelper.clamp(
            toughnessScaled,
            MIN_VALUE,
            MAX_ARMOR_TOUGHNESS_BONUS
        );
        BingXueDaoShazhaoEffectHelper.applyTransientModifier(
            user,
            Attributes.ARMOR_TOUGHNESS,
            TOUGHNESS_MODIFIER_ID,
            toughness,
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    private static void applyCaps(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        for (CapSpec cap : CAPS) {
            final double base = Math.max(
                DEFAULT_CAP_BONUS,
                ShazhaoMetadataHelper.getDouble(data, cap.amountMetaKey(), DEFAULT_CAP_BONUS)
            );
            if (base <= DEFAULT_CAP_BONUS) {
                GuzhenrenVariableModifierService.removeModifier(user, cap.variableKey(), SHAZHAO_ID);
                continue;
            }
            final double scaled = DaoHenEffectScalingHelper.scaleValue(base, multiplier);
            final double clamped = ShazhaoMetadataHelper.clamp(scaled, MIN_VALUE, MAX_CAP_BONUS);
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                cap.variableKey(),
                SHAZHAO_ID,
                clamped
            );
        }
    }

    private static void refreshEffects(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final int baseFireTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_FIRE_RESISTANCE_DURATION_TICKS,
                DEFAULT_FIRE_RESISTANCE_DURATION_TICKS
            )
        );
        final int fireTicks = Math.min(
            MAX_DURATION_TICKS,
            Math.max(0, DaoHenEffectScalingHelper.scaleDurationTicks(baseFireTicks, multiplier))
        );
        if (fireTicks > 0) {
            user.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, fireTicks, 0, true, true));
        }

        final int baseResTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_RESISTANCE_DURATION_TICKS,
                DEFAULT_RESISTANCE_DURATION_TICKS
            )
        );
        final int resTicks = Math.min(
            MAX_DURATION_TICKS,
            Math.max(0, DaoHenEffectScalingHelper.scaleDurationTicks(baseResTicks, multiplier))
        );
        if (resTicks > 0) {
            final int amplifier = Math.max(
                0,
                ShazhaoMetadataHelper.getInt(
                    data,
                    META_RESISTANCE_AMPLIFIER,
                    DEFAULT_RESISTANCE_AMPLIFIER
                )
            );
            user.addEffect(
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, resTicks, amplifier, true, true)
            );
        }
    }

    private record CapSpec(String variableKey, String amountMetaKey) {}
}

