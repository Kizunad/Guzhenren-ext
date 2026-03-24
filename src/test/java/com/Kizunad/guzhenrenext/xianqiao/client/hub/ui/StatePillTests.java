package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class StatePillTests {

    @Test
    void riskPillMapsStableUnknownWarnAndDangerDeterministically() {
        final Map<HubStatusEvaluator.RiskLevel, HubUiTokens.HubTone> expectedToneByRisk = Map.of(
            HubStatusEvaluator.RiskLevel.STABLE, HubUiTokens.HubTone.JADE,
            HubStatusEvaluator.RiskLevel.CAUTION, HubUiTokens.HubTone.WARN,
            HubStatusEvaluator.RiskLevel.UNKNOWN, HubUiTokens.HubTone.AZURE,
            HubStatusEvaluator.RiskLevel.DANGER, HubUiTokens.HubTone.DANGER
        );
        final Map<HubStatusEvaluator.RiskLevel, String> expectedLabelByRisk = Map.of(
            HubStatusEvaluator.RiskLevel.STABLE, "稳定",
            HubStatusEvaluator.RiskLevel.CAUTION, "预警",
            HubStatusEvaluator.RiskLevel.UNKNOWN, "待核验",
            HubStatusEvaluator.RiskLevel.DANGER, "危险"
        );

        final StatePill pill = new StatePill();

        for (Map.Entry<HubStatusEvaluator.RiskLevel, HubUiTokens.HubTone> entry : expectedToneByRisk.entrySet()) {
            final HubStatusEvaluator.RiskLevel riskLevel = entry.getKey();

            pill.applyRisk(riskLevel);

            assertEquals(entry.getValue(), pill.getTone());
            assertEquals(expectedLabelByRisk.get(riskLevel), pill.getText());
            assertFalse(pill.getText().isBlank());
            assertNotNull(pill.getResolvedPalette());
            assertNotNull(pill.getResolvedPalette().theme());
        }
    }
}
