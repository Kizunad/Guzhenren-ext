package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.BenmingSummary;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.FlyingSwordViewModel;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.HelpSignals;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.HubOverview;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.TacticalStateSnapshot;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalSurface;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalTheme;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalTone;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 紧凑战斗 HUD Overlay。
 * <p>
 * 这个 Overlay 只保留战斗即时决策所需的关键簇：焦点飞剑、本命过载仪表、优先级告警、
 * 快捷提示和迷你 roster 痕迹；明确不再复刻旧 HUD 的右侧纵向“每剑一条”列表信息架构。
 * </p>
 * <p>
 * 状态来源严格复用 {@link FlyingSwordTacticalStateService} 的共享快照，确保 benming-first
 * 窗口策略、focus 优先级和本命风险语义都与任务 1/2 已冻结的契约保持一致。
 * </p>
 */
@EventBusSubscriber(
    value = Dist.CLIENT,
    bus = EventBusSubscriber.Bus.GAME,
    modid = GuzhenrenExt.MODID
)
public final class FlyingSwordTacticalHudOverlay {

    private static final TacticalTheme THEME = TacticalTheme.coldConsole();
    private static final int HUD_COLUMNS = 29;
    private static final int MARGIN_RIGHT = THEME.sectionGap();
    private static final int MARGIN_TOP = THEME.sectionGap() * 5;
    private static final int HUD_WIDTH =
        THEME.panelPadding() * 2 + THEME.estimatedCharacterWidth() * HUD_COLUMNS;
    private static final int HEADER_ROW_HEIGHT = THEME.titleLineHeight();
    private static final int META_ROW_HEIGHT = THEME.bodyLineHeight();
    private static final int OVERLOAD_CLUSTER_HEIGHT =
        THEME.panelPadding() * 2
            + THEME.bodyLineHeight()
            + THEME.tightGap()
            + THEME.badgeHeight()
            + THEME.tightGap()
            + THEME.barBlockHeight();
    private static final int ACTION_HINT_ROW_HEIGHT = THEME.badgeHeight();
    private static final int MINI_TRACE_WIDTH = THEME.barBlockHeight() - THEME.regularGap();
    private static final int MINI_TRACE_HEIGHT = THEME.barHeight() + THEME.hairline() * 2;
    private static final int OVERLOAD_TITLE_WIDTH = THEME.estimatedCharacterWidth() * 8;
    private static final int HUD_HEIGHT =
        THEME.panelPadding() * 2
            + HEADER_ROW_HEIGHT
            + THEME.regularGap()
            + META_ROW_HEIGHT
            + THEME.regularGap()
            + OVERLOAD_CLUSTER_HEIGHT
            + THEME.regularGap()
            + THEME.barBlockHeight()
            + THEME.tightGap()
            + THEME.barBlockHeight()
            + THEME.regularGap()
            + ACTION_HINT_ROW_HEIGHT
            + THEME.regularGap()
            + MINI_TRACE_HEIGHT;
    private static final int MAX_MINI_ROSTER_TRACES = 8;
    private static final int MAX_PRIORITY_ALERTS = 3;
    private static final float OVERLOAD_FULL_PERCENT = 100.0F;
    private static final float DURABILITY_WARNING_RATIO = 0.60F;
    private static final float DURABILITY_DANGER_RATIO = 0.30F;
    private static final String META_SEPARATOR = " · ";
    private static final String BENMING_DORMANT_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_BENMING_DORMANT);
    private static final String RESONANCE_DORMANT_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_RESONANCE_DORMANT);
    private static final String OVERLOAD_STABLE_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_OVERLOAD_STABLE);
    private static final String LABEL_DURABILITY =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_LABEL_DURABILITY);
    private static final String LABEL_EXPERIENCE =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_LABEL_EXPERIENCE);
    private static final String LABEL_OVERLOAD_TITLE =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_LABEL_OVERLOAD_TITLE);
    private static final String LABEL_OVERLOAD =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_LABEL_OVERLOAD);
    private static final String ACTION_SELECT_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_ACTION_SELECT);
    private static final String ACTION_MODE_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_ACTION_MODE);
    private static final String ACTION_RECALL_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_ACTION_RECALL);
    private static final String ACTION_RESTORE_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_ACTION_RESTORE);
    private static final String ACTION_BENMING_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_ACTION_BENMING);
    private static final String ACTION_HUB_TEXT =
        localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_ACTION_HUB);

    private FlyingSwordTacticalHudOverlay() {}

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        FlyingSwordHudState.tick();
    }

    @SubscribeEvent
    public static void onRenderGui(final RenderGuiEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();
        if (!shouldRenderTacticalHud(
            mc.player != null,
            mc.options.hideGui,
            FlyingSwordHudState.hasSwords()
        )) {
            return;
        }
        final CompactHudRenderPlan renderPlan = buildCompactHudPlan(
            FlyingSwordHudState.getTacticalStateSnapshot()
        );
        if (renderPlan == null) {
            return;
        }
        renderCompactHud(
            event.getGuiGraphics(),
            mc.font,
            mc.getWindow().getGuiScaledWidth(),
            renderPlan
        );
    }

    private static boolean shouldRenderTacticalHud(
        final boolean hasPlayer,
        final boolean hideGui,
        final boolean hasSwords
    ) {
        return hasPlayer && !hideGui && hasSwords;
    }

    @Nullable
    /**
     * 供测试与后续任务 9 的注册切换复用的 render-plan 入口。
     * <p>
     * 这里故意只接收旧 HUD state 的 distance-ordered roster 与 selected sword id，随后立刻委托给
     * {@link FlyingSwordTacticalStateService#snapshotFromRoster(List, UUID)}，避免 HUD 层再次手写
     * focus / benming / warning / visible window 聚合逻辑。
     * </p>
     */
    private static CompactHudRenderPlan buildCompactHudPlan(
        final List<FlyingSwordHudState.SwordDisplayData> distanceOrderedRoster,
        @Nullable final UUID selectedSwordId
    ) {
        return buildCompactHudPlan(
            FlyingSwordTacticalStateService.snapshotFromRoster(distanceOrderedRoster, selectedSwordId)
        );
    }

    @Nullable
    private static CompactHudRenderPlan buildCompactHudPlan(
        final TacticalStateSnapshot snapshot
    ) {
        if (snapshot == null || snapshot.focusSword() == null || snapshot.focusSword().sword() == null) {
            return null;
        }

        final FlyingSwordViewModel focusView = snapshot.focusSword().sword();
        final List<MiniRosterTrace> miniRosterTraces = buildMiniRosterTraces(
            snapshot.hubOverview(),
            focusView.uuid()
        );
        return new CompactHudRenderPlan(
            HUD_WIDTH,
            HUD_HEIGHT,
            resolvePanelTone(focusView),
            buildHeaderRow(focusView),
            buildMetaRow(focusView),
            buildOverloadCluster(snapshot.squadSummary().benmingSummary()),
            buildResourceBars(focusView),
            buildActionHintRow(snapshot.helpSignals()),
            miniRosterTraces,
            Math.max(0, snapshot.squadSummary().totalCount() - miniRosterTraces.size())
        );
    }

    private static HeaderRow buildHeaderRow(final FlyingSwordViewModel focusView) {
        final List<BadgePlan> badges = new ArrayList<>();
        if (focusView.benmingSword()) {
            badges.add(
                new BadgePlan(
                    localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_MARK),
                    TacticalTone.BENMING
                )
            );
        }
        badges.add(new BadgePlan(focusView.quality().getDisplayName(), TacticalTone.INFO));
        badges.add(
            new BadgePlan(
                localizedHudText(KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_LEVEL_BADGE, focusView.level()),
                TacticalTone.NEUTRAL
            )
        );
        return new HeaderRow(
            resolveSwordName(focusView),
            focusView.benmingSword() ? TacticalTone.BENMING : TacticalTone.INFO,
            List.copyOf(badges)
        );
    }

    private static MetaRow buildMetaRow(final FlyingSwordViewModel focusView) {
        return new MetaRow(
            resolveResonanceText(focusView.resonanceType()),
            focusView.aiMode().getDisplayName(),
            localizedHudText(
                KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_DISTANCE,
                String.format(Locale.ROOT, "%.1f", Math.max(0.0F, focusView.distance()))
            )
        );
    }

    private static OverloadCluster buildOverloadCluster(
        @Nullable final BenmingSummary benmingSummary
    ) {
        if (benmingSummary == null) {
            return new OverloadCluster(
                BENMING_DORMANT_TEXT,
                TacticalTone.MUTED,
                "--",
                0.0F,
                List.of(new BadgePlan(BENMING_DORMANT_TEXT, TacticalTone.MUTED))
            );
        }
        return new OverloadCluster(
            resolveResonanceText(benmingSummary.resonanceType()),
            resolveOverloadTone(benmingSummary),
            formatOverloadText(benmingSummary.overloadPercent()),
            normalizeOverloadFillRatio(benmingSummary.overloadPercent()),
            buildPriorityAlerts(benmingSummary)
        );
    }

    private static ResourceBars buildResourceBars(final FlyingSwordViewModel focusView) {
        final String durabilityValue =
            Math.round(Math.max(0.0F, focusView.health()))
                + "/"
                + Math.round(Math.max(0.0F, focusView.maxHealth()));
        final String experienceValue =
            localizedHudText(
                KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_EXPERIENCE_VALUE,
                focusView.level(),
                focusView.quality().getMaxLevel()
            );
        return new ResourceBars(
            new BarPlan(
                LABEL_DURABILITY,
                durabilityValue,
                normalizeRatio(focusView.healthPercent()),
                resolveDurabilityTone(focusView.healthPercent())
            ),
            new BarPlan(
                LABEL_EXPERIENCE,
                experienceValue,
                normalizeRatio(focusView.expProgress()),
                TacticalTone.INFO
            )
        );
    }

    private static ActionHintRow buildActionHintRow(final HelpSignals helpSignals) {
        final TacticalTone cautionTone = resolveHintTone(helpSignals);
        return new ActionHintRow(
            List.of(
                new ActionHint("Z", ACTION_SELECT_TEXT, ACTION_SELECT_TEXT, TacticalTone.INFO),
                new ActionHint("X", ACTION_MODE_TEXT, ACTION_MODE_TEXT, TacticalTone.INFO),
                new ActionHint("C", ACTION_RECALL_TEXT, ACTION_RECALL_TEXT, TacticalTone.WARNING),
                new ActionHint("V", ACTION_RESTORE_TEXT, ACTION_RESTORE_TEXT, TacticalTone.INFO),
                new ActionHint("G", ACTION_BENMING_TEXT, ACTION_BENMING_TEXT, cautionTone)
            ),
            new ActionHint("H", ACTION_HUB_TEXT, ACTION_HUB_TEXT, cautionTone)
        );
    }

    private static List<MiniRosterTrace> buildMiniRosterTraces(
        final HubOverview hubOverview,
        final UUID focusSwordId
    ) {
        final List<MiniRosterTrace> traces = new ArrayList<>();
        final List<FlyingSwordViewModel> visibleWindow = hubOverview.visibleDisplayWindow();
        for (int index = 0; index < visibleWindow.size() && index < MAX_MINI_ROSTER_TRACES; index++) {
            final FlyingSwordViewModel view = visibleWindow.get(index);
            traces.add(
                new MiniRosterTrace(
                    view.uuid(),
                    resolveTraceTone(view),
                    view.uuid().equals(focusSwordId),
                    view.benmingSword(),
                    view.selected()
                )
            );
        }
        return List.copyOf(traces);
    }

    private static List<BadgePlan> buildPriorityAlerts(final BenmingSummary benmingSummary) {
        final List<BadgePlan> alerts = new ArrayList<>();
        if (benmingSummary.overloadDanger()) {
            alerts.add(
                new BadgePlan(
                    resolveDangerBadgeText(benmingSummary.resonanceType()),
                    TacticalTone.DANGER
                )
            );
        } else if (benmingSummary.highlightWarning()) {
            alerts.add(
                new BadgePlan(
                    localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_OVERLOAD_PRE_WARNING),
                    TacticalTone.WARNING
                )
            );
        }
        if (benmingSummary.overloadBacklashActive()) {
            alerts.add(new BadgePlan(resolveBacklashBadgeText(), TacticalTone.DANGER));
        } else if (benmingSummary.overloadRecoveryActive()) {
            alerts.add(new BadgePlan(resolveRecoveryBadgeText(), TacticalTone.WARNING));
        }
        if (benmingSummary.burstReady()) {
            alerts.add(
                new BadgePlan(
                    resolveBurstBadgeText(benmingSummary.resonanceType()),
                    TacticalTone.BENMING
                )
            );
        }
        if (benmingSummary.aftershockPeriod()) {
            alerts.add(
                new BadgePlan(
                    resolveAftershockBadgeText(benmingSummary.resonanceType()),
                    TacticalTone.INFO
                )
            );
        }
        if (alerts.isEmpty()) {
            alerts.add(new BadgePlan(OVERLOAD_STABLE_TEXT, TacticalTone.INFO));
        }
        if (alerts.size() <= MAX_PRIORITY_ALERTS) {
            return List.copyOf(alerts);
        }
        return List.copyOf(alerts.subList(0, MAX_PRIORITY_ALERTS));
    }

    private static void renderCompactHud(
        final GuiGraphics graphics,
        final Font font,
        final int screenWidth,
        final CompactHudRenderPlan renderPlan
    ) {
        final int panelX = screenWidth - MARGIN_RIGHT - renderPlan.width();
        final int panelY = MARGIN_TOP;
        drawFrame(
            graphics,
            new FrameBox(panelX, panelY, renderPlan.width(), renderPlan.height()),
            TacticalSurface.SECTION,
            renderPlan.panelTone(),
            true
        );

        final int contentLeft = panelX + THEME.panelPadding();
        final int contentRight = panelX + renderPlan.width() - THEME.panelPadding();
        int cursorY = panelY + THEME.panelPadding();

        drawHeaderRow(graphics, font, contentLeft, contentRight, cursorY, renderPlan.headerRow());
        cursorY += HEADER_ROW_HEIGHT + THEME.regularGap();

        drawMetaRow(graphics, font, contentLeft, contentRight, cursorY, renderPlan.metaRow());
        cursorY += META_ROW_HEIGHT + THEME.regularGap();

        drawOverloadCluster(
            graphics,
            font,
            contentLeft,
            contentRight,
            cursorY,
            renderPlan.overloadCluster()
        );
        cursorY += OVERLOAD_CLUSTER_HEIGHT + THEME.regularGap();

        drawBar(graphics, font, contentLeft, contentRight, cursorY, renderPlan.resourceBars().durabilityBar());
        cursorY += THEME.barBlockHeight() + THEME.tightGap();

        drawBar(graphics, font, contentLeft, contentRight, cursorY, renderPlan.resourceBars().experienceBar());
        cursorY += THEME.barBlockHeight() + THEME.regularGap();

        drawActionHintRow(
            graphics,
            font,
            contentLeft,
            contentRight,
            cursorY,
            renderPlan.actionHintRow()
        );
        cursorY += ACTION_HINT_ROW_HEIGHT + THEME.regularGap();

        drawMiniRosterTraces(
            graphics,
            font,
            contentLeft,
            contentRight,
            cursorY,
            renderPlan.miniRosterTraces(),
            renderPlan.hiddenTraceCount()
        );
    }

    private static void drawHeaderRow(
        final GuiGraphics graphics,
        final Font font,
        final int leftX,
        final int rightX,
        final int y,
        final HeaderRow headerRow
    ) {
        final int badgesWidth = drawBadgesRightAligned(graphics, font, rightX, y, headerRow.badges());
        int titleWidth = rightX - leftX - badgesWidth;
        if (badgesWidth > 0) {
            titleWidth -= THEME.tightGap();
        }
        graphics.drawString(
            font,
            clipText(font, headerRow.swordName(), titleWidth),
            leftX,
            y,
            THEME.accentColor(headerRow.titleTone()),
            false
        );
    }

    private static void drawMetaRow(
        final GuiGraphics graphics,
        final Font font,
        final int leftX,
        final int rightX,
        final int y,
        final MetaRow metaRow
    ) {
        final String metaText =
            metaRow.resonanceText()
                + META_SEPARATOR
                + metaRow.modeText()
                + META_SEPARATOR
                + metaRow.distanceText();
        graphics.drawString(
            font,
            clipText(font, metaText, rightX - leftX),
            leftX,
            y,
            THEME.textDimColor(),
            false
        );
    }

    private static void drawOverloadCluster(
        final GuiGraphics graphics,
        final Font font,
        final int leftX,
        final int rightX,
        final int y,
        final OverloadCluster overloadCluster
    ) {
        final int width = rightX - leftX;
        drawFrame(
            graphics,
            new FrameBox(leftX, y, width, OVERLOAD_CLUSTER_HEIGHT),
            TacticalSurface.RAISED,
            overloadCluster.tone(),
            overloadCluster.tone() != TacticalTone.MUTED
        );
        final int innerLeft = leftX + THEME.panelPadding();
        final int innerRight = rightX - THEME.panelPadding();
        int cursorY = y + THEME.panelPadding();

        graphics.drawString(font, LABEL_OVERLOAD_TITLE, innerLeft, cursorY, THEME.textDimColor(), false);
        graphics.drawString(
            font,
            clipText(
                font,
                overloadCluster.resonanceText(),
                innerRight - innerLeft - OVERLOAD_TITLE_WIDTH
            ),
            innerLeft + OVERLOAD_TITLE_WIDTH,
            cursorY,
            THEME.badgeTextColor(overloadCluster.tone()),
            false
        );
        cursorY += THEME.bodyLineHeight() + THEME.tightGap();

        drawBadgesLeftAligned(
            graphics,
            font,
            innerLeft,
            innerRight,
            cursorY,
            overloadCluster.priorityAlerts()
        );
        cursorY += THEME.badgeHeight() + THEME.tightGap();

        drawBar(
            graphics,
            font,
            innerLeft,
            innerRight,
            cursorY,
            new BarPlan(
                LABEL_OVERLOAD,
                overloadCluster.overloadText(),
                overloadCluster.fillRatio(),
                overloadCluster.tone()
            )
        );
    }

    private static void drawBar(
        final GuiGraphics graphics,
        final Font font,
        final int leftX,
        final int rightX,
        final int y,
        final BarPlan barPlan
    ) {
        final int width = rightX - leftX;
        final int borderWidth = THEME.hairline();
        final int trackX = leftX + THEME.panelPadding();
        final int trackWidth = Math.max(0, width - THEME.panelPadding() * 2);
        final int valueWidth = font.width(barPlan.valueText());

        graphics.fill(leftX, y, rightX, y + THEME.barBlockHeight(), THEME.surfaceColor(TacticalSurface.INSET));
        graphics.fill(leftX, y, rightX, y + borderWidth, THEME.quietLineColor());
        graphics.fill(
            leftX,
            y + THEME.barBlockHeight() - borderWidth,
            rightX,
            y + THEME.barBlockHeight(),
            THEME.quietLineColor()
        );
        graphics.fill(leftX, y, leftX + borderWidth, y + THEME.barBlockHeight(), THEME.quietLineColor());
        graphics.fill(
            rightX - borderWidth,
            y,
            rightX,
            y + THEME.barBlockHeight(),
            THEME.quietLineColor()
        );

        graphics.drawString(font, barPlan.label(), trackX, y + THEME.tightGap(), THEME.textDimColor(), false);
        graphics.drawString(
            font,
            barPlan.valueText(),
            rightX - THEME.panelPadding() - valueWidth,
            y + THEME.tightGap(),
            THEME.badgeTextColor(barPlan.tone()),
            false
        );

        final int trackY = y + THEME.tightGap() + font.lineHeight + THEME.hairline();
        final int fillWidth = Math.round(trackWidth * normalizeRatio(barPlan.fillRatio()));
        graphics.fill(trackX, trackY, trackX + trackWidth, trackY + THEME.barHeight(), THEME.barTrackColor());
        if (fillWidth > 0) {
            graphics.fill(
                trackX,
                trackY,
                trackX + fillWidth,
                trackY + THEME.barHeight(),
                THEME.barFillColor(barPlan.tone())
            );
            graphics.fill(
                trackX,
                trackY,
                trackX + fillWidth,
                trackY + borderWidth,
                THEME.barGlowColor(barPlan.tone())
            );
        }
        graphics.fill(trackX, trackY, trackX + trackWidth, trackY + borderWidth, THEME.quietLineColor());
        graphics.fill(
            trackX,
            trackY + THEME.barHeight() - borderWidth,
            trackX + trackWidth,
            trackY + THEME.barHeight(),
            THEME.barShadowColor()
        );
    }

    private static void drawActionHintRow(
        final GuiGraphics graphics,
        final Font font,
        final int leftX,
        final int rightX,
        final int y,
        final ActionHintRow actionHintRow
    ) {
        int cursorX = leftX;
        for (ActionHint actionHint : actionHintRow.actionHints()) {
            final int chipWidth = drawBadge(
                graphics,
                font,
                cursorX,
                y,
                new BadgePlan(actionHint.displayText(), actionHint.tone())
            );
            cursorX += chipWidth + THEME.tightGap();
        }
        final int hubWidth = measureBadgeWidth(font, actionHintRow.hubHint().displayText());
        drawBadge(
            graphics,
            font,
            Math.max(cursorX, rightX - hubWidth),
            y,
            new BadgePlan(actionHintRow.hubHint().displayText(), actionHintRow.hubHint().tone())
        );
    }

    private static void drawMiniRosterTraces(
        final GuiGraphics graphics,
        final Font font,
        final int leftX,
        final int rightX,
        final int y,
        final List<MiniRosterTrace> miniRosterTraces,
        final int hiddenTraceCount
    ) {
        int cursorX = leftX;
        for (MiniRosterTrace miniRosterTrace : miniRosterTraces) {
            drawMiniRosterTrace(graphics, cursorX, y, miniRosterTrace);
            cursorX += MINI_TRACE_WIDTH + THEME.tightGap();
        }
        if (hiddenTraceCount > 0) {
            final String hiddenText = "+" + hiddenTraceCount;
            final int hiddenWidth = font.width(hiddenText);
            graphics.drawString(
                font,
                hiddenText,
                Math.min(cursorX, rightX - hiddenWidth),
                y,
                THEME.textMutedColor(),
                false
            );
        }
    }

    private static void drawMiniRosterTrace(
        final GuiGraphics graphics,
        final int x,
        final int y,
        final MiniRosterTrace miniRosterTrace
    ) {
        final int borderColor = miniRosterTrace.focused()
            ? THEME.accentColor(miniRosterTrace.tone())
            : THEME.quietLineColor();
        final int fillColor = miniRosterTrace.selected()
            ? THEME.barGlowColor(miniRosterTrace.tone())
            : THEME.barFillColor(miniRosterTrace.tone());

        graphics.fill(x, y, x + MINI_TRACE_WIDTH, y + MINI_TRACE_HEIGHT, THEME.surfaceColor(TacticalSurface.INSET));
        graphics.fill(x, y, x + MINI_TRACE_WIDTH, y + THEME.hairline(), borderColor);
        graphics.fill(
            x,
            y + MINI_TRACE_HEIGHT - THEME.hairline(),
            x + MINI_TRACE_WIDTH,
            y + MINI_TRACE_HEIGHT,
            borderColor
        );
        graphics.fill(x, y, x + THEME.hairline(), y + MINI_TRACE_HEIGHT, borderColor);
        graphics.fill(
            x + MINI_TRACE_WIDTH - THEME.hairline(),
            y,
            x + MINI_TRACE_WIDTH,
            y + MINI_TRACE_HEIGHT,
            borderColor
        );
        graphics.fill(
            x + THEME.hairline(),
            y + THEME.hairline(),
            x + MINI_TRACE_WIDTH - THEME.hairline(),
            y + MINI_TRACE_HEIGHT - THEME.hairline(),
            fillColor
        );
        if (miniRosterTrace.benming()) {
            graphics.fill(
                x,
                y,
                x + THEME.panelMarkerWidth(),
                y + MINI_TRACE_HEIGHT,
                THEME.accentColor(TacticalTone.BENMING)
            );
        }
    }

    private static void drawFrame(
        final GuiGraphics graphics,
        final FrameBox frameBox,
        final TacticalSurface surface,
        final TacticalTone tone,
        final boolean highlighted
    ) {
        final int x = frameBox.x();
        final int y = frameBox.y();
        final int width = frameBox.width();
        final int height = frameBox.height();
        final int borderWidth = THEME.hairline();
        final int accentBandHeight = Math.min(height, THEME.accentBandHeight());
        graphics.fill(x, y, x + width, y + height, THEME.surfaceColor(surface));
        graphics.fill(
            x + borderWidth,
            y + borderWidth,
            x + width - borderWidth,
            y + height - borderWidth,
            THEME.surfaceOverlayColor(surface)
        );
        graphics.fill(x, y, x + width, y + borderWidth, THEME.borderColor(surface, tone));
        graphics.fill(
            x,
            y + height - borderWidth,
            x + width,
            y + height,
            THEME.borderColor(surface, tone)
        );
        graphics.fill(x, y, x + borderWidth, y + height, THEME.borderColor(surface, tone));
        graphics.fill(
            x + width - borderWidth,
            y,
            x + width,
            y + height,
            THEME.borderColor(surface, tone)
        );
        graphics.fill(x, y, x + width, y + accentBandHeight, THEME.accentColor(tone));
        if (highlighted) {
            graphics.fill(
                x,
                y,
                x + Math.min(width, THEME.panelMarkerWidth()),
                y + height,
                THEME.accentColor(tone)
            );
        }
    }

    private static int drawBadgesRightAligned(
        final GuiGraphics graphics,
        final Font font,
        final int rightX,
        final int y,
        final List<BadgePlan> badges
    ) {
        int cursorX = rightX;
        for (int index = badges.size() - 1; index >= 0; index--) {
            final BadgePlan badge = badges.get(index);
            final int badgeWidth = measureBadgeWidth(font, badge.text());
            cursorX -= badgeWidth;
            drawBadge(graphics, font, cursorX, y, badge);
            if (index > 0) {
                cursorX -= THEME.tightGap();
            }
        }
        return rightX - cursorX;
    }

    private static void drawBadgesLeftAligned(
        final GuiGraphics graphics,
        final Font font,
        final int leftX,
        final int rightX,
        final int y,
        final List<BadgePlan> badges
    ) {
        int cursorX = leftX;
        for (BadgePlan badge : badges) {
            final int badgeWidth = measureBadgeWidth(font, badge.text());
            if (cursorX + badgeWidth > rightX) {
                return;
            }
            drawBadge(graphics, font, cursorX, y, badge);
            cursorX += badgeWidth + THEME.tightGap();
        }
    }

    private static int drawBadge(
        final GuiGraphics graphics,
        final Font font,
        final int x,
        final int y,
        final BadgePlan badge
    ) {
        final int width = measureBadgeWidth(font, badge.text());
        final int borderWidth = THEME.hairline();
        final int markerWidth = Math.min(width, THEME.badgeMarkerWidth());
        final int textX = x + markerWidth + THEME.badgeSidePadding();
        final int textY = y + Math.max(0, (THEME.badgeHeight() - font.lineHeight) / 2);
        graphics.fill(x, y, x + width, y + THEME.badgeHeight(), THEME.badgeBackgroundColor(badge.tone()));
        graphics.fill(
            x,
            y,
            x + width,
            y + borderWidth,
            THEME.borderColor(TacticalSurface.INSET, badge.tone())
        );
        graphics.fill(
            x,
            y + THEME.badgeHeight() - borderWidth,
            x + width,
            y + THEME.badgeHeight(),
            THEME.borderColor(TacticalSurface.INSET, badge.tone())
        );
        graphics.fill(x, y, x + markerWidth, y + THEME.badgeHeight(), THEME.accentColor(badge.tone()));
        graphics.drawString(font, badge.text(), textX, textY, THEME.badgeTextColor(badge.tone()), false);
        return width;
    }

    private static int measureBadgeWidth(final Font font, final String text) {
        return Math.max(
            THEME.badgeMinWidth(),
            font.width(text) + THEME.badgeSidePadding() * 2 + THEME.badgeMarkerWidth()
        );
    }

    private static String resolveSwordName(final FlyingSwordViewModel focusView) {
        return localizedHudText(
            KongqiaoI18n.FLYING_SWORD_TACTICAL_HUD_SWORD_NAME,
            focusView.quality().getDisplayName()
        );
    }

    private static TacticalTone resolvePanelTone(final FlyingSwordViewModel focusView) {
        if (focusView.benmingSword()) {
            return TacticalTone.BENMING;
        }
        if (focusView.selected()) {
            return TacticalTone.INFO;
        }
        return TacticalTone.NEUTRAL;
    }

    private static TacticalTone resolveOverloadTone(final BenmingSummary benmingSummary) {
        if (benmingSummary.overloadDanger() || benmingSummary.overloadBacklashActive()) {
            return TacticalTone.DANGER;
        }
        if (benmingSummary.highlightWarning() || benmingSummary.overloadRecoveryActive()) {
            return TacticalTone.WARNING;
        }
        if (benmingSummary.burstReady()) {
            return TacticalTone.BENMING;
        }
        return TacticalTone.INFO;
    }

    private static TacticalTone resolveHintTone(final HelpSignals helpSignals) {
        if (helpSignals.hasOverloadDanger() || helpSignals.hasOverloadBacklash()) {
            return TacticalTone.DANGER;
        }
        if (helpSignals.hasOverloadWarning() || helpSignals.hasOverloadRecovery()) {
            return TacticalTone.WARNING;
        }
        if (helpSignals.hasBenmingSword() || helpSignals.hasBurstReady()) {
            return TacticalTone.BENMING;
        }
        return TacticalTone.INFO;
    }

    private static TacticalTone resolveTraceTone(final FlyingSwordViewModel viewModel) {
        if (viewModel.overloadDanger() || viewModel.overloadBacklashActive()) {
            return TacticalTone.DANGER;
        }
        if (viewModel.highlightWarning() || viewModel.overloadRecoveryActive()) {
            return TacticalTone.WARNING;
        }
        if (viewModel.benmingSword()) {
            return TacticalTone.BENMING;
        }
        if (viewModel.selected()) {
            return TacticalTone.INFO;
        }
        return TacticalTone.MUTED;
    }

    private static TacticalTone resolveDurabilityTone(final float ratio) {
        final float safeRatio = normalizeRatio(ratio);
        if (safeRatio <= DURABILITY_DANGER_RATIO) {
            return TacticalTone.DANGER;
        }
        if (safeRatio <= DURABILITY_WARNING_RATIO) {
            return TacticalTone.WARNING;
        }
        return TacticalTone.INFO;
    }

    private static String resolveResonanceText(
        @Nullable final FlyingSwordResonanceType resonanceType
    ) {
        final String resonanceLabel = resolveResonanceLabel(resonanceType);
        return resonanceLabel.isBlank() ? RESONANCE_DORMANT_TEXT : resonanceLabel;
    }

    private static String resolveResonanceLabel(
        @Nullable final FlyingSwordResonanceType resonanceType
    ) {
        if (resonanceType == null) {
            return "";
        }
        return switch (resonanceType) {
            case OFFENSE -> localizedHudText(KongqiaoI18n.BENMING_HUD_RESONANCE_OFFENSE_SHORT);
            case DEFENSE -> localizedHudText(KongqiaoI18n.BENMING_HUD_RESONANCE_DEFENSE_SHORT);
            case SPIRIT -> localizedHudText(KongqiaoI18n.BENMING_HUD_RESONANCE_SPIRIT_SHORT);
            case DEVOUR -> localizedHudText(KongqiaoI18n.BENMING_HUD_RESONANCE_DEVOUR_SHORT);
        };
    }

    private static String resolveBurstBadgeText(
        @Nullable final FlyingSwordResonanceType resonanceType
    ) {
        if (resonanceType == null) {
            return localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_BURST_READY);
        }
        final String badgeKey = switch (resonanceType) {
            case OFFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_OFFENSE_BURST_READY;
            case DEFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_DEFENSE_BURST_READY;
            case SPIRIT -> KongqiaoI18n.BENMING_HUD_BADGE_SPIRIT_BURST_READY;
            case DEVOUR -> KongqiaoI18n.BENMING_HUD_BADGE_DEVOUR_BURST_READY;
        };
        return localizedHudText(badgeKey);
    }

    private static String resolveDangerBadgeText(
        @Nullable final FlyingSwordResonanceType resonanceType
    ) {
        if (resonanceType == null) {
            return localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_OVERLOAD_WARNING);
        }
        final String badgeKey = switch (resonanceType) {
            case OFFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_OFFENSE_DANGER;
            case DEFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_DEFENSE_DANGER;
            case SPIRIT -> KongqiaoI18n.BENMING_HUD_BADGE_SPIRIT_DANGER;
            case DEVOUR -> KongqiaoI18n.BENMING_HUD_BADGE_DEVOUR_DANGER;
        };
        return localizedHudText(badgeKey);
    }

    private static String resolveBacklashBadgeText() {
        return localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_BACKLASH);
    }

    private static String resolveRecoveryBadgeText() {
        return localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_RECOVERY);
    }

    private static String resolveAftershockBadgeText(
        @Nullable final FlyingSwordResonanceType resonanceType
    ) {
        if (resonanceType == null) {
            return localizedHudText(KongqiaoI18n.BENMING_HUD_BADGE_AFTERSHOCK);
        }
        final String badgeKey = switch (resonanceType) {
            case OFFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_OFFENSE_AFTERSHOCK;
            case DEFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_DEFENSE_AFTERSHOCK;
            case SPIRIT -> KongqiaoI18n.BENMING_HUD_BADGE_SPIRIT_AFTERSHOCK;
            case DEVOUR -> KongqiaoI18n.BENMING_HUD_BADGE_DEVOUR_AFTERSHOCK;
        };
        return localizedHudText(badgeKey);
    }

    private static String formatOverloadText(final float overloadPercent) {
        return localizedHudText(
            KongqiaoI18n.BENMING_HUD_OVERLOAD_TEXT,
            Math.round(Math.max(0.0F, overloadPercent))
        );
    }

    private static float normalizeOverloadFillRatio(final float overloadPercent) {
        return normalizeRatio(overloadPercent / OVERLOAD_FULL_PERCENT);
    }

    private static float normalizeRatio(final float ratio) {
        return Math.min(1.0F, Math.max(0.0F, ratio));
    }

    private static String clipText(final Font font, final String text, final int maxWidth) {
        if (text == null || text.isBlank() || maxWidth <= 0) {
            return "";
        }
        return font.plainSubstrByWidth(text, maxWidth);
    }

    private static String localizedHudText(final String key, final Object... args) {
        return KongqiaoI18n.localizedText(key, args);
    }

    private record CompactHudRenderPlan(
        int width,
        int height,
        TacticalTone panelTone,
        HeaderRow headerRow,
        MetaRow metaRow,
        OverloadCluster overloadCluster,
        ResourceBars resourceBars,
        ActionHintRow actionHintRow,
        List<MiniRosterTrace> miniRosterTraces,
        int hiddenTraceCount
    ) {
    }

    private record HeaderRow(String swordName, TacticalTone titleTone, List<BadgePlan> badges) {
    }

    private record MetaRow(String resonanceText, String modeText, String distanceText) {
    }

    private record OverloadCluster(
        String resonanceText,
        TacticalTone tone,
        String overloadText,
        float fillRatio,
        List<BadgePlan> priorityAlerts
    ) {
    }

    private record ResourceBars(BarPlan durabilityBar, BarPlan experienceBar) {
    }

    private record BarPlan(String label, String valueText, float fillRatio, TacticalTone tone) {
    }

    private record ActionHintRow(List<ActionHint> actionHints, ActionHint hubHint) {
    }

    private record ActionHint(String key, String label, String displayText, TacticalTone tone) {
    }

    private record BadgePlan(String text, TacticalTone tone) {
    }

    private record MiniRosterTrace(
        UUID swordUuid,
        TacticalTone tone,
        boolean focused,
        boolean benming,
        boolean selected
    ) {
    }

    private record FrameBox(int x, int y, int width, int height) {
    }
}
