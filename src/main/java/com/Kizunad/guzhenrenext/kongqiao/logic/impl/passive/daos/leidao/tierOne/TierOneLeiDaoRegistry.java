package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoSustainedResourceRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 一转雷道。
 */
public final class TierOneLeiDaoRegistry {

    private TierOneLeiDaoRegistry() {}

    public static void registerAll() {
        // 电流蛊：静电缠身（攻触发）+ 电弧点射（指向）
        GuEffectRegistry.register(
            new LeiDaoAttackProcDebuffEffect(
                "guzhenren:dianliugu_passive_static_arc",
                List.of(MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new LeiDaoSustainedResourceRegenEffect(
                "guzhenren:dianliugu_passive_thunder_pool_charge"
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveTargetStrikeEffect(
                "guzhenren:dianliugu_active_arc_burst",
                cooldownKey("guzhenren:dianliugu_active_arc_burst"),
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
