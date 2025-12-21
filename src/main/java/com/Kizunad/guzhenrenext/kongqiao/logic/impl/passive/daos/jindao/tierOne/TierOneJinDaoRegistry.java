package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoSustainedResourceRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转金道。
 */
public final class TierOneJinDaoRegistry {

    private TierOneJinDaoRegistry() {}

    public static void registerAll() {
        registerBronzeSkin();
        registerCopperSkin();
        registerIronSkin();
    }

    private static void registerBronzeSkin() {
        // 青铜蛊：基础护甲维持 + 金精养窍（持续回资源）+ 短暂护体
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:qingtonggu_passive_bronze_shell",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new JinDaoSustainedResourceRegenEffect(
                "guzhenren:qingtonggu_passive_metal_essence_nourish"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:qingtonggu_active_bronze_guard",
                cooldownKey("guzhenren:qingtonggu_active_bronze_guard"),
                List.of(
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerCopperSkin() {
        // 铜皮蛊：受击触发减伤（偏防御）+ 震荡波（小范围）
        GuEffectRegistry.register(
            new JinDaoHurtProcReductionEffect(
                "guzhenren:tong_pi_gu_passive_copper_hide",
                cooldownKey("guzhenren:tong_pi_gu_passive_copper_hide"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveAoEBurstEffect(
                "guzhenren:tong_pi_gu_active_copper_quake",
                cooldownKey("guzhenren:tong_pi_gu_active_copper_quake"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerIronSkin() {
        // 铁皮蛊：攻击触发压制 + 铁皮撞击
        GuEffectRegistry.register(
            new JinDaoAttackProcDebuffEffect(
                "guzhenren:t_tie_pi_gu_passive_iron_pressure",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:t_tie_pi_gu_active_iron_bash",
                cooldownKey("guzhenren:t_tie_pi_gu_active_iron_bash"),
                List.of(
                    new JinDaoActiveTargetNukeEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
