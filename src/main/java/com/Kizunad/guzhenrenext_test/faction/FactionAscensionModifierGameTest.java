package com.Kizunad.guzhenrenext_test.faction;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.service.FactionService;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationManager;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public final class FactionAscensionModifierGameTest {

    private static final String BATCH_NAME = "faction_ascension_modifier";

    private static final int HOSTILE_RELATION_A = -90;

    private static final int HOSTILE_RELATION_B = -80;

    private FactionAscensionModifierGameTest() {
    }

    @GameTest(template = "empty", timeoutTicks = 120, batch = BATCH_NAME)
    public static void testHostilePressureShouldRaiseInvasionSpawnMultiplier(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        UUID ownerId = deterministicId("task15-owner");
        UUID hostileAId = deterministicId("task15-hostile-a");
        UUID hostileBId = deterministicId("task15-hostile-b");

        FactionCore ownerFaction = FactionService.createFaction(level, "升仙宗", FactionCore.FactionType.SECT);
        FactionCore hostileAFaction = FactionService.createFaction(level, "外敌甲", FactionCore.FactionType.CLAN);
        FactionCore hostileBFaction = FactionService.createFaction(level, "外敌乙", FactionCore.FactionType.ROGUE_GROUP);
        helper.assertTrue(ownerFaction != null, "Task15 GameTest: 拥有者势力创建失败");
        helper.assertTrue(hostileAFaction != null, "Task15 GameTest: 敌对势力 A 创建失败");
        helper.assertTrue(hostileBFaction != null, "Task15 GameTest: 敌对势力 B 创建失败");

        boolean joined = FactionService.addMember(
            level,
            ownerFaction.id(),
            ownerId,
            com.Kizunad.guzhenrenext.faction.core.FactionMembership.MemberRole.MEMBER
        );
        helper.assertTrue(joined, "Task15 GameTest: 拥有者加入势力失败");

        FactionService.updateRelation(level, ownerFaction.id(), hostileAFaction.id(), HOSTILE_RELATION_A);
        FactionService.updateRelation(level, ownerFaction.id(), hostileBFaction.id(), HOSTILE_RELATION_B);

        com.Kizunad.guzhenrenext.faction.integration.FactionAscensionModifier modifier =
            new com.Kizunad.guzhenrenext.faction.integration.FactionAscensionModifier();
        TribulationManager.ExternalTribulationModifier externalModifier =
            modifier.resolveTribulationModifier(level, ownerId);

        helper.assertTrue(
            externalModifier.intensityMultiplier() > 1.0D,
            "Task15 GameTest: 敌对关系应提高灾劫强度倍率"
        );
        helper.assertTrue(
            externalModifier.invasionSpawnMultiplier() > 1.0D,
            "Task15 GameTest: 敌对关系应提高入侵生成倍率"
        );

        helper.succeed();
    }

    private static UUID deterministicId(String token) {
        return UUID.nameUUIDFromBytes(token.getBytes(StandardCharsets.UTF_8));
    }
}
