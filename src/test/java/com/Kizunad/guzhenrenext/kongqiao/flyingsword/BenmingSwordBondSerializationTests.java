package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import java.io.IOException;
import java.io.InputStream;
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

final class BenmingSwordBondSerializationTests {

    private static final String ATTRS_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes";
    private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";
    private static final String TEST_STABLE_SWORD_ID = "benming-sword-stable-001";
    private static final String TEST_OWNER_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final double TEST_RESONANCE = 0.618;
    private static final double DOUBLE_DELTA = 1.0E-9;
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";

    @Test
    void stableSwordIdAndBondRoundTripAcrossApis() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object source = api.newAttributes();
        api.setStableSwordId(source, TEST_STABLE_SWORD_ID);
        final Object sourceBond = api.getBond(source);
        api.setOwnerUuid(sourceBond, TEST_OWNER_UUID);
        api.setResonance(sourceBond, TEST_RESONANCE);

        final Object serialized = api.toNbt(source);
        assertTrue(api.contains(serialized, "stableSwordId"));
        assertTrue(api.contains(serialized, "bond"));

        final Object fromNbt = api.fromNbt(serialized);
        assertEquals(TEST_STABLE_SWORD_ID, api.getStableSwordId(fromNbt));
        assertBondEquals(api, fromNbt, TEST_OWNER_UUID, TEST_RESONANCE);

        final Object readTarget = api.newAttributes();
        api.setStableSwordId(readTarget, "temporary-id");
        final Object readTargetBond = api.getBond(readTarget);
        api.setOwnerUuid(readTargetBond, "temporary-owner");
        api.setResonance(readTargetBond, 99.0);
        api.readFromNbt(readTarget, serialized);
        assertEquals(TEST_STABLE_SWORD_ID, api.getStableSwordId(readTarget));
        assertBondEquals(api, readTarget, TEST_OWNER_UUID, TEST_RESONANCE);

        final Object copied = api.copy(source);
        assertEquals(TEST_STABLE_SWORD_ID, api.getStableSwordId(copied));
        assertBondEquals(api, copied, TEST_OWNER_UUID, TEST_RESONANCE);
    }

    @Test
    void missingBondTagFallsBackToUnboundState() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object source = api.newAttributes();
        api.setStableSwordId(source, TEST_STABLE_SWORD_ID);
        final Object sourceBond = api.getBond(source);
        api.setOwnerUuid(sourceBond, TEST_OWNER_UUID);
        api.setResonance(sourceBond, TEST_RESONANCE);

        final Object legacyTag = api.toNbt(source);
        api.remove(legacyTag, "bond");
        api.remove(legacyTag, "stableSwordId");

        final Object fromLegacy = api.fromNbt(legacyTag);
        assertFalse(api.getStableSwordId(fromLegacy).isBlank());
        assertBondEquals(api, fromLegacy, "", 0.0);

        final Object readTarget = api.newAttributes();
        api.setStableSwordId(readTarget, TEST_STABLE_SWORD_ID);
        final Object readTargetBond = api.getBond(readTarget);
        api.setOwnerUuid(readTargetBond, TEST_OWNER_UUID);
        api.setResonance(readTargetBond, TEST_RESONANCE);
        api.readFromNbt(readTarget, legacyTag);
        assertEquals(TEST_STABLE_SWORD_ID, api.getStableSwordId(readTarget));
        assertBondEquals(api, readTarget, "", 0.0);
    }

    @Test
    void missingStableSwordIdKeepsBondAcrossApis() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object source = api.newAttributes();
        api.setStableSwordId(source, TEST_STABLE_SWORD_ID);
        final Object sourceBond = api.getBond(source);
        api.setOwnerUuid(sourceBond, TEST_OWNER_UUID);
        api.setResonance(sourceBond, TEST_RESONANCE);

        final Object legacyTag = api.toNbt(source);
        api.remove(legacyTag, "stableSwordId");
        assertFalse(api.contains(legacyTag, "stableSwordId"));
        assertTrue(api.contains(legacyTag, "bond"));

        final Object fromLegacy = api.fromNbt(legacyTag);
        assertFalse(api.getStableSwordId(fromLegacy).isBlank());
        assertBondEquals(api, fromLegacy, TEST_OWNER_UUID, TEST_RESONANCE);

        final Object readTarget = api.newAttributes();
        api.setStableSwordId(readTarget, "existing-stable-id");
        final Object readTargetBond = api.getBond(readTarget);
        api.setOwnerUuid(readTargetBond, "old-owner");
        api.setResonance(readTargetBond, 99.0);
        api.readFromNbt(readTarget, legacyTag);
        assertEquals("existing-stable-id", api.getStableSwordId(readTarget));
        assertBondEquals(api, readTarget, TEST_OWNER_UUID, TEST_RESONANCE);
    }

    @Test
    void missingBondResetsToUnboundWhileStableSwordIdLoadsAcrossApis() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object source = api.newAttributes();
        api.setStableSwordId(source, TEST_STABLE_SWORD_ID);
        final Object sourceBond = api.getBond(source);
        api.setOwnerUuid(sourceBond, TEST_OWNER_UUID);
        api.setResonance(sourceBond, TEST_RESONANCE);

        final Object legacyTag = api.toNbt(source);
        api.remove(legacyTag, "bond");
        assertTrue(api.contains(legacyTag, "stableSwordId"));
        assertFalse(api.contains(legacyTag, "bond"));

        final Object fromLegacy = api.fromNbt(legacyTag);
        assertEquals(TEST_STABLE_SWORD_ID, api.getStableSwordId(fromLegacy));
        assertBondEquals(api, fromLegacy, "", 0.0);

        final Object readTarget = api.newAttributes();
        api.setStableSwordId(readTarget, "temporary-id");
        final Object readTargetBond = api.getBond(readTarget);
        api.setOwnerUuid(readTargetBond, TEST_OWNER_UUID);
        api.setResonance(readTargetBond, TEST_RESONANCE);
        api.readFromNbt(readTarget, legacyTag);
        assertEquals(TEST_STABLE_SWORD_ID, api.getStableSwordId(readTarget));
        assertBondEquals(api, readTarget, "", 0.0);
    }

    private static void assertBondEquals(
        RuntimeApi api,
        Object attributes,
        String expectedOwnerUuid,
        double expectedResonance
    ) throws Exception {
        final Object bond = api.getBond(attributes);
        assertEquals(expectedOwnerUuid, api.getOwnerUuid(bond));
        assertEquals(expectedResonance, api.getResonance(bond), DOUBLE_DELTA);
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> attributesClass;
        private final Method fromNbtMethod;
        private final Method readFromNbtMethod;

        private RuntimeApi(Class<?> attributesClass, Class<?> compoundTagClass)
            throws NoSuchMethodException {
            this.attributesClass = attributesClass;
            this.fromNbtMethod = attributesClass.getMethod("fromNBT", compoundTagClass);
            this.readFromNbtMethod = attributesClass.getMethod("readFromNBT", compoundTagClass);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> attrsClass = Class.forName(ATTRS_CLASS_NAME, true, loader);
            final Class<?> compoundTagClass = Class.forName(
                COMPOUND_TAG_CLASS_NAME,
                true,
                loader
            );
            return new RuntimeApi(attrsClass, compoundTagClass);
        }

        Object newAttributes() throws Exception {
            return attributesClass.getConstructor().newInstance();
        }

        void setStableSwordId(Object attributes, String id) throws Exception {
            attributesClass
                .getMethod("setStableSwordId", String.class)
                .invoke(attributes, id);
        }

        String getStableSwordId(Object attributes) throws Exception {
            return (String) attributesClass.getMethod("getStableSwordId").invoke(attributes);
        }

        Object getBond(Object attributes) throws Exception {
            return attributesClass.getMethod("getBond").invoke(attributes);
        }

        void setOwnerUuid(Object bond, String ownerUuid) throws Exception {
            bond.getClass().getMethod("setOwnerUuid", String.class).invoke(bond, ownerUuid);
        }

        String getOwnerUuid(Object bond) throws Exception {
            return (String) bond.getClass().getMethod("getOwnerUuid").invoke(bond);
        }

        void setResonance(Object bond, double resonance) throws Exception {
            bond.getClass().getMethod("setResonance", double.class).invoke(bond, resonance);
        }

        double getResonance(Object bond) throws Exception {
            return (double) bond.getClass().getMethod("getResonance").invoke(bond);
        }

        Object toNbt(Object attributes) throws Exception {
            return attributesClass.getMethod("toNBT").invoke(attributes);
        }

        Object fromNbt(Object nbt) throws Exception {
            return fromNbtMethod.invoke(null, nbt);
        }

        void readFromNbt(Object attributes, Object nbt) throws Exception {
            readFromNbtMethod.invoke(attributes, nbt);
        }

        Object copy(Object attributes) throws Exception {
            return attributesClass.getMethod("copy").invoke(attributes);
        }

        boolean contains(Object nbt, String key) throws Exception {
            return (boolean) nbt.getClass().getMethod("contains", String.class).invoke(nbt, key);
        }

        void remove(Object nbt, String key) throws Exception {
            nbt.getClass().getMethod("remove", String.class).invoke(nbt, key);
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
            try (InputStream input = java.nio.file.Files.newInputStream(manifestPath)) {
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

            return new URLClassLoader(
                urls.toArray(new URL[0]),
                ClassLoader.getPlatformClassLoader()
            );
        }

        private static synchronized Path resolveMinecraftRuntimeJar()
            throws IOException {
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

        private static Path findJarContainingResource(Path root, String resource)
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

        private static boolean jarContainsResource(Path jarPath, String resource) {
            try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
                return jar.getEntry(resource) != null;
            } catch (IOException ignored) {
                return false;
            }
        }
    }
}
