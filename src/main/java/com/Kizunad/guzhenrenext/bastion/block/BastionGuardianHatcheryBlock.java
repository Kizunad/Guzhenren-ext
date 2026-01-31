package com.Kizunad.guzhenrenext.bastion.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * 守卫孵化巢（GuardianHatchery）节点方块。
 * <p>
 * Round 4.2 的目标是提供“守卫产出机制地基”，因此该方块遵循与能源节点一致的结构约束：
 * <ul>
 *     <li>必须放置在 Anchor 上方一格（below 必须是 {@link BastionAnchorBlock}）</li>
 *     <li>不引入 BlockEntity（本回合不做持久化状态，冷却等由 SavedData 运行时缓存承担）</li>
 *     <li>具体孵化/扣费/上限逻辑由 {@code BastionHatcheryService} 在 FULL tick 统一驱动</li>
 * </ul>
 * </p>
 */
public class BastionGuardianHatcheryBlock extends Block {

    public BastionGuardianHatcheryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 作为功能节点，不希望被当作可穿行方块。
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 结构约束：孵化巢必须依附在 Anchor 上。
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof BastionAnchorBlock;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {

        // Anchor 被拆：孵化巢应立即掉落（这里直接变为空气，掉落由方块掉落表决定）。
        if (direction == Direction.DOWN && !canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        // 作为“装置”保持完整方块碰撞。
        return true;
    }
}
