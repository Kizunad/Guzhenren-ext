package com.Kizunad.customNPCs.ai.personality;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 情绪集中式管理器。
 * <p>
 * 设计目标：
 * 1) 统一写入接口：外部模块只通过本类写入情绪变化，避免在各处直接操作情绪存储；
 * 2) 统一衰减逻辑：由本类在 tick 中负责自然衰减，避免各处重复实现衰减；
 * 3) 统一触发入口：提供变化监听器接口，便于后续在阈值变化时触发行为/记忆/状态输出等副作用。
 * <p>
 * 约定：
 * - 情绪值范围固定为 [0, 1]；
 * - 序列化格式与旧实现保持一致：NBT key 使用 {@link EmotionType#getId()}。
 */
public class EmotionManager {
    /**
     * 情绪变化的来源，用于调试与后续扩展。
     */
    public enum Cause {
        DAMAGE,
        DECAY,
        SCRIPT,
        UNKNOWN
    }

    /**
     * 一次情绪变化的不可变快照。
     * @param emotion 情绪类型
     * @param beforeValue 变化前值
     * @param afterValue 变化后值
     * @param cause 变化来源
     */
    public record Change(
        EmotionType emotion,
        float beforeValue,
        float afterValue,
        Cause cause
    ) {}

    /**
     * 情绪变化监听器：用于集中式触发后续逻辑（例如写入记忆、刷新状态、触发中断等）。
     */
    @FunctionalInterface
    public interface Listener {
        void onEmotionChanged(Change change);
    }

    private static final float MIN_VALUE = 0;
    private static final float MAX_VALUE = 1;

    /**
     * 默认完整衰减耗时（ticks）。
     * <p>
     * 该值用于保持与旧实现一致：从 1 衰减到 0 约需 1000 ticks（约 50 秒）。
     */
    private static final int DEFAULT_FULL_DECAY_TICKS = 1000;

    private static final float DEFAULT_DECAY_RATE_PER_TICK =
        0.001f;

    private final EnumMap<EmotionType, Float> emotions;
    private final List<Listener> listeners;

    private float decayRatePerTick;

    public EmotionManager() {
        emotions = new EnumMap<>(EmotionType.class);
        listeners = new ArrayList<>();
        decayRatePerTick = DEFAULT_DECAY_RATE_PER_TICK;

        for (EmotionType emotion : EmotionType.values()) {
            emotions.put(emotion, MIN_VALUE);
        }
    }

    /**
     * 每 tick 更新：情绪自然衰减。
     * <p>
     * 为避免高频 tick 下的额外对象分配，本方法在没有监听器时走无事件分支。
     */
    public void tick() {
        if (listeners.isEmpty()) {
            decayWithoutEvents();
            return;
        }

        for (EmotionType emotion : EmotionType.values()) {
            float current = emotions.get(emotion);
            if (current <= MIN_VALUE) {
                continue;
            }

            float next = Math.max(MIN_VALUE, current - decayRatePerTick);
            if (next == current) {
                continue;
            }

            emotions.put(emotion, next);
            notifyListeners(new Change(emotion, current, next, Cause.DECAY));
        }
    }

    /**
     * 以增量方式写入情绪变化。
     *
     * @param emotion 情绪类型
     * @param delta 变化量（允许为负，最终会被 clamp 到 [0,1]）
     * @param cause 变化来源
     */
    public void applyDelta(EmotionType emotion, float delta, Cause cause) {
        float beforeValue = emotions.get(emotion);
        float afterValue = clampValue(beforeValue + delta);
        if (afterValue == beforeValue) {
            return;
        }

        emotions.put(emotion, afterValue);
        notifyListeners(new Change(emotion, beforeValue, afterValue, cause));
    }

    /**
     * 直接设置情绪值。
     *
     * @param emotion 情绪类型
     * @param value 目标值（会被 clamp 到 [0,1]）
     * @param cause 变化来源
     */
    public void set(EmotionType emotion, float value, Cause cause) {
        float beforeValue = emotions.get(emotion);
        float afterValue = clampValue(value);
        if (afterValue == beforeValue) {
            return;
        }

        emotions.put(emotion, afterValue);
        notifyListeners(new Change(emotion, beforeValue, afterValue, cause));
    }

    /**
     * 获取情绪值（范围 [0,1]）。
     */
    public float get(EmotionType emotion) {
        return emotions.get(emotion);
    }

    /**
     * 设置每 tick 衰减速率（>= 0）。
     * <p>
     * 该接口主要用于配置化/测试；当前默认值用于保持与旧行为一致。
     */
    public void setDecayRatePerTick(float decayRatePerTick) {
        this.decayRatePerTick = Math.max(MIN_VALUE, decayRatePerTick);
    }

    /**
     * 注册监听器：用于集中式触发后续行为。
     */
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * 取消监听器注册。
     * @return 若存在并成功移除则为 true
     */
    public boolean removeListener(Listener listener) {
        return listeners.remove(listener);
    }

    /**
     * 序列化情绪数据。
     * <p>
     * 只关心情绪键值，不携带 decay/监听器等运行态信息。
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (EmotionType emotion : EmotionType.values()) {
            tag.putFloat(emotion.getId(), emotions.get(emotion));
        }
        return tag;
    }

    /**
     * 反序列化情绪数据（兼容缺失字段，缺失按 0 处理）。
     */
    public void deserializeNBT(CompoundTag tag) {
        for (EmotionType emotion : EmotionType.values()) {
            float value = MIN_VALUE;
            if (tag.contains(emotion.getId(), Tag.TAG_FLOAT)) {
                value = tag.getFloat(emotion.getId());
            }
            emotions.put(emotion, clampValue(value));
        }
    }

    private void decayWithoutEvents() {
        for (EmotionType emotion : EmotionType.values()) {
            float current = emotions.get(emotion);
            if (current <= MIN_VALUE) {
                continue;
            }
            emotions.put(emotion, Math.max(MIN_VALUE, current - decayRatePerTick));
        }
    }

    private static float clampValue(float value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }

    private void notifyListeners(Change change) {
        if (listeners.isEmpty()) {
            return;
        }

        for (Listener listener : listeners) {
            listener.onEmotionChanged(change);
        }
    }
}
