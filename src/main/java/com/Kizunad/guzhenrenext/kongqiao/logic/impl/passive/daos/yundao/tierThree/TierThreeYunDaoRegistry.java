package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yundao.common.YunDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yundao.common.YunDaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yundao.common.YunDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 三转云道：阴云/白云（隐匿、机动、资源上限）。
 */
public final class TierThreeYunDaoRegistry {

    private static final int DEFAULT_PRESSURE_EFFECT_TICKS = 60;
    private static final int DEFAULT_STEP_EFFECT_TICKS = 120;

    private TierThreeYunDaoRegistry() {}

    public static void registerAll() {
        registerYinYun();
        registerBaiYun();
    }

    private static void registerYinYun() {
        // 阴云蛊：持续匿踪 + 阴云压境（范围压制，普通伤害为主）
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yin_yun_gu_passive_gloom_shroud",
                DaoHenHelper.DaoType.YUN_DAO,
                MobEffects.INVISIBILITY
            )
        );
        GuEffectRegistry.register(
            new YunDaoActiveAoEBurstEffect(
                "guzhenren:yin_yun_gu_active_gloom_pressure",
                DaoHenHelper.DaoType.YUN_DAO,
                cooldownKey("guzhenren:yin_yun_gu_active_gloom_pressure"),
                List.of(
                    new YunDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "effect_duration_ticks",
                        DEFAULT_PRESSURE_EFFECT_TICKS,
                        "effect_amplifier",
                        0
                    ),
                    new YunDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        DEFAULT_PRESSURE_EFFECT_TICKS,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBaiYun() {
        // 白云蛊：云息供给（资源上限）+ 白云挪移（机动/续航）
        GuEffectRegistry.register(
            new YunDaoSustainedVariableCapEffect(
                "guzhenren:bai_yun_gu_passive_white_cloud_supply",
                DaoHenHelper.DaoType.YUN_DAO,
                List.of(
                    new YunDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new YunDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YunDaoSustainedResourceRegenEffect(
                "guzhenren:bai_yun_gu_passive_cloud_nourish"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:bai_yun_gu_active_white_cloud_step",
                DaoHenHelper.DaoType.YUN_DAO,
                cooldownKey("guzhenren:bai_yun_gu_active_white_cloud_step"),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        DEFAULT_STEP_EFFECT_TICKS,
                        "speed_amplifier",
                        0
                    ),
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        DEFAULT_STEP_EFFECT_TICKS,
                        "regen_amplifier",
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
