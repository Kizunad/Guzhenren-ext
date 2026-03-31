package com.Kizunad.guzhenrenext.faction.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class FactionInfoScreenContractTest {

    private static final int OPEN_PATH_KEY_BINDINGS_INDEX = 0;
    private static final int OPEN_PATH_EVENT_REGISTER_INDEX = 1;
    private static final int OPEN_PATH_CLICK_INDEX = 2;
    private static final int OPEN_PATH_SEND_INDEX = 3;

    private static final int NETWORK_TYPE_INDEX = 0;
    private static final int NETWORK_HANDLE_INDEX = 1;
    private static final int NETWORK_SYNC_TYPE_INDEX = 2;
    private static final int NETWORK_SYNC_HANDLE_INDEX = 3;
    private static final int NETWORK_REQUEST_INDEX = 4;
    private static final int NETWORK_CLIENT_HANDLER_INDEX = 5;

    private static final Path SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/faction/client/FactionInfoScreen.java"
    );
    private static final Path KEY_BINDINGS_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/client/GuKeyBindings.java"
    );
    private static final Path CLIENT_EVENTS_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/client/GuClientEvents.java"
    );
    private static final Path NETWORK_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/network/GuzhenrenExtNetworking.java"
    );
    private static final Path REQUEST_PAYLOAD_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/network/ServerboundFactionInfoRequestPayload.java"
    );
    private static final Path SYNC_PAYLOAD_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/network/ClientboundFactionInfoSyncPayload.java"
    );
    private static final Path CLIENT_HANDLER_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/faction/client/FactionInfoClientHandler.java"
    );

    private static final List<String> SCREEN_MARKERS = List.of(
        "class FactionInfoScreen extends TinyUIScreen",
        "new SolidPanel(theme)",
        "FactionInfoClientState.currentSnapshot()",
        "screen.guzhenrenext.faction_info.line.name",
        "screen.guzhenrenext.faction_info.line.type",
        "screen.guzhenrenext.faction_info.line.member_count",
        "screen.guzhenrenext.faction_info.line.power",
        "screen.guzhenrenext.faction_info.line.resources",
        "screen.guzhenrenext.faction_info.line.player_relation"
    );

    private static final List<String> OPEN_PATH_MARKERS = List.of(
        "OPEN_FACTION_INFO = new KeyMapping",
        "event.register(OPEN_FACTION_INFO);",
        "GuKeyBindings.OPEN_FACTION_INFO.consumeClick()",
        "PacketDistributor.sendToServer(new ServerboundFactionInfoRequestPayload())"
    );

    private static final List<String> NETWORK_MARKERS = List.of(
        "ServerboundFactionInfoRequestPayload.TYPE",
        "ServerboundFactionInfoRequestPayload::handle",
        "ClientboundFactionInfoSyncPayload.TYPE",
        "ClientboundFactionInfoSyncPayload::handle",
        "PacketDistributor.sendToPlayer",
        "minecraft.setScreen(new FactionInfoScreen())"
    );

    private static final List<String> FALLBACK_MARKERS = List.of(
        "resolveDisplayFaction(",
        "FactionService.getAllFactions(level)",
        "Comparator.comparingLong(FactionCore::createdAt)",
        "ClientboundFactionInfoSyncPayload.emptySnapshot()"
    );

    @Test
    void screenSourceContainsRequiredFactionFieldsWithoutGraphBehavior() throws Exception {
        final String source = Files.readString(SCREEN_SOURCE);

        assertTrue(SCREEN_MARKERS.stream().allMatch(source::contains));
        assertFalse(source.contains("ServerboundFactionInfoRequestPayload"));
        assertFalse(source.contains("PacketDistributor.sendToServer("));
        assertFalse(source.contains("relation graph"));
        assertFalse(source.contains("drag"));
        assertFalse(source.contains("zoom"));
    }

    @Test
    void clientOpenPathFollowsExistingKeybindingAndScreenPattern() throws Exception {
        final String keyBindingsSource = Files.readString(KEY_BINDINGS_SOURCE);
        final String clientEventsSource = Files.readString(CLIENT_EVENTS_SOURCE);

        assertTrue(keyBindingsSource.contains(OPEN_PATH_MARKERS.get(OPEN_PATH_KEY_BINDINGS_INDEX)));
        assertTrue(keyBindingsSource.contains(OPEN_PATH_MARKERS.get(OPEN_PATH_EVENT_REGISTER_INDEX)));
        assertTrue(clientEventsSource.contains(OPEN_PATH_MARKERS.get(OPEN_PATH_CLICK_INDEX)));
        assertTrue(clientEventsSource.contains(OPEN_PATH_MARKERS.get(OPEN_PATH_SEND_INDEX)));
    }

    @Test
    void networkingSourceRegistersMinimalRequestAndSyncBridge() throws Exception {
        final String networkingSource = Files.readString(NETWORK_SOURCE);
        final String requestPayloadSource = Files.readString(REQUEST_PAYLOAD_SOURCE);
        final String syncPayloadSource = Files.readString(SYNC_PAYLOAD_SOURCE);
        final String clientHandlerSource = Files.readString(CLIENT_HANDLER_SOURCE);

        assertTrue(networkingSource.contains(NETWORK_MARKERS.get(NETWORK_TYPE_INDEX)));
        assertTrue(networkingSource.contains(NETWORK_MARKERS.get(NETWORK_HANDLE_INDEX)));
        assertTrue(networkingSource.contains(NETWORK_MARKERS.get(NETWORK_SYNC_TYPE_INDEX)));
        assertTrue(networkingSource.contains(NETWORK_MARKERS.get(NETWORK_SYNC_HANDLE_INDEX)));
        assertTrue(requestPayloadSource.contains(NETWORK_MARKERS.get(NETWORK_REQUEST_INDEX)));
        assertTrue(clientHandlerSource.contains(NETWORK_MARKERS.get(NETWORK_CLIENT_HANDLER_INDEX)));
        assertTrue(syncPayloadSource.contains("FactionInfoClientHandler.applySync"));
    }

    @Test
    void serverPayloadFallsBackToFirstAvailableFactionWhenPlayerHasNoMembership() throws Exception {
        final String requestPayloadSource = Files.readString(REQUEST_PAYLOAD_SOURCE);

        assertTrue(FALLBACK_MARKERS.stream().allMatch(requestPayloadSource::contains));
    }
}
