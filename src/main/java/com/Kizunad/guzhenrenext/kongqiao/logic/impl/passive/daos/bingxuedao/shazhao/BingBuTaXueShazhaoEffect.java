package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 冰雪道被动杀招：冰步踏雪。
 * <p>
 * 每秒维持扣费，提供少量移速与抗击退，并刷新短暂抗性；数值随【冰雪】道痕动态变化。
 * </p>
 */
public class BingBuTaXueShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bing_xue_dao_bing_bu_ta_xue";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_SPEED_BONUS = "speed_bonus";
    private static final String META_KNOCKBACK_RESISTANCE_BONUS = "knockback_resistance_bonus";
    private static final String META_RESISTANCE_DURATION_TICKS = "resistance_duration_ticks";
    private static final String META_RESISTANCE_AMPLIFIER = "resistance_amplifier";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_SPEED_BONUS = 0.06;
    private static final double DEFAULT_KNOCKBACK_RESISTANCE_BONUS = 0.05;
    private static final double MAX_SPEED_BONUS = 0.35;
    private static final double MAX_KNOCKBACK_RESISTANCE = 0.8;

    private static final int DEFAULT_RESISTANCE_DURATION_TICKS = 40;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 0;
    private static final int MAX_RESISTANCE_DURATION_TICKS = 200;

    private static final ResourceLocation SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bing_xue_dao_bing_bu_ta_xue_speed"
        );
    private static final ResourceLocation KNOCKBACK_RESISTANCE_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "shazhao_passive_bing_xue_dao_bing_bu_ta_xue_knockback_resistance"
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

        final double speedBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(data, META_SPEED_BONUS, DEFAULT_SPEED_BONUS)
        );
        final double speedScaled = DaoHenEffectScalingHelper.scaleValue(speedBase, selfMultiplier);
        final double speed = ShazhaoMetadataHelper.clamp(speedScaled, MIN_VALUE, MAX_SPEED_BONUS);
        BingXueDaoShazhaoEffectHelper.applyTransientModifier(
            user,
            Attributes.MOVEMENT_SPEED,
            SPEED_MODIFIER_ID,
            speed,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        final double kbBase = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_KNOCKBACK_RESISTANCE_BONUS,
                DEFAULT_KNOCKBACK_RESISTANCE_BONUS
            )
        );
        final double kbScaled = DaoHenEffectScalingHelper.scaleValue(kbBase, selfMultiplier);
        final double knockbackResistance = ShazhaoMetadataHelper.clamp(
            kbScaled,
            MIN_VALUE,
            MAX_KNOCKBACK_RESISTANCE
        );
        BingXueDaoShazhaoEffectHelper.applyTransientModifier(
            user,
            Attributes.KNOCKBACK_RESISTANCE,
            KNOCKBACK_RESISTANCE_MODIFIER_ID,
            knockbackResistance,
            AttributeModifier.Operation.ADD_VALUE
        );

        final int baseTicks = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                META_RESISTANCE_DURATION_TICKS,
                DEFAULT_RESISTANCE_DURATION_TICKS
            )
        );
        final int scaledTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseTicks, selfMultiplier);
        final int durationTicks = Math.min(MAX_RESISTANCE_DURATION_TICKS, Math.max(0, scaledTicks));
        if (durationTicks > 0) {
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
    }

    @Override
    public void onInactive(final LivingEntity user) {
        if (user == null || user.level().isClientSide()) {
            return;
        }
        BingXueDaoShazhaoEffectHelper.removeModifier(
            user,
            Attributes.MOVEMENT_SPEED,
            SPEED_MODIFIER_ID
        );
        BingXueDaoShazhaoEffectHelper.removeModifier(
            user,
            Attributes.KNOCKBACK_RESISTANCE,
            KNOCKBACK_RESISTANCE_MODIFIER_ID
        );
    }
}

