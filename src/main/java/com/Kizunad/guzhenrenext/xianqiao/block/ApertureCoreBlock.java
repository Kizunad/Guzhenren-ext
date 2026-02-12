package com.Kizunad.guzhenrenext.xianqiao.block;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 仙窍核心方块。
 * <p>
 * 该方块用于标记仙窍中心，强度与基岩一致，不可正常破坏。
 * 右键时会尝试打开对应的仙窍中枢菜单。
 * </p>
 */
public class ApertureCoreBlock extends BaseEntityBlock {

    /** 方块 Codec，用于方块序列化与反序列化。 */
    public static final MapCodec<ApertureCoreBlock> CODEC = simpleCodec(ApertureCoreBlock::new);

    /**
     * 不可破坏硬度值。
     */
    private static final float UNBREAKABLE_DESTROY_TIME = -1.0F;

    /**
     * 基岩级爆炸抗性。
     */
    private static final float BEDROCK_EXPLOSION_RESISTANCE = 3600000.0F;

    /**
     * 默认构造器，供注册器直接创建方块实例。
     */
    public ApertureCoreBlock() {
        this(createDefaultProperties());
    }

    /**
     * Codec 反序列化路径构造器。
     *
     * @param properties 方块属性
     */
    public ApertureCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * 构建仙窍核心默认属性。
     *
     * @return 默认方块属性
     */
    private static BlockBehaviour.Properties createDefaultProperties() {
        return BlockBehaviour.Properties.of()
            .destroyTime(UNBREAKABLE_DESTROY_TIME)
            .explosionResistance(BEDROCK_EXPLOSION_RESISTANCE);
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            @Nullable BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ApertureCoreBlockEntity apertureCoreBlockEntity) {
                UUID ownerUUID = apertureCoreBlockEntity.getOwnerUUID();
                if (ownerUUID != null && ownerUUID.equals(serverPlayer.getUUID())) {
                    MenuProvider provider = state.getMenuProvider(level, pos);
                    if (provider != null) {
                        serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
                    }
                } else {
                    serverPlayer.sendSystemMessage(
                        Component.translatable("message.guzhenrenext.aperture_core.not_owner")
                    );
                }
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
        return new ApertureCoreBlockEntity(pos, state);
    }
}
