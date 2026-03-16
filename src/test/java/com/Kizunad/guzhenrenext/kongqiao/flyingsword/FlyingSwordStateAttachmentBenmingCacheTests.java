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

final class FlyingSwordStateAttachmentBenmingCacheTests {

    private static final String ATTACHMENT_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment";
    private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";
    private static final String INITIALIZED_KEY = "Initialized";
    private static final String BONDED_SWORD_ID_KEY = "BondedSwordId";
    private static final String BOND_CACHE_DIRTY_KEY = "BondCacheDirty";
    private static final String LAST_RESOLVED_TICK_KEY = "LastResolvedTick";
    private static final String CACHE_SWORD_ID = "benming-cache-stable-sword-01";
    private static final String CACHE_ONLY_KEY_BOND = "bond";
    private static final String CACHE_ONLY_KEY_OWNER_UUID = "ownerUuid";
    private static final String CACHE_ONLY_KEY_RESONANCE = "resonance";
    private static final String CACHE_ONLY_KEY_DEBT = "debt";
    private static final String CACHE_ONLY_KEY_RESOURCE_DEBT = "resourceDebt";
    private static final long RESOLVED_TICK = 2468L;
    private static final long UNRESOLVED_TICK = -1L;
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";

    @Test
    void serializeDeserializeRetainsCacheFieldsAndInitializedCompatibility() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object state = api.newAttachment();
        api.setInitialized(state, true);
        api.updateBondCache(state, CACHE_SWORD_ID, RESOLVED_TICK);

        final Object serialized = api.serialize(state);
        assertTrue(api.contains(serialized, INITIALIZED_KEY));
        assertTrue(api.contains(serialized, BONDED_SWORD_ID_KEY));
        assertTrue(api.contains(serialized, BOND_CACHE_DIRTY_KEY));
        assertTrue(api.contains(serialized, LAST_RESOLVED_TICK_KEY));

        final Object restored = api.newAttachment();
        api.deserialize(restored, serialized);
        assertTrue(api.isInitialized(restored));
        assertEquals(CACHE_SWORD_ID, api.getBondedSwordId(restored));
        assertFalse(api.isBondCacheDirty(restored));
        assertEquals(RESOLVED_TICK, api.getLastResolvedTick(restored));

        final Object legacyTag = api.newCompoundTag();
        api.putBoolean(legacyTag, INITIALIZED_KEY, true);
        final Object legacyRestored = api.newAttachment();
        api.deserialize(legacyRestored, legacyTag);
        assertTrue(api.isInitialized(legacyRestored));
        assertEquals("", api.getBondedSwordId(legacyRestored));
        assertTrue(api.isBondCacheDirty(legacyRestored));
        assertEquals(UNRESOLVED_TICK, api.getLastResolvedTick(legacyRestored));
    }

    @Test
    void dirtyAndClearSemanticsStayCacheOnlyWithoutCanonicalPayload() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object state = api.newAttachment();
        api.updateBondCache(state, CACHE_SWORD_ID, RESOLVED_TICK);
        api.markBondCacheDirty(state);
        assertEquals(CACHE_SWORD_ID, api.getBondedSwordId(state));
        assertTrue(api.isBondCacheDirty(state));
        assertEquals(RESOLVED_TICK, api.getLastResolvedTick(state));

        api.clearBondCache(state);
        assertEquals("", api.getBondedSwordId(state));
        assertTrue(api.isBondCacheDirty(state));
        assertEquals(UNRESOLVED_TICK, api.getLastResolvedTick(state));

        final Object serialized = api.serialize(state);
        assertFalse(api.contains(serialized, CACHE_ONLY_KEY_BOND));
        assertFalse(api.contains(serialized, CACHE_ONLY_KEY_OWNER_UUID));
        assertFalse(api.contains(serialized, CACHE_ONLY_KEY_RESONANCE));
        assertFalse(api.contains(serialized, CACHE_ONLY_KEY_DEBT));
        assertFalse(api.contains(serialized, CACHE_ONLY_KEY_RESOURCE_DEBT));
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> attachmentClass;
        private final Class<?> compoundTagClass;
        private final Method serializeMethod;
        private final Method deserializeMethod;

        private RuntimeApi(
            Class<?> attachmentClass,
            Class<?> compoundTagClass,
            Class<?> providerClass
        )
            throws NoSuchMethodException {
            this.attachmentClass = attachmentClass;
            this.compoundTagClass = compoundTagClass;
            this.serializeMethod = attachmentClass.getMethod("serializeNBT", providerClass);
            this.deserializeMethod = attachmentClass.getMethod(
                "deserializeNBT",
                providerClass,
                compoundTagClass
            );
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> attachmentClass = Class.forName(ATTACHMENT_CLASS_NAME, true, loader);
            final Class<?> compoundTagClass = Class.forName(COMPOUND_TAG_CLASS_NAME, true, loader);
            final Class<?> providerClass = Class.forName(
                "net.minecraft.core.HolderLookup$Provider",
                true,
                loader
            );
            return new RuntimeApi(attachmentClass, compoundTagClass, providerClass);
        }

        Object newAttachment() throws Exception {
            return attachmentClass.getConstructor().newInstance();
        }

        Object newCompoundTag() throws Exception {
            return compoundTagClass.getConstructor().newInstance();
        }

        void setInitialized(Object state, boolean initialized) throws Exception {
            attachmentClass.getMethod("setInitialized", boolean.class).invoke(state, initialized);
        }

        boolean isInitialized(Object state) throws Exception {
            return (boolean) attachmentClass.getMethod("isInitialized").invoke(state);
        }

        void updateBondCache(Object state, String swordId, long tick) throws Exception {
            attachmentClass.getMethod("updateBondCache", String.class, long.class)
                .invoke(state, swordId, tick);
        }

        void markBondCacheDirty(Object state) throws Exception {
            attachmentClass.getMethod("markBondCacheDirty").invoke(state);
        }

        void clearBondCache(Object state) throws Exception {
            attachmentClass.getMethod("clearBondCache").invoke(state);
        }

        String getBondedSwordId(Object state) throws Exception {
            return (String) attachmentClass.getMethod("getBondedSwordId").invoke(state);
        }

        boolean isBondCacheDirty(Object state) throws Exception {
            return (boolean) attachmentClass.getMethod("isBondCacheDirty").invoke(state);
        }

        long getLastResolvedTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getLastResolvedTick").invoke(state);
        }

        Object serialize(Object state) throws Exception {
            return serializeMethod.invoke(state, new Object[] {null});
        }

        void deserialize(Object state, Object tag) throws Exception {
            deserializeMethod.invoke(state, new Object[] {null, tag});
        }

        void putBoolean(Object tag, String key, boolean value) throws Exception {
            compoundTagClass.getMethod("putBoolean", String.class, boolean.class)
                .invoke(tag, key, value);
        }

        boolean contains(Object tag, String key) throws Exception {
            return (boolean) compoundTagClass.getMethod("contains", String.class).invoke(tag, key);
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
