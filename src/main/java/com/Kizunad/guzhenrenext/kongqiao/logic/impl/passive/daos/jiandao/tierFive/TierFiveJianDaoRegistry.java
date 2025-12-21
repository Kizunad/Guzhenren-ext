package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveMultiBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSelfHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSuiRenAmplifyEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSwordDomainEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSwordLightPierceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoSwordHeartPassiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoSustainedMaxCapModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierFiveJianDaoRegistry {

    private TierFiveJianDaoRegistry() {}

    public static void registerAll() {
        registerJianSuoGuWuZhuan();
        registerJianFengGu5();
        registerJianQiaoGu();
        registerYunJianQingLian();
        registerJianJiaoGuWuZhuan();
        registerJianYuGu();
        registerJianYuYiGu();
        registerSuiRenGu();
        registerHuanJianYuGu();
        registerJianXinGu();
        registerLieJianGu();
    }

    private static void registerJianSuoGuWuZhuan() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jiansuoguwuzhuan_passive_shuttle_mastery",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveMultiBlinkEffect(
                "guzhenren:jiansuoguwuzhuan_active_chain_shuttle",
                JianDaoCooldownKeys.active(
                    "guzhenren:jiansuoguwuzhuan_active_chain_shuttle"
                )
            )
        );
    }

    private static void registerJianFengGu5() {
        final String passiveId = "guzhenren:jian_feng_gu_5_passive_piercing_edge";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveTargetStrikeEffect(
                "guzhenren:jian_feng_gu_5_active_piercing_strike",
                JianDaoCooldownKeys.active(
                    "guzhenren:jian_feng_gu_5_active_piercing_strike"
                ),
                List.of()
            )
        );
    }

    private static void registerJianQiaoGu() {
        GuEffectRegistry.register(
            new JianDaoSustainedMaxCapModifierEffect(
                "guzhenren:jian_qiao_gu_passive_sheathe_capacity"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfHealEffect(
                "guzhenren:jian_qiao_gu_active_sheathe_refill",
                JianDaoCooldownKeys.active("guzhenren:jian_qiao_gu_active_sheathe_refill")
            )
        );
    }

    private static void registerYunJianQingLian() {
        GuEffectRegistry.register(
            new JianDaoSustainedResourceRegenEffect(
                "guzhenren:yun_jian_qing_lian_passive_lotus_rest"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfHealEffect(
                "guzhenren:yun_jian_qing_lian_active_lotus_bless",
                JianDaoCooldownKeys.active(
                    "guzhenren:yun_jian_qing_lian_active_lotus_bless"
                )
            )
        );
    }

    private static void registerJianJiaoGuWuZhuan() {
        final String passiveId = "guzhenren:jianjiaoguwuzhuan_passive_snake_bind_proc";
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
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:jianjiaoguwuzhuan_active_snake_twist",
                JianDaoCooldownKeys.active(
                    "guzhenren:jianjiaoguwuzhuan_active_snake_twist"
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

    private static void registerJianYuGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jianyugu_passive_domain_anchor",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSwordDomainEffect(
                "guzhenren:jianyugu_active_sword_domain",
                JianDaoCooldownKeys.active("guzhenren:jianyugu_active_sword_domain"),
                List.of(
                    new JianDaoActiveSwordDomainEffect.DebuffSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new JianDaoActiveSwordDomainEffect.DebuffSpec(
                        MobEffects.WEAKNESS,
                        "weak_duration_ticks",
                        0,
                        "weak_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianYuYiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jian_yu_yi_gu_passive_feather_robe_stride",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:jian_yu_yi_gu_active_feather_robe_flight",
                JianDaoCooldownKeys.active(
                    "guzhenren:jian_yu_yi_gu_active_feather_robe_flight"
                ),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.SLOW_FALLING,
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

    private static void registerSuiRenGu() {
        GuEffectRegistry.register(
            new JianDaoHurtProcReductionEffect(
                "guzhenren:sui_ren_gu_passive_fragile_guard",
                JianDaoCooldownKeys.proc("guzhenren:sui_ren_gu_passive_fragile_guard")
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSuiRenAmplifyEffect(
                "guzhenren:sui_ren_gu_active_sunder_amplify",
                JianDaoCooldownKeys.active(
                    "guzhenren:sui_ren_gu_active_sunder_amplify"
                )
            )
        );
    }

    private static void registerHuanJianYuGu() {
        final String passiveId = "guzhenren:huanjianyugu_passive_illusory_feather_proc";
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
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:huanjianyugu_active_illusory_feather_burst",
                JianDaoCooldownKeys.active(
                    "guzhenren:huanjianyugu_active_illusory_feather_burst"
                ),
                List.of()
            )
        );
    }

    private static void registerJianXinGu() {
        GuEffectRegistry.register(
            new JianDaoSwordHeartPassiveEffect(
                "guzhenren:jian_xin_gu_passive_sword_heart"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:jian_xin_gu_active_sword_heart_focus",
                JianDaoCooldownKeys.active(
                    "guzhenren:jian_xin_gu_active_sword_heart_focus"
                ),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "boost_duration_ticks",
                        0,
                        "boost_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerLieJianGu() {
        final String passiveId = "guzhenren:liejiangu_passive_fracture_proc";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSwordLightPierceEffect(
                "guzhenren:liejiangu_active_sword_light_pierce",
                JianDaoCooldownKeys.active(
                    "guzhenren:liejiangu_active_sword_light_pierce"
                )
            )
        );
    }
}

