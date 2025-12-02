package com.Kizunad.customNPCs_test.goals;

import com.Kizunad.customNPCs.ai.decision.goals.CookGoal;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
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
 * CookGoal 行为测试：验证背包有可烹饪食材+燃料时会产出熟食。
 */
@GameTestHolder("guzhenrenext")
public class CookGoalTests {

    @GameTest(
        templateNamespace = "guzhenren",
        template = "empty3x3x3",
        batch = TestBatches.GOAP,
        timeoutTicks = 260
    )
    public static void testCookGoalProducesCookedFood(GameTestHelper helper) {
        Zombie npc = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(1, 1, 1),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        mind.getGoalSelector().registerGoal(new CookGoal());

        NpcInventory inventory = mind.getInventory();
        inventory.setItem(0, new ItemStack(Items.BEEF));
        inventory.setItem(1, new ItemStack(Items.COAL));

        if (
            com.Kizunad.customNPCs.ai.actions.common.FurnaceAction
                .findCookCandidate(inventory, helper.getLevel())
                .isEmpty()
        ) {
            helper.fail("准备阶段未找到可烹饪的食材+燃料");
            return;
        }

        NpcTestHelper.tickMind(helper, npc);

        NpcTestHelper.waitForCondition(
            helper,
            () ->
                inventory.anyMatch(stack -> stack.is(Items.COOKED_BEEF)) &&
                    !inventory.anyMatch(stack -> stack.is(Items.BEEF)),
            240,
            "CookGoal 应该在有燃料和食材时产出熟食"
        );
    }
}
