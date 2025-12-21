package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveExtraCostDecorator;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveZhenyuanSurgeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.common.BianHuaDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.common.BianHuaDaoAttackProcLeechEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转变化道效果注册表。
 * <p>
 * “尸系形态”偏向极端：要么高防，要么高攻，要么群体瘟毒/血噬/天魔机动。
 * </p>
 */
public final class TierFiveBianHuaDaoRegistry {

    private TierFiveBianHuaDaoRegistry() {}

    public static void registerAll() {
        registerDiKuiShi();
        registerMengYanShi();
        registerTianMoShi();
        registerXueGuiShi();
        registerBingWenShiGu();
        registerXiuLuoShiGu();
    }

    private static void registerDiKuiShi() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:dikuishi_passive_earth_kui_shell",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor_toughness"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveZhenyuanSurgeEffect(
                    "guzhenren:dikuishi_active_earth_sustain",
                    DaoHenHelper.DaoType.BIAN_HUA_DAO,
                    cooldownKey("guzhenren:dikuishi_active_earth_sustain")
                )
            )
        );
    }

    private static void registerMengYanShi() {
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:mengyanshi_passive_nightmare_hide",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveTargetNukeEffect(
                "guzhenren:mengyanshi_active_nightmare_strike",
                cooldownKey("guzhenren:mengyanshi_active_nightmare_strike"),
                List.of(
                    new BianHuaDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new BianHuaDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerTianMoShi() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:tianmoshi_passive_sky_demon_wind",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveBlinkEffect(
                    "guzhenren:tianmoshi_active_sky_demon_dash",
                    DaoHenHelper.DaoType.BIAN_HUA_DAO,
                    cooldownKey("guzhenren:tianmoshi_active_sky_demon_dash"),
                    List.of(
                        new YuDaoActiveBlinkEffect.EffectSpec(
                            MobEffects.SLOW_FALLING,
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

    private static void registerXueGuiShi() {
        GuEffectRegistry.register(
            new BianHuaDaoAttackProcLeechEffect(
                "guzhenren:xueguishi_passive_blood_leech"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveTargetNukeEffect(
                "guzhenren:xueguishi_active_blood_mark",
                cooldownKey("guzhenren:xueguishi_active_blood_mark"),
                List.of(
                    new BianHuaDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WITHER,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new BianHuaDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerBingWenShiGu() {
        GuEffectRegistry.register(
            new BianHuaDaoAttackProcDebuffEffect(
                "guzhenren:bingwenshigu_passive_plague_touch",
                MobEffects.POISON
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:bingwenshigu_active_plague_mist",
                cooldownKey("guzhenren:bingwenshigu_active_plague_mist"),
                MobEffects.POISON
            )
        );
    }

    private static void registerXiuLuoShiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:xiuluoshigu_passive_asura_fury",
                DaoHenHelper.DaoType.BIAN_HUA_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveSelfBuffEffect(
                    "guzhenren:xiuluoshigu_active_asura_rampage",
                    DaoHenHelper.DaoType.BIAN_HUA_DAO,
                    cooldownKey("guzhenren:xiuluoshigu_active_asura_rampage"),
                    List.of(
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.DAMAGE_BOOST,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.MOVEMENT_SPEED,
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
