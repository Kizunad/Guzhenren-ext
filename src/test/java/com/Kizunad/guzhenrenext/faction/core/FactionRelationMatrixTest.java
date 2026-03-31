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

/**
 * FactionRelationMatrix 单元测试。
 * <p>
 * 验证势力关系矩阵的关系查询、修改、等级判断、NBT 序列化功能。
 * 使用隔离编译模式（参考 FactionCoreTest），避免测试类路径缺少 Minecraft NBT 依赖。
 * </p>
 */
final class FactionRelationMatrixTest {

    private static final MatrixTestHarness HARNESS = new MatrixTestHarness();

    @Test
    void testDefaultRelationIsNeutral() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        int relation = HARNESS.getRelation(matrix, factionA, factionB);
        assertEquals(0, relation, "未设置的关系应默认为 0（中立）");
    }

    @Test
    void testSetAndGetRelation() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, 50);
        assertEquals(50, HARNESS.getRelation(matrix, factionA, factionB), "设置的关系值应正确返回");
    }

    @Test
    void testRelationSymmetry() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, 75);
        assertEquals(75, HARNESS.getRelation(matrix, factionA, factionB), "getRelation(A, B) 应返回 75");
        assertEquals(75, HARNESS.getRelation(matrix, factionB, factionA), "getRelation(B, A) 应返回相同值 75");
    }

    @Test
    void testRelationSymmetryReverse() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionB, factionA, -60);
        assertEquals(-60, HARNESS.getRelation(matrix, factionB, factionA), "setRelation(B, A) 应正确设置");
        assertEquals(-60, HARNESS.getRelation(matrix, factionA, factionB), "getRelation(A, B) 应返回相同值 -60");
    }

    @Test
    void testSelfRelationIsAlwaysZero() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID faction = UUID.randomUUID();

        int relation = HARNESS.getRelation(matrix, faction, faction);
        assertEquals(0, relation, "自己与自己的关系应始终为 0");
    }

    @Test
    void testSelfRelationCannotBeModified() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID faction = UUID.randomUUID();

        HARNESS.setRelation(matrix, faction, faction, 100);
        int relation = HARNESS.getRelation(matrix, faction, faction);
        assertEquals(0, relation, "自己与自己的关系不应被修改");
    }

    @Test
    void testRelationValueClampingMin() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, -200);
        assertEquals(-100, HARNESS.getRelation(matrix, factionA, factionB), "关系值应被 clamp 到最小值 -100");
    }

    @Test
    void testRelationValueClampingMax() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, 200);
        assertEquals(100, HARNESS.getRelation(matrix, factionA, factionB), "关系值应被 clamp 到最大值 100");
    }

    @Test
    void testRelationValueClampingBoundary() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, -100);
        assertEquals(-100, HARNESS.getRelation(matrix, factionA, factionB), "关系值 -100 应被接受");

        HARNESS.setRelation(matrix, factionA, factionB, 100);
        assertEquals(100, HARNESS.getRelation(matrix, factionA, factionB), "关系值 100 应被接受");
    }

    @Test
    void testGetRelationLevelHostile() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, -100);
        assertEquals("HOSTILE", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 -100 应为 HOSTILE");

        HARNESS.setRelation(matrix, factionA, factionB, -51);
        assertEquals("HOSTILE", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 -51 应为 HOSTILE");
    }

    @Test
    void testGetRelationLevelNeutral() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, -50);
        assertEquals("NEUTRAL", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 -50 应为 NEUTRAL");

        HARNESS.setRelation(matrix, factionA, factionB, 0);
        assertEquals("NEUTRAL", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 0 应为 NEUTRAL");

        HARNESS.setRelation(matrix, factionA, factionB, 50);
        assertEquals("NEUTRAL", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 50 应为 NEUTRAL");
    }

    @Test
    void testGetRelationLevelFriendly() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, 51);
        assertEquals("FRIENDLY", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 51 应为 FRIENDLY");

        HARNESS.setRelation(matrix, factionA, factionB, 80);
        assertEquals("FRIENDLY", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 80 应为 FRIENDLY");
    }

    @Test
    void testGetRelationLevelAllied() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, 81);
        assertEquals("ALLIED", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 81 应为 ALLIED");

        HARNESS.setRelation(matrix, factionA, factionB, 100);
        assertEquals("ALLIED", HARNESS.getRelationLevelName(matrix, factionA, factionB),
            "关系值 100 应为 ALLIED");
    }

    @Test
    void testNullFactionHandling() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID faction = UUID.randomUUID();

        int relation = HARNESS.getRelation(matrix, null, faction);
        assertEquals(0, relation, "null 势力应返回默认关系 0");

        relation = HARNESS.getRelation(matrix, faction, null);
        assertEquals(0, relation, "null 势力应返回默认关系 0");

        HARNESS.setRelation(matrix, null, faction, 50);
        relation = HARNESS.getRelation(matrix, null, faction);
        assertEquals(0, relation, "null 势力的关系不应被设置");
    }

    @Test
    void testMultipleRelationUpdates() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, 10);
        assertEquals(10, HARNESS.getRelation(matrix, factionA, factionB), "初始关系应为 10");

        HARNESS.setRelation(matrix, factionA, factionB, 50);
        assertEquals(50, HARNESS.getRelation(matrix, factionA, factionB), "更新后关系应为 50");

        HARNESS.setRelation(matrix, factionA, factionB, -30);
        assertEquals(-30, HARNESS.getRelation(matrix, factionA, factionB), "再次更新后关系应为 -30");
    }

    @Test
    void testRelationLevelBoundaryTransitions() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, -51);
        assertEquals("HOSTILE", HARNESS.getRelationLevelName(matrix, factionA, factionB));

        HARNESS.setRelation(matrix, factionA, factionB, -50);
        assertEquals("NEUTRAL", HARNESS.getRelationLevelName(matrix, factionA, factionB));

        HARNESS.setRelation(matrix, factionA, factionB, 50);
        assertEquals("NEUTRAL", HARNESS.getRelationLevelName(matrix, factionA, factionB));

        HARNESS.setRelation(matrix, factionA, factionB, 51);
        assertEquals("FRIENDLY", HARNESS.getRelationLevelName(matrix, factionA, factionB));

        HARNESS.setRelation(matrix, factionA, factionB, 80);
        assertEquals("FRIENDLY", HARNESS.getRelationLevelName(matrix, factionA, factionB));

        HARNESS.setRelation(matrix, factionA, factionB, 81);
        assertEquals("ALLIED", HARNESS.getRelationLevelName(matrix, factionA, factionB));
    }

    @Test
    void testNbtRoundTrip() throws Exception {
        Object matrix = HARNESS.newMatrix();
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();
        UUID factionC = UUID.randomUUID();

        HARNESS.setRelation(matrix, factionA, factionB, 75);
        HARNESS.setRelation(matrix, factionA, factionC, -60);
        HARNESS.setRelation(matrix, factionB, factionC, 30);

        // 序列化
        Object tag = HARNESS.save(matrix);
        assertNotNull(tag, "save() 应返回非空 CompoundTag");

        // 反序列化
        Object loaded = HARNESS.load(tag);
        assertNotNull(loaded, "load() 应返回非空 FactionRelationMatrix");

        // 验证关系值
        assertEquals(75, HARNESS.getRelation(loaded, factionA, factionB), "A-B 关系应为 75");
        assertEquals(-60, HARNESS.getRelation(loaded, factionA, factionC), "A-C 关系应为 -60");
        assertEquals(30, HARNESS.getRelation(loaded, factionB, factionC), "B-C 关系应为 30");

        // 验证对称性保持
        assertEquals(75, HARNESS.getRelation(loaded, factionB, factionA), "B-A 关系应与 A-B 相同");
    }

    @Test
    void testNbtLoadFromEmptyTag() throws Exception {
        Object emptyTag = HARNESS.newCompoundTag();
        Object loaded = HARNESS.load(emptyTag);
        assertNull(loaded, "缺少 relations 字段时应返回 null");
    }

    /**
     * 关系矩阵测试支架。
     * <p>
     * 使用隔离编译模式，避免测试类路径缺少 Minecraft NBT 依赖。
     * 通过反射调用 FactionRelationMatrix 的方法。
     * </p>
     */
    private static final class MatrixTestHarness {

        private static final String MATRIX_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionRelationMatrix";

        private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";

        private static final String TARGET_SOURCE_PATH =
            "src/main/java/com/Kizunad/guzhenrenext/faction/core/FactionRelationMatrix.java";

        private final Class<?> matrixClass;

        private final Class<?> compoundTagClass;

        private final Constructor<?> matrixConstructor;

        private final Constructor<?> compoundTagConstructor;

        private final Method getRelationMethod;

        private final Method setRelationMethod;

        private final Method getRelationLevelMethod;

        private final Method saveMethod;

        private final Method loadMethod;

        private MatrixTestHarness() {
            try {
                IsolatedCompilation isolated = compileIsolated();
                matrixClass = isolated.loadClass(MATRIX_CLASS_NAME);
                compoundTagClass = isolated.loadClass(COMPOUND_TAG_CLASS_NAME);

                matrixConstructor = matrixClass.getDeclaredConstructor();
                compoundTagConstructor = compoundTagClass.getDeclaredConstructor();

                getRelationMethod = matrixClass.getMethod("getRelation", UUID.class, UUID.class);
                setRelationMethod = matrixClass.getMethod("setRelation", UUID.class, UUID.class, int.class);
                getRelationLevelMethod = matrixClass.getMethod("getRelationLevel", UUID.class, UUID.class);
                saveMethod = matrixClass.getMethod("save");
                loadMethod = matrixClass.getMethod("load", compoundTagClass);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("无法初始化关系矩阵反射测试支架", exception);
            }
        }

        private IsolatedCompilation compileIsolated() {
            try {
                Path sourceRoot = Files.createTempDirectory("faction-matrix-src");
                Path classesRoot = Files.createTempDirectory("faction-matrix-classes");
                Path targetSource = Path.of(System.getProperty("user.dir")).resolve(TARGET_SOURCE_PATH);
                if (!Files.exists(targetSource)) {
                    throw new IllegalStateException("未找到目标源码：" + targetSource);
                }

                List<Path> stubSources = List.of(
                    writeStub(sourceRoot, "net/minecraft/nbt/Tag.java", stubTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/CompoundTag.java", stubCompoundTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/ListTag.java", stubListTag())
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
                    Iterable<? extends JavaFileObject> compilationUnits =
                        fileManager.getJavaFileObjectsFromFiles(sourceFiles);
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
                        StringBuilder sb = new StringBuilder("隔离编译 FactionRelationMatrix 失败：\n");
                        for (javax.tools.Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                            sb.append(d.toString()).append('\n');
                        }
                        throw new IllegalStateException(sb.toString());
                    }
                }
                return new IsolatedCompilation(classesRoot);
            } catch (Exception exception) {
                throw new IllegalStateException("无法构建隔离版 FactionRelationMatrix 测试运行时", exception);
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
                        if (expectedType == TAG_LIST) {
                            return value instanceof ListTag;
                        }
                        if (expectedType == TAG_COMPOUND) {
                            return value instanceof CompoundTag;
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

                    public ListTag getList(String key, int type) {
                        Object value = values.get(key);
                        return value instanceof ListTag ? (ListTag) value : new ListTag();
                    }
                }
                """;
        }

        private String stubListTag() {
            return """
                package net.minecraft.nbt;

                import java.util.ArrayList;
                import java.util.List;

                public class ListTag implements Tag {
                    private final List<Tag> elements = new ArrayList<>();

                    public boolean add(Tag element) {
                        return elements.add(element);
                    }

                    public int size() {
                        return elements.size();
                    }

                    public CompoundTag getCompound(int index) {
                        Tag element = elements.get(index);
                        return element instanceof CompoundTag ? (CompoundTag) element : new CompoundTag();
                    }
                }
                """;
        }

        private Object newMatrix() throws Exception {
            return matrixConstructor.newInstance();
        }

        private Object newCompoundTag() throws Exception {
            return compoundTagConstructor.newInstance();
        }

        private int getRelation(Object matrix, UUID factionA, UUID factionB) throws Exception {
            return (int) getRelationMethod.invoke(matrix, factionA, factionB);
        }

        private void setRelation(Object matrix, UUID factionA, UUID factionB, int value) throws Exception {
            setRelationMethod.invoke(matrix, factionA, factionB, value);
        }

        private String getRelationLevelName(Object matrix, UUID factionA, UUID factionB) throws Exception {
            Object level = getRelationLevelMethod.invoke(matrix, factionA, factionB);
            return ((Enum<?>) level).name();
        }

        private Object save(Object matrix) throws Exception {
            return saveMethod.invoke(matrix);
        }

        private Object load(Object tag) throws Exception {
            return loadMethod.invoke(null, tag);
        }

        private static final class IsolatedCompilation {

            private final ChildFirstClassLoader classLoader;

            private IsolatedCompilation(Path classesRoot) throws Exception {
                URL[] urls = new URL[] {classesRoot.toUri().toURL()};
                classLoader = new ChildFirstClassLoader(urls, FactionRelationMatrixTest.class.getClassLoader());
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
                return className.startsWith(MATRIX_CLASS_NAME) || className.startsWith("net.minecraft.");
            }
        }
    }
}
