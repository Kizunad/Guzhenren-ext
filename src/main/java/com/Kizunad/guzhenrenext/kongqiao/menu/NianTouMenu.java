package com.Kizunad.guzhenrenext.kongqiao.menu;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ZhuanCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService;
import com.Kizunad.tinyUI.demo.TinyUISlot;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NianTouMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;
    private static final int HOTBAR_COLS = 9;
    private static final int HIDDEN_POS = -10000;
    private static final int DATA_FIELDS = 3;
    public static final int IDENTIFY_BUTTON_ID = 99;
    public static final int DERIVE_SHAZHAO_BUTTON_ID = 100;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PLAYER_FIRST_SLOT = 1;
    private static final int PLAYER_LAST_SLOT_EXCLUSIVE =
        PLAYER_FIRST_SLOT + PLAYER_INV_ROWS * PLAYER_INV_COLS + HOTBAR_COLS;

    private final Container container;
    private final ContainerData data;

    public NianTouMenu(int containerId, Inventory playerInventory) {
        this(
            containerId,
            playerInventory,
            new SimpleContainer(1),
            new SimpleContainerData(DATA_FIELDS)
        );
    }

    public NianTouMenu(
        int containerId,
        Inventory playerInventory,
        Container container,
        ContainerData data
    ) {
        super(KongqiaoMenus.NIANTOU.get(), containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(container, 1);
        checkContainerDataCount(data, DATA_FIELDS);
        container.startOpen(playerInventory.player);

        // NianTou Slot (Index 0)
        this.addSlot(new TinyUISlot(container, 0, HIDDEN_POS, HIDDEN_POS));

        addPlayerSlots(playerInventory);
        addDataSlots(data);
    }

    public static NianTouMenu fromNetwork(
        int containerId,
        Inventory inventory,
        FriendlyByteBuf buf
    ) {
        return new NianTouMenu(
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
                    new TinyUISlot(
                        playerInventory,
                        index,
                        HIDDEN_POS,
                        HIDDEN_POS
                    )
                );
            }
        }

        for (int col = 0; col < HOTBAR_COLS; col++) {
            addSlot(
                new TinyUISlot(playerInventory, col, HIDDEN_POS, HIDDEN_POS)
            );
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        // 关闭界面时将蛊物返还给玩家，背包满则掉落
        if (!player.level().isClientSide) {
            ItemStack stack = container.removeItemNoUpdate(0);
            if (!stack.isEmpty()) {
                boolean added = player.getInventory().add(stack);
                if (!added) {
                    player.drop(stack, false);
                }
            }
        }
        super.removed(player);
        container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == IDENTIFY_BUTTON_ID) {
            return handleIdentifyButton(player);
        }
        if (id == DERIVE_SHAZHAO_BUTTON_ID) {
            return handleDeriveShazhaoButton(player);
        }
        return super.clickMenuButton(player, id);
    }

    private boolean handleIdentifyButton(final Player player) {
        final ItemStack stack = container.getItem(0);
        if (stack.isEmpty()) {
            return false;
        }

        final NianTouData data = NianTouDataManager.getData(stack);
        if (data == null || data.usages() == null || data.usages().isEmpty()) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            final NianTouUnlocks unlocks =
                KongqiaoAttachments.getUnlocks(serverPlayer);
            if (unlocks != null) {
                if (unlocks.isProcessing()) {
                    serverPlayer.displayClientMessage(
                        Component.literal("已有鉴定正在进行"),
                        true
                    );
                    return false;
                }

                final ResourceLocation itemId =
                    BuiltInRegistries.ITEM.getKey(stack.getItem());
                final List<NianTouData.Usage> lockedUsages = new ArrayList<>();
                for (NianTouData.Usage usage : data.usages()) {
                    if (usage == null) {
                        continue;
                    }
                    if (!unlocks.isUsageUnlocked(itemId, usage.usageID())) {
                        lockedUsages.add(usage);
                    }
                }

                if (lockedUsages.isEmpty()) {
                    serverPlayer.displayClientMessage(
                        Component.literal("所有用途均已鉴定"),
                        true
                    );
                    return false;
                }

                final NianTouData.Usage selected = lockedUsages.get(
                    serverPlayer.getRandom().nextInt(lockedUsages.size())
                );
                int duration = ZhuanCostHelper.scaleTicksCeil(
                    serverPlayer,
                    selected.costDuration()
                );
                int cost = ZhuanCostHelper.scaleCostCeil(
                    serverPlayer,
                    selected.costTotalNiantou()
                );
                if (duration <= 0) {
                    duration = 1;
                }
                if (cost < 0) {
                    cost = 0;
                }
                unlocks.startProcess(itemId, selected.usageID(), duration, cost);

                PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new PacketSyncNianTouUnlocks(unlocks)
                );
            }
        }
        return true;
    }

    private boolean handleDeriveShazhaoButton(final Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        LOGGER.info(
            "推演杀招按钮点击 | player={} containerId={}",
            serverPlayer.getGameProfile().getName(),
            this.containerId
        );
        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(serverPlayer);
        if (unlocks == null) {
            LOGGER.info(
                "推演杀招失败：unlocks 为空 | player={}",
                serverPlayer.getGameProfile().getName()
            );
            return false;
        }

        final List<ShazhaoUnlockService.UnlockCandidate> candidates =
            ShazhaoUnlockService.listUnlockCandidates(unlocks);
        LOGGER.info(
            "推演杀招候选数量={} | player={}",
            candidates.size(),
            serverPlayer.getGameProfile().getName()
        );
        if (candidates.isEmpty()) {
            unlocks.setShazhaoMessage("无可解锁杀招");
            PacketDistributor.sendToPlayer(
                serverPlayer,
                new PacketSyncNianTouUnlocks(unlocks)
            );
            return true;
        }

        final ShazhaoUnlockService.UnlockCandidate candidate =
            candidates.get(serverPlayer.getRandom().nextInt(candidates.size()));
        final ShazhaoData data = candidate.data();
        final double chance = candidate.chance();
        final double roll = serverPlayer.getRandom().nextDouble();
        final boolean success = roll <= chance;
        LOGGER.info(
            "推演杀招判定 | player={} shazhaoID={} chance={} roll={}",
            serverPlayer.getGameProfile().getName(),
            data != null ? data.shazhaoID() : "null",
            chance,
            roll
        );
        if (success) {
            unlocks.unlockShazhao(ResourceLocation.parse(data.shazhaoID()));
            final String info = data.getFormattedInfo();
            final StringBuilder message = new StringBuilder("推演成功：")
                .append(data.title());
            if (info != null && !info.isBlank()) {
                message.append(" | ").append(info);
            }
            unlocks.setShazhaoMessage(message.toString());
        } else {
            final int cost = Math.max(0, data.costTotalNiantou());
            final double currentNianTou = NianTouHelper.getAmount(serverPlayer);
            if (cost > 0 && currentNianTou < cost) {
                unlocks.setShazhaoMessage("念头不足，无法推演杀招");
                PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new PacketSyncNianTouUnlocks(unlocks)
                );
                return true;
            }
            if (cost > 0) {
                NianTouHelper.modify(serverPlayer, -cost);
            }
            unlocks.setShazhaoMessage("推演失败，消耗 " + cost + " 念头");
        }

        PacketDistributor.sendToPlayer(
            serverPlayer,
            new PacketSyncNianTouUnlocks(unlocks)
        );
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (
                    !this.moveItemStackTo(
                        itemstack1,
                        PLAYER_FIRST_SLOT,
                        PLAYER_LAST_SLOT_EXCLUSIVE,
                        true
                    )
                ) {
                    return ItemStack.EMPTY;
                }
            } else if (
                !this.moveItemStackTo(
                    itemstack1,
                    0,
                    PLAYER_FIRST_SLOT,
                    false
                )
            ) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
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

    public int getProgress() {
        return data.get(0);
    }

    public int getTotalTime() {
        return data.get(1);
    }

    public int getCost() {
        return data.get(2);
    }

    public Container getContainer() {
        return container;
    }
}
