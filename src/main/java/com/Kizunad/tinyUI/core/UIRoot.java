package com.Kizunad.tinyUI.core;

/**
 * UI 根节点，持有全局尺寸并作为输入路由入口。
 */
public final class UIRoot extends UIElement {

    /**
     * 设置视口尺寸（通常为窗口或屏幕大小）。
     * 这个方法会将根节点的位置设置为 (0, 0)，大小设置为指定的宽高。
     *
     * @param width 视口宽度（像素）
     * @param height 视口高度（像素）
     */
    public void setViewport(final int width, final int height) {
        setFrame(0, 0, width, height);
    }
}
