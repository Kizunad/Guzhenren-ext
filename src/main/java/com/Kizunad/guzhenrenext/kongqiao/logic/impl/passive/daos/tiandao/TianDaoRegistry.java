package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.tiandao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoActiveSelfRecoveryEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class TianDaoRegistry {

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.TIAN_DAO;

    private static final int BUFF_SHORT_TICKS = 120;
    private static final int BUFF_MEDIUM_TICKS = 200;
    private static final int DEBUFF_SHORT_TICKS = 120;

    private TianDaoRegistry() {}

    public static void registerAll() {
        registerShouGu();
        registerShiNianShouGu();
        registerBaiNianShouGu();
    }

    private static void registerShouGu() {
        final String passive = "guzhenren:shou_gu_passive_yearly_breath";
        final String active = "guzhenren:shou_gu_active_yearly_guard";

        GuEffectRegistry.register(new DaoSustainedResourceRegenEffect(passive, DAO_TYPE));
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "regen_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShiNianShouGu() {
        final String passive = "guzhenren:shi_nian_shou_gu_passive_ten_year_shell";
        final String active = "guzhenren:shi_nian_shou_gu_active_ten_year_restore";

        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                passive,
                DAO_TYPE,
                MobEffects.REGENERATION
            )
        );
        GuEffectRegistry.register(
            new DaoActiveSelfRecoveryEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active)
            )
        );
    }

    private static void registerBaiNianShouGu() {
        final String passive = "guzhenren:bainianshougu_passive_century_cap";
        final String active = "guzhenren:bainianshougu_active_century_blessing";

        GuEffectRegistry.register(
            new YuDaoSustainedVariableCapEffect(
                passive,
                DAO_TYPE,
                List.of(
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "max_hunpo_bonus"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "max_hunpo_resistance_bonus"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "niantou_capacity_bonus"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        BUFF_MEDIUM_TICKS,
                        "regen_amplifier",
                        0
                    ),
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        DEBUFF_SHORT_TICKS,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );
    }
}

