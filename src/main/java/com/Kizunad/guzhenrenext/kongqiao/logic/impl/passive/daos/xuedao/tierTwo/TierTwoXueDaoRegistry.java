package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveArmTrapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveConsumeStoredBloodEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveSelfRecoveryEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoBloodGourdStorageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSenseGlowEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoTrapArmedOnHurtEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * 二转血道蛊虫注册表。
 */
public final class TierTwoXueDaoRegistry {

    private TierTwoXueDaoRegistry() {}

    public static void registerAll() {
        registerXieHuLuGu();
        registerXueYiGu();
        registerYinXueGu();
        registerXueHenGu();
        registerXueQiGu();
        registerZouXueGu();
        registerXieZhuiGu();
    }

    private static void registerXieHuLuGu() {
        final String passive = "guzhenren:xie_hu_lu_gu_passive_blood_gourd_store";
        final String active = "guzhenren:xie_hu_lu_gu_active_blood_gourd_drink";

        GuEffectRegistry.register(new XueDaoBloodGourdStorageEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveConsumeStoredBloodEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerXueYiGu() {
        final String passive = "guzhenren:xueyigu_passive_blood_armor_guard";
        final String active = "guzhenren:xueyigu_active_blood_armor_burst";

        GuEffectRegistry.register(
            new XueDaoHurtProcReductionEffect(
                passive,
                XueDaoCooldownKeys.proc(passive)
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveAoEBurstEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.POISON
            )
        );
    }

    private static void registerYinXueGu() {
        final String passive = "guzhenren:yin_xue_gu_passive_hidden_blood_trap";
        final String active = "guzhenren:yin_xue_gu_active_arm_trap";

        GuEffectRegistry.register(new XueDaoTrapArmedOnHurtEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveArmTrapEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerXueHenGu() {
        final String passive = "guzhenren:xuehengu_passive_blood_trace_sense";
        final String active = "guzhenren:xuehengu_active_blood_trace_ping";

        GuEffectRegistry.register(new XueDaoSenseGlowEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveAoEBurstEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.GLOWING
            )
        );
    }

    private static void registerXueQiGu() {
        final String passive = "guzhenren:xueqigu_passive_blood_qi_sustain";
        final String active = "guzhenren:xueqigu_active_blood_qi_recover";

        GuEffectRegistry.register(new XueDaoSustainedResourceRegenEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfRecoveryEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerZouXueGu() {
        final String passive = "guzhenren:zou_xue_gu_passive_congeal_sustain";
        final String active = "guzhenren:zou_xue_gu_active_congeal_heal";

        GuEffectRegistry.register(new XueDaoSustainedResourceRegenEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfRecoveryEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerXieZhuiGu() {
        final String passive = "guzhenren:xie_zhui_gu_passive_blood_spike_proc";
        final String active = "guzhenren:xie_zhui_gu_active_blood_spike";

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
}

