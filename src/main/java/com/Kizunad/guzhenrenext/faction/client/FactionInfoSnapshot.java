package com.Kizunad.guzhenrenext.faction.client;

public record FactionInfoSnapshot(
    boolean synced,
    boolean hasDisplayFaction,
    String factionName,
    String factionType,
    int memberCount,
    int power,
    int resources,
    int playerRelationValue
) {
    public FactionInfoSnapshot {
        factionName = factionName == null ? "" : factionName;
        factionType = factionType == null ? "" : factionType;
    }

    public static FactionInfoSnapshot pending() {
        return new FactionInfoSnapshot(false, false, "", "", 0, 0, 0, 0);
    }
}
