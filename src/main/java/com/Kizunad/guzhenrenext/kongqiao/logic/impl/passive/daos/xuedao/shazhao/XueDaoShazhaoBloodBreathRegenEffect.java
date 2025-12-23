package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.world.entity.LivingEntity;

/**
 * 血道被动杀招【血息回春】：每秒维持扣费，小幅回复生命/魂魄/念头/精力。
 * <p>
 * 约束：非伤害类数值使用 {@link DaoHenEffectScalingHelper} 做倍率裁剪，避免离谱膨胀。
 * </p>
 */
public class XueDaoShazhaoBloodBreathRegenEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_xue_dao_blood_breath_regen";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.XUE_DAO;

    private static final String META_HEAL_PER_SECOND = "heal_per_second";
    private static final String META_HUNPO_GAIN = "hunpo_gain";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_JINGLI_GAIN = "jingli_gain";

    private static final double DEFAULT_AMOUNT = 0.0;
    private static final double MAX_HEAL_PER_SECOND = 20.0;
    private static final double MAX_RESOURCE_GAIN_PER_SECOND = 100.0;

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
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DAO_TYPE)
        );

        final double healBase = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_HEAL_PER_SECOND,
                DEFAULT_AMOUNT
            )
        );
        final double heal = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(healBase, selfMultiplier),
            DEFAULT_AMOUNT,
            MAX_HEAL_PER_SECOND
        );
        if (heal > DEFAULT_AMOUNT) {
            user.heal((float) heal);
        }

        restore(
            user,
            data,
            META_HUNPO_GAIN,
            selfMultiplier,
            MAX_RESOURCE_GAIN_PER_SECOND,
            HunPoHelper::modify
        );
        restore(
            user,
            data,
            META_NIANTOU_GAIN,
            selfMultiplier,
            MAX_RESOURCE_GAIN_PER_SECOND,
            NianTouHelper::modify
        );
        restore(
            user,
            data,
            META_JINGLI_GAIN,
            selfMultiplier,
            MAX_RESOURCE_GAIN_PER_SECOND,
            JingLiHelper::modify
        );
    }

    private static void restore(
        final LivingEntity user,
        final ShazhaoData data,
        final String key,
        final double multiplier,
        final double max,
        final ResourceModifier modifier
    ) {
        final double base = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(data, key, DEFAULT_AMOUNT)
        );
        if (base <= DEFAULT_AMOUNT) {
            return;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(
            base,
            multiplier
        );
        final double amount = ShazhaoMetadataHelper.clamp(
            scaled,
            DEFAULT_AMOUNT,
            max
        );
        if (amount > DEFAULT_AMOUNT) {
            modifier.apply(user, amount);
        }
    }

    @FunctionalInterface
    private interface ResourceModifier {
        void apply(LivingEntity user, double amount);
    }
}

