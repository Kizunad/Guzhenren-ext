package com.Kizunad.guzhenrenext.xianqiao.spirit;

import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuItem;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 集群 NPC 交互菜单。
 * <p>
 * 提供一个槽位用于放置储物蛊，以便 NPC 将产出推送进去。
 * 同时同步 NPC 的状态数据（产出、效率等）。
 * </p>
 */
public class ClusterNpcMenu extends AbstractContainerMenu {

    private final ClusterNpcEntity npc;
    private final ContainerData data;

    // --- 布局常量 ---
    private static final int SLOT_STORAGE_GU_X = 80;
    private static final int SLOT_STORAGE_GU_Y = 35;
    
    private static final int SLOT_SPACING = 18;
    private static final int PLAYER_INV_X_START = 8;
    private static final int PLAYER_INV_Y_START = 84;
    private static final int HOTBAR_Y = 142;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int TOTAL_PLAYER_INV_SLOTS = 36;
    private static final int HOTBAR_SLOT_START = 27;
    private static final int PLAYER_INV_END = 37;

    // --- 数据槽索引 ---
    // 0: pendingOutput (low), 1: pendingOutput (high)
    // 2: efficiencyBase (percent)
    // 3: workType (enum ordinal or mapped int) - 暂未完全实现，预留
    private static final int DATA_COUNT = 4;
    private static final int SHIFT_HIGH_32_BIT = 32;

    private static final double MAX_INTERACT_DISTANCE_SQR = 64.0D;

    public ClusterNpcMenu(int containerId, Inventory playerInventory, ClusterNpcEntity npc) {
        this(containerId, playerInventory, npc, new SimpleContainerData(DATA_COUNT));
    }

    public ClusterNpcMenu(int containerId, Inventory playerInventory, ClusterNpcEntity npc, ContainerData data) {
        super(XianqiaoMenus.CLUSTER_NPC.get(), containerId);
        this.npc = npc;
        this.data = data;
        
        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);

        // 0号槽位：储物蛊专用槽
        addSlot(new Slot(npc.getInventory(), 0, SLOT_STORAGE_GU_X, SLOT_STORAGE_GU_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof StorageGuItem;
            }
        });

        addPlayerSlots(playerInventory);
    }

    public static ClusterNpcMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        if (inventory.player.level().getEntity(entityId) instanceof ClusterNpcEntity npc) {
            return new ClusterNpcMenu(containerId, inventory, npc);
        }
        throw new IllegalStateException("Entity not found or not ClusterNpcEntity: " + entityId);
    }

    private void addPlayerSlots(Inventory playerInventory) {
        // 玩家背包
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                int x = PLAYER_INV_X_START + col * SLOT_SPACING;
                int y = PLAYER_INV_Y_START + row * SLOT_SPACING;
                addSlot(new Slot(playerInventory, index, x, y));
            }
        }
        // 快捷栏
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            int x = PLAYER_INV_X_START + col * SLOT_SPACING;
            addSlot(new Slot(playerInventory, col, x, HOTBAR_Y));
        }
    }

    public ClusterNpcEntity getNpc() {
        return npc;
    }

    /**
     * 获取待消费产出总量（从 ContainerData 还原 long）。
     */
    public long getPendingOutput() {
        long low = Integer.toUnsignedLong(data.get(0));
        long high = Integer.toUnsignedLong(data.get(1));
        return low | (high << SHIFT_HIGH_32_BIT);
    }

    @Override
    public boolean stillValid(Player player) {
        return npc.isAlive() && player.distanceToSqr(npc) < MAX_INTERACT_DISTANCE_SQR;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // 0 是 NPC 储物蛊槽位
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                // 尝试从玩家背包移动到 NPC 槽位
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
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
}
