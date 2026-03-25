package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHudActivationPathSwitchTest {

    private static final Path TACTICAL_OVERLAY_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/kongqiao/flyingsword/client/FlyingSwordTacticalHudOverlay.java"
    );
    private static final Path CLIENT_EVENTS_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/client/GuClientEvents.java"
    );

    @Test
    void tacticalOverlayOwnsEventBusRegistrationAndHubToggleStaysClientEntry()
        throws Exception {
        final String tacticalSource = Files.readString(
            TACTICAL_OVERLAY_SOURCE,
            StandardCharsets.UTF_8
        );
        final String clientEventsSource = Files.readString(
            CLIENT_EVENTS_SOURCE,
            StandardCharsets.UTF_8
        );

        assertTrue(tacticalSource.contains("@EventBusSubscriber("));
        assertTrue(tacticalSource.contains("public static void onClientTick(final ClientTickEvent.Post event)"));
        assertTrue(tacticalSource.contains("public static void onRenderGui(final RenderGuiEvent.Post event)"));

        assertTrue(clientEventsSource.contains("FLYING_SWORD_TOGGLE_HUB"));
        assertFalse(clientEventsSource.contains("toggleHud()"));
    }
}
