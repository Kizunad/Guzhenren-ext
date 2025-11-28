package com.Kizunad.tinyUI.core;

/**
 * UI 根节点，持有全局尺寸并作为输入路由入口。
 */
public final class UIRoot extends UIElement {

    public void setViewport(final int width, final int height) {
        setFrame(0, 0, width, height);
    }
}
