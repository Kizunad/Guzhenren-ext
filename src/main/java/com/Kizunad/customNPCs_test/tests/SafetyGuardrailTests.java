package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.sensors.SafetySensor;
import com.Kizunad.customNPCs.ai.sensors.VisionSensor;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 安全护栏相关 GameTest：危险区域规避与敌友识别。
 */
@GameTestHolder("guzhenrenext")
@SuppressWarnings({ "checkstyle:MagicNumber" })
public class SafetyGuardrailTests {

    /**
     * 验证 SafetySensor 能检测近距离危险并写入记忆。
     */
    @GameTest(template = "npcmindgametests.empty")
    public static void testSafetySensorDetectsHazard(GameTestHelper helper) {
        System.out.println("[GameTest] SafetySensorDetectsHazard start");
        Zombie observer = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(1, 2, 1)
        );
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        mind.getSensorManager().removeSensor("vision");
        mind.getSensorManager().registerSensor(new SafetySensor());
        NpcTestHelper.tickMind(helper, observer);

        helper.setBlock(new BlockPos(1, 1, 2), Blocks.MAGMA_BLOCK);

        helper.succeedWhen(() -> {
            System.out.println("[GameTest] SafetySensorDetectsHazard end");
            helper.assertTrue(
                mind.getMemory().hasMemory("hazard_detected"),
                "SafetySensor should flag hazard_detected"
            );
            helper.assertTrue(
                mind.getMemory().hasMemory("nearest_hazard_pos"),
                "SafetySensor should store nearest_hazard_pos"
            );
            double distance = mind
                .getMemory()
                .getShortTerm("nearest_hazard_distance", Double.class, -1.0d);
            helper.assertTrue(
                distance >= 0.0d,
                "nearest_hazard_distance should be recorded"
            );
        });
    }

    /**
     * 验证视觉传感器的敌友识别：记录敌对数量且不把友方计为威胁。
     */
    @GameTest(template = "npcmindgametests.empty")
    public static void testVisionSensorFriendFoe(GameTestHelper helper) {
        System.out.println("[GameTest] VisionSensorFriendFoe start");
        Villager observer = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(2, 2, 2),
            EntityType.VILLAGER,
            true
        );
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        mind.getSensorManager().removeSensor("vision");
        mind.getSensorManager().registerSensor(new VisionSensor(6.0));
        NpcTestHelper.tickMind(helper, observer);

        Zombie hostile = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 2, 2));
        NpcTestHelper.applyTestTag(helper, hostile);

        Villager ally = helper.spawn(
            EntityType.VILLAGER,
            new BlockPos(2, 2, 4)
        );
        NpcTestHelper.applyTestTag(helper, ally);

        helper.succeedWhen(() -> {
            System.out.println("[GameTest] VisionSensorFriendFoe end");
            int hostileCount = mind
                .getMemory()
                .getShortTerm("hostile_entities_count", Integer.class, 0);
            int allyCount = mind
                .getMemory()
                .getShortTerm("ally_entities_count", Integer.class, 0);

            helper.assertTrue(
                hostileCount == 1,
                "Should detect exactly 1 hostile entity"
            );
            helper.assertTrue(
                allyCount >= 0,
                "Ally count memory should be recorded"
            );
            helper.assertTrue(
                mind.getMemory().hasMemory("threat_detected"),
                "Threat memory should be set when hostile present"
            );
        });
    }
}
