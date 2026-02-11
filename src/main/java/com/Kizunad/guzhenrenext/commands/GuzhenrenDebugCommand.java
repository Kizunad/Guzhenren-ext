package com.Kizunad.guzhenrenext.commands;

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
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.service.SpiritFavorabilityService;
import com.Kizunad.guzhenrenext.xianqiao.service.SpiritUnlockService;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 蛊真人扩展调试命令。
 * <p>
 * 注意：CustomNPCs 相关命令（guzhenren_mind）已移除。
 * </p>
 */
public class GuzhenrenDebugCommand {

    /** 仙窍维度键。 */
    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    /** spirit set_favorability 最小值。 */
    private static final double SPIRIT_FAVORABILITY_MIN = 0.0D;

    /** spirit set_favorability 最大值。 */
    private static final double SPIRIT_FAVORABILITY_MAX = 100.0D;

    /** spirit set_tier 最小层级。 */
    private static final int SPIRIT_MIN_TIER = 1;

    private static final double DOMAIN_MAX_RADIUS = 256.0;

    private static final float DOMAIN_DEFAULT_ALPHA = 0.75F;
    private static final float DOMAIN_DEFAULT_ROTATION_SPEED = 0.5F;

    /** 飞剑等级上限（命令参数）。 */
    private static final int MAX_LEVEL_CAP = 1000;

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
                .then(buildSpiritCommands())
                // bastion 子系统已移除，对应调试子命令已下线。
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

    /**
     * 构建地灵信任度/转数/Stage 调试子命令。
     * <p>
     * 命令树：
     * guzhrenren_debug spirit set_favorability <value:0..100>
     * guzhrenren_debug spirit add_favorability <delta:double>
     * guzhrenren_debug spirit set_tier <value:int(1..)>
     * guzhrenren_debug spirit stage
     * </p>
     */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
        CommandSourceStack
    > buildSpiritCommands() {
        return Commands.literal("spirit")
            .then(
                Commands.literal("set_favorability")
                    .then(
                        Commands.argument(
                            "value",
                            DoubleArgumentType.doubleArg(
                                SPIRIT_FAVORABILITY_MIN,
                                SPIRIT_FAVORABILITY_MAX
                            )
                        ).executes(
                            GuzhenrenDebugCommand::setSpiritFavorabilityAbsolute
                        )
                    )
            )
            .then(
                Commands.literal("add_favorability")
                    .then(
                        Commands.argument("delta", DoubleArgumentType.doubleArg())
                            .executes(GuzhenrenDebugCommand::addSpiritFavorability)
                    )
            )
            .then(
                Commands.literal("set_tier")
                    .then(
                        Commands.argument(
                            "value",
                            IntegerArgumentType.integer(SPIRIT_MIN_TIER)
                        ).executes(GuzhenrenDebugCommand::setSpiritTier)
                    )
            )
            .then(Commands.literal("stage").executes(GuzhenrenDebugCommand::querySpiritStage));
    }

    /**
     * 获取仙窍维度。
     * <p>
     * 所有 spirit 子命令都依赖仙窍维度存档数据；若维度不存在，统一给出失败提示。
     * </p>
     */
    private static ServerLevel getApertureLevelOrFail(CommandSourceStack source) {
        ServerLevel apertureLevel = source.getServer().getLevel(APERTURE_DIMENSION);
        if (apertureLevel == null) {
            source.sendFailure(Component.literal("仙窍维度未加载。"));
            return null;
        }
        return apertureLevel;
    }

    /**
     * 读取执行者的仙窍信息。
     * <p>
     * 仅用于 spirit 调试命令；无仙窍记录时直接返回失败，避免后续服务调用语义不明确。
     * </p>
     */
    private static ApertureInfo getApertureInfoOrFail(
        CommandSourceStack source,
        ServerLevel apertureLevel,
        UUID ownerUuid
    ) {
        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ApertureInfo info = worldData.getAperture(ownerUuid);
        if (info == null) {
            source.sendFailure(Component.literal("未找到玩家仙窍数据，请先创建仙窍。"));
            return null;
        }
        return info;
    }

    /**
     * 绝对设置地灵好感度。
     * <p>
     * 由于服务层仅暴露增量入口，因此这里先读取当前值，计算 delta=target-current，
     * 再调用 tryAddFavorability 完成写入。
     * </p>
     */
    private static int setSpiritFavorabilityAbsolute(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerLevel apertureLevel = getApertureLevelOrFail(context.getSource());
            if (apertureLevel == null) {
                return 0;
            }
            ApertureInfo info = getApertureInfoOrFail(
                context.getSource(),
                apertureLevel,
                player.getUUID()
            );
            if (info == null) {
                return 0;
            }

            float targetFavorability = (float) DoubleArgumentType.getDouble(
                context,
                "value"
            );
            float currentFavorability = SpiritFavorabilityService.getFavorability(
                apertureLevel,
                player.getUUID()
            );
            float delta = targetFavorability - currentFavorability;
            boolean changed = SpiritFavorabilityService.tryAddFavorability(
                apertureLevel,
                player.getUUID(),
                delta,
                SpiritFavorabilityService.Reason.DEBUG_COMMAND
            );
            float afterFavorability = SpiritFavorabilityService.getFavorability(
                apertureLevel,
                player.getUUID()
            );

            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            String.format(
                                "spirit 好感度设置: %.2f -> %.2f (目标 %.2f, changed=%s)",
                                currentFavorability,
                                afterFavorability,
                                targetFavorability,
                                changed
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
     * 增量修改地灵好感度。
     * <p>
     * delta 不做命令层限幅，可正可负；最终由 SpiritFavorabilityService 统一 clamp。
     * </p>
     */
    private static int addSpiritFavorability(
        CommandContext<CommandSourceStack> context
    ) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerLevel apertureLevel = getApertureLevelOrFail(context.getSource());
            if (apertureLevel == null) {
                return 0;
            }
            ApertureInfo info = getApertureInfoOrFail(
                context.getSource(),
                apertureLevel,
                player.getUUID()
            );
            if (info == null) {
                return 0;
            }

            float delta = (float) DoubleArgumentType.getDouble(context, "delta");
            float beforeFavorability = SpiritFavorabilityService.getFavorability(
                apertureLevel,
                player.getUUID()
            );
            boolean changed = SpiritFavorabilityService.tryAddFavorability(
                apertureLevel,
                player.getUUID(),
                delta,
                SpiritFavorabilityService.Reason.DEBUG_COMMAND
            );
            float afterFavorability = SpiritFavorabilityService.getFavorability(
                apertureLevel,
                player.getUUID()
            );

            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            String.format(
                                "spirit 好感度增量: %.2f + %.2f -> %.2f (changed=%s)",
                                beforeFavorability,
                                delta,
                                afterFavorability,
                                changed
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
     * 设置地灵转数（tier）。
     */
    private static int setSpiritTier(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerLevel apertureLevel = getApertureLevelOrFail(context.getSource());
            if (apertureLevel == null) {
                return 0;
            }
            ApertureInfo beforeInfo = getApertureInfoOrFail(
                context.getSource(),
                apertureLevel,
                player.getUUID()
            );
            if (beforeInfo == null) {
                return 0;
            }

            int targetTier = IntegerArgumentType.getInteger(context, "value");
            ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
            worldData.updateTier(player.getUUID(), targetTier);
            ApertureInfo afterInfo = worldData.getAperture(player.getUUID());
            if (afterInfo == null) {
                context
                    .getSource()
                    .sendFailure(Component.literal("更新 tier 后读取玩家仙窍数据失败。"));
                return 0;
            }

            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            "spirit tier 设置: " +
                            beforeInfo.tier() +
                            " -> " +
                            afterInfo.tier()
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
     * 查询地灵阶段详情。
     * <p>
     * 输出项包含：tier、favorability、currentStage、stageName、nextStage 门槛。
     * </p>
     */
    private static int querySpiritStage(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerLevel apertureLevel = getApertureLevelOrFail(context.getSource());
            if (apertureLevel == null) {
                return 0;
            }
            ApertureInfo info = getApertureInfoOrFail(
                context.getSource(),
                apertureLevel,
                player.getUUID()
            );
            if (info == null) {
                return 0;
            }

            int tier = info.tier();
            float favorability = info.favorability();
            int currentStage = SpiritUnlockService.computeStage(tier, favorability);
            String stageName = SpiritUnlockService.getStageDisplayName(currentStage);
            int nextStage = SpiritUnlockService.getNextStage(currentStage);
            int nextMinTier = SpiritUnlockService.getMinTierForStage(nextStage);
            float nextMinFavorability = SpiritUnlockService.getMinFavorabilityForStage(
                nextStage
            );
            int maxStage = SpiritUnlockService.getMaxStage();

            context
                .getSource()
                .sendSuccess(
                    () ->
                        Component.literal(
                            String.format(
                                "spirit stage | tier=%d favorability=%.2f currentStage=%d stageName=%s " +
                                "nextStage=%d nextMinTier=%d nextMinFavorability=%.2f maxStage=%d",
                                tier,
                                favorability,
                                currentStage,
                                stageName,
                                nextStage,
                                nextMinTier,
                                nextMinFavorability,
                                maxStage
                            )
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

    // bastion 子系统已移除：相关调试命令与实现已删除。

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

    // ====== 天赋点命令（bastion 依赖）已移除 ======

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

    // ===== 基地调试命令（bastion 依赖）已移除 =====
}
