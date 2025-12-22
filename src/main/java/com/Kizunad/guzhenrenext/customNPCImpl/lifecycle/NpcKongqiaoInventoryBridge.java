package com.Kizunad.guzhenrenext.customNPCImpl.lifecycle;

import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.registry.NpcTickRegistry;
import com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService;

/**
 * NPC 背包作为“空窍容器”的桥接层。
 * <p>
 * 目标：允许 CustomNPC 的 {@code NpcInventory} 被空窍逻辑扫描，从而在 NPC 身上生效
 * {@link com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect} 的被动效果（onTick/onSecond）
 * 以及装备/卸下回调（onEquip/onUnequip）。<br>
 * <br>
 * 说明：玩家依旧使用 {@link com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory}
 * 的“已解锁槽位”作为扫描范围；NPC 直接扫描其主背包槽位。
 * </p>
 */
public final class NpcKongqiaoInventoryBridge {

    private static final int TICKS_PER_SECOND = 20;
    private static boolean registered;

    private NpcKongqiaoInventoryBridge() {}

    public static void register() {
        if (registered) {
            return;
        }
        NpcTickRegistry.register(NpcKongqiaoInventoryBridge::onTick);
        registered = true;
    }

    private static void onTick(CustomNpcEntity npc) {
        if (npc == null || npc.level().isClientSide()) {
            return;
        }

        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }

        var inventory = mind.getInventory();
        if (inventory == null || inventory.getContainerSize() <= 0) {
            return;
        }

        boolean isSecond = (npc.tickCount % TICKS_PER_SECOND == 0);
        GuRunningService.handleContainerEquipChanges(npc, inventory);
        GuRunningService.tickContainerEffects(
            npc,
            inventory,
            inventory.getMainSize(),
            isSecond
        );
    }
}

