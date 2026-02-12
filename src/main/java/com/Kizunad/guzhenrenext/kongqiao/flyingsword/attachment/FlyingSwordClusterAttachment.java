package com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class FlyingSwordClusterAttachment
    implements INBTSerializable<CompoundTag> {

    private static final String TAG_ACTIVE_SWORDS = "ActiveSwords";
    private static final String TAG_MAX_COMPUTATION = "MaxComputation";
    private static final String TAG_CURRENT_LOAD = "CurrentLoad";

    private final Set<UUID> activeSwords = new HashSet<>();

    private int maxComputation;

    private int currentLoad;

    public Set<UUID> getActiveSwords() {
        return new HashSet<>(activeSwords);
    }

    public boolean hasActiveSword(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return activeSwords.contains(uuid);
    }

    public boolean addActiveSword(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return activeSwords.add(uuid);
    }

    public boolean removeActiveSword(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        return activeSwords.remove(uuid);
    }

    public void clearActiveSwords() {
        activeSwords.clear();
    }

    public int getMaxComputation() {
        return maxComputation;
    }

    public void setMaxComputation(int maxComputation) {
        this.maxComputation = Math.max(0, maxComputation);
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(int currentLoad) {
        this.currentLoad = Math.max(0, currentLoad);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag activeList = new ListTag();
        for (UUID uuid : activeSwords) {
            if (uuid == null) {
                continue;
            }
            activeList.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put(TAG_ACTIVE_SWORDS, activeList);
        tag.putInt(TAG_MAX_COMPUTATION, maxComputation);
        tag.putInt(TAG_CURRENT_LOAD, currentLoad);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        activeSwords.clear();
        if (tag == null) {
            maxComputation = 0;
            currentLoad = 0;
            return;
        }

        if (tag.contains(TAG_ACTIVE_SWORDS, Tag.TAG_LIST)) {
            ListTag activeList = tag.getList(TAG_ACTIVE_SWORDS, Tag.TAG_STRING);
            for (int index = 0; index < activeList.size(); index++) {
                String value = activeList.getString(index);
                try {
                    activeSwords.add(UUID.fromString(value));
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
            }
        }

        if (tag.contains(TAG_MAX_COMPUTATION, Tag.TAG_INT)) {
            maxComputation = Math.max(0, tag.getInt(TAG_MAX_COMPUTATION));
        } else {
            maxComputation = 0;
        }

        if (tag.contains(TAG_CURRENT_LOAD, Tag.TAG_INT)) {
            currentLoad = Math.max(0, tag.getInt(TAG_CURRENT_LOAD));
        } else {
            currentLoad = 0;
        }
    }
}
