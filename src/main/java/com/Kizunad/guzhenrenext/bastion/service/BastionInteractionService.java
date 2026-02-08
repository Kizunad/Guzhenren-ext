package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionSoundPlayer;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地玩家交互服务 - 处理玩家与基地的交互事件。
 * <p>
 * 交互类型：
 * <ul>
 *   <li><b>攻击</b>：对核心/节点造成伤害（左键）</li>
 *   <li><b>封印</b>：使用封印道具暂时封印基地（右键+封印物品）</li>
 *   <li><b>占领</b>：在基地被击败后占领它（右键+占领物品）</li>
 *   <li><b>祭献</b>：消耗真元为基地资源池充能（潜行+空手右键）</li>
 *   <li><b>查看</b>：查看基地信息（空手右键）</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(modid = com.Kizunad.guzhenrenext.GuzhenrenExt.MODID)
public final class BastionInteractionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionInteractionService.class);

    private BastionInteractionService() {
        // 工具类
    }

    // ===== 交互配置常量 =====

    /**
     * 交互相关常量。
     */
    private static final class InteractionConfig {
        /** 查找归属基地的搜索半径。 */
        static final int SEARCH_RADIUS = 128;
        /** 封印持续时间（秒）- 基础值。 */
        static final int BASE_SEAL_DURATION_SECONDS = 60;
        /** 每秒 tick 数。 */
        static final long TICKS_PER_SECOND = 20L;
        /** 封印持续时间（tick）。 */
        static final long SEAL_DURATION_TICKS = BASE_SEAL_DURATION_SECONDS * TICKS_PER_SECOND;
        /** 占领需要的封印次数阈值。 */
        static final int SEAL_COUNT_FOR_CAPTURE = 3;
        /** UUID 截取显示长度。 */
        static final int UUID_DISPLAY_LENGTH = 8;
        /** 百分比乘数。 */
        static final int PERCENT_MULTIPLIER = 100;

        private InteractionConfig() {
        }
    }

    // ===== 资源祭献配置常量 =====

    /**
     * 资源祭献相关常量。
     */
    private static final class SacrificeConfig {
        /** 每次祭献消耗的真元量（基础值）。 */
        static final double BASE_ZHENYUAN_COST = 100.0;
        /** 真元转换为资源池的比率（1真元 = X资源）。 */
        static final double ZHENYUAN_TO_POOL_RATIO = 1.0;
        /** 祭献的最低真元要求。 */
        static final double MIN_ZHENYUAN_REQUIRED = 50.0;
        /** 祭献冷却时间（毫秒）。 */
        static final long SACRIFICE_COOLDOWN_MS = 1000L;

        private SacrificeConfig() {
        }
    }

    /** 玩家祭献冷却记录。 */
    private static final java.util.Map<java.util.UUID, Long> SACRIFICE_COOLDOWNS =
        new java.util.concurrent.ConcurrentHashMap<>();

    // ===== 守卫标识常量 =====

    // 守卫标识现在由 BastionGuardianData 统一管理

    // ===== 封印物品识别 =====

    /**
     * 封印物品的 NBT 标签名。
     * <p>
     * 设计上，封印物品可以是：
     * <ul>
     *   <li>带有 "bastion_seal" NBT 标签的任意物品</li>
     *   <li>未来可扩展为特定的封印道具</li>
     * </ul>
     * </p>
     */
    private static final String SEAL_ITEM_TAG = "bastion_seal";

    /**
     * 占领物品的 NBT 标签名。
     */
    private static final String CAPTURE_ITEM_TAG = "bastion_capture";

    // ===== 事件处理 =====

    /**
     * 处理玩家右键方块交互事件。
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // 仅处理基地方块
        if (!(state.getBlock() instanceof BastionCoreBlock)
                && !(state.getBlock() instanceof BastionAnchorBlock)) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        InteractionHand hand = event.getHand();
        ItemStack heldItem = player.getItemInHand(hand);
        boolean isSneaking = player.isShiftKeyDown();

        InteractionResult result = handleBastionInteraction(
            serverLevel, serverPlayer, pos, state, heldItem, isSneaking
        );

        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    /**
     * 处理玩家左键攻击方块事件。
     * <p>
     * 当玩家攻击基地方块时：
     * <ul>
     *   <li>发送警告消息给玩家</li>
     *   <li>触发基地防御反应（刷新守卫）</li>
     *   <li>如果基地被封印，破坏速度加快</li>
     * </ul>
     * </p>
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // 仅处理基地方块
        boolean isCore = state.getBlock() instanceof BastionCoreBlock;
        boolean isNode = state.getBlock() instanceof BastionAnchorBlock;
        if (!isCore && !isNode) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        handleAttack(serverLevel, serverPlayer, pos, state, isCore);
    }

    /**
     * 处理基地方块交互。
     *
     * @param level      服务端世界
     * @param player     服务端玩家
     * @param pos        交互位置
     * @param state      方块状态
     * @param heldItem   手持物品
     * @param isSneaking 是否潜行
     * @return 交互结果
     */
    private static InteractionResult handleBastionInteraction(
            ServerLevel level,
            ServerPlayer player,
            BlockPos pos,
            BlockState state,
            ItemStack heldItem,
            boolean isSneaking) {
        BastionSavedData savedData = BastionSavedData.get(level);

        // 确定目标基地
        BastionData bastion;
        if (state.getBlock() instanceof BastionCoreBlock) {
            bastion = savedData.findByCorePos(pos);
            // worldgen 遗迹会由 BastionRuinAutoActivator 延迟自动激活。
            // 这里保留兜底：玩家交互时若仍未激活，则立即激活以提升体验。
            if (bastion == null) {
                bastion = tryRegisterWorldgenRuin(level, savedData, pos, state);
            }
        } else {
            bastion = savedData.findOwnerBastion(pos, InteractionConfig.SEARCH_RADIUS);
        }

        if (bastion == null) {
            player.sendSystemMessage(Component.literal("§c此方块未关联任何基地"));
            return InteractionResult.FAIL;
        }

        // 根据手持物品和潜行状态决定交互类型
        if (heldItem.isEmpty()) {
            if (isSneaking) {
                // 潜行+空手：祭献真元
                return handleSacrifice(level, savedData, player, bastion);
            } else {
                // 非潜行+空手：检查是否为占领者且对着核心
                if (state.getBlock() instanceof BastionCoreBlock) {
                    BastionData.CaptureState captureState = bastion.captureState();
                    if (captureState != null && captureState.isCapturedBy(player.getUUID())) {
                        // 占领者：打开管理 GUI
                        BastionManagementService.openManagementMenu(level, player, bastion, false);
                        return InteractionResult.SUCCESS;
                    }
                }
                // 非占领者或非核心：查看基地信息
                return handleViewInfo(player, bastion, level.getGameTime());
            }
        } else if (isSealItem(heldItem)) {
            // 封印物品：尝试封印
            return handleSeal(level, player, bastion, heldItem);
        } else if (isCaptureItem(heldItem)) {
            // 占领物品：尝试占领
            return handleCapture(level, player, bastion, heldItem);
        }

        // 其他物品：不处理
        return InteractionResult.PASS;
    }

    private static BastionData tryRegisterWorldgenRuin(
        ServerLevel level,
        BastionSavedData savedData,
        BlockPos corePos,
        BlockState coreState
    ) {
        if (!(coreState.getBlock() instanceof BastionCoreBlock)) {
            return null;
        }
        BastionDao dao = coreState.getValue(BastionCoreBlock.DAO);
        int tier = BastionCoreBlock.getTier(coreState);

        String bastionType = BastionTypeManager.getByDao(dao).id();
        BastionData created = BastionData.create(
            corePos,
            level.dimension(),
            bastionType,
            dao,
            level.getGameTime()
        );
        created = created.withEvolution(created.evolutionProgress(), tier);
        savedData.addBastion(created);
        savedData.initializeFrontierFromCore(created.id(), corePos);

        // 将附近遗迹节点写入 cache，确保扩张/清理服务可用。
        for (Direction dir : new Direction[] {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
        }) {
            BlockPos nodePos = corePos.relative(dir, 2);
            BlockState nodeState = level.getBlockState(nodePos);
            if (nodeState.getBlock() instanceof BastionAnchorBlock
                && nodeState.getValue(BastionAnchorBlock.GENERATED)) {
                savedData.addNodeToCache(created.id(), nodePos);
            }
        }

        BastionNetworkHandler.syncToNearbyPlayers(level, created);
        return created;
    }

    // ===== 查看信息 =====

    /**
     * 处理查看基地信息。
     */
    private static InteractionResult handleViewInfo(
            ServerPlayer player,
            BastionData bastion,
            long gameTime) {
        BastionState effectiveState = bastion.getEffectiveState(gameTime);

        String info = String.format(
            "§e[基地信息]§r\n" +
                "  ID: §b%s§r\n" +
                "  道途: §a%s§r\n" +
                "  转数: §c%d§r\n" +
                "  进化: §e%.1f%%§r\n" +
                "  节点: §9%d§r\n" +
                "  状态: §7%s§r",
            bastion.id().toString().substring(0, InteractionConfig.UUID_DISPLAY_LENGTH),
            bastion.primaryDao().getSerializedName(),
            bastion.tier(),
            bastion.evolutionProgress() * InteractionConfig.PERCENT_MULTIPLIER,
            bastion.totalNodes(),
            effectiveState.getSerializedName()
        );

        // 如果被封印，显示剩余时间
        if (effectiveState == BastionState.SEALED) {
            long remainingTicks = bastion.sealedUntilGameTime() - gameTime;
            long remainingSeconds = remainingTicks / InteractionConfig.TICKS_PER_SECOND;
            info += String.format("\n  封印剩余: §6%d§r 秒", remainingSeconds);
        }

        player.sendSystemMessage(Component.literal(info));
        return InteractionResult.SUCCESS;
    }

    // ===== 资源祭献机制 =====

    /**
     * 处理玩家祭献真元给基地。
     * <p>
     * 祭献条件：
     * <ul>
     *   <li>玩家拥有足够的真元</li>
     *   <li>基地处于 ACTIVE 状态</li>
     *   <li>祭献操作不在冷却中</li>
     * </ul>
     * </p>
     *
     * @param level     服务端世界
     * @param savedData 基地存储数据
     * @param player    服务端玩家
     * @param bastion   目标基地
     * @return 交互结果
     */
    private static InteractionResult handleSacrifice(
            ServerLevel level,
            BastionSavedData savedData,
            ServerPlayer player,
            BastionData bastion) {
        long gameTime = level.getGameTime();
        BastionState effectiveState = bastion.getEffectiveState(gameTime);

        // 仅允许对 ACTIVE 基地祭献
        if (effectiveState != BastionState.ACTIVE) {
            player.sendSystemMessage(Component.literal(
                "§c基地未处于活跃状态，无法接收祭献"
            ));
            return InteractionResult.FAIL;
        }

        // 检查冷却
        long currentTime = System.currentTimeMillis();
        java.util.UUID playerId = player.getUUID();
        Long lastSacrifice = SACRIFICE_COOLDOWNS.get(playerId);
        if (lastSacrifice != null
                && (currentTime - lastSacrifice) < SacrificeConfig.SACRIFICE_COOLDOWN_MS) {
            // 静默失败，避免刷屏
            return InteractionResult.CONSUME;
        }

        // 检查玩家真元
        double currentZhenyuan = ZhenYuanHelper.getAmount(player);
        if (currentZhenyuan < SacrificeConfig.MIN_ZHENYUAN_REQUIRED) {
            player.sendSystemMessage(Component.literal(
                String.format("§c真元不足！当前: %.0f，最低需要: %.0f",
                    currentZhenyuan, SacrificeConfig.MIN_ZHENYUAN_REQUIRED)
            ));
            return InteractionResult.FAIL;
        }

        // 计算实际祭献量（取玩家当前真元与基础消耗的较小值）
        double sacrificeAmount = Math.min(currentZhenyuan, SacrificeConfig.BASE_ZHENYUAN_COST);

        // 消耗玩家真元
        ZhenYuanHelper.modify(player, -sacrificeAmount);

        // 计算资源池增量
        double poolGain = sacrificeAmount * SacrificeConfig.ZHENYUAN_TO_POOL_RATIO;

        // 更新基地资源池
        double newPool = bastion.resourcePool() + poolGain;
        BastionData updated = bastion.withResourcePool(newPool);
        savedData.updateBastion(updated);

        // 更新冷却
        SACRIFICE_COOLDOWNS.put(playerId, currentTime);

        // 发送反馈
        player.sendSystemMessage(Component.literal(
            String.format("§a成功祭献 §e%.0f§a 真元，基地资源池: §b%.1f",
                sacrificeAmount, newPool)
        ));

        LOGGER.debug("玩家 {} 向基地 {} 祭献了 {} 真元，资源池更新为 {}",
            player.getName().getString(),
            bastion.id(),
            sacrificeAmount,
            newPool);

        // 播放祭献音效和粒子效果
        BastionSoundPlayer.playSacrifice(level, bastion.corePos());
        BastionParticles.spawnSacrificeParticles(level, bastion.corePos(), player.blockPosition());

        return InteractionResult.SUCCESS;
    }

    // ===== 封印机制 =====

    /**
     * 检查物品是否为封印物品。
     */
    private static boolean isSealItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // 检查 NBT 标签
        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null && customData.contains(SEAL_ITEM_TAG)) {
                return true;
            }
        }
        // 正式封印物品
        return stack.is(BastionBlocks.BASTION_SEAL.get());
    }

    /**
     * 处理封印基地。
     */
    private static InteractionResult handleSeal(
            ServerLevel level,
            ServerPlayer player,
            BastionData bastion,
            ItemStack sealItem) {
        long gameTime = level.getGameTime();
        BastionState currentState = bastion.getEffectiveState(gameTime);

        // 已封印或已销毁的基地无法再次封印
        if (currentState == BastionState.SEALED) {
            long remainingTicks = bastion.sealedUntilGameTime() - gameTime;
            long remainingSeconds = remainingTicks / InteractionConfig.TICKS_PER_SECOND;
            player.sendSystemMessage(Component.literal(
                String.format("§e基地已被封印，剩余 %d 秒", remainingSeconds)
            ));
            return InteractionResult.FAIL;
        }

        if (currentState == BastionState.DESTROYED) {
            player.sendSystemMessage(Component.literal("§c基地已被摧毁，无法封印"));
            return InteractionResult.FAIL;
        }

        // 计算封印持续时间（可根据转数调整）
        long sealDuration = InteractionConfig.SEAL_DURATION_TICKS / bastion.tier();
        long sealUntil = gameTime + sealDuration;

        // 应用封印
        BastionSavedData savedData = BastionSavedData.get(level);
        savedData.applySeal(bastion.id(), sealUntil);

        // 消耗封印物品
        if (!player.isCreative()) {
            sealItem.shrink(1);
        }

        long durationSeconds = sealDuration / InteractionConfig.TICKS_PER_SECOND;
        player.sendSystemMessage(Component.literal(
            String.format("§a成功封印基地 %s，持续 %d 秒",
                bastion.id().toString().substring(0, InteractionConfig.UUID_DISPLAY_LENGTH),
                durationSeconds)
        ));

        LOGGER.info("玩家 {} 封印了基地 {}，持续 {} 秒",
            player.getName().getString(),
            bastion.id(),
            durationSeconds);

        // 播放封印音效和粒子效果
        BastionSoundPlayer.playSeal(level, bastion.corePos());
        BastionParticles.spawnSealParticles(level, bastion.corePos());

        // 同步更新后的状态到客户端
        BastionData sealedBastion = savedData.getBastion(bastion.id());
        if (sealedBastion != null) {
            BastionNetworkHandler.syncToNearbyPlayers(level, sealedBastion);
        }

        return InteractionResult.SUCCESS;
    }

    // ===== 占领机制 =====

    /**
     * 检查物品是否为占领物品。
     */
    private static boolean isCaptureItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // 检查 NBT 标签
        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null && customData.contains(CAPTURE_ITEM_TAG)) {
                return true;
            }
        }
        // 正式占领物品
        return stack.is(BastionBlocks.BASTION_CAPTURE_TOKEN.get());
    }

    /**
     * 处理占领基地。
     * <p>
     * 占领条件：
     * <ul>
     *   <li>基地必须处于 SEALED 或 DESTROYED 状态</li>
     *   <li>玩家持有占领物品</li>
     * </ul>
     * </p>
     */
    private static InteractionResult handleCapture(
            ServerLevel level,
            ServerPlayer player,
            BastionData bastion,
            ItemStack captureItem) {
        long gameTime = level.getGameTime();
        BastionData.CaptureState captureState = bastion.captureState();
        if (captureState == null) {
            captureState = BastionData.CaptureState.DEFAULT;
        }

        // 检查是否可接管
        if (!captureState.capturable()) {
            player.sendSystemMessage(Component.literal(
                "§c基地尚未进入可接管状态！需要先击杀 Boss 或完成净化阵法"
            ));
            return InteractionResult.FAIL;
        }

        // 检查可接管窗口是否超时
        if (captureState.capturableUntilGameTime() > 0
            && gameTime > captureState.capturableUntilGameTime()) {
            player.sendSystemMessage(Component.literal("§c可接管窗口已超时"));
            return InteractionResult.FAIL;
        }

        boolean success = BastionCaptureService.tryFinalizeCapture(level, bastion, player);
        if (!success) {
            return InteractionResult.FAIL;
        }

        BastionSavedData savedData = BastionSavedData.get(level);

        // 消耗占领物品
        if (!player.isCreative()) {
            captureItem.shrink(1);
        }

        player.sendSystemMessage(Component.literal(
            String.format("§a§l成功占领基地 %s！§r\n" +
                "  原道途: %s\n" +
                "  转数: %d\n" +
                "  节点: %d",
                bastion.id().toString().substring(0, InteractionConfig.UUID_DISPLAY_LENGTH),
                bastion.primaryDao().getSerializedName(),
                bastion.tier(),
                bastion.totalNodes())
        ));

        LOGGER.info("玩家 {} 占领了基地 {} (道途={}, 转数={}, 节点={})",
            player.getName().getString(),
            bastion.id(),
            bastion.primaryDao().getSerializedName(),
            bastion.tier(),
            bastion.totalNodes());

        // 播放占领音效和粒子效果
        BastionSoundPlayer.playCapture(level, bastion.corePos());
        BastionParticles.spawnCaptureParticles(level, bastion.corePos(), bastion.primaryDao());

        // 同步状态变化到客户端（基地被标记为 DESTROYED）
        BastionData capturedBastion = savedData.getBastion(bastion.id());
        if (capturedBastion != null) {
            BastionNetworkHandler.syncToNearbyPlayers(level, capturedBastion);
        }

        return InteractionResult.SUCCESS;
    }

    // ===== 攻击处理 =====

    /** 攻击警告消息的冷却时间（防止刷屏）。 */
    private static final java.util.Map<java.util.UUID, Long> ATTACK_WARN_COOLDOWNS =
        new java.util.concurrent.ConcurrentHashMap<>();

    /** 攻击警告消息冷却时间（毫秒）。 */
    private static final long ATTACK_WARN_COOLDOWN_MS = 3000L;

    /**
     * 处理玩家攻击基地方块。
     *
     * @param level   服务端世界
     * @param player  服务端玩家
     * @param pos     攻击位置
     * @param state   方块状态
     * @param isCore  是否为核心方块
     */
    private static void handleAttack(
            ServerLevel level,
            ServerPlayer player,
            BlockPos pos,
            BlockState state,
            boolean isCore) {
        BastionSavedData savedData = BastionSavedData.get(level);

        // 确定目标基地
        BastionData bastion;
        if (isCore) {
            bastion = savedData.findByCorePos(pos);
        } else {
            bastion = savedData.findOwnerBastion(pos, InteractionConfig.SEARCH_RADIUS);
        }

        if (bastion == null) {
            return; // 未关联基地，正常破坏流程
        }

        long currentTime = System.currentTimeMillis();
        java.util.UUID playerId = player.getUUID();

        // 检查警告消息冷却
        Long lastWarn = ATTACK_WARN_COOLDOWNS.get(playerId);
        boolean shouldWarn = lastWarn == null
            || (currentTime - lastWarn) > ATTACK_WARN_COOLDOWN_MS;

        if (shouldWarn) {
            ATTACK_WARN_COOLDOWNS.put(playerId, currentTime);

            BastionState effectiveState = bastion.getEffectiveState(level.getGameTime());
            String targetType = isCore ? "核心" : "节点";

            // 根据基地状态发送不同警告
            switch (effectiveState) {
                case ACTIVE -> player.sendSystemMessage(Component.literal(
                    String.format("§c警告：你正在攻击 %s 基地的%s！守卫即将响应！",
                        bastion.primaryDao().getSerializedName(), targetType)
                ));
                case SEALED -> player.sendSystemMessage(Component.literal(
                    String.format("§e基地已被封印，%s防御力下降！", targetType)
                ));
                case DESTROYED -> player.sendSystemMessage(Component.literal(
                    String.format("§7基地已被摧毁，%s正在衰减...", targetType)
                ));
            }
        }

        // 尝试触发 Boss
        if (isCore && bastion.getEffectiveState(level.getGameTime()) == BastionState.ACTIVE) {
            BastionBossService.tryTriggerBoss(level, bastion, player);
        }

        // 触发防御反应（仅在 ACTIVE 状态且攻击者不是占领者）
        if (bastion.getEffectiveState(level.getGameTime()) == BastionState.ACTIVE
                && !bastion.isFriendlyTo(player.getUUID())) {
            // 先尝试触发 Boss（符合条件时生成 BASTION_RAVAGER）
            BastionBossService.tryTriggerBoss(level, bastion, player);
            triggerDefenseResponse(level, bastion, player);
            // 播放警报音效
            BastionSoundPlayer.playAlarm(level, bastion.corePos());
        }
    }

    /**
     * 触发基地防御反应。
     * <p>
     * 当玩家攻击活跃基地时，触发守卫刷新和仇恨吸引。
     * 使用 PersistentData 精确匹配基地 UUID 过滤守卫。
     * </p>
     *
     * @param level   服务端世界
     * @param bastion 被攻击的基地
     * @param attacker 攻击者
     */
    private static void triggerDefenseResponse(
            ServerLevel level,
            BastionData bastion,
            ServerPlayer attacker) {
        // 尝试刷新额外守卫
        int spawned = BastionSpawnService.trySpawn(
            level,
            BastionSavedData.get(level),
            bastion,
            level.getGameTime()
        );

        if (spawned > 0) {
            LOGGER.debug("基地 {} 被攻击，刷新了 {} 个守卫", bastion.id(), spawned);
        }

        // 吸引该基地的守卫仇恨（使用 PersistentData 精确匹配，排除已被占领的守卫）
        double searchRadius = bastion.growthRadius();
        final UUID attackerId = attacker.getUUID();

        // 详细说明：该路径会直接 setTarget(attacker)，属于“直设目标”入口。
        // 若不在这里补齐友方判定，守卫可能绕过统一候选筛选逻辑，
        // 将友方玩家（capturedBy owner）强制设为目标，导致 Witch 等守卫继续攻击友军。
        // 因此在 setTarget 前增加 bastion.isFriendlyTo(attackerId) 硬过滤，
        // 对友方直接短路；对非友方保持原有防御行为不变。
        if (bastion.isFriendlyTo(attackerId)) {
            return;
        }

        level.getEntities(
            attacker,
            attacker.getBoundingBox().inflate(searchRadius),
            entity -> entity instanceof net.minecraft.world.entity.Mob mob
                && mob.isAlive()
                && BastionGuardianData.belongsToBastion(mob, bastion.id())
                && !BastionGuardianData.isCapturedBy(mob, attackerId)
        ).forEach(entity -> {
            if (entity instanceof net.minecraft.world.entity.Mob mob) {
                mob.setTarget(attacker);
            }
        });
    }
}
