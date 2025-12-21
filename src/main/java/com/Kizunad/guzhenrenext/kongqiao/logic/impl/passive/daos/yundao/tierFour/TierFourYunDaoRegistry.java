package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yundao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转云道：腾云（常驻身法、主动升空护体）。
 */
public final class TierFourYunDaoRegistry {

    private static final int DEFAULT_RISE_EFFECT_TICKS = 160;

    private TierFourYunDaoRegistry() {}

    public static void registerAll() {
        registerTengYun();
    }

    private static void registerTengYun() {
        // 腾云蛊：常驻身法 + 腾云升空（自护体/机动）
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:tengyungu_passive_cloud_stride",
                DaoHenHelper.DaoType.YUN_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:tengyungu_active_rise_on_cloud",
                DaoHenHelper.DaoType.YUN_DAO,
                cooldownKey("guzhenren:tengyungu_active_rise_on_cloud"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.SLOW_FALLING,
                        "effect_duration_ticks",
                        DEFAULT_RISE_EFFECT_TICKS,
                        "slow_falling_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        DEFAULT_RISE_EFFECT_TICKS,
                        "speed_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.JUMP,
                        "effect_duration_ticks",
                        DEFAULT_RISE_EFFECT_TICKS,
                        "jump_amplifier",
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
