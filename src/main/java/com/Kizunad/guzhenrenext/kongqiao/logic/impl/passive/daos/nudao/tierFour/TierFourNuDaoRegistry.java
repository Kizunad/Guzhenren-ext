package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.nudao.common.NuDaoActiveAoEBurstEffect;
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
 * 四转奴道：形成“兽潮范围爆发”与“高转资源上限”两条强势路线。
 */
public final class TierFourNuDaoRegistry {

    private TierFourNuDaoRegistry() {}

    public static void registerAll() {
        registerTiger();
        registerBeastSwallow();
        registerEagle();
        registerFish();
        registerWolf();
        registerAntelope();
        registerDog();
        registerBear();
    }

    private static void registerTiger() {
        // 御虎蛊（四）：虎王处决
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_hu_gu_4_passive_tiger_king",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_hu_gu_4_active_tiger_king_execute",
                cooldownKey("guzhenren:yu_hu_gu_4_active_tiger_king_execute"),
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

    private static void registerBeastSwallow() {
        // 万兽吞军蛊：兽潮吞军（高转上限 + 范围爆发）
        GuEffectRegistry.register(
            new NuDaoSustainedVariableCapEffect(
                "guzhenren:wan_shou_tun_jun_gu_passive_beast_supply",
                List.of(
                    new NuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new NuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:wan_shou_tun_jun_gu_active_beast_stampede",
                cooldownKey("guzhenren:wan_shou_tun_jun_gu_active_beast_stampede"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerEagle() {
        // 御鹰蛊（四）：鹰眼锁空
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_ying_gu_4_passive_eagle_survey",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveTargetNukeEffect(
                "guzhenren:yu_ying_gu_4_active_eagle_lock",
                cooldownKey("guzhenren:yu_ying_gu_4_active_eagle_lock"),
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

    private static void registerFish() {
        // 御鱼蛊（四）：水脉群疗
        GuEffectRegistry.register(
            new NuDaoSustainedRegenEffect(
                "guzhenren:yu_yu_gu_4_passive_waterline_regen"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yu_yu_gu_4_active_waterline_heal",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_yu_gu_4_active_waterline_heal"),
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
        // 御狼蛊（四）：狼群冲阵
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yu_lang_gu_4_passive_wolf_pressure",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_lang_gu_4_active_wolf_stampede",
                cooldownKey("guzhenren:yu_lang_gu_4_active_wolf_stampede"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerAntelope() {
        // 御羚蛊（四）：疾风闪行
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_ling_gu_4_passive_gale_stride",
                DaoHenHelper.DaoType.NU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:yu_ling_gu_4_active_gale_blink",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_ling_gu_4_active_gale_blink"),
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

    private static void registerDog() {
        // 御犬蛊（四）：围猎束缚
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yu_quan_gu_4_passive_hound_hunt",
                DaoHenHelper.DaoType.NU_DAO,
                MobEffects.DIG_SPEED
            )
        );
        GuEffectRegistry.register(
            new NuDaoActiveAoEBurstEffect(
                "guzhenren:yu_quan_gu_4_active_hound_bind",
                cooldownKey("guzhenren:yu_quan_gu_4_active_hound_bind"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerBear() {
        // 御熊蛊（四）：熊王守阵
        GuEffectRegistry.register(
            new NuDaoHurtProcReductionEffect(
                "guzhenren:yu_xiong_gu_4_passive_bear_king_wall",
                cooldownKey("guzhenren:yu_xiong_gu_4_passive_bear_king_wall"),
                MobEffects.DAMAGE_RESISTANCE,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yu_xiong_gu_4_active_bear_king_guard",
                DaoHenHelper.DaoType.NU_DAO,
                cooldownKey("guzhenren:yu_xiong_gu_4_active_bear_king_guard"),
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

