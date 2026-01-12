package com.Kizunad.guzhenrenext.kongqiao.flyingsword.integration.domain;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;

/**
 * 领域速度修正（Phase 2 占位）。
 */
public final class SwordSpeedModifiers {

    private SwordSpeedModifiers() {}

    public static double computeDomainSpeedScale(FlyingSwordEntity sword) {
        // Phase 2：无领域系统，返回 1.0；Phase 3 迁入后再接入。
        return 1.0;
    }
}
