package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import java.io.IOException;
import java.io.InputStream;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordCompactHudLayoutTest {

    private static final float TEST_MAGIC_0_0001F = 0.0001F;
    private static final float TEST_MAGIC_135_5F = 135.5F;
    private static final float TEST_MAGIC_88_0F = 88.0F;
    private static final float TEST_MAGIC_72_0F = 72.0F;
    private static final float TEST_MAGIC_65_0F = 65.0F;
    private static final float TEST_MAGIC_24_0F = 24.0F;
    private static final int TEST_MAGIC_8 = 8;
    private static final int TEST_MAGIC_9 = 9;
    private static final int TEST_MAGIC_42 = 42;
    private static final int TEST_MAGIC_100 = 100;
    private static final String TACTICAL_HUD_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalHudOverlay";
    private static final String DISPLAY_DATA_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$SwordDisplayData";
    private static final String RESONANCE_ENUM_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType";
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
    private static Path cachedMinecraftJarPath;

    @Test
    void compactPlanContainsRequiredClustersWithoutLegacyFullRosterLayout() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID benmingUuid = UUID.randomUUID();
        final Object benmingSword = api.newDisplayData(benmingUuid);
        api.setEnumField(benmingSword, "quality", "EMPEROR");
        api.setIntField(benmingSword, "level", TEST_MAGIC_42);
        api.setIntField(benmingSword, "experience", 820);
        api.setFloatField(benmingSword, "expProgress", TEST_MAGIC_88_0F / TEST_MAGIC_100);
        api.setFloatField(benmingSword, "health", TEST_MAGIC_72_0F);
        api.setFloatField(benmingSword, "maxHealth", TEST_MAGIC_100);
        api.setEnumField(benmingSword, "aiMode", "GUARD");
        api.setFloatField(benmingSword, "distance", TEST_MAGIC_24_0F);
        api.setBooleanField(benmingSword, "isBenmingSword", true);
        api.setEnumField(benmingSword, "benmingResonanceType", "OFFENSE");
        api.setFloatField(benmingSword, "overloadPercent", TEST_MAGIC_135_5F);
        api.setBooleanField(benmingSword, "shouldHighlightWarning", true);
        api.setBooleanField(benmingSword, "isOverloadDanger", true);
        api.setBooleanField(benmingSword, "isBurstReady", true);
        api.setBooleanField(benmingSword, "isAftershockPeriod", true);

        final Object compactPlan = api.buildPlan(List.of(benmingSword), benmingUuid);
        assertNotNull(compactPlan);

        final Object headerRow = api.accessor(compactPlan, "headerRow");
        final Object metaRow = api.accessor(compactPlan, "metaRow");
        final Object overloadCluster = api.accessor(compactPlan, "overloadCluster");
        final Object resourceBars = api.accessor(compactPlan, "resourceBars");
        final Object actionHintRow = api.accessor(compactPlan, "actionHintRow");
        assertNotNull(headerRow);
        assertNotNull(metaRow);
        assertNotNull(overloadCluster);
        assertNotNull(resourceBars);
        assertNotNull(actionHintRow);

        assertEquals("皇品飞剑", api.stringAccessor(headerRow, "swordName"));
        assertEquals("烈(攻)", api.stringAccessor(metaRow, "resonanceText"));
        assertEquals("防御", api.stringAccessor(metaRow, "modeText"));
        assertEquals("24.0m", api.stringAccessor(metaRow, "distanceText"));
        assertEquals("烈(攻)", api.stringAccessor(overloadCluster, "resonanceText"));
        assertEquals("载136%", api.stringAccessor(overloadCluster, "overloadText"));
        assertEquals(1.0F, api.floatAccessor(overloadCluster, "fillRatio"), TEST_MAGIC_0_0001F);
        assertEquals(
            List.of("越线将崩", "压线可斩", "锋芒未收"),
            api.badgeTexts(api.accessor(overloadCluster, "priorityAlerts"))
        );

        final Object durabilityBar = api.accessor(resourceBars, "durabilityBar");
        final Object experienceBar = api.accessor(resourceBars, "experienceBar");
        assertEquals("耐久", api.stringAccessor(durabilityBar, "label"));
        assertEquals("72/100", api.stringAccessor(durabilityBar, "valueText"));
        assertEquals(
            TEST_MAGIC_72_0F / TEST_MAGIC_100,
            api.floatAccessor(durabilityBar, "fillRatio"),
            TEST_MAGIC_0_0001F
        );
        assertEquals("经验", api.stringAccessor(experienceBar, "label"));
        assertEquals("Lv.42/650", api.stringAccessor(experienceBar, "valueText"));
        assertEquals(
            TEST_MAGIC_88_0F / TEST_MAGIC_100,
            api.floatAccessor(experienceBar, "fillRatio"),
            TEST_MAGIC_0_0001F
        );

        assertEquals(
            List.of("Z", "X", "C", "V", "G"),
            api.actionKeys(api.accessor(actionHintRow, "actionHints"))
        );
        assertEquals("H", api.stringAccessor(api.accessor(actionHintRow, "hubHint"), "key"));

        assertEquals(1, api.listSize(api.accessor(compactPlan, "miniRosterTraces")));
        assertFalse(api.hasMethod(compactPlan.getClass(), "fullRosterRows"));
        assertFalse(api.hasMethod(compactPlan.getClass(), "entryPlacements"));
        assertFalse(api.hasDeclaredMethod(api.overlayClass(), "buildEntryPlacements", List.class));
        assertThrows(
            ClassNotFoundException.class,
            () -> api.loadClass(TACTICAL_HUD_CLASS_NAME + "$EntryPlacement")
        );
    }

    @Test
    void shouldRenderGuardIgnoresLegacyHudToggleAndOnlyFollowsRuntimeAvailability()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        assertFalse(api.shouldRender(false, false, true));
        assertFalse(api.shouldRender(true, true, true));
        assertFalse(api.shouldRender(true, false, false));
        assertTrue(api.shouldRender(true, false, true));
        assertEquals(
            api.shouldRender(true, false, true),
            api.shouldRenderWithLegacyToggle(true, false, false, true)
        );
        assertEquals(
            api.shouldRender(true, false, true),
            api.shouldRenderWithLegacyToggle(true, false, true, true)
        );
        assertNull(api.buildPlan(List.of(), null));
    }

    @Test
    void benmingSelectedMismatchStillHonorsBenmingFirstEightTraceWindow() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final List<Object> swords = new ArrayList<>();
        for (int index = 0; index < TEST_MAGIC_9; index++) {
            final UUID swordUuid = UUID.randomUUID();
            final Object sword = api.newDisplayData(swordUuid);
            api.setEnumField(sword, "quality", "COMMON");
            api.setIntField(sword, "level", index + 1);
            api.setFloatField(sword, "distance", index + 1.0F);
            swords.add(sword);
        }

        final UUID farSelectedUuid = UUID.randomUUID();
        final Object farSelectedOrdinarySword = swords.get(TEST_MAGIC_8 - 1);
        api.setField(farSelectedOrdinarySword, "uuid", farSelectedUuid);
        api.setBooleanField(farSelectedOrdinarySword, "isSelected", true);

        final Object benmingSword = swords.get(TEST_MAGIC_8);
        api.setBooleanField(benmingSword, "isBenmingSword", true);
        api.setEnumField(benmingSword, "benmingResonanceType", "DEFENSE");
        api.setFloatField(benmingSword, "overloadPercent", TEST_MAGIC_65_0F);

        final Object compactPlan = api.buildPlan(swords, farSelectedUuid);
        assertNotNull(compactPlan);

        final List<?> miniTraces = api.asList(api.accessor(compactPlan, "miniRosterTraces"));
        assertEquals(TEST_MAGIC_8, miniTraces.size());
        assertEquals(1, api.intAccessor(compactPlan, "hiddenTraceCount"));
        assertTrue(api.boolAccessor(miniTraces.get(0), "benming"));
        assertEquals(api.uuidField(benmingSword), api.uuidAccessor(miniTraces.get(0), "swordUuid"));
        assertTrue(api.containsTraceUuid(miniTraces, api.uuidField(benmingSword)));
        assertFalse(api.containsTraceUuid(miniTraces, farSelectedUuid));
    }

    private static final class RuntimeApi {

        private final URLClassLoader loader;
        private final Class<?> tacticalHudClass;
        private final Class<?> displayDataClass;
        private final Class<?> resonanceEnumClass;
        private final Class<?> qualityEnumClass;
        private final Class<?> modeEnumClass;
        private final Method buildCompactHudPlanMethod;
        private final Method shouldRenderTacticalHudMethod;

        private RuntimeApi(
            final URLClassLoader loader,
            final Class<?> tacticalHudClass,
            final Class<?> displayDataClass,
            final Class<?> resonanceEnumClass,
            final Class<?> qualityEnumClass,
            final Class<?> modeEnumClass
        ) throws Exception {
            this.loader = loader;
            this.tacticalHudClass = tacticalHudClass;
            this.displayDataClass = displayDataClass;
            this.resonanceEnumClass = resonanceEnumClass;
            this.qualityEnumClass = qualityEnumClass;
            this.modeEnumClass = modeEnumClass;
            this.buildCompactHudPlanMethod = tacticalHudClass.getDeclaredMethod(
                "buildCompactHudPlan",
                List.class,
                UUID.class
            );
            this.shouldRenderTacticalHudMethod = tacticalHudClass.getDeclaredMethod(
                "shouldRenderTacticalHud",
                boolean.class,
                boolean.class,
                boolean.class
            );
            this.buildCompactHudPlanMethod.setAccessible(true);
            this.shouldRenderTacticalHudMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            return new RuntimeApi(
                loader,
                Class.forName(TACTICAL_HUD_CLASS_NAME, true, loader),
                Class.forName(DISPLAY_DATA_CLASS_NAME, true, loader),
                Class.forName(RESONANCE_ENUM_CLASS_NAME, true, loader),
                Class.forName(QUALITY_ENUM_CLASS_NAME, true, loader),
                Class.forName(MODE_ENUM_CLASS_NAME, true, loader)
            );
        }

        Class<?> overlayClass() {
            return tacticalHudClass;
        }

        Class<?> loadClass(final String className) throws Exception {
            return Class.forName(className, false, loader);
        }

        Object newDisplayData(final UUID uuid) throws Exception {
            final Object displayData = displayDataClass.getConstructor().newInstance();
            setField(displayData, "uuid", uuid);
            return displayData;
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
                case "aiMode" -> modeEnumClass;
                default -> resonanceEnumClass;
            };
            final Object enumValue = enumClass.getMethod("valueOf", String.class).invoke(null, enumName);
            field.set(target, enumValue);
        }

        void setField(final Object target, final String fieldName, final Object value) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            field.set(target, value);
        }

        Object buildPlan(final List<?> swords, final UUID selectedSwordId) throws Exception {
            return buildCompactHudPlanMethod.invoke(null, swords, selectedSwordId);
        }

        boolean shouldRender(
            final boolean hasPlayer,
            final boolean hideGui,
            final boolean hasSwords
        ) throws Exception {
            return (boolean) shouldRenderTacticalHudMethod.invoke(
                null,
                hasPlayer,
                hideGui,
                hasSwords
            );
        }

        boolean shouldRenderWithLegacyToggle(
            final boolean hasPlayer,
            final boolean hideGui,
            final boolean legacyHudEnabled,
            final boolean hasSwords
        ) throws Exception {
            return shouldRender(hasPlayer, hideGui, hasSwords);
        }

        Object accessor(final Object target, final String accessorName) throws Exception {
            final Method accessor = target.getClass().getDeclaredMethod(accessorName);
            accessor.setAccessible(true);
            return accessor.invoke(target);
        }

        String stringAccessor(final Object target, final String accessorName) throws Exception {
            return (String) accessor(target, accessorName);
        }

        int intAccessor(final Object target, final String accessorName) throws Exception {
            return (int) accessor(target, accessorName);
        }

        float floatAccessor(final Object target, final String accessorName) throws Exception {
            return (float) accessor(target, accessorName);
        }

        boolean boolAccessor(final Object target, final String accessorName) throws Exception {
            return (boolean) accessor(target, accessorName);
        }

        UUID uuidAccessor(final Object target, final String accessorName) throws Exception {
            return (UUID) accessor(target, accessorName);
        }

        UUID uuidField(final Object target) throws Exception {
            final Field uuidField = displayDataClass.getField("uuid");
            return (UUID) uuidField.get(target);
        }

        boolean hasMethod(final Class<?> type, final String methodName) {
            try {
                type.getDeclaredMethod(methodName);
                return true;
            } catch (NoSuchMethodException exception) {
                return false;
            }
        }

        boolean hasDeclaredMethod(final Class<?> type, final String methodName, final Class<?>... args) {
            try {
                type.getDeclaredMethod(methodName, args);
                return true;
            } catch (NoSuchMethodException exception) {
                return false;
            }
        }

        List<?> asList(final Object target) {
            return (List<?>) target;
        }

        int listSize(final Object target) {
            return asList(target).size();
        }

        List<String> badgeTexts(final Object badgeListObject) throws Exception {
            final List<String> texts = new ArrayList<>();
            for (Object badge : asList(badgeListObject)) {
                texts.add(stringAccessor(badge, "text"));
            }
            return texts;
        }

        List<String> actionKeys(final Object actionHintListObject) throws Exception {
            final List<String> keys = new ArrayList<>();
            for (Object actionHint : asList(actionHintListObject)) {
                keys.add(stringAccessor(actionHint, "key"));
            }
            return keys;
        }

        boolean containsTraceUuid(final List<?> traces, final UUID swordUuid) throws Exception {
            for (Object trace : traces) {
                if (swordUuid.equals(uuidAccessor(trace, "swordUuid"))) {
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

    private static Path findJarContainingResource(final Path root, final String resource)
        throws IOException {
        if (root == null || !root.toFile().exists()) {
            return null;
        }
        try (var stream = Files.walk(root, TEST_MAGIC_8)) {
            final List<Path> candidates = stream.filter(path -> path.toString().endsWith(".jar")).toList();
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
