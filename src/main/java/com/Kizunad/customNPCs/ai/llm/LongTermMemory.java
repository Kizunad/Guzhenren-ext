package com.Kizunad.customNPCs.ai.llm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * LLM 专用的长期记忆存储。
 * <p>
 * 仅允许通过专用动作写入/删除，避免普通逻辑污染高阶策略记忆。
 */
public class LongTermMemory {

    private static final int MAX_VALUE_LENGTH = 512;
    private final Map<String, String> entries = new LinkedHashMap<>();

    /**
     * 写入/更新长期记忆。
     * @param key 记忆键
     * @param value 记忆内容
     */
    public void remember(String key, String value) {
        if (key == null || key.isBlank()) {
            return;
        }
        String sanitizedValue = value == null ? "" : value.trim();
        if (sanitizedValue.length() > MAX_VALUE_LENGTH) {
            sanitizedValue = sanitizedValue.substring(0, MAX_VALUE_LENGTH);
        }
        entries.put(key.trim(), sanitizedValue);
    }

    /**
     * 删除长期记忆。
     * @param key 记忆键
     */
    public void forget(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        entries.remove(key.trim());
    }

    /**
     * 读取长期记忆。
     * @param key 记忆键
     * @return 记忆内容或 null
     */
    public String recall(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return entries.get(key.trim());
    }

    /**
     * 不可变视图。
     */
    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * 简要摘要。
     * @param limit 条目数量上限
     * @return 文本摘要
     */
    public String summarize(int limit) {
        if (entries.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (count > 0) {
                sb.append("; ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            count++;
            if (count >= limit) {
                break;
            }
        }
        if (entries.size() > limit) {
            sb.append(" ... total ").append(entries.size());
        }
        return sb.toString();
    }

    /**
     * 序列化到 NBT。
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("key", entry.getKey());
            entryTag.putString("value", entry.getValue());
            list.add(entryTag);
        }
        tag.put("entries", list);
        return tag;
    }

    /**
     * 从 NBT 反序列化。
     */
    public void deserializeNBT(CompoundTag tag) {
        entries.clear();
        if (tag == null || !tag.contains("entries", Tag.TAG_LIST)) {
            return;
        }
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            String key = entryTag.getString("key");
            String value = entryTag.getString("value");
            if (key != null && !key.isBlank()) {
                remember(key, value);
            }
        }
    }
}
