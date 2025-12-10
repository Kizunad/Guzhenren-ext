package com.Kizunad.guzhenrenext.kongqiao.menu;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoSettings;
import com.Kizunad.guzhenrenext.kongqiao.validator.TagBasedKongqiaoSlotValidator;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 空窍界面菜单容器。
 * <p>
 * TinyUI 层负责真正的渲染，本类仅提供服务器权威的物品栏交互。
 * </p>
 */
public class KongqiaoMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;

    private final KongqiaoInventory kongqiaoInventory;
    private final int accessibleSlots;
    private final int totalContainerSlots;

    public KongqiaoMenu(
        int containerId,
        Inventory playerInventory,
        KongqiaoInventory inventory
    ) {
        super(KongqiaoMenus.KONGQIAO.get(), containerId);
        this.kongqiaoInventory = inventory;
        this.accessibleSlots = inventory.getSettings().getUnlockedSlots();
        this.totalContainerSlots = inventory.getContainerSize();

        addKongqiaoSlots();
        addPlayerSlots(playerInventory);
    }

    public static KongqiaoMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        KongqiaoInventory clientInventory = createClientInventory(buf);
        return new KongqiaoMenu(containerId, inventory, clientInventory);
    }

    public static void writeClientData(
        KongqiaoInventory inventory,
        FriendlyByteBuf buf
    ) {
        buf.writeVarInt(inventory.getVisibleRows());
    }

    private static KongqiaoInventory createClientInventory(FriendlyByteBuf buf) {
        int rows = buf.readVarInt();
        KongqiaoSettings settings = new KongqiaoSettings(
            rows,
            KongqiaoConstants.COLUMNS
        );
        return new KongqiaoInventory(settings, new TagBasedKongqiaoSlotValidator());
    }

    private void addKongqiaoSlots() {
        for (int i = 0; i < totalContainerSlots; i++) {
            boolean unlocked = i < accessibleSlots;
            addSlot(new KongqiaoSlot(kongqiaoInventory, i, unlocked));
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
    public void removed(Player player) {
        super.removed(player);
        kongqiaoInventory.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < accessibleSlots) {
            if (!moveItemStackTo(stack, totalContainerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < totalContainerSlots) {
            // 锁定区域，仅允许回滚到玩家
            if (!moveItemStackTo(stack, totalContainerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (
            !moveItemStackTo(stack, 0, Math.max(accessibleSlots, 0), false)
        ) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    private static class KongqiaoSlot extends TinyUISlot {

        private final boolean unlocked;

        KongqiaoSlot(Container container, int index, boolean unlocked) {
            super(container, index, HIDDEN_POS, HIDDEN_POS);
            this.unlocked = unlocked;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return unlocked && super.mayPlace(stack);
        }

        @Override
        public boolean isActive() {
            return unlocked;
        }
    }

    public int getVisibleRows() {
        return kongqiaoInventory.getVisibleRows();
    }

    public int getTotalSlots() {
        return totalContainerSlots;
    }

    public KongqiaoInventory getInventory() {
        return kongqiaoInventory;
    }
}
