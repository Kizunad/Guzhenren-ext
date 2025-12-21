package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
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
 * 三转奴道：开始出现“统御爆发”与“群体战术”的核心技能。
 */
public final class TierThreeNuDaoRegistry {

    private TierThreeNuDaoRegistry() {}

    public static void registerAll() {
        registerBeastHeart();
        registerTiger();
        registerEagle();
        registerDog();
        registerFish();
        registerWolf();
        registerAntelope();
        registerBear();
    }

    private static void registerBeastHeart() {
        // 千兽食心蛊：噬心统御（偏爆发）
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:qian_shou_shi_xin_gu_passive_beast_frenzy",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:qian_shou_shi_xin_gu_active_beast_heart_crush",
                cooldownKey("guzhenren:qian_shou_shi_xin_gu_active_beast_heart_crush"),
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

    private static void registerTiger() {
        // 御虎蛊（三）：虎威镇压
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_hu_gu_3_passive_tiger_dominance",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_hu_gu_3_active_tiger_execute",
                cooldownKey("guzhenren:yu_hu_gu_3_active_tiger_execute"),
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
        // 御鹰蛊（三）：鹰翼滑翔
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_ying_gu_3_passive_eagle_glide",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.SLOW_FALLING
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_ying_gu_3_active_eagle_pierce",
                cooldownKey("guzhenren:yu_ying_gu_3_active_eagle_pierce"),
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

    private static void registerDog() {
        // 御犬蛊（三）：追猎号令
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_quan_gu_3_passive_hound_focus",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.DIG_SPEED
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_quan_gu_3_active_hound_circle",
                cooldownKey("guzhenren:yu_quan_gu_3_active_hound_circle"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerFish() {
        // 御鱼蛊（三）：潮汐回元
        GuEffectRegistry.register(
            new NuDaoSustainedRegenEffect(
                "guzhenren:yu_yu_gu_3_passive_tide_regen"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yu_yu_gu_3_active_tide_cleanse",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_yu_gu_3_active_tide_cleanse"),
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

    private static void registerWolf() {
        // 御狼蛊（三）：群狼撕扯
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yu_lang_gu_3_passive_wolf_rend",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_lang_gu_3_active_wolf_rush",
                cooldownKey("guzhenren:yu_lang_gu_3_active_wolf_rush"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerAntelope() {
        // 御羚蛊（三）：踏风跃进
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_ling_gu_3_passive_wind_stride",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_ling_gu_3_active_wind_leap",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_ling_gu_3_active_wind_leap"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.SLOW_FALLING,
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
        // 御熊蛊（三）：熊魂镇守
        GuEffectRegistry.register(
            new NuDaoHurtProcReductionEffect(
                "guzhenren:yu_xiong_gu_3_passive_bear_sentinel",
                cooldownKey("guzhenren:yu_xiong_gu_3_passive_bear_sentinel"),
                MobEffects.DAMAGE_RESISTANCE,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_xiong_gu_3_active_bear_bulwark",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_xiong_gu_3_active_bear_bulwark"),
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

