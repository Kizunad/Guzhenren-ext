package com.Kizunad.tinyUI.demo;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Container Menu for the Complex Layout demo.
 * Contains a custom 9xN grid inventory on the left and the player's inventory at the bottom.
 */
public class ComplexLayoutMenu extends AbstractContainerMenu {

    private final Container customInventory;
    private final int customSlotCount;
    
    // Slot indices
    private static final int CUSTOM_INVENTORY_START = 0;
    private int customInventoryEnd;
    private int playerInventoryStart;
    private int playerInventoryEnd;
    private int playerHotbarStart;
    private int playerHotbarEnd;

    private static final int SLOT_SIZE = 18;
    private static final int ROW_LENGTH = 9;
    private static final int HIDDEN_POS = -10000;
    private static final int PLAYER_INV_ROWS = 3;

    /**
     * @param containerId Container ID
     * @param playerInventory Player's inventory
     * @param customSlotCount Number of slots in the custom inventory (any positive count; rows will auto-expand)
     */
    public ComplexLayoutMenu(int containerId, Inventory playerInventory, int customSlotCount) {
        super(null, containerId); // MenuType is null for demo purposes
        this.customSlotCount = Math.max(0, customSlotCount);
        this.customInventory = new SimpleContainer(this.customSlotCount);
        
        // Add custom inventory slots (left side 9xN grid)
        // 使用向上取整，确保非 9 的倍数也能正确放下所有格子
        int rows = (int) Math.ceil(this.customSlotCount / (double) ROW_LENGTH);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < ROW_LENGTH; col++) {
                int index = col + row * ROW_LENGTH;
                if (index >= this.customSlotCount) {
                    break;
                }
                // 位置由 TinyUIContainerScreen 同步，因此此处使用占位坐标
                addSlot(new TinyUISlot(customInventory, index, HIDDEN_POS, HIDDEN_POS));
            }
        }
        customInventoryEnd = slots.size();
        
        // Add player inventory (3x9 grid)
        playerInventoryStart = slots.size();
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < ROW_LENGTH; col++) {
                int index = col + row * ROW_LENGTH + ROW_LENGTH; // Player inventory starts at index 9
                addSlot(new TinyUISlot(playerInventory, index, HIDDEN_POS, HIDDEN_POS));
            }
        }
        playerInventoryEnd = slots.size();
        
        // Add player hotbar (1x9)
        playerHotbarStart = slots.size();
        for (int col = 0; col < ROW_LENGTH; col++) {
            addSlot(new TinyUISlot(playerInventory, col, HIDDEN_POS, HIDDEN_POS));
        }
        playerHotbarEnd = slots.size();
    }

    public int getCustomSlotCount() {
        return customSlotCount;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            
            // From custom inventory to player inventory
            if (index < customInventoryEnd) {
                if (!moveItemStackTo(stack, playerInventoryStart, playerHotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to custom inventory
                if (!moveItemStackTo(stack, CUSTOM_INVENTORY_START, customInventoryEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // Always valid for demo purposes
    }
    
    @Override
    public void removed(Player player) {
        super.removed(player);
        // Drop items from custom inventory when closing
        if (!player.level().isClientSide) {
            for (int i = 0; i < customInventory.getContainerSize(); i++) {
                ItemStack stack = customInventory.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }
    }
}
