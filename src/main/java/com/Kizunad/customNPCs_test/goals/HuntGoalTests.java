package com.Kizunad.customNPCs_test.goals;

import com.Kizunad.customNPCs.ai.decision.goals.HuntGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestBatches;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * HuntGoal 行为测试：验证安全状态下会主动攻击较弱的实体。
 */
@GameTestHolder("guzhenrenext")
public class HuntGoalTests {

    @GameTest(
        templateNamespace = "guzhenren",
        template = "empty3x3x3",
        batch = TestBatches.GOAP,
        timeoutTicks = 240
    )
    public static void testHuntGoalAttacksWeakerEntities(GameTestHelper helper) {
        Zombie npc = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(1, 1, 1),
            EntityType.ZOMBIE,
            true
        );
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(1, 1, 2));

        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        mind.getGoalSelector().registerGoal(new HuntGoal());

        NpcTestHelper.tickMind(helper, npc);

        NpcTestHelper.waitForCondition(
            helper,
            () -> pig.isDeadOrDying() || pig.getHealth() < pig.getMaxHealth(),
            200,
            "HuntGoal 应该主动猎杀更弱的生物"
        );
    }
}
