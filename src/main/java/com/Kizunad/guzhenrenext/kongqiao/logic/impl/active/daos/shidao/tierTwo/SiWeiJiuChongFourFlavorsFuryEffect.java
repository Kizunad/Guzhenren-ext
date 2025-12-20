package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 二转四味酒虫：主动【四味催战】。
 * <p>
 * 设计目标：一个“短窗口爆发”的输出技能：
 * 在持续时间内，每次攻击都能打出额外伤害，但会按次消耗真元。
 * </p>
 */
public class SiWeiJiuChongFourFlavorsFuryEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:si_wei_jiu_chong_active_four_flavors_fury";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "SiWeiJiuChongFourFlavorsFuryCooldownUntilTick";
    private static final String TAG_ACTIVE_UNTIL_TICK =
        "SiWeiJiuChongFourFlavorsFuryActiveUntilTick";

    private static final String META_DURATION_TICKS = "duration_ticks";
    private static final String META_BONUS_DAMAGE = "bonus_damage";
    private static final String META_ZHENYUAN_BASE_COST_PER_HIT =
        "zhenyuan_base_cost_per_hit";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_DURATION_TICKS = 240;
    private static final double DEFAULT_BONUS_DAMAGE = 2.0;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_HIT = 120.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 320;

    private static final int HIT_SLOW_TICKS = 30;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            NBT_COOLDOWN_UNTIL_TICK
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("四味催战冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final int durationTicks = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );
        user.getPersistentData()
            .putInt(TAG_ACTIVE_UNTIL_TICK, user.tickCount + durationTicks);

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
                user,
                NBT_COOLDOWN_UNTIL_TICK,
                user.tickCount + cooldownTicks
            );
        }
        return true;
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

        final int until = attacker.getPersistentData()
            .getInt(TAG_ACTIVE_UNTIL_TICK);
        if (until <= attacker.tickCount) {
            return damage;
        }

        final double baseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_HIT,
                DEFAULT_ZHENYUAN_BASE_COST_PER_HIT
            )
        );
        final double cost = ZhenYuanHelper.calculateGuCost(attacker, baseCost);
        if (cost > 0.0 && !ZhenYuanHelper.hasEnough(attacker, cost)) {
            return damage;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(attacker, -cost);
        }

        final double bonusDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BONUS_DAMAGE,
                DEFAULT_BONUS_DAMAGE
            )
        );
        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.SHI_DAO
        );
        if (bonusDamage > 0.0 && target != null && target.isAlive()) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    HIT_SLOW_TICKS,
                    0,
                    true,
                    true
                )
            );
        }
        return (float) (damage + (bonusDamage * multiplier));
    }
}
