package com.Kizunad.tinyUI.core;

/**
 * UI 根节点，持有全局尺寸、缩放配置，并作为输入路由入口。
 * <p>
 * 当启用设计分辨率缩放时，{@link #getWidth()} 和 {@link #getHeight()} 返回设计分辨率尺寸，
 * 而非实际屏幕尺寸，以确保布局计算（如居中）正确工作。
 * </p>
 */
public final class UIRoot extends UIElement {

    private final ScaleConfig scaleConfig = new ScaleConfig();
    /** 缓存实际视口尺寸 */
    private int actualWidth;
    private int actualHeight;

    /**
     * 获取缩放配置。
     *
     * @return 缩放配置对象
     */
    public ScaleConfig getScaleConfig() {
        return scaleConfig;
    }

    /**
     * 设置设计分辨率。
     * 启用后，所有 UI 元素会根据设计分辨率与实际分辨率的比例自动缩放。
     * <p>
     * <b>注意</b>：可在 {@link #setViewport(int, int)} 之前或之后调用，设置后会立即重新计算布局尺寸。
     * </p>
     *
     * @param width 设计宽度（如 1920）
     * @param height 设计高度（如 1080）
     */
    public void setDesignResolution(final int width, final int height) {
        scaleConfig.setDesignResolution(width, height);
        // 如果已有视口尺寸，重新计算
        if (actualWidth > 0 && actualHeight > 0) {
            applyViewport();
        }
    }

    /**
     * 设置视口尺寸（通常为窗口或屏幕大小）。
     * 这个方法会更新缩放因子，并根据缩放配置设置根节点尺寸。
     *
     * @param width 视口宽度（实际像素）
     * @param height 视口高度（实际像素）
     */
    public void setViewport(final int width, final int height) {
        this.actualWidth = width;
        this.actualHeight = height;
        applyViewport();
    }

    /**
     * 根据当前配置应用视口尺寸。
     */
    private void applyViewport() {
        scaleConfig.updateScale(actualWidth, actualHeight);
        // 如果启用缩放，使用设计分辨率作为布局尺寸
        if (scaleConfig.isScalingEnabled()) {
            setFrame(
                0,
                0,
                scaleConfig.getDesignWidth(),
                scaleConfig.getDesignHeight()
            );
        } else {
            setFrame(0, 0, actualWidth, actualHeight);
        }
    }
}
