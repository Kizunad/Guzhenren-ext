package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.theme.Theme;
import java.util.function.Consumer;

public final class ToggleButton extends Button {

    private boolean toggled;
    private Consumer<Boolean> onToggle;

    public ToggleButton(final String text, final Theme theme) {
        super(text, theme);
    }

    public void setOnToggle(final Consumer<Boolean> onToggle) {
        this.onToggle = onToggle;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(final boolean toggled) {
        if (this.toggled == toggled) {
            return;
        }
        this.toggled = toggled;
        notifyToggle();
    }

    @Override
    public void setOnClick(final Runnable onClick) {
        // Toggle button uses onToggle; ignore standalone click handler.
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        final boolean handled = super.onMouseRelease(mouseX, mouseY, button);
        if (handled && isPointInside(mouseX, mouseY) && isEnabledAndVisible()) {
            toggled = !toggled;
            notifyToggle();
        }
        return handled;
    }

    @Override
    protected int chooseBackground(final boolean hovered) {
        if (!isEnabled()) {
            return super.chooseBackground(hovered);
        }
        if (toggled) {
            return hovered ? super.chooseBackground(true) : super.chooseBackground(false);
        }
        return super.chooseBackground(hovered);
    }

    private void notifyToggle() {
        final Consumer<Boolean> callback = this.onToggle;
        if (callback != null) {
            callback.accept(toggled);
        }
    }
}
