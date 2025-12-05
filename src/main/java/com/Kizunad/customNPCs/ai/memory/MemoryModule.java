package com.Kizunad.customNPCs.ai.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * 记忆模块 - 管理 NPC 的短期和长期记忆
 * <p>
 * 短期记忆：有过期时间的临时信息（如"上次看到敌人的位置"）
 * 长期记忆：持久化信息（如"宗门位置"、"对玩家的好感度"）
 */
public class MemoryModule {

    private final Map<String, MemoryEntry> shortTermMemory;
    private final Map<String, MemoryEntry> longTermMemory;

    public MemoryModule() {
        this.shortTermMemory = new ConcurrentHashMap<>();
        this.longTermMemory = new HashMap<>();
    }

    public Map<String, MemoryEntry> getShortTermMemory() {
        return shortTermMemory;
    }

    public Map<String, MemoryEntry> getLongTermMemory() {
        return longTermMemory;
    }

    /**
     * 存储短期记忆
     * @param key 记忆键
     * @param value 记忆值
     * @param expiryTicks 过期时间（游戏 tick）
     */
    public void rememberShortTerm(String key, Object value, int expiryTicks) {
        shortTermMemory.put(key, new MemoryEntry(value, expiryTicks));
    }

    /**
     * 存储长期记忆
     * @param key 记忆键
     * @param value 记忆值
     */
    public void rememberLongTerm(String key, Object value) {
        longTermMemory.put(key, new MemoryEntry(value, -1)); // -1 表示永不过期
    }

    /**
     * 获取记忆（先查短期，再查长期）
     * @param key 记忆键
     * @return 记忆值，如果不存在返回 null
     */
    public Object recall(String key) {
        MemoryEntry entry = shortTermMemory.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        entry = longTermMemory.get(key);
        if (entry != null) {
            return entry.getValue();
        }

        return null;
    }

    /**
     * 检查是否有指定记忆
     * @param key 记忆键
     * @return 是否存在且未过期
     */
    public boolean hasMemory(String key) {
        return recall(key) != null;
    }

    /**
     * 获取记忆值（recall 的别名）
     * @param key 记忆键
     * @return 记忆值
     */
    public Object getMemory(String key) {
        return recall(key);
    }

    /**
     * 获取记忆值（带类型转换）
     * @param key 记忆键
     * @param type 目标类型
     * @return 记忆值，如果不存在或类型不匹配则返回 null
     * @param <T> 类型参数
     */
    @SuppressWarnings("unchecked")
    public <T> T getMemory(String key, Class<T> type) {
        Object value = recall(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 获取短期记忆值（带类型转换和默认值）
     * @param key 记忆键
     * @param type 值类型
     * @param defaultValue 默认值
     * @param <T> 类型
     * @return 记忆值
     */
    @SuppressWarnings("unchecked")
    public <T> T getShortTerm(String key, Class<T> type, T defaultValue) {
        Object value = recall(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    /**
     * 遗忘记忆
     * @param key 记忆键
     */
    public void forget(String key) {
        shortTermMemory.remove(key);
        longTermMemory.remove(key);
    }

    /**
     * 更新记忆系统（清理过期条目）
     * 应该在每个 tick 调用
     */
    public void tick() {
        shortTermMemory
            .entrySet()
            .removeIf(entry -> {
                entry.getValue().tick();
                return entry.getValue().isExpired();
            });
    }

    /**
     * 序列化到 NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        ListTag shortTermList = new ListTag();
        for (Map.Entry<
            String,
            MemoryEntry
        > entry : shortTermMemory.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("key", entry.getKey());
            entryTag.merge(entry.getValue().serializeNBT());
            shortTermList.add(entryTag);
        }
        tag.put("shortTerm", shortTermList);

        ListTag longTermList = new ListTag();
        for (Map.Entry<String, MemoryEntry> entry : longTermMemory.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("key", entry.getKey());
            entryTag.merge(entry.getValue().serializeNBT());
            longTermList.add(entryTag);
        }
        tag.put("longTerm", longTermList);

        return tag;
    }

    /**
     * 从 NBT 反序列化
     */
    public void deserializeNBT(CompoundTag tag) {
        shortTermMemory.clear();
        longTermMemory.clear();

        ListTag shortTermList = tag.getList("shortTerm", Tag.TAG_COMPOUND);
        for (int i = 0; i < shortTermList.size(); i++) {
            CompoundTag entryTag = shortTermList.getCompound(i);
            String key = entryTag.getString("key");
            MemoryEntry entry = MemoryEntry.fromNBT(entryTag);
            shortTermMemory.put(key, entry);
        }

        ListTag longTermList = tag.getList("longTerm", Tag.TAG_COMPOUND);
        for (int i = 0; i < longTermList.size(); i++) {
            CompoundTag entryTag = longTermList.getCompound(i);
            String key = entryTag.getString("key");
            MemoryEntry entry = MemoryEntry.fromNBT(entryTag);
            longTermMemory.put(key, entry);
        }
    }
}