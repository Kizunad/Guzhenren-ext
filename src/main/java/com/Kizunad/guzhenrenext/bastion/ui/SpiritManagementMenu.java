package com.Kizunad.guzhenrenext.bastion.ui;

import com.Kizunad.guzhenrenext.bastion.SpiritData;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 地灵行囊与管理菜单。
 * <p>
 * 包含 9 格地灵行囊（Indices 0-8）与玩家物品栏（Indices 9-44）。
 * </p>
 */
public class SpiritManagementMenu extends AbstractContainerMenu {

    // 9 格虚拟行囊
    private final SimpleContainer spiritVault;
    // 玩家物品栏引用
    private final Inventory playerInventory;

    // 槽位索引范围定义
    private static final int VAULT_SLOTS = 9;
    private static final int VAULT_START = 0;
    private static final int VAULT_END = VAULT_START + VAULT_SLOTS;

    private static final int PLAYER_INV_START = VAULT_END;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27; // 3行 * 9

    private static final int PLAYER_HOTBAR_START = PLAYER_INV_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;
    
    // 玩家背包行数
    private static final int PLAYER_INV_ROWS = 3;
    // 玩家背包列数
    private static final int PLAYER_INV_COLS = 9;
    // 玩家快捷栏槽位数
    private static final int PLAYER_HOTBAR_SLOTS = 9;
    // 玩家背包起始索引（对于玩家 Inventory 自身而言）
    private static final int PLAYER_INV_OFFSET = 9;

    // 虚拟位置，由 TinyUIContainerScreen 负责布局
    private static final int HIDDEN_POS = -10000;

    /**
     * 客户端构造函数（通常由 MenuType 注册时调用）。
     *
     * @param containerId 容器 ID
     * @param playerInventory 玩家物品栏
     */
    public SpiritManagementMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, SpiritData.DEFAULT);
    }

    /**
     * 服务端/通用构造函数。
     *
     * @param containerId 容器 ID
     * @param playerInventory 玩家物品栏
     * @param spiritData 地灵数据（用于初始化行囊内容）
     */
    public SpiritManagementMenu(int containerId, Inventory playerInventory, SpiritData spiritData) {
        super(BastionMenus.SPIRIT_MANAGEMENT_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.spiritVault = new SimpleContainer(VAULT_SLOTS);

        // 初始化行囊内容
        if (spiritData != null && spiritData.vaultItems() != null) {
            for (int i = 0; i < Math.min(VAULT_SLOTS, spiritData.vaultItems().size()); i++) {
                this.spiritVault.setItem(i, spiritData.vaultItems().get(i));
            }
        }

        // 添加地灵行囊槽位 (0-8)
        for (int i = 0; i < VAULT_SLOTS; i++) {
            addSlot(new TinyUISlot(spiritVault, i, HIDDEN_POS, HIDDEN_POS));
        }

        // 添加玩家物品栏槽位 (9-35)
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                addSlot(new TinyUISlot(
                    playerInventory,
                    col + row * PLAYER_INV_COLS + PLAYER_INV_OFFSET,
                    HIDDEN_POS,
                    HIDDEN_POS
                ));
            }
        }

        // 添加玩家快捷栏槽位 (36-44)
        for (int i = 0; i < PLAYER_HOTBAR_SLOTS; i++) {
            addSlot(new TinyUISlot(playerInventory, i, HIDDEN_POS, HIDDEN_POS));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < VAULT_END) {
                // 从地灵行囊移动到玩家物品栏
                if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家物品栏移动到地灵行囊
                if (!moveItemStackTo(stack, VAULT_START, VAULT_END, false)) {
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
        return spiritVault.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // 注意：地灵行囊数据需要同步回 BastionData/SpiritData。
        // 此处仅作为内存中的临时容器，实际上应该在 Slot 变动时
        // 通过网络包或直接操作 SpiritData 进行持久化更新。
        // 由于 SpiritData 是 record，通常是在操作结束或 slot changed 时
        // 触发保存逻辑。本实现假设数据同步由外部逻辑（如 BlockEntity 或 Capability）处理。
    }

    public SimpleContainer getSpiritVault() {
        return spiritVault;
    }
}
