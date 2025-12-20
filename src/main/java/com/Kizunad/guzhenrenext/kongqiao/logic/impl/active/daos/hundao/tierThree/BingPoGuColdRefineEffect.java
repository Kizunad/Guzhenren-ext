package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 三转冰魄蛊：主动【寒魄凝炼】。
 * <p>
 * 功能性定位：消耗真元，将周身寒意化作魂魄补充，并压制冻伤（降低冻结进度）。
 * 若处于寒冷环境或自身已被冻伤，则凝炼效率更高。
 * </p>
 */
public class BingPoGuColdRefineEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:bingpogu_active_cold_refine";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "BingPoGuColdRefineCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;

    private static final double DEFAULT_BASE_HUNPO_GAIN = 8.0;
    private static final double DEFAULT_BONUS_HUNPO_GAIN = 10.0;
    private static final int DEFAULT_CLEAR_FROZEN_TICKS = 120;
    private static final int DEFAULT_COOLDOWN_TICKS = 240;

    private static final int PARTICLE_COUNT = 18;
    private static final double PARTICLE_SPREAD = 0.35;
    private static final double PARTICLE_SPEED = 0.02;
    private static final double PARTICLE_Y_FACTOR = 0.6;

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
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        final int currentTick = user.tickCount;
        final int cooldownUntil = getCooldownUntilTick(user);
        if (cooldownUntil > currentTick) {
            final int remain = cooldownUntil - currentTick;
            player.displayClientMessage(
                Component.literal(
                    "寒魄凝炼冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final int cooldownTicks = UsageMetadataHelper.getInt(
            usageInfo,
            "cooldown_ticks",
            DEFAULT_COOLDOWN_TICKS
        );
        if (cooldownTicks > 0) {
            setCooldownUntilTick(user, currentTick + cooldownTicks);
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final boolean isColdContext = isColdContext(user);
        final double baseGain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                "base_hunpo_gain",
                DEFAULT_BASE_HUNPO_GAIN
            )
        );
        final double bonusGain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                "bonus_hunpo_gain",
                DEFAULT_BONUS_HUNPO_GAIN
            )
        );
        final double gain =
            (baseGain + (isColdContext ? bonusGain : 0.0)) * selfMultiplier;
        if (gain > 0.0) {
            HunPoHelper.modify(user, gain);
        }

        final int clearFrozenTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                "clear_frozen_ticks",
                DEFAULT_CLEAR_FROZEN_TICKS
            )
        );
        if (clearFrozenTicks > 0) {
            final int currentFrozen = user.getTicksFrozen();
            user.setTicksFrozen(Math.max(0, currentFrozen - clearFrozenTicks));
        }

        spawnParticles(serverLevel, user);
        return true;
    }

    private static boolean isColdContext(final LivingEntity user) {
        if (user.getTicksFrozen() > 0 || user.isFullyFrozen()) {
            return true;
        }

        final BlockPos pos = user.blockPosition();
        final BlockState state = user.level().getBlockState(pos);
        final BlockState below = user.level().getBlockState(pos.below());
        return isColdBlock(state) || isColdBlock(below);
    }

    private static boolean isColdBlock(final BlockState state) {
        return state.is(Blocks.POWDER_SNOW)
            || state.is(Blocks.SNOW)
            || state.is(Blocks.SNOW_BLOCK)
            || state.is(Blocks.ICE)
            || state.is(Blocks.PACKED_ICE)
            || state.is(Blocks.BLUE_ICE)
            || state.is(Blocks.FROSTED_ICE);
    }

    private static void spawnParticles(
        final ServerLevel level,
        final LivingEntity user
    ) {
        level.sendParticles(
            ParticleTypes.SNOWFLAKE,
            user.getX(),
            user.getY() + user.getBbHeight() * PARTICLE_Y_FACTOR,
            user.getZ(),
            PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
        level.sendParticles(
            ParticleTypes.SOUL,
            user.getX(),
            user.getY() + user.getBbHeight() * PARTICLE_Y_FACTOR,
            user.getZ(),
            PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
    }

    private static int getCooldownUntilTick(final LivingEntity user) {
        return user.getPersistentData().getInt(NBT_COOLDOWN_UNTIL_TICK);
    }

    private static void setCooldownUntilTick(
        final LivingEntity user,
        final int untilTick
    ) {
        user.getPersistentData().putInt(NBT_COOLDOWN_UNTIL_TICK, untilTick);
    }
}
