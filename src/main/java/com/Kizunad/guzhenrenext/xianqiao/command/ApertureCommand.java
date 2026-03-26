package com.Kizunad.guzhenrenext.xianqiao.command;

import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.block.ApertureCoreBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInitializationState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.InitPhase;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ReturnPosition;
import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureEntryFlowService;
import com.Kizunad.guzhenrenext.xianqiao.opening.ApertureBootstrapExecutor;
import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.opening.BiomeSearchService;
import com.Kizunad.guzhenrenext.xianqiao.opening.BiomeInferenceService;
import com.Kizunad.guzhenrenext.xianqiao.opening.InitialTerrainPlan;
import com.Kizunad.guzhenrenext.xianqiao.opening.OpeningLayoutPlanner;
import com.Kizunad.guzhenrenext.xianqiao.opening.OpeningProfileResolver;
import com.Kizunad.guzhenrenext.xianqiao.opening.ResolvedOpeningProfile;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureInitialTerrainStrategy;
import com.Kizunad.guzhenrenext.xianqiao.service.OverworldTerrainSampler;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

/**
 * 仙窍进出传送命令。
 */
public final class ApertureCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 仙窍维度键。 */
    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    /** 平台半径：3x3。 */
    private static final int PLATFORM_RADIUS = 1;

    /** 方块中心偏移（用于传送到格子中心）。 */
    private static final double BLOCK_CENTER_OFFSET = 0.5D;

    /** 出生点安全抬升高度。 */
    private static final int SPAWN_Y_OFFSET = 1;

    private static final int CHUNK_BLOCK_SIZE = 16;

    /** 箱子相对核心的 X 偏移。 */
    private static final int CHEST_OFFSET_X = 1;

    /** 箱子相对核心的 Y 偏移。 */
    private static final int CHEST_OFFSET_Y = 0;

    /** 箱子相对核心的 Z 偏移。 */
    private static final int CHEST_OFFSET_Z = 0;

    private static final int SAMPLE_COVERAGE_MIN_OFFSET = -16;

    private static final int SAMPLE_COVERAGE_MAX_OFFSET = 15;

    private static final BiomeSearchService<Holder<Biome>> TASK_SIX_BIOME_SEARCH_SERVICE =
        BiomeSearchService.taskSixDefault();

    private static final ApertureBootstrapExecutor APERTURE_BOOTSTRAP_EXECUTOR = new ApertureBootstrapExecutor();

    private static final int BOOTSTRAP_LAYOUT_VERSION = 1;

    private static final ApertureEntryFlowService APERTURE_ENTRY_FLOW_SERVICE = new ApertureEntryFlowService();

    private ApertureCommand() {
    }

    /**
     * 注册仙窍命令。
     *
     * @param dispatcher Brigadier 分发器
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("guzhenren")
                .then(Commands.literal("enter_aperture").executes(ApertureCommand::enterAperture))
                .then(Commands.literal("leave_aperture").executes(ApertureCommand::leaveAperture))
        );
    }

    /**
     * 执行进入仙窍逻辑。
     */
    private static int enterAperture(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("需要玩家执行。"));
            return 0;
        }
        ApertureEntryFlowService.EntryResult result = executeUnifiedAscensionEntry(
            player,
            AscensionAttemptEntryChannel.LEGACY_COMMAND_ADAPTER
        );
        if (!result.success()) {
            context.getSource().sendFailure(result.failureMessage());
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    public static ApertureEntryFlowService.EntryResult executeUnifiedAscensionEntry(
        ServerPlayer player,
        AscensionAttemptEntryChannel entryChannel
    ) {
        return APERTURE_ENTRY_FLOW_SERVICE.enter(player, entryChannel, new ApertureEntryFlowService.EntryHooks() {
            @Override
            public ResourceKey<Level> apertureDimension() {
                return APERTURE_DIMENSION;
            }

            @Override
            public ResolvedOpeningProfile resolveAttemptProfile(ServerPlayer serverPlayer, boolean playerInitiated) {
                return new OpeningProfileResolver().resolveFromPlayer(serverPlayer, playerInitiated);
            }

            @Override
            public void initializeApertureIfNeeded(
                ServerLevel level,
                ApertureWorldData worldData,
                ServerPlayer serverPlayer,
                ApertureInfo apertureInfo
            ) {
                ApertureCommand.initializeApertureIfNeeded(level, worldData, serverPlayer, apertureInfo);
            }

            @Override
            public void ensureLandSpiritExists(ServerLevel level, ServerPlayer serverPlayer, BlockPos center) {
                ApertureCommand.ensureLandSpiritExists(level, serverPlayer, center);
            }
        });
    }

    /**
     * 执行离开仙窍逻辑，优先返回进入前位置。
     */
    private static int leaveAperture(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("需要玩家执行。"));
            return 0;
        }
        ServerLevel apertureLevel = player.server.getLevel(APERTURE_DIMENSION);
        if (apertureLevel == null) {
            context.getSource().sendFailure(Component.literal("仙窍维度未加载。"));
            return 0;
        }

        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ReturnPosition returnPosition = worldData.getReturnPosition(player.getUUID());
        if (returnPosition != null) {
            ServerLevel targetLevel = resolveReturnLevel(player, returnPosition);
            if (targetLevel != null) {
                player.teleportTo(
                    targetLevel,
                    returnPosition.x(),
                    returnPosition.y(),
                    returnPosition.z(),
                    returnPosition.yRot(),
                    returnPosition.xRot()
                );
                return Command.SINGLE_SUCCESS;
            }
        }

        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            context.getSource().sendFailure(Component.literal("主世界未加载。"));
            return 0;
        }

        BlockPos spawnPos = overworld.getSharedSpawnPos();
        player.teleportTo(
            overworld,
            spawnPos.getX() + BLOCK_CENTER_OFFSET,
            spawnPos.getY() + SPAWN_Y_OFFSET,
            spawnPos.getZ() + BLOCK_CENTER_OFFSET,
            player.getYRot(),
            player.getXRot()
        );
        return Command.SINGLE_SUCCESS;
    }

    private static ServerLevel resolveReturnLevel(ServerPlayer player, ReturnPosition returnPosition) {
        try {
            ResourceLocation targetDimensionLocation = ResourceLocation.parse(returnPosition.dimensionKey());
            ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, targetDimensionLocation);
            return player.server.getLevel(targetDimension);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 首次进入时初始化仙窍平台、核心与箱子。
     */
    private static void initializeApertureIfNeeded(
        ServerLevel level,
        ApertureWorldData worldData,
        ServerPlayer player,
        ApertureInfo apertureInfo
    ) {
        UUID owner = player.getUUID();
        APERTURE_BOOTSTRAP_EXECUTOR.execute(new ApertureBootstrapExecutor.ExecutionContext() {
            @Override
            public ApertureBootstrapExecutor.PersistedState readState() {
                return toExecutorState(worldData.getInitializationState(owner));
            }

            @Override
            public void writeState(ApertureBootstrapExecutor.PersistedState state) {
                worldData.setInitializationState(owner, toWorldDataState(state));
            }

            @Override
            public ApertureBootstrapExecutor.FrozenBootstrapPlan resolvePlanSnapshot() {
                return resolveFrozenBootstrapPlan(player, apertureInfo.center());
            }

            @Override
            public void materializeCells(ApertureBootstrapExecutor.FrozenBootstrapPlan frozenPlan) {
                InitialTerrainPlan layoutPlan = resolveOpeningLayoutPlan(
                    frozenPlan,
                    worldData.getOrAllocate(owner).center()
                );
                materializeInitialTerrainPlan(level, player, frozenPlan, layoutPlan);
            }

            @Override
            public Object resolvePlatformCenterAfterSampling(ApertureBootstrapExecutor.FrozenBootstrapPlan frozenPlan) {
                BlockPos currentCenter = worldData.getOrAllocate(owner).center();
                if (hasApertureCoreBlock(level, currentCenter)) {
                    return currentCenter;
                }
                InitialTerrainPlan layoutPlan = resolveOpeningLayoutPlan(frozenPlan, currentCenter);
                return ApertureCommand.resolvePlatformCenterFromLayoutPlan(level, layoutPlan);
            }

            @Override
            public void updateCenter(Object center) {
                worldData.updateCenter(owner, requireBlockPos(center));
            }

            @Override
            public void createInitialPlatform(Object center) {
                ApertureCommand.createInitialPlatform(level, requireBlockPos(center));
            }

            @Override
            public void spawnLandSpirit(Object center) {
                ApertureCommand.spawnLandSpirit(level, player, requireBlockPos(center));
            }

            @Override
            public void finalizeWorldData() {
                worldData.markApertureInitialized(owner);
            }
        });
    }

    private static ApertureBootstrapExecutor.FrozenBootstrapPlan resolveFrozenBootstrapPlan(
        ServerPlayer player,
        BlockPos seamCenter
    ) {
        ResolvedOpeningProfile openingProfile = new OpeningProfileResolver().resolveFromPlayer(
            player,
            true
        );
        AscensionConditionSnapshot snapshot = openingProfile.conditionSnapshot();
        BiomeInferenceService.BiomePreferenceResult biomePreference = new BiomeInferenceService().infer(openingProfile);
        InitialTerrainPlan layoutPlan = new OpeningLayoutPlanner().plan(
            openingProfile,
            biomePreference,
            new InitialTerrainPlan.AnchorPoint(seamCenter.getX(), seamCenter.getY(), seamCenter.getZ())
        );
        long planSeed = Integer.toUnsignedLong(Objects.hash(player.getUUID(), layoutPlan));
        if (planSeed == 0L) {
            planSeed = 1L;
        }
        return new ApertureBootstrapExecutor.FrozenBootstrapPlan(
            snapshot,
            BOOTSTRAP_LAYOUT_VERSION,
            planSeed,
            layoutPlan
        );
    }

    private static InitialTerrainPlan resolveOpeningLayoutPlan(
        ApertureBootstrapExecutor.FrozenBootstrapPlan frozenPlan,
        BlockPos seamCenter
    ) {
        if (frozenPlan.hasInitialTerrainPlan()) {
            return frozenPlan.initialTerrainPlan();
        }
        ResolvedOpeningProfile openingProfile = new OpeningProfileResolver()
            .resolveFromSnapshot(frozenPlan.openingSnapshot());
        BiomeInferenceService.BiomePreferenceResult biomePreference = new BiomeInferenceService().infer(openingProfile);
        return new OpeningLayoutPlanner().plan(
            openingProfile,
            biomePreference,
            new InitialTerrainPlan.AnchorPoint(seamCenter.getX(), seamCenter.getY(), seamCenter.getZ())
        );
    }

    private static void materializeInitialTerrainPlan(
        ServerLevel apertureLevel,
        ServerPlayer player,
        ApertureBootstrapExecutor.FrozenBootstrapPlan frozenPlan,
        InitialTerrainPlan layoutPlan
    ) {
        ServerLevel overworldLevel = player.server.getLevel(Level.OVERWORLD);
        if (overworldLevel == null) {
            LOGGER.warn("[ApertureCommand] 主世界未加载，跳过冻结布局材质化。玩家={}", player.getName().getString());
            return;
        }
        BlockPos searchOrigin = player.blockPosition();
        RandomSource deterministicRandom = RandomSource.create(frozenPlan.planSeed());
        LOGGER.info(
            "[ApertureCommand] 开始执行仙窍冻结布局材质化。玩家={}，档位={}，cell数={}。",
            player.getName().getString(),
            layoutPlan.layoutTier(),
            layoutPlan.cellCount()
        );

        int successCount = 0;
        for (InitialTerrainPlan.PlannedCell plannedCell : layoutPlan.orderedCells()) {
            TerrainMaterializationTask materializationTask = buildPlannedTerrainTask(plannedCell);
            if (
                sampleTerrainByTask(
                    overworldLevel,
                    apertureLevel,
                    materializationTask,
                    searchOrigin,
                    deterministicRandom
                )
            ) {
                successCount++;
            }
        }
        LOGGER.info(
            "[ApertureCommand] 仙窍冻结布局材质化结束。玩家={}，成功 cell={}/{}。",
            player.getName().getString(),
            successCount,
            layoutPlan.cellCount()
        );
    }

    private static TerrainMaterializationTask buildPlannedTerrainTask(InitialTerrainPlan.PlannedCell plannedCell) {
        List<ResourceKey<Biome>> priorityBiomes = new ArrayList<>(plannedCell.biomeCandidates().size());
        for (BiomeInferenceService.BiomeKey biomeKey : plannedCell.biomeCandidates()) {
            priorityBiomes.add(toBiomeResourceKey(biomeKey));
        }
        return new TerrainMaterializationTask(
            "布局#" + plannedCell.generationOrder(),
            toBlockPos(plannedCell.anchor()),
            List.copyOf(priorityBiomes)
        );
    }

    private static ResourceKey<Biome> toBiomeResourceKey(BiomeInferenceService.BiomeKey biomeKey) {
        ResourceLocation biomeLocation = ResourceLocation.fromNamespaceAndPath(biomeKey.namespace(), biomeKey.path());
        return ResourceKey.create(Registries.BIOME, biomeLocation);
    }

    private static BlockPos toBlockPos(InitialTerrainPlan.AnchorPoint anchorPoint) {
        return new BlockPos(anchorPoint.x(), anchorPoint.y(), anchorPoint.z());
    }

    private static BlockPos resolvePlatformCenterFromLayoutPlan(ServerLevel level, InitialTerrainPlan layoutPlan) {
        int highestY = level.getMinBuildHeight();
        for (InitialTerrainPlan.PlannedCell plannedCell : layoutPlan.orderedCells()) {
            BlockPos anchor = toBlockPos(plannedCell.anchor());
            for (int xOffset = 0; xOffset < CHUNK_BLOCK_SIZE; xOffset++) {
                for (int zOffset = 0; zOffset < CHUNK_BLOCK_SIZE; zOffset++) {
                    int sampleX = anchor.getX() + xOffset;
                    int sampleZ = anchor.getZ() + zOffset;
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sampleX, sampleZ) - 1;
                    highestY = Math.max(highestY, surfaceY);
                }
            }
        }
        int rawCoreY = highestY + SPAWN_Y_OFFSET;
        int clampedCoreY = Math.max(level.getMinBuildHeight() + 1, Math.min(level.getMaxBuildHeight() - 1, rawCoreY));
        InitialTerrainPlan.AnchorPoint coreAnchor = layoutPlan.coreAnchor();
        return new BlockPos(coreAnchor.x(), clampedCoreY, coreAnchor.z());
    }

    private static BlockPos resolvePlatformCenterAfterSampling(ServerLevel level, BlockPos center) {
        int highestY = level.getMinBuildHeight();
        for (int xOffset = SAMPLE_COVERAGE_MIN_OFFSET; xOffset <= SAMPLE_COVERAGE_MAX_OFFSET; xOffset++) {
            for (int zOffset = SAMPLE_COVERAGE_MIN_OFFSET; zOffset <= SAMPLE_COVERAGE_MAX_OFFSET; zOffset++) {
                int sampleX = center.getX() + xOffset;
                int sampleZ = center.getZ() + zOffset;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sampleX, sampleZ) - 1;
                highestY = Math.max(highestY, surfaceY);
            }
        }

        int rawCoreY = highestY + SPAWN_Y_OFFSET;
        int clampedCoreY = Math.max(level.getMinBuildHeight() + 1, Math.min(level.getMaxBuildHeight() - 1, rawCoreY));
        return new BlockPos(center.getX(), clampedCoreY, center.getZ());
    }

    private static void sampleInitialTerrains(ServerLevel apertureLevel, ServerPlayer player, BlockPos center) {
        ServerLevel overworldLevel = player.server.getLevel(Level.OVERWORLD);
        if (overworldLevel == null) {
            LOGGER.warn("[ApertureCommand] 主世界未加载，跳过仙窍初始化地形采样。玩家={}", player.getName().getString());
            return;
        }
        BlockPos searchOrigin = player.blockPosition();
        RandomSource random = player.getRandom();
        ApertureInitialTerrainStrategy.InitialSamplingPlan samplingPlan = ApertureInitialTerrainStrategy.buildPlan(
            player,
            center
        );
        LOGGER.info(
            "[ApertureCommand] 开始执行仙窍初始化采样策略。玩家={}，道途={}，槽位数={}",
            player.getName().getString(),
            samplingPlan.daoType(),
            samplingPlan.tasks().size()
        );

        int successCount = 0;
        for (ApertureInitialTerrainStrategy.SamplingTask samplingTask : samplingPlan.tasks()) {
            if (
                sampleTerrainByTask(
                    overworldLevel,
                    apertureLevel,
                    buildLegacyTerrainTask(samplingTask),
                    searchOrigin,
                    random
                )
            ) {
                successCount++;
            }
        }
        LOGGER.info(
            "[ApertureCommand] 仙窍初始化采样策略结束。玩家={}，成功槽位={}/{}。",
            player.getName().getString(),
            successCount,
            samplingPlan.tasks().size()
        );
    }

    private static TerrainMaterializationTask buildLegacyTerrainTask(
        ApertureInitialTerrainStrategy.SamplingTask samplingTask
    ) {
        return new TerrainMaterializationTask(
            samplingTask.directionName(),
            samplingTask.targetAnchor(),
            samplingTask.priorityBiomes()
        );
    }

    private static boolean sampleTerrainByTask(
        ServerLevel overworldLevel,
        ServerLevel apertureLevel,
        TerrainMaterializationTask materializationTask,
        BlockPos searchOrigin,
        RandomSource random
    ) {
        List<ResourceKey<Biome>> triedBiomes = new ArrayList<>();
        for (ResourceKey<Biome> biomeKey : materializationTask.priorityBiomes()) {
            triedBiomes.add(biomeKey);
            Holder<Biome> targetBiome = resolveBiomeHolder(overworldLevel, biomeKey);
            if (targetBiome == null) {
                continue;
            }
            if (
                    sampleTerrainWithWarn(
                        overworldLevel,
                        apertureLevel,
                        materializationTask,
                        targetBiome,
                        searchOrigin,
                        random
                )
            ) {
                LOGGER.info(
                    "[ApertureCommand] 仙窍{}向命中候选 biome={}，目标锚点={}。",
                    materializationTask.label(),
                    biomeKey.location(),
                    materializationTask.targetAnchor()
                );
                return true;
            }
            LOGGER.warn(
                "[ApertureCommand] 仙窍{}向候选地形采样失败。biome={}，目标锚点={}。",
                materializationTask.label(),
                biomeKey.location(),
                materializationTask.targetAnchor()
            );
        }

        ResourceKey<Biome> fallbackBiomeKey = ApertureInitialTerrainStrategy.pickRandomFallbackBiome(
            random,
            triedBiomes
        );
        if (fallbackBiomeKey == null) {
            LOGGER.warn(
                "[ApertureCommand] 仙窍{}向无可用随机回退 biome，最终跳过。目标锚点={}。",
                materializationTask.label(),
                materializationTask.targetAnchor()
            );
            return false;
        }
        LOGGER.info(
            "[ApertureCommand] 仙窍{}向触发随机回退，选中 biome={}，目标锚点={}。",
            materializationTask.label(),
            fallbackBiomeKey.location(),
            materializationTask.targetAnchor()
        );

        Holder<Biome> fallbackBiome = resolveBiomeHolder(overworldLevel, fallbackBiomeKey);
        if (fallbackBiome == null) {
            return false;
        }
        boolean sampled = sampleTerrainWithWarn(
            overworldLevel,
            apertureLevel,
            materializationTask,
            fallbackBiome,
            searchOrigin,
            random
        );
        if (sampled) {
            LOGGER.info(
                "[ApertureCommand] 仙窍{}向通过随机回退成功。最终 biome={}，目标锚点={}。",
                materializationTask.label(),
                fallbackBiomeKey.location(),
                materializationTask.targetAnchor()
            );
        } else {
            LOGGER.warn(
                "[ApertureCommand] 仙窍{}向候选与回退均失败。最终回退 biome={}，目标锚点={}。",
                materializationTask.label(),
                fallbackBiomeKey.location(),
                materializationTask.targetAnchor()
            );
        }
        return sampled;
    }

    private static Holder<Biome> resolveBiomeHolder(ServerLevel overworldLevel, ResourceKey<Biome> biomeKey) {
        try {
            return overworldLevel.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
        } catch (IllegalStateException exception) {
            LOGGER.warn("[ApertureCommand] 解析 biome 失败：{}，已跳过本次尝试。", biomeKey.location(), exception);
            return null;
        }
    }

    private static boolean sampleTerrainWithWarn(
        ServerLevel overworldLevel,
        ServerLevel apertureLevel,
        TerrainMaterializationTask materializationTask,
        Holder<Biome> targetBiome,
        BlockPos searchOrigin,
        RandomSource random
    ) {
        boolean sampled = OverworldTerrainSampler.sampleAndPlace(
            overworldLevel,
            apertureLevel,
            materializationTask.targetAnchor(),
            targetBiome,
            null,
            new OverworldTerrainSampler.BiomeSearchAdapter(searchOrigin, random, TASK_SIX_BIOME_SEARCH_SERVICE)
        );
        if (!sampled) {
            LOGGER.warn(
                "[ApertureCommand] 仙窍{}向地形采样失败。目标锚点={}。",
                materializationTask.label(),
                materializationTask.targetAnchor()
            );
        }
        return sampled;
    }

    private record TerrainMaterializationTask(
        String label,
        BlockPos targetAnchor,
        List<ResourceKey<Biome>> priorityBiomes
    ) {
    }

    private static boolean hasApertureCoreBlock(ServerLevel level, BlockPos center) {
        return level.getBlockState(center).is(XianqiaoBlocks.apertureCoreBlock());
    }

    private static BlockPos requireBlockPos(Object center) {
        if (center instanceof BlockPos blockPos) {
            return blockPos;
        }
        throw new IllegalArgumentException("center 必须是 BlockPos");
    }

    private static ApertureBootstrapExecutor.PersistedState toExecutorState(ApertureInitializationState state) {
        return new ApertureBootstrapExecutor.PersistedState(
            toBootstrapPhase(state.initPhase()),
            state.openingSnapshot(),
            state.layoutVersion(),
            state.planSeed()
        );
    }

    private static ApertureInitializationState toWorldDataState(ApertureBootstrapExecutor.PersistedState state) {
        return new ApertureInitializationState(
            toWorldDataPhase(state.phase()),
            state.openingSnapshot(),
            state.layoutVersion(),
            state.planSeed()
        );
    }

    private static ApertureBootstrapExecutor.BootstrapPhase toBootstrapPhase(InitPhase phase) {
        return switch (phase) {
            case UNINITIALIZED -> ApertureBootstrapExecutor.BootstrapPhase.UNINITIALIZED;
            case PLANNED -> ApertureBootstrapExecutor.BootstrapPhase.PLANNED;
            case EXECUTING -> ApertureBootstrapExecutor.BootstrapPhase.EXECUTING;
            case COMPLETED -> ApertureBootstrapExecutor.BootstrapPhase.COMPLETED;
        };
    }

    private static InitPhase toWorldDataPhase(ApertureBootstrapExecutor.BootstrapPhase phase) {
        return switch (phase) {
            case UNINITIALIZED -> InitPhase.UNINITIALIZED;
            case PLANNED -> InitPhase.PLANNED;
            case EXECUTING -> InitPhase.EXECUTING;
            case COMPLETED -> InitPhase.COMPLETED;
        };
    }

    /**
     * 生成初始平台：3x3 基岩 + 核心方块 + 偏移一格箱子。
     */
    private static void createInitialPlatform(ServerLevel level, BlockPos center) {
        int baseY = center.getY() - 1;
        for (int offsetX = -PLATFORM_RADIUS; offsetX <= PLATFORM_RADIUS; offsetX++) {
            for (int offsetZ = -PLATFORM_RADIUS; offsetZ <= PLATFORM_RADIUS; offsetZ++) {
                BlockPos bedrockPos = new BlockPos(center.getX() + offsetX, baseY, center.getZ() + offsetZ);
                level.setBlock(bedrockPos, Blocks.BEDROCK.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        level.setBlock(center, XianqiaoBlocks.apertureCoreBlock().defaultBlockState(), Block.UPDATE_ALL);

        BlockPos chestPos = center.offset(CHEST_OFFSET_X, CHEST_OFFSET_Y, CHEST_OFFSET_Z);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
    }

    /**
     * 保证玩家仙窍中存在地灵。
     * <p>
     * 该方法用于补偿历史数据：旧版本只建平台不生成地灵。
     * 当玩家再次进入仙窍时，如果核心附近范围内未检索到地灵，则自动补生成一只并认主。
     * </p>
     *
     * @param level 仙窍维度
     * @param player 当前玩家
     * @param center 当前玩家仙窍核心坐标
     */
    private static void ensureLandSpiritExists(ServerLevel level, ServerPlayer player, BlockPos center) {
        spawnLandSpirit(level, player, center);
    }

    /**
     * 在核心上方生成并认主一只地灵。
     *
     * @param level 仙窍维度
     * @param player 地灵主人
     * @param center 仙窍核心坐标
     */
    private static void spawnLandSpirit(ServerLevel level, ServerPlayer player, BlockPos center) {
        BlockEntity blockEntity = level.getBlockEntity(center);
        if (!(blockEntity instanceof ApertureCoreBlockEntity coreBlockEntity)) {
            return;
        }

        UUID boundSpiritUUID = coreBlockEntity.getBoundSpiritUUID();
        if (boundSpiritUUID != null) {
            if (level.getEntity(boundSpiritUUID) instanceof LandSpiritEntity) {
                player.sendSystemMessage(Component.literal("地灵已存在，无需重复生成。"));
                return;
            }
            coreBlockEntity.setBoundSpiritUUID(null);
        }

        LandSpiritEntity entity = XianqiaoEntities.LAND_SPIRIT.get().create(level);
        if (entity == null) {
            return;
        }
        entity.setOwnerUUID(player.getUUID());
        entity.setBoundCorePos(center);
        entity.moveTo(
            center.getX() + BLOCK_CENTER_OFFSET,
            center.getY() + SPAWN_Y_OFFSET,
            center.getZ() + BLOCK_CENTER_OFFSET,
            player.getYRot(),
            player.getXRot()
        );
        if (level.addFreshEntity(entity)) {
            coreBlockEntity.setBoundSpiritUUID(entity.getUUID());
        }
    }
}
