package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import java.util.List;
import java.util.Objects;

/**
 * Task 7: V2 Hub 界面契约定义。
 * <p>
 * 该契约定义了中枢界面 V2 版本的固定结构层次、布局槽位和组件标识，
 * 确保所有源标记测试能够验证 V2 结构的完整性。
 * </p>
 */
public final class ApertureHubV2Contract {

    private ApertureHubV2Contract() {
    }

    /**
     * 定义 V2 界面的四个固定结构层次。
     */
    public enum StructureLayer {
        LAYER_HEADER_BAND("HeaderBand"),
        LAYER_TOP_SITUATION_ROW("TopSituationRow"),
        LAYER_MAIN_MODULE_GRID("MainModuleGrid"),
        LAYER_BOTTOM_UTILITY_ROW("BottomUtilityRow");

        private final String conceptName;

        StructureLayer(String conceptName) {
            this.conceptName = Objects.requireNonNull(conceptName);
        }

        public String conceptName() {
            return conceptName;
        }
    }

    /**
     * 单一层次的具体槽位布局契约。
     */
    public record LayerContract(
        StructureLayer layer,
        List<String> frozenSlots,
        boolean hostsModuleCards
    ) {
        public LayerContract {
            layer = Objects.requireNonNull(layer);
            frozenSlots = Objects.requireNonNull(frozenSlots);
        }
    }

    /**
     * 返回 V2 界面层次的有序列表。
     */
    public static List<StructureLayer> orderedStructureLayers() {
        return List.of(
            StructureLayer.LAYER_HEADER_BAND,
            StructureLayer.LAYER_TOP_SITUATION_ROW,
            StructureLayer.LAYER_MAIN_MODULE_GRID,
            StructureLayer.LAYER_BOTTOM_UTILITY_ROW
        );
    }

    /**
     * 返回第一版模块卡片的固定 ID 列表（用于 MainModuleGrid 层的布局验证）。
     */
    public static List<String> firstVersionCardIds() {
        return List.of(
            HubRoutePolicy.CARD_APERTURE_OVERVIEW,
            HubRoutePolicy.CARD_LAND_SPIRIT,
            HubRoutePolicy.CARD_TRIBULATION,
            HubRoutePolicy.CARD_RESOURCE,
            HubRoutePolicy.CARD_ALCHEMY,
            HubRoutePolicy.CARD_STORAGE,
            HubRoutePolicy.CARD_CLUSTER,
            HubRoutePolicy.CARD_DAO_MARK
        );
    }

    /**
     * 返回固定顶层布局的完整层次契约列表。
     */
    public static List<LayerContract> fixedTopLevelLayout() {
        return List.of(
            // HeaderBand: 洞天标题 + 稳定性概要
            new LayerContract(
                StructureLayer.LAYER_HEADER_BAND,
                List.of("hub-title", "stability-headline"),
                false
            ),
            // TopSituationRow: 整体概要 + 风险摘要 + 下一路由
            new LayerContract(
                StructureLayer.LAYER_TOP_SITUATION_ROW,
                List.of("overall-summary", "risk-summary", "next-route"),
                false
            ),
            // MainModuleGrid: 八个模块卡（见 firstVersionCardIds）
            new LayerContract(
                StructureLayer.LAYER_MAIN_MODULE_GRID,
                firstVersionCardIds(),
                true
            ),
            // BottomUtilityRow: 快捷路由 + 摘要脚注 + 兜底说明
            new LayerContract(
                StructureLayer.LAYER_BOTTOM_UTILITY_ROW,
                List.of("utility-routes", "summary-footnote", "fallback-explainer"),
                false
            )
        );
    }

    /**
     * 检查给定的层次列表是否包含旧版五 Tab 布局特征。
     * <p>
     * 如果包含，说明尚未完成 V2 重构。
     * </p>
     */
    public static boolean hasLegacyFivePeerTabs(List<StructureLayer> layers) {
        if (layers == null || layers.isEmpty()) {
            return false;
        }
        // 检查是否存在与旧版 tab 标签（如"总览"、"地灵"、"资源"、"灾劫"、"道痕"）相同的层次名
        for (StructureLayer layer : layers) {
            for (String legacyLabel : legacyTabLabels()) {
                if (layer.conceptName().contains(legacyLabel)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回旧版五 Tab 的标签名称列表。
     */
    public static List<String> legacyTabLabels() {
        return List.of("总览", "地灵", "资源", "灾劫", "道痕", "升仙");
    }
}
