package com.Kizunad.guzhenrenext_test.faction;

import com.Kizunad.guzhenrenext.entity.RogueEntity;
import com.Kizunad.guzhenrenext.entity.spawn.RogueSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 散修生成器 GameTest。
 * <p>
 * 验证 RogueSpawner 的生成功能：
 * - 散修可以在指定位置生成
 * - 生成的散修存在于世界中
 * - 数量统计功能正确
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class RogueSpawnerTest {
    public static final String BATCH_ROGUE_SPAWNER = "rogue_spawner_test";

    /**
     * 测试范围搜索半径（方块）。
     */
    private static final int TEST_SEARCH_RADIUS = 6;

    /**
     * 测试生成偏移（方块）。
     */
    private static final int TEST_SPAWN_OFFSET = 2;
    // Center positions for test isolation (relative to origin)
    private static final int CENTER_A_X = 0;
    private static final int CENTER_B_X = 12;
    private static final int CENTER_C_X = -12;

    /**
     * 测试生成数量。
     */
    private static final int TEST_SPAWN_COUNT = 3;

    /**
     * 测试最大散修数量。
     */
    private static final int TEST_MAX_COUNT = 5;

    /**
     * 测试散修生成。
     * <p>
     * 验证：调用 spawnRogue 后，实体存在于世界中。
     * </p>
     */
    @GameTest(template = "empty", batch = RogueSpawnerTest.BATCH_ROGUE_SPAWNER)
    public static void testSpawnRogue(GameTestHelper helper) {
        // 准备：获取生成中心位置，确保与其他测试隔离
        BlockPos center = helper.absolutePos(new BlockPos(CENTER_A_X, 0, 0));
        // 生成位置在中心正上方
        BlockPos spawnPos = center.above();

        // 执行：生成散修
        RogueEntity rogue = RogueSpawner.spawnRogue(helper.getLevel(), spawnPos);

        // 验证：实体存在且位置正确
        helper.assertTrue(rogue != null, "散修实体应该被创建");
        helper.assertTrue(
            helper.getLevel().getEntitiesOfClass(RogueEntity.class, rogue.getBoundingBox()).size() > 0,
            "散修实体应该存在于世界中"
        );

        helper.succeed();
    }

    /**
     * 测试散修数量统计。
     * <p>
     * 验证：countRoguesNear 正确统计范围内的散修数量。
     * </p>
     */
    @GameTest(template = "empty", batch = RogueSpawnerTest.BATCH_ROGUE_SPAWNER)
    public static void testCountRoguesNear(GameTestHelper helper) {
        // 准备：生成中心位置，确保与其他测试隔离
        BlockPos center = helper.absolutePos(new BlockPos(CENTER_B_X, 0, 0));

        // 执行：生成 3 个散修
        RogueSpawner.spawnRogue(helper.getLevel(), center);
        RogueSpawner.spawnRogue(helper.getLevel(), center.offset(TEST_SPAWN_OFFSET, 0, 0));
        RogueSpawner.spawnRogue(helper.getLevel(), center.offset(0, 0, TEST_SPAWN_OFFSET));

        // 验证：统计数量应为 3
        int count = RogueSpawner.countRoguesNear(helper.getLevel(), center, TEST_SEARCH_RADIUS);
        helper.assertTrue(count == TEST_SPAWN_COUNT, "应该统计到 3 个散修，实际：" + count);

        helper.succeed();
    }

    /**
     * 测试秒级检查逻辑。
     * <p>
     * 验证：trySpawn 仅在秒级检查时间点执行生成。
     * </p>
     */
    @GameTest(template = "empty", batch = RogueSpawnerTest.BATCH_ROGUE_SPAWNER)
    public static void testTrySpawnSecondLevelCheck(GameTestHelper helper) {
        // 准备：生成位置和参数
        BlockPos center = helper.absolutePos(new BlockPos(CENTER_C_X, 0, 0));

        // 先生成 up to MAX_COUNT 的散修，确保测试有数据
        for (int i = 0; i < TEST_MAX_COUNT; i++) {
            RogueSpawner.spawnRogue(helper.getLevel(), center.offset(i, 0, 0));
        }

        int count = RogueSpawner.countRoguesNear(helper.getLevel(), center, TEST_SEARCH_RADIUS);
        helper.assertTrue(count == TEST_MAX_COUNT, "应统计到最大数量散修，实际: " + count);

        // 执行：秒级检查点触发 trySpawn，理论上不会增加数量
        RogueSpawner.trySpawn(helper.getLevel(), center, TEST_MAX_COUNT, TEST_SEARCH_RADIUS);

        int countAfter = RogueSpawner.countRoguesNear(helper.getLevel(), center, TEST_SEARCH_RADIUS);
        helper.assertTrue(countAfter == TEST_MAX_COUNT, "上限约束未生效，当前: " + countAfter);

        helper.succeed();
    }
}
