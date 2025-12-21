package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lvdao;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.common.DaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class LvDaoRegistry {

    private static final DaoHenHelper.DaoType DAO_TYPE = DaoHenHelper.DaoType.LV_DAO;

    private static final int BUFF_SHORT_TICKS = 120;
    private static final int BUFF_MEDIUM_TICKS = 200;
    private static final int DEBUFF_SHORT_TICKS = 120;

    private static final double DEFAULT_ATTACK_DAMAGE_ADD = 2.0;

    private LvDaoRegistry() {}

    public static void registerAll() {
        registerGongBeiGu();
        registerGongBeiGuSanZhuan();
        registerGongBeiGuSiZhuan();
        registerGongBeiGuWuZhuan();
    }

    private static void registerGongBeiGu() {
        final String passive = "guzhenren:gongbeigu_passive_rule_of_power";
        final String active = "guzhenren:gongbeigu_active_power_surge";

        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                passive,
                DAO_TYPE,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                DEFAULT_ATTACK_DAMAGE_ADD,
                "lvdao_attack"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "strength_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DIG_SPEED,
                        "haste_duration_ticks",
                        BUFF_SHORT_TICKS,
                        "haste_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerGongBeiGuSanZhuan() {
        final String passive = "guzhenren:gong_bei_gu_3_passive_bound_by_law";
        final String active = "guzhenren:gong_bei_gu_3_active_punish_strike";

        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                passive,
                DAO_TYPE,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetNukeEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                List.of(
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerGongBeiGuSiZhuan() {
        final String passive = "guzhenren:gongbeigusizhuan_passive_ordinance_shell";
        final String active = "guzhenren:gongbeigusizhuan_active_law_domain";

        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                passive,
                DAO_TYPE,
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                active,
                DAO_TYPE,
                DaoCooldownKeys.active(active),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerGongBeiGuWuZhuan() {
        final String passive = "guzhenren:gongbeiguwuzhuan_passive_grand_law_cap";
        final String active = "guzhenren:gongbeiguwuzhuan_active_grand_punishment";

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
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "max_hunpo_resistance_bonus"
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
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        BUFF_MEDIUM_TICKS,
                        "weakness_amplifier",
                        1
                    ),
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        BUFF_MEDIUM_TICKS,
                        "slow_amplifier",
                        1
                    )
                )
            )
        );
    }
}

