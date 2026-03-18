package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 飞剑状态 HUD 渲染器。
 * <p>
 * 在屏幕右侧显示当前拥有的飞剑状态：
 * <ul>
 *     <li>品质与等级</li>
 *     <li>经验进度条</li>
 *     <li>AI 模式</li>
 *     <li>距离</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(
    value = Dist.CLIENT,
    bus = EventBusSubscriber.Bus.GAME,
    modid = GuzhenrenExt.MODID
)
public final class FlyingSwordHudOverlay {

    // ===== 布局常量 =====

    /** 右边距。 */
    private static final int MARGIN_RIGHT = 8;

    /** 顶部边距。 */
    private static final int MARGIN_TOP = 40;

    private static final int NORMAL_ENTRY_HEIGHT = 32;

    private static final int BENMING_ENTRY_HEIGHT = 44;

    private static final int BENMING_STATUS_ENTRY_HEIGHT = 54;

    /** 条目之间的间距。 */
    private static final int ENTRY_SPACING = 4;

    /** 条目背景宽度。 */
    private static final int ENTRY_WIDTH = 100;

    /** 经验条高度。 */
    private static final int EXP_BAR_HEIGHT = 4;

    private static final int OVERLOAD_BAR_HEIGHT = 4;

    private static final float OVERLOAD_FULL_PERCENT = 100.0F;

    /** 经验条宽度。 */
    private static final int EXP_BAR_WIDTH = 80;

    /** 内边距。 */
    private static final int PADDING = 4;

    private static final int INFO_LINE_GAP = 2;

    private static final int BADGE_HORIZONTAL_PADDING = 3;

    private static final int BADGE_GAP = 2;

    private static final int BENMING_ACCENT_WIDTH = 2;

    // ===== 颜色常量 =====

    /** 背景颜色（半透明黑）。 */
    private static final int COLOR_BG = 0x80000000;

    private static final int COLOR_BG_BENMING = 0x80321B0A;

    /** 选中背景颜色（半透明金色）。 */
    private static final int COLOR_BG_SELECTED = 0x80FFD700;

    /** 经验条背景颜色（深灰）。 */
    private static final int COLOR_EXP_BG = 0xFF333333;

    /** 经验条前景颜色（绿色）。 */
    private static final int COLOR_EXP_FG = 0xFF00FF00;

    /** 经验条满级颜色（金色）。 */
    private static final int COLOR_EXP_MAX = 0xFFFFD700;

    /** 白色文字。 */
    private static final int COLOR_WHITE = 0xFFFFFFFF;

    /** 灰色文字。 */
    private static final int COLOR_GRAY = 0xFFAAAAAA;

    /** 模式文字颜色。 */
    private static final int COLOR_MODE = 0xFF88CCFF;

    private static final int COLOR_BENMING_ACCENT = 0xFFE5B85B;

    private static final int COLOR_BENMING_BADGE_BG = 0xCC8E5C17;

    private static final int COLOR_BENMING_BADGE_TEXT = 0xFFFFF2CD;

    private static final int COLOR_WARNING_BADGE_BG = 0xCC8C2D22;

    private static final int COLOR_WARNING_BADGE_TEXT = 0xFFFFDCD5;

    private static final int COLOR_BURST_BADGE_BG = 0xCC87640F;

    private static final int COLOR_BURST_BADGE_TEXT = 0xFFFFF0B5;

    private static final int COLOR_AFTERSHOCK_BADGE_BG = 0xCC204E79;

    private static final int COLOR_AFTERSHOCK_BADGE_TEXT = 0xFFD9F1FF;

    private static final int COLOR_OVERLOAD_BAR_BG = 0xFF3A2A20;

    private static final int COLOR_OVERLOAD_BAR_FILL = 0xFFE7A03B;

    private static final int COLOR_OVERLOAD_BAR_DANGER = 0xFFE05555;

    private FlyingSwordHudOverlay() {}

    /**
     * 客户端 tick 事件处理 - 刷新飞剑数据缓存。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        FlyingSwordHudState.tick();
    }

    /**
     * GUI 渲染事件处理 - 绘制飞剑状态 HUD。
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // 检查是否应该渲染
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!FlyingSwordHudState.isHudEnabled()) {
            return;
        }
        if (!FlyingSwordHudState.hasSwords()) {
            return;
        }

        // 获取渲染参数
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        Font font = mc.font;

        // 获取飞剑数据
        List<FlyingSwordHudState.SwordDisplayData> swords =
            FlyingSwordHudState.getCachedSwords();
        List<EntryPlacement> placements = buildEntryPlacements(swords);

        // 渲染每把飞剑的状态
        for (int index = 0; index < swords.size(); index++) {
            EntryPlacement placement = placements.get(index);
            renderSwordEntry(
                graphics,
                font,
                screenWidth,
                placement.y(),
                swords.get(index),
                placement.renderPlan()
            );
        }
    }

    /**
     * 渲染单个飞剑条目。
     */
    private static void renderSwordEntry(
        GuiGraphics graphics,
        Font font,
        int screenWidth,
        int y,
        FlyingSwordHudState.SwordDisplayData sword,
        EntryRenderPlan renderPlan
    ) {
        int x = screenWidth - MARGIN_RIGHT - ENTRY_WIDTH;
        int entryHeight = renderPlan.entryHeight();
        drawEntryFrame(graphics, x, y, entryHeight, sword.isSelected, renderPlan.benmingEnhanced());

        int textX = x + PADDING;
        if (renderPlan.benmingEnhanced()) {
            textX += BENMING_ACCENT_WIDTH + 1;
        }
        int contentRight = x + ENTRY_WIDTH - PADDING;
        int textY = y + PADDING;

        int markerWidth = 0;
        if (renderPlan.benmingEnhanced()) {
            markerWidth = drawBadge(
                graphics,
                font,
                contentRight,
                textY,
                renderPlan.markerText(),
                COLOR_BENMING_BADGE_BG,
                COLOR_BENMING_BADGE_TEXT
            );
        }

        int nameMaxWidth = contentRight - textX - markerWidth;
        if (markerWidth > 0) {
            nameMaxWidth -= BADGE_GAP;
        }
        String qualityText = clipText(font, sword.getDisplayName(), nameMaxWidth);
        graphics.drawString(
            font,
            qualityText,
            textX,
            textY,
            sword.getQualityColor(),
            true
        );

        textY += font.lineHeight + INFO_LINE_GAP;
        String distText = String.format("%.1fm", sword.distance);
        int distWidth = font.width(distText);
        int secondaryMaxWidth = contentRight - textX - distWidth - BADGE_GAP;

        if (renderPlan.benmingEnhanced()) {
            String resonanceText = clipText(
                font,
                renderPlan.resonanceText(),
                secondaryMaxWidth
            );
            if (!resonanceText.isBlank()) {
                graphics.drawString(
                    font,
                    resonanceText,
                    textX,
                    textY,
                    renderPlan.resonanceColor(),
                    true
                );
            }
        } else {
            String modeText = clipText(font, sword.getAIModeDisplayName(), secondaryMaxWidth);
            graphics.drawString(font, modeText, textX, textY, COLOR_MODE, true);
        }

        graphics.drawString(
            font,
            distText,
            contentRight - distWidth,
            textY,
            COLOR_GRAY,
            true
        );

        int expBarY = y + entryHeight - PADDING - EXP_BAR_HEIGHT;

        if (renderPlan.showOverloadRow()) {
            int overloadRowY = expBarY - INFO_LINE_GAP - font.lineHeight;
            if (renderPlan.showStatusRow()) {
                drawStatusBadgesAboveOverload(
                    graphics,
                    font,
                    textX,
                    overloadRowY,
                    contentRight,
                    renderPlan.statusBadges()
                );
            }
            drawOverloadBar(graphics, font, textX, overloadRowY, contentRight, renderPlan);
        }

        drawExperienceBar(graphics, textX, expBarY, sword.expProgress);
    }

    private static List<EntryPlacement> buildEntryPlacements(
        List<FlyingSwordHudState.SwordDisplayData> swords
    ) {
        List<EntryPlacement> placements = new ArrayList<>(swords.size());
        int currentY = MARGIN_TOP;
        for (FlyingSwordHudState.SwordDisplayData sword : swords) {
            EntryRenderPlan renderPlan = buildEntryRenderPlan(sword);
            placements.add(new EntryPlacement(currentY, renderPlan));
            currentY += renderPlan.entryHeight() + ENTRY_SPACING;
        }
        return placements;
    }

    private static EntryRenderPlan buildEntryRenderPlan(
        FlyingSwordHudState.SwordDisplayData sword
    ) {
        List<StatusBadge> statusBadges = new ArrayList<>();
        if (sword.isBenmingSword) {
            if (sword.shouldHighlightWarning) {
                statusBadges.add(
                    new StatusBadge(
                        localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_OVERLOAD_WARNING),
                        COLOR_WARNING_BADGE_TEXT,
                        COLOR_WARNING_BADGE_BG
                    )
                );
            }
            if (sword.isBurstReady) {
                statusBadges.add(
                    new StatusBadge(
                        localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_BURST_READY),
                        COLOR_BURST_BADGE_TEXT,
                        COLOR_BURST_BADGE_BG
                    )
                );
            }
            if (sword.isAftershockPeriod) {
                statusBadges.add(
                    new StatusBadge(
                        localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_AFTERSHOCK),
                        COLOR_AFTERSHOCK_BADGE_TEXT,
                        COLOR_AFTERSHOCK_BADGE_BG
                    )
                );
            }
        }

        String resonanceText = sword.isBenmingSword
            ? resolveResonanceLabel(sword.benmingResonanceType)
            : "";
        int resonanceColor = sword.isBenmingSword
            ? resolveResonanceColor(sword.benmingResonanceType)
            : COLOR_GRAY;
        boolean showStatusRow = sword.isBenmingSword && !statusBadges.isEmpty();
        boolean showOverloadRow = sword.isBenmingSword;
        int entryHeight = NORMAL_ENTRY_HEIGHT;
        if (sword.isBenmingSword) {
            entryHeight = showStatusRow ? BENMING_STATUS_ENTRY_HEIGHT : BENMING_ENTRY_HEIGHT;
        }
        String overloadText = showOverloadRow ? formatOverloadText(sword.overloadPercent) : "";
        float overloadFillRatio = showOverloadRow
            ? normalizeOverloadFillRatio(sword.overloadPercent)
            : 0.0F;

        return new EntryRenderPlan(
            entryHeight,
            sword.isBenmingSword,
            sword.isBenmingSword
                ? localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_MARK)
                : "",
            resonanceText,
            resonanceColor,
            showStatusRow,
            List.copyOf(statusBadges),
            showOverloadRow,
            overloadText,
            overloadFillRatio,
            sword.shouldHighlightWarning
        );
    }

    private static String resolveResonanceLabel(FlyingSwordResonanceType resonanceType) {
        if (resonanceType == null) {
            return "";
        }
        return switch (resonanceType) {
            case OFFENSE -> localizedHudText(KongqiaoI18n.BENMING_HUD_RESONANCE_OFFENSE_SHORT);
            case DEFENSE -> localizedHudText(KongqiaoI18n.BENMING_HUD_RESONANCE_DEFENSE_SHORT);
            case SPIRIT -> localizedHudText(KongqiaoI18n.BENMING_HUD_RESONANCE_SPIRIT_SHORT);
        };
    }

    private static String localizedHudText(final String key, final Object... args) {
        try {
            return KongqiaoI18n.text(key, args).getString();
        } catch (RuntimeException | LinkageError exception) {
            return localizedHudTextFromBundledLang(key, args);
        }
    }

    private static String localizedHudTextFromBundledLang(
        final String key,
        final Object... args
    ) {
        for (final String localeKey : bundledLocaleFallbackOrder()) {
            final String localized = readBundledLangValue(localeKey, key);
            if (localized != null && !localized.isBlank()) {
                if (args == null || args.length == 0) {
                    return localized;
                }
                try {
                    return String.format(Locale.ROOT, localized, args);
                } catch (IllegalFormatException exception) {
                    return localized;
                }
            }
        }
        return "";
    }

    private static List<String> bundledLocaleFallbackOrder() {
        final List<String> localeKeys = new ArrayList<>();
        addLocaleCandidate(localeKeys, "zh_cn");
        addLocaleCandidate(localeKeys, Locale.getDefault().toString());
        addLocaleCandidate(localeKeys, Locale.getDefault().getLanguage());
        addLocaleCandidate(localeKeys, "en_us");
        return localeKeys;
    }

    private static void addLocaleCandidate(
        final List<String> localeKeys,
        final String rawLocale
    ) {
        if (rawLocale == null || rawLocale.isBlank()) {
            return;
        }
        String normalized = rawLocale.replace('-', '_').toLowerCase(Locale.ROOT);
        if ("zh".equals(normalized)) {
            normalized = "zh_cn";
        } else if ("en".equals(normalized)) {
            normalized = "en_us";
        }
        if (!localeKeys.contains(normalized)) {
            localeKeys.add(normalized);
        }
    }

    private static String readBundledLangValue(
        final String localeKey,
        final String key
    ) {
        final String resourcePath =
            "assets/" + GuzhenrenExt.MODID + "/lang/" + localeKey + ".json";
        try (
            InputStream stream = FlyingSwordHudOverlay.class
                .getClassLoader()
                .getResourceAsStream(resourcePath)
        ) {
            if (stream == null) {
                return null;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                final JsonObject translations =
                    JsonParser.parseReader(reader).getAsJsonObject();
                final JsonElement localized = translations.get(key);
                return localized == null ? null : localized.getAsString();
            }
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    private static int resolveResonanceColor(FlyingSwordResonanceType resonanceType) {
        if (resonanceType == null) {
            return COLOR_GRAY;
        }
        return resonanceType.getPrimaryColor();
    }

    private static int drawBadge(
        GuiGraphics graphics,
        Font font,
        int rightX,
        int y,
        String text,
        int bgColor,
        int textColor
    ) {
        if (text.isBlank()) {
            return 0;
        }
        int badgeWidth = font.width(text) + BADGE_HORIZONTAL_PADDING * 2;
        int badgeX = rightX - badgeWidth;
        graphics.fill(badgeX, y, badgeX + badgeWidth, y + font.lineHeight, bgColor);
        graphics.drawString(font, text, badgeX + BADGE_HORIZONTAL_PADDING, y, textColor, false);
        return badgeWidth;
    }

    private static void drawStatusBadges(
        GuiGraphics graphics,
        Font font,
        int startX,
        int y,
        int rightLimit,
        List<StatusBadge> statusBadges
    ) {
        int badgeX = startX;
        for (StatusBadge badge : statusBadges) {
            int badgeWidth = font.width(badge.text()) + BADGE_HORIZONTAL_PADDING * 2;
            if (badgeX + badgeWidth > rightLimit) {
                break;
            }
            graphics.fill(
                badgeX,
                y,
                badgeX + badgeWidth,
                y + font.lineHeight,
                badge.backgroundColor()
            );
            graphics.drawString(
                font,
                badge.text(),
                badgeX + BADGE_HORIZONTAL_PADDING,
                y,
                badge.textColor(),
                false
            );
            badgeX += badgeWidth + BADGE_GAP;
        }
    }

    private static void drawEntryFrame(
        GuiGraphics graphics,
        int x,
        int y,
        int entryHeight,
        boolean selected,
        boolean benmingEnhanced
    ) {
        int bgColor = selected ? COLOR_BG_SELECTED : COLOR_BG;
        if (benmingEnhanced && !selected) {
            bgColor = COLOR_BG_BENMING;
        }
        graphics.fill(x, y, x + ENTRY_WIDTH, y + entryHeight, bgColor);

        if (benmingEnhanced) {
            graphics.fill(
                x + 1,
                y + 1,
                x + 1 + BENMING_ACCENT_WIDTH,
                y + entryHeight - 1,
                COLOR_BENMING_ACCENT
            );
        }

        if (!selected) {
            return;
        }

        graphics.fill(x, y, x + ENTRY_WIDTH, y + 1, COLOR_WHITE);
        graphics.fill(x, y + entryHeight - 1, x + ENTRY_WIDTH, y + entryHeight, COLOR_WHITE);
        graphics.fill(x, y, x + 1, y + entryHeight, COLOR_WHITE);
        graphics.fill(x + ENTRY_WIDTH - 1, y, x + ENTRY_WIDTH, y + entryHeight, COLOR_WHITE);
    }

    private static void drawStatusBadgesAboveOverload(
        GuiGraphics graphics,
        Font font,
        int textX,
        int overloadRowY,
        int contentRight,
        List<StatusBadge> statusBadges
    ) {
        int statusY = overloadRowY - INFO_LINE_GAP - font.lineHeight;
        drawStatusBadges(graphics, font, textX, statusY, contentRight, statusBadges);
    }

    private static void drawOverloadBar(
        GuiGraphics graphics,
        Font font,
        int startX,
        int rowY,
        int rightLimit,
        EntryRenderPlan renderPlan
    ) {
        String overloadText = renderPlan.overloadText();
        int textColor = renderPlan.overloadDanger() ? COLOR_WARNING_BADGE_TEXT : COLOR_BENMING_BADGE_TEXT;
        graphics.drawString(font, overloadText, startX, rowY, textColor, false);

        int textWidth = font.width(overloadText);
        int meterX = startX + textWidth + BADGE_GAP;
        int meterWidth = rightLimit - meterX;
        if (meterWidth <= 0) {
            return;
        }

        int meterY = rowY + Math.max(0, (font.lineHeight - OVERLOAD_BAR_HEIGHT) / 2);
        graphics.fill(meterX, meterY, meterX + meterWidth, meterY + OVERLOAD_BAR_HEIGHT, COLOR_OVERLOAD_BAR_BG);

        int filledWidth = Math.round(meterWidth * renderPlan.overloadFillRatio());
        if (filledWidth > 0) {
            int fillColor = renderPlan.overloadDanger()
                ? COLOR_OVERLOAD_BAR_DANGER
                : COLOR_OVERLOAD_BAR_FILL;
            graphics.fill(
                meterX,
                meterY,
                meterX + filledWidth,
                meterY + OVERLOAD_BAR_HEIGHT,
                fillColor
            );
        }
    }

    private static String formatOverloadText(float overloadPercent) {
        return localizedHudText(
            KongqiaoI18n.BENMING_HUD_OVERLOAD_TEXT,
            Math.round(Math.max(0.0F, overloadPercent))
        );
    }

    private static float normalizeOverloadFillRatio(float overloadPercent) {
        return Math.min(1.0F, Math.max(0.0F, overloadPercent / OVERLOAD_FULL_PERCENT));
    }

    private static String clipText(Font font, String text, int maxWidth) {
        if (maxWidth <= 0 || text.isBlank()) {
            return "";
        }
        return font.plainSubstrByWidth(text, maxWidth);
    }

    private static void drawExperienceBar(
        GuiGraphics graphics,
        int barX,
        int barY,
        float expProgress
    ) {
        graphics.fill(barX, barY, barX + EXP_BAR_WIDTH, barY + EXP_BAR_HEIGHT, COLOR_EXP_BG);

        int filledWidth = (int) (EXP_BAR_WIDTH * expProgress);
        if (filledWidth <= 0) {
            return;
        }

        int expColor = expProgress >= 1.0f ? COLOR_EXP_MAX : COLOR_EXP_FG;
        graphics.fill(barX, barY, barX + filledWidth, barY + EXP_BAR_HEIGHT, expColor);
    }

    private record EntryPlacement(int y, EntryRenderPlan renderPlan) {
    }

    private record EntryRenderPlan(
        int entryHeight,
        boolean benmingEnhanced,
        String markerText,
        String resonanceText,
        int resonanceColor,
        boolean showStatusRow,
        List<StatusBadge> statusBadges,
        boolean showOverloadRow,
        String overloadText,
        float overloadFillRatio,
        boolean overloadDanger
    ) {
    }

    private record StatusBadge(String text, int textColor, int backgroundColor) {
    }
}
