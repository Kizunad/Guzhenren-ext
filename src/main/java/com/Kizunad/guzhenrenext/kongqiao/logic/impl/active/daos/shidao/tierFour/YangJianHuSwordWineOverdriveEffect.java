package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
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
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_DURATION_TICKS = 240;
    private static final double DEFAULT_BONUS_DAMAGE = 4.0;
    private static final double DEFAULT_HURT_MULTIPLIER = 0.85;
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

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final int baseDurationTicks = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );
        final int durationTicks =
            (int) Math.round(baseDurationTicks * selfMultiplier);
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
        final double multiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.SHI_DAO
        );
        return (float) (damage + (bonus * multiplier));
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

        final double baseMultiplier = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HURT_MULTIPLIER,
                DEFAULT_HURT_MULTIPLIER
            ),
            0.0,
            1.0
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            victim,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double enhancedMultiplier = UsageMetadataHelper.clamp(
            1.0 - ((1.0 - baseMultiplier) * selfMultiplier),
            0.05,
            1.0
        );
        return (float) (damage * enhancedMultiplier);
    }

    private static boolean isActive(final LivingEntity user) {
        return user.getPersistentData().getInt(TAG_ACTIVE_UNTIL_TICK) > user.tickCount;
    }
}
