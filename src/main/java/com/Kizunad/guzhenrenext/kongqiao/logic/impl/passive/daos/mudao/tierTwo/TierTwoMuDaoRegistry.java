package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转木道。
 */
public final class TierTwoMuDaoRegistry {
    private TierTwoMuDaoRegistry() {}

    public static void registerAll() {
        // 九叶生机草：群体生机扶持 + 自身持续再生
        GuEffectRegistry.register(
            new MuDaoSustainedMobEffectEffect(
                "guzhenren:jiu_xie_sheng_ji_cao_passive_nineleaf_vigor",
                MobEffects.REGENERATION
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAllySupportEffect(
                "guzhenren:jiu_xie_sheng_ji_cao_active_vitality_bloom",
                cooldownKey("guzhenren:jiu_xie_sheng_ji_cao_active_vitality_bloom"),
                List.of(
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 松针蛊：概率毒刺 + 范围针雨
        GuEffectRegistry.register(
            new MuDaoAttackProcDebuffEffect(
                "guzhenren:songzhengu_passive_pine_needle_poison",
                MobEffects.POISON
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAoEBurstEffect(
                "guzhenren:songzhengu_active_pine_needle_volley",
                cooldownKey("guzhenren:songzhengu_active_pine_needle_volley"),
                MobEffects.POISON
            )
        );

        // 草裙蛊：常驻防护（护甲）+ 护体树皮
        GuEffectRegistry.register(
            new MuDaoSustainedAttributeModifierEffect(
                "guzhenren:caoqungu_passive_grass_skirt_guard",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveSelfBuffEffect(
                "guzhenren:caoqungu_active_barkskin_wrap",
                cooldownKey("guzhenren:caoqungu_active_barkskin_wrap"),
                List.of(
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

