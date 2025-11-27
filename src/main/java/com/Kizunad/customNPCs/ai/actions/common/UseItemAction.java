package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用物品动作 - 使用手中的物品
 * <p>
 * 功能：
 * - 检查并选择物品（主手/副手/背包）
 * - 区分瞬时使用类（食物、药水）和持续使用类（弓、盾牌）
 * - 处理使用时长超时
 * - 处理物品耗尽/被替换
 * <p>
 * 参数：
 * - targetItem: 目标物品（可为 null，表示使用当前手中物品）
 * - preferHand: 优先使用的手（默认主手）
 * - maxUseTicks: 最大使用时长
 * - allowInterrupt: 是否允许中断
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class UseItemAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(UseItemAction.class);

    // 从配置获取默认值

    // ==================== 参数 ====================
    /**
     * 目标物品（可为 null）
     */
    private final Item targetItem;

    /**
     * 优先使用的手
     */
    private final InteractionHand preferHand;

    /**
     * 最大使用时长（ticks）
     */
    private final int maxUseTicks;

    /**
     * 是否允许中断
     */
    private final boolean allowInterrupt;

    // ==================== 状态 ====================
    /**
     * 使用中的手
     */
    private InteractionHand usingHand;

    /**
     * 是否已开始使用
     */
    private boolean hasStartedUsing;

    /**
     * 使用计数器
     */
    private int useCounter;

    /**
     * 创建使用物品动作（使用默认值）
     * @param targetItem 目标物品（null 表示使用当前手中物品）
     */
    public UseItemAction(Item targetItem) {
        this(targetItem, InteractionHand.MAIN_HAND, CONFIG.getDefaultItemUseTicks(), true);
    }

    /**
     * 创建使用物品动作（完整参数）
     * @param targetItem 目标物品（null 表示使用当前手中物品）
     * @param preferHand 优先使用的手
     * @param maxUseTicks 最大使用时长
     * @param allowInterrupt 是否允许中断
     */
    public UseItemAction(
        Item targetItem,
        InteractionHand preferHand,
        int maxUseTicks,
        boolean allowInterrupt
    ) {
        super("UseItemAction", null, maxUseTicks + CONFIG.getTimeoutBufferTicks(), 0, 0);
        this.targetItem = targetItem;
        this.preferHand = preferHand;
        this.maxUseTicks = maxUseTicks;
        this.allowInterrupt = allowInterrupt;
        this.hasStartedUsing = false;
        this.useCounter = 0;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        // 如果还未开始使用，先选择物品
        if (!hasStartedUsing) {
            InteractionHand hand = selectItemHand(mob);
            if (hand == null) {
                LOGGER.warn("[UseItemAction] 找不到可用物品");
                return ActionStatus.FAILURE;
            }
            this.usingHand = hand;

            // 开始使用物品
            mob.startUsingItem(usingHand);
            hasStartedUsing = true;
            LOGGER.info(
                "[UseItemAction] 开始使用物品: {}",
                mob.getItemInHand(usingHand).getDisplayName().getString()
            );
        }

        // 检查物品是否仍然存在
        ItemStack currentStack = mob.getItemInHand(usingHand);
        if (currentStack.isEmpty() || (targetItem != null && currentStack.getItem() != targetItem)) {
            LOGGER.warn("[UseItemAction] 物品被替换或耗尽");
            mob.stopUsingItem();
            return ActionStatus.FAILURE;
        }

        // 增加使用计数
        useCounter++;

        // 检查是否超时
        if (useCounter >= maxUseTicks) {
            LOGGER.info("[UseItemAction] 使用完成（超时）");
            mob.stopUsingItem();
            return ActionStatus.SUCCESS;
        }

        // 检查物品是否自动完成使用（如食物吃完）
        if (!mob.isUsingItem()) {
            LOGGER.info("[UseItemAction] 物品使用完成");
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    /**
     * 选择可用的物品手
     * @param mob NPC 实体
     * @return 可用的手，如果没有则返回 null
     */
    private InteractionHand selectItemHand(Mob mob) {
        // 1. 优先检查 preferHand
        if (isItemUsable(mob, preferHand)) {
            return preferHand;
        }

        // 2. 检查另一只手
        InteractionHand otherHand = preferHand == InteractionHand.MAIN_HAND
            ? InteractionHand.OFF_HAND
            : InteractionHand.MAIN_HAND;
        if (isItemUsable(mob, otherHand)) {
            return otherHand;
        }

        // 背包搜索功能未实现
        LOGGER.debug("[UseItemAction] 背包搜索功能未实现");

        return null;
    }

    /**
     * 检查指定手中的物品是否可用
     * @param mob NPC 实体
     * @param hand 手
     * @return true 如果可用
     */
    private boolean isItemUsable(Mob mob, InteractionHand hand) {
        ItemStack stack = mob.getItemInHand(hand);
        if (stack.isEmpty()) {
            return false;
        }

        // 如果指定了目标物品，检查是否匹配
        if (targetItem != null) {
            return stack.getItem() == targetItem;
        }

        // 如果未指定目标物品，检查物品是否可使用
        return stack.getFoodProperties(mob) != null || stack.getUseDuration(mob) > 0;
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        this.hasStartedUsing = false;
        this.useCounter = 0;
        String itemName = targetItem != null ? targetItem.toString() : "当前物品";
        LOGGER.info("[UseItemAction] 准备使用物品: {}", itemName);
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        // 确保停止使用物品
        if (entity instanceof Mob mob && mob.isUsingItem()) {
            mob.stopUsingItem();
        }
        LOGGER.info("[UseItemAction] 停止使用物品 | 已使用 {} ticks", useCounter);
    }

    @Override
    public boolean canInterrupt() {
        // 根据 allowInterrupt 参数决定是否可中断
        return allowInterrupt;
    }
}
