package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveAreaStatusEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveTeleportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAttackProcEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转光道蛊虫效果注册。
 */
public final class TierThreeGuangDaoRegistry {

    private TierThreeGuangDaoRegistry() {}

    public static void registerAll() {
        registerLiaoGuangGu();
        registerGuangZhaGu();
        registerGuangHongGu();
        registerLingGuangYiShanGu();
    }

    private static void registerLiaoGuangGu() {
        final String passive =
            "guzhenren:liao_guang_gu_passive_white_stream";
        final String active =
            "guzhenren:liao_guang_gu_active_white_stream";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_liao_guang_gu_active_white_stream";

        GuEffectRegistry.register(new GuangDaoSustainedAreaHealEffect(passive));
        GuEffectRegistry.register(
            new GuangDaoActiveAreaHealEffect(
                active,
                activeCooldownKey,
                List.of(
                    new GuangDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerGuangZhaGu() {
        final String passive =
            "guzhenren:guang_zha_gu_passive_prism_barrier";
        final String active =
            "guzhenren:guang_zha_gu_active_prism_wall";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_guang_zha_gu_active_prism_wall";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttributeModifierEffect(
                passive,
                new GuangDaoSustainedAttributeModifierEffect.AttributeInstanceAccessor() {
                    @Override
                    public net.minecraft.world.entity.ai.attributes.AttributeInstance get(
                        final net.minecraft.world.entity.LivingEntity user
                    ) {
                        return user.getAttribute(Attributes.ARMOR);
                    }

                    @Override
                    public AttributeModifier.Operation getOperation() {
                        return AttributeModifier.Operation.ADD_VALUE;
                    }

                    @Override
                    public String getSalt() {
                        return "armor";
                    }
                }
            )
        );

        GuEffectRegistry.register(
            new GuangDaoActiveAreaStatusEffect(
                active,
                activeCooldownKey,
                List.of(
                    new GuangDaoActiveAreaStatusEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "self_resistance_duration_ticks",
                        0,
                        "self_resistance_amplifier",
                        0
                    ),
                    new GuangDaoActiveAreaStatusEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "self_absorption_duration_ticks",
                        0,
                        "self_absorption_amplifier",
                        0
                    )
                ),
                List.of(),
                List.of(
                    new GuangDaoActiveAreaStatusEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "blind_duration_ticks",
                        0,
                        "blind_amplifier",
                        0
                    ),
                    new GuangDaoActiveAreaStatusEffect.EffectSpec(
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

    private static void registerGuangHongGu() {
        final String passive =
            "guzhenren:guang_hong_gu_passive_rainbow_stride";
        final String active =
            "guzhenren:guang_hong_gu_active_light_escape";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_guang_hong_gu_active_light_escape";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttributeModifierEffect(
                passive,
                new GuangDaoSustainedAttributeModifierEffect.AttributeInstanceAccessor() {
                    @Override
                    public net.minecraft.world.entity.ai.attributes.AttributeInstance get(
                        final net.minecraft.world.entity.LivingEntity user
                    ) {
                        return user.getAttribute(Attributes.MOVEMENT_SPEED);
                    }

                    @Override
                    public AttributeModifier.Operation getOperation() {
                        return AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                    }

                    @Override
                    public String getSalt() {
                        return "speed";
                    }
                }
            )
        );

        GuEffectRegistry.register(
            new GuangDaoActiveTeleportEffect(
                active,
                activeCooldownKey,
                List.of(
                    new GuangDaoActiveTeleportEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    ),
                    new GuangDaoActiveTeleportEffect.EffectSpec(
                        MobEffects.SLOW_FALLING,
                        "slow_fall_duration_ticks",
                        0,
                        "slow_fall_amplifier",
                        0
                    )
                ),
                List.of(
                    new GuangDaoActiveTeleportEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "arrival_blind_duration_ticks",
                        0,
                        "arrival_blind_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerLingGuangYiShanGu() {
        final String passive =
            "guzhenren:ling_guang_yi_shan_gu_passive_sudden_insight";
        final String procCooldownKey =
            "GuzhenrenExtCooldown_ling_guang_yi_shan_gu_passive_sudden_insight_proc";
        final String active =
            "guzhenren:ling_guang_yi_shan_gu_active_sidestep";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_ling_guang_yi_shan_gu_active_sidestep";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttackProcEffect(passive, procCooldownKey)
        );
        GuEffectRegistry.register(
            new GuangDaoActiveTeleportEffect(
                active,
                activeCooldownKey,
                List.of(
                    new GuangDaoActiveTeleportEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    ),
                    new GuangDaoActiveTeleportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        0,
                        "resistance_amplifier",
                        0
                    )
                ),
                List.of()
            )
        );
    }
}

