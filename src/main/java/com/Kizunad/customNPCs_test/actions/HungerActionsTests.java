package com.Kizunad.customNPCs_test.actions;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.goals.SatiateGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestBatches;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 饥饿/进食相关行为测试。
 */
@GameTestHolder("guzhenrenext")
public class HungerActionsTests {

    @GameTest(
        templateNamespace = "guzhenren",
        template = "empty3x3x3",
        batch = TestBatches.GOAP,
        timeoutTicks = 200
    )
    public static void testSatiateUsesLowerValueFood(GameTestHelper helper) {
        Zombie npc = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(2, 2, 2),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        mind.getGoalSelector().registerGoal(new SatiateGoal());
        mind.getStatus().setHungerForTest(6);
        mind.getInventory().setItem(0, new ItemStack(Items.GOLDEN_APPLE));
        mind.getInventory().setItem(1, new ItemStack(Items.BREAD));

        NpcTestHelper.tickMind(helper, npc);

        NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getStatus().getHunger() > 6 && !mind.getInventory().getItem(1).is(Items.BREAD),
            120,
            "NPC 应该优先食用低价值食物补充饥饿"
        );
    }

    @GameTest(
        templateNamespace = "guzhenren",
        template = "empty3x3x3",
        batch = TestBatches.GOAP,
        timeoutTicks = 160
    )
    public static void testSatiateSkipsWhenInDanger(GameTestHelper helper) {
        Zombie npc = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(2, 2, 2),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        mind.getGoalSelector().registerGoal(new SatiateGoal());
        mind.getStatus().setHungerForTest(6);
        mind.getInventory().setItem(0, new ItemStack(Items.BREAD));
        mind.getMemory().rememberShortTerm(WorldStateKeys.HAZARD_NEARBY, true, 200);

        NpcTestHelper.tickMind(helper, npc);

        final int initialHunger = mind.getStatus().getHunger();
        final int[] ticks = {0};
        helper.onEachTick(() -> {
            ticks[0]++;
            if (mind.getStatus().getHunger() != initialHunger) {
                helper.fail("危险状态下不应进食");
            }
            if (ticks[0] >= 80) {
                helper.succeed();
            }
        });
    }
}
