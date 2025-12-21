package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.common.DaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转人道。
 */
public final class TierOneRenDaoRegistry {

    private TierOneRenDaoRegistry() {}

    public static void registerAll() {
        registerQingTongSheLiGu();
        registerXiWangGu();
    }

    private static void registerQingTongSheLiGu() {
        // 青铜舍利蛊：以“滋养空窍”为核心——常驻回元 + 短时振奋
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:qing_tong_she_li_gu_passive_aperture_nourish_1",
                DaoHenHelper.DaoType.REN_DAO
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveSelfBuffEffect(
                "guzhenren:qing_tong_she_li_gu_active_aperture_spark_1",
                cooldownKey("guzhenren:qing_tong_she_li_gu_active_aperture_spark_1"),
                List.of(
                    new RenDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new RenDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerXiWangGu() {
        // 希望蛊：微量“气运/希望”——提升幸运常驻 + 主动短暂护体
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yizhuanrendaoxiwanggu_passive_hope_luck_1",
                DaoHenHelper.DaoType.REN_DAO,
                Attributes.LUCK,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "luck"
            )
        );
        GuEffectRegistry.register(
            new DaoSustainedResourceRegenEffect(
                "guzhenren:yizhuanrendaoxiwanggu_passive_hope_warm_yuan",
                DaoHenHelper.DaoType.REN_DAO,
                false,
                false,
                false,
                true
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveSelfBuffEffect(
                "guzhenren:yizhuanrendaoxiwanggu_active_hope_guard_1",
                cooldownKey("guzhenren:yizhuanrendaoxiwanggu_active_hope_guard_1"),
                List.of(
                    new RenDaoActiveSelfBuffEffect.EffectSpec(
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
