package com.Kizunad.guzhenrenext.kongqiao.menu;

import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NianTouMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;

    private final Container container;
    private final ContainerData data;

    public NianTouMenu(int containerId, Inventory playerInventory) {
        this(
            containerId,
            playerInventory,
            new SimpleContainer(1),
            new SimpleContainerData(3)
        );
    }

    public NianTouMenu(
        int containerId,
        Inventory playerInventory,
        Container container,
        ContainerData data
    ) {
        super(KongqiaoMenus.NIANTOU.get(), containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 3);
        container.startOpen(playerInventory.player);

        // NianTou Slot (Index 0)
        this.addSlot(new TinyUISlot(container, 0, HIDDEN_POS, HIDDEN_POS));

        addPlayerSlots(playerInventory);
        addDataSlots(data);
    }

    public static NianTouMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        return new NianTouMenu(
            containerId,
            inventory,
            new SimpleContainer(1),
            new SimpleContainerData(3)
        );
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
                new TinyUISlot(playerInventory, col, HIDDEN_POS, HIDDEN_POS)
            );
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 99) {
            // TODO: Implement Identify Logic
            // Check cost, start progress, or instant identify
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getTotalTime() {
        return data.get(1);
    }

    public int getCost() {
        return data.get(2);
    }

    public Container getContainer() {
        return container;
    }
}
