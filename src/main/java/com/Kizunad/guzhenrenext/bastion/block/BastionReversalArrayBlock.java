package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.blockentity.BastionReversalArrayBlockEntity;
import com.Kizunad.guzhenrenext.bastion.threat.ThreatEventService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 逆转阵法方块（偷家玩法）。
 * <p>
 * 设计：
 * <ul>
 *   <li>玩家在基地领域内布置阵法结构并启动。</li>
 *   <li>阵法每秒从基地 resourcePool 抽取“大额资源”，并转化为随机蛊虫（按道途标签池抽取）。</li>
 *   <li>阵法运行期间高概率触发猎手/辐射等威胁事件，迫使玩家防守。</li>
 * </ul>
 * </p>
 */
public class BastionReversalArrayBlock extends Block implements EntityBlock {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BastionReversalArrayBlock.class
    );

    /** 是否处于运行状态。 */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    /**
     * 常量配置（MVP 阶段先硬编码，后续可外置到 json/config）。
     */
    private static final class Constants {
        /** 每秒处理一次。 */
        static final int TICK_INTERVAL_TICKS = 20;
        /** 查找归属基地的最大半径。 */
        static final int BASTION_SEARCH_RADIUS = 512;

        /**
         * 逆转阵法操作者检测半径（寻找最近玩家作为“操作者”提示/扣燃料反馈）。
         */
        static final int OPERATOR_SEARCH_RADIUS = 8;

        /** actionbar 状态提示半径（仅用于信息反馈）。 */
        static final int STATUS_BROADCAST_RADIUS = 16;

        /** 方块中心偏移。 */
        static final double CENTER_OFFSET = 0.5;

        /** 基础偷取量（消耗 resourcePool）。 */
        static final double BASE_STEAL_COST = 500.0;
        /** 每转增加偷取量。 */
        static final double STEAL_COST_PER_TIER = 250.0;
        /** 最小偷取量（防止配置异常导致过低）。 */
        static final double MIN_STEAL_COST = 200.0;

        /** 运行时会在阵法上方掉落奖励，用于视觉反馈。 */
        static final double REWARD_DROP_Y_OFFSET = 1.1;

        /** 无燃料时的提示冷却（避免刷屏）。 */
        static final int NO_FUEL_MESSAGE_COOLDOWN_TICKS = 40;

        /** 资源池不足时的提示冷却（避免刷屏）。 */
        static final int NO_RESOURCE_MESSAGE_COOLDOWN_TICKS = 40;

        private Constants() {
        }
    }

    private static final class MsgKeys {
        static final String NO_FUEL_UNTIL_TICK = "guzhenrenext_reversal_no_fuel_until";
        static final String NO_RESOURCE_UNTIL_TICK = "guzhenrenext_reversal_no_resource_until";

        private MsgKeys() {
        }
    }

    public BastionReversalArrayBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BastionReversalArrayBlockEntity(pos, state);
    }

    @Override
    public void onRemove(
        BlockState state,
        Level level,
        BlockPos pos,
        BlockState newState,
        boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BastionReversalArrayBlockEntity arrayBe) {
                Containers.dropContents(level, pos, arrayBe.getContainer());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(
        final StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(ACTIVE);
    }

    @Override
    public InteractionResult useWithoutItem(
        final BlockState state,
        final Level level,
        final BlockPos pos,
        final Player player,
        final BlockHitResult hit
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (!(blockEntity instanceof BastionReversalArrayBlockEntity arrayBe)) {
            return InteractionResult.PASS;
        }

        if (state.getValue(ACTIVE)) {
            // 停止阵法
            serverLevel.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7逆转阵法已停止"));
            return InteractionResult.SUCCESS;
        }

        // 空手右键：只尝试启动（不自动从背包扣燃料/扣蛊材），缺啥提示啥。
        if (!isValidStructure(serverLevel, pos)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c阵法结构不完整：需要在四周放置基地节点"));
            return InteractionResult.SUCCESS;
        }

        BastionData bastion = findBastionForArray(serverLevel, pos);
        if (bastion == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c附近未找到可供逆转的基地"));
            return InteractionResult.SUCCESS;
        }
        if (!isInsideAura(bastion, pos)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c阵法不在基地领域内，无法逆转"));
            return InteractionResult.SUCCESS;
        }

        if (!arrayBe.hasAnyFuel()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c缺少燃料：请手持泉眼右键放入燃料槽"));
            return InteractionResult.SUCCESS;
        }
        if (!arrayBe.hasSacrifice()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c缺少献祭：请手持蛊材右键阵法（用于献祭并启动）"));
            return InteractionResult.SUCCESS;
        }

        serverLevel.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
        serverLevel.scheduleTick(pos, this, Constants.TICK_INTERVAL_TICKS);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a逆转阵法启动：正在抽取基地资源（消耗泉眼燃料）"));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack,
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        InteractionHand hand,
        BlockHitResult hit
    ) {
        // 允许玩家通过“手持蛊材右键”来献祭（并启动阵法）
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(serverLevel.getBlockEntity(pos) instanceof BastionReversalArrayBlockEntity arrayBe)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 已激活：无论手持什么都允许停止，避免“拿着物品停不了”
        if (state.getValue(ACTIVE)) {
            serverLevel.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7逆转阵法已停止"));
            return ItemInteractionResult.SUCCESS;
        }

        // 结构与基地范围检查（启动前先检查，避免材料被误扣）。
        if (!isValidStructure(serverLevel, pos)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c阵法结构不完整：需要在四周放置基地节点"));
            return ItemInteractionResult.CONSUME;
        }
        BastionData bastion = findBastionForArray(serverLevel, pos);
        if (bastion == null || !isInsideAura(bastion, pos)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c阵法不在基地领域内，无法逆转"));
            return ItemInteractionResult.CONSUME;
        }

        // A) 手持泉眼：优先放入燃料槽（不触发 useWithoutItem 回退）。
        if (arrayBe.tryInsertFuel(stack)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7已为阵法补充 1 个泉眼"));
            // 若材料齐全则直接启动
            if (arrayBe.hasSacrifice()) {
                serverLevel.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
                serverLevel.scheduleTick(pos, this, Constants.TICK_INTERVAL_TICKS);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a逆转阵法启动：正在抽取基地资源（消耗泉眼燃料）"));
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e还缺献祭：请手持蛊材右键阵法"));
            }
            return ItemInteractionResult.SUCCESS;
        }

        // B) 手持蛊材：执行献祭（不回退到 useWithoutItem）。
        if (!arrayBe.isValidSacrifice(stack)) {
            // 非燃料/非蛊材：给出明确提示，避免误导为“缺少燃料”。
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7用法：手持泉眼右键=补燃料；手持蛊材右键=献祭并启动；空手右键=启动/停止"));
            return ItemInteractionResult.SUCCESS;
        }

        if (!arrayBe.hasSacrifice()) {
            arrayBe.setSacrifice(stack);
            stack.shrink(1);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6已献祭蛊材"));
        }

        if (!arrayBe.hasAnyFuel()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§e已献祭，但缺少燃料：请手持泉眼右键放入燃料槽"));
            return ItemInteractionResult.SUCCESS;
        }

        serverLevel.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
        serverLevel.scheduleTick(pos, this, Constants.TICK_INTERVAL_TICKS);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a逆转阵法启动：正在抽取基地资源（消耗泉眼燃料）"));
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void tick(
        final BlockState state,
        final ServerLevel level,
        final BlockPos pos,
        final RandomSource random
    ) {
        if (!state.getValue(ACTIVE)) {
            return;
        }

        // 结构被破坏则自动停止
        if (!isValidStructure(level, pos)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            broadcastStatus(level, pos, "§c逆转阵法停止：结构被破坏");
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BastionReversalArrayBlockEntity arrayBe)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            broadcastStatus(level, pos, "§c逆转阵法停止：方块实体丢失");
            return;
        }
        BastionData bastion = findBastionForArray(level, pos);
        if (bastion == null || !isInsideAura(bastion, pos)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            broadcastStatus(level, pos, "§c逆转阵法停止：不在基地领域内");
            return;
        }

        // 取最新数据，避免与 BastionTicker 的更新互相覆盖
        BastionData current = savedData.getBastion(bastion.id());
        if (current == null) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            broadcastStatus(level, pos, "§c逆转阵法停止：基地数据缺失");
            return;
        }

        // 防止异常重复调度导致同一秒多次运行
        if (!arrayBe.shouldProcess(level.getGameTime(), Constants.TICK_INTERVAL_TICKS)) {
            return;
        }
        arrayBe.markProcessed(level.getGameTime(), Constants.TICK_INTERVAL_TICKS);

        // 运行燃料：每秒消耗 20 tick；fuelTicks 不足则从燃料槽消耗 1 个泉眼补充。
        ServerPlayer operator = level.getNearestPlayer(
            pos.getX() + Constants.CENTER_OFFSET,
            pos.getY() + Constants.CENTER_OFFSET,
            pos.getZ() + Constants.CENTER_OFFSET,
            Constants.OPERATOR_SEARCH_RADIUS,
            false
        ) instanceof ServerPlayer sp ? sp : null;
        if (!arrayBe.tryConsumeFuel(level, operator, Constants.TICK_INTERVAL_TICKS)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            sendCooldownMessage(operator, MsgKeys.NO_FUEL_UNTIL_TICK, level.getGameTime(),
                Constants.NO_FUEL_MESSAGE_COOLDOWN_TICKS,
                "§c逆转阵法停止：燃料不足（需要泉眼）");
            return;
        }

        // 尝试偷取资源并发放蛊虫
        double stealCost = calculateStealCost(current);
        if (current.resourcePool() >= stealCost) {
            BastionData updated = current.withResourcePool(current.resourcePool() - stealCost);
            savedData.updateBastion(updated);

            ItemStack reward = arrayBe.rollReward(updated.primaryDao(), random);
            if (!reward.isEmpty()) {
                arrayBe.dropReward(level, pos, reward);
            }
            broadcastProgress(level, pos, updated, stealCost, reward);
        } else {
            sendCooldownMessage(operator, MsgKeys.NO_RESOURCE_UNTIL_TICK, level.getGameTime(),
                Constants.NO_RESOURCE_MESSAGE_COOLDOWN_TICKS,
                "§e逆转阵法：基地资源池不足，暂无产出");
        }

        // 高风险副作用：高概率触发威胁事件（独立冷却，不影响拆节点逻辑）
        ThreatEventService.tryTriggerOnReversalArray(
            level,
            savedData,
            current,
            pos,
            level.getGameTime()
        );

        level.scheduleTick(pos, this, Constants.TICK_INTERVAL_TICKS);
    }

    private static void broadcastProgress(
        ServerLevel level,
        BlockPos pos,
        BastionData bastion,
        double stealCost,
        ItemStack reward
    ) {
        String rewardText = (reward == null || reward.isEmpty())
            ? "无"
            : reward.getHoverName().getString();
        String msg = "§a逆转阵法：-" + (int) Math.round(stealCost)
            + " pool，剩余=" + (int) Math.round(bastion.resourcePool())
            + "，产出=" + rewardText;
        broadcastActionbar(level, pos, msg);
    }

    private static void broadcastStatus(ServerLevel level, BlockPos pos, String msg) {
        broadcastActionbar(level, pos, msg);
    }

    private static void broadcastActionbar(ServerLevel level, BlockPos pos, String msg) {
        for (ServerPlayer player : level.players()) {
            if (player == null || player.isSpectator()) {
                continue;
            }
            long distSq = (long) player.blockPosition().distSqr(pos);
            long max = (long) Constants.STATUS_BROADCAST_RADIUS * Constants.STATUS_BROADCAST_RADIUS;
            if (distSq > max) {
                continue;
            }
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(msg),
                true
            );
        }
    }

    private static void sendCooldownMessage(
        ServerPlayer player,
        String key,
        long gameTime,
        int cooldownTicks,
        String msg
    ) {
        if (player == null) {
            return;
        }
        long until = player.getPersistentData().getLong(key);
        if (gameTime < until) {
            return;
        }
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(msg));
        player.getPersistentData().putLong(key, gameTime + cooldownTicks);
    }

    private static BastionData findBastionForArray(
        final ServerLevel level,
        final BlockPos pos
    ) {
        BastionSavedData savedData = BastionSavedData.get(level);
        return savedData.findOwnerBastion(pos, Constants.BASTION_SEARCH_RADIUS);
    }

    private static boolean isValidStructure(
        final ServerLevel level,
        final BlockPos pos
    ) {
        // MVP：中心四周必须是基地 Anchor（支撑节点）
        for (Direction dir : new Direction[] {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
        }) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (!(neighbor.getBlock() instanceof BastionAnchorBlock)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInsideAura(final BastionData bastion, final BlockPos pos) {
        int auraRadius = bastion.getAuraRadius();
        long radiusSq = (long) auraRadius * auraRadius;
        return bastion.corePos().distSqr(pos) <= radiusSq;
    }

    private static double calculateStealCost(final BastionData bastion) {
        double cost = Constants.BASE_STEAL_COST
            + (bastion.tier() - 1) * Constants.STEAL_COST_PER_TIER;
        return Math.max(Constants.MIN_STEAL_COST, cost);
    }

    @Override
    public @Nullable <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T>
        getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 使用 Block 的 scheduleTick 机制，本方块不通过 BE ticker 运行。
        return null;
    }
}
