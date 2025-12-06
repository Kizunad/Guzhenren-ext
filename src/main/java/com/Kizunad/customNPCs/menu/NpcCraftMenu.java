package com.Kizunad.customNPCs.menu;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * 制造界面菜单，无物品槽，仅用于打开客户端 Craft UI。
 */
public class NpcCraftMenu extends AbstractContainerMenu {

    private static final double MAX_VALID_DIST_SQR = 64.0D;

    private final CustomNpcEntity npc;

    public NpcCraftMenu(
        int containerId,
        Inventory playerInventory,
        CustomNpcEntity npc
    ) {
        super(ModMenus.NPC_CRAFT.get(), containerId);
        this.npc = npc;
    }

    public static NpcCraftMenu fromNetwork(
        int containerId,
        Inventory inv,
        FriendlyByteBuf buf
    ) {
        int entityId = buf.readVarInt();
        Entity entity = inv.player.level().getEntity(entityId);
        if (entity instanceof CustomNpcEntity custom) {
            return new NpcCraftMenu(containerId, inv, custom);
        }
        return new NpcCraftMenu(containerId, inv, null);
    }

    @Override
    public boolean stillValid(Player player) {
        if (npc == null || !npc.isAlive()) {
            return false;
        }
        return npc.distanceToSqr(player) <= MAX_VALID_DIST_SQR;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public int getNpcEntityId() {
        return npc != null ? npc.getId() : -1;
    }
}
