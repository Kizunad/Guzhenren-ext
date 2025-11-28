package com.Kizunad.tinyUI.layout;

import com.Kizunad.tinyUI.core.UIElement;
import java.util.Objects;

/**
 * 锚点定位工具，用于 HUD/Overlay 固定位置。
 */
public final class Anchor {

    public static final class Spec {

        private final int width;
        private final int height;
        private final Horizontal hAnchor;
        private final Vertical vAnchor;
        private final int offsetX;
        private final int offsetY;

        public Spec(final int width, final int height, final Horizontal hAnchor,
                    final Vertical vAnchor, final int offsetX, final int offsetY) {
            this.width = Math.max(0, width);
            this.height = Math.max(0, height);
            this.hAnchor = Objects.requireNonNull(hAnchor, "hAnchor");
            this.vAnchor = Objects.requireNonNull(vAnchor, "vAnchor");
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Horizontal getHorizontal() {
            return hAnchor;
        }

        public Vertical getVertical() {
            return vAnchor;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }
    }

    public enum Horizontal {
        LEFT, CENTER, RIGHT
    }

    public enum Vertical {
        TOP, CENTER, BOTTOM
    }

    private Anchor() {
    }

    public static void apply(final UIElement element, final int parentWidth, final int parentHeight,
                             final Spec spec) {
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(spec, "spec");
        final int x = computeX(parentWidth, spec.getWidth(), spec.getHorizontal()) + spec.getOffsetX();
        final int y = computeY(parentHeight, spec.getHeight(), spec.getVertical()) + spec.getOffsetY();
        element.setFrame(x, y, spec.getWidth(), spec.getHeight());
        element.onLayoutUpdated();
    }

    public static int computeX(final int parentWidth, final int width, final Horizontal anchor) {
        if (anchor == Horizontal.CENTER) {
            return (parentWidth - width) / 2;
        }
        if (anchor == Horizontal.RIGHT) {
            return parentWidth - width;
        }
        return 0;
    }

    public static int computeY(final int parentHeight, final int height, final Vertical anchor) {
        if (anchor == Vertical.CENTER) {
            return (parentHeight - height) / 2;
        }
        if (anchor == Vertical.BOTTOM) {
            return parentHeight - height;
        }
        return 0;
    }
}
