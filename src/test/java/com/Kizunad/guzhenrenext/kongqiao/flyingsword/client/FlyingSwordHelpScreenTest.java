package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.io.IOException;
import java.io.InputStream;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHelpScreenTest {

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
    private static final String GUIDE_BURST_READY_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.burst_ready_first_time";
    private static final String ZH_LANG_PATH =
        "src/main/resources/assets/guzhenrenext/lang/zh_cn.json";
    private static final String EN_LANG_PATH =
        "src/main/resources/assets/guzhenrenext/lang/en_us.json";
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

        assertEquals(5, api.helpTabKeys().size());
        assertEquals(TAB_BENMING_KEY, api.helpTabKeys().get(api.benmingTabIndex()));
        assertTrue(api.hasMethod("buildBenmingContent", int.class));
    }

    @Test
    void guideRouteAlwaysPointsToBenmingHelpEntries() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object overloadRoute = api.routeForGuide(GUIDE_OVERLOAD_WARNING_KEY);
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(overloadRoute));
        assertEquals(HELP_BENMING_OVERLOAD_KEY, api.routeHelpEntry(overloadRoute));

        final Object fallbackRoute = api.routeForGuide("unknown.topic");
        assertEquals(api.benmingTabIndex(), api.routeTabIndex(fallbackRoute));
        assertEquals(HELP_BENMING_OVERVIEW_KEY, api.routeHelpEntry(fallbackRoute));
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
        assertTrue(extractValue(enContent, TAB_BENMING_KEY).isPresent());
        assertTrue(extractValue(enContent, HELP_BENMING_OVERVIEW_KEY).isPresent());

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
        final String enOverview = extractValue(enContent, HELP_BENMING_OVERVIEW_KEY)
            .orElseThrow();
        final String enGuideDesc = extractValue(enContent, HELP_BENMING_GUIDE_DESC_KEY)
            .orElseThrow();
        final String enBondEntry = extractValue(enContent, HELP_BENMING_BOND_ENTRY_KEY)
            .orElseThrow();

        assertTrue(zhOverview.contains("短提示"));
        assertTrue(zhOverview.contains("这里查看"));
        assertTrue(zhGuideDesc.contains("下一步该做什么"));
        assertTrue(zhGuideDesc.contains("本页"));
        assertTrue(zhBondEntry.contains("先确认已经选中目标飞剑"));

        assertTrue(enOverview.contains("First guides stay short"));
        assertTrue(enGuideDesc.contains("next step"));
        assertTrue(enGuideDesc.contains("help page"));
        assertTrue(enBondEntry.contains("selected first"));
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
            this.helpTabKeysMethod.setAccessible(true);
            this.benmingTabIndexMethod.setAccessible(true);
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
            try (var stream = Files.walk(root, 6)) {
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
