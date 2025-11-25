package com.Kizunad.customNPCs.commands;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * NPC Mind 调试命令
 * <p>
 * 用法：
 * /npc_mind inspect [entity] - 查看目标 NPC 的思维状态
 */
public class MindDebugCommand {

    private static final double RAYTRACE_DISTANCE = 20.0D;

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
            Commands.literal("npc_mind")
                .requires(source -> source.hasPermission(2)) // 需要 OP 权限
                .then(
                    Commands.literal("inspect")
                        .executes(MindDebugCommand::inspectLookingAt)
                        .then(
                            Commands.argument(
                                "target",
                                EntityArgument.entity()
                            ).executes(MindDebugCommand::inspectTarget)
                        )
                )
        );
    }

    private static int inspectLookingAt(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            Entity entity = context.getSource().getEntityOrException();
            if (!(entity instanceof LivingEntity player)) {
                context
                    .getSource()
                    .sendFailure(Component.literal("只有实体可以执行此命令"));
                return 0;
            }

            // 简单的射线检测
            Vec3 viewVector = player.getViewVector(1.0F);
            Vec3 eyePosition = player.getEyePosition();
            Vec3 endPosition = eyePosition.add(
                viewVector.scale(RAYTRACE_DISTANCE)
            );
            AABB searchBox = player
                .getBoundingBox()
                .expandTowards(viewVector.scale(RAYTRACE_DISTANCE))
                .inflate(1.0D);

            EntityHitResult result = ProjectileUtil.getEntityHitResult(
                player,
                eyePosition,
                endPosition,
                searchBox,
                e -> !e.isSpectator() && e.isPickable(),
                RAYTRACE_DISTANCE * RAYTRACE_DISTANCE
            );

            if (result != null) {
                return printMindInfo(context.getSource(), result.getEntity());
            }

            context
                .getSource()
                .sendFailure(Component.literal("未找到目标实体"));
            return 0;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("错误: " + e.getMessage()));
            return 0;
        }
    }

    private static int inspectTarget(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            Entity target = EntityArgument.getEntity(context, "target");
            return printMindInfo(context.getSource(), target);
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("错误: " + e.getMessage()));
            return 0;
        }
    }

    private static int printMindInfo(CommandSourceStack source, Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            source.sendFailure(
                Component.literal(
                    "目标不是生物: " + entity.getName().getString()
                )
            );
            return 0;
        }

        if (!livingEntity.hasData(NpcMindAttachment.NPC_MIND)) {
            source.sendFailure(
                Component.literal(
                    "目标没有思维系统: " + entity.getName().getString()
                )
            );
            return 0;
        }

        INpcMind mind = livingEntity.getData(NpcMindAttachment.NPC_MIND);

        source.sendSuccess(
            () ->
                Component.literal(
                    "=== NPC Mind Info: " +
                        entity.getName().getString() +
                        " ==="
                ),
            false
        );

        // 1. 当前目标
        var currentGoal = mind.getGoalSelector().getCurrentGoal();
        if (currentGoal != null) {
            source.sendSuccess(
                () -> Component.literal("当前目标: " + currentGoal.getName()),
                false
            );
            source.sendSuccess(
                () ->
                    Component.literal(
                        "优先级: " + currentGoal.getPriority(mind, livingEntity)
                    ),
                false
            );
        } else {
            source.sendSuccess(
                () -> Component.literal("当前目标: (无)"),
                false
            );
        }

        // 2. 记忆概览
        source.sendSuccess(() -> Component.literal("记忆系统:"), false);

        if (!mind.getMemory().getShortTermMemory().isEmpty()) {
            source.sendSuccess(() -> Component.literal("  短期记忆:"), false);
            mind
                .getMemory()
                .getShortTermMemory()
                .forEach((key, entry) -> {
                    source.sendSuccess(
                        () ->
                            Component.literal(
                                "    - " + key + ": " + entry.getValue()
                            ),
                        false
                    );
                });
        }

        if (!mind.getMemory().getLongTermMemory().isEmpty()) {
            source.sendSuccess(() -> Component.literal("  长期记忆:"), false);
            mind
                .getMemory()
                .getLongTermMemory()
                .forEach((key, entry) -> {
                    source.sendSuccess(
                        () ->
                            Component.literal(
                                "    - " + key + ": " + entry.getValue()
                            ),
                        false
                    );
                });
        }

        return 1;
    }
}
