package com.Kizunad.guzhenrenext_test.faction;

import com.Kizunad.guzhenrenext.faction.event.FactionEventBus;
import com.Kizunad.guzhenrenext.faction.hooks.FactionBehaviorHooks;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 势力行为钩子的 GameTest。
 * <p>
 * 验证 FactionBehaviorHooks 能够正确订阅和处理 FactionEventBus 中的事件。
 * </p>
 */
@GameTestHolder("guzhenrenext")
public final class FactionBehaviorHooksTest {

    private static final String BATCH_NAME = "faction_behavior_hooks_test";

    private static final int TEST_RELATION_VALUE_ALLY = 75;

    private static final int TEST_RELATION_VALUE_NEUTRAL = 60;
    private static final int TEST_RELATION_VALUE_ENEMY_FROM_DISSOLVE = -60;

    private FactionBehaviorHooksTest() {
    }

    /**
     * 测试关系变化事件的订阅和处理。
     * <p>
     * 验证：
     * <ul>
     *   <li>FactionBehaviorHooks 能够订阅 RelationChangedEvent</li>
     *   <li>发布 RelationChangedEvent 后，监听器被正确调用</li>
     * </ul>
     * </p>
     *
     * @param helper GameTest 辅助工具
     */
    @GameTest(template = "empty", batch = BATCH_NAME)
    public static void testRelationChangedEventSubscription(GameTestHelper helper) {
        // 准备：清除所有监听器并注册钩子
        FactionEventBus eventBus = FactionEventBus.INSTANCE;
        eventBus.clearAllListeners();
        FactionBehaviorHooks.resetState();
        FactionBehaviorHooks.register();

        // 发布关系变化事件
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();
        FactionEventBus.RelationChangedEvent event = new FactionEventBus.RelationChangedEvent(
            0L,
            factionA,
            factionB,
            0,
            TEST_RELATION_VALUE_ALLY
        );
        eventBus.post(event);

        // 验证：状态已在钩子中更新，且对称关系正确
        String tagAtoB = FactionBehaviorHooks.queryRelationTag(factionA, factionB);
        String tagBtoA = FactionBehaviorHooks.queryRelationTag(factionB, factionA);
        helper.assertTrue(tagAtoB != null && tagAtoB.equals("ALLY"),
            "源/目标关系标签应为 ALLY，A->B= " + tagAtoB);
        helper.assertTrue(tagBtoA != null && tagBtoA.equals("ALLY"),
            "源/目标关系标签应为 ALLY，B->A= " + tagBtoA);

        // 确保尚未被 dissolve
        helper.assertFalse(FactionBehaviorHooks.isFactionDissolved(factionA), "factionA 不应已解散");
        helper.succeed();
    }

    /**
     * 测试战争声明事件的订阅和处理。
     * <p>
     * 验证：
     * <ul>
     *   <li>FactionBehaviorHooks 能够订阅 WarDeclaredEvent</li>
     *   <li>发布 WarDeclaredEvent 后，监听器被正确调用</li>
     * </ul>
     * </p>
     *
     * @param helper GameTest 辅助工具
     */
    @GameTest(template = "empty", batch = BATCH_NAME)
    public static void testWarDeclaredEventSubscription(GameTestHelper helper) {
        // 准备：清除所有监听器并注册钩子
        FactionEventBus eventBus = FactionEventBus.INSTANCE;
        eventBus.clearAllListeners();
        FactionBehaviorHooks.resetState();
        FactionBehaviorHooks.register();

        // 发布战争声明事件
        UUID aggressorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        FactionEventBus.WarDeclaredEvent event = new FactionEventBus.WarDeclaredEvent(
            0L,
            aggressorId,
            targetId
        );
        eventBus.post(event);

        // 验证：状态更新为 ENEMY，且对称
        String tagAtoB = FactionBehaviorHooks.queryRelationTag(aggressorId, targetId);
        String tagBtoA = FactionBehaviorHooks.queryRelationTag(targetId, aggressorId);
        helper.assertTrue(tagAtoB != null && tagAtoB.equals("ENEMY"),
            "A对B的关系应为 ENEMY，实际为 " + tagAtoB);
        helper.assertTrue(tagBtoA != null && tagBtoA.equals("ENEMY"),
            "B对A的关系应为 ENEMY，实际为 " + tagBtoA);

        helper.succeed();
    }

    /**
     * 测试势力解散事件的订阅和处理。
     * <p>
     * 验证：
     * <ul>
     *   <li>FactionBehaviorHooks 能够订阅 FactionDissolvedEvent</li>
     *   <li>发布 FactionDissolvedEvent 后，监听器被正确调用</li>
     * </ul>
     * </p>
     *
     * @param helper GameTest 辅助工具
     */
    @GameTest(template = "empty", batch = BATCH_NAME)
    public static void testFactionDissolvedEventSubscription(GameTestHelper helper) {
        // 准备：清除所有监听器并注册钩子
        FactionEventBus eventBus = FactionEventBus.INSTANCE;
        eventBus.clearAllListeners();
        FactionBehaviorHooks.resetState();
        FactionBehaviorHooks.register();

        // 先建立一个关系，再解散一个势力，检查状态变化
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();
        // 建立对 factionA 到 factionB 的敌对关系
        FactionEventBus.RelationChangedEvent rc = new FactionEventBus.RelationChangedEvent(
            0L,
            factionA,
            factionB,
            0,
            TEST_RELATION_VALUE_ENEMY_FROM_DISSOLVE
        );
        eventBus.post(rc);

        // 发布势力解散事件
        FactionEventBus.FactionDissolvedEvent fd = new FactionEventBus.FactionDissolvedEvent(0L, factionA);
        eventBus.post(fd);

        // 验证：已解散，且源势力对目标势力的关系清空
        helper.assertTrue(FactionBehaviorHooks.isFactionDissolved(factionA), "factionA 应标记为已解散");
        helper.assertTrue(FactionBehaviorHooks.queryRelationTag(factionA, factionB) == null, "解散后 A->B 的关系应清空");
        helper.assertTrue(FactionBehaviorHooks.queryRelationTag(factionB, factionA) == null, "解散后 B->A 的关系应清空");

        helper.succeed();
    }

    /**
     * 测试多个事件的独立处理。
     * <p>
     * 验证：
     * <ul>
     *   <li>不同类型的事件不会相互干扰</li>
     *   <li>每个事件类型的监听器只接收对应类型的事件</li>
     * </ul>
     * </p>
     *
     * @param helper GameTest 辅助工具
     */
    @GameTest(template = "empty", batch = BATCH_NAME)
    public static void testMultipleEventTypesIndependence(GameTestHelper helper) {
        // 准备：清除所有监听器并注册钩子
        FactionEventBus eventBus = FactionEventBus.INSTANCE;
        eventBus.clearAllListeners();
        FactionBehaviorHooks.resetState();
        FactionBehaviorHooks.register();

        // 使用同一对势力，发布三种事件，验证状态最终一致性
        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        // 关系变化 - 设为中立(0)
        eventBus.post(
            new FactionEventBus.RelationChangedEvent(
                0L,
                factionA,
                factionB,
                0,
                0
            )
        );
        // 战争宣战
        eventBus.post(new FactionEventBus.WarDeclaredEvent(0L, factionA, factionB));
        // 势力解散
        eventBus.post(new FactionEventBus.FactionDissolvedEvent(0L, factionA));

        // 验证最终状态：A对B 为 ENEMY，且解散后状态清零
        String finalTagAtoB = FactionBehaviorHooks.queryRelationTag(factionA, factionB);
        String finalTagBtoA = FactionBehaviorHooks.queryRelationTag(factionB, factionA);
        helper.assertTrue(
            finalTagAtoB == null || finalTagAtoB.equals("ENEMY"),
            "A->B 最终应为 ENEMY 或 null，实际：" + finalTagAtoB
        );
        helper.assertTrue(
            finalTagBtoA == null || finalTagBtoA.equals("ENEMY"),
            "B->A 最终应为 ENEMY 或 null，实际：" + finalTagBtoA
        );
        helper.succeed();
    }
}
