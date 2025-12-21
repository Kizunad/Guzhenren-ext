package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedSelfBuffEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 二转水道。
 */
public final class TierTwoShuiDaoRegistry {

    private static final int SHORT_BUFF_TICKS = 100;
    private static final int MEDIUM_BUFF_TICKS = 120;
    private static final int LONG_BUFF_TICKS = 220;

    private TierTwoShuiDaoRegistry() {}

    public static void registerAll() {
        registerShuiWanGu();
        registerLingXianGu();
        registerShuiZhaoGu();
        registerShuiKeGu();
        registerShuiZuanGu();
        registerShuiTiGu();
    }

    private static void registerShuiWanGu() {
        // 水湾蛊：温润滋养（持续回复）+ 清润水丸（净化/恢复）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:shui_wan_gu_passive_moist_nourish"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_wan_gu_active_clear_pill",
                cooldownKey("guzhenren:shui_wan_gu_active_clear_pill"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerLingXianGu() {
        // 灵泉蛊：泉涌润泽（群体回复）+ 泉涌冲击（指向）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:ling_xian_gu_passive_spring_aura"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveTargetStrikeEffect(
                "guzhenren:ling_xian_gu_active_spring_pierce",
                cooldownKey("guzhenren:ling_xian_gu_active_spring_pierce"),
                List.of(
                    new ShuiDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiZhaoGu() {
        // 水罩蛊：水罩卸力（受击减伤）+ 波罩护体（自 buff）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcReductionEffect(
                "guzhenren:shui_zhao_gu_passive_water_shield",
                cooldownKey("guzhenren:shui_zhao_gu_passive_water_shield"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_zhao_gu_active_wave_guard",
                cooldownKey("guzhenren:shui_zhao_gu_active_wave_guard"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiKeGu() {
        // 水壳蛊：水壳护身（持续维持）+ 水壳迸发（自 buff）
        GuEffectRegistry.register(
            new ShuiDaoSustainedSelfBuffEffect(
                "guzhenren:shui_ke_gu_passive_water_shell",
                List.of(
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.WATER_BREATHING,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.CONDUIT_POWER,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_ke_gu_active_shell_burst",
                cooldownKey("guzhenren:shui_ke_gu_active_shell_burst"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiZuanGu() {
        // 水钻蛊：螺旋水钻（攻触发）+ 水钻穿刺（指向）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:shui_zuan_gu_passive_spiral_drill",
                List.of(MobEffects.DIG_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveTargetStrikeEffect(
                "guzhenren:shui_zuan_gu_active_spiral_pierce",
                cooldownKey("guzhenren:shui_zuan_gu_active_spiral_pierce"),
                List.of(
                    new ShuiDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.DIG_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiTiGu() {
        // 水体蛊：水体卸力（受击减伤）+ 水体化形（自 buff/净化）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcReductionEffect(
                "guzhenren:shui_ti_gu_passive_liquid_body",
                cooldownKey("guzhenren:shui_ti_gu_passive_liquid_body"),
                MobEffects.FIRE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_ti_gu_active_liquid_shift",
                cooldownKey("guzhenren:shui_ti_gu_active_liquid_shift"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DOLPHINS_GRACE,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
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
