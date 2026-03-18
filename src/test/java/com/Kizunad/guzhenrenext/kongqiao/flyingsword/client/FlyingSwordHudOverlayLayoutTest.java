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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHudOverlayLayoutTest {

    private static final String HUD_OVERLAY_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudOverlay";
    private static final String DISPLAY_DATA_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$SwordDisplayData";
    private static final String RESONANCE_ENUM_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType";
    private static final String PLAYER_CLASS_RESOURCE =
        "net/minecraft/world/entity/player/Player.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static Path cachedMinecraftJarPath;

    @Test
    void benmingPlanShowsImmediateStateWhileNormalRowStaysCompact() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object benmingSword = api.newDisplayData();
        api.setBooleanField(benmingSword, "isBenmingSword", true);
        api.setEnumField(benmingSword, "benmingResonanceType", "OFFENSE");
        api.setFloatField(benmingSword, "overloadPercent", 135.5F);
        api.setBooleanField(benmingSword, "shouldHighlightWarning", true);
        api.setBooleanField(benmingSword, "isBurstReady", true);
        api.setBooleanField(benmingSword, "isAftershockPeriod", true);

        final Object benmingPlan = api.buildRenderPlan(benmingSword);
        assertTrue(api.getPlanBoolean(benmingPlan, "benmingEnhanced"));
        assertEquals("本命", api.getPlanString(benmingPlan, "markerText"));
        assertEquals("烈(攻)", api.getPlanString(benmingPlan, "resonanceText"));
        assertTrue(api.getPlanBoolean(benmingPlan, "showStatusRow"));
        assertTrue(api.getPlanBoolean(benmingPlan, "showOverloadRow"));
        assertEquals("载136%", api.getPlanString(benmingPlan, "overloadText"));
        assertEquals(1.0F, api.getPlanFloat(benmingPlan, "overloadFillRatio"), 0.0001F);
        assertTrue(api.getPlanBoolean(benmingPlan, "overloadDanger"));
        assertEquals(
            api.getIntConstant("BENMING_STATUS_ENTRY_HEIGHT"),
            api.getPlanInt(benmingPlan, "entryHeight")
        );
        assertEquals(
            List.of("过载危", "可爆", "余震"),
            api.getStatusBadgeTexts(benmingPlan)
        );

        final Object normalSword = api.newDisplayData();
        api.setEnumField(normalSword, "benmingResonanceType", "SPIRIT");
        api.setFloatField(normalSword, "overloadPercent", 88.0F);
        api.setBooleanField(normalSword, "shouldHighlightWarning", true);
        api.setBooleanField(normalSword, "isBurstReady", true);
        api.setBooleanField(normalSword, "isAftershockPeriod", true);

        final Object normalPlan = api.buildRenderPlan(normalSword);
        assertFalse(api.getPlanBoolean(normalPlan, "benmingEnhanced"));
        assertEquals("", api.getPlanString(normalPlan, "markerText"));
        assertEquals("", api.getPlanString(normalPlan, "resonanceText"));
        assertFalse(api.getPlanBoolean(normalPlan, "showStatusRow"));
        assertFalse(api.getPlanBoolean(normalPlan, "showOverloadRow"));
        assertEquals("", api.getPlanString(normalPlan, "overloadText"));
        assertEquals(0.0F, api.getPlanFloat(normalPlan, "overloadFillRatio"), 0.0001F);
        assertEquals(api.getIntConstant("NORMAL_ENTRY_HEIGHT"), api.getPlanInt(normalPlan, "entryHeight"));
        assertTrue(api.getStatusBadgeTexts(normalPlan).isEmpty());
    }

    @Test
    void placementsOnlyExpandBenmingRowAndPushFollowingRowsDown() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object normalFront = api.newDisplayData();
        final Object benmingSword = api.newDisplayData();
        api.setBooleanField(benmingSword, "isBenmingSword", true);
        api.setEnumField(benmingSword, "benmingResonanceType", "DEFENSE");
        api.setFloatField(benmingSword, "overloadPercent", 75.0F);
        api.setBooleanField(benmingSword, "shouldHighlightWarning", true);
        final Object normalBack = api.newDisplayData();
        api.setEnumField(normalBack, "benmingResonanceType", "SPIRIT");
        api.setFloatField(normalBack, "overloadPercent", 65.0F);
        api.setBooleanField(normalBack, "isBurstReady", true);

        final List<?> placements = api.buildPlacements(List.of(normalFront, benmingSword, normalBack));
        assertEquals(3, placements.size());

        final int marginTop = api.getIntConstant("MARGIN_TOP");
        final int spacing = api.getIntConstant("ENTRY_SPACING");
        final int normalHeight = api.getIntConstant("NORMAL_ENTRY_HEIGHT");
        final int benmingHeight = api.getIntConstant("BENMING_STATUS_ENTRY_HEIGHT");

        assertEquals(marginTop, api.getPlacementY(placements.get(0)));
        assertEquals(marginTop + normalHeight + spacing, api.getPlacementY(placements.get(1)));
        assertEquals(
            marginTop + normalHeight + spacing + benmingHeight + spacing,
            api.getPlacementY(placements.get(2))
        );

        assertFalse(api.getPlacementPlanBoolean(placements.get(0), "benmingEnhanced"));
        assertTrue(api.getPlacementPlanBoolean(placements.get(1), "benmingEnhanced"));
        assertTrue(api.getPlacementPlanBoolean(placements.get(1), "showOverloadRow"));
        assertFalse(api.getPlacementPlanBoolean(placements.get(2), "benmingEnhanced"));
        assertFalse(api.getPlacementPlanBoolean(placements.get(2), "showStatusRow"));
        assertFalse(api.getPlacementPlanBoolean(placements.get(2), "showOverloadRow"));
    }

    @Test
    void nearOverloadWarningStaysVisibleInBenmingHudPlan() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object benmingSword = api.newDisplayData();
        api.setBooleanField(benmingSword, "isBenmingSword", true);
        api.setEnumField(benmingSword, "benmingResonanceType", "DEFENSE");
        api.setFloatField(benmingSword, "overloadPercent", 100.0F);
        api.setBooleanField(benmingSword, "shouldHighlightWarning", true);

        final Object plan = api.buildRenderPlan(benmingSword);
        assertTrue(api.getPlanBoolean(plan, "benmingEnhanced"));
        assertTrue(api.getPlanBoolean(plan, "showStatusRow"));
        assertTrue(api.getPlanBoolean(plan, "showOverloadRow"));
        assertTrue(api.getPlanBoolean(plan, "overloadDanger"));
        assertEquals("载100%", api.getPlanString(plan, "overloadText"));
        assertTrue(api.getStatusBadgeTexts(plan).contains("过载危"));
        assertEquals(
            api.getIntConstant("BENMING_STATUS_ENTRY_HEIGHT"),
            api.getPlanInt(plan, "entryHeight")
        );
    }

    private static final class RuntimeApi {

        private final Class<?> overlayClass;
        private final Class<?> displayDataClass;
        private final Class<?> resonanceEnumClass;
        private final Method buildRenderPlanMethod;
        private final Method buildPlacementsMethod;

        private RuntimeApi(
            final Class<?> overlayClass,
            final Class<?> displayDataClass,
            final Class<?> resonanceEnumClass
        ) throws Exception {
            this.overlayClass = overlayClass;
            this.displayDataClass = displayDataClass;
            this.resonanceEnumClass = resonanceEnumClass;
            this.buildRenderPlanMethod = overlayClass.getDeclaredMethod(
                "buildEntryRenderPlan",
                displayDataClass
            );
            this.buildPlacementsMethod = overlayClass.getDeclaredMethod(
                "buildEntryPlacements",
                List.class
            );
            this.buildRenderPlanMethod.setAccessible(true);
            this.buildPlacementsMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> overlayClass = Class.forName(HUD_OVERLAY_CLASS_NAME, true, loader);
            final Class<?> displayDataClass = Class.forName(DISPLAY_DATA_CLASS_NAME, true, loader);
            final Class<?> resonanceEnumClass = Class.forName(RESONANCE_ENUM_CLASS_NAME, true, loader);
            return new RuntimeApi(overlayClass, displayDataClass, resonanceEnumClass);
        }

        Object newDisplayData() throws Exception {
            return displayDataClass.getConstructor().newInstance();
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

        void setEnumField(final Object target, final String fieldName, final String enumName)
            throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            final Object enumValue = resonanceEnumClass
                .getMethod("valueOf", String.class)
                .invoke(null, enumName);
            field.set(target, enumValue);
        }

        Object buildRenderPlan(final Object displayData) throws Exception {
            return buildRenderPlanMethod.invoke(null, displayData);
        }

        List<?> buildPlacements(final List<?> swords) throws Exception {
            return (List<?>) buildPlacementsMethod.invoke(null, swords);
        }

        int getIntConstant(final String fieldName) throws Exception {
            final Field field = overlayClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(null);
        }

        boolean getPlanBoolean(final Object plan, final String accessorName) throws Exception {
            return (boolean) invokeAccessor(plan, accessorName);
        }

        int getPlanInt(final Object plan, final String accessorName) throws Exception {
            return (int) invokeAccessor(plan, accessorName);
        }

        float getPlanFloat(final Object plan, final String accessorName) throws Exception {
            return (float) invokeAccessor(plan, accessorName);
        }

        String getPlanString(final Object plan, final String accessorName) throws Exception {
            return (String) invokeAccessor(plan, accessorName);
        }

        List<String> getStatusBadgeTexts(final Object plan) throws Exception {
            final List<?> badges = (List<?>) invokeAccessor(plan, "statusBadges");
            final List<String> texts = new ArrayList<>();
            for (Object badge : badges) {
                texts.add((String) invokeAccessor(badge, "text"));
            }
            return texts;
        }

        int getPlacementY(final Object placement) throws Exception {
            return (int) invokeAccessor(placement, "y");
        }

        boolean getPlacementPlanBoolean(final Object placement, final String accessorName)
            throws Exception {
            final Object plan = invokeAccessor(placement, "renderPlan");
            return getPlanBoolean(plan, accessorName);
        }

        private Object invokeAccessor(final Object target, final String accessorName)
            throws Exception {
            final Method accessor = target.getClass().getDeclaredMethod(accessorName);
            accessor.setAccessible(true);
            return accessor.invoke(target);
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

    private static boolean jarContainsResource(final Path jarPath, final String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
