package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.toudao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSwapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoActiveTargetResourceDrainEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcLeechEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TouDaoRegistry {

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.TOU_DAO;

    private static final int BUFF_SHORT_TICKS = 120;
    private static final int DEBUFF_SHORT_TICKS = 120;
    private static final double DEFAULT_SPEED_MULTIPLIER = 0.08;

    private TouDaoRegistry() {}

    public static void registerAll() {
        registerQiangQuGu();
        registerFuDiChouXinGu();
    }

    private static void registerQiangQuGu() {
        final String passive = "guzhenren:qiang_qu_gu_passive_force_snatch";
        final String active = "guzhenren:qiang_qu_gu_active_position_plunder";

        GuEffectRegistry.register(new YuDaoAttackProcLeechEffect(passive, DAO_TYPE));
        GuEffectRegistry.register(
            new YuDaoActiveSwapEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "speed_amplifier",
                        0
                    ),
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "strength_amplifier",
                        0
                    )
                ),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        DEBUFF_SHORT_TICKS,
                        "slow_amplifier",
                        0
                    ),
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        DEBUFF_SHORT_TICKS,
                        "weakness_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerFuDiChouXinGu() {
        final String passive = "guzhenren:fudichouxingu_passive_underhand_step";
        final String active = "guzhenren:fudichouxingu_active_pull_the_fuel";

        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                passive,
                DAO_TYPE,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                DEFAULT_SPEED_MULTIPLIER,
                "toudao_speed"
            )
        );
        GuEffectRegistry.register(
            new DaoActiveTargetResourceDrainEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                MobEffects.WEAKNESS
            )
        );
    }
}

