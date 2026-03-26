package com.Kizunad.guzhenrenext.xianqiao.runtime;

public final class ApertureRuntimeBoundaryService {

    public static final int DEFAULT_SAFEZONE_INSET_CHUNKS = 0;

    public static final int DEFAULT_WARNING_BUFFER_BLOCKS = 8;

    public static final int DEFAULT_LETHAL_BUFFER_BLOCKS = 16;

    public static final int DEFAULT_RESERVED_CHAOS_CHUNKS = 16;

    public static final ChaosZoneModel.ZoneConfig DEFAULT_ZONE_CONFIG = new ChaosZoneModel.ZoneConfig(
        DEFAULT_SAFEZONE_INSET_CHUNKS,
        DEFAULT_WARNING_BUFFER_BLOCKS,
        DEFAULT_LETHAL_BUFFER_BLOCKS,
        DEFAULT_RESERVED_CHAOS_CHUNKS
    );

    private ApertureRuntimeBoundaryService() {
    }

    public static ChaosZoneModel resolveModel(int minChunkX, int maxChunkX, int minChunkZ, int maxChunkZ) {
        return resolveModel(minChunkX, maxChunkX, minChunkZ, maxChunkZ, DEFAULT_ZONE_CONFIG);
    }

    public static ChaosZoneModel resolveModel(
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        ChaosZoneModel.ZoneConfig zoneConfig
    ) {
        ChaosZoneModel.ZoneConfig config = zoneConfig == null ? DEFAULT_ZONE_CONFIG : zoneConfig;
        return new ChaosZoneModel(
            minChunkX,
            maxChunkX,
            minChunkZ,
            maxChunkZ,
            config
        );
    }

}
