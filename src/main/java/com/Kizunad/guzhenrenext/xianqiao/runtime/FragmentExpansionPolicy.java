package com.Kizunad.guzhenrenext.xianqiao.runtime;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;

public final class FragmentExpansionPolicy {

    public static final int V1_SYMMETRIC_RECTANGULAR_CHUNK_DELTA = 1;

    private FragmentExpansionPolicy() {
    }

    public static ApertureInfo expandSymmetrically(ApertureInfo info) {
        return expandSymmetrically(info, V1_SYMMETRIC_RECTANGULAR_CHUNK_DELTA);
    }

    public static ApertureInfo expandSymmetrically(ApertureInfo info, int chunkDelta) {
        Objects.requireNonNull(info, "info");
        ExpandedChunkBounds expandedBounds = expandSymmetricBounds(
            info.minChunkX(),
            info.maxChunkX(),
            info.minChunkZ(),
            info.maxChunkZ(),
            chunkDelta
        );
        ExpandedChunkBounds currentBounds = ExpandedChunkBounds.of(
            info.minChunkX(),
            info.maxChunkX(),
            info.minChunkZ(),
            info.maxChunkZ()
        );
        if (expandedBounds.equals(currentBounds)) {
            return info;
        }
        return new ApertureInfo(
            info.center(),
            expandedBounds.minChunkX(),
            expandedBounds.maxChunkX(),
            expandedBounds.minChunkZ(),
            expandedBounds.maxChunkZ(),
            info.timeSpeed(),
            info.nextTribulationTick(),
            info.isFrozen(),
            info.favorability(),
            info.tier()
        );
    }

    public static void applySymmetricExpansion(ApertureWorldData worldData, UUID owner) {
        applySymmetricExpansion(worldData, owner, V1_SYMMETRIC_RECTANGULAR_CHUNK_DELTA);
    }

    public static void applySymmetricExpansion(ApertureWorldData worldData, UUID owner, int chunkDelta) {
        Objects.requireNonNull(worldData, "worldData");
        Objects.requireNonNull(owner, "owner");
        ApertureInfo currentInfo = worldData.getAperture(owner);
        if (currentInfo == null) {
            return;
        }

        ApertureInfo expandedInfo = expandSymmetrically(currentInfo, chunkDelta);
        int appliedChunkDelta = expandedInfo.maxChunkX() - currentInfo.maxChunkX();
        if (appliedChunkDelta <= 0) {
            return;
        }
        worldData.expandBoundaryByChunkDelta(owner, appliedChunkDelta);
    }

    public static BlockPos resolvePlacementTarget(ApertureInfo info, Direction direction) {
        Objects.requireNonNull(info, "info");
        Direction horizontalDirection = normalizeHorizontalDirection(direction);
        PlacementTarget target = resolvePlacementTarget(
            PlacementContext.of(info),
            horizontalDirection.getStepX(),
            horizontalDirection.getStepZ()
        );
        return new BlockPos(target.blockX(), info.center().getY(), target.blockZ());
    }

    public static ExpandedChunkBounds expandSymmetricBounds(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        int chunkDelta
    ) {
        int normalizedChunkDelta = Math.max(0, chunkDelta);
        return ExpandedChunkBounds.of(
            minChunkX - normalizedChunkDelta,
            maxChunkX + normalizedChunkDelta,
            minChunkZ - normalizedChunkDelta,
            maxChunkZ + normalizedChunkDelta
        );
    }

    public static PlacementTarget resolvePlacementTarget(
        PlacementContext placementContext,
        int stepX,
        int stepZ
    ) {
        Objects.requireNonNull(placementContext, "placementContext");
        ExpandedChunkBounds expandedBounds = expandSymmetricBounds(
            placementContext.minChunkX(),
            placementContext.maxChunkX(),
            placementContext.minChunkZ(),
            placementContext.maxChunkZ(),
            V1_SYMMETRIC_RECTANGULAR_CHUNK_DELTA
        );
        if (stepX > 0) {
            return new PlacementTarget(
                SectionPos.sectionToBlockCoord(expandedBounds.maxChunkX()),
                placementContext.centerBlockZ()
            );
        }
        if (stepX < 0) {
            return new PlacementTarget(
                SectionPos.sectionToBlockCoord(expandedBounds.minChunkX()),
                placementContext.centerBlockZ()
            );
        }
        if (stepZ > 0) {
            return new PlacementTarget(
                placementContext.centerBlockX(),
                SectionPos.sectionToBlockCoord(expandedBounds.maxChunkZ())
            );
        }
        return new PlacementTarget(
            placementContext.centerBlockX(),
            SectionPos.sectionToBlockCoord(expandedBounds.minChunkZ())
        );
    }

    private static Direction normalizeHorizontalDirection(Direction direction) {
        if (direction == null) {
            return Direction.NORTH;
        }
        return switch (direction) {
            case EAST, WEST, SOUTH, NORTH -> direction;
            default -> Direction.NORTH;
        };
    }

    public record ExpandedChunkBounds(int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {

        public static ExpandedChunkBounds of(int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
            return new ExpandedChunkBounds(
                Math.min(minChunkX, maxChunkX),
                Math.max(minChunkX, maxChunkX),
                Math.min(minChunkZ, maxChunkZ),
                Math.max(minChunkZ, maxChunkZ)
            );
        }
    }

    public record PlacementContext(
        int centerBlockX,
        int centerBlockZ,
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ
    ) {

        public static PlacementContext of(ApertureInfo info) {
            Objects.requireNonNull(info, "info");
            return new PlacementContext(
                info.center().getX(),
                info.center().getZ(),
                info.minChunkX(),
                info.maxChunkX(),
                info.minChunkZ(),
                info.maxChunkZ()
            );
        }
    }

    public record PlacementTarget(int blockX, int blockZ) {
    }
}
