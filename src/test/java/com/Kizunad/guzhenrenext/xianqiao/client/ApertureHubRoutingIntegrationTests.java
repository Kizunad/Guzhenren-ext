package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy.CardRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.RouteChip;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureHubRoutingIntegrationTests {

    private static final Path APERTURE_HUB_SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/client/ApertureHubScreen.java"
    );

    @Test
    void routeChipReflectsApprovedPolicyTargetsOnly() {
        for (CardRoutePolicy policy : HubRoutePolicy.orderedPolicies()) {
            final RouteChip routeChip = new RouteChip();
            routeChip.applyPolicy(policy);

            if (policy.launchesScreen()) {
                assertTrue(routeChip.isActionable());
                assertEquals("前往" + policy.target().displayName(), routeChip.getText());
                continue;
            }
            if (policy.staysOnHub()) {
                assertFalse(routeChip.isActionable());
                assertEquals("主殿总览", routeChip.getText());
                continue;
            }

            assertFalse(routeChip.isActionable());
            assertEquals("待前往" + policy.target().displayName(), routeChip.getText());
            assertTrue(policy.usesPlaceholder());
        }
    }

    @Test
    void sourceBindsCallbacksOnlyForApprovedDirectRoutes() throws Exception {
        final String currentSource = Files.readString(APERTURE_HUB_SCREEN_SOURCE);

        assertTrue(currentSource.contains("bindRouteCallbacks(card, policy);"));
        assertTrue(currentSource.contains("private void bindRouteCallbacks("));
        assertTrue(currentSource.contains("if (!policy.launchesScreen()) {"));
        assertTrue(currentSource.contains("card.setOnClick(null);"));
        assertTrue(currentSource.contains("card.getRouteChip().setOnClick(null);"));
        assertTrue(
            currentSource.contains(
                "final Runnable directRouteCallback = () -> openApprovedDirectRoute(policy);"
            )
        );
        assertTrue(currentSource.contains("card.setOnClick(directRouteCallback);"));
        assertTrue(currentSource.contains("card.getRouteChip().setOnClick(directRouteCallback);"));
        assertTrue(
            currentSource.contains(
                "private void openApprovedDirectRoute(final HubRoutePolicy.CardRoutePolicy policy)"
            )
        );
        assertTrue(currentSource.contains("final String routeFailureReason = switch (policy.target()) {"));
        assertTrue(currentSource.contains("case LAND_SPIRIT_SCREEN -> tryOpenLandSpiritRoute(player);"));
        assertTrue(
            currentSource.contains(
                "case RESOURCE_CONTROLLER_SCREEN -> tryOpenResourceControllerRoute(player);"
            )
        );
        assertTrue(currentSource.contains("case ALCHEMY_FURNACE_SCREEN -> tryOpenAlchemyFurnaceRoute(player);"));
        assertTrue(currentSource.contains("case STORAGE_GU_SCREEN -> tryOpenStorageGuRoute(player);"));
        assertTrue(currentSource.contains("case CLUSTER_NPC_SCREEN -> tryOpenClusterRoute(player);"));
        assertTrue(
            currentSource.contains(
                "announceRouteResolutionFailure(policy.target().displayName(), routeFailureReason);"
            )
        );
        assertTrue(currentSource.contains("private String tryOpenStorageGuRoute(final LocalPlayer player)"));
        assertTrue(currentSource.contains("findNearestLoadedEntity(player, LandSpiritEntity.class)"));
        assertTrue(currentSource.contains("ResourceControllerBlock.class,"));
        assertTrue(currentSource.contains("AlchemyFurnaceBlock.class,"));
        assertTrue(currentSource.contains("findNearestLoadedEntity(player, ClusterNpcEntity.class)"));
        assertTrue(currentSource.contains("currentGameMode().useItem(player, hand)"));
        assertTrue(currentSource.contains("currentGameMode().useItemOn(player, hand, blockHitResult)"));
        assertTrue(currentSource.contains("currentGameMode().interact(player, entity, hand)"));
        assertTrue(currentSource.contains("final int previousSelectedSlot = player.getInventory().selected;"));
        assertTrue(currentSource.contains("player.getInventory().selected = hotbarSlot;"));
        assertTrue(currentSource.contains("try {"));
        assertTrue(currentSource.contains("} finally {"));
        assertTrue(currentSource.contains("player.getInventory().selected = previousSelectedSlot;"));
        assertFalse(currentSource.contains("DIRECT_ROUTE_MESSAGE_PREFIX"));
        assertFalse(currentSource.contains("DIRECT_ROUTE_MESSAGE_SUFFIX"));
    }
}
