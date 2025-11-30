package com.Kizunad.tinyUI.theme;

/**
 * 固定主题配置，包含颜色与尺寸。
 */
public final class Theme {

    private static final int DEFAULT_PRIMARY = 0xFF6E8CD5;
    private static final int DEFAULT_ACCENT = 0xFF9DC1F0;
    private static final int DEFAULT_BACKGROUND = 0xCC1E1E1E;
    private static final int DEFAULT_TEXT = 0xFFFFFFFF;
    private static final int DEFAULT_CORNER_RADIUS = 4;
    private static final int DEFAULT_PADDING = 6;
    private static final int DEFAULT_FONT_SIZE = 12;

    private final int primaryColor;
    private final int accentColor;
    private final int backgroundColor;
    private final int textColor;
    private final int cornerRadius;
    private final int padding;
    private final int fontSize;

    public Theme(final int primaryColor, final int accentColor, final int backgroundColor,
                 final int textColor, final int cornerRadius, final int padding,
                 final int fontSize) {
        this.primaryColor = primaryColor;
        this.accentColor = accentColor;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.cornerRadius = Math.max(0, cornerRadius);
        this.padding = Math.max(0, padding);
        this.fontSize = Math.max(0, fontSize);
    }

    public static Theme vanilla() {
        return new Theme(
                DEFAULT_PRIMARY,
                DEFAULT_ACCENT,
                DEFAULT_BACKGROUND,
                DEFAULT_TEXT,
                DEFAULT_CORNER_RADIUS,
                DEFAULT_PADDING,
                DEFAULT_FONT_SIZE
        );
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public int getPadding() {
        return padding;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getFieldBackgroundColor() {
        // Slightly darker than background for fields
        return backgroundColor & FIELD_MASK; 
    }

    public int getBorderColor() {
        return DEFAULT_BORDER_COLOR; 
    }

    private static final int FIELD_MASK = 0xFEFEFEFE;
    private static final int DEFAULT_BORDER_COLOR = 0xFF3F3F46;
}
