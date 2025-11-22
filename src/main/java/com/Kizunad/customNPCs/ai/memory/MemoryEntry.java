package com.Kizunad.customNPCs.ai.memory;

import net.minecraft.nbt.CompoundTag;

/**
 * 记忆条目 - 存储单个记忆及其元数据
 */
public class MemoryEntry {
    
    private final Object value;
    private final int maxAge; // -1 表示永不过期
    private int age; // 当前年龄（tick）
    
    public MemoryEntry(Object value, int maxAge) {
        this.value = value;
        this.maxAge = maxAge;
        this.age = 0;
    }
    
    private MemoryEntry(Object value, int maxAge, int age) {
        this.value = value;
        this.maxAge = maxAge;
        this.age = age;
    }
    
    /**
     * 每个 tick 调用
     */
    public void tick() {
        if (maxAge >= 0) {
            age++;
        }
    }
    
    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return maxAge >= 0 && age >= maxAge;
    }
    
    /**
     * 获取记忆值
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * 序列化到 NBT
     */
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("maxAge", maxAge);
        tag.putInt("age", age);
        
        // 简单处理：只存储字符串和数字
        if (value instanceof String) {
            tag.putString("type", "string");
            tag.putString("value", (String) value);
        } else if (value instanceof Integer) {
            tag.putString("type", "int");
            tag.putInt("value", (Integer) value);
        } else if (value instanceof Double) {
            tag.putString("type", "double");
            tag.putDouble("value", (Double) value);
        } else if (value instanceof Boolean) {
            tag.putString("type", "boolean");
            tag.putBoolean("value", (Boolean) value);
        } else {
            tag.putString("type", "unknown");
            tag.putString("value", value.toString());
        }
        
        return tag;
    }
    
    /**
     * 从 NBT 反序列化
     */
    public static MemoryEntry fromNBT(CompoundTag tag) {
        int maxAge = tag.getInt("maxAge");
        int age = tag.getInt("age");
        String type = tag.getString("type");
        
        Object value;
        switch (type) {
            case "string" -> value = tag.getString("value");
            case "int" -> value = tag.getInt("value");
            case "double" -> value = tag.getDouble("value");
            case "boolean" -> value = tag.getBoolean("value");
            default -> value = tag.getString("value");
        }
        
        return new MemoryEntry(value, maxAge, age);
    }
}
