package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubCardRegistry;
import java.util.List;
import java.util.Objects;

final class ApertureHubV2Contract {

    private static final int LEGACY_TAB_SECTION_COUNT = 5;
    private static final List<String> LEGACY_TAB_LABELS = List.of("总览", "地灵", "资源", "灾劫", "道痕");

    private static final List<StructureLayer> ORDERED_STRUCTURE_LAYERS = List.of(
        StructureLayer.HEADER_BAND,
        StructureLayer.TOP_SITUATION_ROW,
        StructureLayer.MAIN_MODULE_GRID,
        StructureLayer.BOTTOM_UTILITY_ROW
    );

    private static final List<LayerContract> FIXED_TOP_LEVEL_LAYOUT = List.of(
        new LayerContract(
            StructureLayer.HEADER_BAND,
            List.of("hub-title", "stability-headline"),
            false
        ),
        new LayerContract(
            StructureLayer.TOP_SITUATION_ROW,
            List.of("overall-summary", "risk-summary", "next-route"),
            false
        ),
        new LayerContract(
            StructureLayer.MAIN_MODULE_GRID,
            HubCardRegistry.cardIds(),
            true
        ),
        new LayerContract(
            StructureLayer.BOTTOM_UTILITY_ROW,
            List.of("utility-routes", "summary-footnote", "fallback-explainer"),
            false
        )
    );

    private ApertureHubV2Contract() {
    }

    static List<StructureLayer> orderedStructureLayers() {
        return ORDERED_STRUCTURE_LAYERS;
    }

    static List<HubCardRegistry.CardDefinition> firstVersionCards() {
        return HubCardRegistry.cards();
    }

    static List<String> firstVersionCardIds() {
        return HubCardRegistry.cardIds();
    }

    static List<HubCardRegistry.CardDefinition> strongCards() {
        return HubCardRegistry.strongCards();
    }

    static List<HubCardRegistry.CardDefinition> lightCards() {
        return HubCardRegistry.lightCards();
    }

    static List<String> legacyTabLabels() {
        return LEGACY_TAB_LABELS;
    }

    static List<LayerContract> fixedTopLevelLayout() {
        return FIXED_TOP_LEVEL_LAYOUT;
    }

    static boolean hasLegacyFivePeerTabs(final List<StructureLayer> structureLayers) {
        return structureLayers.size() == LEGACY_TAB_SECTION_COUNT;
    }

    enum StructureLayer {
        HEADER_BAND("HeaderBand"),
        TOP_SITUATION_ROW("TopSituationRow"),
        MAIN_MODULE_GRID("MainModuleGrid"),
        BOTTOM_UTILITY_ROW("BottomUtilityRow");

        private final String conceptName;

        StructureLayer(final String conceptName) {
            this.conceptName = conceptName;
        }

        String conceptName() {
            return conceptName;
        }
    }

    record LayerContract(
        StructureLayer layer,
        List<String> frozenSlots,
        boolean hostsModuleCards
    ) {

        LayerContract {
            Objects.requireNonNull(layer, "layer");
            Objects.requireNonNull(frozenSlots, "frozenSlots");

            if (frozenSlots.isEmpty()) {
                throw new IllegalArgumentException("frozenSlots 不能为空");
            }
            if (frozenSlots.stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("frozenSlots 不能包含空字符串");
            }
            if (hostsModuleCards && layer != StructureLayer.MAIN_MODULE_GRID) {
                throw new IllegalArgumentException("只有 MainModuleGrid 可以承载模块卡");
            }
            if (!hostsModuleCards
                && frozenSlots.stream().anyMatch(HubCardRegistry.cardIds()::contains)) {
                throw new IllegalArgumentException("非模块网格层不能直接承载模块卡");
            }
            if (hostsModuleCards && !frozenSlots.equals(HubCardRegistry.cardIds())) {
                throw new IllegalArgumentException("MainModuleGrid 必须冻结全部第一版模块卡");
            }
        }
    }
}
