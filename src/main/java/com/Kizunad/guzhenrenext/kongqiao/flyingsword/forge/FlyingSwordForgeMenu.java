package com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenus;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import java.util.HashMap;
import java.util.Map;
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
import net.neoforged.neoforge.network.PacketDistributor;

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

    /** 客户端侧道痕缓存（由 ClientboundForgeDaoSyncPayload 更新）。 */
    private Map<String, Integer> clientDaoMarks = new HashMap<>();
    /** 客户端侧道痕总分缓存。 */
    private int clientTotalScore = 0;
    /** 客户端侧最近操作反馈消息缓存。 */
    private String clientLastMessage = "";

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

        // 初始同步一次，确保客户端按钮/进度不会一直停留在默认值。
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            syncDataFromAttachment(serverPlayer);
            broadcastChanges();
        }
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

        // 注意：shift-click（QUICK_MOVE）时，原版不一定会触发 inputContainer 的 slotsChanged 回调。
        // 这里补一层兜底：若物品被移入输入槽，则立即按“投喂逻辑”处理并同步 data。
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack input = inputContainer.getItem(0);
            if (!input.isEmpty()) {
                FlyingSwordForgeService.handleInsertItem(
                    serverPlayer,
                    inputContainer
                );
            }
            syncDataFromAttachment(serverPlayer);
            broadcastChanges();
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

        // 同步道痕明细 + 反馈消息到客户端（ContainerData 无法传 Map/String，使用专用网络包）。
        Map<String, Integer> marks = forge.getDaoMarks();
        int totalScore = 0;
        for (Integer value : marks.values()) {
            if (value != null && value > 0) {
                totalScore += value;
            }
        }
        PacketDistributor.sendToPlayer(
            serverPlayer,
            new ClientboundForgeDaoSyncPayload(
                new HashMap<>(marks),
                totalScore,
                forge.getLastMessage()
            )
        );
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

    /**
     * 接收服务端推送的道痕同步数据，更新客户端缓存。
     *
     * @param marks 各道的累计分
     * @param total 道痕总分
     * @param msg 最近操作反馈消息
     */
    public void applyDaoSync(Map<String, Integer> marks, int total, String msg) {
        this.clientDaoMarks = marks != null ? new HashMap<>(marks) : new HashMap<>();
        this.clientTotalScore = total;
        this.clientLastMessage = msg != null ? msg : "";
    }

    /** 获取客户端缓存的道痕明细。 */
    public Map<String, Integer> getClientDaoMarks() {
        return clientDaoMarks;
    }

    /** 获取客户端缓存的道痕总分。 */
    public int getClientTotalScore() {
        return clientTotalScore;
    }

    /** 获取客户端缓存的最近反馈消息。 */
    public String getClientLastMessage() {
        return clientLastMessage;
    }
}
