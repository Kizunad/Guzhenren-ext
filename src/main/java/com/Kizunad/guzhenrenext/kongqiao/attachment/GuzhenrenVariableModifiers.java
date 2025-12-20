package com.Kizunad.guzhenrenext.kongqiao.attachment;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 蛊真人变量临时修饰器存储。
 * <p>
 * 用途：为“高转被动效果”提供对 GuzhenrenModVariables 中部分字段的临时上限/容量增益，
 * 且能在多个效果叠加时正确相加、正确撤销。
 * </p>
 * <p>
 * 设计约束（KISS）：仅存储“变量键 + UsageID + 加成值”，不引入复杂的基线缓存。
 * 基线值由服务端在每次写入时通过 {@code current - oldSum} 推导，以适配玩家成长导致的原值变化。
 * </p>
 */
public class GuzhenrenVariableModifiers
    implements INBTSerializable<CompoundTag> {

    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_VAR = "Var";
    private static final String TAG_USAGE = "Usage";
    private static final String TAG_AMOUNT = "Amount";

    private final Map<String, Map<String, Double>> modifiersByVar =
        new HashMap<>();

    public GuzhenrenVariableModifiers() {}

    public double getSum(final String variableKey) {
        if (variableKey == null) {
            return 0.0;
        }
        final Map<String, Double> byUsage = modifiersByVar.get(variableKey);
        if (byUsage == null || byUsage.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : byUsage.values()) {
            sum += v;
        }
        return sum;
    }

    public void setModifier(
        final String variableKey,
        final String usageId,
        final double amount
    ) {
        if (variableKey == null || usageId == null) {
            return;
        }
        if (Double.compare(amount, 0.0) == 0) {
            removeModifier(variableKey, usageId);
            return;
        }
        modifiersByVar.computeIfAbsent(variableKey, k -> new HashMap<>())
            .put(usageId, amount);
    }

    public void removeModifier(final String variableKey, final String usageId) {
        if (variableKey == null || usageId == null) {
            return;
        }
        final Map<String, Double> byUsage = modifiersByVar.get(variableKey);
        if (byUsage == null) {
            return;
        }
        byUsage.remove(usageId);
        if (byUsage.isEmpty()) {
            modifiersByVar.remove(variableKey);
        }
    }

    @Override
    public CompoundTag serializeNBT(
        final net.minecraft.core.HolderLookup.Provider provider
    ) {
        final CompoundTag root = new CompoundTag();
        final ListTag list = new ListTag();

        for (Map.Entry<String, Map<String, Double>> varEntry : modifiersByVar
            .entrySet()) {
            final String var = varEntry.getKey();
            final Map<String, Double> byUsage = varEntry.getValue();
            if (byUsage == null || byUsage.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Double> usageEntry : byUsage.entrySet()) {
                final String usage = usageEntry.getKey();
                final double amount = usageEntry.getValue();
                final CompoundTag entry = new CompoundTag();
                entry.put(TAG_VAR, StringTag.valueOf(var));
                entry.put(TAG_USAGE, StringTag.valueOf(usage));
                entry.putDouble(TAG_AMOUNT, amount);
                list.add(entry);
            }
        }

        root.put(TAG_ENTRIES, list);
        return root;
    }

    @Override
    public void deserializeNBT(
        final net.minecraft.core.HolderLookup.Provider provider,
        final CompoundTag nbt
    ) {
        modifiersByVar.clear();
        if (nbt == null || !nbt.contains(TAG_ENTRIES)) {
            return;
        }
        final ListTag list = nbt.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
        for (Tag raw : list) {
            if (!(raw instanceof CompoundTag entry)) {
                continue;
            }
            final String var = entry.getString(TAG_VAR);
            final String usage = entry.getString(TAG_USAGE);
            final double amount = entry.getDouble(TAG_AMOUNT);
            if (var == null || var.isBlank() || usage == null || usage.isBlank()) {
                continue;
            }
            if (Double.compare(amount, 0.0) == 0) {
                continue;
            }
            modifiersByVar.computeIfAbsent(var, k -> new HashMap<>())
                .put(usage, amount);
        }
    }
}

