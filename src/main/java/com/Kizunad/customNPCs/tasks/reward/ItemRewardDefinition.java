package com.Kizunad.customNPCs.tasks.reward;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 发放物品的奖励定义。
 */
public record ItemRewardDefinition(ItemStack stack) implements TaskRewardDefinition {

    public ItemRewardDefinition {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("奖励物品不能为空");
        }
    }

    @Override
    public TaskRewardType getType() {
        return TaskRewardType.ITEM;
    }

    @Override
    public void grant(ServerPlayer player, CustomNpcEntity npc) {
        ItemStack copy = stack.copy();
        boolean added = player.addItem(copy);
        if (!added) {
            ItemEntity dropped = player.drop(copy, false);
            if (dropped != null) {
                dropped.setNoPickUpDelay();
                if (npc != null) {
                    dropped.setPos(npc.getX(), npc.getY() + 1.0D, npc.getZ());
                }
            }
        }
    }
}
