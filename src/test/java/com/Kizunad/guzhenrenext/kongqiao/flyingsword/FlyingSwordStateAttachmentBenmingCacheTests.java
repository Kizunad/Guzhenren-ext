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


    private static final long TEST_MAGIC_NEG_2L = -2L;
    private static final int TEST_MAGIC_NEG_3 = -3;
    private static final long TEST_MAGIC_NEG_4L = -4L;
    private static final long TEST_MAGIC_NEG_5L = -5L;
    private static final long TEST_MAGIC_NEG_6L = -6L;
    private static final int TEST_MAGIC_6 = 6;

    private static final String ATTACHMENT_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment";
    private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";
    private static final String INITIALIZED_KEY = "Initialized";
    private static final String BONDED_SWORD_ID_KEY = "BondedSwordId";
    private static final String BOND_CACHE_DIRTY_KEY = "BondCacheDirty";
    private static final String LAST_RESOLVED_TICK_KEY = "LastResolvedTick";
    private static final String RESONANCE_TYPE_KEY = "ResonanceType";
    private static final String OVERLOAD_KEY = "Overload";
    private static final String BURST_COOLDOWN_UNTIL_TICK_KEY = "BurstCooldownUntilTick";
    private static final String BURST_ACTIVE_UNTIL_TICK_KEY = "BurstActiveUntilTick";
    private static final String BURST_AFTERSHOCK_UNTIL_TICK_KEY =
        "BurstAftershockUntilTick";
    private static final String RITUAL_LOCK_UNTIL_TICK_KEY = "RitualLockUntilTick";
    private static final String RESONANCE_LEVEL_KEY = "ResonanceLevel";
    private static final String LAST_OVERLOAD_TICK_KEY = "LastOverloadTick";
    private static final String OVERLOAD_BACKLASH_UNTIL_TICK_KEY =
        "OverloadBacklashUntilTick";
    private static final String OVERLOAD_RECOVERY_UNTIL_TICK_KEY =
        "OverloadRecoveryUntilTick";
    private static final String LAST_COMBAT_TICK_KEY = "LastCombatTick";
    private static final String CACHE_SWORD_ID = "benming-cache-stable-sword-01";
    private static final String RESONANCE_TYPE = "sword-fire";
    private static final double OVERLOAD = 12.5D;
    private static final long BURST_COOLDOWN_UNTIL_TICK = 5678L;
    private static final long BURST_ACTIVE_UNTIL_TICK = 5789L;
    private static final long BURST_AFTERSHOCK_UNTIL_TICK = 5890L;
    private static final long RITUAL_LOCK_UNTIL_TICK = 6789L;
    private static final int RESONANCE_LEVEL = 3;
    private static final long LAST_OVERLOAD_TICK = 3456L;
    private static final long OVERLOAD_BACKLASH_UNTIL_TICK = 7890L;
    private static final long OVERLOAD_RECOVERY_UNTIL_TICK = 7999L;
    private static final long LAST_COMBAT_TICK = 8123L;
    private static final double DEFAULT_OVERLOAD = 0.0D;
    private static final long DEFAULT_NON_NEGATIVE_TICK = 0L;
    private static final int DEFAULT_RESONANCE_LEVEL = 0;
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
        api.setResonanceType(state, RESONANCE_TYPE);
        api.setOverload(state, OVERLOAD);
        api.setBurstCooldownUntilTick(state, BURST_COOLDOWN_UNTIL_TICK);
        api.setBurstActiveUntilTick(state, BURST_ACTIVE_UNTIL_TICK);
        api.setBurstAftershockUntilTick(state, BURST_AFTERSHOCK_UNTIL_TICK);
        api.setRitualLockUntilTick(state, RITUAL_LOCK_UNTIL_TICK);
        api.setResonanceLevel(state, RESONANCE_LEVEL);
        api.setLastOverloadTick(state, LAST_OVERLOAD_TICK);
        api.setOverloadBacklashUntilTick(state, OVERLOAD_BACKLASH_UNTIL_TICK);
        api.setOverloadRecoveryUntilTick(state, OVERLOAD_RECOVERY_UNTIL_TICK);
        api.setLastCombatTick(state, LAST_COMBAT_TICK);

        final Object serialized = api.serialize(state);
        assertTrue(api.contains(serialized, INITIALIZED_KEY));
        assertTrue(api.contains(serialized, BONDED_SWORD_ID_KEY));
        assertTrue(api.contains(serialized, BOND_CACHE_DIRTY_KEY));
        assertTrue(api.contains(serialized, LAST_RESOLVED_TICK_KEY));
        assertTrue(api.contains(serialized, RESONANCE_TYPE_KEY));
        assertTrue(api.contains(serialized, OVERLOAD_KEY));
        assertTrue(api.contains(serialized, BURST_COOLDOWN_UNTIL_TICK_KEY));
        assertTrue(api.contains(serialized, BURST_ACTIVE_UNTIL_TICK_KEY));
        assertTrue(api.contains(serialized, BURST_AFTERSHOCK_UNTIL_TICK_KEY));
        assertTrue(api.contains(serialized, RITUAL_LOCK_UNTIL_TICK_KEY));
        assertTrue(api.contains(serialized, RESONANCE_LEVEL_KEY));
        assertTrue(api.contains(serialized, LAST_OVERLOAD_TICK_KEY));
        assertTrue(api.contains(serialized, OVERLOAD_BACKLASH_UNTIL_TICK_KEY));
        assertTrue(api.contains(serialized, OVERLOAD_RECOVERY_UNTIL_TICK_KEY));
        assertTrue(api.contains(serialized, LAST_COMBAT_TICK_KEY));

        final Object restored = api.newAttachment();
        api.deserialize(restored, serialized);
        assertTrue(api.isInitialized(restored));
        assertEquals(CACHE_SWORD_ID, api.getBondedSwordId(restored));
        assertFalse(api.isBondCacheDirty(restored));
        assertEquals(RESOLVED_TICK, api.getLastResolvedTick(restored));
        assertEquals(RESONANCE_TYPE, api.getResonanceType(restored));
        assertEquals(OVERLOAD, api.getOverload(restored));
        assertEquals(BURST_COOLDOWN_UNTIL_TICK, api.getBurstCooldownUntilTick(restored));
        assertEquals(BURST_ACTIVE_UNTIL_TICK, api.getBurstActiveUntilTick(restored));
        assertEquals(
            BURST_AFTERSHOCK_UNTIL_TICK,
            api.getBurstAftershockUntilTick(restored)
        );
        assertEquals(RITUAL_LOCK_UNTIL_TICK, api.getRitualLockUntilTick(restored));
        assertEquals(RESONANCE_LEVEL, api.getResonanceLevel(restored));
        assertEquals(LAST_OVERLOAD_TICK, api.getLastOverloadTick(restored));
        assertEquals(
            OVERLOAD_BACKLASH_UNTIL_TICK,
            api.getOverloadBacklashUntilTick(restored)
        );
        assertEquals(
            OVERLOAD_RECOVERY_UNTIL_TICK,
            api.getOverloadRecoveryUntilTick(restored)
        );
        assertEquals(LAST_COMBAT_TICK, api.getLastCombatTick(restored));

        final Object legacyTag = api.newCompoundTag();
        api.putBoolean(legacyTag, INITIALIZED_KEY, true);
        final Object legacyRestored = api.newAttachment();
        api.deserialize(legacyRestored, legacyTag);
        assertTrue(api.isInitialized(legacyRestored));
        assertEquals("", api.getBondedSwordId(legacyRestored));
        assertTrue(api.isBondCacheDirty(legacyRestored));
        assertEquals(UNRESOLVED_TICK, api.getLastResolvedTick(legacyRestored));
        assertEquals("", api.getResonanceType(legacyRestored));
        assertEquals(DEFAULT_OVERLOAD, api.getOverload(legacyRestored));
        assertEquals(
            DEFAULT_NON_NEGATIVE_TICK,
            api.getBurstCooldownUntilTick(legacyRestored)
        );
        assertEquals(
            DEFAULT_NON_NEGATIVE_TICK,
            api.getBurstActiveUntilTick(legacyRestored)
        );
        assertEquals(
            DEFAULT_NON_NEGATIVE_TICK,
            api.getBurstAftershockUntilTick(legacyRestored)
        );
        assertEquals(
            DEFAULT_NON_NEGATIVE_TICK,
            api.getRitualLockUntilTick(legacyRestored)
        );
        assertEquals(DEFAULT_RESONANCE_LEVEL, api.getResonanceLevel(legacyRestored));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getLastOverloadTick(legacyRestored));
        assertEquals(
            DEFAULT_NON_NEGATIVE_TICK,
            api.getOverloadBacklashUntilTick(legacyRestored)
        );
        assertEquals(
            DEFAULT_NON_NEGATIVE_TICK,
            api.getOverloadRecoveryUntilTick(legacyRestored)
        );
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getLastCombatTick(legacyRestored));
    }

    @Test
    void deserializeNullTagFallsBackToDefaultState() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object state = api.newAttachment();
        api.setInitialized(state, true);
        api.updateBondCache(state, CACHE_SWORD_ID, RESOLVED_TICK);
        api.setOverload(state, OVERLOAD);
        api.setBurstCooldownUntilTick(state, BURST_COOLDOWN_UNTIL_TICK);
        api.setResonanceLevel(state, RESONANCE_LEVEL);
        api.setLastOverloadTick(state, LAST_OVERLOAD_TICK);
        api.setOverloadBacklashUntilTick(state, OVERLOAD_BACKLASH_UNTIL_TICK);
        api.setOverloadRecoveryUntilTick(state, OVERLOAD_RECOVERY_UNTIL_TICK);
        api.setLastCombatTick(state, LAST_COMBAT_TICK);

        api.deserialize(state, null);
        assertFalse(api.isInitialized(state));
        assertEquals("", api.getBondedSwordId(state));
        assertTrue(api.isBondCacheDirty(state));
        assertEquals(UNRESOLVED_TICK, api.getLastResolvedTick(state));
        assertEquals(DEFAULT_OVERLOAD, api.getOverload(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getBurstCooldownUntilTick(state));
        assertEquals(DEFAULT_RESONANCE_LEVEL, api.getResonanceLevel(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getLastOverloadTick(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getOverloadBacklashUntilTick(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getOverloadRecoveryUntilTick(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getLastCombatTick(state));
    }

    @Test
    void deserializeNegativeSerializedValuesNormalizesToNonNegativeDefaults() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final Object serialized = api.newCompoundTag();
        api.putDouble(serialized, OVERLOAD_KEY, -1.0D);
        api.putLong(serialized, LAST_OVERLOAD_TICK_KEY, TEST_MAGIC_NEG_2L);
        api.putInt(serialized, RESONANCE_LEVEL_KEY, TEST_MAGIC_NEG_3);
        api.putLong(serialized, OVERLOAD_BACKLASH_UNTIL_TICK_KEY, TEST_MAGIC_NEG_4L);
        api.putLong(serialized, OVERLOAD_RECOVERY_UNTIL_TICK_KEY, TEST_MAGIC_NEG_5L);
        api.putLong(serialized, LAST_COMBAT_TICK_KEY, TEST_MAGIC_NEG_6L);

        final Object state = api.newAttachment();
        api.deserialize(state, serialized);
        assertEquals(DEFAULT_OVERLOAD, api.getOverload(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getLastOverloadTick(state));
        assertEquals(DEFAULT_RESONANCE_LEVEL, api.getResonanceLevel(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getOverloadBacklashUntilTick(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getOverloadRecoveryUntilTick(state));
        assertEquals(DEFAULT_NON_NEGATIVE_TICK, api.getLastCombatTick(state));
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

        void setResonanceType(Object state, String resonanceType) throws Exception {
            attachmentClass.getMethod("setResonanceType", String.class)
                .invoke(state, resonanceType);
        }

        String getResonanceType(Object state) throws Exception {
            return (String) attachmentClass.getMethod("getResonanceType").invoke(state);
        }

        void setOverload(Object state, double overload) throws Exception {
            attachmentClass.getMethod("setOverload", double.class).invoke(state, overload);
        }

        double getOverload(Object state) throws Exception {
            return (double) attachmentClass.getMethod("getOverload").invoke(state);
        }

        void setBurstCooldownUntilTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setBurstCooldownUntilTick", long.class)
                .invoke(state, tick);
        }

        long getBurstCooldownUntilTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getBurstCooldownUntilTick").invoke(state);
        }

        void setBurstActiveUntilTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setBurstActiveUntilTick", long.class)
                .invoke(state, tick);
        }

        long getBurstActiveUntilTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getBurstActiveUntilTick").invoke(state);
        }

        void setBurstAftershockUntilTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setBurstAftershockUntilTick", long.class)
                .invoke(state, tick);
        }

        long getBurstAftershockUntilTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getBurstAftershockUntilTick")
                .invoke(state);
        }

        void setRitualLockUntilTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setRitualLockUntilTick", long.class).invoke(state, tick);
        }

        long getRitualLockUntilTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getRitualLockUntilTick").invoke(state);
        }

        void setResonanceLevel(Object state, int level) throws Exception {
            attachmentClass.getMethod("setResonanceLevel", int.class).invoke(state, level);
        }

        int getResonanceLevel(Object state) throws Exception {
            return (int) attachmentClass.getMethod("getResonanceLevel").invoke(state);
        }

        void setLastOverloadTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setLastOverloadTick", long.class).invoke(state, tick);
        }

        long getLastOverloadTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getLastOverloadTick").invoke(state);
        }

        void setOverloadBacklashUntilTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setOverloadBacklashUntilTick", long.class)
                .invoke(state, tick);
        }

        long getOverloadBacklashUntilTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getOverloadBacklashUntilTick")
                .invoke(state);
        }

        void setOverloadRecoveryUntilTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setOverloadRecoveryUntilTick", long.class)
                .invoke(state, tick);
        }

        long getOverloadRecoveryUntilTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getOverloadRecoveryUntilTick")
                .invoke(state);
        }

        void setLastCombatTick(Object state, long tick) throws Exception {
            attachmentClass.getMethod("setLastCombatTick", long.class).invoke(state, tick);
        }

        long getLastCombatTick(Object state) throws Exception {
            return (long) attachmentClass.getMethod("getLastCombatTick").invoke(state);
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

        void putLong(Object tag, String key, long value) throws Exception {
            compoundTagClass.getMethod("putLong", String.class, long.class)
                .invoke(tag, key, value);
        }

        void putDouble(Object tag, String key, double value) throws Exception {
            compoundTagClass.getMethod("putDouble", String.class, double.class)
                .invoke(tag, key, value);
        }

        void putInt(Object tag, String key, int value) throws Exception {
            compoundTagClass.getMethod("putInt", String.class, int.class).invoke(tag, key, value);
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
}
