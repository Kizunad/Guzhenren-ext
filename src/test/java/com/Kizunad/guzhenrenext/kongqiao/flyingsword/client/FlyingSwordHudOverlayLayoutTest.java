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


    private static final float TEST_MAGIC_135_5F = 135.5F;
    private static final float TEST_MAGIC_0_0001F = 0.0001F;
    private static final float TEST_MAGIC_88_0F = 88.0F;
    private static final float TEST_MAGIC_85_0F = 85.0F;
    private static final float TEST_MAGIC_75_0F = 75.0F;
    private static final float TEST_MAGIC_65_0F = 65.0F;
    private static final int TEST_MAGIC_3 = 3;
    private static final int TEST_MAGIC_4 = 4;
    private static final float TEST_MAGIC_100_0F = 100.0F;
    private static final int TEST_MAGIC_6 = 6;

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
        api.setFloatField(benmingSword, "overloadPercent", TEST_MAGIC_135_5F);
        api.setBooleanField(benmingSword, "shouldHighlightWarning", true);
        api.setBooleanField(benmingSword, "isOverloadDanger", true);
        api.setBooleanField(benmingSword, "isBurstReady", true);
        api.setBooleanField(benmingSword, "isAftershockPeriod", true);

        final Object benmingPlan = api.buildRenderPlan(benmingSword);
        assertTrue(api.getPlanBoolean(benmingPlan, "benmingEnhanced"));
        assertEquals("本命", api.getPlanString(benmingPlan, "markerText"));
        assertEquals("烈(攻)", api.getPlanString(benmingPlan, "resonanceText"));
        assertTrue(api.getPlanBoolean(benmingPlan, "showStatusRow"));
        assertTrue(api.getPlanBoolean(benmingPlan, "showOverloadRow"));
        assertEquals("载136%", api.getPlanString(benmingPlan, "overloadText"));
        assertEquals(1.0F, api.getPlanFloat(benmingPlan, "overloadFillRatio"), TEST_MAGIC_0_0001F);
        assertTrue(api.getPlanBoolean(benmingPlan, "overloadDanger"));
        assertEquals(
            api.getIntConstant("BENMING_STATUS_ENTRY_HEIGHT"),
            api.getPlanInt(benmingPlan, "entryHeight")
        );
        assertEquals(
            List.of("越线将崩", "压线可斩", "锋芒未收"),
            api.getStatusBadgeTexts(benmingPlan)
        );

        final Object normalSword = api.newDisplayData();
        api.setEnumField(normalSword, "benmingResonanceType", "SPIRIT");
        api.setFloatField(normalSword, "overloadPercent", TEST_MAGIC_88_0F);
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
        assertEquals(0.0F, api.getPlanFloat(normalPlan, "overloadFillRatio"), TEST_MAGIC_0_0001F);
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
        api.setFloatField(benmingSword, "overloadPercent", TEST_MAGIC_85_0F);
        api.setBooleanField(benmingSword, "shouldHighlightWarning", true);
        final Object normalBack = api.newDisplayData();
        api.setEnumField(normalBack, "benmingResonanceType", "SPIRIT");
        api.setFloatField(normalBack, "overloadPercent", TEST_MAGIC_65_0F);
        api.setBooleanField(normalBack, "isBurstReady", true);

        final List<?> placements = api.buildPlacements(List.of(normalFront, benmingSword, normalBack));
        assertEquals(TEST_MAGIC_3, placements.size());

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
    void preWarningAndDangerUseDifferentBenmingHudBadges() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object preWarningSword = api.newDisplayData();
        api.setBooleanField(preWarningSword, "isBenmingSword", true);
        api.setEnumField(preWarningSword, "benmingResonanceType", "DEFENSE");
        api.setFloatField(preWarningSword, "overloadPercent", TEST_MAGIC_85_0F);
        api.setBooleanField(preWarningSword, "shouldHighlightWarning", true);

        final Object preWarningPlan = api.buildRenderPlan(preWarningSword);
        assertTrue(api.getPlanBoolean(preWarningPlan, "benmingEnhanced"));
        assertTrue(api.getPlanBoolean(preWarningPlan, "showStatusRow"));
        assertTrue(api.getPlanBoolean(preWarningPlan, "showOverloadRow"));
        assertFalse(api.getPlanBoolean(preWarningPlan, "overloadDanger"));
        assertEquals("载85%", api.getPlanString(preWarningPlan, "overloadText"));
        assertTrue(api.getStatusBadgeTexts(preWarningPlan).contains("过载警"));
        assertEquals(
            api.getIntConstant("BENMING_STATUS_ENTRY_HEIGHT"),
            api.getPlanInt(preWarningPlan, "entryHeight")
        );

        final Object dangerSword = api.newDisplayData();
        api.setBooleanField(dangerSword, "isBenmingSword", true);
        api.setEnumField(dangerSword, "benmingResonanceType", "DEFENSE");
        api.setFloatField(dangerSword, "overloadPercent", TEST_MAGIC_100_0F);
        api.setBooleanField(dangerSword, "shouldHighlightWarning", true);
        api.setBooleanField(dangerSword, "isOverloadDanger", true);

        final Object dangerPlan = api.buildRenderPlan(dangerSword);
        assertTrue(api.getPlanBoolean(dangerPlan, "overloadDanger"));
        assertEquals("载100%", api.getPlanString(dangerPlan, "overloadText"));
        assertTrue(api.getStatusBadgeTexts(dangerPlan).contains("稳流将断"));
    }

    @Test
    void routeSpecificBurstAndAftershockBadgesStayDistinctAcrossMvpRoutes()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object defenseSword = api.newDisplayData();
        api.setBooleanField(defenseSword, "isBenmingSword", true);
        api.setEnumField(defenseSword, "benmingResonanceType", "DEFENSE");
        api.setBooleanField(defenseSword, "isBurstReady", true);
        api.setBooleanField(defenseSword, "isAftershockPeriod", true);

        final Object defensePlan = api.buildRenderPlan(defenseSword);
        assertEquals("稳(御)", api.getPlanString(defensePlan, "resonanceText"));
        assertEquals(
            List.of("镇域成形", "余稳回流"),
            api.getStatusBadgeTexts(defensePlan)
        );

        final Object spiritSword = api.newDisplayData();
        api.setBooleanField(spiritSword, "isBenmingSword", true);
        api.setEnumField(spiritSword, "benmingResonanceType", "SPIRIT");
        api.setBooleanField(spiritSword, "isBurstReady", true);
        api.setBooleanField(spiritSword, "isAftershockPeriod", true);

        final Object spiritPlan = api.buildRenderPlan(spiritSword);
        assertEquals("巧(灵)", api.getPlanString(spiritPlan, "resonanceText"));
        assertEquals(
            List.of("抢拍得势", "回身整拍"),
            api.getStatusBadgeTexts(spiritPlan)
        );
    }

    @Test
    void overloadLoopBadgesStayDistinctFromAftershockBadge() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object backlashSword = api.newDisplayData();
        api.setBooleanField(backlashSword, "isBenmingSword", true);
        api.setEnumField(backlashSword, "benmingResonanceType", "DEVOUR");
        api.setBooleanField(backlashSword, "isBurstReady", false);
        api.setBooleanField(backlashSword, "isOverloadBacklashActive", true);

        final Object backlashPlan = api.buildRenderPlan(backlashSword);
        assertEquals(List.of("反噬"), api.getStatusBadgeTexts(backlashPlan));

        final Object recoverySword = api.newDisplayData();
        api.setBooleanField(recoverySword, "isBenmingSword", true);
        api.setEnumField(recoverySword, "benmingResonanceType", "DEVOUR");
        api.setBooleanField(recoverySword, "isOverloadRecoveryActive", true);
        api.setBooleanField(recoverySword, "isBurstReady", true);

        final List<String> recoveryBadges = api.getStatusBadgeTexts(
            api.buildRenderPlan(recoverySword)
        );
        assertEquals(List.of("恢复", "回炉得势"), recoveryBadges);
        assertFalse(recoveryBadges.contains("余烬回炼"));

        final Object aftershockSword = api.newDisplayData();
        api.setBooleanField(aftershockSword, "isBenmingSword", true);
        api.setEnumField(aftershockSword, "benmingResonanceType", "DEVOUR");
        api.setBooleanField(aftershockSword, "isBurstReady", false);
        api.setBooleanField(aftershockSword, "isAftershockPeriod", true);

        final List<String> aftershockBadges = api.getStatusBadgeTexts(
            api.buildRenderPlan(aftershockSword)
        );
        assertEquals(List.of("余烬回炼"), aftershockBadges);
        assertFalse(aftershockBadges.contains("反噬"));
        assertFalse(aftershockBadges.contains("恢复"));
    }

    @Test
    void devourRouteUsesDedicatedBurstDangerAndAftershockBadges() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object devourSword = api.newDisplayData();
        api.setBooleanField(devourSword, "isBenmingSword", true);
        api.setEnumField(devourSword, "benmingResonanceType", "DEVOUR");
        api.setBooleanField(devourSword, "shouldHighlightWarning", true);
        api.setBooleanField(devourSword, "isOverloadDanger", true);
        api.setBooleanField(devourSword, "isBurstReady", true);
        api.setBooleanField(devourSword, "isAftershockPeriod", true);

        final Object devourPlan = api.buildRenderPlan(devourSword);
        assertEquals(
            List.of("炉裂将噬", "回炉得势", "余烬回炼"),
            api.getStatusBadgeTexts(devourPlan)
        );
        assertFalse(api.getStatusBadgeTexts(devourPlan).contains("过载危"));
        assertFalse(api.getStatusBadgeTexts(devourPlan).contains("可爆"));
        assertFalse(api.getStatusBadgeTexts(devourPlan).contains("余震"));
    }

    @Test
    void overloadLoopBadgesStayInsideExistingBenmingEntryHeightPolicy() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object normalFront = api.newDisplayData();
        final Object backlashSword = api.newDisplayData();
        api.setBooleanField(backlashSword, "isBenmingSword", true);
        api.setEnumField(backlashSword, "benmingResonanceType", "DEVOUR");
        api.setBooleanField(backlashSword, "isBurstReady", false);
        api.setBooleanField(backlashSword, "isOverloadBacklashActive", true);

        final Object recoverySword = api.newDisplayData();
        api.setBooleanField(recoverySword, "isBenmingSword", true);
        api.setEnumField(recoverySword, "benmingResonanceType", "DEVOUR");
        api.setBooleanField(recoverySword, "isOverloadRecoveryActive", true);
        api.setBooleanField(recoverySword, "isBurstReady", true);

        final Object normalBack = api.newDisplayData();
        api.setEnumField(normalBack, "benmingResonanceType", "OFFENSE");
        api.setBooleanField(normalBack, "isAftershockPeriod", true);

        final Object backlashPlan = api.buildRenderPlan(backlashSword);
        assertTrue(api.getPlanBoolean(backlashPlan, "showStatusRow"));
        assertTrue(api.getPlanBoolean(backlashPlan, "showOverloadRow"));
        assertEquals(
            api.getIntConstant("BENMING_STATUS_ENTRY_HEIGHT"),
            api.getPlanInt(backlashPlan, "entryHeight")
        );
        assertEquals(List.of("反噬"), api.getStatusBadgeTexts(backlashPlan));

        final Object recoveryPlan = api.buildRenderPlan(recoverySword);
        assertTrue(api.getPlanBoolean(recoveryPlan, "showStatusRow"));
        assertTrue(api.getPlanBoolean(recoveryPlan, "showOverloadRow"));
        assertEquals(
            api.getIntConstant("BENMING_STATUS_ENTRY_HEIGHT"),
            api.getPlanInt(recoveryPlan, "entryHeight")
        );
        assertEquals(List.of("恢复", "回炉得势"), api.getStatusBadgeTexts(recoveryPlan));

        final List<?> placements = api.buildPlacements(
            List.of(normalFront, backlashSword, recoverySword, normalBack)
        );
        assertEquals(TEST_MAGIC_4, placements.size());

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
        assertEquals(
            marginTop + normalHeight + spacing + benmingHeight + spacing + benmingHeight + spacing,
            api.getPlacementY(placements.get(3))
        );

        assertFalse(api.getPlacementPlanBoolean(placements.get(0), "benmingEnhanced"));
        assertTrue(api.getPlacementPlanBoolean(placements.get(1), "benmingEnhanced"));
        assertTrue(api.getPlacementPlanBoolean(placements.get(1), "showStatusRow"));
        assertTrue(api.getPlacementPlanBoolean(placements.get(2), "benmingEnhanced"));
        assertTrue(api.getPlacementPlanBoolean(placements.get(2), "showStatusRow"));
        assertFalse(api.getPlacementPlanBoolean(placements.get(3), "benmingEnhanced"));
        assertFalse(api.getPlacementPlanBoolean(placements.get(3), "showStatusRow"));
        assertFalse(api.getPlacementPlanBoolean(placements.get(3), "showOverloadRow"));
    }

    @Test
    void devourBenmingPlanShowsDedicatedShortLabel() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object devourSword = api.newDisplayData();
        api.setBooleanField(devourSword, "isBenmingSword", true);
        api.setEnumField(devourSword, "benmingResonanceType", "DEVOUR");
        api.setFloatField(devourSword, "overloadPercent", TEST_MAGIC_65_0F);

        final Object devourPlan = api.buildRenderPlan(devourSword);
        assertTrue(api.getPlanBoolean(devourPlan, "benmingEnhanced"));
        assertEquals("噬元", api.getPlanString(devourPlan, "resonanceText"));
        assertEquals("载65%", api.getPlanString(devourPlan, "overloadText"));
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

    private static boolean jarContainsResource(final Path jarPath, final String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
