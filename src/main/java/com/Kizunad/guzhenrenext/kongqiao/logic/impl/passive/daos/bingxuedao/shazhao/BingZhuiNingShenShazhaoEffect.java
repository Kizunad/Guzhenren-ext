package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

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
 * 冰雪道被动杀招：冰锥凝神。
 * <p>
 * 每秒维持扣费，以寒意凝神：缓慢恢复念头/精力/魂魄；数值随【冰雪】道痕动态变化。
 * </p>
 */
public class BingZhuiNingShenShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bing_xue_dao_bing_zhui_ning_shen";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.BING_XUE_DAO;

    private static final String META_NIANTOU_RESTORE_PER_SECOND = "niantou_restore_per_second";
    private static final String META_JINGLI_RESTORE_PER_SECOND = "jingli_restore_per_second";
    private static final String META_HUNPO_RESTORE_PER_SECOND = "hunpo_restore_per_second";

    private static final double MIN_VALUE = 0.0;

    private static final double DEFAULT_RESTORE_PER_SECOND = 0.0;
    private static final double MAX_NIANTOU_RESTORE_PER_SECOND = 8.0;
    private static final double MAX_JINGLI_RESTORE_PER_SECOND = 6.0;
    private static final double MAX_HUNPO_RESTORE_PER_SECOND = 4.0;

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
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DAO_TYPE)
        );

        restoreNianTou(user, data, selfMultiplier);
        restoreJingLi(user, data, selfMultiplier);
        restoreHunPo(user, data, selfMultiplier);
    }

    private static void restoreNianTou(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double base = Math.max(
            DEFAULT_RESTORE_PER_SECOND,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_NIANTOU_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        if (base <= MIN_VALUE) {
            return;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(base, multiplier);
        final double clamped = ShazhaoMetadataHelper.clamp(
            scaled,
            MIN_VALUE,
            MAX_NIANTOU_RESTORE_PER_SECOND
        );
        if (clamped > MIN_VALUE) {
            NianTouHelper.modify(user, clamped);
        }
    }

    private static void restoreJingLi(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double base = Math.max(
            DEFAULT_RESTORE_PER_SECOND,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_JINGLI_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        if (base <= MIN_VALUE) {
            return;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(base, multiplier);
        final double clamped = ShazhaoMetadataHelper.clamp(
            scaled,
            MIN_VALUE,
            MAX_JINGLI_RESTORE_PER_SECOND
        );
        if (clamped > MIN_VALUE) {
            JingLiHelper.modify(user, clamped);
        }
    }

    private static void restoreHunPo(
        final LivingEntity user,
        final ShazhaoData data,
        final double multiplier
    ) {
        final double base = Math.max(
            DEFAULT_RESTORE_PER_SECOND,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_HUNPO_RESTORE_PER_SECOND,
                DEFAULT_RESTORE_PER_SECOND
            )
        );
        if (base <= MIN_VALUE) {
            return;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(base, multiplier);
        final double clamped = ShazhaoMetadataHelper.clamp(
            scaled,
            MIN_VALUE,
            MAX_HUNPO_RESTORE_PER_SECOND
        );
        if (clamped > MIN_VALUE) {
            HunPoHelper.modify(user, clamped);
        }
    }
}

