package com.Kizunad.guzhenrenext.kongqiao.flyingsword.training;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenus;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/**
 * 飞剑修炼菜单。
 * <p>
 * 提供输入槽（剑）与燃料槽（元石），用于挂机修炼飞剑。
 * 通过 ContainerData 同步进度到客户端。
 * </p>
 */
public class FlyingSwordTrainingMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;
    private static final int PERCENT_100 = 100;
    private static final int HOTBAR_SIZE = 9;
    private static final int PLAYER_INV_SIZE = 27;

    // ContainerData 索引定义
    public static final int DATA_FUEL_TIME = 0;
    public static final int DATA_MAX_FUEL_TIME = 1;
    public static final int DATA_ACCUMULATED_EXP = 2;
    public static final int DATA_FIELDS = 3;

    // 槽位索引定义
    public static final int SLOT_SWORD = 0;
    public static final int SLOT_FUEL = 1;

    private final FlyingSwordTrainingAttachment attachment;
    private final ContainerData data;
    private final Player player;

    // 仅用于客户端的构造，使用虚拟容器
    public FlyingSwordTrainingMenu(int containerId, Inventory playerInventory) {
        this(
            containerId,
            playerInventory,
            new FlyingSwordTrainingAttachment(), // 客户端虚拟 attachment
            new SimpleContainerData(DATA_FIELDS)
        );
    }

    public FlyingSwordTrainingMenu(
        int containerId,
        Inventory playerInventory,
        FlyingSwordTrainingAttachment attachment,
        ContainerData data
    ) {
        super(KongqiaoMenus.FLYING_SWORD_TRAINING.get(), containerId);
        this.attachment = attachment;
        this.data = data;
        this.player = playerInventory.player;

        checkContainerDataCount(data, DATA_FIELDS);

        // 绑定输入槽 (ItemStackHandler)
        addSlot(new TinyUISlotItemHandler(attachment.getInputSlots(), SLOT_SWORD, HIDDEN_POS, HIDDEN_POS) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof SwordItem;
            }
        });
        addSlot(new TinyUISlotItemHandler(attachment.getInputSlots(), SLOT_FUEL, HIDDEN_POS, HIDDEN_POS) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return FuelHelper.isFuel(stack);
            }
        });

        addPlayerSlots(playerInventory);
        addDataSlots(data);

        // 初始同步
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            syncDataFromAttachment(serverPlayer);
            broadcastChanges();
        }
    }

    public static FlyingSwordTrainingMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        return new FlyingSwordTrainingMenu(containerId, inventory);
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
    public void slotsChanged(Container container) {
        // 由于使用了 ItemHandler，这里可能不会被直接触发，逻辑主要依赖 Service tick 和手动同步。
        // 但如果需要响应槽位变化，可以在 Attachment 的 ItemStackHandler 中重写 onContentsChanged。
        // 此处仅做标准调用。
        super.slotsChanged(container);
        if (player instanceof ServerPlayer serverPlayer) {
            syncDataFromAttachment(serverPlayer);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        // 0-1 是输入槽，2-28 是背包，29-37 是快捷栏
        int inputSlotsCount = 2;
        int playerInvStart = inputSlotsCount;
        int hotbarStart = playerInvStart + PLAYER_INV_SIZE;
        int hotbarEnd = hotbarStart + HOTBAR_SIZE;

        if (index < inputSlotsCount) {
            // 从输入槽移到玩家背包
            if (!moveItemStackTo(stack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 从玩家背包移入输入槽
            // 优先尝试剑槽 (0)
            boolean processed = false;
            if (stack.getItem() instanceof SwordItem) {
                if (moveItemStackTo(stack, SLOT_SWORD, SLOT_SWORD + 1, false)) {
                    processed = true;
                }
            } else if (FuelHelper.isFuel(stack)) {
                if (moveItemStackTo(stack, SLOT_FUEL, SLOT_FUEL + 1, false)) {
                    processed = true;
                }
            }

            if (!processed) {
                // 在背包内整理
                if (index < hotbarStart) {
                    if (!moveItemStackTo(stack, hotbarStart, hotbarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!moveItemStackTo(stack, playerInvStart, hotbarStart, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (player instanceof ServerPlayer serverPlayer) {
            syncDataFromAttachment(serverPlayer);
            broadcastChanges();
        }
        return copy;
    }

    /**
     * 将 Attachment 中的数据同步到 ContainerData。
     * 这里的 attachment 实际上是 Menu 持有的引用，
     * 在服务端构造时，它引用的是 ServerPlayer 身上的真实 Attachment。
     */
    private void syncDataFromAttachment(ServerPlayer serverPlayer) {
        // 重新获取一次以防万一，虽然构造函数传进来的应该是同一个引用
        FlyingSwordTrainingAttachment realAttachment = KongqiaoAttachments.getFlyingSwordTraining(serverPlayer);
        if (realAttachment == null) {
            return;
        }

        data.set(DATA_FUEL_TIME, realAttachment.getFuelTime());
        data.set(DATA_MAX_FUEL_TIME, realAttachment.getMaxFuelTime());
        data.set(DATA_ACCUMULATED_EXP, realAttachment.getAccumulatedExp());
    }

    public int getFuelTime() {
        return data.get(DATA_FUEL_TIME);
    }

    public int getMaxFuelTime() {
        return data.get(DATA_MAX_FUEL_TIME);
    }

    public int getAccumulatedExp() {
        return data.get(DATA_ACCUMULATED_EXP);
    }

    public int getBurnProgressPercent() {
        int max = getMaxFuelTime();
        if (max <= 0) {
            return 0;
        }
        int current = getFuelTime();
        // 燃烧是倒计时，进度条通常显示剩余或消耗。
        // 这里显示剩余百分比。
        return (current * PERCENT_100) / max;
    }
}
