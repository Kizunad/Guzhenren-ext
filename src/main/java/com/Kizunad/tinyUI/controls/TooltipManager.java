package com.Kizunad.tinyUI.controls;

import java.util.Objects;
import java.util.Optional;

/**
 * 管理 Tooltip 显示的延时与位置，逻辑独立于渲染。
 */
public final class TooltipManager {

    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 12;

    private final double delaySeconds;
    private double timer;
    private boolean active;
    private String pendingText = "";
    private TooltipState activeState;

    public TooltipManager(final double delaySeconds) {
        this.delaySeconds = Math.max(0.0d, delaySeconds);
    }

    public void update(final String text, final boolean hovering, final int mouseX, final int mouseY,
                       final double deltaSeconds) {
        final double delta = Math.max(0.0d, deltaSeconds);
        if (!hovering || text == null || text.isEmpty()) {
            reset();
            return;
        }
        final String next = text;
        if (!Objects.equals(next, pendingText)) {
            pendingText = next;
            timer = 0.0d;
            active = false;
        }
        timer += delta;
        if (timer >= delaySeconds) {
            active = true;
            activeState = new TooltipState(next, mouseX + OFFSET_X, mouseY + OFFSET_Y);
        }
    }

    public Optional<TooltipState> getActiveState() {
        if (active && activeState != null) {
            return Optional.of(activeState);
        }
        return Optional.empty();
    }

    public void reset() {
        timer = 0.0d;
        active = false;
        pendingText = "";
        activeState = null;
    }

    public static final class TooltipState {

        private final String text;
        private final int x;
        private final int y;

        TooltipState(final String text, final int x, final int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }

        public String getText() {
            return text;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
