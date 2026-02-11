package com.Kizunad.guzhenrenext.xianqiao.resource;

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
 * 资源点控制器方块。
 * <p>
 * 右键打开资源控制器 GUI；其核心逻辑由对应方块实体执行。
 * </p>
 */
public class ResourceControllerBlock extends BaseEntityBlock {

    /** 方块 Codec，用于方块序列化与反序列化。 */
    public static final MapCodec<ResourceControllerBlock> CODEC = simpleCodec(ResourceControllerBlock::new);

    /** 方块硬度。 */
    private static final float BLOCK_STRENGTH = 3.0F;

    /** 爆炸抗性。 */
    private static final float BLOCK_RESISTANCE = 8.0F;

    /**
     * 默认构造器，供业务代码直接实例化。
     */
    public ResourceControllerBlock() {
        this(createDefaultProperties());
    }

    /**
     * Codec 反序列化路径构造器。
     *
     * @param properties 方块属性
     */
    public ResourceControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * 构建资源控制器默认方块属性。
     */
    private static BlockBehaviour.Properties createDefaultProperties() {
        return BlockBehaviour.Properties.of()
            .strength(BLOCK_STRENGTH, BLOCK_RESISTANCE)
            .sound(SoundType.METAL);
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
        return new ResourceControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<T> blockEntityType
    ) {
        return createTickerHelper(
            blockEntityType,
            XianqiaoBlockEntities.RESOURCE_CONTROLLER.get(),
            ResourceControllerBlockEntity::serverTick
        );
    }
}
