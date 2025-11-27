package com.Kizunad.customNPCs_test.goals;

import com.Kizunad.customNPCs.ai.decision.goals.HealGoal;
import com.Kizunad.customNPCs.ai.decision.goals.FleeGoal;
import com.Kizunad.customNPCs.ai.decision.goals.DefendGoal;
import com.Kizunad.customNPCs.capabilities.mind.NpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * Goal GameTest测试套件
 * <p>
 * 验证HealGoal、FleeGoal、DefendGoal的核心逻辑
 */
@GameTestHolder("guzhenren")
public class GoalTests {

    /**
     * 测试：HealGoal在低血量时有高优先级
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testHealGoalLowHealthHighPriority(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Mob npc = helper.spawn(EntityType.ZOMBIE, spawnPos);
        NpcMind mind = new NpcMind();
        HealGoal healGoal = new HealGoal();
        
        // 降低血量到30%
        npc.setHealth(npc.getMaxHealth() * 0.3f);
        
        // 检查优先级应该很高
        float priority = healGoal.getPriority(mind, npc);
        
        if (priority > 0.5f) {
            helper.succeed();
        } else {
            helper.fail("血量30%时优先级应该高于0.5，实际: " + priority);
        }
    }

    /**
     * 测试：HealGoal在无食物时不激活
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testHealGoalNoFoodDoesNotActivate(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Mob npc = helper.spawn(EntityType.ZOMBIE, spawnPos);
        NpcMind mind = new NpcMind();
        HealGoal healGoal = new HealGoal();
        
        // 降低血量
        npc.setHealth(npc.getMaxHealth() * 0.3f);
        
        // 检查canRun - 应该返回false（没有食物）
        boolean canRun = healGoal.canRun(mind, npc);
        
        if (!canRun) {
            helper.succeed();
        } else {
            helper.fail("HealGoal应该在没有食物时不激活");
        }
    }

    /**
     * 测试：HealGoal优先级随血量变化
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testHealGoalPriorityScalesWithHealth(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Mob npc = helper.spawn(EntityType.ZOMBIE, spawnPos);
        NpcMind mind = new NpcMind();
        HealGoal healGoal = new HealGoal();
        
        // 测试不同血量下的优先级
        npc.setHealth(npc.getMaxHealth() * 0.1f); // 10%血量
        float priority10 = healGoal.getPriority(mind, npc);
        
        npc.setHealth(npc.getMaxHealth() * 0.4f); // 40%血量
        float priority40 = healGoal.getPriority(mind, npc);
        
        // 血量越低，优先级应该越高
        if (priority10 > priority40) {
            helper.succeed();
        } else {
            helper.fail("血量10%时优先级(" + priority10 + ")应该高于40%时(" + priority40 + ")");
        }
    }

    /**
    * 测试：FleeGoal在危险时有极高优先级
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testFleeGoalInDangerHighestPriority(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Mob npc = helper.spawn(EntityType.ZOMBIE, spawnPos);
        NpcMind mind = new NpcMind();
        FleeGoal fleeGoal = new FleeGoal();
        
        // 降低血量到危险水平
        npc.setHealth(npc.getMaxHealth() * 0.2f);
        
        // 设置受伤标记
        npc.hurt(npc.damageSources().generic(), 1.0f);
        
        // 检查优先级应该非常高
        float priority = fleeGoal.getPriority(mind, npc);
        
        if (priority >= 0.9f) {
            helper.succeed();
        } else {
            helper.fail("FleeGoal在危险时优先级应该>=0.9，实际: " + priority);
        }
    }

    /**
     * 测试：FleeGoal在安全时不激活
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testFleeGoalSafeDoesNotActivate(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Mob npc = helper.spawn(EntityType.ZOMBIE, spawnPos);
        NpcMind mind = new NpcMind();
        FleeGoal fleeGoal = new FleeGoal();
        
        // 满血，无威胁
        npc.setHealth(npc.getMaxHealth());
        
        boolean canRun = fleeGoal.canRun(mind, npc);
        float priority = fleeGoal.getPriority(mind, npc);
        
        if (!canRun && priority == 0.0f) {
            helper.succeed();
        } else {
            helper.fail("FleeGoal在安全时应该不激活");
        }
    }

    /**
     * 测试：DefendGoal在受攻击时激活
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testDefendGoalWhenHurtActivates(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Mob npc = helper.spawn(EntityType.ZOMBIE, spawnPos);
        NpcMind mind = new NpcMind();
        DefendGoal defendGoal = new DefendGoal();
        
        // 模拟受到攻击
        npc.hurt(npc.damageSources().generic(), 1.0f);
        
        // 检查是否可以运行
        boolean canRun = defendGoal.canRun(mind, npc);
        float priority = defendGoal.getPriority(mind, npc);
        
        if (canRun && priority > 0.5f) {
            helper.succeed();
        } else {
            helper.fail("DefendGoal在受攻击时应该激活，canRun: " + canRun + ", priority: " + priority);
        }
    }

    /**
     * 测试：Goal的getName方法返回有效名称
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testGoalsGetNameReturnsValidNames(GameTestHelper helper) {
        HealGoal healGoal = new HealGoal();
        FleeGoal fleeGoal = new FleeGoal();
        DefendGoal defendGoal = new DefendGoal();
        
        String healName = healGoal.getName();
        String fleeName = fleeGoal.getName();
        String defendName = defendGoal.getName();
        
        boolean allValid = healName != null && !healName.isEmpty()
            && fleeName != null && !fleeName.isEmpty()
            && defendName != null && !defendName.isEmpty();
        
        boolean allUnique = !healName.equals(fleeName)
            && !healName.equals(defendName)
            && !fleeName.equals(defendName);
        
        if (allValid && allUnique) {
            helper.succeed();
        } else {
            helper.fail("Goal名称应该有效且唯一");
        }
    }
}
