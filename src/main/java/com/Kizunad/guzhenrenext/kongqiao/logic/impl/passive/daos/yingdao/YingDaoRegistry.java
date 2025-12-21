package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yingdao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class YingDaoRegistry {

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.YING_DAO;

    private static final int BUFF_SHORT_TICKS = 120;
    private static final int DEBUFF_SHORT_TICKS = 120;

    private YingDaoRegistry() {}

    public static void registerAll() {
        registerYouYingSuiXingGu();
        registerDieYingGu();
    }

    private static void registerYouYingSuiXingGu() {
        final String passive = "guzhenren:you_ying_sui_xing_gu_passive_shadow_evasion";
        final String active = "guzhenren:you_ying_sui_xing_gu_active_shadow_blink";

        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                passive,
                DAO_TYPE,
                MobEffects.INVISIBILITY
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.INVISIBILITY,
                        "invis_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "invis_amplifier",
                        0
                    ),
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "speed_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerDieYingGu() {
        final String passive = "guzhenren:dieyinggu_passive_layered_shadow_cap";
        final String active = "guzhenren:dieyinggu_active_layered_shadow_strike";

        GuEffectRegistry.register(
            new YuDaoSustainedVariableCapEffect(
                passive,
                DAO_TYPE,
                List.of(
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "niantou_capacity_bonus"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "max_jingli_bonus"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetNukeEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "blind_duration_ticks",
                        DEBUFF_SHORT_TICKS,
                        "blind_amplifier",
                        0
                    ),
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        DEBUFF_SHORT_TICKS,
                        "slow_amplifier",
                        0
                    )
                )
            )
        );
    }
}

