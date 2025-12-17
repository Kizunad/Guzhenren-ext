package com.Kizunad.guzhenrenext.kongqiao.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 羚魂蛊灵觉：客户端提示状态。
 * <p>
 * 服务端只下发“方向 + 强度 + 持续时间”；客户端负责衰减与渲染。
 * </p>
 */
@OnlyIn(Dist.CLIENT)
public final class LingHunGuIntuitionClientState {

    private static float angleRadians = 0.0F;
    private static float intensity = 0.0F;
    private static int remainingTicks = 0;
    private static int totalTicks = 0;

    private LingHunGuIntuitionClientState() {
    }

    public static void trigger(
        final float angle,
        final float strength,
        final int durationTicks
    ) {
        angleRadians = angle;
        intensity = clamp01(strength);
        remainingTicks = Math.max(1, durationTicks);
        totalTicks = remainingTicks;
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static boolean isActive() {
        return remainingTicks > 0 && intensity > 0.0F;
    }

    public static float angleRadians() {
        return angleRadians;
    }

    /**
     * 当前帧透明度系数（0..1），包含“强度”与“时间衰减”。
     */
    public static float alphaFactor() {
        if (!isActive()) {
            return 0.0F;
        }
        final float fade = totalTicks <= 0 ? 1.0F : (float) remainingTicks / (float) totalTicks;
        return intensity * fade;
    }

    private static float clamp01(final float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        if (value > 1.0F) {
            return 1.0F;
        }
        return value;
    }
}

