package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActivePlasmaFieldEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoHurtProcRetaliationEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 二转雷道。
 */
public final class TierTwoLeiDaoRegistry {

    private TierTwoLeiDaoRegistry() {}

    public static void registerAll() {
        registerNet();
        registerPlasma();
    }

    private static void registerNet() {
        // 电网蛊：缚电回击（受击反制）+ 电网缚束（指向控制）
        GuEffectRegistry.register(
            new LeiDaoHurtProcRetaliationEffect(
                "guzhenren:dianwanggu_passive_snare_counter",
                cooldownKey("guzhenren:dianwanggu_passive_snare_counter"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveTargetStrikeEffect(
                "guzhenren:dianwanggu_active_net_bind",
                cooldownKey("guzhenren:dianwanggu_active_net_bind"),
                List.of(
                    new LeiDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    ),
                    new LeiDaoActiveTargetStrikeEffect.EffectSpec(
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

    private static void registerPlasma() {
        // 电浆蛊：电浆麻痹（攻触发）+ 电浆场（覆盖地面）
        GuEffectRegistry.register(
            new LeiDaoAttackProcDebuffEffect(
                "guzhenren:dianjianggu_passive_plasma_sting",
                List.of(MobEffects.DIG_SLOWDOWN, MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActivePlasmaFieldEffect(
                "guzhenren:dianjianggu_active_plasma_field",
                cooldownKey("guzhenren:dianjianggu_active_plasma_field")
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

