package com.Kizunad.guzhenrenext.entity.ai.goals;

import com.Kizunad.guzhenrenext.entity.ai.GoapMindCore;
import java.util.HashMap;
import java.util.Map;

/**
 * 散修专用 Goal 系统定义工具类。
 * 提供 5 个静态方法，向 GoapMindCore 注册对应的 Goal 和 Action。
 * 所有 WorldState key 定义为常量，便于维护和复用。
 */
public final class RogueGoalDefinitions {

    // ==================== WorldState Key 常量 ====================

    /**
     * 是否有敌人（true = 有敌人，false = 无敌人）。
     */
    public static final String KEY_HAS_ENEMY = "hasEnemy";

    /**
     * 是否有任务（true = 有任务，false = 无任务）。
     */
    public static final String KEY_HAS_QUEST = "hasQuest";

    /**
     * 是否有交易对象（true = 有交易对象，false = 无交易对象）。
     */
    public static final String KEY_HAS_TRADER = "hasTrader";

    /**
     * 生命值是否低（true = 生命值低，false = 生命值正常）。
     */
    public static final String KEY_LOW_HEALTH = "lowHealth";

    /**
     * 是否有势力归属（true = 有归属，false = 无归属）。
     */
    public static final String KEY_HAS_FACTION = "hasFaction";

    // ==================== Goal 优先级常量 ====================

    /**
     * 游荡目标优先级（最低）。
     */
    private static final int PRIORITY_WANDER = 1;

    /**
     * 交易目标优先级。
     */
    private static final int PRIORITY_TRADE = 2;

    /**
     * 加入势力目标优先级。
     */
    private static final int PRIORITY_JOIN_FACTION = 2;

    /**
     * 狩猎目标优先级。
     */
    private static final int PRIORITY_HUNT = 3;

    /**
     * 逃跑目标优先级（最高）。
     */
    private static final int PRIORITY_FLEE = 5;

    // ==================== Goal 名称常量 ====================

    /**
     * 游荡 Goal 名称。
     */
    private static final String GOAL_WANDER = "wander";

    /**
     * 狩猎 Goal 名称。
     */
    private static final String GOAL_HUNT = "hunt";

    /**
     * 交易 Goal 名称。
     */
    private static final String GOAL_TRADE = "trade";

    /**
     * 逃跑 Goal 名称。
     */
    private static final String GOAL_FLEE = "flee";

    /**
     * 加入势力 Goal 名称。
     */
    private static final String GOAL_JOIN_FACTION = "joinFaction";

    // ==================== Action 名称常量 ====================

    /**
     * 游荡 Action 名称。
     */
    private static final String ACTION_WANDER = "actionWander";

    /**
     * 狩猎 Action 名称。
     */
    private static final String ACTION_HUNT = "actionHunt";

    /**
     * 交易 Action 名称。
     */
    private static final String ACTION_TRADE = "actionTrade";

    /**
     * 逃跑 Action 名称。
     */
    private static final String ACTION_FLEE = "actionFlee";

    /**
     * 加入势力 Action 名称。
     */
    private static final String ACTION_JOIN_FACTION = "actionJoinFaction";

    /**
     * 私有构造函数，防止实例化。
     */
    private RogueGoalDefinitions() {
        // 工具类，不应被实例化
    }

    /**
     * 注册游荡 Goal 和对应 Action。
     * 条件：无敌人、无任务。
     * 优先级：1（最低）。
     *
     * @param mindCore GOAP 核心实例
     */
    public static void registerWanderGoal(GoapMindCore mindCore) {
        // 游荡 Goal：无敌人且无任务时触发
        Map<String, Boolean> wanderConditions = new HashMap<>();
        wanderConditions.put(KEY_HAS_ENEMY, false);
        wanderConditions.put(KEY_HAS_QUEST, false);
        mindCore.registerGoal(GOAL_WANDER, PRIORITY_WANDER, wanderConditions);

        // 游荡 Action：无前置条件，效果为满足游荡 Goal
        Map<String, Boolean> wanderPreconditions = new HashMap<>();
        Map<String, Boolean> wanderEffects = new HashMap<>();
        wanderEffects.put(KEY_HAS_ENEMY, false);
        wanderEffects.put(KEY_HAS_QUEST, false);
        mindCore.registerAction(
            ACTION_WANDER,
            wanderPreconditions,
            wanderEffects,
            () -> {
                // 游荡行为由实体处理
            }
        );
    }

    /**
     * 注册狩猎 Goal 和对应 Action。
     * 条件：有敌人。
     * 优先级：3。
     *
     * @param mindCore GOAP 核心实例
     */
    public static void registerHuntGoal(GoapMindCore mindCore) {
        // 狩猎 Goal：有敌人时触发
        Map<String, Boolean> huntConditions = new HashMap<>();
        huntConditions.put(KEY_HAS_ENEMY, true);
        mindCore.registerGoal(GOAL_HUNT, PRIORITY_HUNT, huntConditions);

        // 狩猎 Action：有敌人时执行
        Map<String, Boolean> huntPreconditions = new HashMap<>();
        huntPreconditions.put(KEY_HAS_ENEMY, true);
        Map<String, Boolean> huntEffects = new HashMap<>();
        huntEffects.put(KEY_HAS_ENEMY, true);
        mindCore.registerAction(
            ACTION_HUNT,
            huntPreconditions,
            huntEffects,
            () -> {
                // 狩猎行为由实体处理
            }
        );
    }

    /**
     * 注册交易 Goal 和对应 Action。
     * 条件：有交易对象。
     * 优先级：2。
     *
     * @param mindCore GOAP 核心实例
     */
    public static void registerTradeGoal(GoapMindCore mindCore) {
        // 交易 Goal：有交易对象时触发
        Map<String, Boolean> tradeConditions = new HashMap<>();
        tradeConditions.put(KEY_HAS_TRADER, true);
        mindCore.registerGoal(GOAL_TRADE, PRIORITY_TRADE, tradeConditions);

        // 交易 Action：有交易对象时执行
        Map<String, Boolean> tradePreconditions = new HashMap<>();
        tradePreconditions.put(KEY_HAS_TRADER, true);
        Map<String, Boolean> tradeEffects = new HashMap<>();
        tradeEffects.put(KEY_HAS_TRADER, true);
        mindCore.registerAction(
            ACTION_TRADE,
            tradePreconditions,
            tradeEffects,
            () -> {
                // 交易行为由实体处理
            }
        );
    }

    /**
     * 注册逃跑 Goal 和对应 Action。
     * 条件：生命值低。
     * 优先级：5（最高）。
     *
     * @param mindCore GOAP 核心实例
     */
    public static void registerFleeGoal(GoapMindCore mindCore) {
        // 逃跑 Goal：生命值低时触发
        Map<String, Boolean> fleeConditions = new HashMap<>();
        fleeConditions.put(KEY_LOW_HEALTH, true);
        mindCore.registerGoal(GOAL_FLEE, PRIORITY_FLEE, fleeConditions);

        // 逃跑 Action：生命值低时执行
        Map<String, Boolean> fleePreconditions = new HashMap<>();
        fleePreconditions.put(KEY_LOW_HEALTH, true);
        Map<String, Boolean> fleeEffects = new HashMap<>();
        fleeEffects.put(KEY_LOW_HEALTH, true);
        mindCore.registerAction(
            ACTION_FLEE,
            fleePreconditions,
            fleeEffects,
            () -> {
                // 逃跑行为由实体处理
            }
        );
    }

    /**
     * 注册加入势力 Goal 和对应 Action。
     * 条件：无势力归属。
     * 优先级：2。
     *
     * @param mindCore GOAP 核心实例
     */
    public static void registerJoinFactionGoal(GoapMindCore mindCore) {
        // 加入势力 Goal：无势力归属时触发
        Map<String, Boolean> joinFactionConditions = new HashMap<>();
        joinFactionConditions.put(KEY_HAS_FACTION, false);
        mindCore.registerGoal(
            GOAL_JOIN_FACTION,
            PRIORITY_JOIN_FACTION,
            joinFactionConditions
        );

        // 加入势力 Action：无势力归属时执行
        Map<String, Boolean> joinFactionPreconditions = new HashMap<>();
        joinFactionPreconditions.put(KEY_HAS_FACTION, false);
        Map<String, Boolean> joinFactionEffects = new HashMap<>();
        joinFactionEffects.put(KEY_HAS_FACTION, false);
        mindCore.registerAction(
            ACTION_JOIN_FACTION,
            joinFactionPreconditions,
            joinFactionEffects,
            () -> {
                // 加入势力行为由实体处理
            }
        );
    }
}
