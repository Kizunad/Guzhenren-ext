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
    /** 滚动条宽度 */
    private static final int SCROLLBAR_WIDTH = 6;
    /** 滚动条滑块颜色 */
    private static final int SCROLLBAR_COLOR = 0xFF888888;
    /** 滚动条背景颜色 */
    private static final int SCROLLBAR_BG_COLOR = 0xFF333333;
    /** 最小滑块高度 */
    private static final int MIN_HANDLE_HEIGHT = 10;
    /** 视口最小尺寸，避免除零 */
    private static final int MIN_VIEWPORT_SIZE = 1;

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
            // 等待容器完成布局后再同步宽度，避免父容器宽度为 0 时把子元素压缩到一起
            requestLayout();
        }
    }

    @Override
    public boolean onMouseScroll(
        final double mouseX,
        final double mouseY,
        final double delta
    ) {
        if (
            !isEnabledAndVisible() ||
            content == null ||
            !isPointInside(mouseX, mouseY)
        ) {
            return false;
        }
        final int maxScroll = Math.max(0, content.getHeight() - getHeight());
        scrollY = clamp(scrollY - (int) (delta * SCROLL_STEP), 0, maxScroll);
        applyScrollIfReady();
        return true;
    }

    @Override
    public boolean onMouseDrag(
        final double mouseX,
        final double mouseY,
        final int button,
        final double dragX,
        final double dragY
    ) {
        if (!isEnabledAndVisible() || content == null) {
            return false;
        }
        final int maxScroll = Math.max(0, content.getHeight() - getHeight());
        if (maxScroll <= 0) {
            return false;
        }

        final int viewportH = getHeight() - BORDER_THICKNESS * 2;
        final int contentH = content.getHeight();
        int handleH = (int) (((float) viewportH / contentH) * viewportH);
        handleH = Math.max(MIN_HANDLE_HEIGHT, Math.min(viewportH, handleH));

        final double trackLen = viewportH - handleH;
        if (trackLen <= 0) {
            return false;
        }

        final double scrollPerPixel = (double) maxScroll / trackLen;
        scrollY = clamp(scrollY + (int) (dragY * scrollPerPixel), 0, maxScroll);
        applyScrollIfReady();
        return true;
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        context.drawRect(
            getAbsoluteX(),
            getAbsoluteY(),
            getWidth(),
            getHeight(),
            theme.getBackgroundColor()
        );
        drawBorder(context);
        drawScrollbar(context);
        // 绘制内容裁剪：仅在视口区域内的子元素才会渲染（参见 shouldRenderChild）
        if (content != null && content.isVisible()) {
            for (UIElement child : content.getChildren()) {
                if (!child.isVisible() || !intersectsViewport(child)) {
                    continue;
                }
                child.render(context, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    protected boolean shouldRenderChild(
        final UIElement child,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        // 主体渲染由 onRender 内部处理，跳过父类默认迭代
        return false;
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
        context.drawRect(
            x,
            y + h - BORDER_THICKNESS,
            w,
            BORDER_THICKNESS,
            color
        ); // 下边框
        context.drawRect(x, y, BORDER_THICKNESS, h, color); // 左边框
        context.drawRect(
            x + w - BORDER_THICKNESS,
            y,
            BORDER_THICKNESS,
            h,
            color
        ); // 右边框
    }

    /**
     * 绘制滚动条。
     *
     * @param context 渲染上下文
     */
    private void drawScrollbar(final UIRenderContext context) {
        if (content == null) {
            return;
        }
        final int x =
            getAbsoluteX() + getWidth() - SCROLLBAR_WIDTH - BORDER_THICKNESS;
        final int y = getAbsoluteY() + BORDER_THICKNESS;
        final int h = getHeight() - BORDER_THICKNESS * 2;
        if (h < MIN_VIEWPORT_SIZE) {
            return;
        }

        // Draw track
        context.drawRect(x, y, SCROLLBAR_WIDTH, h, SCROLLBAR_BG_COLOR);

        final int contentHeight = content.getHeight();
        final boolean scrollable = contentHeight > getHeight();
        int handleH = scrollable
            ? (int) (((float) h / contentHeight) * h)
            : h;
        handleH = Math.max(MIN_HANDLE_HEIGHT, Math.min(h, handleH));

        int handleY = 0;
        if (scrollable) {
            final int maxScroll = contentHeight - getHeight();
            if (maxScroll > 0) {
                handleY =
                    (int) (((float) scrollY / maxScroll) * (h - handleH));
            }
        }

        context.drawRect(
            x,
            y + handleY,
            SCROLLBAR_WIDTH,
            handleH,
            SCROLLBAR_COLOR
        );
    }

    /**
     * 应用滚动位置到内容元素。
     * 通过设置内容的 Y 坐标为负值来实现滚动效果。
     */
    private void applyScrollIfReady() {
        if (content == null) {
            return;
        }
        if (getWidth() <= 0) {
            // 父容器尚未布局出有效宽度时跳过，避免将内容宽度强制写成 0
            return;
        }
        // 保持内容自身布局宽度，避免被 viewport 宽度压缩导致网格重叠
        content.setFrame(0, -scrollY, content.getWidth(), content.getHeight());
        content.onLayoutUpdated();
    }

    @Override
    public void onLayoutUpdated() {
        super.onLayoutUpdated();
        applyScrollIfReady();
    }

    public boolean isChildVisibleInViewport(final UIElement element) {
        final int vx = getAbsoluteX();
        final int vy = getAbsoluteY();
        final int vw = Math.max(MIN_VIEWPORT_SIZE, getWidth());
        final int vh = Math.max(MIN_VIEWPORT_SIZE, getHeight());

        final int ex = element.getAbsoluteX();
        final int ey = element.getAbsoluteY();
        final int ew = Math.max(0, element.getWidth());
        final int eh = Math.max(0, element.getHeight());

        final boolean separated =
            ex + ew <= vx || ex >= vx + vw || ey + eh <= vy || ey >= vy + vh;
        return !separated;
    }

    private boolean intersectsViewport(final UIElement element) {
        return isChildVisibleInViewport(element);
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
