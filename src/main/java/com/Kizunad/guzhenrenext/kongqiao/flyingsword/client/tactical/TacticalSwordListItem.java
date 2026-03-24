package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public final class TacticalSwordListItem extends InteractiveElement {

    private static final int META_COLUMN_CHAR_COUNT = 10;

    public record Model(
        Component title,
        Component detail,
        Component modeText,
        Component distanceText,
        Component healthLabel,
        Component healthValue,
        float healthRatio,
        TacticalTone healthTone,
        Component overloadLabel,
        Component overloadValue,
        float overloadRatio,
        TacticalTone overloadTone,
        TacticalTone panelTone,
        boolean highlighted,
        List<TacticalBadgeSpec> badges
    ) {

        public Model {
            title = Objects.requireNonNullElse(title, Component.empty());
            detail = Objects.requireNonNullElse(detail, Component.empty());
            modeText = Objects.requireNonNullElse(modeText, Component.empty());
            distanceText = Objects.requireNonNullElse(distanceText, Component.empty());
            healthLabel = Objects.requireNonNullElse(healthLabel, Component.empty());
            healthValue = Objects.requireNonNullElse(healthValue, Component.empty());
            overloadLabel = Objects.requireNonNullElse(overloadLabel, Component.empty());
            overloadValue = Objects.requireNonNullElse(overloadValue, Component.empty());
            healthTone = Objects.requireNonNullElse(healthTone, TacticalTone.INFO);
            overloadTone = Objects.requireNonNullElse(overloadTone, TacticalTone.INFO);
            panelTone = Objects.requireNonNullElse(panelTone, TacticalTone.NEUTRAL);
            badges = List.copyOf(Objects.requireNonNullElse(badges, List.of()));
            healthRatio = clamp(healthRatio);
            overloadRatio = clamp(overloadRatio);
        }

        public static Model empty() {
            return new Model(
                Component.empty(),
                Component.empty(),
                Component.empty(),
                Component.empty(),
                Component.empty(),
                Component.empty(),
                0.0F,
                TacticalTone.INFO,
                Component.empty(),
                Component.empty(),
                0.0F,
                TacticalTone.INFO,
                TacticalTone.NEUTRAL,
                false,
                List.of()
            );
        }

        private static float clamp(final float value) {
            if (value < 0.0F) {
                return 0.0F;
            }
            if (value > 1.0F) {
                return 1.0F;
            }
            return value;
        }
    }

    private final TacticalTheme theme;
    private final TacticalPanel background;
    private final Label titleLabel;
    private final Label detailLabel;
    private final Label modeLabel;
    private final Label distanceLabel;
    private final TacticalBar healthBar;
    private final TacticalBar overloadBar;
    private final List<TacticalBadge> badges = new ArrayList<>();

    private Runnable onClick;
    private boolean pressed;
    private Model model = Model.empty();

    public TacticalSwordListItem(final TacticalTheme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
        this.background = new TacticalPanel(theme);
        this.titleLabel = new Label(Component.empty(), theme.baseTheme());
        this.detailLabel = new Label(Component.empty(), theme.baseTheme());
        this.modeLabel = new Label(Component.empty(), theme.baseTheme());
        this.distanceLabel = new Label(Component.empty(), theme.baseTheme());
        this.healthBar = new TacticalBar(theme);
        this.overloadBar = new TacticalBar(theme);

        this.titleLabel.setColor(theme.textPrimaryColor());
        this.detailLabel.setColor(theme.textDimColor());
        this.modeLabel.setColor(theme.textDimColor());
        this.distanceLabel.setColor(theme.textPrimaryColor());
        this.modeLabel.setHorizontalAlign(Label.HorizontalAlign.RIGHT);
        this.distanceLabel.setHorizontalAlign(Label.HorizontalAlign.RIGHT);

        addChild(background);
        addChild(titleLabel);
        addChild(detailLabel);
        addChild(modeLabel);
        addChild(distanceLabel);
        addChild(healthBar);
        addChild(overloadBar);
    }

    public void setModel(final Model model) {
        this.model = Objects.requireNonNull(model, "model");
        titleLabel.setText(model.title());
        detailLabel.setText(model.detail());
        modeLabel.setText(model.modeText());
        distanceLabel.setText(model.distanceText());
        healthBar.setLabel(model.healthLabel());
        healthBar.setValueText(model.healthValue());
        healthBar.setFillRatio(model.healthRatio());
        healthBar.setTone(model.healthTone());
        overloadBar.setLabel(model.overloadLabel());
        overloadBar.setValueText(model.overloadValue());
        overloadBar.setFillRatio(model.overloadRatio());
        overloadBar.setTone(model.overloadTone());
        rebuildBadges(model.badges());
        requestLayout();
    }

    public Model getModel() {
        return model;
    }

    public void setOnClick(final Runnable onClick) {
        this.onClick = onClick;
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
        background.setSurface(TacticalSurface.SECTION);
        background.setTone(model.panelTone());
        background.setHighlighted(model.highlighted() || pressed);
        place(background, 0, 0, getWidth(), getHeight());

        final int padding = theme.panelPadding();
        final int contentWidth = Math.max(0, getWidth() - padding * 2);
        final int metaWidth = theme.estimatedCharacterWidth() * META_COLUMN_CHAR_COUNT;
        final int mainWidth = Math.max(0, contentWidth - metaWidth - theme.regularGap());
        final int metaX = getWidth() - padding - metaWidth;
        final int top = padding + theme.accentBandHeight();
        final int titleY = top;
        final int detailY = titleY + theme.titleLineHeight() + theme.tightGap();
        final int badgeY = detailY + theme.bodyLineHeight() + theme.regularGap();
        final int barStartY = badgeY + resolveBadgeRowHeight() + theme.regularGap();

        place(titleLabel, padding, titleY, mainWidth, theme.titleLineHeight());
        place(detailLabel, padding, detailY, mainWidth, theme.bodyLineHeight());
        place(distanceLabel, metaX, titleY, metaWidth, theme.titleLineHeight());
        place(modeLabel, metaX, detailY, metaWidth, theme.bodyLineHeight());
        layoutBadges(padding, badgeY, contentWidth);
        place(healthBar, padding, barStartY, contentWidth, theme.barBlockHeight());
        place(
            overloadBar,
            padding,
            barStartY + theme.barBlockHeight() + theme.tightGap(),
            contentWidth,
            theme.barBlockHeight()
        );
        super.onLayoutUpdated();
    }

    private int resolveBadgeRowHeight() {
        return badges.isEmpty() ? 0 : theme.badgeHeight();
    }

    private void rebuildBadges(final List<TacticalBadgeSpec> specs) {
        for (final TacticalBadge badge : new ArrayList<>(badges)) {
            removeChild(badge);
        }
        badges.clear();
        for (final TacticalBadgeSpec spec : specs) {
            final TacticalBadge badge = new TacticalBadge(theme);
            badge.setSpec(spec);
            badges.add(badge);
            addChild(badge);
        }
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
