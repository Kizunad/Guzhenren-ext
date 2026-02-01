package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * 反火外壳节点方块。
 * <p>
 * Round 29：挂载在 Anchor 上，为基地提供火焰防护（配合事件监听实现阻止火焰蔓延/点燃）。
 * </p>
 */
public class BastionAntiFireShellBlock extends Block {

    public BastionAntiFireShellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 作为功能节点，不允许路径寻找穿过。
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 必须依附在 Anchor 上。
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof BastionAnchorBlock;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            net.minecraft.core.Direction direction,
            BlockState neighborState,
            net.minecraft.world.level.LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {
        // Anchor 被拆则掉落。
        if (direction == net.minecraft.core.Direction.DOWN && !canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state,
                         net.minecraft.world.level.Level level,
                         BlockPos pos,
                         BlockState newState,
                         boolean isMoving) {
        if (!level.isClientSide
            && state.getBlock() != newState.getBlock()
            && level instanceof ServerLevel serverLevel) {
            // 清理归属缓存，避免悬空引用。
            BastionSavedData savedData = BastionSavedData.get(serverLevel);
            savedData.removeNodeOwnershipIfPresent(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
