package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveAreaFieldEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoSustainedAreaDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoSustainedResourceRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 一转毒道。
 */
public final class TierOneDuDaoRegistry {

    private TierOneDuDaoRegistry() {}

    public static void registerAll() {
        registerChouPiGu();
        registerChouPiFeiChong();
    }

    private static void registerChouPiGu() {
        // 臭屁蛊：恶臭缠身（持续毒雾）+ 毒息归元（持续回资源）+ 臭气外放（地面领域）
        GuEffectRegistry.register(
            new DuDaoSustainedAreaDebuffEffect(
                "guzhenren:chou_pi_gu_passive_stink_aura",
                List.of(
                    MobEffects.CONFUSION,
                    MobEffects.WEAKNESS,
                    MobEffects.MOVEMENT_SLOWDOWN
                )
            )
        );
        GuEffectRegistry.register(
            new DuDaoSustainedResourceRegenEffect(
                "guzhenren:chou_pi_gu_passive_poison_breath_refine"
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveAreaFieldEffect(
                "guzhenren:chou_pi_gu_active_stink_field",
                cooldownKey("guzhenren:chou_pi_gu_active_stink_field"),
                List.of(
                    MobEffects.CONFUSION,
                    MobEffects.WEAKNESS,
                    MobEffects.MOVEMENT_SLOWDOWN
                )
            )
        );
    }

    private static void registerChouPiFeiChong() {
        // 臭屁肥虫：臭肥侵染（攻触发）+ 毒雾震荡（周身爆发）
        GuEffectRegistry.register(
            new DuDaoAttackProcDebuffEffect(
                "guzhenren:chou_pi_fei_chong_passive_fat_stink",
                List.of(MobEffects.WEAKNESS)
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveAoEBurstEffect(
                "guzhenren:chou_pi_fei_chong_active_fat_stink_burst",
                cooldownKey("guzhenren:chou_pi_fei_chong_active_fat_stink_burst"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
