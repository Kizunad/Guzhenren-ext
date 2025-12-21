package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.common.NuDaoAttackProcLifeStealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.common.NuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.common.NuDaoSustainedRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转奴道：以“驭兽基础”构建攻防辅三线能力。
 */
public final class TierOneNuDaoRegistry {

    private TierOneNuDaoRegistry() {}

    public static void registerAll() {
        registerEagle();
        registerCommand();
        registerBear();
        registerBlood();
        registerAntelope();
        registerFish();
        registerWolf();
        registerDog();
    }

    private static void registerEagle() {
        // 御鹰蛊：鹰眼洞察
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_ying_gu_passive_eagle_eye",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_ying_gu_active_eagle_mark",
                cooldownKey("guzhenren:yu_ying_gu_active_eagle_mark"),
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

    private static void registerCommand() {
        // 御军蛊：统御号令
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_jun_gu_passive_command_aura",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yu_jun_gu_active_pack_command",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_jun_gu_active_pack_command"),
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

    private static void registerBear() {
        // 御熊蛊：熊皮护命
        GuEffectRegistry.register(
            new NuDaoHurtProcReductionEffect(
                "guzhenren:yu_xiong_gu_passive_bear_hide",
                cooldownKey("guzhenren:yu_xiong_gu_passive_bear_hide"),
                MobEffects.DAMAGE_RESISTANCE,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_xiong_gu_active_bear_rage",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_xiong_gu_active_bear_rage"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBlood() {
        // 凶兽饮血蛊：饮血续战
        GuEffectRegistry.register(
            new NuDaoAttackProcLifeStealEffect(
                "guzhenren:xiong_shou_yin_xie_gu_passive_blood_drain",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:xiong_shou_yin_xie_gu_active_bloody_pounce",
                cooldownKey("guzhenren:xiong_shou_yin_xie_gu_active_bloody_pounce"),
                List.of(
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

    private static void registerAntelope() {
        // 御羚蛊：轻蹄疾行
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_ling_gu_passive_antelope_stride",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_ling_gu_active_leap_stride",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_ling_gu_active_leap_stride"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.JUMP,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerFish() {
        // 御鱼蛊：水息回元
        GuEffectRegistry.register(
            new NuDaoSustainedRegenEffect(
                "guzhenren:yu_yu_gu_passive_fish_respite"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_yu_gu_active_tide_breath",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_yu_gu_active_tide_breath"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.WATER_BREATHING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DOLPHINS_GRACE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerWolf() {
        // 御狼蛊：群狼围猎
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yu_lang_gu_passive_pack_frenzy",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_lang_gu_active_pack_howl",
                cooldownKey("guzhenren:yu_lang_gu_active_pack_howl"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerDog() {
        // 御犬蛊：猎犬嗅迹
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_quan_gu_passive_hound_scent",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.MOVEMENT_SPEED
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_quan_gu_active_hound_mark",
                cooldownKey("guzhenren:yu_quan_gu_active_hound_mark"),
                List.of(
                    new NuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.GLOWING,
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
