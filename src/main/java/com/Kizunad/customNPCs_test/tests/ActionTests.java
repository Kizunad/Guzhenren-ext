package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.actions.base.WaitAction;
import com.Kizunad.customNPCs.ai.decision.goals.TestPlanGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 动作系统测试逻辑
 */
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
public class ActionTests {

    /**
     * 测试动作队列按顺序执行
     */
    public static void testActionQueue(GameTestHelper helper) {
        // 使用工厂创建僵尸
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        
        // 获取 NpcMind
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 创建测试动作序列：等待20 ticks → 等待10 ticks
        List<IAction> actions = List.of(
            new WaitAction(20),
            new WaitAction(10)
        );
        
        // 创建高优先级的测试目标
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 等待所有动作完成
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            40, // 20 + 10 + buffer
            "所有动作应该已完成，执行器应该空闲"
        );
    }

    /**
     * 测试执行器空闲状态
     */
    public static void testActionExecutorIdle(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 初始状态应该是空闲
        helper.assertTrue(mind.getActionExecutor().isIdle(), "初始状态应该空闲");
        
        // 提交一个动作
        List<IAction> actions = List.of(new WaitAction(10));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 等待动作完成后执行器变回空闲
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            20,
            "动作完成后应该空闲"
        );
    }

    /**
     * 测试单个 WaitAction 的正确性
     */
    public static void testWaitAction(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 提交一个等待 40 ticks 的动作
        List<IAction> actions = List.of(new WaitAction(40));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 等待动作完成
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            50,
            "40 ticks 后动作应该完成"
        );
    }

    /**
     * 测试 MoveToAction - 移动到指定坐标
     */
    public static void testMoveToAction(GameTestHelper helper) {
        // 生成一个僵尸在起始位置
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 记录起始位置
        Vec3 startPos = zombie.position();
        
        // 设置目标位置（向前移动 5 格）
        Vec3 targetPos = startPos.add(5, 0, 0);
        
        // 创建移动动作
        List<IAction> actions = List.of(
            new MoveToAction(targetPos, 1.0)
        );
        
        // 提交高优先级测试目标
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 等待移动完成（执行器空闲表示完成或失败）
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                // 动作完成（成功或失败都会变成空闲）
                if (!mind.getActionExecutor().isIdle()) {
                    return false;
                }
                
                // 检查是否已移动（如果动作成功的话应该移动了）
                Vec3 currentPos = zombie.position();
                double distanceMoved = currentPos.distanceTo(startPos);
                return distanceMoved > 0.5;
            },
            100,
            "移动动作应该已完成且NPC应该已移动"
        );
    }

    /**
     * 测试 MoveToAction - 移动到实体
     */
    public static void testMoveToEntity(GameTestHelper helper) {
        // 生成观察者和目标
        Zombie mover = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        Zombie target = com.Kizunad.customNPCs_test.utils.NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            new net.minecraft.core.BlockPos(8, 2, 2));
        
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, mover);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, mover);
        
        Vec3 startPos = mover.position();
        
        // 创建移动到实体的动作
        List<IAction> actions = List.of(
            new MoveToAction(target, 1.0, 3.0) // 接受距离 3 格
        );
        
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 等待移动完成
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                // 动作完成
                if (!mind.getActionExecutor().isIdle()) {
                    return false;
                }
                
                // 检查是否移动了
                Vec3 currentPos = mover.position();
                double distanceMoved = currentPos.distanceTo(startPos);
                return distanceMoved > 0.5;
            },
            100,
            "移动动作应该已完成且NPC应该已移动"
        );
    }
}
