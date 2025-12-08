package com.Kizunad.guzhenrenext.commands;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Action.GuzhenrenItemUseAction;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class GuzhenrenDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("guzhenren_mind")
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("use_hand_item")
                        .then(
                            Commands.argument("npc", EntityArgument.entity())
                                .executes(GuzhenrenDebugCommand::useHandItem)
                        )
                )
        );
    }

    private static int useHandItem(CommandContext<CommandSourceStack> context) {
        try {
            Entity entity = EntityArgument.getEntity(context, "npc");
            if (!(entity instanceof Mob mob)) {
                context.getSource().sendFailure(Component.literal("目标必须是 Mob 实体"));
                return 0;
            }

            if (!mob.hasData(NpcMindAttachment.NPC_MIND)) {
                context.getSource().sendFailure(Component.literal("目标没有 Mind 系统"));
                return 0;
            }

            INpcMind mind = mob.getData(NpcMindAttachment.NPC_MIND);
            ItemStack mainHandItem = mob.getItemInHand(InteractionHand.MAIN_HAND);
            
            if (mainHandItem.isEmpty()) {
                 context.getSource().sendFailure(Component.literal("目标主手为空"));
                 return 0;
            }

            // 创建并提交动作
            GuzhenrenItemUseAction action = new GuzhenrenItemUseAction(mainHandItem);
            mind.getActionExecutor().addAction(action);

            context.getSource().sendSuccess(
                () -> Component.literal(
                    "已让 NPC 尝试使用主手物品: " + mainHandItem.getDisplayName().getString()
                ),
                true
            );
            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}
