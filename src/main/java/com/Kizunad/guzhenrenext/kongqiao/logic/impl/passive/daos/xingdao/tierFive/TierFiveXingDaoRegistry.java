package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common.XingDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common.XingDaoActiveStarGateEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.common.XingDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.common.XingDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转星道：五星连珠/星门。
 */
public final class TierFiveXingDaoRegistry {

    private TierFiveXingDaoRegistry() {}

    public static void registerAll() {
        registerWuXingLianZhuGu();
        registerXingMenGu();
    }

    private static void registerWuXingLianZhuGu() {
        // 五星连珠蛊：极大增幅 + 群体仪式
        GuEffectRegistry.register(
            new XingDaoSustainedAttributeModifierEffect(
                "guzhenren:wu_xing_lian_zhu_gu_passive_five_star_link",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new XingDaoActiveAllySupportEffect(
                "guzhenren:wu_xing_lian_zhu_gu_active_five_star_rite",
                cooldownKey("guzhenren:wu_xing_lian_zhu_gu_active_five_star_rite"),
                List.of(
                    new XingDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new XingDaoActiveAllySupportEffect.EffectSpec(
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

    private static void registerXingMenGu() {
        // 星门蛊：星夜感知（常驻夜视）+ 星门挪移（夜晚/露天可选）
        GuEffectRegistry.register(
            new XingDaoSustainedMobEffectEffect(
                "guzhenren:xing_men_gu_passive_star_gaze",
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new XingDaoActiveStarGateEffect(
                "guzhenren:xing_men_gu_active_star_gate",
                cooldownKey("guzhenren:xing_men_gu_active_star_gate"),
                List.of(
                    new XingDaoActiveStarGateEffect.EffectSpec(
                        MobEffects.INVISIBILITY,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new XingDaoActiveStarGateEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
