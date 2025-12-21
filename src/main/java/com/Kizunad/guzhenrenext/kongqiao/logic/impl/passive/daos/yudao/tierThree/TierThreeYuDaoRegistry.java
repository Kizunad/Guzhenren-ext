package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSwapEffect;
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
 * 三转·宇道 蛊虫效果注册表。
 */
public final class TierThreeYuDaoRegistry {

    private TierThreeYuDaoRegistry() {}

    public static void registerAll() {
        registerTianJiGu();
        registerSanZhuanDongChaGu();
        registerYiJieGu();
        registerJieDanGu();
        registerDunYuGu();
        registerDieJieGu();
        registerDaDuWaThree();
        registerZhanJuGu();
        registerHuanWeiGu();
        registerSanZhuanYuMaoGu();
        registerSanZhuanYuanLaoGu();
    }

    private static void registerTianJiGu() {
        // 天机蛊：运转加速 + 单点破机
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:tianjigu_passive_heavenly_gears",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetNukeEffect(
                "guzhenren:tianjigu_active_fate_pierce",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:tianjigu_active_fate_pierce"),
                List.of(
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerSanZhuanDongChaGu() {
        // 三转洞察蛊：深度洞察 + 星图显形
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:sanzhuandongchagu_passive_deep_insight",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:sanzhuandongchagu_active_star_map",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:sanzhuandongchagu_active_star_map"),
                MobEffects.GLOWING
            )
        );
    }

    private static void registerYiJieGu() {
        // 异界蛊：受击位移闪避 + 虚界形态
        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                "guzhenren:yijiegu_passive_otherworld_shift",
                DaoHenHelper.DaoType.YU_DAO,
                null
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:yijiegu_active_phase_form",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:yijiegu_active_phase_form"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.INVISIBILITY,
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

    private static void registerJieDanGu() {
        // 解弹蛊：弹道偏折 + 周身斥力
        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                "guzhenren:jiedangu_passive_deflect",
                DaoHenHelper.DaoType.YU_DAO,
                null
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:jiedangu_active_repulse",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:jiedangu_active_repulse"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerDunYuGu() {
        // 盾宇蛊：空间护盾
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:dunyugu_passive_space_shield",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:dunyugu_active_space_barrier",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:dunyugu_active_space_barrier"),
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
    }

    private static void registerDieJieGu() {
        // 叠界蛊：界障叠加 + 换界
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:diejiegu_passive_layered_boundary",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSwapEffect(
                "guzhenren:diejiegu_active_swap_boundary",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:diejiegu_active_swap_boundary"),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                ),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
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

    private static void registerDaDuWaThree() {
        // 大肚蛙（三）：更厚腹囊 + 崩震
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:da_du_wa_3_passive_pocket_belly",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:da_du_wa_3_active_belly_slam",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:da_du_wa_3_active_belly_slam"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerZhanJuGu() {
        // 占据蛊：夺位（换位）+ 虚弱
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:zhanjugu_passive_space_seize",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSwapEffect(
                "guzhenren:zhanjugu_active_occupy",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:zhanjugu_active_occupy"),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
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
                    )
                )
            )
        );
    }

    private static void registerHuanWeiGu() {
        // 换位蛊：预备脚力 + 迅捷换位
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:huanweigu_passive_prepared_shift",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSwapEffect(
                "guzhenren:huanweigu_active_swap",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:huanweigu_active_swap"),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                ),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
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

    private static void registerSanZhuanYuMaoGu() {
        // 三转宇猫蛊：狩猎（汲取）+ 挪移突击
        GuEffectRegistry.register(
            new YuDaoAttackProcLeechEffect(
                "guzhenren:sanzhuanyumaogu_passive_space_hunt",
                DaoHenHelper.DaoType.YU_DAO
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:sanzhuanyumaogu_active_blink_strike",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:sanzhuanyumaogu_active_blink_strike"),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerSanZhuanYuanLaoGu() {
        // 元老蛊（三转）：稳态扶持（中）
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:sanzhuanyuanlaogu_passive_elder_nurture",
                DaoHenHelper.DaoType.YU_DAO
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:sanzhuanyuanlaogu_active_elder_support",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:sanzhuanyuanlaogu_active_elder_support"),
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
