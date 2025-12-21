package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveTargetHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.common.RenDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 二转人道。
 */
public final class TierTwoRenDaoRegistry {

    private TierTwoRenDaoRegistry() {}

    public static void registerAll() {
        registerChiTieSheLiGu();
        registerQiGaiE();
    }

    private static void registerChiTieSheLiGu() {
        // 赤铁舍利蛊：以“压制”表现斗争中的人道——攻击触发虚弱 + 主动点杀
        GuEffectRegistry.register(
            new RenDaoAttackProcDebuffEffect(
                "guzhenren:chi_tie_she_li_gu_passive_red_iron_suppress_2",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveTargetNukeEffect(
                "guzhenren:chi_tie_she_li_gu_active_red_iron_strike_2",
                cooldownKey("guzhenren:chi_tie_she_li_gu_active_red_iron_strike_2"),
                List.of(
                    new RenDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerQiGaiE() {
        // 乞丐蛾：存储真元的“口袋”——常驻回元 + 主动放出“真元救急”
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:qi_gai_e_passive_aperture_reserve_2",
                DaoHenHelper.DaoType.REN_DAO
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveTargetHealEffect(
                "guzhenren:qi_gai_e_active_reserve_release_2",
                cooldownKey("guzhenren:qi_gai_e_active_reserve_release_2"),
                List.of(
                    new RenDaoActiveTargetHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
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

