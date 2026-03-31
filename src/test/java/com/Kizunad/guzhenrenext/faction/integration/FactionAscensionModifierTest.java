package com.Kizunad.guzhenrenext.faction.integration;

import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.integration.FactionAscensionModifier.FactionInfluenceSnapshot;
import com.Kizunad.guzhenrenext.faction.integration.FactionAscensionModifier.ModifierBundle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class FactionAscensionModifierTest {

    private static final int LOW_RESOURCE_VALUE = 120;
    private static final int LOW_RESOURCE_SUPPLY = 100;
    private static final int HIGH_RESOURCE_VALUE = 700;
    private static final int HIGH_RESOURCE_SUPPLY = 800;
    private static final int FRIENDLY_CONTEXT_VALUE = 300;
    private static final int PROTECTED_RESOURCE_VALUE = 200;
    private static final int HOSTILE_INFLUENCE_LEVEL = 3;
    private static final int HOSTILE_IMPACT_VALUE = 240;

    @Test
    void factionResourcesShouldIncreaseAscensionReadinessInput() {
        ModifierBundle lowResource = FactionAscensionModifier.evaluateSnapshot(
            new FactionInfluenceSnapshot(
                true,
                FactionCore.FactionType.SECT,
                LOW_RESOURCE_VALUE,
                LOW_RESOURCE_SUPPLY,
                0,
                0
            )
        );
        ModifierBundle highResource = FactionAscensionModifier.evaluateSnapshot(
            new FactionInfluenceSnapshot(
                true,
                FactionCore.FactionType.SECT,
                HIGH_RESOURCE_VALUE,
                HIGH_RESOURCE_SUPPLY,
                0,
                0
            )
        );

        assertTrue(
            highResource.readinessModifier().scoreBonus() > lowResource.readinessModifier().scoreBonus(),
            "高资源势力应获得更高的准备度分数加成"
        );
        assertTrue(
            highResource.readinessModifier().readyThresholdReduction()
                >= lowResource.readinessModifier().readyThresholdReduction(),
            "高资源势力不应降低其阈值减免"
        );
        assertTrue(
            highResource.readinessModifier().balanceThresholdReduction()
                >= lowResource.readinessModifier().balanceThresholdReduction(),
            "高资源势力不应降低其平衡阈值减免"
        );
    }

    @Test
    void sectOrClanProtectionShouldLowerTribulationIntensity() {
        ModifierBundle unaffiliated = FactionAscensionModifier.evaluateSnapshot(
            FactionInfluenceSnapshot.unaffiliated()
        );
        ModifierBundle sectProtected = FactionAscensionModifier.evaluateSnapshot(
            new FactionInfluenceSnapshot(
                true,
                FactionCore.FactionType.SECT,
                PROTECTED_RESOURCE_VALUE,
                PROTECTED_RESOURCE_VALUE,
                0,
                0
            )
        );
        ModifierBundle clanProtected = FactionAscensionModifier.evaluateSnapshot(
            new FactionInfluenceSnapshot(
                true,
                FactionCore.FactionType.CLAN,
                PROTECTED_RESOURCE_VALUE,
                PROTECTED_RESOURCE_VALUE,
                0,
                0
            )
        );

        assertTrue(
            sectProtected.tribulationModifier().intensityMultiplier()
                < unaffiliated.tribulationModifier().intensityMultiplier(),
            "宗门保护应降低灾劫强度"
        );
        assertTrue(
            clanProtected.tribulationModifier().intensityMultiplier()
                < unaffiliated.tribulationModifier().intensityMultiplier(),
            "家族保护应降低灾劫强度"
        );
    }

    @Test
    void hostileRelationsShouldIncreaseTribulationDifficultyAndInterference() {
        ModifierBundle friendlyContext = FactionAscensionModifier.evaluateSnapshot(
            new FactionInfluenceSnapshot(
                true,
                FactionCore.FactionType.CLAN,
                FRIENDLY_CONTEXT_VALUE,
                FRIENDLY_CONTEXT_VALUE,
                0,
                0
            )
        );
        ModifierBundle hostileContext = FactionAscensionModifier.evaluateSnapshot(
            new FactionInfluenceSnapshot(
                true,
                FactionCore.FactionType.CLAN,
                FRIENDLY_CONTEXT_VALUE,
                FRIENDLY_CONTEXT_VALUE,
                HOSTILE_INFLUENCE_LEVEL,
                HOSTILE_IMPACT_VALUE
            )
        );

        assertTrue(
            hostileContext.tribulationModifier().intensityMultiplier()
                > friendlyContext.tribulationModifier().intensityMultiplier(),
            "敌对关系应提高灾劫强度"
        );
        assertTrue(
            hostileContext.tribulationModifier().invasionSpawnMultiplier()
                > friendlyContext.tribulationModifier().invasionSpawnMultiplier(),
            "敌对关系应提高入侵干扰规模"
        );
    }
}
