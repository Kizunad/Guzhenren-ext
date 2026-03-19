package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordHudStateContractTests {


    private static final double TEST_MAGIC_135_5D = 135.5D;
    private static final long TEST_MAGIC_240L = 240L;
    private static final long TEST_MAGIC_180L = 180L;
    private static final long TEST_MAGIC_260L = 260L;
    private static final long TEST_MAGIC_200L = 200L;
    private static final float TEST_MAGIC_135_5F = 135.5F;
    private static final float TEST_MAGIC_0_0001F = 0.0001F;
    private static final long TEST_MAGIC_100L = 100L;
    private static final double TEST_MAGIC_10_0D = 10.0D;
    private static final long TEST_MAGIC_220L = 220L;
    private static final double TEST_MAGIC_99_9D = 99.9D;
    private static final double TEST_MAGIC_100_0D = 100.0D;
    private static final float TEST_MAGIC_100_0F = 100.0F;
    private static final int TEST_MAGIC_6 = 6;

    private static final String HUD_STATE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState";
    private static final String DISPLAY_DATA_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$SwordDisplayData";
    private static final String PROJECTION_INPUT_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHudState$BenmingPhase2ProjectionInput";
    private static final String RESONANCE_ENUM_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType";
    private static final String PLAYER_CLASS_RESOURCE =
        "net/minecraft/world/entity/player/Player.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final String BONDED_STABLE_SWORD_ID = "stable-sword-01";
    private static final String OTHER_STABLE_SWORD_ID = "stable-sword-02";
    private static Path cachedMinecraftJarPath;

    @Test
    void projectionExposesBenmingRichFieldsAndKeepsSelectedFlag() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object data = api.newDisplayData();
        api.setBooleanField(data, "isSelected", true);

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "offense",
            TEST_MAGIC_135_5D,
            TEST_MAGIC_240L,
            TEST_MAGIC_180L,
            TEST_MAGIC_260L,
            TEST_MAGIC_200L
        ));

        assertTrue(api.getBooleanField(data, "isSelected"));
        assertTrue(api.getBooleanField(data, "isBenmingSword"));
        assertEquals("OFFENSE", api.getEnumFieldName(data, "benmingResonanceType"));
        assertEquals(TEST_MAGIC_135_5F, api.getFloatField(data, "overloadPercent"), TEST_MAGIC_0_0001F);
        assertFalse(api.getBooleanField(data, "isBurstReady"));
        assertTrue(api.getBooleanField(data, "isAftershockPeriod"));
        assertTrue(api.getBooleanField(data, "shouldHighlightWarning"));
    }

    @Test
    void projectionUsesConservativeDefaultsWhenStateInputsMissingOrUnknown() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object data = api.newDisplayData();

        api.project(new RuntimeApi.ProjectionArgs(data, null, null, null, 0.0D, 0L, 0L, 0L, TEST_MAGIC_200L));

        assertFalse(api.getBooleanField(data, "isBenmingSword"));
        assertNull(api.getField(data, "benmingResonanceType"));
        assertEquals(0.0F, api.getFloatField(data, "overloadPercent"), TEST_MAGIC_0_0001F);
        assertTrue(api.getBooleanField(data, "isBurstReady"));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));
        assertFalse(api.getBooleanField(data, "shouldHighlightWarning"));

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            OTHER_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "unknown-resonance",
            0.0D,
            TEST_MAGIC_100L,
            TEST_MAGIC_180L,
            TEST_MAGIC_260L,
            TEST_MAGIC_200L
        ));

        assertFalse(api.getBooleanField(data, "isBenmingSword"));
        assertNull(api.getField(data, "benmingResonanceType"));
        assertTrue(api.getBooleanField(data, "isBurstReady"));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));
    }

    @Test
    void projectionReportsAftershockAcrossActiveWindowAndExpiryBoundaries() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object data = api.newDisplayData();

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            TEST_MAGIC_10_0D,
            TEST_MAGIC_180L,
            TEST_MAGIC_240L,
            TEST_MAGIC_260L,
            TEST_MAGIC_200L
        ));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            TEST_MAGIC_10_0D,
            TEST_MAGIC_180L,
            TEST_MAGIC_200L,
            TEST_MAGIC_260L,
            TEST_MAGIC_220L
        ));
        assertTrue(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            TEST_MAGIC_10_0D,
            TEST_MAGIC_180L,
            TEST_MAGIC_200L,
            0L,
            TEST_MAGIC_200L
        ));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            TEST_MAGIC_10_0D,
            TEST_MAGIC_180L,
            TEST_MAGIC_200L,
            TEST_MAGIC_200L,
            TEST_MAGIC_200L
        ));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            OTHER_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            TEST_MAGIC_10_0D,
            TEST_MAGIC_180L,
            TEST_MAGIC_200L,
            TEST_MAGIC_260L,
            TEST_MAGIC_220L
        ));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));
    }

    @Test
    void projectionRaisesNearOverloadWarningAtThresholdBoundary() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object data = api.newDisplayData();

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "defense",
            TEST_MAGIC_99_9D,
            TEST_MAGIC_180L,
            0L,
            0L,
            TEST_MAGIC_200L
        ));
        assertFalse(api.getBooleanField(data, "shouldHighlightWarning"));

        api.project(new RuntimeApi.ProjectionArgs(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "defense",
            TEST_MAGIC_100_0D,
            TEST_MAGIC_180L,
            0L,
            0L,
            TEST_MAGIC_200L
        ));
        assertTrue(api.getBooleanField(data, "isBenmingSword"));
        assertEquals(TEST_MAGIC_100_0F, api.getFloatField(data, "overloadPercent"), TEST_MAGIC_0_0001F);
        assertTrue(api.getBooleanField(data, "shouldHighlightWarning"));
    }

    private static final class RuntimeApi {

        private final Class<?> hudStateClass;
        private final Class<?> displayDataClass;
        private final Class<?> projectionInputClass;
        private final Class<?> resonanceEnumClass;
        private final Method projectionMethod;
        private final Constructor<?> projectionInputConstructor;

        private RuntimeApi(
            final Class<?> hudStateClass,
            final Class<?> displayDataClass,
            final Class<?> projectionInputClass,
            final Class<?> resonanceEnumClass
        ) throws Exception {
            this.hudStateClass = hudStateClass;
            this.displayDataClass = displayDataClass;
            this.projectionInputClass = projectionInputClass;
            this.resonanceEnumClass = resonanceEnumClass;
            this.projectionMethod = hudStateClass.getDeclaredMethod(
                "projectBenmingPhase2Fields",
                displayDataClass,
                projectionInputClass
            );
            this.projectionMethod.setAccessible(true);
            this.projectionInputConstructor = projectionInputClass.getDeclaredConstructor(
                String.class,
                String.class,
                String.class,
                double.class,
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
            final Class<?> displayDataClass = Class.forName(DISPLAY_DATA_CLASS_NAME, true, loader);
            final Class<?> projectionInputClass = Class.forName(
                PROJECTION_INPUT_CLASS_NAME,
                true,
                loader
            );
            final Class<?> resonanceEnumClass = Class.forName(RESONANCE_ENUM_CLASS_NAME, true, loader);
            return new RuntimeApi(
                hudStateClass,
                displayDataClass,
                projectionInputClass,
                resonanceEnumClass
            );
        }

        Object newDisplayData() throws Exception {
            return displayDataClass.getConstructor().newInstance();
        }

        void project(final ProjectionArgs args) throws Exception {
            final Object projectionInput = projectionInputConstructor.newInstance(
                args.stableSwordId(),
                args.bondedSwordId(),
                args.resonanceRaw(),
                args.overload(),
                args.burstCooldownUntilTick(),
                args.burstActiveUntilTick(),
                args.burstAftershockUntilTick(),
                args.gameTick()
            );
            projectionMethod.invoke(
                null,
                args.data(),
                projectionInput
            );
        }

        private record ProjectionArgs(
            Object data,
            String stableSwordId,
            String bondedSwordId,
            String resonanceRaw,
            double overload,
            long burstCooldownUntilTick,
            long burstActiveUntilTick,
            long burstAftershockUntilTick,
            long gameTick
        ) {}

        Object getField(final Object target, final String fieldName) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            return field.get(target);
        }

        boolean getBooleanField(final Object target, final String fieldName) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            return field.getBoolean(target);
        }

        void setBooleanField(final Object target, final String fieldName, final boolean value)
            throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            field.setBoolean(target, value);
        }

        float getFloatField(final Object target, final String fieldName) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            return field.getFloat(target);
        }

        String getEnumFieldName(final Object target, final String fieldName) throws Exception {
            final Field field = displayDataClass.getField(fieldName);
            final Object enumValue = field.get(target);
            assertNotNull(enumValue);
            assertTrue(resonanceEnumClass.isInstance(enumValue));
            return (String) resonanceEnumClass.getMethod("name").invoke(enumValue);
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

    private static boolean jarContainsResource(Path jarPath, String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
