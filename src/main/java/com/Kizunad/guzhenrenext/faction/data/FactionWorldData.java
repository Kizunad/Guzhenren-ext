package com.Kizunad.guzhenrenext.faction.data;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership;
import com.Kizunad.guzhenrenext.faction.core.FactionRelationMatrix;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 势力系统全局数据。
 * <p>
 * 本数据对象负责：
 * 1) 持久化所有势力核心数据（FactionCore）
 * 2) 维护势力间关系矩阵（FactionRelationMatrix）
 * 3) 存储所有成员列表（FactionMembership）
 * 4) 提供 get(ServerLevel) 访问模式
 * </p>
 */
public class FactionWorldData extends SavedData {

    private static final String DATA_NAME = "guzhenrenext_faction_world";

    // ========== NBT 序列化常量 ==========

    private static final String KEY_FACTIONS = "factions";

    private static final String KEY_FACTION_ID = "factionId";

    private static final String KEY_FACTION_DATA = "factionData";

    private static final String KEY_RELATION_MATRIX = "relationMatrix";

    private static final String KEY_MEMBERSHIPS = "memberships";

    private static final String KEY_FACTION_MEMBERS = "factionMembers";

    private static final String KEY_MEMBER_DATA = "memberData";

    private static final int TAG_COMPOUND = Tag.TAG_COMPOUND;

    private static final int TAG_LIST = Tag.TAG_LIST;

    // ========== 内部存储 ==========

    /**
     * 所有势力数据。key 为势力 UUID，value 为 FactionCore。
     */
    private final Map<UUID, FactionCore> factions = new HashMap<>();

    /**
     * 势力间关系矩阵。
     */
    private final FactionRelationMatrix relationMatrix = new FactionRelationMatrix();

    /**
     * 成员列表。key 为势力 UUID，value 为该势力的所有成员列表。
     */
    private final Map<UUID, List<FactionMembership>> memberships = new HashMap<>();

    /**
     * 将势力数据序列化到 NBT。
     *
     * @param tag 目标 CompoundTag
     * @param registries HolderLookup.Provider（用于 Minecraft 1.20.1+ 兼容性）
     * @return 序列化后的 CompoundTag
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        // 序列化所有势力
        ListTag factionsList = new ListTag();
        for (Map.Entry<UUID, FactionCore> entry : factions.entrySet()) {
            CompoundTag factionTag = new CompoundTag();
            factionTag.putUUID(KEY_FACTION_ID, entry.getKey());
            factionTag.put(KEY_FACTION_DATA, entry.getValue().save());
            factionsList.add(factionTag);
        }
        tag.put(KEY_FACTIONS, factionsList);

        // 序列化关系矩阵
        tag.put(KEY_RELATION_MATRIX, relationMatrix.save());

        // 序列化成员列表
        ListTag membershipsList = new ListTag();
        for (Map.Entry<UUID, List<FactionMembership>> entry : memberships.entrySet()) {
            CompoundTag factionMembersTag = new CompoundTag();
            factionMembersTag.putUUID(KEY_FACTION_ID, entry.getKey());

            ListTag membersList = new ListTag();
            for (FactionMembership membership : entry.getValue()) {
                membersList.add(membership.save());
            }
            factionMembersTag.put(KEY_FACTION_MEMBERS, membersList);
            membershipsList.add(factionMembersTag);
        }
        tag.put(KEY_MEMBERSHIPS, membershipsList);

        return tag;
    }

    /**
     * 从 NBT 反序列化势力数据。
     *
     * @param tag 包含势力数据的 CompoundTag
     * @param provider HolderLookup.Provider（用于 Minecraft 1.20.1+ 兼容性）
     * @return 反序列化后的 FactionWorldData 实例
     */
    static FactionWorldData loadFromTag(CompoundTag tag, HolderLookup.Provider provider) {
        FactionWorldData data = new FactionWorldData();

        // 反序列化所有势力
        if (tag.contains(KEY_FACTIONS, TAG_LIST)) {
            ListTag factionsList = tag.getList(KEY_FACTIONS, TAG_COMPOUND);
            for (int i = 0; i < factionsList.size(); i++) {
                CompoundTag factionTag = factionsList.getCompound(i);
                if (!factionTag.hasUUID(KEY_FACTION_ID) || !factionTag.contains(KEY_FACTION_DATA, TAG_COMPOUND)) {
                    continue;
                }
                UUID factionId = factionTag.getUUID(KEY_FACTION_ID);
                FactionCore faction = FactionCore.load(factionTag.getCompound(KEY_FACTION_DATA));
                if (faction != null) {
                    data.factions.put(factionId, faction);
                }
            }
        }

        // 反序列化关系矩阵
        if (tag.contains(KEY_RELATION_MATRIX, TAG_COMPOUND)) {
            FactionRelationMatrix loadedMatrix = FactionRelationMatrix.load(tag.getCompound(KEY_RELATION_MATRIX));
            if (loadedMatrix != null) {
                // 复制加载的矩阵数据到当前实例
                for (UUID factionA : data.factions.keySet()) {
                    for (UUID factionB : data.factions.keySet()) {
                        if (!factionA.equals(factionB)) {
                            int relation = loadedMatrix.getRelation(factionA, factionB);
                            if (relation != 0) {
                                data.relationMatrix.setRelation(factionA, factionB, relation);
                            }
                        }
                    }
                }
            }
        }

        // 反序列化成员列表
        if (tag.contains(KEY_MEMBERSHIPS, TAG_LIST)) {
            ListTag membershipsList = tag.getList(KEY_MEMBERSHIPS, TAG_COMPOUND);
            for (int i = 0; i < membershipsList.size(); i++) {
                CompoundTag factionMembersTag = membershipsList.getCompound(i);
                if (!factionMembersTag.hasUUID(KEY_FACTION_ID)
                    || !factionMembersTag.contains(KEY_FACTION_MEMBERS, TAG_LIST)) {
                    continue;
                }
                UUID factionId = factionMembersTag.getUUID(KEY_FACTION_ID);
                ListTag membersList = factionMembersTag.getList(KEY_FACTION_MEMBERS, TAG_COMPOUND);

                List<FactionMembership> factionMembersList = new ArrayList<>();
                for (int j = 0; j < membersList.size(); j++) {
                    CompoundTag memberTag = membersList.getCompound(j);
                    FactionMembership membership = FactionMembership.load(memberTag);
                    if (membership != null) {
                        factionMembersList.add(membership);
                    }
                }
                if (!factionMembersList.isEmpty()) {
                    data.memberships.put(factionId, factionMembersList);
                }
            }
        }

        return data;
    }

    /**
     * 创建 SavedData 工厂。
     *
     * @return SavedData Factory
     */
    public static SavedData.Factory<FactionWorldData> factory() {
        return new SavedData.Factory<>(FactionWorldData::new, FactionWorldData::loadFromTag, null);
    }

    /**
     * 从指定维度读取或创建势力世界数据。
     *
     * @param level 服务器维度实例
     * @return 势力世界数据
     */
    public static FactionWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    // ========== CRUD 方法 ==========

    /**
     * 添加势力。
     *
     * @param faction 要添加的势力
     */
    public void addFaction(FactionCore faction) {
        if (faction != null) {
            factions.put(faction.id(), faction);
            setDirty();
        }
    }

    /**
     * 移除势力。同时清理该势力的成员和关系。
     *
     * @param factionId 要移除的势力 UUID
     */
    public void removeFaction(UUID factionId) {
        if (factionId != null && factions.remove(factionId) != null) {
            // 清理该势力的成员列表
            memberships.remove(factionId);
            setDirty();
        }
    }

    /**
     * 获取指定势力。
     *
     * @param factionId 势力 UUID
     * @return 势力数据；不存在则返回 null
     */
    @Nullable
    public FactionCore getFaction(UUID factionId) {
        return factions.get(factionId);
    }

    /**
     * 获取所有势力。
     *
     * @return 所有势力的不可变集合
     */
    public Collection<FactionCore> getAllFactions() {
        return Collections.unmodifiableCollection(factions.values());
    }

    /**
     * 添加成员到势力。
     *
     * @param membership 要添加的成员
     */
    public void addMembership(FactionMembership membership) {
        if (membership != null) {
            List<FactionMembership> memberList = memberships.computeIfAbsent(
                membership.factionId(),
                k -> new ArrayList<>()
            );
            memberList.add(membership);
            setDirty();
        }
    }

    /**
     * 从势力中移除成员。
     *
     * @param memberId 成员 UUID
     * @param factionId 势力 UUID
     */
    public void removeMembership(UUID memberId, UUID factionId) {
        if (memberId != null && factionId != null) {
            List<FactionMembership> memberList = memberships.get(factionId);
            if (memberList != null) {
                boolean removed = memberList.removeIf(m -> m.memberId().equals(memberId));
                if (removed) {
                    setDirty();
                }
            }
        }
    }

    /**
     * 获取势力的所有成员。
     *
     * @param factionId 势力 UUID
     * @return 成员列表；不存在则返回空列表
     */
    public List<FactionMembership> getMemberships(UUID factionId) {
        List<FactionMembership> memberList = memberships.get(factionId);
        if (memberList == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(memberList);
    }

    /**
     * 获取关系矩阵。
     *
     * @return 关系矩阵实例
     */
    public FactionRelationMatrix getRelationMatrix() {
        return relationMatrix;
    }
}
