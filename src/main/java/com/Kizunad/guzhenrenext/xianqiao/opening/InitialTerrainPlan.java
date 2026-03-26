package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.List;
import java.util.Objects;

/**
 * 仙窍初始地形规划结果。
 * <p>
 * 该类型是 Task4 的“纯规划层”输出：
 * </p>
 * <ul>
 *     <li>只表达布局语义与参数，不触碰世界写入；</li>
 *     <li>显式分离 seam center、layout origin、core anchor、teleport anchor；</li>
 *     <li>保留 cell 级别的确定性生成顺序与 biome 候选引用。</li>
 * </ul>
 */
public record InitialTerrainPlan(
    LayoutTier layoutTier,
    AnchorPoint seamCenter,
    AnchorPoint layoutOrigin,
    AnchorPoint coreAnchor,
    AnchorPoint teleportAnchor,
    ChunkBoundary initialChunkBoundary,
    ZoneParameters zoneParameters,
    List<PlannedCell> orderedCells
) {

    /**
     * 紧凑构造器：统一做非空校验与不可变冻结。
     */
    public InitialTerrainPlan {
        layoutTier = Objects.requireNonNull(layoutTier, "layoutTier");
        seamCenter = Objects.requireNonNull(seamCenter, "seamCenter");
        layoutOrigin = Objects.requireNonNull(layoutOrigin, "layoutOrigin");
        coreAnchor = Objects.requireNonNull(coreAnchor, "coreAnchor");
        teleportAnchor = Objects.requireNonNull(teleportAnchor, "teleportAnchor");
        initialChunkBoundary = Objects.requireNonNull(initialChunkBoundary, "initialChunkBoundary");
        zoneParameters = Objects.requireNonNull(zoneParameters, "zoneParameters");
        orderedCells = List.copyOf(Objects.requireNonNull(orderedCells, "orderedCells"));
        if (orderedCells.isEmpty()) {
            throw new IllegalArgumentException("orderedCells 不能为空");
        }
        if (orderedCells.size() != layoutTier.cellCount()) {
            throw new IllegalArgumentException("orderedCells 数量与 layoutTier 不一致");
        }
    }

    /**
     * 当前布局边长（chunk 单元数）。
     */
    public int layoutSize() {
        return layoutTier.edgeSize();
    }

    /**
     * 当前布局总 cell 数。
     */
    public int cellCount() {
        return layoutTier.cellCount();
    }

    /**
     * 布局档位：固定四档，分别对应 1x1 / 2x2 / 3x3 / 4x4。
     */
    public enum LayoutTier {
        ONE_BY_ONE(1),
        TWO_BY_TWO(2),
        THREE_BY_THREE(3),
        FOUR_BY_FOUR(4);

        private final int edgeSize;

        LayoutTier(int edgeSize) {
            this.edgeSize = edgeSize;
        }

        public int edgeSize() {
            return edgeSize;
        }

        public int cellCount() {
            return edgeSize * edgeSize;
        }
    }

    /**
     * 规划层通用锚点，使用纯整数坐标，避免绑定运行时世界对象。
     */
    public record AnchorPoint(int x, int y, int z) {

        public AnchorPoint offset(int offsetX, int offsetY, int offsetZ) {
            return new AnchorPoint(x + offsetX, y + offsetY, z + offsetZ);
        }
    }

    /**
     * chunk 坐标锚点。
     */
    public record ChunkCoord(int x, int z) {
    }

    /**
     * 初始边界：min/max chunk 闭区间，保持 chunk-boundary-truth 语义。
     */
    public record ChunkBoundary(int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {

        public ChunkBoundary {
            int normalizedMinChunkX = Math.min(minChunkX, maxChunkX);
            int normalizedMaxChunkX = Math.max(minChunkX, maxChunkX);
            int normalizedMinChunkZ = Math.min(minChunkZ, maxChunkZ);
            int normalizedMaxChunkZ = Math.max(minChunkZ, maxChunkZ);
            minChunkX = normalizedMinChunkX;
            maxChunkX = normalizedMaxChunkX;
            minChunkZ = normalizedMinChunkZ;
            maxChunkZ = normalizedMaxChunkZ;
        }

        public int widthInChunks() {
            return maxChunkX - minChunkX + 1;
        }

        public int depthInChunks() {
            return maxChunkZ - minChunkZ + 1;
        }
    }

    /**
     * 运行时区域参数（仅计划数据，不做执行）。
     */
    public record ZoneParameters(
        int safezoneInsetChunks,
        int warningBufferBlocks,
        int lethalBufferBlocks,
        int reservedChaosChunks
    ) {
    }

    /**
     * 单个 cell 规划项。
     * <p>
     * generationOrder 与 ringOrder 共同定义中心 kernel -> 外环的稳定执行次序。
     * </p>
     */
    public record PlannedCell(
        int generationOrder,
        int ringOrder,
        int rowIndex,
        int columnIndex,
        AnchorPoint anchor,
        ChunkCoord chunk,
        List<BiomeInferenceService.BiomeKey> biomeCandidates
    ) {

        public PlannedCell {
            anchor = Objects.requireNonNull(anchor, "anchor");
            chunk = Objects.requireNonNull(chunk, "chunk");
            biomeCandidates = List.copyOf(Objects.requireNonNull(biomeCandidates, "biomeCandidates"));
            if (generationOrder <= 0) {
                throw new IllegalArgumentException("generationOrder 必须从 1 开始");
            }
            if (ringOrder < 0) {
                throw new IllegalArgumentException("ringOrder 不能为负数");
            }
            if (rowIndex < 0 || columnIndex < 0) {
                throw new IllegalArgumentException("rowIndex/columnIndex 不能为负数");
            }
            if (biomeCandidates.isEmpty()) {
                throw new IllegalArgumentException("biomeCandidates 不能为空");
            }
        }
    }
}
