package com.Kizunad.guzhenrenext.xianqiao.resource;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 资源控制器容器菜单。
 */
public class ResourceControllerMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;

    private static final int PLAYER_INV_COLS = 9;

    private static final int HOTBAR_COLS = 9;

    private static final int OUTPUT_SLOT_X = 80;

    private static final int OUTPUT_SLOT_Y = 35;

    private static final int INPUT_SLOT_X = 56;

    private static final int INPUT_SLOT_Y = 35;

    private static final int PLAYER_INV_X_START = 8;

    private static final int PLAYER_INV_Y_START = 84;

    private static final int HOTBAR_Y = 142;

    private static final int SLOT_SPACING = 18;

    private static final int DATA_PROGRESS_INT = 0;

    private static final int DATA_PROGRESS_SCALE = 1;

    private static final int DATA_FORMED = 2;

    private static final int DATA_EFFICIENCY_PERCENT = 3;

    private static final int DATA_AURA = 4;

    private static final int DATA_REMAINING_TICKS = 5;

    private static final int DATA_FIELDS = 6;

    private static final int PROGRESS_PERMILLE_BASE = 1000;

    private final Container container;

    private final ContainerData data;

    public ResourceControllerMenu(int containerId, Inventory playerInventory) {
        this(
            containerId,
            playerInventory,
            new SimpleContainer(ResourceControllerBlockEntity.INVENTORY_SIZE),
            new SimpleContainerData(DATA_FIELDS)
        );
    }

    public ResourceControllerMenu(
        int containerId,
        Inventory playerInventory,
        Container container,
        ContainerData data
    ) {
        super(XianqiaoMenus.RESOURCE_CONTROLLER.get(), containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(container, ResourceControllerBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(data, DATA_FIELDS);
        container.startOpen(playerInventory.player);

        addControllerSlots();
        addPlayerSlots(playerInventory);
        addDataSlots(data);
    }

    /**
     * 网络构造：用于客户端接收菜单打开事件。
     */
    public static ResourceControllerMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        buf.readBlockPos();
        return new ResourceControllerMenu(
            containerId,
            inventory,
            new SimpleContainer(ResourceControllerBlockEntity.INVENTORY_SIZE),
            new SimpleContainerData(DATA_FIELDS)
        );
    }

    private void addControllerSlots() {
        addSlot(new OutputSlot(container, ResourceControllerBlockEntity.SLOT_OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y));
        addSlot(new Slot(container, ResourceControllerBlockEntity.SLOT_INPUT, INPUT_SLOT_X, INPUT_SLOT_Y));
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                int x = PLAYER_INV_X_START + col * SLOT_SPACING;
                int y = PLAYER_INV_Y_START + row * SLOT_SPACING;
                addSlot(new Slot(playerInventory, index, x, y));
            }
        }
        for (int col = 0; col < HOTBAR_COLS; col++) {
            int x = PLAYER_INV_X_START + col * SLOT_SPACING;
            addSlot(new Slot(playerInventory, col, x, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copied = stack.copy();

        int containerSlots = ResourceControllerBlockEntity.INVENTORY_SIZE;
        if (index < containerSlots) {
            if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, ResourceControllerBlockEntity.SLOT_INPUT, containerSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copied;
    }

    /**
     * 获取当前结构是否形成。
     */
    public boolean isFormed() {
        return data.get(DATA_FORMED) != 0;
    }

    /**
     * 获取进度千分比（0~1000）。
     */
    public int getProgressPermille() {
        int progressInt = data.get(DATA_PROGRESS_INT);
        int progressScale = data.get(DATA_PROGRESS_SCALE);
        if (progressScale <= 0) {
            return 0;
        }
        return Math.min(PROGRESS_PERMILLE_BASE, (progressInt * PROGRESS_PERMILLE_BASE) / progressScale);
    }

    /**
     * 获取效率百分比。
     */
    public int getEfficiencyPercent() {
        return data.get(DATA_EFFICIENCY_PERCENT);
    }

    /**
     * 获取环境灵气值。
     */
    public int getAuraValue() {
        return data.get(DATA_AURA);
    }

    /**
     * 获取预估剩余 tick。
     */
    public int getRemainingTicks() {
        return data.get(DATA_REMAINING_TICKS);
    }

    /**
     * 产出槽只允许取出，不允许放入。
     */
    private static final class OutputSlot extends Slot {

        OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
