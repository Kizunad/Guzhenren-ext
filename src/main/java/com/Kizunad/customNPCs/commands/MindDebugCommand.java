package com.Kizunad.customNPCs.commands;

import com.Kizunad.customNPCs.ai.NpcMindRegistry;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.actions.base.LookAtAction;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.actions.base.WaitAction;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogCategory;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.entity.ModEntities;
import com.Kizunad.customNPCs.menu.NpcInventoryMenu;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.MenuProvider;

/**
 * NPC Mind 调试命令
 * <p>
 * 用法:
 * /npc_mind inspect [entity] - 查看目标 NPC 的思维状态
 * /npc_mind action - 执行各种动作
 * /npc_mind goal - 管理目标
 * /npc_mind plan - 运行规划
 * /npc_mind log - 管理日志
 * </p>
 */
public class MindDebugCommand {

    /** 射线检测距离 */
    private static final double RAYTRACE_DISTANCE = 20.0D;

    /** 测试实体标签 */
    private static final String TEST_ENTITY_TAG = "customnpcs_test_entity";

    /** 测试上下文标签 */
    private static final String TEST_CONTEXT_TAG = "test:command";

    /** 测试实体名称 */
    private static final String TEST_ENTITY_NAME = "TestEntity";

    /** LookAt动作默认持续时间(tick) */
    private static final int DEFAULT_LOOK_AT_DURATION = 40;

    /** LookAt动作最大持续时间(tick) */
    private static final int MAX_LOOK_AT_DURATION = 400;

    /** Wait动作最大持续时间(tick) */
    private static final int MAX_WAIT_DURATION = 2000;

    /** 远程测试箭矢堆叠数量 */
    private static final int TEST_ARROW_STACK = 64;

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        registerRoot(dispatcher, "npc_mind");
        registerRoot(dispatcher, "mind");
    }

    /**
     * 注册NPC Mind命令的根节点。
     *
     * @param dispatcher 命令调度器
     * @param literal 命令字面值
     */
    private static void registerRoot(
        CommandDispatcher<CommandSourceStack> dispatcher,
        String literal
    ) {
        dispatcher.register(
            Commands.literal(literal)
                .requires(source -> source.hasPermission(2)) // 需要 OP 权限
                .then(registerInspectCommands())
                .then(registerInspectInventoryCommand())
                .then(registerSpawnTestCommand())
                .then(registerActionCommands())
                .then(registerGoalCommands())
                .then(registerPlanCommands())
                .then(registerLogCommands())
        );
    }

    /**
     * 注册inspect命令。
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > registerInspectCommands() {
        return Commands.literal("inspect")
            .executes(MindDebugCommand::inspectLookingAt)
            .then(
                Commands.argument("target", EntityArgument.entity()).executes(
                    MindDebugCommand::inspectTarget
                )
            );
    }

    /**
     * 注册 inspectInv 命令：打开 NPC 背包 UI。
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > registerInspectInventoryCommand() {
        return Commands.literal("inspectInv")
            .then(
                Commands.argument("target", EntityArgument.entity()).executes(
                    MindDebugCommand::openInventoryForTarget
                )
            );
    }

    /**
     * 注册spawn_test_entity命令。
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > registerSpawnTestCommand() {
        return Commands.literal("spawn_test_entity").executes(context ->
            spawnTestEntity(context.getSource())
        );
    }

    /**
     * 注册action命令。
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > registerActionCommands() {
        return Commands.literal("action")
            .then(
                Commands.literal("move_to").then(
                    Commands.argument("npc", EntityArgument.entity()).then(
                        Commands.argument(
                            "target",
                            EntityArgument.entity()
                        ).executes(MindDebugCommand::runMoveTo)
                    )
                )
            )
            .then(
                Commands.literal("look_at").then(
                    Commands.argument("npc", EntityArgument.entity()).then(
                        Commands.argument("target", EntityArgument.entity())
                            .then(
                                Commands.argument(
                                    "duration",
                                    IntegerArgumentType.integer(
                                        1,
                                        MAX_LOOK_AT_DURATION
                                    )
                                ).executes(
                                    MindDebugCommand::runLookAtWithDuration
                                )
                            )
                            .executes(MindDebugCommand::runLookAt)
                    )
                )
            )
            .then(
                Commands.literal("attack").then(
                    Commands.argument("npc", EntityArgument.entity()).then(
                        Commands.argument(
                            "target",
                            EntityArgument.entity()
                        ).executes(MindDebugCommand::runAttack)
                    )
                )
            )
            .then(
                Commands.literal("wait").then(
                    Commands.argument("npc", EntityArgument.entity()).then(
                        Commands.argument(
                            "ticks",
                            IntegerArgumentType.integer(1, MAX_WAIT_DURATION)
                        ).executes(MindDebugCommand::runWait)
                    )
                )
            );
    }

    /**
     * 注册goal命令。
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > registerGoalCommands() {
        return Commands.literal("goal").then(
            Commands.literal("force").then(
                Commands.argument("goal", StringArgumentType.word()).then(
                    Commands.argument("npc", EntityArgument.entity()).executes(
                        MindDebugCommand::forceGoal
                    )
                )
            )
        );
    }

    /**
     * 注册plan命令。
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > registerPlanCommands() {
        return Commands.literal("plan")
            .then(
                Commands.literal("run").then(
                    Commands.argument("goal", StringArgumentType.word()).then(
                        Commands.argument(
                            "npc",
                            EntityArgument.entity()
                        ).executes(MindDebugCommand::runPlanGoal)
                    )
                )
            )
            .then(
                Commands.literal("pickup").then(
                    Commands.argument("npc", EntityArgument.entity()).then(
                        Commands.argument("item", EntityArgument.entity())
                            .then(
                                Commands.argument(
                                    "priority",
                                    DoubleArgumentType.doubleArg(0.0)
                                ).executes(
                                    MindDebugCommand::runPickupPlanWithPriority
                                )
                            )
                            .executes(MindDebugCommand::runPickupPlan)
                    )
                )
            );
    }

    /**
     * 注册log命令。
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > registerLogCommands() {
        return Commands.literal("log")
            .then(
                Commands.literal("decision").then(
                    Commands.argument(
                        "enabled",
                        BoolArgumentType.bool()
                    ).executes(context ->
                        toggleLog(
                            context.getSource(),
                            MindLogCategory.DECISION,
                            BoolArgumentType.getBool(context, "enabled")
                        )
                    )
                )
            )
            .then(
                Commands.literal("planning").then(
                    Commands.argument(
                        "enabled",
                        BoolArgumentType.bool()
                    ).executes(context ->
                        toggleLog(
                            context.getSource(),
                            MindLogCategory.PLANNING,
                            BoolArgumentType.getBool(context, "enabled")
                        )
                    )
                )
            )
            .then(
                Commands.literal("execution").then(
                    Commands.argument(
                        "enabled",
                        BoolArgumentType.bool()
                    ).executes(context ->
                        toggleLog(
                            context.getSource(),
                            MindLogCategory.EXECUTION,
                            BoolArgumentType.getBool(context, "enabled")
                        )
                    )
                )
            )
            .then(
                Commands.literal("all").then(
                    Commands.argument(
                        "enabled",
                        BoolArgumentType.bool()
                    ).executes(context ->
                        toggleAllLogs(
                            context.getSource(),
                            BoolArgumentType.getBool(context, "enabled")
                        )
                    )
                )
            )
            .then(
                Commands.literal("status").executes(context ->
                    showLogStatus(context.getSource())
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

    private static int openInventoryForTarget(
        CommandContext<CommandSourceStack> context
    ) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("需要玩家执行"));
            return 0;
        }
        Entity entity = getEntity(context, "target");
        if (!(entity instanceof CustomNpcEntity npc)) {
            context
                .getSource()
                .sendFailure(Component.literal("目标不是 CustomNpcEntity"));
            return 0;
        }
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            context
                .getSource()
                .sendFailure(Component.literal("目标未初始化 NpcMind"));
            return 0;
        }

        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Inventory - " + npc.getName().getString());
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                int containerId,
                Inventory playerInventory,
                Player player
            ) {
                return new NpcInventoryMenu(
                    containerId,
                    playerInventory,
                    npc,
                    mind.getInventory()
                );
            }
        };

        player.openMenu(provider, buf -> buf.writeVarInt(npc.getId()));
        return 1;
    }

    private static int spawnTestEntity(CommandSourceStack source) {
        Entity executor = source.getEntity();
        if (executor == null) {
            source.sendFailure(Component.literal("需要由实体执行该命令"));
            return 0;
        }
        ServerLevel level = source.getLevel();
        // 使用自定义 NPC 作为测试实体，默认远程配置（弓 + 箭），地面导航
        CustomNpcEntity npc =
            new CustomNpcEntity(
                ModEntities.CUSTOM_NPC.get(),
                level,
                CustomNpcEntity.NavigationMode.GROUND
            );
        if (npc == null) {
            source.sendFailure(Component.literal("生成 Custom NPC 失败"));
            return 0;
        }

        Vec3 pos = executor.position();
        npc.moveTo(pos.x(), pos.y(), pos.z(), executor.getYRot(), 0.0F);
        npc.setCustomName(Component.literal(TEST_ENTITY_NAME));
        npc.setCustomNameVisible(true);
        npc.addTag(TEST_ENTITY_TAG);
        npc.addTag(TEST_CONTEXT_TAG);
        npc.setPersistenceRequired();

        // 装备基础护甲 + 远程所需物品（主手弓，副手箭）
        npc.setItemSlot(
            EquipmentSlot.HEAD,
            new ItemStack(Items.LEATHER_HELMET)
        );
        npc.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        npc.setItemInHand(
            InteractionHand.OFF_HAND,
            new ItemStack(Items.ARROW, TEST_ARROW_STACK)
        );

        level.addFreshEntity(npc);

        if (!npc.hasData(NpcMindAttachment.NPC_MIND)) {
            npc.setData(
                NpcMindAttachment.NPC_MIND,
                new com.Kizunad.customNPCs.capabilities.mind.NpcMind()
            );
        }
        INpcMind mind = npc.getData(NpcMindAttachment.NPC_MIND);
        NpcMindRegistry.initializeMind(mind);

        source.sendSuccess(
            () ->
                Component.literal(
                    "生成测试实体: " +
                        npc.getUUID() +
                        " 标签: " +
                        TEST_ENTITY_TAG
                ),
            false
        );
        return 1;
    }

    private static int runMoveTo(CommandContext<CommandSourceStack> context) {
        LivingEntity npc = getLivingEntity(context, "npc");
        Entity target = getEntity(context, "target");
        if (npc == null || target == null) {
            return 0;
        }
        INpcMind mind = getMindOrReport(context, npc);
        if (mind == null) {
            return 0;
        }
        IAction action = new MoveToAction(target, 1.0);
        mind.getActionExecutor().addAction(action);
        context
            .getSource()
            .sendSuccess(
                () ->
                    Component.literal(
                        "已向 " +
                            npc.getName().getString() +
                            " 提交 MoveToAction -> " +
                            target.getName().getString()
                    ),
                false
            );
        return 1;
    }

    private static int runLookAt(CommandContext<CommandSourceStack> context) {
        return runLookAtInternal(context, DEFAULT_LOOK_AT_DURATION);
    }

    private static int runLookAtWithDuration(
        CommandContext<CommandSourceStack> context
    ) {
        int duration = IntegerArgumentType.getInteger(context, "duration");
        return runLookAtInternal(context, duration);
    }

    private static int runLookAtInternal(
        CommandContext<CommandSourceStack> context,
        int duration
    ) {
        LivingEntity npc = getLivingEntity(context, "npc");
        Entity target = getEntity(context, "target");
        if (npc == null || target == null) {
            return 0;
        }
        INpcMind mind = getMindOrReport(context, npc);
        if (mind == null) {
            return 0;
        }
        IAction action = new LookAtAction(target, duration);
        mind.getActionExecutor().addAction(action);
        context
            .getSource()
            .sendSuccess(
                () ->
                    Component.literal(
                        "已向 " +
                            npc.getName().getString() +
                            " 提交 LookAtAction(" +
                            duration +
                            "t) -> " +
                            target.getName().getString()
                    ),
                false
            );
        return 1;
    }

    private static int runAttack(CommandContext<CommandSourceStack> context) {
        LivingEntity npc = getLivingEntity(context, "npc");
        Entity target = getEntity(context, "target");
        if (npc == null || target == null) {
            return 0;
        }
        INpcMind mind = getMindOrReport(context, npc);
        if (mind == null) {
            return 0;
        }
        IAction action = new AttackAction(target.getUUID());
        mind.getActionExecutor().addAction(action);
        context
            .getSource()
            .sendSuccess(
                () ->
                    Component.literal(
                        "已向 " +
                            npc.getName().getString() +
                            " 提交 AttackAction -> " +
                            target.getName().getString()
                    ),
                false
            );
        return 1;
    }

    private static int runWait(CommandContext<CommandSourceStack> context) {
        LivingEntity npc = getLivingEntity(context, "npc");
        if (npc == null) {
            return 0;
        }
        INpcMind mind = getMindOrReport(context, npc);
        if (mind == null) {
            return 0;
        }
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        IAction action = new WaitAction(ticks);
        mind.getActionExecutor().addAction(action);
        context
            .getSource()
            .sendSuccess(
                () ->
                    Component.literal(
                        "已向 " +
                            npc.getName().getString() +
                            " 提交 WaitAction(" +
                            ticks +
                            "t)"
                    ),
                false
            );
        return 1;
    }

    private static int forceGoal(CommandContext<CommandSourceStack> context) {
        LivingEntity npc = getLivingEntity(context, "npc");
        if (npc == null) {
            return 0;
        }
        INpcMind mind = getMindOrReport(context, npc);
        if (mind == null) {
            return 0;
        }
        String goalName = StringArgumentType.getString(context, "goal");
        if ("pick_up_item".equals(goalName)) {
            context
                .getSource()
                .sendFailure(
                    Component.literal(
                        "pick_up_item 需要指定 item 实体，请使用 /mind plan pickup <npc> <item> [priority]"
                    )
                );
            return 0;
        }
        IGoal goal = NpcMindRegistry.createGoal(goalName);
        if (goal == null) {
            context
                .getSource()
                .sendFailure(Component.literal("未知目标: " + goalName));
            return 0;
        }

        // 确保注册
        if (!mind.getGoalSelector().containsGoal(goal.getName())) {
            mind.getGoalSelector().registerGoal(goal);
        }

        mind.getGoalSelector().forceSwitchTo(mind, npc, goal);
        context
            .getSource()
            .sendSuccess(
                () ->
                    Component.literal(
                        "强制切换 " +
                            npc.getName().getString() +
                            " 的目标为: " +
                            goal.getName()
                    ),
                false
            );
        return 1;
    }

    private static int runPickupPlan(
        CommandContext<CommandSourceStack> context
    ) {
        return runPickupPlanInternal(context, 1.0F, false);
    }

    private static int runPickupPlanWithPriority(
        CommandContext<CommandSourceStack> context
    ) {
        double priority = DoubleArgumentType.getDouble(context, "priority");
        return runPickupPlanInternal(context, (float) priority, true);
    }

    private static int runPickupPlanInternal(
        CommandContext<CommandSourceStack> context,
        float priority,
        boolean hasPriorityArg
    ) {
        LivingEntity npc = getLivingEntity(context, "npc");
        if (npc == null) {
            return 0;
        }
        Entity targetEntity = getEntity(context, "item");
        if (!(targetEntity instanceof ItemEntity itemEntity)) {
            context
                .getSource()
                .sendFailure(
                    Component.literal("item 需要是掉落实体(ItemEntity)")
                );
            return 0;
        }
        INpcMind mind = getMindOrReport(context, npc);
        if (mind == null) {
            return 0;
        }

        var goal = new com.Kizunad.customNPCs.ai.decision.goals.PickUpItemGoal(
            itemEntity,
            priority
        );
        mind.getGoalSelector().registerGoal(goal);
        mind.getGoalSelector().forceSwitchTo(mind, npc, goal);

        context
            .getSource()
            .sendSuccess(
                () ->
                    Component.literal(
                        "已为 " +
                            npc.getName().getString() +
                            " 启动拾取计划 pick_up_item -> " +
                            itemEntity.getDisplayName().getString() +
                            " 优先级 " +
                            priority +
                            (hasPriorityArg ? "" : " (默认)")
                    ),
                false
            );
        return 1;
    }

    private static int runPlanGoal(CommandContext<CommandSourceStack> context) {
        LivingEntity npc = getLivingEntity(context, "npc");
        if (npc == null) {
            return 0;
        }
        INpcMind mind = getMindOrReport(context, npc);
        if (mind == null) {
            return 0;
        }
        String goalName = StringArgumentType.getString(context, "goal");
        IGoal goal = NpcMindRegistry.createGoal(goalName);
        if (goal == null) {
            context
                .getSource()
                .sendFailure(Component.literal("未知目标: " + goalName));
            return 0;
        }
        if (!(goal instanceof PlanBasedGoal planGoal)) {
            context
                .getSource()
                .sendFailure(
                    Component.literal("目标不是 PlanBasedGoal: " + goalName)
                );
            return 0;
        }

        if (!mind.getGoalSelector().containsGoal(goal.getName())) {
            mind.getGoalSelector().registerGoal(goal);
        }

        mind.getActionExecutor().stopCurrentPlan();
        planGoal.start(mind, npc);
        context
            .getSource()
            .sendSuccess(
                () ->
                    Component.literal(
                        "已启动计划目标: " +
                            goal.getName() +
                            " -> " +
                            npc.getName().getString()
                    ),
                false
            );
        return 1;
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

    private static int toggleLog(
        CommandSourceStack source,
        MindLogCategory category,
        boolean enabled
    ) {
        MindLog.setCategoryEnabled(category, enabled);
        source.sendSuccess(
            () ->
                Component.literal(
                    "[" +
                        category.getDisplayName() +
                        "] 日志已" +
                        (enabled ? "启用" : "禁用")
                ),
            false
        );
        return 1;
    }

    private static int toggleAllLogs(
        CommandSourceStack source,
        boolean enabled
    ) {
        MindLog.setAllEnabled(enabled);
        source.sendSuccess(
            () ->
                Component.literal(
                    "所有 NpcMind 日志已" + (enabled ? "启用" : "禁用")
                ),
            false
        );
        return 1;
    }

    private static int showLogStatus(CommandSourceStack source) {
        String status = Arrays.stream(MindLogCategory.values())
            .map(
                category ->
                    category.getDisplayName() +
                    "=" +
                    (MindLog.isEnabled(category) ? "on" : "off")
            )
            .reduce((left, right) -> left + ", " + right)
            .orElse("无");
        source.sendSuccess(
            () -> Component.literal("日志状态: " + status),
            false
        );
        return 1;
    }

    private static LivingEntity getLivingEntity(
        CommandContext<CommandSourceStack> context,
        String name
    ) {
        try {
            Entity entity = EntityArgument.getEntity(context, name);
            if (entity instanceof LivingEntity living) {
                return living;
            }
            context
                .getSource()
                .sendFailure(Component.literal(name + " 不是生物实体"));
            return null;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(
                    Component.literal("解析实体失败: " + e.getMessage())
                );
            return null;
        }
    }

    private static Entity getEntity(
        CommandContext<CommandSourceStack> context,
        String name
    ) {
        try {
            return EntityArgument.getEntity(context, name);
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(
                    Component.literal("解析实体失败: " + e.getMessage())
                );
            return null;
        }
    }

    private static INpcMind getMindOrReport(
        CommandContext<CommandSourceStack> context,
        LivingEntity npc
    ) {
        if (!npc.hasData(NpcMindAttachment.NPC_MIND)) {
            context
                .getSource()
                .sendFailure(
                    Component.literal(
                        npc.getName().getString() + " 没有 NpcMind"
                    )
                );
            return null;
        }
        return npc.getData(NpcMindAttachment.NPC_MIND);
    }
}
