package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.block.BastionAntiFireShellBlock;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * 反火外壳服务：为拥有反火节点的基地提供火焰保护。
 * <p>
 * 监听方块放置与邻接更新，在基地范围内阻止火焰点燃与蔓延。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionAntiFireService {

    /**
     * 火焰归属搜索半径兜底值。
     * <p>
     * 取 512 覆盖高转/扩张后的基地范围，避免因半径不足漏判。
     * </p>
     */
    private static final int SEARCH_RADIUS = 512;

    private BastionAntiFireService() {
        // 工具类
    }

    /**
     * 阻止火焰在基地范围内传播（邻接通知阶段）。
     */
    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockState state = event.getState();
        if (!isFireBlock(state)) {
            return;
        }

        if (shouldBlockFire(level, event.getPos())) {
            // 取消邻接通知，避免继续蔓延，并顺便熄灭当前火焰。
            event.setCanceled(true);
            level.removeBlock(event.getPos(), false);
        }
    }

    /**
     * 阻止火焰被点燃（玩家/实体/机制放置）。
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!isFireBlock(event.getPlacedBlock())) {
            return;
        }

        if (shouldBlockFire(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    private static boolean isFireBlock(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.FIRE || block == Blocks.SOUL_FIRE;
    }

    private static boolean shouldBlockFire(ServerLevel level, BlockPos pos) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.findOwnerBastion(pos, SEARCH_RADIUS);
        if (bastion == null || bastion.state() != BastionState.ACTIVE) {
            return false;
        }

        Map<UUID, Boolean> cache = new HashMap<>();
        boolean hasShield = cache.computeIfAbsent(
            bastion.id(),
            id -> hasAntiFireShell(level, savedData, bastion)
        );
        if (!hasShield) {
            return false;
        }

        int auraRadius = bastion.getAuraRadius();
        if (auraRadius <= 0) {
            return false;
        }
        long radiusSq = (long) auraRadius * auraRadius;
        double distSq = pos.distSqr(bastion.corePos());
        return distSq <= (double) radiusSq;
    }

    private static boolean hasAntiFireShell(ServerLevel level, BastionSavedData savedData, BastionData bastion) {
        java.util.Set<BlockPos> anchors = savedData.getAnchorCache(bastion.id());
        if (anchors == null || anchors.isEmpty()) {
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (BlockPos anchor : anchors) {
            // 反火节点挂载在 Anchor 上方一格。
            cursor.set(anchor.getX(), anchor.getY() + 1, anchor.getZ());
            Block block = level.getBlockState(cursor).getBlock();
            if (block instanceof BastionAntiFireShellBlock) {
                return true;
            }
        }
        return false;
    }
}
