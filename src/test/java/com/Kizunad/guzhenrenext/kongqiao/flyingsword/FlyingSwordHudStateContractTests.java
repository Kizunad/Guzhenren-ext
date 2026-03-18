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

        api.project(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "offense",
            135.5D,
            240L,
            180L,
            260L,
            200L
        );

        assertTrue(api.getBooleanField(data, "isSelected"));
        assertTrue(api.getBooleanField(data, "isBenmingSword"));
        assertEquals("OFFENSE", api.getEnumFieldName(data, "benmingResonanceType"));
        assertEquals(135.5F, api.getFloatField(data, "overloadPercent"), 0.0001F);
        assertFalse(api.getBooleanField(data, "isBurstReady"));
        assertTrue(api.getBooleanField(data, "isAftershockPeriod"));
        assertTrue(api.getBooleanField(data, "shouldHighlightWarning"));
    }

    @Test
    void projectionUsesConservativeDefaultsWhenStateInputsMissingOrUnknown() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object data = api.newDisplayData();

        api.project(data, null, null, null, 0.0D, 0L, 0L, 0L, 200L);

        assertFalse(api.getBooleanField(data, "isBenmingSword"));
        assertNull(api.getField(data, "benmingResonanceType"));
        assertEquals(0.0F, api.getFloatField(data, "overloadPercent"), 0.0001F);
        assertTrue(api.getBooleanField(data, "isBurstReady"));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));
        assertFalse(api.getBooleanField(data, "shouldHighlightWarning"));

        api.project(
            data,
            OTHER_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "unknown-resonance",
            0.0D,
            100L,
            180L,
            260L,
            200L
        );

        assertFalse(api.getBooleanField(data, "isBenmingSword"));
        assertNull(api.getField(data, "benmingResonanceType"));
        assertTrue(api.getBooleanField(data, "isBurstReady"));
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));
    }

    @Test
    void projectionReportsAftershockAcrossActiveWindowAndExpiryBoundaries() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object data = api.newDisplayData();

        api.project(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            10.0D,
            180L,
            240L,
            260L,
            200L
        );
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            10.0D,
            180L,
            200L,
            260L,
            220L
        );
        assertTrue(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            10.0D,
            180L,
            200L,
            0L,
            200L
        );
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            10.0D,
            180L,
            200L,
            200L,
            200L
        );
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));

        api.project(
            data,
            OTHER_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "spirit",
            10.0D,
            180L,
            200L,
            260L,
            220L
        );
        assertFalse(api.getBooleanField(data, "isAftershockPeriod"));
    }

    @Test
    void projectionRaisesNearOverloadWarningAtThresholdBoundary() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object data = api.newDisplayData();

        api.project(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "defense",
            99.9D,
            180L,
            0L,
            0L,
            200L
        );
        assertFalse(api.getBooleanField(data, "shouldHighlightWarning"));

        api.project(
            data,
            BONDED_STABLE_SWORD_ID,
            BONDED_STABLE_SWORD_ID,
            "defense",
            100.0D,
            180L,
            0L,
            0L,
            200L
        );
        assertTrue(api.getBooleanField(data, "isBenmingSword"));
        assertEquals(100.0F, api.getFloatField(data, "overloadPercent"), 0.0001F);
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

        void project(
            final Object data,
            final String stableSwordId,
            final String bondedSwordId,
            final String resonanceRaw,
            final double overload,
            final long burstCooldownUntilTick,
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
                burstActiveUntilTick,
                burstAftershockUntilTick,
                gameTick
            );
            projectionMethod.invoke(
                null,
                data,
                projectionInput
            );
        }

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

    private static boolean jarContainsResource(Path jarPath, String resource) {
        try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
            return jar.getEntry(resource) != null;
        } catch (IOException ignored) {
            return false;
        }
    }
}
