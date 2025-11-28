package com.Kizunad.tinyUI.theme;

import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

/**
 * 九宫格绘制助手 - 若未提供纹理则回退为纯色背景。
 * <p>
 * 功能：
 * <ul>
 *   <li>绘制九宫格纹理（如果提供）</li>
 *   <li>自动回退到纯色背景</li>
 *   <li>验证纹理路径有效性</li>
 * </ul>
 *
 * @see NinePatch
 * @see Theme
 */
public final class NinePatchRenderer {

    /** 主题配置（用于回退背景色） */
    private final Theme theme;

    /**
     * 创建九宫格绘制助手。
     *
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public NinePatchRenderer(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 绘制九宫格或回退到纯色背景。
     * 如果纹理路径有效，绘制九宫格；否则绘制纯色矩形。
     *
     * @param context 渲染上下文（不能为 null）
     * @param patch 九宫格配置（可以为 null，会回退到纯色）
     * @param x 目标区域左上角 X 坐标
     * @param y 目标区域左上角 Y 坐标
     * @param width 目标区域宽度
     * @param height 目标区域高度
     * @throws NullPointerException 如果 context 为 null
     */
    public void render(final UIRenderContext context, final NinePatch patch,
                       final int x, final int y, final int width, final int height) {
        Objects.requireNonNull(context, "context");
        if (isValidPatch(patch)) {
            context.drawNinePatch(patch, x, y, width, height);
        } else {
            context.drawRect(x, y, width, height, theme.getBackgroundColor());
        }
    }

    /**
     * 检查九宫格配置是否有效。
     * 有效的配置必须不为 null且具有非空的纹理路径。
     *
     * @param patch 要检查的九宫格配置
     * @return true 如果配置有效，false 否则
     */
    private boolean isValidPatch(final NinePatch patch) {
        return patch != null && patch.getTexturePath() != null && !patch.getTexturePath().isEmpty();
    }
}
