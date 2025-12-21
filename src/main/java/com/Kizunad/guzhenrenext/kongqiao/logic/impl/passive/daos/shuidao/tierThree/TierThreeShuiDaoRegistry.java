package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAreaFieldEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveRevealPulseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoAttackProcLifestealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoHurtProcRetaliationEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedSelfBuffEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 三转水道。
 */
public final class TierThreeShuiDaoRegistry {

    private static final int LONG_BUFF_TICKS = 220;
    private static final int SHORT_BUFF_TICKS = 80;
    private static final int REGEN_BUFF_TICKS = 100;
    private static final int SELF_REGEN_TICKS = 120;
    private static final int ARMOR_BUFF_TICKS = 140;

    private TierThreeShuiDaoRegistry() {}

    public static void registerAll() {
        registerShuiJiGu();
        registerLuoXuanShuiJianGu();
        registerChunYuGu();
        registerShuiShenGu();
        registerShuiLaoGu();
        registerShuiLianGu();
        registerHongShuiGu();
        registerShuiLongGu();
        registerXueShuiGu();
        registerShuiShengHuaGu();
        registerShuiBiGu();
        registerShuiJiaGu();
    }

    private static void registerShuiJiGu() {
        // 水迹蛊：水迹识途（持续维持）+ 水迹标记（显形）
        GuEffectRegistry.register(
            new ShuiDaoSustainedSelfBuffEffect(
                "guzhenren:shui_ji_gu_passive_trace_sense",
                List.of(
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.NIGHT_VISION,
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
                "guzhenren:shui_ji_gu_active_trace_mark",
                cooldownKey("guzhenren:shui_ji_gu_active_trace_mark")
            )
        );
    }

    private static void registerLuoXuanShuiJianGu() {
        // 螺旋水剑蛊：旋水剑痕（攻触发）+ 旋水穿斩（指向）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:luo_xuan_shui_jian_gu_passive_torrent_edge",
                List.of(MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveTargetStrikeEffect(
                "guzhenren:luo_xuan_shui_jian_gu_active_torrent_edge",
                cooldownKey("guzhenren:luo_xuan_shui_jian_gu_active_torrent_edge"),
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

    private static void registerChunYuGu() {
        // 春雨蛊：春雨润泽（持续群体回复）+ 春雨回春（范围治疗）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:chun_yu_gu_passive_spring_rain"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAreaHealEffect(
                "guzhenren:chun_yu_gu_active_spring_rain",
                cooldownKey("guzhenren:chun_yu_gu_active_spring_rain"),
                List.of(
                    new ShuiDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        REGEN_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiShenGu() {
        // 水神蛊：河神赐福（持续群体回复）+ 河神回潮（范围治疗）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:shuishengu_passive_river_blessing"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAreaHealEffect(
                "guzhenren:shuishengu_active_river_blessing",
                cooldownKey("guzhenren:shuishengu_active_river_blessing"),
                List.of(
                    new ShuiDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoActiveAreaHealEffect.EffectSpec(
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

    private static void registerShuiLaoGu() {
        // 水牢蛊：水牢牵制（攻触发）+ 水牢封困（领域）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:shui_lao_gu_passive_prison_hook",
                List.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAreaFieldEffect(
                "guzhenren:shui_lao_gu_active_water_prison",
                cooldownKey("guzhenren:shui_lao_gu_active_water_prison")
            )
        );
    }

    private static void registerShuiLianGu() {
        // 水链蛊：水链束缚（攻触发）+ 水链缠锁（指向）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:shui_lian_gu_passive_chain_bind",
                List.of(MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveTargetStrikeEffect(
                "guzhenren:shui_lian_gu_active_chain_bind",
                cooldownKey("guzhenren:shui_lian_gu_active_chain_bind"),
                List.of(
                    new ShuiDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        1
                    ),
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

    private static void registerHongShuiGu() {
        // 洪水蛊：洪潮反冲（受击反制）+ 洪潮冲击（范围潮击）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcRetaliationEffect(
                "guzhenren:hong_shui_gu_passive_flood_backlash",
                cooldownKey("guzhenren:hong_shui_gu_passive_flood_backlash"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAoEBurstEffect(
                "guzhenren:hong_shui_gu_active_flood_burst",
                cooldownKey("guzhenren:hong_shui_gu_active_flood_burst"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerShuiLongGu() {
        // 水龙蛊：水龙余威（攻触发）+ 水龙冲击（指向+溅射）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:shui_long_gu_passive_dragon_fang",
                List.of(MobEffects.WEAKNESS)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveTargetStrikeEffect(
                "guzhenren:shui_long_gu_active_dragon_strike",
                cooldownKey("guzhenren:shui_long_gu_active_dragon_strike"),
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

    private static void registerXueShuiGu() {
        // 血水蛊：血潮回生（吸血）+ 血潮护体（自 buff）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcLifestealEffect(
                "guzhenren:xue_shui_gu_passive_blood_tide",
                List.of(MobEffects.WEAKNESS)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:xue_shui_gu_active_blood_tide",
                cooldownKey("guzhenren:xue_shui_gu_active_blood_tide"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        SELF_REGEN_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiShengHuaGu() {
        // 水生花蛊：水生花露（持续群体回复）+ 水生花开（范围治疗）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:shui_sheng_hua_gu_passive_lotus_bloom"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAreaHealEffect(
                "guzhenren:shui_sheng_hua_gu_active_lotus_bloom",
                cooldownKey("guzhenren:shui_sheng_hua_gu_active_lotus_bloom"),
                List.of(
                    new ShuiDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        ARMOR_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiBiGu() {
        // 水壁蛊：水壁卸力（受击减伤）+ 水壁护体（自 buff）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcReductionEffect(
                "guzhenren:shui_bi_gu_passive_water_wall",
                cooldownKey("guzhenren:shui_bi_gu_passive_water_wall"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_bi_gu_active_water_wall",
                cooldownKey("guzhenren:shui_bi_gu_active_water_wall"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        ARMOR_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "buff_duration_ticks",
                        ARMOR_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiJiaGu() {
        // 水甲蛊：水甲卸力（受击减伤）+ 水甲凝护（自 buff）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcReductionEffect(
                "guzhenren:shui_jia_gu_passive_water_armor",
                cooldownKey("guzhenren:shui_jia_gu_passive_water_armor"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_jia_gu_active_water_armor",
                cooldownKey("guzhenren:shui_jia_gu_active_water_armor"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        ARMOR_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "buff_duration_ticks",
                        ARMOR_BUFF_TICKS,
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
