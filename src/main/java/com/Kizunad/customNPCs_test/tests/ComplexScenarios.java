package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.planner.GoapPlanner;

import com.Kizunad.customNPCs_test.goap.CourierGoal;
import com.Kizunad.customNPCs_test.utils.TestBatches;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.List;

/**
 * 复杂场景集成测试
 * <p>
 * 验证感知、规划和执行系统的协同工作能力
 */
@GameTestHolder("guzhenrenext_disabled")
public class ComplexScenarios {
    
    /**
     * 场景 A: "搜寻者" (The Gatherer)
     * <p>
     * NPC 需要找到并拾取一个掉落的物品
     */
//    @GameTest(template = "empty", timeoutTicks = 300, batch = TestBatches.COMPLEX)
//    public static void testTheGatherer(GameTestHelper helper) {
//        // 0. 构建地板
//        buildFloor(helper);
//
//        // 1. 环境设置
//        BlockPos spawnPos = new BlockPos(2, 2, 2);
//        BlockPos itemPos = new BlockPos(12, 2, 2); // 距离10格
//        
//        // 使用Skeleton避免与其他测试的Zombie冲突
//        var skeleton =  com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(
//            helper, spawnPos, EntityType.SKELETON);
//        
//        //  注册VisionSensor
//        if (skeleton.hasData(NpcMindAttachment.NPC_MIND)) {
//            var mind = skeleton.getData(NpcMindAttachment.NPC_MIND);
//            mind.getSensorManager().registerSensor(new com.Kizunad.customNPCs.ai.sensors.VisionSensor());
//        }
//        
//        // 生成掉落物（木棍）
//        ItemStack itemStack = new ItemStack(Items.STICK, 1);
//        net.minecraft.world.phys.Vec3 itemAbsPos = helper.absolutePos(itemPos).getCenter();
//        ItemEntity itemEntity = new ItemEntity(
//            helper.getLevel(),
//            itemAbsPos.x,
//            itemAbsPos.y,
//            itemAbsPos.z,
//            itemStack
//        );
//        helper.getLevel().addFreshEntity(itemEntity);
//        
//        System.out.println("[testTheGatherer] 场景设置完成: Skeleton在 " + 
//            spawnPos.toShortString() + ", 物品在 " + itemPos.toShortString());
//        
//        // 2. 配置 NpcMind
//        if (skeleton.hasData(NpcMindAttachment.NPC_MIND)) {
//            var mind = skeleton.getData(NpcMindAttachment.NPC_MIND);
//            
//            // 添加 GatherItemGoal
//            GatherItemGoal gatherGoal = new GatherItemGoal(itemEntity, 100.0f);
//            mind.getGoalSelector().registerGoal(gatherGoal);
//            
//            System.out.println("[testTheGatherer] NpcMind已配置，目标优先级: " + 
//                gatherGoal.getPriority(mind, skeleton));
//        }
//        
//        // 3. 启动 Mind tick
//        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, skeleton);
//        
//        // 4. 使用 waitForCondition 等待成功（带超时）
//        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
//            helper,
//            () -> {
//                // 验证物品已被拾取
//                if (itemEntity.isAlive()) {
//                    return false;
//                }
//                
//                // 验证 Skeleton 主手持有物品
//                ItemStack heldItem = skeleton.getItemInHand(InteractionHand.MAIN_HAND);
//                if (heldItem.isEmpty() || heldItem.getItem() != Items.STICK) {
//                    return false;
//                }
//                
//                System.out.println("[testTheGatherer] ✓ 物品实体已移除");
//                System.out.println("[testTheGatherer] ✓ Skeleton主手持有木棍 x" + heldItem.getCount());
//                System.out.println("[testTheGatherer] ===== 测试通过！=====");
//                return true;
//            },
//            250,  // 最大等待250 ticks
//            "物品未被拾取或Skeleton未持有木棍 (超时: 250 ticks)"
//        );
//    }
    
    /**
     * 场景 B: "搬运工" (The Courier)
     * <p>
     * NPC 需要将物品从 A 点搬运到 B 点
     */
    @GameTest(template = "empty", timeoutTicks = 400, batch = TestBatches.COMPLEX)
    public static void testTheCourier(GameTestHelper helper) {
        // 0. 构建地板
        buildFloor(helper);

        // 1. 环境设置
        BlockPos spawnPos = new BlockPos(2, 2, 2);      // A点（实体出生点）
        BlockPos itemPos = new BlockPos(12, 2, 2);      // B点（物品位置）
        BlockPos targetPos = new BlockPos(22, 2, 2);    // C点（目标位置）
        
        // 使用工厂创建 Zombie 实体
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createTestZombie(helper, spawnPos);
        
        // 生成掉落物（苹果）
        ItemStack itemStack = new ItemStack(Items.APPLE, 1);
        net.minecraft.world.phys.Vec3 itemAbsPos = helper.absolutePos(itemPos).getCenter();
        ItemEntity itemEntity = new ItemEntity(
            helper.getLevel(),
            itemAbsPos.x,
            itemAbsPos.y,
            itemAbsPos.z,
            itemStack
        );
        helper.getLevel().addFreshEntity(itemEntity);
        
        System.out.println("[testTheCourier] 场景设置完成:");
        System.out.println("  A点(出生): " + spawnPos.toShortString());
        System.out.println("  B点(物品): " + itemPos.toShortString());
        System.out.println("  C点(目标): " + targetPos.toShortString());
        
        // 2. 配置 NpcMind
        if (zombie.hasData(NpcMindAttachment.NPC_MIND)) {
            var mind = zombie.getData(NpcMindAttachment.NPC_MIND);
            
            // 添加 CourierGoal
            BlockPos absoluteTargetPos = helper.absolutePos(targetPos);
            CourierGoal courierGoal = new CourierGoal(itemEntity, absoluteTargetPos, 100.0f);
            mind.getGoalSelector().registerGoal(courierGoal);
            
            System.out.println("[testTheCourier] NpcMind已配置");
        }
        
        // 3. 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 4. 使用 waitForCondition 等待成功（带超时）
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                // 验证源物品已被移除
                if (itemEntity.isAlive()) {
                    return false;
                }
                
                // 验证 Zombie 主手为空
                ItemStack heldItem = zombie.getItemInHand(InteractionHand.MAIN_HAND);
                if (!heldItem.isEmpty()) {
                    return false;
                }
                
                // 验证目标位置附近存在物品实体
                AABB searchBox = new AABB(helper.absolutePos(targetPos)).inflate(3.0);
                List<ItemEntity> itemsAtTarget = helper.getLevel().getEntitiesOfClass(
                    ItemEntity.class,
                    searchBox,
                    item -> item.isAlive() && item.getItem().getItem() == Items.APPLE
                );
                
                if (itemsAtTarget.isEmpty()) {
                    return false;
                }
                
                ItemEntity droppedItem = itemsAtTarget.get(0);
                System.out.println("[testTheCourier] ✓ 源物品实体已移除");
                System.out.println("[testTheCourier] ✓ Zombie主手已清空");
                System.out.println("[testTheCourier] ✓ 物品已送达目标位置: " + 
                    droppedItem.blockPosition().toShortString());
                System.out.println("[testTheCourier] ===== 测试通过！=====");
                return true;
            },
            350,  // 最大等待350 ticks
            "物品未被搬运到目标位置 (超时: 350 ticks)"
        );
    }
    
    /**
     * 场景 C: "压力测试" (Performance Stress Test)
     * <p>
     * 评估规划器和感知系统在复杂环境下的性能
     */
    @GameTest(template = "empty", timeoutTicks = 100, batch = TestBatches.PERFORMANCE)
    public static void testPerformanceStress(GameTestHelper helper) {
        // 0. 构建地板
        buildFloor(helper);

        // 1. 环境设置
        BlockPos centerPos = new BlockPos(15, 2, 15);
        
        System.out.println("[testPerformanceStress] 开始压力测试...");
        
        // 生成50个干扰实体（村民）
        System.out.println("[testPerformanceStress] 生成50个村民...");
        for (int i = 0; i < 50; i++) {
            int x = centerPos.getX() + (i % 10) * 2 - 10;
            int z = centerPos.getZ() + (i / 10) * 2 - 10;
            BlockPos pos = new BlockPos(x, 2, z);
            com.Kizunad.customNPCs_test.utils.NpcTestHelper.spawnTaggedEntity(helper, EntityType.VILLAGER, pos);
        }
        
        // 生成20个掉落物
        System.out.println("[testPerformanceStress] 生成20个掉落物...");
        for (int i = 0; i < 20; i++) {
            int x = centerPos.getX() + (i % 5) * 3 - 6;
            int z = centerPos.getZ() + (i / 5) * 3 - 6;
            BlockPos itemPos = new BlockPos(x, 2, z);
            net.minecraft.world.phys.Vec3 itemAbsPos = helper.absolutePos(itemPos).getCenter();
            ItemEntity item = new ItemEntity(
                helper.getLevel(),
                itemAbsPos.x, itemAbsPos.y, itemAbsPos.z,
                new ItemStack(Items.DIAMOND, 1)
            );
            helper.getLevel().addFreshEntity(item);
        }
        
        // 生成测试 NPC
        Zombie testNpc = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createTestZombie(helper, centerPos);
        
        // 2. 性能测试变量
        final long[] planningStartTime = {0};
        final long[] planningEndTime = {0};
        final long[] totalTickTime = {0};
        final int[] tickCount = {0};
        final int sampleSize = 20;
        final boolean[] testCompleted = {false};
        
        // 3. 配置 NpcMind 并触发规划
        if (testNpc.hasData(NpcMindAttachment.NPC_MIND)) {
            var mind = testNpc.getData(NpcMindAttachment.NPC_MIND);
            
            // 记录规划开始时间
            planningStartTime[0] = System.nanoTime();
            
            // 执行规划（使用空规划器测试）
            GoapPlanner planner = new GoapPlanner();
            var currentState = mind.getCurrentWorldState(testNpc);
            var desiredState = mind.getCurrentWorldState(testNpc);
            desiredState.setState("test_goal", true);
            
            var plan = planner.plan(currentState, desiredState, List.of());
            
            // 记录规划结束时间
            planningEndTime[0] = System.nanoTime();
            
            long planningTimeMs = (planningEndTime[0] - planningStartTime[0]) / 1_000_000;
            System.out.println("[testPerformanceStress] 规划耗时: " + planningTimeMs + " ms");
        }
        
        // 4. 测量 tick 性能（手动计数）
        helper.onEachTick(() -> {
            if (!testCompleted[0] && tickCount[0] < sampleSize) {
                long tickStart = System.nanoTime();
                
                if (testNpc.hasData(NpcMindAttachment.NPC_MIND)) {
                    var mind = testNpc.getData(NpcMindAttachment.NPC_MIND);
                    mind.tick(helper.getLevel(), testNpc);
                }
                
                long tickEnd = System.nanoTime();
                totalTickTime[0] += (tickEnd - tickStart);
                tickCount[0]++;
                
                // 5. 当达到采样数时，验证并成功
                if (tickCount[0] >= sampleSize) {
                    testCompleted[0] = true;
                    
                    long planningTimeMs = (planningEndTime[0] - planningStartTime[0]) / 1_000_000;
                    long avgTickTimeMs = (totalTickTime[0] / sampleSize) / 1_000_000;
                    
                    System.out.println("\n[testPerformanceStress] ===== 性能报告 =====");
                    System.out.println("  规划耗时: " + planningTimeMs + " ms");
                    System.out.println("  平均Tick耗时: " + avgTickTimeMs + " ms (" + sampleSize + "次采样)");
                    System.out.println("  干扰实体数: 50");
                    System.out.println("  掉落物数: 20");
                    
                    // 性能回归断言
                    boolean planningTooSlow = planningTimeMs > 50;
                    boolean tickTooSlow = avgTickTimeMs > 5;
                    if (planningTooSlow || tickTooSlow) {
                        helper.fail(
                            "性能回归: 规划=" + planningTimeMs + "ms(阈值<=50ms), " +
                                "Tick=" + avgTickTimeMs + "ms(阈值<=5ms)"
                        );
                        return;
                    }
                    System.out.println("  ✓ 规划性能良好");
                    System.out.println("  ✓ Tick性能良好");
                    
                    System.out.println("[testPerformanceStress] ===== 测试完成！=====\n");
                    
                    // 手动调用成功
                    helper.succeed();
                }
            }
        });
    }

    /**
     * 构建地板，确保实体不会掉落且可以寻路
     */
    private static void buildFloor(GameTestHelper helper) {
        for (int x = 0; x < 30; x++) {
            for (int z = 0; z < 30; z++) {
                helper.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.STONE);
            }
        }
    }
}
