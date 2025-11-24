package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.decision.goals.TestPlanGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * MoveToAction 完整测试集
 * 覆盖各种正常、失败、边界和集成场景
 */
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
public class MoveToActionExtendedTests {

    // ==================== 基础功能测试 ====================
    
    // ==================== 基础功能测试 ====================
    
    /**
     * 测试近距离移动（< 5 格）
     */
    public static void testMoveToCoordinate_NearDistance(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 startPos = zombie.position();
        Vec3 targetPos = startPos.add(3, 0, 0); // 3格远
        
        List<IAction> actions = List.of(new MoveToAction(targetPos, 1.0));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                if (!mind.getActionExecutor().isIdle()) return false;
                double distanceMoved = zombie.position().distanceTo(startPos);
                return distanceMoved > 1.0;
            },
            100,
            "移动应该完成且有位移"
        );
    }

    /**
     * 测试自定义接受距离参数
     */
    public static void testMoveToAction_CustomAcceptableDistance(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 startPos = zombie.position();
        Vec3 targetPos = startPos.add(10, 0, 0);
        
        // 接受距离设为 8.0，所以不需要完全到达 10 格外
        List<IAction> actions = List.of(new MoveToAction(targetPos, 1.0, 8.0));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                if (!mind.getActionExecutor().isIdle()) return false;
                double distanceMoved = zombie.position().distanceTo(startPos);
                return distanceMoved > 0.5;
            },
            100,
            "移动应该完成且有位移"
        );
    }

    /**
     * 测试已在接受范围内的情况
     */
    public static void testMoveToAction_AlreadyInRange(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 currentPos = zombie.position();
        Vec3 nearbyPos = currentPos.add(0.5, 0, 0); // 0.5 格远
        
        // 接受距离 2.0，所以已经在范围内
        List<IAction> actions = List.of(new MoveToAction(nearbyPos, 1.0, 2.0));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 应该几乎立即完成
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            10,
            "应该立即完成"
        );
    }

    // ==================== 失败场景测试 ====================
    
    /**
     * 测试无效坐标 - NaN
     */
    public static void testMoveToAction_InvalidCoordinates_NaN(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 invalidPos = new Vec3(Double.NaN, 0, 0);
        
        List<IAction> actions = List.of(new MoveToAction(invalidPos, 1.0));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 应该很快失败
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            20,
            "无效坐标应该导致失败"
        );
    }

    /**
     * 测试无效坐标 - 极值Y坐标
     */
    public static void testMoveToAction_InvalidCoordinates_ExtremeY(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 startPos = zombie.position();
        Vec3 extremeYPos = new Vec3(startPos.x, 1000, startPos.z); // Y=1000 超出范围
        
        List<IAction> actions = List.of(new MoveToAction(extremeYPos, 1.0));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            20,
            "极值Y坐标应该导致失败"
        );
    }

    /**
     * 测试目标实体消失
     */
    public static void testMoveToAction_TargetEntityDespawned(GameTestHelper helper) {
        Zombie mover = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        Zombie target = com.Kizunad.customNPCs_test.utils.NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            new net.minecraft.core.BlockPos(8, 2, 2));
        
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, mover);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, mover);
        
        List<IAction> actions = List.of(new MoveToAction(target, 1.0));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 等待几个 tick 后移除目标实体
        helper.runAtTickTime(10, () -> {
            target.discard(); // 移除目标实体
        });
        
        // 移除后应该失败
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            40,
            "目标消失应该导致失败或完成"
        );
    }

    /**
     * 测试超时机制
     */
    public static void testMoveToAction_Timeout(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 farPos = zombie.position().add(100, 0, 0); // 很远的位置
        
        // 设置很短的超时时间（20 ticks）
        List<IAction> actions = List.of(new MoveToAction(farPos, 1.0, 2.0, 20));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 等待超时
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            40,
            "应该因超时而失败"
        );
    }

    // ==================== 边界条件测试 ====================
    
    /**
     * 测试当前位置即目标（0距离）
     */
    public static void testMoveToAction_SamePosition(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 currentPos = zombie.position();
        
        List<IAction> actions = List.of(new MoveToAction(currentPos, 1.0));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        // 应该立即成功
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            10,
            "同位置应该立即完成"
        );
    }

    /**
     * 测试极近距离（< 0.5 格）
     */
    public static void testMoveToAction_ExtremelyClose(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 currentPos = zombie.position();
        Vec3 veryClose = currentPos.add(0.3, 0, 0); // 0.3 格
        
        List<IAction> actions = List.of(new MoveToAction(veryClose, 1.0, 0.5));
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> mind.getActionExecutor().isIdle(),
            20,
            "极近距离应该快速完成"
        );
    }

    // ==================== 集成测试 ====================
    
    /**
     * 测试连续多次移动
     */
    public static void testMoveToAction_SequentialMoves(GameTestHelper helper) {
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        Vec3 start = zombie.position();
        Vec3 pos1 = start.add(3, 0, 0);
        Vec3 pos2 = pos1.add(0, 0, 3);
        
        // 两个连续移动动作
        List<IAction> actions = List.of(
            new MoveToAction(pos1, 1.0),
            new MoveToAction(pos2, 1.0)
        );
        TestPlanGoal testGoal = new TestPlanGoal(0.9f, actions);
        mind.getGoalSelector().registerGoal(testGoal);
        
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                if (!mind.getActionExecutor().isIdle()) return false;
                double totalDistance = zombie.position().distanceTo(start);
                return totalDistance > 2.0;
            },
            100,
            "连续移动应该完成且有位移"
        );
    }
}
