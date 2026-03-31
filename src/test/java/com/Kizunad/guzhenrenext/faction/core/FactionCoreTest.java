package com.Kizunad.guzhenrenext.faction.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * FactionCore 单元测试。
 * <p>
 * 验证势力核心数据模型的序列化/反序列化往返、字段验证等功能。
 * </p>
 */
final class FactionCoreTest {

    private static final NbtTestHarness NBT = new NbtTestHarness();

    @Test
    void testSaveAndLoadRoundTrip() throws Exception {
        // 创建原始势力数据
        UUID factionId = UUID.randomUUID();
        String factionName = "天剑宗";
        Object factionType = NBT.getFactionType("SECT");
        long createdAt = 1000L;
        Object factionStatus = NBT.getFactionStatus("ACTIVE");
        int power = 5000;
        int resources = 3000;

        Object original = NBT.newFactionCore(factionId, factionName, factionType, createdAt, factionStatus, power,
            resources);

        // 序列化到 NBT
        Object tag = NBT.save(original);
        assertNotNull(tag, "save() 应返回非空 CompoundTag");

        // 反序列化
        Object loaded = NBT.load(tag);
        assertNotNull(loaded, "load() 应返回非空 FactionCore");

        // 验证所有字段相同
        assertEquals(factionId, NBT.getId(loaded), "id 应相同");
        assertEquals(factionName, NBT.getName(loaded), "name 应相同");
        assertEquals("SECT", NBT.getTypeName(loaded), "type 应相同");
        assertEquals(createdAt, NBT.getCreatedAt(loaded), "createdAt 应相同");
        assertEquals("ACTIVE", NBT.getStatusName(loaded), "status 应相同");
        assertEquals(power, NBT.getPower(loaded), "power 应相同");
        assertEquals(resources, NBT.getResources(loaded), "resources 应相同");
    }

    @Test
    void testLoadWithMissingFields() throws Exception {
        // 创建不完整的 NBT（缺少 name 字段）
        Object incompleteTag = NBT.newCompoundTag();
        NBT.putUUID(incompleteTag, "id", UUID.randomUUID());
        // 缺少 name、type、createdAt、status、power、resources

        Object loaded = NBT.load(incompleteTag);
        assertNull(loaded, "缺少必填字段时应返回 null");
    }

    @Test
    void testLoadWithInvalidEnumValue() throws Exception {
        // 创建包含无效枚举值的 NBT
        Object invalidTag = NBT.newCompoundTag();
        NBT.putUUID(invalidTag, "id", UUID.randomUUID());
        NBT.putString(invalidTag, "name", "测试宗门");
        NBT.putString(invalidTag, "type", "INVALID_TYPE");
        NBT.putLong(invalidTag, "createdAt", 1000L);
        NBT.putString(invalidTag, "status", "ACTIVE");
        NBT.putInt(invalidTag, "power", 5000);
        NBT.putInt(invalidTag, "resources", 3000);

        Object loaded = NBT.load(invalidTag);
        assertNull(loaded, "无效的枚举值应导致返回 null");
    }

    @Test
    void testConstructorValidation() throws Exception {
        UUID validId = UUID.randomUUID();
        String validName = "测试宗门";
        Object validType = NBT.getFactionType("SECT");
        long validCreatedAt = 1000L;
        Object validStatus = NBT.getFactionStatus("ACTIVE");

        // 测试 power 超出范围
        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, validName, validType,
            validCreatedAt, validStatus, -1, 5000), IllegalArgumentException.class, "power 为负数应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, validName, validType,
            validCreatedAt, validStatus, 10001, 5000), IllegalArgumentException.class, "power 超过最大值应抛出异常");

        // 测试 resources 超出范围
        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, validName, validType,
            validCreatedAt, validStatus, 5000, -1), IllegalArgumentException.class, "resources 为负数应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, validName, validType,
            validCreatedAt, validStatus, 5000, 10001), IllegalArgumentException.class, "resources 超过最大值应抛出异常");

        // 测试 createdAt 为负数
        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, validName, validType, -1L,
            validStatus, 5000, 5000), IllegalArgumentException.class, "createdAt 为负数应抛出异常");

        // 测试 name 为空字符串
        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, "", validType, validCreatedAt,
            validStatus, 5000, 5000), IllegalArgumentException.class, "name 为空字符串应抛出异常");

        // 测试 null 字段
        assertThrowsReflectionException(() -> NBT.newFactionCore(null, validName, validType, validCreatedAt,
            validStatus, 5000, 5000), NullPointerException.class, "id 为 null 应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, null, validType, validCreatedAt,
            validStatus, 5000, 5000), NullPointerException.class, "name 为 null 应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, validName, null, validCreatedAt,
            validStatus, 5000, 5000), NullPointerException.class, "type 为 null 应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionCore(validId, validName, validType, validCreatedAt,
            null, 5000, 5000), NullPointerException.class, "status 为 null 应抛出异常");
    }

    private static void assertThrowsReflectionException(ThrowingRunnable runnable, Class<? extends Throwable> expectedCause, String message) {
        try {
            runnable.run();
            throw new AssertionError(message);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (!expectedCause.isInstance(e.getCause())) {
                throw new AssertionError(message + " - 期望 " + expectedCause.getSimpleName() + " 但得到 " + e.getCause().getClass().getSimpleName(), e);
            }
        } catch (Throwable e) {
            throw new AssertionError(message, e);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @Test
    void testMultipleFactionTypes() throws Exception {
        UUID factionId = UUID.randomUUID();
        String factionName = "测试势力";
        long createdAt = 2000L;
        Object factionStatus = NBT.getFactionStatus("ACTIVE");

        // 测试 SECT 类型
        Object sect = NBT.newFactionCore(factionId, factionName, NBT.getFactionType("SECT"), createdAt,
            factionStatus, 5000, 3000);
        Object sectTag = NBT.save(sect);
        Object loadedSect = NBT.load(sectTag);
        assertEquals("SECT", NBT.getTypeName(loadedSect), "SECT 类型应正确序列化/反序列化");

        // 测试 CLAN 类型
        Object clan = NBT.newFactionCore(factionId, factionName, NBT.getFactionType("CLAN"), createdAt,
            factionStatus, 5000, 3000);
        Object clanTag = NBT.save(clan);
        Object loadedClan = NBT.load(clanTag);
        assertEquals("CLAN", NBT.getTypeName(loadedClan), "CLAN 类型应正确序列化/反序列化");

        // 测试 ROGUE_GROUP 类型
        Object rogueGroup = NBT.newFactionCore(factionId, factionName, NBT.getFactionType("ROGUE_GROUP"),
            createdAt, factionStatus, 5000, 3000);
        Object rogueGroupTag = NBT.save(rogueGroup);
        Object loadedRogueGroup = NBT.load(rogueGroupTag);
        assertEquals("ROGUE_GROUP", NBT.getTypeName(loadedRogueGroup),
            "ROGUE_GROUP 类型应正确序列化/反序列化");
    }

    @Test
    void testMultipleFactionStatuses() throws Exception {
        UUID factionId = UUID.randomUUID();
        String factionName = "测试势力";
        Object factionType = NBT.getFactionType("SECT");
        long createdAt = 3000L;

        // 测试 ACTIVE 状态
        Object active = NBT.newFactionCore(factionId, factionName, factionType, createdAt,
            NBT.getFactionStatus("ACTIVE"), 5000, 3000);
        Object activeTag = NBT.save(active);
        Object loadedActive = NBT.load(activeTag);
        assertEquals("ACTIVE", NBT.getStatusName(loadedActive), "ACTIVE 状态应正确序列化/反序列化");

        // 测试 DISSOLVED 状态
        Object dissolved = NBT.newFactionCore(factionId, factionName, factionType, createdAt,
            NBT.getFactionStatus("DISSOLVED"), 5000, 3000);
        Object dissolvedTag = NBT.save(dissolved);
        Object loadedDissolved = NBT.load(dissolvedTag);
        assertEquals("DISSOLVED", NBT.getStatusName(loadedDissolved),
            "DISSOLVED 状态应正确序列化/反序列化");

        // 测试 AT_WAR 状态
        Object atWar = NBT.newFactionCore(factionId, factionName, factionType, createdAt,
            NBT.getFactionStatus("AT_WAR"), 5000, 3000);
        Object atWarTag = NBT.save(atWar);
        Object loadedAtWar = NBT.load(atWarTag);
        assertEquals("AT_WAR", NBT.getStatusName(loadedAtWar), "AT_WAR 状态应正确序列化/反序列化");
    }

    @Test
    void testBoundaryValues() throws Exception {
        UUID factionId = UUID.randomUUID();
        String factionName = "测试宗门";
        Object factionType = NBT.getFactionType("SECT");
        long createdAt = 4000L;
        Object factionStatus = NBT.getFactionStatus("ACTIVE");

        // 测试最小值
        Object minValues = NBT.newFactionCore(factionId, factionName, factionType, createdAt, factionStatus, 0, 0);
        Object minTag = NBT.save(minValues);
        Object loadedMin = NBT.load(minTag);
        assertEquals(0, NBT.getPower(loadedMin), "power 最小值应为 0");
        assertEquals(0, NBT.getResources(loadedMin), "resources 最小值应为 0");

        // 测试最大值
        Object maxValues = NBT.newFactionCore(factionId, factionName, factionType, createdAt, factionStatus, 10000,
            10000);
        Object maxTag = NBT.save(maxValues);
        Object loadedMax = NBT.load(maxTag);
        assertEquals(10000, NBT.getPower(loadedMax), "power 最大值应为 10000");
        assertEquals(10000, NBT.getResources(loadedMax), "resources 最大值应为 10000");
    }

    @Test
    void testChineseCharactersInName() throws Exception {
        UUID factionId = UUID.randomUUID();
        String chineseName = "天剑宗门-测试";
        Object factionType = NBT.getFactionType("SECT");
        long createdAt = 5000L;
        Object factionStatus = NBT.getFactionStatus("ACTIVE");

        Object original = NBT.newFactionCore(factionId, chineseName, factionType, createdAt, factionStatus, 5000,
            3000);
        Object tag = NBT.save(original);
        Object loaded = NBT.load(tag);

        assertEquals(chineseName, NBT.getName(loaded), "中文名称应正确序列化/反序列化");
    }

    private static final class NbtTestHarness {

        private static final String FACTION_CORE_CLASS_NAME = "com.Kizunad.guzhenrenext.faction.core.FactionCore";

        private static final String FACTION_TYPE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionCore$FactionType";

        private static final String FACTION_STATUS_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionCore$FactionStatus";

        private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";

        private static final String TARGET_SOURCE_PATH =
            "src/main/java/com/Kizunad/guzhenrenext/faction/core/FactionCore.java";

        private final Class<?> factionCoreClass;

        private final Class<?> factionTypeClass;

        private final Class<?> factionStatusClass;

        private final Class<?> compoundTagClass;

        private final Constructor<?> factionCoreConstructor;

        private final Constructor<?> compoundTagConstructor;

        private final Method factionCoreSaveMethod;

        private final Method factionCoreLoadMethod;

        private final Method compoundTagPutUUIDMethod;

        private final Method compoundTagPutStringMethod;

        private final Method compoundTagPutLongMethod;

        private final Method compoundTagPutIntMethod;

        private final Method compoundTagGetUUIDMethod;

        private final Method compoundTagGetStringMethod;

        private final Method compoundTagGetLongMethod;

        private final Method compoundTagGetIntMethod;

        private final Method compoundTagHasUUIDMethod;

        private final Method compoundTagContainsMethod;

        private NbtTestHarness() {
            try {
                IsolatedCompilation isolated = compileIsolatedFactionCore();
                factionCoreClass = isolated.loadClass(FACTION_CORE_CLASS_NAME);
                factionTypeClass = isolated.loadClass(FACTION_TYPE_CLASS_NAME);
                factionStatusClass = isolated.loadClass(FACTION_STATUS_CLASS_NAME);
                compoundTagClass = isolated.loadClass(COMPOUND_TAG_CLASS_NAME);

                factionCoreConstructor = factionCoreClass.getDeclaredConstructor(
                    java.util.UUID.class,
                    String.class,
                    factionTypeClass,
                    long.class,
                    factionStatusClass,
                    int.class,
                    int.class
                );

                compoundTagConstructor = compoundTagClass.getDeclaredConstructor();

                factionCoreSaveMethod = factionCoreClass.getMethod("save");
                factionCoreLoadMethod = factionCoreClass.getMethod("load", compoundTagClass);

                Class<?> tagClass = isolated.loadClass("net.minecraft.nbt.Tag");
                compoundTagPutUUIDMethod = compoundTagClass.getMethod("putUUID", String.class, java.util.UUID.class);
                compoundTagPutStringMethod = compoundTagClass.getMethod("putString", String.class, String.class);
                compoundTagPutLongMethod = compoundTagClass.getMethod("putLong", String.class, long.class);
                compoundTagPutIntMethod = compoundTagClass.getMethod("putInt", String.class, int.class);
                compoundTagGetUUIDMethod = compoundTagClass.getMethod("getUUID", String.class);
                compoundTagGetStringMethod = compoundTagClass.getMethod("getString", String.class);
                compoundTagGetLongMethod = compoundTagClass.getMethod("getLong", String.class);
                compoundTagGetIntMethod = compoundTagClass.getMethod("getInt", String.class);
                compoundTagHasUUIDMethod = compoundTagClass.getMethod("hasUUID", String.class);
                compoundTagContainsMethod = compoundTagClass.getMethod("contains", String.class);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("无法初始化 NBT 反射测试支架", exception);
            }
        }

        private IsolatedCompilation compileIsolatedFactionCore() {
            try {
                Path sourceRoot = Files.createTempDirectory("faction-core-src");
                Path classesRoot = Files.createTempDirectory("faction-core-classes");
                Path targetSource = Path.of(System.getProperty("user.dir")).resolve(TARGET_SOURCE_PATH);
                if (!Files.exists(targetSource)) {
                    throw new IllegalStateException("未找到目标源码：" + targetSource);
                }

                List<Path> stubSources = List.of(
                    writeStub(sourceRoot, "javax/annotation/Nullable.java", stubNullable()),
                    writeStub(sourceRoot, "net/minecraft/nbt/Tag.java", stubTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/CompoundTag.java", stubCompoundTag())
                );

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                if (compiler == null) {
                    throw new IllegalStateException("当前运行环境不提供 JavaCompiler");
                }

                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
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
                        null,
                        fileManager,
                        diagnostics,
                        options,
                        null,
                        compilationUnits
                    ).call();
                    if (!success) {
                        throw new IllegalStateException("隔离编译 FactionCore 失败");
                    }
                }
                return new IsolatedCompilation(classesRoot);
            } catch (Exception exception) {
                throw new IllegalStateException("无法构建隔离版 FactionCore 测试运行时", exception);
            }
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
                            return value instanceof java.util.List;
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
                }
                """;
        }

        private Object newFactionCore(UUID id, String name, Object type, long createdAt, Object status, int power,
            int resources) throws Exception {
            return factionCoreConstructor.newInstance(id, name, type, createdAt, status, power, resources);
        }

        private Object newCompoundTag() throws Exception {
            return compoundTagConstructor.newInstance();
        }

        private void putUUID(Object compoundTag, String key, UUID value) throws Exception {
            compoundTagPutUUIDMethod.invoke(compoundTag, key, value);
        }

        private void putString(Object compoundTag, String key, String value) throws Exception {
            compoundTagPutStringMethod.invoke(compoundTag, key, value);
        }

        private void putLong(Object compoundTag, String key, long value) throws Exception {
            compoundTagPutLongMethod.invoke(compoundTag, key, value);
        }

        private void putInt(Object compoundTag, String key, int value) throws Exception {
            compoundTagPutIntMethod.invoke(compoundTag, key, value);
        }

        private Object save(Object factionCore) throws Exception {
            return factionCoreSaveMethod.invoke(factionCore);
        }

        private Object load(Object tag) throws Exception {
            return factionCoreLoadMethod.invoke(null, tag);
        }

        private UUID getId(Object factionCore) throws Exception {
            Method method = factionCoreClass.getMethod("id");
            return (UUID) method.invoke(factionCore);
        }

        private String getName(Object factionCore) throws Exception {
            Method method = factionCoreClass.getMethod("name");
            return (String) method.invoke(factionCore);
        }

        private String getTypeName(Object factionCore) throws Exception {
            Method method = factionCoreClass.getMethod("type");
            Object type = method.invoke(factionCore);
            return ((Enum<?>) type).name();
        }

        private long getCreatedAt(Object factionCore) throws Exception {
            Method method = factionCoreClass.getMethod("createdAt");
            return (long) method.invoke(factionCore);
        }

        private String getStatusName(Object factionCore) throws Exception {
            Method method = factionCoreClass.getMethod("status");
            Object status = method.invoke(factionCore);
            return ((Enum<?>) status).name();
        }

        private int getPower(Object factionCore) throws Exception {
            Method method = factionCoreClass.getMethod("power");
            return (int) method.invoke(factionCore);
        }

        private int getResources(Object factionCore) throws Exception {
            Method method = factionCoreClass.getMethod("resources");
            return (int) method.invoke(factionCore);
        }

        private Object getFactionType(String name) throws Exception {
            Method method = factionTypeClass.getMethod("valueOf", String.class);
            return method.invoke(null, name);
        }

        private Object getFactionStatus(String name) throws Exception {
            Method method = factionStatusClass.getMethod("valueOf", String.class);
            return method.invoke(null, name);
        }

        private static final class IsolatedCompilation {

            private final ChildFirstClassLoader classLoader;

            private IsolatedCompilation(Path classesRoot) throws Exception {
                URL[] urls = new URL[] {classesRoot.toUri().toURL()};
                classLoader = new ChildFirstClassLoader(urls, FactionCoreTest.class.getClassLoader());
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
                return className.startsWith(FACTION_CORE_CLASS_NAME) || className.startsWith("net.minecraft.");
            }
        }
    }
}
