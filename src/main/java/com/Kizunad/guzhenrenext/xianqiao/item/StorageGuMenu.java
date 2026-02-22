package com.Kizunad.guzhenrenext.xianqiao.item;

import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 储物蛊菜单。
 * <p>
 * 提供储物蛊的基础交互容器，主要用于展示蛊内物品。
 * 对应 Item: {@link StorageGuItem}
 * </p>
 */
public class StorageGuMenu extends AbstractContainerMenu {

    private final ItemStack stack;

    // --- 布局常量 ---
    private static final int SLOT_SPACING = 18;
    private static final int PLAYER_INV_X_START = 8;
    private static final int PLAYER_INV_Y_START = 84;
    private static final int HOTBAR_Y = 142;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int TOTAL_PLAYER_INV_SLOTS = 36;
    private static final int HOTBAR_SLOT_START = 27;

    /**
     * 服务端/通用构造。
     *
     * @param containerId 容器 ID
     * @param playerInventory 玩家背包
     * @param stack 储物蛊物品堆栈
     */
    public StorageGuMenu(int containerId, Inventory playerInventory, ItemStack stack) {
        super(XianqiaoMenus.STORAGE_GU.get(), containerId);
        this.stack = stack;
        addPlayerSlots(playerInventory);
    }

    /**
     * 网络构造：用于客户端接收菜单打开事件。
     */
    public static StorageGuMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        return new StorageGuMenu(containerId, inventory, inventory.player.getItemInHand(hand));
    }

    private void addPlayerSlots(Inventory playerInventory) {
        // 玩家背包
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                int x = PLAYER_INV_X_START + col * SLOT_SPACING;
                int y = PLAYER_INV_Y_START + row * SLOT_SPACING;
                addSlot(new Slot(playerInventory, index, x, y));
            }
        }
        // 快捷栏
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            int x = PLAYER_INV_X_START + col * SLOT_SPACING;
            addSlot(new Slot(playerInventory, col, x, HOTBAR_Y));
        }
    }

    /**
     * 获取当前操作的储物蛊物品堆栈。
     */
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        // 简单校验：物品必须还在手中且是储物蛊
        return !stack.isEmpty() && stack.getItem() instanceof StorageGuItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack copiedStack = slotStack.copy();

        // 仅在玩家背包和快捷栏之间移动，不涉及容器内部槽位（因为储物蛊是虚拟存储）
        if (index < HOTBAR_SLOT_START) { // 玩家背包区域 (0-26) -> 移动到快捷栏 (27-35)
            if (!moveItemStackTo(slotStack, HOTBAR_SLOT_START, TOTAL_PLAYER_INV_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else { // 快捷栏区域 (27-35) -> 移动到玩家背包 (0-26)
            if (!moveItemStackTo(slotStack, 0, HOTBAR_SLOT_START, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copiedStack;
    }
}
