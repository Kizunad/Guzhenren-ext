package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class StateBadgeTests {

    @Test
    void dataClassBadgeUsesDeterministicHubToneAndLabel() {
        assertBadge(HubSnapshot.DataClass.REAL_CORE, HubUiTokens.HubTone.GOLD, "真核");
        assertBadge(HubSnapshot.DataClass.REAL_SUMMARY, HubUiTokens.HubTone.JADE, "实况摘要");
        assertBadge(HubSnapshot.DataClass.SUMMARY_ROUTE, HubUiTokens.HubTone.AZURE, "摘要路由");
    }

    private void assertBadge(
        final HubSnapshot.DataClass dataClass,
        final HubUiTokens.HubTone expectedTone,
        final String expectedLabel
    ) {
        final StateBadge badge = StateBadge.forDataClass(dataClass);

        assertEquals(expectedTone, badge.getTone());
        assertEquals(expectedLabel, badge.getText());
        assertNotNull(badge.getResolvedPalette());
        assertNotNull(badge.getResolvedPalette().theme());
    }
}
