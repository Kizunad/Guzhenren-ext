package com.Kizunad.guzhenrenext.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BenmingClientInputWiringTests {

    @Test
    void benmingActionResolverUsesMinimalDeterministicModifierMapping() {
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
        assertTrue(source.contains("PacketDistributor.sendToServer(resolveBenmingActionPayload())"));
        assertTrue(source.contains("resolveBenmingActionPayload()"));
        assertTrue(source.contains("ServerboundBenmingSwordActionPayload"));
        assertTrue(source.contains("BenmingClientActionResolver.createPayload("));
    }
}
