package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveSelfRecoveryEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedSelfPotionEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 一转血道蛊虫注册表。
 */
public final class TierOneXueDaoRegistry {

    private static final int BUFF_TICKS_SHORT = 120;
    private static final int BUFF_TICKS_MEDIUM = 140;
    private static final int BUFF_TICKS_LONG = 160;

    private TierOneXueDaoRegistry() {}

    public static void registerAll() {
        registerXieYanGu();
        registerTieXueGu();
        registerXieFeiGu();
        registerXieDiGu();
        registerXueShuGu();
        registerXieWangGu();
    }

    private static void registerXieYanGu() {
        final String passive = "guzhenren:xie_yan_gu_passive_blood_eye";
        final String active = "guzhenren:xie_yan_gu_active_blood_gaze";

        GuEffectRegistry.register(
            new XueDaoSustainedSelfPotionEffect(
                passive,
                List.of(
                    new XueDaoSustainedSelfPotionEffect.EffectSpec(
                        MobEffects.NIGHT_VISION,
                        "night_vision_duration_ticks",
                        BUFF_TICKS_MEDIUM,
                        "night_vision_amplifier",
                        0
                    )
                )
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

    private static void registerTieXueGu() {
        final String passive = "guzhenren:tiexuegu_passive_iron_blood_guard";
        final String active = "guzhenren:tiexuegu_active_iron_blood_stance";

        GuEffectRegistry.register(
            new XueDaoHurtProcReductionEffect(
                passive,
                XueDaoCooldownKeys.proc(passive)
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveSelfBuffEffect(
                active,
                XueDaoCooldownKeys.active(active),
                List.of(
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        BUFF_TICKS_SHORT,
                        "resistance_amplifier",
                        0
                    ),
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slowness_duration_ticks",
                        BUFF_TICKS_SHORT,
                        "slowness_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerXieFeiGu() {
        final String passive = "guzhenren:xie_fei_gu_passive_blood_lung";
        final String active = "guzhenren:xie_fei_gu_active_deep_breath";

        GuEffectRegistry.register(
            new XueDaoSustainedSelfPotionEffect(
                passive,
                List.of(
                    new XueDaoSustainedSelfPotionEffect.EffectSpec(
                        MobEffects.WATER_BREATHING,
                        "water_breathing_duration_ticks",
                        BUFF_TICKS_MEDIUM,
                        "water_breathing_amplifier",
                        0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveSelfBuffEffect(
                active,
                XueDaoCooldownKeys.active(active),
                List.of(
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DOLPHINS_GRACE,
                        "dolphins_grace_duration_ticks",
                        BUFF_TICKS_LONG,
                        "dolphins_grace_amplifier",
                        0
                    ),
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        BUFF_TICKS_LONG,
                        "speed_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerXieDiGu() {
        final String passive = "guzhenren:xie_di_gu_passive_blood_drop_proc";
        final String active = "guzhenren:xie_di_gu_active_blood_shot";

        GuEffectRegistry.register(
            new XueDaoAttackProcBonusDamageEffect(
                passive,
                XueDaoCooldownKeys.proc(passive),
                MobEffects.POISON
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveTargetStrikeEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.POISON
            )
        );
    }

    private static void registerXueShuGu() {
        final String passive = "guzhenren:xueshugu_passive_blood_script_sustain";
        final String active = "guzhenren:xueshugu_active_blood_script_recover";

        GuEffectRegistry.register(new XueDaoSustainedResourceRegenEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfRecoveryEffect(
                active,
                XueDaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerXieWangGu() {
        final String passive = "guzhenren:xie_wang_gu_passive_blood_net_proc";
        final String active = "guzhenren:xie_wang_gu_active_blood_net";

        GuEffectRegistry.register(
            new XueDaoAttackProcBonusDamageEffect(
                passive,
                XueDaoCooldownKeys.proc(passive),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveAoEBurstEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }
}
