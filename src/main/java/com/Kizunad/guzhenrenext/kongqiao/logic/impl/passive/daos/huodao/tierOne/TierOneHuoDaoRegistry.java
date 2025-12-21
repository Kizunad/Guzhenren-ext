package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierOneHuoDaoRegistry {
    private TierOneHuoDaoRegistry() {}

    public static void registerAll() {
        // 火衣蛊：持续火抗护身 + 主动火衣护体（增益/小幅恢复）
        GuEffectRegistry.register(
            new HuoDaoSustainedMobEffectEffect(
                "guzhenren:huo_gu_passive_flame_cloak",
                MobEffects.FIRE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveSelfBuffEffect(
                "guzhenren:huo_gu_active_flame_ward",
                cooldownKey("guzhenren:huo_gu_active_flame_ward"),
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
                    )
                )
            )
        );

        // 单窍火炭蛊：持续增甲御寒 + 主动暖炉脉冲（群体治疗/增益）
        GuEffectRegistry.register(
            new HuoDaoSustainedAttributeModifierEffect(
                "guzhenren:dan_qiao_huo_tan_gu_passive_charcoal_shell",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveAllySupportEffect(
                "guzhenren:dan_qiao_huo_tan_gu_active_warmth_pulse",
                cooldownKey("guzhenren:dan_qiao_huo_tan_gu_active_warmth_pulse"),
                List.of(
                    new HuoDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    ),
                    new HuoDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.FIRE_RESISTANCE,
                        "fire_resistance_duration_ticks",
                        0,
                        "fire_resistance_amplifier",
                        0
                    )
                )
            )
        );

        // 火油蛊：攻击触发油污迟缓 + 主动泼油爆燃（小范围爆发）
        GuEffectRegistry.register(
            new HuoDaoAttackProcDebuffEffect(
                "guzhenren:huo_you_gu_passive_oil_ignite",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveAoEBurstEffect(
                "guzhenren:huo_you_gu_active_oil_burst",
                cooldownKey("guzhenren:huo_you_gu_active_oil_burst"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

