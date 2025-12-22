package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
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
 * 雷道被动杀招：电流养神。
 * <p>
 * 每秒维持扣费，稳定恢复精力与少量念头，作为空窍内的“续航”手段。
 * </p>
 */
public class DianLiuNourishShazhaoEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_dian_liu_nourish";

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LEI_DAO;

    private static final String META_JINGLI_RESTORE = "jingli_restore";
    private static final String META_NIANTOU_RESTORE = "niantou_restore";

    private static final double DEFAULT_JINGLI_RESTORE = 1.2;
    private static final double DEFAULT_NIANTOU_RESTORE = 0.08;

    private static final double MIN_VALUE = 0.0;

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

        final double baseJingLi = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_JINGLI_RESTORE,
                DEFAULT_JINGLI_RESTORE
            )
        );
        final double baseNianTou = Math.max(
            MIN_VALUE,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_NIANTOU_RESTORE,
                DEFAULT_NIANTOU_RESTORE
            )
        );

        final double jingliRestore = DaoHenEffectScalingHelper.scaleValue(
            baseJingLi,
            selfMultiplier
        );
        final double niantouRestore = DaoHenEffectScalingHelper.scaleValue(
            baseNianTou,
            selfMultiplier
        );

        if (jingliRestore > MIN_VALUE) {
            JingLiHelper.modify(user, jingliRestore);
        }
        if (niantouRestore > MIN_VALUE) {
            NianTouHelper.modify(user, niantouRestore);
        }
    }
}

