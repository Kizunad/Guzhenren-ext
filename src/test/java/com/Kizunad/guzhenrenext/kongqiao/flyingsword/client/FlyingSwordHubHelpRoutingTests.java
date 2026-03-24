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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHubHelpRoutingTests {

    private static final String HUB_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHubScreen";
    private static final String HELP_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHelpScreen";
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final int TEST_SEARCH_DEPTH = 6;

    private static Path cachedMinecraftJarPath;

    @Test
    void hubHelpPageExposesFiveTaskEightRoutes() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        assertEquals(
            List.of("快速上手", "操作", "路线", "失败原因", "完整帮助"),
            api.helpRouteTitles()
        );
        assertEquals(
            List.of(
                api.helpOverviewTabIndex(),
                api.helpCombatTabIndex(),
                api.helpGrowthTabIndex(),
                api.helpBenmingTabIndex(),
                api.helpOverviewTabIndex()
            ),
            api.helpRouteTargets()
        );
    }

    @Test
    void hubOriginHelpReturnsToHubHelpTabOnClose() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object hubScreen = api.newHubScreen();

        api.simulateHubInit(hubScreen, 1920, 1080);
        api.switchHubTab(hubScreen, api.hubHelpTabIndex());

        final Object routeHelp = api.openHelpRoute(hubScreen, 3);
        assertTrue(api.helpReturnsToHubOnClose(routeHelp));
        assertEquals(api.helpBenmingTabIndex(), api.currentHelpTab(routeHelp));

        final Object closeTarget = api.resolveHelpCloseTarget(routeHelp);
        assertNotNull(closeTarget);
        assertTrue(api.isHubScreen(closeTarget));
        api.simulateHubInit(closeTarget, 1920, 1080);
        assertEquals(api.hubHelpTabIndex(), api.currentHubTab(closeTarget));
    }

    @Test
    void completeHelpRouteStillReturnsToHubHelpTab() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object hubScreen = api.newHubScreen();

        api.simulateHubInit(hubScreen, 1920, 1080);
        api.switchHubTab(hubScreen, api.hubHelpTabIndex());

        final Object routeHelp = api.openHelpRoute(hubScreen, 4);
        assertEquals(api.helpOverviewTabIndex(), api.currentHelpTab(routeHelp));

        final Object closeTarget = api.resolveHelpCloseTarget(routeHelp);
        assertNotNull(closeTarget);
        api.simulateHubInit(closeTarget, 1920, 1080);
        assertEquals(api.hubHelpTabIndex(), api.currentHubTab(closeTarget));
    }

    private static final class RuntimeApi {

        private final Constructor<?> hubConstructor;
        private final Method helpRouteTitlesMethod;
        private final Method helpRouteTargetsMethod;
        private final Method hubHelpTabIndexMethod;
        private final Method hubCurrentTabMethod;
        private final Method hubSwitchTabMethod;
        private final Method hubSimulateInitMethod;
        private final Method openHelpRouteMethod;
        private final Method isHubScreenMethod;
        private final Method helpOverviewTabIndexMethod;
        private final Method helpCombatTabIndexMethod;
        private final Method helpGrowthTabIndexMethod;
        private final Method helpBenmingTabIndexMethod;
        private final Method helpCurrentTabMethod;
        private final Method helpReturnsToHubOnCloseMethod;
        private final Method helpResolveCloseTargetMethod;

        private RuntimeApi(
            final Class<?> hubScreenClass,
            final Class<?> helpScreenClass,
            final Class<?> screenClass
        ) throws Exception {
            this.hubConstructor = hubScreenClass.getConstructor();
            this.helpRouteTitlesMethod = hubScreenClass.getDeclaredMethod(
                "helpRouteTitlesForTesting"
            );
            this.helpRouteTargetsMethod = hubScreenClass.getDeclaredMethod(
                "helpRouteTabTargetsForTesting"
            );
            this.hubHelpTabIndexMethod = hubScreenClass.getDeclaredMethod("helpTabIndex");
            this.hubCurrentTabMethod = hubScreenClass.getDeclaredMethod("currentTabForTesting");
            this.hubSwitchTabMethod = hubScreenClass.getDeclaredMethod(
                "switchTabForTesting",
                int.class
            );
            this.hubSimulateInitMethod = hubScreenClass.getDeclaredMethod(
                "simulateInitForTesting",
                int.class,
                int.class
            );
            this.openHelpRouteMethod = hubScreenClass.getDeclaredMethod(
                "openHelpRouteForTesting",
                int.class
            );
            this.isHubScreenMethod = hubScreenClass.getMethod("isHubScreen", screenClass);
            this.helpOverviewTabIndexMethod = helpScreenClass.getDeclaredMethod(
                "overviewTabIndex"
            );
            this.helpCombatTabIndexMethod = helpScreenClass.getDeclaredMethod(
                "combatTabIndex"
            );
            this.helpGrowthTabIndexMethod = helpScreenClass.getDeclaredMethod(
                "growthTabIndex"
            );
            this.helpBenmingTabIndexMethod = helpScreenClass.getDeclaredMethod(
                "benmingTabIndex"
            );
            this.helpCurrentTabMethod = helpScreenClass.getDeclaredMethod(
                "currentTabForTesting"
            );
            this.helpReturnsToHubOnCloseMethod = helpScreenClass.getDeclaredMethod(
                "returnsToHubOnCloseForTesting"
            );
            this.helpResolveCloseTargetMethod = helpScreenClass.getDeclaredMethod(
                "resolveCloseTargetForTesting"
            );

            this.helpRouteTitlesMethod.setAccessible(true);
            this.helpRouteTargetsMethod.setAccessible(true);
            this.hubHelpTabIndexMethod.setAccessible(true);
            this.hubCurrentTabMethod.setAccessible(true);
            this.hubSwitchTabMethod.setAccessible(true);
            this.hubSimulateInitMethod.setAccessible(true);
            this.openHelpRouteMethod.setAccessible(true);
            this.helpOverviewTabIndexMethod.setAccessible(true);
            this.helpCombatTabIndexMethod.setAccessible(true);
            this.helpGrowthTabIndexMethod.setAccessible(true);
            this.helpBenmingTabIndexMethod.setAccessible(true);
            this.helpCurrentTabMethod.setAccessible(true);
            this.helpReturnsToHubOnCloseMethod.setAccessible(true);
            this.helpResolveCloseTargetMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> hubScreenClass = Class.forName(
                HUB_SCREEN_CLASS_NAME,
                true,
                loader
            );
            final Class<?> helpScreenClass = Class.forName(
                HELP_SCREEN_CLASS_NAME,
                true,
                loader
            );
            final Class<?> screenClass = Class.forName(
                "net.minecraft.client.gui.screens.Screen",
                true,
                loader
            );
            return new RuntimeApi(hubScreenClass, helpScreenClass, screenClass);
        }

        Object newHubScreen() throws Exception {
            return hubConstructor.newInstance();
        }

        List<?> helpRouteTitles() throws Exception {
            return (List<?>) helpRouteTitlesMethod.invoke(null);
        }

        List<?> helpRouteTargets() throws Exception {
            return (List<?>) helpRouteTargetsMethod.invoke(null);
        }

        int hubHelpTabIndex() throws Exception {
            return (int) hubHelpTabIndexMethod.invoke(null);
        }

        int currentHubTab(final Object hubScreen) throws Exception {
            return (int) hubCurrentTabMethod.invoke(hubScreen);
        }

        void switchHubTab(final Object hubScreen, final int tabIndex) throws Exception {
            hubSwitchTabMethod.invoke(hubScreen, tabIndex);
        }

        void simulateHubInit(
            final Object hubScreen,
            final int width,
            final int height
        ) throws Exception {
            hubSimulateInitMethod.invoke(hubScreen, width, height);
        }

        Object openHelpRoute(final Object hubScreen, final int routeIndex) throws Exception {
            return openHelpRouteMethod.invoke(hubScreen, routeIndex);
        }

        boolean isHubScreen(final Object screen) throws Exception {
            return (boolean) isHubScreenMethod.invoke(null, screen);
        }

        int helpOverviewTabIndex() throws Exception {
            return (int) helpOverviewTabIndexMethod.invoke(null);
        }

        int helpCombatTabIndex() throws Exception {
            return (int) helpCombatTabIndexMethod.invoke(null);
        }

        int helpGrowthTabIndex() throws Exception {
            return (int) helpGrowthTabIndexMethod.invoke(null);
        }

        int helpBenmingTabIndex() throws Exception {
            return (int) helpBenmingTabIndexMethod.invoke(null);
        }

        int currentHelpTab(final Object helpScreen) throws Exception {
            return (int) helpCurrentTabMethod.invoke(helpScreen);
        }

        boolean helpReturnsToHubOnClose(final Object helpScreen) throws Exception {
            return (boolean) helpReturnsToHubOnCloseMethod.invoke(helpScreen);
        }

        Object resolveHelpCloseTarget(final Object helpScreen) throws Exception {
            return helpResolveCloseTargetMethod.invoke(helpScreen);
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
