package com.Kizunad.guzhenrenext.xianqiao.runtime;

public final class ChaosZoneModel {

    private static final int CHUNK_BLOCK_SIZE = 16;

    private static final int CHUNK_MAX_OFFSET = CHUNK_BLOCK_SIZE - 1;

    public static final int DEFAULT_SAFEZONE_INSET_CHUNKS = 0;

    public static final int DEFAULT_WARNING_BUFFER_BLOCKS = 8;

    public static final int DEFAULT_LETHAL_BUFFER_BLOCKS = 16;

    public static final int DEFAULT_RESERVED_CHAOS_CHUNKS = 16;

    private final int minChunkX;
    private final int maxChunkX;
    private final int minChunkZ;
    private final int maxChunkZ;

    private final int safezoneInsetChunks;
    private final int warningBufferBlocks;
    private final int lethalBufferBlocks;
    private final int reservedChaosChunks;

    private final int minPlayableChunkX;
    private final int maxPlayableChunkX;
    private final int minPlayableChunkZ;
    private final int maxPlayableChunkZ;

    private final int minTruthBlockX;
    private final int maxTruthBlockX;
    private final int minTruthBlockZ;
    private final int maxTruthBlockZ;

    private final int minPlayableBlockX;
    private final int maxPlayableBlockX;
    private final int minPlayableBlockZ;
    private final int maxPlayableBlockZ;

    private final int minReserveBlockX;
    private final int maxReserveBlockX;
    private final int minReserveBlockZ;
    private final int maxReserveBlockZ;

    private final long warningDistanceSquared;
    private final long lethalDistanceSquared;

    public ChaosZoneModel(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        ZoneConfig zoneConfig
    ) {
        this.minChunkX = Math.min(minChunkX, maxChunkX);
        this.maxChunkX = Math.max(minChunkX, maxChunkX);
        this.minChunkZ = Math.min(minChunkZ, maxChunkZ);
        this.maxChunkZ = Math.max(minChunkZ, maxChunkZ);

        ZoneConfig config = zoneConfig == null ? ZoneConfig.defaults() : zoneConfig;
        this.safezoneInsetChunks = normalizeInset(this.minChunkX, this.maxChunkX, this.minChunkZ, this.maxChunkZ,
            config.safezoneInsetChunks());
        this.warningBufferBlocks = Math.max(0, config.warningBufferBlocks());
        this.lethalBufferBlocks = Math.max(this.warningBufferBlocks, config.lethalBufferBlocks());
        this.reservedChaosChunks = Math.max(0, config.reservedChaosChunks());

        this.minPlayableChunkX = this.minChunkX + this.safezoneInsetChunks;
        this.maxPlayableChunkX = this.maxChunkX - this.safezoneInsetChunks;
        this.minPlayableChunkZ = this.minChunkZ + this.safezoneInsetChunks;
        this.maxPlayableChunkZ = this.maxChunkZ - this.safezoneInsetChunks;

        this.minTruthBlockX = chunkToMinBlock(this.minChunkX);
        this.maxTruthBlockX = chunkToMaxBlock(this.maxChunkX);
        this.minTruthBlockZ = chunkToMinBlock(this.minChunkZ);
        this.maxTruthBlockZ = chunkToMaxBlock(this.maxChunkZ);

        this.minPlayableBlockX = chunkToMinBlock(this.minPlayableChunkX);
        this.maxPlayableBlockX = chunkToMaxBlock(this.maxPlayableChunkX);
        this.minPlayableBlockZ = chunkToMinBlock(this.minPlayableChunkZ);
        this.maxPlayableBlockZ = chunkToMaxBlock(this.maxPlayableChunkZ);

        this.minReserveBlockX = chunkToMinBlock(this.minChunkX - this.reservedChaosChunks);
        this.maxReserveBlockX = chunkToMaxBlock(this.maxChunkX + this.reservedChaosChunks);
        this.minReserveBlockZ = chunkToMinBlock(this.minChunkZ - this.reservedChaosChunks);
        this.maxReserveBlockZ = chunkToMaxBlock(this.maxChunkZ + this.reservedChaosChunks);

        this.warningDistanceSquared = square(this.warningBufferBlocks);
        this.lethalDistanceSquared = square(this.lethalBufferBlocks);
    }

    public boolean isInsidePlayableZone(int blockX, int blockZ) {
        return isInsideClosedRange(blockX, minPlayableBlockX, maxPlayableBlockX)
            && isInsideClosedRange(blockZ, minPlayableBlockZ, maxPlayableBlockZ);
    }

    public boolean isInsideTruthBoundary(int blockX, int blockZ) {
        return isInsideClosedRange(blockX, minTruthBlockX, maxTruthBlockX)
            && isInsideClosedRange(blockZ, minTruthBlockZ, maxTruthBlockZ);
    }

    public boolean isInsideReserveZone(int blockX, int blockZ) {
        return isInsideClosedRange(blockX, minReserveBlockX, maxReserveBlockX)
            && isInsideClosedRange(blockZ, minReserveBlockZ, maxReserveBlockZ);
    }

    public boolean isInReserveChaosBand(int blockX, int blockZ) {
        return !isInsideTruthBoundary(blockX, blockZ) && isInsideReserveZone(blockX, blockZ);
    }

    public long getOutsideDistanceSquaredToPlayable(int blockX, int blockZ) {
        long deltaX = computeAxisOutsideDistance(blockX, minPlayableBlockX, maxPlayableBlockX);
        long deltaZ = computeAxisOutsideDistance(blockZ, minPlayableBlockZ, maxPlayableBlockZ);
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    public long getOutsideDistanceSquaredToReserve(int blockX, int blockZ) {
        long deltaX = computeAxisOutsideDistance(blockX, minReserveBlockX, maxReserveBlockX);
        long deltaZ = computeAxisOutsideDistance(blockZ, minReserveBlockZ, maxReserveBlockZ);
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    public boolean isInWarningBand(int blockX, int blockZ) {
        long outsideDistanceSquared = getOutsideDistanceSquaredToPlayable(blockX, blockZ);
        return outsideDistanceSquared > warningDistanceSquared
            && outsideDistanceSquared <= lethalDistanceSquared;
    }

    public boolean isInLethalChaosBand(int blockX, int blockZ) {
        return getOutsideDistanceSquaredToPlayable(blockX, blockZ) > lethalDistanceSquared;
    }

    public ChaosBand resolveChaosBand(int blockX, int blockZ) {
        long outsideDistanceSquared = getOutsideDistanceSquaredToPlayable(blockX, blockZ);
        if (outsideDistanceSquared <= warningDistanceSquared) {
            return ChaosBand.PLAYABLE_OR_SAFE;
        }
        if (outsideDistanceSquared <= lethalDistanceSquared) {
            return ChaosBand.WARNING;
        }
        return ChaosBand.LETHAL;
    }

    public int safezoneInsetChunks() {
        return safezoneInsetChunks;
    }

    public int warningBufferBlocks() {
        return warningBufferBlocks;
    }

    public int lethalBufferBlocks() {
        return lethalBufferBlocks;
    }

    public int reservedChaosChunks() {
        return reservedChaosChunks;
    }

    public enum ChaosBand {
        PLAYABLE_OR_SAFE,
        WARNING,
        LETHAL
    }

    public record ZoneConfig(
        int safezoneInsetChunks,
        int warningBufferBlocks,
        int lethalBufferBlocks,
        int reservedChaosChunks
    ) {

        public static ZoneConfig defaults() {
            return new ZoneConfig(
                DEFAULT_SAFEZONE_INSET_CHUNKS,
                DEFAULT_WARNING_BUFFER_BLOCKS,
                DEFAULT_LETHAL_BUFFER_BLOCKS,
                DEFAULT_RESERVED_CHAOS_CHUNKS
            );
        }
    }

    private static int chunkToMinBlock(int chunk) {
        return chunk * CHUNK_BLOCK_SIZE;
    }

    private static int chunkToMaxBlock(int chunk) {
        return chunkToMinBlock(chunk) + CHUNK_MAX_OFFSET;
    }

    private static int normalizeInset(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        int safezoneInsetChunks
    ) {
        int normalizedInset = Math.max(0, safezoneInsetChunks);
        int maxInsetX = Math.max(0, (maxChunkX - minChunkX) / 2);
        int maxInsetZ = Math.max(0, (maxChunkZ - minChunkZ) / 2);
        return Math.min(normalizedInset, Math.min(maxInsetX, maxInsetZ));
    }

    private static long computeAxisOutsideDistance(int value, int minInclusive, int maxInclusive) {
        if (value < minInclusive) {
            return (long) minInclusive - value;
        }
        if (value > maxInclusive) {
            return (long) value - maxInclusive;
        }
        return 0L;
    }

    private static boolean isInsideClosedRange(int value, int minInclusive, int maxInclusive) {
        return value >= minInclusive && value <= maxInclusive;
    }

    private static long square(int value) {
        long longValue = value;
        return longValue * longValue;
    }
}
