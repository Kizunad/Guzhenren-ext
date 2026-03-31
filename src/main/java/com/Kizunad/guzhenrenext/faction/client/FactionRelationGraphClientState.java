package com.Kizunad.guzhenrenext.faction.client;

public final class FactionRelationGraphClientState {

    private static volatile FactionRelationGraphSnapshot currentSnapshot =
        FactionRelationGraphSnapshot.pending();

    private FactionRelationGraphClientState() {
    }

    public static void markSyncPending() {
        currentSnapshot = FactionRelationGraphSnapshot.pending();
    }

    public static void applySnapshot(final FactionRelationGraphSnapshot snapshot) {
        currentSnapshot = snapshot == null
            ? FactionRelationGraphSnapshot.pending()
            : snapshot;
    }

    public static FactionRelationGraphSnapshot currentSnapshot() {
        return currentSnapshot;
    }
}
