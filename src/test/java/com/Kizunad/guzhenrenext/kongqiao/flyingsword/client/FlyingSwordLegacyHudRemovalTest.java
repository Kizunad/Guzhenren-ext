package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordLegacyHudRemovalTest {

    private static final Path LEGACY_OVERLAY_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/kongqiao/flyingsword/client/"
            + legacyOverlaySimpleName()
            + ".java"
    );
    private static final Path LEGACY_LAYOUT_TEST_SOURCE = Path.of(
        "src/test/java/com/Kizunad/guzhenrenext/kongqiao/flyingsword/client/"
            + legacyOverlaySimpleName()
            + "LayoutTest.java"
    );
    private static final Path HELP_SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/kongqiao/flyingsword/client/FlyingSwordHelpScreen.java"
    );
    private static final Path I18N_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/kongqiao/KongqiaoI18n.java"
    );
    private static final Path KEY_BINDINGS_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/client/GuKeyBindings.java"
    );
    private static final Path ZH_LANG_SOURCE = Path.of(
        "src/main/resources/assets/guzhenrenext/lang/zh_cn.json"
    );
    private static final Path EN_LANG_SOURCE = Path.of(
        "src/main/resources/assets/guzhenrenext/lang/en_us.json"
    );
    private static final String OVERVIEW_KEY_HUB =
        "screen.guzhenrenext.forge.help.overview.key_hub";
    private static final String TOGGLE_HUB =
        "key.guzhenrenext.flyingsword.toggle_hub";

    @Test
    void legacyOverlaySourceAndCharacterizationTestAreGone() {
        assertFalse(Files.exists(LEGACY_OVERLAY_SOURCE));
        assertFalse(Files.exists(LEGACY_LAYOUT_TEST_SOURCE));
    }

    @Test
    void helpAndKeySourcesOnlyKeepHubNaming() throws Exception {
        final String helpScreenSource = Files.readString(
            HELP_SCREEN_SOURCE,
            StandardCharsets.UTF_8
        );
        final String i18nSource = Files.readString(
            I18N_SOURCE,
            StandardCharsets.UTF_8
        );
        final String keyBindingsSource = Files.readString(
            KEY_BINDINGS_SOURCE,
            StandardCharsets.UTF_8
        );
        final String zhLangSource = Files.readString(
            ZH_LANG_SOURCE,
            StandardCharsets.UTF_8
        );
        final String enLangSource = Files.readString(
            EN_LANG_SOURCE,
            StandardCharsets.UTF_8
        );

        assertTrue(i18nSource.contains("HELP_OVERVIEW_KEY_HUB"));
        assertTrue(i18nSource.contains(OVERVIEW_KEY_HUB));
        assertTrue(helpScreenSource.contains("KongqiaoI18n.HELP_OVERVIEW_KEY_HUB"));
        assertTrue(keyBindingsSource.contains("FLYING_SWORD_TOGGLE_HUB"));
        assertTrue(keyBindingsSource.contains(TOGGLE_HUB));
        assertTrue(zhLangSource.contains(OVERVIEW_KEY_HUB));
        assertTrue(enLangSource.contains(OVERVIEW_KEY_HUB));
        assertTrue(zhLangSource.contains(TOGGLE_HUB));
        assertTrue(enLangSource.contains(TOGGLE_HUB));

        assertFalse(i18nSource.contains(legacyOverviewKeyConstant()));
        assertFalse(i18nSource.contains(legacyOverviewKeyPath()));
        assertFalse(helpScreenSource.contains(legacyOverviewKeyConstant()));
        assertFalse(zhLangSource.contains(legacyOverviewKeyPath()));
        assertFalse(enLangSource.contains(legacyOverviewKeyPath()));
        assertFalse(keyBindingsSource.contains(legacyToggleKeyConstant()));
        assertFalse(zhLangSource.contains(legacyToggleKeyPath()));
        assertFalse(enLangSource.contains(legacyToggleKeyPath()));
    }

    private static String legacyOverlaySimpleName() {
        return "FlyingSword" + "HudOverlay";
    }

    private static String legacyOverviewKeyConstant() {
        return "HELP_OVERVIEW_KEY_" + "HUD";
    }

    private static String legacyOverviewKeyPath() {
        return "screen.guzhenrenext.forge.help.overview.key_" + "hud";
    }

    private static String legacyToggleKeyConstant() {
        return "FLYING_SWORD_TOGGLE_" + "HUD";
    }

    private static String legacyToggleKeyPath() {
        return "key.guzhenrenext.flyingsword.toggle_" + "hud";
    }
}
