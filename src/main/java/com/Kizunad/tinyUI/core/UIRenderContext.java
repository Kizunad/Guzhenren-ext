package com.Kizunad.tinyUI.core;

import com.Kizunad.tinyUI.theme.NinePatch;
import net.minecraft.network.chat.Component;

/**
 * 渲染接口，封装基础绘制能力以便测试和替换实现。
 */
public interface UIRenderContext {

    /**
     * 保存当前渲染状态（例如：变换矩阵、裁剪区域）到堆栈。
     * 必须与 {@link #popState()} 配对使用。
     */
    void pushState();

    /**
     * 从堆栈恢复之前保存的渲染状态。
     * 必须与 {@link #pushState()} 配对使用。
     */
    void popState();

    /**
     * 绘制实心矩形。
     *
     * @param x 矩形左上角 X 坐标（屏幕坐标）
     * @param y 矩形左上角 Y 坐标（屏幕坐标）
     * @param width 矩形宽度（像素）
     * @param height 矩形高度（像素）
     * @param argbColor 颜色值（ARGB 格式，例如 0xFFFFFFFF 为白色）
     */
    void drawRect(int x, int y, int width, int height, int argbColor);

    /**
     * 绘制文本。
     *
     * @param text 要绘制的文本内容
     * @param x 文本起始 X 坐标（屏幕坐标）
     * @param y 文本起始 Y 坐标（屏幕坐标）
     * @param argbColor 文本颜色（ARGB 格式）
     */
    void drawText(String text, int x, int y, int argbColor);

    /**
     * 绘制富文本组件。
     *
     * @param text 要绘制的文本组件
     * @param x 文本起始 X 坐标（屏幕坐标）
     * @param y 文本起始 Y 坐标（屏幕坐标）
     * @param argbColor 文本颜色（ARGB 格式）
     */
    void drawText(Component text, int x, int y, int argbColor);

    /**
     * 获取文本宽度（像素）。
     *
     * @param text 文本组件
     * @return 渲染后的像素宽度
     */
    int measureTextWidth(Component text);

    /**
     * 获取字体行高。
     *
     * @return 当前字体的行高（像素）
     */
    int getFontLineHeight();

    /**
     * 缩放绘制文本。
     *
     * @param text 文本组件
     * @param x 起始 X 坐标
     * @param y 起始 Y 坐标
     * @param argbColor 颜色
     * @param scale 缩放系数
     */
    void drawTextScaled(Component text, int x, int y, int argbColor, float scale);

    /**
     * 绘制九宫格纹理（可缩放的边框/背景）。
     * 九宫格将纹理分为9个区域，边角保持原始大小，边缘和中心区域可拉伸。
     *
     * @param patch 九宫格配置（包含纹理路径和切片参数）
     * @param x 目标区域左上角 X 坐标
     * @param y 目标区域左上角 Y 坐标
     * @param width 目标区域宽度
     * @param height 目标区域高度
     */
    void drawNinePatch(NinePatch patch, int x, int y, int width, int height);

    /**
     * 缩放绘制字符串（默认实现转换为组件）。
     *
     * @param text 字符串文本
     * @param x 起始 X
     * @param y 起始 Y
     * @param argbColor 颜色
     * @param scale 缩放
     */
    default void drawTextScaled(String text, int x, int y, int argbColor, float scale) {
        drawTextScaled(Component.literal(text), x, y, argbColor, scale);
    }

    /**
     * 计算字符串宽度（默认实现转换为组件）。
     *
     * @param text 字符串文本
     * @return 像素宽度
     */
    default int measureTextWidth(String text) {
        return measureTextWidth(Component.literal(text));
    }
}
