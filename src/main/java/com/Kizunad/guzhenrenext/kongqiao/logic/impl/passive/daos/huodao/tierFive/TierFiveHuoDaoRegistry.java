package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class TierFiveHuoDaoRegistry {
    private TierFiveHuoDaoRegistry() {}

    public static void registerAll() {
        // 紫烟蝉：高转上限强化（念头容量/真元/魂魄抗性）+ 主动紫烟遮天（大范围压制）
        GuEffectRegistry.register(
            new HuoDaoSustainedVariableCapEffect(
                "guzhenren:ziyanchan_passive_purple_smoke_cap",
                List.of(
                    new HuoDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "niantou_capacity_bonus"
                    ),
                    new HuoDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "max_zhenyuan_bonus"
                    ),
                    new HuoDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "max_hunpo_resistance_bonus"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveAoEBurstEffect(
                "guzhenren:ziyanchan_active_purple_smoke_heaven",
                cooldownKey("guzhenren:ziyanchan_active_purple_smoke_heaven"),
                MobEffects.BLINDNESS
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

