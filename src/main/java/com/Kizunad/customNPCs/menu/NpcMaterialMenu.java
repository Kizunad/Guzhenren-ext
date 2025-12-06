package com.Kizunad.customNPCs.menu;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * 材料转化界面容器：承载材料输入槽与玩家背包。
 */
public class NpcMaterialMenu extends AbstractContainerMenu implements MaterialSlotProvider {

    public static final int MATERIAL_SLOT_COUNT = 9;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HIDDEN_POS = -1000;
    private static final double MAX_VALID_DIST_SQR = 64.0D;

    private final SimpleContainer materialSlots = new SimpleContainer(MATERIAL_SLOT_COUNT);
    private final List<Slot> materialSlotViews = new ArrayList<>();
    private final CustomNpcEntity npc;

    public NpcMaterialMenu(
        int containerId,
        Inventory playerInventory,
        CustomNpcEntity npc
    ) {
        super(ModMenus.NPC_MATERIAL.get(), containerId);
        this.npc = npc;

        addMaterialSlots();
        addPlayerSlots(playerInventory);
    }

    public static NpcMaterialMenu fromNetwork(
        int containerId,
        Inventory inv,
        FriendlyByteBuf buf
    ) {
        int entityId = buf.readVarInt();
        Entity entity = inv.player.level().getEntity(entityId);
        if (entity instanceof CustomNpcEntity custom) {
            return new NpcMaterialMenu(containerId, inv, custom);
        }
        return new NpcMaterialMenu(containerId, inv, null);
    }

    private void addMaterialSlots() {
        for (int i = 0; i < MATERIAL_SLOT_COUNT; i++) {
            TinyUISlot slot = new TinyUISlot(materialSlots, i, HIDDEN_POS, HIDDEN_POS);
            materialSlotViews.add(addSlot(slot));
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        // 背包 3 行
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                addSlot(new TinyUISlot(playerInventory, index, HIDDEN_POS, HIDDEN_POS));
            }
        }
        // 快捷栏
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            addSlot(new TinyUISlot(playerInventory, col, HIDDEN_POS, HIDDEN_POS));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < MATERIAL_SLOT_COUNT) {
                if (!moveItemStackTo(stack, MATERIAL_SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, MATERIAL_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
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
        if (npc == null || !npc.isAlive()) {
            return false;
        }
        return npc.distanceToSqr(player) <= MAX_VALID_DIST_SQR;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            clearContainer(player, materialSlots);
        }
    }

    @Override
    public List<Slot> getMaterialSlots() {
        return Collections.unmodifiableList(materialSlotViews);
    }

    public int getNpcEntityId() {
        return npc != null ? npc.getId() : -1;
    }
}
