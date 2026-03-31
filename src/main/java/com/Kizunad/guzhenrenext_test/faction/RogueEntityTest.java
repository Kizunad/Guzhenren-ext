package com.Kizunad.guzhenrenext_test.faction;

import com.Kizunad.guzhenrenext.entity.ModEntities;
import com.Kizunad.guzhenrenext.entity.RogueEntity;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 散修实体基础能力测试。
 */
@GameTestHolder("guzhenrenext")
public class RogueEntityTest {

    private static final String TEST_BATCH = "rogue_entity_spawn";

    private static final int TEST_TIMEOUT_TICKS = 80;

    private static final double SPAWN_X = 4.0D;

    private static final double SPAWN_Y = 2.0D;

    private static final double SPAWN_Z = 4.0D;

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TEST_BATCH)
    public void testRogueEntitySpawn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RogueEntity rogue = ModEntities.ROGUE.get().create(level);
        helper.assertTrue(rogue != null, "散修实体创建失败");

        rogue.setPos(SPAWN_X, SPAWN_Y, SPAWN_Z);
        boolean spawnResult = level.addFreshEntity(rogue);
        helper.assertTrue(spawnResult, "散修实体加入世界失败");
        helper.assertTrue(rogue.isAlive(), "散修实体生成后应处于存活状态");

        UUID factionId = UUID.randomUUID();
        rogue.setFactionId(factionId);
        helper.assertTrue(factionId.equals(rogue.getFactionId()), "势力 UUID 设置后应可读回");

        rogue.tick();
        helper.assertTrue(rogue.isAlive(), "散修实体 tick 后应保持存活状态");
        helper.assertTrue(factionId.equals(rogue.getFactionId()), "散修实体 tick 后应保持势力 UUID");

        helper.succeed();
    }
}
