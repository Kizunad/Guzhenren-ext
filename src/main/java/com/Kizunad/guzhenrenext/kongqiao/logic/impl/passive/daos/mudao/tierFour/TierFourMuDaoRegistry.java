package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转木道。
 */
public final class TierFourMuDaoRegistry {
    private TierFourMuDaoRegistry() {}

    public static void registerAll() {
        registerChangQingGu();
        registerZhangYanGu();
        registerKuRongYou();
        registerHuHuaGu();
        registerShiMuYi();
        registerTianYuanBaoJunLian();
        registerLuoHuGu();
        registerQingTengJianYiGu();
    }

    private static void registerChangQingGu() {
        // 长青蛊：常驻生机回流 + 长青赐福
        GuEffectRegistry.register(
            new MuDaoSustainedRegenEffect(
                "guzhenren:changqinggu_passive_evergreen_regrowth"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveSelfBuffEffect(
                "guzhenren:changqinggu_active_evergreen_blessing",
                cooldownKey("guzhenren:changqinggu_active_evergreen_blessing"),
                List.of(
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerZhangYanGu() {
        // 瘴烟蛊：攻击触发失明/中毒 + 范围瘴云爆发
        GuEffectRegistry.register(
            new MuDaoAttackProcDebuffEffect(
                "guzhenren:zhangyangu_passive_miasma_touch",
                MobEffects.BLINDNESS
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAoEBurstEffect(
                "guzhenren:zhangyangu_active_miasma_cloud",
                cooldownKey("guzhenren:zhangyangu_active_miasma_cloud"),
                MobEffects.POISON
            )
        );
    }

    private static void registerKuRongYou() {
        // 枯荣蝣：受击减伤 + 枯荣打击（带凋零）
        GuEffectRegistry.register(
            new MuDaoHurtProcReductionEffect(
                "guzhenren:kurongyou_passive_wither_bloom",
                cooldownKey("guzhenren:kurongyou_passive_wither_bloom"),
                MobEffects.REGENERATION
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveTargetNukeEffect(
                "guzhenren:kurongyou_active_wither_strike",
                cooldownKey("guzhenren:kurongyou_active_wither_strike"),
                List.of(
                    new MuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WITHER,
                        "wither_duration_ticks",
                        0,
                        "wither_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerHuHuaGu() {
        // 护花蛊：常驻抗性护持 + 群体花护
        GuEffectRegistry.register(
            new MuDaoSustainedMobEffectEffect(
                "guzhenren:huhuagu_passive_flower_guard",
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAllySupportEffect(
                "guzhenren:huhuagu_active_flower_bulwark",
                cooldownKey("guzhenren:huhuagu_active_flower_bulwark"),
                List.of(
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShiMuYi() {
        // 蚀木蛊：攻击触发凋零 + 蚀木穿刺（普通伤害为主）
        GuEffectRegistry.register(
            new MuDaoAttackProcDebuffEffect(
                "guzhenren:shimuyi_passive_rotting_sap",
                MobEffects.WITHER
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveTargetNukeEffect(
                "guzhenren:shimuyi_active_rotwood_pierce",
                cooldownKey("guzhenren:shimuyi_active_rotwood_pierce"),
                List.of(
                    new MuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WITHER,
                        "wither_duration_ticks",
                        0,
                        "wither_amplifier",
                        0
                    ),
                    new MuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerTianYuanBaoJunLian() {
        // 天元宝君莲：持续恢复 + 群体宝君赐福
        GuEffectRegistry.register(
            new MuDaoSustainedRegenEffect(
                "guzhenren:tianyuanbaojunlian_passive_junlian_sanctuary"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAllySupportEffect(
                "guzhenren:tianyuanbaojunlian_active_junlian_favor",
                cooldownKey("guzhenren:tianyuanbaojunlian_active_junlian_favor"),
                List.of(
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerLuoHuGu() {
        // 落花蛊：受击退避/加速 + 花影挪移
        GuEffectRegistry.register(
            new MuDaoHurtProcReductionEffect(
                "guzhenren:luohugu_passive_falling_petals",
                cooldownKey("guzhenren:luohugu_passive_falling_petals"),
                MobEffects.MOVEMENT_SPEED
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveBlinkEffect(
                "guzhenren:luohugu_active_petal_step",
                cooldownKey("guzhenren:luohugu_active_petal_step"),
                List.of(
                    new MuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerQingTengJianYiGu() {
        // 青藤茧衣蛊：常驻韧性护体 + 青藤缠身
        GuEffectRegistry.register(
            new MuDaoSustainedAttributeModifierEffect(
                "guzhenren:qingtengjianyigu_passive_vine_cocoon_armor",
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "toughness"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveSelfBuffEffect(
                "guzhenren:qingtengjianyigu_active_vine_cocoon",
                cooldownKey("guzhenren:qingtengjianyigu_active_vine_cocoon"),
                List.of(
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
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
