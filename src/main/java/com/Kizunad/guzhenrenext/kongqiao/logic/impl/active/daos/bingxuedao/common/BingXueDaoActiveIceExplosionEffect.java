package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.LineOfSightTargetHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 冰雪道主动：冰爆（以锁定目标为中心，根据周围冰雪环境增强爆发伤害）。
 * <p>
 * 注意：此效果不破坏方块，仅读取环境并对生物造成魔法伤害。
 * </p>
 */
public class BingXueDaoActiveIceExplosionEffect implements IGuEffect {

    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";
    private static final String META_RANGE = "range";
    private static final String META_RADIUS = "radius";
    private static final String META_BASE_MAGIC_DAMAGE = "base_magic_damage";
    private static final String META_PER_ICE_BLOCK_BONUS = "per_ice_block_bonus";
    private static final String META_MAX_ICE_BLOCKS = "max_ice_blocks";
    private static final String META_SCAN_RADIUS = "scan_radius";
    private static final String META_FREEZE_TICKS = "freeze_ticks";

    private static final int DEFAULT_COOLDOWN_TICKS = 240;
    private static final double DEFAULT_RANGE = 12.0;
    private static final double DEFAULT_RADIUS = 4.0;
    private static final int DEFAULT_MAX_ICE_BLOCKS = 64;
    private static final int DEFAULT_SCAN_RADIUS = 4;
    private static final int DEFAULT_FREEZE_TICKS = 80;

    private final String usageId;
    private final String nbtCooldownKey;

    public BingXueDaoActiveIceExplosionEffect(
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

        final double range = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RANGE, DEFAULT_RANGE)
        );
        final LivingEntity target = LineOfSightTargetHelper.findTarget(player, range);
        if (target == null) {
            player.displayClientMessage(Component.literal("未找到可锁定目标。"), true);
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double radius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final int scanRadius = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SCAN_RADIUS,
                DEFAULT_SCAN_RADIUS
            )
        );
        final int maxIceBlocks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_MAX_ICE_BLOCKS,
                DEFAULT_MAX_ICE_BLOCKS
            )
        );

        final int iceBlocks = countIceBlocksAround(
            target.blockPosition(),
            scanRadius,
            maxIceBlocks,
            user
        );
        final double baseDamage = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_BASE_MAGIC_DAMAGE, 0.0)
        );
        final double perIceBonus = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_PER_ICE_BLOCK_BONUS, 0.0)
        );
        final double rawDamage = baseDamage + perIceBonus * iceBlocks;

        final int freezeTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_FREEZE_TICKS,
                DEFAULT_FREEZE_TICKS
            )
        );

        final AABB area = new AABB(target.blockPosition()).inflate(radius);
        final List<LivingEntity> victims = user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );

        for (LivingEntity victim : victims) {
            if (rawDamage > 0.0) {
                final double multiplier = DaoHenCalculator.calculateMultiplier(
                    user,
                    victim,
                    DaoHenHelper.DaoType.BING_XUE_DAO
                );
                victim.hurt(
                    user.damageSources().mobAttack(user),
                    (float) (rawDamage * multiplier)
                );
            }
            if (freezeTicks > 0) {
                victim.setTicksFrozen(victim.getTicksFrozen() + freezeTicks);
            }
        }

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

        return true;
    }

    private static int countIceBlocksAround(
        final BlockPos center,
        final int radius,
        final int maxCount,
        final LivingEntity user
    ) {
        if (center == null || user == null || radius <= 0 || maxCount <= 0) {
            return 0;
        }
        int count = 0;
        final int minX = center.getX() - radius;
        final int minY = center.getY() - radius;
        final int minZ = center.getZ() - radius;
        final int maxX = center.getX() + radius;
        final int maxY = center.getY() + radius;
        final int maxZ = center.getZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (count >= maxCount) {
                        return count;
                    }
                    final BlockState state = user.level().getBlockState(
                        new BlockPos(x, y, z)
                    );
                    if (isIceLike(state)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static boolean isIceLike(final BlockState state) {
        if (state == null) {
            return false;
        }
        final Block block = state.getBlock();
        return block == Blocks.ICE
            || block == Blocks.PACKED_ICE
            || block == Blocks.BLUE_ICE
            || block == Blocks.FROSTED_ICE
            || block == Blocks.SNOW_BLOCK
            || block == Blocks.POWDER_SNOW;
    }
}
