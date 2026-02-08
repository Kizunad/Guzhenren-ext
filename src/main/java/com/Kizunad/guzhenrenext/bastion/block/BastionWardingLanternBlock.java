package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.blockentity.BastionWardingLanternBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

/**
 * 镇地灯方块。
 * <p>
 * 该方块使用 {@code hanging} 方块状态驱动模型切换：
 * <ul>
 *   <li>当玩家从方块下表面放置时，设置为悬挂模型；</li>
 *   <li>其他情况使用落地模型。</li>
 * </ul>
 * </p>
 */
public class BastionWardingLanternBlock extends Block implements EntityBlock {

    /**
     * 是否为悬挂状态。
     */
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

    public BastionWardingLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean isHanging = context.getClickedFace() == net.minecraft.core.Direction.DOWN;
        return this.defaultBlockState().setValue(HANGING, isHanging);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BastionWardingLanternBlockEntity(pos, state);
    }
}
