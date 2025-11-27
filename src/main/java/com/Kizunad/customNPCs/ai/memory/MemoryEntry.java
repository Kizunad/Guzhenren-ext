package com.Kizunad.customNPCs.ai.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

/**
 * 记忆条目 - 存储单个记忆及其元数据
 */
public class MemoryEntry {

    private static final String TYPE_KEY = "type";
    private static final String VALUE_KEY = "value";

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
        SerializedValue serialized = serializeValue(value);
        tag.putString(TYPE_KEY, serialized.type());
        if (serialized.tag() != null) {
            tag.put(VALUE_KEY, serialized.tag());
        }

        return tag;
    }

    /**
     * 从 NBT 反序列化
     */
    public static MemoryEntry fromNBT(CompoundTag tag) {
        int maxAge = tag.getInt("maxAge");
        int age = tag.getInt("age");
        String type = tag.getString(TYPE_KEY);
        Tag valueTag = tag.get(VALUE_KEY);
        Object value = deserializeValue(type, valueTag);

        return new MemoryEntry(value, maxAge, age);
    }

    private static SerializedValue serializeValue(Object rawValue) {
        if (rawValue == null) {
            return new SerializedValue("null", new CompoundTag());
        }

        if (rawValue instanceof String value) {
            return new SerializedValue("string", StringTag.valueOf(value));
        }
        if (rawValue instanceof Integer value) {
            return new SerializedValue("int", IntTag.valueOf(value));
        }
        if (rawValue instanceof Long value) {
            return new SerializedValue("long", LongTag.valueOf(value));
        }
        if (rawValue instanceof Double value) {
            return new SerializedValue("double", DoubleTag.valueOf(value));
        }
        if (rawValue instanceof Float value) {
            return new SerializedValue("float", FloatTag.valueOf(value));
        }
        if (rawValue instanceof Boolean value) {
            return new SerializedValue(
                "boolean",
                ByteTag.valueOf((byte) (value ? 1 : 0))
            );
        }
        if (rawValue instanceof UUID value) {
            return new SerializedValue("uuid", NbtUtils.createUUID(value));
        }
        if (rawValue instanceof BlockPos value) {
            return new SerializedValue("block_pos", NbtUtils.writeBlockPos(value));
        }
        if (rawValue instanceof Vec3 value) {
            CompoundTag vecTag = new CompoundTag();
            vecTag.putDouble("x", value.x);
            vecTag.putDouble("y", value.y);
            vecTag.putDouble("z", value.z);
            return new SerializedValue("vec3", vecTag);
        }
        if (rawValue instanceof List<?> listValue) {
            ListTag listTag = new ListTag();
            for (Object element : listValue) {
                SerializedValue elementSerialized = serializeValue(element);
                CompoundTag elementTag = new CompoundTag();
                elementTag.putString(TYPE_KEY, elementSerialized.type());
                if (elementSerialized.tag() != null) {
                    elementTag.put(VALUE_KEY, elementSerialized.tag());
                }
                listTag.add(elementTag);
            }
            return new SerializedValue("list", listTag);
        }
        if (rawValue instanceof CompoundTag value) {
            return new SerializedValue("compound", value.copy());
        }

        return new SerializedValue(
            "string",
            StringTag.valueOf(String.valueOf(rawValue))
        );
    }

    private static Object deserializeValue(String type, Tag valueTag) {
        return switch (type) {
            case "string" -> valueTag != null ? valueTag.getAsString() : "";
            case "int" -> valueTag instanceof IntTag intTag ? intTag.getAsInt() : 0;
            case "long" -> valueTag instanceof LongTag longTag ? longTag.getAsLong() : 0L;
            case "double" -> valueTag instanceof DoubleTag doubleTag ? doubleTag.getAsDouble() : 0.0d;
            case "float" -> valueTag instanceof FloatTag floatTag ? floatTag.getAsFloat() : 0.0f;
            case "boolean" -> valueTag instanceof ByteTag byteTag && byteTag.getAsByte() != 0;
            case "uuid" -> valueTag instanceof IntArrayTag uuidTag ? NbtUtils.loadUUID(uuidTag) : null;
            case "block_pos" -> {
                if (valueTag instanceof CompoundTag posTag) {
                    yield new BlockPos(
                        posTag.getInt("X"),
                        posTag.getInt("Y"),
                        posTag.getInt("Z")
                    );
                }
                yield null;
            }
            case "vec3" -> {
                if (valueTag instanceof CompoundTag vecTag) {
                    yield new Vec3(
                        vecTag.getDouble("x"),
                        vecTag.getDouble("y"),
                        vecTag.getDouble("z")
                    );
                }
                yield Vec3.ZERO;
            }
            case "list" -> {
                List<Object> result = new ArrayList<>();
                if (valueTag instanceof ListTag listTag) {
                    for (Tag element : listTag) {
                        if (element instanceof CompoundTag compound) {
                            String elementType = compound.getString(TYPE_KEY);
                            Tag nested = compound.get(VALUE_KEY);
                            result.add(deserializeValue(elementType, nested));
                        }
                    }
                }
                yield result;
            }
            case "compound" -> valueTag instanceof CompoundTag compound
                ? compound.copy()
                : new CompoundTag();
            default -> valueTag != null ? valueTag.getAsString() : null;
        };
    }

    private record SerializedValue(String type, Tag tag) {}
}
