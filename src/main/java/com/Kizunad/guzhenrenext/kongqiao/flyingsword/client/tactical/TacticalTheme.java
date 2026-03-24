package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public final class TacticalTheme {

    private static final int ROOT_SURFACE = 0xFF07141B;
    private static final int SECTION_SURFACE = 0xFF0D1E27;
    private static final int RAISED_SURFACE = 0xFF122733;
    private static final int INSET_SURFACE = 0xFF081018;

    private static final int ROOT_OVERLAY = 0xFF0B1A23;
    private static final int SECTION_OVERLAY = 0xFF11242E;
    private static final int RAISED_OVERLAY = 0xFF16303D;
    private static final int INSET_OVERLAY = 0xFF0C1520;

    private static final int TEXT_PRIMARY = 0xFFE7F7FF;
    private static final int TEXT_DIM = 0xFF9EBECB;
    private static final int TEXT_MUTED = 0xFF688591;

    private static final int INFO_ACCENT = 0xFF4CC7E8;
    private static final int INFO_FILL = 0xFF153946;
    private static final int BENMING_ACCENT = 0xFFE3B757;
    private static final int BENMING_FILL = 0xFF3F2E10;
    private static final int WARNING_ACCENT = 0xFFF0B44A;
    private static final int WARNING_FILL = 0xFF4B3511;
    private static final int DANGER_ACCENT = 0xFFE56763;
    private static final int DANGER_FILL = 0xFF4E1F1D;
    private static final int NEUTRAL_ACCENT = 0xFF355C69;
    private static final int NEUTRAL_FILL = 0xFF162630;
    private static final int MUTED_ACCENT = 0xFF284550;
    private static final int MUTED_FILL = 0xFF0E1A21;

    private static final int QUIET_LINE = 0xFF17303B;
    private static final int QUIET_INFO_ACCENT = 0xFF315A67;
    private static final int QUIET_BENMING_ACCENT = 0xFF7A6642;
    private static final int QUIET_WARNING_ACCENT = 0xFF72603D;
    private static final int QUIET_DANGER_ACCENT = 0xFF734442;
    private static final int QUIET_MUTED_ACCENT = 0xFF182B33;
    private static final int BAR_TRACK = 0xFF061018;
    private static final int BAR_SHADOW = 0xFF02070B;

    private static final int NO_RADIUS = 0;
    private static final int FONT_SIZE = 18;
    private static final int HAIRLINE = 1;
    private static final int ACCENT_BAND_HEIGHT = 5;
    private static final int PANEL_MARKER_WIDTH = 5;
    private static final int PANEL_PADDING = 12;
    private static final int TIGHT_GAP = 6;
    private static final int REGULAR_GAP = 9;
    private static final int SECTION_GAP = 12;
    private static final int BADGE_HEIGHT = 21;
    private static final int BADGE_SIDE_PADDING = 9;
    private static final int BADGE_MARKER_WIDTH = 5;
    private static final int BADGE_MIN_WIDTH = 48;
    private static final int BAR_HEIGHT = 9;
    private static final int BAR_BLOCK_HEIGHT = 27;
    private static final int TITLE_LINE_HEIGHT = 18;
    private static final int BODY_LINE_HEIGHT = 15;
    private static final int CAPTION_LINE_HEIGHT = 15;
    private static final int ESTIMATED_CHARACTER_WIDTH = 9;

    private static final TacticalTheme COLD_CONSOLE = new TacticalTheme();

    private final Theme baseTheme;

    private TacticalTheme() {
        this.baseTheme = new Theme(
            INFO_FILL,
            INFO_ACCENT,
            SECTION_SURFACE,
            TEXT_PRIMARY,
            NO_RADIUS,
            PANEL_PADDING,
            FONT_SIZE
        );
    }

    public static TacticalTheme coldConsole() {
        return COLD_CONSOLE;
    }

    public Theme baseTheme() {
        return baseTheme;
    }

    public Theme createSurfaceTheme(
        final TacticalSurface surface,
        final TacticalTone tone
    ) {
        final TacticalSurface safeSurface = Objects.requireNonNull(surface, "surface");
        final TacticalTone safeTone = Objects.requireNonNull(tone, "tone");
        return new Theme(
            fillColor(safeTone),
            borderColor(safeSurface, safeTone),
            surfaceColor(safeSurface),
            TEXT_PRIMARY,
            NO_RADIUS,
            PANEL_PADDING,
            FONT_SIZE
        );
    }

    public int surfaceColor(final TacticalSurface surface) {
        return switch (Objects.requireNonNull(surface, "surface")) {
            case ROOT -> ROOT_SURFACE;
            case SECTION -> SECTION_SURFACE;
            case RAISED -> RAISED_SURFACE;
            case INSET -> INSET_SURFACE;
        };
    }

    public int surfaceOverlayColor(final TacticalSurface surface) {
        return switch (Objects.requireNonNull(surface, "surface")) {
            case ROOT -> ROOT_OVERLAY;
            case SECTION -> SECTION_OVERLAY;
            case RAISED -> RAISED_OVERLAY;
            case INSET -> INSET_OVERLAY;
        };
    }

    public int accentColor(final TacticalTone tone) {
        return switch (Objects.requireNonNull(tone, "tone")) {
            case NEUTRAL -> NEUTRAL_ACCENT;
            case INFO -> INFO_ACCENT;
            case BENMING -> BENMING_ACCENT;
            case WARNING -> WARNING_ACCENT;
            case DANGER -> DANGER_ACCENT;
            case MUTED -> MUTED_ACCENT;
        };
    }

    public int fillColor(final TacticalTone tone) {
        return switch (Objects.requireNonNull(tone, "tone")) {
            case NEUTRAL -> NEUTRAL_FILL;
            case INFO -> INFO_FILL;
            case BENMING -> BENMING_FILL;
            case WARNING -> WARNING_FILL;
            case DANGER -> DANGER_FILL;
            case MUTED -> MUTED_FILL;
        };
    }

    public int borderColor(final TacticalSurface surface, final TacticalTone tone) {
        if (Objects.requireNonNull(surface, "surface") == TacticalSurface.INSET) {
            return blendQuietAccent(Objects.requireNonNull(tone, "tone"));
        }
        return accentColor(Objects.requireNonNull(tone, "tone"));
    }

    public int textPrimaryColor() {
        return TEXT_PRIMARY;
    }

    public int textDimColor() {
        return TEXT_DIM;
    }

    public int textMutedColor() {
        return TEXT_MUTED;
    }

    public int badgeBackgroundColor(final TacticalTone tone) {
        return fillColor(tone);
    }

    public int badgeTextColor(final TacticalTone tone) {
        return switch (Objects.requireNonNull(tone, "tone")) {
            case NEUTRAL -> TEXT_DIM;
            case MUTED -> TEXT_MUTED;
            default -> accentColor(tone);
        };
    }

    public int barTrackColor() {
        return BAR_TRACK;
    }

    public int barFillColor(final TacticalTone tone) {
        return fillColor(tone);
    }

    public int barGlowColor(final TacticalTone tone) {
        return accentColor(tone);
    }

    public int barShadowColor() {
        return BAR_SHADOW;
    }

    public int quietLineColor() {
        return QUIET_LINE;
    }

    public int hairline() {
        return HAIRLINE;
    }

    public int accentBandHeight() {
        return ACCENT_BAND_HEIGHT;
    }

    public int panelMarkerWidth() {
        return PANEL_MARKER_WIDTH;
    }

    public int panelPadding() {
        return PANEL_PADDING;
    }

    public int tightGap() {
        return TIGHT_GAP;
    }

    public int regularGap() {
        return REGULAR_GAP;
    }

    public int sectionGap() {
        return SECTION_GAP;
    }

    public int badgeHeight() {
        return BADGE_HEIGHT;
    }

    public int badgeSidePadding() {
        return BADGE_SIDE_PADDING;
    }

    public int badgeMarkerWidth() {
        return BADGE_MARKER_WIDTH;
    }

    public int badgeMinWidth() {
        return BADGE_MIN_WIDTH;
    }

    public int barHeight() {
        return BAR_HEIGHT;
    }

    public int barBlockHeight() {
        return BAR_BLOCK_HEIGHT;
    }

    public int titleLineHeight() {
        return TITLE_LINE_HEIGHT;
    }

    public int bodyLineHeight() {
        return BODY_LINE_HEIGHT;
    }

    public int captionLineHeight() {
        return CAPTION_LINE_HEIGHT;
    }

    public int estimatedCharacterWidth() {
        return ESTIMATED_CHARACTER_WIDTH;
    }

    public int estimateCompactWidth(final Component text) {
        final Component safeText = Objects.requireNonNullElse(text, Component.empty());
        final String raw = safeText.getString();
        final int charCount = raw.codePointCount(0, raw.length());
        final int chromeWidth = BADGE_MARKER_WIDTH + BADGE_SIDE_PADDING * 2;
        final int estimated = charCount * ESTIMATED_CHARACTER_WIDTH + chromeWidth;
        return Math.max(BADGE_MIN_WIDTH, estimated);
    }

    private int blendQuietAccent(final TacticalTone tone) {
        return switch (tone) {
            case BENMING -> QUIET_BENMING_ACCENT;
            case WARNING -> QUIET_WARNING_ACCENT;
            case DANGER -> QUIET_DANGER_ACCENT;
            case INFO -> QUIET_INFO_ACCENT;
            case NEUTRAL -> QUIET_LINE;
            case MUTED -> QUIET_MUTED_ACCENT;
        };
    }
}
