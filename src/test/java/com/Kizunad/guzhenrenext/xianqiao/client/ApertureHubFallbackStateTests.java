package com.Kizunad.guzhenrenext.xianqiao.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureHubFallbackStateTests {

    private static final Path APERTURE_HUB_SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/client/ApertureHubScreen.java"
    );

    private static final List<String> TASK7_LIGHT_CARD_SOURCE_MARKERS = List.of(
        "private String buildLightCardSummary(final HubRoutePolicy.CardRoutePolicy policy)",
        "case HubRoutePolicy.CARD_ALCHEMY -> \"独立工作台，待前往查看\";",
        "case HubRoutePolicy.CARD_STORAGE -> \"物品域最小视图，待前往查看\";",
        "case HubRoutePolicy.CARD_CLUSTER -> \"本地产出待提取，需前往查看\";",
        "case HubRoutePolicy.CARD_DAO_MARK -> \"占位子页入口，待前往查看\";",
        "return \"仅保留入口，需前往\" + policy.target().displayName() + \"核验\";",
        "return policy.noticeText() + \"；主殿不展示全局实况\";"
    );

    private static final List<String> TASK7_FOOTER_SOURCE_MARKERS = List.of(
        "createRowBlock(bottomUtilityRow, 0, \"快捷路由\")",
        "createRowBlock(bottomUtilityRow, 1, \"近况摘要\")",
        "createRowBlock(bottomUtilityRow, 2, \"中枢说明\")",
        "summaryFootnoteBlock.setBodyText(buildRecentStatusSummary(status));",
        "fallbackExplainerBlock.setBodyText(buildHubGuideText());",
        "return \"非权威运营占位：轻卡未接入稳定聚合源，请以前往分台核验为准；当前建议：\"",
        "return \"主殿只保留总览与入口；炼丹、储物、集群、道痕仅显示摘要入口或占位入口，\""
    );

    @Test
    void task7LightCardsUseExplicitFallbackSummariesInsteadOfPseudoGlobalMetrics() throws Exception {
        final String currentSource = Files.readString(APERTURE_HUB_SCREEN_SOURCE);

        assertTrue(TASK7_LIGHT_CARD_SOURCE_MARKERS.stream().allMatch(currentSource::contains));
        assertFalse(currentSource.contains("炼丹总产出"));
        assertFalse(currentSource.contains("储物总库存"));
        assertFalse(currentSource.contains("集群总吞吐"));
        assertFalse(currentSource.contains("道痕总量"));
    }

    @Test
    void task7FooterMarksRecentSummaryAsNonAuthoritativeAndKeepsHubGuideExplicit() throws Exception {
        final String currentSource = Files.readString(APERTURE_HUB_SCREEN_SOURCE);

        assertTrue(TASK7_FOOTER_SOURCE_MARKERS.stream().allMatch(currentSource::contains));
        assertTrue(currentSource.contains("非权威运营占位"));
        assertTrue(currentSource.contains("不展示看似精准的全局运行数值"));
    }
}
