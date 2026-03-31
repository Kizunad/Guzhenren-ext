package com.Kizunad.guzhenrenext.faction.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class FactionRelationGraphContractTest {

    private static final Path GRAPH_SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/faction/client/FactionRelationGraph.java"
    );
    private static final Path INFO_SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/faction/client/FactionInfoScreen.java"
    );
    private static final Path NETWORKING_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/network/GuzhenrenExtNetworking.java"
    );
    private static final Path REQUEST_PAYLOAD_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/network/ServerboundFactionRelationGraphRequestPayload.java"
    );
    private static final Path SYNC_PAYLOAD_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/network/ClientboundFactionRelationGraphSyncPayload.java"
    );

    private static final List<String> GRAPH_SCREEN_MARKERS = List.of(
        "class FactionRelationGraph extends TinyUIScreen",
        "PacketDistributor.sendToServer(new ServerboundFactionRelationGraphRequestPayload())",
        "FactionRelationGraphClientState.currentSnapshot()",
        "mouseDragged(",
        "mouseScrolled("
    );

    private static final List<String> OPEN_PATH_MARKERS = List.of(
        "new FactionRelationGraph()",
        "minecraft.setScreen(new FactionRelationGraph())",
        "relationGraphButton.setOnClick"
    );

    private static final List<String> NETWORK_MARKERS = List.of(
        "ServerboundFactionRelationGraphRequestPayload.TYPE",
        "ServerboundFactionRelationGraphRequestPayload.STREAM_CODEC",
        "ServerboundFactionRelationGraphRequestPayload::handle",
        "ClientboundFactionRelationGraphSyncPayload.TYPE",
        "ClientboundFactionRelationGraphSyncPayload.STREAM_CODEC",
        "ClientboundFactionRelationGraphSyncPayload::handle"
    );

    private static final List<String> REQUEST_PAYLOAD_MARKERS = List.of(
        "ClientboundFactionRelationGraphSyncPayload.emptySnapshot()",
        "PacketDistributor.sendToPlayer",
        "FactionService.getAllFactions(level)",
        "Comparator.comparingLong(FactionCore::createdAt)",
        "relationMatrix.getRelationLevel(sourceFaction.id(), targetFaction.id()).name()"
    );

    private static final List<String> SYNC_PAYLOAD_MARKERS = List.of(
        "FactionRelationGraphClientState.applySnapshot",
        "payload == null ? null : payload.toSnapshot()",
        "public FactionRelationGraphSnapshot toSnapshot()",
        "public static ClientboundFactionRelationGraphSyncPayload emptySnapshot()"
    );

    @Test
    void graphScreenSourceContainsInteractiveRenderAndRequestMarkers() throws Exception {
        final String graphScreenSource = Files.readString(GRAPH_SCREEN_SOURCE);

        assertTrue(GRAPH_SCREEN_MARKERS.stream().allMatch(graphScreenSource::contains));
    }

    @Test
    void factionInfoScreenContainsOpenPathToRelationGraph() throws Exception {
        final String infoScreenSource = Files.readString(INFO_SCREEN_SOURCE);

        assertTrue(OPEN_PATH_MARKERS.stream().allMatch(infoScreenSource::contains));
    }

    @Test
    void networkingSourceRegistersRelationGraphRequestAndSyncPayloads() throws Exception {
        final String networkingSource = Files.readString(NETWORKING_SOURCE);

        assertTrue(NETWORK_MARKERS.stream().allMatch(networkingSource::contains));
    }

    @Test
    void requestAndSyncPayloadSourcesContainExpectedBridgeMarkers() throws Exception {
        final String requestPayloadSource = Files.readString(REQUEST_PAYLOAD_SOURCE);
        final String syncPayloadSource = Files.readString(SYNC_PAYLOAD_SOURCE);

        assertTrue(REQUEST_PAYLOAD_MARKERS.stream().allMatch(requestPayloadSource::contains));
        assertTrue(SYNC_PAYLOAD_MARKERS.stream().allMatch(syncPayloadSource::contains));
    }
}
