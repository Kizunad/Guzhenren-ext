package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 四转养剑葫：主动【剑酒催锋】。
 * <p>
 * 设计目标：提供一个“爆发输出 + 受击卸力”的短时间窗口技能，便于近战与Boss战。</p>
 */
public class YangJianHuSwordWineOverdriveEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:yangjianhu_active_sword_wine_overdrive";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "YangJianHuSwordWineOverdriveCooldownUntilTick";
    private static final String TAG_ACTIVE_UNTIL_TICK =
        "YangJianHuSwordWineOverdriveActiveUntilTick";

    private static final String META_DURATION_TICKS = "duration_ticks";
    private static final String META_BONUS_DAMAGE = "bonus_damage";
    private static final String META_HURT_MULTIPLIER = "hurt_multiplier";
    private static final String META_ACTIVATE_ZHENYUAN_BASE_COST =
        "activate_zhenyuan_base_cost";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_DURATION_TICKS = 240;
    private static final double DEFAULT_BONUS_DAMAGE = 4.0;
    private static final double DEFAULT_HURT_MULTIPLIER = 0.85;
    private static final double DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST = 720.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 620;

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
                Component.literal("剑酒催锋冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final double baseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ACTIVATE_ZHENYUAN_BASE_COST,
                DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST
            )
        );
        final double cost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (cost > 0.0 && !ZhenYuanHelper.hasEnough(user, cost)) {
            player.displayClientMessage(Component.literal("真元不足。"), true);
            return false;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
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

        if (!isActive(attacker)) {
            return damage;
        }

        final double bonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_BONUS_DAMAGE,
                DEFAULT_BONUS_DAMAGE
            )
        );
        return (float) (damage + bonus);
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
        if (!isActive(victim)) {
            return damage;
        }

        final double multiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HURT_MULTIPLIER,
                DEFAULT_HURT_MULTIPLIER
            ),
            0.0,
            1.0
        );
        return (float) (damage * multiplier);
    }

    private static boolean isActive(final LivingEntity user) {
        return user.getPersistentData().getInt(TAG_ACTIVE_UNTIL_TICK) > user.tickCount;
    }
}

