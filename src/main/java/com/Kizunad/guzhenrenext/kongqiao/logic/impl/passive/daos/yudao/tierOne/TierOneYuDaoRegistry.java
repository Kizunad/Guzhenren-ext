package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetDisplaceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.common.DaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转·宇道 蛊虫效果注册表。
 */
public final class TierOneYuDaoRegistry {

    private TierOneYuDaoRegistry() {}

    public static void registerAll() {
        // 空锁蛊：锁定敌势 + 牵引
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:kongsuogu_passive_spatial_lock",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new DaoSustainedResourceRegenEffect(
                "guzhenren:kongsuogu_passive_kong_xi_hui_qi",
                DaoHenHelper.DaoType.YU_DAO,
                false,
                true,
                true,
                false
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetDisplaceEffect(
                "guzhenren:kongsuogu_active_space_chain",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:kongsuogu_active_space_chain"),
                true,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );

        // 一转宇猫蛊：挪移脚力
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yizhuanyumaogu_passive_space_stride",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:yizhuanyumaogu_active_blink_step",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:yizhuanyumaogu_active_blink_step"),
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

        // 大肚蛙：口袋腹囊（护体）
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:daduwa_passive_pocket_belly",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:daduwa_active_belly_shield",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:daduwa_active_belly_shield"),
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
        );

        // 元老蛊（1）：稳态扶持
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:yuan_lao_gu_1_passive_elder_nurture",
                DaoHenHelper.DaoType.YU_DAO
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yuan_lao_gu_1_active_elder_support",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:yuan_lao_gu_1_active_elder_support"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveAllySupportEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
