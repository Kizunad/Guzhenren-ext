package com.Kizunad.guzhenrenext.bastion.item;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionWardingLanternBlock;
import com.Kizunad.guzhenrenext.bastion.blockentity.BastionWardingLanternBlockEntity;
import com.Kizunad.guzhenrenext.bastion.service.BastionManagementService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 基地管理令牌：绑定 bastionId 后可远程打开管理 GUI。
 * <p>
 * 使用方式：
 * <ul>
 *     <li>对核心右键：绑定 bastionId 到令牌</li>
 *     <li>对空气右键：若已绑定则打开 GUI（需为占领者）</li>
 * </ul>
 * </p>
 */
public class BastionManagementTokenItem extends Item {

    /** CustomData 中存储 bastionId 的 key。 */
    private static final String KEY_BASTION_ID = "BastionId";

    /** 方块中心偏移量。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5d;

    /** 绑定时允许的最大距离。 */
    private static final double MAX_BIND_RANGE = 10.0d;

    /** 绑定镇地灯时的反馈粒子数量。 */
    private static final int LANTERN_BIND_PARTICLE_COUNT = 8;

    /** 绑定镇地灯时粒子扩散偏移量。 */
    private static final double LANTERN_BIND_PARTICLE_DELTA = 0.25d;

    /** 绑定镇地灯时粒子速度。 */
    private static final double LANTERN_BIND_PARTICLE_SPEED = 0.01d;

    public BastionManagementTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)
            || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        // 射线检测是否对着核心
        BlockHitResult hitResult = getPlayerPOVHitResult(serverLevel, serverPlayer, ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            // 对着镇地灯：尝试将令牌已绑定的 bastionId 写入灯笼 BE
            if (serverLevel.getBlockState(hitPos).getBlock() instanceof BastionWardingLanternBlock) {
                return handleBindLantern(serverLevel, serverPlayer, stack, hitPos);
            }

            if (serverLevel.getBlockState(hitPos).getBlock() instanceof BastionCoreBlock) {
                if (!isWithinBindRange(serverPlayer, hitPos)) {
                    return InteractionResultHolder.pass(stack);
                }
                return handleBindToCore(serverLevel, serverPlayer, stack, hitPos);
            }
        }

        // 未对着核心，尝试远程打开 GUI
        return handleRemoteOpen(serverLevel, serverPlayer, stack);
    }

    /**
     * 绑定令牌到核心所属的基地。
     */
    private InteractionResultHolder<ItemStack> handleBindToCore(
            ServerLevel level,
            ServerPlayer player,
            ItemStack stack,
            BlockPos corePos) {

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.findByCorePos(corePos);

        if (bastion == null) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.no_bastion"));
            return InteractionResultHolder.fail(stack);
        }

        // 校验是否为占领者
        BastionData.CaptureState captureState = bastion.captureState();
        if (captureState == null || !captureState.isCapturedBy(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.not_owner"));
            return InteractionResultHolder.fail(stack);
        }

        // 写入 bastionId
        CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        tag.putUUID(KEY_BASTION_ID, bastion.id());
        ItemStackCustomDataHelper.setCustomDataTag(stack, tag);

        player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.bound"));
        return InteractionResultHolder.success(stack);
    }

    /**
     * 使用已绑定令牌将镇地灯绑定到对应基地。
     */
    private InteractionResultHolder<ItemStack> handleBindLantern(
            ServerLevel level,
            ServerPlayer player,
            ItemStack stack,
            BlockPos lanternPos) {

        UUID bastionId = getBoundBastionId(stack);
        if (bastionId == null) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.not_bound"));
            return InteractionResultHolder.fail(stack);
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.getBastion(bastionId);
        if (bastion == null) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.bastion_not_found"));
            return InteractionResultHolder.fail(stack);
        }

        // 校验是否为占领者：只有占领者才能把灯笼绑定到基地。
        BastionData.CaptureState captureState = bastion.captureState();
        if (captureState == null || !captureState.isCapturedBy(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.not_owner"));
            return InteractionResultHolder.fail(stack);
        }

        if (!(level.getBlockEntity(lanternPos) instanceof BastionWardingLanternBlockEntity lanternBlockEntity)) {
            return InteractionResultHolder.fail(stack);
        }

        // 通过 BE 的 setter 写入，确保触发 setChanged() 与缓存更新。
        lanternBlockEntity.setBastionId(bastionId);

        // 绑定成功反馈：粒子 + 系统消息。
        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            lanternPos.getX() + BLOCK_CENTER_OFFSET,
            lanternPos.getY() + BLOCK_CENTER_OFFSET,
            lanternPos.getZ() + BLOCK_CENTER_OFFSET,
            LANTERN_BIND_PARTICLE_COUNT,
            LANTERN_BIND_PARTICLE_DELTA,
            LANTERN_BIND_PARTICLE_DELTA,
            LANTERN_BIND_PARTICLE_DELTA,
            LANTERN_BIND_PARTICLE_SPEED
        );
        player.sendSystemMessage(Component.literal("§a镇地灯已绑定到令牌所属基地"));
        return InteractionResultHolder.success(stack);
    }

    /**
     * 远程打开已绑定基地的 GUI。
     */
    private InteractionResultHolder<ItemStack> handleRemoteOpen(
            ServerLevel level,
            ServerPlayer player,
            ItemStack stack) {

        CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        if (!tag.hasUUID(KEY_BASTION_ID)) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.not_bound"));
            return InteractionResultHolder.fail(stack);
        }

        UUID bastionId = tag.getUUID(KEY_BASTION_ID);
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.getBastion(bastionId);

        if (bastion == null) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.bastion_not_found"));
            return InteractionResultHolder.fail(stack);
        }

        // 校验是否为占领者
        BastionData.CaptureState captureState = bastion.captureState();
        if (captureState == null || !captureState.isCapturedBy(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("message.guzhenrenext.management_token.not_owner"));
            return InteractionResultHolder.fail(stack);
        }

        // 打开远程管理 GUI
        BastionManagementService.openManagementMenu(level, player, bastion, true);

        return InteractionResultHolder.success(stack);
    }

    /**
     * 判断目标核心是否在允许绑定距离内。
     */
    private boolean isWithinBindRange(ServerPlayer player, BlockPos corePos) {
        double distanceSqr = player.distanceToSqr(
            corePos.getX() + BLOCK_CENTER_OFFSET,
            corePos.getY() + BLOCK_CENTER_OFFSET,
            corePos.getZ() + BLOCK_CENTER_OFFSET
        );
        return distanceSqr <= MAX_BIND_RANGE * MAX_BIND_RANGE;
    }

    /**
     * 获取已绑定的 bastionId（用于 Tooltip 或其他检查）。
     */
    public static UUID getBoundBastionId(ItemStack stack) {
        CompoundTag tag = ItemStackCustomDataHelper.copyCustomDataTag(stack);
        return tag.hasUUID(KEY_BASTION_ID) ? tag.getUUID(KEY_BASTION_ID) : null;
    }
}
