package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.block.AlchemyFurnaceBlock;
import com.Kizunad.guzhenrenext.xianqiao.block.ApertureHubMenu;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubCardRegistry;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator.HubStatus;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.HubPanel;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.ModuleCard;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.PriorityStrip;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.StateBadge;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.StatePill;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuItem;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlock;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ApertureHubScreen extends TinyUIContainerScreen<ApertureHubMenu> {

    private static final int WINDOW_WIDTH = 336;
    private static final int WINDOW_HEIGHT = 264;
    private static final int WINDOW_PADDING = 10;
    private static final int BODY_SECTION_GAP = 8;
    private static final int HEADER_BAND_HEIGHT = 60;
    private static final int TOP_SITUATION_ROW_HEIGHT = 82;
    private static final int BOTTOM_UTILITY_ROW_HEIGHT = 92;
    private static final int SECTION_STRIP_X = 6;
    private static final int SECTION_STRIP_Y = 6;
    private static final int SECTION_STRIP_WIDTH = 112;
    private static final int SECTION_STRIP_HEIGHT = 14;
    private static final int SECTION_CONTENT_TOP = 24;
    private static final int HEADER_TITLE_X = 12;
    private static final int HEADER_TITLE_Y = 18;
    private static final int HEADER_TITLE_WIDTH = 150;
    private static final int HEADER_TITLE_HEIGHT = 14;
    private static final int HEADER_DETAIL_Y = 36;
    private static final int HEADER_DETAIL_WIDTH_MARGIN = 120;
    private static final int HEADER_DETAIL_HEIGHT = 14;
    private static final int HEADER_BADGE_WIDTH = 74;
    private static final int HEADER_BADGE_HEIGHT = 16;
    private static final int HEADER_BADGE_Y = 8;
    private static final int HEADER_BADGE_RIGHT_MARGIN = 12;
    private static final int HEADER_PILL_WIDTH = 94;
    private static final int HEADER_PILL_HEIGHT = 18;
    private static final int HEADER_PILL_Y = 32;
    private static final int ROW_BLOCK_COUNT = 3;
    private static final int ROW_BLOCK_GAP = 6;
    private static final int MODULE_CARD_COLUMNS = 2;
    private static final int MODULE_CARD_ROWS = 4;
    private static final int MODULE_CARD_GAP = 8;
    private static final int MODULE_CARD_HEIGHT = 82;
    private static final int DIRECT_ROUTE_HORIZONTAL_RADIUS = 6;
    private static final int DIRECT_ROUTE_VERTICAL_RADIUS = 3;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int CENTER_DIVISOR = 2;
    private static final int TEXT_COLOR = 0xFFE4E4E4;
    private static final int MUTED_TEXT_COLOR = 0xFFC1C1C1;
    private static final double PERCENT_BASE = 100.0D;
    private static final long TICKS_PER_SECOND = 20L;
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long HOURS_PER_DAY = 24L;
    private static final long TRIBULATION_WARNING_TICKS = 24000L;
    private static final String LAYER_HEADER_BAND = "LAYER_HEADER_BAND";
    private static final String LAYER_TOP_SITUATION_ROW = "LAYER_TOP_SITUATION_ROW";
    private static final String LAYER_MAIN_MODULE_GRID = "LAYER_MAIN_MODULE_GRID";
    private static final String LAYER_BOTTOM_UTILITY_ROW = "LAYER_BOTTOM_UTILITY_ROW";
    private static final String TAB_ASCENSION = "飞升桥接";
    private static final int ASCENSION_PANEL_WIDTH = 98;
    private static final int ASCENSION_PANEL_HEIGHT = 44;
    private static final int ASCENSION_PANEL_INSET = 4;
    private static final int ASCENSION_BUTTON_HEIGHT = 18;

    private final Theme theme = Theme.vanilla();
    private final HubStatusEvaluator statusEvaluator = new HubStatusEvaluator();
    private final Map<String, ModuleCard> moduleCardsById = new LinkedHashMap<>();

    private Label headerTitleLabel;
    private Label headerDetailLabel;
    private StateBadge headerStatusBadge;
    private StatePill headerStatusPill;
    private PriorityStrip headerLayerStrip;
    private RowBlock overallSummaryBlock;
    private RowBlock riskSummaryBlock;
    private RowBlock nextRouteBlock;
    private RowBlock utilityRoutesBlock;
    private RowBlock summaryFootnoteBlock;
    private RowBlock fallbackExplainerBlock;

    public ApertureHubScreen(
        final ApertureHubMenu menu,
        final Inventory playerInventory,
        final Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void initUI(final UIRoot root) {
        moduleCardsById.clear();
        resetUiReferences();

        final BandLayout layout = BandLayout.create(this.width, this.height);
        final HubPanel windowShell = new HubPanel();
        windowShell.setFrame(layout.rootX(), layout.rootY(), WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(windowShell);

        buildHeaderBand(windowShell, layout);

        final UIElement bodyContent = new UIElement() { };
        bodyContent.setFrame(0, 0, layout.bodyWidth(), layout.bodyContentHeight());
        buildTopSituationRow(bodyContent, layout);
        buildMainModuleGrid(bodyContent, layout);
        buildBottomUtilityRow(bodyContent, layout);

        if (layout.useBodyScrollFallback()) {
            final ScrollContainer scrollContainer = new ScrollContainer(theme);
            scrollContainer.setFrame(
                layout.bodyX(),
                layout.bodyY(),
                layout.bodyWidth(),
                layout.bodyViewportHeight()
            );
            scrollContainer.setContent(bodyContent);
            windowShell.addChild(scrollContainer);
        } else {
            bodyContent.setFrame(
                layout.bodyX(),
                layout.bodyY(),
                layout.bodyWidth(),
                layout.bodyContentHeight()
            );
            windowShell.addChild(bodyContent);
        }
        windowShell.addChild(createAscensionPanel());

        refreshHubContents();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshHubContents();
    }

    @Override
    protected double getUiScale() {
        return 1.0D;
    }

    private void resetUiReferences() {
        headerTitleLabel = null;
        headerDetailLabel = null;
        headerStatusBadge = null;
        headerStatusPill = null;
        headerLayerStrip = null;
        overallSummaryBlock = null;
        riskSummaryBlock = null;
        nextRouteBlock = null;
        utilityRoutesBlock = null;
        summaryFootnoteBlock = null;
        fallbackExplainerBlock = null;
    }

    private void buildHeaderBand(final UIElement parent, final BandLayout layout) {
        final HubPanel headerBand = new HubPanel();
        headerBand.setFrame(layout.bodyX(), WINDOW_PADDING, layout.bodyWidth(), HEADER_BAND_HEIGHT);
        parent.addChild(headerBand);

        headerLayerStrip = createLayerStrip(LAYER_HEADER_BAND);
        headerBand.addChild(headerLayerStrip);

        headerTitleLabel = new Label(Component.literal("仙窍中枢 · v2"), theme);
        headerTitleLabel.setColor(TEXT_COLOR);
        headerTitleLabel.setFrame(
            HEADER_TITLE_X,
            HEADER_TITLE_Y,
            HEADER_TITLE_WIDTH,
            HEADER_TITLE_HEIGHT
        );
        headerBand.addChild(headerTitleLabel);

        headerDetailLabel = new Label(Component.empty(), theme);
        headerDetailLabel.setColor(MUTED_TEXT_COLOR);
        headerDetailLabel.setFrame(
            HEADER_TITLE_X,
            HEADER_DETAIL_Y,
            layout.bodyWidth() - HEADER_DETAIL_WIDTH_MARGIN,
            HEADER_DETAIL_HEIGHT
        );
        headerBand.addChild(headerDetailLabel);

        headerStatusBadge = new StateBadge();
        headerStatusBadge.setFrame(
            layout.bodyWidth() - HEADER_BADGE_WIDTH - HEADER_BADGE_RIGHT_MARGIN,
            HEADER_BADGE_Y,
            HEADER_BADGE_WIDTH,
            HEADER_BADGE_HEIGHT
        );
        headerBand.addChild(headerStatusBadge);

        headerStatusPill = new StatePill();
        headerStatusPill.setFrame(
            layout.bodyWidth() - HEADER_PILL_WIDTH - HEADER_BADGE_RIGHT_MARGIN,
            HEADER_PILL_Y,
            HEADER_PILL_WIDTH,
            HEADER_PILL_HEIGHT
        );
        headerBand.addChild(headerStatusPill);
    }

    private void buildTopSituationRow(final UIElement parent, final BandLayout layout) {
        final HubPanel topSituationRow = new HubPanel();
        topSituationRow.setFrame(
            0,
            layout.topSituationRowY(),
            layout.bodyWidth(),
            TOP_SITUATION_ROW_HEIGHT
        );
        parent.addChild(topSituationRow);
        topSituationRow.addChild(createLayerStrip(LAYER_TOP_SITUATION_ROW));

        overallSummaryBlock = createRowBlock(topSituationRow, 0, "总览摘要");
        riskSummaryBlock = createRowBlock(topSituationRow, 1, "灾劫风险");
        nextRouteBlock = createRowBlock(topSituationRow, 2, "下一路由");
    }

    private void buildMainModuleGrid(final UIElement parent, final BandLayout layout) {
        final HubPanel mainModuleGrid = new HubPanel();
        mainModuleGrid.setFrame(
            0,
            layout.mainModuleGridY(),
            layout.bodyWidth(),
            layout.mainModuleGridHeight()
        );
        parent.addChild(mainModuleGrid);
        mainModuleGrid.addChild(createLayerStrip(LAYER_MAIN_MODULE_GRID));

        final int cardWidth = (layout.bodyWidth() - MODULE_CARD_GAP) / MODULE_CARD_COLUMNS;
        int cardIndex = 0;
        for (final HubCardRegistry.CardDefinition cardDefinition : HubCardRegistry.cards()) {
            final int row = cardIndex / MODULE_CARD_COLUMNS;
            final int column = cardIndex % MODULE_CARD_COLUMNS;
            final int cardX = column * (cardWidth + MODULE_CARD_GAP);
            final int cardY = SECTION_CONTENT_TOP + row * (MODULE_CARD_HEIGHT + MODULE_CARD_GAP);
            final ModuleCard card = new ModuleCard(cardDefinition.cardId(), cardDefinition.cardTitle());
            final HubRoutePolicy.CardRoutePolicy policy = HubRoutePolicy.routeForCard(cardDefinition.cardId());
            card.setFrame(cardX, cardY, cardWidth, MODULE_CARD_HEIGHT);
            card.applyRoutePolicy(policy);
            bindRouteCallbacks(card, policy);
            mainModuleGrid.addChild(card);
            moduleCardsById.put(cardDefinition.cardId(), card);
            cardIndex++;
        }
    }

    private void buildBottomUtilityRow(final UIElement parent, final BandLayout layout) {
        final HubPanel bottomUtilityRow = new HubPanel();
        bottomUtilityRow.setFrame(
            0,
            layout.bottomUtilityRowY(),
            layout.bodyWidth(),
            BOTTOM_UTILITY_ROW_HEIGHT
        );
        parent.addChild(bottomUtilityRow);
        bottomUtilityRow.addChild(createLayerStrip(LAYER_BOTTOM_UTILITY_ROW));

        utilityRoutesBlock = createRowBlock(bottomUtilityRow, 0, "快捷路由");
        summaryFootnoteBlock = createRowBlock(bottomUtilityRow, 1, "近况摘要");
        fallbackExplainerBlock = createRowBlock(bottomUtilityRow, 2, "中枢说明");
    }

    private HubPanel createAscensionPanel() {
        final HubPanel ascensionPanel = new HubPanel();
        ascensionPanel.setFrame(
            WINDOW_WIDTH - ASCENSION_PANEL_WIDTH - WINDOW_PADDING,
            WINDOW_HEIGHT - ASCENSION_PANEL_HEIGHT - WINDOW_PADDING,
            ASCENSION_PANEL_WIDTH,
            ASCENSION_PANEL_HEIGHT
        );

        final PriorityStrip ascensionStrip = new PriorityStrip();
        ascensionStrip.setText(TAB_ASCENSION);
        ascensionStrip.setFrame(
            ASCENSION_PANEL_INSET,
            ASCENSION_PANEL_INSET,
            ASCENSION_PANEL_WIDTH - ASCENSION_PANEL_INSET * CENTER_DIVISOR,
            SECTION_STRIP_HEIGHT
        );
        ascensionPanel.addChild(ascensionStrip);

        final Button ascensionEntryButton = new Button("进入飞升", theme);
        ascensionEntryButton.setFrame(
            ASCENSION_PANEL_INSET,
            ASCENSION_PANEL_HEIGHT - ASCENSION_BUTTON_HEIGHT - ASCENSION_PANEL_INSET,
            ASCENSION_PANEL_WIDTH - ASCENSION_PANEL_INSET * CENTER_DIVISOR,
            ASCENSION_BUTTON_HEIGHT
        );
        ascensionEntryButton.setOnClick(this::triggerLegacyAscensionEntry);
        ascensionPanel.addChild(ascensionEntryButton);
        return ascensionPanel;
    }

    private RowBlock createRowBlock(final UIElement parent, final int rowIndex, final String title) {
        final int blockWidth =
            (parent.getWidth() - ROW_BLOCK_GAP * (ROW_BLOCK_COUNT - 1)) / ROW_BLOCK_COUNT;
        final int blockX = rowIndex * (blockWidth + ROW_BLOCK_GAP);
        final int blockHeight = parent.getHeight() - SECTION_CONTENT_TOP;
        final RowBlock rowBlock = new RowBlock(title, theme);
        rowBlock.setFrame(blockX, SECTION_CONTENT_TOP, blockWidth, blockHeight);
        parent.addChild(rowBlock);
        return rowBlock;
    }

    private PriorityStrip createLayerStrip(final String layerName) {
        final PriorityStrip structureStrip = new PriorityStrip();
        structureStrip.setText(layerName);
        structureStrip.setFrame(
            SECTION_STRIP_X,
            SECTION_STRIP_Y,
            SECTION_STRIP_WIDTH,
            SECTION_STRIP_HEIGHT
        );
        return structureStrip;
    }

    private void triggerLegacyAscensionEntry() {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }
        this.minecraft.gameMode.handleInventoryButtonClick(
            this.menu.containerId,
            ApertureHubMenu.BUTTON_ASCENSION_ENTRY
        );
    }

    private void refreshHubContents() {
        if (headerDetailLabel == null || overallSummaryBlock == null || fallbackExplainerBlock == null) {
            return;
        }

        final HubSnapshot snapshot = HubSnapshot.fromApertureMenu(menu);
        final HubStatus status = statusEvaluator.evaluate(snapshot);

        headerTitleLabel.setText(Component.literal("仙窍中枢 · 洞天总览"));
        headerDetailLabel.setText(buildHeaderDetailText(snapshot.core()));
        headerStatusBadge.applyRisk(status.overallRisk());
        headerStatusPill.applyRisk(status.overallRisk());
        headerLayerStrip.setText(LAYER_HEADER_BAND);

        overallSummaryBlock.setRiskLevel(status.overallRisk());
        overallSummaryBlock.setBodyText(buildTopOverallSummary(snapshot, status));
        riskSummaryBlock.setRiskLevel(resolveTribulationRisk(snapshot.core().tribulationTick()));
        riskSummaryBlock.setBodyText(buildTopTribulationSummary(snapshot.core().tribulationTick(), status));
        nextRouteBlock.setRiskLevel(status.overallRisk());
        nextRouteBlock.setBodyText(buildNextRouteSummary(status));

        utilityRoutesBlock.setRiskLevel(HubStatusEvaluator.RiskLevel.STABLE);
        utilityRoutesBlock.setBodyText(buildUtilityRoutesSummary());
        summaryFootnoteBlock.setRiskLevel(status.overallRisk());
        summaryFootnoteBlock.setBodyText(buildRecentStatusSummary(status));
        fallbackExplainerBlock.setRiskLevel(HubStatusEvaluator.RiskLevel.UNKNOWN);
        fallbackExplainerBlock.setBodyText(buildHubGuideText());

        for (final HubCardRegistry.CardDefinition cardDefinition : HubCardRegistry.cards()) {
            final ModuleCard card = moduleCardsById.get(cardDefinition.cardId());
            if (card == null) {
                continue;
            }
            final HubRoutePolicy.CardRoutePolicy policy = HubRoutePolicy.routeForCard(cardDefinition.cardId());
            card.setTitle(cardDefinition.cardTitle());
            card.applyDataClass(resolveCardDataClass(policy));
            card.applyRisk(resolveCardRisk(policy, snapshot));
            card.applyRoutePolicy(policy);
            card.setSummary(resolveCardSummary(snapshot, status, policy));
            card.setFootnote(resolveCardFootnote(snapshot, status, policy));
        }
    }

    private HubSnapshot.DataClass resolveCardDataClass(final HubRoutePolicy.CardRoutePolicy policy) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW,
                HubRoutePolicy.CARD_TRIBULATION -> HubSnapshot.DataClass.REAL_CORE;
            case HubRoutePolicy.CARD_LAND_SPIRIT -> HubSnapshot.DataClass.REAL_SUMMARY;
            case HubRoutePolicy.CARD_RESOURCE, HubRoutePolicy.CARD_ALCHEMY, HubRoutePolicy.CARD_STORAGE,
                HubRoutePolicy.CARD_CLUSTER, HubRoutePolicy.CARD_DAO_MARK -> HubSnapshot.DataClass.SUMMARY_ROUTE;
            default -> HubSnapshot.DataClass.SUMMARY_ROUTE;
        };
    }

    private HubStatusEvaluator.RiskLevel resolveCardRisk(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> snapshot.core().hasValidBoundary()
                ? HubStatusEvaluator.RiskLevel.STABLE
                : HubStatusEvaluator.RiskLevel.DANGER;
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? HubStatusEvaluator.RiskLevel.STABLE
                : HubStatusEvaluator.RiskLevel.UNKNOWN;
            case HubRoutePolicy.CARD_TRIBULATION -> resolveTribulationRisk(snapshot.core().tribulationTick());
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? HubStatusEvaluator.RiskLevel.CAUTION
                : HubStatusEvaluator.RiskLevel.STABLE;
            case HubRoutePolicy.CARD_ALCHEMY, HubRoutePolicy.CARD_STORAGE, HubRoutePolicy.CARD_CLUSTER,
                HubRoutePolicy.CARD_DAO_MARK -> HubStatusEvaluator.RiskLevel.UNKNOWN;
            default -> HubStatusEvaluator.RiskLevel.UNKNOWN;
        };
    }

    private String resolveCardSummary(
        final HubSnapshot snapshot,
        final HubStatus status,
        final HubRoutePolicy.CardRoutePolicy policy
    ) {
        if (policy.isRouteOnly()) {
            return buildLightCardSummary(policy);
        }
        return buildStrongCardSummary(snapshot, status, policy);
    }

    private String resolveCardFootnote(
        final HubSnapshot snapshot,
        final HubStatus status,
        final HubRoutePolicy.CardRoutePolicy policy
    ) {
        if (policy.isRouteOnly()) {
            return buildLightCardFootnote(policy);
        }
        return buildStrongCardFootnote(snapshot, status, policy);
    }

    private String buildStrongCardSummary(
        final HubSnapshot snapshot,
        final HubStatus status,
        final HubRoutePolicy.CardRoutePolicy policy
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> formatCoreSummary(snapshot.core());
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? formatLandSpiritSummary(snapshot)
                : snapshot.landSpirit().fallbackText();
            case HubRoutePolicy.CARD_TRIBULATION -> formatTribulationSummary(snapshot.core().tribulationTick(), status);
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? snapshot.resource().fallbackText()
                : formatResourceSummary(snapshot);
            default -> status.fallbackText();
        };
    }

    private String buildStrongCardFootnote(
        final HubSnapshot snapshot,
        final HubStatus status,
        final HubRoutePolicy.CardRoutePolicy policy
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> buildCoreFootnote(snapshot.core());
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? buildLandSpiritFootnote(snapshot)
                : snapshot.landSpirit().fallbackText();
            case HubRoutePolicy.CARD_TRIBULATION ->
                buildTribulationFootnote(snapshot.core().tribulationTick(), status);
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().fallbackText();
            default -> status.fallbackText();
        };
    }

    private String buildLightCardSummary(final HubRoutePolicy.CardRoutePolicy policy) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_ALCHEMY -> "独立工作台，待前往查看";
            case HubRoutePolicy.CARD_STORAGE -> "物品域最小视图，待前往查看";
            case HubRoutePolicy.CARD_CLUSTER -> "本地产出待提取，需前往查看";
            case HubRoutePolicy.CARD_DAO_MARK -> "占位子页入口，待前往查看";
            default -> buildDefaultLightCardSummary(policy);
        };
    }

    private String buildDefaultLightCardSummary(final HubRoutePolicy.CardRoutePolicy policy) {
        return "仅保留入口，需前往" + policy.target().displayName() + "核验";
    }

    private String buildLightCardFootnote(final HubRoutePolicy.CardRoutePolicy policy) {
        if (policy.noticeText().isBlank()) {
            return policy.target().displayName() + "分台尚未接入主殿稳定聚合源";
        }
        return policy.noticeText() + "；主殿不展示全局实况";
    }

    private String buildHeaderDetailText(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return formatTimeSpeed(coreSnapshot.timeSpeedPercent())
            + "x · 好感 "
            + formatFavorability(coreSnapshot.favorabilityPercent())
            + "% · "
            + formatTier(coreSnapshot.tier())
            + " · "
            + formatFrozenState(coreSnapshot.frozen());
    }

    private String buildTopOverallSummary(final HubSnapshot snapshot, final HubStatus status) {
        return status.overallSummary() + "\n" + formatCoreSummary(snapshot.core());
    }

    private String buildTopTribulationSummary(final long tribulationTick, final HubStatus status) {
        if (status.tribulationRiskSummary().isBlank()) {
            return "倒计时 " + formatTribulationTime(tribulationTick);
        }
        return "倒计时 " + formatTribulationTime(tribulationTick)
            + "\n"
            + status.tribulationRiskSummary();
    }

    private String buildNextRouteSummary(final HubStatus status) {
        return "建议：" + status.recommendation();
    }

    private String buildUtilityRoutesSummary() {
        final StringBuilder builder = new StringBuilder();
        for (final HubRoutePolicy.CardRoutePolicy policy : HubRoutePolicy.orderedPolicies()) {
            if (!policy.launchesScreen()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append('、');
            }
            builder.append(policy.target().displayName());
        }
        return "已接直达：" + builder;
    }

    private String buildRecentStatusSummary(final HubStatus status) {
        return "非权威运营占位：轻卡未接入稳定聚合源，请以前往分台核验为准；当前建议："
            + status.recommendation();
    }

    private String buildHubGuideText() {
        return "主殿只保留总览与入口；炼丹、储物、集群、道痕仅显示摘要入口或占位入口，"
            + "不展示看似精准的全局运行数值；需要细节时请直接前往对应分台。";
    }

    private String formatCoreSummary(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return "边界 X["
            + coreSnapshot.minChunkX()
            + ", "
            + coreSnapshot.maxChunkX()
            + "] Z["
            + coreSnapshot.minChunkZ()
            + ", "
            + coreSnapshot.maxChunkZ()
            + "] · "
            + formatTier(coreSnapshot.tier())
            + " · "
            + formatFrozenState(coreSnapshot.frozen());
    }

    private String formatLandSpiritSummary(final HubSnapshot snapshot) {
        return "阶段 "
            + snapshot.landSpirit().stage()
            + " · 转数 "
            + snapshot.landSpirit().tier()
            + " · 好感 "
            + snapshot.landSpirit().favorabilityPermille()
            + "‰";
    }

    private String formatResourceSummary(final HubSnapshot snapshot) {
        return "效率 "
            + snapshot.resource().efficiencyPercent()
            + "% · 灵气 "
            + snapshot.resource().auraValue()
            + " · 剩余 "
            + formatTribulationTime(snapshot.resource().remainingTicks());
    }

    private String formatTribulationSummary(final long tribulationTick, final HubStatus status) {
        if (status == null) {
            return "倒计时 " + formatTribulationTime(tribulationTick);
        }
        return "倒计时 " + formatTribulationTime(tribulationTick)
            + " · "
            + status.tribulationRiskSummary();
    }

    private String buildCoreFootnote(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return "跨度 "
            + coreSnapshot.chunkSpanX()
            + "×"
            + coreSnapshot.chunkSpanZ()
            + " · 转速 "
            + formatTimeSpeed(coreSnapshot.timeSpeedPercent())
            + "x";
    }

    private String buildLandSpiritFootnote(final HubSnapshot snapshot) {
        return "下一阶段需求：转数 "
            + snapshot.landSpirit().nextStageMinTier()
            + " · 好感 "
            + snapshot.landSpirit().nextStageMinFavorabilityPermille()
            + "‰";
    }

    private String buildTribulationFootnote(final long tribulationTick, final HubStatus status) {
        return status.tribulationRiskSummary()
            + " · 倒计时 "
            + formatTribulationTime(tribulationTick);
    }

    private String formatTier(final int tier) {
        return "转数 " + tier;
    }

    private String formatFrozenState(final boolean frozen) {
        return frozen ? "冻结态" : "运行态";
    }

    private void bindRouteCallbacks(final ModuleCard card, final HubRoutePolicy.CardRoutePolicy policy) {
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
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.gameMode == null) {
            announceRouteResolutionFailure(policy.target().displayName(), "客户端上下文未就绪");
            return;
        }
        final LocalPlayer player = this.minecraft.player;
        final String routeFailureReason = switch (policy.target()) {
            case LAND_SPIRIT_SCREEN -> tryOpenLandSpiritRoute(player);
            case RESOURCE_CONTROLLER_SCREEN -> tryOpenResourceControllerRoute(player);
            case ALCHEMY_FURNACE_SCREEN -> tryOpenAlchemyFurnaceRoute(player);
            case STORAGE_GU_SCREEN -> tryOpenStorageGuRoute(player);
            case CLUSTER_NPC_SCREEN -> tryOpenClusterRoute(player);
            case CURRENT_HUB_OVERVIEW,
                TRIBULATION_SUB_VIEW,
                DAOMARK_SUB_VIEW -> "当前策略不允许主殿直达";
        };
        if (routeFailureReason != null) {
            announceRouteResolutionFailure(policy.target().displayName(), routeFailureReason);
        }
    }

    private String tryOpenLandSpiritRoute(final LocalPlayer player) {
        final LandSpiritEntity entity = findNearestLoadedEntity(player, LandSpiritEntity.class);
        if (entity == null) {
            return "附近未找到已加载地灵";
        }
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        currentGameMode().interact(player, entity, hand);
        return null;
    }

    private String tryOpenResourceControllerRoute(final LocalPlayer player) {
        final BlockPos blockPos = findNearestLoadedBlock(
            player,
            ResourceControllerBlock.class,
            DIRECT_ROUTE_HORIZONTAL_RADIUS
        );
        if (blockPos == null) {
            return "附近未找到资源控制器";
        }
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        final BlockHitResult blockHitResult = createCenterBlockHitResult(blockPos);
        currentGameMode().useItemOn(player, hand, blockHitResult);
        return null;
    }

    private String tryOpenAlchemyFurnaceRoute(final LocalPlayer player) {
        final BlockPos blockPos = findNearestLoadedBlock(
            player,
            AlchemyFurnaceBlock.class,
            DIRECT_ROUTE_HORIZONTAL_RADIUS
        );
        if (blockPos == null) {
            return "附近未找到炼丹炉";
        }
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        final BlockHitResult blockHitResult = createCenterBlockHitResult(blockPos);
        currentGameMode().useItemOn(player, hand, blockHitResult);
        return null;
    }

    private String tryOpenStorageGuRoute(final LocalPlayer player) {
        final int hotbarSlot = findStorageGuHotbarSlot(player);
        if (hotbarSlot < 0) {
            return "热键栏未找到储物蛊";
        }
        final int previousSelectedSlot = player.getInventory().selected;
        player.getInventory().selected = hotbarSlot;
        try {
            final InteractionHand hand = InteractionHand.MAIN_HAND;
            currentGameMode().useItem(player, hand);
            return null;
        } finally {
            player.getInventory().selected = previousSelectedSlot;
        }
    }

    private String tryOpenClusterRoute(final LocalPlayer player) {
        final ClusterNpcEntity entity = findNearestLoadedEntity(player, ClusterNpcEntity.class);
        if (entity == null) {
            return "附近未找到集群劳作体";
        }
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        currentGameMode().interact(player, entity, hand);
        return null;
    }

    private void announceRouteResolutionFailure(final String routeName, final String routeFailureReason) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        this.minecraft.player.displayClientMessage(
            Component.literal(routeName + "打开失败：" + routeFailureReason),
            true
        );
    }

    private <T extends Entity> T findNearestLoadedEntity(final LocalPlayer player, final Class<T> entityClass) {
        final AABB searchBox = new AABB(player.blockPosition()).inflate(
            DIRECT_ROUTE_HORIZONTAL_RADIUS,
            DIRECT_ROUTE_VERTICAL_RADIUS,
            DIRECT_ROUTE_HORIZONTAL_RADIUS
        );
        T nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (final T entity : player.level().getEntitiesOfClass(entityClass, searchBox)) {
            final double distance = player.distanceToSqr(entity);
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
        final int horizontalRadius
    ) {
        final BlockPos playerPos = player.blockPosition();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int yOffset = -DIRECT_ROUTE_VERTICAL_RADIUS; yOffset <= DIRECT_ROUTE_VERTICAL_RADIUS; yOffset++) {
            for (
                int xOffset = -horizontalRadius;
                xOffset <= horizontalRadius;
                xOffset++
            ) {
                for (
                    int zOffset = -horizontalRadius;
                    zOffset <= horizontalRadius;
                    zOffset++
                ) {
                    cursor.set(
                        playerPos.getX() + xOffset,
                        playerPos.getY() + yOffset,
                        playerPos.getZ() + zOffset
                    );
                    if (!player.level().hasChunkAt(cursor)) {
                        continue;
                    }
                    if (!blockClass.isInstance(player.level().getBlockState(cursor).getBlock())) {
                        continue;
                    }
                    final double distance = cursor.distSqr(playerPos);
                    if (distance < nearestDistance) {
                        nearest = cursor.immutable();
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
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

    private BlockHitResult createCenterBlockHitResult(final BlockPos blockPos) {
        return new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);
    }

    private MultiPlayerGameMode currentGameMode() {
        return Objects.requireNonNull(this.minecraft.gameMode, "gameMode");
    }

    private HubStatusEvaluator.RiskLevel resolveTribulationRisk(final long tribulationTick) {
        if (tribulationTick <= 0L) {
            return HubStatusEvaluator.RiskLevel.DANGER;
        }
        if (tribulationTick <= TRIBULATION_WARNING_TICKS) {
            return HubStatusEvaluator.RiskLevel.CAUTION;
        }
        return HubStatusEvaluator.RiskLevel.STABLE;
    }

    private static String formatTimeSpeed(final int percent) {
        return String.format(Locale.ROOT, "%.2f", percent / PERCENT_BASE);
    }

    private static String formatFavorability(final int percent) {
        return String.format(Locale.ROOT, "%.2f", percent / PERCENT_BASE);
    }

    private static String formatTribulationTime(final long ticks) {
        final long totalSeconds = ticks / TICKS_PER_SECOND;
        final long hours = totalSeconds / SECONDS_PER_HOUR;
        final long days = hours / HOURS_PER_DAY;
        final long remainHours = hours % HOURS_PER_DAY;
        return days + "天 " + remainHours + "时";
    }

    private record BandLayout(
        int rootX,
        int rootY,
        int bodyX,
        int bodyY,
        int bodyWidth,
        int bodyViewportHeight,
        int topSituationRowY,
        int mainModuleGridY,
        int mainModuleGridHeight,
        int bottomUtilityRowY,
        int bodyContentHeight,
        boolean useBodyScrollFallback
    ) {

        private static BandLayout create(final int screenWidth, final int screenHeight) {
            final int rootX = Math.max(0, (screenWidth - WINDOW_WIDTH) / CENTER_DIVISOR);
            final int rootY = Math.max(0, (screenHeight - WINDOW_HEIGHT) / CENTER_DIVISOR);
            final int bodyX = WINDOW_PADDING;
            final int bodyY = WINDOW_PADDING + HEADER_BAND_HEIGHT + BODY_SECTION_GAP;
            final int bodyWidth = WINDOW_WIDTH - WINDOW_PADDING * CENTER_DIVISOR;
            final int bodyViewportHeight = WINDOW_HEIGHT - WINDOW_PADDING - bodyY;
            final int mainModuleGridHeight = SECTION_CONTENT_TOP
                + MODULE_CARD_ROWS * MODULE_CARD_HEIGHT
                + MODULE_CARD_GAP * (MODULE_CARD_ROWS - 1);
            final int topSituationRowY = 0;
            final int mainModuleGridY = TOP_SITUATION_ROW_HEIGHT + BODY_SECTION_GAP;
            final int bottomUtilityRowY = mainModuleGridY + mainModuleGridHeight + BODY_SECTION_GAP;
            final int bodyContentHeight = TOP_SITUATION_ROW_HEIGHT
                + BODY_SECTION_GAP
                + mainModuleGridHeight
                + BODY_SECTION_GAP
                + BOTTOM_UTILITY_ROW_HEIGHT;
            final boolean useBodyScrollFallback = bodyContentHeight > bodyViewportHeight;
            return new BandLayout(
                rootX,
                rootY,
                bodyX,
                bodyY,
                bodyWidth,
                bodyViewportHeight,
                topSituationRowY,
                mainModuleGridY,
                mainModuleGridHeight,
                bottomUtilityRowY,
                bodyContentHeight,
                useBodyScrollFallback
            );
        }
    }

    private static final class RowBlock extends HubPanel {

        private static final int STRIP_WIDTH = 68;
        private static final int STRIP_HEIGHT = 14;
        private static final int BADGE_WIDTH = 58;
        private static final int BADGE_HEIGHT = 16;
        private static final int PILL_WIDTH = 76;
        private static final int PILL_HEIGHT = 18;
        private static final int PANEL_PADDING = 6;
        private static final int TITLE_Y = 24;
        private static final int TITLE_HEIGHT = 14;
        private static final int BODY_Y = 42;

        private final PriorityStrip structureStrip;
        private final StateBadge statusBadge;
        private final StatePill statusPill;
        private final Label titleLabel;
        private final Label bodyLabel;

        private RowBlock(final String title, final Theme theme) {
            this.structureStrip = new PriorityStrip();
            this.statusBadge = new StateBadge();
            this.statusPill = new StatePill();
            this.titleLabel = new Label(Component.literal(title), theme);
            this.bodyLabel = new Label(Component.empty(), theme);
            this.titleLabel.setColor(TEXT_COLOR);
            this.bodyLabel.setColor(MUTED_TEXT_COLOR);
            this.structureStrip.setText("冻结槽位");
            addChild(structureStrip);
            addChild(statusBadge);
            addChild(statusPill);
            addChild(titleLabel);
            addChild(bodyLabel);
        }

        private void setBodyText(final String text) {
            bodyLabel.setText(text);
        }

        private void setRiskLevel(final HubStatusEvaluator.RiskLevel riskLevel) {
            statusBadge.applyRisk(riskLevel);
            statusPill.applyRisk(riskLevel);
        }

        @Override
        protected void onRender(
            final com.Kizunad.tinyUI.core.UIRenderContext context,
            final double mouseX,
            final double mouseY,
            final float partialTicks
        ) {
            drawPanelChrome(context, false);
            layoutChildren();
        }

        private void layoutChildren() {
            final int badgeX = getWidth() - BADGE_WIDTH - PANEL_PADDING;
            final int pillY = getHeight() - PILL_HEIGHT - PANEL_PADDING;
            structureStrip.setFrame(PANEL_PADDING, PANEL_PADDING, STRIP_WIDTH, STRIP_HEIGHT);
            statusBadge.setFrame(badgeX, PANEL_PADDING, BADGE_WIDTH, BADGE_HEIGHT);
            statusPill.setFrame(PANEL_PADDING, pillY, PILL_WIDTH, PILL_HEIGHT);
            titleLabel.setFrame(
                PANEL_PADDING,
                TITLE_Y,
                getWidth() - PANEL_PADDING * CENTER_DIVISOR,
                TITLE_HEIGHT
            );
            bodyLabel.setFrame(
                PANEL_PADDING,
                BODY_Y,
                getWidth() - PANEL_PADDING * CENTER_DIVISOR,
                Math.max(0, pillY - BODY_Y - PANEL_PADDING)
            );
        }
    }
}
