package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Map;
import java.util.Objects;

public final class HubUiTokens {

    public static final int PANEL_PADDING = 6;
    public static final int PANEL_GAP = 4;
    public static final int BORDER_THICKNESS = 1;
    public static final int INNER_LINE_INSET = 1;
    public static final int BAND_HEIGHT = 2;
    public static final int SMALL_CONTROL_HEIGHT = 16;
    public static final int MEDIUM_CONTROL_HEIGHT = 18;
    public static final int TITLE_REGION_HEIGHT = 14;
    public static final int SUMMARY_REGION_HEIGHT = 28;
    public static final int FOOTNOTE_REGION_HEIGHT = 24;

    private static final int HUB_CORNER_RADIUS = 4;
    private static final int HUB_FONT_SIZE = 12;

    private static final Theme HALL_THEME = new Theme(
        0xFF7A6339,
        0xFFC9A769,
        0xFF13171D,
        0xFFF2E8D1,
        HUB_CORNER_RADIUS,
        PANEL_PADDING,
        HUB_FONT_SIZE
    );

    private static final HubTonePalette GOLD_PALETTE = new HubTonePalette(
        new Theme(0xFF8E6D36, 0xFFE2C27E, 0xFF1E1911, 0xFFF5E8C6, HUB_CORNER_RADIUS, PANEL_PADDING, HUB_FONT_SIZE),
        0xFF3E3120,
        0xFF6A532E,
        0xFF9C7B42
    );

    private static final HubTonePalette JADE_PALETTE = new HubTonePalette(
        new Theme(0xFF3D7B59, 0xFF88D6A6, 0xFF122019, 0xFFE2F6E7, HUB_CORNER_RADIUS, PANEL_PADDING, HUB_FONT_SIZE),
        0xFF1F392B,
        0xFF305340,
        0xFF4A8966
    );

    private static final HubTonePalette AZURE_PALETTE = new HubTonePalette(
        new Theme(0xFF3B638D, 0xFF8CBEE9, 0xFF101B26, 0xFFE2F0FA, HUB_CORNER_RADIUS, PANEL_PADDING, HUB_FONT_SIZE),
        0xFF1A2E41,
        0xFF294862,
        0xFF447398
    );

    private static final HubTonePalette WARN_PALETTE = new HubTonePalette(
        new Theme(0xFF8A6A22, 0xFFFFD16A, 0xFF231A08, 0xFFFFF0C9, HUB_CORNER_RADIUS, PANEL_PADDING, HUB_FONT_SIZE),
        0xFF49360E,
        0xFF6A4D14,
        0xFF967022
    );

    private static final HubTonePalette DANGER_PALETTE = new HubTonePalette(
        new Theme(0xFF8D3C3C, 0xFFFF8E8E, 0xFF261111, 0xFFFFE5E5, HUB_CORNER_RADIUS, PANEL_PADDING, HUB_FONT_SIZE),
        0xFF4A2020,
        0xFF6E2F2F,
        0xFF934141
    );

    private static final HubTonePalette STONE_PALETTE = new HubTonePalette(
        new Theme(0xFF555F6D, 0xFFA0A9B6, 0xFF171C22, 0xFFE3E8EE, HUB_CORNER_RADIUS, PANEL_PADDING, HUB_FONT_SIZE),
        0xFF2B333D,
        0xFF3A4552,
        0xFF505D6C
    );

    private static final Map<HubTone, HubTonePalette> PALETTES = Map.of(
        HubTone.GOLD, GOLD_PALETTE,
        HubTone.JADE, JADE_PALETTE,
        HubTone.AZURE, AZURE_PALETTE,
        HubTone.WARN, WARN_PALETTE,
        HubTone.DANGER, DANGER_PALETTE,
        HubTone.STONE, STONE_PALETTE
    );

    private HubUiTokens() {
    }

    public static Theme hallTheme() {
        return HALL_THEME;
    }

    public static HubTonePalette palette(final HubTone tone) {
        Objects.requireNonNull(tone, "tone");
        final HubTonePalette palette = PALETTES.get(tone);
        if (palette == null) {
            throw new IllegalArgumentException("未声明 Hub 色调: " + tone.serializedName());
        }
        return palette;
    }

    public static HubTone toneForDataClass(final HubSnapshot.DataClass dataClass) {
        Objects.requireNonNull(dataClass, "dataClass");
        return switch (dataClass) {
            case REAL_CORE -> HubTone.GOLD;
            case REAL_SUMMARY -> HubTone.JADE;
            case SUMMARY_ROUTE -> HubTone.AZURE;
        };
    }

    public static String labelForDataClass(final HubSnapshot.DataClass dataClass) {
        Objects.requireNonNull(dataClass, "dataClass");
        return switch (dataClass) {
            case REAL_CORE -> "真核";
            case REAL_SUMMARY -> "实况摘要";
            case SUMMARY_ROUTE -> "摘要路由";
        };
    }

    public static HubTone toneForRisk(final HubStatusEvaluator.RiskLevel riskLevel) {
        Objects.requireNonNull(riskLevel, "riskLevel");
        return switch (riskLevel) {
            case STABLE -> HubTone.JADE;
            case CAUTION -> HubTone.WARN;
            case UNKNOWN -> HubTone.AZURE;
            case DANGER -> HubTone.DANGER;
        };
    }

    public static String labelForRisk(final HubStatusEvaluator.RiskLevel riskLevel) {
        Objects.requireNonNull(riskLevel, "riskLevel");
        return switch (riskLevel) {
            case STABLE -> "稳定";
            case CAUTION -> "预警";
            case UNKNOWN -> "待核验";
            case DANGER -> "危险";
        };
    }

    public static HubTone toneForTaxonomy(final HubRoutePolicy.HubCardTaxonomy taxonomy) {
        Objects.requireNonNull(taxonomy, "taxonomy");
        return switch (taxonomy) {
            case REAL_SUMMARY -> HubTone.JADE;
            case SUMMARY_ROUTE -> HubTone.AZURE;
            case ROUTE_ONLY -> HubTone.STONE;
        };
    }

    public static String labelForTaxonomy(final HubRoutePolicy.HubCardTaxonomy taxonomy) {
        Objects.requireNonNull(taxonomy, "taxonomy");
        return switch (taxonomy) {
            case REAL_SUMMARY -> "实况卡";
            case SUMMARY_ROUTE -> "摘要路由";
            case ROUTE_ONLY -> "仅路由";
        };
    }

    public static HubTone toneForRouteKind(final HubRoutePolicy.RouteKind routeKind) {
        Objects.requireNonNull(routeKind, "routeKind");
        return switch (routeKind) {
            case CURRENT_HUB -> HubTone.GOLD;
            case DIRECT_SCREEN -> HubTone.AZURE;
            case PLACEHOLDER -> HubTone.WARN;
        };
    }

    public static String labelForRouteKind(final HubRoutePolicy.RouteKind routeKind) {
        Objects.requireNonNull(routeKind, "routeKind");
        return switch (routeKind) {
            case CURRENT_HUB -> "主殿总览";
            case DIRECT_SCREEN -> "可直达";
            case PLACEHOLDER -> "占位入口";
        };
    }

    static void drawPanel(
        final com.Kizunad.tinyUI.core.UIRenderContext context,
        final int x,
        final int y,
        final int width,
        final int height,
        final HubTonePalette palette,
        final boolean highlighted
    ) {
        final int background = highlighted ? palette.softFillColor() : palette.backgroundColor();
        context.drawRect(x, y, width, height, background);
        drawOutline(context, x, y, width, height, palette.borderColor());
        drawTopBand(context, x, y, width, palette.accentColor());
        drawInnerLines(context, x, y, width, height, palette.lineColor());
    }

    static void drawChip(
        final com.Kizunad.tinyUI.core.UIRenderContext context,
        final int x,
        final int y,
        final int width,
        final int height,
        final HubTonePalette palette,
        final boolean pressed
    ) {
        final int background = pressed ? palette.accentColor() : palette.softFillColor();
        context.drawRect(x, y, width, height, background);
        drawOutline(context, x, y, width, height, palette.borderColor());
        drawTopBand(context, x, y, width, palette.borderColor());
    }

    static void drawStrip(
        final com.Kizunad.tinyUI.core.UIRenderContext context,
        final int x,
        final int y,
        final int width,
        final int height,
        final HubTonePalette palette
    ) {
        context.drawRect(x, y, width, height, palette.softFillColor());
        drawOutline(context, x, y, width, height, palette.borderColor());
        drawTopBand(context, x, y, width, palette.accentColor());
    }

    private static void drawOutline(
        final com.Kizunad.tinyUI.core.UIRenderContext context,
        final int x,
        final int y,
        final int width,
        final int height,
        final int color
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }
        context.drawRect(x, y, width, BORDER_THICKNESS, color);
        context.drawRect(x, y + height - BORDER_THICKNESS, width, BORDER_THICKNESS, color);
        context.drawRect(x, y, BORDER_THICKNESS, height, color);
        context.drawRect(x + width - BORDER_THICKNESS, y, BORDER_THICKNESS, height, color);
    }

    private static void drawTopBand(
        final com.Kizunad.tinyUI.core.UIRenderContext context,
        final int x,
        final int y,
        final int width,
        final int color
    ) {
        if (width <= BORDER_THICKNESS * 2) {
            return;
        }
        context.drawRect(
            x + BORDER_THICKNESS,
            y + BORDER_THICKNESS,
            width - BORDER_THICKNESS * 2,
            BAND_HEIGHT,
            color
        );
    }

    private static void drawInnerLines(
        final com.Kizunad.tinyUI.core.UIRenderContext context,
        final int x,
        final int y,
        final int width,
        final int height,
        final int color
    ) {
        if (width <= INNER_LINE_INSET * 2 || height <= INNER_LINE_INSET * 2) {
            return;
        }
        context.drawRect(
            x + INNER_LINE_INSET,
            y + height - BORDER_THICKNESS - BAND_HEIGHT,
            width - INNER_LINE_INSET * 2,
            BORDER_THICKNESS,
            color
        );
    }

    public enum HubTone {

        GOLD("gold"),
        JADE("jade"),
        AZURE("azure"),
        WARN("warn"),
        DANGER("danger"),
        STONE("stone");

        private final String serializedName;

        HubTone(final String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }
    }

    public record HubTonePalette(
        Theme theme,
        int softFillColor,
        int mutedTextColor,
        int lineColor
    ) {

        public HubTonePalette {
            Objects.requireNonNull(theme, "theme");
        }

        public int backgroundColor() {
            return theme.getBackgroundColor();
        }

        public int borderColor() {
            return theme.getAccentColor();
        }

        public int accentColor() {
            return theme.getPrimaryColor();
        }

        public int textColor() {
            return theme.getTextColor();
        }
    }
}
