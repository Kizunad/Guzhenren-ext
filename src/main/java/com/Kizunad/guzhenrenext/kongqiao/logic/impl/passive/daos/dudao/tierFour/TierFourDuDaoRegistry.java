package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoSustainedCleanseEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 四转毒道。
 */
public final class TierFourDuDaoRegistry {

    private static final int BUFF_TICKS = 100;

    private TierFourDuDaoRegistry() {}

    public static void registerAll() {
        registerTunDuWaGu();
    }

    private static void registerTunDuWaGu() {
        // 吞毒蛙蛊：吞毒解厄（持续净化）+ 毒尽回生（主动净化/恢复）
        GuEffectRegistry.register(
            new DuDaoSustainedCleanseEffect(
                "guzhenren:tun_du_wa_gu_passive_swallow_toxin"
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveSelfBuffEffect(
                "guzhenren:tun_du_wa_gu_active_detox_recover",
                cooldownKey("guzhenren:tun_du_wa_gu_active_detox_recover"),
                List.of(
                    new DuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new DuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        BUFF_TICKS,
                        "buff_amplifier",
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

