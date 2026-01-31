package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.energy.BastionEnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * 基地能源节点方块（可建造，挂载在 Anchor 上）。
 * <p>
 * Round 3.1 的目标是把“能源节点”从纯扫描缓存推进到玩家可建造的方块。
 * 该方块本身不含 BlockEntity：
 * <ul>
 *     <li>放置约束/扣费/上限/归属由 {@code BastionEnergyBuildService} 在 server 侧校验</li>
 *     <li>能源类型用 BlockState 属性保存（避免额外 Tile/BE 成本）</li>
 *     <li>能源产出仍由 BastionEnergyService 的环境扫描语义兜底（世界变更后可纠正缓存）</li>
 * </ul>
 * </p>
 */
public class BastionEnergyNodeBlock extends Block {

    /** 能源类型属性。 */
    public static final EnumProperty<BastionEnergyType> ENERGY_TYPE = EnumProperty.create(
        "energy_type", BastionEnergyType.class);

    public BastionEnergyNodeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(ENERGY_TYPE, BastionEnergyType.PHOTOSYNTHESIS)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENERGY_TYPE);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 作为功能节点，不希望被当作可穿行方块。
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 结构约束：能源节点必须依附在 Anchor 上。
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

        // Anchor 被拆：能源节点应立即掉落（这里直接变为空气，掉落由方块掉落表决定）。
        if (direction == Direction.DOWN && !canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        // 能源节点本质是“装置”，保持完整方块碰撞。
        return true;
    }
}
