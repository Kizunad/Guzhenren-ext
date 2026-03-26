package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.block.AlchemyFurnaceBlock;
import com.Kizunad.guzhenrenext.xianqiao.block.ApertureHubMenu;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.HubPanel;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.HubUiTokens;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.ModuleCard;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuItem;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlock;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ApertureHubScreen extends TinyUIContainerScreen<ApertureHubMenu> {

    private static final int WINDOW_WIDTH = 332;

    private static final int WINDOW_HEIGHT = 232;

    private static final int WINDOW_PADDING = 8;

    private static final int LAYER_GAP = HubUiTokens.PANEL_GAP;

    private static final int LAYER_HEADER_BAND = 0;

    private static final int LAYER_TOP_SITUATION_ROW = 1;

    private static final int LAYER_MAIN_MODULE_GRID = 2;

    private static final int LAYER_BOTTOM_UTILITY_ROW = 3;

    private static final int HEADER_BAND_HEIGHT = 30;

    private static final int TOP_SITUATION_ROW_HEIGHT = 48;

    private static final int MAIN_MODULE_GRID_HEIGHT = 124;

    private static final int BOTTOM_UTILITY_ROW_HEIGHT = 46;

    private static final int MODULE_CARD_COLUMNS = 2;

    private static final int MODULE_CARD_ROWS = 4;

    private static final int MODULE_CARD_GAP = HubUiTokens.PANEL_GAP;

    private static final int MODULE_CARD_HEIGHT = 54;

    private static final int ROW_BLOCK_GAP = HubUiTokens.PANEL_GAP;

    private static final int ROUTE_ENTITY_SEARCH_RADIUS = 16;

    private static final int ROUTE_BLOCK_SEARCH_RADIUS = 8;

    private static final int HOTBAR_SLOT_COUNT = 9;

    private static final int TAB_ASCENSION = 5;

    private static final int ASCENSION_BUTTON_WIDTH = 84;

    private static final int ASCENSION_BUTTON_HEIGHT = 18;

    private static final int ASCENSION_PANEL_TOP = 22;

    private static final double PERCENT_DENOMINATOR = 100.0D;

    private static final double PERMILLE_DENOMINATOR = 10.0D;

    private static final long TICKS_PER_SECOND = 20L;

    private static final long SECONDS_PER_HOUR = 3600L;

    private static final long HOURS_PER_DAY = 24L;

    private static final Theme DEFAULT_THEME = HubUiTokens.hallTheme();

    private final Theme theme;

    private final HubStatusEvaluator statusEvaluator;

    private final Map<String, ModuleCard> moduleCardsById;

    private Label headerTitleLabel;

    private Label headerDetailLabel;

    private RowBlock overallSummaryBlock;

    private RowBlock riskSummaryBlock;

    private RowBlock nextRouteBlock;

    private RowBlock summaryFootnoteBlock;

    private RowBlock fallbackExplainerBlock;

    private Label ascensionPlaceholderLabel;

    private com.Kizunad.tinyUI.controls.Button ascensionEntryButton;

    public ApertureHubScreen(
        final ApertureHubMenu menu,
        final Inventory playerInventory,
        final Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = DEFAULT_THEME;
        this.statusEvaluator = new HubStatusEvaluator();
        this.moduleCardsById = new LinkedHashMap<>();
    }

    @Override
    protected void initUI(final UIRoot root) {
        root.setViewport(this.width, this.height);
        resetUiReferences();

        final HubSnapshot snapshot = buildSnapshot();
        final HubStatusEvaluator.HubStatus status = statusEvaluator.evaluate(snapshot);
        final HubV2Layout layout = resolveLayout();
        final int shellX = (this.width - WINDOW_WIDTH) / 2;
        final int shellY = (this.height - WINDOW_HEIGHT) / 2;

        final HubPanel shell = new HubPanel(HubUiTokens.HubTone.STONE);
        shell.setFrame(shellX, shellY, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(shell);

        final UIElement headerBand = buildHeaderBand(layout);
        headerBand.setFrame(
            WINDOW_PADDING,
            WINDOW_PADDING,
            layout.contentWidth(),
            layout.headerBandHeight()
        );
        shell.addChild(headerBand);

        final UIElement topSituationRow = buildTopSituationRow(layout);
        topSituationRow.setFrame(
            WINDOW_PADDING,
            WINDOW_PADDING + layout.headerBandHeight() + LAYER_GAP,
            layout.contentWidth(),
            layout.topSituationRowHeight()
        );
        shell.addChild(topSituationRow);

        final int bodyViewportY = WINDOW_PADDING
            + layout.headerBandHeight()
            + LAYER_GAP
            + layout.topSituationRowHeight()
            + LAYER_GAP;

        if (layout.useBodyScrollFallback()) {
            final ScrollContainer bodyScroll = new ScrollContainer(theme);
            bodyScroll.setFrame(
                WINDOW_PADDING,
                bodyViewportY,
                layout.contentWidth(),
                layout.bodyViewportHeight()
            );
            shell.addChild(bodyScroll);

            final UIElement bodyContent = new UIElement() { };
            bodyContent.setFrame(0, 0, layout.contentWidth(), layout.bodyContentHeight());
            bodyScroll.setContent(bodyContent);

            final UIElement mainModuleGrid = buildMainModuleGrid(layout, snapshot, status);
            mainModuleGrid.setFrame(0, 0, layout.contentWidth(), layout.mainModuleGridHeight());
            bodyContent.addChild(mainModuleGrid);

            final UIElement bottomUtilityRow = buildBottomUtilityRow(layout, status);
            bottomUtilityRow.setFrame(
                0,
                layout.mainModuleGridHeight() + LAYER_GAP,
                layout.contentWidth(),
                layout.bottomUtilityRowHeight()
            );
            bodyContent.addChild(bottomUtilityRow);
        } else {
            final UIElement mainModuleGrid = buildMainModuleGrid(layout, snapshot, status);
            mainModuleGrid.setFrame(
                WINDOW_PADDING,
                bodyViewportY,
                layout.contentWidth(),
                layout.mainModuleGridHeight()
            );
            shell.addChild(mainModuleGrid);

            final UIElement bottomUtilityRow = buildBottomUtilityRow(layout, status);
            bottomUtilityRow.setFrame(
                WINDOW_PADDING,
                bodyViewportY + layout.mainModuleGridHeight() + LAYER_GAP,
                layout.contentWidth(),
                layout.bottomUtilityRowHeight()
            );
            shell.addChild(bottomUtilityRow);
        }

        refreshScreenData(snapshot, status);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (headerDetailLabel == null || overallSummaryBlock == null || moduleCardsById.isEmpty()) {
            return;
        }
        final HubSnapshot snapshot = buildSnapshot();
        final HubStatusEvaluator.HubStatus status = statusEvaluator.evaluate(snapshot);
        refreshScreenData(snapshot, status);
    }

    @Override
    protected double getUiScale() {
        return 1.0D;
    }

    private void resetUiReferences() {
        headerTitleLabel = null;
        headerDetailLabel = null;
        overallSummaryBlock = null;
        riskSummaryBlock = null;
        nextRouteBlock = null;
        summaryFootnoteBlock = null;
        fallbackExplainerBlock = null;
        moduleCardsById.clear();
    }

    private HubV2Layout resolveLayout() {
        final int contentWidth = WINDOW_WIDTH - WINDOW_PADDING * 2;
        final int bodyViewportHeight = WINDOW_HEIGHT
            - WINDOW_PADDING * 2
            - HEADER_BAND_HEIGHT
            - LAYER_GAP
            - TOP_SITUATION_ROW_HEIGHT
            - LAYER_GAP;
        final int bodyContentHeight = MAIN_MODULE_GRID_HEIGHT + LAYER_GAP + BOTTOM_UTILITY_ROW_HEIGHT;
        final boolean useBodyScrollFallback = bodyContentHeight > bodyViewportHeight;
        return new HubV2Layout(
            contentWidth,
            HEADER_BAND_HEIGHT,
            TOP_SITUATION_ROW_HEIGHT,
            MAIN_MODULE_GRID_HEIGHT,
            BOTTOM_UTILITY_ROW_HEIGHT,
            bodyViewportHeight,
            bodyContentHeight,
            useBodyScrollFallback
        );
    }

    private UIElement buildHeaderBand(final HubV2Layout layout) {
        final HubPanel headerBand = new HubPanel(HubUiTokens.HubTone.GOLD);
        headerTitleLabel = createLabel("洞天主殿", Label.HorizontalAlign.LEFT);
        headerDetailLabel = createLabel("待同步", Label.HorizontalAlign.RIGHT);
        headerBand.addChild(headerTitleLabel);
        headerBand.addChild(headerDetailLabel);
        layoutHeaderBand(headerBand, layout);
        return headerBand;
    }

    private void layoutHeaderBand(final HubPanel headerBand, final HubV2Layout layout) {
        final int contentWidth = layout.contentWidth() - HubUiTokens.PANEL_PADDING * 2;
        headerTitleLabel.setFrame(
            HubUiTokens.PANEL_PADDING,
            HubUiTokens.PANEL_PADDING,
            contentWidth / 2,
            HubUiTokens.MEDIUM_CONTROL_HEIGHT
        );
        headerDetailLabel.setFrame(
            HubUiTokens.PANEL_PADDING + contentWidth / 2,
            HubUiTokens.PANEL_PADDING,
            contentWidth / 2,
            HubUiTokens.MEDIUM_CONTROL_HEIGHT
        );
    }

    private UIElement buildTopSituationRow(final HubV2Layout layout) {
        final UIElement topSituationRow = new UIElement() { };
        overallSummaryBlock = createRowBlock(topSituationRow, 0, "整体态势");
        riskSummaryBlock = createRowBlock(topSituationRow, 1, "灾劫摘要");
        nextRouteBlock = createRowBlock(topSituationRow, 2, "下一步");
        layoutRowBlocks(topSituationRow, layout.topSituationRowHeight());
        return topSituationRow;
    }

    private UIElement buildMainModuleGrid(
        final HubV2Layout layout,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        final UIElement mainModuleGrid = new UIElement() { };
        final int cardWidth =
            (layout.contentWidth() - MODULE_CARD_GAP * (MODULE_CARD_COLUMNS - 1)) / MODULE_CARD_COLUMNS;

        int rowIndex = 0;
        int columnIndex = 0;
        for (HubRoutePolicy.CardRoutePolicy policy : HubRoutePolicy.orderedPolicies()) {
            final ModuleCard card = new ModuleCard(policy.cardId(), policy.cardTitle());
            card.setFrame(
                columnIndex * (cardWidth + MODULE_CARD_GAP),
                rowIndex * (MODULE_CARD_HEIGHT + MODULE_CARD_GAP),
                cardWidth,
                MODULE_CARD_HEIGHT
            );
            bindRouteCallbacks(card, policy);
            applyModuleCardState(card, policy, snapshot, status);
            mainModuleGrid.addChild(card);
            moduleCardsById.put(policy.cardId(), card);

            columnIndex++;
            if (columnIndex >= MODULE_CARD_COLUMNS) {
                columnIndex = 0;
                rowIndex++;
            }
        }
        mainModuleGrid.setFrame(0, 0, layout.contentWidth(), layout.mainModuleGridHeight());
        return mainModuleGrid;
    }

    private UIElement buildBottomUtilityRow(
        final HubV2Layout layout,
        final HubStatusEvaluator.HubStatus status
    ) {
        final UIElement bottomUtilityRow = new UIElement() { };
        final RowBlock quickRouteBlock = createRowBlock(bottomUtilityRow, 0, "快捷路由");
        summaryFootnoteBlock = createRowBlock(bottomUtilityRow, 1, "近况摘要");
        fallbackExplainerBlock = createRowBlock(bottomUtilityRow, 2, "中枢说明");

        quickRouteBlock.setBodyText(buildQuickRouteSummary());
        summaryFootnoteBlock.setBodyText(buildRecentStatusSummary(status));
        fallbackExplainerBlock.setBodyText(buildHubGuideText());
        createAscensionPanel(bottomUtilityRow);
        layoutRowBlocks(bottomUtilityRow, layout.bottomUtilityRowHeight());
        return bottomUtilityRow;
    }

    private UIElement createAscensionPanel(final UIElement parent) {
        final UIElement ascensionPanel = new UIElement() { };
        ascensionPanel.setFrame(
            TAB_ASCENSION,
            TAB_ASCENSION,
            ASCENSION_BUTTON_WIDTH,
            ASCENSION_BUTTON_HEIGHT * 2
        );
        ascensionPlaceholderLabel = createLabel("升仙入口待同步", Label.HorizontalAlign.CENTER);
        ascensionPlaceholderLabel.setFrame(0, 0, ASCENSION_BUTTON_WIDTH, HubUiTokens.SMALL_CONTROL_HEIGHT);
        ascensionEntryButton = new com.Kizunad.tinyUI.controls.Button(
            Component.literal("发起升仙冲关"),
            theme
        );
        ascensionEntryButton.setFrame(
            0,
            ASCENSION_PANEL_TOP,
            ASCENSION_BUTTON_WIDTH,
            ASCENSION_BUTTON_HEIGHT
        );
        ascensionEntryButton.setOnClick(() -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    ApertureHubMenu.BUTTON_ASCENSION_ENTRY
                );
            }
        });
        ascensionPanel.addChild(ascensionPlaceholderLabel);
        ascensionPanel.addChild(ascensionEntryButton);
        parent.addChild(ascensionPanel);
        return ascensionPanel;
    }

    private void layoutRowBlocks(final UIElement row, final int rowHeight) {
        final int blockWidth = (WINDOW_WIDTH - WINDOW_PADDING * 2 - ROW_BLOCK_GAP * 2) / 3;
        int index = 0;
        for (UIElement child : row.getChildren()) {
            child.setFrame(index * (blockWidth + ROW_BLOCK_GAP), 0, blockWidth, rowHeight);
            index++;
        }
    }

    private RowBlock createRowBlock(final UIElement parent, final int columnIndex, final String title) {
        final RowBlock block = new RowBlock(theme, title);
        block.setTone(columnIndex == LAYER_HEADER_BAND ? HubUiTokens.HubTone.GOLD : HubUiTokens.HubTone.STONE);
        parent.addChild(block);
        return block;
    }

    private Label createLabel(final String text, final Label.HorizontalAlign align) {
        final Label label = new Label(text, theme);
        label.setHorizontalAlign(align);
        label.setColor(theme.getTextColor());
        return label;
    }

    private void refreshScreenData(
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        headerTitleLabel.setText("洞天主殿 · 核验面板");
        headerDetailLabel.setText(buildHeaderDetailText(snapshot.core()));
        overallSummaryBlock.setBodyText(buildTopOverallSummary(snapshot, status));
        riskSummaryBlock.setBodyText(buildTopTribulationSummary(snapshot.core().tribulationTick(), status));
        nextRouteBlock.setBodyText(status.recommendation());
        summaryFootnoteBlock.setBodyText(buildRecentStatusSummary(status));
        fallbackExplainerBlock.setBodyText(buildHubGuideText());
        if (ascensionPlaceholderLabel != null) {
            ascensionPlaceholderLabel.setText(menu.getUiProjection().entryButtonLabel());
        }
        if (ascensionEntryButton != null) {
            ascensionEntryButton.setText(Component.literal(menu.getUiProjection().entryButtonLabel()));
            ascensionEntryButton.setEnabled(menu.getUiProjection().gameplayEntryAvailable());
        }
        refreshModuleCards(snapshot, status);
    }

    private void refreshModuleCards(
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        for (HubRoutePolicy.CardRoutePolicy policy : HubRoutePolicy.orderedPolicies()) {
            final ModuleCard card = moduleCardsById.get(policy.cardId());
            if (card == null) {
                continue;
            }
            applyModuleCardState(card, policy, snapshot, status);
        }
    }

    private void applyModuleCardState(
        final ModuleCard card,
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        card.setSummary(buildModuleCardSummary(policy, snapshot, status));
        card.setFootnote(buildModuleCardFootnote(policy, snapshot, status));
        card.applyRoutePolicy(policy);
        card.applyRisk(resolveRiskForCard(policy, snapshot, status));
        if (policy.isSummaryRoute()) {
            card.applyDataClass(HubSnapshot.DataClass.SUMMARY_ROUTE);
            return;
        }
        if (policy.isRouteOnly()) {
            card.setTaxonomy(HubRoutePolicy.HubCardTaxonomy.ROUTE_ONLY);
            return;
        }
        card.applyDataClass(HubSnapshot.DataClass.REAL_SUMMARY);
    }

    private HubStatusEvaluator.RiskLevel resolveRiskForCard(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW, HubRoutePolicy.CARD_TRIBULATION -> status.overallRisk();
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? HubStatusEvaluator.RiskLevel.UNKNOWN
                : status.overallRisk();
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? status.overallRisk()
                : HubStatusEvaluator.RiskLevel.UNKNOWN;
            default -> policy.usesPlaceholder()
                ? HubStatusEvaluator.RiskLevel.UNKNOWN
                : HubStatusEvaluator.RiskLevel.CAUTION;
        };
    }

    private String buildModuleCardSummary(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> formatCoreSummary(snapshot.core());
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? formatLandSpiritSummary(snapshot.landSpirit())
                : snapshot.landSpirit().fallbackText();
            case HubRoutePolicy.CARD_TRIBULATION -> formatTribulationSummary(snapshot.core().tribulationTick(), status);
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? snapshot.resource().fallbackText()
                : formatResourceSummary(snapshot.resource());
            default -> buildLightCardSummary(policy);
        };
    }

    private String buildModuleCardFootnote(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> buildCoreFootnote(snapshot.core());
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? buildLandSpiritFootnote(snapshot.landSpirit())
                : snapshot.landSpirit().fallbackText();
            case HubRoutePolicy.CARD_TRIBULATION -> buildTribulationFootnote(snapshot.core().tribulationTick(), status);
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? buildFallbackFootnote(policy, snapshot)
                : buildResourceFootnote(snapshot.resource());
            default -> buildLightCardFootnote(policy);
        };
    }

    private String buildFallbackFootnote(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().fallbackText();
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().fallbackText();
            default -> buildLightCardFootnote(policy);
        };
    }

    private String buildLightCardSummary(final HubRoutePolicy.CardRoutePolicy policy) {
        final String builtSummary = switch (policy.cardId()) {
            case HubRoutePolicy.CARD_ALCHEMY -> "独立工作台，待前往查看";
            case HubRoutePolicy.CARD_STORAGE -> "物品域最小视图，待前往查看";
            case HubRoutePolicy.CARD_CLUSTER -> "本地产出待提取，需前往查看";
            case HubRoutePolicy.CARD_DAO_MARK -> "占位子页入口，待前往查看";
            default -> "";
        };
        if (!builtSummary.isEmpty()) {
            return builtSummary;
        }
        return "仅保留入口，需前往" + policy.target().displayName() + "核验";
    }

    private String buildLightCardFootnote(final HubRoutePolicy.CardRoutePolicy policy) {
        if (policy.noticeText().isBlank()) {
            return "主殿只展示入口，不展示聚合运行数据";
        }
        return policy.noticeText() + "；主殿不展示全局实况";
    }

    private String buildHeaderDetailText(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return formatTimeSpeed(coreSnapshot.timeSpeedPercent())
            + "x · 好感 "
            + formatFavorability(coreSnapshot.favorabilityPercent())
            + " · "
            + formatTier(coreSnapshot.tier())
            + " · "
            + formatFrozenState(coreSnapshot.frozen());
    }

    private String buildTopOverallSummary(
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        return status.overallSummary() + "；边界 " + formatBoundary(snapshot.core());
    }

    private String buildTopTribulationSummary(
        final long tribulationTick,
        final HubStatusEvaluator.HubStatus status
    ) {
        return "倒计时 " + formatTribulationTime(tribulationTick);
    }

    private String buildQuickRouteSummary() {
        return "可直达：地灵、资源、炼丹、储物、集群";
    }

    private String buildRecentStatusSummary(final HubStatusEvaluator.HubStatus status) {
        return "非权威运营占位：轻卡未接入稳定聚合源，请以前往分台核验为准；当前建议："
            + status.recommendation();
    }

    private String buildHubGuideText() {
        return "主殿只保留总览与入口；炼丹、储物、集群、道痕仅显示摘要入口或占位入口，"
            + "不展示看似精准的全局运行数值。";
    }

    private String formatCoreSummary(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return formatTimeSpeed(coreSnapshot.timeSpeedPercent())
            + "x · 好感 "
            + formatFavorability(coreSnapshot.favorabilityPercent())
            + " · "
            + formatTier(coreSnapshot.tier());
    }

    private String buildCoreFootnote(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return "边界 "
            + formatBoundary(coreSnapshot)
            + "；状态 "
            + formatFrozenState(coreSnapshot.frozen());
    }

    private String formatLandSpiritSummary(final HubSnapshot.LandSpiritSnapshot landSpiritSnapshot) {
        return "阶段 "
            + landSpiritSnapshot.stage()
            + " · 转数 "
            + formatTier(landSpiritSnapshot.tier())
            + " · 好感 "
            + formatPermille(landSpiritSnapshot.favorabilityPermille());
    }

    private String buildLandSpiritFootnote(final HubSnapshot.LandSpiritSnapshot landSpiritSnapshot) {
        return "下一阶段：转数≥"
            + landSpiritSnapshot.nextStageMinTier()
            + "，好感≥"
            + formatPermille(landSpiritSnapshot.nextStageMinFavorabilityPermille());
    }

    private String formatResourceSummary(final HubSnapshot.ResourceSnapshot resourceSnapshot) {
        return "进度 "
            + formatPermille(resourceSnapshot.progressPermille())
            + " · 效率 "
            + resourceSnapshot.efficiencyPercent()
            + "%";
    }

    private String buildResourceFootnote(final HubSnapshot.ResourceSnapshot resourceSnapshot) {
        return "灵气 "
            + resourceSnapshot.auraValue()
            + " · 剩余 "
            + formatTribulationTime(resourceSnapshot.remainingTicks());
    }

    private String formatTribulationSummary(
        final long tribulationTick,
        final HubStatusEvaluator.HubStatus status
    ) {
        return "倒计时 " + formatTribulationTime(tribulationTick) + " · " + status.tribulationRiskSummary();
    }

    private String buildTribulationFootnote(
        final long tribulationTick,
        final HubStatusEvaluator.HubStatus status
    ) {
        return formatTribulationSummary(tribulationTick, status) + "；建议 " + status.recommendation();
    }

    private String formatBoundary(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return "X["
            + coreSnapshot.minChunkX()
            + ", "
            + coreSnapshot.maxChunkX()
            + "] Z["
            + coreSnapshot.minChunkZ()
            + ", "
            + coreSnapshot.maxChunkZ()
            + "]";
    }

    private static String formatTimeSpeed(final int percent) {
        return String.format(Locale.ROOT, "%.2f", percent / PERCENT_DENOMINATOR);
    }

    private static String formatFavorability(final int percent) {
        return String.format(Locale.ROOT, "%.2f%%", percent / PERCENT_DENOMINATOR);
    }

    private static String formatPermille(final int permille) {
        return String.format(Locale.ROOT, "%.1f%%", permille / PERMILLE_DENOMINATOR);
    }

    private static String formatTier(final int tier) {
        return tier + "转";
    }

    private static String formatFrozenState(final boolean frozen) {
        return frozen ? "冻结" : "运行中";
    }

    private static String formatTribulationTime(final long ticks) {
        final long totalSeconds = Math.max(0L, ticks / TICKS_PER_SECOND);
        final long hours = totalSeconds / SECONDS_PER_HOUR;
        final long days = hours / HOURS_PER_DAY;
        final long remainHours = hours % HOURS_PER_DAY;
        return days + "天 " + remainHours + "时";
    }

    private HubSnapshot buildSnapshot() {
        final HubSnapshot.CoreSnapshot coreSnapshot = new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            menu.getMinChunkX(),
            menu.getMaxChunkX(),
            menu.getMinChunkZ(),
            menu.getMaxChunkZ(),
            menu.getChunkSpanX(),
            menu.getChunkSpanZ(),
            menu.getTimeSpeedPercent(),
            menu.getFavorabilityPercent(),
            menu.getTier(),
            menu.isFrozen(),
            menu.getTribulationTick()
        );
        return HubSnapshot.fromCore(coreSnapshot);
    }

    private void bindRouteCallbacks(
        final ModuleCard card,
        final HubRoutePolicy.CardRoutePolicy policy
    ) {
        if (!policy.launchesScreen()) {
            card.setOnClick(null);
            card.getRouteChip().setOnClick(null);
            return;
        }
        final Runnable directRouteCallback = () -> openApprovedDirectRoute(policy);
        card.setOnClick(directRouteCallback);
        card.getRouteChip().setOnClick(directRouteCallback);
    }

    private void openApprovedDirectRoute(final HubRoutePolicy.CardRoutePolicy policy) {
        if (!policy.launchesScreen()) {
            return;
        }
        final LocalPlayer player = minecraft == null ? null : minecraft.player;
        if (player == null) {
            return;
        }
        final String routeFailureReason = switch (policy.target()) {
            case LAND_SPIRIT_SCREEN -> tryOpenLandSpiritRoute(player);
            case RESOURCE_CONTROLLER_SCREEN -> tryOpenResourceControllerRoute(player);
            case ALCHEMY_FURNACE_SCREEN -> tryOpenAlchemyFurnaceRoute(player);
            case STORAGE_GU_SCREEN -> tryOpenStorageGuRoute(player);
            case CLUSTER_NPC_SCREEN -> tryOpenClusterRoute(player);
            case CURRENT_HUB_OVERVIEW, TRIBULATION_SUB_VIEW, DAOMARK_SUB_VIEW -> "当前入口不允许直接打开";
        };
        if (routeFailureReason != null && !routeFailureReason.isBlank()) {
            announceRouteResolutionFailure(policy.target().displayName(), routeFailureReason);
        }
    }

    private String tryOpenLandSpiritRoute(final LocalPlayer player) {
        final LandSpiritEntity entity = findNearestLoadedEntity(player, LandSpiritEntity.class);
        if (entity == null) {
            return "附近未找到地灵实体";
        }
        if (currentGameMode() == null) {
            return "当前交互管理器不可用";
        }
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        final InteractionResult interactionResult = currentGameMode().interact(player, entity, hand);
        return interactionResult.consumesAction() ? null : "未能打开地灵分台";
    }

    private String tryOpenResourceControllerRoute(final LocalPlayer player) {
        return tryOpenNearbyBlockRoute(player, ResourceControllerBlock.class, "附近未找到资源控制器");
    }

    private String tryOpenAlchemyFurnaceRoute(final LocalPlayer player) {
        return tryOpenNearbyBlockRoute(player, AlchemyFurnaceBlock.class, "附近未找到炼丹炉");
    }

    private String tryOpenNearbyBlockRoute(
        final LocalPlayer player,
        final Class<? extends Block> blockClass,
        final String missingReason
    ) {
        final BlockPos blockPos = findNearestLoadedBlock(player, blockClass, ROUTE_BLOCK_SEARCH_RADIUS);
        if (blockPos == null) {
            return missingReason;
        }
        if (currentGameMode() == null) {
            return "当前交互管理器不可用";
        }
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        final BlockHitResult blockHitResult = new BlockHitResult(
            Vec3.atCenterOf(blockPos),
            Direction.UP,
            blockPos,
            false
        );
        final InteractionResult interactionResult = currentGameMode().useItemOn(player, hand, blockHitResult);
        return interactionResult.consumesAction() ? null : "未能打开目标分台";
    }

    private String tryOpenStorageGuRoute(final LocalPlayer player) {
        final int hotbarSlot = findStorageGuHotbarSlot(player);
        if (hotbarSlot < 0) {
            return "热栏未放置储物蛊";
        }
        if (currentGameMode() == null) {
            return "当前交互管理器不可用";
        }
        final int previousSelectedSlot = player.getInventory().selected;
        player.getInventory().selected = hotbarSlot;
        try {
            final InteractionHand hand = InteractionHand.MAIN_HAND;
            final InteractionResult interactionResult = currentGameMode().useItem(player, hand);
            return interactionResult.consumesAction() ? null : "未能打开储物分台";
        } finally {
            player.getInventory().selected = previousSelectedSlot;
        }
    }

    private String tryOpenClusterRoute(final LocalPlayer player) {
        final ClusterNpcEntity entity = findNearestLoadedEntity(player, ClusterNpcEntity.class);
        if (entity == null) {
            return "附近未找到集群节点";
        }
        if (currentGameMode() == null) {
            return "当前交互管理器不可用";
        }
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        final InteractionResult interactionResult = currentGameMode().interact(player, entity, hand);
        return interactionResult.consumesAction() ? null : "未能打开集群分台";
    }

    private void announceRouteResolutionFailure(
        final String targetDisplayName,
        final String routeFailureReason
    ) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        minecraft.player.displayClientMessage(
            Component.literal("无法前往" + targetDisplayName + "：" + routeFailureReason),
            true
        );
    }

    private MultiPlayerGameMode currentGameMode() {
        return minecraft == null ? null : minecraft.gameMode;
    }

    private int findStorageGuHotbarSlot(final LocalPlayer player) {
        for (int hotbarSlot = 0; hotbarSlot < HOTBAR_SLOT_COUNT; hotbarSlot++) {
            final ItemStack stack = player.getInventory().getItem(hotbarSlot);
            if (!stack.isEmpty() && stack.getItem() instanceof StorageGuItem) {
                return hotbarSlot;
            }
        }
        return -1;
    }

    private <T extends Entity> T findNearestLoadedEntity(
        final LocalPlayer player,
        final Class<T> entityClass
    ) {
        final List<T> candidates = player.level().getEntitiesOfClass(
            entityClass,
            player.getBoundingBox().inflate(ROUTE_ENTITY_SEARCH_RADIUS)
        );
        T nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (T entity : candidates) {
            final double distance = entity.distanceToSqr(player);
            if (distance < nearestDistance) {
                nearest = entity;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private BlockPos findNearestLoadedBlock(
        final LocalPlayer player,
        final Class<? extends Block> blockClass,
        final int searchRadiusBlocks
    ) {
        final BlockPos playerPos = player.blockPosition();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        final BlockPos min = playerPos.offset(-searchRadiusBlocks, -2, -searchRadiusBlocks);
        final BlockPos max = playerPos.offset(searchRadiusBlocks, 2, searchRadiusBlocks);
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (!player.level().hasChunkAt(candidate)) {
                continue;
            }
            final BlockState blockState = player.level().getBlockState(candidate);
            if (!blockClass.isInstance(blockState.getBlock())) {
                continue;
            }
            final double distance = candidate.distSqr(playerPos);
            if (distance < nearestDistance) {
                nearest = candidate.immutable();
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private record HubV2Layout(
        int contentWidth,
        int headerBandHeight,
        int topSituationRowHeight,
        int mainModuleGridHeight,
        int bottomUtilityRowHeight,
        int bodyViewportHeight,
        int bodyContentHeight,
        boolean useBodyScrollFallback
    ) {
    }

    private static final class RowBlock extends HubPanel {

        private static final int TITLE_Y = 6;

        private static final int BODY_Y = 20;

        private static final int INNER_PADDING = 6;

        private final Theme baseTheme;

        private final Label titleLabel;

        private final Label bodyLabel;

        private RowBlock(final Theme baseTheme, final String titleText) {
            super(HubUiTokens.HubTone.STONE);
            this.baseTheme = Objects.requireNonNull(baseTheme, "baseTheme");
            this.titleLabel = new Label(titleText, baseTheme);
            this.bodyLabel = new Label(Component.empty(), baseTheme);
            this.titleLabel.setColor(baseTheme.getPrimaryColor());
            this.bodyLabel.setColor(baseTheme.getTextColor());
            addChild(titleLabel);
            addChild(bodyLabel);
        }

        private void setBodyText(final String bodyText) {
            bodyLabel.setText(bodyText);
        }

        @Override
        public void onLayoutUpdated() {
            super.onLayoutUpdated();
            final int innerWidth = Math.max(0, getWidth() - INNER_PADDING * 2);
            titleLabel.setFrame(INNER_PADDING, TITLE_Y, innerWidth, HubUiTokens.SMALL_CONTROL_HEIGHT);
            bodyLabel.setFrame(INNER_PADDING, BODY_Y, innerWidth, Math.max(0, getHeight() - BODY_Y - INNER_PADDING));
        }
    }
}
