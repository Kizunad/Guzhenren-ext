package com.Kizunad.guzhenrenext.entity.ai.goals;

import com.Kizunad.guzhenrenext.entity.ai.GoapMindCore;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 散修 Goal 系统单元测试。
 * 验证 5 个 Goal 的注册、条件匹配、优先级选择等功能。
 */
public class RogueGoalDefinitionsTest {

    private GoapMindCore mindCore;

    @BeforeEach
    public void setUp() {
        mindCore = new GoapMindCore();
    }

    @Test
    public void testRegisterAllGoals() {
        // 准备：注册所有 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);
        RogueGoalDefinitions.registerHuntGoal(mindCore);
        RogueGoalDefinitions.registerTradeGoal(mindCore);
        RogueGoalDefinitions.registerFleeGoal(mindCore);
        RogueGoalDefinitions.registerJoinFactionGoal(mindCore);

        // 验证：所有 Goal 已注册
        assertEquals(5, mindCore.getRegisteredGoalNames().size());
    }

    @Test
    public void testWanderGoalSelection() {
        // 准备：注册游荡 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);

        // 执行：设置无敌人、无任务的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_QUEST, false);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择游荡 Action
        assertEquals("actionWander", mindCore.getCurrentAction());
    }

    @Test
    public void testHuntGoalSelection() {
        // 准备：注册狩猎 Goal
        RogueGoalDefinitions.registerHuntGoal(mindCore);

        // 执行：设置有敌人的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, true);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择狩猎 Action
        assertEquals("actionHunt", mindCore.getCurrentAction());
    }

    @Test
    public void testTradeGoalSelection() {
        // 准备：注册交易 Goal
        RogueGoalDefinitions.registerTradeGoal(mindCore);

        // 执行：设置有交易对象的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_TRADER, true);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择交易 Action
        assertEquals("actionTrade", mindCore.getCurrentAction());
    }

    @Test
    public void testFleeGoalSelection() {
        // 准备：注册逃跑 Goal
        RogueGoalDefinitions.registerFleeGoal(mindCore);

        // 执行：设置生命值低的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_LOW_HEALTH, true);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择逃跑 Action
        assertEquals("actionFlee", mindCore.getCurrentAction());
    }

    @Test
    public void testJoinFactionGoalSelection() {
        // 准备：注册加入势力 Goal
        RogueGoalDefinitions.registerJoinFactionGoal(mindCore);

        // 执行：设置无势力归属的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_FACTION, false);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择加入势力 Action
        assertEquals("actionJoinFaction", mindCore.getCurrentAction());
    }

    @Test
    public void testFleeGoalHighestPriority() {
        // 准备：注册所有 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);
        RogueGoalDefinitions.registerHuntGoal(mindCore);
        RogueGoalDefinitions.registerTradeGoal(mindCore);
        RogueGoalDefinitions.registerFleeGoal(mindCore);
        RogueGoalDefinitions.registerJoinFactionGoal(mindCore);

        // 执行：设置多个条件满足的世界状态
        // 有敌人、有交易对象、生命值低、无势力归属
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, true);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_TRADER, true);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_LOW_HEALTH, true);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_FACTION, false);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择逃跑 Action（优先级最高）
        assertEquals("actionFlee", mindCore.getCurrentAction());
    }

    @Test
    public void testHuntGoalHigherThanTrade() {
        // 准备：注册狩猎和交易 Goal
        RogueGoalDefinitions.registerHuntGoal(mindCore);
        RogueGoalDefinitions.registerTradeGoal(mindCore);

        // 执行：设置有敌人和有交易对象的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, true);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_TRADER, true);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择狩猎 Action（优先级 3 > 2）
        assertEquals("actionHunt", mindCore.getCurrentAction());
    }

    @Test
    public void testNoGoalSelectedWhenConditionsNotMet() {
        // 准备：注册所有 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);
        RogueGoalDefinitions.registerHuntGoal(mindCore);
        RogueGoalDefinitions.registerTradeGoal(mindCore);
        RogueGoalDefinitions.registerFleeGoal(mindCore);
        RogueGoalDefinitions.registerJoinFactionGoal(mindCore);

        // 执行：设置所有条件都不满足的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_QUEST, true);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_TRADER, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_LOW_HEALTH, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_FACTION, true);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：不应选择任何 Action
        assertNull(mindCore.getCurrentAction());
    }

    @Test
    public void testWanderGoalLowestPriority() {
        // 准备：注册游荡和交易 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);
        RogueGoalDefinitions.registerTradeGoal(mindCore);

        // 执行：设置无敌人、无任务、有交易对象的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_QUEST, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_TRADER, true);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择交易 Action（优先级 2 > 1）
        assertEquals("actionTrade", mindCore.getCurrentAction());
    }

    @Test
    public void testUnsetWorldStateDoesNotMatchFalseConditionsUntilExplicitlySet() {
        // 准备：注册游荡 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);

        // 执行：不设置任何世界状态，直接触发规划
        mindCore.tick(20L);

        // 验证：未设置的状态在当前 GoapMindCore 语义下不会自动视为 false，因此不应命中 wander
        assertNull(mindCore.getCurrentAction());

        // 执行：显式设置需要的 false 条件后再次触发规划
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_QUEST, false);
        mindCore.tick(40L);

        // 验证：显式设置后应选择游荡 Action
        assertEquals("actionWander", mindCore.getCurrentAction());
    }

    @Test
    public void testMultipleTicksWithStateChange() {
        // 准备：注册所有 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);
        RogueGoalDefinitions.registerHuntGoal(mindCore);
        RogueGoalDefinitions.registerFleeGoal(mindCore);

        // 执行：第一次规划 - 无敌人
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, false);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_QUEST, false);
        mindCore.tick(20L);
        String firstAction = mindCore.getCurrentAction();

        // 执行：第二次规划 - 有敌人
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, true);
        mindCore.tick(40L);
        String secondAction = mindCore.getCurrentAction();

        // 验证：Action 应该改变
        assertEquals("actionWander", firstAction);
        assertEquals("actionHunt", secondAction);
    }

    @Test
    public void testAllGoalsRegisteredWithCorrectNames() {
        // 准备：注册所有 Goal
        RogueGoalDefinitions.registerWanderGoal(mindCore);
        RogueGoalDefinitions.registerHuntGoal(mindCore);
        RogueGoalDefinitions.registerTradeGoal(mindCore);
        RogueGoalDefinitions.registerFleeGoal(mindCore);
        RogueGoalDefinitions.registerJoinFactionGoal(mindCore);

        // 验证：所有 Goal 名称都已注册
        assertNotNull(mindCore.getRegisteredGoalNames());
        assertEquals(5, mindCore.getRegisteredGoalNames().size());
    }

    @Test
    public void testAllActionsRegistered() {
        // 准备：注册所有 Goal（会同时注册对应的 Action）
        RogueGoalDefinitions.registerWanderGoal(mindCore);
        RogueGoalDefinitions.registerHuntGoal(mindCore);
        RogueGoalDefinitions.registerTradeGoal(mindCore);
        RogueGoalDefinitions.registerFleeGoal(mindCore);
        RogueGoalDefinitions.registerJoinFactionGoal(mindCore);

        // 验证：所有 Action 都已注册
        assertNotNull(mindCore.getRegisteredActionNames());
        assertEquals(5, mindCore.getRegisteredActionNames().size());
    }

    @Test
    public void testTradeAndJoinFactionSamePriority() {
        // 准备：注册交易和加入势力 Goal
        RogueGoalDefinitions.registerTradeGoal(mindCore);
        RogueGoalDefinitions.registerJoinFactionGoal(mindCore);

        // 执行：设置有交易对象和无势力归属的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_TRADER, true);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_FACTION, false);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择其中一个 Action（两者优先级相同，选择注册顺序第一个）
        String action = mindCore.getCurrentAction();
        assertEquals("actionTrade", action);
    }

    @Test
    public void testFleeGoalWithEnemyAndLowHealth() {
        // 准备：注册逃跑和狩猎 Goal
        RogueGoalDefinitions.registerFleeGoal(mindCore);
        RogueGoalDefinitions.registerHuntGoal(mindCore);

        // 执行：设置有敌人和生命值低的世界状态
        mindCore.setWorldState(RogueGoalDefinitions.KEY_HAS_ENEMY, true);
        mindCore.setWorldState(RogueGoalDefinitions.KEY_LOW_HEALTH, true);

        // 执行：触发规划
        mindCore.tick(20L);

        // 验证：应选择逃跑 Action（优先级 5 > 3）
        assertEquals("actionFlee", mindCore.getCurrentAction());
    }
}
