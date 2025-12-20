package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common.XingDaoActiveRescueStarEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转星道：三星/救星/流星/大步。
 */
public final class TierThreeXingDaoRegistry {

    private TierThreeXingDaoRegistry() {}

    public static void registerAll() {
        registerSanXingZaiTianGu();
        registerJiuXingGu();
        registerLiuXingTianZhuiGu();
        registerDaBuLiuXingGu();
    }

    private static void registerSanXingZaiTianGu() {
        // 三星在天蛊：增幅（攻强）+ 群体祝福
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:san_xing_zai_tian_gu_passive_three_star_power",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:san_xing_zai_tian_gu_active_three_star_blessing",
                cooldownKey("guzhenren:san_xing_zai_tian_gu_active_three_star_blessing"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
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

    private static void registerJiuXingGu() {
        // 救星蛊：稳态续航 + 缺血越多治疗越强（救星）
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:jiuxinggu_passive_rescue_aura"
            )
        );
        GuEffectRegistry.register(
            new XingDaoActiveRescueStarEffect(
                "guzhenren:jiuxinggu_active_rescue_star",
                cooldownKey("guzhenren:jiuxinggu_active_rescue_star")
            )
        );
    }

    private static void registerLiuXingTianZhuiGu() {
        // 流星天坠蛊：攻击触发迟滞 + 范围陨落
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:liuxingtianzhuigu_passive_meteor_fragment",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:liuxingtianzhuigu_active_meteor_fall",
                cooldownKey("guzhenren:liuxingtianzhuigu_active_meteor_fall"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerDaBuLiuXingGu() {
        // 大步流星蛊：脚力（移速）+ 挪移突进
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:da_bu_liu_xing_gu_passive_meteor_stride",
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:da_bu_liu_xing_gu_active_meteor_step",
                cooldownKey("guzhenren:da_bu_liu_xing_gu_active_meteor_step"),
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

