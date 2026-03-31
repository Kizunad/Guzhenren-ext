package com.Kizunad.guzhenrenext_test.faction;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionCore.FactionStatus;
import com.Kizunad.guzhenrenext.faction.core.FactionCore.FactionType;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership.MemberRole;
import com.Kizunad.guzhenrenext.faction.data.FactionWorldData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 势力世界数据持久化测试。
 * <p>
 * 验证 FactionWorldData 的数据持久化功能：
 * 1) 创建势力并添加到 WorldData
 * 2) 验证数据可以正确保存和加载
 * 3) 验证成员列表和关系矩阵也被正确持久化
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class FactionWorldDataTest {

    private static final int TEST_TIMEOUT_TICKS = 80;

    private static final String TEST_FACTION_NAME = "测试宗门";

    private static final String TEST_MEMBER_NAME = "测试成员";

    private static final long TEST_CREATED_AT = 1000L;

    private static final long TEST_JOINED_AT = 2000L;

    private static final int TEST_POWER = 500;

    private static final int TEST_RESOURCES = 300;

    private static final int TEST_CONTRIBUTION = 1000;

    private static final int TEST_RELATION_VALUE = 50;

    /**
     * 测试势力数据持久化。
     * <p>
     * 流程：
     * 1) 创建两个势力
     * 2) 添加成员到第一个势力
     * 3) 设置两个势力间的关系
     * 4) 从 WorldData 读取并验证所有数据一致
     * </p>
     *
     * @param helper GameTest 辅助工具
     */
    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "faction_world_data_persist"
    )
    public void testFactionWorldDataPersistence(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FactionWorldData worldData = FactionWorldData.get(level);

        // 先清理同一保存数据里上一次 GameTest 遗留的势力，避免历史状态污染本次断言。
        List<UUID> existingFactionIds = new ArrayList<>();
        for (FactionCore faction : worldData.getAllFactions()) {
            existingFactionIds.add(faction.id());
        }
        for (UUID existingFactionId : existingFactionIds) {
            worldData.removeFaction(existingFactionId);
        }

        // 创建第一个势力
        UUID factionId1 = UUID.randomUUID();
        FactionCore faction1 = new FactionCore(
            factionId1,
            TEST_FACTION_NAME,
            FactionType.SECT,
            TEST_CREATED_AT,
            FactionStatus.ACTIVE,
            TEST_POWER,
            TEST_RESOURCES
        );

        // 创建第二个势力
        UUID factionId2 = UUID.randomUUID();
        FactionCore faction2 = new FactionCore(
            factionId2,
            "测试家族",
            FactionType.CLAN,
            TEST_CREATED_AT,
            FactionStatus.ACTIVE,
            TEST_POWER,
            TEST_RESOURCES
        );

        // 添加势力到 WorldData
        worldData.addFaction(faction1);
        worldData.addFaction(faction2);

        // 验证势力已添加
        FactionCore retrievedFaction1 = worldData.getFaction(factionId1);
        helper.assertTrue(retrievedFaction1 != null, "势力 1 应该存在");
        helper.assertTrue(retrievedFaction1.name().equals(TEST_FACTION_NAME), "势力 1 名称应该匹配");

        // 创建成员
        UUID memberId = UUID.randomUUID();
        FactionMembership membership = new FactionMembership(
            memberId,
            factionId1,
            MemberRole.MEMBER,
            TEST_JOINED_AT,
            TEST_CONTRIBUTION
        );

        // 添加成员
        worldData.addMembership(membership);

        // 验证成员已添加
        List<FactionMembership> members = worldData.getMemberships(factionId1);
        helper.assertTrue(members.size() == 1, "势力 1 应该有 1 个成员");
        helper.assertTrue(members.get(0).memberId().equals(memberId), "成员 UUID 应该匹配");

        // 设置两个势力间的关系
        worldData.getRelationMatrix().setRelation(factionId1, factionId2, TEST_RELATION_VALUE);

        // 验证关系已设置
        int relation = worldData.getRelationMatrix().getRelation(factionId1, factionId2);
        helper.assertTrue(relation == TEST_RELATION_VALUE, "关系值应该匹配");

        // 验证对称性
        int reverseRelation = worldData.getRelationMatrix().getRelation(factionId2, factionId1);
        helper.assertTrue(reverseRelation == TEST_RELATION_VALUE, "关系应该对称");

        // 验证所有势力都能被检索
        Collection<FactionCore> allFactions = worldData.getAllFactions();
        helper.assertTrue(allFactions.size() == 2, "应该有 2 个势力");

        helper.succeed();
    }
}
