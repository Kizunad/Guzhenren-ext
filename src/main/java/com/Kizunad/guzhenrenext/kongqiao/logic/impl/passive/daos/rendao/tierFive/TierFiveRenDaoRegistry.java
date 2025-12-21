package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveSelfSacrificeExplosionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.common.RenDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转人道。
 */
public final class TierFiveRenDaoRegistry {

    private TierFiveRenDaoRegistry() {}

    public static void registerAll() {
        registerZiJinSheLiGu();
        registerYuSuiGu();
    }

    private static void registerZiJinSheLiGu() {
        // 紫金舍利蛊：五转“底蕴”——大幅提升多项上限 + 强力群体滋养
        GuEffectRegistry.register(
            new RenDaoSustainedVariableCapEffect(
                "guzhenren:zi_jin_she_li_gu_passive_purple_gold_cap_5",
                List.of(
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    ),
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    ),
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_max_hunpo_resistance"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveAreaHealEffect(
                "guzhenren:zi_jin_she_li_gu_active_purple_gold_bless_5",
                cooldownKey("guzhenren:zi_jin_she_li_gu_active_purple_gold_bless_5"),
                List.of(
                    new RenDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new RenDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resist_duration_ticks",
                        0,
                        "resist_amplifier",
                        0
                    ),
                    new RenDaoActiveAreaHealEffect.EffectSpec(
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

    private static void registerYuSuiGu() {
        // 玉碎蛊：以“执念殉爆”表现——常驻提升攻击（代价维持）+ 主动自毁殉爆
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yu_shu_gu_passive_obsession_edge_5",
                DaoHenHelper.DaoType.REN_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveSelfSacrificeExplosionEffect(
                "guzhenren:yu_shu_gu_active_jade_shatter_5",
                cooldownKey("guzhenren:yu_shu_gu_active_jade_shatter_5")
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

