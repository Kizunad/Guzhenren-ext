package com.Kizunad.guzhenrenext.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BenmingClientInputWiringTests {

    @BeforeEach
    void resetThrottleState() {
        BenmingClientActionResolver.resetThrottleStateForTests();
    }

    @Test
    void benmingActionResolverUsesMinimalDeterministicModifierMapping() {
        assertEquals(
            BenmingClientActionResolver.BenmingActionRoute.RITUAL_BIND,
            BenmingClientActionResolver.resolveActionRoute(false, false)
        );
        assertEquals(
            BenmingClientActionResolver.BenmingActionRoute.SWITCH_RESONANCE,
            BenmingClientActionResolver.resolveActionRoute(true, false)
        );
        assertEquals(
            BenmingClientActionResolver.BenmingActionRoute.BURST_ATTEMPT,
            BenmingClientActionResolver.resolveActionRoute(false, true)
        );
        assertEquals(
            BenmingClientActionResolver.BenmingActionRoute.BURST_ATTEMPT,
            BenmingClientActionResolver.resolveActionRoute(true, true)
        );
        assertEquals("RITUAL_BIND", BenmingClientActionResolver.resolveActionName(false, false));
        assertEquals("SWITCH_RESONANCE", BenmingClientActionResolver.resolveActionName(true, false));
        assertEquals("BURST_ATTEMPT", BenmingClientActionResolver.resolveActionName(false, true));
        assertEquals("BURST_ATTEMPT", BenmingClientActionResolver.resolveActionName(true, true));
    }

    @Test
    void langFilesContainBenmingClientInputLabel() throws Exception {
        final String zh = Files.readString(
            Path.of("src/main/resources/assets/guzhenrenext/lang/zh_cn.json")
        );
        final String en = Files.readString(
            Path.of("src/main/resources/assets/guzhenrenext/lang/en_us.json")
        );

        assertTrue(zh.contains("\"key.guzhenrenext.flyingsword.benming_action\""));
        assertTrue(en.contains("\"key.guzhenrenext.flyingsword.benming_action\""));
    }

    @Test
    void clientEventsSendDedicatedBenmingPayload() throws Exception {
        final String source = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/client/GuClientEvents.java")
        );

        assertTrue(source.contains("GuKeyBindings.FLYING_SWORD_BENMING_ACTION.consumeClick()"));
        assertTrue(source.contains("BenmingClientActionResolver.resolveActionRoute("));
        assertTrue(source.contains("BenmingClientActionResolver.shouldSendAction(actionRoute, currentTick)"));
        assertTrue(source.contains("PacketDistributor.sendToServer("));
        assertTrue(source.contains("BenmingClientActionResolver.createPayload(actionRoute)"));
        assertTrue(source.contains("ServerboundBenmingSwordActionPayload"));
        assertTrue(source.contains("BenmingClientActionResolver.createPayload(\n            BenmingClientActionResolver.resolveActionRoute("));
        assertTrue(source.contains("BenmingActionRoute.RITUAL_BIND"));
        assertTrue(source.contains("BenmingActionRoute.SWITCH_RESONANCE"));
        assertTrue(source.contains("BenmingActionRoute.BURST_ATTEMPT"));
    }

    @Test
    void benmingClientUsesDedicatedTypedPayloadCreationPath() throws Exception {
        final String source = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/client/GuClientEvents.java")
        );

        assertTrue(source.contains("static ServerboundBenmingSwordActionPayload createPayload("));
        assertTrue(source.contains("final BenmingActionRoute actionRoute"));
        assertTrue(source.contains("switch (actionRoute)"));
        assertTrue(source.contains("createRitualBindPayload()"));
        assertTrue(source.contains("createSwitchResonancePayload()"));
        assertTrue(source.contains("createBurstAttemptPayload()"));
        assertTrue(source.contains("createPayloadForActionName(BenmingActionRoute.RITUAL_BIND.name())"));
        assertTrue(source.contains("createPayloadForActionName(BenmingActionRoute.SWITCH_RESONANCE.name())"));
        assertTrue(source.contains("createPayloadForActionName(BenmingActionRoute.BURST_ATTEMPT.name())"));
    }

    @Test
    void benmingClientNoFallbackRouteNeededForActiveActions() throws Exception {
        final String source = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/client/GuClientEvents.java")
        );

        assertTrue(source.contains("resolveActionRoute("));
        assertTrue(source.contains("return BenmingActionRoute.BURST_ATTEMPT;"));
        assertTrue(source.contains("return BenmingActionRoute.SWITCH_RESONANCE;"));
        assertTrue(source.contains("return BenmingActionRoute.RITUAL_BIND;"));
    }

    @Test
    void benmingClientThrottleAllowsFirstHitThenBlocksImmediateRepeat() {
        assertTrue(
            BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.RITUAL_BIND,
                100L
            )
        );
        assertTrue(
            !BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.RITUAL_BIND,
                101L
            )
        );
    }

    @Test
    void benmingClientThrottleKeepsOtherActionsIndependent() {
        assertTrue(
            BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.RITUAL_BIND,
                200L
            )
        );
        assertTrue(
            BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.SWITCH_RESONANCE,
                201L
            )
        );
    }

    @Test
    void benmingClientThrottleAllowsSameActionAfterWindowExpires() {
        final long window = BenmingClientActionResolver.BENMING_ACTION_THROTTLE_WINDOW_TICKS;

        assertTrue(
            BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.BURST_ATTEMPT,
                300L
            )
        );
        assertTrue(
            !BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.BURST_ATTEMPT,
                300L + window - 1L
            )
        );
        assertTrue(
            BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.BURST_ATTEMPT,
                300L + window
            )
        );
    }

    @Test
    void benmingClientThrottleAllowsActionWhenTickRewindsToNewWorld() {
        assertTrue(
            BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.RITUAL_BIND,
                500L
            )
        );
        assertTrue(
            BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.RITUAL_BIND,
                20L
            )
        );
        assertTrue(
            !BenmingClientActionResolver.shouldSendAction(
                BenmingClientActionResolver.BenmingActionRoute.RITUAL_BIND,
                21L
            )
        );
    }

}
