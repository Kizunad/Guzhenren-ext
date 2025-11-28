package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.input.KeyStroke;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 文本输入框控件 - 简化文本输入框，支持光标移动、选中、基本编辑与 Ctrl+A 占位快捷键。
 * 不实现剪贴板，Ctrl+C/V 返回未处理。
 * <p>
 * 功能：
 * <ul>
 *   <li>基本文本输入和显示</li>
 *   <li>光标移动（左、右、Home、End）</li>
 *   <li>文本选中（Ctrl+A 全选）</li>
 *   <li>文本编辑（插入、删除、退格）</li>
 *   <li>鼠标点击定位光标</li>
 *   <li>文本变化回调</li>
 * </ul>
 * <p>
 * 限制：
 * <ul>
 *   <li>不支持剪贴板操作（Ctrl+C/V/X）</li>
 *   <li>不支持文本滚动（长文本可能被裁剪）</li>
 *   <li>单行文本输入</li>
 * </ul>
 *
 * @see InteractiveElement
 * @see Theme
 */
public final class TextInput extends InteractiveElement {

    /** 左箭头键码 (GLFW_KEY_LEFT) */
    public static final int KEY_LEFT = 263;
    /** 右箭头键码 (GLFW_KEY_RIGHT) */
    public static final int KEY_RIGHT = 262;
    /** 退格键码 (GLFW_KEY_BACKSPACE) */
    public static final int KEY_BACKSPACE = 259;
    /** 删除键码 (GLFW_KEY_DELETE) */
    public static final int KEY_DELETE = 261;
    /** Home 键码 (GLFW_KEY_HOME) */
    public static final int KEY_HOME = 268;
    /** End 键码 (GLFW_KEY_END) */
    public static final int KEY_END = 269;

    /** 字符宽度（像素，简化估算） */
    private static final int CHAR_WIDTH = 6;
    /** 内边距（像素） */
    private static final int PADDING = 3;
    /** 边框线条粗细（像素） */
    private static final int BORDER = 1;

    /** 主题配置 */
    private final Theme theme;
    /** 当前文本内容 */
    private String text = "";
    /** 光标位置（字符索引） */
    private int cursor;
    /** 选中开始位置（-1 表示无选中） */
    private int selectionStart = -1;
    /** 文本变化回调函数 */
    private Consumer<String> onChange;

    /**
     * 创建文本输入框。
     *
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public TextInput(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 设置文本内容。
     * 如果光标位置超出新文本长度，会自动调整。
     *
     * @param text 新的文本内容（null 会被转换为空字符串）
     */
    public void setText(final String text) {
        this.text = text == null ? "" : text;
        cursor = Math.min(cursor, this.text.length());
        clearSelection();
    }

    /**
     * 获取当前文本内容。
     *
     * @return 文本内容
     */
    public String getText() {
        return text;
    }

    /**
     * 设置文本变化回调函数。
     * 当文本内容发生变化时，会调用此回调。
     *
     * @param onChange 变化回调函数（可以为 null）
     */
    public void setOnChange(final Consumer<String> onChange) {
        this.onChange = onChange;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (!isEnabledAndVisible() || !isPointInside(mouseX, mouseY)) {
            return false;
        }
        final int relX = (int) (mouseX - getAbsoluteX() - PADDING);
        cursor = clamp(relX / CHAR_WIDTH, 0, text.length());
        clearSelection();
        return true;
    }

    @Override
    public boolean onKeyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (!isEnabledAndVisible()) {
            return false;
        }
        if ((modifiers & KeyStroke.CONTROL) != 0 && keyCode == 'A') {
            selectAll();
            return true;
        }
        switch (keyCode) {
            case KEY_LEFT:
                moveCursor(-1);
                return true;
            case KEY_RIGHT:
                moveCursor(1);
                return true;
            case KEY_HOME:
                cursor = 0;
                clearSelection();
                return true;
            case KEY_END:
                cursor = text.length();
                clearSelection();
                return true;
            case KEY_BACKSPACE:
                backspace();
                return true;
            case KEY_DELETE:
                delete();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCharTyped(final char codePoint, final int modifiers) {
        if (!isEnabledAndVisible()) {
            return false;
        }
        if (Character.isISOControl(codePoint)) {
            return false;
        }
        insert(String.valueOf(codePoint));
        return true;
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(),
                theme.getBackgroundColor());
        drawBorder(context);
        drawText(context);
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

    /**
     * 绘制文本和光标。
     * 显示当前文本并在光标位置绘制一条竖线。
     *
     * @param context 渲染上下文
     */
    private void drawText(final UIRenderContext context) {
        final int x = getAbsoluteX() + PADDING;
        final int y = getAbsoluteY() + PADDING;
        context.drawText(text, x, y, theme.getTextColor());
        // 光标渲染占位：绘制一条竖线
        final int cursorX = x + cursor * CHAR_WIDTH;
        context.drawRect(cursorX, y, BORDER, getHeight() - PADDING * 2, theme.getAccentColor());
    }

    /**
     * 移动光标。
     * 移动后会清除选中状态。
     *
     * @param delta 移动增量（正数右移，负数左移）
     */
    private void moveCursor(final int delta) {
        cursor = clamp(cursor + delta, 0, text.length());
        clearSelection();
    }

    /**
     * 全选文本。
     * 设置选中开始位置为 0，光标移动到文本末尾。
     */
    private void selectAll() {
        selectionStart = 0;
        cursor = text.length();
    }

    /**
     * 退格操作。
     * 如果有选中文本，删除选中部分；否则删除光标前一个字符。
     */
    private void backspace() {
        if (hasSelection()) {
            deleteSelection();
            return;
        }
        if (cursor <= 0) {
            return;
        }
        text = text.substring(0, cursor - 1) + text.substring(cursor);
        cursor = Math.max(0, cursor - 1);
        notifyChange();
    }

    /**
     * 删除操作。
     * 如果有选中文本，删除选中部分；否则删除光标后一个字符。
     */
    private void delete() {
        if (hasSelection()) {
            deleteSelection();
            return;
        }
        if (cursor >= text.length()) {
            return;
        }
        text = text.substring(0, cursor) + text.substring(cursor + 1);
        notifyChange();
    }

    /**
     * 插入文本。
     * 如果有选中文本，先删除选中部分，然后在光标位置插入新文本。
     *
     * @param value 要插入的文本
     */
    private void insert(final String value) {
        if (hasSelection()) {
            deleteSelection();
        }
        text = text.substring(0, cursor) + value + text.substring(cursor);
        cursor += value.length();
        notifyChange();
    }

    /**
     * 删除选中的文本。
     * 删除选中开始和光标之间的文本，并将光标移动到选中开始位置。
     */
    private void deleteSelection() {
        if (!hasSelection()) {
            return;
        }
        final int start = Math.min(selectionStart, cursor);
        final int end = Math.max(selectionStart, cursor);
        text = text.substring(0, start) + text.substring(end);
        cursor = start;
        clearSelection();
        notifyChange();
    }

    /**
     * 检查是否有选中文本。
     *
     * @return true 如果有选中，false 否则
     */
    private boolean hasSelection() {
        return selectionStart >= 0 && selectionStart != cursor;
    }

    /**
     * 清除选中状态。
     */
    private void clearSelection() {
        selectionStart = -1;
    }

    /**
     * 通知监听者文本已改变。
     * 如果设置了回调函数，则调用它。
     */
    private void notifyChange() {
        if (onChange != null) {
            onChange.accept(text);
        }
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
