package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.sensors.SensorEventType;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.LivingEntity;

/**
 * 并发与抢占控制相关测试
 */
@SuppressWarnings({"checkstyle:MagicNumber"})
public class PreemptionAndCooldownTests {

    /**
     * 刚切换后的目标应该获得额外保护，防止轻微优势的目标立即抢占
     */
    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testEarlyPreemptionRespectsThreshold(
        GameTestHelper helper
    ) {
        Zombie npc = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        if (mind == null) {
            return;
        }
        NpcTestHelper.tickMind(helper, npc);

        SimpleGoal stableGoal = new SimpleGoal("StableGoal", 10.0f);
        SimpleGoal contender = new SimpleGoal("Contender", 8.0f);
        mind.getGoalSelector().registerGoal(stableGoal);
        mind.getGoalSelector().registerGoal(contender);

        helper.runAfterDelay(5, () -> {
            assertCurrentGoal(helper, mind, stableGoal, "初始目标应为 StableGoal");
            contender.setPriority(12.0f); // 高20%，但处于抢占保护期
            mind.triggerInterrupt(npc, SensorEventType.IMPORTANT);
        });

        helper.runAfterDelay(
            10,
            () ->
                assertCurrentGoal(
                    helper,
                    mind,
                    stableGoal,
                    "抢占保护期内不应切换"
                )
        );

        // 等待超过抢占保护期后再次尝试切换
        helper.runAfterDelay(
            35,
            () -> mind.triggerInterrupt(npc, SensorEventType.IMPORTANT)
        );

        helper.runAfterDelay(40, () -> {
            IGoal finalGoal = mind.getGoalSelector().getCurrentGoal();
            if (finalGoal == contender) {
                helper.succeed();
            } else {
                helper.fail(
                    "抢占保护期后应切换到 Contender，实际: " +
                    nameOrNull(finalGoal)
                );
            }
        });
    }

    /**
     * 目标被切换后应进入冷却，避免在短时间内反复抢占
     */
    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testGoalCooldownPreventsImmediateReturn(
        GameTestHelper helper
    ) {
        Zombie npc = TestEntityFactory.createSimpleTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.ZOMBIE
        );
        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        if (mind == null) {
            return;
        }
        NpcTestHelper.tickMind(helper, npc);

        SimpleGoal goalA = new SimpleGoal("GoalA", 1.0f);
        SimpleGoal goalB = new SimpleGoal("GoalB", 0.8f);
        mind.getGoalSelector().registerGoal(goalA);
        mind.getGoalSelector().registerGoal(goalB);

        helper.runAfterDelay(5, () -> {
            assertCurrentGoal(helper, mind, goalA, "初始应该选择 GoalA");
            goalB.setPriority(1.4f); // 提升优先级促使切换
            mind.triggerInterrupt(npc, SensorEventType.CRITICAL);
        });

        helper.runAfterDelay(8, () -> {
            assertCurrentGoal(helper, mind, goalB, "应已切换到 GoalB");
            goalA.setPriority(1.5f); // 更高优先级，尝试回切
            goalB.setPriority(0.2f);
            mind.triggerInterrupt(npc, SensorEventType.IMPORTANT);
        });

        helper.runAfterDelay(
            15,
            () ->
                assertCurrentGoal(
                    helper,
                    mind,
                    goalB,
                    "冷却期间不应重新选择 GoalA"
                )
        );

        // 等待冷却结束后再次触发评估
        helper.runAfterDelay(
            65,
            () -> mind.triggerInterrupt(npc, SensorEventType.IMPORTANT)
        );

        helper.runAfterDelay(70, () -> {
            IGoal finalGoal = mind.getGoalSelector().getCurrentGoal();
            if (finalGoal == goalA) {
                helper.succeed();
            } else {
                helper.fail(
                    "冷却结束后应恢复到更高优先级的 GoalA，当前: " +
                    nameOrNull(finalGoal)
                );
            }
        });
    }

    private static void assertCurrentGoal(
        GameTestHelper helper,
        INpcMind mind,
        IGoal expected,
        String message
    ) {
        IGoal current = mind.getGoalSelector().getCurrentGoal();
        if (current != expected) {
            helper.fail(message + "，当前: " + nameOrNull(current));
        }
    }

    private static String nameOrNull(IGoal goal) {
        return goal != null ? goal.getName() : "null";
    }

    /**
     * 极简目标实现，允许在测试中动态调整优先级和状态
     */
    private static class SimpleGoal implements IGoal {

        private final String name;
        private float priority;
        private boolean canRun = true;
        private boolean finished = false;

        SimpleGoal(String name, float priority) {
            this.name = name;
            this.priority = priority;
        }

        void setPriority(float priority) {
            this.priority = priority;
        }

        void setCanRun(boolean canRun) {
            this.canRun = canRun;
        }

        void finish() {
            this.finished = true;
        }

        @Override
        public float getPriority(INpcMind mind, LivingEntity entity) {
            return priority;
        }

        @Override
        public boolean canRun(INpcMind mind, LivingEntity entity) {
            return canRun && !finished;
        }

        @Override
        public void start(INpcMind mind, LivingEntity entity) {}

        @Override
        public void tick(INpcMind mind, LivingEntity entity) {}

        @Override
        public void stop(INpcMind mind, LivingEntity entity) {}

        @Override
        public boolean isFinished(INpcMind mind, LivingEntity entity) {
            return finished;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
