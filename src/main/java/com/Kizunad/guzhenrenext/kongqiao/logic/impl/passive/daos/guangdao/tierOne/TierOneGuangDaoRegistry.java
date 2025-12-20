package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveAreaStatusEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveTargetHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAttackProcEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedHurtProcEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 一转光道蛊虫效果注册。
 */
public final class TierOneGuangDaoRegistry {

    private TierOneGuangDaoRegistry() {}

    public static void registerAll() {
        registerShanGuangGu();
        registerXiaoGuangGu();
    }

    private static void registerShanGuangGu() {
        final String passive =
            "guzhenren:shan_guang_gu_passive_dazzling_guard";
        final String procCooldownKey =
            "GuzhenrenExtCooldown_shan_guang_gu_passive_dazzling_guard_proc";
        final String active =
            "guzhenren:shan_guang_gu_active_flash_burst";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_shan_guang_gu_active_flash_burst";

        GuEffectRegistry.register(
            new GuangDaoSustainedHurtProcEffect(passive, procCooldownKey)
        );
        GuEffectRegistry.register(
            new GuangDaoActiveAreaStatusEffect(
                active,
                activeCooldownKey,
                List.of(),
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

    private static void registerXiaoGuangGu() {
        final String passive =
            "guzhenren:xiaoguanggu_passive_twinkling_assist";
        final String procCooldownKey =
            "GuzhenrenExtCooldown_xiaoguanggu_passive_twinkling_assist_proc";
        final String active =
            "guzhenren:xiaoguanggu_active_warm_glimmer";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_xiaoguanggu_active_warm_glimmer";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttackProcEffect(passive, procCooldownKey)
        );
        GuEffectRegistry.register(
            new GuangDaoActiveTargetHealEffect(
                active,
                activeCooldownKey,
                List.of(
                    new GuangDaoActiveTargetHealEffect.EffectSpec(
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
}

