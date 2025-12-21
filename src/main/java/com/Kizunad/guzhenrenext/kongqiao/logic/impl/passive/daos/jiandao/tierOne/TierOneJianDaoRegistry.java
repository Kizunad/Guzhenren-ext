package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.common.JianDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.common.JianDaoCooldownKeys;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierOneJianDaoRegistry {

    private TierOneJianDaoRegistry() {}

    public static void registerAll() {
        registerJianHenGu();
        registerJianXiaGu();
    }

    private static void registerJianHenGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jianhengu_passive_scar_edge",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveSelfBuffEffect(
                "guzhenren:jianhengu_active_scar_resolve",
                JianDaoCooldownKeys.active("guzhenren:jianhengu_active_scar_resolve"),
                List.of(
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new JianDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "boost_duration_ticks",
                        0,
                        "boost_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJianXiaGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:jianxiagu_passive_sword_box_training",
                DaoHenHelper.DaoType.JIAN_DAO,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new JianDaoActiveAoEBurstEffect(
                "guzhenren:jianxiagu_active_sword_box_wave",
                JianDaoCooldownKeys.active("guzhenren:jianxiagu_active_sword_box_wave"),
                List.of(
                    new JianDaoActiveAoEBurstEffect.DebuffSpec(
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
}
