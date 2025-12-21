package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedRegenEffect;
import net.minecraft.world.effect.MobEffects;

public final class TierTwoHuoDaoRegistry {
    private TierTwoHuoDaoRegistry() {}

    public static void registerAll() {
        // 双窍火炉蛊：持续温养恢复 + 主动喷焰震退
        GuEffectRegistry.register(
            new HuoDaoSustainedRegenEffect(
                "guzhenren:shuang_qiao_huo_lu_gu_passive_double_furnace_regen"
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveAoEBurstEffect(
                "guzhenren:shuang_qiao_huo_lu_gu_active_furnace_blast",
                cooldownKey("guzhenren:shuang_qiao_huo_lu_gu_active_furnace_blast"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

