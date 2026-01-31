package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * 基地菌毯方块（贴地蔓延主网）。
 * <p>
 * 设计目标：
 * <ul>
 *   <li>无 BlockEntity，性能友好</li>
 *   <li>硬度低、易清理</li>
 *   <li>不直接触发节点掉落/威胁事件（这些由 Anchor 节点承担）</li>
 * </ul>
 * </p>
 */
public class BastionMyceliumBlock extends Block {

    /** 查找归属基地的最大搜索半径（与交互/Anchor 逻辑对齐）。 */
    private static final int MAX_OWNER_SEARCH_RADIUS = 128;

    public BastionMyceliumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // 仅在服务端且方块实际被替换/移除时处理。
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                handleMyceliumRemoved(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * 菌毯被移除时的最小化缓存修复。
     * <p>
     * 目的：确保连通性/衰败逻辑不会被“缓存撒谎”误导。
     * </p>
     */
    private static void handleMyceliumRemoved(ServerLevel level, BlockPos pos) {
        BastionSavedData savedData = BastionSavedData.get(level);

        // 在半径内寻找归属基地：该方法本身就是设计上的“非重叠约束”入口。
        BastionData owner = savedData.findOwnerBastion(pos, MAX_OWNER_SEARCH_RADIUS);
        if (owner == null) {
            return;
        }

        // 从缓存中移除节点（若本来不在缓存，该调用为幂等）。
        savedData.removeNodeFromCache(owner.id(), pos);

        // 清理运行时衰败状态，防止 map 残留。
        savedData.clearMyceliumDecay(owner.id(), pos);

        // 计数：菌毯属于 counts.totalMycelium。
        // 注意：这里不尝试修正 totalNodes/nodesByTier（它们当前用于核心/旧机制）。
        BastionData updated = owner.withMyceliumCountDelta(-1);
        savedData.updateBastion(updated);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 允许寻路穿过（类似地毯/苔藓的“地表覆盖物”体验）。
        return true;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState state) {
        // 允许在其上方复活判定更宽松（与“覆盖物”定位一致）。
        return true;
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        // 避免当作完整方块影响碰撞/渲染判断。
        return false;
    }
}
