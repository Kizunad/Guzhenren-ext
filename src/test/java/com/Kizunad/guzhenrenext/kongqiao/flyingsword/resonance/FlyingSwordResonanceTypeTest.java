package com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class FlyingSwordResonanceTypeTest {


    private static final int TEST_MAGIC_HEX_FFE36A2E = 0xFFE36A2E;
    private static final int TEST_MAGIC_HEX_FF8A4FD6 = 0xFF8A4FD6;

    @Test
    void resolvesByEnglishAndChineseAlias() {
        assertEquals(
            FlyingSwordResonanceType.OFFENSE,
            FlyingSwordResonanceType.resolve("offense").orElseThrow()
        );
        assertEquals(
            FlyingSwordResonanceType.DEFENSE,
            FlyingSwordResonanceType.resolve("DEFENSE").orElseThrow()
        );
        assertEquals(
            FlyingSwordResonanceType.SPIRIT,
            FlyingSwordResonanceType.resolve("巧").orElseThrow()
        );
        assertEquals(
            FlyingSwordResonanceType.DEVOUR,
            FlyingSwordResonanceType.resolve("devour").orElseThrow()
        );
        assertEquals(
            FlyingSwordResonanceType.DEVOUR,
            FlyingSwordResonanceType.resolve("噬").orElseThrow()
        );
    }

    @Test
    void rejectsUnknownType() {
        assertTrue(FlyingSwordResonanceType.resolve("unknown").isEmpty());
    }

    @Test
    void exposesStableKeysAndColor() {
        final FlyingSwordResonanceType type = FlyingSwordResonanceType.OFFENSE;
        assertEquals("screen.guzhenrenext.forge.help.resonance.offense.name", type.getDisplayNameKey());
        assertEquals("screen.guzhenrenext.forge.help.resonance.offense.desc", type.getDescriptionKey());
        assertEquals(
            "screen.guzhenrenext.forge.help.resonance.offense.color_semantic",
            type.getColorSemanticKey()
        );
        assertEquals(TEST_MAGIC_HEX_FFE36A2E, type.getPrimaryColor());
    }

    @Test
    void exposesDevourKeysAndColor() {
        final FlyingSwordResonanceType type = FlyingSwordResonanceType.DEVOUR;
        assertEquals("screen.guzhenrenext.forge.help.resonance.devour.name", type.getDisplayNameKey());
        assertEquals("screen.guzhenrenext.forge.help.resonance.devour.desc", type.getDescriptionKey());
        assertEquals(
            "screen.guzhenrenext.forge.help.resonance.devour.color_semantic",
            type.getColorSemanticKey()
        );
        assertEquals(TEST_MAGIC_HEX_FF8A4FD6, type.getPrimaryColor());
    }

    @Test
    void resonanceKeysExistInBothLangFiles() {
        final String zh = readResourceText("/assets/guzhenrenext/lang/zh_cn.json");
        final String en = readResourceText("/assets/guzhenrenext/lang/en_us.json");
        for (final FlyingSwordResonanceType type : FlyingSwordResonanceType.values()) {
            assertTrue(zh.contains(quoted(type.getDisplayNameKey())));
            assertTrue(zh.contains(quoted(type.getDescriptionKey())));
            assertTrue(zh.contains(quoted(type.getColorSemanticKey())));
            assertTrue(en.contains(quoted(type.getDisplayNameKey())));
            assertTrue(en.contains(quoted(type.getDescriptionKey())));
            assertTrue(en.contains(quoted(type.getColorSemanticKey())));
        }
    }

    private static String readResourceText(String resourcePath) {
        try (InputStream stream = FlyingSwordResonanceTypeTest.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                fail("resource not found: " + resourcePath);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read resource: " + resourcePath, exception);
        }
    }

    private static String quoted(String key) {
        return "\"" + key + "\"";
    }
}
