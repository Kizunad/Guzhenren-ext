package com.Kizunad.guzhenrenext.bastion.item;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 封印道具：用于临时封印玩家面前的基地核心。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>仅服务端执行核心逻辑，避免客户端崩溃。</li>
 *     <li>射线检测玩家视线前方的基地方块。</li>
 *     <li>找到基地后调用封印服务处理封印流程。</li>
 *     <li>非创造模式消耗自身。</li>
 *     <li>使用常量避免 Magic Number，行长不超过 120。</li>
 * </ul>
 * </p>
 */
public class BastionSealItem extends Item {

    /** 方块中心偏移量。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5d;

    /** 允许的最大封印距离（方块单位）。 */
    private static final double MAX_SEAL_RANGE = 20.0d;

    /** 查找归属基地的搜索半径。 */
    private static final int SEARCH_RADIUS = 128;

    /** 封印基础时长（秒）。 */
    private static final int BASE_SEAL_DURATION_SECONDS = 60;

    /** 每秒 tick 数。 */
    private static final long TICKS_PER_SECOND = 20L;

    /** UUID 显示截取长度。 */
    private static final int UUID_DISPLAY_LENGTH = 8;

    public BastionSealItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(serverLevel, serverPlayer, ClipContext.Fluid.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos hitPos = hitResult.getBlockPos();

        if (!isWithinRange(serverPlayer, hitPos)) {
            return InteractionResultHolder.pass(stack);
        }

        BastionSavedData savedData = BastionSavedData.get(serverLevel);
        BastionData bastion = findTargetBastion(serverLevel, savedData, hitPos);

        if (bastion == null) {
            serverPlayer.sendSystemMessage(Component.literal("§c附近未发现可封印的基地"));
            return InteractionResultHolder.fail(stack);
        }

        BlockState state = serverLevel.getBlockState(hitPos);
        if (!isBastionBlock(state)) {
            return InteractionResultHolder.pass(stack);
        }

        InteractionResultHolder<ItemStack> result = handleSeal(serverLevel, serverPlayer, bastion, stack);
        return result;
    }

    /**
     * 处理封印逻辑：校验状态、应用封印、反馈效果。
     */
    private InteractionResultHolder<ItemStack> handleSeal(
            ServerLevel level,
            ServerPlayer player,
            BastionData bastion,
            ItemStack stack) {
        long gameTime = level.getGameTime();
        BastionState state = bastion.getEffectiveState(gameTime);

        if (state == BastionState.SEALED) {
            long remainingTicks = bastion.sealedUntilGameTime() - gameTime;
            long remainingSeconds = remainingTicks / TICKS_PER_SECOND;
            player.sendSystemMessage(Component.literal(
                String.format("§e基地已被封印，剩余 %d 秒", remainingSeconds)
            ));
            return InteractionResultHolder.fail(stack);
        }

        if (state == BastionState.DESTROYED) {
            player.sendSystemMessage(Component.literal("§c基地已被摧毁，无法封印"));
            return InteractionResultHolder.fail(stack);
        }

        long sealDuration = (BASE_SEAL_DURATION_SECONDS * TICKS_PER_SECOND) / bastion.tier();
        long sealUntil = gameTime + sealDuration;

        BastionSavedData savedData = BastionSavedData.get(level);
        savedData.applySeal(bastion.id(), sealUntil);

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        long durationSeconds = sealDuration / TICKS_PER_SECOND;
        player.sendSystemMessage(Component.literal(
            String.format("§a成功封印基地 %s，持续 %d 秒",
                bastion.id().toString().substring(0, UUID_DISPLAY_LENGTH),
                durationSeconds)
        ));

        BastionSoundPlayer.playSeal(level, bastion.corePos());
        BastionParticles.spawnSealParticles(level, bastion.corePos());

        BastionData sealed = savedData.getBastion(bastion.id());
        if (sealed != null) {
            BastionNetworkHandler.syncToNearbyPlayers(level, sealed);
        }

        return InteractionResultHolder.consume(stack);
    }

    /**
     * 判断目标方块是否在允许射线范围内。
     */
    private boolean isWithinRange(ServerPlayer player, BlockPos pos) {
        double distanceSqr = player.distanceToSqr(
            pos.getX() + BLOCK_CENTER_OFFSET,
            pos.getY() + BLOCK_CENTER_OFFSET,
            pos.getZ() + BLOCK_CENTER_OFFSET
        );
        return distanceSqr <= MAX_SEAL_RANGE * MAX_SEAL_RANGE;
    }

    /**
     * 查找命中的方块所属基地。
     */
    private BastionData findTargetBastion(ServerLevel level, BastionSavedData savedData, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BastionCoreBlock) {
            return savedData.findByCorePos(pos);
        }
        return savedData.findOwnerBastion(pos, SEARCH_RADIUS);
    }

    /**
     * 判定是否为基地相关方块：核心或锚点。
     */
    private boolean isBastionBlock(BlockState state) {
        return state.getBlock() instanceof BastionCoreBlock
            || state.getBlock() instanceof BastionAnchorBlock;
    }
}
