package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

/**
 * 统一的矩形背景面板，提供与 TinyUI 主题一致的填充与描边，用于还原 drawio 布局中的容器轮廓。
 */
final class SolidPanel extends UIElement {

    private static final int BORDER = 1;

    private final Theme theme;

    SolidPanel(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
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
        int color = theme.getAccentColor();
        context.drawRect(
            getAbsoluteX(),
            getAbsoluteY(),
            getWidth(),
            BORDER,
            color
        );
        context.drawRect(
            getAbsoluteX(),
            getAbsoluteY() + getHeight() - BORDER,
            getWidth(),
            BORDER,
            color
        );
        context.drawRect(
            getAbsoluteX(),
            getAbsoluteY(),
            BORDER,
            getHeight(),
            color
        );
        context.drawRect(
            getAbsoluteX() + getWidth() - BORDER,
            getAbsoluteY(),
            BORDER,
            getHeight(),
            color
        );
    }
}
