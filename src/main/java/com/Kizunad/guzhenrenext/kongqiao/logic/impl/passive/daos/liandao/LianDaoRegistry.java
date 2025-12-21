package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.liandao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class LianDaoRegistry {

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LIAN_DAO;

    private static final int BUFF_SHORT_TICKS = 120;
    private static final int BUFF_MEDIUM_TICKS = 200;

    private LianDaoRegistry() {}

    public static void registerAll() {
        registerQingShanZaiGu();
    }

    private static void registerQingShanZaiGu() {
        final String passive = "guzhenren:qingshanzaigu_passive_green_mountain_cap";
        final String active = "guzhenren:qingshanzaigu_active_green_mountain_guard";

        GuEffectRegistry.register(
            new YuDaoSustainedVariableCapEffect(
                passive,
                DAO_TYPE,
                List.of(
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "max_zhenyuan_bonus"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "max_jingli_bonus"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "niantou_capacity_bonus"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        BUFF_MEDIUM_TICKS,
                        "resistance_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );
    }
}

