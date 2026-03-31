package com.Kizunad.guzhenrenext.faction.service;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership.MemberRole;
import com.Kizunad.guzhenrenext.faction.data.FactionWorldData;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

/**
 * 宗门/家族成员管理器。
 * <p>
 * 本管理器负责成员相关的业务逻辑：
 * 1) 招募条件检查（候选人是否已加入其他势力）
 * 2) 成员晋升（角色变更）
 * 3) 贡献值计算与更新（含上限限制）
 * 4) 成员查询（按势力、按条件）
 * </p>
 * <p>
 * 宗门和家族共用基础逻辑，通过策略区分（由调用方处理）。
 * </p>
 */
public final class FactionMembershipManager {

    // ========== 常量定义 ==========

    /**
     * 贡献值上限。
     */
    private static final int MAX_CONTRIBUTION = 100000;

    /**
     * 贡献值下限。
     */
    private static final int MIN_CONTRIBUTION = 0;

    private FactionMembershipManager() {
    }

    /**
     * 检查是否可以招募候选人。
     * <p>
     * 招募条件：
     * 1) 势力存在；
     * 2) 候选人未加入任何势力。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @param candidateId 候选人 UUID
     * @return 可以招募返回 true，否则返回 false
     */
    public static boolean canRecruit(ServerLevel level, UUID factionId, UUID candidateId) {
        if (level == null || factionId == null || candidateId == null) {
            return false;
        }

        FactionWorldData worldData = FactionWorldData.get(level);

        // 检查势力是否存在
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return false;
        }

        // 检查候选人是否已加入其他势力
        for (FactionCore existingFaction : worldData.getAllFactions()) {
            List<FactionMembership> members = worldData.getMemberships(existingFaction.id());
            for (FactionMembership member : members) {
                if (member.memberId().equals(candidateId)) {
                    // 候选人已加入其他势力
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 晋升成员到新角色。
     * <p>
     * 晋升流程：
     * 1) 验证成员存在于该势力；
     * 2) 创建新的 FactionMembership 实例（保留原有加入时间和贡献值）；
     * 3) 移除旧成员记录，添加新成员记录。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @param memberId 成员 UUID
     * @param newRole 新角色
     * @return 晋升成功返回 true，否则返回 false
     */
    public static boolean promote(ServerLevel level, UUID factionId, UUID memberId, MemberRole newRole) {
        if (level == null || factionId == null || memberId == null || newRole == null) {
            return false;
        }

        FactionWorldData worldData = FactionWorldData.get(level);

        // 检查势力是否存在
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return false;
        }

        // 查找成员
        List<FactionMembership> members = worldData.getMemberships(factionId);
        FactionMembership existingMember = null;
        for (FactionMembership member : members) {
            if (member.memberId().equals(memberId)) {
                existingMember = member;
                break;
            }
        }

        if (existingMember == null) {
            // 成员不存在
            return false;
        }

        // 如果新角色与现有角色相同，无需更新
        if (existingMember.role() == newRole) {
            return true;
        }

        // 创建新的成员记录（保留加入时间和贡献值）
        FactionMembership promotedMember = new FactionMembership(
            memberId,
            factionId,
            newRole,
            existingMember.joinedAt(),
            existingMember.contribution()
        );

        // 移除旧记录，添加新记录
        worldData.removeMembership(memberId, factionId);
        worldData.addMembership(promotedMember);

        return true;
    }

    /**
     * 增加成员的贡献值。
     * <p>
     * 贡献流程：
     * 1) 验证成员存在于该势力；
     * 2) 计算新贡献值（原值 + 增量）；
     * 3) 限制在 [0, MAX_CONTRIBUTION] 范围内；
     * 4) 更新成员记录。
     * </p>
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @param memberId 成员 UUID
     * @param amount 增加的贡献值（可为负数表示扣除）
     * @return 更新成功返回 true，否则返回 false
     */
    public static boolean addContribution(ServerLevel level, UUID factionId, UUID memberId, int amount) {
        if (level == null || factionId == null || memberId == null) {
            return false;
        }

        FactionWorldData worldData = FactionWorldData.get(level);

        // 检查势力是否存在
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return false;
        }

        // 查找成员
        List<FactionMembership> members = worldData.getMemberships(factionId);
        FactionMembership existingMember = null;
        for (FactionMembership member : members) {
            if (member.memberId().equals(memberId)) {
                existingMember = member;
                break;
            }
        }

        if (existingMember == null) {
            // 成员不存在
            return false;
        }

        // 计算新贡献值，限制在范围内
        int newContribution = existingMember.contribution() + amount;
        newContribution = Math.max(MIN_CONTRIBUTION, Math.min(MAX_CONTRIBUTION, newContribution));

        // 如果贡献值未变化，无需更新
        if (newContribution == existingMember.contribution()) {
            return true;
        }

        // 创建新的成员记录
        FactionMembership updatedMember = new FactionMembership(
            memberId,
            factionId,
            existingMember.role(),
            existingMember.joinedAt(),
            newContribution
        );

        // 移除旧记录，添加新记录
        worldData.removeMembership(memberId, factionId);
        worldData.addMembership(updatedMember);

        return true;
    }

    /**
     * 获取势力的所有成员。
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @return 成员列表；不存在则返回空列表
     */
    public static List<FactionMembership> getMembers(ServerLevel level, UUID factionId) {
        if (level == null || factionId == null) {
            return Collections.emptyList();
        }

        FactionWorldData worldData = FactionWorldData.get(level);

        // 检查势力是否存在
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return Collections.emptyList();
        }

        return worldData.getMemberships(factionId);
    }

    /**
     * 检查指定成员是否属于该势力。
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @param memberId 成员 UUID
     * @return 是成员返回 true，否则返回 false
     */
    public static boolean isMember(ServerLevel level, UUID factionId, UUID memberId) {
        if (level == null || factionId == null || memberId == null) {
            return false;
        }

        FactionWorldData worldData = FactionWorldData.get(level);

        // 检查势力是否存在
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return false;
        }

        // 查找成员
        List<FactionMembership> members = worldData.getMemberships(factionId);
        for (FactionMembership member : members) {
            if (member.memberId().equals(memberId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取势力的成员数量。
     *
     * @param level 服务器维度实例
     * @param factionId 势力 UUID
     * @return 成员数量；势力不存在返回 0
     */
    public static int getMemberCount(ServerLevel level, UUID factionId) {
        if (level == null || factionId == null) {
            return 0;
        }

        FactionWorldData worldData = FactionWorldData.get(level);

        // 检查势力是否存在
        FactionCore faction = worldData.getFaction(factionId);
        if (faction == null) {
            return 0;
        }

        return worldData.getMemberships(factionId).size();
    }
}
