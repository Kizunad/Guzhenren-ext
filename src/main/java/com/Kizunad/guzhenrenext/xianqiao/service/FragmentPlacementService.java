package com.Kizunad.guzhenrenext.xianqiao.service;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.runtime.FragmentExpansionPolicy;
import com.Kizunad.guzhenrenext.xianqiao.runtime.FragmentExpansionPolicy.HorizontalDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

/**
 * 九天碎片放置服务。
 * <p>
 * 该服务负责将碎片沿玩家朝向投放到当前仙窍边界外，
 * 并完成基础地形搭建、领土 chunk 边界扩张以及初始土道灵气注入。
 * </p>
 */
public final class FragmentPlacementService {

    /** Minecraft 单个 chunk 的方块边长。 */
    private static final int CHUNK_BLOCK_SIZE = 16;

    /** 每次使用碎片时，边界四向统一扩张的 chunk 数。 */
    public static final int BOUNDARY_CHUNK_INCREMENT = FragmentExpansionPolicy.V1_SYMMETRIC_CHUNK_DELTA;

    /** 平台半边长度（5x5 平台对应半边 2）。 */
    public static final int PLATFORM_HALF_SIZE = 2;

    /** 平台相对中心的 Y 偏移（中心下方一层）。 */
    private static final int PLATFORM_Y_OFFSET = -1;

    /** 方块更新标志（更新客户端与邻接方块）。 */
    private static final int BLOCK_UPDATE_FLAGS = 3;

    /** 新区域初始土道灵气值。 */
    public static final int INITIAL_AURA_AMOUNT = 200;

    private FragmentPlacementService() {
    }

    /**
     * 执行九天碎片放置与领土扩张。
     *
     * @param level 仙窍维度服务端世界
     * @param player 使用碎片的玩家
     * @param info 玩家当前仙窍信息
     * @return 放置成功返回 true，否则返回 false
     */
    public static boolean placeFragment(ServerLevel level, Player player, ApertureInfo info) {
        Direction direction = player.getDirection();
        BlockPos targetPos = resolvePlacementTarget(info, direction);

        buildStonePlatform(level, targetPos);

        ApertureWorldData worldData = ApertureWorldData.get(level);
        FragmentExpansionPolicy.applySymmetricExpansion(worldData, player.getUUID());
        DaoMarkApi.addAura(level, targetPos, DaoType.EARTH, INITIAL_AURA_AMOUNT);
        player.sendSystemMessage(Component.literal("九天碎片放置成功，" + FragmentExpansionPolicy.successSummary()));
        return true;
    }

    /**
     * 计算九天碎片的目标放置点。
     * <p>
     * 目标点与真实扩张语义保持一致：
     * 每次扩张都会让 min/max chunk 边界四向各 +1 chunk，
     * 因此放置点应位于“当前边界再向朝向外侧推进 1 chunk”处。
     * </p>
     *
     * @param info 当前仙窍边界信息
     * @param direction 玩家朝向
     * @return 与本次扩张行为对齐的放置目标坐标
     */
    public static BlockPos resolvePlacementTarget(ApertureInfo info, Direction direction) {
        int centerChunkX = Math.floorDiv(info.center().getX(), CHUNK_BLOCK_SIZE);
        int centerChunkZ = Math.floorDiv(info.center().getZ(), CHUNK_BLOCK_SIZE);
        HorizontalDirection horizontalDirection = FragmentExpansionPolicy.resolveHorizontalDirection(
            direction.getStepX(),
            direction.getStepZ()
        );
        int placementDistance = FragmentExpansionPolicy.resolvePlacementDistanceChunks(
            centerChunkX,
            centerChunkZ,
            info.minChunkX(),
            info.maxChunkX(),
            info.minChunkZ(),
            info.maxChunkZ(),
            horizontalDirection
        ) * CHUNK_BLOCK_SIZE;

        return info.center().offset(
            direction.getStepX() * placementDistance,
            0,
            direction.getStepZ() * placementDistance
        );
    }

    /**
     * 在目标位置下方生成 5x5 石头平台。
     *
     * @param level 服务器世界
     * @param center 平台中心点
     */
    private static void buildStonePlatform(ServerLevel level, BlockPos center) {
        int platformY = center.getY() + PLATFORM_Y_OFFSET;
        for (int xOffset = -PLATFORM_HALF_SIZE; xOffset <= PLATFORM_HALF_SIZE; xOffset++) {
            for (int zOffset = -PLATFORM_HALF_SIZE; zOffset <= PLATFORM_HALF_SIZE; zOffset++) {
                BlockPos placePos = new BlockPos(center.getX() + xOffset, platformY, center.getZ() + zOffset);
                level.setBlock(placePos, Blocks.STONE.defaultBlockState(), BLOCK_UPDATE_FLAGS);
            }
        }
    }
}
