package com.Kizunad.guzhenrenext.xianqiao.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class ApertureWorldDataSchema {

    static final int LEGACY_SCHEMA_VERSION = 1;

    static final int CURRENT_SCHEMA_VERSION = 2;

    private static final int SCORE_MIN = 0;

    private static final int SCORE_MAX = 100;

    private ApertureWorldDataSchema() {
    }

    static ApertureInitPhase parseInitPhase(String serializedName) {
        for (ApertureInitPhase value : ApertureInitPhase.values()) {
            if (value.name().equals(serializedName)) {
                return value;
            }
        }
        return ApertureInitPhase.UNINITIALIZED;
    }

    static ApertureInitializationState normalizeInitializationState(ApertureInitializationState state) {
        if (state == null) {
            return ApertureInitializationState.uninitialized();
        }
        ApertureInitPhase normalizedPhase = Objects.requireNonNullElse(
            state.initPhase(),
            ApertureInitPhase.UNINITIALIZED
        );
        ApertureOpeningSnapshot normalizedSnapshot = state.openingSnapshot();
        Integer normalizedLayoutVersion = state.layoutVersion();
        Long normalizedPlanSeed = state.planSeed();
        if (normalizedLayoutVersion != null && normalizedLayoutVersion.intValue() <= 0) {
            normalizedLayoutVersion = null;
        }
        if (normalizedPhase == ApertureInitPhase.UNINITIALIZED) {
            normalizedSnapshot = null;
            normalizedLayoutVersion = null;
            normalizedPlanSeed = null;
        }
        return new ApertureInitializationState(
            normalizedPhase,
            normalizedSnapshot,
            normalizedLayoutVersion,
            normalizedPlanSeed
        );
    }

    static ApertureInitializationState legacyInitializationState(boolean initialized) {
        if (initialized) {
            return new ApertureInitializationState(ApertureInitPhase.COMPLETED, null, null, null);
        }
        return ApertureInitializationState.uninitialized();
    }

    static ApertureSerializedWorldState normalizeWorldState(ApertureSerializedWorldState state) {
        if (state == null) {
            return new ApertureSerializedWorldState(
                CURRENT_SCHEMA_VERSION,
                1,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptySet()
            );
        }
        Set<UUID> knownOwners = new HashSet<>();
        knownOwners.addAll(state.apertures().keySet());
        knownOwners.addAll(state.initializationStates().keySet());
        knownOwners.addAll(state.initializedApertures());
        Map<UUID, ApertureInitializationState> normalizedInitializationStates = new HashMap<>();
        for (UUID owner : knownOwners) {
            ApertureInitializationState normalizedState = state.initializationStates().containsKey(owner)
                ? normalizeInitializationState(state.initializationStates().get(owner))
                : legacyInitializationState(state.initializedApertures().contains(owner));
            normalizedInitializationStates.put(owner, normalizedState);
        }
        Set<UUID> normalizedInitializedOwners = new HashSet<>();
        for (Map.Entry<UUID, ApertureInitializationState> entry : normalizedInitializationStates.entrySet()) {
            if (entry.getValue().isInitializedEquivalent()) {
                normalizedInitializedOwners.add(entry.getKey());
            }
        }
        return new ApertureSerializedWorldState(
            CURRENT_SCHEMA_VERSION,
            Math.max(1, state.nextIndex()),
            state.apertures(),
            normalizedInitializationStates,
            normalizedInitializedOwners
        );
    }

    static int clampScore(int score) {
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, score));
    }
}

record ApertureSerializedApertureInfo(
    int centerX,
    int centerY,
    int centerZ,
    int minChunkX,
    int maxChunkX,
    int minChunkZ,
    int maxChunkZ,
    float timeSpeed,
    long nextTribulationTick,
    boolean frozen,
    float favorability,
    int tier
) {
}

record ApertureSerializedWorldState(
    int schemaVersion,
    int nextIndex,
    Map<UUID, ApertureSerializedApertureInfo> apertures,
    Map<UUID, ApertureInitializationState> initializationStates,
    Set<UUID> initializedApertures
) {

    public ApertureSerializedWorldState {
        schemaVersion = Math.max(ApertureWorldDataSchema.LEGACY_SCHEMA_VERSION, schemaVersion);
        nextIndex = Math.max(1, nextIndex);
        apertures = Collections.unmodifiableMap(new HashMap<>(apertures));
        initializationStates = Collections.unmodifiableMap(new HashMap<>(initializationStates));
        initializedApertures = Collections.unmodifiableSet(new HashSet<>(initializedApertures));
    }
}
