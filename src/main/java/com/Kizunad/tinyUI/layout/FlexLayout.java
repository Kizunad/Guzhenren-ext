package com.Kizunad.tinyUI.layout;

import com.Kizunad.tinyUI.core.UIElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 单行/列 Flex 布局，实现 grow/shrink 与 gap/padding。
 */
public final class FlexLayout {

    public enum Direction {
        ROW, COLUMN
    }

    private final Direction direction;
    private final int gap;
    private final int padding;

    public FlexLayout(final Direction direction, final int gap, final int padding) {
        this.direction = Objects.requireNonNull(direction, "direction");
        this.gap = Math.max(0, gap);
        this.padding = Math.max(0, padding);
    }

    public void layout(final UIElement container, final Map<UIElement, FlexParams> params) {
        Objects.requireNonNull(container, "container");
        final List<UIElement> children = container.getChildren();
        if (children.isEmpty()) {
            container.onLayoutUpdated();
            return;
        }
        final Map<UIElement, FlexParams> paramMap =
                params == null ? Collections.emptyMap() : params;
        final int availableMain = (direction == Direction.ROW ? container.getWidth()
                : container.getHeight()) - padding * 2 - gap * (children.size() - 1);
        final int availableCross = (direction == Direction.ROW ? container.getHeight()
                : container.getWidth()) - padding * 2;
        int mainUsed = 0;
        int totalGrow = 0;
        int totalShrink = 0;
        final int[] bases = new int[children.size()];
        for (int i = 0; i < children.size(); i++) {
            final UIElement child = children.get(i);
            final FlexParams fp = paramMap.getOrDefault(child, FlexParams.DEFAULT);
            final int basis = fp.getBasis() >= 0
                    ? fp.getBasis()
                    : direction == Direction.ROW ? child.getWidth() : child.getHeight();
            bases[i] = Math.max(0, basis);
            mainUsed += bases[i];
            totalGrow += fp.getFlexGrow();
            totalShrink += fp.getFlexShrink();
        }
        int remaining = availableMain - mainUsed;
        final int[] finalMain = new int[children.size()];
        for (int i = 0; i < children.size(); i++) {
            final FlexParams fp = paramMap.getOrDefault(children.get(i), FlexParams.DEFAULT);
            int size = bases[i];
            if (remaining > 0 && totalGrow > 0 && fp.getFlexGrow() > 0) {
                size += (int) Math.floor((double) remaining * fp.getFlexGrow() / totalGrow);
            } else if (remaining < 0 && totalShrink > 0 && fp.getFlexShrink() > 0) {
                size += (int) Math.ceil((double) remaining * fp.getFlexShrink() / totalShrink);
                size = Math.max(0, size);
            }
            finalMain[i] = size;
        }
        int cursor = padding;
        for (int i = 0; i < children.size(); i++) {
            final UIElement child = children.get(i);
            if (direction == Direction.ROW) {
                child.setFrame(cursor, padding, finalMain[i], Math.max(0, availableCross));
            } else {
                child.setFrame(padding, cursor, Math.max(0, availableCross), finalMain[i]);
            }
            cursor += finalMain[i] + gap;
            child.onLayoutUpdated();
        }
        container.onLayoutUpdated();
    }
}
