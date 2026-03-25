package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.block.ApertureCoreBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.command.ApertureCommand;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureInitPhase;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureInitializationState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureOpeningSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ReturnPosition;
import com.Kizunad.guzhenrenext.xianqiao.opening.ApertureBootstrapExecutor;
import com.Kizunad.guzhenrenext.xianqiao.opening.BiomeInferenceService;
import com.Kizunad.guzhenrenext.xianqiao.opening.BiomeSearchService;
import com.Kizunad.guzhenrenext.xianqiao.opening.InitialTerrainPlan;
import com.Kizunad.guzhenrenext.xianqiao.service.OverworldTerrainSampler;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import com.mojang.logging.LogUtils;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public final class ApertureInitializationRuntimeExecutor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    private static final double BLOCK_CENTER_OFFSET = 0.5D;

    private static final int SPAWN_Y_OFFSET = 1;

    private static final int PLATFORM_RADIUS = 1;

    private static final int CHEST_OFFSET_X = 1;

    private static final int CHEST_OFFSET_Y = 0;

    private static final int CHEST_OFFSET_Z = 0;

    private static final int SAMPLE_COVERAGE_MIN_OFFSET = -16;

    private static final int SAMPLE_COVERAGE_MAX_OFFSET = 15;

    private static final int PLANNED_CELL_SIDE_LENGTH = 16;

    private static final double CHUNK_SIZE_BLOCKS = 16.0D;

    private static final double SPIRIT_SEARCH_VERTICAL_INFLATE = 3.0D;

    private final AperturePrimaryPathPlanningHelper primaryPathPlanningHelper;

    private final ApertureBootstrapExecutor bootstrapExecutor;

    public ApertureInitializationRuntimeExecutor() {
        this(new AperturePrimaryPathPlanningHelper(), new ApertureBootstrapExecutor());
    }

    ApertureInitializationRuntimeExecutor(
        AperturePrimaryPathPlanningHelper primaryPathPlanningHelper,
        ApertureBootstrapExecutor bootstrapExecutor
    ) {
        this.primaryPathPlanningHelper = Objects.requireNonNull(primaryPathPlanningHelper, "primaryPathPlanningHelper");
        this.bootstrapExecutor = Objects.requireNonNull(bootstrapExecutor, "bootstrapExecutor");
    }

    public static ResourceKey<Level> apertureDimension() {
        return APERTURE_DIMENSION;
    }

    public void initializeIfNeeded(ServerPlayer player) {
        ServerLevel apertureLevel = player.server.getLevel(APERTURE_DIMENSION);
        if (apertureLevel == null) {
            throw new IllegalStateException("仙窍维度未加载。");
        }

        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        UUID owner = player.getUUID();
        ApertureInfo apertureInfo = worldData.getOrAllocate(owner);
        AperturePrimaryPathPlanningHelper.PlanningBundle planningBundle = resolvePlanningBundle(
            player,
            worldData,
            owner
        );
        bootstrapExecutor.execute(
            owner,
            planningBundle == null ? null : planningBundle.bootstrapInput(),
            new ApertureBootstrapExecutor.WorldDataStateStore(worldData),
            new SinglePlanStore(worldData, planningBundle),
            new RuntimeOwnedWorldMutationOperations(apertureLevel, worldData, player, apertureInfo)
        );
        apertureInfo = worldData.getOrAllocate(owner);
        ApertureCommand.ensureLandSpiritExistsFromSharedEntry(apertureLevel, player, apertureInfo.center());

        ServerLevel currentLevel = player.serverLevel();
        worldData.setReturnPosition(
            owner,
            new ReturnPosition(
                currentLevel.dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
            )
        );

        BlockPos center = apertureInfo.center();
        player.teleportTo(
            apertureLevel,
            center.getX() + BLOCK_CENTER_OFFSET,
            center.getY(),
            center.getZ() + BLOCK_CENTER_OFFSET,
            player.getYRot(),
            player.getXRot()
        );
    }

    private AperturePrimaryPathPlanningHelper.PlanningBundle resolvePlanningBundle(
        ServerPlayer player,
        ApertureWorldData worldData,
        UUID owner
    ) {
        if (worldData.getInitPhase(owner) != ApertureInitPhase.UNINITIALIZED) {
            return null;
        }
        return primaryPathPlanningHelper.createFromPlayer(player);
    }

    private static final class SinglePlanStore implements ApertureBootstrapExecutor.PlanStore {

        private final ApertureWorldData worldData;

        private final AperturePrimaryPathPlanningHelper.PlanningBundle planningBundle;

        private SinglePlanStore(
            ApertureWorldData worldData,
            AperturePrimaryPathPlanningHelper.PlanningBundle planningBundle
        ) {
            this.worldData = Objects.requireNonNull(worldData, "worldData");
            this.planningBundle = planningBundle;
        }

        @Override
        public void saveResolvedPlan(UUID owner, int layoutVersion, long planSeed, InitialTerrainPlan plan) {
            worldData.saveResolvedInitialTerrainPlan(owner, layoutVersion, planSeed, plan);
        }

        @Override
        public Optional<InitialTerrainPlan> loadResolvedPlan(UUID owner, int layoutVersion, long planSeed) {
            Optional<InitialTerrainPlan> persistedPlan = worldData.loadResolvedInitialTerrainPlan(
                owner,
                layoutVersion,
                planSeed
            );
            if (persistedPlan.isPresent()) {
                return persistedPlan;
            }
            if (planningBundle == null) {
                return Optional.empty();
            }
            ApertureBootstrapExecutor.BootstrapInput bootstrapInput = planningBundle.bootstrapInput();
            if (bootstrapInput.layoutVersion() != layoutVersion || bootstrapInput.planSeed() != planSeed) {
                return Optional.empty();
            }
            return Optional.of(planningBundle.initialTerrainPlan());
        }
    }

    private static final class RuntimeOwnedWorldMutationOperations
        implements ApertureBootstrapExecutor.WorldMutationOperations {

        private static final long DEFAULT_BOUNDED_SEARCH_SEED = 0L;

        private static final BiomeSearchService BIOME_SEARCH_SERVICE = new BiomeSearchService();

        private record PlannedCellSearchContext(
            BlockPos searchOrigin,
            RandomSource random,
            long deterministicSeed
        ) {

            private PlannedCellSearchContext {
                Objects.requireNonNull(searchOrigin, "searchOrigin");
                Objects.requireNonNull(random, "random");
            }
        }

        private final ServerLevel apertureLevel;

        private final ApertureWorldData worldData;

        private final ServerPlayer player;

        private final ApertureInfo apertureInfo;

        private RuntimeOwnedWorldMutationOperations(
            ServerLevel apertureLevel,
            ApertureWorldData worldData,
            ServerPlayer player,
            ApertureInfo apertureInfo
        ) {
            this.apertureLevel = Objects.requireNonNull(apertureLevel, "apertureLevel");
            this.worldData = Objects.requireNonNull(worldData, "worldData");
            this.player = Objects.requireNonNull(player, "player");
            this.apertureInfo = Objects.requireNonNull(apertureInfo, "apertureInfo");
        }

        @Override
        public boolean isCellsMaterialized(UUID owner, InitialTerrainPlan plan) {
            if (isCompleted(owner)) {
                return true;
            }
            return areAllPlannedCellsMaterialized(plan, apertureInfo.center())
                || areAllPlannedCellsMaterialized(plan, worldData.getOrAllocate(owner).center());
        }

        @Override
        public void materializeCells(UUID owner, InitialTerrainPlan plan) {
            if (worldData.isApertureInitialized(owner)) {
                return;
            }
            BlockPos originalCenter = apertureInfo.center();
            materializePlannedCells(owner, apertureLevel, player, originalCenter, plan);
            BlockPos resolvedCenter = resolvePlatformCenterAfterSampling(apertureLevel, originalCenter);
            worldData.updateCenter(owner, resolvedCenter);
        }

        @Override
        public boolean isCorePlatformSpawned(UUID owner, InitialTerrainPlan plan) {
            if (isCompleted(owner)) {
                return true;
            }
            return hasPlatformCoreAt(worldData.getOrAllocate(owner).center());
        }

        @Override
        public void spawnCenterPlatformCore(UUID owner, InitialTerrainPlan plan) {
            if (worldData.isApertureInitialized(owner)) {
                return;
            }
            BlockPos resolvedCenter = worldData.getOrAllocate(owner).center();
            createInitialPlatform(apertureLevel, resolvedCenter);
        }

        @Override
        public boolean isSpiritSpawned(UUID owner, InitialTerrainPlan plan) {
            if (isCompleted(owner)) {
                return true;
            }
            return hasBoundSpiritAt(worldData.getOrAllocate(owner).center(), owner);
        }

        @Override
        public void spawnSpirit(UUID owner, InitialTerrainPlan plan) {
            BlockPos resolvedCenter = worldData.getOrAllocate(owner).center();
            ApertureCommand.ensureLandSpiritExistsFromSharedEntry(apertureLevel, player, resolvedCenter);
        }

        @Override
        public boolean isWorldDataFinalized(UUID owner, InitialTerrainPlan plan) {
            return worldData.getInitializationState(owner).initPhase() == ApertureInitPhase.COMPLETED;
        }

        @Override
        public void finalizeWorldData(UUID owner, InitialTerrainPlan plan) {
            ApertureInitializationState existingState = worldData.getInitializationState(owner);
            ApertureOpeningSnapshot openingSnapshot = existingState.openingSnapshot();
            Integer layoutVersion = existingState.layoutVersion();
            Long planSeed = existingState.planSeed();
            BlockPos resolvedCenter = worldData.getOrAllocate(owner).center();
            WorldChunkBoundary worldChunkBoundary = resolveWorldChunkBoundary(plan, resolvedCenter);
            worldData.updateChunkBoundary(
                owner,
                worldChunkBoundary.minChunkX(),
                worldChunkBoundary.maxChunkX(),
                worldChunkBoundary.minChunkZ(),
                worldChunkBoundary.maxChunkZ()
            );
            worldData.setInitializationState(
                owner,
                new ApertureInitializationState(ApertureInitPhase.COMPLETED, openingSnapshot, layoutVersion, planSeed)
            );
        }

        /**
         * 将 planner 局部 chunk 边界映射为玩家仙窍真实世界 chunk 边界。
         * <p>
         * 这里必须复用与 planned-cell 物化相同的 coreAnchor/seam-center 变换基准：
         * 先把局部 chunk 编号映射到世界方块锚点，再把整格 16x16 的覆盖范围转回 chunk 闭区间。
         * 这样可保证 worldData 中 min/maxChunk 与 runtime 实际物化区域严格一致，
         * 并避免偶数布局把 seam-center 误退化为 teleportAnchor 单格中心。
         * </p>
         */
        private static WorldChunkBoundary resolveWorldChunkBoundary(InitialTerrainPlan plan, BlockPos center) {
            InitialTerrainPlan.InitialChunkBoundary localBoundary = plan.initialChunkBoundary();
            InitialTerrainPlan.CoreAnchor coreAnchor = plan.coreAnchor();
            int minBlockX = resolvePlannedChunkMinBlock(
                localBoundary.minChunkX(),
                coreAnchor.seamCenterChunkX(),
                center.getX()
            );
            int maxBlockX = resolvePlannedChunkMaxBlock(
                localBoundary.maxChunkX(),
                coreAnchor.seamCenterChunkX(),
                center.getX()
            );
            int minBlockZ = resolvePlannedChunkMinBlock(
                localBoundary.minChunkZ(),
                coreAnchor.seamCenterChunkZ(),
                center.getZ()
            );
            int maxBlockZ = resolvePlannedChunkMaxBlock(
                localBoundary.maxChunkZ(),
                coreAnchor.seamCenterChunkZ(),
                center.getZ()
            );
            return new WorldChunkBoundary(
                SectionPos.blockToSectionCoord(minBlockX),
                SectionPos.blockToSectionCoord(maxBlockX),
                SectionPos.blockToSectionCoord(minBlockZ),
                SectionPos.blockToSectionCoord(maxBlockZ)
            );
        }

        private static int resolvePlannedChunkMinBlock(int localChunk, double seamCenterChunk, int centerBlock) {
            return centerBlock + (int) Math.round((localChunk - seamCenterChunk) * CHUNK_SIZE_BLOCKS);
        }

        private static int resolvePlannedChunkMaxBlock(int localChunk, double seamCenterChunk, int centerBlock) {
            return resolvePlannedChunkMinBlock(localChunk, seamCenterChunk, centerBlock) + PLANNED_CELL_SIDE_LENGTH - 1;
        }

        private void materializePlannedCells(
            UUID owner,
            ServerLevel apertureLevel,
            ServerPlayer player,
            BlockPos center,
            InitialTerrainPlan plan
        ) {
            ServerLevel overworldLevel = player.server.getLevel(Level.OVERWORLD);
            if (overworldLevel == null) {
                LOGGER.warn(
                    "[ApertureInitializationRuntimeExecutor] 主世界未加载，跳过仙窍初始化地形采样。玩家={}",
                    player.getName().getString()
                );
                return;
            }
            PlannedCellSearchContext searchContext = new PlannedCellSearchContext(
                player.blockPosition(),
                player.getRandom(),
                resolveBoundedSearchSeed(owner)
            );
            LOGGER.info(
                "[ApertureInitializationRuntimeExecutor] 开始执行仙窍规划地形物化。玩家={}，规划单元数={}",
                player.getName().getString(),
                plan.plannedCells().size()
            );

            int successCount = 0;
            List<InitialTerrainPlan.PlannedTerrainCell> plannedCells = plan.plannedCells().stream()
                .sorted((left, right) -> Integer.compare(left.generationOrder(), right.generationOrder()))
                .toList();
            for (InitialTerrainPlan.PlannedTerrainCell plannedCell : plannedCells) {
                if (
                    materializePlannedCell(
                        overworldLevel,
                        apertureLevel,
                        plannedCell,
                        center,
                        plan,
                        searchContext
                    )
                ) {
                    successCount++;
                }
            }
            LOGGER.info(
                "[ApertureInitializationRuntimeExecutor] 仙窍规划地形物化结束。玩家={}，成功单元={}/{}。",
                player.getName().getString(),
                successCount,
                plannedCells.size()
            );
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
            int clampedCoreY = Math.max(
                level.getMinBuildHeight() + 1,
                Math.min(level.getMaxBuildHeight() - 1, rawCoreY)
            );
            return new BlockPos(center.getX(), clampedCoreY, center.getZ());
        }

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

        private boolean isCompleted(UUID owner) {
            return worldData.getInitializationState(owner).initPhase() == ApertureInitPhase.COMPLETED;
        }

        private boolean areAllPlannedCellsMaterialized(InitialTerrainPlan plan, BlockPos center) {
            for (InitialTerrainPlan.PlannedTerrainCell plannedCell : plan.plannedCells()) {
                BlockPos targetAnchor = resolvePlannedCellTargetAnchor(plannedCell, center, plan);
                if (!hasAnyNonAirBlockInPlannedCell(targetAnchor)) {
                    return false;
                }
            }
            return true;
        }

        private boolean hasAnyNonAirBlockInPlannedCell(BlockPos targetAnchor) {
            int minX = targetAnchor.getX();
            int minZ = targetAnchor.getZ();
            for (int offsetX = 0; offsetX < PLANNED_CELL_SIDE_LENGTH; offsetX++) {
                for (int offsetZ = 0; offsetZ < PLANNED_CELL_SIDE_LENGTH; offsetZ++) {
                    int sampleX = minX + offsetX;
                    int sampleZ = minZ + offsetZ;
                    int highestY = apertureLevel.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        sampleX,
                        sampleZ
                    ) - 1;
                    if (highestY < apertureLevel.getMinBuildHeight()) {
                        continue;
                    }
                    BlockPos samplePos = new BlockPos(sampleX, highestY, sampleZ);
                    if (!apertureLevel.getBlockState(samplePos).isAir()) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean hasPlatformCoreAt(BlockPos center) {
            return apertureLevel.getBlockState(center).is(XianqiaoBlocks.apertureCoreBlock());
        }

        private boolean hasBoundSpiritAt(BlockPos center, UUID owner) {
            BlockEntity blockEntity = apertureLevel.getBlockEntity(center);
            if (blockEntity instanceof ApertureCoreBlockEntity coreBlockEntity) {
                UUID boundSpiritUUID = coreBlockEntity.getBoundSpiritUUID();
                if (
                    boundSpiritUUID != null
                        && apertureLevel.getEntity(boundSpiritUUID) instanceof LandSpiritEntity landSpiritEntity
                ) {
                    return owner.equals(landSpiritEntity.getOwnerUUID());
                }
            }
            AABB searchBox = new AABB(center).inflate(2.0D, SPIRIT_SEARCH_VERTICAL_INFLATE, 2.0D);
            return !apertureLevel.getEntitiesOfClass(
                LandSpiritEntity.class,
                searchBox,
                entity -> owner.equals(entity.getOwnerUUID())
            ).isEmpty();
        }

        private static boolean materializePlannedCell(
            ServerLevel overworldLevel,
            ServerLevel apertureLevel,
            InitialTerrainPlan.PlannedTerrainCell plannedCell,
            BlockPos center,
            InitialTerrainPlan plan,
            PlannedCellSearchContext searchContext
        ) {
            BlockPos targetAnchor = resolvePlannedCellTargetAnchor(plannedCell, center, plan);
            List<String> candidateBiomeIds = collectCandidateBiomeIds(plannedCell);
            Holder<Biome> targetBiome = resolveFirstAvailableBiomeHolder(overworldLevel, candidateBiomeIds);
            if (targetBiome == null) {
                LOGGER.warn(
                    "[ApertureInitializationRuntimeExecutor] 仙窍规划单元全部候选 biome 均无法解析，跳过物化。"
                        + "cell=({}, {})，chunk=({}, {})，候选 biome={}。",
                    plannedCell.cellX(),
                    plannedCell.cellZ(),
                    plannedCell.chunkX(),
                    plannedCell.chunkZ(),
                    candidateBiomeIds
                );
                return false;
            }
            String fallbackBiomeId = resolveFallbackBiomeId(plan, candidateBiomeIds);
            OverworldTerrainSampler.SourceAnchorResolver boundedResolver = BIOME_SEARCH_SERVICE.createBoundedResolver(
                candidateBiomeIds,
                fallbackBiomeId,
                searchContext.deterministicSeed()
            );
            OverworldTerrainSampler.SourceSearchRequest sourceSearchRequest =
                new OverworldTerrainSampler.SourceSearchRequest(
                    searchContext.searchOrigin(),
                    searchContext.random(),
                    boundedResolver
                );
            if (
                sampleTerrainWithWarn(
                    overworldLevel,
                    apertureLevel,
                    plannedCell,
                    targetAnchor,
                    targetBiome,
                    sourceSearchRequest
                )
            ) {
                LOGGER.info(
                    "[ApertureInitializationRuntimeExecutor] 仙窍规划单元物化成功。cell=({}, {})，chunk=({}, {})，"
                        + "候选 biome={}，fallbackBiome={}，目标锚点={}",
                    plannedCell.cellX(),
                    plannedCell.cellZ(),
                    plannedCell.chunkX(),
                    plannedCell.chunkZ(),
                    candidateBiomeIds,
                    fallbackBiomeId,
                    targetAnchor
                );
                return true;
            }
            LOGGER.warn(
                "[ApertureInitializationRuntimeExecutor] 仙窍规划单元地形采样失败。cell=({}, {})，chunk=({}, {})，"
                    + "候选 biome={}，fallbackBiome={}，目标锚点={}。",
                plannedCell.cellX(),
                plannedCell.cellZ(),
                plannedCell.chunkX(),
                plannedCell.chunkZ(),
                candidateBiomeIds,
                fallbackBiomeId,
                targetAnchor
            );
            return false;
        }

        private static List<String> collectCandidateBiomeIds(InitialTerrainPlan.PlannedTerrainCell plannedCell) {
            LinkedHashSet<String> orderedBiomeIds = new LinkedHashSet<>();
            orderedBiomeIds.add(plannedCell.primaryBiomeId());
            orderedBiomeIds.addAll(plannedCell.biomeCandidates());
            return List.copyOf(orderedBiomeIds);
        }

        private static BlockPos resolvePlannedCellTargetAnchor(
            InitialTerrainPlan.PlannedTerrainCell plannedCell,
            BlockPos center,
            InitialTerrainPlan plan
        ) {
            InitialTerrainPlan.CoreAnchor coreAnchor = plan.coreAnchor();
            int targetX = center.getX()
                + (int) Math.round((plannedCell.chunkX() - coreAnchor.seamCenterChunkX()) * CHUNK_SIZE_BLOCKS);
            int targetZ = center.getZ()
                + (int) Math.round((plannedCell.chunkZ() - coreAnchor.seamCenterChunkZ()) * CHUNK_SIZE_BLOCKS);
            return new BlockPos(targetX, center.getY(), targetZ);
        }

        private static Holder<Biome> resolveBiomeHolder(ServerLevel overworldLevel, String biomeId) {
            ResourceLocation biomeLocation = ResourceLocation.tryParse(biomeId);
            if (biomeLocation == null) {
                LOGGER.warn(
                    "[ApertureInitializationRuntimeExecutor] 解析 biome 标识失败：{}，已跳过本次尝试。",
                    biomeId
                );
                return null;
            }
            ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeLocation);
            try {
                return overworldLevel.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(biomeKey);
            } catch (IllegalStateException exception) {
                LOGGER.warn(
                    "[ApertureInitializationRuntimeExecutor] 解析 biome 失败：{}，已跳过本次尝试。",
                    biomeLocation,
                    exception
                );
                return null;
            }
        }

        private static Holder<Biome> resolveFirstAvailableBiomeHolder(
            ServerLevel overworldLevel,
            List<String> candidateBiomeIds
        ) {
            for (String biomeId : candidateBiomeIds) {
                Holder<Biome> resolved = resolveBiomeHolder(overworldLevel, biomeId);
                if (resolved != null) {
                    return resolved;
                }
            }
            return null;
        }

        private long resolveBoundedSearchSeed(UUID owner) {
            Long persistedPlanSeed = worldData.getInitializationState(owner).planSeed();
            if (persistedPlanSeed != null) {
                return persistedPlanSeed;
            }
            return DEFAULT_BOUNDED_SEARCH_SEED;
        }

        private record WorldChunkBoundary(int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
        }

        private static String resolveFallbackBiomeId(
            InitialTerrainPlan plan,
            List<String> candidateBiomeIds
        ) {
            if (plan.biomeFallbackPolicy() == BiomeInferenceService.BiomeFallbackPolicy.NONE) {
                return null;
            }
            if (candidateBiomeIds.isEmpty()) {
                return null;
            }
            return candidateBiomeIds.get(candidateBiomeIds.size() - 1);
        }

        private static boolean sampleTerrainWithWarn(
            ServerLevel overworldLevel,
            ServerLevel apertureLevel,
            InitialTerrainPlan.PlannedTerrainCell plannedCell,
            BlockPos targetAnchor,
            Holder<Biome> targetBiome,
            OverworldTerrainSampler.SourceSearchRequest sourceSearchRequest
        ) {
            boolean sampled = OverworldTerrainSampler.sampleAndPlace(
                overworldLevel,
                apertureLevel,
                targetAnchor,
                targetBiome,
                null,
                sourceSearchRequest
            );
            if (!sampled) {
                LOGGER.warn(
                    "[ApertureInitializationRuntimeExecutor] 仙窍规划单元地形采样失败。cell=({}, {})，chunk=({}, {})，目标锚点={}。",
                    plannedCell.cellX(),
                    plannedCell.cellZ(),
                    plannedCell.chunkX(),
                    plannedCell.chunkZ(),
                    targetAnchor
                );
            }
            return sampled;
        }
    }
}
