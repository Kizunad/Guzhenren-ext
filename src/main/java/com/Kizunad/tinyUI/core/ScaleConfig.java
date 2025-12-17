package com.Kizunad.tinyUI.core;

/**
 * 设计分辨率缩放配置。
 * <p>
 * 允许开发者按照固定的设计分辨率（如 1920x1080）编写 UI 布局，
 * 框架会根据实际屏幕尺寸自动计算缩放因子。
 * </p>
 * <p>
 * 缩放模式：
 * <ul>
 *   <li>{@link ScaleMode#NONE} - 不缩放，使用实际像素</li>
 *   <li>{@link ScaleMode#FIT_WIDTH} - 按宽度缩放，高度可能超出或不足</li>
 *   <li>{@link ScaleMode#FIT_HEIGHT} - 按高度缩放，宽度可能超出或不足</li>
 *   <li>{@link ScaleMode#FIT_MIN} - 取宽高缩放比的最小值，保证内容完全可见</li>
 * </ul>
 * </p>
 */
public final class ScaleConfig {

    /** 默认设计宽度（1080p） */
    public static final int DEFAULT_DESIGN_WIDTH = 1920;
    /** 默认设计高度（1080p） */
    public static final int DEFAULT_DESIGN_HEIGHT = 1080;

    private static final double DEFAULT_SCALE_FACTOR = 1.5;

    /**
     * 缩放模式枚举。
     */
    public enum ScaleMode {
        /** 不缩放，使用实际像素 */
        NONE,
        /** 按宽度缩放 */
        FIT_WIDTH,
        /** 按高度缩放 */
        FIT_HEIGHT,
        /** 取宽高缩放比的最小值，保证内容完全可见（默认） */
        FIT_MIN,
        /** 使用自定义缩放因子 */
        CUSTOM,
    }

    private int designWidth = DEFAULT_DESIGN_WIDTH;
    private int designHeight = DEFAULT_DESIGN_HEIGHT;
    private double scaleFactor = DEFAULT_SCALE_FACTOR;
    private ScaleMode scaleMode = ScaleMode.FIT_MIN;
    private double customScaleFactor = 1.0;

    /**
     * 设置设计分辨率。
     *
     * @param width 设计宽度（像素）
     * @param height 设计高度（像素）
     */
    public void setDesignResolution(final int width, final int height) {
        this.designWidth = Math.max(1, width);
        this.designHeight = Math.max(1, height);
    }

    /**
     * 设置自定义缩放因子（仅在 ScaleMode.CUSTOM 下生效）。
     *
     * @param factor 缩放因子
     */
    public void setCustomScaleFactor(double factor) {
        this.customScaleFactor = factor;
        if (this.scaleMode == ScaleMode.CUSTOM) {
            this.scaleFactor = factor;
        }
    }

    /**
     * 获取设计宽度。
     *
     * @return 设计宽度
     */
    public int getDesignWidth() {
        return designWidth;
    }

    /**
     * 获取设计高度。
     *
     * @return 设计高度
     */
    public int getDesignHeight() {
        return designHeight;
    }

    /**
     * 设置缩放模式。
     *
     * @param mode 缩放模式
     */
    public void setScaleMode(final ScaleMode mode) {
        this.scaleMode = mode != null ? mode : ScaleMode.NONE;
    }

    /**
     * 获取缩放模式。
     *
     * @return 当前缩放模式
     */
    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    /**
     * 根据实际视口尺寸更新缩放因子。
     *
     * @param actualWidth 实际视口宽度
     * @param actualHeight 实际视口高度
     */
    public void updateScale(final int actualWidth, final int actualHeight) {
        if (scaleMode == ScaleMode.NONE) {
            scaleFactor = 1.0;
            return;
        }
        if (scaleMode == ScaleMode.CUSTOM) {
            scaleFactor = customScaleFactor;
            return;
        }
        final double scaleX = (double) actualWidth / designWidth;
        final double scaleY = (double) actualHeight / designHeight;

        switch (scaleMode) {
            case FIT_WIDTH:
                scaleFactor = scaleX;
                break;
            case FIT_HEIGHT:
                scaleFactor = scaleY;
                break;
            case FIT_MIN:
            default:
                scaleFactor = Math.min(scaleX, scaleY);
                break;
        }
    }

    /**
     * 获取当前缩放因子。
     *
     * @return 缩放因子（1.0 = 无缩放）
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * 是否启用了缩放。
     *
     * @return 如果缩放模式不是 NONE 且缩放因子不等于 1.0，则返回 true
     */
    public boolean isScalingEnabled() {
        return scaleMode != ScaleMode.NONE;
    }

    /**
     * 将设计像素转换为实际像素。
     *
     * @param designPixels 设计分辨率下的像素值
     * @return 缩放后的实际像素值
     */
    public int scale(final int designPixels) {
        if (scaleMode == ScaleMode.NONE) {
            return designPixels;
        }
        return (int) Math.round(designPixels * scaleFactor);
    }

    /**
     * 将设计像素转换为实际像素（浮点精度）。
     *
     * @param designPixels 设计分辨率下的像素值
     * @return 缩放后的实际像素值
     */
    public double scaleDouble(final double designPixels) {
        if (scaleMode == ScaleMode.NONE) {
            return designPixels;
        }
        return designPixels * scaleFactor;
    }

    /**
     * 将实际像素转换回设计像素（用于鼠标坐标转换）。
     *
     * @param actualPixels 实际屏幕像素值
     * @return 设计分辨率下的像素值
     */
    public double unscale(final double actualPixels) {
        if (scaleMode == ScaleMode.NONE || scaleFactor <= 0) {
            return actualPixels;
        }
        return actualPixels / scaleFactor;
    }
}
