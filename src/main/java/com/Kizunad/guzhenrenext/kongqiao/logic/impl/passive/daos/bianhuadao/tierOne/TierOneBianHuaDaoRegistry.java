package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveExtraCostDecorator;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转变化道效果注册表。
 * <p>
 * 以“皮/鳞”类改造为主：提供基础防御与轻度续航工具。
 * </p>
 */
public final class TierOneBianHuaDaoRegistry {

    private TierOneBianHuaDaoRegistry() {}

    public static void registerAll() {
        registerShouPiGu();
        registerYuLinGu();
    }

    private static void registerShouPiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:shou_pi_gu_passive_beast_hide",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveSelfBuffEffect(
                    "guzhenren:shou_pi_gu_active_hide_harden",
                    cooldownKey("guzhenren:shou_pi_gu_active_hide_harden"),
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
            )
        );
    }

    private static void registerYuLinGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_lin_gu_passive_fish_scale_vigor",
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveSelfBuffEffect(
                    "guzhenren:yu_lin_gu_active_aqua_skin",
                    cooldownKey("guzhenren:yu_lin_gu_active_aqua_skin"),
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
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
