package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.daodao.common.DaoDaoActiveBladeLightPierceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.daodao.common.DaoDaoActiveConeSlashEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common.DaoDaoSustainedDamageReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common.DaoDaoSustainedMeleeBonusRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 三转刀道：拔刀斩/刀光。
 */
public final class TierThreeDaoDaoRegistry {

    private static final int DEFAULT_EFFECT_TICKS = 60;

    private TierThreeDaoDaoRegistry() {}

    public static void registerAll() {
        registerBaDaoZhanGu();
        registerDaoGuangGu();
    }

    private static void registerBaDaoZhanGu() {
        // 拔刀斩蛊：持续架势减伤 + 扇形拔刀斩
        GuEffectRegistry.register(
            new DaoDaoSustainedDamageReductionEffect(
                "guzhenren:ba_dao_zhan_gu_passive_draw_stance"
            )
        );
        GuEffectRegistry.register(
            new DaoDaoActiveConeSlashEffect(
                "guzhenren:ba_dao_zhan_gu_active_draw_slash",
                cooldownKey("guzhenren:ba_dao_zhan_gu_active_draw_slash"),
                List.of(
                    new DaoDaoActiveConeSlashEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        DEFAULT_EFFECT_TICKS,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerDaoGuangGu() {
        // 刀光蛊：持续维持获得近战附伤+资源小恢复 + 刀光穿行（直线斩击）
        GuEffectRegistry.register(
            new DaoDaoSustainedMeleeBonusRegenEffect(
                "guzhenren:dao_guang_gu_passive_blade_light_focus"
            )
        );
        GuEffectRegistry.register(
            new DaoDaoActiveBladeLightPierceEffect(
                "guzhenren:dao_guang_gu_active_blade_light_pierce",
                cooldownKey("guzhenren:dao_guang_gu_active_blade_light_pierce"),
                List.of(
                    new DaoDaoActiveBladeLightPierceEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        DEFAULT_EFFECT_TICKS,
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

