package com.Kizunad.guzhenrenext.commands;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionModifier;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.worldgen.BastionRuinLocator;
import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.LiuPaiHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainRemovePayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.ClientboundDomainSyncPayload;
import com.Kizunad.guzhenrenext.kongqiao.domain.network.DomainNetworkHandler;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordSpawner;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.effects.FlyingSwordEffects;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordExpCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthData;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
import com.Kizunad.guzhenrenext.bastion.service.BastionGuardianUpkeepService;
import com.Kizunad.guzhenrenext.bastion.service.BastionThreatService;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 蛊真人扩展调试命令。
 * <p>
 * 注意：CustomNPCs 相关命令（guzhenren_mind）已移除。
 * </p>
 */
public class GuzhenrenDebugCommand {

    private static final double DOMAIN_MAX_RADIUS = 256.0;

    private static final float DOMAIN_DEFAULT_ALPHA = 0.75F;
    private static final float DOMAIN_DEFAULT_ROTATION_SPEED = 0.5F;

    /** 飞剑等级上限（命令参数）。 */
    private static final int MAX_LEVEL_CAP = 1000;

    /** 方块中心偏移。 */
    private static final double CENTER_OFFSET = 0.5;

    /** 品质名称建议提供器。 */
    private static final SuggestionProvider<
        CommandSourceStack
    > QUALITY_SUGGESTIONS = (context, builder) -> {
        for (SwordQuality quality : SwordQuality.values()) {
            builder.suggest(quality.name().toLowerCase());
        }
        return builder.buildFuture();
    };

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        // 注意：guzhenren_mind 命令已移除（CustomNPCs 依赖已解除）

        dispatcher.register(
            Commands.literal("guzhenren_debug")
                .requires(source -> source.hasPermission(2))
                .then(
                    Commands.literal("unlock_all")
                        .executes(GuzhenrenDebugCommand::unlockAllSelf)
                        .then(
                            Commands.argument(
                                "player",
                                EntityArgument.player()
                            ).executes(GuzhenrenDebugCommand::unlockAllPlayer)
                        )
                )
                .then(buildFlyingSwordCommands())
                .then(buildBridgeCommands())
                .then(buildDomainCommands())
                .then(buildBastionCommands())
                .then(buildBastionRuinCommands())
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > buildFlyingSwordCommands() {
        return Commands.literal("flyingsword")
            .then(
                Commands.literal("spawn").executes(
                    GuzhenrenDebugCommand::spawnFlyingSword
                )
            )
            .then(
                Commands.literal("recall").executes(
                    GuzhenrenDebugCommand::recallNearestFlyingSword
                )
            )
            .then(
                Commands.literal("recall_all").executes(
                    GuzhenrenDebugCommand::recallAllFlyingSwords
                )
            )
            .then(
                Commands.literal("restore_all").executes(
                    GuzhenrenDebugCommand::restoreAllFlyingSwords
                )
            )
            .then(
                Commands.literal("restore_one").executes(
                    GuzhenrenDebugCommand::restoreOneFlyingSword
                )
            )
            .then(
                Commands.literal("mode").executes(
                    GuzhenrenDebugCommand::cycleNearestFlyingSwordMode
                )
            )
            .then(
                Commands.literal("list").executes(
                    GuzhenrenDebugCommand::listFlyingSwords
                )
            )
            .then(
                Commands.literal("info").executes(
                    GuzhenrenDebugCommand::showNearestSwordInfo
                )
            )
            .then(
                Commands.literal("setquality").then(
                    Commands.argument("quality", StringArgumentType.word())
                        .suggests(QUALITY_SUGGESTIONS)
                        .executes(GuzhenrenDebugCommand::setNearestSwordQuality)
                )
            )
            .then(
                Commands.literal("setlevel").then(
                    Commands.argument(
                        "level",
                        IntegerArgumentType.integer(1, MAX_LEVEL_CAP)
                    ).executes(GuzhenrenDebugCommand::setNearestSwordLevel)
                )
            )
            .then(
                Commands.literal("addexp").then(
                    Commands.argument(
                        "amount",
                        IntegerArgumentType.integer(1)
                    ).executes(GuzhenrenDebugCommand::addExpToNearestSword)
                )
            )
            .then(
                Commands.literal("breakthrough").executes(
                    GuzhenrenDebugCommand::breakthroughNearestSword
                )
            );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > buildBridgeCommands() {
        return Commands.literal("bridge")
            .then(
                Commands.literal("dump").executes(
                    GuzhenrenDebugCommand::dumpBridge
                )
            )
            .then(
                Commands.literal("add")
                    .then(
                        Commands.literal("daohen").then(
                            Commands.literal("jiandao").then(
                                Commands.argument(
                                    "delta",
                                    DoubleArgumentType.doubleArg()
                                ).executes(
                                    GuzhenrenDebugCommand::addDaoHenJianDao
                                )
                            )
                        )
                    )
                    .then(
                        Commands.literal("liupai").then(
                            Commands.literal("jiandao").then(
                                Commands.argument(
                                    "delta",
                                    DoubleArgumentType.doubleArg()
                                ).executes(
                                    GuzhenrenDebugCommand::addLiuPaiJianDao
                                )
                            )
                        )
                    )
            );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > buildDomainCommands() {
        return Commands.literal("domain")
            .then(
                Commands.literal("spawn").then(
                    Commands.argument("pos", Vec3Argument.vec3()).then(
                        Commands.argument(
                            "radius",
                            DoubleArgumentType.doubleArg(1.0, DOMAIN_MAX_RADIUS)
                        ).then(
                            Commands.argument(
                                "texture",
                                net.minecraft.commands.arguments.ResourceLocationArgument.id()
                            ).executes(GuzhenrenDebugCommand::spawnDomain)
                        )
                    )
                )
            )
            .then(
                Commands.literal("remove").then(
                    Commands.argument(
                        "id",
                        net.minecraft.commands.arguments.UuidArgument.uuid()
                    ).executes(GuzhenrenDebugCommand::removeDomain)
                )
            );
    }

    /** 基地道途名称建议提供器。 */
    private static final SuggestionProvider<
        CommandSourceStack
    > DAO_SUGGESTIONS = (context, builder) -> {
        for (BastionDao dao : BastionDao.values()) {
            builder.suggest(dao.getSerializedName());
        }
        return builder.buildFuture();
    };

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > buildBastionCommands() {
        return Commands.literal("bastion")
            .then(
                Commands.literal("debug").then(
                    Commands.argument("pos", Vec3Argument.vec3())
                        .executes(GuzhenrenDebugCommand::debugBastion)
                )
            )
            .then(
                Commands.literal("create").then(
                    Commands.argument("dao", StringArgumentType.word())
                        .suggests(DAO_SUGGESTIONS)
                        .executes(GuzhenrenDebugCommand::createBastion)
                )
            )
            .then(
                Commands.literal("list").executes(
                    GuzhenrenDebugCommand::listBastions
                )
            )
            .then(
                Commands.literal("info").executes(
                    GuzhenrenDebugCommand::showNearestBastionInfo
                )
            )
            .then(
                Commands.literal("destroy").executes(
                    GuzhenrenDebugCommand::destroyNearestBastion
                )
            )
            .then(
                Commands.literal("seal").then(
                    Commands.argument(
                        "duration",
                        IntegerArgumentType.integer(1)
                    ).executes(GuzhenrenDebugCommand::sealNearestBastion)
                )
            )
            .then(
                Commands.literal("give_core").executes(
                    GuzhenrenDebugCommand::giveBastionCore
                )
            )
            .then(
                Commands.literal("give_node").executes(
                    GuzhenrenDebugCommand::giveBastionNode
                )
            )
            .then(
                Commands.literal("modifier")
                    .then(
                        Commands.literal("add")
                            .then(
                                Commands.argument("modifier", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        for (BastionModifier modifier : BastionModifier.values()) {
                                            builder.suggest(modifier.getSerializedName());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(GuzhenrenDebugCommand::addModifierToNearestBastion)
                            )
                    )
                    .then(
                        Commands.literal("remove")
                            .then(
                                Commands.argument("modifier", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        for (BastionModifier modifier : BastionModifier.values()) {
                                            builder.suggest(modifier.getSerializedName());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(GuzhenrenDebugCommand::removeModifierFromNearestBastion)
                            )
                    )
                    .then(
                        Commands.literal("list")
                            .executes(GuzhenrenDebugCommand::listNearestBastionModifiers)
                    )
            );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > buildBastionRuinCommands() {
        return Commands.literal("bastion_ruin")
            .then(
                Commands.literal("stats")
                    .executes(GuzhenrenDebugCommand::bastionRuinStats)
            )
            .then(
                Commands.literal("tp_last")
                    .executes(GuzhenrenDebugCommand::bastionRuinTpLast)
            )
            .then(
                Commands.literal("tp_last_attempt")
                    .executes(GuzhenrenDebugCommand::bastionRuinTpLastAttempt)
            )
            .then(
                Commands.literal("clear")
                    .executes(GuzhenrenDebugCommand::bastionRuinClear)
            );
    }

    private static BastionModifier parseModifier(final String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        for (BastionModifier modifier : BastionModifier.values()) {
            if (modifier.getSerializedName().equals(text)) {
                return modifier;
            }
        }
        return null;
    }

    private static int addModifierToNearestBastion(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            String modifierText = StringArgumentType.getString(context, "modifier");
            BastionModifier modifier = parseModifier(modifierText);
            if (modifier == null) {
                context.getSource().sendFailure(Component.literal("未知 modifier: " + modifierText));
                return 0;
            }

            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = savedData.findOwnerBastion(player.blockPosition(), BastionConfig.SEARCH_RADIUS);
            if (bastion == null) {
                context.getSource().sendFailure(Component.literal("附近未找到基地"));
                return 0;
            }

            java.util.Set<BastionModifier> newModifiers = new java.util.HashSet<>(bastion.modifiers());
            newModifiers.add(modifier);
            BastionData updated = bastion.withModifiers(newModifiers);
            savedData.updateBastion(updated);

            context.getSource().sendSuccess(
                () -> Component.literal("已为基地 "
                    + bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH)
                    + " 添加词缀: " + modifier.getSerializedName()),
                false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("操作失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int removeModifierFromNearestBastion(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            String modifierText = StringArgumentType.getString(context, "modifier");
            BastionModifier modifier = parseModifier(modifierText);
            if (modifier == null) {
                context.getSource().sendFailure(Component.literal("未知 modifier: " + modifierText));
                return 0;
            }

            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = savedData.findOwnerBastion(player.blockPosition(), BastionConfig.SEARCH_RADIUS);
            if (bastion == null) {
                context.getSource().sendFailure(Component.literal("附近未找到基地"));
                return 0;
            }

            java.util.Set<BastionModifier> newModifiers = new java.util.HashSet<>(bastion.modifiers());
            boolean removed = newModifiers.remove(modifier);
            if (!removed) {
                context.getSource().sendFailure(Component.literal("基地未包含该词缀: " + modifier.getSerializedName()));
                return 0;
            }

            BastionData updated = bastion.withModifiers(newModifiers);
            savedData.updateBastion(updated);

            context.getSource().sendSuccess(
                () -> Component.literal("已为基地 "
                    + bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH)
                    + " 移除词缀: " + modifier.getSerializedName()),
                false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("操作失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int listNearestBastionModifiers(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = savedData.findOwnerBastion(player.blockPosition(), BastionConfig.SEARCH_RADIUS);
            if (bastion == null) {
                context.getSource().sendFailure(Component.literal("附近未找到基地"));
                return 0;
            }

            String modifierText = bastion.modifiers().isEmpty()
                ? "(none)"
                : bastion.modifiers().stream()
                    .map(BastionModifier::getSerializedName)
                    .sorted()
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("(none)");

            context.getSource().sendSuccess(
                () -> Component.literal("基地 "
                    + bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH)
                    + " modifiers: " + modifierText),
                false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("操作失败: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 调试：查看指定位置所属基地的守卫系统状态。
     * <p>
     * 输出内容：
     * <ul>
     *   <li>当前守卫数量（按基地归属计数）</li>
     *   <li>威胁值与威胁等级</li>
     *   <li>孵化巢冷却（下次尝试 tick 与剩余 tick）</li>
     * </ul>
     * </p>
     */
    private static int debugBastion(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            Vec3 vec = Vec3Argument.getVec3(context, "pos");
            BlockPos pos = BlockPos.containing(vec);

            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = savedData.findOwnerBastion(pos, BastionConfig.SEARCH_RADIUS);
            if (bastion == null) {
                context.getSource().sendFailure(Component.literal("该位置附近未找到基地"));
                return 0;
            }

            // 1) 守卫数量（复用运行时计数逻辑，与 upkeep/孵化巢一致）。
            int guardianCount = countBastionGuardians(level, bastion);

            // 2) 威胁值与威胁等级（按配置阈值划分）。
            int threatValue = bastion.threatMeter();
            BastionThreatService.ThreatTier threatTier = BastionThreatService.getThreatTier(bastion);

            // 3) 孵化巢冷却（基地级）。
            long gameTime = level.getGameTime();
            long nextHatcheryTry = savedData.getNextHatcheryTryTick(bastion.id());
            long remaining = Math.max(0L, nextHatcheryTry - gameTime);

            Component msg = Component.literal(String.format(
                "§e[守卫调试]§r 基地 %s @ %s\n" +
                    "  守卫数量: §b%d§r\n" +
                    "  威胁值: §d%d§r 等级: §c%s§r\n" +
                    "  孵化巢冷却: 下次 %d (剩余 %d tick)",
                bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH),
                bastion.corePos().toShortString(),
                guardianCount,
                threatValue,
                threatTier.name(),
                nextHatcheryTry,
                remaining
            ));

            context.getSource().sendSuccess(() -> msg, false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("执行出错: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 统计指定基地范围内属于该基地的守卫数量（与孵化巢/维护费保持一致）。
     */
    private static int countBastionGuardians(ServerLevel level, BastionData bastion) {
        BlockPos core = bastion.corePos();
        int radius = bastion.growthRadius();

        AABB searchBox = new AABB(
            core.getX() - radius,
            core.getY() - BastionGuardianUpkeepService.Constants.GUARDIAN_SEARCH_HEIGHT,
            core.getZ() - radius,
            core.getX() + radius,
            core.getY() + BastionGuardianUpkeepService.Constants.GUARDIAN_SEARCH_HEIGHT,
            core.getZ() + radius
        );

        return (int) level.getEntitiesOfClass(Mob.class, searchBox,
            mob -> BastionGuardianData.isGuardian(mob)
                && BastionGuardianData.belongsToBastion(mob, bastion.id())
        ).size();
    }

    private static int bastionRuinStats(CommandContext<CommandSourceStack> context) {
        int count = BastionRuinLocator.count();
        context.getSource().sendSuccess(
            () -> Component.literal(
                "已记录的 Bastion 遗迹生成次数: " + count
                    + " | 尝试: " + BastionRuinLocator.attemptCount()
                    + " | 成功: " + BastionRuinLocator.successCount()
                    + " | 待激活: " + BastionRuinLocator.pendingCount()
            ),
            false
        );
        BastionRuinLocator.lastSuccess().ifPresent(record -> context.getSource().sendSuccess(
            () -> Component.literal(
                "最近一次: " + record.dimension().location() + " @ " + record.pos().toShortString()
            ),
            false
        ));
        return 1;
    }

    private static int bastionRuinTpLast(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }
            var recordOpt = BastionRuinLocator.lastSuccess();
            if (recordOpt.isEmpty()) {
                context.getSource().sendFailure(Component.literal("尚未记录任何 Bastion 遗迹生成"));
                return 0;
            }
            var record = recordOpt.get();
            if (!record.dimension().equals(level.dimension())) {
                context.getSource().sendFailure(Component.literal(
                    "最近遗迹不在当前维度: " + record.dimension().location()
                ));
                return 0;
            }

            BlockPos pos = record.pos();
            player.teleportTo(level,
                pos.getX() + CENTER_OFFSET,
                pos.getY() + 1.0,
                pos.getZ() + CENTER_OFFSET,
                player.getYRot(), player.getXRot());
            context.getSource().sendSuccess(
                () -> Component.literal("已传送到最近遗迹: " + pos.toShortString()),
                false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("执行出错: " + e.getMessage()));
            return 0;
        }
    }

    private static int bastionRuinTpLastAttempt(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }
            var recordOpt = BastionRuinLocator.lastAttempt();
            if (recordOpt.isEmpty()) {
                context.getSource().sendFailure(Component.literal("尚未记录任何 Bastion 遗迹尝试"));
                return 0;
            }
            var record = recordOpt.get();
            if (!record.dimension().equals(level.dimension())) {
                context.getSource().sendFailure(Component.literal(
                    "最近尝试不在当前维度: " + record.dimension().location()
                ));
                return 0;
            }

            BlockPos pos = record.pos();
            player.teleportTo(level,
                pos.getX() + CENTER_OFFSET,
                pos.getY() + 1.0,
                pos.getZ() + CENTER_OFFSET,
                player.getYRot(), player.getXRot());
            context.getSource().sendSuccess(
                () -> Component.literal("已传送到最近尝试位置: " + pos.toShortString()),
                false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("执行出错: " + e.getMessage()));
            return 0;
        }
    }

    private static int bastionRuinClear(CommandContext<CommandSourceStack> context) {
        BastionRuinLocator.clear();
        context.getSource().sendSuccess(
            () -> Component.literal("已清空 Bastion 遗迹记录"),
            false
        );
        return 1;
    }

    private static int spawnDomain(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            Vec3 pos = Vec3Argument.getVec3(context, "pos");
            double radius = DoubleArgumentType.getDouble(context, "radius");
            ResourceLocation texture =
                net.minecraft.commands.arguments.ResourceLocationArgument.getId(
                    context,
                    "texture"
                );

            UUID domainId = UUID.randomUUID();
            ClientboundDomainSyncPayload payload =
                new ClientboundDomainSyncPayload(
                    domainId,
                    player.getUUID(),
                    pos.x,
                    pos.y,
                    pos.z,
                    radius,
                    1,
                    texture.toString(),
                    com.Kizunad.guzhenrenext.kongqiao.domain.client.DomainRenderer.DEFAULT_HEIGHT_OFFSET,
                    DOMAIN_DEFAULT_ALPHA,
                    DOMAIN_DEFAULT_ROTATION_SPEED
                );
            DomainNetworkHandler.sendDomainSync(payload, pos, level);

            context
                .getSource()
                .sendSuccess(
                    () -> Component.literal("已发送领域同步: " + domainId),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int removeDomain(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            UUID id = net.minecraft.commands.arguments.UuidArgument.getUuid(
                context,
                "id"
            );
            Vec3 center = player.position();

            DomainNetworkHandler.sendDomainRemove(
                new ClientboundDomainRemovePayload(id),
                center,
                level
            );

            context
                .getSource()
                .sendSuccess(
                    () -> Component.literal("已发送领域移除: " + id),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int spawnFlyingSword(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (
                !(player.level() instanceof
                        net.minecraft.server.level.ServerLevel level)
            ) {
                return 0;
            }
            var sword = FlyingSwordSpawner.spawnBasic(level, player);
            if (sword == null) {
                context.getSource().sendFailure(Component.literal("生成失败"));
                return 0;
            }
            context
                .getSource()
                .sendSuccess(() -> Component.literal("已生成飞剑"), true);
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int dumpBridge(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            double daohenJiandao = DaoHenHelper.getDaoHen(
                player,
                DaoHenHelper.DaoType.JIAN_DAO
            );
            double liupaiJiandao = LiuPaiHelper.getLiuPai(
                player,
                LiuPaiHelper.LiuPaiType.JIAN_DAO
            );
            double liupaiGudao = LiuPaiHelper.getLiuPai(
                player,
                LiuPaiHelper.LiuPaiType.GU_DAO
            );

            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            "bridge dump | daohen_jiandao=" +
                                daohenJiandao +
                                " liupai_jiandao=" +
                                liupaiJiandao +
                                " liupai_gudao=" +
                                liupaiGudao
                        ),
                    false
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int addDaoHenJianDao(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            double delta = DoubleArgumentType.getDouble(context, "delta");

            double before = DaoHenHelper.getDaoHen(
                player,
                DaoHenHelper.DaoType.JIAN_DAO
            );
            DaoHenHelper.addDaoHen(
                player,
                DaoHenHelper.DaoType.JIAN_DAO,
                delta
            );
            double after = DaoHenHelper.getDaoHen(
                player,
                DaoHenHelper.DaoType.JIAN_DAO
            );

            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            "已修改 daohen_jiandao: " + before + " -> " + after
                        ),
                    false
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int addLiuPaiJianDao(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            double delta = DoubleArgumentType.getDouble(context, "delta");

            double before = LiuPaiHelper.getLiuPai(
                player,
                LiuPaiHelper.LiuPaiType.JIAN_DAO
            );
            LiuPaiHelper.addLiuPai(
                player,
                LiuPaiHelper.LiuPaiType.JIAN_DAO,
                delta
            );
            double after = LiuPaiHelper.getLiuPai(
                player,
                LiuPaiHelper.LiuPaiType.JIAN_DAO
            );

            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            "已修改 liupai_jiandao: " + before + " -> " + after
                        ),
                    false
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int recallNearestFlyingSword(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (
                !(player.level() instanceof
                        net.minecraft.server.level.ServerLevel level)
            ) {
                return 0;
            }
            FlyingSwordEntity sword = FlyingSwordController.getNearestSword(
                level,
                player
            );
            if (sword == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("附近没有飞剑"));
                return 0;
            }
            FlyingSwordController.recall(sword);
            context
                .getSource()
                .sendSuccess(
                    () -> Component.literal("已请求召回最近飞剑"),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int recallAllFlyingSwords(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (
                !(player.level() instanceof
                        net.minecraft.server.level.ServerLevel level)
            ) {
                return 0;
            }
            int count = FlyingSwordController.recallAll(level, player);
            context
                .getSource()
                .sendSuccess(
                    () -> Component.literal("已请求召回飞剑: " + count),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int restoreAllFlyingSwords(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (
                !(player.level() instanceof
                        net.minecraft.server.level.ServerLevel level)
            ) {
                return 0;
            }
            FlyingSwordStorageAttachment storage =
                KongqiaoAttachments.getFlyingSwordStorage(player);
            if (storage == null || storage.getCount() == 0) {
                context
                    .getSource()
                    .sendFailure(Component.literal("存储中没有飞剑"));
                return 0;
            }

            List<FlyingSwordStorageAttachment.RecalledSword> list =
                storage.getRecalledSwords();
            int success = 0;
            for (FlyingSwordStorageAttachment.RecalledSword rec : list) {
                if (rec == null || rec.itemWithdrawn) {
                    continue;
                }
                if (
                    FlyingSwordSpawner.restoreFromStorage(level, player, rec) !=
                    null
                ) {
                    success++;
                    rec.itemWithdrawn = true;
                }
            }
            storage.clear();
            final int restored = success;
            context
                .getSource()
                .sendSuccess(
                    () -> Component.literal("已恢复飞剑: " + restored),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int restoreOneFlyingSword(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (
                !(player.level() instanceof
                        net.minecraft.server.level.ServerLevel level)
            ) {
                return 0;
            }
            FlyingSwordStorageAttachment storage =
                KongqiaoAttachments.getFlyingSwordStorage(player);
            if (storage == null || storage.getCount() == 0) {
                context
                    .getSource()
                    .sendFailure(Component.literal("存储中没有飞剑"));
                return 0;
            }
            List<FlyingSwordStorageAttachment.RecalledSword> list =
                storage.getRecalledSwords();
            for (int i = 0; i < list.size(); i++) {
                FlyingSwordStorageAttachment.RecalledSword rec = list.get(i);
                if (rec == null || rec.itemWithdrawn) {
                    continue;
                }
                if (
                    FlyingSwordSpawner.restoreFromStorage(level, player, rec) !=
                    null
                ) {
                    rec.itemWithdrawn = true;
                    storage.remove(i);
                    context
                        .getSource()
                        .sendSuccess(
                            () -> Component.literal("已恢复一把飞剑"),
                            true
                        );
                    return 1;
                }
            }
            context
                .getSource()
                .sendFailure(Component.literal("没有可恢复的飞剑"));
            return 0;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int cycleNearestFlyingSwordMode(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (
                !(player.level() instanceof
                        net.minecraft.server.level.ServerLevel level)
            ) {
                return 0;
            }
            FlyingSwordEntity sword = FlyingSwordController.getNearestSword(
                level,
                player
            );
            if (sword == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("附近没有飞剑"));
                return 0;
            }
            var next = FlyingSwordController.cycleAIMode(sword);
            context
                .getSource()
                .sendSuccess(
                    () -> Component.literal("已切换模式为: " + next.name()),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int listFlyingSwords(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (
                !(player.level() instanceof
                        net.minecraft.server.level.ServerLevel level)
            ) {
                return 0;
            }
            List<FlyingSwordEntity> swords =
                FlyingSwordController.getPlayerSwords(level, player);
            FlyingSwordStorageAttachment storage =
                KongqiaoAttachments.getFlyingSwordStorage(player);
            int stored = storage == null ? 0 : storage.getCount();
            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            "飞剑实体=" + swords.size() + " 存储=" + stored
                        ),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 显示最近飞剑的详细信息。
     */
    private static int showNearestSwordInfo(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            FlyingSwordEntity sword = FlyingSwordController.getNearestSword(
                level,
                player
            );
            if (sword == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("附近没有飞剑"));
                return 0;
            }

            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            SwordQuality quality = attrs.getQuality();
            int swordLevel = attrs.getLevel();
            int exp = attrs.getExperience();
            int expNext = attrs.getExpForNextLevel();

            String info = String.format(
                "§e[飞剑信息]§r\n" +
                    "  品质: %s%s§r\n" +
                    "  等级: §b%d§r / %d\n" +
                    "  经验: §a%d§r / %d\n" +
                    "  伤害: §c%.1f§r\n" +
                    "  速度: §9%.2f§r\n" +
                    "  耐久: §e%.0f§r / %.0f\n" +
                    "  模式: §d%s§r",
                quality.getColor().toString(),
                quality.getDisplayName(),
                swordLevel,
                quality.getMaxLevel(),
                exp,
                expNext > 0 ? expNext : exp,
                attrs.getEffectiveDamage(),
                attrs.getEffectiveSpeedMax(),
                attrs.durability,
                attrs.maxDurability,
                sword.getAIModeEnum().getDisplayName()
            );

            context
                .getSource()
                .sendSuccess(() -> Component.literal(info), false);
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 设置最近飞剑的品质。
     */
    private static int setNearestSwordQuality(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            FlyingSwordEntity sword = FlyingSwordController.getNearestSword(
                level,
                player
            );
            if (sword == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("附近没有飞剑"));
                return 0;
            }

            String qualityName = StringArgumentType.getString(
                context,
                "quality"
            );
            SwordQuality quality = SwordQuality.fromName(qualityName);

            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            SwordGrowthData growthData = attrs.getGrowthData();

            // 设置新品质
            growthData.setQualityRaw(quality);

            // 如果当前等级超过新品质上限，调整等级
            if (growthData.getLevel() > quality.getMaxLevel()) {
                growthData.setLevelRaw(quality.getMaxLevel());
            }

            // 重算属性
            attrs.recalculateFromGrowth();

            final SwordQuality finalQuality = quality;
            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            String.format(
                                "已将飞剑品质设置为: %s%s§r (Lv.%d)",
                                finalQuality.getColor().toString(),
                                finalQuality.getDisplayName(),
                                growthData.getLevel()
                            )
                        ),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 设置最近飞剑的等级。
     */
    private static int setNearestSwordLevel(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            FlyingSwordEntity sword = FlyingSwordController.getNearestSword(
                level,
                player
            );
            if (sword == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("附近没有飞剑"));
                return 0;
            }

            int newLevel = IntegerArgumentType.getInteger(context, "level");

            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            SwordGrowthData growthData = attrs.getGrowthData();
            SwordQuality quality = growthData.getQuality();

            // 限制等级不超过品质上限
            int cappedLevel = Math.min(newLevel, quality.getMaxLevel());
            growthData.setLevelRaw(cappedLevel);

            // 重算属性
            attrs.recalculateFromGrowth();

            final int finalLevel = cappedLevel;
            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            String.format(
                                "已将飞剑等级设置为: Lv.%d (上限 Lv.%d)",
                                finalLevel,
                                quality.getMaxLevel()
                            )
                        ),
                    true
                );
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 为最近飞剑添加经验。
     */
    private static int addExpToNearestSword(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            FlyingSwordEntity sword = FlyingSwordController.getNearestSword(
                level,
                player
            );
            if (sword == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("附近没有飞剑"));
                return 0;
            }

            int amount = IntegerArgumentType.getInteger(context, "amount");

            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            int oldLevel = attrs.getLevel();
            SwordGrowthData.ExpAddResult result = attrs.addExperience(amount);

            String message;
            if (result.levelsGained > 0) {
                message = String.format(
                    "添加 %d 经验，升级 %d 次！ Lv.%d → Lv.%d (当前经验: %d)",
                    amount,
                    result.levelsGained,
                    oldLevel,
                    result.newLevel,
                    result.newExp
                );
            } else {
                message = String.format(
                    "添加 %d 经验 (当前经验: %d / %d)",
                    amount,
                    result.newExp,
                    attrs.getExpForNextLevel()
                );
            }

            context
                .getSource()
                .sendSuccess(() -> Component.literal(message), true);
            return 1;
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 尝试突破最近飞剑到下一品质。
     */
    private static int breakthroughNearestSword(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            FlyingSwordEntity sword = FlyingSwordController.getNearestSword(
                level,
                player
            );
            if (sword == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("附近没有飞剑"));
                return 0;
            }

            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            SwordQuality oldQuality = attrs.getQuality();

            SwordExpCalculator.BreakthroughResult result =
                attrs.tryBreakthrough();

            if (result.success) {
                // 播放突破特效
                FlyingSwordEffects.playBreakthroughEffect(
                    sword,
                    oldQuality,
                    result.newQuality
                );

                context
                    .getSource()
                    .sendSuccess(
                        () ->
                            Component.literal(
                                String.format(
                                    "§a突破成功！§r %s%s§r → %s%s§r (Lv.%d, 剩余经验: %d)",
                                    oldQuality.getColor().toString(),
                                    oldQuality.getDisplayName(),
                                    result.newQuality.getColor().toString(),
                                    result.newQuality.getDisplayName(),
                                    result.newLevel,
                                    result.remainingExp
                                )
                            ),
                        true
                    );
                return 1;
            } else {
                context
                    .getSource()
                    .sendFailure(
                        Component.literal("§c突破失败：§r" + result.failReason)
                    );
                return 0;
            }
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int unlockAllSelf(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            return unlockAll(context.getSource(), player);
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int unlockAllPlayer(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            return unlockAll(context.getSource(), player);
        } catch (Exception e) {
            context
                .getSource()
                .sendFailure(Component.literal("执行出错: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }

    private static int unlockAll(
        CommandSourceStack source,
        ServerPlayer player
    ) {
        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        if (unlocks == null) {
            source.sendFailure(
                Component.literal("目标玩家缺少 NianTouUnlocks 附加数据")
            );
            return 0;
        }

        // Debug：一键解锁全部念头用途与杀招，便于快速测试轮盘/推演/UI 流程。
        NianTouUnlockCount nianTouUnlockCount = unlockAllNianTouUsages(unlocks);
        int unlockedShazhao = unlockAllShazhao(unlocks);
        int unlockedNianTouItems = nianTouUnlockCount.items();
        int unlockedNianTouUsages = nianTouUnlockCount.usages();

        unlocks.clearProcess();
        unlocks.setShazhaoMessage("已通过 Debug 命令解锁全部杀招");
        PacketDistributor.sendToPlayer(
            player,
            new PacketSyncNianTouUnlocks(unlocks)
        );

        source.sendSuccess(
            () ->
                Component.literal(
                    "已为玩家解锁全部念头用途与杀招" +
                        " | nianTouItems=" +
                        unlockedNianTouItems +
                        " nianTouUsages=" +
                        unlockedNianTouUsages +
                        " shazhao=" +
                        unlockedShazhao
                ),
            true
        );
        player.sendSystemMessage(
            Component.literal(
                "Debug：已解锁全部念头用途与杀招" +
                    " | nianTouItems=" +
                    unlockedNianTouItems +
                    " nianTouUsages=" +
                    unlockedNianTouUsages +
                    " shazhao=" +
                    unlockedShazhao
            )
        );
        return 1;
    }

    private record NianTouUnlockCount(int items, int usages) {}

    private static NianTouUnlockCount unlockAllNianTouUsages(
        NianTouUnlocks unlocks
    ) {
        int unlockedItems = 0;
        int unlockedUsages = 0;
        for (NianTouData data : NianTouDataManager.getAll()) {
            if (
                data == null || data.itemID() == null || data.itemID().isBlank()
            ) {
                continue;
            }
            ResourceLocation itemId;
            try {
                itemId = ResourceLocation.parse(data.itemID());
            } catch (Exception e) {
                continue;
            }
            unlocks.unlock(itemId);
            unlockedItems++;
            if (data.usages() != null) {
                unlockedUsages += data.usages().size();
            }
        }
        return new NianTouUnlockCount(unlockedItems, unlockedUsages);
    }

    private static int unlockAllShazhao(NianTouUnlocks unlocks) {
        int unlockedShazhao = 0;
        for (ShazhaoData data : ShazhaoDataManager.getAll()) {
            if (
                data == null ||
                data.shazhaoID() == null ||
                data.shazhaoID().isBlank()
            ) {
                continue;
            }
            ResourceLocation shazhaoId;
            try {
                shazhaoId = ResourceLocation.parse(data.shazhaoID());
            } catch (Exception e) {
                continue;
            }
            ResourceLocation migrated = ShazhaoId.migrateLegacyId(shazhaoId);
            if (migrated == null) {
                continue;
            }
            unlocks.unlockShazhao(migrated);
            unlockedShazhao++;
        }
        return unlockedShazhao;
    }

    // ===== 基地调试命令 =====

    /**
     * 基地命令相关常量。
     */
    private static final class BastionConfig {
        /** 基地搜索最大半径。 */
        static final int SEARCH_RADIUS = 128;
        /** 放置基地的最小缓冲距离。 */
        static final int PLACEMENT_BUFFER = 32;
        /** UUID 截取显示长度。 */
        static final int UUID_DISPLAY_LENGTH = 8;
        /** 进度百分比乘数。 */
        static final int PROGRESS_PERCENT_MULTIPLIER = 100;
        /** 每秒 tick 数。 */
        static final long TICKS_PER_SECOND = 20L;

        private BastionConfig() {
        }
    }

    /**
     * 在玩家位置创建基地。
     */
    private static int createBastion(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            String daoName = StringArgumentType.getString(context, "dao");
            BastionDao dao = null;
            for (BastionDao d : BastionDao.values()) {
                if (d.getSerializedName().equals(daoName)) {
                    dao = d;
                    break;
                }
            }
            if (dao == null) {
                context.getSource().sendFailure(
                    Component.literal("未知道途类型: " + daoName)
                );
                return 0;
            }

            BlockPos corePos = player.blockPosition();
            BastionSavedData savedData = BastionSavedData.get(level);

            // 检查是否可以放置
            if (!savedData.canPlaceBastion(corePos, BastionConfig.SEARCH_RADIUS, BastionConfig.PLACEMENT_BUFFER)) {
                context.getSource().sendFailure(
                    Component.literal("该位置距离其他基地太近")
                );
                return 0;
            }

            // 创建基地数据
            BastionData bastion = BastionData.create(
                corePos,
                level.dimension(),
                "default",
                dao,
                level.getGameTime()
            );

            // 放置核心方块
            level.setBlockAndUpdate(
                corePos,
                BastionBlocks.BASTION_CORE.get().defaultBlockState()
            );

            // 注册基地
            savedData.addBastion(bastion);

            final BastionDao finalDao = dao;
            context.getSource().sendSuccess(
                () -> Component.literal(
                    String.format(
                        "已在 %s 创建 %s 基地 (ID: %s)",
                        corePos.toShortString(),
                        finalDao.getSerializedName(),
                        bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH)
                    )
                ),
                true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("执行出错: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 列出当前维度所有基地。
     */
    private static int listBastions(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            BastionSavedData savedData = BastionSavedData.get(level);
            int count = savedData.getBastionCount();

            if (count == 0) {
                context.getSource().sendSuccess(
                    () -> Component.literal("当前维度没有基地"),
                    false
                );
                return 1;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("§e当前维度基地列表 (").append(count).append(")§r\n");

            for (BastionData bastion : savedData.getAllBastions()) {
                sb.append(String.format(
                    "  §b%s§r [%s] %s T%d | 节点: %d | 状态: %s\n",
                    bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH),
                    bastion.primaryDao().getSerializedName(),
                    bastion.corePos().toShortString(),
                    bastion.tier(),
                    bastion.totalNodes(),
                    bastion.state().getSerializedName()
                ));
            }

            context.getSource().sendSuccess(
                () -> Component.literal(sb.toString()),
                false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("执行出错: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 显示最近基地的详细信息。
     */
    private static int showNearestBastionInfo(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = savedData.findOwnerBastion(
                player.blockPosition(),
                BastionConfig.SEARCH_RADIUS
            );

            if (bastion == null) {
                context.getSource().sendFailure(
                    Component.literal("附近没有基地")
                );
                return 0;
            }

            // 显示接管状态，便于调试
            BastionData.CaptureState captureState = bastion.captureState() == null
                ? BastionData.CaptureState.DEFAULT
                : bastion.captureState();
            String captureFlagText = captureState.capturable() ? "是" : "否";
            String captureUntilText = captureState.capturableUntilGameTime() > 0
                ? String.valueOf(captureState.capturableUntilGameTime())
                : "无限期";

            String info = String.format(
                "§e[基地信息]§r\n" +
                    "  ID: §b%s§r\n" +
                    "  道途: §a%s§r\n" +
                    "  位置: %s\n" +
                    "  转数: §c%d§r / %d\n" +
                    "  进化: §e%.1f%%§r\n" +
                    "  节点: §9%d§r\n" +
                    "  半径: §d%d§r\n" +
                    "  资源池: §6%.1f§r\n" +
                    "  状态: §7%s§r\n" +
                    "  接管可用: §a%s§r\n" +
                    "  接管原因: §7%s§r\n" +
                    "  接管窗口截止: §d%s§r",
                bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH),
                bastion.primaryDao().getSerializedName(),
                bastion.corePos().toShortString(),
                bastion.tier(),
                BastionData.DEFAULT_MAX_TIER,
                bastion.evolutionProgress() * BastionConfig.PROGRESS_PERCENT_MULTIPLIER,
                bastion.totalNodes(),
                bastion.growthRadius(),
                bastion.resourcePool(),
                bastion.getEffectiveState(level.getGameTime()).getSerializedName(),
                captureFlagText,
                captureState.reason().name(),
                captureUntilText
            );

            context.getSource().sendSuccess(
                () -> Component.literal(info),
                false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("执行出错: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 销毁最近的基地。
     */
    private static int destroyNearestBastion(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = savedData.findOwnerBastion(
                player.blockPosition(),
                BastionConfig.SEARCH_RADIUS
            );

            if (bastion == null) {
                context.getSource().sendFailure(
                    Component.literal("附近没有基地")
                );
                return 0;
            }

            savedData.markDestroyed(bastion.id(), level.getGameTime());

            context.getSource().sendSuccess(
                () -> Component.literal(
                    "已将基地 " + bastion.id().toString().substring(
                        0, BastionConfig.UUID_DISPLAY_LENGTH
                    ) + " 标记为销毁"
                ),
                true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("执行出错: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 封印最近的基地。
     */
    private static int sealNearestBastion(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            if (!(player.level() instanceof ServerLevel level)) {
                return 0;
            }

            int durationSeconds = IntegerArgumentType.getInteger(
                context,
                "duration"
            );
            long durationTicks = (long) durationSeconds * BastionConfig.TICKS_PER_SECOND;

            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = savedData.findOwnerBastion(
                player.blockPosition(),
                BastionConfig.SEARCH_RADIUS
            );

            if (bastion == null) {
                context.getSource().sendFailure(
                    Component.literal("附近没有基地")
                );
                return 0;
            }

            long sealUntil = level.getGameTime() + durationTicks;
            savedData.applySeal(bastion.id(), sealUntil);

            context.getSource().sendSuccess(
                () -> Component.literal(
                    String.format(
                        "已封印基地 %s %d 秒",
                        bastion.id().toString().substring(0, BastionConfig.UUID_DISPLAY_LENGTH),
                        durationSeconds
                    )
                ),
                true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("执行出错: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 给予玩家基地核心物品。
     */
    private static int giveBastionCore(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            net.minecraft.world.item.ItemStack stack =
                new net.minecraft.world.item.ItemStack(
                    BastionBlocks.BASTION_CORE_ITEM.get()
                );

            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }

            context.getSource().sendSuccess(
                () -> Component.literal("已给予基地核心"),
                true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("执行出错: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 给予玩家基地节点物品。
     */
    private static int giveBastionNode(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            net.minecraft.world.item.ItemStack stack =
                new net.minecraft.world.item.ItemStack(
                    BastionBlocks.BASTION_NODE_ITEM.get()
                );

            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }

            context.getSource().sendSuccess(
                () -> Component.literal("已给予基地节点"),
                true
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                Component.literal("执行出错: " + e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }
}
