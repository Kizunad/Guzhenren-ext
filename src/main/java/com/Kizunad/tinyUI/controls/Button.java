package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;
import net.minecraft.network.chat.Component;

/**
 * 按钮控件 - 支持点击交互的基础 UI 组件。
 * <p>
 * 功能：
 * <ul>
 *   <li>显示文本标签</li>
 *   <li>支持鼠标悬停、按下、释放交互</li>
 *   <li>可自定义主题颜色</li>
 *   <li>可设置点击回调函数</li>
 * </ul>
 * <p>
 * 状态：
 * <ul>
 *   <li>正常 - 显示背景色</li>
 *   <li>悬停 - 显示主题色</li>
 *   <li>按下 - 显示强调色</li>
 *   <li>禁用 - 显示背景色（不响应交互）</li>
 * </ul>
 *
 * @see InteractiveElement
 * @see Theme
 */
public class Button extends InteractiveElement {

    /** 边框线条粗细（像素） */
    private static final int BORDER_THICKNESS = 1;
    /** 文本缩放系数 */
    private static final float LABEL_SCALE = 1.5F;

    /** 按钮文本 */
    private Component text;
    /** 主题配置 */
    private Theme theme;
    /** 点击回调函数 */
    private Runnable onClick;
    /** 是否处于按下状态 */
    private boolean pressed;

    /**
     * 创建按钮。
     *
     * @param text 按钮文本（如果为 null 则使用空组件）
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public Button(final Component text, final Theme theme) {
        this.text = Objects.requireNonNullElse(text, Component.empty());
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 创建按钮（兼容字符串）。
     *
     * @param text 按钮文本（如果为 null 则使用空字符串）
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public Button(final String text, final Theme theme) {
        this(Component.literal(Objects.requireNonNullElse(text, "")), theme);
    }

    /**
     * 设置按钮文本。
     *
     * @param text 新的文本内容（如果为 null 则使用空字符串）
     */
    public void setText(final String text) {
        this.text = Component.literal(Objects.requireNonNullElse(text, ""));
    }

    /**
     * 设置按钮文本。
     *
     * @param text 新的文本组件（如果为 null 则使用空组件）
     */
    public void setText(final Component text) {
        this.text = Objects.requireNonNullElse(text, Component.empty());
    }

    /**
     * 设置按钮主题。
     *
     * @param theme 新的主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public void setTheme(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 设置点击回调函数。
     * 当用户在按钮上按下并释放鼠标时，会调用此回调。
     *
     * @param onClick 点击回调函数（可以为 null）
     */
    public void setOnClick(final Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (!isEnabledAndVisible() || !isPointInside(mouseX, mouseY)) {
            return false;
        }
        pressed = true;
        return true;
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        if (!pressed) {
            return false;
        }
        final boolean inside = isPointInside(mouseX, mouseY);
        pressed = false;
        if (inside && onClick != null && isEnabledAndVisible()) {
            onClick.run();
            return true;
        }
        return inside;
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        final boolean hovered = isPointInside(mouseX, mouseY);
        final int bgColor = chooseBackground(hovered);
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), bgColor);
        drawBorder(context);
        drawLabel(context);
    }

    /**
     * 根据按钮状态选择背景颜色。
     *
     * @param hovered 鼠标是否悬停在按钮上
     * @return ARGB 格式的背景颜色
     */
    protected int chooseBackground(final boolean hovered) {
        if (!isEnabled()) {
            return theme.getBackgroundColor();
        }
        if (pressed) {
            return theme.getAccentColor();
        }
        if (hovered) {
            return theme.getPrimaryColor();
        }
        return theme.getBackgroundColor();
    }

    /**
     * 绘制按钮边框。
     * 使用主题的强调色绘制上、下、左、右四条边框线。
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
     * 绘制按钮文本标签。
     * 文本位置会根据内边距进行偏移。
     *
     * @param context 渲染上下文
     */
    private void drawLabel(final UIRenderContext context) {
        final int textWidth = context.measureTextWidth(text);
        final int textHeight = context.getFontLineHeight();
        final int scaledWidth = Math.max(0, Math.round(textWidth * LABEL_SCALE));
        final int scaledHeight = Math.max(0, Math.round(textHeight * LABEL_SCALE));
        final int centerX = getAbsoluteX() + getWidth() / 2;
        final int centerY = getAbsoluteY() + getHeight() / 2;
        final int drawX = centerX - scaledWidth / 2;
        final int drawY = centerY - scaledHeight / 2;
        context.drawTextScaled(text, drawX, drawY, theme.getTextColor(), LABEL_SCALE);
    }
}
