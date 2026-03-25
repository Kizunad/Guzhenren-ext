package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.service.OverworldTerrainSampler;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

/**
 * biome 搜索服务。
 * <p>
 * 该服务将“候选 biome 搜索 + fallback 决策”从地形 materializer 中剥离，
 * 用于在不破坏旧逻辑默认行为的前提下，为新初始化路径提供可注入、可测试的受限搜索能力。
 * </p>
 */
public final class BiomeSearchService {

    /** 任务要求：受限搜索半径固定为 r=1000。 */
    public static final int BOUNDED_SEARCH_RADIUS_BLOCKS = 1_000;

    private static final int LOCATE_HORIZONTAL_STEP = 16;

    private static final int LOCATE_VERTICAL_STEP = 16;

    private static final int CHUNK_SIZE = 16;

    private static final String DEFAULT_FALLBACK_BIOME_ID = "minecraft:plains";

    private static final List<String> STABLE_FALLBACK_POOL = List.of(
        "minecraft:plains",
        "minecraft:forest",
        "minecraft:savanna",
        "minecraft:taiga",
        "minecraft:meadow",
        "minecraft:swamp",
        "minecraft:desert",
        "minecraft:badlands"
    );

    /**
     * locate 缝隙接口：用于把世界查询抽象为可测试端口。
     */
    @FunctionalInterface
    public interface BiomeLocatePort {

        /**
         * 执行最近 biome 查询。
         *
         * @param searchOrigin 搜索原点
         * @param biomeId 目标 biome id
         * @param radiusBlocks 搜索半径（方块）
         * @param horizontalStep 水平步长
         * @param verticalStep 垂直步长
         * @return 命中坐标；未命中返回 null
         */
        @Nullable
        BlockPos locate(
            BlockPos searchOrigin,
            String biomeId,
            int radiusBlocks,
            int horizontalStep,
            int verticalStep
        );
    }

    /**
     * 地表高度解析缝隙接口。
     */
    @FunctionalInterface
    public interface SurfaceYPort {

        /**
         * 解析指定 X/Z 的地表顶层 Y。
         *
         * @param x 世界坐标 X
         * @param z 世界坐标 Z
         * @return 地表顶层 Y
         */
        int resolveSurfaceY(int x, int z);
    }

    /**
     * 搜索结果。
     *
     * @param sourceAnchor 采样源锚点，未命中时为 null
     * @param matchedBiomeId 实际命中的 biome id，未命中时为空
     * @param fallbackUsed 是否使用了 fallback 路径
     */
    public record SearchResult(
        @Nullable BlockPos sourceAnchor,
        Optional<String> matchedBiomeId,
        boolean fallbackUsed
    ) {

        public SearchResult {
            matchedBiomeId = Objects.requireNonNull(matchedBiomeId, "matchedBiomeId");
        }
    }

    /**
     * 单次 locate 查询计划项。
     *
     * @param biomeId 本次要查询的 biome id
     * @param radiusBlocks 搜索半径（方块）
     * @param horizontalStep 水平步长
     * @param verticalStep 垂直步长
     * @param fallbackAttempt 是否属于 fallback 查询
     */
    public record LocateAttempt(
        String biomeId,
        int radiusBlocks,
        int horizontalStep,
        int verticalStep,
        boolean fallbackAttempt
    ) {
    }

    /**
     * 创建可注入到 {@link OverworldTerrainSampler} 的受限搜索解析器。
     *
     * @param preferredBiomeIds 候选 biome 列表（按优先级）
     * @param fallbackBiomeId 推断层给出的 related/default fallback，可为空
     * @param deterministicSeed 稳定 fallback 的种子
     * @return 可注入 materializer 的源锚点解析器
     */
    public OverworldTerrainSampler.SourceAnchorResolver createBoundedResolver(
        List<String> preferredBiomeIds,
        @Nullable String fallbackBiomeId,
        long deterministicSeed
    ) {
        List<String> normalizedPreferred = normalizeBiomeIds(preferredBiomeIds);
        return new OverworldTerrainSampler.SourceAnchorResolver() {
            @Override
            @Nullable
            public BlockPos resolve(
                ServerLevel overworldLevel,
                Holder<Biome> targetBiome,
                BlockPos searchOrigin,
                RandomSource random
            ) {
                List<String> mergedCandidates = mergeTargetBiome(normalizedPreferred, targetBiomeIdOf(targetBiome));
                SearchResult result = locateWithFallback(
                    searchOrigin,
                    mergedCandidates,
                    fallbackBiomeId,
                    deterministicSeed,
                    (origin, biomeId, radius, horizontalStep, verticalStep) -> locateInLevel(
                        overworldLevel,
                        origin,
                        biomeId,
                        radius,
                        horizontalStep,
                        verticalStep
                    ),
                    (x, z) -> overworldLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1
                );
                return result.sourceAnchor();
            }
        };
    }

    /**
     * 按“优先候选 -> deterministic fallback”执行受限搜索。
     *
     * @param searchOrigin 搜索原点
     * @param preferredBiomeIds 候选 biome 列表（按优先级）
     * @param fallbackBiomeId 推断层 fallback（related/default），可为空
     * @param deterministicSeed 稳定 fallback 种子
     * @param locatePort locate 查询端口
     * @param surfaceYPort 地表高度端口
     * @return 搜索结果
     */
    public SearchResult locateWithFallback(
        BlockPos searchOrigin,
        List<String> preferredBiomeIds,
        @Nullable String fallbackBiomeId,
        long deterministicSeed,
        BiomeLocatePort locatePort,
        SurfaceYPort surfaceYPort
    ) {
        Objects.requireNonNull(searchOrigin, "searchOrigin");
        Objects.requireNonNull(preferredBiomeIds, "preferredBiomeIds");
        Objects.requireNonNull(locatePort, "locatePort");
        Objects.requireNonNull(surfaceYPort, "surfaceYPort");

        List<LocateAttempt> attempts = planLocateAttempts(
            preferredBiomeIds,
            fallbackBiomeId,
            deterministicSeed,
            searchOrigin.getX(),
            searchOrigin.getZ()
        );
        for (LocateAttempt attempt : attempts) {
            @Nullable BlockPos located = locatePort.locate(
                searchOrigin,
                attempt.biomeId(),
                attempt.radiusBlocks(),
                attempt.horizontalStep(),
                attempt.verticalStep()
            );
            if (located != null) {
                BlockPos sourceAnchor = normalizeLocatedAnchor(located, surfaceYPort);
                return new SearchResult(sourceAnchor, Optional.of(attempt.biomeId()), attempt.fallbackAttempt());
            }
        }
        boolean fallbackUsed = attempts.stream().anyMatch(LocateAttempt::fallbackAttempt);
        return new SearchResult(null, Optional.empty(), fallbackUsed);
    }

    /**
     * 生成 locate 查询计划（纯数据），用于验证半径约束与 fallback 决策。
     *
     * @param preferredBiomeIds 候选 biome 列表（按优先级）
     * @param fallbackBiomeId 推断层 fallback（related/default），可为空
     * @param deterministicSeed 稳定 fallback 种子
     * @param originX 搜索原点 X
     * @param originZ 搜索原点 Z
     * @return 按执行顺序排列的 locate 查询计划
     */
    public List<LocateAttempt> planLocateAttempts(
        List<String> preferredBiomeIds,
        @Nullable String fallbackBiomeId,
        long deterministicSeed,
        int originX,
        int originZ
    ) {
        Objects.requireNonNull(preferredBiomeIds, "preferredBiomeIds");
        List<String> normalizedPreferred = normalizeBiomeIds(preferredBiomeIds);
        List<LocateAttempt> attempts = new ArrayList<>();
        for (String preferredBiomeId : normalizedPreferred) {
            attempts.add(
                new LocateAttempt(
                    preferredBiomeId,
                    BOUNDED_SEARCH_RADIUS_BLOCKS,
                    LOCATE_HORIZONTAL_STEP,
                    LOCATE_VERTICAL_STEP,
                    false
                )
            );
        }

        String fallbackId = resolveDeterministicFallbackBiomeId(
            normalizedPreferred,
            fallbackBiomeId,
            deterministicSeed,
            originX,
            originZ
        );
        if (!normalizedPreferred.contains(fallbackId)) {
            attempts.add(
                new LocateAttempt(
                    fallbackId,
                    BOUNDED_SEARCH_RADIUS_BLOCKS,
                    LOCATE_HORIZONTAL_STEP,
                    LOCATE_VERTICAL_STEP,
                    true
                )
            );
        }
        return List.copyOf(attempts);
    }

    /**
     * 计算 deterministic fallback biome id。
     * <p>
     * 决策顺序：
     * </p>
     * <ol>
     *     <li>若推断层提供 related/default biome，则直接使用该值。</li>
     *     <li>否则在稳定池中基于输入与 seed 做稳定哈希选择。</li>
     * </ol>
     *
     * @param preferredBiomeIds 候选 biome 列表
     * @param fallbackBiomeId 推断层 fallback biome
     * @param deterministicSeed 稳定种子
     * @param searchOrigin 搜索原点
     * @return deterministic fallback biome id
     */
    String resolveDeterministicFallbackBiomeId(
        List<String> preferredBiomeIds,
        @Nullable String fallbackBiomeId,
        long deterministicSeed,
        BlockPos searchOrigin
    ) {
        return resolveDeterministicFallbackBiomeId(
            preferredBiomeIds,
            fallbackBiomeId,
            deterministicSeed,
            searchOrigin.getX(),
            searchOrigin.getZ()
        );
    }

    String resolveDeterministicFallbackBiomeId(
        List<String> preferredBiomeIds,
        @Nullable String fallbackBiomeId,
        long deterministicSeed,
        int originX,
        int originZ
    ) {
        String normalizedFallback = normalizeBiomeId(fallbackBiomeId);
        if (normalizedFallback != null) {
            return normalizedFallback;
        }

        List<String> normalizedPreferred = normalizeBiomeIds(preferredBiomeIds);
        int poolIndex = Math.floorMod(
            Objects.hash(normalizedPreferred, deterministicSeed, originX, originZ),
            STABLE_FALLBACK_POOL.size()
        );
        String stablePoolFallback = STABLE_FALLBACK_POOL.get(poolIndex);
        if (stablePoolFallback != null && !stablePoolFallback.isBlank()) {
            return stablePoolFallback;
        }
        return DEFAULT_FALLBACK_BIOME_ID;
    }

    @Nullable
    private static BlockPos locateInLevel(
        ServerLevel overworldLevel,
        BlockPos searchOrigin,
        String biomeId,
        int radius,
        int horizontalStep,
        int verticalStep
    ) {
        Pair<BlockPos, Holder<Biome>> located = overworldLevel.findClosestBiome3d(
            biomeHolder -> biomeId.equals(targetBiomeIdOf(biomeHolder)),
            searchOrigin,
            radius,
            horizontalStep,
            verticalStep
        );
        return located == null ? null : located.getFirst();
    }

    private static BlockPos normalizeLocatedAnchor(BlockPos locatedPos, SurfaceYPort surfaceYPort) {
        int chunkMinX = alignToChunkMin(locatedPos.getX());
        int chunkMinZ = alignToChunkMin(locatedPos.getZ());
        int centerX = chunkMinX + CHUNK_SIZE / 2;
        int centerZ = chunkMinZ + CHUNK_SIZE / 2;
        int centerY = surfaceYPort.resolveSurfaceY(centerX, centerZ);
        return new BlockPos(chunkMinX, centerY, chunkMinZ);
    }

    private static int alignToChunkMin(int value) {
        return Math.floorDiv(value, CHUNK_SIZE) * CHUNK_SIZE;
    }

    private static List<String> mergeTargetBiome(List<String> preferredBiomeIds, @Nullable String targetBiomeId) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (targetBiomeId != null && !targetBiomeId.isBlank()) {
            merged.add(targetBiomeId);
        }
        merged.addAll(preferredBiomeIds);
        return List.copyOf(merged);
    }

    private static String targetBiomeIdOf(Holder<Biome> biomeHolder) {
        Optional<ResourceKey<Biome>> biomeKey = biomeHolder.unwrapKey();
        if (biomeKey.isPresent()) {
            return biomeKey.get().location().toString().toLowerCase(Locale.ROOT);
        }
        return DEFAULT_FALLBACK_BIOME_ID;
    }

    private static List<String> normalizeBiomeIds(List<String> biomeIds) {
        List<String> normalized = new ArrayList<>();
        for (String biomeId : biomeIds) {
            String value = normalizeBiomeId(biomeId);
            if (value != null) {
                normalized.add(value);
            }
        }
        return normalized.stream().distinct().toList();
    }

    @Nullable
    private static String normalizeBiomeId(@Nullable String biomeId) {
        if (biomeId == null || biomeId.isBlank()) {
            return null;
        }
        return biomeId.toLowerCase(Locale.ROOT);
    }
}
