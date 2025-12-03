package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.interfaces.IUseItemAction;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
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
public class UseItemAction extends AbstractStandardAction implements IUseItemAction {

    public static final String LLM_USAGE_DESC =
        "UseItemAction: use item in hand/backpack (food/potion/shield/bow charge etc); "
            + "pair with EatFromInventoryAction to fetch food first; respects maxUseTicks.";

    private static final Logger LOGGER = LoggerFactory.getLogger(UseItemAction.class);

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

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
     * 当前使用的物品栈
     */
    private ItemStack currentStack = ItemStack.EMPTY;

    /**
     * 记录临时占用的手上原有物品，便于恢复。
     */
    private ItemStack previousHandItem = ItemStack.EMPTY;

    /**
     * 是否从背包临时取出了物品。
     */
    private boolean borrowedFromInventory = false;

    /**
     * 取自背包的槽位索引，便于调试。
     */
    private int borrowedSlotIndex = -1;

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
            ItemSelection selection = selectItem(mind, mob);
            if (selection == null) {
                LOGGER.warn("[UseItemAction] 找不到可用物品");
                return ActionStatus.FAILURE;
            }
            this.usingHand = selection.hand();

            // 如果是从背包取出的物品，临时放到手上，使用后归还
            if (selection.fromInventory()) {
                previousHandItem = mob.getItemInHand(usingHand).copy();
                mob.setItemInHand(usingHand, selection.stack());
                borrowedFromInventory = true;
                borrowedSlotIndex = selection.inventorySlot();
                LOGGER.debug(
                    "[UseItemAction] 从背包槽位 {} 取出物品临时使用，放入 {}",
                    borrowedSlotIndex,
                    usingHand
                );
            }

            // 开始使用物品
            mob.startUsingItem(usingHand);
            hasStartedUsing = true;
            LOGGER.info(
                "[UseItemAction] 开始使用物品: {}",
                mob.getItemInHand(usingHand).getDisplayName().getString()
            );
        }

        // 检查物品是否仍然存在
        this.currentStack = mob.getItemInHand(usingHand);
        if (currentStack.isEmpty() || (targetItem != null && currentStack.getItem() != targetItem)) {
            LOGGER.warn("[UseItemAction] 物品被替换或耗尽");
            mob.stopUsingItem();
            return ActionStatus.FAILURE;
        }

        // 增加使用计数
        useCounter++;

        // 动态估算使用时长（物品 useDuration + 缓冲），避免过早打断食物/药水
        int expectedTicks = getExpectedUseTicks(mob);

        // 检查物品是否自动完成使用（如食物吃完）
        if (!mob.isUsingItem()) {
            LOGGER.info("[UseItemAction] 物品使用完成");
            return ActionStatus.SUCCESS;
        }

        // 超过预期时长则强制收尾，防止卡死
        if (useCounter >= expectedTicks) {
            LOGGER.info("[UseItemAction] 使用达到预期时长，强制结束");
            mob.stopUsingItem();
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
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

    /**
     * 估算本次使用的合理时长（物品 useDuration + 缓冲），下限为配置的默认值。
     */
    private int getExpectedUseTicks(Mob mob) {
        int itemDuration = 0;
        if (usingHand != null) {
            ItemStack stack = mob.getItemInHand(usingHand);
            itemDuration = stack.getUseDuration(mob);
        }
        int baseline = Math.max(maxUseTicks, CONFIG.getDefaultItemUseTicks());
        int buffered = itemDuration > 0
            ? itemDuration + CONFIG.getTimeoutBufferTicks()
            : baseline + CONFIG.getTimeoutBufferTicks();
        return Math.max(baseline, buffered);
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        this.hasStartedUsing = false;
        this.useCounter = 0;
        String itemName = targetItem != null ? targetItem.toString() : "当前物品";
        LOGGER.info("[UseItemAction] 准备使用物品: {}", itemName);
        this.previousHandItem = ItemStack.EMPTY;
        this.borrowedFromInventory = false;
        this.borrowedSlotIndex = -1;
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        // 确保停止使用物品
        if (entity instanceof Mob mob) {
            if (mob.isUsingItem()) {
                mob.stopUsingItem();
            }
            // 使用完成后将手上的剩余物品归还背包，并恢复原手持
            if (usingHand != null && borrowedFromInventory && mind != null) {
                ItemStack hand = mob.getItemInHand(usingHand);
                if (!hand.isEmpty()) {
                    ItemStack leftover = mind.getInventory().addItem(hand);
                    if (!leftover.isEmpty()) {
                        mob.spawnAtLocation(leftover);
                    }
                }
                mob.setItemInHand(usingHand, previousHandItem);
            }
        }
        LOGGER.info("[UseItemAction] 停止使用物品 | 已使用 {} ticks", useCounter);
    }

    @Override
    public boolean canInterrupt() {
        // 根据 allowInterrupt 参数决定是否可中断
        return allowInterrupt;
    }

    @Override
    public ItemStack getItemStack() {
        return currentStack;
    }

    @Override
    public boolean isUsageComplete() {
        return useCounter >= maxUseTicks; // 或者其他完成条件
    }

    /**
     * 选择可用物品来源：优先手上，其次背包。
     * @param mind NPC 心智
     * @param mob  实体
     * @return 选择结果，找不到返回 null
     */
    private ItemSelection selectItem(INpcMind mind, Mob mob) {
        // 1. 优先检查 preferHand
        if (isItemUsable(mob, preferHand)) {
            return new ItemSelection(preferHand, ItemStack.EMPTY, false, -1);
        }

        // 2. 检查另一只手
        InteractionHand otherHand = preferHand == InteractionHand.MAIN_HAND
            ? InteractionHand.OFF_HAND
            : InteractionHand.MAIN_HAND;
        if (isItemUsable(mob, otherHand)) {
            return new ItemSelection(otherHand, ItemStack.EMPTY, false, -1);
        }

        // 3. 背包搜索（解决副手被占用时无法使用的问题）
        if (mind != null && mind.getInventory() != null) {
            NpcInventory inventory = mind.getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!isInventoryCandidate(stack, mob)) {
                    continue;
                }
                ItemStack toUse = extractUsableStack(inventory, i);
                if (toUse.isEmpty()) {
                    continue;
                }
                return new ItemSelection(preferHand, toUse, true, i);
            }
        }
        return null;
    }

    /**
     * 判断背包中的物品是否可用。
     */
    private boolean isInventoryCandidate(ItemStack stack, Mob mob) {
        if (stack.isEmpty()) {
            return false;
        }
        if (targetItem != null) {
            return stack.getItem() == targetItem;
        }
        return stack.getFoodProperties(mob) != null || stack.getUseDuration(mob) > 0;
    }

    /**
     * 从背包取出一份可用物品（可堆叠则只取 1 个）。
     */
    private ItemStack extractUsableStack(NpcInventory inventory, int slot) {
        ItemStack source = inventory.getItem(slot);
        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (source.getMaxStackSize() > 1 && source.getCount() > 1) {
            ItemStack split = source.copy();
            split.setCount(1);
            source.shrink(1);
            inventory.setItem(slot, source);
            return split;
        }
        return inventory.removeItem(slot);
    }

    /**
     * 选择结果封装。
     */
    private record ItemSelection(
        InteractionHand hand,
        ItemStack stack,
        boolean fromInventory,
        int inventorySlot
    ) { }
}
