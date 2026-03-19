package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SwordCombatOpsNormalDamageTests {

    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final int BENMING_CONTEXT_ARG_COUNT = 9;

    private static final double TEST_MAGIC_10_0D = 10.0D;
    private static final float TEST_MAGIC_11_5F = 11.5F;
    private static final float TEST_MAGIC_14_03F = 14.03F;
    private static final float TEST_MAGIC_10_58F = 10.58F;
    private static final float TEST_MAGIC_4_9105F = 4.9105F;
    private static final float TEST_MAGIC_9_821F = 9.821F;
    private static final double TEST_MAGIC_2_4192D = 2.4192D;
    private static final double TEST_MAGIC_1_692D = 1.692D;
    private static final int TEST_MAGIC_20 = 20;
    private static final int TEST_MAGIC_17 = 17;
    private static final int TEST_MAGIC_22 = 22;
    private static final float TEST_MAGIC_17_5375F = 17.5375F;
    private static final double TEST_MAGIC_2_90304D = 2.90304D;
    private static final int TEST_MAGIC_14 = 14;
    private static final float TEST_MAGIC_11_224F = 11.224F;
    private static final double TEST_MAGIC_2_05632D = 2.05632D;
    private static final int TEST_MAGIC_21 = 21;
    private static final int TEST_MAGIC_6 = 6;

    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final String ATTRIBUTES_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes";
    private static final String COMBAT_OPS_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai.SwordCombatOps";
    private static final double DOUBLE_DELTA = 1.0E-6D;

    @Test
    void normalDamageKeepsLegacyBaselineWhenNoBenmingContextApplies() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", TEST_MAGIC_10_0D);
        api.setDoubleField(attrs, "speedMax", 2.0D);

        final float damage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            null
        );

        assertEquals(TEST_MAGIC_11_5F, damage, DOUBLE_DELTA);
    }

    @Test
    void normalDamageUsesResonanceMappedDamageForBoundBenmingSword() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", TEST_MAGIC_10_0D);
        api.setDoubleField(attrs, "speedMax", 2.0D);

        final Object offenseContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            200L,
            0L,
            0L,
            0L,
            0L
        );
        final Object defenseContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "defense",
            200L,
            0L,
            0L,
            0L,
            0L
        );

        final float offenseDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            offenseContext
        );
        final float defenseDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            defenseContext
        );

        assertEquals(TEST_MAGIC_14_03F, offenseDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_10_58F, defenseDamage, DOUBLE_DELTA);
        assertTrue(offenseDamage > defenseDamage);
    }

    @Test
    void backlashAndRecoveryPenaltyReduceDamageButNeverBelowZero() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", TEST_MAGIC_10_0D);
        api.setDoubleField(attrs, "speedMax", 2.0D);

        final Object backlashContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            200L,
            0L,
            260L,
            320L,
            0L
        );
        final Object recoveryContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            280L,
            0L,
            260L,
            320L,
            0L
        );
        final Object invalidContext = api.newBenmingDamageContext(
            "stable-benming",
            "other-sword",
            false,
            "offense",
            200L,
            0L,
            260L,
            320L,
            0L
        );

        final float backlashDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            backlashContext
        );
        final float recoveryDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            recoveryContext
        );
        final float invalidDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            invalidContext
        );

        assertEquals(TEST_MAGIC_4_9105F, backlashDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_9_821F, recoveryDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_11_5F, invalidDamage, DOUBLE_DELTA);
        assertTrue(backlashDamage >= 0.0F);
        assertTrue(recoveryDamage >= backlashDamage);
    }

    @Test
    void pursuitSpeedUsesResonanceMappingOnlyForValidBenmingContext() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "speedMax", 2.0D);

        final Object offenseContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            200L,
            0L,
            0L,
            0L,
            0L
        );
        final Object defenseContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "defense",
            200L,
            0L,
            0L,
            0L,
            0L
        );
        final Object invalidContext = api.newBenmingDamageContext(
            "stable-benming",
            "other-sword",
            false,
            "offense",
            200L,
            0L,
            0L,
            0L,
            0L
        );

        final double offenseSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            offenseContext
        );
        final double defenseSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            defenseContext
        );
        final double baselineSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            null
        );
        final double invalidSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            invalidContext
        );

        assertEquals(TEST_MAGIC_2_4192D, offenseSpeed, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_1_692D, defenseSpeed, DOUBLE_DELTA);
        assertEquals(2.0D, baselineSpeed, DOUBLE_DELTA);
        assertEquals(2.0D, invalidSpeed, DOUBLE_DELTA);
        assertTrue(offenseSpeed > baselineSpeed);
        assertTrue(baselineSpeed > defenseSpeed);
    }

    @Test
    void attackCooldownUsesResonanceMappingAndFallsBackSafely() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setIntField(attrs, "attackCooldown", TEST_MAGIC_20);

        final Object offenseContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            200L,
            0L,
            260L,
            320L,
            0L
        );
        final Object defenseContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "defense",
            200L,
            0L,
            260L,
            320L,
            0L
        );
        final Object dirtyContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            true,
            "spirit",
            200L,
            0L,
            260L,
            320L,
            0L
        );

        assertEquals(TEST_MAGIC_17, api.resolveCombatAttackCooldown(attrs, offenseContext));
        assertEquals(TEST_MAGIC_22, api.resolveCombatAttackCooldown(attrs, defenseContext));
        assertEquals(TEST_MAGIC_20, api.resolveCombatAttackCooldown(attrs, null));
        assertEquals(TEST_MAGIC_20, api.resolveCombatAttackCooldown(attrs, dirtyContext));
        assertTrue(api.resolveCombatAttackCooldown(attrs, offenseContext) >= 0);
        assertTrue(api.resolveCombatAttackCooldown(attrs, defenseContext) >= 0);
    }

    @Test
    void burstActiveWindowBoostsCombatOutputsRelativeToNeutralBenmingBaseline()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", TEST_MAGIC_10_0D);
        api.setDoubleField(attrs, "speedMax", 2.0D);
        api.setIntField(attrs, "attackCooldown", TEST_MAGIC_20);

        final Object neutralContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            200L,
            0L,
            0L,
            0L,
            0L
        );
        final Object activeContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            200L,
            240L,
            0L,
            0L,
            300L
        );

        final float neutralDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            neutralContext
        );
        final float activeDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            activeContext
        );
        final double neutralPursuitSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            neutralContext
        );
        final double activePursuitSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            activeContext
        );
        final int neutralCooldown = api.resolveCombatAttackCooldown(
            attrs,
            neutralContext
        );
        final int activeCooldown = api.resolveCombatAttackCooldown(
            attrs,
            activeContext
        );

        assertEquals(TEST_MAGIC_14_03F, neutralDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_17_5375F, activeDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_2_4192D, neutralPursuitSpeed, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_2_90304D, activePursuitSpeed, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_17, neutralCooldown);
        assertEquals(TEST_MAGIC_14, activeCooldown);
        assertTrue(activeDamage > neutralDamage);
        assertTrue(activePursuitSpeed > neutralPursuitSpeed);
        assertTrue(activeCooldown < neutralCooldown);
    }

    @Test
    void aftershockWindowPenalizesCombatOutputsRelativeToNeutralBenmingBaseline()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", TEST_MAGIC_10_0D);
        api.setDoubleField(attrs, "speedMax", 2.0D);
        api.setIntField(attrs, "attackCooldown", TEST_MAGIC_20);

        final Object neutralContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            260L,
            0L,
            0L,
            0L,
            0L
        );
        final Object aftershockContext = api.newBenmingDamageContext(
            "stable-benming",
            "stable-benming",
            false,
            "offense",
            260L,
            240L,
            0L,
            0L,
            300L
        );

        final float neutralDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            neutralContext
        );
        final float aftershockDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            aftershockContext
        );
        final double neutralPursuitSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            neutralContext
        );
        final double aftershockPursuitSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            aftershockContext
        );
        final int neutralCooldown = api.resolveCombatAttackCooldown(
            attrs,
            neutralContext
        );
        final int aftershockCooldown = api.resolveCombatAttackCooldown(
            attrs,
            aftershockContext
        );

        assertEquals(TEST_MAGIC_14_03F, neutralDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_11_224F, aftershockDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_2_4192D, neutralPursuitSpeed, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_2_05632D, aftershockPursuitSpeed, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_17, neutralCooldown);
        assertEquals(TEST_MAGIC_21, aftershockCooldown);
        assertTrue(aftershockDamage < neutralDamage);
        assertTrue(aftershockPursuitSpeed < neutralPursuitSpeed);
        assertTrue(aftershockCooldown > neutralCooldown);
    }

    @Test
    void invalidBenmingContextStillFallsBackToSafeLegacyBehaviorDuringBurstWindows()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object attrs = api.newAttributes();
        api.setDoubleField(attrs, "damage", TEST_MAGIC_10_0D);
        api.setDoubleField(attrs, "speedMax", 2.0D);
        api.setIntField(attrs, "attackCooldown", TEST_MAGIC_20);

        final Object invalidBurstContext = api.newBenmingDamageContext(
            "stable-benming",
            "other-sword",
            false,
            "offense",
            200L,
            240L,
            0L,
            0L,
            300L
        );

        final float neutralDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            null
        );
        final float invalidDamage = api.calculateNormalAttackDamage(
            attrs,
            1.0D,
            1.0F,
            invalidBurstContext
        );
        final double neutralPursuitSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            null
        );
        final double invalidPursuitSpeed = api.resolveCombatPursuitSpeed(
            attrs,
            1.0D,
            invalidBurstContext
        );
        final int neutralCooldown = api.resolveCombatAttackCooldown(attrs, null);
        final int invalidCooldown = api.resolveCombatAttackCooldown(
            attrs,
            invalidBurstContext
        );

        assertEquals(TEST_MAGIC_11_5F, neutralDamage, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_11_5F, invalidDamage, DOUBLE_DELTA);
        assertEquals(2.0D, neutralPursuitSpeed, DOUBLE_DELTA);
        assertEquals(2.0D, invalidPursuitSpeed, DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_20, neutralCooldown);
        assertEquals(TEST_MAGIC_20, invalidCooldown);
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> attributesClass;
        private final Class<?> combatOpsClass;
        private final Class<?> benmingDamageContextClass;
        private final Method calculateNormalAttackDamageMethod;
        private final Method resolveCombatPursuitSpeedMethod;
        private final Method resolveCombatAttackCooldownMethod;

        private RuntimeApi(
            final Class<?> attributesClass,
            final Class<?> combatOpsClass,
            final Class<?> benmingDamageContextClass
        ) throws ReflectiveOperationException {
            this.attributesClass = attributesClass;
            this.combatOpsClass = combatOpsClass;
            this.benmingDamageContextClass = benmingDamageContextClass;
            this.calculateNormalAttackDamageMethod = combatOpsClass.getDeclaredMethod(
                "calculateNormalAttackDamage",
                attributesClass,
                double.class,
                float.class,
                benmingDamageContextClass
            );
            this.resolveCombatPursuitSpeedMethod = combatOpsClass.getDeclaredMethod(
                "resolveCombatPursuitSpeed",
                attributesClass,
                double.class,
                benmingDamageContextClass
            );
            this.resolveCombatAttackCooldownMethod = combatOpsClass.getDeclaredMethod(
                "resolveCombatAttackCooldown",
                attributesClass,
                benmingDamageContextClass
            );
            this.calculateNormalAttackDamageMethod.setAccessible(true);
            this.resolveCombatPursuitSpeedMethod.setAccessible(true);
            this.resolveCombatAttackCooldownMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> attrsClass = Class.forName(ATTRIBUTES_CLASS_NAME, true, loader);
            final Class<?> opsClass = Class.forName(COMBAT_OPS_CLASS_NAME, true, loader);
            return new RuntimeApi(
                attrsClass,
                opsClass,
                Class.forName(
                    COMBAT_OPS_CLASS_NAME + "$BenmingDamageContext",
                    true,
                    loader
                )
            );
        }

        Object newAttributes() throws Exception {
            return attributesClass.getConstructor().newInstance();
        }

        void setDoubleField(final Object attrs, final String fieldName, final double value)
            throws Exception {
            final Field field = attributesClass.getField(fieldName);
            field.setDouble(attrs, value);
        }

        void setIntField(final Object attrs, final String fieldName, final int value)
            throws Exception {
            final Field field = attributesClass.getField(fieldName);
            field.setInt(attrs, value);
        }

        Object newBenmingDamageContext(final Object... args) throws Exception {
            if (args.length != BENMING_CONTEXT_ARG_COUNT) {
                throw new IllegalArgumentException("本命伤害上下文参数数量非法: " + args.length);
            }
            final Iterator<Object> iterator = List.of(args).iterator();
            final Object swordStableId = iterator.next();
            final Object bondedSwordId = iterator.next();
            final Object bondCacheDirty = iterator.next();
            final Object resonanceType = iterator.next();
            final Object currentTick = iterator.next();
            final Object burstActiveUntilTick = iterator.next();
            final Object overloadBacklashUntilTick = iterator.next();
            final Object overloadRecoveryUntilTick = iterator.next();
            final Object burstAftershockUntilTick = iterator.next();
            final Constructor<?> constructor = benmingDamageContextClass.getDeclaredConstructor(
                String.class,
                String.class,
                boolean.class,
                String.class,
                long.class,
                long.class,
                long.class,
                long.class,
                long.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                swordStableId,
                bondedSwordId,
                bondCacheDirty,
                resonanceType,
                currentTick,
                burstActiveUntilTick,
                burstAftershockUntilTick,
                overloadBacklashUntilTick,
                overloadRecoveryUntilTick
            );
        }

        float calculateNormalAttackDamage(
            final Object attrs,
            final double currentSpeed,
            final float synergyAttackMultiplier,
            final Object benmingDamageContext
        ) throws Exception {
            return (float) calculateNormalAttackDamageMethod.invoke(
                null,
                attrs,
                currentSpeed,
                synergyAttackMultiplier,
                benmingDamageContext
            );
        }

        double resolveCombatPursuitSpeed(
            final Object attrs,
            final double chaseSpeedScale,
            final Object benmingDamageContext
        ) throws Exception {
            return (double) resolveCombatPursuitSpeedMethod.invoke(
                null,
                attrs,
                chaseSpeedScale,
                benmingDamageContext
            );
        }

        int resolveCombatAttackCooldown(
            final Object attrs,
            final Object benmingDamageContext
        ) throws Exception {
            return (int) resolveCombatAttackCooldownMethod.invoke(
                null,
                attrs,
                benmingDamageContext
            );
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
