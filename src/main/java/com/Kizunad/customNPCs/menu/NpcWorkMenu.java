package com.Kizunad.customNPCs.menu;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * 工作主界面菜单，占位承载 Work UI，无物品槽。
 */
public class NpcWorkMenu extends AbstractContainerMenu {

    private static final double MAX_VALID_DIST_SQR = 64.0D;

    private final CustomNpcEntity npc;

    public NpcWorkMenu(
        int containerId,
        Inventory playerInventory,
        CustomNpcEntity npc
    ) {
        super(ModMenus.NPC_WORK.get(), containerId);
        this.npc = npc;
    }

    public static NpcWorkMenu fromNetwork(
        int containerId,
        Inventory inv,
        FriendlyByteBuf buf
    ) {
        int entityId = buf.readVarInt();
        Entity entity = inv.player.level().getEntity(entityId);
        if (entity instanceof CustomNpcEntity custom) {
            return new NpcWorkMenu(containerId, inv, custom);
        }
        return new NpcWorkMenu(containerId, inv, null);
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
