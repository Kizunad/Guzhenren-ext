package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 玩家念头鉴定解锁记录。
 * 存储玩家已经鉴定过的物品 ID，以及当前正在进行的鉴定进程。
 */
public class NianTouUnlocks implements INBTSerializable<CompoundTag> {

    private static final String ALL_USAGE_TOKEN = "*";

    private final Map<ResourceLocation, Set<String>> unlockedUsages = new HashMap<>();
    private UnlockProcess currentProcess = null;

    // --- Process Logic ---

    public void startProcess(
        ResourceLocation itemId,
        String usageId,
        int totalTicks,
        int totalCost
    ) {
        String normalizedUsage = normalizeUsageId(usageId);
        int clampedTicks = Math.max(1, totalTicks);
        int clampedCost = Math.max(0, totalCost);
        this.currentProcess = new UnlockProcess(
            itemId,
            normalizedUsage,
            clampedTicks,
            clampedTicks,
            clampedCost
        );
    }

    /**
     * 兼容旧调用，默认表示整条物品。
     */
    public void startProcess(ResourceLocation itemId, int totalTicks, int totalCost) {
        startProcess(itemId, ALL_USAGE_TOKEN, totalTicks, totalCost);
    }

    public UnlockProcess getCurrentProcess() {
        return currentProcess;
    }

    public void clearProcess() {
        this.currentProcess = null;
    }

    public boolean isProcessing() {
        return currentProcess != null;
    }

    // --- Unlock Logic ---

    public void unlock(ResourceLocation item) {
        unlock(item, ALL_USAGE_TOKEN);
    }

    public void unlock(ResourceLocation item, String usageId) {
        if (item == null) {
            return;
        }
        Set<String> usages = unlockedUsages.computeIfAbsent(item, id ->
            new HashSet<>()
        );
        String normalizedUsage = normalizeUsageId(usageId);
        if (ALL_USAGE_TOKEN.equals(normalizedUsage)) {
            usages.clear();
            usages.add(ALL_USAGE_TOKEN);
            return;
        }
        if (usages.contains(ALL_USAGE_TOKEN)) {
            return;
        }
        usages.add(normalizedUsage);
    }

    public boolean isUsageUnlocked(ResourceLocation item, String usageId) {
        if (item == null || usageId == null) {
            return false;
        }
        Set<String> usages = unlockedUsages.get(item);
        if (usages == null) {
            return false;
        }
        return usages.contains(ALL_USAGE_TOKEN) || usages.contains(usageId);
    }

    public boolean isUnlocked(ResourceLocation item) {
        return unlockedUsages.containsKey(item);
    }

    public boolean isFullyUnlocked(ResourceLocation item, Collection<NianTouData.Usage> usages) {
        if (item == null || usages == null || usages.isEmpty()) {
            return false;
        }
        Set<String> unlocked = unlockedUsages.get(item);
        if (unlocked == null || unlocked.isEmpty()) {
            return false;
        }
        if (unlocked.contains(ALL_USAGE_TOKEN)) {
            return true;
        }
        for (var usage : usages) {
            if (usage == null) {
                continue;
            }
            if (!unlocked.contains(usage.usageID())) {
                return false;
            }
        }
        return true;
    }

    public Set<ResourceLocation> getUnlockedItems() {
        return new HashSet<>(unlockedUsages.keySet());
    }

    public Map<ResourceLocation, Set<String>> getUnlockedUsageMap() {
        Map<ResourceLocation, Set<String>> copy = new HashMap<>();
        for (Map.Entry<ResourceLocation, Set<String>> entry : unlockedUsages.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    public void setUnlockedUsageMap(Map<ResourceLocation, Set<String>> entries) {
        unlockedUsages.clear();
        if (entries == null) {
            return;
        }
        for (Map.Entry<ResourceLocation, Set<String>> entry : entries.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            unlockedUsages.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    /**
     * 兼容旧逻辑：认为整个物品均已解锁。
     */
    public void setUnlockedItems(Set<ResourceLocation> items) {
        unlockedUsages.clear();
        if (items == null) {
            return;
        }
        for (ResourceLocation id : items) {
            unlock(id);
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<ResourceLocation, Set<String>> entry : unlockedUsages.entrySet()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putString("item", entry.getKey().toString());
            ListTag usages = new ListTag();
            for (String usageId : entry.getValue()) {
                usages.add(StringTag.valueOf(usageId));
            }
            itemTag.put("usages", usages);
            list.add(itemTag);
        }
        tag.put("unlocked", list);

        if (currentProcess != null) {
            CompoundTag processTag = new CompoundTag();
            processTag.putString("id", currentProcess.itemId.toString());
            processTag.putInt("remaining", currentProcess.remainingTicks);
            processTag.putInt("total", currentProcess.totalTicks);
            processTag.putInt("cost", currentProcess.totalCost);
            processTag.putString("usage", currentProcess.usageId);
            tag.put("process", processTag);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        unlockedUsages.clear();
        if (tag.contains("unlocked", Tag.TAG_LIST)) {
            ListTag compoundList = tag.getList("unlocked", Tag.TAG_COMPOUND);
            if (!compoundList.isEmpty()) {
                for (Tag entry : compoundList) {
                    if (!(entry instanceof CompoundTag itemTag) || !itemTag.contains("item")) {
                        continue;
                    }
                    ResourceLocation itemId = ResourceLocation.parse(itemTag.getString("item"));
                    ListTag usages = itemTag.getList("usages", Tag.TAG_STRING);
                    if (usages.isEmpty()) {
                        unlock(itemId);
                        continue;
                    }
                    for (Tag usageTag : usages) {
                        unlock(itemId, usageTag.getAsString());
                    }
                }
            } else {
                ListTag legacyList = tag.getList("unlocked", Tag.TAG_STRING);
                for (Tag t : legacyList) {
                    unlock(ResourceLocation.parse(t.getAsString()));
                }
            }
        }

        if (tag.contains("process", Tag.TAG_COMPOUND)) {
            CompoundTag processTag = tag.getCompound("process");
            this.currentProcess = new UnlockProcess(
                ResourceLocation.parse(processTag.getString("id")),
                normalizeUsageId(processTag.getString("usage")),
                processTag.getInt("remaining"),
                processTag.getInt("total"),
                processTag.getInt("cost")
            );
        } else {
            this.currentProcess = null;
        }
    }

    /**
     * 鉴定进程数据结构
     */
    public static class UnlockProcess {
        public final ResourceLocation itemId;
        public final String usageId;
        public int remainingTicks;
        public final int totalTicks;
        public final int totalCost;

        public UnlockProcess(
            ResourceLocation itemId,
            String usageId,
            int remainingTicks,
            int totalTicks,
            int totalCost
        ) {
            this.itemId = itemId;
            this.usageId = usageId;
            this.remainingTicks = remainingTicks;
            this.totalTicks = totalTicks;
            this.totalCost = totalCost;
        }
    }

    private static String normalizeUsageId(String usageId) {
        if (usageId == null || usageId.isBlank()) {
            return ALL_USAGE_TOKEN;
        }
        return usageId;
    }

    public boolean hasLockedUsage(ResourceLocation itemId, Collection<NianTouData.Usage> usages) {
        if (itemId == null || usages == null || usages.isEmpty()) {
            return false;
        }
        Set<String> unlocked = unlockedUsages.get(itemId);
        if (unlocked == null || unlocked.isEmpty()) {
            return true;
        }
        if (unlocked.contains(ALL_USAGE_TOKEN)) {
            return false;
        }
        for (var usage : usages) {
            if (usage == null) {
                continue;
            }
            if (!unlocked.contains(usage.usageID())) {
                return true;
            }
        }
        return false;
    }
}
