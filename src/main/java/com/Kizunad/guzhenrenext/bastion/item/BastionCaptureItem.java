package com.Kizunad.guzhenrenext.bastion.item;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import com.Kizunad.guzhenrenext.bastion.service.BastionCaptureService;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 占领道具：用于在满足条件时占领玩家面前的基地。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>仅服务端执行核心逻辑，避免客户端崩溃。</li>
 *     <li>射线检测玩家视线前方的基地方块。</li>
 *     <li>基地需处于 SEALED/DESTROYED 或 captureState.capturable() 为 true。</li>
 *     <li>调用占领服务完成占领流程，非创造模式消耗自身。</li>
 *     <li>使用常量避免 Magic Number，行长不超过 120。</li>
 * </ul>
 * </p>
 */
public class BastionCaptureItem extends Item {

    /** 方块中心偏移量。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5d;

    /** 允许的最大占领距离（方块单位）。 */
    private static final double MAX_CAPTURE_RANGE = 20.0d;

    /** 查找归属基地的搜索半径。 */
    private static final int SEARCH_RADIUS = 128;

    /** UUID 显示截取长度。 */
    private static final int UUID_DISPLAY_LENGTH = 8;

    /** 占领基地所需念头。与服务端消耗保持一致。 */
    private static final double CAPTURE_NIANTOU_COST = 100.0d;

    public BastionCaptureItem(Properties properties) {
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
            serverPlayer.sendSystemMessage(Component.literal("§c附近未发现可占领的基地"));
            return InteractionResultHolder.fail(stack);
        }

        BlockState state = serverLevel.getBlockState(hitPos);
        if (!isBastionBlock(state)) {
            return InteractionResultHolder.pass(stack);
        }

        return handleCapture(serverLevel, serverPlayer, bastion, stack);
    }

    /**
     * 执行占领逻辑：校验状态、调用占领服务、反馈效果。
     */
    private InteractionResultHolder<ItemStack> handleCapture(
            ServerLevel level,
            ServerPlayer player,
            BastionData bastion,
            ItemStack stack) {
        long gameTime = level.getGameTime();
        BastionState state = bastion.getEffectiveState(gameTime);
        BastionData.CaptureState captureState = bastion.captureState();
        if (captureState == null) {
            captureState = BastionData.CaptureState.DEFAULT;
        }

        boolean stateAllowsCapture = state == BastionState.SEALED || state == BastionState.DESTROYED;
        boolean captureFlagAllows = captureState.capturable();

        if (!stateAllowsCapture && !captureFlagAllows) {
            player.sendSystemMessage(Component.literal(
                "§c基地尚未进入可占领状态！需要先击败守卫或封印基地"
            ));
            return InteractionResultHolder.fail(stack);
        }

        if (captureFlagAllows
            && captureState.capturableUntilGameTime() > 0
            && gameTime > captureState.capturableUntilGameTime()) {
            player.sendSystemMessage(Component.literal("§c可接管窗口已超时"));
            return InteractionResultHolder.fail(stack);
        }

        double currentNiantou = NianTouHelper.getAmount(player);
        if (currentNiantou < CAPTURE_NIANTOU_COST) {
            player.sendSystemMessage(Component.translatable(
                "message.guzhenrenext.bastion_capture.insufficient_niantou",
                (int) CAPTURE_NIANTOU_COST
            ));
            return InteractionResultHolder.fail(stack);
        }

        boolean success = BastionCaptureService.tryFinalizeCapture(level, bastion, player);
        if (!success) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        player.sendSystemMessage(Component.literal(
            String.format("§a§l成功占领基地 %s！§r\n  原道途: %s\n  转数: %d\n  节点: %d",
                bastion.id().toString().substring(0, UUID_DISPLAY_LENGTH),
                bastion.primaryDao().getSerializedName(),
                bastion.tier(),
                bastion.totalNodes())
        ));

        BastionSoundPlayer.playCapture(level, bastion.corePos());
        BastionParticles.spawnCaptureParticles(level, bastion.corePos(), bastion.primaryDao());

        BastionData captured = BastionSavedData.get(level).getBastion(bastion.id());
        if (captured != null) {
            BastionNetworkHandler.syncToNearbyPlayers(level, captured);
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
        return distanceSqr <= MAX_CAPTURE_RANGE * MAX_CAPTURE_RANGE;
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
