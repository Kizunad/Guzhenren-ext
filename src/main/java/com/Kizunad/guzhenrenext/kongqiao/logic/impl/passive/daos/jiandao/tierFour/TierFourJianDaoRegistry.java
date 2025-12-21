package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveMarkTargetEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoMarkedTargetBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoSustainedMaxCapModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierFourJianDaoRegistry {

    private TierFourJianDaoRegistry() {}

    public static void registerAll() {
        registerJianFengGu();
        registerYuJianGu();
        registerJianRenGu();
        registerJianJiaoGu();
        registerJianSuoGuSiZhuan();
        registerJianLuGu();
        registerDuoChongJianYing();
        registerJianYinGu();
        registerJianShangGu();
        registerJianDangGu();
        registerJianQiGu();
        registerYuZangJianGuanGu();
    }

    private static void registerJianFengGu() {
        final String passiveId = "guzhenren:jianfenggu_passive_piercing_edge";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveTargetStrikeEffect(
                "guzhenren:jianfenggu_active_piercing_strike",
                JianDaoCooldownKeys.active("guzhenren:jianfenggu_active_piercing_strike"),
                List.of()
            )
        );
    }

    private static void registerYuJianGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yujianggu_passive_sword_rider_stride",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveBlinkEffect(
                "guzhenren:yujianggu_active_sword_rider_shift",
                JianDaoCooldownKeys.active("guzhenren:yujianggu_active_sword_rider_shift"),
                List.of(
                    new JianDaoActiveBlinkEffect.AfterEffectSpec(
                        MobEffects.SLOW_FALLING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianRenGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jianrengu_passive_blade_mastery",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:jianrengu_active_blade_fan",
                JianDaoCooldownKeys.active("guzhenren:jianrengu_active_blade_fan"),
                List.of()
            )
        );
    }

    private static void registerJianJiaoGu() {
        final String passiveId = "guzhenren:jianjiaogu_passive_snake_bind_proc";
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
                "guzhenren:jianjiaogu_active_snake_twist",
                JianDaoCooldownKeys.active("guzhenren:jianjiaogu_active_snake_twist"),
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

    private static void registerJianSuoGuSiZhuan() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jiansuogusizhuan_passive_shuttle_stride",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveBlinkEffect(
                "guzhenren:jiansuogusizhuan_active_shuttle_chain",
                JianDaoCooldownKeys.active(
                    "guzhenren:jiansuogusizhuan_active_shuttle_chain"
                ),
                List.of(
                    new JianDaoActiveBlinkEffect.AfterEffectSpec(
                        MobEffects.ABSORPTION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianLuGu() {
        GuEffectRegistry.register(
            new JianDaoSustainedMaxCapModifierEffect(
                "guzhenren:jianlugu_passive_sword_road_capacity"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:jianlugu_active_sword_road_step",
                JianDaoCooldownKeys.active("guzhenren:jianlugu_active_sword_road_step"),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resist_duration_ticks",
                        0,
                        "resist_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerDuoChongJianYing() {
        final String passiveId = "guzhenren:duochongjianying_passive_multi_shadow_proc";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:duochongjianying_active_multi_shadow_burst",
                JianDaoCooldownKeys.active(
                    "guzhenren:duochongjianying_active_multi_shadow_burst"
                ),
                List.of()
            )
        );
    }

    private static void registerJianYinGu() {
        final String passiveId = "guzhenren:jianyingu_passive_marked_hunt";
        GuEffectRegistry.register(
            new JianDaoMarkedTargetBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveMarkTargetEffect(
                "guzhenren:jianyingu_active_mark_target",
                JianDaoCooldownKeys.active("guzhenren:jianyingu_active_mark_target")
            )
        );
    }

    private static void registerJianShangGu() {
        GuEffectRegistry.register(
            new JianDaoSustainedMaxCapModifierEffect(
                "guzhenren:jianshanggu_passive_soul_wound_capacity"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:jianshanggu_active_wound_for_power",
                JianDaoCooldownKeys.active(
                    "guzhenren:jianshanggu_active_wound_for_power"
                ),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerJianDangGu() {
        final String passiveId = "guzhenren:jiandanggu_passive_sword_wave_guard";
        GuEffectRegistry.register(
            new JianDaoHurtProcReductionEffect(passiveId, JianDaoCooldownKeys.proc(passiveId))
        );
        GuEffectRegistry.register(
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:jiandanggu_active_sword_wave_control",
                JianDaoCooldownKeys.active(
                    "guzhenren:jiandanggu_active_sword_wave_control"
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

    private static void registerJianQiGu() {
        final String passiveId = "guzhenren:jianqigu_passive_sword_qi_proc";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveTargetStrikeEffect(
                "guzhenren:jianqigu_active_sword_qi_cut",
                JianDaoCooldownKeys.active("guzhenren:jianqigu_active_sword_qi_cut"),
                List.of()
            )
        );
    }

    private static void registerYuZangJianGuanGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yuzangjianguangu_passive_sword_coffin_guard",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor_toughness"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:yuzangjianguangu_active_coffin_burst",
                JianDaoCooldownKeys.active(
                    "guzhenren:yuzangjianguangu_active_coffin_burst"
                ),
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
}

