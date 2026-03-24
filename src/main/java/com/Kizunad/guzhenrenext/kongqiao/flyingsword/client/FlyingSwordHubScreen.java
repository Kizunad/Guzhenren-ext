package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.config.ClientConfig;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.BenmingSummary;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.FocusSource;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.FlyingSwordViewModel;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.HelpSignals;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.HubOverview;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.SquadSummary;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordTacticalStateService.TacticalStateSnapshot;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalBadge;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalBadgeSpec;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalBar;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalPanel;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalRouteCard;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalSurface;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalSwordListItem;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalTheme;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical.TacticalTone;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.ClusterClientStateCache;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge.FlyingSwordForgeAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingAttachment;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.ScaleConfig;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import com.Kizunad.guzhenrenext.network.ServerboundKongqiaoActionPayload;
import com.Kizunad.guzhenrenext.network.ServerboundOpenClusterGuiPayload;
import com.Kizunad.guzhenrenext.network.ServerboundOpenTrainingGuiPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class FlyingSwordHubScreen extends TinyUIScreen {

    private static final Component HUB_TITLE = KongqiaoI18n.text(KongqiaoI18n.FLYING_SWORD_HUB_TITLE);
    private static final List<String> TOP_LEVEL_TABS = List.of(
        localizedText(KongqiaoI18n.FLYING_SWORD_HUB_TAB_OVERVIEW),
        localizedText(KongqiaoI18n.FLYING_SWORD_HUB_TAB_CULTIVATION),
        localizedText(KongqiaoI18n.FLYING_SWORD_HUB_TAB_HELP)
    );
    private static final List<String> CULTIVATION_SUBTABS =
        FlyingSwordHubCultivationModel.subTabTitles();
    private static final List<HelpRouteSpec> HELP_ROUTE_SPECS = List.of(
        new HelpRouteSpec(
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_QUICK_START_TITLE),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_QUICK_START_SUMMARY),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_QUICK_START_ACTION),
            TacticalTone.INFO,
            FlyingSwordHelpScreen.overviewTabIndex()
        ),
        new HelpRouteSpec(
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_OPERATIONS_TITLE),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_OPERATIONS_SUMMARY),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_OPERATIONS_ACTION),
            TacticalTone.INFO,
            FlyingSwordHelpScreen.combatTabIndex()
        ),
        new HelpRouteSpec(
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_ROUTES_TITLE),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_ROUTES_SUMMARY),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_ROUTES_ACTION),
            TacticalTone.BENMING,
            FlyingSwordHelpScreen.growthTabIndex()
        ),
        new HelpRouteSpec(
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_FAILURES_TITLE),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_FAILURES_SUMMARY),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_FAILURES_ACTION),
            TacticalTone.WARNING,
            FlyingSwordHelpScreen.benmingTabIndex()
        ),
        new HelpRouteSpec(
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_COMPLETE_TITLE),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_COMPLETE_SUMMARY),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_ROUTE_COMPLETE_ACTION),
            TacticalTone.WARNING,
            FlyingSwordHelpScreen.overviewTabIndex()
        )
    );

    private static final int WINDOW_WIDTH = 752;
    private static final int WINDOW_HEIGHT = 484;
    private static final int WINDOW_PADDING = 14;
    private static final int TITLE_Y = 12;
    private static final int TITLE_HEIGHT = 16;
    private static final int CLOSE_HINT_WIDTH = 220;
    private static final int CLOSE_HINT_HEIGHT = 12;
    private static final int CLOSE_HINT_Y = 14;
    private static final int TAB_BAR_Y = 42;
    private static final int TAB_BUTTON_HEIGHT = 24;
    private static final int TAB_GAP = 6;
    private static final int CONTENT_TOP = 80;
    private static final int CONTENT_BOTTOM_PADDING = 14;
    private static final int SECTION_PADDING = 12;
    private static final int SUMMARY_TITLE_Y = 18;
    private static final int SUMMARY_BODY_Y = 40;
    private static final int BADGE_Y = 88;
    private static final int BADGE_HEIGHT = 16;
    private static final int BADGE_GAP = 6;
    private static final int ROUTE_CARD_TOP = 124;
    private static final int ROUTE_CARD_HEIGHT = 150;
    private static final int SECOND_ROUTE_CARD_Y = 286;
    private static final int CULTIVATION_TAB_BAR_Y = 88;
    private static final int CULTIVATION_TAB_HEIGHT = 20;
    private static final int CULTIVATION_TAB_GAP = 6;
    private static final int CULTIVATION_DETAIL_TOP = 118;
    private static final int CULTIVATION_DETAIL_HEIGHT = 188;
    private static final int CULTIVATION_STATUS_Y = 56;
    private static final int CULTIVATION_PROGRESS_BAR_Y = 84;
    private static final int CULTIVATION_PROGRESS_BAR_HEIGHT = 28;
    private static final int CULTIVATION_BADGE_Y = 126;
    private static final int CULTIVATION_ACTION_BUTTON_Y = 152;
    private static final int CULTIVATION_ACTION_BUTTON_HEIGHT = 22;
    private static final int CULTIVATION_ACTION_BUTTON_WIDTH = 176;
    private static final int CULTIVATION_SCOPE_CARD_Y = 318;
    private static final int CULTIVATION_SCOPE_CARD_HEIGHT = 72;
    private static final int CULTIVATION_RESOURCE_LIMIT = 3;
    private static final int HELP_ROUTE_TOP = 90;
    private static final int HELP_ROUTE_CARD_HEIGHT = 58;
    private static final int HELP_ROUTE_GAP = 8;
    private static final int CLOSE_KEY_COUNT = 2;
    private static final int MULTILINE_SECOND_BREAK_THRESHOLD = 48;

    private static final int TAB_OVERVIEW = 0;
    private static final int TAB_CULTIVATION = 1;
    private static final int TAB_HELP = 2;
    private static final int OVERVIEW_SECTION_SUMMARY = 0;
    private static final int OVERVIEW_SECTION_ROSTER = 1;
    private static final int OVERVIEW_SECTION_FOCUS = 2;
    private static final int OVERVIEW_SECTION_ROUTE = 3;

    private final TacticalTheme tacticalTheme = TacticalTheme.coldConsole();
    private final List<UIElement> tabPanels = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private final List<UIElement> cultivationRoutePanels = new ArrayList<>();
    private final List<Button> cultivationRouteButtons = new ArrayList<>();
    private final List<Button> overviewSectionButtons = new ArrayList<>();
    private final List<UIElement> overviewSectionPanels = new ArrayList<>();

    private int activeTab = TAB_OVERVIEW;
    private int activeCultivationRoute;
    private int activeOverviewSection;
    private boolean closeRequestedForTesting;

    public FlyingSwordHubScreen() {
        this(TAB_OVERVIEW);
    }

    private FlyingSwordHubScreen(final int initialTab) {
        super(HUB_TITLE, new UIRoot());
        this.activeTab = sanitizeTabIndex(initialTab);
    }

    public static boolean isHubScreen(final Screen screen) {
        return screen instanceof FlyingSwordHubScreen;
    }

    public static Screen toggleHubScreen(final Screen currentScreen) {
        if (currentScreen instanceof FlyingSwordHubScreen) {
            return null;
        }
        return new FlyingSwordHubScreen();
    }

    static Screen openTab(final int tabIndex) {
        return new FlyingSwordHubScreen(tabIndex);
    }

    static List<String> topLevelTabTitlesForTesting() {
        return TOP_LEVEL_TABS;
    }

    static List<String> helpRouteTitlesForTesting() {
        return HELP_ROUTE_SPECS.stream()
            .map(HelpRouteSpec::title)
            .toList();
    }

    static List<Integer> helpRouteTabTargetsForTesting() {
        return HELP_ROUTE_SPECS.stream()
            .map(HelpRouteSpec::helpTabIndex)
            .toList();
    }

    static int overviewTabIndex() {
        return TAB_OVERVIEW;
    }

    static int cultivationTabIndex() {
        return TAB_CULTIVATION;
    }

    static List<String> cultivationSubTabTitlesForTesting() {
        return CULTIVATION_SUBTABS;
    }

    static FlyingSwordHubCultivationModel.CultivationPanelState buildCultivationStateForTesting(
        final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot,
        final FlyingSwordHubCultivationModel.ForgeSummary forgeSummary,
        final FlyingSwordHubCultivationModel.TrainingSummary trainingSummary,
        final int clusterLoad,
        final int clusterCapacity,
        final int clusterActiveCount
    ) {
        return FlyingSwordHubCultivationModel.fromSummaries(
            snapshot,
            forgeSummary,
            trainingSummary,
            clusterLoad,
            clusterCapacity,
            clusterActiveCount
        );
    }

    static Object buildOverviewPlanForTesting(
        final List<FlyingSwordHudState.SwordDisplayData> distanceOrderedRoster,
        @Nullable final UUID selectedSwordId
    ) {
        return OverviewContent.buildPlan(distanceOrderedRoster, selectedSwordId);
    }

    static int helpTabIndex() {
        return TAB_HELP;
    }

    static boolean isCloseKey(final int keyCode) {
        return keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_TAB;
    }

    int currentTabForTesting() {
        return activeTab;
    }

    boolean closeRequestedForTesting() {
        return closeRequestedForTesting;
    }

    int currentCultivationRouteForTesting() {
        return activeCultivationRoute;
    }

    FlyingSwordHelpScreen openHelpRouteForTesting(final int routeIndex) {
        return createHubLinkedHelpScreen(resolveHelpRouteSpec(routeIndex).helpTabIndex());
    }

    void switchTabForTesting(final int tabIndex) {
        switchTab(tabIndex);
    }

    void switchCultivationRouteForTesting(final int routeIndex) {
        switchCultivationRoute(routeIndex);
    }

    void simulateInitForTesting(final int screenWidth, final int screenHeight) {
        this.width = screenWidth;
        this.height = screenHeight;
        rebuildUi();
    }

    @Override
    protected void init() {
        super.init();
        applyUiScale(getRoot());
        rebuildUi();
    }

    @Override
    public boolean keyPressed(
        final int keyCode,
        final int scanCode,
        final int modifiers
    ) {
        if (isCloseKey(keyCode)) {
            requestClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void rebuildUi() {
        applyUiScale(getRoot());
        getRoot().clearChildren();
        tabPanels.clear();
        tabButtons.clear();
        cultivationRoutePanels.clear();
        cultivationRouteButtons.clear();
        overviewSectionButtons.clear();
        overviewSectionPanels.clear();
        buildUi(getRoot());
        switchTab(activeTab);
    }

    private void applyUiScale(final UIRoot root) {
        final double uiScale = sanitizeScaleFactor(
            resolveHubUiScale()
        );
        root.getScaleConfig().setScaleMode(ScaleConfig.ScaleMode.CUSTOM);
        root.getScaleConfig().setCustomScaleFactor(uiScale);
        root.setDesignResolution(
            (int) Math.round((double) width / uiScale),
            (int) Math.round((double) height / uiScale)
        );
        root.setViewport(width, height);
    }

    private static double resolveHubUiScale() {
        try {
            return ClientConfig.INSTANCE.kongQiaoUiScale.get();
        } catch (IllegalStateException exception) {
            return 1.0;
        }
    }

    private static double sanitizeScaleFactor(final double scaleFactor) {
        if (Double.isNaN(scaleFactor) || Double.isInfinite(scaleFactor)) {
            return 1.0;
        }
        if (scaleFactor <= 0.0) {
            return 1.0;
        }
        return scaleFactor;
    }

    private void requestClose() {
        closeRequestedForTesting = true;
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    private void buildUi(final UIRoot root) {
        final int rootX = (root.getWidth() - WINDOW_WIDTH) / 2;
        final int rootY = (root.getHeight() - WINDOW_HEIGHT) / 2;
        final UIElement window = new UIElement() { };
        window.setFrame(rootX, rootY, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(window);

        final TacticalPanel background = new TacticalPanel(tacticalTheme);
        background.setSurface(TacticalSurface.ROOT);
        background.setTone(TacticalTone.INFO);
        background.setHighlighted(true);
        place(background, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.addChild(background);

        final Label titleLabel = createLabel(
            HUB_TITLE,
            tacticalTheme.textPrimaryColor(),
            Label.HorizontalAlign.CENTER
        );
        place(titleLabel, 0, TITLE_Y, WINDOW_WIDTH, TITLE_HEIGHT);
        window.addChild(titleLabel);

        final Label closeHintLabel = createLabel(
            KongqiaoI18n.text(KongqiaoI18n.FLYING_SWORD_HUB_CLOSE_HINT),
            tacticalTheme.textDimColor(),
            Label.HorizontalAlign.RIGHT
        );
        place(
            closeHintLabel,
            WINDOW_WIDTH - CLOSE_HINT_WIDTH - WINDOW_PADDING,
            CLOSE_HINT_Y,
            CLOSE_HINT_WIDTH,
            CLOSE_HINT_HEIGHT
        );
        window.addChild(closeHintLabel);

        final int availableWidth = WINDOW_WIDTH - WINDOW_PADDING * 2;
        final int buttonWidth =
            (availableWidth - TAB_GAP * (TOP_LEVEL_TABS.size() - 1)) / TOP_LEVEL_TABS.size();
        for (int tabIndex = 0; tabIndex < TOP_LEVEL_TABS.size(); tabIndex++) {
            createTabButton(
                window,
                tabIndex,
                TOP_LEVEL_TABS.get(tabIndex),
                WINDOW_PADDING + (buttonWidth + TAB_GAP) * tabIndex,
                TAB_BAR_Y,
                buttonWidth
            );
        }

        final int contentWidth = WINDOW_WIDTH - WINDOW_PADDING * 2;
        final int contentHeight = WINDOW_HEIGHT - CONTENT_TOP - CONTENT_BOTTOM_PADDING;

        final TacticalPanel overviewPanel = createOverviewPanel(contentWidth, contentHeight);
        place(overviewPanel, WINDOW_PADDING, CONTENT_TOP, contentWidth, contentHeight);
        window.addChild(overviewPanel);
        tabPanels.add(overviewPanel);

        final TacticalPanel cultivationPanel = createCultivationPanel(contentWidth, contentHeight);
        place(cultivationPanel, WINDOW_PADDING, CONTENT_TOP, contentWidth, contentHeight);
        window.addChild(cultivationPanel);
        tabPanels.add(cultivationPanel);

        final TacticalPanel helpPanel = createHelpPanel(contentWidth, contentHeight);
        place(helpPanel, WINDOW_PADDING, CONTENT_TOP, contentWidth, contentHeight);
        window.addChild(helpPanel);
        tabPanels.add(helpPanel);
    }

    private void createTabButton(
        final UIElement parent,
        final int tabIndex,
        final String text,
        final int x,
        final int y,
        final int width
    ) {
        final Button button = new Button(text, tacticalTheme.baseTheme());
        place(button, x, y, width, TAB_BUTTON_HEIGHT);
        button.setOnClick(() -> switchTab(tabIndex));
        parent.addChild(button);
        tabButtons.add(button);
    }

    private TacticalPanel createOverviewPanel(final int width, final int height) {
        return OverviewContent.createPanel(
            tacticalTheme,
            FlyingSwordHudState.getTacticalStateSnapshot(),
            this::switchTab,
            TAB_CULTIVATION,
            TAB_HELP,
            width,
            height
        );
    }

    private TacticalPanel createCultivationPanel(final int width, final int height) {
        final TacticalPanel panel = createContentPanel(TacticalTone.BENMING, width, height);
        final FlyingSwordHubCultivationModel.CultivationPanelState cultivationState =
            buildCultivationState();
        addSectionTitle(
            panel,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SUMMARY_TITLE),
            TacticalTone.BENMING,
            width
        );
        addBodyLabel(
            panel,
            SUMMARY_BODY_Y,
            width,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SUMMARY_BODY_PRIMARY)
        );
        addBodyLabel(
            panel,
            SUMMARY_BODY_Y + tacticalTheme.bodyLineHeight() + tacticalTheme.regularGap(),
            width,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SUMMARY_BODY_SECONDARY)
        );

        final int availableWidth = width - SECTION_PADDING * 2;
        final int cultivationButtonWidth =
            (availableWidth - CULTIVATION_TAB_GAP * (CULTIVATION_SUBTABS.size() - 1))
                / CULTIVATION_SUBTABS.size();
        for (int routeIndex = 0; routeIndex < CULTIVATION_SUBTABS.size(); routeIndex++) {
            createCultivationTabButton(
                panel,
                routeIndex,
                CULTIVATION_SUBTABS.get(routeIndex),
                SECTION_PADDING + (cultivationButtonWidth + CULTIVATION_TAB_GAP) * routeIndex,
                CULTIVATION_TAB_BAR_Y,
                cultivationButtonWidth
            );
        }

        final int detailWidth = width - SECTION_PADDING * 2;
        for (final FlyingSwordHubCultivationModel.CultivationRoute route : cultivationState.routes()) {
            final TacticalPanel detailPanel = createCultivationRoutePanel(route, detailWidth);
            place(
                detailPanel,
                SECTION_PADDING,
                CULTIVATION_DETAIL_TOP,
                detailWidth,
                CULTIVATION_DETAIL_HEIGHT
            );
            panel.addChild(detailPanel);
            cultivationRoutePanels.add(detailPanel);
        }

        final TacticalRouteCard scopeCard = createRouteCard(
            TacticalTone.NEUTRAL,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SCOPE_TITLE),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SCOPE_SUMMARY),
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SCOPE_ACTION)
        );
        scopeCard.setBadges(
            List.of(
                TacticalBadgeSpec.of(
                    localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SCOPE_BADGE_SHARED),
                    TacticalTone.INFO
                ),
                TacticalBadgeSpec.of(
                    localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SCOPE_BADGE_ORIGINAL),
                    TacticalTone.BENMING
                ),
                TacticalBadgeSpec.of(
                    localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_SCOPE_BADGE_NO_SLOT),
                    TacticalTone.WARNING
                )
            )
        );
        place(
            scopeCard,
            SECTION_PADDING,
            CULTIVATION_SCOPE_CARD_Y,
            width - SECTION_PADDING * 2,
            CULTIVATION_SCOPE_CARD_HEIGHT
        );
        panel.addChild(scopeCard);
        switchCultivationRoute(activeCultivationRoute);
        return panel;
    }

    private void createCultivationTabButton(
        final TacticalPanel parent,
        final int routeIndex,
        final String text,
        final int x,
        final int y,
        final int width
    ) {
        final Button button = new Button(text, tacticalTheme.baseTheme());
        place(button, x, y, width, CULTIVATION_TAB_HEIGHT);
        button.setOnClick(() -> switchCultivationRoute(routeIndex));
        parent.addChild(button);
        cultivationRouteButtons.add(button);
    }

    private TacticalPanel createCultivationRoutePanel(
        final FlyingSwordHubCultivationModel.CultivationRoute route,
        final int width
    ) {
        final TacticalPanel detailPanel = new TacticalPanel(tacticalTheme);
        detailPanel.setSurface(TacticalSurface.RAISED);
        detailPanel.setTone(route.tone());
        place(detailPanel, 0, 0, width, CULTIVATION_DETAIL_HEIGHT);

        addSectionTitle(
            detailPanel,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_DETAIL_TITLE, route.title()),
            route.tone(),
            width
        );
        addBodyLabel(detailPanel, SUMMARY_BODY_Y, width, route.summaryText());
        addBodyLabel(detailPanel, CULTIVATION_STATUS_Y, width, route.statusText());

        final TacticalBar progressBar = new TacticalBar(tacticalTheme);
        progressBar.setTone(route.tone());
        progressBar.setLabel(KongqiaoI18n.text(KongqiaoI18n.FLYING_SWORD_HUB_PROGRESS_LABEL));
        progressBar.setValueText(Component.literal(route.progressText()));
        progressBar.setFillRatio(route.progressRatio());
        place(
            progressBar,
            SECTION_PADDING,
            CULTIVATION_PROGRESS_BAR_Y,
            width - SECTION_PADDING * 2,
            CULTIVATION_PROGRESS_BAR_HEIGHT
        );
        detailPanel.addChild(progressBar);

        addCultivationRouteBadges(detailPanel, route, width);

        final Button actionButton = new Button(Component.literal(route.actionText()), tacticalTheme.baseTheme());
        place(
            actionButton,
            SECTION_PADDING,
            CULTIVATION_ACTION_BUTTON_Y,
            CULTIVATION_ACTION_BUTTON_WIDTH,
            CULTIVATION_ACTION_BUTTON_HEIGHT
        );
        actionButton.setOnClick(() -> executeCultivationRouteAction(route.actionType()));
        detailPanel.addChild(actionButton);
        return detailPanel;
    }

    private void addCultivationRouteBadges(
        final TacticalPanel parent,
        final FlyingSwordHubCultivationModel.CultivationRoute route,
        final int width
    ) {
        final int availableWidth = width - SECTION_PADDING * 2;
        int currentX = SECTION_PADDING;
        for (
            int badgeIndex = 0;
            badgeIndex < route.resourceBadges().size() && badgeIndex < CULTIVATION_RESOURCE_LIMIT;
            badgeIndex++
        ) {
            final TacticalBadge badge = new TacticalBadge(tacticalTheme);
            badge.setSpec(
                TacticalBadgeSpec.of(route.resourceBadges().get(badgeIndex), route.tone())
            );
            final int remainingWidth = SECTION_PADDING + availableWidth - currentX;
            if (remainingWidth <= 0) {
                break;
            }
            final int badgeWidth = Math.min(badge.suggestWidth(), remainingWidth);
            place(badge, currentX, CULTIVATION_BADGE_Y, badgeWidth, BADGE_HEIGHT);
            parent.addChild(badge);
            currentX += badgeWidth + BADGE_GAP;
        }
    }

    private TacticalPanel createHelpPanel(final int width, final int height) {
        final TacticalPanel panel = createContentPanel(TacticalTone.WARNING, width, height);
        addSectionTitle(
            panel,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_SUMMARY_TITLE),
            TacticalTone.WARNING,
            width
        );
        addBodyLabel(
            panel,
            SUMMARY_BODY_Y,
            width,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_SUMMARY_BODY_PRIMARY)
        );
        addBodyLabel(
            panel,
            SUMMARY_BODY_Y + tacticalTheme.bodyLineHeight() + tacticalTheme.regularGap(),
            width,
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_SUMMARY_BODY_SECONDARY)
        );

        for (int routeIndex = 0; routeIndex < HELP_ROUTE_SPECS.size(); routeIndex++) {
            final int cardY =
                HELP_ROUTE_TOP + (HELP_ROUTE_CARD_HEIGHT + HELP_ROUTE_GAP) * routeIndex;
            addHelpRouteCard(
                panel,
                width,
                cardY,
                HELP_ROUTE_SPECS.get(routeIndex)
            );
        }
        return panel;
    }

    private void addHelpRouteCard(
        final TacticalPanel panel,
        final int width,
        final int y,
        final HelpRouteSpec routeSpec
    ) {
        final TacticalRouteCard routeCard = createRouteCard(
            routeSpec.tone(),
            routeSpec.title(),
            routeSpec.summary(),
            routeSpec.actionText()
        );
        routeCard.setBadges(
            List.of(
                TacticalBadgeSpec.of(
                    localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_BADGE_HUB),
                    TacticalTone.NEUTRAL
                ),
                TacticalBadgeSpec.of(
                    localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_BADGE_DEEP_HELP),
                    routeSpec.tone()
                ),
                TacticalBadgeSpec.of(
                    localizedText(KongqiaoI18n.FLYING_SWORD_HUB_HELP_BADGE_RETURN),
                    TacticalTone.WARNING
                )
            )
        );
        routeCard.setOnClick(() -> openHelpScreen(routeSpec.helpTabIndex()));
        place(
            routeCard,
            SECTION_PADDING,
            y,
            width - SECTION_PADDING * 2,
            HELP_ROUTE_CARD_HEIGHT
        );
        panel.addChild(routeCard);
    }

    private TacticalPanel createContentPanel(
        final TacticalTone tone,
        final int width,
        final int height
    ) {
        final TacticalPanel panel = new TacticalPanel(tacticalTheme);
        panel.setSurface(TacticalSurface.SECTION);
        panel.setTone(tone);
        place(panel, 0, 0, width, height);
        return panel;
    }

    private TacticalRouteCard createRouteCard(
        final TacticalTone tone,
        final String title,
        final String summary,
        final String actionText
    ) {
        final TacticalRouteCard routeCard = new TacticalRouteCard(tacticalTheme);
        routeCard.setTone(tone);
        routeCard.setTitle(Component.literal(title));
        routeCard.setSummary(Component.literal(summary));
        routeCard.setActionText(Component.literal(actionText));
        routeCard.setBadges(List.of());
        return routeCard;
    }

    private void openHelpScreen(final int helpTabIndex) {
        if (minecraft != null) {
            minecraft.setScreen(createHubLinkedHelpScreen(helpTabIndex));
        }
    }

    private FlyingSwordHubCultivationModel.CultivationPanelState buildCultivationState() {
        final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot =
            FlyingSwordHudState.getTacticalStateSnapshot();
        FlyingSwordForgeAttachment forgeAttachment = null;
        FlyingSwordTrainingAttachment trainingAttachment = null;
        if (minecraft != null && minecraft.player != null) {
            forgeAttachment = KongqiaoAttachments.getFlyingSwordForge(minecraft.player);
            trainingAttachment = KongqiaoAttachments.getFlyingSwordTraining(minecraft.player);
        }
        return FlyingSwordHubCultivationModel.fromSources(
            snapshot,
            forgeAttachment,
            trainingAttachment,
            ClusterClientStateCache.getCurrentLoad(),
            ClusterClientStateCache.getMaxComputation(),
            ClusterClientStateCache.getActiveSwordUuids().size()
        );
    }

    private FlyingSwordHelpScreen createHubLinkedHelpScreen(final int helpTabIndex) {
        return FlyingSwordHelpScreen.openFromHub(helpTabIndex);
    }

    private static final class OverviewContent {

        private static final String EMPTY_ROSTER_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_EMPTY_ROSTER_TITLE);
        private static final String EMPTY_RESONANCE_TEXT =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_EMPTY_RESONANCE);
        private static final String EMPTY_VALUE_TEXT = "--";
        private static final String FOCUS_NONE_BADGE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_FOCUS_NONE_BADGE);
        private static final String WINDOW_BADGE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_WINDOW_BADGE);
        private static final String BENMING_ONLINE_TEXT =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_ONLINE);
        private static final String BENMING_NONE_TEXT =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_NONE);
        private static final String FOCUS_PREFIX =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_FOCUS_PREFIX);
        private static final String ROUTE_NOTE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_NOTE);
        private static final String ROSTER_NOTE_PREFIX =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROSTER_NOTE_PREFIX);
        private static final String ROSTER_EMPTY_NOTE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROSTER_EMPTY_NOTE);
        private static final String ACTION_ROUTE_HELP =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_ROUTE_HELP);
        private static final String ACTION_ROUTE_DEFAULT =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_ROUTE_DEFAULT);
        private static final String ACTION_BURST_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_TITLE);
        private static final String ACTION_SQUAD_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_SQUAD_TITLE);
        private static final String ACTION_ROUTE_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_ROUTE_TITLE);
        private static final String ACTION_BURST_NO_SWORD =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_NO_SWORD);
        private static final String ACTION_BURST_NO_BENMING =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_NO_BENMING);
        private static final String ACTION_BURST_NOT_BENMING =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_NOT_BENMING);
        private static final String ACTION_BURST_BACKLASH =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_BACKLASH);
        private static final String ACTION_BURST_RECOVERY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_RECOVERY);
        private static final String ACTION_BURST_AFTERSHOCK =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_AFTERSHOCK);
        private static final String ACTION_BURST_DANGER =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_DANGER);
        private static final String ACTION_BURST_COOLDOWN =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_COOLDOWN);
        private static final String ACTION_BURST_READY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_BURST_READY);
        private static final String ACTION_SQUAD_NO_SWORD =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_SQUAD_NO_SWORD);
        private static final String ACTION_SQUAD_READY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_SQUAD_READY);
        private static final String ROUTE_EMPTY_ROSTER =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_EMPTY_ROSTER);
        private static final String ROUTE_NO_BENMING =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_NO_BENMING);
        private static final String ROUTE_DANGER =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_DANGER);
        private static final String ROUTE_BACKLASH =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_BACKLASH);
        private static final String ROUTE_RECOVERY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_RECOVERY);
        private static final String ROUTE_AFTERSHOCK =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_AFTERSHOCK);
        private static final String ROUTE_BURST_READY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_BURST_READY);
        private static final String ROUTE_STABLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_STABLE);
        private static final String ROUTE_CULTIVATION_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_CULTIVATION_TITLE);
        private static final String ROUTE_RECOMMENDATION_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_RECOMMENDATION_TITLE);
        private static final String ROUTE_GROWTH_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_TITLE);
        private static final String ROUTE_TO_CULTIVATION =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_TO_CULTIVATION);
        private static final String ROUTE_TO_HELP =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_TO_HELP);
        private static final String ROUTE_STAY_OVERVIEW =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_STAY_OVERVIEW);
        private static final String COLUMN_ROSTER_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_COLUMN_ROSTER_TITLE);
        private static final String COLUMN_FOCUS_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_COLUMN_FOCUS_TITLE);
        private static final String COLUMN_ACTION_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_COLUMN_ACTION_TITLE);
        private static final String COLUMN_ROUTE_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_COLUMN_ROUTE_TITLE);
        private static final String SUMMARY_SQUAD_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_SQUAD_TITLE);
        private static final String SUMMARY_BENMING_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_BENMING_TITLE);
        private static final String SUMMARY_GROWTH_TITLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_GROWTH_TITLE);
        private static final String BADGE_SELECTED =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_SELECTED);
        private static final String BADGE_RECENT =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_RECENT);
        private static final String BADGE_STABLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_STABLE);
        private static final String BADGE_EMPTY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_EMPTY);
        private static final String BADGE_HELP =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_HELP);
        private static final String BADGE_CULTIVATION =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_CULTIVATION);
        private static final String BADGE_GROWTH =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_GROWTH);
        private static final String BADGE_BURST_READY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_BURST_READY);
        private static final String BADGE_NO_PRIORITY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_NO_PRIORITY);
        private static final String BADGE_BENMING_IN_WINDOW = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_BENMING_IN_WINDOW
        );
        private static final String BADGE_BENMING_OUT_WINDOW = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_BENMING_OUT_WINDOW
        );
        private static final String SUMMARY_SQUAD_LINE_EMPTY = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_SQUAD_LINE_EMPTY
        );
        private static final String SUMMARY_BENMING_IDLE_PHASE = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_BENMING_IDLE_PHASE
        );
        private static final String SUMMARY_BENMING_IDLE_GUIDE = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_BENMING_IDLE_GUIDE
        );
        private static final String SUMMARY_GROWTH_IDLE_TITLE = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_GROWTH_IDLE_TITLE
        );
        private static final String SUMMARY_GROWTH_IDLE_BODY = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_GROWTH_IDLE_BODY
        );
        private static final String SUMMARY_GROWTH_IDLE_GUIDE = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_GROWTH_IDLE_GUIDE
        );
        private static final String FOCUS_EMPTY_BODY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_FOCUS_EMPTY_BODY);
        private static final String FOCUS_EMPTY_GUIDE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_FOCUS_EMPTY_GUIDE);
        private static final String ROUTE_CULTIVATION_EMPTY = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_CULTIVATION_EMPTY
        );
        private static final String ROUTE_GROWTH_EMPTY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_EMPTY);
        private static final String LEVEL_PREFIX = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_LEVEL_PREFIX
        );
        private static final String DISTANCE_PREFIX = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_DISTANCE_PREFIX
        );
        private static final String MODE_PREFIX = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_MODE_PREFIX
        );
        private static final String QUALITY_PREFIX = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_QUALITY_PREFIX
        );
        private static final String RESONANCE_PREFIX = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_RESONANCE_PREFIX
        );
        private static final String HEALTH_PREFIX =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_DURABILITY) + " ";
        private static final String EXP_PREFIX = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_EXPERIENCE_PROGRESS_PREFIX
        );
        private static final String OVERLOAD_PREFIX =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_OVERLOAD) + " ";
        private static final String PHASE_PREFIX = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_PHASE_PREFIX
        );
        private static final String LABEL_DURABILITY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_DURABILITY);
        private static final String LABEL_EXPERIENCE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_EXPERIENCE);
        private static final String LABEL_OVERLOAD =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_OVERLOAD);
        private static final String LABEL_BENMING_OVERLOAD = localizedText(
            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_BENMING_OVERLOAD
        );
        private static final String SWORD_SUFFIX =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SWORD_SUFFIX);
        private static final String DISTANCE_SUFFIX =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_DISTANCE_SUFFIX);
        private static final String LEVEL_SEPARATOR = " · ";
        private static final String BENMING_STABLE =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_PHASE_STABLE);
        private static final String BENMING_WARNING =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_PHASE_WARNING);
        private static final String BENMING_DANGER =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_PHASE_DANGER);
        private static final String BENMING_BACKLASH =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_PHASE_BACKLASH);
        private static final String BENMING_RECOVERY =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_PHASE_RECOVERY);
        private static final String BENMING_AFTERSHOCK =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_PHASE_AFTERSHOCK);
        private static final String BENMING_BURST =
            localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_PHASE_BURST);

        private static final float FULL_PERCENT = 100.0F;
        private static final float WARNING_HEALTH_RATIO = 0.65F;
        private static final float DANGER_HEALTH_RATIO = 0.35F;

        private static final int ROOT_PADDING = 12;
        private static final int SECTION_RAIL_WIDTH = 156;
        private static final int SECTION_RAIL_ITEM_HEIGHT = 26;
        private static final int SECTION_RAIL_GAP = 6;
        private static final int SUMMARY_CARD_HEIGHT = 102;
        private static final int SUMMARY_CARD_GAP = 8;
        private static final int SUMMARY_TITLE_Y = 16;
        private static final int SUMMARY_BODY_Y = 32;
        private static final int SUMMARY_BODY_HEIGHT = 30;
        private static final int BODY_TOP = 100;
        private static final int COLUMN_GAP = 8;
        private static final int LEFT_COLUMN_WIDTH = 214;
        private static final int RIGHT_COLUMN_WIDTH = 198;
        private static final int COLUMN_TITLE_Y = 16;
        private static final int COLUMN_NOTE_Y = 34;
        private static final int COLUMN_NOTE_HEIGHT = 20;
        private static final int COLUMN_CONTENT_TOP = 52;
        private static final int ROSTER_ITEM_HEIGHT = 84;
        private static final int ROSTER_ITEM_GAP = 6;
        private static final int SCROLLBAR_SPACE = 10;
        private static final int FOCUS_DETAIL_HEIGHT = 156;
        private static final int FOCUS_ACTION_GAP = 8;
        private static final int FOCUS_META_Y = 38;
        private static final int FOCUS_META_HEIGHT = 40;
        private static final int FOCUS_BADGE_Y = 84;
        private static final int FOCUS_BAR1_Y = 98;
        private static final int FOCUS_BAR2_Y = 122;
        private static final int FOCUS_BAR3_Y = 146;
        private static final int ACTION_CARD_TITLE_Y = 16;
        private static final int ACTION_ROW_TOP = 32;
        private static final int ACTION_ROW_HEIGHT = 38;
        private static final int ACTION_ROW_GAP = 6;
        private static final int ACTION_BADGE_MIN_WIDTH = 36;
        private static final int ROUTE_CARD_HEIGHT = 92;
        private static final int ROUTE_CARD_GAP = 6;

        private OverviewContent() {
        }

        private static OverviewPlan buildPlan(
            final List<FlyingSwordHudState.SwordDisplayData> distanceOrderedRoster,
            @Nullable final UUID selectedSwordId
        ) {
            return buildPlan(
                FlyingSwordTacticalStateService.snapshotFromRoster(distanceOrderedRoster, selectedSwordId)
            );
        }

        private static OverviewPlan buildPlan(@Nullable final TacticalStateSnapshot snapshot) {
            final TacticalStateSnapshot safeSnapshot = snapshot == null
                ? FlyingSwordTacticalStateService.snapshotFromRoster(List.of(), null)
                : snapshot;
            final SquadSummary squadSummary = safeSnapshot.squadSummary();
            final HubOverview hubOverview = safeSnapshot.hubOverview();
            final HelpSignals helpSignals = safeSnapshot.helpSignals();
            final FlyingSwordViewModel focusView = safeSnapshot.focusSword().sword();
            final int hiddenCount = Math.max(
                0,
                squadSummary.totalCount() - hubOverview.visibleDisplayWindow().size()
            );
            return new OverviewPlan(
                List.of(
                    buildSquadSummaryCard(safeSnapshot, hiddenCount),
                    buildBenmingSummaryCard(squadSummary.benmingSummary(), hubOverview),
                    buildGrowthSummaryCard(safeSnapshot.focusSword().source(), focusView)
                ),
                buildRosterEntries(hubOverview.visibleDisplayWindow(), focusView),
                hiddenCount,
                buildRosterCaption(hubOverview, hiddenCount),
                buildFocusDetail(
                    safeSnapshot.focusSword().source(),
                    focusView,
                    squadSummary.benmingSummary(),
                    helpSignals
                ),
                buildRouteCards(
                    safeSnapshot.focusSword().source(),
                    focusView,
                    squadSummary,
                    hubOverview,
                    helpSignals
                )
            );
        }

        private static TacticalPanel createPanel(
            final TacticalTheme theme,
            @Nullable final TacticalStateSnapshot snapshot,
            final IntConsumer tabNavigator,
            final int cultivationTabIndex,
            final int helpTabIndex,
            final int width,
            final int height
        ) {
            final OverviewPlan plan = buildPlan(snapshot);
            final TacticalPanel root = new TacticalPanel(theme);
            root.setSurface(TacticalSurface.SECTION);
            root.setTone(TacticalTone.INFO);
            place(root, 0, 0, width, height);

            final int summaryHeight = layoutSummaryCards(root, theme, plan.summaryCards(), width);
            final int bodyTop = Math.max(BODY_TOP, ROOT_PADDING + summaryHeight + ROOT_PADDING);
            final int bodyHeight = Math.max(0, height - bodyTop - ROOT_PADDING);
            final int railX = ROOT_PADDING;
            final int railWidth = SECTION_RAIL_WIDTH;
            final int contentX = railX + railWidth + COLUMN_GAP;
            final int contentWidth = Math.max(0, width - contentX - ROOT_PADDING);
            final List<Button> sectionButtons = new ArrayList<>();
            final List<UIElement> sectionPanels = new ArrayList<>();

            final TacticalPanel railPanel = new TacticalPanel(theme);
            railPanel.setSurface(TacticalSurface.SECTION);
            railPanel.setTone(TacticalTone.INFO);
            place(railPanel, railX, bodyTop, railWidth, bodyHeight);
            root.addChild(railPanel);
            addOverviewSectionButtons(
                railPanel,
                theme,
                bodyHeight,
                sectionButtons,
                sectionPanels
            );

            final TacticalPanel summaryPanel = createSummarySectionPanel(
                theme,
                plan.summaryCards(),
                contentWidth,
                bodyHeight
            );
            place(summaryPanel, contentX, bodyTop, contentWidth, bodyHeight);
            root.addChild(summaryPanel);

            final TacticalPanel rosterPanel = createRosterColumn(
                theme,
                plan,
                contentWidth,
                bodyHeight
            );
            place(rosterPanel, contentX, bodyTop, contentWidth, bodyHeight);
            root.addChild(rosterPanel);

            final TacticalPanel focusPanel = createFocusColumn(
                theme,
                plan.focusDetail(),
                contentWidth,
                bodyHeight
            );
            place(focusPanel, contentX, bodyTop, contentWidth, bodyHeight);
            root.addChild(focusPanel);

            final TacticalPanel routePanel = createRouteColumn(
                theme,
                plan.routeCards(),
                tabNavigator,
                cultivationTabIndex,
                helpTabIndex,
                contentWidth,
                bodyHeight
            );
            place(routePanel, contentX, bodyTop, contentWidth, bodyHeight);
            root.addChild(routePanel);

            registerOverviewSection(sectionPanels, OVERVIEW_SECTION_SUMMARY, summaryPanel);
            registerOverviewSection(sectionPanels, OVERVIEW_SECTION_ROSTER, rosterPanel);
            registerOverviewSection(sectionPanels, OVERVIEW_SECTION_FOCUS, focusPanel);
            registerOverviewSection(sectionPanels, OVERVIEW_SECTION_ROUTE, routePanel);
            switchOverviewSection(sectionButtons, sectionPanels, OVERVIEW_SECTION_SUMMARY);
            return root;
        }

        private static TacticalPanel createSummarySectionPanel(
            final TacticalTheme theme,
            final List<SummaryCardPlan> summaryCards,
            final int width,
            final int height
        ) {
            final TacticalPanel section = new TacticalPanel(theme);
            section.setSurface(TacticalSurface.SECTION);
            section.setTone(TacticalTone.INFO);
            place(section, 0, 0, width, height);
            final int contentTop = addColumnHeader(
                section,
                theme,
                SUMMARY_SQUAD_TITLE,
                ACTION_ROUTE_DEFAULT,
                width,
                TacticalTone.INFO
            );
            final ScrollContainer scroll = new ScrollContainer(theme.baseTheme());
            final int scrollWidth = width - ROOT_PADDING * 2;
            final int scrollHeight = Math.max(0, height - contentTop - ROOT_PADDING);
            place(scroll, ROOT_PADDING, contentTop, scrollWidth, scrollHeight);
            section.addChild(scroll);
            final UIElement content = new UIElement() { };
            int cursorY = 0;
            for (final SummaryCardPlan plan : summaryCards) {
                final TacticalPanel card = new TacticalPanel(theme);
                card.setSurface(TacticalSurface.RAISED);
                card.setTone(plan.tone());
                final String bodyText = String.join(
                    "\n",
                    plan.lines().stream().map(FlyingSwordHubScreen::formatDenseMultilineText).toList()
                );
                final int bodyHeight = Math.max(
                    SUMMARY_BODY_HEIGHT,
                    resolveMultilineHeight(bodyText, theme.bodyLineHeight())
                );
                final int badgeY = SUMMARY_BODY_Y + bodyHeight + theme.regularGap();
                final int cardHeight = Math.max(
                    SUMMARY_CARD_HEIGHT,
                    badgeY + theme.badgeHeight() + ROOT_PADDING
                );
                place(card, 0, cursorY, scrollWidth - SCROLLBAR_SPACE, cardHeight);
                content.addChild(card);

                final Label titleLabel = createLabel(
                    theme,
                    Component.literal(plan.title()),
                    theme.accentColor(plan.tone()),
                    Label.HorizontalAlign.LEFT
                );
                place(
                    titleLabel,
                    ROOT_PADDING,
                    SUMMARY_TITLE_Y,
                    card.getWidth() - ROOT_PADDING * 2,
                    theme.titleLineHeight()
                );
                card.addChild(titleLabel);

                final Label bodyLabel = createLabel(
                    theme,
                    Component.literal(bodyText),
                    theme.textDimColor(),
                    Label.HorizontalAlign.LEFT
                );
                place(
                    bodyLabel,
                    ROOT_PADDING,
                    SUMMARY_BODY_Y,
                    card.getWidth() - ROOT_PADDING * 2,
                    bodyHeight
                );
                card.addChild(bodyLabel);
                addBadges(card, theme, plan.badges(), ROOT_PADDING, badgeY, card.getWidth() - ROOT_PADDING * 2);
                cursorY += cardHeight + SUMMARY_CARD_GAP;
            }
            content.setFrame(0, 0, scrollWidth - SCROLLBAR_SPACE, Math.max(0, cursorY - SUMMARY_CARD_GAP));
            scroll.setContent(content);
            return section;
        }

        private static SummaryCardPlan buildSquadSummaryCard(
            final TacticalStateSnapshot snapshot,
            final int hiddenCount
        ) {
            final SquadSummary squadSummary = snapshot.squadSummary();
            TacticalTone tone = TacticalTone.INFO;
            if (squadSummary.dangerCount() > 0) {
                tone = TacticalTone.DANGER;
            } else if (squadSummary.warningCount() > 0) {
                tone = TacticalTone.WARNING;
            }
            final List<BadgePlan> badges = new ArrayList<>();
            badges.add(
                new BadgePlan(
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_SWORD_COUNT,
                        squadSummary.totalCount()
                    ),
                    TacticalTone.INFO
                )
            );
            if (squadSummary.benmingCount() > 0) {
                badges.add(new BadgePlan(BENMING_ONLINE_TEXT, TacticalTone.BENMING));
            }
            if (hiddenCount > 0) {
                badges.add(
                    new BadgePlan(
                        localizedText(
                            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_HIDDEN_COUNT,
                            hiddenCount
                        ),
                        TacticalTone.MUTED
                    )
                );
            }
            final List<String> lines = List.of(
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_SQUAD_LINE_COUNTS,
                    squadSummary.totalCount(),
                    squadSummary.selectedCount()
                ),
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_SQUAD_LINE_ALERTS,
                    squadSummary.warningCount(),
                    squadSummary.dangerCount()
                ),
                snapshot.hubOverview().visibleDisplayWindow().isEmpty()
                    ? SUMMARY_SQUAD_LINE_EMPTY
                    : localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_SUMMARY_SQUAD_LINE_VISIBLE,
                        squadSummary.burstReadyCount(),
                        snapshot.hubOverview().visibleDisplayWindow().size()
                    )
            );
            return new SummaryCardPlan(SUMMARY_SQUAD_TITLE, tone, lines, badges);
        }

        private static SummaryCardPlan buildBenmingSummaryCard(
            @Nullable final BenmingSummary benmingSummary,
            final HubOverview hubOverview
        ) {
            if (benmingSummary == null) {
                return new SummaryCardPlan(
                    SUMMARY_BENMING_TITLE,
                    TacticalTone.MUTED,
                    List.of(BENMING_NONE_TEXT, SUMMARY_BENMING_IDLE_PHASE, SUMMARY_BENMING_IDLE_GUIDE),
                    List.of(
                        new BadgePlan(BADGE_EMPTY, TacticalTone.MUTED),
                        new BadgePlan(
                            hubOverview.benmingInsideWindow()
                                ? BENMING_ONLINE_TEXT
                                : BADGE_BENMING_OUT_WINDOW,
                            TacticalTone.MUTED
                        )
                    )
                );
            }
            final List<BadgePlan> badges = new ArrayList<>();
            badges.add(new BadgePlan(BENMING_ONLINE_TEXT, TacticalTone.BENMING));
            badges.add(
                new BadgePlan(
                    hubOverview.benmingInsideWindow()
                        ? BADGE_BENMING_IN_WINDOW
                        : BADGE_BENMING_OUT_WINDOW,
                    hubOverview.benmingInsideWindow() ? TacticalTone.INFO : TacticalTone.WARNING
                )
            );
            if (benmingSummary.overloadDanger()) {
                badges.add(new BadgePlan(resolveDangerBadgeText(benmingSummary.resonanceType()), TacticalTone.DANGER));
            } else if (benmingSummary.highlightWarning()) {
                badges.add(new BadgePlan(resolvePreWarningBadgeText(), TacticalTone.WARNING));
            }
            if (benmingSummary.burstReady()) {
                badges.add(new BadgePlan(resolveBurstBadgeText(benmingSummary.resonanceType()), TacticalTone.BENMING));
            }
            final List<String> lines = List.of(
                RESONANCE_PREFIX + resolveResonanceText(benmingSummary.resonanceType()),
                OVERLOAD_PREFIX + formatOverloadText(benmingSummary.overloadPercent()),
                PHASE_PREFIX + resolveBenmingPhaseText(benmingSummary)
            );
            return new SummaryCardPlan(
                SUMMARY_BENMING_TITLE,
                resolveBenmingTone(benmingSummary),
                lines,
                badges
            );
        }

        private static SummaryCardPlan buildGrowthSummaryCard(
            final FocusSource focusSource,
            @Nullable final FlyingSwordViewModel focusView
        ) {
            if (focusView == null) {
                return new SummaryCardPlan(
                    SUMMARY_GROWTH_TITLE,
                    TacticalTone.MUTED,
                    List.of(
                        SUMMARY_GROWTH_IDLE_TITLE,
                        SUMMARY_GROWTH_IDLE_BODY,
                        SUMMARY_GROWTH_IDLE_GUIDE
                    ),
                    List.of(
                        new BadgePlan(FOCUS_NONE_BADGE, TacticalTone.MUTED),
                        new BadgePlan(BADGE_HELP, TacticalTone.WARNING)
                    )
                );
            }
            return new SummaryCardPlan(
                SUMMARY_GROWTH_TITLE,
                resolveViewTone(focusView),
                List.of(
                    QUALITY_PREFIX + focusView.quality().getDisplayName() + LEVEL_SEPARATOR + LEVEL_PREFIX
                        + focusView.level(),
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_PROGRESS,
                        formatPercent(focusView.expProgress())
                    ) + LEVEL_SEPARATOR + localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_LEVEL_CAP,
                        focusView.quality().getMaxLevel()
                    ),
                    HEALTH_PREFIX + formatHealthText(focusView)
                ),
                List.of(
                    new BadgePlan(describeFocusSourceBadge(focusSource), TacticalTone.INFO),
                    new BadgePlan(
                        focusView.burstReady() ? BADGE_BURST_READY : BADGE_GROWTH,
                        focusView.burstReady() ? TacticalTone.BENMING : TacticalTone.INFO
                    )
                )
            );
        }

        private static String buildRosterCaption(
            final HubOverview hubOverview,
            final int hiddenCount
        ) {
            if (hubOverview.visibleDisplayWindow().isEmpty()) {
                return ROSTER_NOTE_PREFIX + ROSTER_EMPTY_NOTE;
            }
            if (hiddenCount > 0) {
                return ROSTER_NOTE_PREFIX + WINDOW_BADGE + " "
                    + localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROSTER_CAPTION_HIDDEN,
                        hiddenCount
                    );
            }
            return ROSTER_NOTE_PREFIX + WINDOW_BADGE + " "
                + localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROSTER_CAPTION_FULL);
        }

        private static List<RosterEntryPlan> buildRosterEntries(
            final List<FlyingSwordViewModel> visibleWindow,
            @Nullable final FlyingSwordViewModel focusView
        ) {
            final List<RosterEntryPlan> entries = new ArrayList<>();
            final UUID focusUuid = focusView == null ? null : focusView.uuid();
            for (final FlyingSwordViewModel view : visibleWindow) {
                final List<BadgePlan> badges = new ArrayList<>();
                if (view.benmingSword()) {
                    badges.add(new BadgePlan(resolveMarkBadgeText(), TacticalTone.BENMING));
                }
                if (view.selected() && !view.benmingSword()) {
                    badges.add(new BadgePlan(BADGE_SELECTED, TacticalTone.INFO));
                }
                if (view.overloadDanger()) {
                    badges.add(new BadgePlan(resolveDangerBadgeText(view.resonanceType()), TacticalTone.DANGER));
                } else if (view.highlightWarning()) {
                    badges.add(new BadgePlan(resolvePreWarningBadgeText(), TacticalTone.WARNING));
                }
                if (view.burstReady()) {
                    badges.add(new BadgePlan(resolveBurstBadgeText(view.resonanceType()), TacticalTone.BENMING));
                }
                entries.add(
                    new RosterEntryPlan(
                        view.uuid(),
                        view.quality().getDisplayName() + SWORD_SUFFIX,
                        LEVEL_PREFIX + view.level() + LEVEL_SEPARATOR + resolveResonanceText(view.resonanceType()),
                        view.aiMode().getDisplayName(),
                        formatDistance(view.distance()),
                        formatHealthText(view),
                        normalizeRatio(view.healthPercent()),
                        resolveDurabilityTone(view.healthPercent()),
                        view.benmingSword() ? formatOverloadText(view.overloadPercent()) : EMPTY_VALUE_TEXT,
                        view.benmingSword() ? normalizeOverloadRatio(view.overloadPercent()) : 0.0F,
                        view.benmingSword() ? resolveViewTone(view) : TacticalTone.MUTED,
                        resolveViewTone(view),
                        focusUuid != null && focusUuid.equals(view.uuid()),
                        badges
                    )
                );
            }
            return List.copyOf(entries);
        }

        private static FocusDetailPlan buildFocusDetail(
            final FocusSource focusSource,
            @Nullable final FlyingSwordViewModel focusView,
            @Nullable final BenmingSummary benmingSummary,
            final HelpSignals helpSignals
        ) {
            if (focusView == null) {
                return new FocusDetailPlan(
                    EMPTY_ROSTER_TITLE,
                    TacticalTone.MUTED,
                    List.of(
                        FOCUS_PREFIX + describeFocusSource(focusSource),
                        FOCUS_EMPTY_BODY,
                        FOCUS_EMPTY_GUIDE
                    ),
                    List.of(new BadgePlan(FOCUS_NONE_BADGE, TacticalTone.MUTED)),
                    new FocusBarPlan(LABEL_DURABILITY, EMPTY_VALUE_TEXT, 0.0F, TacticalTone.MUTED),
                    new FocusBarPlan(LABEL_EXPERIENCE, EMPTY_VALUE_TEXT, 0.0F, TacticalTone.MUTED),
                    new FocusBarPlan(LABEL_OVERLOAD, EMPTY_VALUE_TEXT, 0.0F, TacticalTone.MUTED),
                    List.of(
                        new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.MUTED, ACTION_BURST_NO_SWORD),
                        new ActionPlan("Z", ACTION_SQUAD_TITLE, false, TacticalTone.MUTED, ACTION_SQUAD_NO_SWORD),
                        new ActionPlan("H", ACTION_ROUTE_TITLE, true, TacticalTone.WARNING, ACTION_ROUTE_HELP)
                    )
                );
            }

            final List<BadgePlan> badges = new ArrayList<>();
            badges.add(new BadgePlan(describeFocusSourceBadge(focusSource), TacticalTone.INFO));
            if (focusView.benmingSword()) {
                badges.add(new BadgePlan(resolveMarkBadgeText(), TacticalTone.BENMING));
            }
            if (focusView.selected() && !focusView.benmingSword()) {
                badges.add(new BadgePlan(BADGE_SELECTED, TacticalTone.INFO));
            }
            if (focusView.overloadDanger()) {
                badges.add(new BadgePlan(resolveDangerBadgeText(focusView.resonanceType()), TacticalTone.DANGER));
            } else if (focusView.highlightWarning()) {
                badges.add(new BadgePlan(resolvePreWarningBadgeText(), TacticalTone.WARNING));
            } else {
                badges.add(new BadgePlan(BADGE_STABLE, TacticalTone.INFO));
            }

            final FocusBarPlan overloadBar = benmingSummary == null
                ? new FocusBarPlan(LABEL_OVERLOAD, EMPTY_VALUE_TEXT, 0.0F, TacticalTone.MUTED)
                : new FocusBarPlan(
                    focusView.benmingSword() ? LABEL_OVERLOAD : LABEL_BENMING_OVERLOAD,
                    formatOverloadText(benmingSummary.overloadPercent()),
                    normalizeOverloadRatio(benmingSummary.overloadPercent()),
                    resolveBenmingTone(benmingSummary)
                );

            return new FocusDetailPlan(
                focusView.quality().getDisplayName() + SWORD_SUFFIX,
                resolveViewTone(focusView),
                List.of(
                    FOCUS_PREFIX + describeFocusSource(focusSource),
                    QUALITY_PREFIX + focusView.quality().getDisplayName() + LEVEL_SEPARATOR + LEVEL_PREFIX
                        + focusView.level(),
                    MODE_PREFIX + focusView.aiMode().getDisplayName() + LEVEL_SEPARATOR + DISTANCE_PREFIX
                        + formatDistance(focusView.distance()),
                    RESONANCE_PREFIX + resolveResonanceText(focusView.resonanceType())
                ),
                badges,
                new FocusBarPlan(
                    LABEL_DURABILITY,
                    formatHealthText(focusView),
                    normalizeRatio(focusView.healthPercent()),
                    resolveDurabilityTone(focusView.healthPercent())
                ),
                new FocusBarPlan(
                    LABEL_EXPERIENCE,
                    LEVEL_PREFIX + focusView.level() + "/" + focusView.quality().getMaxLevel(),
                    normalizeRatio(focusView.expProgress()),
                    TacticalTone.INFO
                ),
                overloadBar,
                List.of(
                    buildBurstAction(focusView, benmingSummary),
                    new ActionPlan("Z", ACTION_SQUAD_TITLE, true, TacticalTone.INFO, ACTION_SQUAD_READY),
                    new ActionPlan(
                        "H",
                        ACTION_ROUTE_TITLE,
                        true,
                        resolveRouteTone(helpSignals),
                        resolveRouteDescription(helpSignals)
                    )
                )
            );
        }

        private static ActionPlan buildBurstAction(
            final FlyingSwordViewModel focusView,
            @Nullable final BenmingSummary benmingSummary
        ) {
            if (benmingSummary == null) {
                return new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.MUTED, ACTION_BURST_NO_BENMING);
            }
            if (!focusView.benmingSword()) {
                return new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.WARNING, ACTION_BURST_NOT_BENMING);
            }
            if (benmingSummary.overloadBacklashActive()) {
                return new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.DANGER, ACTION_BURST_BACKLASH);
            }
            if (benmingSummary.overloadRecoveryActive()) {
                return new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.WARNING, ACTION_BURST_RECOVERY);
            }
            if (benmingSummary.aftershockPeriod()) {
                return new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.INFO, ACTION_BURST_AFTERSHOCK);
            }
            if (benmingSummary.overloadDanger()) {
                return new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.DANGER, ACTION_BURST_DANGER);
            }
            if (!benmingSummary.burstReady()) {
                return new ActionPlan("G", ACTION_BURST_TITLE, false, TacticalTone.WARNING, ACTION_BURST_COOLDOWN);
            }
            return new ActionPlan("G", ACTION_BURST_TITLE, true, TacticalTone.BENMING, ACTION_BURST_READY);
        }

        private static List<RouteCardPlan> buildRouteCards(
            final FocusSource focusSource,
            @Nullable final FlyingSwordViewModel focusView,
            final SquadSummary squadSummary,
            final HubOverview hubOverview,
            final HelpSignals helpSignals
        ) {
            return List.of(
                buildCultivationRoute(focusView, squadSummary),
                buildRecommendationRoute(squadSummary, hubOverview, helpSignals),
                buildGrowthRoute(focusSource, focusView)
            );
        }

        private static RouteCardPlan buildCultivationRoute(
            @Nullable final FlyingSwordViewModel focusView,
            final SquadSummary squadSummary
        ) {
            final String summary = focusView == null
                ? ROUTE_CULTIVATION_EMPTY
                : localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_CULTIVATION_ACTIVE,
                    focusView.quality().getDisplayName() + SWORD_SUFFIX
                );
            return new RouteCardPlan(
                ROUTE_CULTIVATION_TITLE,
                focusView == null ? TacticalTone.WARNING : TacticalTone.INFO,
                List.of(summary),
                ROUTE_TO_CULTIVATION,
                RouteTarget.CULTIVATION,
                List.of(
                    new BadgePlan(BADGE_CULTIVATION, TacticalTone.INFO),
                    new BadgePlan(
                        localizedText(
                            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_SWORD_COUNT,
                            squadSummary.totalCount()
                        ),
                        TacticalTone.NEUTRAL
                    )
                )
            );
        }

        private static RouteCardPlan buildRecommendationRoute(
            final SquadSummary squadSummary,
            final HubOverview hubOverview,
            final HelpSignals helpSignals
        ) {
            final String summary;
            final TacticalTone tone;
            final RouteTarget target;
            if (squadSummary.totalCount() <= 0) {
                summary = ROUTE_EMPTY_ROSTER;
                tone = TacticalTone.WARNING;
                target = RouteTarget.HELP;
            } else if (!helpSignals.hasBenmingSword()) {
                summary = ROUTE_NO_BENMING;
                tone = TacticalTone.WARNING;
                target = RouteTarget.HELP;
            } else if (helpSignals.hasOverloadDanger()) {
                summary = ROUTE_DANGER;
                tone = TacticalTone.DANGER;
                target = RouteTarget.HELP;
            } else if (helpSignals.hasOverloadBacklash()) {
                summary = ROUTE_BACKLASH;
                tone = TacticalTone.DANGER;
                target = RouteTarget.HELP;
            } else if (helpSignals.hasOverloadRecovery()) {
                summary = ROUTE_RECOVERY;
                tone = TacticalTone.WARNING;
                target = RouteTarget.HELP;
            } else if (helpSignals.hasAftershock()) {
                summary = ROUTE_AFTERSHOCK;
                tone = TacticalTone.INFO;
                target = RouteTarget.HELP;
            } else if (helpSignals.hasBurstReady()) {
                summary = ROUTE_BURST_READY;
                tone = TacticalTone.BENMING;
                target = RouteTarget.CULTIVATION;
            } else {
                summary = ROUTE_STABLE;
                tone = hubOverview.visibleDisplayWindow().isEmpty() ? TacticalTone.MUTED : TacticalTone.INFO;
                target = RouteTarget.CULTIVATION;
            }
            final List<BadgePlan> badges = new ArrayList<>();
            if (target == RouteTarget.HELP) {
                badges.add(new BadgePlan(BADGE_HELP, tone));
            }
            if (helpSignals.hasBenmingSword()) {
                badges.add(new BadgePlan(BENMING_ONLINE_TEXT, TacticalTone.BENMING));
            }
            return new RouteCardPlan(
                ROUTE_RECOMMENDATION_TITLE,
                tone,
                List.of(summary),
                target == RouteTarget.HELP ? ROUTE_TO_HELP : ROUTE_TO_CULTIVATION,
                target,
                badges
            );
        }

        private static RouteCardPlan buildGrowthRoute(
            final FocusSource focusSource,
            @Nullable final FlyingSwordViewModel focusView
        ) {
            if (focusView == null) {
                return new RouteCardPlan(
                    ROUTE_GROWTH_TITLE,
                    TacticalTone.MUTED,
                    List.of(ROUTE_GROWTH_EMPTY),
                    ROUTE_STAY_OVERVIEW,
                    RouteTarget.NONE,
                    List.of(new BadgePlan(BADGE_EMPTY, TacticalTone.MUTED))
                );
            }
            return new RouteCardPlan(
                ROUTE_GROWTH_TITLE,
                resolveViewTone(focusView),
                List.of(
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_SUMMARY,
                        describeFocusSource(focusSource)
                    ),
                    LEVEL_PREFIX + focusView.level() + " / " + localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_LEVEL_CAP,
                        focusView.quality().getMaxLevel()
                    ),
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_PROGRESS,
                        formatPercent(focusView.expProgress())
                    )
                ),
                ROUTE_TO_CULTIVATION,
                RouteTarget.CULTIVATION,
                List.of(
                    new BadgePlan(LEVEL_PREFIX + focusView.level(), TacticalTone.INFO),
                    new BadgePlan(
                        localizedText(
                            KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ROUTE_GROWTH_MAX_BADGE,
                            focusView.quality().getMaxLevel()
                        ),
                        TacticalTone.NEUTRAL
                    )
                )
            );
        }

        private static int layoutSummaryCards(
            final TacticalPanel parent,
            final TacticalTheme theme,
            final List<SummaryCardPlan> summaryCards,
            final int width
        ) {
            final int cardWidth =
                (width - ROOT_PADDING * 2 - SUMMARY_CARD_GAP * (summaryCards.size() - 1))
                    / summaryCards.size();
            final List<String> bodyTexts = new ArrayList<>(summaryCards.size());
            final List<Integer> bodyHeights = new ArrayList<>(summaryCards.size());
            final List<Integer> badgeYs = new ArrayList<>(summaryCards.size());
            int maxCardHeight = SUMMARY_CARD_HEIGHT;
            for (final SummaryCardPlan plan : summaryCards) {
                final String bodyText = String.join(
                    "\n",
                    plan.lines().stream().map(FlyingSwordHubScreen::formatDenseMultilineText).toList()
                );
                final int bodyHeight = Math.max(
                    SUMMARY_BODY_HEIGHT,
                    resolveMultilineHeight(bodyText, theme.bodyLineHeight())
                );
                final int badgeY = SUMMARY_BODY_Y + bodyHeight + theme.regularGap();
                final int cardHeight = Math.max(
                    SUMMARY_CARD_HEIGHT,
                    badgeY + theme.badgeHeight() + ROOT_PADDING
                );
                bodyTexts.add(bodyText);
                bodyHeights.add(bodyHeight);
                badgeYs.add(badgeY);
                maxCardHeight = Math.max(maxCardHeight, cardHeight);
            }
            int cursorX = ROOT_PADDING;
            for (int index = 0; index < summaryCards.size(); index++) {
                final SummaryCardPlan plan = summaryCards.get(index);
                final String bodyText = bodyTexts.get(index);
                final int bodyHeight = bodyHeights.get(index);
                final int badgeY = badgeYs.get(index);
                final TacticalPanel card = new TacticalPanel(theme);
                card.setSurface(TacticalSurface.RAISED);
                card.setTone(plan.tone());
                place(
                    card,
                    cursorX,
                    ROOT_PADDING,
                    cardWidth,
                    maxCardHeight
                );
                parent.addChild(card);

                final Label titleLabel = createLabel(
                    theme,
                    Component.literal(plan.title()),
                    theme.accentColor(plan.tone()),
                    Label.HorizontalAlign.LEFT
                );
                place(
                    titleLabel,
                    ROOT_PADDING,
                    SUMMARY_TITLE_Y,
                    cardWidth - ROOT_PADDING * 2,
                    theme.titleLineHeight()
                );
                card.addChild(titleLabel);

                final Label bodyLabel = createLabel(
                    theme,
                    Component.literal(bodyText),
                    theme.textDimColor(),
                    Label.HorizontalAlign.LEFT
                );
                place(
                    bodyLabel,
                    ROOT_PADDING,
                    SUMMARY_BODY_Y,
                    cardWidth - ROOT_PADDING * 2,
                    bodyHeight
                );
                card.addChild(bodyLabel);
                addBadges(card, theme, plan.badges(), ROOT_PADDING, badgeY, cardWidth - ROOT_PADDING * 2);
                cursorX += cardWidth + SUMMARY_CARD_GAP;
            }
            return maxCardHeight;
        }

        private static TacticalPanel createRosterColumn(
            final TacticalTheme theme,
            final OverviewPlan plan,
            final int width,
            final int height
        ) {
            final TacticalPanel column = new TacticalPanel(theme);
            column.setSurface(TacticalSurface.SECTION);
            column.setTone(plan.hiddenRosterCount() > 0 ? TacticalTone.INFO : TacticalTone.NEUTRAL);
            place(column, 0, 0, width, height);
            final int contentTop = addColumnHeader(
                column,
                theme,
                COLUMN_ROSTER_TITLE,
                plan.rosterCaption(),
                width,
                TacticalTone.INFO
            );

            final ScrollContainer scroll = new ScrollContainer(theme.baseTheme());
            final int scrollWidth = width - ROOT_PADDING * 2;
            final int scrollHeight = Math.max(0, height - contentTop - ROOT_PADDING);
            place(scroll, ROOT_PADDING, contentTop, scrollWidth, scrollHeight);
            column.addChild(scroll);

            final UIElement content = new UIElement() { };
            final int contentWidth = Math.max(0, scrollWidth - SCROLLBAR_SPACE);
            if (plan.rosterEntries().isEmpty()) {
                final String emptyText = formatDenseMultilineText(ROSTER_EMPTY_NOTE);
                final int emptyHeight = resolveMultilineHeight(emptyText, theme.bodyLineHeight());
                final Label emptyLabel = createLabel(
                    theme,
                    Component.literal(emptyText),
                    theme.textDimColor(),
                    Label.HorizontalAlign.LEFT
                );
                place(
                    emptyLabel,
                    0,
                    0,
                    contentWidth,
                    emptyHeight
                );
                content.addChild(emptyLabel);
                content.setFrame(
                    0,
                    0,
                    contentWidth,
                    emptyHeight
                );
            } else {
                int cursorY = 0;
                for (final RosterEntryPlan entryPlan : plan.rosterEntries()) {
                    final TacticalSwordListItem item = new TacticalSwordListItem(theme);
                    item.setModel(toSwordListModel(entryPlan));
                    place(item, 0, cursorY, contentWidth, ROSTER_ITEM_HEIGHT);
                    content.addChild(item);
                    cursorY += ROSTER_ITEM_HEIGHT + ROSTER_ITEM_GAP;
                }
                content.setFrame(0, 0, contentWidth, Math.max(0, cursorY - ROSTER_ITEM_GAP));
            }
            scroll.setContent(content);
            return column;
        }

        private static TacticalPanel createFocusColumn(
            final TacticalTheme theme,
            final FocusDetailPlan plan,
            final int width,
            final int height
        ) {
            final TacticalPanel column = new TacticalPanel(theme);
            column.setSurface(TacticalSurface.SECTION);
            column.setTone(plan.tone());
            place(column, 0, 0, width, height);

            final TacticalPanel detailCard = new TacticalPanel(theme);
            detailCard.setSurface(TacticalSurface.RAISED);
            detailCard.setTone(plan.tone());
            detailCard.setHighlighted(true);
            place(detailCard, 0, 0, width, FOCUS_DETAIL_HEIGHT);
            column.addChild(detailCard);

            final Label titleLabel = createLabel(
                theme,
                Component.literal(COLUMN_FOCUS_TITLE + LEVEL_SEPARATOR + plan.title()),
                theme.accentColor(plan.tone()),
                Label.HorizontalAlign.LEFT
            );
            place(titleLabel, ROOT_PADDING, COLUMN_TITLE_Y, width - ROOT_PADDING * 2, theme.titleLineHeight());
            detailCard.addChild(titleLabel);

            final Label metaLabel = createLabel(
                theme,
                Component.literal(String.join("\n", plan.metaLines())),
                theme.textDimColor(),
                Label.HorizontalAlign.LEFT
            );
            place(
                metaLabel,
                ROOT_PADDING,
                FOCUS_META_Y,
                width - ROOT_PADDING * 2,
                Math.max(
                    FOCUS_META_HEIGHT,
                    resolveMultilineHeight(String.join("\n", plan.metaLines()), theme.bodyLineHeight())
                )
            );
            detailCard.addChild(metaLabel);
            addBadges(detailCard, theme, plan.statusBadges(), ROOT_PADDING, FOCUS_BADGE_Y, width - ROOT_PADDING * 2);

            final TacticalBar durabilityBar = createBar(theme, plan.durabilityBar());
            place(durabilityBar, ROOT_PADDING, FOCUS_BAR1_Y, width - ROOT_PADDING * 2, theme.barBlockHeight());
            detailCard.addChild(durabilityBar);

            final TacticalBar experienceBar = createBar(theme, plan.experienceBar());
            place(experienceBar, ROOT_PADDING, FOCUS_BAR2_Y, width - ROOT_PADDING * 2, theme.barBlockHeight());
            detailCard.addChild(experienceBar);

            final TacticalBar overloadBar = createBar(theme, plan.overloadBar());
            place(overloadBar, ROOT_PADDING, FOCUS_BAR3_Y, width - ROOT_PADDING * 2, theme.barBlockHeight());
            detailCard.addChild(overloadBar);

            final int actionY = FOCUS_DETAIL_HEIGHT + FOCUS_ACTION_GAP;
            final TacticalPanel actionCard = new TacticalPanel(theme);
            actionCard.setSurface(TacticalSurface.RAISED);
            actionCard.setTone(resolveActionPanelTone(plan.actionPlans()));
            place(actionCard, 0, actionY, width, Math.max(0, height - actionY));
            column.addChild(actionCard);

            final Label actionTitle = createLabel(
                theme,
                Component.literal(COLUMN_ACTION_TITLE),
                theme.accentColor(resolveActionPanelTone(plan.actionPlans())),
                Label.HorizontalAlign.LEFT
            );
            place(
                actionTitle,
                ROOT_PADDING,
                ACTION_CARD_TITLE_Y,
                width - ROOT_PADDING * 2,
                theme.titleLineHeight()
            );
            actionCard.addChild(actionTitle);

            final ScrollContainer scroll = new ScrollContainer(theme.baseTheme());
            final int scrollWidth = width - ROOT_PADDING * 2;
            final int scrollHeight = Math.max(
                0,
                actionCard.getHeight() - ACTION_ROW_TOP - ROOT_PADDING
            );
            place(scroll, ROOT_PADDING, ACTION_ROW_TOP, scrollWidth, scrollHeight);
            actionCard.addChild(scroll);

            final UIElement content = new UIElement() { };
            final int contentWidth = Math.max(0, scrollWidth - SCROLLBAR_SPACE);
            int cursorY = 0;
            for (final ActionPlan actionPlan : plan.actionPlans()) {
                final int rowHeight = addActionRow(content, theme, actionPlan, contentWidth, cursorY);
                cursorY += rowHeight + ACTION_ROW_GAP;
            }
            content.setFrame(0, 0, contentWidth, Math.max(0, cursorY - ACTION_ROW_GAP));
            scroll.setContent(content);
            return column;
        }

        private static TacticalPanel createRouteColumn(
            final TacticalTheme theme,
            final List<RouteCardPlan> routeCards,
            final IntConsumer tabNavigator,
            final int cultivationTabIndex,
            final int helpTabIndex,
            final int width,
            final int height
        ) {
            final TacticalPanel column = new TacticalPanel(theme);
            column.setSurface(TacticalSurface.SECTION);
            column.setTone(TacticalTone.INFO);
            place(column, 0, 0, width, height);
            final int contentTop = addColumnHeader(
                column,
                theme,
                COLUMN_ROUTE_TITLE,
                ROUTE_NOTE,
                width,
                TacticalTone.INFO
            );

            final ScrollContainer scroll = new ScrollContainer(theme.baseTheme());
            final int scrollWidth = width - ROOT_PADDING * 2;
            final int scrollHeight = Math.max(0, height - contentTop - ROOT_PADDING);
            place(scroll, ROOT_PADDING, contentTop, scrollWidth, scrollHeight);
            column.addChild(scroll);

            final UIElement content = new UIElement() { };
            final int contentWidth = Math.max(0, scrollWidth - SCROLLBAR_SPACE);
            int cursorY = 0;
            for (final RouteCardPlan routeCardPlan : routeCards) {
                final TacticalRouteCard routeCard = new TacticalRouteCard(theme);
                routeCard.setTone(routeCardPlan.tone());
                routeCard.setTitle(Component.literal(routeCardPlan.title()));
                routeCard.setSummary(Component.literal(String.join("\n", routeCardPlan.summaryLines())));
                routeCard.setActionText(Component.literal(routeCardPlan.actionText()));
                routeCard.setBadges(toBadgeSpecs(routeCardPlan.badges()));
                if (routeCardPlan.target() == RouteTarget.CULTIVATION) {
                    routeCard.setOnClick(() -> tabNavigator.accept(cultivationTabIndex));
                } else if (routeCardPlan.target() == RouteTarget.HELP) {
                    routeCard.setOnClick(() -> tabNavigator.accept(helpTabIndex));
                }
                place(routeCard, 0, cursorY, contentWidth, ROUTE_CARD_HEIGHT);
                content.addChild(routeCard);
                cursorY += ROUTE_CARD_HEIGHT + ROUTE_CARD_GAP;
            }
            content.setFrame(0, 0, contentWidth, Math.max(0, cursorY - ROUTE_CARD_GAP));
            scroll.setContent(content);
            return column;
        }

        private static int addColumnHeader(
            final TacticalPanel parent,
            final TacticalTheme theme,
            final String title,
            final String note,
            final int width,
            final TacticalTone tone
        ) {
            final Label titleLabel = createLabel(
                theme,
                Component.literal(title),
                theme.accentColor(tone),
                Label.HorizontalAlign.LEFT
            );
            place(titleLabel, ROOT_PADDING, COLUMN_TITLE_Y, width - ROOT_PADDING * 2, theme.titleLineHeight());
            parent.addChild(titleLabel);

            final String formattedNote = formatDenseMultilineText(note);
            final int noteHeight = Math.max(
                COLUMN_NOTE_HEIGHT,
                resolveMultilineHeight(formattedNote, theme.bodyLineHeight())
            );
            final Label noteLabel = createLabel(
                theme,
                Component.literal(formattedNote),
                theme.textDimColor(),
                Label.HorizontalAlign.LEFT
            );
            place(noteLabel, ROOT_PADDING, COLUMN_NOTE_Y, width - ROOT_PADDING * 2, noteHeight);
            parent.addChild(noteLabel);
            return Math.max(COLUMN_CONTENT_TOP, COLUMN_NOTE_Y + noteHeight + theme.regularGap());
        }

        private static int addActionRow(
            final UIElement parent,
            final TacticalTheme theme,
            final ActionPlan actionPlan,
            final int width,
            final int y
        ) {
            final String titleText = actionPlan.title() + localizedText(
                actionPlan.available()
                    ? KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_STATUS_AVAILABLE
                    : KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_ACTION_STATUS_RESTRICTED
            );
            final String descriptionText = formatDenseMultilineText(actionPlan.description());
            final int titleHeight = resolveMultilineHeight(titleText, theme.bodyLineHeight());
            final int descriptionHeight = resolveMultilineHeight(
                descriptionText,
                theme.bodyLineHeight()
            );
            final int rowHeight = Math.max(
                ACTION_ROW_HEIGHT,
                theme.tightGap() + titleHeight + theme.tightGap() + descriptionHeight + theme.tightGap()
            );
            final TacticalPanel row = new TacticalPanel(theme);
            row.setSurface(TacticalSurface.INSET);
            row.setTone(actionPlan.tone());
            row.setHighlighted(actionPlan.available());
            place(row, ROOT_PADDING, y, width - ROOT_PADDING * 2, rowHeight);
            parent.addChild(row);

            final TacticalBadge keyBadge = new TacticalBadge(theme);
            keyBadge.setSpec(TacticalBadgeSpec.of(actionPlan.keyHint(), actionPlan.tone()));
            final int badgeWidth = Math.max(ACTION_BADGE_MIN_WIDTH, keyBadge.suggestWidth());
            place(keyBadge, ROOT_PADDING, theme.tightGap(), badgeWidth, theme.badgeHeight());
            row.addChild(keyBadge);

            final int contentX = ROOT_PADDING + badgeWidth + theme.regularGap();
            final int contentWidth = row.getWidth() - contentX - ROOT_PADDING;
            final Label titleLabel = createLabel(
                theme,
                Component.literal(titleText),
                theme.textPrimaryColor(),
                Label.HorizontalAlign.LEFT
            );
            place(titleLabel, contentX, theme.tightGap(), contentWidth, titleHeight);
            row.addChild(titleLabel);

            final Label descLabel = createLabel(
                theme,
                Component.literal(descriptionText),
                theme.textDimColor(),
                Label.HorizontalAlign.LEFT
            );
            place(
                descLabel,
                contentX,
                theme.tightGap() + titleHeight + theme.tightGap(),
                contentWidth,
                descriptionHeight
            );
            row.addChild(descLabel);
            return rowHeight;
        }

        private static void addOverviewSectionButtons(
            final TacticalPanel parent,
            final TacticalTheme theme,
            final int height,
            final List<Button> sectionButtons,
            final List<UIElement> sectionPanels
        ) {
            final String[] labels = {
                SUMMARY_SQUAD_TITLE,
                COLUMN_ROSTER_TITLE,
                COLUMN_FOCUS_TITLE,
                COLUMN_ROUTE_TITLE,
            };
            int cursorY = ROOT_PADDING;
            for (int index = 0; index < labels.length; index++) {
                final Button button = new Button(labels[index], theme.baseTheme());
                place(
                    button,
                    ROOT_PADDING,
                    cursorY,
                    Math.max(0, SECTION_RAIL_WIDTH - ROOT_PADDING * 2),
                    SECTION_RAIL_ITEM_HEIGHT
                );
                final int targetIndex = index;
                button.setOnClick(() -> switchOverviewSection(sectionButtons, sectionPanels, targetIndex));
                parent.addChild(button);
                sectionButtons.add(button);
                cursorY += SECTION_RAIL_ITEM_HEIGHT + SECTION_RAIL_GAP;
                if (cursorY >= height - ROOT_PADDING) {
                    break;
                }
            }
        }

        private static void registerOverviewSection(
            final List<UIElement> sectionPanels,
            final int sectionIndex,
            final UIElement panel
        ) {
            while (sectionPanels.size() <= sectionIndex) {
                sectionPanels.add(null);
            }
            sectionPanels.set(sectionIndex, panel);
        }

        private static void switchOverviewSection(
            final List<Button> sectionButtons,
            final List<UIElement> sectionPanels,
            final int sectionIndex
        ) {
            final int activeSection = Math.max(
                OVERVIEW_SECTION_SUMMARY,
                Math.min(sectionIndex, OVERVIEW_SECTION_ROUTE)
            );
            for (int index = 0; index < sectionPanels.size(); index++) {
                final UIElement panel = sectionPanels.get(index);
                if (panel != null) {
                    panel.setVisible(index == activeSection);
                }
            }
            for (int index = 0; index < sectionButtons.size(); index++) {
                sectionButtons.get(index).setEnabled(index != activeSection);
            }
        }

        private static TacticalSwordListItem.Model toSwordListModel(final RosterEntryPlan plan) {
            return new TacticalSwordListItem.Model(
                Component.literal(plan.title()),
                Component.literal(plan.detail()),
                Component.literal(plan.modeText()),
                Component.literal(plan.distanceText()),
                KongqiaoI18n.text(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_DURABILITY),
                Component.literal(plan.healthValue()),
                plan.healthRatio(),
                plan.healthTone(),
                KongqiaoI18n.text(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_LABEL_OVERLOAD),
                Component.literal(plan.overloadValue()),
                plan.overloadRatio(),
                plan.overloadTone(),
                plan.panelTone(),
                plan.highlighted(),
                toBadgeSpecs(plan.badges())
            );
        }

        private static TacticalBar createBar(final TacticalTheme theme, final FocusBarPlan plan) {
            final TacticalBar bar = new TacticalBar(theme);
            bar.setLabel(Component.literal(plan.label()));
            bar.setValueText(Component.literal(plan.valueText()));
            bar.setFillRatio(plan.fillRatio());
            bar.setTone(plan.tone());
            return bar;
        }

        private static void addBadges(
            final TacticalPanel parent,
            final TacticalTheme theme,
            final List<BadgePlan> badges,
            final int startX,
            final int y,
            final int availableWidth
        ) {
            int cursorX = startX;
            for (final BadgePlan badgePlan : badges) {
                final TacticalBadge badge = new TacticalBadge(theme);
                badge.setSpec(TacticalBadgeSpec.of(badgePlan.text(), badgePlan.tone()));
                final int remaining = startX + availableWidth - cursorX;
                if (remaining <= 0) {
                    break;
                }
                final int badgeWidth = Math.min(badge.suggestWidth(), remaining);
                place(badge, cursorX, y, badgeWidth, theme.badgeHeight());
                parent.addChild(badge);
                cursorX += badgeWidth + theme.tightGap();
            }
        }

        private static List<TacticalBadgeSpec> toBadgeSpecs(final List<BadgePlan> badges) {
            final List<TacticalBadgeSpec> specs = new ArrayList<>();
            for (final BadgePlan badge : badges) {
                specs.add(TacticalBadgeSpec.of(badge.text(), badge.tone()));
            }
            return List.copyOf(specs);
        }

        private static Label createLabel(
            final TacticalTheme theme,
            final Component text,
            final int color,
            final Label.HorizontalAlign align
        ) {
            final Label label = new Label(text, theme.baseTheme());
            label.setColor(color);
            label.setHorizontalAlign(align);
            return label;
        }

        private static TacticalTone resolveViewTone(final FlyingSwordViewModel view) {
            if (view.overloadDanger() || view.overloadBacklashActive()) {
                return TacticalTone.DANGER;
            }
            if (view.highlightWarning() || view.overloadRecoveryActive()) {
                return TacticalTone.WARNING;
            }
            if (view.benmingSword()) {
                return TacticalTone.BENMING;
            }
            if (view.selected()) {
                return TacticalTone.INFO;
            }
            return TacticalTone.NEUTRAL;
        }

        private static TacticalTone resolveBenmingTone(final BenmingSummary benmingSummary) {
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

        private static TacticalTone resolveDurabilityTone(final float healthRatio) {
            final float safeRatio = normalizeRatio(healthRatio);
            if (safeRatio <= DANGER_HEALTH_RATIO) {
                return TacticalTone.DANGER;
            }
            if (safeRatio <= WARNING_HEALTH_RATIO) {
                return TacticalTone.WARNING;
            }
            return TacticalTone.INFO;
        }

        private static TacticalTone resolveRouteTone(final HelpSignals helpSignals) {
            if (helpSignals.hasOverloadDanger() || helpSignals.hasOverloadBacklash()) {
                return TacticalTone.DANGER;
            }
            if (
                !helpSignals.hasBenmingSword()
                    || helpSignals.hasOverloadRecovery()
                    || helpSignals.hasAftershock()
            ) {
                return TacticalTone.WARNING;
            }
            return TacticalTone.INFO;
        }

        private static TacticalTone resolveActionPanelTone(final List<ActionPlan> actionPlans) {
            for (final ActionPlan actionPlan : actionPlans) {
                if (!actionPlan.available() && actionPlan.tone() == TacticalTone.DANGER) {
                    return TacticalTone.DANGER;
                }
            }
            for (final ActionPlan actionPlan : actionPlans) {
                if (!actionPlan.available() && actionPlan.tone() == TacticalTone.WARNING) {
                    return TacticalTone.WARNING;
                }
            }
            return TacticalTone.INFO;
        }

        private static String resolveRouteDescription(final HelpSignals helpSignals) {
            return resolveRouteTone(helpSignals) == TacticalTone.INFO
                ? ACTION_ROUTE_DEFAULT
                : ACTION_ROUTE_HELP;
        }

        private static String describeFocusSource(final FocusSource source) {
            return switch (source) {
                case BENMING -> localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_BENMING
                );
                case SELECTED -> localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_SELECTED
                );
                case RECENT -> localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_RECENT
                );
                case NONE -> localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_NONE
                );
            };
        }

        private static String describeFocusSourceBadge(final FocusSource source) {
            return switch (source) {
                case BENMING -> localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_FOCUS_BENMING
                );
                case SELECTED -> localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_FOCUS_SELECTED
                );
                case RECENT -> BADGE_RECENT;
                case NONE -> FOCUS_NONE_BADGE;
            };
        }

        private static String resolveBenmingPhaseText(final BenmingSummary benmingSummary) {
            if (benmingSummary.overloadBacklashActive()) {
                return BENMING_BACKLASH;
            }
            if (benmingSummary.overloadRecoveryActive()) {
                return BENMING_RECOVERY;
            }
            if (benmingSummary.aftershockPeriod()) {
                return BENMING_AFTERSHOCK;
            }
            if (benmingSummary.overloadDanger()) {
                return BENMING_DANGER;
            }
            if (benmingSummary.highlightWarning()) {
                return BENMING_WARNING;
            }
            if (benmingSummary.burstReady()) {
                return BENMING_BURST;
            }
            return BENMING_STABLE;
        }

        private static String resolveResonanceText(
            @Nullable final FlyingSwordResonanceType resonanceType
        ) {
            final String label = resolveResonanceLabel(resonanceType);
            return label.isBlank() ? EMPTY_RESONANCE_TEXT : label;
        }

        private static String resolveResonanceLabel(
            @Nullable final FlyingSwordResonanceType resonanceType
        ) {
            if (resonanceType == null) {
                return "";
            }
            return switch (resonanceType) {
                case OFFENSE -> localizedText(KongqiaoI18n.BENMING_HUD_RESONANCE_OFFENSE_SHORT);
                case DEFENSE -> localizedText(KongqiaoI18n.BENMING_HUD_RESONANCE_DEFENSE_SHORT);
                case SPIRIT -> localizedText(KongqiaoI18n.BENMING_HUD_RESONANCE_SPIRIT_SHORT);
                case DEVOUR -> localizedText(KongqiaoI18n.BENMING_HUD_RESONANCE_DEVOUR_SHORT);
            };
        }

        private static String resolveMarkBadgeText() {
            return localizedText(KongqiaoI18n.BENMING_HUD_BADGE_MARK);
        }

        private static String resolvePreWarningBadgeText() {
            return localizedText(KongqiaoI18n.BENMING_HUD_BADGE_OVERLOAD_PRE_WARNING);
        }

        private static String resolveBurstBadgeText(
            @Nullable final FlyingSwordResonanceType resonanceType
        ) {
            if (resonanceType == null) {
                return localizedText(KongqiaoI18n.BENMING_HUD_BADGE_BURST_READY);
            }
            final String badgeKey = switch (resonanceType) {
                case OFFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_OFFENSE_BURST_READY;
                case DEFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_DEFENSE_BURST_READY;
                case SPIRIT -> KongqiaoI18n.BENMING_HUD_BADGE_SPIRIT_BURST_READY;
                case DEVOUR -> KongqiaoI18n.BENMING_HUD_BADGE_DEVOUR_BURST_READY;
            };
            return localizedText(badgeKey);
        }

        private static String resolveDangerBadgeText(
            @Nullable final FlyingSwordResonanceType resonanceType
        ) {
            if (resonanceType == null) {
                return localizedText(KongqiaoI18n.BENMING_HUD_BADGE_OVERLOAD_WARNING);
            }
            final String badgeKey = switch (resonanceType) {
                case OFFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_OFFENSE_DANGER;
                case DEFENSE -> KongqiaoI18n.BENMING_HUD_BADGE_DEFENSE_DANGER;
                case SPIRIT -> KongqiaoI18n.BENMING_HUD_BADGE_SPIRIT_DANGER;
                case DEVOUR -> KongqiaoI18n.BENMING_HUD_BADGE_DEVOUR_DANGER;
            };
            return localizedText(badgeKey);
        }

        private static String formatOverloadText(final float overloadPercent) {
            return localizedText(
                KongqiaoI18n.BENMING_HUD_OVERLOAD_TEXT,
                Math.round(Math.max(0.0F, overloadPercent))
            );
        }

        private static String formatHealthText(final FlyingSwordViewModel view) {
            return Math.round(Math.max(0.0F, view.health()))
                + "/"
                + Math.round(Math.max(0.0F, view.maxHealth()));
        }

        private static String formatDistance(final float distance) {
            return String.format(Locale.ROOT, "%.1f%s", Math.max(0.0F, distance), DISTANCE_SUFFIX);
        }

        private static String formatPercent(final float ratio) {
            return Math.round(normalizeRatio(ratio) * FULL_PERCENT) + "%";
        }

        private static float normalizeRatio(final float ratio) {
            return Math.min(1.0F, Math.max(0.0F, ratio));
        }

        private static float normalizeOverloadRatio(final float overloadPercent) {
            return normalizeRatio(overloadPercent / FULL_PERCENT);
        }

        private static String localizedText(final String key, final Object... args) {
            return FlyingSwordHubScreen.localizedText(key, args);
        }

        private record OverviewPlan(
            List<SummaryCardPlan> summaryCards,
            List<RosterEntryPlan> rosterEntries,
            int hiddenRosterCount,
            String rosterCaption,
            FocusDetailPlan focusDetail,
            List<RouteCardPlan> routeCards
        ) {

            private OverviewPlan {
                summaryCards = List.copyOf(Objects.requireNonNullElse(summaryCards, List.of()));
                rosterEntries = List.copyOf(Objects.requireNonNullElse(rosterEntries, List.of()));
                rosterCaption = Objects.requireNonNullElse(rosterCaption, "");
                focusDetail = Objects.requireNonNull(focusDetail, "focusDetail");
                routeCards = List.copyOf(Objects.requireNonNullElse(routeCards, List.of()));
            }
        }

        private record SummaryCardPlan(
            String title,
            TacticalTone tone,
            List<String> lines,
            List<BadgePlan> badges
        ) {

            private SummaryCardPlan {
                title = Objects.requireNonNullElse(title, "");
                tone = Objects.requireNonNullElse(tone, TacticalTone.NEUTRAL);
                lines = List.copyOf(Objects.requireNonNullElse(lines, List.of()));
                badges = List.copyOf(Objects.requireNonNullElse(badges, List.of()));
            }
        }

        private record BadgePlan(String text, TacticalTone tone) {

            private BadgePlan {
                text = Objects.requireNonNullElse(text, "");
                tone = Objects.requireNonNullElse(tone, TacticalTone.NEUTRAL);
            }
        }

        private record RosterEntryPlan(
            UUID swordUuid,
            String title,
            String detail,
            String modeText,
            String distanceText,
            String healthValue,
            float healthRatio,
            TacticalTone healthTone,
            String overloadValue,
            float overloadRatio,
            TacticalTone overloadTone,
            TacticalTone panelTone,
            boolean highlighted,
            List<BadgePlan> badges
        ) {

            private RosterEntryPlan {
                title = Objects.requireNonNullElse(title, "");
                detail = Objects.requireNonNullElse(detail, "");
                modeText = Objects.requireNonNullElse(modeText, "");
                distanceText = Objects.requireNonNullElse(distanceText, "");
                healthValue = Objects.requireNonNullElse(healthValue, "");
                overloadValue = Objects.requireNonNullElse(overloadValue, "");
                healthRatio = normalizeRatio(healthRatio);
                overloadRatio = normalizeRatio(overloadRatio);
                healthTone = Objects.requireNonNullElse(healthTone, TacticalTone.INFO);
                overloadTone = Objects.requireNonNullElse(overloadTone, TacticalTone.INFO);
                panelTone = Objects.requireNonNullElse(panelTone, TacticalTone.NEUTRAL);
                badges = List.copyOf(Objects.requireNonNullElse(badges, List.of()));
            }
        }

        private record FocusBarPlan(String label, String valueText, float fillRatio, TacticalTone tone) {

            private FocusBarPlan {
                label = Objects.requireNonNullElse(label, "");
                valueText = Objects.requireNonNullElse(valueText, "");
                fillRatio = normalizeRatio(fillRatio);
                tone = Objects.requireNonNullElse(tone, TacticalTone.INFO);
            }
        }

        private record FocusDetailPlan(
            String title,
            TacticalTone tone,
            List<String> metaLines,
            List<BadgePlan> statusBadges,
            FocusBarPlan durabilityBar,
            FocusBarPlan experienceBar,
            FocusBarPlan overloadBar,
            List<ActionPlan> actionPlans
        ) {

            private FocusDetailPlan {
                title = Objects.requireNonNullElse(title, "");
                tone = Objects.requireNonNullElse(tone, TacticalTone.NEUTRAL);
                metaLines = List.copyOf(Objects.requireNonNullElse(metaLines, List.of()));
                statusBadges = List.copyOf(Objects.requireNonNullElse(statusBadges, List.of()));
                durabilityBar = Objects.requireNonNull(durabilityBar, "durabilityBar");
                experienceBar = Objects.requireNonNull(experienceBar, "experienceBar");
                overloadBar = Objects.requireNonNull(overloadBar, "overloadBar");
                actionPlans = List.copyOf(Objects.requireNonNullElse(actionPlans, List.of()));
            }
        }

        private record ActionPlan(
            String keyHint,
            String title,
            boolean available,
            TacticalTone tone,
            String description
        ) {

            private ActionPlan {
                keyHint = Objects.requireNonNullElse(keyHint, "");
                title = Objects.requireNonNullElse(title, "");
                tone = Objects.requireNonNullElse(tone, TacticalTone.NEUTRAL);
                description = Objects.requireNonNullElse(description, "");
            }
        }

        private record RouteCardPlan(
            String title,
            TacticalTone tone,
            List<String> summaryLines,
            String actionText,
            RouteTarget target,
            List<BadgePlan> badges
        ) {

            private RouteCardPlan {
                title = Objects.requireNonNullElse(title, "");
                tone = Objects.requireNonNullElse(tone, TacticalTone.NEUTRAL);
                summaryLines = List.copyOf(Objects.requireNonNullElse(summaryLines, List.of()));
                actionText = Objects.requireNonNullElse(actionText, "");
                target = Objects.requireNonNullElse(target, RouteTarget.NONE);
                badges = List.copyOf(Objects.requireNonNullElse(badges, List.of()));
            }
        }

        private enum RouteTarget {
            NONE,
            CULTIVATION,
            HELP
        }
    }

    private void addSectionTitle(
        final TacticalPanel parent,
        final String title,
        final TacticalTone tone,
        final int width
    ) {
        final Label titleLabel = createLabel(
            Component.literal(title),
            tacticalTheme.accentColor(tone),
            Label.HorizontalAlign.LEFT
        );
        place(
            titleLabel,
            SECTION_PADDING,
            SUMMARY_TITLE_Y,
            width - SECTION_PADDING * 2,
            tacticalTheme.titleLineHeight()
        );
        parent.addChild(titleLabel);
    }

    private void addBodyLabel(
        final TacticalPanel parent,
        final int y,
        final int width,
        final String text
    ) {
        final String safeText = formatDenseMultilineText(text);
        final Label bodyLabel = createLabel(
            Component.literal(safeText),
            tacticalTheme.textDimColor(),
            Label.HorizontalAlign.LEFT
        );
        place(
            bodyLabel,
            SECTION_PADDING,
            y,
            width - SECTION_PADDING * 2,
            resolveMultilineHeight(safeText, tacticalTheme.bodyLineHeight())
        );
        parent.addChild(bodyLabel);
    }

    private void addOverviewBadges(
        final TacticalPanel parent,
        final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot,
        final int width
    ) {
        final List<TacticalBadgeSpec> badgeSpecs = buildOverviewBadgeSpecs(snapshot);
        final int availableWidth = width - SECTION_PADDING * 2;
        int currentX = SECTION_PADDING;
        for (final TacticalBadgeSpec spec : badgeSpecs) {
            final TacticalBadge badge = new TacticalBadge(tacticalTheme);
            badge.setSpec(spec);
            final int badgeWidth = Math.min(badge.suggestWidth(), availableWidth);
            place(badge, currentX, BADGE_Y, badgeWidth, BADGE_HEIGHT);
            parent.addChild(badge);
            currentX += badgeWidth + BADGE_GAP;
        }
    }

    private List<TacticalBadgeSpec> buildOverviewBadgeSpecs(
        final FlyingSwordTacticalStateService.TacticalStateSnapshot snapshot
    ) {
        final String benmingOnlineText = localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BENMING_ONLINE);
        final String badgeNoPriority = localizedText(KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_NO_PRIORITY);
        final List<TacticalBadgeSpec> badgeSpecs = new ArrayList<>();
        final FlyingSwordTacticalStateService.SquadSummary squadSummary =
            snapshot.squadSummary();
        badgeSpecs.add(
            TacticalBadgeSpec.of(
                localizedText(
                    KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_SWORD_COUNT,
                    squadSummary.totalCount()
                ),
                TacticalTone.INFO
            )
        );
        if (squadSummary.benmingCount() > 0) {
            badgeSpecs.add(
                TacticalBadgeSpec.of(benmingOnlineText, TacticalTone.BENMING)
            );
        }
        if (squadSummary.warningCount() > 0) {
            badgeSpecs.add(
                TacticalBadgeSpec.of(
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_WARNING_COUNT,
                        squadSummary.warningCount()
                    ),
                    TacticalTone.WARNING
                )
            );
        }
        if (squadSummary.dangerCount() > 0) {
            badgeSpecs.add(
                TacticalBadgeSpec.of(
                    localizedText(
                        KongqiaoI18n.FLYING_SWORD_HUB_OVERVIEW_BADGE_DANGER_COUNT,
                        squadSummary.dangerCount()
                    ),
                    TacticalTone.DANGER
                )
            );
        }
        if (badgeSpecs.size() == CLOSE_KEY_COUNT) {
            badgeSpecs.add(TacticalBadgeSpec.of(badgeNoPriority, TacticalTone.NEUTRAL));
        }
        return badgeSpecs;
    }

    private void switchTab(final int tabIndex) {
        activeTab = sanitizeTabIndex(tabIndex);
        for (int index = 0; index < tabPanels.size(); index++) {
            tabPanels.get(index).setVisible(index == activeTab);
        }
        for (int index = 0; index < tabButtons.size(); index++) {
            tabButtons.get(index).setEnabled(index != activeTab);
        }
    }

    private void switchCultivationRoute(final int routeIndex) {
        activeCultivationRoute = sanitizeCultivationRouteIndex(routeIndex);
        for (int index = 0; index < cultivationRoutePanels.size(); index++) {
            cultivationRoutePanels.get(index).setVisible(index == activeCultivationRoute);
        }
        for (int index = 0; index < cultivationRouteButtons.size(); index++) {
            cultivationRouteButtons.get(index).setEnabled(index != activeCultivationRoute);
        }
    }

    private int sanitizeTabIndex(final int tabIndex) {
        if (tabIndex < TAB_OVERVIEW || tabIndex > TAB_HELP) {
            return TAB_OVERVIEW;
        }
        return tabIndex;
    }

    private int sanitizeCultivationRouteIndex(final int routeIndex) {
        if (routeIndex < 0 || routeIndex >= CULTIVATION_SUBTABS.size()) {
            return 0;
        }
        return routeIndex;
    }

    private void executeCultivationRouteAction(
        final FlyingSwordHubCultivationModel.RouteActionType actionType
    ) {
        if (minecraft == null) {
            return;
        }
        switch (actionType) {
            case OPEN_FORGE -> PacketDistributor.sendToServer(
                new ServerboundKongqiaoActionPayload(
                    ServerboundKongqiaoActionPayload.Action.OPEN_FORGE
                )
            );
            case OPEN_TRAINING -> PacketDistributor.sendToServer(
                new ServerboundOpenTrainingGuiPayload()
            );
            case OPEN_CLUSTER -> PacketDistributor.sendToServer(
                new ServerboundOpenClusterGuiPayload()
            );
            case OPEN_GROWTH_HELP -> minecraft.setScreen(
                createHubLinkedHelpScreen(FlyingSwordHelpScreen.growthTabIndex())
            );
        }
    }

    private HelpRouteSpec resolveHelpRouteSpec(final int routeIndex) {
        if (routeIndex < 0 || routeIndex >= HELP_ROUTE_SPECS.size()) {
            return HELP_ROUTE_SPECS.get(0);
        }
        return HELP_ROUTE_SPECS.get(routeIndex);
    }

    private static String localizedText(final String key, final Object... args) {
        return KongqiaoI18n.localizedText(key, args);
    }

    private static int resolveMultilineHeight(final String text, final int lineHeight) {
        return Math.max(lineHeight, resolveMultilineLineCount(text) * lineHeight);
    }

    private static int resolveMultilineLineCount(final String text) {
        final String safeText = Objects.requireNonNullElse(text, "");
        if (safeText.isEmpty()) {
            return 1;
        }
        return safeText.split("\\n", -1).length;
    }

    private static String formatDenseMultilineText(final String text) {
        String formatted = Objects.requireNonNullElse(text, "");
        if (formatted.isBlank() || formatted.contains("\n")) {
            return formatted;
        }
        formatted = insertPreferredLineBreak(formatted);
        if (!formatted.contains("\n") || formatted.length() <= MULTILINE_SECOND_BREAK_THRESHOLD) {
            return formatted;
        }
        return insertPreferredLineBreak(formatted);
    }

    private static String insertPreferredLineBreak(final String text) {
        final String[] delimiters = {
            "； ",
            "；",
            "; ",
            "。 ",
            "。",
            ": ",
            "：",
            "， ",
            "，",
            ", ",
            " / ",
            " · ",
        };
        final String safeText = Objects.requireNonNullElse(text, "");
        final int midpoint = safeText.length() / 2;
        int bestIndex = -1;
        int bestDistance = Integer.MAX_VALUE;
        for (final String delimiter : delimiters) {
            int searchIndex = 0;
            while (searchIndex >= 0 && searchIndex < safeText.length()) {
                final int candidate = safeText.indexOf(delimiter, searchIndex);
                if (candidate < 0) {
                    break;
                }
                final int insertAt = candidate + delimiter.length();
                if (insertAt < safeText.length()) {
                    final int distance = Math.abs(midpoint - insertAt);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestIndex = insertAt;
                    }
                }
                searchIndex = candidate + delimiter.length();
            }
        }
        if (bestIndex <= 0 || bestIndex >= safeText.length()) {
            return safeText;
        }
        if (safeText.charAt(bestIndex - 1) == '\n' || safeText.charAt(bestIndex) == '\n') {
            return safeText;
        }
        return safeText.substring(0, bestIndex) + "\n" + safeText.substring(bestIndex);
    }

    private record HelpRouteSpec(
        String title,
        String summary,
        String actionText,
        TacticalTone tone,
        int helpTabIndex
    ) {
    }

    private String describeFocusSource(
        final FlyingSwordTacticalStateService.FocusSource source
    ) {
        return switch (source) {
            case BENMING -> localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_BENMING);
            case SELECTED -> localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_SELECTED);
            case RECENT -> localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_RECENT);
            case NONE -> localizedText(KongqiaoI18n.FLYING_SWORD_HUB_CULTIVATION_FOCUS_SOURCE_NONE);
        };
    }

    private Label createLabel(
        final Component text,
        final int color,
        final Label.HorizontalAlign align
    ) {
        final Label label = new Label(text, tacticalTheme.baseTheme());
        label.setColor(color);
        label.setHorizontalAlign(align);
        return label;
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
