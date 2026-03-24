package com.Kizunad.guzhenrenext.xianqiao.client.hub;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HubCardRegistry {

    private static final List<CardDefinition> CARDS = List.of(
        new CardDefinition(
            HubRoutePolicy.CARD_APERTURE_OVERVIEW,
            "洞天全貌",
            HubRoutePolicy.HubCardTaxonomy.REAL_SUMMARY,
            CardWeight.STRONG_REAL_CARD,
            true,
            true,
            true,
            FallbackPresentation.MARK_UNVERIFIED,
            "状态待校验"
        ),
        new CardDefinition(
            HubRoutePolicy.CARD_LAND_SPIRIT,
            "地灵",
            HubRoutePolicy.HubCardTaxonomy.REAL_SUMMARY,
            CardWeight.STRONG_REAL_CARD,
            true,
            true,
            true,
            FallbackPresentation.MARK_UNVERIFIED,
            "状态待校验"
        ),
        new CardDefinition(
            HubRoutePolicy.CARD_TRIBULATION,
            "灾劫",
            HubRoutePolicy.HubCardTaxonomy.REAL_SUMMARY,
            CardWeight.STRONG_REAL_CARD,
            true,
            true,
            true,
            FallbackPresentation.MARK_UNVERIFIED,
            "状态待校验"
        ),
        new CardDefinition(
            HubRoutePolicy.CARD_RESOURCE,
            "资源",
            HubRoutePolicy.HubCardTaxonomy.SUMMARY_ROUTE,
            CardWeight.STRONG_REAL_CARD,
            false,
            false,
            true,
            FallbackPresentation.ROUTE_TO_DETAIL,
            "待前往资源分台查看"
        ),
        new CardDefinition(
            HubRoutePolicy.CARD_ALCHEMY,
            "炼丹",
            HubRoutePolicy.HubCardTaxonomy.ROUTE_ONLY,
            CardWeight.LIGHT_CARD,
            false,
            false,
            true,
            FallbackPresentation.ROUTE_TO_DETAIL,
            "待前往炼丹分台查看"
        ),
        new CardDefinition(
            HubRoutePolicy.CARD_STORAGE,
            "储物",
            HubRoutePolicy.HubCardTaxonomy.ROUTE_ONLY,
            CardWeight.LIGHT_CARD,
            false,
            false,
            true,
            FallbackPresentation.ROUTE_TO_DETAIL,
            "待前往储物分台查看"
        ),
        new CardDefinition(
            HubRoutePolicy.CARD_CLUSTER,
            "集群",
            HubRoutePolicy.HubCardTaxonomy.ROUTE_ONLY,
            CardWeight.LIGHT_CARD,
            false,
            false,
            true,
            FallbackPresentation.ROUTE_TO_DETAIL,
            "待前往集群分台查看"
        ),
        new CardDefinition(
            HubRoutePolicy.CARD_DAO_MARK,
            "道痕",
            HubRoutePolicy.HubCardTaxonomy.ROUTE_ONLY,
            CardWeight.LIGHT_CARD,
            false,
            false,
            true,
            FallbackPresentation.ROUTE_TO_DETAIL,
            "待前往道痕分台查看"
        )
    );

    private static final Map<String, CardDefinition> CARDS_BY_ID = indexCards();

    private HubCardRegistry() {
    }

    public static List<CardDefinition> cards() {
        return CARDS;
    }

    public static List<String> cardIds() {
        return CARDS.stream().map(CardDefinition::cardId).toList();
    }

    public static List<CardDefinition> strongCards() {
        return CARDS.stream().filter(card -> card.cardWeight() == CardWeight.STRONG_REAL_CARD).toList();
    }

    public static List<CardDefinition> lightCards() {
        return CARDS.stream().filter(card -> card.cardWeight() == CardWeight.LIGHT_CARD).toList();
    }

    public static CardDefinition cardForId(final String cardId) {
        Objects.requireNonNull(cardId, "cardId");
        CardDefinition card = CARDS_BY_ID.get(cardId);
        if (card == null) {
            throw new IllegalArgumentException("未声明 Hub 模块卡: " + cardId);
        }
        return card;
    }

    private static Map<String, CardDefinition> indexCards() {
        LinkedHashMap<String, CardDefinition> cardsById = new LinkedHashMap<>();
        for (CardDefinition card : CARDS) {
            CardDefinition previous = cardsById.putIfAbsent(card.cardId(), card);
            if (previous != null) {
                throw new IllegalStateException("重复声明 Hub 模块卡: " + card.cardId());
            }
        }
        return Map.copyOf(cardsById);
    }

    public enum CardWeight {
        STRONG_REAL_CARD,
        LIGHT_CARD
    }

    public enum FallbackPresentation {
        MARK_UNVERIFIED,
        ROUTE_TO_DETAIL
    }

    public record CardDefinition(
        String cardId,
        String cardTitle,
        HubRoutePolicy.HubCardTaxonomy taxonomy,
        CardWeight cardWeight,
        boolean hasStableDataSource,
        boolean hasStableAggregationSource,
        boolean forbidSyntheticHealthyState,
        FallbackPresentation fallbackPresentation,
        String fallbackText
    ) {

        public CardDefinition {
            Objects.requireNonNull(cardId, "cardId");
            Objects.requireNonNull(cardTitle, "cardTitle");
            Objects.requireNonNull(taxonomy, "taxonomy");
            Objects.requireNonNull(cardWeight, "cardWeight");
            Objects.requireNonNull(fallbackPresentation, "fallbackPresentation");
            Objects.requireNonNull(fallbackText, "fallbackText");

            if (cardId.isBlank()) {
                throw new IllegalArgumentException("cardId 不能为空");
            }
            if (cardTitle.isBlank()) {
                throw new IllegalArgumentException("cardTitle 不能为空");
            }
            if (fallbackText.isBlank()) {
                throw new IllegalArgumentException("fallbackText 不能为空");
            }
            if (!forbidSyntheticHealthyState) {
                throw new IllegalArgumentException("所有 Hub 卡都必须禁止伪造健康态");
            }
            if (taxonomy == HubRoutePolicy.HubCardTaxonomy.REAL_SUMMARY
                && (!hasStableDataSource || !hasStableAggregationSource)) {
                throw new IllegalArgumentException("real-summary 卡必须具备稳定数据源与稳定聚合源");
            }
        }

        public boolean lacksStableSummarySource() {
            return !hasStableDataSource || !hasStableAggregationSource;
        }
    }
}
