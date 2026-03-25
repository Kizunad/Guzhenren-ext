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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHubOverviewPanelTest {

    private static final String HUB_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHubScreen";
    private static final String DISPLAY_DATA_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$SwordDisplayData";
    private static final String PROJECTION_INPUT_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$BenmingPhase2ProjectionInput";
    private static final String QUALITY_ENUM_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality";
    private static final String MODE_ENUM_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordAIMode";
    private static final String PLAYER_CLASS_RESOURCE =
        "net/minecraft/world/entity/player/Player.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");

    private static final int TEST_SEARCH_DEPTH = 6;
    private static final int TEST_MAGIC_8 = 8;
    private static final int TEST_MAGIC_9 = 9;
    private static final int TEST_MAGIC_42 = 42;
    private static final float TEST_MAGIC_72_F = 72.0F;
    private static final float TEST_MAGIC_88_F = 88.0F;
    private static final float TEST_MAGIC_100_F = 100.0F;
    private static final float TEST_MAGIC_120_F = 120.0F;
    private static final long TICK_0 = 0L;
    private static final long TICK_100 = 100L;
    private static final long TICK_180 = 180L;
    private static final long TICK_200 = 200L;
    private static final long TICK_220 = 220L;
    private static final long TICK_260 = 260L;
    private static final String BENMING_STABLE_ID = "stable-benming";

    private static Path cachedMinecraftJarPath;

    @Test
    void emptyRosterBuildsThreeSummaryCardsAndExplicitDisabledReasons() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object plan = api.buildPlan(List.of(), null);

        assertEquals(3, api.listSize(api.accessor(plan, "summaryCards")));
        assertEquals(0, api.listSize(api.accessor(plan, "rosterEntries")));
        final String rosterCaption = api.stringAccessor(api.accessor(plan, "rosterCaption"));
        assertTrue(rosterCaption.contains("当前编队观察"));
        assertTrue(rosterCaption.contains("当前没有可见飞剑"));
        assertEquals(3, api.listSize(api.accessor(plan, "routeCards")));

        final Object focusDetail = api.accessor(plan, "focusDetail");
        assertEquals("当前无可调度飞剑", api.stringAccessor(api.accessor(focusDetail, "title")));
        assertTrue(api.actionDescriptionAt(focusDetail, 0).contains("无法进入本命爆发"));
        assertTrue(api.actionDescriptionAt(focusDetail, 1).contains("先召出飞剑"));
    }

    @Test
    void benmingRosterProjectsSummaryCardsAndBurstReadyReason() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID benmingUuid = UUID.randomUUID();
        final Object benmingSword = api.newDisplayData(benmingUuid);
        api.setEnumField(benmingSword, "quality", "EMPEROR");
        api.setEnumField(benmingSword, "aiMode", "GUARD");
        api.setIntField(benmingSword, "level", TEST_MAGIC_42);
        api.setFloatField(benmingSword, "distance", 12.0F);
        api.setFloatField(benmingSword, "health", TEST_MAGIC_72_F);
        api.setFloatField(benmingSword, "maxHealth", TEST_MAGIC_100_F);
        api.setFloatField(benmingSword, "expProgress", TEST_MAGIC_88_F / TEST_MAGIC_100_F);
        api.projectBenming(
            benmingSword,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "offense",
            TEST_MAGIC_72_F,
            TICK_100,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_200
        );

        final Object plan = api.buildPlan(List.of(benmingSword), benmingUuid);

        final Object benmingSummaryCard = api.listItem(api.accessor(plan, "summaryCards"), 1);
        assertTrue(api.linesContain(api.accessor(benmingSummaryCard, "lines"), "烈(攻)"));
        assertTrue(api.badgesContain(api.accessor(benmingSummaryCard, "badges"), "本命在线"));

        final Object focusDetail = api.accessor(plan, "focusDetail");
        assertEquals("皇品飞剑", api.stringAccessor(api.accessor(focusDetail, "title")));
        assertTrue(api.badgesContain(api.accessor(focusDetail, "statusBadges"), "本命"));
        assertFalse(api.actionDescriptionAt(focusDetail, 0).isBlank());

        final Object currentRecommendation = api.listItem(api.accessor(plan, "routeCards"), 1);
        assertEquals("当前推荐", api.stringAccessor(api.accessor(currentRecommendation, "title")));
        assertTrue(api.linesContain(api.accessor(currentRecommendation, "summaryLines"), "本命")
            || api.linesContain(api.accessor(currentRecommendation, "summaryLines"), "爆发")
            || api.linesContain(api.accessor(currentRecommendation, "summaryLines"), "培养"));
    }

    @Test
    void multiSwordWindowKeepsBenmingFirstAndSelectedOrdinaryOutOfVisibleWindow() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final List<Object> swords = new ArrayList<>();
        for (int index = 0; index < TEST_MAGIC_9; index++) {
            final Object sword = api.newDisplayData(UUID.randomUUID());
            api.setEnumField(sword, "quality", "COMMON");
            api.setEnumField(sword, "aiMode", "ORBIT");
            api.setIntField(sword, "level", index + 1);
            api.setFloatField(sword, "distance", index + 1.0F);
            api.setFloatField(sword, "health", TEST_MAGIC_100_F);
            api.setFloatField(sword, "maxHealth", TEST_MAGIC_100_F);
            swords.add(sword);
        }

        final UUID farSelectedUuid = UUID.randomUUID();
        final Object farSelected = swords.get(TEST_MAGIC_8 - 1);
        api.setField(farSelected, "uuid", farSelectedUuid);
        api.setBooleanField(farSelected, "isSelected", true);

        final Object benmingSword = swords.get(TEST_MAGIC_8);
        api.projectBenming(
            benmingSword,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "defense",
            65.0D,
            TICK_100,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_200
        );

        final Object plan = api.buildPlan(swords, farSelectedUuid);

        assertEquals(TEST_MAGIC_8, api.listSize(api.accessor(plan, "rosterEntries")));
        assertEquals(1, api.intAccessor(api.accessor(plan, "hiddenRosterCount")));

        final Object firstEntry = api.listItem(api.accessor(plan, "rosterEntries"), 0);
        assertEquals(api.uuidField(benmingSword), api.uuidAccessor(api.accessor(firstEntry, "swordUuid")));
        assertFalse(api.rosterContainsUuid(api.accessor(plan, "rosterEntries"), farSelectedUuid));
        assertTrue(api.stringAccessor(api.accessor(plan, "rosterCaption")).contains("本命先入窗"));
    }

    @Test
    void notBenmingFocusShowsExplicitBurstBlockedReason() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID selectedUuid = UUID.randomUUID();
        final UUID benmingUuid = UUID.randomUUID();

        final Object selectedSword = api.newDisplayData(selectedUuid);
        api.setEnumField(selectedSword, "quality", "KING");
        api.setEnumField(selectedSword, "aiMode", "HUNT");
        api.setIntField(selectedSword, "level", 15);
        api.setFloatField(selectedSword, "distance", 4.0F);
        api.setFloatField(selectedSword, "health", TEST_MAGIC_100_F);
        api.setFloatField(selectedSword, "maxHealth", TEST_MAGIC_100_F);
        api.setBooleanField(selectedSword, "isSelected", true);

        final Object benmingSword = api.newDisplayData(benmingUuid);
        api.setEnumField(benmingSword, "quality", "EMPEROR");
        api.setEnumField(benmingSword, "aiMode", "GUARD");
        api.setIntField(benmingSword, "level", 30);
        api.setFloatField(benmingSword, "distance", TEST_MAGIC_120_F);
        api.setFloatField(benmingSword, "health", TEST_MAGIC_72_F);
        api.setFloatField(benmingSword, "maxHealth", TEST_MAGIC_100_F);
        api.projectBenming(
            benmingSword,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "devour",
            10.0D,
            TICK_180,
            TICK_220,
            TICK_260,
            TICK_0,
            TICK_0,
            TICK_200
        );

        final Object plan = api.buildPlan(List.of(selectedSword, benmingSword), selectedUuid);

        final Object focusDetail = api.accessor(plan, "focusDetail");
        assertEquals("皇品飞剑", api.stringAccessor(api.accessor(focusDetail, "title")));
        assertTrue(api.badgesContain(api.accessor(focusDetail, "statusBadges"), "本命"));
        assertTrue(api.actionDescriptionAt(focusDetail, 0).contains("反噬期"));

        final Object currentRecommendation = api.listItem(api.accessor(plan, "routeCards"), 1);
        assertTrue(api.linesContain(api.accessor(currentRecommendation, "summaryLines"), "反噬封锁"));
    }

    private static final class RuntimeApi {

        private final URLClassLoader loader;
        private final Class<?> hubScreenClass;
        private final Class<?> displayDataClass;
        private final Class<?> projectionInputClass;
        private final Class<?> qualityEnumClass;
        private final Class<?> modeEnumClass;
        private final Method buildOverviewPlanMethod;
        private final Method projectMethod;
        private final Constructor<?> projectionInputConstructor;

        private RuntimeApi(
            final URLClassLoader loader,
            final Class<?> hubScreenClass,
            final Class<?> displayDataClass,
            final Class<?> projectionInputClass,
            final Class<?> qualityEnumClass,
            final Class<?> modeEnumClass
        ) throws Exception {
            this.loader = loader;
            this.hubScreenClass = hubScreenClass;
            this.displayDataClass = displayDataClass;
            this.projectionInputClass = projectionInputClass;
            this.qualityEnumClass = qualityEnumClass;
            this.modeEnumClass = modeEnumClass;
            this.buildOverviewPlanMethod = hubScreenClass.getDeclaredMethod(
                "buildOverviewPlanForTesting",
                List.class,
                UUID.class
            );
            final Class<?> hudStateClass = Class.forName(
                "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState",
                true,
                loader
            );
            this.projectMethod = hudStateClass.getDeclaredMethod(
                "projectBenmingPhase2Fields",
                displayDataClass,
                projectionInputClass
            );
            this.projectionInputConstructor = projectionInputClass.getDeclaredConstructor(
                String.class,
                String.class,
                String.class,
                double.class,
                long.class,
                long.class,
                long.class,
                long.class,
                long.class,
                long.class
            );
            this.buildOverviewPlanMethod.setAccessible(true);
            this.projectMethod.setAccessible(true);
            this.projectionInputConstructor.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            return new RuntimeApi(
                loader,
                Class.forName(HUB_SCREEN_CLASS_NAME, true, loader),
                Class.forName(DISPLAY_DATA_CLASS_NAME, true, loader),
                Class.forName(PROJECTION_INPUT_CLASS_NAME, true, loader),
                Class.forName(QUALITY_ENUM_CLASS_NAME, true, loader),
                Class.forName(MODE_ENUM_CLASS_NAME, true, loader)
            );
        }

        Object newDisplayData(final UUID uuid) throws Exception {
            final Object row = displayDataClass.getConstructor().newInstance();
            setField(row, "uuid", uuid);
            return row;
        }

        Object buildPlan(final List<?> rows, final UUID selectedSwordId) throws Exception {
            return buildOverviewPlanMethod.invoke(null, rows, selectedSwordId);
        }

        void projectBenming(
            final Object row,
            final String stableSwordId,
            final String bondedSwordId,
            final String resonanceRaw,
            final double overload,
            final long burstCooldownUntilTick,
            final long overloadBacklashUntilTick,
            final long overloadRecoveryUntilTick,
            final long burstActiveUntilTick,
            final long burstAftershockUntilTick,
            final long gameTick
        ) throws Exception {
            final Object projectionInput = projectionInputConstructor.newInstance(
                stableSwordId,
                bondedSwordId,
                resonanceRaw,
                overload,
                burstCooldownUntilTick,
                overloadBacklashUntilTick,
                overloadRecoveryUntilTick,
                burstActiveUntilTick,
                burstAftershockUntilTick,
                gameTick
            );
            projectMethod.invoke(null, row, projectionInput);
        }

        void setBooleanField(final Object target, final String fieldName, final boolean value)
            throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            field.setBoolean(target, value);
        }

        void setFloatField(final Object target, final String fieldName, final float value)
            throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            field.setFloat(target, value);
        }

        void setIntField(final Object target, final String fieldName, final int value)
            throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            field.setInt(target, value);
        }

        void setEnumField(final Object target, final String fieldName, final String enumName)
            throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            final Class<?> enumClass = switch (fieldName) {
                case "quality" -> qualityEnumClass;
                default -> modeEnumClass;
            };
            final Object enumValue = enumClass.getMethod("valueOf", String.class).invoke(null, enumName);
            field.set(target, enumValue);
        }

        void setField(final Object target, final String fieldName, final Object value) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            field.set(target, value);
        }

        Object accessor(final Object target, final String accessorName) throws Exception {
            if (target instanceof String stringValue) {
                return stringValue;
            }
            final Method method = target.getClass().getDeclaredMethod(accessorName);
            method.setAccessible(true);
            return method.invoke(target);
        }

        String stringAccessor(final Object target) {
            return target == null ? null : target.toString();
        }

        int intAccessor(final Object target) {
            return ((Number) target).intValue();
        }

        int listSize(final Object listObject) {
            return ((List<?>) listObject).size();
        }

        Object listItem(final Object listObject, final int index) {
            return ((List<?>) listObject).get(index);
        }

        boolean linesContain(final Object listObject, final String fragment) {
            for (final Object item : (List<?>) listObject) {
                if (item != null && item.toString().contains(fragment)) {
                    return true;
                }
            }
            return false;
        }

        boolean badgesContain(final Object listObject, final String fragment) throws Exception {
            for (final Object badge : (List<?>) listObject) {
                final Object text = accessor(badge, "text");
                if (text != null && text.toString().contains(fragment)) {
                    return true;
                }
            }
            return false;
        }

        String actionDescriptionAt(final Object focusDetail, final int index) throws Exception {
            final Object actionPlan = listItem(accessor(focusDetail, "actionPlans"), index);
            return stringAccessor(accessor(actionPlan, "description"));
        }

        UUID uuidAccessor(final Object target) {
            return (UUID) target;
        }

        UUID uuidField(final Object target) throws Exception {
            final Field field = displayDataClass.getField("uuid");
            return (UUID) field.get(target);
        }

        boolean rosterContainsUuid(final Object rosterEntries, final UUID uuid) throws Exception {
            for (final Object entry : (List<?>) rosterEntries) {
                final UUID entryUuid = (UUID) accessor(entry, "swordUuid");
                if (uuid.equals(entryUuid)) {
                    return true;
                }
            }
            return false;
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
        return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    }

    private static synchronized Path resolveMinecraftRuntimeJar() throws IOException {
        if (cachedMinecraftJarPath != null && cachedMinecraftJarPath.toFile().exists()) {
            return cachedMinecraftJarPath;
        }

        final List<Path> searchRoots = new ArrayList<>();
        final String userHome = System.getProperty("user.home");
        searchRoots.add(
            Path.of(userHome, ".gradle", "caches", "neoformruntime", "intermediate_results")
        );
        searchRoots.add(
            Path.of(userHome, ".gradle", "caches", "fabric-loom", "minecraftMaven")
        );

        for (Path root : searchRoots) {
            final Path matched = findJarContainingResource(root, PLAYER_CLASS_RESOURCE);
            if (matched != null) {
                cachedMinecraftJarPath = matched;
                return matched;
            }
        }

        throw new IOException("未找到包含 net.minecraft.world.entity.player.Player 的运行时 Jar");
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

    private static boolean jarContainsResource(final Path jarPath, final String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
