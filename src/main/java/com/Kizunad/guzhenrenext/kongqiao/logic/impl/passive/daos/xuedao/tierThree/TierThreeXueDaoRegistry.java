package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.common.XueDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoLowHealthFuryEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedMaxCapModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common.XueDaoSustainedSelfPotionEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 三转血道蛊虫注册表。
 */
public final class TierThreeXueDaoRegistry {

    private static final int PASSIVE_REFRESH_TICKS = 60;
    private static final int BUFF_TICKS_140 = 140;
    private static final int BUFF_TICKS_180 = 180;
    private static final int BUFF_TICKS_200 = 200;
    private static final int BUFF_TICKS_220 = 220;

    private TierThreeXueDaoRegistry() {}

    public static void registerAll() {
        registerXieKuangZhenFengGu();
        registerXueYinGu();
        registerXieZouGu();
        registerXueZhanGu();
        registerPenXieGu();
        registerXueRenGu();
        registerLiZhanXueQiGu();
        registerXieLuoGu();
    }

    private static void registerXieKuangZhenFengGu() {
        final String passive = "guzhenren:xie_kuang_zhen_feng_gu_passive_frenzy_proc";
        final String active = "guzhenren:xie_kuang_zhen_feng_gu_active_frenzy_sting";

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

    private static void registerXueYinGu() {
        final String passive = "guzhenren:xueyingu_passive_blood_seal_caps";
        final String active = "guzhenren:xueyingu_active_blood_seal_guard";

        GuEffectRegistry.register(new XueDaoSustainedMaxCapModifierEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfBuffEffect(
                active,
                XueDaoCooldownKeys.active(active),
                List.of(
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        BUFF_TICKS_140,
                        "absorption_amplifier",
                        0
                    ),
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        BUFF_TICKS_140,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerXieZouGu() {
        final String passive = "guzhenren:xie_zou_gu_passive_blood_walk_sustain";
        final String active = "guzhenren:xie_zou_gu_active_blood_walk_dash";

        GuEffectRegistry.register(
            new XueDaoSustainedSelfPotionEffect(
                passive,
                List.of(
                    new XueDaoSustainedSelfPotionEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        PASSIVE_REFRESH_TICKS,
                        "speed_amplifier",
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
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        BUFF_TICKS_200,
                        "speed_amplifier",
                        1
                    ),
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.JUMP,
                        "jump_duration_ticks",
                        BUFF_TICKS_200,
                        "jump_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerXueZhanGu() {
        final String passive = "guzhenren:xuezhangu_passive_blood_battle_fury";
        final String active = "guzhenren:xuezhangu_active_blood_battle_burst";

        GuEffectRegistry.register(new XueDaoLowHealthFuryEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfBuffEffect(
                active,
                XueDaoCooldownKeys.active(active),
                List.of(
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        BUFF_TICKS_180,
                        "strength_amplifier",
                        1
                    ),
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        BUFF_TICKS_180,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerPenXieGu() {
        final String passive = "guzhenren:pen_xie_gu_passive_spray_blood_sustain";
        final String active = "guzhenren:pen_xie_gu_active_spray_blood_dash";

        GuEffectRegistry.register(
            new XueDaoSustainedSelfPotionEffect(
                passive,
                List.of(
                    new XueDaoSustainedSelfPotionEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        PASSIVE_REFRESH_TICKS,
                        "speed_amplifier",
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
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        BUFF_TICKS_220,
                        "speed_amplifier",
                        2
                    )
                )
            )
        );
    }

    private static void registerXueRenGu() {
        final String passive = "guzhenren:xue_ren_gu_passive_blood_blade_proc";
        final String active = "guzhenren:xue_ren_gu_active_blood_blade_sweep";

        GuEffectRegistry.register(
            new XueDaoAttackProcBonusDamageEffect(
                passive,
                XueDaoCooldownKeys.proc(passive),
                MobEffects.POISON
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

    private static void registerLiZhanXueQiGu() {
        final String passive = "guzhenren:li_zhan_xue_qi_gu_passive_battle_caps";
        final String active = "guzhenren:li_zhan_xue_qi_gu_active_battle_body";

        GuEffectRegistry.register(new XueDaoSustainedMaxCapModifierEffect(passive));
        GuEffectRegistry.register(
            new XueDaoActiveSelfBuffEffect(
                active,
                XueDaoCooldownKeys.active(active),
                List.of(
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        BUFF_TICKS_200,
                        "strength_amplifier",
                        1
                    ),
                    new XueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        BUFF_TICKS_200,
                        "absorption_amplifier",
                        1
                    )
                )
            )
        );
    }

    private static void registerXieLuoGu() {
        final String passive = "guzhenren:xie_luo_gu_passive_moon_blood_proc";
        final String active = "guzhenren:xie_luo_gu_active_moon_blood_strike";

        GuEffectRegistry.register(
            new XueDaoAttackProcBonusDamageEffect(
                passive,
                XueDaoCooldownKeys.proc(passive),
                MobEffects.GLOWING
            )
        );
        GuEffectRegistry.register(
            new XueDaoActiveTargetStrikeEffect(
                active,
                XueDaoCooldownKeys.active(active),
                MobEffects.GLOWING
            )
        );
    }
}
