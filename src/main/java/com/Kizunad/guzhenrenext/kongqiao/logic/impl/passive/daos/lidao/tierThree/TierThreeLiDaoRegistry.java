package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转力道。
 */
public final class TierThreeLiDaoRegistry {
    private static final int SIX_SECONDS_TICKS = 20 * 6;

    private TierThreeLiDaoRegistry() {}

    public static void registerAll() {
        registerMobilitySupport();
        registerBodyAndArmor();
        registerBeastForces();
        registerDashAndGrowth();
    }

    private static void registerMobilitySupport() {
        // 赤马军力蛊：持续行军（移速）+ 战吼加速
        GuEffectRegistry.register(
            new LiDaoSustainedMobEffectEffect(
                "guzhenren:chi_ma_jun_li_gu_passive_march",
                MobEffects.MOVEMENT_SPEED,
                SIX_SECONDS_TICKS,
                0
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAllySupportEffect(
                "guzhenren:chi_ma_jun_li_gu_active_war_march",
                cooldownKey("guzhenren:chi_ma_jun_li_gu_active_war_march"),
                List.of(
                    new LiDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new LiDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "boost_duration_ticks",
                        0,
                        "boost_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBodyAndArmor() {
        // 精气蛊：精气回转（持续恢复）+ 体魄提振
        GuEffectRegistry.register(
            new LiDaoSustainedRegenEffect(
                "guzhenren:jingqigu_passive_vital_cycle"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:jingqigu_active_vital_burst",
                cooldownKey("guzhenren:jingqigu_active_vital_burst"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resist_duration_ticks",
                        0,
                        "resist_amplifier",
                        0
                    )
                )
            )
        );

        // 军之力蛊：受击硬扛（减伤）+ 铁壁增幅
        GuEffectRegistry.register(
            new LiDaoHurtProcReductionEffect(
                "guzhenren:jun_zhi_li_gu_passive_ironwall",
                cooldownKey("guzhenren:jun_zhi_li_gu_passive_ironwall"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:jun_zhi_li_gu_active_ironwall_stance",
                cooldownKey("guzhenren:jun_zhi_li_gu_active_ironwall_stance"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBeastForces() {
        // 雷电狼力蛊：攻触发迟滞 + 突进咬杀
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:leidianlangligu_passive_thunder_bite",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveChargeEffect(
                "guzhenren:leidianlangligu_active_thunder_charge",
                cooldownKey("guzhenren:leidianlangligu_active_thunder_charge")
            )
        );

        // 烈焰熊力蛊：受击反震（减伤+反扑）+ 范围震荡
        GuEffectRegistry.register(
            new LiDaoHurtProcReductionEffect(
                "guzhenren:lieyanxiongligu_passive_burning_fury",
                cooldownKey("guzhenren:lieyanxiongligu_passive_burning_fury"),
                MobEffects.DAMAGE_BOOST
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAoEBurstEffect(
                "guzhenren:lieyanxiongligu_active_burning_quake",
                cooldownKey("guzhenren:lieyanxiongligu_active_burning_quake"),
                MobEffects.WEAKNESS
            )
        );

        // 青牛劳力蛊：提高上限（高转）+ 践踏压制
        GuEffectRegistry.register(
            new LiDaoSustainedVariableCapEffect(
                "guzhenren:qing_niu_lao_li_gu_passive_tough_yoke",
                List.of(
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAoEBurstEffect(
                "guzhenren:qing_niu_lao_li_gu_active_oxen_stomp",
                cooldownKey("guzhenren:qing_niu_lao_li_gu_active_oxen_stomp"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerDashAndGrowth() {
        // 全力以赴蛊：持续增势（攻击）+ 单点重击
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:quan_li_yi_fu_gu_passive_all_in",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:quan_li_yi_fu_gu_active_all_in_blow",
                cooldownKey("guzhenren:quan_li_yi_fu_gu_active_all_in_blow"),
                List.of()
            )
        );

        // 石鬼负力蛊：攻触发压制 + 破势重击
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:shi_gui_fu_li_gu_passive_stone_ghost_pressure",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:shi_gui_fu_li_gu_active_stone_ghost_crush",
                cooldownKey("guzhenren:shi_gui_fu_li_gu_active_stone_ghost_crush"),
                List.of(
                    new LiDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 蓄力蛊：持续蓄势（力量）+ 蓄势重击
        GuEffectRegistry.register(
            new LiDaoSustainedMobEffectEffect(
                "guzhenren:xu_li_gu_passive_storing_force",
                MobEffects.DAMAGE_BOOST,
                SIX_SECONDS_TICKS,
                0
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:xu_li_gu_active_storing_blow",
                cooldownKey("guzhenren:xu_li_gu_active_storing_blow"),
                List.of()
            )
        );

        // 自力更生蛊·三转：提高上限（高转）+ 体魄提振
        GuEffectRegistry.register(
            new LiDaoSustainedVariableCapEffect(
                "guzhenren:zi_li_geng_sheng_gu_3_passive_self_reliance_3",
                List.of(
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:zi_li_geng_sheng_gu_3_active_self_uplift_3",
                cooldownKey("guzhenren:zi_li_geng_sheng_gu_3_active_self_uplift_3"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 未炼化·横冲蛊：持续行进（移速）+ 长程横冲
        GuEffectRegistry.register(
            new LiDaoSustainedMobEffectEffect(
                "guzhenren:wei_lian_hua_heng_chong_gu_passive_unrefined_stride",
                MobEffects.MOVEMENT_SPEED,
                SIX_SECONDS_TICKS,
                0
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveChargeEffect(
                "guzhenren:wei_lian_hua_heng_chong_gu_active_unrefined_dash",
                cooldownKey("guzhenren:wei_lian_hua_heng_chong_gu_active_unrefined_dash")
            )
        );

        // 未炼化·直撞蛊：受击硬扛（减伤）+ 直撞突破
        GuEffectRegistry.register(
            new LiDaoHurtProcReductionEffect(
                "guzhenren:wei_lian_hua_zhi_zhuang_gu_passive_unrefined_guard",
                cooldownKey("guzhenren:wei_lian_hua_zhi_zhuang_gu_passive_unrefined_guard"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveChargeEffect(
                "guzhenren:wei_lian_hua_zhi_zhuang_gu_active_unrefined_strike",
                cooldownKey("guzhenren:wei_lian_hua_zhi_zhuang_gu_active_unrefined_strike")
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
