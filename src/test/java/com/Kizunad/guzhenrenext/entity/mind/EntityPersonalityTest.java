package com.Kizunad.guzhenrenext.entity.mind;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EntityPersonalityTest {

    private static final float DELTA = 0.0001F;

    private static final PersonalityNbtHarness HARNESS = new PersonalityNbtHarness();

    @Test
    void testEmotionSetAndGetWithClamp() throws Exception {
        Object personality = HARNESS.newPersonality();

        HARNESS.setEmotion(personality, "JOY", 0.6F);
        assertEquals(0.6F, HARNESS.getEmotion(personality, "JOY"), DELTA);

        HARNESS.setEmotion(personality, "JOY", 2.5F);
        assertEquals(1.0F, HARNESS.getEmotion(personality, "JOY"), DELTA);

        HARNESS.setEmotion(personality, "JOY", -0.5F);
        assertEquals(0.0F, HARNESS.getEmotion(personality, "JOY"), DELTA);
    }

    @Test
    void testDriveSetAndGetWithClamp() throws Exception {
        Object personality = HARNESS.newPersonality();

        HARNESS.setDrive(personality, "WEALTH", 0.7F);
        assertEquals(0.7F, HARNESS.getDrive(personality, "WEALTH"), DELTA);

        HARNESS.setDrive(personality, "WEALTH", 1.5F);
        assertEquals(1.0F, HARNESS.getDrive(personality, "WEALTH"), DELTA);

        HARNESS.setDrive(personality, "WEALTH", -2.0F);
        assertEquals(0.0F, HARNESS.getDrive(personality, "WEALTH"), DELTA);
    }

    @Test
    void testDominantEmotionAndStrongestDrive() throws Exception {
        Object personality = HARNESS.newPersonality();

        HARNESS.setEmotion(personality, "FEAR", 0.9F);
        HARNESS.setEmotion(personality, "JOY", 0.4F);
        assertEquals("FEAR", HARNESS.getDominantEmotionName(personality));

        HARNESS.setDrive(personality, "POWER", 0.8F);
        HARNESS.setDrive(personality, "FOOD", 0.3F);
        assertEquals("POWER", HARNESS.getStrongestDriveName(personality));
    }

    @Test
    void testTieReturnsFirstEnumValue() throws Exception {
        Object personality = HARNESS.newPersonality();

        HARNESS.setEmotion(personality, "JOY", 0.5F);
        HARNESS.setEmotion(personality, "ANGER", 0.5F);
        assertEquals("JOY", HARNESS.getDominantEmotionName(personality));

        HARNESS.setDrive(personality, "FOOD", 0.6F);
        HARNESS.setDrive(personality, "SLEEP", 0.6F);
        assertEquals("FOOD", HARNESS.getStrongestDriveName(personality));
    }

    @Test
    void testNbtSaveAndLoadRoundTrip() throws Exception {
        Object personality = HARNESS.newPersonality();

        HARNESS.setEmotion(personality, "ANGER", 0.75F);
        HARNESS.setDrive(personality, "POWER", 0.8F);

        Object tag = HARNESS.save(personality);
        Object loaded = HARNESS.newPersonality();
        HARNESS.load(loaded, tag);

        assertEquals(0.75F, HARNESS.getEmotion(loaded, "ANGER"), DELTA);
        assertEquals(0.8F, HARNESS.getDrive(loaded, "POWER"), DELTA);
    }

    @Test
    void testNbtLoadClamp() throws Exception {
        Object personality = HARNESS.newPersonality();
        Object tag = HARNESS.newCompoundTag();
        Object emotionsTag = HARNESS.newCompoundTag();
        Object drivesTag = HARNESS.newCompoundTag();

        HARNESS.putFloat(emotionsTag, "anger", 2.0F);
        HARNESS.putFloat(drivesTag, "power", -1.0F);
        HARNESS.putTag(tag, "emotions", emotionsTag);
        HARNESS.putTag(tag, "drives", drivesTag);

        HARNESS.load(personality, tag);
        assertEquals(1.0F, HARNESS.getEmotion(personality, "ANGER"), DELTA);
        assertEquals(0.0F, HARNESS.getDrive(personality, "POWER"), DELTA);
    }

    private static final class PersonalityNbtHarness {

        private static final String TARGET_SOURCE_PATH =
            "src/main/java/com/Kizunad/guzhenrenext/entity/mind/EntityPersonality.java";

        private static final String CLASS_NAME =
            "com.Kizunad.guzhenrenext.entity.mind.EntityPersonality";

        private static final String EMOTION_CLASS_NAME = CLASS_NAME + "$EmotionType";

        private static final String DRIVE_CLASS_NAME = CLASS_NAME + "$DriveType";

        private static final String COMPOUND_TAG_CLASS_NAME = "net.minecraft.nbt.CompoundTag";

        private final Class<?> personalityClass;

        private final Class<?> emotionClass;

        private final Class<?> driveClass;

        private final Class<?> compoundTagClass;

        private final Constructor<?> personalityCtor;

        private final Constructor<?> compoundTagCtor;

        private final Method setEmotionMethod;

        private final Method getEmotionMethod;

        private final Method setDriveMethod;

        private final Method getDriveMethod;

        private final Method getDominantEmotionMethod;

        private final Method getStrongestDriveMethod;

        private final Method saveMethod;

        private final Method loadMethod;

        private final Method putMethod;

        private final Method putFloatMethod;

        private PersonalityNbtHarness() {
            try {
                IsolatedCompilation isolated = compileIsolated();
                personalityClass = isolated.loadClass(CLASS_NAME);
                emotionClass = isolated.loadClass(EMOTION_CLASS_NAME);
                driveClass = isolated.loadClass(DRIVE_CLASS_NAME);
                compoundTagClass = isolated.loadClass(COMPOUND_TAG_CLASS_NAME);

                personalityCtor = personalityClass.getDeclaredConstructor();
                compoundTagCtor = compoundTagClass.getDeclaredConstructor();

                setEmotionMethod = personalityClass.getMethod("setEmotion", emotionClass, float.class);
                getEmotionMethod = personalityClass.getMethod("getEmotion", emotionClass);
                setDriveMethod = personalityClass.getMethod("setDrive", driveClass, float.class);
                getDriveMethod = personalityClass.getMethod("getDrive", driveClass);
                getDominantEmotionMethod = personalityClass.getMethod("getDominantEmotion");
                getStrongestDriveMethod = personalityClass.getMethod("getStrongestDrive");
                saveMethod = personalityClass.getMethod("save");
                loadMethod = personalityClass.getMethod("load", compoundTagClass);

                Class<?> tagClass = isolated.loadClass("net.minecraft.nbt.Tag");
                putMethod = compoundTagClass.getMethod("put", String.class, tagClass);
                putFloatMethod = compoundTagClass.getMethod("putFloat", String.class, float.class);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException(exception);
            }
        }

        private IsolatedCompilation compileIsolated() {
            try {
                Path sourceRoot = Files.createTempDirectory("entity-personality-src");
                Path classesRoot = Files.createTempDirectory("entity-personality-classes");
                Path targetSource = Path.of(System.getProperty("user.dir")).resolve(TARGET_SOURCE_PATH);

                List<Path> stubs = List.of(
                    writeStub(sourceRoot, "net/minecraft/nbt/Tag.java", stubTag()),
                    writeStub(sourceRoot, "net/minecraft/nbt/CompoundTag.java", stubCompoundTag())
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
                        throw new IllegalStateException("隔离编译 EntityPersonality 失败");
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
                    int TAG_COMPOUND = 10;
                }
                """;
        }

        private String stubCompoundTag() {
            return """
                package net.minecraft.nbt;

                import java.util.HashMap;
                import java.util.Map;

                public class CompoundTag implements Tag {
                    private final Map<String, Object> values = new HashMap<>();

                    public void put(String key, Tag value) {
                        values.put(key, value);
                    }

                    public void putFloat(String key, float value) {
                        values.put(key, Float.valueOf(value));
                    }

                    public boolean contains(String key, int expectedType) {
                        Object value = values.get(key);
                        if (value == null) {
                            return false;
                        }
                        return expectedType != TAG_COMPOUND || value instanceof CompoundTag;
                    }

                    public float getFloat(String key) {
                        Object value = values.get(key);
                        return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
                    }

                    public CompoundTag getCompound(String key) {
                        Object value = values.get(key);
                        return value instanceof CompoundTag ? (CompoundTag) value : new CompoundTag();
                    }
                }
                """;
        }

        private Object newPersonality() throws Exception {
            return personalityCtor.newInstance();
        }

        private Object newCompoundTag() throws Exception {
            return compoundTagCtor.newInstance();
        }

        private void setEmotion(Object personality, String emotionName, float value) throws Exception {
            Object emotion = enumByName(emotionClass, emotionName);
            setEmotionMethod.invoke(personality, emotion, value);
        }

        private float getEmotion(Object personality, String emotionName) throws Exception {
            Object emotion = enumByName(emotionClass, emotionName);
            return (float) getEmotionMethod.invoke(personality, emotion);
        }

        private void setDrive(Object personality, String driveName, float value) throws Exception {
            Object drive = enumByName(driveClass, driveName);
            setDriveMethod.invoke(personality, drive, value);
        }

        private float getDrive(Object personality, String driveName) throws Exception {
            Object drive = enumByName(driveClass, driveName);
            return (float) getDriveMethod.invoke(personality, drive);
        }

        private String getDominantEmotionName(Object personality) throws Exception {
            Object result = getDominantEmotionMethod.invoke(personality);
            return ((Enum<?>) result).name();
        }

        private String getStrongestDriveName(Object personality) throws Exception {
            Object result = getStrongestDriveMethod.invoke(personality);
            return ((Enum<?>) result).name();
        }

        private Object save(Object personality) throws Exception {
            return saveMethod.invoke(personality);
        }

        private void load(Object personality, Object tag) throws Exception {
            loadMethod.invoke(personality, tag);
        }

        private void putTag(Object targetTag, String key, Object valueTag) throws Exception {
            putMethod.invoke(targetTag, key, valueTag);
        }

        private void putFloat(Object targetTag, String key, float value) throws Exception {
            putFloatMethod.invoke(targetTag, key, value);
        }

        private Object enumByName(Class<?> enumClass, String name) throws Exception {
            Method valueOf = enumClass.getMethod("valueOf", String.class);
            return valueOf.invoke(null, name);
        }

        private static final class IsolatedCompilation {

            private final ChildFirstClassLoader classLoader;

            private IsolatedCompilation(Path classesRoot) throws Exception {
                classLoader = new ChildFirstClassLoader(
                    new URL[] {classesRoot.toUri().toURL()},
                    EntityPersonalityTest.class.getClassLoader()
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
