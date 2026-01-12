package com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 飞剑冷却附件（Multi-cooldown）。
 * <p>
 * 用途：用字符串 Key → 剩余 tick 存储冷却倒计时。
 * </p>
 * <p>
 * Key 约定：建议使用 "guzhenrenext:flying_sword/<scope>/<name>" 形式，避免不同系统冲突。
 * </p>
 */
public class FlyingSwordCooldownAttachment implements INBTSerializable<CompoundTag> {

    private static final String ROOT = "cooldowns";

    private final Map<String, Integer> cooldowns = new HashMap<>();

    public int get(final String key) {
        if (key == null) {
            return 0;
        }
        return Math.max(0, cooldowns.getOrDefault(key, 0));
    }

    public void set(final String key, final int ticks) {
        if (key == null) {
            return;
        }
        final int v = Math.max(0, ticks);
        if (v == 0) {
            cooldowns.remove(key);
        } else {
            cooldowns.put(key, v);
        }
    }

    public int tickDown(final String key) {
        if (key == null) {
            return 0;
        }
        final Integer v = cooldowns.get(key);
        if (v == null || v <= 0) {
            return 0;
        }
        final int nv = v - 1;
        if (nv <= 0) {
            cooldowns.remove(key);
            return 0;
        }
        cooldowns.put(key, nv);
        return nv;
    }

    /**
     * 每 tick 递减所有冷却。
     */
    public void tickAll() {
        if (cooldowns.isEmpty()) {
            return;
        }
        final java.util.List<String> keys = new java.util.ArrayList<>(cooldowns.keySet());
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            final Integer v = cooldowns.get(key);
            if (v == null || v <= 1) {
                cooldowns.remove(key);
                continue;
            }
            cooldowns.put(key, v - 1);
        }
    }

    public void clear() {
        cooldowns.clear();
    }

    @Override
    public CompoundTag serializeNBT(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        final CompoundTag map = new CompoundTag();
        for (var entry : cooldowns.entrySet()) {
            final String k = Objects.requireNonNull(entry.getKey());
            final int v = Math.max(0, entry.getValue() == null ? 0 : entry.getValue());
            if (v > 0) {
                map.putInt(k, v);
            }
        }
        tag.put(ROOT, map);
        return tag;
    }

    @Override
    public void deserializeNBT(final HolderLookup.Provider provider, final CompoundTag tag) {
        cooldowns.clear();
        if (tag == null) {
            return;
        }
        final CompoundTag map = tag.getCompound(ROOT);
        for (String key : map.getAllKeys()) {
            final int v = Math.max(0, map.getInt(key));
            if (v > 0) {
                cooldowns.put(key, v);
            }
        }
    }
}
