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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordTacticalStateProjectionTests {

    private static final String HUD_STATE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState";
    private static final String TACTICAL_SERVICE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService";
    private static final String DISPLAY_DATA_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$SwordDisplayData";
    private static final String PROJECTION_INPUT_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$BenmingPhase2ProjectionInput";
    private static final String PLAYER_CLASS_RESOURCE =
        "net/minecraft/world/entity/player/Player.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");

    private static final String BENMING_STABLE_ID = "stable-benming";
    private static final String OTHER_STABLE_ID = "stable-other";
    private static final float ASSERT_DELTA = 0.0001F;
    private static final float OVERLOAD_WARNING = 80.0F;
    private static final float OVERLOAD_DANGER = 100.0F;

    private static final long TICK_0 = 0L;
    private static final long TICK_100 = 100L;
    private static final long TICK_180 = 180L;
    private static final long TICK_200 = 200L;
    private static final long TICK_220 = 220L;
    private static final long TICK_260 = 260L;

    private static final int TEST_MAGIC_8 = 8;
    private static final int TEST_MAGIC_9 = 9;
    private static Path cachedMinecraftJarPath;

    @Test
    void emptyRosterBuildsNeutralSnapshot() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object snapshot = api.snapshot(List.of(), null);

        assertEquals("NONE", api.focusSource(snapshot));
        assertNull(api.focusSwordUuid(snapshot));
        assertEquals(0, api.squadCount(snapshot, "totalCount"));
        assertEquals(0, api.squadCount(snapshot, "benmingCount"));
        assertEquals(0, api.squadCount(snapshot, "selectedCount"));
        assertNull(api.squadBenmingSummary(snapshot));
        assertEquals(0, api.hubVisibleWindow(snapshot).size());
        assertFalse(api.hubBenmingInsideWindow(snapshot));
        assertFalse(api.helpSignal(snapshot, "hasBenmingSword"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadWarning"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadDanger"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadBacklash"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadRecovery"));
        assertFalse(api.helpSignal(snapshot, "hasAftershock"));
        assertFalse(api.helpSignal(snapshot, "hasBurstReady"));
    }

    @Test
    void focusPriorityPrefersBenmingOverSelectedAndRecent() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID recentUuid = UUID.randomUUID();
        final UUID selectedUuid = UUID.randomUUID();
        final UUID benmingUuid = UUID.randomUUID();

        final Object recentRow = api.newDisplayData(recentUuid);
        final Object selectedRow = api.newDisplayData(selectedUuid);
        final Object benmingRow = api.newDisplayData(benmingUuid);
        api.projectBenming(
            benmingRow,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "defense",
            10.0D,
            TICK_180,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_200
        );

        final Object benmingFirstSnapshot = api.snapshot(
            List.of(recentRow, selectedRow, benmingRow),
            selectedUuid
        );
        assertEquals("BENMING", api.focusSource(benmingFirstSnapshot));
        assertEquals(benmingUuid, api.focusSwordUuid(benmingFirstSnapshot));

        final Object selectedSnapshot = api.snapshot(
            List.of(recentRow, selectedRow),
            selectedUuid
        );
        assertEquals("SELECTED", api.focusSource(selectedSnapshot));
        assertEquals(selectedUuid, api.focusSwordUuid(selectedSnapshot));

        final Object recentSnapshot = api.snapshot(List.of(recentRow), null);
        assertEquals("RECENT", api.focusSource(recentSnapshot));
        assertEquals(recentUuid, api.focusSwordUuid(recentSnapshot));
    }

    @Test
    void onlySelectedRosterKeepsNoBenmingSummary() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID selectedUuid = UUID.randomUUID();
        final Object selectedRow = api.newDisplayData(selectedUuid);

        final Object snapshot = api.snapshot(List.of(selectedRow), selectedUuid);

        assertEquals("SELECTED", api.focusSource(snapshot));
        assertEquals(selectedUuid, api.focusSwordUuid(snapshot));
        assertEquals(1, api.squadCount(snapshot, "totalCount"));
        assertEquals(0, api.squadCount(snapshot, "benmingCount"));
        assertEquals(1, api.squadCount(snapshot, "selectedCount"));
        assertNull(api.squadBenmingSummary(snapshot));
        assertFalse(api.helpSignal(snapshot, "hasBenmingSword"));
    }

    @Test
    void overloadWarningAndDangerSignalsFollowBenmingProjection() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object warningRow = api.newDisplayData(UUID.randomUUID());
        api.projectBenming(
            warningRow,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "offense",
            OVERLOAD_WARNING,
            TICK_180,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_200
        );
        final Object warningSnapshot = api.snapshot(List.of(warningRow), null);
        final Object warningSummary = api.squadBenmingSummary(warningSnapshot);
        assertNotNull(warningSummary);
        assertEquals(
            OVERLOAD_WARNING,
            api.floatAccessor(warningSummary, "overloadPercent"),
            ASSERT_DELTA
        );
        assertTrue(api.boolAccessor(warningSummary, "highlightWarning"));
        assertFalse(api.boolAccessor(warningSummary, "overloadDanger"));
        assertTrue(api.helpSignal(warningSnapshot, "hasOverloadWarning"));
        assertFalse(api.helpSignal(warningSnapshot, "hasOverloadDanger"));

        final Object dangerRow = api.newDisplayData(UUID.randomUUID());
        api.projectBenming(
            dangerRow,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "offense",
            OVERLOAD_DANGER,
            TICK_180,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_200
        );
        final Object dangerSnapshot = api.snapshot(List.of(dangerRow), null);
        final Object dangerSummary = api.squadBenmingSummary(dangerSnapshot);
        assertNotNull(dangerSummary);
        assertEquals(
            OVERLOAD_DANGER,
            api.floatAccessor(dangerSummary, "overloadPercent"),
            ASSERT_DELTA
        );
        assertTrue(api.boolAccessor(dangerSummary, "highlightWarning"));
        assertTrue(api.boolAccessor(dangerSummary, "overloadDanger"));
        assertTrue(api.helpSignal(dangerSnapshot, "hasOverloadWarning"));
        assertTrue(api.helpSignal(dangerSnapshot, "hasOverloadDanger"));
    }

    @Test
    void backlashRecoveryAftershockAndBurstReadySignalsRemainDistinct() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object backlashRow = api.newDisplayData(UUID.randomUUID());
        api.projectBenming(
            backlashRow,
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
        final Object backlashSnapshot = api.snapshot(List.of(backlashRow), null);
        assertTrue(api.helpSignal(backlashSnapshot, "hasOverloadBacklash"));
        assertFalse(api.helpSignal(backlashSnapshot, "hasOverloadRecovery"));
        assertFalse(api.helpSignal(backlashSnapshot, "hasAftershock"));
        assertFalse(api.helpSignal(backlashSnapshot, "hasBurstReady"));

        final Object recoveryRow = api.newDisplayData(UUID.randomUUID());
        api.projectBenming(
            recoveryRow,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "devour",
            10.0D,
            TICK_180,
            TICK_200,
            TICK_260,
            TICK_0,
            TICK_0,
            TICK_220
        );
        final Object recoverySnapshot = api.snapshot(List.of(recoveryRow), null);
        assertFalse(api.helpSignal(recoverySnapshot, "hasOverloadBacklash"));
        assertTrue(api.helpSignal(recoverySnapshot, "hasOverloadRecovery"));
        assertFalse(api.helpSignal(recoverySnapshot, "hasAftershock"));
        assertTrue(api.helpSignal(recoverySnapshot, "hasBurstReady"));

        final Object aftershockRow = api.newDisplayData(UUID.randomUUID());
        api.projectBenming(
            aftershockRow,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "spirit",
            10.0D,
            TICK_180,
            TICK_0,
            TICK_0,
            TICK_200,
            TICK_260,
            TICK_220
        );
        final Object aftershockSnapshot = api.snapshot(List.of(aftershockRow), null);
        assertFalse(api.helpSignal(aftershockSnapshot, "hasOverloadBacklash"));
        assertFalse(api.helpSignal(aftershockSnapshot, "hasOverloadRecovery"));
        assertTrue(api.helpSignal(aftershockSnapshot, "hasAftershock"));
        assertFalse(api.helpSignal(aftershockSnapshot, "hasBurstReady"));

        final Object burstReadyRow = api.newDisplayData(UUID.randomUUID());
        api.projectBenming(
            burstReadyRow,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "defense",
            10.0D,
            TICK_100,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_200
        );
        final Object burstReadySnapshot = api.snapshot(List.of(burstReadyRow), null);
        assertFalse(api.helpSignal(burstReadySnapshot, "hasOverloadBacklash"));
        assertFalse(api.helpSignal(burstReadySnapshot, "hasOverloadRecovery"));
        assertFalse(api.helpSignal(burstReadySnapshot, "hasAftershock"));
        assertTrue(api.helpSignal(burstReadySnapshot, "hasBurstReady"));
    }

    @Test
    void benmingSelectedMismatchStillUsesBenmingFocusAndWindowPolicy() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final List<Object> rows = new ArrayList<>();
        for (int index = 0; index < TEST_MAGIC_9; index++) {
            rows.add(api.newDisplayData(UUID.randomUUID()));
        }

        final Object benmingRow = rows.get(TEST_MAGIC_8);
        api.projectBenming(
            benmingRow,
            BENMING_STABLE_ID,
            BENMING_STABLE_ID,
            "defense",
            10.0D,
            TICK_100,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_0,
            TICK_200
        );
        final UUID farSelectedOrdinaryUuid = UUID.randomUUID();
        api.setField(rows.get(TEST_MAGIC_8 - 1), "uuid", farSelectedOrdinaryUuid);

        final Object snapshot = api.snapshot(rows, farSelectedOrdinaryUuid);
        assertEquals("BENMING", api.focusSource(snapshot));
        assertEquals(api.field(benmingRow, "uuid"), api.focusSwordUuid(snapshot));
        assertTrue(api.hubBenmingInsideWindow(snapshot));

        final List<?> visibleWindow = api.hubVisibleWindow(snapshot);
        assertEquals(TEST_MAGIC_8, visibleWindow.size());
        assertEquals(api.field(benmingRow, "uuid"), api.viewUuid(visibleWindow.get(0)));
        assertFalse(api.windowContainsUuid(visibleWindow, farSelectedOrdinaryUuid));
    }

    @Test
    void noBenmingWithBondedMismatchKeepsHelpSignalsNeutral() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID rowUuid = UUID.randomUUID();
        final Object mismatchRow = api.newDisplayData(rowUuid);

        api.projectBenming(
            mismatchRow,
            OTHER_STABLE_ID,
            BENMING_STABLE_ID,
            "spirit",
            OVERLOAD_WARNING,
            TICK_100,
            TICK_220,
            TICK_260,
            TICK_200,
            TICK_260,
            TICK_220
        );

        final Object snapshot = api.snapshot(List.of(mismatchRow), null);

        assertEquals("RECENT", api.focusSource(snapshot));
        assertEquals(rowUuid, api.focusSwordUuid(snapshot));
        assertEquals(0, api.squadCount(snapshot, "benmingCount"));
        assertNull(api.squadBenmingSummary(snapshot));
        assertFalse(api.helpSignal(snapshot, "hasBenmingSword"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadWarning"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadDanger"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadBacklash"));
        assertFalse(api.helpSignal(snapshot, "hasOverloadRecovery"));
        assertFalse(api.helpSignal(snapshot, "hasAftershock"));
        assertFalse(api.helpSignal(snapshot, "hasBurstReady"));
    }

    private static final class RuntimeApi {

        private final Class<?> displayDataClass;
        private final Class<?> projectionInputClass;
        private final Method projectMethod;
        private final Method snapshotMethod;
        private final Constructor<?> projectionInputConstructor;

        private RuntimeApi(
            Class<?> hudStateClass,
            Class<?> tacticalStateServiceClass,
            Class<?> displayDataClass,
            Class<?> projectionInputClass
        ) throws Exception {
            this.displayDataClass = displayDataClass;
            this.projectionInputClass = projectionInputClass;
            this.projectMethod = hudStateClass.getDeclaredMethod(
                "projectBenmingPhase2Fields",
                displayDataClass,
                projectionInputClass
            );
            this.projectMethod.setAccessible(true);
            this.snapshotMethod = tacticalStateServiceClass.getMethod(
                "snapshotFromRoster",
                List.class,
                UUID.class
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
            this.projectionInputConstructor.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> hudStateClass = Class.forName(HUD_STATE_CLASS_NAME, true, loader);
            final Class<?> tacticalStateServiceClass = Class.forName(
                TACTICAL_SERVICE_CLASS_NAME,
                true,
                loader
            );
            final Class<?> displayDataClass = Class.forName(DISPLAY_DATA_CLASS_NAME, true, loader);
            final Class<?> projectionInputClass = Class.forName(
                PROJECTION_INPUT_CLASS_NAME,
                true,
                loader
            );
            return new RuntimeApi(
                hudStateClass,
                tacticalStateServiceClass,
                displayDataClass,
                projectionInputClass
            );
        }

        Object newDisplayData(UUID uuid) throws Exception {
            final Object row = displayDataClass.getConstructor().newInstance();
            setField(row, "uuid", uuid);
            return row;
        }

        void projectBenming(
            Object row,
            String stableSwordId,
            String bondedSwordId,
            String resonanceRaw,
            double overload,
            long burstCooldownUntilTick,
            long overloadBacklashUntilTick,
            long overloadRecoveryUntilTick,
            long burstActiveUntilTick,
            long burstAftershockUntilTick,
            long gameTick
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
            projectMethod.invoke(
                null,
                row,
                projectionInput
            );
        }

        Object snapshot(List<Object> rows, UUID selectedSwordId) throws Exception {
            return snapshotMethod.invoke(null, rows, selectedSwordId);
        }

        Object focusSword(Object snapshot) throws Exception {
            return accessor(snapshot, "focusSword");
        }

        String focusSource(Object snapshot) throws Exception {
            final Object focusSword = focusSword(snapshot);
            final Object source = accessor(focusSword, "source");
            return enumName(source);
        }

        UUID focusSwordUuid(Object snapshot) throws Exception {
            final Object focusSword = focusSword(snapshot);
            final Object sword = accessor(focusSword, "sword");
            if (sword == null) {
                return null;
            }
            return (UUID) accessor(sword, "uuid");
        }

        int squadCount(Object snapshot, String accessorName) throws Exception {
            return (int) accessor(accessor(snapshot, "squadSummary"), accessorName);
        }

        Object squadBenmingSummary(Object snapshot) throws Exception {
            return accessor(accessor(snapshot, "squadSummary"), "benmingSummary");
        }

        boolean helpSignal(Object snapshot, String accessorName) throws Exception {
            return (boolean) accessor(accessor(snapshot, "helpSignals"), accessorName);
        }

        List<?> hubVisibleWindow(Object snapshot) throws Exception {
            final Object hubOverview = accessor(snapshot, "hubOverview");
            final Object visibleWindow = accessor(hubOverview, "visibleDisplayWindow");
            assertTrue(visibleWindow instanceof List<?>);
            return (List<?>) visibleWindow;
        }

        boolean hubBenmingInsideWindow(Object snapshot) throws Exception {
            return (boolean) accessor(accessor(snapshot, "hubOverview"), "benmingInsideWindow");
        }

        UUID viewUuid(Object view) throws Exception {
            return (UUID) accessor(view, "uuid");
        }

        boolean windowContainsUuid(List<?> window, UUID uuid) throws Exception {
            for (Object view : window) {
                if (uuid.equals(viewUuid(view))) {
                    return true;
                }
            }
            return false;
        }

        float floatAccessor(Object target, String accessorName) throws Exception {
            return (float) accessor(target, accessorName);
        }

        boolean boolAccessor(Object target, String accessorName) throws Exception {
            return (boolean) accessor(target, accessorName);
        }

        Object field(Object target, String fieldName) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            return field.get(target);
        }

        void setField(Object target, String fieldName, Object value) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            field.set(target, value);
        }

        private Object accessor(Object target, String accessorName) throws Exception {
            if (target == null) {
                return null;
            }
            return target.getClass().getMethod(accessorName).invoke(target);
        }

        private String enumName(Object enumValue) throws Exception {
            return (String) enumValue.getClass().getMethod("name").invoke(enumValue);
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

        final Path minecraftJarPath = resolveMinecraftRuntimeJar();
        urls.add(minecraftJarPath.toUri().toURL());

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

    private static Path findJarContainingResource(Path root, String resource) throws IOException {
        if (root == null || !root.toFile().exists()) {
            return null;
        }

        try (var stream = Files.walk(root, TEST_MAGIC_8)) {
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

    private static boolean jarContainsResource(Path jarPath, String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
