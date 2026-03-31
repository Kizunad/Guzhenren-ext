package com.Kizunad.guzhenrenext.faction.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * FactionEventBus 单元测试。
 * <p>
 * 测试事件总线的订阅、取消订阅、发布功能。
 * </p>
 */
final class FactionEventBusTest {

    /**
     * 事件总线实例。
     */
    private FactionEventBus eventBus;

    /**
     * 测试用的势力 UUID。
     */
    private UUID testFactionId;

    /**
     * 测试用的成员 UUID。
     */
    private UUID testMemberId;

    /**
     * 测试用的游戏 tick。
     */
    private static final long TEST_TIMESTAMP = 1000L;

    /**
     * 测试用的势力名称。
     */
    private static final String TEST_FACTION_NAME = "测试势力";

    /**
     * 测试用的关系值变化。
     */
    private static final int OLD_RELATION_VALUE = 0;

    /**
     * 测试用的新关系值。
     */
    private static final int NEW_RELATION_VALUE = 50;

    private static final int MEMBER_ROLE_COUNT = 4;

    private static final int FACTION_TYPE_COUNT = 3;

    private static final int EVENT_COUNT_AFTER_UNSUBSCRIBE = 3;

    private static final long CUSTOM_TIMESTAMP = 5000L;

    /**
     * 初始化测试环境。
     */
    @BeforeEach
    void setUp() {
        eventBus = FactionEventBus.INSTANCE;
        eventBus.clearAllListeners();
        testFactionId = UUID.randomUUID();
        testMemberId = UUID.randomUUID();
    }

    /**
     * 测试：订阅和发布事件，验证监听器被调用。
     */
    @Test
    void testSubscribeAndPostEvent() {
        List<FactionEventBus.FactionCreatedEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, receivedEvents::add);

        FactionEventBus.FactionCreatedEvent event = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.SECT
        );

        eventBus.post(event);

        assertEquals(1, receivedEvents.size());
        assertEquals(testFactionId, receivedEvents.get(0).getFactionId());
        assertEquals(TEST_FACTION_NAME, receivedEvents.get(0).getFactionName());
        assertEquals(FactionEventBus.FactionType.SECT, receivedEvents.get(0).getFactionType());
        assertEquals(TEST_TIMESTAMP, receivedEvents.get(0).getTimestamp());
    }

    /**
     * 测试：多个监听器订阅同一事件。
     */
    @Test
    void testMultipleListenersForSameEvent() {
        List<FactionEventBus.FactionCreatedEvent> receivedEvents1 = new ArrayList<>();
        List<FactionEventBus.FactionCreatedEvent> receivedEvents2 = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, receivedEvents1::add);
        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, receivedEvents2::add);

        FactionEventBus.FactionCreatedEvent event = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.CLAN
        );

        eventBus.post(event);

        assertEquals(1, receivedEvents1.size());
        assertEquals(1, receivedEvents2.size());
        assertEquals(testFactionId, receivedEvents1.get(0).getFactionId());
        assertEquals(testFactionId, receivedEvents2.get(0).getFactionId());
    }

    /**
     * 测试：取消订阅后不再接收事件。
     */
    @Test
    void testUnsubscribeStopsReceivingEvents() {
        List<FactionEventBus.FactionCreatedEvent> receivedEvents = new ArrayList<>();
        java.util.function.Consumer<FactionEventBus.FactionCreatedEvent> listener = receivedEvents::add;

        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, listener);

        FactionEventBus.FactionCreatedEvent event1 = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.SECT
        );

        eventBus.post(event1);
        assertEquals(1, receivedEvents.size());

        eventBus.unsubscribe(FactionEventBus.FactionCreatedEvent.class, listener);

        FactionEventBus.FactionCreatedEvent event2 = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP + 1,
            UUID.randomUUID(),
            "另一个势力",
            FactionEventBus.FactionType.CLAN
        );

        eventBus.post(event2);
        assertEquals(1, receivedEvents.size());
    }

    /**
     * 测试：发布不同类型事件只触发对应监听器。
     */
    @Test
    void testDifferentEventTypesOnlyTriggerCorrespondingListeners() {
        List<FactionEventBus.FactionCreatedEvent> createdEvents = new ArrayList<>();
        List<FactionEventBus.FactionDissolvedEvent> dissolvedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, createdEvents::add);
        eventBus.subscribe(FactionEventBus.FactionDissolvedEvent.class, dissolvedEvents::add);

        FactionEventBus.FactionCreatedEvent createdEvent = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.SECT
        );

        FactionEventBus.FactionDissolvedEvent dissolvedEvent = new FactionEventBus.FactionDissolvedEvent(
            TEST_TIMESTAMP + 1,
            testFactionId
        );

        eventBus.post(createdEvent);
        eventBus.post(dissolvedEvent);

        assertEquals(1, createdEvents.size());
        assertEquals(1, dissolvedEvents.size());
    }

    /**
     * 测试：事件数据正确传递。
     */
    @Test
    void testEventDataCorrectlyPassed() {
        List<FactionEventBus.MemberJoinedEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.MemberJoinedEvent.class, receivedEvents::add);

        FactionEventBus.MemberJoinedEvent event = new FactionEventBus.MemberJoinedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            testMemberId,
            FactionEventBus.MemberRole.MEMBER
        );

        eventBus.post(event);

        assertEquals(1, receivedEvents.size());
        FactionEventBus.MemberJoinedEvent received = receivedEvents.get(0);
        assertEquals(testFactionId, received.getFactionId());
        assertEquals(testMemberId, received.getMemberId());
        assertEquals(FactionEventBus.MemberRole.MEMBER, received.getRole());
        assertEquals(TEST_TIMESTAMP, received.getTimestamp());
    }

    /**
     * 测试：MemberLeftEvent 事件数据传递。
     */
    @Test
    void testMemberLeftEventDataPassed() {
        List<FactionEventBus.MemberLeftEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.MemberLeftEvent.class, receivedEvents::add);

        FactionEventBus.MemberLeftEvent event = new FactionEventBus.MemberLeftEvent(
            TEST_TIMESTAMP,
            testFactionId,
            testMemberId
        );

        eventBus.post(event);

        assertEquals(1, receivedEvents.size());
        FactionEventBus.MemberLeftEvent received = receivedEvents.get(0);
        assertEquals(testFactionId, received.getFactionId());
        assertEquals(testMemberId, received.getMemberId());
        assertEquals(TEST_TIMESTAMP, received.getTimestamp());
    }

    /**
     * 测试：RelationChangedEvent 事件数据传递。
     */
    @Test
    void testRelationChangedEventDataPassed() {
        List<FactionEventBus.RelationChangedEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.RelationChangedEvent.class, receivedEvents::add);

        UUID factionA = UUID.randomUUID();
        UUID factionB = UUID.randomUUID();

        FactionEventBus.RelationChangedEvent event = new FactionEventBus.RelationChangedEvent(
            TEST_TIMESTAMP,
            factionA,
            factionB,
            OLD_RELATION_VALUE,
            NEW_RELATION_VALUE
        );

        eventBus.post(event);

        assertEquals(1, receivedEvents.size());
        FactionEventBus.RelationChangedEvent received = receivedEvents.get(0);
        assertEquals(factionA, received.getFactionA());
        assertEquals(factionB, received.getFactionB());
        assertEquals(OLD_RELATION_VALUE, received.getOldValue());
        assertEquals(NEW_RELATION_VALUE, received.getNewValue());
        assertEquals(TEST_TIMESTAMP, received.getTimestamp());
    }

    /**
     * 测试：WarDeclaredEvent 事件数据传递。
     */
    @Test
    void testWarDeclaredEventDataPassed() {
        List<FactionEventBus.WarDeclaredEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.WarDeclaredEvent.class, receivedEvents::add);

        UUID aggressorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        FactionEventBus.WarDeclaredEvent event = new FactionEventBus.WarDeclaredEvent(
            TEST_TIMESTAMP,
            aggressorId,
            targetId
        );

        eventBus.post(event);

        assertEquals(1, receivedEvents.size());
        FactionEventBus.WarDeclaredEvent received = receivedEvents.get(0);
        assertEquals(aggressorId, received.getAggressorId());
        assertEquals(targetId, received.getTargetId());
        assertEquals(TEST_TIMESTAMP, received.getTimestamp());
    }

    /**
     * 测试：FactionDissolvedEvent 事件数据传递。
     */
    @Test
    void testFactionDissolvedEventDataPassed() {
        List<FactionEventBus.FactionDissolvedEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.FactionDissolvedEvent.class, receivedEvents::add);

        FactionEventBus.FactionDissolvedEvent event = new FactionEventBus.FactionDissolvedEvent(
            TEST_TIMESTAMP,
            testFactionId
        );

        eventBus.post(event);

        assertEquals(1, receivedEvents.size());
        FactionEventBus.FactionDissolvedEvent received = receivedEvents.get(0);
        assertEquals(testFactionId, received.getFactionId());
        assertEquals(TEST_TIMESTAMP, received.getTimestamp());
    }

    /**
     * 测试：订阅 null 事件类型抛出异常。
     */
    @Test
    void testSubscribeWithNullEventTypeThrows() {
        assertThrows(NullPointerException.class, () -> {
            eventBus.subscribe(null, event -> {
            });
        });
    }

    /**
     * 测试：订阅 null 监听器抛出异常。
     */
    @Test
    void testSubscribeWithNullListenerThrows() {
        assertThrows(NullPointerException.class, () -> {
            eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, null);
        });
    }

    /**
     * 测试：发布 null 事件抛出异常。
     */
    @Test
    void testPostNullEventThrows() {
        assertThrows(NullPointerException.class, () -> {
            eventBus.post(null);
        });
    }

    /**
     * 测试：取消订阅 null 事件类型抛出异常。
     */
    @Test
    void testUnsubscribeWithNullEventTypeThrows() {
        assertThrows(NullPointerException.class, () -> {
            eventBus.unsubscribe(null, event -> {
            });
        });
    }

    /**
     * 测试：取消订阅 null 监听器抛出异常。
     */
    @Test
    void testUnsubscribeWithNullListenerThrows() {
        assertThrows(NullPointerException.class, () -> {
            eventBus.unsubscribe(FactionEventBus.FactionCreatedEvent.class, null);
        });
    }

    /**
     * 测试：创建事件时 null 参数抛出异常。
     */
    @Test
    void testFactionCreatedEventWithNullParametersThrows() {
        assertThrows(NullPointerException.class, () -> {
            new FactionEventBus.FactionCreatedEvent(TEST_TIMESTAMP, null, TEST_FACTION_NAME,
                FactionEventBus.FactionType.SECT);
        });

        assertThrows(NullPointerException.class, () -> {
            new FactionEventBus.FactionCreatedEvent(TEST_TIMESTAMP, testFactionId, null,
                FactionEventBus.FactionType.SECT);
        });

        assertThrows(NullPointerException.class, () -> {
            new FactionEventBus.FactionCreatedEvent(TEST_TIMESTAMP, testFactionId, TEST_FACTION_NAME, null);
        });
    }

    /**
     * 测试：成员加入事件时 null 参数抛出异常。
     */
    @Test
    void testMemberJoinedEventWithNullParametersThrows() {
        assertThrows(NullPointerException.class, () -> {
            new FactionEventBus.MemberJoinedEvent(TEST_TIMESTAMP, null, testMemberId,
                FactionEventBus.MemberRole.MEMBER);
        });

        assertThrows(NullPointerException.class, () -> {
            new FactionEventBus.MemberJoinedEvent(TEST_TIMESTAMP, testFactionId, null,
                FactionEventBus.MemberRole.MEMBER);
        });

        assertThrows(NullPointerException.class, () -> {
            new FactionEventBus.MemberJoinedEvent(TEST_TIMESTAMP, testFactionId, testMemberId, null);
        });
    }

    /**
     * 测试：所有成员角色枚举值。
     */
    @Test
    void testAllMemberRoleEnumValues() {
        FactionEventBus.MemberRole[] roles = FactionEventBus.MemberRole.values();
        assertEquals(MEMBER_ROLE_COUNT, roles.length);

        assertTrue(contains(roles, FactionEventBus.MemberRole.LEADER));
        assertTrue(contains(roles, FactionEventBus.MemberRole.ELDER));
        assertTrue(contains(roles, FactionEventBus.MemberRole.MEMBER));
        assertTrue(contains(roles, FactionEventBus.MemberRole.OUTER_DISCIPLE));
    }

    /**
     * 测试：所有势力类型枚举值。
     */
    @Test
    void testAllFactionTypeEnumValues() {
        FactionEventBus.FactionType[] types = FactionEventBus.FactionType.values();
        assertEquals(FACTION_TYPE_COUNT, types.length);

        assertTrue(contains(types, FactionEventBus.FactionType.SECT));
        assertTrue(contains(types, FactionEventBus.FactionType.CLAN));
        assertTrue(contains(types, FactionEventBus.FactionType.ROGUE_GROUP));
    }

    /**
     * 测试：事件总线单例。
     */
    @Test
    void testEventBusSingleton() {
        FactionEventBus instance1 = FactionEventBus.INSTANCE;
        FactionEventBus instance2 = FactionEventBus.INSTANCE;

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertEquals(instance1, instance2);
    }

    /**
     * 测试：多次订阅同一监听器。
     */
    @Test
    void testMultipleSubscriptionsOfSameListener() {
        List<FactionEventBus.FactionCreatedEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, receivedEvents::add);
        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, receivedEvents::add);

        FactionEventBus.FactionCreatedEvent event = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.SECT
        );

        eventBus.post(event);

        assertEquals(2, receivedEvents.size());
    }

    /**
     * 测试：取消订阅仅移除第一个匹配项。
     */
    @Test
    void testUnsubscribeRemovesOnlyFirstMatch() {
        List<FactionEventBus.FactionCreatedEvent> receivedEvents = new ArrayList<>();
        java.util.function.Consumer<FactionEventBus.FactionCreatedEvent> listener = receivedEvents::add;

        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, listener);
        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, listener);

        FactionEventBus.FactionCreatedEvent event1 = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.SECT
        );

        eventBus.post(event1);
        assertEquals(2, receivedEvents.size());

        eventBus.unsubscribe(FactionEventBus.FactionCreatedEvent.class, listener);

        FactionEventBus.FactionCreatedEvent event2 = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP + 1,
            UUID.randomUUID(),
            "另一个势力",
            FactionEventBus.FactionType.CLAN
        );

        eventBus.post(event2);
        assertEquals(EVENT_COUNT_AFTER_UNSUBSCRIBE, receivedEvents.size());
    }

    /**
     * 测试：取消订阅不存在的监听器不抛出异常。
     */
    @Test
    void testUnsubscribeNonExistentListenerDoesNotThrow() {
        eventBus.unsubscribe(FactionEventBus.FactionCreatedEvent.class, event -> {
        });
    }

    /**
     * 测试：发布事件到没有监听器的事件类型。
     */
    @Test
    void testPostEventWithNoListenersDoesNotThrow() {
        FactionEventBus.FactionCreatedEvent event = new FactionEventBus.FactionCreatedEvent(
            TEST_TIMESTAMP,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.SECT
        );

        eventBus.post(event);
    }

    /**
     * 测试：事件时间戳正确传递。
     */
    @Test
    void testEventTimestampCorrectlyPassed() {
        List<FactionEventBus.FactionCreatedEvent> receivedEvents = new ArrayList<>();

        eventBus.subscribe(FactionEventBus.FactionCreatedEvent.class, receivedEvents::add);

        long customTimestamp = CUSTOM_TIMESTAMP;
        FactionEventBus.FactionCreatedEvent event = new FactionEventBus.FactionCreatedEvent(
            customTimestamp,
            testFactionId,
            TEST_FACTION_NAME,
            FactionEventBus.FactionType.SECT
        );

        eventBus.post(event);

        assertEquals(customTimestamp, receivedEvents.get(0).getTimestamp());
    }

    /**
     * 辅助方法：检查数组是否包含元素。
     *
     * @param <T> 元素类型
     * @param array 数组
     * @param element 元素
     * @return 是否包含
     */
    private static <T> boolean contains(T[] array, T element) {
        for (T item : array) {
            if (item.equals(element)) {
                return true;
            }
        }
        return false;
    }
}
