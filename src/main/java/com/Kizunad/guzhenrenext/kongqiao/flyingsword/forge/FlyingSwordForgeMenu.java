package com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenus;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
 * 飞剑培养菜单。
 * <p>
 * 提供一个输入槽用于投喂核心剑/材料剑/蛊虫，
 * 通过 ContainerData 同步进度到客户端。
 * </p>
 */
public class FlyingSwordForgeMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;

    public static final int DATA_ACTIVE = 0;
    public static final int DATA_FED_SWORD_COUNT = 1;
    public static final int DATA_REQUIRED_SWORD_COUNT = 2;
    public static final int DATA_CAN_CLAIM = 3;
    public static final int DATA_FIELDS = 4;

    public static final int BUTTON_CLAIM = 1;
    public static final int BUTTON_CANCEL = 2;

    private final Container inputContainer;
    private final ContainerData data;
    private final Player player;

    public FlyingSwordForgeMenu(int containerId, Inventory playerInventory) {
        this(
            containerId,
            playerInventory,
            new SimpleContainer(1),
            new SimpleContainerData(DATA_FIELDS)
        );
    }

    public FlyingSwordForgeMenu(
        int containerId,
        Inventory playerInventory,
        Container inputContainer,
        ContainerData data
    ) {
        super(KongqiaoMenus.FLYING_SWORD_FORGE.get(), containerId);
        this.inputContainer = inputContainer;
        this.data = data;
        this.player = playerInventory.player;

        checkContainerSize(inputContainer, 1);
        checkContainerDataCount(data, DATA_FIELDS);
        inputContainer.startOpen(player);

        addSlot(new TinyUISlot(inputContainer, 0, HIDDEN_POS, HIDDEN_POS));
        addPlayerSlots(playerInventory);
        addDataSlots(data);
    }

    public static FlyingSwordForgeMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        return new FlyingSwordForgeMenu(
            containerId,
            inventory,
            new SimpleContainer(1),
            new SimpleContainerData(DATA_FIELDS)
        );
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int index = col + row * PLAYER_INV_COLS + PLAYER_INV_COLS;
                addSlot(
                    new TinyUISlot(playerInventory, index, HIDDEN_POS, HIDDEN_POS)
                );
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
    public void removed(Player player) {
        if (!player.level().isClientSide) {
            ItemStack stack = inputContainer.removeItemNoUpdate(0);
            if (!stack.isEmpty()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
        super.removed(player);
        inputContainer.stopOpen(player);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (player instanceof ServerPlayer serverPlayer && container == inputContainer) {
            ItemStack stack = inputContainer.getItem(0);
            if (!stack.isEmpty()) {
                FlyingSwordForgeService.handleInsertItem(serverPlayer, inputContainer);
                syncDataFromAttachment(serverPlayer);
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (id == BUTTON_CLAIM) {
            boolean success = FlyingSwordForgeService.claim(serverPlayer);
            syncDataFromAttachment(serverPlayer);
            return success;
        }
        if (id == BUTTON_CANCEL) {
            FlyingSwordForgeService.cancel(serverPlayer);
            syncDataFromAttachment(serverPlayer);
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private void syncDataFromAttachment(ServerPlayer serverPlayer) {
        FlyingSwordForgeAttachment forge = KongqiaoAttachments.getFlyingSwordForge(serverPlayer);
        if (forge == null) {
            return;
        }
        data.set(DATA_ACTIVE, forge.isActive() ? 1 : 0);
        data.set(DATA_FED_SWORD_COUNT, forge.getFedSwordCount());
        data.set(DATA_REQUIRED_SWORD_COUNT, forge.getRequiredSwordCount());
        data.set(DATA_CAN_CLAIM, forge.canClaim() ? 1 : 0);
    }

    public int getDataActive() {
        return data.get(DATA_ACTIVE);
    }

    public int getDataFedSwordCount() {
        return data.get(DATA_FED_SWORD_COUNT);
    }

    public int getDataRequiredSwordCount() {
        return data.get(DATA_REQUIRED_SWORD_COUNT);
    }

    public int getDataCanClaim() {
        return data.get(DATA_CAN_CLAIM);
    }

    public boolean isActive() {
        return getDataActive() != 0;
    }

    public boolean canClaim() {
        return getDataCanClaim() != 0;
    }

    private static final int PERCENT_100 = 100;

    public int getProgressPercent() {
        int required = getDataRequiredSwordCount();
        if (required <= 0) {
            return 0;
        }
        return Math.min(PERCENT_100, (getDataFedSwordCount() * PERCENT_100) / required);
    }
}
