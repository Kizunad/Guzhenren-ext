package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.input.KeyStroke;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 热键捕获控件 - 捕获按键并输出 KeyStroke 的控件，用于快捷键配置。
 * <p>
 * 功能：
 * <ul>
 *   <li>捕获用户按下的键盘按键</li>
 *   <li>支持修饰键（Shift/Ctrl/Alt/Meta）</li>
 *   <li>可设置捕获回调函数</li>
 *   <li>显示当前捕获的按键组合</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * HotkeyCapture capture = new HotkeyCapture(theme);
 * capture.setOnCapture(keyStroke -> {
 *     System.out.println("Captured: " + keyStroke);
 * });
 * }</pre>
 *
 * @see KeyStroke
 * @see InteractiveElement
 */
public final class HotkeyCapture extends InteractiveElement {

    /** 内边距（像素） */
    private static final int PADDING = 4;
    /** 边框线条粗细（像素） */
    private static final int BORDER = 1;

    /** 主题配置 */
    private final Theme theme;
    /** 当前捕获的按键组合 */
    private KeyStroke captured;
    /** 捕获回调函数 */
    private Consumer<KeyStroke> onCapture;

    /**
     * 创建热键捕获控件。
     *
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public HotkeyCapture(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 设置捕获回调函数。
     * 当用户按下按键时，会调用此回调并传入捕获的按键组合。
     *
     * @param onCapture 捕获回调函数（可以为 null）
     */
    public void setOnCapture(final Consumer<KeyStroke> onCapture) {
        this.onCapture = onCapture;
    }

    /**
     * 获取当前捕获的按键组合。
     *
     * @return 捕获的按键组合，如果还未捕获则为 null
     */
    public KeyStroke getCaptured() {
        return captured;
    }

    @Override
    public boolean onKeyPressed(final int keyCode, final int scanCode, final int modifiers) {
        captured = KeyStroke.of(keyCode, modifiers);
        if (onCapture != null) {
            onCapture.accept(captured);
        }
        return true;
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(),
                theme.getBackgroundColor());
        drawBorder(context);
        final String label = captured != null ? captured.toString() : "[Press key]";
        context.drawText(label, getAbsoluteX() + PADDING, getAbsoluteY() + PADDING,
                theme.getTextColor());
    }

    /**
     * 绘制边框。
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
        context.drawRect(x, y, w, BORDER, color); // 上边框
        context.drawRect(x, y + h - BORDER, w, BORDER, color); // 下边框
        context.drawRect(x, y, BORDER, h, color); // 左边框
        context.drawRect(x + w - BORDER, y, BORDER, h, color); // 右边框
    }
}
