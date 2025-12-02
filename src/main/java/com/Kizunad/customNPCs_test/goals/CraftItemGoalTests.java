package com.Kizunad.customNPCs_test.goals;

import com.Kizunad.customNPCs.ai.actions.base.WaitAction;
import com.Kizunad.customNPCs.ai.decision.goals.CraftItemGoal;
import com.Kizunad.customNPCs.ai.decision.goals.IdleGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestBatches;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * CraftItemGoal 行为测试：验证不会被 Utility 自动选中，但可手动触发并执行计划。
 */
@GameTestHolder("guzhenrenext")
public class CraftItemGoalTests {

    @GameTest(
        templateNamespace = "guzhenren",
        template = "empty3x3x3",
        batch = TestBatches.GOAP,
        timeoutTicks = 220
    )
    public static void testCraftItemGoalManualPlan(GameTestHelper helper) {
        Zombie npc = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(1, 1, 1),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, npc);

        IdleGoal idleGoal = new IdleGoal();
        CraftItemGoal craftGoal = new CraftItemGoal();
        mind.getGoalSelector().registerGoal(idleGoal);
        mind.getGoalSelector().registerGoal(craftGoal);

        NpcTestHelper.tickMind(helper, npc);

        final int[] ticks = {0};
        final int[] stage = {0};

        helper.onEachTick(() -> {
            ticks[0]++;
            var current = mind.getGoalSelector().getCurrentGoal();

            if (stage[0] == 0) {
                // 只要目标被选中且不是 CraftItemGoal，即视为“未被自动选择”
                if (current instanceof CraftItemGoal) {
                    helper.fail("CraftItemGoal 不应在未手动触发时被自动选中");
                    return;
                }
                if (current instanceof IdleGoal) {
                    // 进入手动触发阶段
                    craftGoal.assignPlan(
                        java.util.List.of(new WaitAction(10)),
                        "manual_plan"
                    );
                    mind.getGoalSelector().forceSwitchTo(mind, npc, craftGoal);
                    stage[0] = 1;
                }
            } else if (stage[0] == 1) {
                boolean executorIdle = mind.getActionExecutor().isIdle();
                boolean backToIdle =
                    mind.getGoalSelector().getCurrentGoal() instanceof IdleGoal;
                boolean planCleared = craftGoal.getAssignedPlan().isEmpty();

                if (executorIdle && backToIdle && planCleared) {
                    helper.succeed();
                    return;
                }
            }

            if (ticks[0] > 200) {
                helper.fail("CraftItemGoal 手动执行流程超时");
            }
        });
    }
}
