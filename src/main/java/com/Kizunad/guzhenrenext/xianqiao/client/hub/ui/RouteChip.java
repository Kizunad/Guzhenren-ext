package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

public final class RouteChip extends HubPanel {

    private static final int TEXT_Y_OFFSET = 4;

    private String text = "";
    private Runnable onClick;
    private boolean pressed;
    private boolean actionable;
    private String noticeText = "";

    public RouteChip() {
        super(HubUiTokens.HubTone.STONE);
    }

    public RouteChip(final String text, final HubUiTokens.HubTone tone) {
        this();
        setText(text);
        setTone(tone);
    }

    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    public String getText() {
        return text;
    }

    public void setOnClick(final Runnable onClick) {
        this.onClick = onClick;
    }

    public void setActionable(final boolean actionable) {
        this.actionable = actionable;
    }

    public boolean isActionable() {
        return actionable;
    }

    public String getNoticeText() {
        return noticeText;
    }

    public void applyPolicy(final HubRoutePolicy.CardRoutePolicy policy) {
        Objects.requireNonNull(policy, "policy");
        setTone(HubUiTokens.toneForRouteKind(policy.routeKind()));
        noticeText = policy.noticeText();
        if (policy.launchesScreen()) {
            setText("前往" + policy.target().displayName());
            actionable = true;
            return;
        }
        if (policy.staysOnHub()) {
            setText("主殿总览");
            actionable = false;
            return;
        }
        setText("待前往" + policy.target().displayName());
        actionable = false;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (!actionable || !isEnabledAndVisible() || !isPointInside(mouseX, mouseY)) {
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
        if (inside && actionable && onClick != null && isEnabledAndVisible()) {
            onClick.run();
            return true;
        }
        return inside;
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        final HubUiTokens.HubTonePalette palette = actionable
            ? getResolvedPalette()
            : HubUiTokens.palette(HubUiTokens.HubTone.STONE);
        HubUiTokens.drawChip(
            context,
            getAbsoluteX(),
            getAbsoluteY(),
            getWidth(),
            getHeight(),
            palette,
            pressed
        );
        final int textWidth = context.measureTextWidth(text);
        final int drawX = getAbsoluteX() + Math.max(0, (getWidth() - textWidth) / 2);
        context.drawText(text, drawX, getAbsoluteY() + TEXT_Y_OFFSET, palette.textColor());
    }
}
