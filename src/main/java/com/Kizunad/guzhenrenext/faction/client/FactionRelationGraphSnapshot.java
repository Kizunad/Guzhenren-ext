package com.Kizunad.guzhenrenext.faction.client;

import java.util.List;

public record FactionRelationGraphSnapshot(
    boolean synced,
    boolean hasData,
    List<NodeSnapshot> nodes,
    List<EdgeSnapshot> edges
) {
    public FactionRelationGraphSnapshot {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
    }

    public static FactionRelationGraphSnapshot pending() {
        return new FactionRelationGraphSnapshot(false, false, List.of(), List.of());
    }

    public record NodeSnapshot(
        String factionId,
        String factionName,
        String factionType,
        int power,
        int resources,
        int memberCount
    ) {
        public NodeSnapshot {
            factionId = normalize(factionId);
            factionName = normalize(factionName);
            factionType = normalize(factionType);
        }
    }

    public record EdgeSnapshot(
        String sourceFactionId,
        String targetFactionId,
        int relationValue,
        String relationLevel
    ) {
        public EdgeSnapshot {
            sourceFactionId = normalize(sourceFactionId);
            targetFactionId = normalize(targetFactionId);
            relationLevel = normalize(relationLevel);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
