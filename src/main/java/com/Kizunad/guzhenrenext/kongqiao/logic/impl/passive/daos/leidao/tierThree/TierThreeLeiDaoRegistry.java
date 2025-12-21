package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveRevealPulseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveWingLeapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoAttackProcKnockbackEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoHurtProcRetaliationEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoSustainedRevealInvisEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoSustainedWingPassiveEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 三转雷道。
 */
public final class TierThreeLeiDaoRegistry {

    private TierThreeLeiDaoRegistry() {}

    public static void registerAll() {
        registerShield();
        registerBomb();
        registerEye();
        registerRoar();
        registerWings();
    }

    private static void registerShield() {
        // 雷盾蛊：护盾回响（受击减伤）+ 半圆护体（主动护体）
        GuEffectRegistry.register(
            new LeiDaoHurtProcReductionEffect(
                "guzhenren:leidungu_passive_thunder_shield",
                cooldownKey("guzhenren:leidungu_passive_thunder_shield"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveSelfBuffEffect(
                "guzhenren:leidungu_active_thunder_guard",
                cooldownKey("guzhenren:leidungu_active_thunder_guard"),
                List.of(
                    new LeiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new LeiDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerBomb() {
        // 炸雷蛊：电爆反制（受击电击）+ 雷球轰击（指向强打）
        GuEffectRegistry.register(
            new LeiDaoHurtProcRetaliationEffect(
                "guzhenren:zha_lei_gu_passive_bomb_counter",
                cooldownKey("guzhenren:zha_lei_gu_passive_bomb_counter"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveTargetStrikeEffect(
                "guzhenren:zha_lei_gu_active_thunder_orb",
                cooldownKey("guzhenren:zha_lei_gu_active_thunder_orb"),
                List.of(
                    new LeiDaoActiveTargetStrikeEffect.EffectSpec(
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

    private static void registerEye() {
        // 电眼蛊：巡照显形（持续）+ 电眼一瞬（主动脉冲显形）
        GuEffectRegistry.register(
            new LeiDaoSustainedRevealInvisEffect(
                "guzhenren:dianyangu_passive_storm_eye"
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveRevealPulseEffect(
                "guzhenren:dianyangu_active_storm_eye_pulse",
                cooldownKey("guzhenren:dianyangu_active_storm_eye_pulse")
            )
        );
    }

    private static void registerRoar() {
        // 雷哮蛊：雷哮震荡（攻触发击退）+ 雷哮震鸣（范围震荡）
        GuEffectRegistry.register(
            new LeiDaoAttackProcKnockbackEffect(
                "guzhenren:leixiaogu_passive_thunder_roar",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveAoEBurstEffect(
                "guzhenren:leixiaogu_active_roar_burst",
                cooldownKey("guzhenren:leixiaogu_active_roar_burst"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerWings() {
        // 雷翼蛊：雷翼余势（持续机动）+ 雷翼飞跃（主动机动爆发）
        GuEffectRegistry.register(
            new LeiDaoSustainedWingPassiveEffect(
                "guzhenren:lei_yi_gu_passive_wing_sustain"
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveWingLeapEffect(
                "guzhenren:lei_yi_gu_active_wing_leap",
                cooldownKey("guzhenren:lei_yi_gu_active_wing_leap")
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
