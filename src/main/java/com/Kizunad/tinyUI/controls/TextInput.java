package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.input.KeyStroke;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 简化文本输入框，支持光标移动、选中、基本编辑与 Ctrl+A 占位快捷键。
 * 不实现剪贴板，Ctrl+C/V 返回未处理。
 */
public final class TextInput extends InteractiveElement {

    public static final int KEY_LEFT = 263;
    public static final int KEY_RIGHT = 262;
    public static final int KEY_BACKSPACE = 259;
    public static final int KEY_DELETE = 261;
    public static final int KEY_HOME = 268;
    public static final int KEY_END = 269;

    private static final int CHAR_WIDTH = 6;
    private static final int PADDING = 3;
    private static final int BORDER = 1;

    private final Theme theme;
    private String text = "";
    private int cursor;
    private int selectionStart = -1;
    private Consumer<String> onChange;

    public TextInput(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setText(final String text) {
        this.text = text == null ? "" : text;
        cursor = Math.min(cursor, this.text.length());
        clearSelection();
    }

    public String getText() {
        return text;
    }

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

    private void drawBorder(final UIRenderContext context) {
        final int x = getAbsoluteX();
        final int y = getAbsoluteY();
        final int w = getWidth();
        final int h = getHeight();
        final int color = theme.getAccentColor();
        context.drawRect(x, y, w, BORDER, color);
        context.drawRect(x, y + h - BORDER, w, BORDER, color);
        context.drawRect(x, y, BORDER, h, color);
        context.drawRect(x + w - BORDER, y, BORDER, h, color);
    }

    private void drawText(final UIRenderContext context) {
        final int x = getAbsoluteX() + PADDING;
        final int y = getAbsoluteY() + PADDING;
        context.drawText(text, x, y, theme.getTextColor());
        // 光标渲染占位：绘制一条竖线
        final int cursorX = x + cursor * CHAR_WIDTH;
        context.drawRect(cursorX, y, BORDER, getHeight() - PADDING * 2, theme.getAccentColor());
    }

    private void moveCursor(final int delta) {
        cursor = clamp(cursor + delta, 0, text.length());
        clearSelection();
    }

    private void selectAll() {
        selectionStart = 0;
        cursor = text.length();
    }

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

    private void insert(final String value) {
        if (hasSelection()) {
            deleteSelection();
        }
        text = text.substring(0, cursor) + value + text.substring(cursor);
        cursor += value.length();
        notifyChange();
    }

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

    private boolean hasSelection() {
        return selectionStart >= 0 && selectionStart != cursor;
    }

    private void clearSelection() {
        selectionStart = -1;
    }

    private void notifyChange() {
        if (onChange != null) {
            onChange.accept(text);
        }
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }
}
