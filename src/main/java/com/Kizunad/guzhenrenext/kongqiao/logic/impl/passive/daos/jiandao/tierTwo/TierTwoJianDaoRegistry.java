package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoAttackProcBonusDamageEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierTwoJianDaoRegistry {

    private TierTwoJianDaoRegistry() {}

    public static void registerAll() {
        registerJinWenJianXiaGu();
        registerJianJiGu();
        registerJianZhiGu();
    }

    private static void registerJinWenJianXiaGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jinweijianxiagu_passive_golden_box_stride",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveBlinkEffect(
                "guzhenren:jinweijianxiagu_active_golden_box_shift",
                JianDaoCooldownKeys.active(
                    "guzhenren:jinweijianxiagu_active_golden_box_shift"
                ),
                List.of(
                    new JianDaoActiveBlinkEffect.AfterEffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianJiGu() {
        final String passiveId = "guzhenren:jianjigu_passive_trace_proc";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of()
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveTargetStrikeEffect(
                "guzhenren:jianjigu_active_trace_puncture",
                JianDaoCooldownKeys.active("guzhenren:jianjigu_active_trace_puncture"),
                List.of(
                    new JianDaoActiveTargetStrikeEffect.EffectSpec(
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

    private static void registerJianZhiGu() {
        final String passiveId = "guzhenren:jianzhigu_passive_finger_edge";
        GuEffectRegistry.register(
            new JianDaoAttackProcBonusDamageEffect(
                passiveId,
                JianDaoCooldownKeys.proc(passiveId),
                List.of(
                    new JianDaoAttackProcBonusDamageEffect.EffectSpec(
                        MobEffects.WEAKNESS,
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
                "guzhenren:jianzhigu_active_finger_shot",
                JianDaoCooldownKeys.active("guzhenren:jianzhigu_active_finger_shot"),
                List.of()
            )
        );
    }
}

