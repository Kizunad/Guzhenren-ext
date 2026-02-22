package com.Kizunad.guzhenrenext.xianqiao.alchemy.block;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.blockentity.AlchemyFurnaceBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoBlockEntities;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 炼丹炉方块。
 */
public class AlchemyFurnaceBlock extends BaseEntityBlock {

    /** 方块 Codec。 */
    public static final MapCodec<AlchemyFurnaceBlock> CODEC = simpleCodec(AlchemyFurnaceBlock::new);

    /** 默认方块硬度。 */
    private static final float BLOCK_STRENGTH = 2.5F;

    /** 默认爆炸抗性。 */
    private static final float BLOCK_RESISTANCE = 6.0F;

    public AlchemyFurnaceBlock() {
        this(createDefaultProperties());
    }

    public AlchemyFurnaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static BlockBehaviour.Properties createDefaultProperties() {
        return BlockBehaviour.Properties.of()
            .strength(BLOCK_STRENGTH, BLOCK_RESISTANCE)
            .sound(SoundType.STONE);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        net.minecraft.world.entity.player.Player player,
        BlockHitResult hitResult
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = state.getMenuProvider(level, pos);
            if (provider != null) {
                serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyFurnaceBlockEntity(pos, state);
    }

    /**
     * 方块实体服务端 ticker。
     * <p>
     * 仅在服务端执行转运逻辑，客户端不做任何状态推进。
     * </p>
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<T> blockEntityType
    ) {
        return createTickerHelper(
            blockEntityType,
            XianqiaoBlockEntities.ALCHEMY_FURNACE.get(),
            AlchemyFurnaceBlockEntity::serverTick
        );
    }
}
