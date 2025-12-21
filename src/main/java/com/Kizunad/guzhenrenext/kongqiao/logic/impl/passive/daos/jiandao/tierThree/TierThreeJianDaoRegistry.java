package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSelfHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoSustainedMaxCapModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierThreeJianDaoRegistry {

    private TierThreeJianDaoRegistry() {}

    public static void registerAll() {
        registerJianMaiGu();
        registerJianMuGu();
        registerJianWenGu();
        registerJianYingGu();
        registerQiXingJianXiaGu();
        registerJianZhiGu3();
        registerLiShiGu();
        registerJianLiaoGu();
        registerJianSuoGu();
    }

    private static void registerJianMaiGu() {
        GuEffectRegistry.register(
            new JianDaoSustainedMaxCapModifierEffect(
                "guzhenren:jianmaigu_passive_meridian_capacity"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:jianmaigu_active_meridian_surge",
                JianDaoCooldownKeys.active("guzhenren:jianmaigu_active_meridian_surge"),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianMuGu() {
        final String passiveId = "guzhenren:jianmugu_passive_sword_curtain_guard";
        GuEffectRegistry.register(
            new JianDaoHurtProcReductionEffect(passiveId, JianDaoCooldownKeys.proc(passiveId))
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:jianmugu_active_curtain_barrier",
                JianDaoCooldownKeys.active("guzhenren:jianmugu_active_curtain_barrier"),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianWenGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jianwengu_passive_pattern_armor",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:jianwengu_active_pattern_shock",
                JianDaoCooldownKeys.active("guzhenren:jianwengu_active_pattern_shock"),
                List.of(
                    new JianDaoActiveAoEBurstEffect.DebuffSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianYingGu() {
        final String passiveId = "guzhenren:jian_ying_gu_passive_shadow_proc";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveTargetStrikeEffect(
                "guzhenren:jian_ying_gu_active_shadow_cut",
                JianDaoCooldownKeys.active("guzhenren:jian_ying_gu_active_shadow_cut"),
                List.of(
                    new JianDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerQiXingJianXiaGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:qixingjianxiagu_passive_seven_star_training",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:qixingjianxiagu_active_seven_star_rain",
                JianDaoCooldownKeys.active(
                    "guzhenren:qixingjianxiagu_active_seven_star_rain"
                ),
                List.of(
                    new JianDaoActiveAoEBurstEffect.DebuffSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianZhiGu3() {
        final String passiveId = "guzhenren:jian_zhi_gu_3_passive_finger_edge_proc";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of(
                    new JianDaoAttackProcBonusDamageEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveTargetStrikeEffect(
                "guzhenren:jian_zhi_gu_3_active_finger_pierce",
                JianDaoCooldownKeys.active("guzhenren:jian_zhi_gu_3_active_finger_pierce"),
                List.of()
            )
        );
    }

    private static void registerLiShiGu() {
        final String passiveId = "guzhenren:lishigu_passive_whetstone_sharp";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:lishigu_active_whetstone_edge",
                JianDaoCooldownKeys.active("guzhenren:lishigu_active_whetstone_edge"),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianLiaoGu() {
        GuEffectRegistry.register(
            new JianDaoSustainedResourceRegenEffect(
                "guzhenren:jian_liao_gu_passive_sword_heal_sustain"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfHealEffect(
                "guzhenren:jian_liao_gu_active_sword_heal",
                JianDaoCooldownKeys.active("guzhenren:jian_liao_gu_active_sword_heal")
            )
        );
    }

    private static void registerJianSuoGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jiansuogu_passive_sword_shuttle_stride",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveBlinkEffect(
                "guzhenren:jiansuogu_active_sword_shuttle",
                JianDaoCooldownKeys.active("guzhenren:jiansuogu_active_sword_shuttle"),
                List.of(
                    new JianDaoActiveBlinkEffect.AfterEffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }
}

