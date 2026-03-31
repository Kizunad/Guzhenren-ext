package com.Kizunad.guzhenrenext.faction.hooks;

import com.Kizunad.guzhenrenext.faction.event.FactionEventBus;
import java.util.UUID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 势力事件驱动的 NPC 行为钩子。
 * <p>
 * 本类订阅 FactionEventBus 中的关键事件，并根据事件更新 NPC 的记忆和态度。
 * 包括：
 * <ul>
 *   <li>关系变化事件 - 更新相关 NPC 的关系标签</li>
 *   <li>战争声明事件 - 将敌对方的关系标签设为 ENEMY</li>
 *   <li>势力解散事件 - 清除相关成员的势力归属</li>
 * </ul>
 * </p>
 * <p>
 * 注意：本类的事件处理器只更新 NPC 的记忆状态，不直接操作实体。
 * 实际的实体行为更新由 RogueEntity.tick() 中读取记忆状态来完成。
 * </p>
 */
public final class FactionBehaviorHooks {

    // --------- 测试/状态查询用的内部静态状态 ---------
    // 关系表：sourceFaction -> (targetFaction -> relationTag)
    private static final Map<UUID, Map<UUID, String>> RELATION_MAP = new HashMap<>();
    // 已解散势力集合
    private static final Set<UUID> DISSOLVED_FACTIONS = new HashSet<>();
    // 是否已注册订阅，防止重复订阅导致的多重回调
    private static boolean registered = false;

    /**
     * 关系标签：敌对。
     */
    private static final String RELATION_TAG_ENEMY = "ENEMY";

    /**
     * 关系标签：盟友。
     */
    private static final String RELATION_TAG_ALLY = "ALLY";

    /**
     * 关系标签：中立。
     */
    private static final String RELATION_TAG_NEUTRAL = "NEUTRAL";

    /**
     * 关系值阈值：敌对与中立的分界线。
     */
    private static final int RELATION_THRESHOLD_ENEMY = -50;

    /**
     * 关系值阈值：中立与盟友的分界线。
     */
    private static final int RELATION_THRESHOLD_ALLY = 50;

    /**
     * 私有构造器，防止外部实例化。
     */
    private FactionBehaviorHooks() {
    }

    /**
     * 注册所有事件监听器到 FactionEventBus。
     * <p>
     * 该方法应在游戏初始化时调用一次，以订阅所有相关事件。
     * </p>
     */
    public static void register() {
        // 防重复订阅，测试场景多次调用也安全
        if (registered) {
            return;
        }
        registered = true;

        FactionEventBus eventBus = FactionEventBus.INSTANCE;

        // 订阅关系变化事件
        eventBus.subscribe(
            FactionEventBus.RelationChangedEvent.class,
            FactionBehaviorHooks::handleRelationChanged
        );

        // 订阅战争声明事件
        eventBus.subscribe(
            FactionEventBus.WarDeclaredEvent.class,
            FactionBehaviorHooks::handleWarDeclared
        );

        // 订阅势力解散事件
        eventBus.subscribe(
            FactionEventBus.FactionDissolvedEvent.class,
            FactionBehaviorHooks::handleFactionDissolved
        );
    }

    /**
     * 处理关系变化事件。
     * <p>
     * 当两个势力的关系值发生变化时，更新相关 NPC 的记忆中的关系标签。
     * 根据新的关系值判断是敌对、中立还是盟友。
     * </p>
     *
     * @param event 关系变化事件
     */
    private static void handleRelationChanged(FactionEventBus.RelationChangedEvent event) {
        UUID factionA = event.getFactionA();
        UUID factionB = event.getFactionB();
        int newValue = event.getNewValue();

        // 根据新的关系值确定关系标签
        String relationTag = determineRelationTag(newValue);

        // 更新 factionA 对 factionB 的关系标签
        updateFactionRelationTag(factionA, factionB, relationTag);

        // 更新 factionB 对 factionA 的关系标签（对称关系）
        updateFactionRelationTag(factionB, factionA, relationTag);
    }

    /**
     * 处理战争声明事件。
     * <p>
     * 当一个势力向另一个势力宣战时，将双方的关系标签设为 ENEMY。
     * </p>
     *
     * @param event 战争声明事件
     */
    private static void handleWarDeclared(FactionEventBus.WarDeclaredEvent event) {
        UUID aggressorId = event.getAggressorId();
        UUID targetId = event.getTargetId();

        // 将攻击方对目标的关系标签设为 ENEMY
        updateFactionRelationTag(aggressorId, targetId, RELATION_TAG_ENEMY);

        // 将目标对攻击方的关系标签设为 ENEMY
        updateFactionRelationTag(targetId, aggressorId, RELATION_TAG_ENEMY);
    }

    /**
     * 处理势力解散事件。
     * <p>
     * 当一个势力解散时，清除该势力所有成员的势力归属。
     * 这通过更新成员的记忆状态来实现。
     * </p>
     *
     * @param event 势力解散事件
     */
    private static void handleFactionDissolved(FactionEventBus.FactionDissolvedEvent event) {
        UUID dissolvedFaction = event.getFactionId();
        // 标记为已解散
        DISSOLVED_FACTIONS.add(dissolvedFaction);
        // 清理涉及该势力的所有关系映射
        for (Iterator<Map.Entry<UUID, Map<UUID, String>>> it = RELATION_MAP.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, Map<UUID, String>> outer = it.next();
            UUID source = outer.getKey();
            Map<UUID, String> inner = outer.getValue();
            if (source.equals(dissolvedFaction)) {
                it.remove();
            } else if (inner != null) {
                inner.remove(dissolvedFaction);
            }
        }
    }

    /**
     * 根据关系值确定关系标签。
     * <p>
     * 关系值范围：-100 到 +100
     * <ul>
     *   <li>关系值 &lt; -50：ENEMY（敌对）</li>
     *   <li>关系值 &gt;= -50 且 &lt;= 50：NEUTRAL（中立）</li>
     *   <li>关系值 &gt; 50：ALLY（盟友）</li>
     * </ul>
     * </p>
     *
     * @param relationValue 关系值
     * @return 关系标签
     */
    private static String determineRelationTag(int relationValue) {
        if (relationValue < RELATION_THRESHOLD_ENEMY) {
            return RELATION_TAG_ENEMY;
        } else if (relationValue > RELATION_THRESHOLD_ALLY) {
            return RELATION_TAG_ALLY;
        } else {
            return RELATION_TAG_NEUTRAL;
        }
    }

    /**
     * 更新一个势力的所有成员对另一个势力的关系标签。
     * <p>
     * 该方法通过遍历世界中的所有 RogueEntity，找到属于 sourceFaction 的实体，
     * 并更新其记忆中对 targetFaction 的关系标签。
     * </p>
     * <p>
     * 注意：本方法需要访问 ServerLevel 来查找实体。
     * 由于事件处理器中没有 ServerLevel，实际的实体更新应在 RogueEntity.tick() 中完成。
     * 本处理器只负责更新 FactionWorldData 中的状态。
     * </p>
     *
     * @param sourceFaction 源势力 UUID
     * @param targetFaction 目标势力 UUID
     * @param relationTag 关系标签
     */
    private static void updateFactionRelationTag(
        UUID sourceFaction,
        UUID targetFaction,
        String relationTag
    ) {
        // 如果该源/目标势力已解散，跳过更新
        if (DISSOLVED_FACTIONS.contains(sourceFaction) || DISSOLVED_FACTIONS.contains(targetFaction)) {
            return;
        }
        // 记录到关系表中，供后续测试查询
        Map<UUID, String> inner = RELATION_MAP.computeIfAbsent(sourceFaction, k -> new HashMap<>());
        inner.put(targetFaction, relationTag);
    }

    // ----------------- 测试友好接口 -----------------
    /**
     * 查询源势力对目标势力的关系标签（若不存在则返回 null）。
     */
    public static String queryRelationTag(UUID sourceFaction, UUID targetFaction) {
        Map<UUID, String> inner = RELATION_MAP.get(sourceFaction);
        return inner != null ? inner.get(targetFaction) : null;
    }

    /**
     * 判断某势力是否已经被解散。
     */
    public static boolean isFactionDissolved(UUID faction) {
        return DISSOLVED_FACTIONS.contains(faction);
    }

    /**
     * 将内部状态重置为初始状态，供测试用。
     */
    public static void resetState() {
        RELATION_MAP.clear();
        DISSOLVED_FACTIONS.clear();
        registered = false;
    }
}
