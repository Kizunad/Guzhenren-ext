package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.kongqiao.inventory.AttackInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * 攻击背包相关服务。
 * <p>
 * 暂时只实现与玩家背包互换的核心逻辑，未来可以在此扩展
 * 自动装备、快捷键触发等业务。
 * </p>
 */
public final class AttackInventoryService {

    private AttackInventoryService() {}

    /**
     * 将玩家当前物品栏与攻击背包内容逐槽互换。
     *
     * @param player 玩家
     * @param attackInventory 攻击背包
     */
    public static void swapWithPlayerInventory(
        ServerPlayer player,
        AttackInventory attackInventory
    ) {
        Inventory playerInventory = player.getInventory();
        int size = Math.min(
            playerInventory.items.size(),
            attackInventory.getContainerSize()
        );
        for (int i = 0; i < size; i++) {
            ItemStack playerStack = playerInventory.getItem(i).copy();
            ItemStack attackStack = attackInventory.getItem(i).copy();
            playerInventory.setItem(i, attackStack);
            attackInventory.setItem(i, playerStack);
        }
        playerInventory.setChanged();
        attackInventory.setChanged();
    }
}
