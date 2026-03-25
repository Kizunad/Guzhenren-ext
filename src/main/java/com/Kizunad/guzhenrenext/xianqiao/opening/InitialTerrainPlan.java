package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 开窍初始地形规划结果。
 */
public record InitialTerrainPlan(
    LayoutTier layoutTier,
    int layoutSize,
    int cellCount,
    CoreAnchor coreAnchor,
    TeleportAnchor teleportAnchor,
    LayoutOrigin layoutOrigin,
    InitialChunkBoundary initialChunkBoundary,
    RingParameters ringParameters,
    BiomeInferenceService.BiomeFallbackPolicy biomeFallbackPolicy,
    List<PlannedTerrainCell> plannedCells
) {

    private static final int ZERO = 0;

    private static final int ONE = 1;

    private static final int TWO = 2;

    private static final int THREE = TWO + ONE;

    private static final int FOUR = TWO * TWO;

    private static final String DEFAULT_BIOME_ID = "minecraft:plains";

    public InitialTerrainPlan {
        Objects.requireNonNull(layoutTier, "layoutTier");
        Objects.requireNonNull(coreAnchor, "coreAnchor");
        Objects.requireNonNull(teleportAnchor, "teleportAnchor");
        Objects.requireNonNull(layoutOrigin, "layoutOrigin");
        Objects.requireNonNull(initialChunkBoundary, "initialChunkBoundary");
        Objects.requireNonNull(ringParameters, "ringParameters");
        Objects.requireNonNull(biomeFallbackPolicy, "biomeFallbackPolicy");
        plannedCells = List.copyOf(Objects.requireNonNull(plannedCells, "plannedCells"));
        if (layoutSize != layoutTier.size()) {
            throw new IllegalArgumentException("layoutSize 必须与 layoutTier 对齐");
        }
        int expectedCellCount = layoutSize * layoutSize;
        if (cellCount != expectedCellCount) {
            throw new IllegalArgumentException("cellCount 必须等于 layoutSize²");
        }
        if (plannedCells.size() != expectedCellCount) {
            throw new IllegalArgumentException("plannedCells 数量必须等于 layoutSize²");
        }
        validateGenerationOrder(plannedCells, expectedCellCount);
    }

    private static void validateGenerationOrder(List<PlannedTerrainCell> cells, int expectedCellCount) {
        Set<Integer> orders = new HashSet<>(cells.size());
        for (PlannedTerrainCell cell : cells) {
            if (cell.generationOrder() < ZERO || cell.generationOrder() >= expectedCellCount) {
                throw new IllegalArgumentException("generationOrder 超出范围");
            }
            if (!orders.add(cell.generationOrder())) {
                throw new IllegalArgumentException("generationOrder 必须唯一");
            }
        }
        if (orders.size() != expectedCellCount) {
            throw new IllegalArgumentException("generationOrder 必须覆盖全部单元");
        }
    }

    public enum LayoutTier {
        ONE_BY_ONE(ONE),
        TWO_BY_TWO(TWO),
        THREE_BY_THREE(THREE),
        FOUR_BY_FOUR(FOUR);

        private final int size;

        LayoutTier(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }
    }

    public record CoreAnchor(
        double seamCenterChunkX,
        double seamCenterChunkZ,
        CoreAnchorSemantics semantics
    ) {

        public CoreAnchor {
            Objects.requireNonNull(semantics, "semantics");
            if (!Double.isFinite(seamCenterChunkX) || !Double.isFinite(seamCenterChunkZ)) {
                throw new IllegalArgumentException("核心锚点必须是有限数值");
            }
        }
    }

    public enum CoreAnchorSemantics {
        ODD_CENTER_CELL,
        SEAM_CENTER
    }

    public record TeleportAnchor(
        int anchorCellX,
        int anchorCellZ,
        int anchorChunkX,
        int anchorChunkZ,
        double anchorChunkCenterX,
        double anchorChunkCenterZ,
        TeleportAnchorSemantics semantics
    ) {

        public TeleportAnchor {
            Objects.requireNonNull(semantics, "semantics");
            if (!Double.isFinite(anchorChunkCenterX) || !Double.isFinite(anchorChunkCenterZ)) {
                throw new IllegalArgumentException("传送锚点中心必须是有限数值");
            }
        }
    }

    public enum TeleportAnchorSemantics {
        ODD_CENTER_CELL,
        EVEN_SEAM_KERNEL_NORTHWEST_CELL
    }

    public record LayoutOrigin(int originChunkX, int originChunkZ, LayoutOriginSemantics semantics) {

        public LayoutOrigin {
            Objects.requireNonNull(semantics, "semantics");
        }
    }

    public enum LayoutOriginSemantics {
        NORTHWEST_CORNER_CHUNK
    }

    public record InitialChunkBoundary(int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {

        public InitialChunkBoundary {
            if (minChunkX > maxChunkX || minChunkZ > maxChunkZ) {
                throw new IllegalArgumentException("chunk 边界闭区间非法");
            }
        }

        public int spanXChunks() {
            return maxChunkX - minChunkX + ONE;
        }

        public int spanZChunks() {
            return maxChunkZ - minChunkZ + ONE;
        }
    }

    public record RingParameters(
        int maxReservedChaosBlocks,
        int safeZoneMaxOutsideDistanceBlocks,
        int warningZoneMaxOutsideDistanceBlocks,
        int lethalZoneStartOutsideDistanceBlocks
    ) {

        public RingParameters {
            if (maxReservedChaosBlocks < ZERO) {
                throw new IllegalArgumentException("maxReservedChaosBlocks 不能小于 0");
            }
            if (safeZoneMaxOutsideDistanceBlocks < ZERO) {
                throw new IllegalArgumentException("safeZoneMaxOutsideDistanceBlocks 不能小于 0");
            }
            if (warningZoneMaxOutsideDistanceBlocks < safeZoneMaxOutsideDistanceBlocks) {
                throw new IllegalArgumentException("warning 阈值不能小于 safezone 阈值");
            }
            if (warningZoneMaxOutsideDistanceBlocks > maxReservedChaosBlocks) {
                throw new IllegalArgumentException("warning 阈值不能超过 maxReservedChaosBlocks");
            }
            if (lethalZoneStartOutsideDistanceBlocks <= warningZoneMaxOutsideDistanceBlocks) {
                throw new IllegalArgumentException("lethal 起点必须大于 warning 阈值");
            }
        }
    }

    public record PlannedTerrainCell(
        int cellX,
        int cellZ,
        int chunkX,
        int chunkZ,
        int generationOrder,
        int ringIndex,
        boolean centerKernel,
        String primaryBiomeId,
        List<String> biomeCandidates
    ) {

        public PlannedTerrainCell {
            if (generationOrder < ZERO) {
                throw new IllegalArgumentException("generationOrder 不能小于 0");
            }
            if (ringIndex < ZERO) {
                throw new IllegalArgumentException("ringIndex 不能小于 0");
            }
            primaryBiomeId = normalizeBiomeId(primaryBiomeId);
            biomeCandidates = normalizeBiomeCandidates(biomeCandidates);
        }

        private static String normalizeBiomeId(String biomeId) {
            if (biomeId == null || biomeId.isBlank()) {
                return DEFAULT_BIOME_ID;
            }
            return biomeId.toLowerCase(Locale.ROOT);
        }

        private static List<String> normalizeBiomeCandidates(List<String> candidates) {
            if (candidates == null || candidates.isEmpty()) {
                return List.of(DEFAULT_BIOME_ID);
            }
            List<String> normalized = candidates.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
            if (normalized.isEmpty()) {
                return List.of(DEFAULT_BIOME_ID);
            }
            return normalized;
        }
    }
}
