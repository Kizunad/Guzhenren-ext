package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveExtraCostDecorator;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.common.BianHuaDaoActiveTargetDisplaceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSwapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.common.BianHuaDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转变化道效果注册表。
 * <p>
 * 本转偏向“重躯护体 + 翼类机动 + 阴阳转身”的综合战斗形态。
 * </p>
 */
public final class TierFourBianHuaDaoRegistry {

    private TierFourBianHuaDaoRegistry() {}

    public static void registerAll() {
        registerTiaoJiangGu();
        registerLieHuoXiongQu();
        registerXuanWuZhongQu();
        registerYinYangZhuanShenGu();
        registerYunFengKuiGu();
        registerYanChiGu();
        registerYingYangGu();
        registerShiLongQunJiaGu();
        registerShuiXiangJia();
        registerYunShouXueGu();
    }

    private static void registerTiaoJiangGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:tiaojianggu_passive_hopping_jiang",
                MobEffects.JUMP
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:tiaojianggu_active_jiang_slam",
                cooldownKey("guzhenren:tiaojianggu_active_jiang_slam"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerLieHuoXiongQu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:liehuoxiongqu_passive_flame_bear_armor",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveSelfBuffEffect(
                    "guzhenren:liehuoxiongqu_active_flame_bear_roar",
                    cooldownKey("guzhenren:liehuoxiongqu_active_flame_bear_roar"),
                    List.of(
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.FIRE_RESISTANCE,
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
                        ),
                        new YuDaoActiveSelfBuffEffect.EffectSpec(
                            MobEffects.DAMAGE_BOOST,
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

    private static void registerXuanWuZhongQu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:xuanwuzhongqu_passive_xuanwu_heavy_shell",
                Attributes.KNOCKBACK_RESISTANCE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "knockback_resistance"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:xuanwuzhongqu_active_shell_quake",
                cooldownKey("guzhenren:xuanwuzhongqu_active_shell_quake"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerYinYangZhuanShenGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:yin_yang_zhuan_shen_gu_passive_rebirth_flow"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveSwapEffect(
                    "guzhenren:yin_yang_zhuan_shen_gu_active_turn_over",
                    cooldownKey("guzhenren:yin_yang_zhuan_shen_gu_active_turn_over"),
                    List.of(
                        new YuDaoActiveSwapEffect.EffectSpec(
                            MobEffects.REGENERATION,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        )
                    ),
                    List.of(
                        new YuDaoActiveSwapEffect.EffectSpec(
                            MobEffects.WEAKNESS,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveSwapEffect.EffectSpec(
                            MobEffects.MOVEMENT_SLOWDOWN,
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

    private static void registerYunFengKuiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:yunfengkuigu_passive_cloud_helm_glide",
                MobEffects.SLOW_FALLING
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveBlinkEffect(
                    "guzhenren:yunfengkuigu_active_cloud_dash",
                    cooldownKey("guzhenren:yunfengkuigu_active_cloud_dash"),
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

    private static void registerYanChiGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yanchigu_passive_swallow_wing",
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveBlinkEffect(
                    "guzhenren:yanchigu_active_swallow_flight",
                    cooldownKey("guzhenren:yanchigu_active_swallow_flight"),
                    List.of(
                        new YuDaoActiveBlinkEffect.EffectSpec(
                            MobEffects.SLOW_FALLING,
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

    private static void registerYingYangGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yingyanggu_passive_eagle_surge",
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveTargetDisplaceEffect(
                "guzhenren:yingyanggu_active_eagle_dive",
                cooldownKey("guzhenren:yingyanggu_active_eagle_dive"),
                false,
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerShiLongQunJiaGu() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:shilongqunjiagu_passive_stone_dragon_plate",
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor_toughness"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveAoEBurstEffect(
                "guzhenren:shilongqunjiagu_active_stone_dragon_shock",
                cooldownKey("guzhenren:shilongqunjiagu_active_stone_dragon_shock"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerShuiXiangJia() {
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:shuixiangjia_passive_water_elephant_armor",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveAllySupportEffect(
                    "guzhenren:shuixiangjia_active_water_elephant_blessing",
                    cooldownKey("guzhenren:shuixiangjia_active_water_elephant_blessing"),
                    List.of(
                        new YuDaoActiveAllySupportEffect.EffectSpec(
                            MobEffects.DAMAGE_RESISTANCE,
                            "effect_duration_ticks",
                            0,
                            "effect_amplifier",
                            0
                        ),
                        new YuDaoActiveAllySupportEffect.EffectSpec(
                            MobEffects.WATER_BREATHING,
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

    private static void registerYunShouXueGu() {
        GuEffectRegistry.register(
            new BianHuaDaoHurtProcReductionEffect(
                "guzhenren:yunshouxuegu_passive_cloud_beast_dodge",
                MobEffects.MOVEMENT_SPEED
            )
        );
        GuEffectRegistry.register(
            new BianHuaDaoActiveExtraCostDecorator(
                new YuDaoActiveBlinkEffect(
                    "guzhenren:yunshouxuegu_active_cloud_step",
                    cooldownKey("guzhenren:yunshouxuegu_active_cloud_step"),
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
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
