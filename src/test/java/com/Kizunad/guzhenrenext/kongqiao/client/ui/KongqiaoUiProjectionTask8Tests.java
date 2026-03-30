package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoUiProjectionTask8Tests {

    @Test
    void kongqiaoPanelStateShowsPressureBreakdownTierAndBlockedSummary() {
        KongqiaoPressureProjection projection = new KongqiaoPressureProjection(
            34.5D,
            34.5D,
            28.0D,
            8.0D,
            13.0D,
            4.5D,
            6.0D,
            3.0D,
            2,
            "passive_overload",
            1,
            2,
            "中等",
            3,
            2,
            3,
            1,
            4
        );

        KongqiaoUiProjectionText.ProjectionPanelState state =
            KongqiaoTask8UiText.buildProjectionPanelState(projection);

        assertEquals("总压力/承压上限: 34.5 / 28.0", state.totalLine());
        assertEquals("构成: 驻留 8.0 | 被动 13.0 | 轮盘预载 4.5", state.breakdownLine());
        assertEquals("附加: 爆发 6.0 | 疲劳债 3.0", state.supplementalLine());
        assertEquals("超压档位: 超压 | 强制熄火 2 | 封槽 1", state.overloadLine());
        assertEquals("阻断摘要: 被动因超压被强制停用 2 项", state.blockedLine());
    }

    @Test
    void nianTouSnapshotKeepsTask6ImpactTextAndAddsRuntimeStabilityTruth() {
        KongqiaoPressureProjection projection = new KongqiaoPressureProjection(
            21.0D,
            21.0D,
            20.0D,
            5.0D,
            8.0D,
            2.0D,
            3.0D,
            3.0D,
            2,
            "passive_overload",
            0,
            1,
            "上等",
            4,
            3,
            4,
            1,
            5
        );

        String impactText = KongqiaoTask8UiText.buildNianTouDefaultImpactText(4, 8);
        String snapshotText = KongqiaoTask8UiText.buildNianTouStabilitySnapshotText(projection);

        assertTrue(impactText.contains("鉴定推进 +4"));
        assertTrue(impactText.contains("推演尝试 +8"));
        assertTrue(snapshotText.contains("当前空窍: 压力 21.0/20.0 | 档位 超压"));
        assertTrue(snapshotText.contains("疲劳债 3.0"));
        assertTrue(snapshotText.contains("被动超压停用 1 项"));
    }

    @Test
    void tweakScreenSeparatesPreferenceFromForcedDisabledRuntimeTruth() {
        KongqiaoPressureProjection projection = new KongqiaoPressureProjection(
            26.0D,
            26.0D,
            24.0D,
            6.0D,
            10.0D,
            3.0D,
            4.0D,
            3.0D,
            2,
            "passive_overload",
            0,
            1,
            "中等",
            3,
            2,
            3,
            1,
            4
        );
        KongqiaoTask8UiText.PassiveRuntimeView forcedDisabled =
            KongqiaoTask8UiText.buildPassiveRuntimeView(
                true,
                true,
                Set.of("guzhenren:test_passive"),
                "guzhenren:test_passive",
                projection
            );
        KongqiaoTask8UiText.PassiveRuntimeView preferredOff =
            KongqiaoTask8UiText.buildPassiveRuntimeView(
                false,
                false,
                Set.of("guzhenren:test_passive"),
                "guzhenren:test_passive",
                projection
            );

        assertEquals("开启", forcedDisabled.preferenceText());
        assertEquals("超压强停", forcedDisabled.runtimeText());
        assertEquals("关闭", preferredOff.preferenceText());
        assertEquals("按偏好关闭", preferredOff.runtimeText());
        assertTrue(KongqiaoTask8UiText.buildTweakProjectionSummaryText(projection).contains("空窍: 26.0/24.0 | 超压"));
        assertEquals("轮盘门禁: 当前空窍已超压，继续加入会被拒绝", KongqiaoTask8UiText.buildTweakWheelGateText(projection));
    }
}
