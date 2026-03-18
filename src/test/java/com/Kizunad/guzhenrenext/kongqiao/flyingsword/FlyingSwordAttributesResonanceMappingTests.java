package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordAttributesResonanceMappingTests {

    private static final String ATTRS_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final double DOUBLE_DELTA = 1.0E-9;

    @Test
    void resonanceArchetypesProduceDeterministicCombatFeelDifferences() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", 10.0D);
        api.setDoubleField(attrs, "speedMax", 2.0D);
        api.setDoubleField(attrs, "speedBase", 1.0D);
        api.setDoubleField(attrs, "turnRate", 30.0D);
        api.setIntField(attrs, "attackCooldown", 20);

        final double offenseDamage = api.getEffectiveDamage(attrs, "offense");
        final double defenseDamage = api.getEffectiveDamage(attrs, "defense");
        final double spiritDamage = api.getEffectiveDamage(attrs, "spirit");
        assertEquals(12.2D, offenseDamage, DOUBLE_DELTA);
        assertEquals(9.2D, defenseDamage, DOUBLE_DELTA);
        assertEquals(10.5D, spiritDamage, DOUBLE_DELTA);

        final double offensePursuit = api.getEffectivePursuitSpeed(attrs, "offense");
        final double defensePursuit = api.getEffectivePursuitSpeed(attrs, "defense");
        final double spiritPursuit = api.getEffectivePursuitSpeed(attrs, "spirit");
        assertEquals(2.4192D, offensePursuit, DOUBLE_DELTA);
        assertEquals(1.692D, defensePursuit, DOUBLE_DELTA);
        assertEquals(2.2896D, spiritPursuit, DOUBLE_DELTA);

        assertEquals(17, api.getEffectiveAttackCooldown(attrs, "offense"));
        assertEquals(22, api.getEffectiveAttackCooldown(attrs, "defense"));
        assertEquals(19, api.getEffectiveAttackCooldown(attrs, "spirit"));

        final double offenseGuardCostMultiplier =
            api.getGuardDurabilityCostMultiplier(attrs, "offense");
        final double defenseGuardCostMultiplier =
            api.getGuardDurabilityCostMultiplier(attrs, "defense");
        assertEquals(1.18D, offenseGuardCostMultiplier, DOUBLE_DELTA);
        assertEquals(0.82D, defenseGuardCostMultiplier, DOUBLE_DELTA);

        final double offenseRecallStability = api.getRecallStabilityScore(attrs, "offense");
        final double defenseRecallStability = api.getRecallStabilityScore(attrs, "defense");
        final double spiritRecallStability = api.getRecallStabilityScore(attrs, "spirit");
        assertEquals(13.2D, offenseRecallStability, DOUBLE_DELTA);
        assertEquals(18.0D, defenseRecallStability, DOUBLE_DELTA);
        assertEquals(16.5D, spiritRecallStability, DOUBLE_DELTA);

        assertTrue(offenseDamage > spiritDamage);
        assertTrue(spiritDamage > defenseDamage);
        assertTrue(offensePursuit > spiritPursuit);
        assertTrue(spiritPursuit > defensePursuit);
        assertTrue(defenseRecallStability > spiritRecallStability);
        assertTrue(spiritRecallStability > offenseRecallStability);
    }

    @Test
    void missingOrUnknownResonanceFallsBackToSafeBaseline() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", 9.5D);
        api.setDoubleField(attrs, "speedMax", 2.4D);
        api.setDoubleField(attrs, "speedBase", 1.2D);
        api.setDoubleField(attrs, "turnRate", 25.0D);
        api.setIntField(attrs, "attackCooldown", 13);

        final double baselineDamage = api.getEffectiveDamage(attrs);
        final double baselineSpeedMax = api.getEffectiveSpeedMax(attrs);
        final double baselinePursuit = api.getEffectiveSpeedMax(attrs);
        final int baselineCooldown = api.getIntField(attrs, "attackCooldown");
        final double baselineRecallStability =
            api.getDoubleField(attrs, "turnRate")
                * api.getEffectiveSpeedBase(attrs)
                / api.getEffectiveSpeedMax(attrs);

        assertEquals(baselineDamage, api.getEffectiveDamage(attrs, null), DOUBLE_DELTA);
        assertEquals(baselineDamage, api.getEffectiveDamage(attrs, ""), DOUBLE_DELTA);
        assertEquals(
            baselineDamage,
            api.getEffectiveDamage(attrs, "unknown-resonance"),
            DOUBLE_DELTA
        );

        assertEquals(baselineSpeedMax, api.getEffectiveSpeedMax(attrs, null), DOUBLE_DELTA);
        assertEquals(
            baselinePursuit,
            api.getEffectivePursuitSpeed(attrs, "unknown-resonance"),
            DOUBLE_DELTA
        );
        assertEquals(
            baselineCooldown,
            api.getEffectiveAttackCooldown(attrs, "unknown-resonance")
        );
        assertEquals(
            baselineRecallStability,
            api.getRecallStabilityScore(attrs, "unknown-resonance"),
            DOUBLE_DELTA
        );
        assertEquals(
            1.0D,
            api.getGuardDurabilityCostMultiplier(attrs, "unknown-resonance"),
            DOUBLE_DELTA
        );
        assertEquals(6.0D, api.mapGuardDurabilityCost(attrs, 6.0D, null), DOUBLE_DELTA);
        assertEquals(0.0D, api.mapGuardDurabilityCost(attrs, -3.0D, "offense"), DOUBLE_DELTA);
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> attributesClass;
        private final Method getEffectiveDamageNoArgMethod;
        private final Method getEffectiveDamageMethod;
        private final Method getEffectiveSpeedMaxNoArgMethod;
        private final Method getEffectiveSpeedMaxMethod;
        private final Method getEffectiveSpeedBaseMethod;
        private final Method getEffectivePursuitSpeedMethod;
        private final Method getEffectiveAttackCooldownMethod;
        private final Method getGuardDurabilityCostMultiplierMethod;
        private final Method mapGuardDurabilityCostMethod;
        private final Method getRecallStabilityScoreMethod;

        private RuntimeApi(final Class<?> attributesClass) throws NoSuchMethodException {
            this.attributesClass = attributesClass;
            this.getEffectiveDamageNoArgMethod = attributesClass.getMethod("getEffectiveDamage");
            this.getEffectiveDamageMethod = attributesClass.getMethod(
                "getEffectiveDamage",
                String.class
            );
            this.getEffectiveSpeedMaxNoArgMethod = attributesClass.getMethod("getEffectiveSpeedMax");
            this.getEffectiveSpeedMaxMethod = attributesClass.getMethod(
                "getEffectiveSpeedMax",
                String.class
            );
            this.getEffectiveSpeedBaseMethod = attributesClass.getMethod("getEffectiveSpeedBase");
            this.getEffectivePursuitSpeedMethod = attributesClass.getMethod(
                "getEffectivePursuitSpeed",
                String.class
            );
            this.getEffectiveAttackCooldownMethod = attributesClass.getMethod(
                "getEffectiveAttackCooldown",
                String.class
            );
            this.getGuardDurabilityCostMultiplierMethod = attributesClass.getMethod(
                "getGuardDurabilityCostMultiplier",
                String.class
            );
            this.mapGuardDurabilityCostMethod = attributesClass.getMethod(
                "mapGuardDurabilityCost",
                double.class,
                String.class
            );
            this.getRecallStabilityScoreMethod = attributesClass.getMethod(
                "getRecallStabilityScore",
                String.class
            );
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> attrsClass = Class.forName(ATTRS_CLASS_NAME, true, loader);
            return new RuntimeApi(attrsClass);
        }

        Object newAttributes() throws Exception {
            return attributesClass.getConstructor().newInstance();
        }

        void setDoubleField(final Object attrs, final String fieldName, final double value)
            throws Exception {
            final Field field = attributesClass.getField(fieldName);
            field.setDouble(attrs, value);
        }

        void setIntField(final Object attrs, final String fieldName, final int value) throws Exception {
            final Field field = attributesClass.getField(fieldName);
            field.setInt(attrs, value);
        }

        double getDoubleField(final Object attrs, final String fieldName) throws Exception {
            final Field field = attributesClass.getField(fieldName);
            return field.getDouble(attrs);
        }

        int getIntField(final Object attrs, final String fieldName) throws Exception {
            final Field field = attributesClass.getField(fieldName);
            return field.getInt(attrs);
        }

        double getEffectiveDamage(final Object attrs) throws Exception {
            return (double) getEffectiveDamageNoArgMethod.invoke(attrs);
        }

        double getEffectiveDamage(final Object attrs, final String resonanceRaw) throws Exception {
            return (double) getEffectiveDamageMethod.invoke(attrs, new Object[] {resonanceRaw});
        }

        double getEffectiveSpeedMax(final Object attrs) throws Exception {
            return (double) getEffectiveSpeedMaxNoArgMethod.invoke(attrs);
        }

        double getEffectiveSpeedMax(final Object attrs, final String resonanceRaw) throws Exception {
            return (double) getEffectiveSpeedMaxMethod.invoke(attrs, new Object[] {resonanceRaw});
        }

        double getEffectiveSpeedBase(final Object attrs) throws Exception {
            return (double) getEffectiveSpeedBaseMethod.invoke(attrs);
        }

        double getEffectivePursuitSpeed(final Object attrs, final String resonanceRaw)
            throws Exception {
            return (double) getEffectivePursuitSpeedMethod.invoke(attrs, new Object[] {resonanceRaw});
        }

        int getEffectiveAttackCooldown(final Object attrs, final String resonanceRaw)
            throws Exception {
            return (int) getEffectiveAttackCooldownMethod.invoke(attrs, new Object[] {resonanceRaw});
        }

        double getGuardDurabilityCostMultiplier(final Object attrs, final String resonanceRaw)
            throws Exception {
            return (double) getGuardDurabilityCostMultiplierMethod.invoke(
                attrs,
                new Object[] {resonanceRaw}
            );
        }

        double mapGuardDurabilityCost(final Object attrs, final double baseCost, final String resonanceRaw)
            throws Exception {
            return (double) mapGuardDurabilityCostMethod.invoke(
                attrs,
                new Object[] {baseCost, resonanceRaw}
            );
        }

        double getRecallStabilityScore(final Object attrs, final String resonanceRaw)
            throws Exception {
            return (double) getRecallStabilityScoreMethod.invoke(attrs, new Object[] {resonanceRaw});
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

            try (var stream = Files.walk(root, 6)) {
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
