package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * 血道被动杀招【血魄养窍】：每秒维持扣费，提升资源上限并提供少量续航回复。
 */
public class XueDaoShazhaoBloodApertureReserveEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_xue_dao_blood_aperture_reserve";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.XUE_DAO;

    private static final String META_MAX_HUNPO_BONUS = "max_hunpo_bonus";
    private static final String META_MAX_ZHENYUAN_BONUS = "max_zhenyuan_bonus";
    private static final String META_NIANTOU_CAPACITY_BONUS =
        "niantou_capacity_bonus";

    private static final String META_HEAL_PER_SECOND = "heal_per_second";
    private static final String META_HUNPO_GAIN = "hunpo_gain";
    private static final String META_JINGLI_GAIN = "jingli_gain";

    private static final double DEFAULT_AMOUNT = 0.0;
    private static final double MAX_CAP_BONUS = 900.0;
    private static final double MAX_HEAL_PER_SECOND = 20.0;
    private static final double MAX_RESOURCE_GAIN_PER_SECOND = 100.0;

    private static final List<CapSpec> CAPS = List.of(
        new CapSpec(
            GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
            META_MAX_HUNPO_BONUS
        ),
        new CapSpec(
            GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
            META_MAX_ZHENYUAN_BONUS
        ),
        new CapSpec(
            GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
            META_NIANTOU_CAPACITY_BONUS
        )
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

        applyCaps(user, data, selfMultiplier);
        applyRegen(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        if (user == null) {
            return;
        }
        for (CapSpec cap : CAPS) {
            GuzhenrenVariableModifierService.removeModifier(
                user,
                cap.variableKey(),
                SHAZHAO_ID
            );
        }
    }

    private static void applyCaps(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        for (CapSpec cap : CAPS) {
            final double base = Math.max(
                DEFAULT_AMOUNT,
                ShazhaoMetadataHelper.getDouble(
                    data,
                    cap.amountMetaKey(),
                    DEFAULT_AMOUNT
                )
            );
            if (base <= DEFAULT_AMOUNT) {
                GuzhenrenVariableModifierService.removeModifier(
                    user,
                    cap.variableKey(),
                    SHAZHAO_ID
                );
                continue;
            }

            final double scaled = DaoHenEffectScalingHelper.scaleValue(
                base,
                multiplier
            );
            final double clamped = ShazhaoMetadataHelper.clamp(
                scaled,
                DEFAULT_AMOUNT,
                MAX_CAP_BONUS
            );
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                cap.variableKey(),
                SHAZHAO_ID,
                clamped
            );
        }
    }

    private static void applyRegen(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double healBase = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_HEAL_PER_SECOND,
                DEFAULT_AMOUNT
            )
        );
        final double heal = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(healBase, multiplier),
            DEFAULT_AMOUNT,
            MAX_HEAL_PER_SECOND
        );
        if (heal > DEFAULT_AMOUNT) {
            user.heal((float) heal);
        }

        final double hunpoBase = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(data, META_HUNPO_GAIN, DEFAULT_AMOUNT)
        );
        final double hunpo = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(hunpoBase, multiplier),
            DEFAULT_AMOUNT,
            MAX_RESOURCE_GAIN_PER_SECOND
        );
        if (hunpo > DEFAULT_AMOUNT) {
            HunPoHelper.modify(user, hunpo);
        }

        final double jingliBase = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_JINGLI_GAIN,
                DEFAULT_AMOUNT
            )
        );
        final double jingli = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(jingliBase, multiplier),
            DEFAULT_AMOUNT,
            MAX_RESOURCE_GAIN_PER_SECOND
        );
        if (jingli > DEFAULT_AMOUNT) {
            JingLiHelper.modify(user, jingli);
        }
    }

    private record CapSpec(String variableKey, String amountMetaKey) {}
}

