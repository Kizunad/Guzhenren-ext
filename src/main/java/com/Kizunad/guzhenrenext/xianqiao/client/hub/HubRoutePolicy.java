package com.Kizunad.guzhenrenext.xianqiao.client.hub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class HubRoutePolicy {

    public static final String CARD_APERTURE_OVERVIEW = "aperture-overview";

    public static final String CARD_LAND_SPIRIT = "land-spirit";

    public static final String CARD_TRIBULATION = "tribulation";

    public static final String CARD_RESOURCE = "resource";

    public static final String CARD_ALCHEMY = "alchemy";

    public static final String CARD_STORAGE = "storage";

    public static final String CARD_CLUSTER = "cluster";

    public static final String CARD_DAO_MARK = "dao-mark";

    public static final String CURRENT_HUB_OVERVIEW_NOTICE = "当前已在洞天全貌总览";

    public static final String TRIBULATION_PLACEHOLDER_NOTICE = "待前往灾劫子页查看";

    public static final String DAOMARK_PLACEHOLDER_NOTICE = "待前往道痕子页查看";

    public static final String LAND_SPIRIT_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.xianqiao.client.LandSpiritScreen";

    public static final String RESOURCE_CONTROLLER_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.xianqiao.client.ResourceControllerScreen";

    public static final String ALCHEMY_FURNACE_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.xianqiao.client.AlchemyFurnaceScreen";

    public static final String STORAGE_GU_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.xianqiao.client.StorageGuScreen";

    public static final String CLUSTER_NPC_SCREEN_CLASS_NAME =
        "com.Kizunad.guzhenrenext.xianqiao.client.ClusterNpcScreen";

    private static final Map<String, CardRoutePolicy> POLICIES_BY_CARD_ID = createPolicies();

    private static final List<CardRoutePolicy> ORDERED_POLICIES = List.copyOf(POLICIES_BY_CARD_ID.values());

    private HubRoutePolicy() {
    }

    public static CardRoutePolicy routeForCard(String cardId) {
        Objects.requireNonNull(cardId, "cardId");
        CardRoutePolicy policy = POLICIES_BY_CARD_ID.get(cardId);
        if (policy == null) {
            throw new IllegalArgumentException("未声明 Hub 模块卡路由策略: " + cardId);
        }
        return policy;
    }

    public static Map<String, CardRoutePolicy> policiesByCardId() {
        return POLICIES_BY_CARD_ID;
    }

    public static List<CardRoutePolicy> orderedPolicies() {
        return ORDERED_POLICIES;
    }

    public static List<String> approvedDirectScreenClassNames() {
        List<String> screenClassNames = new ArrayList<>();
        for (CardRoutePolicy policy : ORDERED_POLICIES) {
            if (policy.launchesScreen() && !screenClassNames.contains(policy.target().screenClassName())) {
                screenClassNames.add(policy.target().screenClassName());
            }
        }
        return List.copyOf(screenClassNames);
    }

    private static Map<String, CardRoutePolicy> createPolicies() {
        LinkedHashMap<String, CardRoutePolicy> policies = new LinkedHashMap<>();

        register(
            policies,
            new CardRoutePolicy(
                CARD_APERTURE_OVERVIEW,
                "洞天全貌",
                HubCardTaxonomy.REAL_SUMMARY,
                RouteKind.CURRENT_HUB,
                RouteTarget.CURRENT_HUB_OVERVIEW,
                CURRENT_HUB_OVERVIEW_NOTICE
            )
        );
        register(
            policies,
            new CardRoutePolicy(
                CARD_LAND_SPIRIT,
                "地灵",
                HubCardTaxonomy.REAL_SUMMARY,
                RouteKind.DIRECT_SCREEN,
                RouteTarget.LAND_SPIRIT_SCREEN,
                ""
            )
        );
        register(
            policies,
            new CardRoutePolicy(
                CARD_TRIBULATION,
                "灾劫",
                HubCardTaxonomy.REAL_SUMMARY,
                RouteKind.PLACEHOLDER,
                RouteTarget.TRIBULATION_SUB_VIEW,
                TRIBULATION_PLACEHOLDER_NOTICE
            )
        );
        register(
            policies,
            new CardRoutePolicy(
                CARD_RESOURCE,
                "资源",
                HubCardTaxonomy.SUMMARY_ROUTE,
                RouteKind.DIRECT_SCREEN,
                RouteTarget.RESOURCE_CONTROLLER_SCREEN,
                ""
            )
        );
        register(
            policies,
            new CardRoutePolicy(
                CARD_ALCHEMY,
                "炼丹",
                HubCardTaxonomy.ROUTE_ONLY,
                RouteKind.DIRECT_SCREEN,
                RouteTarget.ALCHEMY_FURNACE_SCREEN,
                ""
            )
        );
        register(
            policies,
            new CardRoutePolicy(
                CARD_STORAGE,
                "储物",
                HubCardTaxonomy.ROUTE_ONLY,
                RouteKind.DIRECT_SCREEN,
                RouteTarget.STORAGE_GU_SCREEN,
                ""
            )
        );
        register(
            policies,
            new CardRoutePolicy(
                CARD_CLUSTER,
                "集群",
                HubCardTaxonomy.ROUTE_ONLY,
                RouteKind.DIRECT_SCREEN,
                RouteTarget.CLUSTER_NPC_SCREEN,
                ""
            )
        );
        register(
            policies,
            new CardRoutePolicy(
                CARD_DAO_MARK,
                "道痕",
                HubCardTaxonomy.ROUTE_ONLY,
                RouteKind.PLACEHOLDER,
                RouteTarget.DAOMARK_SUB_VIEW,
                DAOMARK_PLACEHOLDER_NOTICE
            )
        );

        return Collections.unmodifiableMap(new LinkedHashMap<>(policies));
    }

    private static void register(
        LinkedHashMap<String, CardRoutePolicy> policies,
        CardRoutePolicy policy
    ) {
        CardRoutePolicy previous = policies.putIfAbsent(policy.cardId(), policy);
        if (previous != null) {
            throw new IllegalStateException("重复声明 Hub 模块卡路由策略: " + policy.cardId());
        }
    }

    public enum HubCardTaxonomy {
        REAL_SUMMARY("real-summary"),
        SUMMARY_ROUTE("summary-route"),
        ROUTE_ONLY("route-only");

        private final String serializedName;

        HubCardTaxonomy(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }
    }

    public enum RouteKind {
        CURRENT_HUB(RouteSurface.CURRENT_HUB),
        DIRECT_SCREEN(RouteSurface.SCREEN),
        PLACEHOLDER(RouteSurface.PLACEHOLDER);

        private final RouteSurface surface;

        RouteKind(RouteSurface surface) {
            this.surface = surface;
        }

        public RouteSurface surface() {
            return surface;
        }
    }

    public enum RouteSurface {
        CURRENT_HUB,
        SCREEN,
        PLACEHOLDER
    }

    public enum RouteTarget {
        CURRENT_HUB_OVERVIEW(RouteSurface.CURRENT_HUB, "current-hub-overview", "当前 Hub 总览", null),
        LAND_SPIRIT_SCREEN(
            RouteSurface.SCREEN,
            "land-spirit-screen",
            "地灵分台",
            LAND_SPIRIT_SCREEN_CLASS_NAME
        ),
        RESOURCE_CONTROLLER_SCREEN(
            RouteSurface.SCREEN,
            "resource-controller-screen",
            "资源分台",
            RESOURCE_CONTROLLER_SCREEN_CLASS_NAME
        ),
        ALCHEMY_FURNACE_SCREEN(
            RouteSurface.SCREEN,
            "alchemy-furnace-screen",
            "炼丹分台",
            ALCHEMY_FURNACE_SCREEN_CLASS_NAME
        ),
        STORAGE_GU_SCREEN(RouteSurface.SCREEN, "storage-gu-screen", "储物分台", STORAGE_GU_SCREEN_CLASS_NAME),
        CLUSTER_NPC_SCREEN(RouteSurface.SCREEN, "cluster-npc-screen", "集群分台", CLUSTER_NPC_SCREEN_CLASS_NAME),
        TRIBULATION_SUB_VIEW(RouteSurface.PLACEHOLDER, "tribulation-sub-view", "灾劫子页", null),
        DAOMARK_SUB_VIEW(RouteSurface.PLACEHOLDER, "daomark-sub-view", "道痕子页", null);

        private final RouteSurface surface;
        private final String targetId;
        private final String displayName;
        private final String screenClassName;

        RouteTarget(RouteSurface surface, String targetId, String displayName, String screenClassName) {
            this.surface = Objects.requireNonNull(surface, "surface");
            this.targetId = Objects.requireNonNull(targetId, "targetId");
            this.displayName = Objects.requireNonNull(displayName, "displayName");
            this.screenClassName = screenClassName;
        }

        public RouteSurface surface() {
            return surface;
        }

        public String targetId() {
            return targetId;
        }

        public String displayName() {
            return displayName;
        }

        public String screenClassName() {
            return screenClassName;
        }
    }

    public record CardRoutePolicy(
        String cardId,
        String cardTitle,
        HubCardTaxonomy taxonomy,
        RouteKind routeKind,
        RouteTarget target,
        String noticeText
    ) {

        public CardRoutePolicy {
            Objects.requireNonNull(cardId, "cardId");
            Objects.requireNonNull(cardTitle, "cardTitle");
            Objects.requireNonNull(taxonomy, "taxonomy");
            Objects.requireNonNull(routeKind, "routeKind");
            Objects.requireNonNull(target, "target");
            Objects.requireNonNull(noticeText, "noticeText");
            boolean directScreen = routeKind == RouteKind.DIRECT_SCREEN;

            if (cardId.isBlank()) {
                throw new IllegalArgumentException("cardId 不能为空");
            }
            if (cardTitle.isBlank()) {
                throw new IllegalArgumentException("cardTitle 不能为空");
            }
            if (routeKind.surface() != target.surface()) {
                throw new IllegalArgumentException("routeKind 与 target.surface 不匹配");
            }
            if (directScreen && (target.screenClassName() == null || target.screenClassName().isBlank())) {
                throw new IllegalArgumentException("direct screen 策略必须绑定具体 Screen 类型");
            }
            if (!directScreen && target.screenClassName() != null && !target.screenClassName().isBlank()) {
                throw new IllegalArgumentException("非 direct screen 策略不得绑定 Screen 类型");
            }
            if (!directScreen && noticeText.isBlank()) {
                throw new IllegalArgumentException("非 direct screen 策略必须提供明确说明文案");
            }
            if (directScreen && !noticeText.isBlank()) {
                throw new IllegalArgumentException("direct screen 策略不应混入 placeholder 文案");
            }
        }

        public boolean launchesScreen() {
            return routeKind == RouteKind.DIRECT_SCREEN;
        }

        public boolean staysOnHub() {
            return routeKind == RouteKind.CURRENT_HUB;
        }

        public boolean usesPlaceholder() {
            return routeKind == RouteKind.PLACEHOLDER;
        }

        public boolean isSummaryRoute() {
            return taxonomy == HubCardTaxonomy.SUMMARY_ROUTE;
        }

        public boolean isRouteOnly() {
            return taxonomy == HubCardTaxonomy.ROUTE_ONLY;
        }
    }
}
