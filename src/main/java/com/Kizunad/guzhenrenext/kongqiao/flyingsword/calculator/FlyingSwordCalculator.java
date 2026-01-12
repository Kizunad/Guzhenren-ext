package com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.context.CalcContexts;

/**
 * 飞剑数值计算（Phase 2 最小版）。
 */
public final class FlyingSwordCalculator {

    private FlyingSwordCalculator() {}

    public static double effectiveSpeedBase(double base, CalcContexts ctx) {
        if (ctx == null) {
            return base;
        }
        return base;
    }

    public static double effectiveSpeedMax(double max, CalcContexts ctx) {
        if (ctx == null) {
            return max;
        }
        return max;
    }

    public static double effectiveAccel(double accel, CalcContexts ctx) {
        if (ctx == null) {
            return accel;
        }
        return accel;
    }
}
