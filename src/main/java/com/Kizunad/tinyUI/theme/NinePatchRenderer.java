package com.Kizunad.tinyUI.theme;

import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

/**
 * 九宫格绘制助手，若未提供纹理则回退为纯色背景。
 */
public final class NinePatchRenderer {

    private final Theme theme;

    public NinePatchRenderer(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void render(final UIRenderContext context, final NinePatch patch,
                       final int x, final int y, final int width, final int height) {
        Objects.requireNonNull(context, "context");
        if (isValidPatch(patch)) {
            context.drawNinePatch(patch, x, y, width, height);
        } else {
            context.drawRect(x, y, width, height, theme.getBackgroundColor());
        }
    }

    private boolean isValidPatch(final NinePatch patch) {
        return patch != null && patch.getTexturePath() != null && !patch.getTexturePath().isEmpty();
    }
}
