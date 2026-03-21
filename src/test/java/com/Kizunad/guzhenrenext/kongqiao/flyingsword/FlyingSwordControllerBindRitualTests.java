package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordControllerBindRitualTests {


    private static final int TEST_MAGIC_4 = 4;
    private static final double TEST_MAGIC_12_5D = 12.5D;
    private static final long TEST_MAGIC_20L = 20L;
    private static final int TEST_MAGIC_6 = 6;

    private static final String CONTROLLER_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController";
    private static final String BOND_SERVICE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService";
    private static final String RESOURCE_TRANSACTION_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction";
    private static final String SNAPSHOT_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot";
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final String OWNER_UUID = "player-owner-001";
    private static final long RESOLVED_TICK = 1200L;
    private static final double RITUAL_ZHENYUAN_COST = 10.0D;
    private static final double RITUAL_NIANTOU_COST = 6.0D;
    private static final double RITUAL_HUNPO_COST = 4.0D;
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path CONTROLLER_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/kongqiao/flyingsword/FlyingSwordController.java"
    );
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");

    @Test
    void resolveSelectedOrNearestCandidatePrefersSelectedSword() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID selectedUuid = UUID.randomUUID();
        final String selectedValue = "selected-sword";
        final String nearestValue = "nearest-sword";
        final AtomicBoolean cleared = new AtomicBoolean(false);
        final AtomicReference<Object> remembered = new AtomicReference<>();

        final Object result = api.resolveSelectedOrNearestCandidate(
            Optional.of(selectedUuid),
            swordUuid -> swordUuid.equals(selectedUuid) ? selectedValue : null,
            () -> cleared.set(true),
            () -> nearestValue,
            remembered::set
        );

        assertEquals(selectedValue, result);
        assertFalse(cleared.get());
        assertNull(remembered.get());
    }

    @Test
    void resolveSelectedOrNearestCandidateFallsBackToNearestWhenSelectedTargetInvalid()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID selectedUuid = UUID.randomUUID();
        final String nearestValue = "nearest-sword";
        final AtomicBoolean cleared = new AtomicBoolean(false);
        final AtomicReference<Object> remembered = new AtomicReference<>();

        final Object result = api.resolveSelectedOrNearestCandidate(
            Optional.of(selectedUuid),
            swordUuid -> null,
            () -> cleared.set(true),
            () -> nearestValue,
            remembered::set
        );

        assertEquals(nearestValue, result);
        assertTrue(cleared.get());
        assertEquals(nearestValue, remembered.get());
    }

    @Test
    void resolveStrictSelectedCandidatePrefersSelectedSword() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID selectedUuid = UUID.randomUUID();
        final String selectedValue = "selected-sword";
        final AtomicBoolean cleared = new AtomicBoolean(false);

        final Object result = api.resolveStrictSelectedCandidate(
            Optional.of(selectedUuid),
            swordUuid -> swordUuid.equals(selectedUuid) ? selectedValue : null,
            () -> cleared.set(true)
        );

        assertEquals(selectedValue, result);
        assertFalse(cleared.get());
    }

    @Test
    void resolveStrictSelectedCandidateReturnsNullWhenSelectedTargetInvalid() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final UUID selectedUuid = UUID.randomUUID();
        final AtomicBoolean cleared = new AtomicBoolean(false);

        final Object result = api.resolveStrictSelectedCandidate(
            Optional.of(selectedUuid),
            swordUuid -> null,
            () -> cleared.set(true)
        );

        assertNull(result);
        assertTrue(cleared.get());
    }

    @Test
    void bindSwordAsBenmingWithRitualBindsResolvedSelectedTarget() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final SwordState nearestSword = new SwordState("stable-nearest", "", 0.15D);
        final SwordState selectedSword = new SwordState("stable-selected", "", 0.45D);
        final CacheState cacheState = new CacheState();
        final RitualRequestState requestState = new RitualRequestState();
        final MutationState mutationState = MutationState.phaseWritable();

        final Object result = api.bindSwordAsBenmingWithRitual(
            OWNER_UUID,
            selectedSword,
            List.of(nearestSword, selectedSword),
            cacheState,
            requestState,
            mutationState,
            RESOLVED_TICK
        );

        assertTrue(api.resultSuccess(result));
        assertEquals("RITUAL_BIND", api.resultBranch(result));
        assertEquals("stable-selected", api.resultStableSwordId(result));
        assertEquals(OWNER_UUID, selectedSword.bondOwnerUuid);
        assertEquals("", nearestSword.bondOwnerUuid);
        assertEquals(0.0D, selectedSword.bondResonance);
        assertEquals("stable-selected", cacheState.bondedSwordId);
        assertFalse(cacheState.dirty);
        assertFalse(requestState.executionPending);
        assertEquals(TEST_MAGIC_4, mutationState.operations.size());
        assertEquals(
            RESOLVED_TICK + api.defaultRitualDuplicateGuardTicks(),
            mutationState.ritualLockUntilTick
        );
        assertTrue(mutationState.operations.contains("setRitualLockUntilTick"));
    }

    @Test
    void bindSwordAsBenmingWithRitualReturnsExplicitFailureReason() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final SwordState alreadyBoundSword = new SwordState("stable-bound", OWNER_UUID, 0.90D);
        final SwordState targetSword = new SwordState("stable-target", "", 0.15D);
        final CacheState cacheState = new CacheState();
        final RitualRequestState requestState = new RitualRequestState();
        final MutationState mutationState = new MutationState();

        final Object result = api.bindSwordAsBenmingWithRitual(
            OWNER_UUID,
            targetSword,
            List.of(alreadyBoundSword, targetSword),
            cacheState,
            requestState,
            mutationState,
            RESOLVED_TICK
        );

        assertFalse(api.resultSuccess(result));
        assertEquals("RITUAL_PRECHECK", api.resultBranch(result));
        assertNotEquals("NONE", api.resultFailureReason(result));
        assertEquals("PLAYER_ALREADY_HAS_BONDED_SWORD", api.resultFailureReason(result));
        assertEquals("stable-bound", api.resultStableSwordId(result));
        assertEquals("", targetSword.bondOwnerUuid);
        assertEquals(0, mutationState.operations.size());
    }

    @Test
    void createDefaultRitualBindRequestRejectsActiveRitualLockWithoutSpendingResources()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final MutationState mutationState = MutationState.phaseWritable();
        final Object state = api.newRuntimeStateAttachment();
        api.setRuntimeStateOverload(state, TEST_MAGIC_12_5D);
        api.setRuntimeStateBurstCooldownUntilTick(state, 0L);
        api.setRuntimeStateRitualLockUntilTick(state, RESOLVED_TICK + TEST_MAGIC_20L);

        final Object request = api.createDefaultRitualBindRequest(state, RESOLVED_TICK);
        final Object result = api.tryConsumePhaseAwareRequest(request, mutationState);

        assertFalse(api.transactionResultSuccess(result));
        assertEquals("RITUAL_LOCK_ACTIVE", api.transactionFailureReason(result));
        assertEquals(0, mutationState.operations.size());
        assertEquals(0L, mutationState.ritualLockUntilTick);
    }

    @Test
    void bindSelectedOrNearestSwordAsBenmingKeepsStrictSelectionAndDedicatedMissingSelectionFailure()
        throws Exception {
        final String methodBody = extractMethodBody(
            Files.readString(CONTROLLER_SOURCE),
            "public static BenmingSwordBondService.Result bindSelectedOrNearestSwordAsBenming(",
            "public static BenmingSwordBondService.Result activeUnbindSelectedOrNearestBenmingSword("
        );

        assertTrue(methodBody.contains("getStrictSelectedSword(level, owner)"));
        assertTrue(methodBody.contains("FailureReason.NO_SELECTED_SWORD"));
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> controllerClass;
        private final Class<?> swordBondPortClass;
        private final Class<?> playerBondCachePortClass;
        private final Class<?> ritualRequestStatePortClass;
        private final Class<?> ritualRequestContextClass;
        private final Class<?> ritualBindTransactionContextClass;
        private final Class<?> transactionMutationPortClass;
        private final Class<?> costScalerClass;
        private final Class<?> requestClass;
        private final Class<?> snapshotClass;
        private final Class<?> stateAttachmentClass;
        private final Method resolveSelectedOrNearestCandidateMethod;
        private final Method resolveStrictSelectedCandidateMethod;
        private final Method bindSwordAsBenmingWithRitualMethod;
        private final Method createDefaultRitualBindRequestMethod;
        private final Method defaultRitualDuplicateGuardTicksMethod;
        private final Method snapshotOfMethod;
        private final Method tryConsumeMethod;

        private RuntimeApi(final RuntimeApiDeps deps) throws ReflectiveOperationException {
            this.controllerClass = deps.controllerClass();
            this.swordBondPortClass = deps.swordBondPortClass();
            this.playerBondCachePortClass = deps.playerBondCachePortClass();
            this.ritualRequestStatePortClass = deps.ritualRequestStatePortClass();
            this.ritualRequestContextClass = deps.ritualRequestContextClass();
            this.ritualBindTransactionContextClass = deps.ritualBindTransactionContextClass();
            this.transactionMutationPortClass = deps.transactionMutationPortClass();
            this.costScalerClass = deps.costScalerClass();
            this.requestClass = deps.requestClass();
            this.snapshotClass = deps.snapshotClass();
            this.stateAttachmentClass = deps.stateAttachmentClass();
            this.resolveSelectedOrNearestCandidateMethod = controllerClass.getDeclaredMethod(
                "resolveSelectedOrNearestCandidate",
                Optional.class,
                java.util.function.Function.class,
                Runnable.class,
                java.util.function.Supplier.class,
                java.util.function.Consumer.class
            );
            this.resolveStrictSelectedCandidateMethod = controllerClass.getDeclaredMethod(
                "resolveStrictSelectedCandidate",
                Optional.class,
                java.util.function.Function.class,
                Runnable.class
            );
            this.bindSwordAsBenmingWithRitualMethod = controllerClass.getDeclaredMethod(
                "bindSwordAsBenmingWithRitual",
                String.class,
                swordBondPortClass,
                List.class,
                playerBondCachePortClass,
                ritualBindTransactionContextClass,
                long.class
            );
            this.createDefaultRitualBindRequestMethod = controllerClass.getDeclaredMethod(
                "createDefaultRitualBindRequest",
                stateAttachmentClass,
                long.class
            );
            this.defaultRitualDuplicateGuardTicksMethod = deps.bondServiceClass().getMethod(
                "defaultRitualDuplicateGuardTicks"
            );
            this.snapshotOfMethod = snapshotClass.getMethod(
                "of",
                double.class,
                double.class,
                double.class,
                double.class,
                int.class
            );
            this.tryConsumeMethod = Class.forName(
                RESOURCE_TRANSACTION_CLASS_NAME,
                true,
                controllerClass.getClassLoader()
            ).getMethod(
                "tryConsume",
                snapshotClass,
                requestClass,
                double.class,
                costScalerClass,
                transactionMutationPortClass
            );
            this.resolveSelectedOrNearestCandidateMethod.setAccessible(true);
            this.resolveStrictSelectedCandidateMethod.setAccessible(true);
            this.bindSwordAsBenmingWithRitualMethod.setAccessible(true);
            this.createDefaultRitualBindRequestMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> controllerClass = Class.forName(CONTROLLER_CLASS_NAME, true, loader);
            final Class<?> bondServiceClass = Class.forName(BOND_SERVICE_CLASS_NAME, true, loader);
            final Class<?> resourceTransactionClass = Class.forName(
                RESOURCE_TRANSACTION_CLASS_NAME,
                true,
                loader
            );
            final Class<?> swordBondPortClass = Class.forName(
                BOND_SERVICE_CLASS_NAME + "$SwordBondPort",
                true,
                loader
            );
            final Class<?> playerBondCachePortClass = Class.forName(
                BOND_SERVICE_CLASS_NAME + "$PlayerBondCachePort",
                true,
                loader
            );
            final Class<?> ritualRequestStatePortClass = Class.forName(
                BOND_SERVICE_CLASS_NAME + "$RitualRequestStatePort",
                true,
                loader
            );
            final Class<?> ritualRequestContextClass = Class.forName(
                BOND_SERVICE_CLASS_NAME + "$RitualRequestContext",
                true,
                loader
            );
            final Class<?> ritualBindTransactionContextClass = Class.forName(
                BOND_SERVICE_CLASS_NAME + "$RitualBindTransactionContext",
                true,
                loader
            );
            final Class<?> transactionMutationPortClass = Class.forName(
                RESOURCE_TRANSACTION_CLASS_NAME + "$TransactionMutationPort",
                true,
                loader
            );
            final Class<?> costScalerClass = Class.forName(
                RESOURCE_TRANSACTION_CLASS_NAME + "$CostScaler",
                true,
                loader
            );
            final Class<?> requestClass = Class.forName(
                RESOURCE_TRANSACTION_CLASS_NAME + "$Request",
                true,
                loader
            );
            final Class<?> snapshotClass = Class.forName(SNAPSHOT_CLASS_NAME, true, loader);
            final Class<?> stateAttachmentClass = Class.forName(
                "com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment",
                true,
                loader
            );
            return new RuntimeApi(
                new RuntimeApiDeps(
                    controllerClass,
                    swordBondPortClass,
                    playerBondCachePortClass,
                    ritualRequestStatePortClass,
                    ritualRequestContextClass,
                    ritualBindTransactionContextClass,
                    transactionMutationPortClass,
                    costScalerClass,
                    requestClass,
                    snapshotClass,
                    stateAttachmentClass,
                    bondServiceClass
                )
            );
        }

        private record RuntimeApiDeps(
            Class<?> controllerClass,
            Class<?> swordBondPortClass,
            Class<?> playerBondCachePortClass,
            Class<?> ritualRequestStatePortClass,
            Class<?> ritualRequestContextClass,
            Class<?> ritualBindTransactionContextClass,
            Class<?> transactionMutationPortClass,
            Class<?> costScalerClass,
            Class<?> requestClass,
            Class<?> snapshotClass,
            Class<?> stateAttachmentClass,
            Class<?> bondServiceClass
        ) {}

        Object resolveSelectedOrNearestCandidate(
            final Optional<UUID> selectedId,
            final java.util.function.Function<UUID, Object> selectedResolver,
            final Runnable clearSelection,
            final java.util.function.Supplier<Object> nearestSupplier,
            final java.util.function.Consumer<Object> rememberNearest
        ) throws Exception {
            return resolveSelectedOrNearestCandidateMethod.invoke(
                null,
                selectedId,
                selectedResolver,
                clearSelection,
                nearestSupplier,
                rememberNearest
            );
        }

        Object resolveStrictSelectedCandidate(
            final Optional<UUID> selectedId,
            final java.util.function.Function<UUID, Object> selectedResolver,
            final Runnable clearSelection
        ) throws Exception {
            return resolveStrictSelectedCandidateMethod.invoke(
                null,
                selectedId,
                selectedResolver,
                clearSelection
            );
        }

        Object bindSwordAsBenmingWithRitual(
            final String ownerUuid,
            final SwordState targetSword,
            final List<SwordState> swords,
            final CacheState cacheState,
            final RitualRequestState requestState,
            final MutationState mutationState,
            final long resolvedTick
        ) throws Exception {
            final Object swordProxy = newSwordBondPort(targetSword);
            final List<Object> swordProxies = new ArrayList<>();
            for (SwordState swordState : swords) {
                swordProxies.add(newSwordBondPort(swordState));
            }
            final Object cacheProxy = newPlayerBondCachePort(cacheState);
            final Object requestStateProxy = newRitualRequestStatePort(requestState);
            final Object mutationProxy = newTransactionMutationPort(mutationState);
            final Object context = newRitualBindTransactionContext(
                requestStateProxy,
                mutationProxy,
                resolvedTick
            );
            return bindSwordAsBenmingWithRitualMethod.invoke(
                null,
                ownerUuid,
                swordProxy,
                swordProxies,
                cacheProxy,
                context,
                resolvedTick
            );
        }

        boolean resultSuccess(final Object result) throws Exception {
            return (boolean) result.getClass().getMethod("success").invoke(result);
        }

        String resultBranch(final Object result) throws Exception {
            return ((Enum<?>) result.getClass().getMethod("branch").invoke(result)).name();
        }

        String resultFailureReason(final Object result) throws Exception {
            return ((Enum<?>) result.getClass().getMethod("failureReason").invoke(result)).name();
        }

        String resultStableSwordId(final Object result) throws Exception {
            return (String) result.getClass().getMethod("stableSwordId").invoke(result);
        }

        int defaultRitualDuplicateGuardTicks() throws Exception {
            return (int) defaultRitualDuplicateGuardTicksMethod.invoke(null);
        }

        Object newRuntimeStateAttachment() throws Exception {
            return stateAttachmentClass.getConstructor().newInstance();
        }

        void setRuntimeStateOverload(final Object state, final double overload) throws Exception {
            stateAttachmentClass.getMethod("setOverload", double.class).invoke(state, overload);
        }

        void setRuntimeStateBurstCooldownUntilTick(final Object state, final long tick)
            throws Exception {
            stateAttachmentClass.getMethod("setBurstCooldownUntilTick", long.class)
                .invoke(state, tick);
        }

        void setRuntimeStateRitualLockUntilTick(final Object state, final long tick)
            throws Exception {
            stateAttachmentClass.getMethod("setRitualLockUntilTick", long.class)
                .invoke(state, tick);
        }

        Object createDefaultRitualBindRequest(final Object state, final long resolvedTick)
            throws Exception {
            return createDefaultRitualBindRequestMethod.invoke(null, state, resolvedTick);
        }

        Object tryConsumePhaseAwareRequest(final Object request, final MutationState mutationState)
            throws Exception {
            final Object snapshot = snapshotOfMethod.invoke(
                null,
                120.0D,
                120.0D,
                120.0D,
                0.0D,
                0
            );
            final Object mutationProxy = newTransactionMutationPort(mutationState);
            final Object costScaler = Proxy.newProxyInstance(
                controllerClass.getClassLoader(),
                new Class<?>[] {costScalerClass},
                (proxy, method, args) -> {
                    if (isObjectMethod(method)) {
                        return handleObjectMethod(proxy, method, args, "CostScalerProxy");
                    }
                    return args == null || args.length == 0 ? 0.0D : args[0];
                }
            );
            return tryConsumeMethod.invoke(
                null,
                snapshot,
                request,
                RITUAL_ZHENYUAN_COST,
                costScaler,
                mutationProxy
            );
        }

        boolean transactionResultSuccess(final Object result) throws Exception {
            return (boolean) result.getClass().getMethod("success").invoke(result);
        }

        String transactionFailureReason(final Object result) throws Exception {
            return ((Enum<?>) result.getClass().getMethod("failureReason").invoke(result)).name();
        }

        private Object newSwordBondPort(final SwordState swordState) {
            return Proxy.newProxyInstance(
                controllerClass.getClassLoader(),
                new Class<?>[] {swordBondPortClass},
                new SwordBondPortHandler(swordState)
            );
        }

        private Object newPlayerBondCachePort(final CacheState cacheState) {
            return Proxy.newProxyInstance(
                controllerClass.getClassLoader(),
                new Class<?>[] {playerBondCachePortClass},
                new PlayerBondCachePortHandler(cacheState)
            );
        }

        private Object newRitualRequestStatePort(final RitualRequestState requestState) {
            return Proxy.newProxyInstance(
                controllerClass.getClassLoader(),
                new Class<?>[] {ritualRequestStatePortClass},
                new RitualRequestStatePortHandler(requestState)
            );
        }

        private Object newTransactionMutationPort(final MutationState mutationState) {
            return Proxy.newProxyInstance(
                controllerClass.getClassLoader(),
                new Class<?>[] {transactionMutationPortClass},
                new TransactionMutationPortHandler(mutationState)
            );
        }

        private Object newRitualBindTransactionContext(
            final Object requestStateProxy,
            final Object mutationProxy,
            final long resolvedTick
        ) throws Exception {
            final int duplicateGuardTicks =
                (int) defaultRitualDuplicateGuardTicksMethod.invoke(null);
            final Object requestContext = ritualRequestContextClass
                .getConstructor(ritualRequestStatePortClass, int.class)
                .newInstance(requestStateProxy, duplicateGuardTicks);
            final Object defaultState = newRuntimeStateAttachment();
            final Object request = createDefaultRitualBindRequest(defaultState, resolvedTick);
            final Object snapshot = snapshotOfMethod.invoke(
                null,
                120.0D,
                120.0D,
                120.0D,
                0.0D,
                0
            );
            final Object costScaler = Proxy.newProxyInstance(
                controllerClass.getClassLoader(),
                new Class<?>[] {costScalerClass},
                (proxy, method, args) -> {
                    if (isObjectMethod(method)) {
                        return handleObjectMethod(proxy, method, args, "CostScalerProxy");
                    }
                    return args == null || args.length == 0 ? 0.0D : args[0];
                }
            );
            return ritualBindTransactionContextClass
                .getConstructor(
                    snapshotClass,
                    requestClass,
                    double.class,
                    costScalerClass,
                    transactionMutationPortClass,
                    ritualRequestContextClass
                )
                .newInstance(
                    snapshot,
                    request,
                    RITUAL_ZHENYUAN_COST,
                    costScaler,
                    mutationProxy,
                    requestContext
                );
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

    private static final class SwordState {

        private final String stableSwordId;
        private String bondOwnerUuid;
        private double bondResonance;

        private SwordState(
            final String stableSwordId,
            final String bondOwnerUuid,
            final double bondResonance
        ) {
            this.stableSwordId = stableSwordId;
            this.bondOwnerUuid = bondOwnerUuid;
            this.bondResonance = bondResonance;
        }
    }

    private static final class CacheState {

        private String bondedSwordId = "";
        private boolean dirty = true;
        private long lastResolvedTick = -1L;
    }

    private static final class RitualRequestState {

        private String lockedSwordId = "";
        private long lockedUntilTick;
        private boolean executionPending;
    }

    private static final class MutationState {

        private final List<String> operations = new ArrayList<>();
        private boolean phaseStateWritesSupported;
        private double overload;
        private long burstCooldownUntilTick;
        private long ritualLockUntilTick;

        private static MutationState phaseWritable() {
            final MutationState state = new MutationState();
            state.phaseStateWritesSupported = true;
            return state;
        }
    }

    private static final class SwordBondPortHandler implements InvocationHandler {

        private final SwordState swordState;

        private SwordBondPortHandler(final SwordState swordState) {
            this.swordState = swordState;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            if (isObjectMethod(method)) {
                return handleObjectMethod(proxy, method, args, swordState.stableSwordId);
            }
            return switch (method.getName()) {
                case "getStableSwordId" -> swordState.stableSwordId;
                case "getBondOwnerUuid" -> swordState.bondOwnerUuid;
                case "getBondResonance" -> swordState.bondResonance;
                case "setBondOwnerUuid" -> {
                    swordState.bondOwnerUuid = args == null ? "" : (String) args[0];
                    yield null;
                }
                case "setBondResonance" -> {
                    swordState.bondResonance = args == null ? 0.0D : (double) args[0];
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }

    private static final class PlayerBondCachePortHandler implements InvocationHandler {

        private final CacheState cacheState;

        private PlayerBondCachePortHandler(final CacheState cacheState) {
            this.cacheState = cacheState;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            if (isObjectMethod(method)) {
                return handleObjectMethod(proxy, method, args, "PlayerBondCachePortProxy");
            }
            return switch (method.getName()) {
                case "getBondedSwordId" -> cacheState.bondedSwordId;
                case "isBondCacheDirty" -> cacheState.dirty;
                case "updateBondCache" -> {
                    cacheState.bondedSwordId = args == null ? "" : (String) args[0];
                    cacheState.lastResolvedTick = args == null ? -1L : (long) args[1];
                    cacheState.dirty = false;
                    yield null;
                }
                case "markBondCacheDirty" -> {
                    cacheState.dirty = true;
                    yield null;
                }
                case "clearBondCache" -> {
                    cacheState.bondedSwordId = "";
                    cacheState.lastResolvedTick = -1L;
                    cacheState.dirty = true;
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }

    private static final class RitualRequestStatePortHandler implements InvocationHandler {

        private final RitualRequestState requestState;

        private RitualRequestStatePortHandler(final RitualRequestState requestState) {
            this.requestState = requestState;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            if (isObjectMethod(method)) {
                return handleObjectMethod(proxy, method, args, "RitualRequestStatePortProxy");
            }
            return switch (method.getName()) {
                case "getLockedSwordId" -> requestState.lockedSwordId;
                case "getLockedUntilTick" -> requestState.lockedUntilTick;
                case "isExecutionPending" -> requestState.executionPending;
                case "beginRitualRequest" -> {
                    requestState.lockedSwordId = args == null ? "" : (String) args[0];
                    requestState.lockedUntilTick = args == null ? 0L : (long) args[1];
                    requestState.executionPending = true;
                    yield null;
                }
                case "markExecutionConsumed" -> {
                    requestState.executionPending = false;
                    yield null;
                }
                case "clearRitualRequest" -> {
                    requestState.lockedSwordId = "";
                    requestState.lockedUntilTick = 0L;
                    requestState.executionPending = false;
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }

    private static final class TransactionMutationPortHandler implements InvocationHandler {

        private final MutationState mutationState;

        private TransactionMutationPortHandler(final MutationState mutationState) {
            this.mutationState = mutationState;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            if (isObjectMethod(method)) {
                return handleObjectMethod(proxy, method, args, "TransactionMutationPortProxy");
            }
            return switch (method.getName()) {
                case "spendZhenyuan", "spendNiantou", "spendHunpo",
                    "refundZhenyuan", "refundNiantou", "refundHunpo" -> {
                    mutationState.operations.add(method.getName());
                    yield null;
                }
                case "supportsResourceRollback" -> true;
                case "supportsPhaseStateWrites" -> mutationState.phaseStateWritesSupported;
                case "setOverload" -> {
                    mutationState.overload = args == null ? 0.0D : (double) args[0];
                    mutationState.operations.add("setOverload");
                    yield null;
                }
                case "setBurstCooldownUntilTick" -> {
                    mutationState.burstCooldownUntilTick = args == null ? 0L : (long) args[0];
                    mutationState.operations.add("setBurstCooldownUntilTick");
                    yield null;
                }
                case "setRitualLockUntilTick" -> {
                    mutationState.ritualLockUntilTick = args == null ? 0L : (long) args[0];
                    mutationState.operations.add("setRitualLockUntilTick");
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }

    private static boolean isObjectMethod(final Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    private static Object handleObjectMethod(
        final Object proxy,
        final Method method,
        final Object[] args,
        final String label
    ) {
        return switch (method.getName()) {
            case "toString" -> label;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == (args == null ? null : args[0]);
            default -> throw new UnsupportedOperationException(method.getName());
        };
    }

    private static String extractMethodBody(
        final String source,
        final String startMarker,
        final String endMarker
    ) {
        final int start = source.indexOf(startMarker);
        final int end = source.indexOf(endMarker, start);
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalStateException("无法从源码中提取目标方法片段");
        }
        return source.substring(start, end);
    }
}
