package com.Kizunad.guzhenrenext.xianqiao.data;

import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureWorldDataMigrationTests {

    private static final int CURRENT_SCHEMA_VERSION = 1;

    private static final int TAG_COMPOUND = 10;

    private static final String KEY_NEXT_INDEX = "nextIndex";

    private static final String KEY_SCHEMA_VERSION = "schemaVersion";

    private static final String KEY_APERTURES = "apertures";

    private static final String KEY_INITIALIZED_APERTURES = "initializedApertures";

    private static final String KEY_APERTURE_INIT_STATES = "apertureInitStates";

    private static final String KEY_OWNER = "owner";

    private static final String KEY_INFO = "info";

    private static final String KEY_INIT_STATE = "initState";

    private static final String KEY_INIT_PHASE = "initPhase";

    private static final String KEY_OPENING_SNAPSHOT = "openingSnapshot";

    private static final String KEY_LAYOUT_VERSION = "layoutVersion";

    private static final String KEY_PLAN_SEED = "planSeed";

    private static final String KEY_CENTER_X = "centerX";

    private static final String KEY_CENTER_Y = "centerY";

    private static final String KEY_CENTER_Z = "centerZ";

    private static final String KEY_MIN_CHUNK_X = "minChunkX";

    private static final String KEY_MAX_CHUNK_X = "maxChunkX";

    private static final String KEY_MIN_CHUNK_Z = "minChunkZ";

    private static final String KEY_MAX_CHUNK_Z = "maxChunkZ";

    private static final String KEY_TIME_SPEED = "timeSpeed";

    private static final String KEY_NEXT_TRIBULATION_TICK = "nextTribulationTick";

    private static final String KEY_IS_FROZEN = "isFrozen";

    private static final String KEY_FAVORABILITY = "favorability";

    private static final String KEY_TIER = "tier";

    private static final NbtHarness NBT = new NbtHarness();

    @Test
    void legacyInitializedSaveLoadsAsCompletedPhase() throws Exception {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000101");
        ApertureShape legacyShape = new ApertureShape(160, 92, -32, 9, 11, -3, -1, 1.25F, 48000L, false, 66.0F, 3);

        Object legacyRoot = NBT.compound();
        NBT.putInt(legacyRoot, KEY_NEXT_INDEX, 7);
        NBT.put(legacyRoot, KEY_APERTURES, NBT.singleEntryList(legacyApertureEntry(owner, legacyShape)));
        NBT.put(legacyRoot, KEY_INITIALIZED_APERTURES, NBT.singleEntryList(ownerEntry(owner)));

        Object loaded = NBT.loadWorldData(legacyRoot);

        assertEquals(CURRENT_SCHEMA_VERSION, NBT.getSchemaVersion(loaded));
        assertEquals("COMPLETED", NBT.getInitPhaseName(loaded, owner));
        assertTrue(NBT.isApertureInitialized(loaded, owner));
        assertNull(NBT.getOpeningSnapshot(loaded, owner));
        assertNull(NBT.getLayoutVersion(loaded, owner));
        assertNull(NBT.getPlanSeed(loaded, owner));

        Object migratedRoot = NBT.saveWorldData(loaded);
        assertEquals(CURRENT_SCHEMA_VERSION, NBT.getInt(migratedRoot, KEY_SCHEMA_VERSION));
        assertEquals(1, NBT.listSize(NBT.getList(migratedRoot, KEY_INITIALIZED_APERTURES)));
        assertEquals(1, NBT.listSize(NBT.getList(migratedRoot, KEY_APERTURE_INIT_STATES)));

        Object initStateTag = NBT.findInitStateTag(migratedRoot, owner);
        assertEquals("COMPLETED", NBT.getString(initStateTag, KEY_INIT_PHASE));
        assertFalse(NBT.contains(initStateTag, KEY_OPENING_SNAPSHOT));
        assertShapeEquals(legacyShape, NBT.findApertureInfoTag(migratedRoot, owner));
    }

    @Test
    void legacyUninitializedSaveLoadsWithoutLosingBoundaryTruth() throws Exception {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000102");
        ApertureShape legacyShape = new ApertureShape(320, 88, 48, 18, 20, 2, 4, 0.75F, 9600L, true, 12.5F, 2);

        Object legacyRoot = NBT.compound();
        NBT.putInt(legacyRoot, KEY_NEXT_INDEX, 8);
        NBT.put(legacyRoot, KEY_APERTURES, NBT.singleEntryList(legacyApertureEntry(owner, legacyShape)));

        Object loaded = NBT.loadWorldData(legacyRoot);

        assertEquals(CURRENT_SCHEMA_VERSION, NBT.getSchemaVersion(loaded));
        assertEquals("UNINITIALIZED", NBT.getInitPhaseName(loaded, owner));
        assertFalse(NBT.isApertureInitialized(loaded, owner));
        assertNull(NBT.getOpeningSnapshot(loaded, owner));

        Object migratedRoot = NBT.saveWorldData(loaded);
        assertEquals(0, NBT.listSize(NBT.getList(migratedRoot, KEY_INITIALIZED_APERTURES)));
        assertEquals(1, NBT.listSize(NBT.getList(migratedRoot, KEY_APERTURE_INIT_STATES)));

        Object initStateTag = NBT.findInitStateTag(migratedRoot, owner);
        assertEquals("UNINITIALIZED", NBT.getString(initStateTag, KEY_INIT_PHASE));
        assertFalse(NBT.contains(initStateTag, KEY_LAYOUT_VERSION));
        assertFalse(NBT.contains(initStateTag, KEY_PLAN_SEED));
        assertShapeEquals(legacyShape, NBT.findApertureInfoTag(migratedRoot, owner));
    }

    @Test
    void newPhasedStateRoundTripsForPlannedExecutingAndCompleted() throws Exception {
        UUID plannedOwner = UUID.fromString("00000000-0000-0000-0000-000000000201");
        UUID executingOwner = UUID.fromString("00000000-0000-0000-0000-000000000202");
        UUID completedOwner = UUID.fromString("00000000-0000-0000-0000-000000000203");

        Object data = NBT.newWorldData();
        NBT.getOrAllocate(data, plannedOwner);
        NBT.getOrAllocate(data, executingOwner);
        NBT.getOrAllocate(data, completedOwner);

        AscensionConditionSnapshot plannedSnapshot = snapshot("benming:planned", false, 31.0D);
        AscensionConditionSnapshot executingSnapshot = snapshot("benming:executing", true, 47.0D);
        AscensionConditionSnapshot completedSnapshot = snapshot("benming:completed", true, 63.0D);

        NBT.setInitializationState(
            plannedOwner,
            data,
            NBT.newInitState("PLANNED", plannedSnapshot, Integer.valueOf(2), Long.valueOf(2002L))
        );
        NBT.setInitializationState(
            executingOwner,
            data,
            NBT.newInitState("EXECUTING", executingSnapshot, Integer.valueOf(3), Long.valueOf(3003L))
        );
        NBT.setInitializationState(
            completedOwner,
            data,
            NBT.newInitState("COMPLETED", completedSnapshot, Integer.valueOf(4), Long.valueOf(4004L))
        );

        Object beforeRoundTrip = NBT.saveWorldData(data);
        Object restored = NBT.loadWorldData(beforeRoundTrip);
        Object afterRoundTrip = NBT.saveWorldData(restored);

        assertEquals("PLANNED", NBT.getInitPhaseName(restored, plannedOwner));
        assertEquals("EXECUTING", NBT.getInitPhaseName(restored, executingOwner));
        assertEquals("COMPLETED", NBT.getInitPhaseName(restored, completedOwner));
        assertFalse(NBT.isApertureInitialized(restored, plannedOwner));
        assertFalse(NBT.isApertureInitialized(restored, executingOwner));
        assertTrue(NBT.isApertureInitialized(restored, completedOwner));
        assertEquals(plannedSnapshot, NBT.getOpeningSnapshot(restored, plannedOwner));
        assertEquals(executingSnapshot, NBT.getOpeningSnapshot(restored, executingOwner));
        assertEquals(completedSnapshot, NBT.getOpeningSnapshot(restored, completedOwner));
        assertEquals(Integer.valueOf(2), NBT.getLayoutVersion(restored, plannedOwner));
        assertEquals(Integer.valueOf(3), NBT.getLayoutVersion(restored, executingOwner));
        assertEquals(Integer.valueOf(4), NBT.getLayoutVersion(restored, completedOwner));
        assertEquals(Long.valueOf(2002L), NBT.getPlanSeed(restored, plannedOwner));
        assertEquals(Long.valueOf(3003L), NBT.getPlanSeed(restored, executingOwner));
        assertEquals(Long.valueOf(4004L), NBT.getPlanSeed(restored, completedOwner));

        assertEquals(3, NBT.listSize(NBT.getList(afterRoundTrip, KEY_APERTURE_INIT_STATES)));
        assertEquals(1, NBT.listSize(NBT.getList(afterRoundTrip, KEY_INITIALIZED_APERTURES)));
        assertShapeEquals(
            NBT.readShape(NBT.findApertureInfoTag(beforeRoundTrip, plannedOwner)),
            NBT.findApertureInfoTag(afterRoundTrip, plannedOwner)
        );
        assertShapeEquals(
            NBT.readShape(NBT.findApertureInfoTag(beforeRoundTrip, executingOwner)),
            NBT.findApertureInfoTag(afterRoundTrip, executingOwner)
        );
        assertShapeEquals(
            NBT.readShape(NBT.findApertureInfoTag(beforeRoundTrip, completedOwner)),
            NBT.findApertureInfoTag(afterRoundTrip, completedOwner)
        );
    }

    @Test
    void legacyInitializedProjectionStaysSynchronizedWithExplicitPhaseAfterSaveLoad() throws Exception {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000401");
        AscensionConditionSnapshot snapshot = snapshot("benming:projection", true, 91.0D);

        Object data = NBT.newWorldData();
        NBT.getOrAllocate(data, owner);
        NBT.setInitializationState(
            owner,
            data,
            NBT.newInitState("COMPLETED", snapshot, Integer.valueOf(9), Long.valueOf(9009L))
        );

        Object completedRoot = NBT.saveWorldData(data);
        assertEquals(1, NBT.listSize(NBT.getList(completedRoot, KEY_INITIALIZED_APERTURES)));

        Object restoredCompleted = NBT.loadWorldData(completedRoot);
        assertTrue(NBT.isApertureInitialized(restoredCompleted, owner));
        assertEquals("COMPLETED", NBT.getInitPhaseName(restoredCompleted, owner));

        NBT.setInitializationState(
            owner,
            restoredCompleted,
            NBT.newInitState("EXECUTING", snapshot, Integer.valueOf(9), Long.valueOf(9009L))
        );
        Object executingRoot = NBT.saveWorldData(restoredCompleted);
        assertEquals(0, NBT.listSize(NBT.getList(executingRoot, KEY_INITIALIZED_APERTURES)));

        Object restoredExecuting = NBT.loadWorldData(executingRoot);
        assertFalse(NBT.isApertureInitialized(restoredExecuting, owner));
        assertEquals("EXECUTING", NBT.getInitPhaseName(restoredExecuting, owner));
    }

    @Test
    void openingSnapshotPersistencePreservesAllFrozenInputs() throws Exception {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000301");
        AscensionConditionSnapshot snapshot = snapshot("benming:fidelity", true, 79.0D);

        Object data = NBT.newWorldData();
        NBT.getOrAllocate(data, owner);
        NBT.setInitializationState(
            owner,
            data,
            NBT.newInitState("EXECUTING", snapshot, Integer.valueOf(7), Long.valueOf(7007L))
        );

        Object restored = NBT.loadWorldData(NBT.saveWorldData(data));
        AscensionConditionSnapshot restoredSnapshot = NBT.getOpeningSnapshot(restored, owner);

        assertNotNull(restoredSnapshot);
        assertEquals(snapshot, restoredSnapshot);
        assertEquals(snapshot.daoMarks(), restoredSnapshot.daoMarks());
        assertEquals(snapshot.benmingGuFallbackState(), restoredSnapshot.benmingGuFallbackState());
        assertEquals(snapshot.daoMarkCoverageState(), restoredSnapshot.daoMarkCoverageState());
        assertEquals(snapshot.aptitudeResourceState(), restoredSnapshot.aptitudeResourceState());
        assertEquals(snapshot.playerInitiated(), restoredSnapshot.playerInitiated());
    }

    private static Object legacyApertureEntry(UUID owner, ApertureShape shape) throws Exception {
        Object entryTag = NBT.compound();
        NBT.putUUID(entryTag, KEY_OWNER, owner);
        NBT.put(entryTag, KEY_INFO, legacyApertureInfo(shape));
        return entryTag;
    }

    private static Object legacyApertureInfo(ApertureShape shape) throws Exception {
        Object infoTag = NBT.compound();
        NBT.putInt(infoTag, KEY_CENTER_X, shape.centerX());
        NBT.putInt(infoTag, KEY_CENTER_Y, shape.centerY());
        NBT.putInt(infoTag, KEY_CENTER_Z, shape.centerZ());
        NBT.putInt(infoTag, KEY_MIN_CHUNK_X, shape.minChunkX());
        NBT.putInt(infoTag, KEY_MAX_CHUNK_X, shape.maxChunkX());
        NBT.putInt(infoTag, KEY_MIN_CHUNK_Z, shape.minChunkZ());
        NBT.putInt(infoTag, KEY_MAX_CHUNK_Z, shape.maxChunkZ());
        NBT.putFloat(infoTag, KEY_TIME_SPEED, shape.timeSpeed());
        NBT.putLong(infoTag, KEY_NEXT_TRIBULATION_TICK, shape.nextTribulationTick());
        NBT.putBoolean(infoTag, KEY_IS_FROZEN, shape.frozen());
        NBT.putFloat(infoTag, KEY_FAVORABILITY, shape.favorability());
        NBT.putInt(infoTag, KEY_TIER, shape.tier());
        return infoTag;
    }

    private static Object ownerEntry(UUID owner) throws Exception {
        Object entryTag = NBT.compound();
        NBT.putUUID(entryTag, KEY_OWNER, owner);
        return entryTag;
    }

    private static void assertShapeEquals(ApertureShape expected, Object actualInfoTag) throws Exception {
        assertEquals(expected.centerX(), NBT.getInt(actualInfoTag, KEY_CENTER_X));
        assertEquals(expected.centerY(), NBT.getInt(actualInfoTag, KEY_CENTER_Y));
        assertEquals(expected.centerZ(), NBT.getInt(actualInfoTag, KEY_CENTER_Z));
        assertEquals(expected.minChunkX(), NBT.getInt(actualInfoTag, KEY_MIN_CHUNK_X));
        assertEquals(expected.maxChunkX(), NBT.getInt(actualInfoTag, KEY_MAX_CHUNK_X));
        assertEquals(expected.minChunkZ(), NBT.getInt(actualInfoTag, KEY_MIN_CHUNK_Z));
        assertEquals(expected.maxChunkZ(), NBT.getInt(actualInfoTag, KEY_MAX_CHUNK_Z));
        assertEquals(expected.timeSpeed(), NBT.getFloat(actualInfoTag, KEY_TIME_SPEED));
        assertEquals(expected.nextTribulationTick(), NBT.getLong(actualInfoTag, KEY_NEXT_TRIBULATION_TICK));
        assertEquals(expected.frozen(), NBT.getBoolean(actualInfoTag, KEY_IS_FROZEN));
        assertEquals(expected.favorability(), NBT.getFloat(actualInfoTag, KEY_FAVORABILITY));
        assertEquals(expected.tier(), NBT.getInt(actualInfoTag, KEY_TIER));
    }

    private static AscensionConditionSnapshot snapshot(String token, boolean playerInitiated, double baseValue) {
        Map<String, Double> daoMarks = new HashMap<>();
        daoMarks.put("huodao", baseValue + 3.0D);
        daoMarks.put("shuidao", baseValue + 5.0D);
        daoMarks.put("tudao", baseValue + 1.0D);
        return new AscensionConditionSnapshot(
            baseValue,
            AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED,
            token,
            daoMarks,
            AscensionConditionSnapshot.DaoMarkCoverageState.COMPLETE,
            baseValue + 10.0D,
            baseValue + 8.0D,
            AscensionConditionSnapshot.AptitudeResourceState.HEALTHY,
            baseValue + 100.0D,
            baseValue + 90.0D,
            baseValue + 20.0D,
            baseValue + 120.0D,
            baseValue + 25.0D,
            baseValue + 130.0D,
            baseValue + 30.0D,
            5.0D,
            5.0D,
            baseValue + 40.0D,
            baseValue + 50.0D,
            baseValue + 150.0D,
            baseValue + 60.0D,
            baseValue + 70.0D,
            baseValue + 80.0D,
            baseValue + 85.0D,
            baseValue + 95.0D,
            playerInitiated
        );
    }

    private record ApertureShape(
        int centerX,
        int centerY,
        int centerZ,
        int minChunkX,
        int maxChunkX,
        int minChunkZ,
        int maxChunkZ,
        float timeSpeed,
        long nextTribulationTick,
        boolean frozen,
        float favorability,
        int tier
    ) {
    }

    private static final class NbtHarness {

        private static final String WORLD_DATA_CLASS_NAME =
            "com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData";

        private static final String INIT_STATE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData$ApertureInitializationState";

        private static final String INIT_PHASE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData$InitPhase";

        private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";

        private static final String LIST_TAG_CLASS_NAME = "net.minecraft.nbt.ListTag";

        private static final String TARGET_SOURCE_PATH =
            "src/main/java/com/Kizunad/guzhenrenext/xianqiao/data/ApertureWorldData.java";

        private final Class<?> worldDataClass;

        private final Class<?> initStateClass;

        private final Class<?> initPhaseClass;

        private final Class<?> compoundTagClass;

        private final Class<?> listTagClass;

        private final Method compoundPutMethod;

        private final Method compoundPutIntMethod;

        private final Method compoundPutLongMethod;

        private final Method compoundPutFloatMethod;

        private final Method compoundPutBooleanMethod;

        private final Method compoundPutUUIDMethod;

        private final Method compoundContainsMethod;

        private final Method compoundGetIntMethod;

        private final Method compoundGetLongMethod;

        private final Method compoundGetFloatMethod;

        private final Method compoundGetBooleanMethod;

        private final Method compoundGetStringMethod;

        private final Method compoundGetUUIDMethod;

        private final Method compoundGetCompoundMethod;

        private final Method compoundGetListMethod;

        private final Method listAddMethod;

        private final Method listGetCompoundMethod;

        private final Method listSizeMethod;

        private final Method worldDataSaveMethod;

        private final Method worldDataLoadMethod;

        private final Method worldDataGetSchemaVersionMethod;

        private final Method worldDataGetInitPhaseMethod;

        private final Method worldDataIsInitializedMethod;

        private final Method worldDataGetOpeningSnapshotMethod;

        private final Method worldDataGetLayoutVersionMethod;

        private final Method worldDataGetPlanSeedMethod;

        private final Method worldDataGetOrAllocateMethod;

        private final Method worldDataSetInitializationStateMethod;

        private final Constructor<?> compoundConstructor;

        private final Constructor<?> listConstructor;

        private final Constructor<?> worldDataConstructor;

        private final Constructor<?> initStateConstructor;

        private NbtHarness() {
            try {
                IsolatedCompilation isolated = compileIsolatedWorldData();
                worldDataClass = isolated.loadClass(WORLD_DATA_CLASS_NAME);
                initStateClass = isolated.loadClass(INIT_STATE_CLASS_NAME);
                initPhaseClass = isolated.loadClass(INIT_PHASE_CLASS_NAME);
                compoundTagClass = isolated.loadClass(COMPOUND_TAG_CLASS_NAME);
                listTagClass = isolated.loadClass(LIST_TAG_CLASS_NAME);
                Class<?> tagClass = isolated.loadClass("net.minecraft.nbt.Tag");
                worldDataConstructor = worldDataClass.getDeclaredConstructor();
                initStateConstructor = initStateClass.getDeclaredConstructor(
                    initPhaseClass,
                    AscensionConditionSnapshot.class,
                    Integer.class,
                    Long.class
                );
                compoundConstructor = compoundTagClass.getDeclaredConstructor();
                listConstructor = listTagClass.getDeclaredConstructor();
                compoundPutMethod = compoundTagClass.getMethod("put", String.class, tagClass);
                compoundPutIntMethod = compoundTagClass.getMethod("putInt", String.class, int.class);
                compoundPutLongMethod = compoundTagClass.getMethod("putLong", String.class, long.class);
                compoundPutFloatMethod = compoundTagClass.getMethod("putFloat", String.class, float.class);
                compoundPutBooleanMethod = compoundTagClass.getMethod("putBoolean", String.class, boolean.class);
                compoundPutUUIDMethod = compoundTagClass.getMethod("putUUID", String.class, UUID.class);
                compoundContainsMethod = compoundTagClass.getMethod("contains", String.class);
                compoundGetIntMethod = compoundTagClass.getMethod("getInt", String.class);
                compoundGetLongMethod = compoundTagClass.getMethod("getLong", String.class);
                compoundGetFloatMethod = compoundTagClass.getMethod("getFloat", String.class);
                compoundGetBooleanMethod = compoundTagClass.getMethod("getBoolean", String.class);
                compoundGetStringMethod = compoundTagClass.getMethod("getString", String.class);
                compoundGetUUIDMethod = compoundTagClass.getMethod("getUUID", String.class);
                compoundGetCompoundMethod = compoundTagClass.getMethod("getCompound", String.class);
                compoundGetListMethod = compoundTagClass.getMethod("getList", String.class, int.class);
                listAddMethod = listTagClass.getMethod("add", Object.class);
                listGetCompoundMethod = listTagClass.getMethod("getCompound", int.class);
                listSizeMethod = listTagClass.getMethod("size");
                worldDataSaveMethod = resolveSaveMethod();
                worldDataLoadMethod = resolveLoadMethod();
                worldDataGetSchemaVersionMethod = worldDataClass.getMethod("getSchemaVersion");
                worldDataGetInitPhaseMethod = worldDataClass.getMethod("getInitPhase", UUID.class);
                worldDataIsInitializedMethod = worldDataClass.getMethod("isApertureInitialized", UUID.class);
                worldDataGetOpeningSnapshotMethod = worldDataClass.getMethod("getOpeningSnapshot", UUID.class);
                worldDataGetLayoutVersionMethod = worldDataClass.getMethod("getLayoutVersion", UUID.class);
                worldDataGetPlanSeedMethod = worldDataClass.getMethod("getPlanSeed", UUID.class);
                worldDataGetOrAllocateMethod = worldDataClass.getMethod("getOrAllocate", UUID.class);
                worldDataSetInitializationStateMethod = worldDataClass.getMethod(
                    "setInitializationState",
                    UUID.class,
                    initStateClass
                );
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("无法初始化 NBT 反射测试支架", exception);
            }
        }

        private Method resolveSaveMethod() {
            for (Method method : worldDataClass.getMethods()) {
                if ("save".equals(method.getName()) && method.getParameterCount() == 2) {
                    return method;
                }
            }
            throw new IllegalStateException("未找到 ApertureWorldData.save 方法");
        }

        private Method resolveLoadMethod() {
            for (Method method : worldDataClass.getDeclaredMethods()) {
                if ("loadFromTag".equals(method.getName()) && method.getParameterCount() == 1) {
                    method.setAccessible(true);
                    return method;
                }
            }
            throw new IllegalStateException("未找到 ApertureWorldData.loadFromTag 方法");
        }

        private IsolatedCompilation compileIsolatedWorldData() {
            try {
                Path sourceRoot = Files.createTempDirectory("aperture-worlddata-src");
                Path classesRoot = Files.createTempDirectory("aperture-worlddata-classes");
                Path targetSource = Path.of(System.getProperty("user.dir")).resolve(TARGET_SOURCE_PATH);
                if (!Files.exists(targetSource)) {
                    throw new IllegalStateException("未找到目标源码：" + targetSource);
                }

                List<Path> stubSources = List.of(
                    writeStub(sourceRoot, "javax/annotation/Nullable.java", stubNullable()),
                    writeStub(sourceRoot, "net/minecraft/nbt/Tag.java", stubTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/CompoundTag.java", stubCompoundTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/ListTag.java", stubListTag()),
                    writeStub(sourceRoot, "net/minecraft/core/BlockPos.java", stubBlockPos()),
                    writeStub(sourceRoot, "net/minecraft/core/HolderLookup.java", stubHolderLookup()),
                    writeStub(sourceRoot, "net/minecraft/world/level/saveddata/SavedData.java", stubSavedData()),
                    writeStub(sourceRoot, "net/minecraft/server/level/ServerLevel.java", stubServerLevel())
                );

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                if (compiler == null) {
                    throw new IllegalStateException("当前运行环境不提供 JavaCompiler");
                }

                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                StringWriter compilerOutput = new StringWriter();
                try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
                    List<java.io.File> sourceFiles = new java.util.ArrayList<>();
                    sourceFiles.add(targetSource.toFile());
                    for (Path stubSource : stubSources) {
                        sourceFiles.add(stubSource.toFile());
                    }
                    Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
                    List<String> options = List.of(
                        "-classpath",
                        System.getProperty("java.class.path"),
                        "-d",
                        classesRoot.toString()
                    );
                    boolean success = compiler.getTask(
                        compilerOutput,
                        fileManager,
                        diagnostics,
                        options,
                        null,
                        compilationUnits
                    ).call();
                    if (!success) {
                        throw new IllegalStateException(buildCompilerFailureMessage(diagnostics, compilerOutput));
                    }
                }
                return new IsolatedCompilation(classesRoot);
            } catch (Exception exception) {
                throw new IllegalStateException("无法构建隔离版 ApertureWorldData 测试运行时", exception);
            }
        }

        private String buildCompilerFailureMessage(
            DiagnosticCollector<JavaFileObject> diagnostics,
            StringWriter compilerOutput
        ) {
            StringBuilder message = new StringBuilder("隔离编译 ApertureWorldData 失败");
            String output = compilerOutput.toString();
            if (!output.isEmpty()) {
                message.append("\n").append(output);
            }
            for (javax.tools.Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                message.append("\n")
                    .append(diagnostic.getKind())
                    .append(": ")
                    .append(diagnostic.getMessage(null));
            }
            return message.toString();
        }

        private Path writeStub(Path sourceRoot, String relativePath, String source) throws Exception {
            Path stubPath = sourceRoot.resolve(relativePath);
            Files.createDirectories(stubPath.getParent());
            Files.writeString(stubPath, source);
            return stubPath;
        }

        private String stubTag() {
            return """
                package net.minecraft.nbt;

                public interface Tag {
                    int TAG_LIST = 9;
                    int TAG_COMPOUND = 10;
                }
                """;
        }

        private String stubNullable() {
            return """
                package javax.annotation;

                import java.lang.annotation.ElementType;
                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;
                import java.lang.annotation.Target;

                @Retention(RetentionPolicy.RUNTIME)
                @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_USE})
                public @interface Nullable {
                }
                """;
        }

        private String stubCompoundTag() {
            return """
                package net.minecraft.nbt;

                import java.util.HashMap;
                import java.util.Map;
                import java.util.Set;
                import java.util.UUID;

                public class CompoundTag implements Tag {
                    private final Map<String, Object> values = new HashMap<>();

                    public void put(String key, Tag value) {
                        values.put(key, value);
                    }

                    public void putInt(String key, int value) {
                        values.put(key, Integer.valueOf(value));
                    }

                    public void putLong(String key, long value) {
                        values.put(key, Long.valueOf(value));
                    }

                    public void putFloat(String key, float value) {
                        values.put(key, Float.valueOf(value));
                    }

                    public void putDouble(String key, double value) {
                        values.put(key, Double.valueOf(value));
                    }

                    public void putBoolean(String key, boolean value) {
                        values.put(key, Boolean.valueOf(value));
                    }

                    public void putString(String key, String value) {
                        values.put(key, value);
                    }

                    public void putUUID(String key, UUID value) {
                        values.put(key, value);
                    }

                    public boolean contains(String key) {
                        return values.containsKey(key);
                    }

                    public boolean contains(String key, int expectedType) {
                        Object value = values.get(key);
                        if (value == null) {
                            return false;
                        }
                        if (expectedType == TAG_COMPOUND) {
                            return value instanceof CompoundTag;
                        }
                        if (expectedType == TAG_LIST) {
                            return value instanceof ListTag;
                        }
                        return true;
                    }

                    public boolean hasUUID(String key) {
                        return values.get(key) instanceof UUID;
                    }

                    public int getInt(String key) {
                        Object value = values.get(key);
                        return value instanceof Number ? ((Number) value).intValue() : 0;
                    }

                    public long getLong(String key) {
                        Object value = values.get(key);
                        return value instanceof Number ? ((Number) value).longValue() : 0L;
                    }

                    public float getFloat(String key) {
                        Object value = values.get(key);
                        return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
                    }

                    public double getDouble(String key) {
                        Object value = values.get(key);
                        return value instanceof Number ? ((Number) value).doubleValue() : 0.0D;
                    }

                    public boolean getBoolean(String key) {
                        Object value = values.get(key);
                        return value instanceof Boolean && (Boolean) value;
                    }

                    public String getString(String key) {
                        Object value = values.get(key);
                        return value instanceof String ? (String) value : "";
                    }

                    public UUID getUUID(String key) {
                        return (UUID) values.get(key);
                    }

                    public CompoundTag getCompound(String key) {
                        Object value = values.get(key);
                        return value instanceof CompoundTag ? (CompoundTag) value : new CompoundTag();
                    }

                    public ListTag getList(String key, int expectedType) {
                        Object value = values.get(key);
                        return value instanceof ListTag ? (ListTag) value : new ListTag();
                    }

                    public Set<String> getAllKeys() {
                        return values.keySet();
                    }
                }
                """;
        }

        private String stubListTag() {
            return """
                package net.minecraft.nbt;

                import java.util.ArrayList;

                public class ListTag extends ArrayList<Tag> implements Tag {
                    public CompoundTag getCompound(int index) {
                        Object value = get(index);
                        return value instanceof CompoundTag ? (CompoundTag) value : new CompoundTag();
                    }
                }
                """;
        }

        private String stubBlockPos() {
            return """
                package net.minecraft.core;

                import java.util.Objects;

                public class BlockPos {
                    private final int x;
                    private final int y;
                    private final int z;

                    public BlockPos(int x, int y, int z) {
                        this.x = x;
                        this.y = y;
                        this.z = z;
                    }

                    public int getX() {
                        return x;
                    }

                    public int getY() {
                        return y;
                    }

                    public int getZ() {
                        return z;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        if (this == obj) {
                            return true;
                        }
                        if (!(obj instanceof BlockPos other)) {
                            return false;
                        }
                        return x == other.x && y == other.y && z == other.z;
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(x, y, z);
                    }
                }
                """;
        }

        private String stubHolderLookup() {
            return """
                package net.minecraft.core;

                public final class HolderLookup {
                    private HolderLookup() {
                    }

                    public static final class Provider {
                    }
                }
                """;
        }

        private String stubSavedData() {
            return """
                package net.minecraft.world.level.saveddata;

                import java.util.function.BiFunction;
                import java.util.function.Supplier;
                import net.minecraft.core.HolderLookup;
                import net.minecraft.nbt.CompoundTag;

                public abstract class SavedData {
                    private boolean dirty;

                    public abstract CompoundTag save(CompoundTag tag, HolderLookup.Provider registries);

                    public void setDirty() {
                        dirty = true;
                    }

                    public boolean isDirty() {
                        return dirty;
                    }

                    public static final class Factory<T extends SavedData> {
                        public Factory(
                            Supplier<T> constructor,
                            BiFunction<CompoundTag, HolderLookup.Provider, T> loader,
                            Object codec
                        ) {
                        }
                    }
                }
                """;
        }

        private String stubServerLevel() {
            return """
                package net.minecraft.server.level;

                import net.minecraft.world.level.saveddata.SavedData;

                public class ServerLevel {
                    public DataStorage getDataStorage() {
                        return new DataStorage();
                    }

                    public static final class DataStorage {
                        public <T extends SavedData> T computeIfAbsent(SavedData.Factory<T> factory, String name) {
                            return null;
                        }
                    }
                }
                """;
        }

        private Object newWorldData() throws Exception {
            return worldDataConstructor.newInstance();
        }

        private Object newInitState(
            String phaseName,
            AscensionConditionSnapshot snapshot,
            Integer layoutVersion,
            Long planSeed
        ) throws Exception {
            Object phase = initPhaseClass.getMethod("valueOf", String.class).invoke(null, phaseName);
            return initStateConstructor.newInstance(phase, snapshot, layoutVersion, planSeed);
        }

        private Object compound() throws Exception {
            return compoundConstructor.newInstance();
        }

        private void put(Object compoundTag, String key, Object value) throws Exception {
            compoundPutMethod.invoke(compoundTag, key, value);
        }

        private void putInt(Object compoundTag, String key, int value) throws Exception {
            compoundPutIntMethod.invoke(compoundTag, key, value);
        }

        private void putLong(Object compoundTag, String key, long value) throws Exception {
            compoundPutLongMethod.invoke(compoundTag, key, value);
        }

        private void putFloat(Object compoundTag, String key, float value) throws Exception {
            compoundPutFloatMethod.invoke(compoundTag, key, value);
        }

        private void putBoolean(Object compoundTag, String key, boolean value) throws Exception {
            compoundPutBooleanMethod.invoke(compoundTag, key, value);
        }

        private void putUUID(Object compoundTag, String key, UUID value) throws Exception {
            compoundPutUUIDMethod.invoke(compoundTag, key, value);
        }

        private Object singleEntryList(Object entry) throws Exception {
            Object listTag = listConstructor.newInstance();
            listAddMethod.invoke(listTag, entry);
            return listTag;
        }

        private boolean contains(Object compoundTag, String key) throws Exception {
            return (boolean) compoundContainsMethod.invoke(compoundTag, key);
        }

        private int getInt(Object compoundTag, String key) throws Exception {
            return (int) compoundGetIntMethod.invoke(compoundTag, key);
        }

        private long getLong(Object compoundTag, String key) throws Exception {
            return (long) compoundGetLongMethod.invoke(compoundTag, key);
        }

        private float getFloat(Object compoundTag, String key) throws Exception {
            return (float) compoundGetFloatMethod.invoke(compoundTag, key);
        }

        private boolean getBoolean(Object compoundTag, String key) throws Exception {
            return (boolean) compoundGetBooleanMethod.invoke(compoundTag, key);
        }

        private String getString(Object compoundTag, String key) throws Exception {
            return (String) compoundGetStringMethod.invoke(compoundTag, key);
        }

        private UUID getUUID(Object compoundTag, String key) throws Exception {
            return (UUID) compoundGetUUIDMethod.invoke(compoundTag, key);
        }

        private Object getCompound(Object compoundTag, String key) throws Exception {
            return compoundGetCompoundMethod.invoke(compoundTag, key);
        }

        private Object getList(Object compoundTag, String key) throws Exception {
            return compoundGetListMethod.invoke(compoundTag, key, TAG_COMPOUND);
        }

        private int listSize(Object listTag) throws Exception {
            return (int) listSizeMethod.invoke(listTag);
        }

        private Object getCompoundAt(Object listTag, int index) throws Exception {
            return listGetCompoundMethod.invoke(listTag, index);
        }

        private int getSchemaVersion(Object worldData) throws Exception {
            return (int) worldDataGetSchemaVersionMethod.invoke(worldData);
        }

        private String getInitPhaseName(Object worldData, UUID owner) throws Exception {
            Enum<?> phase = (Enum<?>) worldDataGetInitPhaseMethod.invoke(worldData, owner);
            return phase.name();
        }

        private boolean isApertureInitialized(Object worldData, UUID owner) throws Exception {
            return (boolean) worldDataIsInitializedMethod.invoke(worldData, owner);
        }

        private AscensionConditionSnapshot getOpeningSnapshot(Object worldData, UUID owner) throws Exception {
            return (AscensionConditionSnapshot) worldDataGetOpeningSnapshotMethod.invoke(worldData, owner);
        }

        private Integer getLayoutVersion(Object worldData, UUID owner) throws Exception {
            return (Integer) worldDataGetLayoutVersionMethod.invoke(worldData, owner);
        }

        private Long getPlanSeed(Object worldData, UUID owner) throws Exception {
            return (Long) worldDataGetPlanSeedMethod.invoke(worldData, owner);
        }

        private void getOrAllocate(Object worldData, UUID owner) throws Exception {
            worldDataGetOrAllocateMethod.invoke(worldData, owner);
        }

        private void setInitializationState(UUID owner, Object worldData, Object initState) throws Exception {
            worldDataSetInitializationStateMethod.invoke(worldData, owner, initState);
        }

        private Object loadWorldData(Object rootTag) throws Exception {
            return worldDataLoadMethod.invoke(null, rootTag);
        }

        private Object saveWorldData(Object data) throws Exception {
            return worldDataSaveMethod.invoke(data, compound(), null);
        }

        private Object findApertureInfoTag(Object rootTag, UUID owner) throws Exception {
            Object apertureList = getList(rootTag, KEY_APERTURES);
            for (int index = 0; index < listSize(apertureList); index++) {
                Object entryTag = getCompoundAt(apertureList, index);
                if (owner.equals(getUUID(entryTag, KEY_OWNER))) {
                    return getCompound(entryTag, KEY_INFO);
                }
            }
            throw new IllegalStateException("未找到目标 aperture info：" + owner);
        }

        private Object findInitStateTag(Object rootTag, UUID owner) throws Exception {
            Object initStateList = getList(rootTag, KEY_APERTURE_INIT_STATES);
            for (int index = 0; index < listSize(initStateList); index++) {
                Object entryTag = getCompoundAt(initStateList, index);
                if (owner.equals(getUUID(entryTag, KEY_OWNER))) {
                    return getCompound(entryTag, KEY_INIT_STATE);
                }
            }
            throw new IllegalStateException("未找到目标初始化状态：" + owner);
        }

        private ApertureShape readShape(Object infoTag) throws Exception {
            return new ApertureShape(
                getInt(infoTag, KEY_CENTER_X),
                getInt(infoTag, KEY_CENTER_Y),
                getInt(infoTag, KEY_CENTER_Z),
                getInt(infoTag, KEY_MIN_CHUNK_X),
                getInt(infoTag, KEY_MAX_CHUNK_X),
                getInt(infoTag, KEY_MIN_CHUNK_Z),
                getInt(infoTag, KEY_MAX_CHUNK_Z),
                getFloat(infoTag, KEY_TIME_SPEED),
                getLong(infoTag, KEY_NEXT_TRIBULATION_TICK),
                getBoolean(infoTag, KEY_IS_FROZEN),
                getFloat(infoTag, KEY_FAVORABILITY),
                getInt(infoTag, KEY_TIER)
            );
        }

        private static final class IsolatedCompilation {

            private final ChildFirstClassLoader classLoader;

            private IsolatedCompilation(Path classesRoot) throws Exception {
                URL[] urls = new URL[] {classesRoot.toUri().toURL()};
                classLoader = new ChildFirstClassLoader(urls, ApertureWorldDataMigrationTests.class.getClassLoader());
            }

            private Class<?> loadClass(String className) throws ClassNotFoundException {
                return Class.forName(className, true, classLoader);
            }
        }

        private static final class ChildFirstClassLoader extends URLClassLoader {

            private ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
                super(urls, parent);
            }

            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (shouldLoadLocally(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        try {
                            loaded = findClass(name);
                        } catch (ClassNotFoundException exception) {
                            loaded = super.loadClass(name, false);
                        }
                    }
                    if (resolve) {
                        resolveClass(loaded);
                    }
                    return loaded;
                }
                return super.loadClass(name, resolve);
            }

            private boolean shouldLoadLocally(String className) {
                return className.startsWith(WORLD_DATA_CLASS_NAME)
                    || className.startsWith("net.minecraft.");
            }
        }
    }
}
