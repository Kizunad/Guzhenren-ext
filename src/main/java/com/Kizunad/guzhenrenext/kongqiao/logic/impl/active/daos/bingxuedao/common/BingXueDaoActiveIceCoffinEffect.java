package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 冰雪道主动：蓝鸟冰棺（锁定目标并冻结封印）。
 */
public class BingXueDaoActiveIceCoffinEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_FREEZE_TICKS = "freeze_ticks";
    private static final String META_SLOW_DURATION_TICKS = "slowness_duration_ticks";
    private static final String META_SLOW_AMPLIFIER = "slowness_amplifier";
    private static final String META_WEAKNESS_DURATION_TICKS = "weakness_duration_ticks";
    private static final String META_WEAKNESS_AMPLIFIER = "weakness_amplifier";
    private static final String META_MAGIC_DAMAGE = "magic_damage";

    private static final int DEFAULT_COOLDOWN_TICKS = 260;
    private static final double DEFAULT_RANGE = 12.0;
    private static final int DEFAULT_FREEZE_TICKS = 200;
    private static final int DEFAULT_SLOW_DURATION_TICKS = 80;
    private static final int DEFAULT_SLOW_AMPLIFIER = 2;
    private static final int DEFAULT_WEAKNESS_DURATION_TICKS = 80;
    private static final int DEFAULT_WEAKNESS_AMPLIFIER = 0;

    private final String usageId;
    private final String nbtCooldownKey;

    public BingXueDaoActiveIceCoffinEffect(
        final String usageId,
        final String nbtCooldownKey
    ) {
        this.usageId = usageId;
        this.nbtCooldownKey = nbtCooldownKey;
    }

    @Override
    public String getUsageId() {
        return usageId;
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
            nbtCooldownKey
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final LivingEntity target = findTarget(player, usageInfo);
        if (target == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        applyIceCoffin(user, target, usageInfo);
        applyCooldown(user, usageInfo);

        return true;
    }

    private static LivingEntity findTarget(
        final ServerPlayer player,
        final NianTouData.Usage usageInfo
    ) {
        final double range = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        return LineOfSightTargetHelper.findTarget(player, range);
    }

    private static void applyIceCoffin(
        final LivingEntity user,
        final LivingEntity target,
        final NianTouData.Usage usageInfo
    ) {
        final int freezeTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_FREEZE_TICKS,
                DEFAULT_FREEZE_TICKS
            )
        );
        if (freezeTicks > 0) {
            target.setTicksFrozen(target.getTicksFrozen() + freezeTicks);
        }

        final int slownessDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SLOW_DURATION_TICKS,
                DEFAULT_SLOW_DURATION_TICKS
            )
        );
        final int slownessAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SLOW_AMPLIFIER,
                DEFAULT_SLOW_AMPLIFIER
            )
        );
        if (slownessDuration > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slownessDuration,
                    slownessAmplifier,
                    true,
                    true
                )
            );
        }

        final int weaknessDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_WEAKNESS_DURATION_TICKS,
                DEFAULT_WEAKNESS_DURATION_TICKS
            )
        );
        final int weaknessAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_WEAKNESS_AMPLIFIER,
                DEFAULT_WEAKNESS_AMPLIFIER
            )
        );
        if (weaknessDuration > 0) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    weaknessDuration,
                    weaknessAmplifier,
                    true,
                    true
                )
            );
        }

        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_MAGIC_DAMAGE, 0.0)
        );
        if (baseDamage <= 0.0) {
            return;
        }
        final double multiplier = DaoHenCalculator.calculateMultiplier(
            user,
            target,
            DaoHenHelper.DaoType.BING_XUE_DAO
        );
        target.hurt(
            user.damageSources().mobAttack(user),
            (float) (baseDamage * multiplier)
        );
    }

    private void applyCooldown(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
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
                nbtCooldownKey,
                user.tickCount + cooldownTicks
            );
        }
    }
}
