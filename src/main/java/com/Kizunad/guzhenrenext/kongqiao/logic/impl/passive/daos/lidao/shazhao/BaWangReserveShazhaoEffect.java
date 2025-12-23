package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
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
import net.minecraft.world.entity.LivingEntity;

/**
 * 力道被动杀招：霸王蓄势。
 * <p>
 * 高转常驻：每秒维持扣费，提升多项资源上限，并缓慢恢复真元/念头/精力；数值随【力】道痕动态变化。
 * </p>
 */
public class BaWangReserveShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_li_dao_ba_wang_reserve";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LI_DAO;

    private static final String META_MAX_ZHENYUAN_BONUS = "max_zhenyuan_bonus";
    private static final String META_MAX_JINGLI_BONUS = "max_jingli_bonus";
    private static final String META_MAX_HUNPO_BONUS = "max_hunpo_bonus";
    private static final String META_NIANTOU_CAPACITY_BONUS = "niantou_capacity_bonus";

    private static final String META_ZHENYUAN_RESTORE_PER_SECOND =
        "zhenyuan_restore_per_second";
    private static final String META_NIANTOU_RESTORE_PER_SECOND =
        "niantou_restore_per_second";
    private static final String META_JINGLI_RESTORE_PER_SECOND =
        "jingli_restore_per_second";

    private static final double DEFAULT_AMOUNT = 0.0;
    private static final double MAX_CAP_BONUS = 900.0;

    private static final double MAX_ZHENYUAN_RESTORE_PER_SECOND = 120.0;
    private static final double MAX_NIANTOU_RESTORE_PER_SECOND = 80.0;
    private static final double MAX_JINGLI_RESTORE_PER_SECOND = 80.0;

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
        applyRestore(user, data, selfMultiplier);
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

    private static void applyRestore(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double baseZhenyuan = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_ZHENYUAN_RESTORE_PER_SECOND,
                DEFAULT_AMOUNT
            )
        );
        final double zhenyuan = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseZhenyuan, multiplier),
            DEFAULT_AMOUNT,
            MAX_ZHENYUAN_RESTORE_PER_SECOND
        );
        if (zhenyuan > DEFAULT_AMOUNT) {
            ZhenYuanHelper.modify(user, zhenyuan);
        }

        final double baseNianTou = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_NIANTOU_RESTORE_PER_SECOND,
                DEFAULT_AMOUNT
            )
        );
        final double nianTou = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseNianTou, multiplier),
            DEFAULT_AMOUNT,
            MAX_NIANTOU_RESTORE_PER_SECOND
        );
        if (nianTou > DEFAULT_AMOUNT) {
            NianTouHelper.modify(user, nianTou);
        }

        final double baseJingli = Math.max(
            DEFAULT_AMOUNT,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_JINGLI_RESTORE_PER_SECOND,
                DEFAULT_AMOUNT
            )
        );
        final double jingli = ShazhaoMetadataHelper.clamp(
            DaoHenEffectScalingHelper.scaleValue(baseJingli, multiplier),
            DEFAULT_AMOUNT,
            MAX_JINGLI_RESTORE_PER_SECOND
        );
        if (jingli > DEFAULT_AMOUNT) {
            JingLiHelper.modify(user, jingli);
        }
    }

    private record CapSpec(String variableKey, String amountMetaKey) {}
}

