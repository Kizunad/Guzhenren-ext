package com.Kizunad.tinyUI.core;

import com.Kizunad.tinyUI.theme.NinePatch;

/**
 * 渲染接口，封装基础绘制能力以便测试和替换实现。
 */
public interface UIRenderContext {

    void pushState();

    void popState();

    void drawRect(int x, int y, int width, int height, int argbColor);

    void drawText(String text, int x, int y, int argbColor);

    void drawNinePatch(NinePatch patch, int x, int y, int width, int height);
}
