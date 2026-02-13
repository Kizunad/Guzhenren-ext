package com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster;

import com.Kizunad.guzhenrenext.network.ClientboundClusterStatePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClusterClientStateCache {

    private static volatile int currentLoad;
    private static volatile int maxComputation;
    private static volatile List<UUID> activeSwordUuids = List.of();

    private ClusterClientStateCache() {}

    public static void apply(ClientboundClusterStatePayload payload) {
        if (payload == null) {
            return;
        }
        currentLoad = Math.max(0, payload.currentLoad());
        maxComputation = Math.max(0, payload.maxComputation());
        activeSwordUuids = List.copyOf(payload.activeSwordUuids());
    }

    public static int getCurrentLoad() {
        return currentLoad;
    }

    public static int getMaxComputation() {
        return maxComputation;
    }

    public static List<UUID> getActiveSwordUuids() {
        return new ArrayList<>(activeSwordUuids);
    }

    public static boolean isActive(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return activeSwordUuids.contains(uuid);
    }

    public static void clear() {
        currentLoad = 0;
        maxComputation = 0;
        activeSwordUuids = List.of();
    }
}
