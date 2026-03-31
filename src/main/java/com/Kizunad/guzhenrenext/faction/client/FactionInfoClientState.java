package com.Kizunad.guzhenrenext.faction.client;

public final class FactionInfoClientState {

    private static volatile FactionInfoSnapshot currentSnapshot =
        FactionInfoSnapshot.pending();

    private FactionInfoClientState() {
    }

    public static void markSyncPending() {
        currentSnapshot = FactionInfoSnapshot.pending();
    }

    public static void applySnapshot(final FactionInfoSnapshot snapshot) {
        currentSnapshot = snapshot == null
            ? FactionInfoSnapshot.pending()
            : snapshot;
    }

    public static FactionInfoSnapshot currentSnapshot() {
        return currentSnapshot;
    }
}
