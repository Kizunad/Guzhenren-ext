package com.Kizunad.guzhenrenext.entity.mind;

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

final class EntityMemoryTest {

    private static final int MAX_SHORT_TERM_MEMORIES = 20;

    private static final MemoryNbtHarness HARNESS = new MemoryNbtHarness();

    @Test
    void testAddAndReadShortTermMemory() throws Exception {
        Object memory = HARNESS.newMemory();
        HARNESS.addShortTermMemory(memory, "见到敌人");
        HARNESS.addShortTermMemory(memory, "发现灵石");

        List<?> memories = HARNESS.getShortTermMemories(memory);
        assertEquals(2, memories.size());
        assertEquals("见到敌人", memories.get(0));
        assertEquals("发现灵石", memories.get(1));
    }

    @Test
    void testShortTermMemoryOverflow() throws Exception {
        Object memory = HARNESS.newMemory();
        int count = MAX_SHORT_TERM_MEMORIES + 5;
        for (int index = 0; index < count; index++) {
            HARNESS.addShortTermMemory(memory, "event-" + index);
        }

        List<?> memories = HARNESS.getShortTermMemories(memory);
        assertEquals(MAX_SHORT_TERM_MEMORIES, memories.size());
        assertEquals("event-5", memories.get(0));
        assertEquals("event-24", memories.get(memories.size() - 1));
    }

    @Test
    void testRelationTagSetGetAndRemove() throws Exception {
        Object memory = HARNESS.newMemory();
        UUID entityId = UUID.randomUUID();

        assertEquals("NEUTRAL", HARNESS.getRelationTag(memory, entityId));

        HARNESS.setRelationTag(memory, entityId, "ENEMY");
        assertEquals("ENEMY", HARNESS.getRelationTag(memory, entityId));

        HARNESS.removeRelationTag(memory, entityId);
        assertEquals("NEUTRAL", HARNESS.getRelationTag(memory, entityId));
    }

    @Test
    void testNbtRoundTrip() throws Exception {
        Object memory = HARNESS.newMemory();
        UUID ally = UUID.randomUUID();
        UUID enemy = UUID.randomUUID();

        HARNESS.addShortTermMemory(memory, "发现敌修");
        HARNESS.addShortTermMemory(memory, "与盟友交易");
        HARNESS.setRelationTag(memory, ally, "ALLY");
        HARNESS.setRelationTag(memory, enemy, "ENEMY");

        Object tag = HARNESS.save(memory);
        Object loaded = HARNESS.newMemory();
        HARNESS.load(loaded, tag);

        List<?> loadedMemories = HARNESS.getShortTermMemories(loaded);
        assertEquals(2, loadedMemories.size());
        assertEquals("发现敌修", loadedMemories.get(0));
        assertEquals("与盟友交易", loadedMemories.get(1));
        assertEquals("ALLY", HARNESS.getRelationTag(loaded, ally));
        assertEquals("ENEMY", HARNESS.getRelationTag(loaded, enemy));
        assertEquals("NEUTRAL", HARNESS.getRelationTag(loaded, UUID.randomUUID()));
    }

    private static final class MemoryNbtHarness {

        private static final String TARGET_SOURCE_PATH =
            "src/main/java/com/Kizunad/guzhenrenext/entity/mind/EntityMemory.java";

        private static final String CLASS_NAME =
            "com.Kizunad.guzhenrenext.entity.mind.EntityMemory";

        private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";

        private final Class<?> memoryClass;

        private final Class<?> compoundTagClass;

        private final Constructor<?> memoryCtor;

        private final Method addShortTermMemoryMethod;

        private final Method getShortTermMemoriesMethod;

        private final Method setRelationTagMethod;

        private final Method getRelationTagMethod;

        private final Method removeRelationTagMethod;

        private final Method saveMethod;

        private final Method loadMethod;

        private MemoryNbtHarness() {
            try {
                IsolatedCompilation isolated = compileIsolated();
                memoryClass = isolated.loadClass(CLASS_NAME);
                compoundTagClass = isolated.loadClass(COMPOUND_TAG_CLASS_NAME);

                memoryCtor = memoryClass.getDeclaredConstructor();
                addShortTermMemoryMethod = memoryClass.getMethod("addShortTermMemory", String.class);
                getShortTermMemoriesMethod = memoryClass.getMethod("getShortTermMemories");
                setRelationTagMethod = memoryClass.getMethod("setRelationTag", UUID.class, String.class);
                getRelationTagMethod = memoryClass.getMethod("getRelationTag", UUID.class);
                removeRelationTagMethod = memoryClass.getMethod("removeRelationTag", UUID.class);
                saveMethod = memoryClass.getMethod("save");
                loadMethod = memoryClass.getMethod("load", compoundTagClass);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private IsolatedCompilation compileIsolated() {
            try {
                Path sourceRoot = Files.createTempDirectory("entity-memory-src");
                Path classesRoot = Files.createTempDirectory("entity-memory-classes");
                Path targetSource = Path.of(System.getProperty("user.dir")).resolve(TARGET_SOURCE_PATH);

                List<Path> stubs = List.of(
                    writeStub(sourceRoot, "net/minecraft/nbt/Tag.java", stubTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/CompoundTag.java", stubCompoundTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/ListTag.java", stubListTag())
                );

                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                if (compiler == null) {
                    throw new IllegalStateException("当前环境缺少 JavaCompiler");
                }

                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
                try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
                    List<java.io.File> sources = new java.util.ArrayList<>();
                    sources.add(targetSource.toFile());
                    for (Path stub : stubs) {
                        sources.add(stub.toFile());
                    }
                    Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(sources);
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
                        units
                    ).call();
                    if (!success) {
                        throw new IllegalStateException("隔离编译 EntityMemory 失败");
                    }
                }

                return new IsolatedCompilation(classesRoot);
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }

        private Path writeStub(Path root, String relativePath, String source) throws Exception {
            Path path = root.resolve(relativePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, source);
            return path;
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

                    public void putString(String key, String value) {
                        values.put(key, value);
                    }

                    public void putUUID(String key, UUID value) {
                        values.put(key, value);
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

                    public ListTag getList(String key, int expectedType) {
                        Object value = values.get(key);
                        return value instanceof ListTag ? (ListTag) value : new ListTag();
                    }

                    public String getString(String key) {
                        Object value = values.get(key);
                        return value instanceof String ? (String) value : "";
                    }

                    public boolean hasUUID(String key) {
                        return values.get(key) instanceof UUID;
                    }

                    public UUID getUUID(String key) {
                        return (UUID) values.get(key);
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
                        Tag value = elements.get(index);
                        return value instanceof CompoundTag ? (CompoundTag) value : new CompoundTag();
                    }
                }
                """;
        }

        private Object newMemory() throws Exception {
            return memoryCtor.newInstance();
        }

        private void addShortTermMemory(Object memory, String event) throws Exception {
            addShortTermMemoryMethod.invoke(memory, event);
        }

        private List<?> getShortTermMemories(Object memory) throws Exception {
            return (List<?>) getShortTermMemoriesMethod.invoke(memory);
        }

        private void setRelationTag(Object memory, UUID entityId, String tag) throws Exception {
            setRelationTagMethod.invoke(memory, entityId, tag);
        }

        private String getRelationTag(Object memory, UUID entityId) throws Exception {
            return (String) getRelationTagMethod.invoke(memory, entityId);
        }

        private void removeRelationTag(Object memory, UUID entityId) throws Exception {
            removeRelationTagMethod.invoke(memory, entityId);
        }

        private Object save(Object memory) throws Exception {
            return saveMethod.invoke(memory);
        }

        private void load(Object memory, Object tag) throws Exception {
            loadMethod.invoke(memory, tag);
        }

        private static final class IsolatedCompilation {

            private final ChildFirstClassLoader classLoader;

            private IsolatedCompilation(Path classesRoot) throws Exception {
                classLoader = new ChildFirstClassLoader(
                    new URL[] {classesRoot.toUri().toURL()},
                    EntityMemoryTest.class.getClassLoader()
                );
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
                return className.startsWith(CLASS_NAME) || className.startsWith("net.minecraft.");
            }
        }
    }
}
