package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
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
 * 水道被动杀招：北水蓄势。
 * <p>
 * 高转常驻：每秒维持扣费，提升多项资源上限，并缓慢修复魂魄抗性。
 * </p>
 */
public class BeiShuiReserveShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bei_shui_reserve";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.SHUI_DAO;

    private static final String META_MAX_ZHENYUAN_BONUS = "max_zhenyuan_bonus";
    private static final String META_MAX_JINGLI_BONUS = "max_jingli_bonus";
    private static final String META_NIANTOU_CAPACITY_BONUS = "niantou_capacity_bonus";
    private static final String META_MAX_HUNPO_RESISTANCE_BONUS =
        "max_hunpo_resistance_bonus";
    private static final String META_HUNPO_RESISTANCE_RESTORE =
        "hunpo_resistance_restore";

    private static final double DEFAULT_AMOUNT = 0.0;
    private static final double MAX_CAP_BONUS = 900.0;
    private static final double MAX_HUNPO_RESISTANCE_RESTORE = 5.0;

    private static final List<CapSpec> CAPS = List.of(
        new CapSpec(GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN, META_MAX_ZHENYUAN_BONUS),
        new CapSpec(GuzhenrenVariableModifierService.VAR_MAX_JINGLI, META_MAX_JINGLI_BONUS),
        new CapSpec(GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY, META_NIANTOU_CAPACITY_BONUS),
        new CapSpec(
            GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
            META_MAX_HUNPO_RESISTANCE_BONUS
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
        applyHunpoResistanceRestore(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        if (user == null) {
            return;
        }
        for (CapSpec cap : CAPS) {
            GuzhenrenVariableModifierService.removeModifier(user, cap.variableKey(), SHAZHAO_ID);
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
                ShazhaoMetadataHelper.getDouble(data, cap.amountMetaKey(), DEFAULT_AMOUNT)
            );
            if (base <= DEFAULT_AMOUNT) {
                GuzhenrenVariableModifierService.removeModifier(
                    user,
                    cap.variableKey(),
                    SHAZHAO_ID
                );
                continue;
            }
            final double scaled = DaoHenEffectScalingHelper.scaleValue(base, multiplier);
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

    private static void applyHunpoResistanceRestore(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double base = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_HUNPO_RESISTANCE_RESTORE,
                DEFAULT_AMOUNT
            )
        );
        if (base <= DEFAULT_AMOUNT) {
            return;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(base, multiplier);
        final double clamped = ShazhaoMetadataHelper.clamp(
            scaled,
            DEFAULT_AMOUNT,
            MAX_HUNPO_RESISTANCE_RESTORE
        );
        if (clamped > DEFAULT_AMOUNT) {
            HunPoHelper.modifyResistance(user, clamped);
        }
    }

    private record CapSpec(String variableKey, String amountMetaKey) {}
}

