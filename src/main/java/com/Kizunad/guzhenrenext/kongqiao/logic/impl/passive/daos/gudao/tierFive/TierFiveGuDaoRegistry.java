package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转骨道蛊虫效果注册。
 */
public final class TierFiveGuDaoRegistry {

    private TierFiveGuDaoRegistry() {}

    public static void registerAll() {
        registerSongGuGu();
        registerBaiGuCheLunGu();
    }

    private static void registerSongGuGu() {
        final String passive = "guzhenren:song_gu_gu_passive_pine_marrow";
        final String active = "guzhenren:song_gu_gu_active_pine_storm";
        final String cooldownKey =
            "GuzhenrenExtCooldown_song_gu_gu_active_pine_storm";

        GuEffectRegistry.register(new ZhiDaoSustainedRegenEffect(passive));
        GuEffectRegistry.register(
            new GuDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new GuDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBaiGuCheLunGu() {
        final String passive =
            "guzhenren:bai_gu_che_lun_gu_passive_white_wheel";
        final String active =
            "guzhenren:bai_gu_che_lun_gu_active_wheel_charge";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bai_gu_che_lun_gu_active_wheel_charge";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(new GuDaoActiveChargeEffect(active, cooldownKey));
    }
}

