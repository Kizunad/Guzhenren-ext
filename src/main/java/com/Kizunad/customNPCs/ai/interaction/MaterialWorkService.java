package com.Kizunad.customNPCs.ai.interaction;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.capabilities.mind.MaterialWallet;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.menu.MaterialSlotProvider;
import com.Kizunad.customNPCs.network.SyncMaterialDataPayload;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 材料转化/制作后台逻辑，实现 Phase1 的基础数据处理。
 */
public final class MaterialWorkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        MaterialWorkService.class
    );
    private static final double MAX_INTERACT_DISTANCE = 20.0D;
    private static final double MAX_INTERACT_DISTANCE_SQR =
        MAX_INTERACT_DISTANCE * MAX_INTERACT_DISTANCE;
    public static final int MAX_CRAFT_AMOUNT = 256;
    private static final double EPSILON = 1.0E-6D;

    private MaterialWorkService() {}

    /**
     * 将界面中提供的输入槽转换为 Owner 材料点。
     */
    public static void handleConversionFromMenu(
        CustomNpcEntity npc,
        ServerPlayer player
    ) {
        if (!validateContext(npc, player)) {
            return;
        }
        MaterialSlotProvider provider = getSlotProvider(player);
        if (provider == null) {
            player.displayClientMessage(
                Component.literal("当前界面不支持材料转换"),
                true
            );
            return;
        }

        List<Slot> slots = provider.getMaterialSlots();
        if (slots == null || slots.isEmpty()) {
            player.displayClientMessage(
                Component.literal("未找到可转换的输入槽"),
                true
            );
            return;
        }

        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        MaterialWallet wallet = mind.getMaterialWallet();
        double total = 0.0D;

        for (Slot slot : slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            float unit = MaterialValueManager
                .getInstance()
                .getMaterialValue(stack);
            if (unit <= 0.0F) {
                continue;
            }
            int count = stack.getCount();
            total += unit * count;
            slot.remove(count);
            slot.setChanged();
        }

        if (total <= 0.0D) {
            player.displayClientMessage(
                Component.literal("没有可转换为材料点的物品"),
                true
            );
            return;
        }

        wallet.addOwnerMaterial(total);
        syncOwnerMaterial(npc, wallet, player);
        player.displayClientMessage(
            Component.literal(
                "转换成功，获得材料点: " + String.format("%.2f", total)
            ),
            false
        );
        LOGGER.debug(
            "[MaterialWorkService] ownerMaterial={}, npc={}",
            wallet.getOwnerMaterial(),
            npc.getStringUUID()
        );
    }

    /**
     * 消耗材料点直接制作指定物品并放入 NPC 背包，不足则提示失败。
     */
    public static void handleCraftRequest(
        CustomNpcEntity npc,
        ServerPlayer player,
        ResourceLocation itemId,
        int amount
    ) {
        if (!validateContext(npc, player)) {
            return;
        }
        if (amount <= 0 || amount > MAX_CRAFT_AMOUNT) {
            player.displayClientMessage(
                Component.literal("数量不合法"),
                true
            );
            return;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            player.displayClientMessage(
                Component.literal("无法制作未知物品: " + itemId),
                true
            );
            return;
        }

        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        MaterialWallet wallet = mind.getMaterialWallet();
        float unitCost = MaterialValueManager
            .getInstance()
            .getMaterialValue(new ItemStack(item));
        if (unitCost <= 0.0F) {
            player.displayClientMessage(
                Component.literal("该物品未定义材料成本"),
                true
            );
            return;
        }

        double cost = unitCost * amount;
        if (wallet.getOwnerMaterial() + EPSILON < cost) {
            player.displayClientMessage(
                Component.literal("材料不足，需 " + String.format("%.2f", cost)),
                true
            );
            syncOwnerMaterial(npc, wallet, player);
            return;
        }

        wallet.addOwnerMaterial(-cost);
        insertCraftResult(npc, player, new ItemStack(item, amount));
        syncOwnerMaterial(npc, wallet, player);
        player.displayClientMessage(
            Component.literal(
                "制作完成，消耗材料点: " + String.format("%.2f", cost)
            ),
            false
        );
    }

    private static boolean validateContext(
        CustomNpcEntity npc,
        ServerPlayer player
    ) {
        if (
            npc == null ||
            player == null ||
            !npc.isAlive() ||
            npc.isRemoved() ||
            npc.distanceToSqr(player) > MAX_INTERACT_DISTANCE_SQR
        ) {
            return false;
        }
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return false;
        }
        UUID ownerId = mind
            .getMemory()
            .getMemory(WorldStateKeys.OWNER_UUID, UUID.class);
        if (ownerId == null || !ownerId.equals(player.getUUID())) {
            player.displayClientMessage(
                Component.literal("仅主人可以操作材料点"),
                true
            );
            return false;
        }
        return true;
    }

    private static MaterialSlotProvider getSlotProvider(ServerPlayer player) {
        if (player.containerMenu instanceof MaterialSlotProvider provider) {
            return provider;
        }
        return null;
    }

    private static void insertCraftResult(
        CustomNpcEntity npc,
        ServerPlayer player,
        ItemStack combined
    ) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        var inventory = player.getInventory();
        int remaining = combined.getCount();
        int maxStack = combined.getMaxStackSize();
        while (remaining > 0) {
            int batch = Math.min(remaining, maxStack);
            ItemStack toInsert = combined.copyWithCount(batch);
            inventory.placeItemBackInInventory(toInsert);
            remaining -= batch;
        }
    }

    public static void syncOwnerMaterial(
        CustomNpcEntity npc,
        MaterialWallet wallet,
        ServerPlayer player
    ) {
        PacketDistributor.sendToPlayer(
            player,
            new SyncMaterialDataPayload(npc.getId(), wallet.getOwnerMaterial())
        );
    }
}
