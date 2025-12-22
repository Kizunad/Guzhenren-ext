package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.shazhao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import net.minecraft.world.entity.LivingEntity;

/**
 * 火/炎道被动杀招【炉息归元】：持续维持并回复多资源（精力/魂魄/生命）。
 * <p>
 * 该杀招不改变属性，只在每秒扣费成功时执行回复；扣费失败时自然失效（不做提示，避免刷屏）。
 * </p>
 */
public class HuoDaoShazhaoFurnaceBreathEffect implements IShazhaoEffect {

    public static final String SHAZHAO_ID =
        "guzhenrenext:shazhao_passive_huo_dao_furnace_breath";

    private static final String META_JINGLI_REGEN_PER_SECOND =
        "jingli_regen_per_second";
    private static final String META_HUNPO_REGEN_PER_SECOND =
        "hunpo_regen_per_second";
    private static final String META_HEAL_PER_SECOND = "heal_per_second";

    private static final double MAX_REGEN_PER_SECOND = 100.0;
    private static final double MAX_HEAL_PER_SECOND = 100.0;

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
            DaoHenHelper.DaoType.HUO_DAO
        );

        final double jingliBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_JINGLI_REGEN_PER_SECOND, 0.0)
        );
        final double jingliRegen = Math.min(
            MAX_REGEN_PER_SECOND,
            DaoHenEffectScalingHelper.scaleValue(jingliBase, selfMultiplier)
        );
        if (jingliRegen > 0.0) {
            JingLiHelper.modify(user, jingliRegen);
        }

        final double hunpoBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_HUNPO_REGEN_PER_SECOND, 0.0)
        );
        final double hunpoRegen = Math.min(
            MAX_REGEN_PER_SECOND,
            DaoHenEffectScalingHelper.scaleValue(hunpoBase, selfMultiplier)
        );
        if (hunpoRegen > 0.0) {
            HunPoHelper.modify(user, hunpoRegen);
        }

        final double healBase = Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, META_HEAL_PER_SECOND, 0.0)
        );
        final double heal = Math.min(
            MAX_HEAL_PER_SECOND,
            DaoHenEffectScalingHelper.scaleValue(healBase, selfMultiplier)
        );
        if (heal > 0.0) {
            user.heal((float) heal);
        }
    }
}

