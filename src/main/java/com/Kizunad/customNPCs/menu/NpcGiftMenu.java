package com.Kizunad.customNPCs.menu;

import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 赠礼容器：9 个空槽 + 玩家物品栏，确认按钮触发物品转移到 NPC 背包。
 */
public class NpcGiftMenu extends AbstractContainerMenu {

    private static final int GIFT_SLOT_COUNT = 9;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HIDDEN_POS = -10000;
    private static final double MAX_VALID_DIST_SQR = 64.0D;
    private static final int CONFIRM_BUTTON_ID = 0;

    private final SimpleContainer giftSlots = new SimpleContainer(
        GIFT_SLOT_COUNT
    );
    private final CustomNpcEntity npc;
    private final NpcInventory npcInventory;

    public NpcGiftMenu(
        int containerId,
        Inventory playerInventory,
        CustomNpcEntity npc,
        NpcInventory npcInventory
    ) {
        super(ModMenus.NPC_GIFT.get(), containerId);
        this.npc = npc;
        this.npcInventory = npcInventory;

        addGiftSlots();
        addPlayerSlots(playerInventory);
    }

    public static NpcGiftMenu fromNetwork(
        int containerId,
        Inventory inv,
        FriendlyByteBuf buf
    ) {
        int entityId = buf.readVarInt();
        Entity entity = inv.player.level().getEntity(entityId);
        if (entity instanceof CustomNpcEntity custom) {
            var mind = custom.getData(NpcMindAttachment.NPC_MIND);
            if (mind != null) {
                return new NpcGiftMenu(
                    containerId,
                    inv,
                    custom,
                    mind.getInventory()
                );
            }
        }
        return new NpcGiftMenu(containerId, inv, null, new NpcInventory());
    }

    private void addGiftSlots() {
        for (int i = 0; i < GIFT_SLOT_COUNT; i++) {
            addSlot(new TinyUISlot(giftSlots, i, HIDDEN_POS, HIDDEN_POS));
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                addSlot(new TinyUISlot(playerInventory, index, HIDDEN_POS, HIDDEN_POS));
            }
        }
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

            if (index < GIFT_SLOT_COUNT) {
                if (
                    !moveItemStackTo(
                        stack,
                        GIFT_SLOT_COUNT,
                        slots.size(),
                        true
                    )
                ) {
                    return ItemStack.EMPTY;
                }
            } else if (
                !moveItemStackTo(stack, 0, GIFT_SLOT_COUNT, false)
            ) {
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
    public boolean clickMenuButton(Player player, int id) {
        if (id != CONFIRM_BUTTON_ID || npc == null || npcInventory == null) {
            return false;
        }
        transferGiftsToNpc();
        giftSlots.clearContent();
        player.closeContainer();
        return true;
    }

    private void transferGiftsToNpc() {
        for (int i = 0; i < giftSlots.getContainerSize(); i++) {
            ItemStack stack = giftSlots.removeItemNoUpdate(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = npcInventory.addItem(stack);
            if (!leftover.isEmpty() && npc != null) {
                npc.spawnAtLocation(leftover);
            }
        }
    }

    public int getNpcEntityId() {
        return npc == null ? -1 : npc.getId();
    }

    public int getConfirmButtonId() {
        return CONFIRM_BUTTON_ID;
    }
}
