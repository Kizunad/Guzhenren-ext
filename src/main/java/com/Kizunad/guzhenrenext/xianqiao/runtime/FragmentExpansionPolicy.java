package com.Kizunad.guzhenrenext.xianqiao.runtime;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionOpeningArchitectureContract;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import java.util.UUID;

public final class FragmentExpansionPolicy {

    public static final int V1_SYMMETRIC_CHUNK_DELTA =
        AscensionOpeningArchitectureContract.FRAGMENT_V1_SYMMETRIC_CHUNK_DELTA;

    private static final String PREVIEW_SUMMARY_TEMPLATE = "本次边界四向各 +%d 区块";

    private static final String PREVIEW_TARGET_NOTE = "落点为扩张后当前朝向外缘";

    private static final String SUCCESS_SUMMARY_TEMPLATE = "仙窍边界向四周各扩张 %d 个区块。";

    private FragmentExpansionPolicy() {
    }

    public static int chunkDelta() {
        return V1_SYMMETRIC_CHUNK_DELTA;
    }

    public static String previewSummary() {
        return PREVIEW_SUMMARY_TEMPLATE.formatted(V1_SYMMETRIC_CHUNK_DELTA);
    }

    public static String previewTargetSemantics() {
        return PREVIEW_TARGET_NOTE;
    }

    public static String successSummary() {
        return SUCCESS_SUMMARY_TEMPLATE.formatted(V1_SYMMETRIC_CHUNK_DELTA);
    }

    public static ExpandedChunkBoundary resolveExpandedBoundary(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ
    ) {
        return new ExpandedChunkBoundary(
            minChunkX - V1_SYMMETRIC_CHUNK_DELTA,
            maxChunkX + V1_SYMMETRIC_CHUNK_DELTA,
            minChunkZ - V1_SYMMETRIC_CHUNK_DELTA,
            maxChunkZ + V1_SYMMETRIC_CHUNK_DELTA
        );
    }

    public static int resolvePlacementDistanceChunks(
        int centerChunkX,
        int centerChunkZ,
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        HorizontalDirection direction
    ) {
        ExpandedChunkBoundary expandedBoundary = resolveExpandedBoundary(minChunkX, maxChunkX, minChunkZ, maxChunkZ);
        return switch (direction) {
            case EAST -> Math.max(0, expandedBoundary.maxChunkX() - centerChunkX);
            case WEST -> Math.max(0, centerChunkX - expandedBoundary.minChunkX());
            case SOUTH -> Math.max(0, expandedBoundary.maxChunkZ() - centerChunkZ);
            case NORTH -> Math.max(0, centerChunkZ - expandedBoundary.minChunkZ());
        };
    }

    public static HorizontalDirection resolveHorizontalDirection(int stepX, int stepZ) {
        if (stepX > 0) {
            return HorizontalDirection.EAST;
        }
        if (stepX < 0) {
            return HorizontalDirection.WEST;
        }
        if (stepZ > 0) {
            return HorizontalDirection.SOUTH;
        }
        return HorizontalDirection.NORTH;
    }

    public static void applySymmetricExpansion(ApertureWorldData worldData, UUID owner) {
        worldData.expandBoundaryByChunkDelta(owner, V1_SYMMETRIC_CHUNK_DELTA);
    }

    public enum HorizontalDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    public record ExpandedChunkBoundary(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ
    ) {
    }
}
