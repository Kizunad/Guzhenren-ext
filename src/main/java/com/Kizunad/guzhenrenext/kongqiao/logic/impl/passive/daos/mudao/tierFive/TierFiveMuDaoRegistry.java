package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转木道。
 */
public final class TierFiveMuDaoRegistry {
    private TierFiveMuDaoRegistry() {}

    public static void registerAll() {
        // 天元宝王莲：常驻回流 + 群体王莲敕令
        GuEffectRegistry.register(
            new MuDaoSustainedRegenEffect(
                "guzhenren:tianyuanbaowanglian_passive_wanglian_eternity"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAllySupportEffect(
                "guzhenren:tianyuanbaowanglian_active_wanglian_mandate",
                cooldownKey("guzhenren:tianyuanbaowanglian_active_wanglian_mandate"),
                List.of(
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );

        // 青峰承元蛊：常驻锋势（增伤）+ 峰落裁决
        GuEffectRegistry.register(
            new MuDaoSustainedAttributeModifierEffect(
                "guzhenren:qingfengchengyuangu_passive_peak_chengyuan",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveTargetNukeEffect(
                "guzhenren:qingfengchengyuangu_active_peak_punishment",
                cooldownKey("guzhenren:qingfengchengyuangu_active_peak_punishment"),
                List.of(
                    new MuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    ),
                    new MuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
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

