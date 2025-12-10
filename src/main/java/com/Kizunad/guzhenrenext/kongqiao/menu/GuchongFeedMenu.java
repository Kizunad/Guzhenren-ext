package com.Kizunad.guzhenrenext.kongqiao.menu;

import com.Kizunad.guzhenrenext.kongqiao.inventory.GuchongFeedInventory;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 蛊虫喂食菜单，复刻玩家 4x9 背包布局。
 */
public class GuchongFeedMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;

    private final GuchongFeedInventory feedInventory;
    private final int containerSlots;

    public GuchongFeedMenu(
        int containerId,
        Inventory playerInventory,
        GuchongFeedInventory feedInventory
    ) {
        super(KongqiaoMenus.GUCHONG_FEED.get(), containerId);
        this.feedInventory = feedInventory;
        this.containerSlots = feedInventory.getContainerSize();
        addFeedSlots();
        addPlayerSlots(playerInventory);
    }

    public static GuchongFeedMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf ignored
    ) {
        return new GuchongFeedMenu(
            containerId,
            inventory,
            new GuchongFeedInventory()
        );
    }

    private void addFeedSlots() {
        for (int i = 0; i < containerSlots; i++) {
            addSlot(new TinyUISlot(feedInventory, i, HIDDEN_POS, HIDDEN_POS));
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                addSlot(
                    new TinyUISlot(
                        playerInventory,
                        index,
                        HIDDEN_POS,
                        HIDDEN_POS
                    )
                );
            }
        }
        for (int col = 0; col < HOTBAR_COLS; col++) {
            addSlot(
                new TinyUISlot(
                    playerInventory,
                    col,
                    HIDDEN_POS,
                    HIDDEN_POS
                )
            );
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < containerSlots) {
            if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, containerSlots, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    public GuchongFeedInventory getInventory() {
        return feedInventory;
    }

    public int getContainerSlots() {
        return containerSlots;
    }
}
