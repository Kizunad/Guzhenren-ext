package com.Kizunad.customNPCs.config;

public class SpawnConfig {
    private static final SpawnConfig INSTANCE = new SpawnConfig();
    public static final int DEFAULT_MAX_NATURAL_SPAWNS = 5;
    
    private boolean naturalSpawnEnabled = true;
    private int maxNaturalSpawns = DEFAULT_MAX_NATURAL_SPAWNS;

    private SpawnConfig() {}

    public static SpawnConfig getInstance() {
        return INSTANCE;
    }

    public boolean isNaturalSpawnEnabled() {
        return naturalSpawnEnabled;
    }

    public SpawnConfig setNaturalSpawnEnabled(boolean enabled) {
        this.naturalSpawnEnabled = enabled;
        return this;
    }

    public int getMaxNaturalSpawns() {
        return maxNaturalSpawns;
    }

    public SpawnConfig setMaxNaturalSpawns(int max) {
        this.maxNaturalSpawns = max;
        return this;
    }
}
