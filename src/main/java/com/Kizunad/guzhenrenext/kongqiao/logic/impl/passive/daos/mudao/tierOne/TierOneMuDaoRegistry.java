package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 一转木道。
 */
public final class TierOneMuDaoRegistry {
    private TierOneMuDaoRegistry() {}

    public static void registerAll() {
        // 生机叶：持续生机 + 点化自愈
        GuEffectRegistry.register(
            new MuDaoSustainedRegenEffect(
                "guzhenren:sheng_ji_xie_passive_leaf_breath"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveSelfBuffEffect(
                "guzhenren:sheng_ji_xie_active_life_sprout",
                cooldownKey("guzhenren:sheng_ji_xie_active_life_sprout"),
                List.of(
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 镰刀蛊：割取生命（普通伤害为主）+ 毒素附伤
        GuEffectRegistry.register(
            new MuDaoAttackProcDebuffEffect(
                "guzhenren:liandaogu_passive_reaping_edge",
                MobEffects.POISON
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveTargetNukeEffect(
                "guzhenren:liandaogu_active_crescent_reap",
                cooldownKey("guzhenren:liandaogu_active_crescent_reap"),
                List.of(
                    new MuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

