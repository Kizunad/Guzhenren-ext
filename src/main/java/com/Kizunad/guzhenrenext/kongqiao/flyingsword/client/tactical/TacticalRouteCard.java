package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public final class TacticalRouteCard extends InteractiveElement {

    private static final int SUMMARY_LINES = 3;

    private final TacticalTheme theme;
    private final TacticalPanel background;
    private final Label titleLabel;
    private final Label summaryLabel;
    private final Label actionLabel;
    private final List<TacticalBadge> badges = new ArrayList<>();

    private TacticalSurface surface = TacticalSurface.RAISED;
    private TacticalTone tone = TacticalTone.INFO;
    private Runnable onClick;
    private boolean pressed;
    private boolean active;

    public TacticalRouteCard(final TacticalTheme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
        this.background = new TacticalPanel(theme);
        this.titleLabel = new Label(Component.empty(), theme.baseTheme());
        this.summaryLabel = new Label(Component.empty(), theme.baseTheme());
        this.actionLabel = new Label(Component.empty(), theme.baseTheme());

        this.titleLabel.setColor(theme.textPrimaryColor());
        this.summaryLabel.setColor(theme.textDimColor());
        this.actionLabel.setColor(theme.badgeTextColor(TacticalTone.INFO));
        this.actionLabel.setHorizontalAlign(Label.HorizontalAlign.RIGHT);

        addChild(background);
        addChild(titleLabel);
        addChild(summaryLabel);
        addChild(actionLabel);
    }

    public void setTitle(final Component title) {
        titleLabel.setText(Objects.requireNonNullElse(title, Component.empty()));
        requestLayout();
    }

    public void setSummary(final Component summary) {
        summaryLabel.setText(Objects.requireNonNullElse(summary, Component.empty()));
        requestLayout();
    }

    public void setActionText(final Component actionText) {
        actionLabel.setText(Objects.requireNonNullElse(actionText, Component.empty()));
        requestLayout();
    }

    public void setTone(final TacticalTone tone) {
        this.tone = Objects.requireNonNull(tone, "tone");
        actionLabel.setColor(theme.badgeTextColor(tone));
    }

    public void setSurface(final TacticalSurface surface) {
        this.surface = Objects.requireNonNull(surface, "surface");
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setOnClick(final Runnable onClick) {
        this.onClick = onClick;
    }

    public void setBadges(final List<TacticalBadgeSpec> specs) {
        final List<TacticalBadgeSpec> safeSpecs =
            specs == null ? List.<TacticalBadgeSpec>of() : specs;
        for (final TacticalBadge badge : new ArrayList<>(badges)) {
            removeChild(badge);
        }
        badges.clear();
        for (final TacticalBadgeSpec spec : safeSpecs) {
            final TacticalBadge badge = new TacticalBadge(theme);
            badge.setSpec(spec);
            badges.add(badge);
            addChild(badge);
        }
        requestLayout();
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (!isEnabledAndVisible() || !isPointInside(mouseX, mouseY)) {
            return false;
        }
        pressed = true;
        return true;
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        if (!pressed) {
            return false;
        }
        final boolean inside = isPointInside(mouseX, mouseY);
        pressed = false;
        if (inside && onClick != null && isEnabledAndVisible()) {
            onClick.run();
            return true;
        }
        return inside;
    }

    @Override
    public void onLayoutUpdated() {
        background.setSurface(active ? TacticalSurface.RAISED : surface);
        background.setTone(tone);
        background.setHighlighted(active || pressed);
        place(background, 0, 0, getWidth(), getHeight());

        final int padding = theme.panelPadding();
        final int contentWidth = Math.max(0, getWidth() - padding * 2);
        final int top = padding + theme.accentBandHeight();
        final int titleY = top;
        final int summaryY = titleY + theme.titleLineHeight() + theme.regularGap();
        final int badgeY =
            getHeight() - padding - theme.captionLineHeight() - theme.regularGap() - theme.badgeHeight();
        final int actionY = getHeight() - padding - theme.captionLineHeight();
        final int summaryHeight = Math.max(
            theme.bodyLineHeight() * SUMMARY_LINES,
            badgeY - summaryY - theme.regularGap()
        );

        place(titleLabel, padding, titleY, contentWidth, theme.titleLineHeight());
        place(summaryLabel, padding, summaryY, contentWidth, summaryHeight);
        place(actionLabel, padding, actionY, contentWidth, theme.captionLineHeight());
        layoutBadges(padding, badgeY, contentWidth);
        super.onLayoutUpdated();
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        background.setHighlighted(active || pressed);
    }

    private void layoutBadges(final int startX, final int y, final int availableWidth) {
        int cursor = startX;
        for (final TacticalBadge badge : badges) {
            final int remainingWidth = startX + availableWidth - cursor;
            if (remainingWidth <= 0) {
                badge.setVisible(false);
                continue;
            }
            final int badgeWidth = Math.min(badge.suggestWidth(), remainingWidth);
            badge.setVisible(true);
            place(badge, cursor, y, badgeWidth, theme.badgeHeight());
            cursor += badgeWidth + theme.tightGap();
        }
    }

    private static void place(
        final UIElement element,
        final int x,
        final int y,
        final int width,
        final int height
    ) {
        element.setFrame(x, y, width, height);
        element.onLayoutUpdated();
    }
}
