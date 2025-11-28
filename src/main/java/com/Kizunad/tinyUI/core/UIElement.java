package com.Kizunad.tinyUI.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 基础 UI 元素，负责树结构、可见性、尺寸与位置。
 */
public abstract class UIElement {

    private final List<UIElement> children = new ArrayList<>();
    private UIElement parent;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean visible = true;
    private boolean enabled = true;
    private boolean needsLayout = true;

    public final void addChild(final UIElement child) {
        Objects.requireNonNull(child, "child");
        if (child == this) {
            throw new IllegalArgumentException("element cannot be its own child");
        }
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        children.add(child);
        child.parent = this;
        child.onAttachedToParent(this);
        requestLayout();
    }

    public final void removeChild(final UIElement child) {
        if (children.remove(child)) {
            child.onDetachedFromParent(this);
            child.parent = null;
            requestLayout();
        }
    }

    public final void clearChildren() {
        if (children.isEmpty()) {
            return;
        }
        for (final UIElement child : new ArrayList<>(children)) {
            removeChild(child);
        }
    }

    public final List<UIElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public UIElement getParent() {
        return parent;
    }

    public final void setFrame(final int x, final int y, final int width, final int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        requestLayout();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getAbsoluteX() {
        return parent == null ? x : parent.getAbsoluteX() + x;
    }

    public int getAbsoluteY() {
        return parent == null ? y : parent.getAbsoluteY() + y;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public final boolean isEnabledAndVisible() {
        return enabled && visible;
    }

    public boolean isPointInside(final double px, final double py) {
        final int ax = getAbsoluteX();
        final int ay = getAbsoluteY();
        return px >= ax && py >= ay && px < ax + width && py < ay + height;
    }

    public final void render(final UIRenderContext context, final double mouseX, final double mouseY,
                             final float partialTicks) {
        if (!isVisible()) {
            return;
        }
        onRender(context, mouseX, mouseY, partialTicks);
        for (final UIElement child : children) {
            child.render(context, mouseX, mouseY, partialTicks);
        }
    }

    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        // 默认无绘制，交由子类实现。
    }

    public void onLayoutUpdated() {
        needsLayout = false;
    }

    public final boolean needsLayout() {
        return needsLayout;
    }

    public final void requestLayout() {
        needsLayout = true;
        if (parent != null) {
            parent.requestLayout();
        }
    }

    protected void onAttachedToParent(final UIElement newParent) {
        // 钩子：子类可覆写。
    }

    protected void onDetachedFromParent(final UIElement oldParent) {
        // 钩子：子类可覆写。
    }
}
