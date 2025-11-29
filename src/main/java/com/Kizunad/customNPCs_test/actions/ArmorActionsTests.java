package com.Kizunad.customNPCs_test.actions;

import com.Kizunad.customNPCs.ai.decision.goals.EquipArmorGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestBatches;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 装备盔甲相关动作测试。
 */
@GameTestHolder("guzhenrenext")
public class ArmorActionsTests {

    @GameTest(
        templateNamespace = "guzhenren",
        template = "empty3x3x3",
        batch = TestBatches.GOAP,
        timeoutTicks = 120
    )
    public static void testEquipArmorChoosesBest(GameTestHelper helper) {
        Zombie npc = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(2, 2, 2),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, npc);

        // 背包放入两件不同品质的胸甲
        mind.getInventory().setItem(0, new ItemStack(Items.LEATHER_CHESTPLATE));
        mind.getInventory().setItem(1, new ItemStack(Items.IRON_CHESTPLATE));

        mind.getGoalSelector().registerGoal(new EquipArmorGoal(0.9f));

        NpcTestHelper.tickMind(helper, npc);

        NpcTestHelper.waitForCondition(
            helper,
            () -> npc
                .getItemBySlot(EquipmentSlot.CHEST)
                .is(Items.IRON_CHESTPLATE),
            80,
            "NPC 应该装备更高护甲值的胸甲"
        );
    }
}
