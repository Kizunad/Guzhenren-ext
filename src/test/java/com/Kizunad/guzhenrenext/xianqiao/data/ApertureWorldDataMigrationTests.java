package com.Kizunad.guzhenrenext.xianqiao.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureWorldDataMigrationTests {

    private static final int CURRENT_SCHEMA_VERSION = 2;

    private static final int LEGACY_SCHEMA_VERSION = 1;

    private static final int LEGACY_NEXT_INDEX = 7;

    private static final int CURRENT_NEXT_INDEX = 9;

    private static final int CENTER_X = 128;

    private static final int CENTER_Y = 72;

    private static final int CENTER_Z = -64;

    private static final int MIN_CHUNK_X = 4;

    private static final int MAX_CHUNK_X = 10;

    private static final int MIN_CHUNK_Z = -8;

    private static final int MAX_CHUNK_Z = -2;

    private static final float TIME_SPEED = 1.25F;

    private static final long NEXT_TRIBULATION_TICK = 9000L;

    private static final float FAVORABILITY = 8.0F;

    private static final int TIER = 2;

    private static final int PLANNED_LAYOUT_VERSION = 3;

    private static final int EXECUTING_LAYOUT_VERSION = 4;

    private static final int COMPLETED_LAYOUT_VERSION = 5;

    private static final long PLANNED_PLAN_SEED = 123456789L;

    private static final long EXECUTING_PLAN_SEED = 99887766L;

    private static final long COMPLETED_PLAN_SEED = 456789123L;

    private static final int TAG_LIST = 9;

    private static final int TAG_COMPOUND = 10;

    private static final String KEY_SCHEMA_VERSION = "schemaVersion";

    private static final String KEY_NEXT_INDEX = "nextIndex";

    private static final String KEY_APERTURES = "apertures";

    private static final String KEY_INITIALIZED_APERTURES = "initializedApertures";

    private static final String KEY_INITIALIZATION_STATES = "initializationStates";

    private static final String KEY_OWNER = "owner";

    private static final String KEY_INFO = "info";

    private static final String KEY_INIT_PHASE = "initPhase";

    private static final String KEY_OPENING_SNAPSHOT = "openingSnapshot";

    private static final String KEY_LAYOUT_VERSION = "layoutVersion";

    private static final String KEY_PLAN_SEED = "planSeed";

    @Test
    void legacyInitializedMustProjectToCompletedWithoutLosingChunkTruth() {
        UUID owner = UUID.randomUUID();
        ApertureSerializedApertureInfo apertureInfo = createApertureInfo();
        ApertureSerializedWorldState migratedState = ApertureWorldDataSchema.normalizeWorldState(
            new ApertureSerializedWorldState(
                LEGACY_SCHEMA_VERSION,
                LEGACY_NEXT_INDEX,
                Map.of(owner, apertureInfo),
                Map.of(),
                Set.of(owner)
            )
        );

        assertEquals(CURRENT_SCHEMA_VERSION, migratedState.schemaVersion());
        assertEquals(LEGACY_NEXT_INDEX, migratedState.nextIndex());
        assertEquals(apertureInfo, migratedState.apertures().get(owner));
        assertEquals(ApertureInitPhase.COMPLETED, migratedState.initializationStates().get(owner).initPhase());
        assertTrue(migratedState.initializedApertures().contains(owner));
        assertTrue(migratedState.initializationStates().get(owner).isInitializedEquivalent());
        assertNull(migratedState.initializationStates().get(owner).openingSnapshot());
        assertNull(migratedState.initializationStates().get(owner).layoutVersion());
        assertNull(migratedState.initializationStates().get(owner).planSeed());
        assertBoundaryTruthPreserved(migratedState.apertures().get(owner));
    }

    @Test
    void legacyUninitializedMustLoadSafelyWithoutLosingChunkTruth() {
        UUID owner = UUID.randomUUID();
        ApertureSerializedApertureInfo apertureInfo = createApertureInfo();
        ApertureSerializedWorldState migratedState = ApertureWorldDataSchema.normalizeWorldState(
            new ApertureSerializedWorldState(
                LEGACY_SCHEMA_VERSION,
                LEGACY_NEXT_INDEX,
                Map.of(owner, apertureInfo),
                Map.of(),
                Set.of()
            )
        );

        assertEquals(CURRENT_SCHEMA_VERSION, migratedState.schemaVersion());
        assertEquals(apertureInfo, migratedState.apertures().get(owner));
        assertEquals(ApertureInitPhase.UNINITIALIZED, migratedState.initializationStates().get(owner).initPhase());
        assertFalse(migratedState.initializationStates().get(owner).isInitializedEquivalent());
        assertTrue(migratedState.initializedApertures().isEmpty());
        assertNull(migratedState.initializationStates().get(owner).openingSnapshot());
        assertNull(migratedState.initializationStates().get(owner).layoutVersion());
        assertNull(migratedState.initializationStates().get(owner).planSeed());
        assertBoundaryTruthPreserved(migratedState.apertures().get(owner));
    }

    @Test
    void plannedStateMustRoundTripSnapshotAndPlanningMetadata() {
        ApertureInitializationState expectedState = new ApertureInitializationState(
            ApertureInitPhase.PLANNED,
            createSnapshot(),
            PLANNED_LAYOUT_VERSION,
            PLANNED_PLAN_SEED
        );

        ApertureInitializationState reloadedState = ApertureWorldDataSchema.normalizeInitializationState(expectedState);

        assertEquals(ApertureInitPhase.PLANNED, reloadedState.initPhase());
        assertEquals(createSnapshot(), reloadedState.openingSnapshot());
        assertEquals(PLANNED_LAYOUT_VERSION, reloadedState.layoutVersion());
        assertEquals(PLANNED_PLAN_SEED, reloadedState.planSeed());
        assertFalse(reloadedState.isInitializedEquivalent());
    }

    @Test
    void executingStateMustRoundTripSnapshotAndPlanningMetadata() {
        ApertureInitializationState expectedState = new ApertureInitializationState(
            ApertureInitPhase.EXECUTING,
            createSnapshot(),
            EXECUTING_LAYOUT_VERSION,
            EXECUTING_PLAN_SEED
        );

        ApertureInitializationState reloadedState = ApertureWorldDataSchema.normalizeInitializationState(expectedState);

        assertEquals(ApertureInitPhase.EXECUTING, reloadedState.initPhase());
        assertEquals(createSnapshot(), reloadedState.openingSnapshot());
        assertEquals(EXECUTING_LAYOUT_VERSION, reloadedState.layoutVersion());
        assertEquals(EXECUTING_PLAN_SEED, reloadedState.planSeed());
        assertFalse(reloadedState.isInitializedEquivalent());
    }

    @Test
    void completedStateMustRoundTripSnapshotAndPlanningMetadata() {
        ApertureInitializationState expectedState = new ApertureInitializationState(
            ApertureInitPhase.COMPLETED,
            createSnapshot(),
            COMPLETED_LAYOUT_VERSION,
            COMPLETED_PLAN_SEED
        );

        ApertureInitializationState reloadedState = ApertureWorldDataSchema.normalizeInitializationState(expectedState);

        assertEquals(ApertureInitPhase.COMPLETED, reloadedState.initPhase());
        assertEquals(createSnapshot(), reloadedState.openingSnapshot());
        assertEquals(COMPLETED_LAYOUT_VERSION, reloadedState.layoutVersion());
        assertEquals(COMPLETED_PLAN_SEED, reloadedState.planSeed());
        assertTrue(reloadedState.isInitializedEquivalent());
    }

    @Test
    void explicitStateMustOverrideLegacyInitializedProjection() {
        UUID owner = UUID.randomUUID();
        ApertureInitializationState plannedState = new ApertureInitializationState(
            ApertureInitPhase.PLANNED,
            createSnapshot(),
            PLANNED_LAYOUT_VERSION,
            PLANNED_PLAN_SEED
        );
        ApertureSerializedWorldState migratedState = ApertureWorldDataSchema.normalizeWorldState(
            new ApertureSerializedWorldState(
                LEGACY_SCHEMA_VERSION,
                CURRENT_NEXT_INDEX,
                Map.of(owner, createApertureInfo()),
                Map.of(owner, plannedState),
                Set.of(owner)
            )
        );

        assertEquals(ApertureInitPhase.PLANNED, migratedState.initializationStates().get(owner).initPhase());
        assertFalse(migratedState.initializedApertures().contains(owner));
    }

    @Test
    void realSaveMustWriteSchemaAndInitializationStateToNbtWhenMinecraftPersistenceRuntimeIsAvailable() {
        assumeRealPersistenceRuntimeAvailable();
        UUID owner = UUID.randomUUID();
        Object worldData = newWorldData();

        invoke(worldData, "getOrAllocate", owner);
        invoke(
            worldData,
            "setInitializationState",
            owner,
            new ApertureInitializationState(
                ApertureInitPhase.PLANNED,
                createSnapshot(),
                PLANNED_LAYOUT_VERSION,
                PLANNED_PLAN_SEED
            )
        );

        Object savedTag = invokeSave(worldData);
        Object stateList = invoke(savedTag, "getList", KEY_INITIALIZATION_STATES, TAG_COMPOUND);
        Object initializedList = invoke(savedTag, "getList", KEY_INITIALIZED_APERTURES, TAG_COMPOUND);
        Object stateTag = invoke(stateList, "getCompound", 0);
        Object infoTag = invoke(stateTag, "getCompound", KEY_INFO);

        assertEquals(CURRENT_SCHEMA_VERSION, invoke(savedTag, "getInt", KEY_SCHEMA_VERSION));
        assertEquals(1, listSize(stateList));
        assertEquals(0, listSize(initializedList));
        assertEquals(owner, invoke(stateTag, "getUUID", KEY_OWNER));
        assertEquals(ApertureInitPhase.PLANNED.name(), invoke(infoTag, "getString", KEY_INIT_PHASE));
        assertTrue((Boolean) invoke(infoTag, "contains", KEY_OPENING_SNAPSHOT, TAG_COMPOUND));
        assertEquals(PLANNED_LAYOUT_VERSION, invoke(infoTag, "getInt", KEY_LAYOUT_VERSION));
        assertEquals(PLANNED_PLAN_SEED, invoke(infoTag, "getLong", KEY_PLAN_SEED));
    }

    @Test
    void realLoadMustRestorePlannedExecutingAndCompletedMetadataWhenMinecraftPersistenceRuntimeIsAvailable() {
        assumeRealPersistenceRuntimeAvailable();
        UUID plannedOwner = UUID.randomUUID();
        UUID executingOwner = UUID.randomUUID();
        UUID completedOwner = UUID.randomUUID();
        Object sourceTag = newCompoundTag();

        putInt(sourceTag, KEY_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION);
        putInt(sourceTag, KEY_NEXT_INDEX, CURRENT_NEXT_INDEX);
        Object apertures = newListTag();
        addToList(apertures, createApertureEntryTag(plannedOwner, createApertureInfo()));
        addToList(apertures, createApertureEntryTag(executingOwner, createApertureInfo()));
        addToList(apertures, createApertureEntryTag(completedOwner, createApertureInfo()));
        putTag(sourceTag, KEY_APERTURES, apertures);

        Object states = newListTag();
        addToList(
            states,
            createInitializationStateEntryTag(
                plannedOwner,
                new ApertureInitializationState(
                    ApertureInitPhase.PLANNED,
                    createSnapshot(),
                    PLANNED_LAYOUT_VERSION,
                    PLANNED_PLAN_SEED
                )
            )
        );
        addToList(
            states,
            createInitializationStateEntryTag(
                executingOwner,
                new ApertureInitializationState(
                    ApertureInitPhase.EXECUTING,
                    createSnapshot(),
                    EXECUTING_LAYOUT_VERSION,
                    EXECUTING_PLAN_SEED
                )
            )
        );
        addToList(
            states,
            createInitializationStateEntryTag(
                completedOwner,
                new ApertureInitializationState(
                    ApertureInitPhase.COMPLETED,
                    createSnapshot(),
                    COMPLETED_LAYOUT_VERSION,
                    COMPLETED_PLAN_SEED
                )
            )
        );
        putTag(sourceTag, KEY_INITIALIZATION_STATES, states);

        Object initializedOwners = newListTag();
        addToList(initializedOwners, createInitializedOwnerTag(completedOwner));
        putTag(sourceTag, KEY_INITIALIZED_APERTURES, initializedOwners);

        Object loaded = invokePrivateStaticLoad(sourceTag);

        assertEquals(CURRENT_SCHEMA_VERSION, invoke(loaded, "getSchemaVersion"));
        assertEquals(ApertureInitPhase.PLANNED, invoke(loaded, "getInitPhase", plannedOwner));
        assertEquals(ApertureInitPhase.EXECUTING, invoke(loaded, "getInitPhase", executingOwner));
        assertEquals(ApertureInitPhase.COMPLETED, invoke(loaded, "getInitPhase", completedOwner));
        assertEquals(createSnapshot(), invoke(loaded, "getOpeningSnapshot", plannedOwner));
        assertEquals(createSnapshot(), invoke(loaded, "getOpeningSnapshot", executingOwner));
        assertEquals(createSnapshot(), invoke(loaded, "getOpeningSnapshot", completedOwner));
        assertEquals(PLANNED_LAYOUT_VERSION, invoke(loaded, "getLayoutVersion", plannedOwner));
        assertEquals(EXECUTING_LAYOUT_VERSION, invoke(loaded, "getLayoutVersion", executingOwner));
        assertEquals(COMPLETED_LAYOUT_VERSION, invoke(loaded, "getLayoutVersion", completedOwner));
        assertEquals(PLANNED_PLAN_SEED, invoke(loaded, "getPlanSeed", plannedOwner));
        assertEquals(EXECUTING_PLAN_SEED, invoke(loaded, "getPlanSeed", executingOwner));
        assertEquals(COMPLETED_PLAN_SEED, invoke(loaded, "getPlanSeed", completedOwner));
        assertFalse((Boolean) invoke(loaded, "isApertureInitialized", plannedOwner));
        assertFalse((Boolean) invoke(loaded, "isApertureInitialized", executingOwner));
        assertTrue((Boolean) invoke(loaded, "isApertureInitialized", completedOwner));
    }

    @Test
    void realLoadMustMigrateLegacyInitializedWithoutRewritingBoundaryTruthWhenMinecraftPersistenceRuntimeIsAvailable() {
        assumeRealPersistenceRuntimeAvailable();
        UUID owner = UUID.randomUUID();
        Object sourceTag = newCompoundTag();

        putInt(sourceTag, KEY_NEXT_INDEX, LEGACY_NEXT_INDEX);
        Object apertures = newListTag();
        addToList(apertures, createApertureEntryTag(owner, createApertureInfo()));
        putTag(sourceTag, KEY_APERTURES, apertures);

        Object initializedOwners = newListTag();
        addToList(initializedOwners, createInitializedOwnerTag(owner));
        putTag(sourceTag, KEY_INITIALIZED_APERTURES, initializedOwners);

        Object loaded = invokePrivateStaticLoad(sourceTag);
        Object apertureInfo = invoke(loaded, "getAperture", owner);
        Object center = invoke(apertureInfo, "center");

        assertEquals(CURRENT_SCHEMA_VERSION, invoke(loaded, "getSchemaVersion"));
        assertEquals(ApertureInitPhase.COMPLETED, invoke(loaded, "getInitPhase", owner));
        assertTrue((Boolean) invoke(loaded, "isApertureInitialized", owner));
        assertNull(invoke(loaded, "getOpeningSnapshot", owner));
        assertNull(invoke(loaded, "getLayoutVersion", owner));
        assertNull(invoke(loaded, "getPlanSeed", owner));
        assertEquals(CENTER_X, invoke(center, "getX"));
        assertEquals(CENTER_Y, invoke(center, "getY"));
        assertEquals(CENTER_Z, invoke(center, "getZ"));
        assertEquals(MIN_CHUNK_X, invoke(apertureInfo, "minChunkX"));
        assertEquals(MAX_CHUNK_X, invoke(apertureInfo, "maxChunkX"));
        assertEquals(MIN_CHUNK_Z, invoke(apertureInfo, "minChunkZ"));
        assertEquals(MAX_CHUNK_Z, invoke(apertureInfo, "maxChunkZ"));
    }

    private static void assertBoundaryTruthPreserved(ApertureSerializedApertureInfo apertureInfo) {
        assertEquals(CENTER_X, apertureInfo.centerX());
        assertEquals(CENTER_Y, apertureInfo.centerY());
        assertEquals(CENTER_Z, apertureInfo.centerZ());
        assertEquals(MIN_CHUNK_X, apertureInfo.minChunkX());
        assertEquals(MAX_CHUNK_X, apertureInfo.maxChunkX());
        assertEquals(MIN_CHUNK_Z, apertureInfo.minChunkZ());
        assertEquals(MAX_CHUNK_Z, apertureInfo.maxChunkZ());
    }

    private static Object newWorldData() {
        return instantiate(loadClass("com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData"));
    }

    private static void assumeRealPersistenceRuntimeAvailable() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
            isRealPersistenceRuntimeAvailable(),
            "当前 test runtime classpath 不包含 Minecraft SavedData/NBT 类型，跳过真实持久化路径验证"
        );
    }

    private static boolean isRealPersistenceRuntimeAvailable() {
        return isLoadable("net.minecraft.world.level.saveddata.SavedData")
            && isLoadable("net.minecraft.nbt.CompoundTag")
            && isLoadable("net.minecraft.nbt.ListTag")
            && isLoadable("com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData");
    }

    private static boolean isLoadable(String className) {
        try {
            Class.forName(className, false, ApertureWorldDataMigrationTests.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    private static Object invokeSave(Object worldData) {
        Object compoundTag = newCompoundTag();
        Method saveMethod = findMethod(worldData.getClass(), "save", 2);
        return invokeMethod(saveMethod, worldData, compoundTag, null);
    }

    private static Object invokePrivateStaticLoad(Object tag) {
        Class<?> worldDataClass = loadClass("com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData");
        Method loadMethod = findDeclaredMethod(worldDataClass, "load", 1);
        loadMethod.setAccessible(true);
        return invokeMethod(loadMethod, null, tag);
    }

    private static Object createApertureEntryTag(UUID owner, ApertureSerializedApertureInfo apertureInfo) {
        Object entryTag = newCompoundTag();
        invoke(entryTag, "putUUID", KEY_OWNER, owner);
        putTag(entryTag, KEY_INFO, createApertureInfoTag(apertureInfo));
        return entryTag;
    }

    private static Object createInitializationStateEntryTag(UUID owner, ApertureInitializationState state) {
        Object entryTag = newCompoundTag();
        invoke(entryTag, "putUUID", KEY_OWNER, owner);
        putTag(entryTag, KEY_INFO, createInitializationStateTag(state));
        return entryTag;
    }

    private static Object createInitializedOwnerTag(UUID owner) {
        Object entryTag = newCompoundTag();
        invoke(entryTag, "putUUID", KEY_OWNER, owner);
        return entryTag;
    }

    private static Object createApertureInfoTag(ApertureSerializedApertureInfo apertureInfo) {
        Object infoTag = newCompoundTag();
        putInt(infoTag, "centerX", apertureInfo.centerX());
        putInt(infoTag, "centerY", apertureInfo.centerY());
        putInt(infoTag, "centerZ", apertureInfo.centerZ());
        putInt(infoTag, "minChunkX", apertureInfo.minChunkX());
        putInt(infoTag, "maxChunkX", apertureInfo.maxChunkX());
        putInt(infoTag, "minChunkZ", apertureInfo.minChunkZ());
        putInt(infoTag, "maxChunkZ", apertureInfo.maxChunkZ());
        putFloat(infoTag, "timeSpeed", apertureInfo.timeSpeed());
        putLong(infoTag, "nextTribulationTick", apertureInfo.nextTribulationTick());
        putBoolean(infoTag, "isFrozen", apertureInfo.frozen());
        putFloat(infoTag, "favorability", apertureInfo.favorability());
        putInt(infoTag, "tier", apertureInfo.tier());
        return infoTag;
    }

    private static Object createInitializationStateTag(ApertureInitializationState initializationState) {
        Object infoTag = newCompoundTag();
        invoke(infoTag, "putString", KEY_INIT_PHASE, initializationState.initPhase().name());
        if (initializationState.openingSnapshot() != null) {
            putTag(infoTag, KEY_OPENING_SNAPSHOT, createSnapshotTag(initializationState.openingSnapshot()));
        }
        if (initializationState.layoutVersion() != null) {
            putInt(infoTag, KEY_LAYOUT_VERSION, initializationState.layoutVersion());
        }
        if (initializationState.planSeed() != null) {
            putLong(infoTag, KEY_PLAN_SEED, initializationState.planSeed());
        }
        return infoTag;
    }

    private static Object createSnapshotTag(ApertureOpeningSnapshot snapshot) {
        Object snapshotTag = newCompoundTag();
        putInt(snapshotTag, "zhuanshu", snapshot.zhuanshu());
        putInt(snapshotTag, "jieduan", snapshot.jieduan());
        putInt(snapshotTag, "heavenScore", snapshot.heavenScore());
        putInt(snapshotTag, "earthScore", snapshot.earthScore());
        putInt(snapshotTag, "humanScore", snapshot.humanScore());
        putInt(snapshotTag, "balanceScore", snapshot.balanceScore());
        putBoolean(snapshotTag, "ascensionAttemptInitiated", snapshot.ascensionAttemptInitiated());
        putBoolean(snapshotTag, "snapshotFrozen", snapshot.snapshotFrozen());
        return snapshotTag;
    }

    private static void putTag(Object compoundTag, String key, Object tagValue) {
        invoke(compoundTag, "put", key, tagValue);
    }

    private static void putInt(Object compoundTag, String key, int value) {
        invoke(compoundTag, "putInt", key, value);
    }

    private static void putLong(Object compoundTag, String key, long value) {
        invoke(compoundTag, "putLong", key, value);
    }

    private static void putFloat(Object compoundTag, String key, float value) {
        invoke(compoundTag, "putFloat", key, value);
    }

    private static void putBoolean(Object compoundTag, String key, boolean value) {
        invoke(compoundTag, "putBoolean", key, value);
    }

    private static void addToList(Object listTag, Object value) {
        invoke(listTag, "add", value);
    }

    private static int listSize(Object listTag) {
        return (Integer) invoke(listTag, "size");
    }

    private static Object newCompoundTag() {
        return instantiate(loadClass("net.minecraft.nbt.CompoundTag"));
    }

    private static Object newListTag() {
        return instantiate(loadClass("net.minecraft.nbt.ListTag"));
    }

    private static Object invoke(Object target, String methodName, Object... args) {
        Method method = findMethod(target.getClass(), methodName, args.length);
        return invokeMethod(method, target, args);
    }

    private static Method findMethod(Class<?> type, String methodName, int parameterCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("未找到方法: " + type.getName() + '#' + methodName + '/' + parameterCount);
    }

    private static Method findDeclaredMethod(Class<?> type, String methodName, int parameterCount) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                return method;
            }
        }
        throw new IllegalStateException("未找到声明方法: " + type.getName() + '#' + methodName + '/' + parameterCount);
    }

    private static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("方法不可访问: " + method.getName(), exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("方法执行失败: " + method.getName(), exception.getCause());
        }
    }

    private static Object instantiate(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            assertNotNull(instance);
            return instance;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("缺少无参构造: " + type.getName(), exception);
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new IllegalStateException("无法实例化: " + type.getName(), exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException("构造执行失败: " + type.getName(), exception.getCause());
        }
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("无法加载类型: " + className, exception);
        }
    }

    private static ApertureSerializedApertureInfo createApertureInfo() {
        return new ApertureSerializedApertureInfo(
            CENTER_X,
            CENTER_Y,
            CENTER_Z,
            MIN_CHUNK_X,
            MAX_CHUNK_X,
            MIN_CHUNK_Z,
            MAX_CHUNK_Z,
            TIME_SPEED,
            NEXT_TRIBULATION_TICK,
            false,
            FAVORABILITY,
            TIER
        );
    }

    private static ApertureOpeningSnapshot createSnapshot() {
        return new ApertureOpeningSnapshot(5, 5, 82, 79, 88, 90, true, true);
    }
}
