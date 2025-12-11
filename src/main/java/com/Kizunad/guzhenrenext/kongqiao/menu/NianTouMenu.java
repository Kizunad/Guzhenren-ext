package com.Kizunad.guzhenrenext.kongqiao.menu;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class NianTouMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;
    private static final int DATA_FIELDS = 3;
    public static final int IDENTIFY_BUTTON_ID = 99;
    private static final int PLAYER_FIRST_SLOT = 1;
    private static final int PLAYER_LAST_SLOT_EXCLUSIVE =
        PLAYER_FIRST_SLOT + PLAYER_INV_ROWS * PLAYER_INV_COLS + HOTBAR_COLS;

    private final Container container;
    private final ContainerData data;

    public NianTouMenu(int containerId, Inventory playerInventory) {
        this(
            containerId,
            playerInventory,
            new SimpleContainer(1),
            new SimpleContainerData(DATA_FIELDS)
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
        checkContainerDataCount(data, DATA_FIELDS);
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
            new SimpleContainerData(DATA_FIELDS)
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
        if (id == IDENTIFY_BUTTON_ID) {
            ItemStack stack = container.getItem(0);
            if (stack.isEmpty()) {
                return false;
            }

            NianTouData data = NianTouDataManager.getData(stack);
            if (data == null) {
                return false;
            }

            // TODO: 检查消耗 (真元/魂魄)
            // if (!consumeResources(player, data)) return false;

            if (player instanceof ServerPlayer serverPlayer) {
                NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(
                    serverPlayer
                );
                if (unlocks != null) {
                    ResourceLocation itemId =
                        BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (!unlocks.isUnlocked(itemId)) {
                        unlocks.unlock(itemId);
                        // 同步给客户端
                        PacketDistributor.sendToPlayer(
                            serverPlayer,
                            new PacketSyncNianTouUnlocks(
                                unlocks.getUnlockedItems()
                            )
                        );
                    }
                }
            }
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
                if (
                    !this.moveItemStackTo(
                        itemstack1,
                        PLAYER_FIRST_SLOT,
                        PLAYER_LAST_SLOT_EXCLUSIVE,
                        true
                    )
                ) {
                    return ItemStack.EMPTY;
                }
            } else if (
                !this.moveItemStackTo(
                    itemstack1,
                    0,
                    PLAYER_FIRST_SLOT,
                    false
                )
            ) {
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
