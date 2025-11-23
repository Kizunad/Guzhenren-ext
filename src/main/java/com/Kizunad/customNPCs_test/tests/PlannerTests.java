package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.planner.GoapPlanner;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs_test.goap.ChopWoodGoapAction;
import com.Kizunad.customNPCs_test.goap.CraftPlanksGoapAction;
import com.Kizunad.customNPCs_test.goap.GetAppleGoapAction;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Arrays;
import java.util.List;

/**
 * GOAP 规划器测试逻辑
 */
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
public class PlannerTests {
    
    /**
     * 测试 WorldState 匹配逻辑
     */
    public static void testWorldStateMatch(GameTestHelper helper) {
        // 创建当前状态
        WorldState current = new WorldState();
        current.setState("has_apple", true);
        current.setState("has_wood", true);
        
        // 测试：满足的目标
        WorldState goal1 = new WorldState();
        goal1.setState("has_apple", true);
        helper.assertTrue(current.matches(goal1), 
            "当前状态应该满足 {has_apple: true}");
        
        // 测试：不满足的目标
        WorldState goal2 = new WorldState();
        goal2.setState("has_apple", true);
        goal2.setState("has_planks", true);
        helper.assertFalse(current.matches(goal2), 
            "当前状态不应该满足 {has_apple: true, has_planks: true}");
        
        // 测试：空目标（应该总是满足）
        WorldState emptyGoal = new WorldState();
        helper.assertTrue(current.matches(emptyGoal), 
            "空目标应该总是被满足");
        
        helper.succeed();
    }
    
    /**
     * 测试 WorldState 应用效果逻辑
     */
    public static void testWorldStateApply(GameTestHelper helper) {
        // 初始状态
        WorldState initial = new WorldState();
        initial.setState("has_wood", false);
        initial.setState("has_apple", true);
        
        // 效果
        WorldState effects = new WorldState();
        effects.setState("has_wood", true);
        
        // 应用效果
        WorldState newState = initial.apply(effects);
        
        // 验证新状态
        helper.assertTrue((Boolean) newState.getState("has_wood"), 
            "has_wood 应该为 true");
        helper.assertTrue((Boolean) newState.getState("has_apple"), 
            "has_apple 应该保持为 true");
        
        // 验证原状态未被修改
        helper.assertFalse((Boolean) initial.getState("has_wood"), 
            "原状态不应被修改");
        
        helper.succeed();
    }
    
    /**
     * 测试简单规划（单个动作）
     */
    public static void testSimplePlan(GameTestHelper helper) {
        GoapPlanner planner = new GoapPlanner();
        
        // 初始状态：没有苹果
        WorldState initial = new WorldState();
        initial.setState("has_apple", false);
        
        // 目标状态：有苹果
        WorldState goal = new WorldState();
        goal.setState("has_apple", true);
        
        // 可用动作
        List<IGoapAction> actions = Arrays.asList(new GetAppleGoapAction());
        
        // 规划
        List<IAction> plan = planner.plan(initial, goal, actions);
        
        // 验证
        helper.assertValueEqual(plan != null, true, "应该能生成计划");
        helper.assertValueEqual(plan.size(), 1, "计划应该包含 1 个动作");
        helper.assertValueEqual(plan.get(0).getName(), "get_apple", "动作应该是 get_apple");
        
        helper.succeed();
    }
    
    /**
     * 测试链式规划（多个动作）
     */
    public static void testChainedPlan(GameTestHelper helper) {
        GoapPlanner planner = new GoapPlanner();
        
        // 初始状态：没有木头和木板
        WorldState initial = new WorldState();
        initial.setState("has_wood", false);
        initial.setState("has_planks", false);
        
        // 目标状态：有木板
        WorldState goal = new WorldState();
        goal.setState("has_planks", true);
        
        // 可用动作
        List<IGoapAction> actions = Arrays.asList(
            new ChopWoodGoapAction(),
            new CraftPlanksGoapAction()
        );
        
        // 规划
        List<IAction> plan = planner.plan(initial, goal, actions);
        
        // 验证
        helper.assertValueEqual(plan != null, true, "应该能生成计划");
        helper.assertValueEqual(plan.size(), 2, "计划应该包含 2 个动作");
        helper.assertValueEqual(plan.get(0).getName(), "chop_wood", 
            "第一个动作应该是 chop_wood");
        helper.assertValueEqual(plan.get(1).getName(), "craft_planks", 
            "第二个动作应该是 craft_planks");
        
        helper.succeed();
    }
    
    /**
     * 测试无解规划
     */
    public static void testImpossiblePlan(GameTestHelper helper) {
        GoapPlanner planner = new GoapPlanner();
        
        // 初始状态：没有木头
        WorldState initial = new WorldState();
        initial.setState("has_wood", false);
        
        // 目标状态：有木板
        WorldState goal = new WorldState();
        goal.setState("has_planks", true);
        
        // 可用动作：只有 CraftPlanks（缺少 ChopWood）
        List<IGoapAction> actions = Arrays.asList(new CraftPlanksGoapAction());
        
        // 规划
        List<IAction> plan = planner.plan(initial, goal, actions);
        
        // 验证
        helper.assertTrue(plan == null || plan.isEmpty(), 
            "无法满足前置条件时，应该返回 null 或空列表");
        
        helper.succeed();
    }
    
    /**
     * GOAP 集成测试 - 完整流程
     * <p>
     * 测试场景：NPC 想要获得木板
     * 预期规划：[MoveToTree, BreakBlock, CollectItem, CraftPlanks]
     * 验证：整个动作链按顺序执行，最终状态正确
     */
    public static void testGoapIntegration(GameTestHelper helper) {
        // 在测试区域中放置一棵树
        net.minecraft.core.BlockPos treePos = new net.minecraft.core.BlockPos(5, 2, 5);
        helper.setBlock(treePos, net.minecraft.world.level.block.Blocks.OAK_LOG);
        
        // 生成一个僵尸
        net.minecraft.world.entity.monster.Zombie zombie = helper.spawn(
            net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 2
        );
        
        // 获取 NpcMind
        com.Kizunad.customNPCs.capabilities.mind.INpcMind mind = 
            zombie.getData(com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND);
        
        // 创建收集木板目标（真实版本）
        com.Kizunad.customNPCs_test.goap.GatherPlanksRealGoal gatherGoal = 
            new com.Kizunad.customNPCs_test.goap.GatherPlanksRealGoal(0.9f, treePos);
        
        // 注册目标
        mind.getGoalSelector().registerGoal(gatherGoal);
        
        System.out.println("[GOAP Integration Test] 测试开始");
        System.out.println("  目标: 获得木板");
        System.out.println("  预期规划: [MoveToTree, BreakBlock, CollectItem, CraftPlanks]");
        
        // 等待目标完成（最多 200 ticks）
        helper.succeedWhen(() -> {
            // 检查目标是否完成
            helper.assertTrue(gatherGoal.isFinished(mind, zombie), 
                "目标应该已完成");
            
            // 检查最终状态
            Object hasPlanks = mind.getMemory().getMemory("has_planks");
            helper.assertTrue(hasPlanks != null && (Boolean) hasPlanks, 
                "最终应该拥有木板");
            
            // 验证中间状态也被正确设置
            helper.assertTrue(mind.getMemory().hasMemory("at_tree_location"), 
                "应该到达过树木位置");
            helper.assertTrue(mind.getMemory().hasMemory("tree_broken"), 
                "应该破坏过树木");
            helper.assertTrue(mind.getMemory().hasMemory("has_wood"), 
                "应该收集过木头");
            
            System.out.println("[GOAP Integration Test] 测试成功！");
            System.out.println("  ✓ 完整动作链执行成功");
            System.out.println("  ✓ 所有中间状态正确");
            System.out.println("  ✓ 最终目标达成");
        });
    }
    
    /**
     * 真实 API GOAP 集成测试 - 使用真实的 Minecraft API
     * <p>
     * 测试场景：Zombie 使用真实 API 获得木板
     * 预期规划：[RealMoveToTree, RealBreakBlock, RealCollectItem, CraftPlanks]
     * 验证：真实的移动、破坏、收集和制作流程
     */
    public static void testRealApiGoapIntegration(GameTestHelper helper) {
        // 在测试区域中放置一棵树（橡木原木）
        // 注意：使用相对坐标，然后转换为绝对坐标
        net.minecraft.core.BlockPos relativeTreePos = new net.minecraft.core.BlockPos(5, 2, 5);
        net.minecraft.core.BlockPos absoluteTreePos = helper.absolutePos(relativeTreePos);
        
        helper.setBlock(relativeTreePos, net.minecraft.world.level.block.Blocks.OAK_LOG);
        
        // 生成一个僵尸在起始位置
        net.minecraft.world.entity.monster.Zombie zombie = helper.spawn(
            net.minecraft.world.entity.EntityType.ZOMBIE, 2, 2, 2
        );
        
        // 获取 NpcMind
        com.Kizunad.customNPCs.capabilities.mind.INpcMind mind = 
            zombie.getData(com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment.NPC_MIND);
        
        // 创建真实 API 收集木板目标 - 使用绝对坐标！
        com.Kizunad.customNPCs_test.goap.real.RealGatherPlanksGoal realGoal = 
            new com.Kizunad.customNPCs_test.goap.real.RealGatherPlanksGoal(0.9f, absoluteTreePos);
        
        // 注册目标
        mind.getGoalSelector().registerGoal(realGoal);
        
        System.out.println("[Real API GOAP Test] 测试开始");
        System.out.println("  Zombie位置: " + zombie.position());
        System.out.println("  树木相对位置: " + relativeTreePos);
        System.out.println("  树木绝对位置: " + absoluteTreePos);
        System.out.println("  距离: " + zombie.position().distanceTo(
            net.minecraft.world.phys.Vec3.atCenterOf(absoluteTreePos)));
        System.out.println("  Zombie将会:");
        System.out.println("    1. 使用 PathNavigation 移动到树木");
        System.out.println("    2. 使用 level.destroyBlock() 破坏方块");
        System.out.println("    3. 搜索并收集 ItemEntity");
        System.out.println("    4. 制作木板");
        
        // 等待目标完成（最多 300 ticks，因为真实移动可能需要更长时间）
        helper.succeedWhen(() -> {
            // 检查目标是否完成
            helper.assertTrue(realGoal.isFinished(mind, zombie), 
                "目标应该已完成");
            
            // 检查最终状态
            Object hasPlanks = mind.getMemory().getMemory("has_planks");
            helper.assertTrue(hasPlanks != null && (Boolean) hasPlanks, 
                "最终应该拥有木板");
            
            // 验证树木已被破坏
            net.minecraft.world.level.block.state.BlockState blockState = 
                helper.getBlockState(relativeTreePos);
            helper.assertTrue(blockState.isAir(), 
                "树木方块应该已被破坏");
            
            // 验证中间状态
            helper.assertTrue(mind.getMemory().hasMemory("at_tree_location"), 
                "应该到达过树木位置");
            helper.assertTrue(mind.getMemory().hasMemory("tree_broken"), 
                "应该破坏过树木");
            helper.assertTrue(mind.getMemory().hasMemory("has_wood"), 
                "应该收集过木头");
            
            System.out.println("[Real API GOAP Test] 测试成功！");
            System.out.println("  ✓ Zombie 真实移动到树木位置");
            System.out.println("  ✓ 真实破坏了方块");
            System.out.println("  ✓ 真实收集了掉落物品");
            System.out.println("  ✓ 完成了制作");
        });
    }
}
