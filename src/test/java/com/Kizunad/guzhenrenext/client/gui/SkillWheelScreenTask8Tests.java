package com.Kizunad.guzhenrenext.client.gui;

import com.Kizunad.guzhenrenext.kongqiao.client.ui.KongqiaoTask8UiText;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SkillWheelScreenTask8Tests {

    @Test
    void projectionWarningShowsStableHintWithoutInventingBlock() {
        KongqiaoPressureProjection projection = new KongqiaoPressureProjection(
            12.0D,
            12.0D,
            24.0D,
            4.0D,
            4.0D,
            1.0D,
            2.0D,
            1.0D,
            0,
            "",
            0,
            0,
            "中等",
            3,
            2,
            3,
            1,
            4
        );

        KongqiaoTask8UiText.SkillWheelWarning warning =
            KongqiaoTask8UiText.buildSkillWheelWarning(projection);

        assertEquals("空窍稳定：当前压力 12.0/24.0", warning.summary());
        assertEquals("档位 稳定", warning.detail());
    }

    @Test
    void projectionWarningShowsOverloadBlockContextFromSyncedProjection() {
        KongqiaoPressureProjection projection = new KongqiaoPressureProjection(
            29.0D,
            29.0D,
            24.0D,
            6.0D,
            11.0D,
            3.0D,
            5.0D,
            4.0D,
            3,
            "passive_overload",
            2,
            2,
            "上等",
            4,
            3,
            4,
            1,
            5
        );

        KongqiaoTask8UiText.SkillWheelWarning warning =
            KongqiaoTask8UiText.buildSkillWheelWarning(projection);

        assertTrue(warning.summary().contains("空窍失控"));
        assertTrue(warning.summary().contains("当前空窍压力过高"));
        assertEquals("被动因超压被强制停用 2 项", warning.detail());
    }
}
