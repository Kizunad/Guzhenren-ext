package com.Kizunad.guzhenrenext.entity.mind;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class EntityMemory {

    public static final int MAX_SHORT_TERM_MEMORIES = 20;

    public static final String DEFAULT_RELATION_TAG = "NEUTRAL";

    private static final String KEY_SHORT_TERM = "shortTerm";

    private static final String KEY_RELATIONS = "relations";

    private static final String KEY_EVENT = "event";

    private static final String KEY_ENTITY_ID = "entityId";

    private static final String KEY_TAG = "tag";

    private final LinkedList<String> shortTermMemories;

    private final Map<UUID, String> relationTags;

    public EntityMemory() {
        this.shortTermMemories = new LinkedList<>();
        this.relationTags = new HashMap<>();
    }

    public void addShortTermMemory(String event) {
        Objects.requireNonNull(event, "event 不能为空");
        shortTermMemories.addLast(event);
        while (shortTermMemories.size() > MAX_SHORT_TERM_MEMORIES) {
            shortTermMemories.removeFirst();
        }
    }

    public List<String> getShortTermMemories() {
        return List.copyOf(shortTermMemories);
    }

    public void setRelationTag(UUID entityId, String tag) {
        Objects.requireNonNull(entityId, "entityId 不能为空");
        Objects.requireNonNull(tag, "tag 不能为空");
        relationTags.put(entityId, tag);
    }

    public String getRelationTag(UUID entityId) {
        Objects.requireNonNull(entityId, "entityId 不能为空");
        return relationTags.getOrDefault(entityId, DEFAULT_RELATION_TAG);
    }

    public void removeRelationTag(UUID entityId) {
        Objects.requireNonNull(entityId, "entityId 不能为空");
        relationTags.remove(entityId);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag shortTermTag = new ListTag();
        ListTag relationsTag = new ListTag();

        for (String event : shortTermMemories) {
            CompoundTag eventTag = new CompoundTag();
            eventTag.putString(KEY_EVENT, event);
            shortTermTag.add(eventTag);
        }

        for (Map.Entry<UUID, String> entry : relationTags.entrySet()) {
            CompoundTag relationTag = new CompoundTag();
            relationTag.putUUID(KEY_ENTITY_ID, entry.getKey());
            relationTag.putString(KEY_TAG, entry.getValue());
            relationsTag.add(relationTag);
        }

        tag.put(KEY_SHORT_TERM, shortTermTag);
        tag.put(KEY_RELATIONS, relationsTag);
        return tag;
    }

    public void load(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag 不能为空");
        shortTermMemories.clear();
        relationTags.clear();

        if (tag.contains(KEY_SHORT_TERM, Tag.TAG_LIST)) {
            ListTag shortTermTag = tag.getList(KEY_SHORT_TERM, Tag.TAG_COMPOUND);
            for (int index = 0; index < shortTermTag.size(); index++) {
                CompoundTag eventTag = shortTermTag.getCompound(index);
                addShortTermMemory(eventTag.getString(KEY_EVENT));
            }
        }

        if (tag.contains(KEY_RELATIONS, Tag.TAG_LIST)) {
            ListTag relationsTag = tag.getList(KEY_RELATIONS, Tag.TAG_COMPOUND);
            for (int index = 0; index < relationsTag.size(); index++) {
                CompoundTag relationTag = relationsTag.getCompound(index);
                if (!relationTag.hasUUID(KEY_ENTITY_ID)) {
                    continue;
                }
                UUID entityId = relationTag.getUUID(KEY_ENTITY_ID);
                String loadedTag = relationTag.getString(KEY_TAG);
                relationTags.put(entityId, loadedTag.isEmpty() ? DEFAULT_RELATION_TAG : loadedTag);
            }
        }
    }
}
