package com.Kizunad.guzhenrenext.xianqiao.service;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.runtime.FragmentExpansionPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public final class FragmentPlacementService {

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
        player.sendSystemMessage(
            Component.literal("九天碎片放置成功，已在朝向外缘投放；仙窍边界向四周各扩张 1 个区块。")
        );
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
        return FragmentExpansionPolicy.resolvePlacementTarget(info, direction);
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
