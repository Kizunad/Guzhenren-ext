package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
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
 * 二转奴道：驭兽技艺成型，开始拥有明确的“主坦/主伤/主辅”分工。
 */
public final class TierTwoNuDaoRegistry {

    private TierTwoNuDaoRegistry() {}

    public static void registerAll() {
        registerTiger();
        registerEagle();
        registerMeat();
        registerFish();
        registerDog();
        registerAntelope();
        registerWolf();
        registerBear();
    }

    private static void registerTiger() {
        // 御虎蛊（二）：猛虎扑杀
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_hu_gu_2_passive_tiger_might",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_hu_gu_2_active_tiger_pounce",
                cooldownKey("guzhenren:yu_hu_gu_2_active_tiger_pounce"),
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
        // 御鹰蛊（二）：鹰眼压制
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_ying_gu_2_passive_eagle_watch",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_ying_gu_2_active_eagle_dive",
                cooldownKey("guzhenren:yu_ying_gu_2_active_eagle_dive"),
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

    private static void registerMeat() {
        // 百兽食肉蛊：饕餮噬肉
        GuEffectRegistry.register(
            new NuDaoAttackProcLifeStealEffect(
                "guzhenren:bai_shou_shi_rou_gu_passive_feast_fang",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:bai_shou_shi_rou_gu_active_feast_bite",
                cooldownKey("guzhenren:bai_shou_shi_rou_gu_active_feast_bite"),
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

    private static void registerFish() {
        // 御鱼蛊（二）：潮息扶持
        GuEffectRegistry.register(
            new NuDaoSustainedRegenEffect(
                "guzhenren:yu_yu_gu_2_passive_tide_respite"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yu_yu_gu_2_active_tide_support",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_yu_gu_2_active_tide_support"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.WATER_BREATHING,
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
        // 御犬蛊（二）：咬缚牵制
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yu_quan_gu_2_passive_hound_bite",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_quan_gu_2_active_hound_pursuit",
                cooldownKey("guzhenren:yu_quan_gu_2_active_hound_pursuit"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerAntelope() {
        // 御羚蛊（二）：轻灵奔跃
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_ling_gu_2_passive_grace_stride",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_ling_gu_2_active_grace_leap",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_ling_gu_2_active_grace_leap"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.JUMP,
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
        // 御狼蛊（二）：狼牙毒袭
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yu_lang_gu_2_passive_wolf_poison",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.POISON
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_lang_gu_2_active_wolf_howl",
                cooldownKey("guzhenren:yu_lang_gu_2_active_wolf_howl"),
                MobEffects.POISON
            )
        );
    }

    private static void registerBear() {
        // 御熊蛊（二）：熊魄护体
        GuEffectRegistry.register(
            new NuDaoHurtProcReductionEffect(
                "guzhenren:yu_xiong_gu_2_passive_bear_wall",
                cooldownKey("guzhenren:yu_xiong_gu_2_passive_bear_wall"),
                MobEffects.DAMAGE_RESISTANCE,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_xiong_gu_2_active_bear_guard",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_xiong_gu_2_active_bear_guard"),
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

