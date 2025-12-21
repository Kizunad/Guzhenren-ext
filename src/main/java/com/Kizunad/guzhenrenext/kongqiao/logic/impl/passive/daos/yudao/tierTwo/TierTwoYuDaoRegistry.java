package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetDisplaceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcLeechEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转·宇道 蛊虫效果注册表。
 */
public final class TierTwoYuDaoRegistry {

    private TierTwoYuDaoRegistry() {}

    public static void registerAll() {
        registerDongChaGu();
        registerErZhuanYuMaoGu();
        registerDiYuGu();
        registerXiMoGu();
        registerDaDuWaTwo();
        registerBuXiGu();
        registerQiJieGu();
        registerMoDiGu();
        registerErZhuanYuanLaoGu();
    }

    private static void registerDongChaGu() {
        // 洞察蛊：常驻洞察 + 显形
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:dongchagu_passive_spatial_insight",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:dongchagu_active_reveal",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:dongchagu_active_reveal"),
                MobEffects.GLOWING
            )
        );
    }

    private static void registerErZhuanYuMaoGu() {
        // 二转宇猫蛊：更强挪移
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:erzhuanyumaogu_passive_space_stride",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:erzhuanyumaogu_active_blink_dash",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:erzhuanyumaogu_active_blink_dash"),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerDiYuGu() {
        // 地宇蛊：地缚 + 崩裂
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:di_yu_gu_passive_earth_lock",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:di_yu_gu_active_earth_burst",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:di_yu_gu_active_earth_burst"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerXiMoGu() {
        // 吸魔蛊：汲取 + 牵引
        GuEffectRegistry.register(
            new YuDaoAttackProcLeechEffect(
                "guzhenren:ximogu_passive_mana_leech",
                DaoHenHelper.DaoType.YU_DAO
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetDisplaceEffect(
                "guzhenren:ximogu_active_siphon",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:ximogu_active_siphon"),
                true,
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerDaDuWaTwo() {
        // 大肚蛙（二）：更厚护体
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:da_du_wa_2_passive_pocket_belly",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:da_du_wa_2_active_belly_shield",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:da_du_wa_2_active_belly_shield"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBuXiGu() {
        // 不息蛊：受击闪避 + 续命
        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                "guzhenren:buxigu_passive_unending_breath",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.REGENERATION
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:buxigu_active_last_stand",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:buxigu_active_last_stand"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
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
        );
    }

    private static void registerQiJieGu() {
        // 七界蛊：界压单体
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:qijiegu_passive_sevenfold_bind",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetNukeEffect(
                "guzhenren:qijiegu_active_world_press",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:qijiegu_active_world_press"),
                List.of(
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.LEVITATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerMoDiGu() {
        // 末地蛊：踏虚（缓降）+ 挪移
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:mo_di_gu_passive_end_step",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.SLOW_FALLING
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:mo_di_gu_active_end_blink",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:mo_di_gu_active_end_blink"),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.INVISIBILITY,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerErZhuanYuanLaoGu() {
        // 元老蛊（二转）：群体扶持（小）
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:e_yuanlaogurzhuan_passive_elder_nurture",
                DaoHenHelper.DaoType.YU_DAO
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:e_yuanlaogurzhuan_active_elder_support",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:e_yuanlaogurzhuan_active_elder_support"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
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
