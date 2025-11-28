package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

/**
 * 滚动容器控件 - 当内容超出容器高度时支持垂直滚动。
 * 继承自 {@link InteractiveElement}，支持鼠标滚轮交互。
 * <p>
 * 功能：
 * <ul>
 *   <li>垂直滚动支持</li>
 *   <li>鼠标滚轮控制</li>
 *   <li>自动裁剪超出内容</li>
 *   <li>带边框的背景</li>
 * </ul>
 *
 * @see InteractiveElement
 * @see Theme
 */
public final class ScrollContainer extends InteractiveElement {

    /** 每次滚轮滚动的像素数 */
    private static final int SCROLL_STEP = 12;
    /** 边框线条粗细（像素） */
    private static final int BORDER_THICKNESS = 1;

    /** 主题配置 */
    private final Theme theme;
    /** 容器内的内容元素 */
    private UIElement content;
    /** 当前垂直滚动位置（像素） */
    private int scrollY;

    /**
     * 创建滚动容器。
     *
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public ScrollContainer(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 设置容器内容。
     * 如果之前有内容，会先移除旧内容。
     *
     * @param content 新的内容元素（可以为 null 表示清空）
     */
    public void setContent(final UIElement content) {
        if (this.content != null) {
            removeChild(this.content);
        }
        this.content = content;
        if (content != null) {
            addChild(content);
            applyScroll();
        }
    }

    @Override
    public boolean onMouseScroll(final double mouseX, final double mouseY, final double delta) {
        if (!isEnabledAndVisible() || content == null || !isPointInside(mouseX, mouseY)) {
            return false;
        }
        final int maxScroll = Math.max(0, content.getHeight() - getHeight());
        scrollY = clamp(scrollY - (int) (delta * SCROLL_STEP), 0, maxScroll);
        applyScroll();
        return true;
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(),
                theme.getBackgroundColor());
        drawBorder(context);
    }

    /**
     * 绘制容器边框。
     * 使用主题的强调色绘制上、下、左、右四条边框。
     *
     * @param context 渲染上下文
     */
    private void drawBorder(final UIRenderContext context) {
        final int x = getAbsoluteX();
        final int y = getAbsoluteY();
        final int w = getWidth();
        final int h = getHeight();
        final int color = theme.getAccentColor();
        context.drawRect(x, y, w, BORDER_THICKNESS, color); // 上边框
        context.drawRect(x, y + h - BORDER_THICKNESS, w, BORDER_THICKNESS, color); // 下边框
        context.drawRect(x, y, BORDER_THICKNESS, h, color); // 左边框
        context.drawRect(x + w - BORDER_THICKNESS, y, BORDER_THICKNESS, h, color); // 右边框
    }

    /**
     * 应用滚动位置到内容元素。
     * 通过设置内容的 Y 坐标为负值来实现滚动效果。
     */
    private void applyScroll() {
        if (content == null) {
            return;
        }
        content.setFrame(0, -scrollY, getWidth(), content.getHeight());
        content.onLayoutUpdated();
    }

    /**
     * 将值限制在指定范围内。
     *
     * @param value 要限制的值
     * @param min 最小值
     * @param max 最大值
     * @return 限制后的值
     */
    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }
}
