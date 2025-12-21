package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveAreaFieldEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoAttackMarkDetonateEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoHurtProcReductionEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 二转毒道。
 */
public final class TierTwoDuDaoRegistry {

    private static final int SHORT_BUFF_TICKS = 60;

    private TierTwoDuDaoRegistry() {}

    public static void registerAll() {
        registerAiBieChi();
        registerDuZhenGu();
        registerSuanShuiGu();
        registerXieZiShi();
    }

    private static void registerAiBieChi() {
        // 爱别离：情毒暗印（延迟引爆）+ 影行偷袭（自我隐匿）
        final String passiveId = "guzhenren:ai_bie_chi_passive_love_separation_mark";
        GuEffectRegistry.register(
            new DuDaoAttackMarkDetonateEffect(
                passiveId,
                cooldownKey(passiveId),
                markUntilKey(passiveId),
                markExpireKey(passiveId),
                List.of(MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN)
            )
        );

        GuEffectRegistry.register(
            new DuDaoActiveSelfBuffEffect(
                "guzhenren:ai_bie_chi_active_shadow_ambush",
                cooldownKey("guzhenren:ai_bie_chi_active_shadow_ambush"),
                List.of(
                    new DuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.INVISIBILITY,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new DuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerDuZhenGu() {
        // 毒针蛊：毒针余劲（攻触发）+ 毒针点射（指向）
        GuEffectRegistry.register(
            new DuDaoAttackProcDebuffEffect(
                "guzhenren:du_zhen_gu_passive_poison_needle",
                List.of(MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveTargetStrikeEffect(
                "guzhenren:du_zhen_gu_active_poison_needle",
                cooldownKey("guzhenren:du_zhen_gu_active_poison_needle"),
                List.of(
                    new DuDaoActiveTargetStrikeEffect.EffectSpec(
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

    private static void registerSuanShuiGu() {
        // 酸水蛊：腐蚀酸液（攻触发）+ 酸蚀沼泽（地面领域）
        GuEffectRegistry.register(
            new DuDaoAttackProcDebuffEffect(
                "guzhenren:suan_shui_gu_passive_acid_corrosion",
                List.of(MobEffects.DIG_SLOWDOWN, MobEffects.WEAKNESS)
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveAreaFieldEffect(
                "guzhenren:suan_shui_gu_active_acid_field",
                cooldownKey("guzhenren:suan_shui_gu_active_acid_field"),
                List.of(MobEffects.DIG_SLOWDOWN, MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
    }

    private static void registerXieZiShi() {
        // 蝎子屎：毒性护体（受击卸力）+ 蝎毒一口（指向削弱）
        final String passiveId = "guzhenren:xie_zi_shi_passive_toxin_shell";
        GuEffectRegistry.register(
            new DuDaoHurtProcReductionEffect(
                passiveId,
                cooldownKey(passiveId),
                MobEffects.DAMAGE_RESISTANCE
            )
        );

        GuEffectRegistry.register(
            new DuDaoActiveTargetStrikeEffect(
                "guzhenren:xie_zi_shi_active_scorpion_pellet",
                cooldownKey("guzhenren:xie_zi_shi_active_scorpion_pellet"),
                List.of(
                    new DuDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new DuDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.HUNGER,
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

    private static String markUntilKey(final String usageId) {
        return "GuzhenrenExtDuDaoMarkUntil_" + usageId;
    }

    private static String markExpireKey(final String usageId) {
        return "GuzhenrenExtDuDaoMarkExpire_" + usageId;
    }
}

