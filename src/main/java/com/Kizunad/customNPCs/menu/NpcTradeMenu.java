package com.Kizunad.customNPCs.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 交易界面容器占位符。
 * 实际逻辑需配合 Slot 添加和 MenuType 注册。
 */
public class NpcTradeMenu extends AbstractContainerMenu {

    private static final int OFFER_GRID_SIZE = 9;
    private static final int OFFER_GRID_COLUMNS = 3;
    private static final int PLAYER_INVENTORY_ROWS = 3;
    private static final int PLAYER_INVENTORY_COLUMNS = 9;
    private static final int PLAYER_HOTBAR_SIZE = 9;

    // 0-8: Player Offer
    public final Container playerOffer;
    // 9-17: NPC Offer
    public final Container npcOffer;

    private float priceMultiplier = 1.0f;
    private int npcEntityId = -1;

    public NpcTradeMenu(
        int containerId,
        Inventory playerInventory,
        FriendlyByteBuf extraData
    ) {
        this(containerId, playerInventory);
        if (extraData != null) {
            this.npcEntityId = extraData.readVarInt();
            this.priceMultiplier = extraData.readFloat();
        }
    }

    public NpcTradeMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.NPC_TRADE.get(), containerId);
        this.playerOffer = new SimpleContainer(OFFER_GRID_SIZE);
        this.npcOffer = new SimpleContainer(OFFER_GRID_SIZE);

        // 0-8: Player Offer
        for (int i = 0; i < OFFER_GRID_SIZE; i++) {
            this.addSlot(new Slot(playerOffer, i, 0, 0));
        }

        // 9-17: NPC Offer (Read-only)
        for (int i = 0; i < OFFER_GRID_SIZE; i++) {
            this.addSlot(
                new Slot(npcOffer, i, 0, 0) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                }
            );
        }

        // 18+: Player Inventory
        // Main inventory
        for (int r = 0; r < PLAYER_INVENTORY_ROWS; ++r) {
            for (int c = 0; c < PLAYER_INVENTORY_COLUMNS; ++c) {
                addSlot(
                    new Slot(
                        playerInventory,
                        c +
                            r * PLAYER_INVENTORY_COLUMNS +
                            PLAYER_INVENTORY_COLUMNS,
                        0,
                        0
                    )
                );
            }
        }
        // Hotbar
        for (int c = 0; c < PLAYER_HOTBAR_SIZE; ++c) {
            addSlot(new Slot(playerInventory, c, 0, 0));
        }
    }

    public static NpcTradeMenu fromNetwork(
        int containerId,
        Inventory inv,
        FriendlyByteBuf buf
    ) {
        return new NpcTradeMenu(containerId, inv, buf);
    }

    public float getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(float priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public int getNpcEntityId() {
        return npcEntityId;
    }

    public void setNpcEntityId(int npcEntityId) {
        this.npcEntityId = npcEntityId;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 简易实现：不允许 shift-click 移动，或者暂留空
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
