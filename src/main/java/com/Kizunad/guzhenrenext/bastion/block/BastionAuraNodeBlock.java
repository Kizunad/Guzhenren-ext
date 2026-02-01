package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.aura.AuraNodeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * 光环节点方块。
 * <p>
 * Round 5.2：可建造的光环节点，用于承载/触发光环叠加规则。
 * 必须放在 Anchor 上方。
 * </p>
 */
public class BastionAuraNodeBlock extends Block {

    /** 光环类型属性。 */
    public static final EnumProperty<AuraNodeType> AURA_TYPE = EnumProperty.create(
        "aura_type",
        AuraNodeType.class
    );

    public BastionAuraNodeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(AURA_TYPE, AuraNodeType.BUFF)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AURA_TYPE);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 作为功能节点，不希望被当作可穿行方块。
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 结构约束：光环节点必须依附在 Anchor 上。
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

        // Anchor 被拆：光环节点应立即掉落（这里直接变为空气，掉落由方块掉落表决定）。
        if (direction == Direction.DOWN && !canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        // 光环节点作为“装置”保持完整方块碰撞。
        return true;
    }
}
