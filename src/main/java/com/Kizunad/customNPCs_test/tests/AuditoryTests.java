package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
//import com.Kizunad.customNPCs.ai.sensors.AuditorySensor;
import com.Kizunad.customNPCs_test.overrides.TestAuditorySensor;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 听觉传感器测试逻辑
 */
@GameTestHolder("guzhenrenext_disabled")
public class AuditoryTests {

    private static final int TICK_DELAY = 20;

    /**
     * 测试：听觉传感器能检测到移动声音
     */
    @GameTest(template = "empty")
    public static void testAuditorySensorDetectsMovement(
        GameTestHelper helper
    ) {
        // 创建观察者（静止）
        Zombie observer = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(2, 2, 2)
        );
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        // 替换为短距离听觉传感器（避免听到其他测试中的声音）
        mind.getSensorManager().removeSensor("auditory");
        mind.getSensorManager().registerSensor(new TestAuditorySensor(10.0));

        // 创建移动中的目标
        Zombie target = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(4, 2, 2)
        );
        // 给目标一个速度，让它产生移动声音
        target.setItemSlot(
            EquipmentSlot.HEAD,
            new ItemStack(Items.LEATHER_HELMET)
        );
        target.setDeltaMovement(new Vec3(0.2, 0, 0));

        // 驱动 Mind
        NpcTestHelper.tickMind(helper, observer);

        // 验证：应该听到声音
        NpcTestHelper.waitForAssertion(
            helper,
            () -> {
                if (!mind.getMemory().hasMemory("heard_sounds_count")) {
                    throw new GameTestAssertException(
                        "Auditory sensor memory not present"
                    );
                }

                int count = mind
                    .getMemory()
                    .getShortTerm("heard_sounds_count", Integer.class, -1);
                if (count <= 0) {
                    throw new GameTestAssertException(
                        "Expected to hear sounds, but count was " + count
                    );
                }

                // 验证听到了移动声音
                @SuppressWarnings("unchecked")
                List<String> soundTypes = (List<String>) mind
                    .getMemory()
                    .getShortTerm("loudest_sound_types", List.class, List.of());
                if (!soundTypes.contains("movement")) {
                    throw new GameTestAssertException(
                        "Expected to hear 'movement' sound, but got: " +
                            soundTypes
                    );
                }
            },
            "Auditory sensor should detect movement sound"
        );
    }

    /**
     * 测试：听觉传感器在无声音时返回 0
     */
    @GameTest(template = "empty")
    public static void testAuditorySensorNoSounds(GameTestHelper helper) {
        // 创建孤立的观察者
        Zombie observer = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(2, 2, 2)
        );
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        // 替换为短距离听觉传感器
        mind.getSensorManager().removeSensor("auditory");
        mind.getSensorManager().registerSensor(new TestAuditorySensor(3.0));

        // 驱动 Mind
        NpcTestHelper.tickMind(helper, observer);

        // 验证：应该没有听到任何声音
        NpcTestHelper.waitForAssertion(
            helper,
            () -> {
                if (!mind.getMemory().hasMemory("heard_sounds_count")) {
                    throw new GameTestAssertException(
                        "Auditory sensor memory not present"
                    );
                }

                int count = mind
                    .getMemory()
                    .getShortTerm("heard_sounds_count", Integer.class, -1);
                if (count != 0) {
                    throw new GameTestAssertException(
                        "Expected 0 sounds, but found " + count
                    );
                }

                // 验证没有记录最响亮声音信息
                if (mind.getMemory().hasMemory("loudest_sound_types")) {
                    throw new GameTestAssertException(
                        "Memory should not contain loudest_sound_types when no sounds are heard"
                    );
                }
            },
            "Auditory sensor should detect 0 sounds"
        );
    }

    /**
     * 测试：听觉传感器能检测到多种声音类型
     */
    @GameTest(template = "empty")
    public static void testAuditorySensorMultipleSoundTypes(
        GameTestHelper helper
    ) {
        // 创建观察者
        Zombie observer = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(2, 2, 2)
        );
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        // 替换为短距离听觉传感器
        mind.getSensorManager().removeSensor("auditory");
        mind.getSensorManager().registerSensor(new TestAuditorySensor(10.0));

        // 创建目标并让它既移动又受伤
        Zombie target = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(4, 2, 2)
        );
        target.setItemSlot(
            EquipmentSlot.HEAD,
            new ItemStack(Items.LEATHER_HELMET)
        );
        target.setDeltaMovement(new Vec3(0.2, 0, 0)); // 移动
        target.hurt(helper.getLevel().damageSources().generic(), 1.0f); // 受伤

        // 驱动 Mind
        NpcTestHelper.tickMind(helper, observer);

        // 验证：应该听到多种声音
        NpcTestHelper.waitForAssertion(
            helper,
            () -> {
                @SuppressWarnings("unchecked")
                List<String> soundTypes = (List<String>) mind
                    .getMemory()
                    .getShortTerm("loudest_sound_types", List.class, List.of());

                if (soundTypes.size() < 2) {
                    throw new GameTestAssertException(
                        "Expected to hear multiple sound types, but got: " +
                            soundTypes
                    );
                }

                if (!soundTypes.contains("movement")) {
                    throw new GameTestAssertException(
                        "Expected to hear 'movement' sound"
                    );
                }

                if (!soundTypes.contains("pain")) {
                    throw new GameTestAssertException(
                        "Expected to hear 'pain' sound"
                    );
                }
            },
            "Auditory sensor should detect multiple sound types"
        );
    }

    /**
     * 测试：声音强度随距离衰减
     */
    @GameTest(template = "empty")
    public static void testAuditorySensorDistanceAttenuation(
        GameTestHelper helper
    ) {
        // 创建观察者
        Zombie observer = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(5, 2, 5)
        );
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        // 使用较大范围的听觉传感器
        mind.getSensorManager().removeSensor("auditory");
        mind.getSensorManager().registerSensor(new TestAuditorySensor(20.0));

        // 创建近距离声音源
        Zombie nearTarget = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(6, 2, 5)
        );
        nearTarget.setItemSlot(
            EquipmentSlot.HEAD,
            new ItemStack(Items.LEATHER_HELMET)
        );
        nearTarget.setDeltaMovement(new Vec3(0.2, 0, 0));

        // 驱动 Mind
        NpcTestHelper.tickMind(helper, observer);

        // 记录近距离声音强度
        NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getMemory().hasMemory("loudest_sound_intensity"),
            50,
            "Should detect near sound"
        );

        double nearIntensity = mind
            .getMemory()
            .getShortTerm("loudest_sound_intensity", Double.class, 0.0);

        // 杀死近距离目标，创建远距离声音源
        nearTarget.kill();
        helper.runAfterDelay(TICK_DELAY, () -> {
            Zombie farTarget = TestEntityFactory.createTestZombie(
                helper,
                new BlockPos(9, 2, 5)
            );
            farTarget.setItemSlot(
                EquipmentSlot.HEAD,
                new ItemStack(Items.LEATHER_HELMET)
            );
            farTarget.setDeltaMovement(new Vec3(0.2, 0, 0));

            // 再次驱动 Mind
            NpcTestHelper.tickMind(helper, observer);

            // 验证远距离声音强度更低
            helper.runAfterDelay(TICK_DELAY, () -> {
                double farIntensity = mind
                    .getMemory()
                    .getShortTerm("loudest_sound_intensity", Double.class, 0.0);

                if (farIntensity >= nearIntensity) {
                    helper.fail(
                        "Far sound intensity (" +
                            farIntensity +
                            ") should be less than near sound intensity (" +
                            nearIntensity +
                            ")"
                    );
                }

                helper.succeed();
            });
        });
    }

    /**
     * 测试：多个声音源时选择最响亮的
     */
    @GameTest(template = "empty")
    public static void testAuditorySensorMultipleSources(
        GameTestHelper helper
    ) {
        // 创建观察者
        Zombie observer = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(5, 2, 5)
        );
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        // 使用较大范围的听觉传感器
        mind.getSensorManager().removeSensor("auditory");
        mind.getSensorManager().registerSensor(new TestAuditorySensor(20.0));

        // 创建近距离声音源（应该是最响亮的）
        Zombie nearTarget = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(6, 2, 5)
        );
        nearTarget.setItemSlot(
            EquipmentSlot.HEAD,
            new ItemStack(Items.LEATHER_HELMET)
        );
        nearTarget.setDeltaMovement(new Vec3(0.2, 0, 0));

        // 创建远距离声音源
        Zombie farTarget = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(9, 2, 5)
        );
        farTarget.setItemSlot(
            EquipmentSlot.HEAD,
            new ItemStack(Items.LEATHER_HELMET)
        );
        farTarget.setDeltaMovement(new Vec3(0.2, 0, 0));

        // 驱动 Mind
        NpcTestHelper.tickMind(helper, observer);

        // 验证：选择了最响亮（近距离）的声音
        NpcTestHelper.waitForAssertion(
            helper,
            () -> {
                int count = mind
                    .getMemory()
                    .getShortTerm("heard_sounds_count", Integer.class, 0);
                if (count < 2) {
                    throw new GameTestAssertException(
                        "Expected to hear at least 2 sounds, but got " + count
                    );
                }

                // 最响亮的声音应该来自近距离目标
                Vec3 loudestLocation = mind
                    .getMemory()
                    .getShortTerm(
                        "loudest_sound_location",
                        Vec3.class,
                        Vec3.ZERO
                    );
                double distanceToLoudest = loudestLocation.distanceTo(
                    observer.position()
                );
                double distanceToNear = nearTarget
                    .position()
                    .distanceTo(observer.position());

                if (Math.abs(distanceToLoudest - distanceToNear) > 1.0) {
                    throw new GameTestAssertException(
                        "Loudest sound should be from near target, but distances don't match"
                    );
                }
            },
            "Auditory sensor should prioritize louder (closer) sounds"
        );
    }
}
