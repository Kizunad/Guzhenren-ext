package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveAreaPhysicalStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveAreaStatusEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveTeleportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAttackProcEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedRevealAndCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转光道蛊虫效果注册。
 */
public final class TierFiveGuangDaoRegistry {

    private TierFiveGuangDaoRegistry() {}

    public static void registerAll() {
        registerXunDianLiuGuangGu();
        registerChunGuangWuXianGu();
        registerHuoGuangZhuTianGu();
        registerTaiGuangGu();
        registerJiangHeRiXiaGu();
    }

    private static void registerXunDianLiuGuangGu() {
        final String passive =
            "guzhenren:xun_dian_liu_guang_gu_passive_swift_current";
        final String active =
            "guzhenren:xun_dian_liu_guang_gu_active_swift_flash";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_xun_dian_liu_guang_gu_active_swift_flash";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttributeModifierEffect(
                passive,
                new GuangDaoSustainedAttributeModifierEffect.AttributeInstanceAccessor() {
                    @Override
                    public net.minecraft.world.entity.ai.attributes.AttributeInstance get(
                        final net.minecraft.world.entity.LivingEntity user
                    ) {
                        return user.getAttribute(Attributes.ATTACK_SPEED);
                    }

                    @Override
                    public AttributeModifier.Operation getOperation() {
                        return AttributeModifier.Operation.ADD_VALUE;
                    }

                    @Override
                    public String getSalt() {
                        return "attack_speed";
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

    private static void registerChunGuangWuXianGu() {
        final String passive =
            "guzhenren:chun_guang_wu_xian_gu_passive_endless_spring";
        final String active =
            "guzhenren:chun_guang_wu_xian_gu_active_endless_spring";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_chun_guang_wu_xian_gu_active_endless_spring";

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
                    ),
                    new GuangDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerHuoGuangZhuTianGu() {
        final String passive =
            "guzhenren:huo_guang_zhu_tian_gu_passive_candle_sky";
        final String procCooldownKey =
            "GuzhenrenExtCooldown_huo_guang_zhu_tian_gu_passive_candle_sky_proc";
        final String active =
            "guzhenren:huo_guang_zhu_tian_gu_active_candle_sky";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_huo_guang_zhu_tian_gu_active_candle_sky";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttackProcEffect(passive, procCooldownKey)
        );
        GuEffectRegistry.register(
            new GuangDaoActiveAreaPhysicalStrikeEffect(active, activeCooldownKey)
        );
    }

    private static void registerTaiGuangGu() {
        final String passive =
            "guzhenren:taiguanggu_passive_ancient_sun";
        final String active =
            "guzhenren:taiguanggu_active_ancient_sun";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_taiguanggu_active_ancient_sun";

        GuEffectRegistry.register(
            new GuangDaoSustainedRevealAndCapEffect(
                passive,
                List.of(
                    new GuangDaoSustainedRevealAndCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "niantou_capacity_bonus"
                    ),
                    new GuangDaoSustainedRevealAndCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "jingli_max_bonus"
                    )
                )
            )
        );

        GuEffectRegistry.register(
            new GuangDaoActiveAreaStatusEffect(
                active,
                activeCooldownKey,
                List.of(),
                List.of(),
                List.of(
                    new GuangDaoActiveAreaStatusEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "glowing_duration_ticks",
                        0,
                        "glowing_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJiangHeRiXiaGu() {
        final String passive =
            "guzhenren:jiangherixiagu_passive_falling_river";
        final String procCooldownKey =
            "GuzhenrenExtCooldown_jiangherixiagu_passive_falling_river_proc";
        final String active =
            "guzhenren:jiangherixiagu_active_falling_river";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_jiangherixiagu_active_falling_river";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttackProcEffect(passive, procCooldownKey)
        );
        GuEffectRegistry.register(
            new GuangDaoActiveTargetDebuffEffect(
                active,
                activeCooldownKey,
                List.of(
                    new GuangDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    ),
                    new GuangDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    ),
                    new GuangDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "blind_duration_ticks",
                        0,
                        "blind_amplifier",
                        0
                    )
                )
            )
        );
    }
}

