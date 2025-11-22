package com.Kizunad.customNPCs_test.items;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 思维检测仪 - 测试物品
 * <p>
 * 右键点击 NPC 可查看其思维状态
 */
public class MindInspectorItem extends Item {
    
    public MindInspectorItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, 
                                                InteractionHand usedHand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        if (interactionTarget.hasData(NpcMindAttachment.NPC_MIND)) {
            INpcMind mind = interactionTarget.getData(NpcMindAttachment.NPC_MIND);
            
            player.sendSystemMessage(Component.literal("--- NPC 思维检测 ---"));
            player.sendSystemMessage(Component.literal("目标: " + interactionTarget.getName().getString()));
            
            var currentGoal = mind.getGoalSelector().getCurrentGoal();
            if (currentGoal != null) {
                player.sendSystemMessage(Component.literal("当前目标: " + currentGoal.getName()));
                player.sendSystemMessage(Component.literal("优先级: " + currentGoal.getPriority(mind, interactionTarget)));
            } else {
                player.sendSystemMessage(Component.literal("当前目标: (无)"));
            }
            
            player.sendSystemMessage(Component.literal("短期记忆数: " + mind.getMemory().getShortTermMemory().size()));
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
}
