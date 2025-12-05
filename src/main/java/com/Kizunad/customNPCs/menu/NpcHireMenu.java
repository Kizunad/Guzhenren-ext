package com.Kizunad.customNPCs.menu;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 雇佣容器：3x9 槽位用于放置支付物品，确认按钮根据估值判定是否雇佣成功。
 * <p>
 * - 关闭窗口且未成功雇佣时，物品原路返还给玩家。
 * - 估值包含物品堆叠数量，避免忽略大组物品价值。
 */
public class NpcHireMenu extends AbstractContainerMenu {

    private static final int HIRE_SLOT_COUNT = 27;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HIDDEN_POS = -10000;
    private static final double MAX_VALID_DIST_SQR = 64.0D;
    private static final int CONFIRM_BUTTON_ID = 0;
    private static final double ENCHANT_VALUE_DIVISOR = 5.0D;

    private final SimpleContainer hireSlots = new SimpleContainer(
        HIRE_SLOT_COUNT
    );
    private final CustomNpcEntity npc;
    private final NpcInventory npcInventory;
    private boolean hireCompleted;

    public NpcHireMenu(
        int containerId,
        Inventory playerInventory,
        CustomNpcEntity npc,
        NpcInventory npcInventory
    ) {
        super(ModMenus.NPC_HIRE.get(), containerId);
        this.npc = npc;
        this.npcInventory = npcInventory;

        addHireSlots();
        addPlayerSlots(playerInventory);
    }

    public static NpcHireMenu fromNetwork(
        int containerId,
        Inventory inv,
        FriendlyByteBuf buf
    ) {
        int entityId = buf.readVarInt();
        Entity entity = inv.player.level().getEntity(entityId);
        if (entity instanceof CustomNpcEntity custom) {
            var mind = custom.getData(NpcMindAttachment.NPC_MIND);
            if (mind != null) {
                return new NpcHireMenu(
                    containerId,
                    inv,
                    custom,
                    mind.getInventory()
                );
            }
        }
        return new NpcHireMenu(containerId, inv, null, new NpcInventory());
    }

    private void addHireSlots() {
        for (int i = 0; i < HIRE_SLOT_COUNT; i++) {
            addSlot(new TinyUISlot(hireSlots, i, HIDDEN_POS, HIDDEN_POS));
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

            if (index < HIRE_SLOT_COUNT) {
                if (
                    !moveItemStackTo(
                        stack,
                        HIRE_SLOT_COUNT,
                        slots.size(),
                        true
                    )
                ) {
                    return ItemStack.EMPTY;
                }
            } else if (
                !moveItemStackTo(stack, 0, HIRE_SLOT_COUNT, false)
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
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        boolean success = handleHireFlow(serverPlayer);
        if (success) {
            hireCompleted = true;
            hireSlots.clearContent();
            player.closeContainer();
        }
        return success;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!hireCompleted) {
            returnItemsToPlayer(player);
        }
    }

    private boolean handleHireFlow(ServerPlayer player) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return false;
        }
        var candidate = mind
            .getMemory()
            .getMemory(WorldStateKeys.HIRE_CANDIDATE, java.util.UUID.class);
        if (candidate != null && !candidate.equals(player.getUUID())) {
            player.displayClientMessage(
                Component.literal("Only the designated player can confirm this hire."),
                false
            );
            return false;
        }
        Double required = mind
            .getMemory()
            .getMemory(WorldStateKeys.HIRE_REQUIRED_VALUE, Double.class);
        if (required == null) {
            required = 0.0D;
        }
        double offered = calculateOfferValue();
        if (offered < required) {
            player.displayClientMessage(
                Component.literal(
                    "Offer value too low: " +
                    String.format("%.1f", offered) +
                    " / " +
                    String.format("%.1f", required)
                ),
                false
            );
            return false;
        }

        transferHireItemsToNpc();
        mind
            .getMemory()
            .rememberLongTerm(WorldStateKeys.OWNER_UUID, player.getUUID());
        mind
            .getMemory()
            .rememberLongTerm(WorldStateKeys.RELATIONSHIP_TYPE, "HIRED");
        mind.getMemory().forget(WorldStateKeys.HIRE_PENDING);
        mind.getMemory().forget(WorldStateKeys.HIRE_REQUIRED_VALUE);
        mind.getMemory().forget(WorldStateKeys.HIRE_CANDIDATE);
        player.displayClientMessage(
            Component.literal(
                "You hired " + npc.getDisplayName().getString() + "!"
            ),
            false
        );
        return true;
    }

    private void transferHireItemsToNpc() {
        for (int i = 0; i < hireSlots.getContainerSize(); i++) {
            ItemStack stack = hireSlots.removeItemNoUpdate(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack leftover = npcInventory.addItem(stack);
            if (!leftover.isEmpty() && npc != null) {
                npc.spawnAtLocation(leftover);
            }
        }
    }

    private void returnItemsToPlayer(Player player) {
        for (int i = 0; i < hireSlots.getContainerSize(); i++) {
            ItemStack stack = hireSlots.removeItemNoUpdate(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (player.isAlive()) {
                player.getInventory().placeItemBackInInventory(stack);
            } else if (npc != null) {
                npc.spawnAtLocation(stack);
            }
        }
    }

    private double calculateOfferValue() {
        double total = 0.0D;
        for (int i = 0; i < hireSlots.getContainerSize(); i++) {
            ItemStack stack = hireSlots.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            double rarityWeight = stack.getRarity().ordinal() + 1.0D;
            double enchantWeight = Math.max(
                1.0D,
                stack.getEnchantmentValue() / ENCHANT_VALUE_DIVISOR
            );
            total += stack.getCount() * rarityWeight * enchantWeight;
        }
        return total;
    }

    public int getNpcEntityId() {
        return npc == null ? -1 : npc.getId();
    }

    public int getConfirmButtonId() {
        return CONFIRM_BUTTON_ID;
    }
}
