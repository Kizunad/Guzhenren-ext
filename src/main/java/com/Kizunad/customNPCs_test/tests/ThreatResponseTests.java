package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.decision.goals.DefendGoal;
import com.Kizunad.customNPCs.ai.decision.goals.FleeGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 威胁响应收尾阶段的 GameTest：验证 Goal 结束时的清理与冷却。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public final class ThreatResponseTests {

    private ThreatResponseTests() {}

    /**
     * 防御结束后应清理威胁记忆并写入短冷却，避免反复进入防御状态。
     */
    public static void testDefendGoalClearsThreatMemory(GameTestHelper helper) {
        var defender = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.VILLAGER,
            true
        );
        INpcMind mind = NpcTestHelper.getMind(helper, defender);

        mind
            .getMemory()
            .rememberShortTerm("current_threat_id", UUID.randomUUID(), 40);
        mind.getMemory().rememberShortTerm("threat_detected", true, 40);

        DefendGoal goal = new DefendGoal();
        goal.start(mind, defender);
        goal.stop(mind, defender);

        helper.succeedWhen(() -> {
            helper.assertTrue(
                !mind.getMemory().hasMemory("threat_detected"),
                "threat_detected should be cleared after defense ends"
            );
            helper.assertTrue(
                !mind.getMemory().hasMemory("current_threat_id"),
                "current_threat_id should be cleared after defense ends"
            );
            helper.assertTrue(
                mind.getMemory().hasMemory("defend_cooldown"),
                "defend_cooldown should be set to prevent oscillation"
            );
        });
    }

    /**
     * 撤退结束后应清理威胁记忆并写入短冷却，避免重复逃跑。
     */
    public static void testFleeGoalClearsThreatMemory(GameTestHelper helper) {
        var runner = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.VILLAGER,
            false
        );
        INpcMind mind = NpcTestHelper.getMind(helper, runner);

        // 进入危险状态：低血量 + 威胁记忆
        runner.setHealth(runner.getMaxHealth() * 0.2f);
        mind.getMemory().rememberShortTerm("threat_detected", true, 40);
        mind
            .getMemory()
            .rememberShortTerm("current_threat_id", UUID.randomUUID(), 40);

        FleeGoal goal = new FleeGoal();
        goal.start(mind, runner);

        // 模拟脱战：恢复血量，使 isInDanger 返回 false，触发清理逻辑
        runner.setHealth(runner.getMaxHealth());
        runner.hurtTime = 0;
        goal.stop(mind, runner);

        helper.succeedWhen(() -> {
            helper.assertTrue(
                !mind.getMemory().hasMemory("threat_detected"),
                "threat_detected should be cleared after fleeing ends"
            );
            helper.assertTrue(
                !mind.getMemory().hasMemory("current_threat_id"),
                "current_threat_id should be cleared after fleeing ends"
            );
            helper.assertTrue(
                mind.getMemory().hasMemory("flee_cooldown"),
                "flee_cooldown should be set to prevent oscillation"
            );
        });
    }

    /**
     * 近距离且持盾时，应优先进入格挡模式（使用盾牌）。
     */
    public static void testDefendGoalPrefersBlockClose(GameTestHelper helper) {
        var defender = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.VILLAGER,
            false
        );
        defender.setHealth(defender.getMaxHealth());
        defender.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));

        var attacker = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 1));
        INpcMind mind = NpcTestHelper.getMind(helper, defender);
        mind
            .getMemory()
            .rememberShortTerm("current_threat_id", attacker.getUUID(), 40);

        DefendGoal goal = new DefendGoal();
        goal.start(mind, defender);
        helper.onEachTick(() -> goal.tick(mind, defender));

        helper.runAtTickTime(
            20,
            () -> helper.assertTrue(
                defender.isUsingItem() && defender.getUseItem().is(Items.SHIELD),
                "DefendGoal should raise shield at close range"
            )
        );
    }

    /**
     * 中距离且有远程武器时，应进入远程攻击模式（拉弓/弩）。
     */
    public static void testDefendGoalPrefersRanged(GameTestHelper helper) {
        var defender = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.SKELETON,
            false
        );
        defender.setHealth(defender.getMaxHealth());
        // Skeleton 默认可用远程攻击，补充弓和箭以防重置
        defender.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        defender.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        var attacker = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 1));
        INpcMind mind = NpcTestHelper.getMind(helper, defender);
        mind
            .getMemory()
            .rememberShortTerm("current_threat_id", attacker.getUUID(), 40);

        DefendGoal goal = new DefendGoal();
        goal.start(mind, defender);
        helper.onEachTick(() -> goal.tick(mind, defender));

        helper.runAtTickTime(
            40,
            () -> helper.assertTrue(
                defender.isUsingItem() && defender.getUseItem().is(Items.BOW),
                "DefendGoal should use bow at mid range"
            )
        );
    }
}
