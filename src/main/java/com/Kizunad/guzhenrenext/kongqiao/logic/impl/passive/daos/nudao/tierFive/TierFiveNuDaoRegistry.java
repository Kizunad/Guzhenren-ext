package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveCooldownRefreshEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.common.NuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.common.NuDaoSustainedRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.common.NuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转奴道：高转资源上限 + 大规模战术技能。
 */
public final class TierFiveNuDaoRegistry {

    private TierFiveNuDaoRegistry() {}

    public static void registerAll() {
        registerAntQueen();
        registerTiger();
        registerEagle();
        registerBeastBell();
        registerFish();
        registerAntelope();
        registerBearKing();
        registerWolfKing();
        registerDogKing();
        registerArmyHorn();
        registerTotems();
    }

    private static void registerAntQueen() {
        // 军团蚁后蛊：军团供给（上限）+ 战阵扶持
        GuEffectRegistry.register(
            new NuDaoSustainedVariableCapEffect(
                "guzhenren:jun_tuan_yi_hou_gu_passive_army_supply",
                List.of(
                    new NuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new NuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new NuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    ),
                    new NuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:jun_tuan_yi_hou_gu_active_army_command",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:jun_tuan_yi_hou_gu_active_army_command"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DIG_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerTiger() {
        // 御虎蛊（五）：虎皇斩杀（主伤）
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_hu_gu_5_passive_tiger_emperor",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_hu_gu_5_active_tiger_emperor_slash",
                cooldownKey("guzhenren:yu_hu_gu_5_active_tiger_emperor_slash"),
                List.of(
                    new NuDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerEagle() {
        // 御鹰蛊（五）：鹰皇锁定（偏控）
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_ying_gu_5_passive_eagle_emperor_eye",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_ying_gu_5_active_eagle_emperor_lock",
                cooldownKey("guzhenren:yu_ying_gu_5_active_eagle_emperor_lock"),
                List.of(
                    new NuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new NuDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerBeastBell() {
        // 御兽铃蛊：冷却回响
        GuEffectRegistry.register(
            new NuDaoSustainedRegenEffect(
                "guzhenren:yu_shou_ling_gu_5_passive_bell_respite"
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveCooldownRefreshEffect(
                "guzhenren:yu_shou_ling_gu_5_active_bell_refresh",
                cooldownKey("guzhenren:yu_shou_ling_gu_5_active_bell_refresh")
            )
        );
    }

    private static void registerFish() {
        // 御鱼蛊（五）：潮汐大赦（群体续航）
        GuEffectRegistry.register(
            new NuDaoSustainedRegenEffect(
                "guzhenren:yu_yu_gu_5_passive_tide_sustain"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yu_yu_gu_5_active_tide_salvation",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_yu_gu_5_active_tide_salvation"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerAntelope() {
        // 御羚蛊（五）：踏空闪行（机动）
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_ling_gu_5_passive_sky_stride",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:yu_ling_gu_5_active_sky_blink",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_ling_gu_5_active_sky_blink"),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBearKing() {
        // 御熊蛊（五）：熊王震地（范围压制）
        GuEffectRegistry.register(
            new NuDaoHurtProcReductionEffect(
                "guzhenren:yu_xiong_5_passive_bear_king_bastion",
                cooldownKey("guzhenren:yu_xiong_5_passive_bear_king_bastion"),
                MobEffects.DAMAGE_RESISTANCE,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_xiong_5_active_bear_king_quake",
                cooldownKey("guzhenren:yu_xiong_5_active_bear_king_quake"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerWolfKing() {
        // 御狼蛊（五）：狼王破阵
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yu_lang_gu_5_passive_wolf_king_rend",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_lang_gu_5_active_wolf_king_stampede",
                cooldownKey("guzhenren:yu_lang_gu_5_active_wolf_king_stampede"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerDogKing() {
        // 御犬蛊（五）：猎犬锁命
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_quan_gu_5_passive_hound_king_sense",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.DIG_SPEED
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_quan_gu_5_active_hound_king_lock",
                cooldownKey("guzhenren:yu_quan_gu_5_active_hound_king_lock"),
                List.of(
                    new NuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new NuDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerArmyHorn() {
        // 军号蛊（五）：战意鼓舞
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jun_hao_gu_5_passive_horn_might",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:jun_hao_gu_5_active_horn_rally",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:jun_hao_gu_5_active_horn_rally"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerTotems() {
        registerTotem(
            "guzhenren:hu_tu_gu_passive_tiger_totem",
            "guzhenren:hu_tu_gu_active_tiger_totem_roar",
            GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
            "cap_max_zhenyuan",
            MobEffects.DAMAGE_BOOST
        );
        registerTotem(
            "guzhenren:xiong_tu_gu_passive_bear_totem",
            "guzhenren:xiong_tu_gu_active_bear_totem_guard",
            GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
            "cap_max_hunpo",
            MobEffects.DAMAGE_RESISTANCE
        );
        registerTotem(
            "guzhenren:lang_tu_gu_passive_wolf_totem",
            "guzhenren:lang_tu_gu_active_wolf_totem_howl",
            GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
            "cap_max_jingli",
            MobEffects.MOVEMENT_SPEED
        );
        registerTotem(
            "guzhenren:quan_tu_gu_passive_hound_totem",
            "guzhenren:quan_tu_gu_active_hound_totem_hunt",
            GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
            "cap_niantou_capacity",
            MobEffects.DIG_SPEED
        );
        registerTotem(
            "guzhenren:ling_tu_gu_passive_antelope_totem",
            "guzhenren:ling_tu_gu_active_antelope_totem_dash",
            GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
            "cap_max_jingli",
            MobEffects.JUMP
        );
    }

    private static void registerTotem(
        final String passiveUsageId,
        final String activeUsageId,
        final String variableKey,
        final String capMetaKey,
        final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> activeBuff
    ) {
        GuEffectRegistry.register(
            new NuDaoSustainedVariableCapEffect(
                passiveUsageId,
                List.of(
                    new NuDaoSustainedVariableCapEffect.CapSpec(
                        variableKey,
                        capMetaKey
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                activeUsageId,
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey(activeUsageId),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        activeBuff,
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
