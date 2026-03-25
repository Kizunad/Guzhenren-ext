package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHelpScreenTest {


    private static final int TEST_MAGIC_5 = 5;
    private static final int TEST_MAGIC_6 = 6;

    private static final String HELP_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHelpScreen";
    private static final String GUIDE_STATE_CLASS_NAME =
        HELP_SCREEN_CLASS_NAME + "$BenmingFirstGuideState";
    private static final String GUIDE_HINT_CLASS_NAME =
        HELP_SCREEN_CLASS_NAME + "$BenmingFirstGuideHint";
    private static final String GUIDE_ROUTE_CLASS_NAME =
        HELP_SCREEN_CLASS_NAME + "$BenmingHelpRoute";
    private static final String TAB_BENMING_KEY =
        "screen.guzhenrenext.forge.help.tab.benming";
    private static final String HELP_BENMING_OVERVIEW_KEY =
        "screen.guzhenrenext.forge.help.benming.overview";
    private static final String HELP_BENMING_GUIDE_DESC_KEY =
        "screen.guzhenrenext.forge.help.benming.guide.desc";
    private static final String HELP_BENMING_OVERLOAD_KEY =
        "screen.guzhenrenext.forge.help.benming.overload.warning";
    private static final String HELP_BENMING_BACKLASH_KEY =
        "screen.guzhenrenext.forge.help.benming.backlash.rule";
    private static final String HELP_BENMING_RECOVERY_KEY =
        "screen.guzhenrenext.forge.help.benming.recovery.rule";
    private static final String HELP_BENMING_AFTERSHOCK_KEY =
        "screen.guzhenrenext.forge.help.benming.aftershock.rule";
    private static final String HELP_RESONANCE_OFFENSE_HUD_CUES_KEY =
        "screen.guzhenrenext.forge.help.resonance.offense.hud_cues";
    private static final String HELP_RESONANCE_DEFENSE_HUD_CUES_KEY =
        "screen.guzhenrenext.forge.help.resonance.defense.hud_cues";
    private static final String HELP_RESONANCE_SPIRIT_HUD_CUES_KEY =
        "screen.guzhenrenext.forge.help.resonance.spirit.hud_cues";
    private static final String HELP_RESONANCE_DEVOUR_HUD_CUES_KEY =
        "screen.guzhenrenext.forge.help.resonance.devour.hud_cues";
    private static final String HELP_OVERVIEW_KEY_HUB =
        "screen.guzhenrenext.forge.help.overview.key_hub";
    private static final String HUD_DEVOUR_BURST_READY_KEY =
        "hud.guzhenrenext.flyingsword.benming.badge.devour.burst_ready";
    private static final String HUD_DEVOUR_DANGER_KEY =
        "hud.guzhenrenext.flyingsword.benming.badge.devour.danger";
    private static final String HUD_DEVOUR_AFTERSHOCK_KEY =
        "hud.guzhenrenext.flyingsword.benming.badge.devour.aftershock";
    private static final String KEY_TOGGLE_HUB =
        "key.guzhenrenext.flyingsword.toggle_hub";
    private static final String HELP_BENMING_BOND_ENTRY_KEY =
        "screen.guzhenrenext.forge.help.benming.bond.entry";
    private static final String HELP_BENMING_BOND_AFTER_SUCCESS_KEY =
        "screen.guzhenrenext.forge.help.benming.bond.after_success";
    private static final String GUIDE_BOND_START_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.bond_start";
    private static final String GUIDE_AFTER_BOND_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.after_bond";
    private static final String GUIDE_OVERLOAD_WARNING_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.overload_first_warning";
    private static final String GUIDE_BACKLASH_FIRST_TIME_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.backlash_first_time";
    private static final String GUIDE_BURST_READY_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.burst_ready_first_time";
    private static final String GUIDE_AFTERSHOCK_FIRST_TIME_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.aftershock_first_time";
    private static final String ZH_LANG_PATH =
        "src/main/resources/assets/guzhenrenext/lang/zh_cn.json";
    private static final String EN_LANG_PATH =
        "src/main/resources/assets/guzhenrenext/lang/en_us.json";
    private static final String CLIENT_EVENTS_SOURCE_PATH =
        "src/main/java/com/Kizunad/guzhenrenext/client/GuClientEvents.java";
    private static final String KEY_BINDINGS_SOURCE_PATH =
        "src/main/java/com/Kizunad/guzhenrenext/client/GuKeyBindings.java";
    private static final int ZH_FIRST_GUIDE_CHAR_LIMIT = 26;
    private static final int EN_FIRST_GUIDE_CHAR_LIMIT = 72;
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";

    @Test
    void helpScreenExposesBenmingTabAndDedicatedBuilder() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        assertEquals(TEST_MAGIC_5, api.helpTabKeys().size());
        assertEquals(TAB_BENMING_KEY, api.helpTabKeys().get(api.benmingTabIndex()));
        assertTrue(api.hasMethod("buildBenmingContent", int.class));
    }

    @Test
    void guideRouteAlwaysPointsToBenmingHelpEntries() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object overloadRoute = api.routeForGuide(GUIDE_OVERLOAD_WARNING_KEY);
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(overloadRoute));
        assertEquals(HELP_BENMING_OVERLOAD_KEY, api.routeHelpEntry(overloadRoute));

        final Object backlashRoute = api.routeForGuide(GUIDE_BACKLASH_FIRST_TIME_KEY);
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(backlashRoute));
        assertEquals(HELP_BENMING_BACKLASH_KEY, api.routeHelpEntry(backlashRoute));

        final Object aftershockRoute = api.routeForGuide(GUIDE_AFTERSHOCK_FIRST_TIME_KEY);
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(aftershockRoute));
        assertEquals(HELP_BENMING_AFTERSHOCK_KEY, api.routeHelpEntry(aftershockRoute));
        assertFalse(
            api.routeHelpEntry(backlashRoute).equals(api.routeHelpEntry(aftershockRoute))
        );

        final Object fallbackRoute = api.routeForGuide("unknown.topic");
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(fallbackRoute));
        assertEquals(HELP_BENMING_OVERVIEW_KEY, api.routeHelpEntry(fallbackRoute));
    }

    @Test
    void openBenmingHelpAlwaysLandsOnBenmingTab() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object helpScreen = api.openBenmingHelp();

        assertEquals(api.benmingTabIndex(), api.currentTabForTesting(helpScreen));
    }

    @Test
    void nonHubHelpEntriesKeepLegacyCloseToGameSemantics() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object defaultHelpScreen = api.newHelpScreen();
        assertFalse(api.returnsToHubOnClose(defaultHelpScreen));
        assertNull(api.resolveCloseTarget(defaultHelpScreen));

        final Object benmingHelpScreen = api.openBenmingHelp();
        assertFalse(api.returnsToHubOnClose(benmingHelpScreen));
        assertNull(api.resolveCloseTarget(benmingHelpScreen));
    }

    @Test
    void guideRouteNormalizesWhitespaceAndBlankTopicFallsBackToOverview()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object trimmedRoute = api.routeForGuide(
            "  " + GUIDE_OVERLOAD_WARNING_KEY + "  "
        );
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(trimmedRoute));
        assertEquals(HELP_BENMING_OVERLOAD_KEY, api.routeHelpEntry(trimmedRoute));

        final Object blankRoute = api.routeForGuide("   ");
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(blankRoute));
        assertEquals(HELP_BENMING_OVERVIEW_KEY, api.routeHelpEntry(blankRoute));
    }

    @Test
    void onboardingSkeletonOnlyReturnsShortHintOncePerTopic() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newGuideState();

        final Optional<?> firstHint = api.createFirstGuideOnce(
            state,
            GUIDE_BOND_START_KEY
        );
        final Optional<?> duplicateHint = api.createFirstGuideOnce(
            state,
            GUIDE_BOND_START_KEY
        );

        assertTrue(firstHint.isPresent());
        assertEquals(GUIDE_BOND_START_KEY, api.hintMessageKey(firstHint.orElseThrow()));
        assertEquals(
            HELP_BENMING_BOND_ENTRY_KEY,
            api.hintRouteHelpEntry(firstHint.orElseThrow())
        );
        assertFalse(duplicateHint.isPresent());
        assertTrue(api.hasSeen(state, GUIDE_BOND_START_KEY));
    }

    @Test
    void onboardingSkeletonRejectsNullBlankAndTrimmedDuplicateTopics()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newGuideState();

        assertTrue(api.createFirstGuideOnce(state, null).isEmpty());
        assertTrue(api.createFirstGuideOnce(state, "   ").isEmpty());

        final Optional<?> firstHint = api.createFirstGuideOnce(
            state,
            "  " + GUIDE_BACKLASH_FIRST_TIME_KEY + "  "
        );
        final Optional<?> duplicateHint = api.createFirstGuideOnce(
            state,
            GUIDE_BACKLASH_FIRST_TIME_KEY
        );

        assertTrue(firstHint.isPresent());
        assertEquals(
            GUIDE_BACKLASH_FIRST_TIME_KEY,
            api.hintMessageKey(firstHint.orElseThrow())
        );
        assertEquals(
            HELP_BENMING_BACKLASH_KEY,
            api.hintRouteHelpEntry(firstHint.orElseThrow())
        );
        assertTrue(duplicateHint.isEmpty());
        assertTrue(api.hasSeen(state, GUIDE_BACKLASH_FIRST_TIME_KEY));
    }

    @Test
    void firstBenmingOnboardingEscalatesToBenmingHelpWithinTwoPrompts()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newGuideState();

        final Object firstHint = api.createFirstGuideOnce(state, GUIDE_BOND_START_KEY)
            .orElseThrow();
        final Object secondHint = api.createFirstGuideOnce(state, GUIDE_AFTER_BOND_KEY)
            .orElseThrow();

        assertEquals(GUIDE_BOND_START_KEY, api.hintMessageKey(firstHint));
        assertEquals(api.benmingTabIndex(), api.hintRouteTabIndex(firstHint));
        assertEquals(HELP_BENMING_BOND_ENTRY_KEY, api.hintRouteHelpEntry(firstHint));

        assertEquals(GUIDE_AFTER_BOND_KEY, api.hintMessageKey(secondHint));
        assertEquals(api.benmingTabIndex(), api.hintRouteTabIndex(secondHint));
        assertEquals(
            HELP_BENMING_BOND_AFTER_SUCCESS_KEY,
            api.hintRouteHelpEntry(secondHint)
        );
        assertTrue(api.hasSeen(state, GUIDE_BOND_START_KEY));
        assertTrue(api.hasSeen(state, GUIDE_AFTER_BOND_KEY));
    }

    @Test
    void benmingGuideCopyStaysShortAndHelpKeysExistInBothLanguages()
        throws Exception {
        final String zhContent = Files.readString(
            Path.of(ZH_LANG_PATH),
            StandardCharsets.UTF_8
        );
        final String enContent = Files.readString(
            Path.of(EN_LANG_PATH),
            StandardCharsets.UTF_8
        );

        assertTrue(extractValue(zhContent, TAB_BENMING_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_BENMING_OVERVIEW_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_BENMING_BACKLASH_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_BENMING_RECOVERY_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_BENMING_AFTERSHOCK_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_RESONANCE_OFFENSE_HUD_CUES_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_RESONANCE_DEFENSE_HUD_CUES_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_RESONANCE_SPIRIT_HUD_CUES_KEY).isPresent());
        assertTrue(extractValue(zhContent, HELP_RESONANCE_DEVOUR_HUD_CUES_KEY).isPresent());
        assertTrue(extractValue(enContent, TAB_BENMING_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_BENMING_OVERVIEW_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_BENMING_BACKLASH_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_BENMING_RECOVERY_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_BENMING_AFTERSHOCK_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_RESONANCE_OFFENSE_HUD_CUES_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_RESONANCE_DEFENSE_HUD_CUES_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_RESONANCE_SPIRIT_HUD_CUES_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_RESONANCE_DEVOUR_HUD_CUES_KEY).isPresent());

        assertTrue(
            extractValue(zhContent, GUIDE_BOND_START_KEY).orElseThrow().length() <=
            ZH_FIRST_GUIDE_CHAR_LIMIT
        );
        assertTrue(
            extractValue(zhContent, GUIDE_OVERLOAD_WARNING_KEY).orElseThrow().length() <=
            ZH_FIRST_GUIDE_CHAR_LIMIT
        );
        assertTrue(
            extractValue(zhContent, GUIDE_BURST_READY_KEY).orElseThrow().length() <=
            ZH_FIRST_GUIDE_CHAR_LIMIT
        );
        assertTrue(
            extractValue(enContent, GUIDE_BOND_START_KEY).orElseThrow().length() <=
            EN_FIRST_GUIDE_CHAR_LIMIT
        );
        assertTrue(
            extractValue(enContent, GUIDE_OVERLOAD_WARNING_KEY).orElseThrow().length() <=
            EN_FIRST_GUIDE_CHAR_LIMIT
        );
        assertTrue(
            extractValue(enContent, GUIDE_BURST_READY_KEY).orElseThrow().length() <=
            EN_FIRST_GUIDE_CHAR_LIMIT
        );
    }

    @Test
    void benmingHelpCopyMakesFirstOnboardingSelfDirected() throws Exception {
        final String zhContent = Files.readString(
            Path.of(ZH_LANG_PATH),
            StandardCharsets.UTF_8
        );
        final String enContent = Files.readString(
            Path.of(EN_LANG_PATH),
            StandardCharsets.UTF_8
        );

        final String zhOverview = extractValue(zhContent, HELP_BENMING_OVERVIEW_KEY)
            .orElseThrow();
        final String zhGuideDesc = extractValue(zhContent, HELP_BENMING_GUIDE_DESC_KEY)
            .orElseThrow();
        final String zhBondEntry = extractValue(zhContent, HELP_BENMING_BOND_ENTRY_KEY)
            .orElseThrow();
        final String zhBacklash = extractValue(zhContent, HELP_BENMING_BACKLASH_KEY)
            .orElseThrow();
        final String zhRecovery = extractValue(zhContent, HELP_BENMING_RECOVERY_KEY)
            .orElseThrow();
        final String zhAftershock = extractValue(zhContent, HELP_BENMING_AFTERSHOCK_KEY)
            .orElseThrow();
        final String zhOffenseHudCues = extractValue(
            zhContent,
            HELP_RESONANCE_OFFENSE_HUD_CUES_KEY
        ).orElseThrow();
        final String zhDefenseHudCues = extractValue(
            zhContent,
            HELP_RESONANCE_DEFENSE_HUD_CUES_KEY
        ).orElseThrow();
        final String zhSpiritHudCues = extractValue(
            zhContent,
            HELP_RESONANCE_SPIRIT_HUD_CUES_KEY
        ).orElseThrow();
        final String zhDevourHudCues = extractValue(
            zhContent,
            HELP_RESONANCE_DEVOUR_HUD_CUES_KEY
        ).orElseThrow();
        final String zhDevourBurst = extractValue(zhContent, HUD_DEVOUR_BURST_READY_KEY)
            .orElseThrow();
        final String zhDevourDanger = extractValue(zhContent, HUD_DEVOUR_DANGER_KEY)
            .orElseThrow();
        final String zhDevourAftershock = extractValue(zhContent, HUD_DEVOUR_AFTERSHOCK_KEY)
            .orElseThrow();
        final String enOverview = extractValue(enContent, HELP_BENMING_OVERVIEW_KEY)
            .orElseThrow();
        final String enGuideDesc = extractValue(enContent, HELP_BENMING_GUIDE_DESC_KEY)
            .orElseThrow();
        final String enBondEntry = extractValue(enContent, HELP_BENMING_BOND_ENTRY_KEY)
            .orElseThrow();
        final String enBacklash = extractValue(enContent, HELP_BENMING_BACKLASH_KEY)
            .orElseThrow();
        final String enRecovery = extractValue(enContent, HELP_BENMING_RECOVERY_KEY)
            .orElseThrow();
        final String enAftershock = extractValue(enContent, HELP_BENMING_AFTERSHOCK_KEY)
            .orElseThrow();
        final String enDevourHudCues = extractValue(
            enContent,
            HELP_RESONANCE_DEVOUR_HUD_CUES_KEY
        ).orElseThrow();
        final String enDevourBurst = extractValue(enContent, HUD_DEVOUR_BURST_READY_KEY)
            .orElseThrow();
        final String enDevourDanger = extractValue(enContent, HUD_DEVOUR_DANGER_KEY)
            .orElseThrow();
        final String enDevourAftershock = extractValue(enContent, HUD_DEVOUR_AFTERSHOCK_KEY)
            .orElseThrow();

        assertTrue(zhOverview.contains("短提示"));
        assertTrue(zhOverview.contains("这里查看"));
        assertTrue(zhGuideDesc.contains("下一步该做什么"));
        assertTrue(zhGuideDesc.contains("本页"));
        assertTrue(zhBondEntry.contains("先确认已经选中目标飞剑"));
        assertTrue(zhBacklash.contains("过载循环"));
        assertTrue(zhBacklash.contains("反噬"));
        assertTrue(zhBacklash.contains("不是爆发后的余震"));
        assertTrue(zhRecovery.contains("恢复"));
        assertTrue(zhRecovery.contains("过载循环"));
        assertTrue(zhRecovery.contains("不是同一阶段"));
        assertTrue(zhAftershock.contains("爆发尾段"));
        assertTrue(zhAftershock.contains("不是过载反噬/恢复"));
        assertTrue(zhOffenseHudCues.contains("压线可斩"));
        assertTrue(zhOffenseHudCues.contains("越线将崩"));
        assertTrue(zhOffenseHudCues.contains("锋芒未收"));
        assertTrue(zhDefenseHudCues.contains("镇域成形"));
        assertTrue(zhDefenseHudCues.contains("稳流将断"));
        assertTrue(zhDefenseHudCues.contains("余稳回流"));
        assertTrue(zhSpiritHudCues.contains("抢拍得势"));
        assertTrue(zhSpiritHudCues.contains("错拍失窗"));
        assertTrue(zhSpiritHudCues.contains("回身整拍"));
        assertTrue(zhDevourHudCues.contains(zhDevourBurst));
        assertTrue(zhDevourHudCues.contains(zhDevourDanger));
        assertTrue(zhDevourHudCues.contains(zhDevourAftershock));
        assertTrue(zhDevourHudCues.contains("反噬 / 恢复"));

        assertTrue(enOverview.contains("First guides stay short"));
        assertTrue(enGuideDesc.contains("next step"));
        assertTrue(enGuideDesc.contains("help page"));
        assertTrue(enBondEntry.contains("selected first"));
        assertTrue(enBacklash.contains("overload loop"));
        assertTrue(enBacklash.contains("not the aftershock tail"));
        assertTrue(enRecovery.contains("rebuild segment"));
        assertTrue(enRecovery.contains("not the same phase as burst aftershock"));
        assertTrue(enAftershock.contains("burst tail"));
        assertTrue(enAftershock.contains("not overload backlash or recovery"));
        assertTrue(enDevourHudCues.contains(enDevourBurst));
        assertTrue(enDevourHudCues.contains(enDevourDanger));
        assertTrue(enDevourHudCues.contains(enDevourAftershock));
        assertTrue(enDevourHudCues.contains("Backlash / Recovery"));
    }

    @Test
    void hubToggleKeypathAndOverviewCopyReflectFinalHubNaming()
        throws Exception {
        final String zhContent = Files.readString(
            Path.of(ZH_LANG_PATH),
            StandardCharsets.UTF_8
        );
        final String enContent = Files.readString(
            Path.of(EN_LANG_PATH),
            StandardCharsets.UTF_8
        );
        final String clientEventsSource = Files.readString(
            Path.of(CLIENT_EVENTS_SOURCE_PATH),
            StandardCharsets.UTF_8
        );
        final String keyBindingsSource = Files.readString(
            Path.of(KEY_BINDINGS_SOURCE_PATH),
            StandardCharsets.UTF_8
        );

        assertEquals(
            "Open/Close Flying Sword Hub",
            extractValue(enContent, KEY_TOGGLE_HUB).orElseThrow()
        );
        assertEquals(
            "H - Open/Close the flying sword hub",
            extractValue(enContent, HELP_OVERVIEW_KEY_HUB).orElseThrow()
        );
        assertEquals(
            "打开/关闭飞剑战术总台",
            extractValue(zhContent, KEY_TOGGLE_HUB).orElseThrow()
        );
        assertEquals(
            "H - 打开/关闭飞剑战术总台",
            extractValue(zhContent, HELP_OVERVIEW_KEY_HUB).orElseThrow()
        );
        assertTrue(keyBindingsSource.contains("FLYING_SWORD_TOGGLE_HUB"));
        assertTrue(keyBindingsSource.contains(KEY_TOGGLE_HUB));
        assertTrue(keyBindingsSource.contains("GLFW.GLFW_KEY_H"));
        assertTrue(
            clientEventsSource.contains(
                "while (GuKeyBindings.FLYING_SWORD_TOGGLE_HUB.consumeClick())"
            )
        );
        assertTrue(
            clientEventsSource.contains(
                "FlyingSwordHubScreen.toggleHubScreen(minecraft.screen)"
            )
        );
        assertFalse(clientEventsSource.contains("toggleHud()"));
        assertFalse(
            keyBindingsSource.contains("FLYING_SWORD_TOGGLE_" + "HUD")
        );
    }

    private static Optional<String> extractValue(
        final String jsonContent,
        final String key
    ) {
        final Pattern pattern = Pattern.compile(
            "\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\""
        );
        final Matcher matcher = pattern.matcher(jsonContent);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(matcher.group(1));
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> helpScreenClass;
        private final Class<?> guideStateClass;
        private final Method helpTabKeysMethod;
        private final Method benmingTabIndexMethod;
        private final Method routeForGuideMethod;
        private final Method createFirstGuideMethod;
        private final Method stateHasSeenMethod;
        private final Constructor<?> helpScreenConstructor;
        private final Method openBenmingHelpMethod;
        private final Method currentTabForTestingMethod;
        private final Method returnsToHubOnCloseMethod;
        private final Method resolveCloseTargetMethod;

        private RuntimeApi(
            final Class<?> helpScreenClass,
            final Class<?> guideStateClass
        ) throws NoSuchMethodException {
            this.helpScreenClass = helpScreenClass;
            this.guideStateClass = guideStateClass;
            this.helpTabKeysMethod = helpScreenClass.getDeclaredMethod("helpTabKeys");
            this.benmingTabIndexMethod = helpScreenClass.getDeclaredMethod("benmingTabIndex");
            this.routeForGuideMethod = helpScreenClass.getMethod(
                "routeForBenmingGuide",
                String.class
            );
            this.createFirstGuideMethod = helpScreenClass.getMethod(
                "createBenmingFirstGuideOnce",
                guideStateClass,
                String.class
            );
            this.stateHasSeenMethod = guideStateClass.getMethod("hasSeen", String.class);
            this.helpScreenConstructor = helpScreenClass.getConstructor();
            this.openBenmingHelpMethod = helpScreenClass.getMethod("openBenmingHelp");
            this.currentTabForTestingMethod = helpScreenClass.getDeclaredMethod(
                "currentTabForTesting"
            );
            this.returnsToHubOnCloseMethod = helpScreenClass.getDeclaredMethod(
                "returnsToHubOnCloseForTesting"
            );
            this.resolveCloseTargetMethod = helpScreenClass.getDeclaredMethod(
                "resolveCloseTargetForTesting"
            );
            this.helpTabKeysMethod.setAccessible(true);
            this.benmingTabIndexMethod.setAccessible(true);
            this.currentTabForTestingMethod.setAccessible(true);
            this.returnsToHubOnCloseMethod.setAccessible(true);
            this.resolveCloseTargetMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> helpScreenClass = Class.forName(
                HELP_SCREEN_CLASS_NAME,
                true,
                loader
            );
            final Class<?> guideStateClass = Class.forName(
                GUIDE_STATE_CLASS_NAME,
                true,
                loader
            );
            return new RuntimeApi(helpScreenClass, guideStateClass);
        }

        List<String> helpTabKeys() throws Exception {
            return (List<String>) helpTabKeysMethod.invoke(null);
        }

        int benmingTabIndex() throws Exception {
            return (int) benmingTabIndexMethod.invoke(null);
        }

        boolean hasMethod(final String name, final Class<?>... parameterTypes) {
            try {
                assertNotNull(helpScreenClass.getDeclaredMethod(name, parameterTypes));
                return true;
            } catch (NoSuchMethodException exception) {
                return false;
            }
        }

        Object routeForGuide(final String topicKey) throws Exception {
            return routeForGuideMethod.invoke(null, topicKey);
        }

        Object openBenmingHelp() throws Exception {
            return openBenmingHelpMethod.invoke(null);
        }

        Object newHelpScreen() throws Exception {
            return helpScreenConstructor.newInstance();
        }

        int routeTabIndex(final Object route) throws Exception {
            return (int) route.getClass().getMethod("tabIndex").invoke(route);
        }

        String routeHelpEntry(final Object route) throws Exception {
            return (String) route.getClass().getMethod("helpEntryKey").invoke(route);
        }

        Object newGuideState() throws Exception {
            return guideStateClass.getConstructor().newInstance();
        }

        Optional<?> createFirstGuideOnce(final Object state, final String topicKey)
            throws Exception {
            return (Optional<?>) createFirstGuideMethod.invoke(null, state, topicKey);
        }

        String hintMessageKey(final Object hint) throws Exception {
            final Class<?> hintClass = Class.forName(
                GUIDE_HINT_CLASS_NAME,
                true,
                hint.getClass().getClassLoader()
            );
            return (String) hintClass.getMethod("messageKey").invoke(hint);
        }

        String hintRouteHelpEntry(final Object hint) throws Exception {
            final Class<?> hintClass = Class.forName(
                GUIDE_HINT_CLASS_NAME,
                true,
                hint.getClass().getClassLoader()
            );
            final Object route = hintClass.getMethod("route").invoke(hint);
            final Class<?> routeClass = Class.forName(
                GUIDE_ROUTE_CLASS_NAME,
                true,
                hint.getClass().getClassLoader()
            );
            return (String) routeClass.getMethod("helpEntryKey").invoke(route);
        }

        int hintRouteTabIndex(final Object hint) throws Exception {
            final Class<?> hintClass = Class.forName(
                GUIDE_HINT_CLASS_NAME,
                true,
                hint.getClass().getClassLoader()
            );
            final Object route = hintClass.getMethod("route").invoke(hint);
            final Class<?> routeClass = Class.forName(
                GUIDE_ROUTE_CLASS_NAME,
                true,
                hint.getClass().getClassLoader()
            );
            return (int) routeClass.getMethod("tabIndex").invoke(route);
        }

        boolean hasSeen(final Object state, final String topicKey) throws Exception {
            return (boolean) stateHasSeenMethod.invoke(state, topicKey);
        }

        int currentTabForTesting(final Object screen) throws Exception {
            return (int) currentTabForTestingMethod.invoke(screen);
        }

        boolean returnsToHubOnClose(final Object screen) throws Exception {
            return (boolean) returnsToHubOnCloseMethod.invoke(screen);
        }

        Object resolveCloseTarget(final Object screen) throws Exception {
            return resolveCloseTargetMethod.invoke(screen);
        }

        private static URLClassLoader buildRuntimeClassLoader() throws IOException {
            final List<URL> urls = new ArrayList<>();
            final Path mainClassesPath = MAIN_CLASSES.toAbsolutePath();
            if (!mainClassesPath.toFile().exists()) {
                throw new IOException("缺少主类输出目录: " + mainClassesPath);
            }
            urls.add(mainClassesPath.toUri().toURL());

            final Path mainResourcesPath = MAIN_RESOURCES.toAbsolutePath();
            if (mainResourcesPath.toFile().exists()) {
                urls.add(mainResourcesPath.toUri().toURL());
            }

            final Properties props = new Properties();
            final Path manifestPath = ARTIFACT_MANIFEST.toAbsolutePath();
            if (!manifestPath.toFile().exists()) {
                throw new IOException("缺少依赖清单: " + manifestPath);
            }
            try (InputStream input = Files.newInputStream(manifestPath)) {
                props.load(input);
            }

            for (String key : props.stringPropertyNames()) {
                final String jarPath = props.getProperty(key);
                if (jarPath == null || jarPath.isBlank()) {
                    continue;
                }
                final Path absoluteJarPath = Path.of(jarPath).toAbsolutePath();
                if (absoluteJarPath.toFile().exists()) {
                    urls.add(absoluteJarPath.toUri().toURL());
                }
            }

            urls.add(resolveMinecraftRuntimeJar().toUri().toURL());
            return new URLClassLoader(
                urls.toArray(new URL[0]),
                ClassLoader.getPlatformClassLoader()
            );
        }

        private static synchronized Path resolveMinecraftRuntimeJar()
            throws IOException {
            if (cachedMinecraftJarPath != null && cachedMinecraftJarPath.toFile().exists()) {
                return cachedMinecraftJarPath;
            }

            final List<Path> searchRoots = new ArrayList<>();
            final String userHome = System.getProperty("user.home");
            searchRoots.add(
                Path.of(
                    userHome,
                    ".gradle",
                    "caches",
                    "neoformruntime",
                    "intermediate_results"
                )
            );
            searchRoots.add(
                Path.of(
                    userHome,
                    ".gradle",
                    "caches",
                    "fabric-loom",
                    "minecraftMaven"
                )
            );

            for (Path root : searchRoots) {
                final Path matched = findJarContainingResource(
                    root,
                    NBT_TAG_CLASS_RESOURCE
                );
                if (matched != null) {
                    cachedMinecraftJarPath = matched;
                    return matched;
                }
            }

            throw new IOException("未找到包含 net.minecraft.nbt.Tag 的运行时 Jar");
        }

        private static Path findJarContainingResource(
            final Path root,
            final String resource
        ) throws IOException {
            if (root == null || !root.toFile().exists()) {
                return null;
            }
            try (var stream = Files.walk(root, TEST_MAGIC_6)) {
                final List<Path> candidates = stream
                    .filter(path -> path.toString().endsWith(".jar"))
                    .toList();
                for (Path candidate : candidates) {
                    if (jarContainsResource(candidate, resource)) {
                        return candidate;
                    }
                }
            }
            return null;
        }

        private static boolean jarContainsResource(
            final Path jarPath,
            final String resource
        ) {
            try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
                return jar.getEntry(resource) != null;
            } catch (IOException ignored) {
                return false;
            }
        }
    }
}
