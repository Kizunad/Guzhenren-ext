package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveSelfRecoveryEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveSummonBloodPythonEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedMaxCapModifierEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * 五转血道蛊虫注册表。
 */
public final class TierFiveXueDaoRegistry {

    private TierFiveXueDaoRegistry() {}

    public static void registerAll() {
        registerXieLuGuFive();
        registerXueHeMangGu();
        registerXueShouYinGu();
        registerXueDiZiGu();
    }

    private static void registerXieLuGuFive() {
        final String passive = "guzhenren:xie_lu_gu_5_passive_blood_skull_caps";
        final String active = "guzhenren:xie_lu_gu_5_active_blood_skull_refine";

        GuEffectRegistry.register(new XueDaoSustainedMaxCapModifierEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfRecoveryEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerXueHeMangGu() {
        final String passive = "guzhenren:xue_he_mang_gu_passive_serpent_fury_proc";
        final String active = "guzhenren:xue_he_mang_gu_active_summon_blood_python";

        GuEffectRegistry.register(
            new XueDaoAttackProcBonusDamageEffect(
                passive,
                XueDaoCooldownKeys.proc(passive),
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveSummonBloodPythonEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerXueShouYinGu() {
        final String passive = "guzhenren:xue_shou_yin_gu_passive_blood_palm_proc";
        final String active = "guzhenren:xue_shou_yin_gu_active_blood_palm_finisher";

        GuEffectRegistry.register(
            new XueDaoAttackProcBonusDamageEffect(
                passive,
                XueDaoCooldownKeys.proc(passive),
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveTargetStrikeEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerXueDiZiGu() {
        final String passive = "guzhenren:xue_di_zi_gu_passive_blood_split_caps";
        final String active = "guzhenren:xue_di_zi_gu_active_blood_split_burst";

        GuEffectRegistry.register(new XueDaoSustainedMaxCapModifierEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveAoEBurstEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }
}

