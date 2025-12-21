package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveExtraCostDecorator;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveTargetDisplaceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.common.BianHuaDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转变化道效果注册表。
 * <p>
 * 以“护甲/隐匿/灵巧”方向分化：正面防护、背甲震荡、狐妖突袭、隐鳞潜行等。
 * </p>
 */
public final class TierTwoBianHuaDaoRegistry {

    private TierTwoBianHuaDaoRegistry() {}

    public static void registerAll() {
        registerLinJiaGu();
        registerBeiJiaGu();
        registerHeiZongGu();
        registerYinLinGu();
        registerHuYaoGu();
    }

    private static void registerLinJiaGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:lin_jia_gu_passive_scale_vitality",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveSelfBuffEffect(
                    "guzhenren:lin_jia_gu_active_scale_fortify",
                    DaoHenHelper.DaoType.BIAN_HUA_DAO,
                    cooldownKey("guzhenren:lin_jia_gu_active_scale_fortify"),
                    List.of(
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.ABSORPTION,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.DAMAGE_RESISTANCE,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        )
                    )
                )
            )
        );
    }

    private static void registerBeiJiaGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:bei_jia_gu_passive_backplate_guard",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:bei_jia_gu_active_backplate_shockwave",
                cooldownKey("guzhenren:bei_jia_gu_active_backplate_shockwave"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerHeiZongGu() {
        GuEffectRegistry.register(
            new BianHuaDaoHurtProcReductionEffect(
                "guzhenren:hei_zong_gu_passive_black_mane_guard",
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:hei_zong_gu_active_mane_bristle",
                cooldownKey("guzhenren:hei_zong_gu_active_mane_bristle"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerYinLinGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yin_lin_gu_passive_hidden_scale",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                MobEffects.INVISIBILITY
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveBlinkEffect(
                    "guzhenren:yin_lin_gu_active_hidden_step",
                    DaoHenHelper.DaoType.BIAN_HUA_DAO,
                    cooldownKey("guzhenren:yin_lin_gu_active_hidden_step"),
                    List.of(
                        new YuDaoActiveBlinkEffect.EffectSpec(
                            MobEffects.INVISIBILITY,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveBlinkEffect.EffectSpec(
                            MobEffects.MOVEMENT_SPEED,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        )
                    )
                )
            )
        );
    }

    private static void registerHuYaoGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:hu_yao_gu_passive_fox_grace",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveTargetDisplaceEffect(
                "guzhenren:hu_yao_gu_active_fox_pounce",
                cooldownKey("guzhenren:hu_yao_gu_active_fox_pounce"),
                true,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
