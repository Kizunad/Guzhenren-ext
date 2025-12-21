package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveBloodRainDomainEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveDrinkBloodWineEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveSelfRecoveryEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoBloodWineStackEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedMaxCapModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedResourceRegenEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * 四转血道蛊虫注册表。
 */
public final class TierFourXueDaoRegistry {

    private TierFourXueDaoRegistry() {}

    public static void registerAll() {
        registerHuaXieLuGu();
        registerNuMuXieYanGu();
        registerXueYuGu();
        registerXieCangGongGu();
        registerXueBaoGu();
        registerXieNingJianGu();
        registerCanYueXueYiGu();
    }

    private static void registerHuaXieLuGu() {
        final String passive = "guzhenren:hua_xie_lu_gu_passive_blood_skull_caps";
        final String active = "guzhenren:hua_xie_lu_gu_active_blood_skull_refine";

        GuEffectRegistry.register(new XueDaoSustainedMaxCapModifierEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfRecoveryEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerNuMuXieYanGu() {
        final String passive = "guzhenren:nu_mu_xie_yan_gu_passive_anger_leech_proc";
        final String active = "guzhenren:nu_mu_xie_yan_gu_active_anger_eye_reveal";

        GuEffectRegistry.register(
            new XueDaoAttackProcBonusDamageEffect(
                passive,
                XueDaoCooldownKeys.proc(passive),
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveAoEBurstEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.GLOWING
            )
        );
    }

    private static void registerXueYuGu() {
        final String passive = "guzhenren:xuehugu_passive_blood_rain_sustain";
        final String active = "guzhenren:xuehugu_active_blood_rain_domain";

        GuEffectRegistry.register(new XueDaoSustainedResourceRegenEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveBloodRainDomainEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerXieCangGongGu() {
        final String passive = "guzhenren:xie_cang_gong_gu_passive_blood_arrow_proc";
        final String active = "guzhenren:xie_cang_gong_gu_active_blood_arrow";

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

    private static void registerXueBaoGu() {
        final String passive = "guzhenren:xuebaogu_passive_blood_burst_sustain";
        final String active = "guzhenren:xuebaogu_active_blood_burst";

        GuEffectRegistry.register(new XueDaoSustainedResourceRegenEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveAoEBurstEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerXieNingJianGu() {
        final String passive = "guzhenren:xie_ning_jian_gu_passive_blood_wine_stack";
        final String active = "guzhenren:xie_ning_jian_gu_active_drink_blood_wine";

        GuEffectRegistry.register(new XueDaoBloodWineStackEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveDrinkBloodWineEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerCanYueXueYiGu() {
        final String passive = "guzhenren:canyuexueyi_passive_crescent_armor_guard";
        final String active = "guzhenren:canyuexueyi_active_crescent_slash";

        GuEffectRegistry.register(
            new XueDaoHurtProcReductionEffect(
                passive,
                XueDaoCooldownKeys.proc(passive)
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveTargetStrikeEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }
}

