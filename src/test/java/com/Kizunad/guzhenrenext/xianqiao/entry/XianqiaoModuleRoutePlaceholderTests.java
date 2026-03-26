package com.Kizunad.guzhenrenext.xianqiao.entry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class XianqiaoModuleRoutePlaceholderTests {

    @Test
    void detectorRouteUsesExplicitDeferredPlaceholder() {
        XianqiaoModuleRoutePlaceholders.PlaceholderRoute detector = XianqiaoModuleRoutePlaceholders.daoDetector();

        assertEquals("dao", detector.targetModule());
        assertEquals("environment", detector.targetSubview());
        assertEquals("detector", detector.targetFocus());
        assertEquals("待实现", detector.primaryState());
        assertEquals("", detector.secondaryState());
        assertEquals("待实现", detector.displayState());
        assertEquals("道痕 / 环境 / 检测入口", detector.displayTargetPath());
        assertFalse(detector.displayTargetPath().contains("dao"));
        assertTrue(detector.detail().contains("占位语义"));
    }

    @Test
    void daomarkDistributionRouteUsesVerifyAndReservedPlaceholder() {
        XianqiaoModuleRoutePlaceholders.PlaceholderRoute distribution =
            XianqiaoModuleRoutePlaceholders.daomarkDistribution();

        assertEquals("dao", distribution.targetModule());
        assertEquals("environment", distribution.targetSubview());
        assertEquals("daomark_distribution", distribution.targetFocus());
        assertEquals("待核验", distribution.primaryState());
        assertEquals("预留", distribution.secondaryState());
        assertEquals("待核验 / 预留", distribution.displayState());
        assertEquals("道痕 / 环境 / 详细分布", distribution.displayTargetPath());
        assertFalse(distribution.displayTargetPath().contains("environment"));
        assertTrue(distribution.detail().contains("命令校验"));
        assertTrue(distribution.detail().contains("预留"));
    }
}
