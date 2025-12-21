package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class TierThreeHuoDaoRegistry {
    private TierThreeHuoDaoRegistry() {}

    public static void registerAll() {
        registerBurningSkin();
        registerFlameArmor();
        registerFireMark();
        registerFireHeart();
        registerMagmaBurst();
    }

    private static void registerBurningSkin() {
        // 焚身蛊：受击减伤并点燃反击 + 主动焚身护体（强力增益）
        GuEffectRegistry.register(
            new HuoDaoHurtProcReductionEffect(
                "guzhenren:fen_shen_gu_passive_burning_skin",
                cooldownKey("guzhenren:fen_shen_gu_passive_burning_skin"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveSelfBuffEffect(
                "guzhenren:fen_shen_gu_active_burning_guard",
                cooldownKey("guzhenren:fen_shen_gu_active_burning_guard"),
                List.of(
                    new HuoDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.FIRE_RESISTANCE,
                        "fire_resistance_duration_ticks",
                        0,
                        "fire_resistance_amplifier",
                        0
                    ),
                    new HuoDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        0,
                        "resistance_amplifier",
                        0
                    ),
                    new HuoDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "damage_boost_duration_ticks",
                        0,
                        "damage_boost_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerFlameArmor() {
        // 炎胄蛊：持续火焰免疫 + 主动炎胄壁垒（护盾/减伤）
        GuEffectRegistry.register(
            new HuoDaoSustainedMobEffectEffect(
                "guzhenren:yan_zhou_gu_passive_flame_armor",
                MobEffects.FIRE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveSelfBuffEffect(
                "guzhenren:yan_zhou_gu_active_flame_bulwark",
                cooldownKey("guzhenren:yan_zhou_gu_active_flame_bulwark"),
                List.of(
                    new HuoDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    ),
                    new HuoDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        0,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerFireMark() {
        // 火眼蛊：攻击标记（发光）+ 主动火眼灼射（锁定点杀）
        GuEffectRegistry.register(
            new HuoDaoAttackProcDebuffEffect(
                "guzhenren:huo_yan_gu_passive_fire_mark",
                MobEffects.GLOWING
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveTargetNukeEffect(
                "guzhenren:huo_yan_gu_active_fire_gaze",
                cooldownKey("guzhenren:huo_yan_gu_active_fire_gaze"),
                List.of(
                    new HuoDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "glow_duration_ticks",
                        0,
                        "glow_amplifier",
                        0
                    ),
                    new HuoDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerFireHeart() {
        // 火心蛊：提升真元/精力/魂魄上限 + 主动火心激发（恢复/增益）
        GuEffectRegistry.register(
            new HuoDaoSustainedVariableCapEffect(
                "guzhenren:huoxingu_passive_fire_heart_cap",
                List.of(
                    new HuoDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "max_zhenyuan_bonus"
                    ),
                    new HuoDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "max_jingli_bonus"
                    ),
                    new HuoDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "max_hunpo_bonus"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveSelfBuffEffect(
                "guzhenren:huoxingu_active_fire_heart_awakening",
                cooldownKey("guzhenren:huoxingu_active_fire_heart_awakening"),
                List.of(
                    new HuoDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    ),
                    new HuoDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "damage_boost_duration_ticks",
                        0,
                        "damage_boost_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerMagmaBurst() {
        // 熔岩炸裂蛊：攻击触发虚弱与爆燃 + 主动熔岩爆裂（高额单体打击）
        GuEffectRegistry.register(
            new HuoDaoAttackProcDebuffEffect(
                "guzhenren:rong_yan_zha_lie_gu_passive_magma_shard",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveTargetNukeEffect(
                "guzhenren:rong_yan_zha_lie_gu_active_magma_burst",
                cooldownKey("guzhenren:rong_yan_zha_lie_gu_active_magma_burst"),
                List.of(
                    new HuoDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
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

