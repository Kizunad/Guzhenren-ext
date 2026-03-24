package com.Kizunad.guzhenrenext.xianqiao.client.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HubStatusEvaluator {

    private static final long TRIBULATION_WARNING_TICKS = 24000L;

    public HubStatus evaluate(HubSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");

        HubSnapshot.CoreSnapshot core = snapshot.core();
        RiskLevel tribulationRisk = evaluateTribulationRisk(core.tribulationTick());
        RiskLevel completenessRisk = evaluateCompletenessRisk(snapshot);
        RiskLevel frozenRisk = core.frozen() ? RiskLevel.CAUTION : RiskLevel.STABLE;
        RiskLevel boundaryRisk = core.hasValidBoundary() ? RiskLevel.STABLE : RiskLevel.DANGER;

        RiskLevel overallRisk = maxRisk(boundaryRisk, maxRisk(tribulationRisk, maxRisk(completenessRisk, frozenRisk)));

        String overallSummary = buildOverallSummary(snapshot, overallRisk);
        String recommendation = buildRecommendation(
            snapshot,
            overallRisk,
            tribulationRisk,
            completenessRisk,
            frozenRisk
        );
        String fallbackText = buildFallbackText(snapshot);
        String tribulationRiskSummary = buildTribulationRiskSummary(core.tribulationTick(), tribulationRisk);

        return new HubStatus(overallRisk, overallSummary, tribulationRiskSummary, recommendation, fallbackText);
    }

    private RiskLevel evaluateCompletenessRisk(HubSnapshot snapshot) {
        boolean spiritMissing = !snapshot.landSpirit().isAvailable();
        boolean resourceRouteFallback = snapshot.resource().isRouteFallback();
        if (spiritMissing && resourceRouteFallback) {
            return RiskLevel.UNKNOWN;
        }
        if (spiritMissing || resourceRouteFallback) {
            return RiskLevel.CAUTION;
        }
        return RiskLevel.STABLE;
    }

    private RiskLevel evaluateTribulationRisk(long tribulationTick) {
        if (tribulationTick <= 0L) {
            return RiskLevel.DANGER;
        }
        if (tribulationTick <= TRIBULATION_WARNING_TICKS) {
            return RiskLevel.CAUTION;
        }
        return RiskLevel.STABLE;
    }

    private String buildOverallSummary(HubSnapshot snapshot, RiskLevel overallRisk) {
        if (!snapshot.core().hasValidBoundary()) {
            return "洞天边界异常，当前状态不可判定";
        }
        return switch (overallRisk) {
            case DANGER -> "洞天存在高风险，请立即处理灾劫或同步异常";
            case UNKNOWN -> "关键摘要缺失，请先前往分台核验";
            case CAUTION -> "洞天可运行，但存在待确认风险";
            case STABLE -> "洞天核心稳定，可继续当前节奏";
        };
    }

    private String buildRecommendation(
        HubSnapshot snapshot,
        RiskLevel overallRisk,
        RiskLevel tribulationRisk,
        RiskLevel completenessRisk,
        RiskLevel frozenRisk
    ) {
        if (!snapshot.core().hasValidBoundary()) {
            return "请先校验中枢边界同步，再重新打开主界面确认状态";
        }
        if (tribulationRisk == RiskLevel.DANGER) {
            return "立即前往灾劫分台，核验倒计时与当前灾劫阶段";
        }
        if (completenessRisk == RiskLevel.UNKNOWN) {
            return "先补齐地灵摘要，再前往资源分台核验局部数据";
        }
        if (frozenRisk == RiskLevel.CAUTION) {
            return "当前处于冻结态，请确认是否需要解除冻结后再推进";
        }
        if (tribulationRisk == RiskLevel.CAUTION) {
            return "灾劫窗口临近，建议优先准备防护与资源储备";
        }
        if (snapshot.resource().isRouteFallback()) {
            return "资源摘要不可用，请前往资源分台查看详细状态";
        }
        if (!snapshot.landSpirit().isAvailable()) {
            return "地灵摘要缺失，请前往地灵分台补齐关键信息";
        }
        if (overallRisk == RiskLevel.STABLE) {
            return "维持当前节奏，持续观察地灵与灾劫倒计时";
        }
        return "请按主界面分台入口逐项核验异常模块";
    }

    private String buildFallbackText(HubSnapshot snapshot) {
        List<String> policies = new ArrayList<>();
        if (!snapshot.landSpirit().isAvailable()) {
            policies.add(snapshot.landSpirit().fallbackText());
        }
        policies.add(snapshot.resource().fallbackText());
        return String.join("；", policies);
    }

    private String buildTribulationRiskSummary(long tribulationTick, RiskLevel riskLevel) {
        if (riskLevel == RiskLevel.DANGER) {
            return "灾劫计时不可用或已进入进行态";
        }
        if (riskLevel == RiskLevel.CAUTION) {
            return "灾劫窗口临近，请尽快完成准备";
        }
        return "灾劫窗口相对充裕";
    }

    private RiskLevel maxRisk(RiskLevel left, RiskLevel right) {
        return left.priority >= right.priority ? left : right;
    }

    public enum RiskLevel {
        STABLE(0),
        CAUTION(1),
        UNKNOWN(2),
        DANGER(3);

        private final int priority;

        RiskLevel(int priority) {
            this.priority = priority;
        }
    }

    public record HubStatus(
        RiskLevel overallRisk,
        String overallSummary,
        String tribulationRiskSummary,
        String recommendation,
        String fallbackText
    ) {

        public HubStatus {
            Objects.requireNonNull(overallRisk, "overallRisk");
            Objects.requireNonNull(overallSummary, "overallSummary");
            Objects.requireNonNull(tribulationRiskSummary, "tribulationRiskSummary");
            Objects.requireNonNull(recommendation, "recommendation");
            Objects.requireNonNull(fallbackText, "fallbackText");
        }
    }
}
