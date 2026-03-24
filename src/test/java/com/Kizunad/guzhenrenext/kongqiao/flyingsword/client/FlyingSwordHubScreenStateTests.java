package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHubScreenStateTests {

    private static final String HUB_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHubScreen";
    private static final String SCREEN_CLASS_NAME =
        "net.minecraft.client.gui.screens.Screen";
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");

    private static final int KEY_ESCAPE = 256;
    private static final int KEY_TAB = 258;
    private static final int SCREEN_WIDTH_LARGE = 1920;
    private static final int SCREEN_HEIGHT_LARGE = 1080;
    private static final int SCREEN_WIDTH_SMALL = 1280;
    private static final int SCREEN_HEIGHT_SMALL = 720;
    private static final int TEST_SEARCH_DEPTH = 6;

    private static Path cachedMinecraftJarPath;

    @Test
    void defaultTabIsOverviewAndTopLevelTabsStayThreeWay() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object screen = api.newHubScreen();

        api.simulateInit(screen, SCREEN_WIDTH_LARGE, SCREEN_HEIGHT_LARGE);

        assertEquals(List.of("总览", "培养", "帮助"), api.topLevelTabs());
        assertEquals(api.overviewTabIndex(), api.currentTab(screen));
        assertFalse(api.closeRequested(screen));
    }

    @Test
    void hToggleOpensClosesAndReopenFallsBackToOverview() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object opened = api.toggleFrom(null);
        assertNotNull(opened);
        api.simulateInit(opened, SCREEN_WIDTH_LARGE, SCREEN_HEIGHT_LARGE);
        api.switchTab(opened, api.helpTabIndex());
        assertEquals(api.helpTabIndex(), api.currentTab(opened));

        assertNull(api.toggleFrom(opened));

        final Object reopened = api.toggleFrom(null);
        assertNotNull(reopened);
        api.simulateInit(reopened, SCREEN_WIDTH_LARGE, SCREEN_HEIGHT_LARGE);
        assertEquals(api.overviewTabIndex(), api.currentTab(reopened));
    }

    @Test
    void escapeAndTabBothRequestClose() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object escScreen = api.newHubScreen();
        assertTrue(api.keyPressed(escScreen, KEY_ESCAPE));
        assertTrue(api.closeRequested(escScreen));

        final Object tabScreen = api.newHubScreen();
        assertTrue(api.keyPressed(tabScreen, KEY_TAB));
        assertTrue(api.closeRequested(tabScreen));
    }

    @Test
    void reinitAfterResizeKeepsCurrentTopLevelTabStable() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object screen = api.newHubScreen();

        api.simulateInit(screen, SCREEN_WIDTH_LARGE, SCREEN_HEIGHT_LARGE);
        api.switchTab(screen, api.helpTabIndex());
        assertEquals(api.helpTabIndex(), api.currentTab(screen));

        api.simulateInit(screen, SCREEN_WIDTH_SMALL, SCREEN_HEIGHT_SMALL);

        assertEquals(api.helpTabIndex(), api.currentTab(screen));
    }

    private static final class RuntimeApi {

        private final Constructor<?> hubConstructor;
        private final Method toggleHubScreenMethod;
        private final Method topLevelTabsMethod;
        private final Method overviewTabIndexMethod;
        private final Method helpTabIndexMethod;
        private final Method currentTabMethod;
        private final Method switchTabMethod;
        private final Method simulateInitMethod;
        private final Method closeRequestedMethod;
        private final Method keyPressedMethod;

        private RuntimeApi(
            final Class<?> hubScreenClass,
            final Class<?> screenClass
        ) throws Exception {
            this.hubConstructor = hubScreenClass.getConstructor();
            this.toggleHubScreenMethod = hubScreenClass.getMethod(
                "toggleHubScreen",
                screenClass
            );
            this.topLevelTabsMethod = hubScreenClass.getDeclaredMethod(
                "topLevelTabTitlesForTesting"
            );
            this.overviewTabIndexMethod = hubScreenClass.getDeclaredMethod(
                "overviewTabIndex"
            );
            this.helpTabIndexMethod = hubScreenClass.getDeclaredMethod("helpTabIndex");
            this.currentTabMethod = hubScreenClass.getDeclaredMethod("currentTabForTesting");
            this.switchTabMethod = hubScreenClass.getDeclaredMethod(
                "switchTabForTesting",
                int.class
            );
            this.simulateInitMethod = hubScreenClass.getDeclaredMethod(
                "simulateInitForTesting",
                int.class,
                int.class
            );
            this.closeRequestedMethod = hubScreenClass.getDeclaredMethod(
                "closeRequestedForTesting"
            );
            this.keyPressedMethod = hubScreenClass.getMethod(
                "keyPressed",
                int.class,
                int.class,
                int.class
            );

            this.topLevelTabsMethod.setAccessible(true);
            this.overviewTabIndexMethod.setAccessible(true);
            this.helpTabIndexMethod.setAccessible(true);
            this.currentTabMethod.setAccessible(true);
            this.switchTabMethod.setAccessible(true);
            this.simulateInitMethod.setAccessible(true);
            this.closeRequestedMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> hubScreenClass = Class.forName(
                HUB_SCREEN_CLASS_NAME,
                true,
                loader
            );
            final Class<?> screenClass = Class.forName(SCREEN_CLASS_NAME, true, loader);
            return new RuntimeApi(hubScreenClass, screenClass);
        }

        Object newHubScreen() throws Exception {
            return hubConstructor.newInstance();
        }

        Object toggleFrom(final Object currentScreen) throws Exception {
            return toggleHubScreenMethod.invoke(null, currentScreen);
        }

        List<?> topLevelTabs() throws Exception {
            return (List<?>) topLevelTabsMethod.invoke(null);
        }

        int overviewTabIndex() throws Exception {
            return (int) overviewTabIndexMethod.invoke(null);
        }

        int helpTabIndex() throws Exception {
            return (int) helpTabIndexMethod.invoke(null);
        }

        int currentTab(final Object screen) throws Exception {
            return (int) currentTabMethod.invoke(screen);
        }

        void switchTab(final Object screen, final int tabIndex) throws Exception {
            switchTabMethod.invoke(screen, tabIndex);
        }

        void simulateInit(
            final Object screen,
            final int width,
            final int height
        ) throws Exception {
            simulateInitMethod.invoke(screen, width, height);
        }

        boolean closeRequested(final Object screen) throws Exception {
            return (boolean) closeRequestedMethod.invoke(screen);
        }

        boolean keyPressed(final Object screen, final int keyCode) throws Exception {
            return (boolean) keyPressedMethod.invoke(screen, keyCode, 0, 0);
        }
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

    private static synchronized Path resolveMinecraftRuntimeJar() throws IOException {
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
            final Path matched = findJarContainingResource(root, NBT_TAG_CLASS_RESOURCE);
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
        try (var stream = Files.walk(root, TEST_SEARCH_DEPTH)) {
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
