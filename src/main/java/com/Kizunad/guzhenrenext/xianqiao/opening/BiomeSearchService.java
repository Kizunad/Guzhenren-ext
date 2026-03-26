package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * biome 搜索服务。
 * <p>
 * 该服务只负责“按策略挑选采样源锚点”，不参与任何方块复制/materialize 逻辑。
 * 搜索上下文由调用方注入，以便在不依赖具体世界实现的前提下复用同一套搜索策略。
 * </p>
 *
 * @param <TBiome> biome 表示类型（生产侧通常为 {@code Holder<Biome>}）
 */
public final class BiomeSearchService<TBiome> {

    private static final int CHUNK_SIZE = 16;
    private static final int LEGACY_LOCATE_RADIUS_BLOCKS = 4_096;
    private static final int NEW_TASK_LOCATE_RADIUS_BLOCKS = 1_000;
    private static final int DEFAULT_LOCATE_STEP_BLOCKS = 16;
    private static final int DEFAULT_MAX_FALLBACK_ATTEMPTS = 20;
    private static final double EPSILON = 1.0E-9D;

    private final SearchPolicy searchPolicy;

    public BiomeSearchService(SearchPolicy searchPolicy) {
        this.searchPolicy = Objects.requireNonNull(searchPolicy, "searchPolicy");
    }

    /**
     * legacy 默认搜索策略（保留旧行为语义）。
     */
    public static <TBiome> BiomeSearchService<TBiome> legacyDefault() {
        return new BiomeSearchService<>(
            new SearchPolicy(
                LEGACY_LOCATE_RADIUS_BLOCKS,
                DEFAULT_LOCATE_STEP_BLOCKS,
                DEFAULT_LOCATE_STEP_BLOCKS,
                DEFAULT_MAX_FALLBACK_ATTEMPTS
            )
        );
    }

    /**
     * Task6 新路径默认搜索策略（半径严格限制为 r=1000）。
     */
    public static <TBiome> BiomeSearchService<TBiome> taskSixDefault() {
        return new BiomeSearchService<>(
            new SearchPolicy(
                NEW_TASK_LOCATE_RADIUS_BLOCKS,
                DEFAULT_LOCATE_STEP_BLOCKS,
                DEFAULT_LOCATE_STEP_BLOCKS,
                DEFAULT_MAX_FALLBACK_ATTEMPTS
            )
        );
    }

    /**
     * 执行一次搜索：先 locate，再随机回退；若都未严格命中，则返回质量最高候选。
     *
     * @param targetBiome 目标 biome
     * @param request 搜索请求（仅包含原点）
     * @param random 随机源抽象
     * @param context 世界访问上下文
     * @return 命中候选；完全无候选时返回 null
     */
    @Nullable
    public AnchorCandidate search(
        TBiome targetBiome,
        SearchRequest request,
        IntInRangeRandom random,
        SearchContext<TBiome> context
    ) {
        Objects.requireNonNull(targetBiome, "targetBiome");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(context, "context");

        AnchorCandidate bestCandidate = context.locateClosest(targetBiome, request, searchPolicy);
        if (bestCandidate != null && bestCandidate.quality().strictPass()) {
            return bestCandidate;
        }

        int originChunkX = Math.floorDiv(request.originBlockX(), CHUNK_SIZE);
        int originChunkZ = Math.floorDiv(request.originBlockZ(), CHUNK_SIZE);
        int fallbackChunkRange = Math.floorDiv(searchPolicy.locateSearchRadiusBlocks(), CHUNK_SIZE);

        for (int attempt = 0; attempt < searchPolicy.maxFallbackAttempts(); attempt++) {
            int randomChunkX = originChunkX + random.nextIntBetweenInclusive(-fallbackChunkRange, fallbackChunkRange);
            int randomChunkZ = originChunkZ + random.nextIntBetweenInclusive(-fallbackChunkRange, fallbackChunkRange);
            int chunkMinX = randomChunkX * CHUNK_SIZE;
            int chunkMinZ = randomChunkZ * CHUNK_SIZE;

            if (!isChunkCenterInsideRadius(request, chunkMinX, chunkMinZ, searchPolicy.locateSearchRadiusBlocks())) {
                continue;
            }
            if (!context.chunkCenterMatchesBiome(chunkMinX, chunkMinZ, targetBiome)) {
                continue;
            }

            AnchorCandidate fallbackCandidate = context.evaluateChunk(chunkMinX, chunkMinZ);
            if (fallbackCandidate == null) {
                continue;
            }
            if (fallbackCandidate.quality().strictPass()) {
                return fallbackCandidate;
            }
            bestCandidate = selectBetterCandidate(bestCandidate, fallbackCandidate);
        }

        return bestCandidate;
    }

    private static boolean isChunkCenterInsideRadius(
        SearchRequest request,
        int chunkMinX,
        int chunkMinZ,
        int radiusBlocks
    ) {
        int centerX = chunkMinX + CHUNK_SIZE / 2;
        int centerZ = chunkMinZ + CHUNK_SIZE / 2;
        long dx = (long) centerX - request.originBlockX();
        long dz = (long) centerZ - request.originBlockZ();
        long radiusSquared = (long) radiusBlocks * radiusBlocks;
        return dx * dx + dz * dz <= radiusSquared;
    }

    @Nullable
    private static AnchorCandidate selectBetterCandidate(
        @Nullable AnchorCandidate current,
        AnchorCandidate incoming
    ) {
        if (current == null) {
            return incoming;
        }

        double scoreDelta = incoming.quality().score() - current.quality().score();
        if (scoreDelta > EPSILON) {
            return incoming;
        }
        if (Math.abs(scoreDelta) <= EPSILON) {
            int xCompare = Integer.compare(incoming.anchor().chunkMinX(), current.anchor().chunkMinX());
            if (xCompare < 0) {
                return incoming;
            }
            if (xCompare == 0) {
                int zCompare = Integer.compare(incoming.anchor().chunkMinZ(), current.anchor().chunkMinZ());
                if (zCompare < 0) {
                    return incoming;
                }
                if (zCompare == 0 && incoming.anchor().anchorY() < current.anchor().anchorY()) {
                    return incoming;
                }
            }
        }
        return current;
    }

    /**
     * 随机源抽象，便于把随机行为从 MC 运行时解耦到可测接口。
     */
    @FunctionalInterface
    public interface IntInRangeRandom {

        int nextIntBetweenInclusive(int minInclusive, int maxInclusive);
    }

    /**
     * 搜索时需要的世界访问能力。
     * <p>service 本身不持有世界对象，所有环境访问通过该上下文下沉到调用方。</p>
     */
    public interface SearchContext<TBiome> {

        @Nullable
        AnchorCandidate locateClosest(TBiome targetBiome, SearchRequest request, SearchPolicy policy);

        boolean chunkCenterMatchesBiome(int chunkMinX, int chunkMinZ, TBiome targetBiome);

        @Nullable
        AnchorCandidate evaluateChunk(int chunkMinX, int chunkMinZ);
    }

    public record SearchRequest(int originBlockX, int originBlockZ) {
    }

    public record SearchPolicy(
        int locateSearchRadiusBlocks,
        int locateHorizontalStepBlocks,
        int locateVerticalStepBlocks,
        int maxFallbackAttempts
    ) {

        public SearchPolicy {
            if (locateSearchRadiusBlocks < 0) {
                throw new IllegalArgumentException("locateSearchRadiusBlocks 不能小于 0");
            }
            if (locateHorizontalStepBlocks <= 0) {
                throw new IllegalArgumentException("locateHorizontalStepBlocks 必须大于 0");
            }
            if (locateVerticalStepBlocks <= 0) {
                throw new IllegalArgumentException("locateVerticalStepBlocks 必须大于 0");
            }
            if (maxFallbackAttempts < 0) {
                throw new IllegalArgumentException("maxFallbackAttempts 不能小于 0");
            }
        }
    }

    public record ChunkAnchor(int chunkMinX, int chunkMinZ, int anchorY) {
    }

    public record AnchorQuality(boolean strictPass, double score) {
    }

    public record AnchorCandidate(ChunkAnchor anchor, AnchorQuality quality) {
    }
}
