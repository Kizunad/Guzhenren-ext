package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection;
import java.util.Set;

public final class KongqiaoTask8UiText {

    private KongqiaoTask8UiText() {}

    public static KongqiaoUiProjectionText.ProjectionPanelState buildProjectionPanelState(
        final KongqiaoPressureProjection projection
    ) {
        return KongqiaoUiProjectionText.buildProjectionPanelState(projection);
    }

    public static String buildNianTouDefaultImpactText(
        final int identifyProgressFatigueDebt,
        final int deriveShazhaoFatigueDebt
    ) {
        return "稳定影响: 鉴定推进 +"
            + identifyProgressFatigueDebt
            + "；推演尝试 +"
            + deriveShazhaoFatigueDebt;
    }

    public static String buildNianTouStabilitySnapshotText(
        final KongqiaoPressureProjection projection
    ) {
        final KongqiaoPressureProjection safeProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
        final StringBuilder builder = new StringBuilder()
            .append("当前空窍: 压力 ")
            .append(KongqiaoUiProjectionText.formatPressureValue(
                safeProjection.totalPressure()
            ))
            .append("/")
            .append(KongqiaoUiProjectionText.formatPressureValue(
                safeProjection.pressureCap()
            ))
            .append(" | 档位 ")
            .append(KongqiaoUiProjectionText.overloadTierName(
                safeProjection.overloadTier()
            ))
            .append("\n疲劳债 ")
            .append(KongqiaoUiProjectionText.formatPressureValue(
                safeProjection.fatigueDebt()
            ));
        if ("passive_overload".equals(safeProjection.blockedReason())) {
            builder
                .append(" | 被动超压停用 ")
                .append(safeProjection.forcedDisabledCount())
                .append(" 项");
        } else if (safeProjection.overloadTier() >= 2) {
            builder.append(" | 当前已超压，继续鉴定/推演会更险");
        }
        return builder.toString();
    }

    public static String buildTweakProjectionSummaryText(
        final KongqiaoPressureProjection projection
    ) {
        final KongqiaoPressureProjection safeProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
        return "空窍: "
            + KongqiaoUiProjectionText.formatPressureValue(
                safeProjection.totalPressure()
            )
            + "/"
            + KongqiaoUiProjectionText.formatPressureValue(
                safeProjection.pressureCap()
            )
            + " | "
            + KongqiaoUiProjectionText.overloadTierName(
                safeProjection.overloadTier()
            )
            + "\n阻断: "
            + KongqiaoUiProjectionText.buildBlockedSummaryText(safeProjection)
                .replace("阻断摘要: ", "");
    }

    public static String buildTweakWheelGateText(
        final KongqiaoPressureProjection projection
    ) {
        final KongqiaoPressureProjection safeProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
        if (safeProjection.overloadTier() >= 2) {
            return "轮盘门禁: 当前空窍已超压，继续加入会被拒绝";
        }
        if (safeProjection.overloadTier() == 1) {
            return "轮盘门禁: 当前紧绷，继续加入后更易超压";
        }
        return "轮盘门禁: 当前可加入轮盘";
    }

    public static PassiveRuntimeView buildPassiveRuntimeView(
        final boolean preferredEnabled,
        final boolean runtimeActive,
        final Set<String> forcedDisabledUsageIds,
        final String usageId,
        final KongqiaoPressureProjection projection
    ) {
        final String preferenceText = preferredEnabled ? "开启" : "关闭";
        if (!preferredEnabled) {
            return new PassiveRuntimeView(preferenceText, "按偏好关闭");
        }
        final Set<String> safeForcedDisabledUsageIds = forcedDisabledUsageIds == null
            ? Set.of()
            : forcedDisabledUsageIds;
        if (usageId != null && safeForcedDisabledUsageIds.contains(usageId)) {
            return new PassiveRuntimeView(preferenceText, "超压强停");
        }
        if (runtimeActive) {
            return new PassiveRuntimeView(preferenceText, "运行中");
        }
        final KongqiaoPressureProjection safeProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
        if (safeProjection.overloadTier() >= 2) {
            return new PassiveRuntimeView(preferenceText, "高压待机");
        }
        return new PassiveRuntimeView(preferenceText, "未在运行");
    }

    public static SkillWheelWarning buildSkillWheelWarning(
        final KongqiaoPressureProjection projection
    ) {
        final KongqiaoPressureProjection safeProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
        final String baseDetail = "当前压力 "
            + KongqiaoUiProjectionText.formatPressureValue(
                safeProjection.totalPressure()
            )
            + "/"
            + KongqiaoUiProjectionText.formatPressureValue(
                safeProjection.pressureCap()
            );
        if (safeProjection.overloadTier() >= 2) {
            return new SkillWheelWarning(
                "空窍"
                    + KongqiaoUiProjectionText.overloadTierName(
                        safeProjection.overloadTier()
                    )
                    + "：轮盘触发可能收到“当前空窍压力过高”提示",
                KongqiaoUiProjectionText.buildBlockedSummaryText(safeProjection)
                    .replace("阻断摘要: ", "")
            );
        }
        if (safeProjection.overloadTier() == 1) {
            return new SkillWheelWarning(
                "空窍紧绷：继续触发主动更易踏入超压",
                baseDetail
            );
        }
        return new SkillWheelWarning(
            "空窍稳定：" + baseDetail,
            "档位 "
                + KongqiaoUiProjectionText.overloadTierName(
                    safeProjection.overloadTier()
                )
        );
    }

    public record PassiveRuntimeView(String preferenceText, String runtimeText) {}

    public record SkillWheelWarning(String summary, String detail) {}
}
