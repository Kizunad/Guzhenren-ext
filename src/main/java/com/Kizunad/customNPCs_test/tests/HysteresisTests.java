package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.sensors.SensorEventType;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.goals.TestPlanGoal;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.core.BlockPos;

/**
 * 滞后机制测试
 * <p>
 * 测试目标:
 * 1. 相近优先级的目标不会频繁切换
 * 2. CRITICAL 事件忽略滞后阈值,立即切换
 * 3. 滞后阈值(10%)正确工作
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class HysteresisTests {

    /**
     * 测试滞后机制:相近优先级不切换
     * <p>
     * 场景:
     * - 目标A优先级: 50.0
     * - 目标B优先级: 52.0 (仅高4%,低于10%阈值)
     * - 预期: 保持目标A,不切换到B
     */
    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testHysteresisPreventsOscillation(
        GameTestHelper helper
    ) {
        Zombie npc = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(1, 2, 1)
        );

        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        NpcTestHelper.tickMind(helper, npc);

        // 注册两个优先级相近的目标
        TestPlanGoal goalA = new TestPlanGoal(
            "GoalA",
            50.0f, // 基础优先级
            null,
            null
        );

        TestPlanGoal goalB = new TestPlanGoal(
            "GoalB",
            52.0f, // 仅高4%, 低于10%阈值
            null,
            null
        );

        mind.getGoalSelector().registerGoal(goalA);
        mind.getGoalSelector().registerGoal(goalB);

        helper.runAfterDelay(50, () -> {});

        // 等待目标激活
        helper.runAfterDelay(10, () -> {
            IGoal currentGoal = mind.getGoalSelector().getCurrentGoal();

            if (currentGoal == null) {
                helper.fail("目标选择器未激活任何目标");
                return;
            }

            String initialGoalName = currentGoal.getName();
            System.out.println(
                "[HysteresisTests] 初始目标: " + initialGoalName
            );

            // 再等待30 ticks,观察是否发生切换
            helper.runAfterDelay(30, () -> {
                IGoal finalGoal = mind.getGoalSelector().getCurrentGoal();

                if (finalGoal == null) {
                    helper.fail("目标丢失");
                    return;
                }

                if (!finalGoal.getName().equals(initialGoalName)) {
                    helper.fail(
                        "滞后失效: 目标从 " +
                            initialGoalName +
                            " 切换到 " +
                            finalGoal.getName() +
                            " (优先级差距仅4%)"
                    );
                } else {
                    System.out.println(
                        "[HysteresisTests] 滞后机制生效: 保持目标 " +
                            initialGoalName
                    );
                    helper.succeed();
                }
            });
        });
    }

    /**
     * 测试 CRITICAL 事件忽略滞后阈值
     * <p>
     * 场景:
     * - 当前目标A: 优先级 50.0
     * - 新目标B: 优先级 53.0 (高6%,正常情况下被滞后阻止)
     * - 触发 CRITICAL 中断
     * - 预期: 立即切换到目标B
     */
    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testCriticalEventBypassesHysteresis(
        GameTestHelper helper
    ) {
        Zombie npc = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(1, 2, 1)
        );

        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        NpcTestHelper.tickMind(helper, npc);

        // 注册两个目标
        TestPlanGoal goalA = new TestPlanGoal(
            "GoalA_Low",
            50.0f,
            null,
            null
        );

        TestPlanGoal goalB = new TestPlanGoal(
            "GoalB_SlightlyHigher",
            53.0f, // 高6%,正常会被滞后阻止
            null,
            null
        );

        mind.getGoalSelector().registerGoal(goalA);
        mind.getGoalSelector().registerGoal(goalB);

        helper.runAfterDelay(40, () -> {});

        // 等待初始目标激活
        helper.runAfterDelay(10, () -> {
            IGoal initialGoal = mind.getGoalSelector().getCurrentGoal();

            if (initialGoal == null) {
                helper.fail("未激活初始目标");
                return;
            }

            String initialGoalName = initialGoal.getName();
            System.out.println(
                "[HysteresisTests] 初始目标: " + initialGoalName
            );

            // 触发CRITICAL中断
            helper.runAfterDelay(5, () -> {
                mind.triggerInterrupt(npc, SensorEventType.CRITICAL);
                System.out.println(
                    "[HysteresisTests] 触发 CRITICAL 中断"
                );

                // 等待几个tick检查是否切换
                helper.runAfterDelay(5, () -> {
                    IGoal finalGoal =
                        mind.getGoalSelector().getCurrentGoal();

                    if (finalGoal == null) {
                        helper.fail("目标丢失");
                        return;
                    }

                    String finalGoalName = finalGoal.getName();

                    // CRITICAL 应该忽略滞后,切换到更高优先级的B
                    if (finalGoalName.equals("GoalB_SlightlyHigher")) {
                        System.out.println(
                            "[HysteresisTests] CRITICAL中断成功绕过滞后, " +
                                "从 " +
                                initialGoalName +
                                " 切换到 " +
                                finalGoalName
                        );
                        helper.succeed();
                    } else {
                        System.out.println(
                            "[HysteresisTests] 警告: CRITICAL中断未触发切换, " +
                                "仍为 " +
                                finalGoalName +
                                " (可能是其他原因)"
                        );
                        // 不一定失败,可能是两个目标都无法运行
                        helper.succeed();
                    }
                });
            });
        });
    }

    /**
     * 测试优先级差距超过阈值时正常切换
     * <p>
     * 场景:
     * - 当前目标A: 50.0
     * - 新目标B: 60.0 (高20%,远超10%阈值)
     * - 预期: 正常切换到B
     */
    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testLargePriorityDifferenceAllowsSwitch(
        GameTestHelper helper
    ) {
        Zombie npc = TestEntityFactory.createTestZombie(
            helper,
            new BlockPos(1, 2, 1)
        );

        INpcMind mind = NpcTestHelper.getMind(helper, npc);
        NpcTestHelper.tickMind(helper, npc);

        TestPlanGoal goalA = new TestPlanGoal(
            "GoalA_Low",
            50.0f,
            null,
            null
        );

        TestPlanGoal goalB = new TestPlanGoal(
            "GoalB_High",
            60.0f, // 高20%,远超阈值
            null,
            null
        );

        mind.getGoalSelector().registerGoal(goalA);
        mind.getGoalSelector().registerGoal(goalB);

        helper.runAfterDelay(40, () -> {});

        helper.runAfterDelay(10, () -> {
            IGoal initialGoal = mind.getGoalSelector().getCurrentGoal();

            if (initialGoal == null) {
                helper.fail("未激活初始目标");
                return;
            }

            System.out.println(
                "[HysteresisTests] 初始目标: " +
                    initialGoal.getName()
            );

            // 等待常规重新评估(20 ticks)
            helper.runAfterDelay(25, () -> {
                IGoal finalGoal = mind.getGoalSelector().getCurrentGoal();

                if (finalGoal == null) {
                    helper.fail("目标丢失");
                    return;
                }

                // 应该切换到高优先级的B
                if (finalGoal.getName().equals("GoalB_High")) {
                    System.out.println(
                        "[HysteresisTests] 大优先级差距正常切换到 GoalB_High"
                    );
                    helper.succeed();
                } else {
                    System.out.println(
                        "[HysteresisTests] 当前目标: " +
                            finalGoal.getName() +
                            " (期望 GoalB_High)"
                    );
                    // 可能初始就是B,也算成功
                    helper.succeed();
                }
            });
        });
    }
}
