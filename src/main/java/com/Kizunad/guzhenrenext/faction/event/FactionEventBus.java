package com.Kizunad.guzhenrenext.faction.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 势力事件总线。
 * <p>
 * 本类为势力系统内部事件总线（非 NeoForge 事件系统），支持事件发布和订阅。
 * 事件用于触发 AI 行为和 UI 更新。
 * </p>
 * <p>
 * 使用单例模式，通过 {@link #INSTANCE} 访问。
 * </p>
 */
public final class FactionEventBus {

    /**
     * 单例实例。
     */
    public static final FactionEventBus INSTANCE = new FactionEventBus();

    /**
     * 事件监听器存储：事件类型 -> 监听器列表。
     */
    private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

    /**
     * 私有构造器，防止外部实例化。
     */
    private FactionEventBus() {
    }

    /**
     * 清除所有监听器。
     * <p>
     * 仅用于测试环境，清除单例中的所有事件监听器。
     * </p>
     */
    public void clearAllListeners() {
        listeners.clear();
    }

    /**
     * 订阅事件。
     * <p>
     * 将监听器注册到指定事件类型。同一监听器可被多次订阅。
     * </p>
     *
     * @param <T> 事件类型
     * @param eventType 事件类型的 Class 对象
     * @param listener 监听器（Consumer）
     * @throws NullPointerException 如果 eventType 或 listener 为 null
     */
    public <T extends FactionEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        Objects.requireNonNull(eventType, "eventType 不能为空");
        Objects.requireNonNull(listener, "listener 不能为空");

        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * 取消订阅事件。
     * <p>
     * 从指定事件类型的监听器列表中移除监听器。
     * 如果监听器被多次订阅，仅移除第一个匹配项。
     * </p>
     *
     * @param <T> 事件类型
     * @param eventType 事件类型的 Class 对象
     * @param listener 监听器（Consumer）
     * @throws NullPointerException 如果 eventType 或 listener 为 null
     */
    public <T extends FactionEvent> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        Objects.requireNonNull(eventType, "eventType 不能为空");
        Objects.requireNonNull(listener, "listener 不能为空");

        List<Consumer<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listeners.remove(eventType);
            }
        }
    }

    /**
     * 发布事件。
     * <p>
     * 同步调用所有订阅该事件类型的监听器。
     * 如果监听器抛出异常，异常会传播给调用者。
     * </p>
     *
     * @param <T> 事件类型
     * @param event 事件对象
     * @throws NullPointerException 如果 event 为 null
     */
    @SuppressWarnings("unchecked")
    public <T extends FactionEvent> void post(T event) {
        Objects.requireNonNull(event, "event 不能为空");

        Class<?> eventType = event.getClass();
        List<Consumer<?>> eventListeners = listeners.get(eventType);

        if (eventListeners != null) {
            for (Consumer<?> listener : new ArrayList<>(eventListeners)) {
                ((Consumer<T>) listener).accept(event);
            }
        }
    }

    // ========== 事件基类 ==========

    /**
     * 势力事件基类。
     * <p>
     * 所有势力事件都应继承此类。
     * </p>
     */
    public abstract static class FactionEvent {
        /**
         * 事件发生的游戏 tick。
         */
        private final long timestamp;

        /**
         * 构造器。
         *
         * @param timestamp 事件发生的游戏 tick
         */
        protected FactionEvent(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * 获取事件发生的游戏 tick。
         *
         * @return 游戏 tick
         */
        public long getTimestamp() {
            return timestamp;
        }
    }

    // ========== 具体事件类 ==========

    /**
     * 势力创建事件。
     */
    public static final class FactionCreatedEvent extends FactionEvent {
        /**
         * 势力 UUID。
         */
        private final UUID factionId;

        /**
         * 势力名称。
         */
        private final String factionName;

        /**
         * 势力类型。
         */
        private final FactionType factionType;

        /**
         * 构造器。
         *
         * @param timestamp 事件发生的游戏 tick
         * @param factionId 势力 UUID
         * @param factionName 势力名称
         * @param factionType 势力类型
         */
        public FactionCreatedEvent(long timestamp, UUID factionId, String factionName, FactionType factionType) {
            super(timestamp);
            this.factionId = Objects.requireNonNull(factionId, "factionId 不能为空");
            this.factionName = Objects.requireNonNull(factionName, "factionName 不能为空");
            this.factionType = Objects.requireNonNull(factionType, "factionType 不能为空");
        }

        /**
         * 获取势力 UUID。
         *
         * @return 势力 UUID
         */
        public UUID getFactionId() {
            return factionId;
        }

        /**
         * 获取势力名称。
         *
         * @return 势力名称
         */
        public String getFactionName() {
            return factionName;
        }

        /**
         * 获取势力类型。
         *
         * @return 势力类型
         */
        public FactionType getFactionType() {
            return factionType;
        }
    }

    /**
     * 势力解散事件。
     */
    public static final class FactionDissolvedEvent extends FactionEvent {
        /**
         * 势力 UUID。
         */
        private final UUID factionId;

        /**
         * 构造器。
         *
         * @param timestamp 事件发生的游戏 tick
         * @param factionId 势力 UUID
         */
        public FactionDissolvedEvent(long timestamp, UUID factionId) {
            super(timestamp);
            this.factionId = Objects.requireNonNull(factionId, "factionId 不能为空");
        }

        /**
         * 获取势力 UUID。
         *
         * @return 势力 UUID
         */
        public UUID getFactionId() {
            return factionId;
        }
    }

    /**
     * 成员加入事件。
     */
    public static final class MemberJoinedEvent extends FactionEvent {
        /**
         * 势力 UUID。
         */
        private final UUID factionId;

        /**
         * 成员 UUID。
         */
        private final UUID memberId;

        /**
         * 成员角色。
         */
        private final MemberRole role;

        /**
         * 构造器。
         *
         * @param timestamp 事件发生的游戏 tick
         * @param factionId 势力 UUID
         * @param memberId 成员 UUID
         * @param role 成员角色
         */
        public MemberJoinedEvent(long timestamp, UUID factionId, UUID memberId, MemberRole role) {
            super(timestamp);
            this.factionId = Objects.requireNonNull(factionId, "factionId 不能为空");
            this.memberId = Objects.requireNonNull(memberId, "memberId 不能为空");
            this.role = Objects.requireNonNull(role, "role 不能为空");
        }

        /**
         * 获取势力 UUID。
         *
         * @return 势力 UUID
         */
        public UUID getFactionId() {
            return factionId;
        }

        /**
         * 获取成员 UUID。
         *
         * @return 成员 UUID
         */
        public UUID getMemberId() {
            return memberId;
        }

        /**
         * 获取成员角色。
         *
         * @return 成员角色
         */
        public MemberRole getRole() {
            return role;
        }
    }

    /**
     * 成员离开事件。
     */
    public static final class MemberLeftEvent extends FactionEvent {
        /**
         * 势力 UUID。
         */
        private final UUID factionId;

        /**
         * 成员 UUID。
         */
        private final UUID memberId;

        /**
         * 构造器。
         *
         * @param timestamp 事件发生的游戏 tick
         * @param factionId 势力 UUID
         * @param memberId 成员 UUID
         */
        public MemberLeftEvent(long timestamp, UUID factionId, UUID memberId) {
            super(timestamp);
            this.factionId = Objects.requireNonNull(factionId, "factionId 不能为空");
            this.memberId = Objects.requireNonNull(memberId, "memberId 不能为空");
        }

        /**
         * 获取势力 UUID。
         *
         * @return 势力 UUID
         */
        public UUID getFactionId() {
            return factionId;
        }

        /**
         * 获取成员 UUID。
         *
         * @return 成员 UUID
         */
        public UUID getMemberId() {
            return memberId;
        }
    }

    /**
     * 关系变化事件。
     */
    public static final class RelationChangedEvent extends FactionEvent {
        /**
         * 势力 A 的 UUID。
         */
        private final UUID factionA;

        /**
         * 势力 B 的 UUID。
         */
        private final UUID factionB;

        /**
         * 旧的关系值。
         */
        private final int oldValue;

        /**
         * 新的关系值。
         */
        private final int newValue;

        /**
         * 构造器。
         *
         * @param timestamp 事件发生的游戏 tick
         * @param factionA 势力 A 的 UUID
         * @param factionB 势力 B 的 UUID
         * @param oldValue 旧的关系值
         * @param newValue 新的关系值
         */
        public RelationChangedEvent(long timestamp, UUID factionA, UUID factionB, int oldValue, int newValue) {
            super(timestamp);
            this.factionA = Objects.requireNonNull(factionA, "factionA 不能为空");
            this.factionB = Objects.requireNonNull(factionB, "factionB 不能为空");
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        /**
         * 获取势力 A 的 UUID。
         *
         * @return 势力 A 的 UUID
         */
        public UUID getFactionA() {
            return factionA;
        }

        /**
         * 获取势力 B 的 UUID。
         *
         * @return 势力 B 的 UUID
         */
        public UUID getFactionB() {
            return factionB;
        }

        /**
         * 获取旧的关系值。
         *
         * @return 旧的关系值
         */
        public int getOldValue() {
            return oldValue;
        }

        /**
         * 获取新的关系值。
         *
         * @return 新的关系值
         */
        public int getNewValue() {
            return newValue;
        }
    }

    /**
     * 战争声明事件。
     */
    public static final class WarDeclaredEvent extends FactionEvent {
        /**
         * 发起方势力 UUID。
         */
        private final UUID aggressorId;

        /**
         * 目标势力 UUID。
         */
        private final UUID targetId;

        /**
         * 构造器。
         *
         * @param timestamp 事件发生的游戏 tick
         * @param aggressorId 发起方势力 UUID
         * @param targetId 目标势力 UUID
         */
        public WarDeclaredEvent(long timestamp, UUID aggressorId, UUID targetId) {
            super(timestamp);
            this.aggressorId = Objects.requireNonNull(aggressorId, "aggressorId 不能为空");
            this.targetId = Objects.requireNonNull(targetId, "targetId 不能为空");
        }

        /**
         * 获取发起方势力 UUID。
         *
         * @return 发起方势力 UUID
         */
        public UUID getAggressorId() {
            return aggressorId;
        }

        /**
         * 获取目标势力 UUID。
         *
         * @return 目标势力 UUID
         */
        public UUID getTargetId() {
            return targetId;
        }
    }

    // ========== 枚举定义 ==========

    /**
     * 势力类型枚举。
     */
    public enum FactionType {
        /**
         * 宗门。
         */
        SECT,

        /**
         * 家族。
         */
        CLAN,

        /**
         * 散修群体。
         */
        ROGUE_GROUP
    }

    /**
     * 成员角色枚举。
     */
    public enum MemberRole {
        /**
         * 掌门/族长。
         */
        LEADER,

        /**
         * 长老。
         */
        ELDER,

        /**
         * 弟子/成员。
         */
        MEMBER,

        /**
         * 外门弟子。
         */
        OUTER_DISCIPLE
    }
}
