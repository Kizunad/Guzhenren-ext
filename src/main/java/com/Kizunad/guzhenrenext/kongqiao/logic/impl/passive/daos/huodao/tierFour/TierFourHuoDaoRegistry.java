package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.common.HuoDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common.HuoDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierFourHuoDaoRegistry {
    private TierFourHuoDaoRegistry() {}

    public static void registerAll() {
        // 炎瞳蛊：持续夜视洞察 + 主动目光灼烧（远距离点杀）
        GuEffectRegistry.register(
            new HuoDaoSustainedMobEffectEffect(
                "guzhenren:yantonggu_passive_yan_tong_focus",
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveTargetNukeEffect(
                "guzhenren:yantonggu_active_yan_tong_burn",
                cooldownKey("guzhenren:yantonggu_active_yan_tong_burn"),
                List.of(
                    new HuoDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "blind_duration_ticks",
                        0,
                        "blind_amplifier",
                        0
                    )
                )
            )
        );

        // 火龙蛊：持续强化攻势 + 主动火龙咆哮（大范围爆发）
        GuEffectRegistry.register(
            new HuoDaoSustainedAttributeModifierEffect(
                "guzhenren:huolonggu_passive_fire_dragon_fury",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new HuoDaoActiveAoEBurstEffect(
                "guzhenren:huolonggu_active_fire_dragon_roar",
                cooldownKey("guzhenren:huolonggu_active_fire_dragon_roar"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

