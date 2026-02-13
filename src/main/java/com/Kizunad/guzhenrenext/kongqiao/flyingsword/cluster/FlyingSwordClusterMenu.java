package com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster;

import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenus;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 飞剑集群菜单。
 * <p>
 * 用于展示和管理飞剑集群信息。
 * </p>
 */
public class FlyingSwordClusterMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;
    
    // ContainerData 索引定义
    public static final int DATA_COMPUTE_POWER = 0;
    public static final int DATA_MAX_COMPUTE_POWER = 1;
    public static final int DATA_FIELDS = 2;

    private final ContainerData data;

    // 仅用于客户端的构造
    public FlyingSwordClusterMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainerData(DATA_FIELDS));
    }

    public FlyingSwordClusterMenu(
        int containerId,
        Inventory playerInventory,
        ContainerData data
    ) {
        super(KongqiaoMenus.FLYING_SWORD_CLUSTER.get(), containerId);
        this.data = data;

        checkContainerDataCount(data, DATA_FIELDS);

        // 添加玩家背包和快捷栏（虽然UI上可能不需要直接操作，但通常保留以防万一或作为标准实践）
        addPlayerSlots(playerInventory);
        addDataSlots(data);
    }

    public static FlyingSwordClusterMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        return new FlyingSwordClusterMenu(containerId, inventory);
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                addSlot(new TinyUISlot(playerInventory, index, HIDDEN_POS, HIDDEN_POS));
            }
        }
        for (int col = 0; col < HOTBAR_COLS; col++) {
            addSlot(new TinyUISlot(playerInventory, col, HIDDEN_POS, HIDDEN_POS));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public int getComputePower() {
        int fromData = data.get(DATA_COMPUTE_POWER);
        int fromSync = ClusterClientStateCache.getCurrentLoad();
        return Math.max(fromData, fromSync);
    }

    public int getMaxComputePower() {
        int fromData = data.get(DATA_MAX_COMPUTE_POWER);
        int fromSync = ClusterClientStateCache.getMaxComputation();
        return Math.max(fromData, fromSync);
    }
}
