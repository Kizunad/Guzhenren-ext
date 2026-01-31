package com.Kizunad.guzhenrenext.worldgen;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 遗迹自动激活器。
 * <p>
 * 目标：让“自然生成遗迹”在生成后自动转化为 Bastion（写入 SavedData 并开始运作），
 * 但避免在 worldgen 阶段直接写入 SavedData 造成卡顿/超时。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionRuinAutoActivator {

    private BastionRuinAutoActivator() {
    }

    private static final class Constants {
        /** 每 5 秒尝试激活一个。 */
        static final int INTERVAL_TICKS = 100;

        /** 遗迹节点与核心的偏移（与 BastionRuinFeature 保持一致）。 */
        static final int RUIN_NODE_OFFSET = 2;

        /** Chunk 位移量（16 = 2^4）。 */
        static final int CHUNK_BITS = 4;

        private Constants() {
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % Constants.INTERVAL_TICKS != 0) {
            return;
        }

        BastionRuinLocator.pollActivation().ifPresent(record -> {
            ServerLevel level = event.getServer().getLevel(record.dimension());
            if (level == null) {
                return;
            }
            tryActivate(level, record.pos());
        });
    }

    private static void tryActivate(ServerLevel level, BlockPos corePos) {
        if (!level.hasChunk(corePos.getX() >> Constants.CHUNK_BITS, corePos.getZ() >> Constants.CHUNK_BITS)) {
            // 该核心所在 chunk 尚未加载，延后再试
            BastionRuinLocator.enqueueActivation(level.dimension(), corePos);
            return;
        }

        BlockState coreState = level.getBlockState(corePos);
        if (!(coreState.getBlock() instanceof BastionCoreBlock)) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData existing = savedData.findByCorePos(corePos);
        if (existing != null) {
            return;
        }

        BastionDao dao = coreState.getValue(BastionCoreBlock.DAO);
        int tier = BastionCoreBlock.getTier(coreState);
        String bastionType = BastionTypeManager.getByDao(dao).id();

        BastionData created = BastionData.create(
            corePos,
            level.dimension(),
            bastionType,
            dao,
            level.getGameTime()
        );
        created = created.withEvolution(created.evolutionProgress(), tier);
        savedData.addBastion(created);
        savedData.initializeFrontierFromCore(created.id(), corePos);

        // 写入遗迹节点到缓存
        for (Direction dir : new Direction[] {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
        }) {
            BlockPos nodePos = corePos.relative(dir, Constants.RUIN_NODE_OFFSET);
            BlockState nodeState = level.getBlockState(nodePos);
            if (nodeState.getBlock() instanceof BastionAnchorBlock
                && nodeState.getValue(BastionAnchorBlock.GENERATED)) {
                savedData.addNodeToCache(created.id(), nodePos);
            }
        }

        BastionNetworkHandler.syncToNearbyPlayers(level, created);
    }
}
