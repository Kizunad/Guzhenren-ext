package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHubCultivationPanelTest {

    private static final String HUB_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHubScreen";
    private static final String TACTICAL_SERVICE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService";
    private static final String DISPLAY_DATA_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$SwordDisplayData";
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");

    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int SCREEN_WIDTH_SMALL = 1280;
    private static final int SCREEN_HEIGHT_SMALL = 720;
    private static final int TEST_SEARCH_DEPTH = 6;
    private static Path cachedMinecraftJarPath;

    @Test
    void cultivationPageExposesFourSecondaryTabsAndKeepsSelectionAcrossReinit()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object screen = api.newHubScreen();

        api.simulateInit(screen, SCREEN_WIDTH, SCREEN_HEIGHT);
        api.switchTab(screen, api.cultivationTabIndex());

        assertEquals(List.of("锻造", "修炼", "集群", "成长"), api.cultivationSubTabs());
        assertEquals(0, api.currentCultivationRoute(screen));

        api.switchCultivationRoute(screen, 2);
        assertEquals(2, api.currentCultivationRoute(screen));

        api.simulateInit(screen, SCREEN_WIDTH_SMALL, SCREEN_HEIGHT_SMALL);
        assertEquals(2, api.currentCultivationRoute(screen));
    }

    @Test
    void cultivationRoutesStaySummaryOnlyAndKeepOriginalScreenLinks()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object benmingRow = api.newDisplayData(UUID.randomUUID());
        api.setDisplayStringField(benmingRow, "quality", "MYSTIC");
        api.setDisplayIntField(benmingRow, "level", 7);
        api.setDisplayIntField(benmingRow, "experience", 345);
        api.setDisplayFloatField(benmingRow, "expProgress", 0.45F);
        api.setDisplayFloatField(benmingRow, "health", 82.0F);
        api.setDisplayFloatField(benmingRow, "maxHealth", 100.0F);
        api.setDisplayBooleanField(benmingRow, "isBenmingSword", true);

        final Object snapshot = api.snapshot(List.of(benmingRow), null);
        final Object forgeSummary = api.newForgeSummary(true, 64, 32, 1, 12, "等待继续投喂");
        final Object trainingSummary = api.newTrainingSummary(80, 100, 345);

        final List<?> routes = api.routes(
            api.buildCultivationState(snapshot, forgeSummary, trainingSummary, 24, 40, 2)
        );

        assertEquals(4, routes.size());
        assertRoute(
            api,
            routes.get(0),
            "锻造",
            "OPEN_FORGE",
            true,
            true,
            "32/64",
            "道痕总分 12"
        );
        assertRoute(
            api,
            routes.get(1),
            "修炼",
            "OPEN_TRAINING",
            true,
            true,
            "80/100",
            "累计经验 345"
        );
        assertRoute(
            api,
            routes.get(2),
            "集群",
            "OPEN_CLUSTER",
            true,
            true,
            "24/40",
            "活跃 2"
        );
        assertRoute(
            api,
            routes.get(3),
            "成长",
            "OPEN_GROWTH_HELP",
            true,
            false,
            "Lv.7",
            "经验 345"
        );
    }

    @Test
    void cultivationStateFallsBackToNeutralSummariesWhenNoRuntimeContextExists()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final List<?> routes = api.routes(
            api.buildCultivationState(api.snapshot(List.of(), null), null, null, 0, 0, 0)
        );

        assertEquals(4, routes.size());
        assertEquals("锻造", api.routeString(routes.get(0), "title"));
        assertTrue(api.routeString(routes.get(0), "summaryText").contains("未检测到进行中的培养工程"));
        assertTrue(api.routeString(routes.get(1), "summaryText").contains("暂无燃料或尚未开始修炼"));
        assertTrue(api.routeString(routes.get(2), "summaryText").contains("暂无客户端集群算力缓存"));
        assertTrue(api.routeString(routes.get(3), "summaryText").contains("暂无成长锚点"));
        assertTrue(api.routeBoolean(routes.get(0), "routeOnly"));
        assertFalse(api.routeBoolean(routes.get(3), "opensOriginalScreen"));
    }

    private static void assertRoute(
        final RuntimeApi api,
        final Object route,
        final String expectedTitle,
        final String expectedActionType,
        final boolean expectedRouteOnly,
        final boolean expectedOriginalScreen,
        final String progressNeedle,
        final String badgeNeedle
    ) throws Exception {
        assertEquals(expectedTitle, api.routeString(route, "title"));
        assertEquals(expectedActionType, api.routeActionType(route));
        assertEquals(expectedRouteOnly, api.routeBoolean(route, "routeOnly"));
        assertEquals(expectedOriginalScreen, api.routeBoolean(route, "opensOriginalScreen"));
        assertTrue(api.routeString(route, "actionText").contains("打开"));
        assertTrue(api.routeString(route, "progressText").contains(progressNeedle));
        assertTrue(api.routeBadges(route).stream().anyMatch(badge -> badge.contains(badgeNeedle)));
    }

    private static final class RuntimeApi {

        private final Constructor<?> hubConstructor;
        private final Constructor<?> displayDataConstructor;
        private final Constructor<?> forgeSummaryConstructor;
        private final Constructor<?> trainingSummaryConstructor;
        private final Method cultivationTabIndexMethod;
        private final Method cultivationSubTabsMethod;
        private final Method currentTabMethod;
        private final Method switchTabMethod;
        private final Method simulateInitMethod;
        private final Method currentCultivationRouteMethod;
        private final Method switchCultivationRouteMethod;
        private final Method buildCultivationStateMethod;
        private final Method snapshotMethod;
        private final Method routesMethod;

        private final Field qualityField;
        private final Field levelField;
        private final Field experienceField;
        private final Field expProgressField;
        private final Field healthField;
        private final Field maxHealthField;
        private final Field benmingField;
        private final Field uuidField;

        private RuntimeApi(
            final Class<?> hubScreenClass,
            final Class<?> tacticalServiceClass,
            final Class<?> displayDataClass
        ) throws Exception {
            this.hubConstructor = hubScreenClass.getConstructor();
            this.displayDataConstructor = displayDataClass.getConstructor();

            final Class<?> forgeSummaryClass = Class.forName(
                HUB_SCREEN_CLASS_NAME.replace(
                    "FlyingSwordHubScreen",
                    "FlyingSwordHubCultivationModel$ForgeSummary"
                ),
                true,
                hubScreenClass.getClassLoader()
            );
            final Class<?> trainingSummaryClass = Class.forName(
                HUB_SCREEN_CLASS_NAME.replace(
                    "FlyingSwordHubScreen",
                    "FlyingSwordHubCultivationModel$TrainingSummary"
                ),
                true,
                hubScreenClass.getClassLoader()
            );
            this.forgeSummaryConstructor = forgeSummaryClass.getDeclaredConstructor(
                boolean.class,
                int.class,
                int.class,
                int.class,
                int.class,
                String.class
            );
            this.trainingSummaryConstructor = trainingSummaryClass.getDeclaredConstructor(
                int.class,
                int.class,
                int.class
            );

            this.cultivationTabIndexMethod = hubScreenClass.getDeclaredMethod(
                "cultivationTabIndex"
            );
            this.cultivationSubTabsMethod = hubScreenClass.getDeclaredMethod(
                "cultivationSubTabTitlesForTesting"
            );
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
            this.currentCultivationRouteMethod = hubScreenClass.getDeclaredMethod(
                "currentCultivationRouteForTesting"
            );
            this.switchCultivationRouteMethod = hubScreenClass.getDeclaredMethod(
                "switchCultivationRouteForTesting",
                int.class
            );
            this.buildCultivationStateMethod = hubScreenClass.getDeclaredMethod(
                "buildCultivationStateForTesting",
                Class.forName(
                    TACTICAL_SERVICE_CLASS_NAME + "$TacticalStateSnapshot",
                    true,
                    hubScreenClass.getClassLoader()
                ),
                forgeSummaryClass,
                trainingSummaryClass,
                int.class,
                int.class,
                int.class
            );
            this.snapshotMethod = tacticalServiceClass.getMethod(
                "snapshotFromRoster",
                List.class,
                UUID.class
            );

            final Class<?> cultivationStateClass = Class.forName(
                HUB_SCREEN_CLASS_NAME.replace(
                    "FlyingSwordHubScreen",
                    "FlyingSwordHubCultivationModel$CultivationPanelState"
                ),
                true,
                hubScreenClass.getClassLoader()
            );
            this.routesMethod = cultivationStateClass.getDeclaredMethod("routes");

            this.qualityField = displayDataClass.getField("quality");
            this.levelField = displayDataClass.getField("level");
            this.experienceField = displayDataClass.getField("experience");
            this.expProgressField = displayDataClass.getField("expProgress");
            this.healthField = displayDataClass.getField("health");
            this.maxHealthField = displayDataClass.getField("maxHealth");
            this.benmingField = displayDataClass.getField("isBenmingSword");
            this.uuidField = displayDataClass.getField("uuid");

            this.cultivationTabIndexMethod.setAccessible(true);
            this.cultivationSubTabsMethod.setAccessible(true);
            this.currentTabMethod.setAccessible(true);
            this.switchTabMethod.setAccessible(true);
            this.simulateInitMethod.setAccessible(true);
            this.currentCultivationRouteMethod.setAccessible(true);
            this.switchCultivationRouteMethod.setAccessible(true);
            this.buildCultivationStateMethod.setAccessible(true);
            this.routesMethod.setAccessible(true);
            this.forgeSummaryConstructor.setAccessible(true);
            this.trainingSummaryConstructor.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> hubScreenClass = Class.forName(HUB_SCREEN_CLASS_NAME, true, loader);
            final Class<?> tacticalServiceClass = Class.forName(
                TACTICAL_SERVICE_CLASS_NAME,
                true,
                loader
            );
            final Class<?> displayDataClass = Class.forName(DISPLAY_DATA_CLASS_NAME, true, loader);
            return new RuntimeApi(hubScreenClass, tacticalServiceClass, displayDataClass);
        }

        Object newHubScreen() throws Exception {
            return hubConstructor.newInstance();
        }

        int cultivationTabIndex() throws Exception {
            return (int) cultivationTabIndexMethod.invoke(null);
        }

        List<?> cultivationSubTabs() throws Exception {
            return (List<?>) cultivationSubTabsMethod.invoke(null);
        }

        void switchTab(final Object screen, final int tabIndex) throws Exception {
            switchTabMethod.invoke(screen, tabIndex);
            assertEquals(tabIndex, (int) currentTabMethod.invoke(screen));
        }

        void simulateInit(final Object screen, final int width, final int height)
            throws Exception {
            simulateInitMethod.invoke(screen, width, height);
        }

        int currentCultivationRoute(final Object screen) throws Exception {
            return (int) currentCultivationRouteMethod.invoke(screen);
        }

        void switchCultivationRoute(final Object screen, final int routeIndex)
            throws Exception {
            switchCultivationRouteMethod.invoke(screen, routeIndex);
        }

        Object newDisplayData(final UUID uuid) throws Exception {
            final Object displayData = displayDataConstructor.newInstance();
            uuidField.set(displayData, uuid);
            return displayData;
        }

        void setDisplayStringField(
            final Object displayData,
            final String fieldName,
            final String enumConstant
        ) throws Exception {
            if (!"quality".equals(fieldName)) {
                throw new IllegalArgumentException(fieldName);
            }
            final Class<?> enumClass = qualityField.getType();
            qualityField.set(
                displayData,
                Enum.valueOf(enumClass.asSubclass(Enum.class), enumConstant)
            );
        }

        void setDisplayIntField(
            final Object displayData,
            final String fieldName,
            final int value
        ) throws Exception {
            final Field field = switch (fieldName) {
                case "level" -> levelField;
                case "experience" -> experienceField;
                default -> throw new IllegalArgumentException(fieldName);
            };
            field.setInt(displayData, value);
        }

        void setDisplayFloatField(
            final Object displayData,
            final String fieldName,
            final float value
        ) throws Exception {
            final Field field = switch (fieldName) {
                case "expProgress" -> expProgressField;
                case "health" -> healthField;
                case "maxHealth" -> maxHealthField;
                default -> throw new IllegalArgumentException(fieldName);
            };
            field.setFloat(displayData, value);
        }

        void setDisplayBooleanField(
            final Object displayData,
            final String fieldName,
            final boolean value
        ) throws Exception {
            if (!"isBenmingSword".equals(fieldName)) {
                throw new IllegalArgumentException(fieldName);
            }
            benmingField.setBoolean(displayData, value);
        }

        Object snapshot(final List<Object> rows, final UUID selectedUuid) throws Exception {
            return snapshotMethod.invoke(null, rows, selectedUuid);
        }

        Object newForgeSummary(
            final boolean active,
            final int requiredCount,
            final int fedCount,
            final int daoTypeCount,
            final int daoTotalScore,
            final String lastMessage
        ) throws Exception {
            return forgeSummaryConstructor.newInstance(
                active,
                requiredCount,
                fedCount,
                daoTypeCount,
                daoTotalScore,
                lastMessage
            );
        }

        Object newTrainingSummary(
            final int fuelTime,
            final int maxFuelTime,
            final int accumulatedExp
        ) throws Exception {
            return trainingSummaryConstructor.newInstance(
                fuelTime,
                maxFuelTime,
                accumulatedExp
            );
        }

        Object buildCultivationState(
            final Object snapshot,
            final Object forgeSummary,
            final Object trainingSummary,
            final int clusterLoad,
            final int clusterCapacity,
            final int clusterActiveCount
        ) throws Exception {
            return buildCultivationStateMethod.invoke(
                null,
                snapshot,
                forgeSummary,
                trainingSummary,
                clusterLoad,
                clusterCapacity,
                clusterActiveCount
            );
        }

        List<?> routes(final Object cultivationState) throws Exception {
            return (List<?>) routesMethod.invoke(cultivationState);
        }

        String routeString(final Object route, final String accessorName) throws Exception {
            final Method accessor = route.getClass().getDeclaredMethod(accessorName);
            accessor.setAccessible(true);
            return String.valueOf(accessor.invoke(route));
        }

        boolean routeBoolean(final Object route, final String accessorName) throws Exception {
            final Method accessor = route.getClass().getDeclaredMethod(accessorName);
            accessor.setAccessible(true);
            return (boolean) accessor.invoke(route);
        }

        String routeActionType(final Object route) throws Exception {
            final Method accessor = route.getClass().getDeclaredMethod("actionType");
            accessor.setAccessible(true);
            return String.valueOf(accessor.invoke(route));
        }

        List<String> routeBadges(final Object route) throws Exception {
            final Method accessor = route.getClass().getDeclaredMethod("resourceBadges");
            accessor.setAccessible(true);
            final List<?> rawBadges = (List<?>) accessor.invoke(route);
            return rawBadges.stream().map(String::valueOf).toList();
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
        searchRoots.add(Path.of(System.getProperty("user.home"), ".gradle", "caches"));
        searchRoots.add(Path.of(System.getProperty("user.home"), ".gradle"));

        for (final Path root : searchRoots) {
            if (!root.toFile().exists()) {
                continue;
            }
            final Path resolved = searchForMinecraftRuntimeJar(root, 0);
            if (resolved != null) {
                cachedMinecraftJarPath = resolved;
                return resolved;
            }
        }
        throw new IOException("无法定位 Minecraft 运行时 Jar（缺少 " + NBT_TAG_CLASS_RESOURCE + "）");
    }

    private static Path searchForMinecraftRuntimeJar(final Path directory, final int depth)
        throws IOException {
        if (depth > TEST_SEARCH_DEPTH || directory == null || !directory.toFile().isDirectory()) {
            return null;
        }

        final Path[] children;
        try (var stream = Files.list(directory)) {
            children = stream.toArray(Path[]::new);
        }

        for (final Path child : children) {
            if (child.toFile().isFile() && child.getFileName().toString().endsWith(".jar")) {
                if (jarContainsEntry(child, NBT_TAG_CLASS_RESOURCE)) {
                    return child;
                }
            }
        }

        for (final Path child : children) {
            if (child.toFile().isDirectory()) {
                final Path resolved = searchForMinecraftRuntimeJar(child, depth + 1);
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return null;
    }

    private static boolean jarContainsEntry(final Path jarPath, final String entryName)
        throws IOException {
        try (final java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath.toFile())) {
            return jarFile.getEntry(entryName) != null;
        }
    }
}
