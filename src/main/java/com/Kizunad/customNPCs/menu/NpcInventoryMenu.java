package com.Kizunad.customNPCs.menu;

import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.SimpleContainer;

public class NpcInventoryMenu extends AbstractContainerMenu {

    private static final int ARMOR_SLOT_COUNT = 4;
    public static final int NPC_MAIN_START = 0;
    public static final int NPC_MAIN_COUNT = NpcInventory.MAIN_SIZE;
    public static final int NPC_EQUIP_START = NPC_MAIN_START + NPC_MAIN_COUNT;
    public static final int NPC_EQUIP_COUNT = 1 + ARMOR_SLOT_COUNT + 1; // main hand + armor + offhand
    public static final int EQUIP_MAIN_HAND_INDEX = NPC_EQUIP_START;
    public static final int EQUIP_ARMOR_START = EQUIP_MAIN_HAND_INDEX + 1;
    public static final int EQUIP_ARMOR_COUNT = ARMOR_SLOT_COUNT;
    public static final int EQUIP_OFFHAND_INDEX =
        EQUIP_ARMOR_START + EQUIP_ARMOR_COUNT;
    public static final int TOTAL_NPC_SLOTS = NPC_EQUIP_START + NPC_EQUIP_COUNT;

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HIDDEN_POS = -10000;
    private static final double MAX_VALID_DIST_SQR = 64.0D;

    private final NpcInventory inventory;
    private final CustomNpcEntity npc;
    private final Level level;

    public NpcInventoryMenu(
        int containerId,
        Inventory playerInventory,
        CustomNpcEntity npc,
        NpcInventory inventory
    ) {
        super(ModMenus.NPC_INVENTORY.get(), containerId);
        this.inventory = inventory;
        this.npc = npc;
        this.level = playerInventory.player.level();
        if (inventory != null) {
            inventory.setViewer(playerInventory.player);
        }

        addNpcSlots();
        addPlayerSlots(playerInventory);
    }

    public static NpcInventoryMenu fromNetwork(int containerId, Inventory inv, FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        Level level = inv.player.level();
        Entity entity = level.getEntity(entityId);
        if (entity instanceof CustomNpcEntity custom) {
            var mind = custom.getData(
                com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND
            );
            return new NpcInventoryMenu(containerId, inv, custom, mind.getInventory());
        }
        return new NpcInventoryMenu(containerId, inv, null, new NpcInventory());
    }

    private void addNpcSlots() {
        // 主背包 4 行 x 9
        for (int i = 0; i < NPC_MAIN_COUNT; i++) {
            addSlot(new TinyUISlot(inventory, NPC_MAIN_START + i, HIDDEN_POS, HIDDEN_POS));
        }
        // 主手
        addSlot(
            new EquipmentSlotProxy(
                npc,
                EquipmentSlot.MAINHAND,
                EQUIP_MAIN_HAND_INDEX,
                HIDDEN_POS,
                HIDDEN_POS
            )
        );
        // 护甲（头、胸、腿、脚）
        addSlot(
            new EquipmentSlotProxy(
                npc,
                EquipmentSlot.HEAD,
                EQUIP_ARMOR_START,
                HIDDEN_POS,
                HIDDEN_POS
            )
        );
        addSlot(
            new EquipmentSlotProxy(
                npc,
                EquipmentSlot.CHEST,
                EQUIP_ARMOR_START + 1,
                HIDDEN_POS,
                HIDDEN_POS
            )
        );
        addSlot(
            new EquipmentSlotProxy(
                npc,
                EquipmentSlot.LEGS,
                EQUIP_ARMOR_START + 2,
                HIDDEN_POS,
                HIDDEN_POS
            )
        );
        addSlot(
            new EquipmentSlotProxy(
                npc,
                EquipmentSlot.FEET,
                EQUIP_ARMOR_START + (ARMOR_SLOT_COUNT - 1),
                HIDDEN_POS,
                HIDDEN_POS
            )
        );
        // 副手
        addSlot(
            new EquipmentSlotProxy(
                npc,
                EquipmentSlot.OFFHAND,
                EQUIP_OFFHAND_INDEX,
                HIDDEN_POS,
                HIDDEN_POS
            )
        );
    }

    private void addPlayerSlots(Inventory playerInventory) {
        // Player inventory
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                addSlot(new TinyUISlot(playerInventory, index, HIDDEN_POS, HIDDEN_POS));
            }
        }
        // Hotbar
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            addSlot(new TinyUISlot(playerInventory, col, HIDDEN_POS, HIDDEN_POS));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < TOTAL_NPC_SLOTS) {
                // NPC -> player
                if (!moveItemStackTo(
                    stack,
                    TOTAL_NPC_SLOTS,
                    slots.size(),
                    true
                )) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player -> NPC main first, then armor/offhand
                if (
                    !moveItemStackTo(stack, NPC_MAIN_START, NPC_MAIN_START + NPC_MAIN_COUNT, false) &&
                    !moveItemStackTo(stack, NPC_EQUIP_START, NPC_EQUIP_START + NPC_EQUIP_COUNT, false)
                ) {
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
        if (npc == null || !npc.isAlive()) {
            return false;
        }
        return npc.distanceToSqr(player) <= MAX_VALID_DIST_SQR;
    }

    public CustomNpcEntity getNpc() {
        return npc;
    }

    public NpcInventory getInventory() {
        return inventory;
    }

    private static class EquipmentSlotProxy extends Slot {

        private static final SimpleContainer DUMMY = new SimpleContainer(1);
        private final CustomNpcEntity npc;
        private final EquipmentSlot slotType;

        EquipmentSlotProxy(
            CustomNpcEntity npc,
            EquipmentSlot slotType,
            int index,
            int x,
            int y
        ) {
            super(DUMMY, index, x, y);
            this.npc = npc;
            this.slotType = slotType;
        }

        @Override
        public ItemStack getItem() {
            return npc == null ? ItemStack.EMPTY : npc.getItemBySlot(slotType);
        }

        @Override
        public void set(ItemStack stack) {
            if (npc != null) {
                npc.setItemSlot(slotType, stack);
            }
            setChanged();
        }

        @Override
        public ItemStack remove(int amount) {
            if (npc == null || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack current = npc.getItemBySlot(slotType);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted;
            if (amount >= current.getCount()) {
                extracted = current.copy();
                npc.setItemSlot(slotType, ItemStack.EMPTY);
            } else {
                extracted = current.copy();
                extracted.setCount(amount);
                current.shrink(amount);
                npc.setItemSlot(slotType, current);
            }
            setChanged();
            return extracted;
        }

        @Override
        public boolean mayPickup(Player player) {
            return npc != null && !getItem().isEmpty();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            setChanged();
            super.onTake(player, stack);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (npc == null) {
                return false;
            }
            // 主手/副手均可放任意物品；护甲按槽位匹配
            if (slotType.getType() == EquipmentSlot.Type.HAND) {
                return true;
            }
            if (slotType.isArmor()) {
                return stack.canEquip(slotType, npc);
            }
            return true;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            if (slotType.isArmor()) {
                return 1;
            }
            return super.getMaxStackSize(stack);
        }
    }
}
