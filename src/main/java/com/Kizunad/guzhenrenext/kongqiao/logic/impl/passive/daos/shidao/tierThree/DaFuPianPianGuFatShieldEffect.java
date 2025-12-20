package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 三转大腹便便蛊：被动【厚脂护体】。
 * <p>
 * 设计目标：提供一个“用饱食换减伤”的防守机制，强调食道的资源转化。
 * </p>
 */
public class DaFuPianPianGuFatShieldEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:dafupianpiangu_passive_fat_shield";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "DaFuPianPianGuFatShieldCooldownUntilTick";

    private static final String META_REDUCTION_RATIO = "reduction_ratio";
    private static final String META_HUNGER_COST = "hunger_cost";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_REDUCTION_RATIO = 0.25;
    private static final int DEFAULT_HUNGER_COST = 1;
    private static final int DEFAULT_COOLDOWN_TICKS = 20;

    private static final float EXHAUSTION_PER_HUNGER = 4.0F;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }

        if (!(victim instanceof Player player)) {
            return damage;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            victim,
            NBT_COOLDOWN_UNTIL_TICK
        );
        if (remain > 0) {
            return damage;
        }

        final int hungerCost = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_HUNGER_COST,
                DEFAULT_HUNGER_COST
            )
        );
        if (hungerCost > 0 && player.getFoodData().getFoodLevel() < hungerCost) {
            return damage;
        }
        if (hungerCost > 0) {
            player.causeFoodExhaustion(hungerCost * EXHAUSTION_PER_HUNGER);
        }

        if (!GuEffectCostHelper.tryConsumeOnce(null, victim, usageInfo)) {
            return damage;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double baseRatio = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_REDUCTION_RATIO,
                DEFAULT_REDUCTION_RATIO
            ),
            0.0,
            0.90
        );
        final double ratio = UsageMetadataHelper.clamp(
            baseRatio * multiplier,
            0.0,
            0.90
        );

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                victim,
                NBT_COOLDOWN_UNTIL_TICK,
                victim.tickCount + cooldownTicks
            );
        }

        return (float) (damage * (1.0 - ratio));
    }
}
