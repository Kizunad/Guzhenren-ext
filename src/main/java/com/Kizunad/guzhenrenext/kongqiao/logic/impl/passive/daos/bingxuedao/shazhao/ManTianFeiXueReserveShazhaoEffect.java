package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 冰雪道被动杀招：漫天飞雪·寒月归元。
 * <p>
 * 高转常驻：每秒维持扣费，提升多项资源上限，并缓慢恢复真元/念头/精力/魂魄，且刷新短暂抗性；数值随【冰雪】道痕动态变化。
 * </p>
 */
public class ManTianFeiXueReserveShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bing_xue_dao_man_tian_fei_xue_reserve";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_MAX_ZHENYUAN_BONUS = "max_zhenyuan_bonus";
    private static final String META_MAX_JINGLI_BONUS = "max_jingli_bonus";
    private static final String META_MAX_HUNPO_BONUS = "max_hunpo_bonus";
    private static final String META_NIANTOU_CAPACITY_BONUS = "niantou_capacity_bonus";

    private static final String META_ZHENYUAN_RESTORE_PER_SECOND = "zhenyuan_restore_per_second";
    private static final String META_NIANTOU_RESTORE_PER_SECOND = "niantou_restore_per_second";
    private static final String META_JINGLI_RESTORE_PER_SECOND = "jingli_restore_per_second";
    private static final String META_HUNPO_RESTORE_PER_SECOND = "hunpo_restore_per_second";

    private static final String META_RESISTANCE_DURATION_TICKS = "resistance_duration_ticks";
    private static final String META_RESISTANCE_AMPLIFIER = "resistance_amplifier";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_AMOUNT = 0.0;
    private static final double MAX_CAP_BONUS = 900.0;
    private static final double MAX_ZHENYUAN_RESTORE_PER_SECOND = 120.0;
    private static final double MAX_NIANTOU_RESTORE_PER_SECOND = 30.0;
    private static final double MAX_JINGLI_RESTORE_PER_SECOND = 20.0;
    private static final double MAX_HUNPO_RESTORE_PER_SECOND = 12.0;

    private static final int DEFAULT_RESISTANCE_DURATION_TICKS = 60;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 0;
    private static final int MAX_RESISTANCE_DURATION_TICKS = 260;

    private static final List<CapSpec> CAPS = List.of(
        new CapSpec(GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN, META_MAX_ZHENYUAN_BONUS),
        new CapSpec(GuzhenrenVariableModifierService.VAR_MAX_JINGLI, META_MAX_JINGLI_BONUS),
        new CapSpec(GuzhenrenVariableModifierService.VAR_MAX_HUNPO, META_MAX_HUNPO_BONUS),
        new CapSpec(GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY, META_NIANTOU_CAPACITY_BONUS)
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

        applyCaps(user, data, selfMultiplier);
        restoreResources(user, data, selfMultiplier);
        refreshResistance(user, data, selfMultiplier);
    }

    @Override
    public void onInactive(final LivingEntity user) {
        if (user == null || user.level().isClientSide()) {
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

    private static void restoreResources(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double zhenyuan = clampScaled(
            ShazhaoMetadataHelper.getDouble(data, META_ZHENYUAN_RESTORE_PER_SECOND, DEFAULT_AMOUNT),
            multiplier,
            MAX_ZHENYUAN_RESTORE_PER_SECOND
        );
        final double niantou = clampScaled(
            ShazhaoMetadataHelper.getDouble(data, META_NIANTOU_RESTORE_PER_SECOND, DEFAULT_AMOUNT),
            multiplier,
            MAX_NIANTOU_RESTORE_PER_SECOND
        );
        final double jingli = clampScaled(
            ShazhaoMetadataHelper.getDouble(data, META_JINGLI_RESTORE_PER_SECOND, DEFAULT_AMOUNT),
            multiplier,
            MAX_JINGLI_RESTORE_PER_SECOND
        );
        final double hunpo = clampScaled(
            ShazhaoMetadataHelper.getDouble(data, META_HUNPO_RESTORE_PER_SECOND, DEFAULT_AMOUNT),
            multiplier,
            MAX_HUNPO_RESTORE_PER_SECOND
        );

        if (zhenyuan > MIN_VALUE) {
            ZhenYuanHelper.modify(user, zhenyuan);
        }
        if (niantou > MIN_VALUE) {
            NianTouHelper.modify(user, niantou);
        }
        if (jingli > MIN_VALUE) {
            JingLiHelper.modify(user, jingli);
        }
        if (hunpo > MIN_VALUE) {
            HunPoHelper.modify(user, hunpo);
        }
    }

    private static double clampScaled(
        final double base,
        final double multiplier,
        final double maxValue
    ) {
        final double safeBase = Math.max(DEFAULT_AMOUNT, base);
        if (safeBase <= MIN_VALUE) {
            return MIN_VALUE;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(safeBase, multiplier);
        return ShazhaoMetadataHelper.clamp(scaled, MIN_VALUE, maxValue);
    }

    private static void refreshResistance(
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
        final int scaledTicks = DaoHenEffectScalingHelper.scaleDurationTicks(baseTicks, multiplier);
        final int durationTicks = Math.min(
            MAX_RESISTANCE_DURATION_TICKS,
            Math.max(0, scaledTicks)
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

    private record CapSpec(String variableKey, String amountMetaKey) {}
}

