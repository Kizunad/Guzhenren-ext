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

final class FlyingSwordControllerResonanceBurstTests {


    private static final long TEST_MAGIC_30L = 30L;
    private static final long TEST_MAGIC_1500L = 1500L;
    private static final long TEST_MAGIC_1600L = 1600L;
    private static final long TEST_MAGIC_1800L = 1800L;
    private static final long TEST_MAGIC_1810L = 1810L;
    private static final long TEST_MAGIC_1820L = 1820L;
    private static final long TEST_MAGIC_1900L = 1900L;
    private static final long TEST_MAGIC_1950L = 1950L;
    private static final int TEST_MAGIC_6 = 6;

    private static final String CONTROLLER_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController";
    private static final String STATE_ATTACHMENT_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment";
    private static final String RESOURCE_TRANSACTION_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction";
    private static final String RESONANCE_TYPE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType";
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final long RESOLVED_TICK = 1200L;

    @Test
    void switchBenmingSwordResonanceWritesAttachmentStateOnSuccess() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newStateAttachment();
        api.setStateResonanceType(state, "offense");
        api.setStateBurstCooldownUntilTick(state, 0L);

        final Object result = api.switchBenmingSwordResonance(
            state,
            "stable-benming",
            "stable-benming",
            "SPIRIT"
        );

        assertTrue(api.resultSuccess(result));
        assertEquals("NONE", api.resultFailureReason(result));
        assertEquals("spirit", api.resultResonanceType(result));
        assertEquals("spirit", api.getStateResonanceType(state));
    }

    @Test
    void switchBenmingSwordResonanceRejectsNonBenmingTarget() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newStateAttachment();
        api.setStateResonanceType(state, "defense");
        api.setStateBurstCooldownUntilTick(state, 0L);

        final Object result = api.switchBenmingSwordResonance(
            state,
            "stable-other",
            "stable-benming",
            "OFFENSE"
        );

        assertFalse(api.resultSuccess(result));
        assertEquals("TARGET_NOT_CURRENT_BENMING", api.resultFailureReason(result));
        assertEquals("defense", api.resultResonanceType(result));
        assertEquals("defense", api.getStateResonanceType(state));
    }

    @Test
    void attemptBenmingSwordBurstRejectsActiveCooldownWithExplicitReason() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newStateAttachment();
        api.setStateResonanceType(state, "offense");
        api.setStateBurstCooldownUntilTick(state, RESOLVED_TICK + TEST_MAGIC_30L);
        api.setStateBurstActiveUntilTick(state, TEST_MAGIC_1500L);
        api.setStateBurstAftershockUntilTick(state, TEST_MAGIC_1600L);

        final Object result = api.attemptBenmingSwordBurst(
            state,
            "stable-benming",
            "stable-benming",
            RESOLVED_TICK
        );

        assertFalse(api.resultSuccess(result));
        assertEquals("BURST_COOLDOWN_ACTIVE", api.resultFailureReason(result));
        assertEquals(RESOLVED_TICK + TEST_MAGIC_30L, api.resultBurstCooldownUntilTick(result));
        assertEquals(RESOLVED_TICK + TEST_MAGIC_30L, api.getStateBurstCooldownUntilTick(state));
        assertEquals(TEST_MAGIC_1500L, api.getStateBurstActiveUntilTick(state));
        assertEquals(TEST_MAGIC_1600L, api.getStateBurstAftershockUntilTick(state));
    }

    @Test
    void attemptBenmingSwordBurstWritesAuthorityTimelineOnSuccess() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newStateAttachment();
        api.setStateResonanceType(state, "spirit");
        api.setStateBurstCooldownUntilTick(state, 0L);

        final Object result = api.attemptBenmingSwordBurst(
            state,
            "stable-benming",
            "stable-benming",
            RESOLVED_TICK
        );

        assertTrue(api.resultSuccess(result));
        assertEquals("NONE", api.resultFailureReason(result));
        assertEquals(
            api.resolveBurstAttemptCooldownUntilTick(RESOLVED_TICK),
            api.resultBurstCooldownUntilTick(result)
        );
        assertEquals(
            api.resolveBurstActiveUntilTick(RESOLVED_TICK),
            api.getStateBurstActiveUntilTick(state)
        );
        assertEquals(
            api.resolveBurstAftershockUntilTick(
                api.resolveBurstActiveUntilTick(RESOLVED_TICK)
            ),
            api.getStateBurstAftershockUntilTick(state)
        );
        assertEquals(
            api.resolveBurstAttemptCooldownUntilTick(RESOLVED_TICK),
            api.getStateBurstCooldownUntilTick(state)
        );
    }

    @Test
    void mapBurstAvailabilityResultMapsOverloadFailureWithoutMutatingState() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newStateAttachment();
        api.setStateResonanceType(state, "offense");
        api.setStateBurstCooldownUntilTick(state, TEST_MAGIC_1800L);
        api.setStateBurstActiveUntilTick(state, TEST_MAGIC_1810L);
        api.setStateBurstAftershockUntilTick(state, TEST_MAGIC_1820L);

        final Object transactionFailure = api.newTransactionFailureResult(
            "OVERLOAD_LIMIT_EXCEEDED"
        );
        final Object result = api.mapBurstAttemptAvailabilityResult(
            transactionFailure,
            "stable-benming",
            state
        );

        assertFalse(api.resultSuccess(result));
        assertEquals("BURST_OVERLOAD_BLOCKED", api.resultFailureReason(result));
        assertEquals(TEST_MAGIC_1800L, api.getStateBurstCooldownUntilTick(state));
        assertEquals(TEST_MAGIC_1810L, api.getStateBurstActiveUntilTick(state));
        assertEquals(TEST_MAGIC_1820L, api.getStateBurstAftershockUntilTick(state));
    }

    @Test
    void mapBurstAvailabilityResultMapsResourceFailureWithoutMutatingState() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object state = api.newStateAttachment();
        api.setStateResonanceType(state, "spirit");
        api.setStateBurstCooldownUntilTick(state, 0L);
        api.setStateBurstActiveUntilTick(state, TEST_MAGIC_1900L);
        api.setStateBurstAftershockUntilTick(state, TEST_MAGIC_1950L);

        final Object transactionFailure = api.newTransactionFailureResult(
            "INSUFFICIENT_ZHENYUAN"
        );
        final Object result = api.mapBurstAttemptAvailabilityResult(
            transactionFailure,
            "stable-benming",
            state
        );

        assertFalse(api.resultSuccess(result));
        assertEquals("BURST_RESOURCES_INSUFFICIENT", api.resultFailureReason(result));
        assertEquals(0L, api.getStateBurstCooldownUntilTick(state));
        assertEquals(TEST_MAGIC_1900L, api.getStateBurstActiveUntilTick(state));
        assertEquals(TEST_MAGIC_1950L, api.getStateBurstAftershockUntilTick(state));
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> controllerClass;
        private final Class<?> stateAttachmentClass;
        private final Class<?> transactionResultClass;
        private final Class<?> transactionFailureReasonClass;
        private final Class<?> resonanceTypeClass;
        private final Method switchBenmingSwordResonanceMethod;
        private final Method attemptBenmingSwordBurstMethod;
        private final Method mapBurstAttemptAvailabilityResultMethod;
        private final Method resolveBurstAttemptCooldownUntilTickMethod;
        private final Method resolveBurstActiveUntilTickMethod;
        private final Method resolveBurstAftershockUntilTickMethod;
        private final Method transactionFailureFactoryMethod;

        private RuntimeApi(
            final Class<?> controllerClass,
            final Class<?> stateAttachmentClass,
            final Class<?> transactionResultClass,
            final Class<?> transactionFailureReasonClass,
            final Class<?> resonanceTypeClass
        ) throws ReflectiveOperationException {
            this.controllerClass = controllerClass;
            this.stateAttachmentClass = stateAttachmentClass;
            this.transactionResultClass = transactionResultClass;
            this.transactionFailureReasonClass = transactionFailureReasonClass;
            this.resonanceTypeClass = resonanceTypeClass;
            this.switchBenmingSwordResonanceMethod = controllerClass.getDeclaredMethod(
                "switchBenmingSwordResonance",
                stateAttachmentClass,
                String.class,
                String.class,
                resonanceTypeClass
            );
            this.attemptBenmingSwordBurstMethod = controllerClass.getDeclaredMethod(
                "attemptBenmingSwordBurst",
                stateAttachmentClass,
                String.class,
                String.class,
                long.class
            );
            this.mapBurstAttemptAvailabilityResultMethod = controllerClass.getDeclaredMethod(
                "mapBurstAttemptAvailabilityResult",
                transactionResultClass,
                String.class,
                stateAttachmentClass
            );
            this.resolveBurstActiveUntilTickMethod = controllerClass.getDeclaredMethod(
                "resolveBurstActiveUntilTick",
                long.class
            );
            this.resolveBurstAftershockUntilTickMethod = controllerClass.getDeclaredMethod(
                "resolveBurstAftershockUntilTick",
                long.class
            );
            this.resolveBurstAttemptCooldownUntilTickMethod = controllerClass.getDeclaredMethod(
                "resolveBurstAttemptCooldownUntilTick",
                long.class
            );
            this.transactionFailureFactoryMethod = transactionResultClass.getDeclaredMethod(
                "failure",
                transactionFailureReasonClass,
                double.class,
                double.class,
                double.class
            );
            this.switchBenmingSwordResonanceMethod.setAccessible(true);
            this.attemptBenmingSwordBurstMethod.setAccessible(true);
            this.mapBurstAttemptAvailabilityResultMethod.setAccessible(true);
            this.resolveBurstActiveUntilTickMethod.setAccessible(true);
            this.resolveBurstAftershockUntilTickMethod.setAccessible(true);
            this.resolveBurstAttemptCooldownUntilTickMethod.setAccessible(true);
            this.transactionFailureFactoryMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            return new RuntimeApi(
                Class.forName(CONTROLLER_CLASS_NAME, true, loader),
                Class.forName(STATE_ATTACHMENT_CLASS_NAME, true, loader),
                Class.forName(RESOURCE_TRANSACTION_CLASS_NAME + "$Result", true, loader),
                Class.forName(
                    RESOURCE_TRANSACTION_CLASS_NAME + "$FailureReason",
                    true,
                    loader
                ),
                Class.forName(RESONANCE_TYPE_CLASS_NAME, true, loader)
            );
        }

        Object newStateAttachment() throws Exception {
            return stateAttachmentClass.getConstructor().newInstance();
        }

        void setStateResonanceType(final Object state, final String rawType) throws Exception {
            stateAttachmentClass.getMethod("setResonanceType", String.class).invoke(state, rawType);
        }

        String getStateResonanceType(final Object state) throws Exception {
            return (String) stateAttachmentClass.getMethod("getResonanceType").invoke(state);
        }

        void setStateBurstCooldownUntilTick(final Object state, final long tick)
            throws Exception {
            stateAttachmentClass.getMethod("setBurstCooldownUntilTick", long.class)
                .invoke(state, tick);
        }

        void setStateBurstActiveUntilTick(final Object state, final long tick) throws Exception {
            stateAttachmentClass.getMethod("setBurstActiveUntilTick", long.class)
                .invoke(state, tick);
        }

        void setStateBurstAftershockUntilTick(final Object state, final long tick)
            throws Exception {
            stateAttachmentClass.getMethod("setBurstAftershockUntilTick", long.class)
                .invoke(state, tick);
        }

        long getStateBurstCooldownUntilTick(final Object state) throws Exception {
            return (long) stateAttachmentClass.getMethod("getBurstCooldownUntilTick")
                .invoke(state);
        }

        long getStateBurstActiveUntilTick(final Object state) throws Exception {
            return (long) stateAttachmentClass.getMethod("getBurstActiveUntilTick")
                .invoke(state);
        }

        long getStateBurstAftershockUntilTick(final Object state) throws Exception {
            return (long) stateAttachmentClass.getMethod("getBurstAftershockUntilTick")
                .invoke(state);
        }

        Object switchBenmingSwordResonance(
            final Object state,
            final String resolvedSwordId,
            final String bondedSwordId,
            final String resonanceEnumName
        ) throws Exception {
            final Class<? extends Enum> enumClass = resonanceTypeClass.asSubclass(Enum.class);
            final Method valueOfMethod = Enum.class.getMethod(
                "valueOf",
                Class.class,
                String.class
            );
            final Object resonanceType = valueOfMethod.invoke(
                null,
                enumClass,
                resonanceEnumName
            );
            return switchBenmingSwordResonanceMethod.invoke(
                null,
                state,
                resolvedSwordId,
                bondedSwordId,
                resonanceType
            );
        }

        Object attemptBenmingSwordBurst(
            final Object state,
            final String resolvedSwordId,
            final String bondedSwordId,
            final long resolvedTick
        ) throws Exception {
            return attemptBenmingSwordBurstMethod.invoke(
                null,
                state,
                resolvedSwordId,
                bondedSwordId,
                resolvedTick
            );
        }

        Object newTransactionFailureResult(final String failureReasonName) throws Exception {
            return transactionFailureFactoryMethod.invoke(
                null,
                enumValue(transactionFailureReasonClass, failureReasonName),
                0.0D,
                0.0D,
                0.0D
            );
        }

        Object mapBurstAttemptAvailabilityResult(
            final Object transactionResult,
            final String resolvedSwordId,
            final Object state
        ) throws Exception {
            return mapBurstAttemptAvailabilityResultMethod.invoke(
                null,
                transactionResult,
                resolvedSwordId,
                state
            );
        }

        long resolveBurstAttemptCooldownUntilTick(final long resolvedTick) throws Exception {
            return (long) resolveBurstAttemptCooldownUntilTickMethod.invoke(null, resolvedTick);
        }

        long resolveBurstActiveUntilTick(final long resolvedTick) throws Exception {
            return (long) resolveBurstActiveUntilTickMethod.invoke(null, resolvedTick);
        }

        long resolveBurstAftershockUntilTick(final long burstActiveUntilTick)
            throws Exception {
            return (long) resolveBurstAftershockUntilTickMethod.invoke(
                null,
                burstActiveUntilTick
            );
        }

        boolean resultSuccess(final Object result) throws Exception {
            return (boolean) result.getClass().getMethod("success").invoke(result);
        }

        String resultFailureReason(final Object result) throws Exception {
            return ((Enum<?>) result.getClass().getMethod("failureReason").invoke(result)).name();
        }

        String resultResonanceType(final Object result) throws Exception {
            return (String) result.getClass().getMethod("resonanceType").invoke(result);
        }

        long resultBurstCooldownUntilTick(final Object result) throws Exception {
            return (long) result.getClass().getMethod("burstCooldownUntilTick").invoke(result);
        }

        private static Object enumValue(final Class<?> enumClass, final String enumName)
            throws Exception {
            final Method valueOfMethod = Enum.class.getMethod(
                "valueOf",
                Class.class,
                String.class
            );
            return valueOfMethod.invoke(null, enumClass.asSubclass(Enum.class), enumName);
        }
        private static URLClassLoader buildRuntimeClassLoader() throws IOException {
            final List<URL> urls = new ArrayList<>();
            urls.add(MAIN_CLASSES.toAbsolutePath().toUri().toURL());
            if (MAIN_RESOURCES.toAbsolutePath().toFile().exists()) {
                urls.add(MAIN_RESOURCES.toAbsolutePath().toUri().toURL());
            }

            final Properties props = new Properties();
            try (InputStream input = Files.newInputStream(ARTIFACT_MANIFEST.toAbsolutePath())) {
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
            return new URLClassLoader(
                urls.toArray(new URL[0]),
                ClassLoader.getPlatformClassLoader()
            );
        }

        private static synchronized Path resolveMinecraftRuntimeJar() throws IOException {
            if (cachedMinecraftJarPath != null && cachedMinecraftJarPath.toFile().exists()) {
                return cachedMinecraftJarPath;
            }

            final List<Path> searchRoots = List.of(
                Path.of(
                    System.getProperty("user.home"),
                    ".gradle",
                    "caches",
                    "neoformruntime",
                    "intermediate_results"
                ),
                Path.of(
                    System.getProperty("user.home"),
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
}
