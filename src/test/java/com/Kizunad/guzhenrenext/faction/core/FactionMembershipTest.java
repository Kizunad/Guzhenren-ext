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
 * FactionMembership 单元测试。
 * <p>
 * 验证成员数据模型的序列化/反序列化往返、字段验证等功能。
 * </p>
 */
final class FactionMembershipTest {

    private static final NbtTestHarness NBT = new NbtTestHarness();

    @Test
    void testSaveAndLoadRoundTrip() throws Exception {
        // 创建原始成员数据
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();
        Object memberRole = NBT.getMemberRole("LEADER");
        long joinedAt = 1000L;
        int contribution = 5000;

        Object original = NBT.newFactionMembership(memberId, factionId, memberRole, joinedAt, contribution);

        // 序列化到 NBT
        Object tag = NBT.save(original);
        assertNotNull(tag, "save() 应返回非空 CompoundTag");

        // 反序列化
        Object loaded = NBT.load(tag);
        assertNotNull(loaded, "load() 应返回非空 FactionMembership");

        // 验证所有字段相同
        assertEquals(memberId, NBT.getMemberId(loaded), "memberId 应相同");
        assertEquals(factionId, NBT.getFactionId(loaded), "factionId 应相同");
        assertEquals("LEADER", NBT.getRoleName(loaded), "role 应相同");
        assertEquals(joinedAt, NBT.getJoinedAt(loaded), "joinedAt 应相同");
        assertEquals(contribution, NBT.getContribution(loaded), "contribution 应相同");
    }

    @Test
    void testLoadWithMissingFields() throws Exception {
        // 创建不完整的 NBT（缺少 role 字段）
        Object incompleteTag = NBT.newCompoundTag();
        NBT.putUUID(incompleteTag, "memberId", UUID.randomUUID());
        NBT.putUUID(incompleteTag, "factionId", UUID.randomUUID());
        // 缺少 role、joinedAt、contribution

        Object loaded = NBT.load(incompleteTag);
        assertNull(loaded, "缺少必填字段时应返回 null");
    }

    @Test
    void testLoadWithInvalidEnumValue() throws Exception {
        // 创建包含无效枚举值的 NBT
        Object invalidTag = NBT.newCompoundTag();
        NBT.putUUID(invalidTag, "memberId", UUID.randomUUID());
        NBT.putUUID(invalidTag, "factionId", UUID.randomUUID());
        NBT.putString(invalidTag, "role", "INVALID_ROLE");
        NBT.putLong(invalidTag, "joinedAt", 1000L);
        NBT.putInt(invalidTag, "contribution", 5000);

        Object loaded = NBT.load(invalidTag);
        assertNull(loaded, "无效的枚举值应导致返回 null");
    }

    @Test
    void testConstructorValidation() throws Exception {
        UUID validMemberId = UUID.randomUUID();
        UUID validFactionId = UUID.randomUUID();
        Object validRole = NBT.getMemberRole("MEMBER");
        long validJoinedAt = 1000L;

        // 测试 contribution 超出范围
        assertThrowsReflectionException(() -> NBT.newFactionMembership(validMemberId, validFactionId, validRole,
            validJoinedAt, -1), IllegalArgumentException.class, "contribution 为负数应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionMembership(validMemberId, validFactionId, validRole,
            validJoinedAt, 100001), IllegalArgumentException.class, "contribution 超过最大值应抛出异常");

        // 测试 joinedAt 为负数
        assertThrowsReflectionException(() -> NBT.newFactionMembership(validMemberId, validFactionId, validRole, -1L,
            5000), IllegalArgumentException.class, "joinedAt 为负数应抛出异常");

        // 测试 null 字段
        assertThrowsReflectionException(() -> NBT.newFactionMembership(null, validFactionId, validRole,
            validJoinedAt, 5000), NullPointerException.class, "memberId 为 null 应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionMembership(validMemberId, null, validRole,
            validJoinedAt, 5000), NullPointerException.class, "factionId 为 null 应抛出异常");

        assertThrowsReflectionException(() -> NBT.newFactionMembership(validMemberId, validFactionId, null,
            validJoinedAt, 5000), NullPointerException.class, "role 为 null 应抛出异常");
    }

    @Test
    void testAllMemberRoles() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();
        long joinedAt = 2000L;
        int contribution = 3000;

        // 测试 LEADER 角色
        Object leader = NBT.newFactionMembership(memberId, factionId, NBT.getMemberRole("LEADER"), joinedAt,
            contribution);
        Object leaderTag = NBT.save(leader);
        Object loadedLeader = NBT.load(leaderTag);
        assertEquals("LEADER", NBT.getRoleName(loadedLeader), "LEADER 角色应正确序列化/反序列化");

        // 测试 ELDER 角色
        Object elder = NBT.newFactionMembership(memberId, factionId, NBT.getMemberRole("ELDER"), joinedAt,
            contribution);
        Object elderTag = NBT.save(elder);
        Object loadedElder = NBT.load(elderTag);
        assertEquals("ELDER", NBT.getRoleName(loadedElder), "ELDER 角色应正确序列化/反序列化");

        // 测试 MEMBER 角色
        Object member = NBT.newFactionMembership(memberId, factionId, NBT.getMemberRole("MEMBER"), joinedAt,
            contribution);
        Object memberTag = NBT.save(member);
        Object loadedMember = NBT.load(memberTag);
        assertEquals("MEMBER", NBT.getRoleName(loadedMember), "MEMBER 角色应正确序列化/反序列化");

        // 测试 OUTER_DISCIPLE 角色
        Object outerDisciple = NBT.newFactionMembership(memberId, factionId, NBT.getMemberRole("OUTER_DISCIPLE"),
            joinedAt, contribution);
        Object outerDiscipleTag = NBT.save(outerDisciple);
        Object loadedOuterDisciple = NBT.load(outerDiscipleTag);
        assertEquals("OUTER_DISCIPLE", NBT.getRoleName(loadedOuterDisciple),
            "OUTER_DISCIPLE 角色应正确序列化/反序列化");
    }

    @Test
    void testBoundaryValues() throws Exception {
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();
        Object memberRole = NBT.getMemberRole("MEMBER");
        long joinedAt = 3000L;

        // 测试最小值
        Object minValues = NBT.newFactionMembership(memberId, factionId, memberRole, joinedAt, 0);
        Object minTag = NBT.save(minValues);
        Object loadedMin = NBT.load(minTag);
        assertEquals(0, NBT.getContribution(loadedMin), "contribution 最小值应为 0");

        // 测试最大值
        Object maxValues = NBT.newFactionMembership(memberId, factionId, memberRole, joinedAt, 100000);
        Object maxTag = NBT.save(maxValues);
        Object loadedMax = NBT.load(maxTag);
        assertEquals(100000, NBT.getContribution(loadedMax), "contribution 最大值应为 100000");
    }

    @Test
    void testChineseCharactersInUUIDs() throws Exception {
        // 测试中文 UUID 字符串（虽然 UUID 本身不包含中文，但测试数据完整性）
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();
        Object memberRole = NBT.getMemberRole("LEADER");
        long joinedAt = 4000L;
        int contribution = 7500;

        Object original = NBT.newFactionMembership(memberId, factionId, memberRole, joinedAt, contribution);
        Object tag = NBT.save(original);
        Object loaded = NBT.load(tag);

        assertEquals(memberId, NBT.getMemberId(loaded), "memberId 应正确序列化/反序列化");
        assertEquals(factionId, NBT.getFactionId(loaded), "factionId 应正确序列化/反序列化");
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

    private static final class NbtTestHarness {

        private static final String FACTION_MEMBERSHIP_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionMembership";

        private static final String MEMBER_ROLE_CLASS_NAME =
            "com.Kizunad.guzhenrenext.faction.core.FactionMembership$MemberRole";

        private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";

        private static final String TARGET_SOURCE_PATH =
            "src/main/java/com/Kizunad/guzhenrenext/faction/core/FactionMembership.java";

        private final Class<?> factionMembershipClass;

        private final Class<?> memberRoleClass;

        private final Class<?> compoundTagClass;

        private final Constructor<?> factionMembershipConstructor;

        private final Constructor<?> compoundTagConstructor;

        private final Method factionMembershipSaveMethod;

        private final Method factionMembershipLoadMethod;

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
                IsolatedCompilation isolated = compileIsolatedFactionMembership();
                factionMembershipClass = isolated.loadClass(FACTION_MEMBERSHIP_CLASS_NAME);
                memberRoleClass = isolated.loadClass(MEMBER_ROLE_CLASS_NAME);
                compoundTagClass = isolated.loadClass(COMPOUND_TAG_CLASS_NAME);

                factionMembershipConstructor = factionMembershipClass.getDeclaredConstructor(
                    java.util.UUID.class,
                    java.util.UUID.class,
                    memberRoleClass,
                    long.class,
                    int.class
                );

                compoundTagConstructor = compoundTagClass.getDeclaredConstructor();

                factionMembershipSaveMethod = factionMembershipClass.getMethod("save");
                factionMembershipLoadMethod = factionMembershipClass.getMethod("load", compoundTagClass);

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

        private IsolatedCompilation compileIsolatedFactionMembership() {
            try {
                Path sourceRoot = Files.createTempDirectory("faction-membership-src");
                Path classesRoot = Files.createTempDirectory("faction-membership-classes");
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
                        throw new IllegalStateException("隔离编译 FactionMembership 失败");
                    }
                }
                return new IsolatedCompilation(classesRoot);
            } catch (Exception exception) {
                throw new IllegalStateException("无法构建隔离版 FactionMembership 测试运行时", exception);
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

        private Object newFactionMembership(UUID memberId, UUID factionId, Object role, long joinedAt,
            int contribution) throws Exception {
            return factionMembershipConstructor.newInstance(memberId, factionId, role, joinedAt, contribution);
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

        private Object save(Object factionMembership) throws Exception {
            return factionMembershipSaveMethod.invoke(factionMembership);
        }

        private Object load(Object tag) throws Exception {
            return factionMembershipLoadMethod.invoke(null, tag);
        }

        private UUID getMemberId(Object factionMembership) throws Exception {
            Method method = factionMembershipClass.getMethod("memberId");
            return (UUID) method.invoke(factionMembership);
        }

        private UUID getFactionId(Object factionMembership) throws Exception {
            Method method = factionMembershipClass.getMethod("factionId");
            return (UUID) method.invoke(factionMembership);
        }

        private String getRoleName(Object factionMembership) throws Exception {
            Method method = factionMembershipClass.getMethod("role");
            Object role = method.invoke(factionMembership);
            return ((Enum<?>) role).name();
        }

        private long getJoinedAt(Object factionMembership) throws Exception {
            Method method = factionMembershipClass.getMethod("joinedAt");
            return (long) method.invoke(factionMembership);
        }

        private int getContribution(Object factionMembership) throws Exception {
            Method method = factionMembershipClass.getMethod("contribution");
            return (int) method.invoke(factionMembership);
        }

        private Object getMemberRole(String name) throws Exception {
            Method method = memberRoleClass.getMethod("valueOf", String.class);
            return method.invoke(null, name);
        }

        private static final class IsolatedCompilation {

            private final ChildFirstClassLoader classLoader;

            private IsolatedCompilation(Path classesRoot) throws Exception {
                URL[] urls = new URL[] {classesRoot.toUri().toURL()};
                classLoader = new ChildFirstClassLoader(urls, FactionMembershipTest.class.getClassLoader());
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
                return className.startsWith(FACTION_MEMBERSHIP_CLASS_NAME) || className.startsWith("net.minecraft.");
            }
        }
    }
}
