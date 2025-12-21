package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveRevealPulseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.common.DaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedSelfBuffEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 一转水道。
 */
public final class TierOneShuiDaoRegistry {

    private static final int LONG_BUFF_TICKS = 220;
    private static final int SHORT_BUFF_TICKS = 80;

    private TierOneShuiDaoRegistry() {}

    public static void registerAll() {
        registerShuiJianGu();
        registerShuiGuangGu();
        registerLiuShuiGu();
    }

    private static void registerShuiJianGu() {
        // 水箭蛊：水箭余劲（攻触发）+ 水箭点射（指向）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:shui_jian_gu_passive_water_arrow",
                List.of(MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveTargetStrikeEffect(
                "guzhenren:shui_jian_gu_active_water_arrow",
                cooldownKey("guzhenren:shui_jian_gu_active_water_arrow"),
                List.of(
                    new ShuiDaoActiveTargetStrikeEffect.EffectSpec(
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

    private static void registerShuiGuangGu() {
        // 水光蛊：水光映体（持续维持）+ 水光照映（显形）
        GuEffectRegistry.register(
            new ShuiDaoSustainedSelfBuffEffect(
                "guzhenren:shui_guang_gu_passive_ripple_light",
                List.of(
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.NIGHT_VISION,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.WATER_BREATHING,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveRevealPulseEffect(
                "guzhenren:shui_guang_gu_active_ripple_glow",
                cooldownKey("guzhenren:shui_guang_gu_active_ripple_glow")
            )
        );
    }

    private static void registerLiuShuiGu() {
        // 流水蛊：涓流回气（持续回复）+ 流水步（自 buff）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:liu_shui_gu_passive_flowing_recovery"
            )
        );
        GuEffectRegistry.register(
            new DaoSustainedResourceRegenEffect(
                "guzhenren:liu_shui_gu_passive_shui_xi_yang_nian",
                DaoHenHelper.DaoType.SHUI_DAO,
                true,
                false,
                false,
                false
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:liu_shui_gu_active_flowing_step",
                cooldownKey("guzhenren:liu_shui_gu_active_flowing_step"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DOLPHINS_GRACE,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
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
