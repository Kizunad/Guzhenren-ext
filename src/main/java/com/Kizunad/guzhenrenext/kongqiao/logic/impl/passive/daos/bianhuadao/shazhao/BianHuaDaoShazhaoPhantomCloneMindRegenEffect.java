package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao;

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
 * 变化道被动杀招【幻身养念】：以分身幻影分摊心神消耗，缓慢回复念头并提振精力（持续维持）。
 */
public class BianHuaDaoShazhaoPhantomCloneMindRegenEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_bian_hua_phantom_clone_mind_regen";

    private static final DaoHenHelper.DaoType DAO_TYPE =
        DaoHenHelper.DaoType.BIAN_HUA_DAO;

    private static final String META_NIANTOU_GAIN_PER_SECOND =
        "niantou_gain_per_second";
    private static final String META_JINGLI_GAIN_PER_SECOND =
        "jingli_gain_per_second";

    private static final double DEFAULT_NIANTOU_GAIN_PER_SECOND = 0.35;
    private static final double DEFAULT_JINGLI_GAIN_PER_SECOND = 0.12;

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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DAO_TYPE
        );

        final double baseNianTou = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_NIANTOU_GAIN_PER_SECOND,
                DEFAULT_NIANTOU_GAIN_PER_SECOND
            )
        );
        final double baseJingli = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(
                data,
                META_JINGLI_GAIN_PER_SECOND,
                DEFAULT_JINGLI_GAIN_PER_SECOND
            )
        );

        if (baseNianTou > 0.0) {
            NianTouHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseNianTou, selfMultiplier)
            );
        }
        if (baseJingli > 0.0) {
            JingLiHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseJingli, selfMultiplier)
            );
        }
    }
}

