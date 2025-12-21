package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.daodao.common.DaoDaoActiveBladeLightPierceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.daodao.common.DaoDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common.DaoDaoSustainedChargedStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common.DaoDaoSustainedMultiAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转刀道：单刀/拔刀斩（强化）。
 */
public final class TierFourDaoDaoRegistry {

    private static final int DEFAULT_EFFECT_TICKS = 80;

    private TierFourDaoDaoRegistry() {}

    public static void registerAll() {
        registerDanDaoGu();
        registerBaDaoZhanGuFour();
    }

    private static void registerDanDaoGu() {
        // 单刀蛊：被动每秒充能一次“断刀附伤” + 主动锁定斩击
        GuEffectRegistry.register(
            new DaoDaoSustainedChargedStrikeEffect(
                "guzhenren:dandaogu_passive_single_blade_charge"
            )
        );
        GuEffectRegistry.register(
            new DaoDaoActiveTargetStrikeEffect(
                "guzhenren:dandaogu_active_single_blade_break",
                cooldownKey("guzhenren:dandaogu_active_single_blade_break"),
                List.of(
                    new DaoDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        DEFAULT_EFFECT_TICKS,
                        "effect_amplifier",
                        0
                    ),
                    new DaoDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        DEFAULT_EFFECT_TICKS,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBaDaoZhanGuFour() {
        // 四转拔刀斩蛊：被动护甲/韧性强化 + 主动刀光穿行（更远更痛）
        GuEffectRegistry.register(
            new DaoDaoSustainedMultiAttributeModifierEffect(
                "guzhenren:ba_dao_zhan_gu_4_passive_draw_slash_guard",
                List.of(
                    DaoDaoSustainedMultiAttributeModifierEffect.of(
                        "guzhenren:ba_dao_zhan_gu_4_passive_draw_slash_guard",
                        Attributes.ARMOR,
                        AttributeModifier.Operation.ADD_VALUE,
                        "armor",
                        0.0
                    ),
                    DaoDaoSustainedMultiAttributeModifierEffect.of(
                        "guzhenren:ba_dao_zhan_gu_4_passive_draw_slash_guard",
                        Attributes.ARMOR_TOUGHNESS,
                        AttributeModifier.Operation.ADD_VALUE,
                        "armor_toughness",
                        0.0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new DaoDaoActiveBladeLightPierceEffect(
                "guzhenren:ba_dao_zhan_gu_4_active_draw_slash_flash",
                cooldownKey("guzhenren:ba_dao_zhan_gu_4_active_draw_slash_flash"),
                List.of(
                    new DaoDaoActiveBladeLightPierceEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        DEFAULT_EFFECT_TICKS,
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

