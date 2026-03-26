package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * 开局布局规划器。
 * <p>
 * 该规划器只做确定性纯计算，不访问世界、不执行搜索、不落地方块。
 * 输入为 Task2 的画像快照与 Task3 的 biome 倾向结果，输出为 Task4 的初始布局计划。
 * </p>
 */
public final class OpeningLayoutPlanner {

    private static final int CHUNK_BLOCK_SIZE = 16;
    private static final int ORDER_START = 1;
    private static final int SAFEZONE_INSET_CHUNKS = 0;
    private static final int WARNING_BUFFER_BLOCKS = 8;
    private static final int LETHAL_BUFFER_BLOCKS = 16;
    private static final int RESERVED_CHAOS_CHUNKS = 16;

    private static final double RESOURCE_CAP_ZHENYUAN = 120.0D;
    private static final double RESOURCE_CAP_SHOUYUAN = 120.0D;
    private static final double RESOURCE_CAP_JINGLI = 100.0D;
    private static final double RESOURCE_CAP_HUNPO = 100.0D;
    private static final double RESOURCE_CAP_TIZHI = 100.0D;

    private static final double WEIGHT_ZHENYUAN = 0.25D;
    private static final double WEIGHT_SHOUYUAN = 0.20D;
    private static final double WEIGHT_JINGLI = 0.20D;
    private static final double WEIGHT_HUNPO = 0.20D;
    private static final double WEIGHT_TIZHI = 0.15D;
    private static final double PARTIAL_MISSING_PENALTY = 0.15D;
    private static final double HIGH_QUALITY_BONUS = 0.05D;

    private static final double TIER_ONE_UPPER_BOUND = 0.35D;
    private static final double TIER_TWO_UPPER_BOUND = 0.55D;
    private static final double TIER_THREE_UPPER_BOUND = 0.75D;

    /**
     * 规划入口。
     *
     * @param profile Task2 冻结后的画像结果
     * @param biomePreference Task3 推断出的 biome 倾向结果
     * @param seamCenter 接缝中心锚点（偶数布局以该点作为几何中心）
     * @return 纯规划数据
     */
    public InitialTerrainPlan plan(
        ResolvedOpeningProfile profile,
        BiomeInferenceService.BiomePreferenceResult biomePreference,
        InitialTerrainPlan.AnchorPoint seamCenter
    ) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(biomePreference, "biomePreference");
        Objects.requireNonNull(seamCenter, "seamCenter");

        InitialTerrainPlan.LayoutTier layoutTier = resolveLayoutTier(profile);
        int layoutSize = layoutTier.edgeSize();
        InitialTerrainPlan.AnchorPoint layoutOrigin = resolveLayoutOrigin(seamCenter, layoutSize);
        InitialTerrainPlan.AnchorPoint coreAnchor = seamCenter;
        InitialTerrainPlan.AnchorPoint teleportAnchor = seamCenter.offset(0, ORDER_START, 0);

        List<BiomeInferenceService.BiomeKey> baseCandidates = resolveBaseBiomeCandidates(biomePreference);
        List<CellDraft> drafts = buildCellDrafts(layoutSize, layoutOrigin);
        List<InitialTerrainPlan.PlannedCell> orderedCells = materializeOrderedCells(drafts, baseCandidates);
        InitialTerrainPlan.ChunkBoundary boundary = resolveBoundary(orderedCells);
        InitialTerrainPlan.ZoneParameters zoneParameters = new InitialTerrainPlan.ZoneParameters(
            SAFEZONE_INSET_CHUNKS,
            WARNING_BUFFER_BLOCKS,
            LETHAL_BUFFER_BLOCKS,
            RESERVED_CHAOS_CHUNKS
        );

        return new InitialTerrainPlan(
            layoutTier,
            seamCenter,
            layoutOrigin,
            coreAnchor,
            teleportAnchor,
            boundary,
            zoneParameters,
            orderedCells
        );
    }

    private static InitialTerrainPlan.LayoutTier resolveLayoutTier(ResolvedOpeningProfile profile) {
        AscensionConditionSnapshot snapshot = profile.conditionSnapshot();
        double zhenyuanScore = ratio(snapshot.maxZhenyuan(), RESOURCE_CAP_ZHENYUAN);
        double shouyuanScore = ratio(snapshot.shouyuan(), RESOURCE_CAP_SHOUYUAN);
        double jingliScore = ratio(snapshot.maxJingli(), RESOURCE_CAP_JINGLI);
        double hunpoScore = ratio(snapshot.maxHunpo(), RESOURCE_CAP_HUNPO);
        double tizhiScore = ratio(snapshot.tizhi(), RESOURCE_CAP_TIZHI);

        double weighted = (zhenyuanScore * WEIGHT_ZHENYUAN)
            + (shouyuanScore * WEIGHT_SHOUYUAN)
            + (jingliScore * WEIGHT_JINGLI)
            + (hunpoScore * WEIGHT_HUNPO)
            + (tizhiScore * WEIGHT_TIZHI);

        if (snapshot.aptitudeResourceState() == AscensionConditionSnapshot.AptitudeResourceState.ALL_ZERO_OR_MISSING) {
            return InitialTerrainPlan.LayoutTier.ONE_BY_ONE;
        }
        if (snapshot.aptitudeResourceState() == AscensionConditionSnapshot.AptitudeResourceState.PARTIAL_MISSING) {
            weighted = clamp01(weighted - PARTIAL_MISSING_PENALTY);
        }
        if (profile.threeQiEvaluation().highQualityWindow()) {
            weighted = clamp01(weighted + HIGH_QUALITY_BONUS);
        }

        if (weighted < TIER_ONE_UPPER_BOUND) {
            return InitialTerrainPlan.LayoutTier.ONE_BY_ONE;
        }
        if (weighted < TIER_TWO_UPPER_BOUND) {
            return InitialTerrainPlan.LayoutTier.TWO_BY_TWO;
        }
        if (weighted < TIER_THREE_UPPER_BOUND) {
            return InitialTerrainPlan.LayoutTier.THREE_BY_THREE;
        }
        return InitialTerrainPlan.LayoutTier.FOUR_BY_FOUR;
    }

    private static InitialTerrainPlan.AnchorPoint resolveLayoutOrigin(
        InitialTerrainPlan.AnchorPoint seamCenter,
        int layoutSize
    ) {
        int halfSize = layoutSize / 2;
        int offset = halfSize * CHUNK_BLOCK_SIZE;
        return seamCenter.offset(-offset, 0, -offset);
    }

    private static List<CellDraft> buildCellDrafts(int layoutSize, InitialTerrainPlan.AnchorPoint layoutOrigin) {
        List<CellDraft> drafts = new ArrayList<>(layoutSize * layoutSize);
        for (int rowIndex = 0; rowIndex < layoutSize; rowIndex++) {
            for (int columnIndex = 0; columnIndex < layoutSize; columnIndex++) {
                int x = layoutOrigin.x() + (columnIndex * CHUNK_BLOCK_SIZE);
                int z = layoutOrigin.z() + (rowIndex * CHUNK_BLOCK_SIZE);
                InitialTerrainPlan.AnchorPoint anchor = new InitialTerrainPlan.AnchorPoint(x, layoutOrigin.y(), z);
                InitialTerrainPlan.ChunkCoord chunk = new InitialTerrainPlan.ChunkCoord(
                    Math.floorDiv(anchor.x(), CHUNK_BLOCK_SIZE),
                    Math.floorDiv(anchor.z(), CHUNK_BLOCK_SIZE)
                );
                int ringOrder = resolveRingOrder(layoutSize, rowIndex, columnIndex);
                drafts.add(new CellDraft(rowIndex, columnIndex, ringOrder, anchor, chunk));
            }
        }
        return drafts;
    }

    /**
     * 计算中心向外 ring 序。
     * <p>
     * 该公式天然支持奇数中心与偶数接缝中心：
     * size=2 时四格同属 ring0；size=4 时中心 2x2 为 ring0、外圈为 ring1。
     * </p>
     */
    private static int resolveRingOrder(int layoutSize, int rowIndex, int columnIndex) {
        int maxDepth = (layoutSize - 1) / 2;
        int borderDistance = Math.min(
            Math.min(rowIndex, columnIndex),
            Math.min(layoutSize - 1 - rowIndex, layoutSize - 1 - columnIndex)
        );
        return maxDepth - borderDistance;
    }

    private static List<InitialTerrainPlan.PlannedCell> materializeOrderedCells(
        List<CellDraft> drafts,
        List<BiomeInferenceService.BiomeKey> baseCandidates
    ) {
        drafts.sort(
            Comparator.comparingInt(CellDraft::ringOrder)
                .thenComparingInt(CellDraft::rowIndex)
                .thenComparingInt(CellDraft::columnIndex)
        );

        List<InitialTerrainPlan.PlannedCell> orderedCells = new ArrayList<>(drafts.size());
        int generationOrder = ORDER_START;
        for (CellDraft draft : drafts) {
            orderedCells.add(
                new InitialTerrainPlan.PlannedCell(
                    generationOrder,
                    draft.ringOrder(),
                    draft.rowIndex(),
                    draft.columnIndex(),
                    draft.anchor(),
                    draft.chunk(),
                    baseCandidates
                )
            );
            generationOrder++;
        }
        return List.copyOf(orderedCells);
    }

    private static InitialTerrainPlan.ChunkBoundary resolveBoundary(List<InitialTerrainPlan.PlannedCell> orderedCells) {
        int minChunkX = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;
        for (InitialTerrainPlan.PlannedCell cell : orderedCells) {
            int chunkX = cell.chunk().x();
            int chunkZ = cell.chunk().z();
            if (chunkX < minChunkX) {
                minChunkX = chunkX;
            }
            if (chunkX > maxChunkX) {
                maxChunkX = chunkX;
            }
            if (chunkZ < minChunkZ) {
                minChunkZ = chunkZ;
            }
            if (chunkZ > maxChunkZ) {
                maxChunkZ = chunkZ;
            }
        }
        return new InitialTerrainPlan.ChunkBoundary(minChunkX, maxChunkX, minChunkZ, maxChunkZ);
    }

    private static List<BiomeInferenceService.BiomeKey> resolveBaseBiomeCandidates(
        BiomeInferenceService.BiomePreferenceResult biomePreference
    ) {
        LinkedHashSet<BiomeInferenceService.BiomeKey> ordered = new LinkedHashSet<>();
        ordered.add(biomePreference.primaryBiome());
        for (BiomeInferenceService.RankedBiome rankedBiome : biomePreference.rankedBiomes()) {
            ordered.add(rankedBiome.biomeKey());
        }
        if (ordered.isEmpty()) {
            throw new IllegalArgumentException("biomePreference 必须至少包含一个候选 biome");
        }
        return List.copyOf(ordered);
    }

    private static double ratio(double value, double cap) {
        if (cap <= 0.0D) {
            return 0.0D;
        }
        return clamp01(value / cap);
    }

    private static double clamp01(double value) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            return 0.0D;
        }
        if (value >= 1.0D) {
            return 1.0D;
        }
        return value;
    }

    private record CellDraft(
        int rowIndex,
        int columnIndex,
        int ringOrder,
        InitialTerrainPlan.AnchorPoint anchor,
        InitialTerrainPlan.ChunkCoord chunk
    ) {
    }
}
