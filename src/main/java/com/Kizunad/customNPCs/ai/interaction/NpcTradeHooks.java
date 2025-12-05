package com.Kizunad.customNPCs.ai.interaction;

import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.menu.NpcTradeMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 交易逻辑钩子。
 */
public final class NpcTradeHooks {

    private static final float TRADE_FEE = 0.1f; // 10%
    private static final Random RANDOM = new Random();

    private NpcTradeHooks() {}

    public static InteractionResult tryOpenTrade(
        CustomNpcEntity npc,
        Player player,
        NpcTradeState tradeState
    ) {
        if (tradeState == null || !tradeState.isTradeEnabled()) {
            return InteractionResult.PASS;
        }
        if (
            !player.level().isClientSide() &&
            player instanceof ServerPlayer serverPlayer
        ) {
            // 打开菜单
            serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> {
                    NpcTradeMenu menu = new NpcTradeMenu(id, inv);
                    menu.setNpcEntityId(npc.getId());
                    // 初始化 NPC Offer
                    populateNpcOffer(npc, menu.npcOffer);
                    // 同步 Multiplier 到服务端菜单实例（虽然这里主要是给客户端显示的，但保持一致也好）
                    menu.setPriceMultiplier(tradeState.getPriceMultiplier());
                    return menu;
                },
                Component.literal("Trade with " + npc.getName().getString())
            ), buf -> {
                // 发送数据给客户端菜单
                buf.writeVarInt(npc.getId());
                buf.writeFloat(tradeState.getPriceMultiplier());
            });
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * 从 NPC 背包随机抽取一个物品放入 Offer 容器（副本）。
     */
    private static void populateNpcOffer(
        CustomNpcEntity npc,
        Container npcOffer
    ) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }

        NpcInventory inv = mind.getInventory();
        List<Integer> validSlots = new ArrayList<>();

        // 仅从主背包区域选取
        for (int i = 0; i < inv.getMainSize(); i++) {
            if (!inv.getItem(i).isEmpty()) {
                validSlots.add(i);
            }
        }

        if (!validSlots.isEmpty()) {
            int slot = validSlots.get(RANDOM.nextInt(validSlots.size()));
            ItemStack stack = inv.getItem(slot).copy();
            // 放入第一个槽位
            npcOffer.setItem(0, stack);
        }
    }

    public static int calculateSellValue(ItemStack stack, float multiplier) {
        if (stack.isEmpty()) {
            return 0;
        }
        int base = ItemValueManager.getInstance().getItemBaseValue(stack);
        if (base == 0) {
            return 0;
        }
        // Value = Base * Multiplier * (1 + Fee)
        double unitPrice = base * multiplier * (1.0 + TRADE_FEE);
        return (int) Math.ceil(unitPrice * stack.getCount());
    }

    public static int calculateBuyValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        int base = ItemValueManager.getInstance().getItemBaseValue(stack);
        return base * stack.getCount();
    }

    /**
     * 执行交易逻辑（服务端调用）。
     */
    public static void performTrade(
        CustomNpcEntity npc,
        NpcTradeMenu menu,
        Player player
    ) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        NpcInventory npcInv = mind.getInventory();

        // 1. 计算总价值
        int playerValue = 0;
        for (int i = 0; i < menu.playerOffer.getContainerSize(); i++) {
            playerValue += calculateBuyValue(menu.playerOffer.getItem(i));
        }

        int npcCost = 0;
        float multiplier = menu.getPriceMultiplier();
        for (int i = 0; i < menu.npcOffer.getContainerSize(); i++) {
            npcCost += calculateSellValue(menu.npcOffer.getItem(i), multiplier);
        }

        // 2. 验证
        if (playerValue < npcCost) {
            player.displayClientMessage(
                Component.literal("Not enough value offered!"),
                true
            );
            return;
        }

        // 3. 执行交易
        boolean success = false;

        // 将 NPC Offer 的物品给予玩家（并从 NPC 背包扣除）
        for (int i = 0; i < menu.npcOffer.getContainerSize(); i++) {
            ItemStack sellStack = menu.npcOffer.getItem(i);
            if (!sellStack.isEmpty()) {
                // 从 NPC 背包移除实际物品
                ItemStack removed = removeItemFromNpc(npcInv, sellStack);
                if (!removed.isEmpty()) {
                    // 给予玩家
                    player.getInventory().add(removed);
                    success = true;
                }
                // 清空 Offer 槽
                menu.npcOffer.setItem(i, ItemStack.EMPTY);
            }
        }

        if (success) {
            // 将 Player Offer 的物品给予 NPC
            for (int i = 0; i < menu.playerOffer.getContainerSize(); i++) {
                ItemStack offerStack = menu.playerOffer.removeItemNoUpdate(i);
                if (!offerStack.isEmpty()) {
                    ItemStack remainder = npcInv.addItem(offerStack);
                    if (!remainder.isEmpty()) {
                        // NPC 背包满了？
                        // player.getInventory().add(remainder); - 这里不退回
                    }
                }
            }
            player.displayClientMessage(
                Component.literal("Trade successful!"),
                true
            );
        } else {
            player.displayClientMessage(
                Component.literal("Trade failed (NPC lost item?)"),
                true
            );
        }
    }

    private static ItemStack removeItemFromNpc(
        NpcInventory inv,
        ItemStack prototype
    ) {
        ItemStack result = prototype.copy();
        result.setCount(0);
        int needed = prototype.getCount();

        for (int i = 0; i < inv.getMainSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, prototype)) {
                int take = Math.min(needed, slot.getCount());
                ItemStack taken = inv.removeItem(i, take);
                result.grow(taken.getCount());
                needed -= taken.getCount();
                if (needed <= 0) {
                    break;
                }
            }
        }

        // 如果没凑够，可能需要回滚？这里简化处理，能给多少给多少
        return result;
    }
}
