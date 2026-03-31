package com.Kizunad.guzhenrenext.faction.service;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionCore.FactionStatus;
import com.Kizunad.guzhenrenext.faction.core.FactionCore.FactionType;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership.MemberRole;
import com.Kizunad.guzhenrenext.faction.data.FactionWorldData;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

/**
 * 势力服务层。
 * <p>
 * 本服务定位为"服务端权威"的无状态工具类，仅负责：
 * 1) 创建、解散势力；
 * 2) 添加、移除成员；
 * 3) 更新、查询势力间关系；
 * 4) 查询势力信息。
 * </p>
 * <p>
 * 所有操作通过 {@link FactionWorldData} 进行，不直接操作实体或执行 AI 逻辑。
 * </p>
 */
public final class FactionService {

    private static final int INITIAL_FACTION_POWER = 100;
    private static final int INITIAL_FACTION_RESOURCES = 100;

    private FactionService() {
    }

    /**
     * 创建新势力。
     * <p>
     * 创建流程：
     * 1) 生成新 UUID；
     * 2) 初始化势力值为 100，资源为 100；
     * 3) 状态设为 ACTIVE；
     * 4) 创建时间使用 level.getGameTime()；
     * 5) 添加到 WorldData。
     * </p>
     *
     * @param level 服务器维度实例
     * @param name 势力名称
     * @param type 势力类型（SECT/CLAN/ROGUE_GROUP）
     * @return 创建的势力数据
     */
    public static FactionCore createFaction(ServerLevel level, String name, FactionType type) {
        if (level == null || name == null || type == null) {
            return null;
        }

        UUID factionId = UUID.randomUUID();
        long createdAt = level.getGameTime();
        int initialPower = INITIAL_FACTION_POWER;
        int initialResources = INITIAL_FACTION_RESOURCES;

        FactionCore faction = new FactionCore(
            factionId,
            name,
            type,
            createdAt,
            FactionStatus.ACTIVE,
            initialPower,
            initialResources
        );

        FactionWorldData worldData = FactionWorldData.get(level);
        worldData.addFaction(faction);

        return faction;
    }

    /**
     * 解散势力。
     * <p>
     * 解散流程：
     * 1) 从 WorldData 移除势力；
     * 2) 同时清理该势力的成员和关系。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionId 要解散的势力 UUID
     * @return 解散成功返回 true，否则返回 false
     */
    public static boolean dissolveFaction(ServerLevel level, UUID factionId) {
        if (level == null || factionId == null) {
            return false;
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return false;
        }

        worldData.removeFaction(factionId);
        return true;
    }

    /**
     * 添加成员到势力。
     * <p>
     * 添加流程：
     * 1) 验证势力存在；
     * 2) 创建 FactionMembership 实例；
     * 3) 加入时间使用 level.getGameTime()；
     * 4) 初始贡献值为 0；
     * 5) 添加到 WorldData。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @param memberId 成员 UUID
     * @param role 成员角色
     * @return 添加成功返回 true，否则返回 false
     */
    public static boolean addMember(ServerLevel level, UUID factionId, UUID memberId, MemberRole role) {
        if (level == null || factionId == null || memberId == null || role == null) {
            return false;
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return false;
        }

        long joinedAt = level.getGameTime();
        int initialContribution = 0;

        FactionMembership membership = new FactionMembership(
            memberId,
            factionId,
            role,
            joinedAt,
            initialContribution
        );

        worldData.addMembership(membership);
        return true;
    }

    /**
     * 从势力中移除成员。
     * <p>
     * 移除流程：
     * 1) 验证势力存在；
     * 2) 从 WorldData 移除成员。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @param memberId 成员 UUID
     * @return 移除成功返回 true，否则返回 false
     */
    public static boolean removeMember(ServerLevel level, UUID factionId, UUID memberId) {
        if (level == null || factionId == null || memberId == null) {
            return false;
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return false;
        }

        List<FactionMembership> members = worldData.getMemberships(factionId);
        boolean hasMember = members.stream().anyMatch(m -> m.memberId().equals(memberId));
        if (!hasMember) {
            return false;
        }

        worldData.removeMembership(memberId, factionId);
        return true;
    }

    /**
     * 更新两个势力间的关系值。
     * <p>
     * 更新流程：
     * 1) 验证两个势力都存在；
     * 2) 通过关系矩阵更新关系值（自动限幅到 [-100, 100]）。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionA 第一个势力 UUID
     * @param factionB 第二个势力 UUID
     * @param value 关系值（-100 到 +100）
     */
    public static void updateRelation(ServerLevel level, UUID factionA, UUID factionB, int value) {
        if (level == null || factionA == null || factionB == null) {
            return;
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        FactionCore factionACore = worldData.getFaction(factionA);
        FactionCore factionBCore = worldData.getFaction(factionB);

        if (factionACore == null || factionBCore == null) {
            return;
        }

        worldData.getRelationMatrix().setRelation(factionA, factionB, value);
    }

    /**
     * 查询两个势力间的关系值。
     * <p>
     * 查询流程：
     * 1) 验证两个势力都存在；
     * 2) 从关系矩阵读取关系值。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionA 第一个势力 UUID
     * @param factionB 第二个势力 UUID
     * @return 关系值（-100 到 +100）；若势力不存在则返回 0
     */
    public static int getRelation(ServerLevel level, UUID factionA, UUID factionB) {
        if (level == null || factionA == null || factionB == null) {
            return 0;
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        FactionCore factionACore = worldData.getFaction(factionA);
        FactionCore factionBCore = worldData.getFaction(factionB);

        if (factionACore == null || factionBCore == null) {
            return 0;
        }

        return worldData.getRelationMatrix().getRelation(factionA, factionB);
    }

    /**
     * 查询指定势力。
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @return 势力数据；不存在则返回 null
     */
    @Nullable
    public static FactionCore getFaction(ServerLevel level, UUID factionId) {
        if (level == null || factionId == null) {
            return null;
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        return worldData.getFaction(factionId);
    }

    /**
     * 查询所有势力。
     *
     * @param level 服务器维度实例
     * @return 所有势力的不可变集合
     */
    public static Collection<FactionCore> getAllFactions(ServerLevel level) {
        if (level == null) {
            return java.util.Collections.emptyList();
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        return worldData.getAllFactions();
    }

    /**
     * 查询势力的所有成员。
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @return 成员列表；不存在则返回空列表
     */
    public static List<FactionMembership> getMembers(ServerLevel level, UUID factionId) {
        if (level == null || factionId == null) {
            return java.util.Collections.emptyList();
        }

        FactionWorldData worldData = FactionWorldData.get(level);
        return worldData.getMemberships(factionId);
    }
}
