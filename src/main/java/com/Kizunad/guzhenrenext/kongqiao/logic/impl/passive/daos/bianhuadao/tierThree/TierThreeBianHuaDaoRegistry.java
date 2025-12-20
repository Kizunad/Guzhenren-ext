package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveExtraCostDecorator;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveTargetDisplaceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.common.BianHuaDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.common.BianHuaDaoAttackProcLeechEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.common.BianHuaDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转变化道效果注册表。
 * <p>
 * 本转大量涉及“形态改造”的攻防与机动：僵尸系、兽系、水下系、硬鬃系等。
 * </p>
 */
public final class TierThreeBianHuaDaoRegistry {

    private TierThreeBianHuaDaoRegistry() {}

    public static void registerAll() {
        // 犬魄蛊：保命替身（已实现的特化被动）
        GuEffectRegistry.register(new QuanPuGuSubstituteSacrificeEffect());

        registerYinGu();
        registerYangGu();
        registerMaoJiangGu();
        registerChongZhiGu();
        registerDiXiongZhuaGu();
        registerJiaoWeiGu();
        registerHuPiGu();
        registerGangZongGu();
        registerBaiYuGu();
    }

    private static void registerYinGu() {
        GuEffectRegistry.register(
            new BianHuaDaoAttackProcDebuffEffect(
                "guzhenren:yin_gu_passive_yin_drain",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveAllySupportEffect(
                    "guzhenren:yin_gu_active_yin_soothe",
                    cooldownKey("guzhenren:yin_gu_active_yin_soothe"),
                    List.of(
                        new YuDaoActiveAllySupportEffect.EffectSpec(
                            MobEffects.REGENERATION,
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
                        )
                    )
                )
            )
        );
    }

    private static void registerYangGu() {
        GuEffectRegistry.register(
            new BianHuaDaoAttackProcLeechEffect(
                "guzhenren:yang_gu_passive_yang_leech"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveSelfBuffEffect(
                    "guzhenren:yang_gu_active_yang_surge",
                    cooldownKey("guzhenren:yang_gu_active_yang_surge"),
                    List.of(
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.DAMAGE_BOOST,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.REGENERATION,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        )
                    )
                )
            )
        );
    }

    private static void registerMaoJiangGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:mao_jiang_gu_passive_mao_jiang_hide",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:mao_jiang_gu_active_jiang_slam",
                cooldownKey("guzhenren:mao_jiang_gu_active_jiang_slam"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerChongZhiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:chong_zhi_gu_passive_insect_mind"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveTargetNukeEffect(
                "guzhenren:chong_zhi_gu_active_insect_hex",
                cooldownKey("guzhenren:chong_zhi_gu_active_insect_hex"),
                List.of(
                    new BianHuaDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new BianHuaDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.POISON,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerDiXiongZhuaGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:di_xiong_zhua_gu_passive_ground_wolf_stride",
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveTargetDisplaceEffect(
                "guzhenren:di_xiong_zhua_gu_active_wolf_grab",
                cooldownKey("guzhenren:di_xiong_zhua_gu_active_wolf_grab"),
                true,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerJiaoWeiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:jiao_wei_gu_passive_mermaid_tail",
                MobEffects.DOLPHINS_GRACE
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveAllySupportEffect(
                    "guzhenren:jiao_wei_gu_active_aqua_support",
                    cooldownKey("guzhenren:jiao_wei_gu_active_aqua_support"),
                    List.of(
                        new YuDaoActiveAllySupportEffect.EffectSpec(
                            MobEffects.WATER_BREATHING,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveAllySupportEffect.EffectSpec(
                            MobEffects.DOLPHINS_GRACE,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        )
                    )
                )
            )
        );
    }

    private static void registerHuPiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:hupigu_passive_tiger_strength",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveTargetNukeEffect(
                "guzhenren:hupigu_active_tiger_rend",
                cooldownKey("guzhenren:hupigu_active_tiger_rend"),
                List.of(
                    new BianHuaDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerGangZongGu() {
        GuEffectRegistry.register(
            new BianHuaDaoHurtProcReductionEffect(
                "guzhenren:gang_zong_gu_passive_steel_bristle",
                MobEffects.DAMAGE_BOOST
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:gang_zong_gu_active_bristle_burst",
                cooldownKey("guzhenren:gang_zong_gu_active_bristle_burst"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerBaiYuGu() {
        GuEffectRegistry.register(
            new BianHuaDaoHurtProcReductionEffect(
                "guzhenren:baiyugu_2_passive_broken_feather_dodge",
                MobEffects.MOVEMENT_SPEED
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveBlinkEffect(
                    "guzhenren:baiyugu_2_active_feather_leap",
                    cooldownKey("guzhenren:baiyugu_2_active_feather_leap"),
                    List.of(
                        new YuDaoActiveBlinkEffect.EffectSpec(
                            MobEffects.SLOW_FALLING,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveBlinkEffect.EffectSpec(
                            MobEffects.MOVEMENT_SPEED,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        )
                    )
                )
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
