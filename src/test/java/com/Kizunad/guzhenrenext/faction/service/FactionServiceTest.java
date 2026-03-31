package com.Kizunad.guzhenrenext.faction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionCore.FactionStatus;
import com.Kizunad.guzhenrenext.faction.core.FactionCore.FactionType;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership.MemberRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * FactionService 单元测试。
 * <p>
 * 测试服务层依赖的数据模型和枚举。
 * 由于 FactionService 依赖 ServerLevel（Minecraft 类），
 * 本测试专注于验证数据模型的创建和枚举值的有效性。
 * </p>
 */
final class FactionServiceTest {

    @Test
    void testFactionCoreCreationWithValidData() {
        // 准备
        UUID factionId = UUID.randomUUID();
        String name = "测试宗门";
        FactionType type = FactionType.SECT;
        long createdAt = 1000L;
        FactionStatus status = FactionStatus.ACTIVE;
        int power = 100;
        int resources = 100;

        // 执行
        FactionCore faction = new FactionCore(factionId, name, type, createdAt, status, power, resources);

        // 验证
        assertNotNull(faction);
        assertEquals(factionId, faction.id());
        assertEquals(name, faction.name());
        assertEquals(type, faction.type());
        assertEquals(createdAt, faction.createdAt());
        assertEquals(status, faction.status());
        assertEquals(power, faction.power());
        assertEquals(resources, faction.resources());
    }

    @Test
    void testFactionCoreWithDifferentTypes() {
        // 准备
        UUID factionId = UUID.randomUUID();

        // 执行 - SECT
        FactionCore sect = new FactionCore(
            factionId,
            "宗门",
            FactionType.SECT,
            1000L,
            FactionStatus.ACTIVE,
            100,
            100
        );

        // 执行 - CLAN
        FactionCore clan = new FactionCore(
            UUID.randomUUID(),
            "家族",
            FactionType.CLAN,
            1000L,
            FactionStatus.ACTIVE,
            100,
            100
        );

        // 执行 - ROGUE_GROUP
        FactionCore rogueGroup = new FactionCore(
            UUID.randomUUID(),
            "散修群体",
            FactionType.ROGUE_GROUP,
            1000L,
            FactionStatus.ACTIVE,
            100,
            100
        );

        // 验证
        assertEquals(FactionType.SECT, sect.type());
        assertEquals(FactionType.CLAN, clan.type());
        assertEquals(FactionType.ROGUE_GROUP, rogueGroup.type());
    }

    @Test
    void testFactionCoreWithDifferentStatuses() {
        // 准备
        UUID factionId = UUID.randomUUID();

        // 执行 - ACTIVE
        FactionCore active = new FactionCore(
            factionId,
            "活跃势力",
            FactionType.SECT,
            1000L,
            FactionStatus.ACTIVE,
            100,
            100
        );

        // 执行 - DISSOLVED
        FactionCore dissolved = new FactionCore(
            UUID.randomUUID(),
            "解散势力",
            FactionType.SECT,
            1000L,
            FactionStatus.DISSOLVED,
            100,
            100
        );

        // 执行 - AT_WAR
        FactionCore atWar = new FactionCore(
            UUID.randomUUID(),
            "战争势力",
            FactionType.SECT,
            1000L,
            FactionStatus.AT_WAR,
            100,
            100
        );

        // 验证
        assertEquals(FactionStatus.ACTIVE, active.status());
        assertEquals(FactionStatus.DISSOLVED, dissolved.status());
        assertEquals(FactionStatus.AT_WAR, atWar.status());
    }

    @Test
    void testFactionMembershipCreationWithValidData() {
        // 准备
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();
        MemberRole role = MemberRole.MEMBER;
        long joinedAt = 2000L;
        int contribution = 50;

        // 执行
        FactionMembership membership = new FactionMembership(memberId, factionId, role, joinedAt, contribution);

        // 验证
        assertNotNull(membership);
        assertEquals(memberId, membership.memberId());
        assertEquals(factionId, membership.factionId());
        assertEquals(role, membership.role());
        assertEquals(joinedAt, membership.joinedAt());
        assertEquals(contribution, membership.contribution());
    }

    @Test
    void testFactionMembershipWithDifferentRoles() {
        // 准备
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();

        // 执行 - LEADER
        FactionMembership leader = new FactionMembership(
            memberId,
            factionId,
            MemberRole.LEADER,
            1000L,
            100
        );

        // 执行 - ELDER
        FactionMembership elder = new FactionMembership(
            UUID.randomUUID(),
            factionId,
            MemberRole.ELDER,
            1000L,
            80
        );

        // 执行 - MEMBER
        FactionMembership member = new FactionMembership(
            UUID.randomUUID(),
            factionId,
            MemberRole.MEMBER,
            1000L,
            50
        );

        // 执行 - OUTER_DISCIPLE
        FactionMembership outerDisciple = new FactionMembership(
            UUID.randomUUID(),
            factionId,
            MemberRole.OUTER_DISCIPLE,
            1000L,
            20
        );

        // 验证
        assertEquals(MemberRole.LEADER, leader.role());
        assertEquals(MemberRole.ELDER, elder.role());
        assertEquals(MemberRole.MEMBER, member.role());
        assertEquals(MemberRole.OUTER_DISCIPLE, outerDisciple.role());
    }

    @Test
    void testFactionTypeEnumValues() {
        // 验证所有势力类型都存在
        assertNotNull(FactionType.SECT);
        assertNotNull(FactionType.CLAN);
        assertNotNull(FactionType.ROGUE_GROUP);
        assertEquals(3, FactionType.values().length);
    }

    @Test
    void testFactionStatusEnumValues() {
        // 验证所有势力状态都存在
        assertNotNull(FactionStatus.ACTIVE);
        assertNotNull(FactionStatus.DISSOLVED);
        assertNotNull(FactionStatus.AT_WAR);
        assertEquals(3, FactionStatus.values().length);
    }

    @Test
    void testMemberRoleEnumValues() {
        // 验证所有成员角色都存在
        assertNotNull(MemberRole.LEADER);
        assertNotNull(MemberRole.ELDER);
        assertNotNull(MemberRole.MEMBER);
        assertNotNull(MemberRole.OUTER_DISCIPLE);
        assertEquals(4, MemberRole.values().length);
    }

    @Test
    void testFactionCoreWithMaximumValues() {
        // 准备
        UUID factionId = UUID.randomUUID();

        // 执行
        FactionCore faction = new FactionCore(
            factionId,
            "最大值势力",
            FactionType.SECT,
            Long.MAX_VALUE,
            FactionStatus.ACTIVE,
            10000,
            10000
        );

        // 验证
        assertEquals(10000, faction.power());
        assertEquals(10000, faction.resources());
    }

    @Test
    void testFactionCoreWithMinimumValues() {
        // 准备
        UUID factionId = UUID.randomUUID();

        // 执行
        FactionCore faction = new FactionCore(
            factionId,
            "最小值势力",
            FactionType.SECT,
            0L,
            FactionStatus.ACTIVE,
            0,
            0
        );

        // 验证
        assertEquals(0, faction.power());
        assertEquals(0, faction.resources());
    }

    @Test
    void testFactionMembershipWithMaximumContribution() {
        // 准备
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();

        // 执行
        FactionMembership membership = new FactionMembership(
            memberId,
            factionId,
            MemberRole.LEADER,
            1000L,
            100000
        );

        // 验证
        assertEquals(100000, membership.contribution());
    }

    @Test
    void testFactionMembershipWithMinimumContribution() {
        // 准备
        UUID memberId = UUID.randomUUID();
        UUID factionId = UUID.randomUUID();

        // 执行
        FactionMembership membership = new FactionMembership(
            memberId,
            factionId,
            MemberRole.OUTER_DISCIPLE,
            1000L,
            0
        );

        // 验证
        assertEquals(0, membership.contribution());
    }
}
