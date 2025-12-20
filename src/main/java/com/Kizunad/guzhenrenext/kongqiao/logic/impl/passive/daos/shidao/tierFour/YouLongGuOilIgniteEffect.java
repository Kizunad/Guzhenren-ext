package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 四转油龙蛊：被动【油火增伤】。
 * <p>
 * 设计目标：提供“附着 + 状态增伤”的输出逻辑：
 * 普攻为目标附着油火，延长燃烧；目标燃烧时额外增伤。</p>
 */
public class YouLongGuOilIgniteEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:youlonggu_passive_oil_ignite";

    private static final String TAG_OIL_UNTIL_TICK = "YouLongGuOilUntilTick";

    private static final String META_OIL_DURATION_TICKS = "oil_duration_ticks";
    private static final String META_BURN_SECONDS = "burn_seconds";
    private static final String META_BURNING_BONUS_RATIO = "burning_bonus_ratio";

    private static final int DEFAULT_OIL_DURATION_TICKS = 200;
    private static final int DEFAULT_BURN_SECONDS = 4;
    private static final double DEFAULT_BURNING_BONUS_RATIO = 0.20;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }

        if (target == null || !target.isAlive()) {
            return damage;
        }

        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final int oilDuration = Math.min(
            2400,
            (int) Math.round(
                Math.max(
                    1,
                    UsageMetadataHelper.getInt(
                        usageInfo,
                        META_OIL_DURATION_TICKS,
                        DEFAULT_OIL_DURATION_TICKS
                    )
                ) * multiplier
            )
        );
        target.getPersistentData()
            .putInt(TAG_OIL_UNTIL_TICK, target.tickCount + oilDuration);

        final int burnSeconds = Math.min(
            30,
            (int) Math.round(
                Math.max(
                    0,
                    UsageMetadataHelper.getInt(
                        usageInfo,
                        META_BURN_SECONDS,
                        DEFAULT_BURN_SECONDS
                    )
                ) * multiplier
            )
        );
        if (burnSeconds > 0) {
            target.igniteForSeconds(burnSeconds);
        }

        final double bonusRatio = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BURNING_BONUS_RATIO,
                DEFAULT_BURNING_BONUS_RATIO
            ),
            0.0,
            2.0
        );
        if (bonusRatio <= 0.0) {
            return damage;
        }

        final boolean burning = target.isOnFire();
        final boolean oiled = target.getPersistentData().getInt(TAG_OIL_UNTIL_TICK) > target.tickCount;
        if (burning && oiled) {
            if (!GuEffectCostHelper.tryConsumeOnce(null, attacker, usageInfo)) {
                return damage;
            }
            return (float) (damage * (1.0 + (bonusRatio * multiplier)));
        }
        return damage;
    }
}
