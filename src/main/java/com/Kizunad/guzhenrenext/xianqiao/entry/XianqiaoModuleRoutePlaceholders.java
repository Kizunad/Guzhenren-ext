package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.util.Objects;

public final class XianqiaoModuleRoutePlaceholders {

    private static final String TARGET_MODULE_DAO = "dao";
    private static final String TARGET_SUBVIEW_ENVIRONMENT = "environment";
    private static final String TARGET_FOCUS_DETECTOR = "detector";
    private static final String TARGET_FOCUS_DAOMARK_DISTRIBUTION = "daomark_distribution";

    private static final PlaceholderRoute DAO_DETECTOR = new PlaceholderRoute(
        TARGET_MODULE_DAO,
        TARGET_SUBVIEW_ENVIRONMENT,
        TARGET_FOCUS_DETECTOR,
        "待实现",
        "",
        "道痕检测器入口尚未接线，只保留占位语义。"
    );

    private static final PlaceholderRoute DAOMARK_DISTRIBUTION = new PlaceholderRoute(
        TARGET_MODULE_DAO,
        TARGET_SUBVIEW_ENVIRONMENT,
        TARGET_FOCUS_DAOMARK_DISTRIBUTION,
        "待核验",
        "预留",
        "详细分布暂不伪装成现成页面，当前仍以命令校验与预留语义呈现。"
    );

    private XianqiaoModuleRoutePlaceholders() {
    }

    public static PlaceholderRoute daoDetector() {
        return DAO_DETECTOR;
    }

    public static PlaceholderRoute daomarkDistribution() {
        return DAOMARK_DISTRIBUTION;
    }

    public record PlaceholderRoute(
        String targetModule,
        String targetSubview,
        String targetFocus,
        String primaryState,
        String secondaryState,
        String detail
    ) {

        public PlaceholderRoute {
            targetModule = Objects.requireNonNull(targetModule, "targetModule");
            targetSubview = Objects.requireNonNull(targetSubview, "targetSubview");
            targetFocus = Objects.requireNonNull(targetFocus, "targetFocus");
            primaryState = Objects.requireNonNull(primaryState, "primaryState");
            secondaryState = Objects.requireNonNull(secondaryState, "secondaryState");
            detail = Objects.requireNonNull(detail, "detail");
        }

        public String displayState() {
            if (secondaryState.isEmpty()) {
                return primaryState;
            }
            return primaryState + " / " + secondaryState;
        }

        public String displayTargetPath() {
            return displayTargetModule() + " / " + displayTargetSubview() + " / " + displayTargetFocus();
        }

        private String displayTargetModule() {
            if (TARGET_MODULE_DAO.equals(targetModule)) {
                return "道痕";
            }
            return targetModule;
        }

        private String displayTargetSubview() {
            if (TARGET_SUBVIEW_ENVIRONMENT.equals(targetSubview)) {
                return "环境";
            }
            return targetSubview;
        }

        private String displayTargetFocus() {
            if (TARGET_FOCUS_DETECTOR.equals(targetFocus)) {
                return "检测入口";
            }
            if (TARGET_FOCUS_DAOMARK_DISTRIBUTION.equals(targetFocus)) {
                return "详细分布";
            }
            return targetFocus;
        }
    }
}
