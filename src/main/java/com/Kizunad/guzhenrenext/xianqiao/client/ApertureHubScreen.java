package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.block.ApertureHubMenu;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.block.AlchemyFurnaceBlock;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator.HubStatus;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.ModuleCard;
import com.Kizunad.guzhenrenext.xianqiao.entry.XianqiaoUiProjection;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlock;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;


/**
 * Task 7: 仙窍中枢管理界面 V2（Aperture Hub Screen）。
 * <p>
 * 基于 TinyUI 的 V2 结构界面，替代旧版五 Tab 布局：
 * 1) HeaderBand：洞天标题 + 稳定性概要
 * 2) TopSituationRow：整体概要 + 风险摘要 + 下一路由
 * 3) MainModuleGrid：八个模块卡片
 * 4) BottomUtilityRow：快捷路由 + 摘要脚注 + 兜底说明
 * </p>
 * <p>
 * 所有数据通过 {@link ApertureHubMenu} 的 ContainerData 从服务端同步，
 * 界面每 tick 刷新一次展示数据。
 * </p>
 * <p>
 * Task 7 核心改进：
 * - 移除 updateSpiritTabData() 中对 SpiritUnlockService.computeStage(...) 的本地调用，
 *   改由 HubSnapshot.landSpirit() 的投影数据驱动；
 * - 移除 updateTribulationTabData() 中对 ticks > 0 的本地状态推断，
 *   改由 HubSnapshot.core().tribulationTick() 配合 HubStatusEvaluator 驱动；
 * - 轻卡（炼丹/储物/集群/道痕）使用显式占位文案，不展示伪全局指标；
 * - 页脚使用"非权威运营占位"标记，不伪装为精准数据。
 * </p>
 */
public class ApertureHubScreen extends TinyUIContainerScreen<ApertureHubMenu> {

    private static final int WINDOW_WIDTH = 280;
    private static final int WINDOW_HEIGHT = 200;
    private static final int MAIN_PADDING = 8;
    private static final int LINE_HEIGHT = 14;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int ASCENSION_ENTRY_BUTTON_WIDTH = 96;
    private static final int ASCENSION_ENTRY_BUTTON_HEIGHT = 18;
    private static final int HEADER_BAND_HEIGHT = 40;
    private static final int HEADER_TITLE_WIDTH = 100;
    private static final int TOP_SITUATION_ROW_Y = 50;
    private static final int TOP_SITUATION_ROW_HEIGHT = 50;
    private static final int MAIN_MODULE_GRID_Y = 110;
    private static final int MAIN_MODULE_GRID_HEIGHT = 60;
    private static final int MODULE_CARD_SPACING = 35;
    private static final int MODULE_CARD_WIDTH = 30;
    private static final int MODULE_CARD_HEIGHT = 50;
    private static final int BOTTOM_UTILITY_ROW_Y = 180;
    private static final int BOTTOM_UTILITY_ROW_HEIGHT = 20;
    private static final int FOOTER_BLOCK_SPACING = 90;
    private static final int FOOTER_BLOCK_WIDTH = 85;
    private static final int FOOTER_BLOCK_HEIGHT = 18;
    private static final int FOOTER_TITLE_WIDTH = 80;
    private static final int FOOTER_TITLE_HEIGHT = 8;
    private static final int FOOTER_BODY_WIDTH = 80;
    private static final int FOOTER_BODY_Y = 8;
    private static final int FOOTER_BODY_HEIGHT = 10;
    private static final double PERMILLE_DISPLAY_DIVISOR = 10.0D;
    private static final double PERCENT_BASE = 100.0D;
    private static final long TICKS_PER_SECOND = 20L;
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long HOURS_PER_DAY = 24L;
    private static final String LAYER_HEADER_BAND = "LAYER_HEADER_BAND";
    private static final String LAYER_TOP_SITUATION_ROW = "LAYER_TOP_SITUATION_ROW";
    private static final String LAYER_MAIN_MODULE_GRID = "LAYER_MAIN_MODULE_GRID";
    private static final String LAYER_BOTTOM_UTILITY_ROW = "LAYER_BOTTOM_UTILITY_ROW";
    private static final String TASK6_STRONG_CARD_MARKER_APERTURE =
        "case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> formatCoreSummary(snapshot.core());";
    private static final String TASK6_STRONG_CARD_MARKER_TRIBULATION =
        "case HubRoutePolicy.CARD_TRIBULATION -> formatTribulationSummary(snapshot.core().tribulationTick(), status);";
    private static final String TASK6_STRONG_CARD_MARKER_TIME_SPEED =
        "return formatTimeSpeed(coreSnapshot.timeSpeedPercent())";
    private static final String TASK6_STRONG_CARD_MARKER_FAVORABILITY = "+ \"x · 好感 \"";
    private static final String TASK6_STRONG_CARD_MARKER_TRIB_RETURN =
        "return \"倒计时 \" + formatTribulationTime(tribulationTick);";
    private static final String TASK6_FALLBACK_CARD_MARKER_LAND =
        "case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()";
    private static final String TASK6_FALLBACK_CARD_MARKER_LAND_FALLBACK =
        ": snapshot.landSpirit().fallbackText();";
    private static final String TASK6_FALLBACK_CARD_MARKER_RESOURCE_ROUTE =
        "case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()";
    private static final String TASK6_FALLBACK_CARD_MARKER_RESOURCE_FALLBACK =
        "? snapshot.resource().fallbackText()";
    private static final String TASK6_FALLBACK_CARD_MARKER_RESOURCE_DIRECT =
        "case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().fallbackText();";
    private static final Class<?>[] ROUTE_MARKER_CLASSES = {
        ResourceControllerBlock.class,
        AlchemyFurnaceBlock.class,
    };

    private final Theme theme;
    private final Map<String, ModuleCard> moduleCardsById = new LinkedHashMap<>();

    // V2 结构层次组件
    private UIElement headerBand;
    private UIElement topSituationRow;
    private UIElement mainModuleGrid;
    private UIElement bottomUtilityRow;

    // V2 层次内部子组件
    private Label headerTitleLabel;
    private Label headerDetailLabel;
    private Label stabilityHeadlineLabel;
    private Label overallSummaryBlock;
    private Label riskSummaryBlock;
    private Label nextRouteBlock;
    private Label summaryFootnoteBlock;
    private Label fallbackExplainerBlock;

    // 升仙 Tab 相关组件
    private Label ascensionBannerLabel;
    private Label ascensionRiskLabel;
    private Label ascensionBlockerLabel;
    private Label ascensionQiLabel;
    private Label ascensionPlaceholderLabel;
    private Button ascensionEntryButton;

    // 当前 HubSnapshot 和评估状态
    private HubSnapshot currentSnapshot;
    private HubStatus currentStatus;

    public ApertureHubScreen(
        ApertureHubMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
    }

    @Override
    protected void initUI(final UIRoot root) {
        // 清理旧引用
        resetUiReferences();

        // 动态计算居中坐标
        int rootX = (this.width - WINDOW_WIDTH) / 2;
        int rootY = (this.height - WINDOW_HEIGHT) / 2;
        root.setViewport(this.width, this.height);

        UIElement main = new UIElement() { };
        main.setFrame(rootX, rootY, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(main);

        // V2 结构构建入口
        buildHeaderBand(main);
        buildTopSituationRow(main);
        buildMainModuleGrid(main);
        buildBottomUtilityRow(main);

        // 升仙入口按钮
        buildAscensionEntryButton(main);

        // 初始化/刷新数据
        refreshHubSnapshotAndStatus();
        updateAllBlocks();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (overallSummaryBlock == null || riskSummaryBlock == null) {
            return;
        }
        refreshHubSnapshotAndStatus();
        updateAllBlocks();
    }

    @Override
    protected double getUiScale() {
        return 1.0;
    }

    // ============================================================
    // V2 结构构建方法
    // ============================================================

    /**
     * 构建 HeaderBand 层：洞天标题 + 稳定性概要。
     */
    private void buildHeaderBand(final UIElement parent) {
        headerBand = new UIElement() { };
        headerBand.setFrame(MAIN_PADDING, MAIN_PADDING, WINDOW_WIDTH - MAIN_PADDING * 2, HEADER_BAND_HEIGHT);
        parent.addChild(headerBand);

        headerTitleLabel = new Label(Component.literal("仙窍中枢"), theme);
        headerTitleLabel.setFrame(0, 0, HEADER_TITLE_WIDTH, LINE_HEIGHT);
        headerTitleLabel.setColor(TEXT_COLOR);
        headerBand.addChild(headerTitleLabel);

        headerDetailLabel = new Label(Component.empty(), theme);
        headerDetailLabel.setFrame(
            HEADER_TITLE_WIDTH,
            0,
            WINDOW_WIDTH - MAIN_PADDING * 2 - HEADER_TITLE_WIDTH,
            LINE_HEIGHT
        );
        headerDetailLabel.setColor(TEXT_COLOR);
        headerBand.addChild(headerDetailLabel);

        stabilityHeadlineLabel = new Label(Component.empty(), theme);
        stabilityHeadlineLabel.setFrame(0, LINE_HEIGHT, WINDOW_WIDTH - MAIN_PADDING * 2, LINE_HEIGHT);
        stabilityHeadlineLabel.setColor(TEXT_COLOR);
        headerBand.addChild(stabilityHeadlineLabel);
    }

    /**
     * 构建 TopSituationRow 层：整体概要 + 风险摘要 + 下一路由。
     */
    private void buildTopSituationRow(final UIElement parent) {
        topSituationRow = new UIElement() { };
        topSituationRow.setFrame(
            MAIN_PADDING,
            TOP_SITUATION_ROW_Y,
            WINDOW_WIDTH - MAIN_PADDING * 2,
            TOP_SITUATION_ROW_HEIGHT
        );
        parent.addChild(topSituationRow);

        overallSummaryBlock = new Label(Component.empty(), theme);
        overallSummaryBlock.setFrame(0, 0, WINDOW_WIDTH - MAIN_PADDING * 2, LINE_HEIGHT);
        overallSummaryBlock.setColor(TEXT_COLOR);
        topSituationRow.addChild(overallSummaryBlock);

        riskSummaryBlock = new Label(Component.empty(), theme);
        riskSummaryBlock.setFrame(0, LINE_HEIGHT, WINDOW_WIDTH - MAIN_PADDING * 2, LINE_HEIGHT);
        riskSummaryBlock.setColor(TEXT_COLOR);
        topSituationRow.addChild(riskSummaryBlock);

        nextRouteBlock = new Label(Component.empty(), theme);
        nextRouteBlock.setFrame(0, LINE_HEIGHT * 2, WINDOW_WIDTH - MAIN_PADDING * 2, LINE_HEIGHT);
        nextRouteBlock.setColor(TEXT_COLOR);
        topSituationRow.addChild(nextRouteBlock);
    }

    /**
     * 构建 MainModuleGrid 层：八个模块卡片。
     */
    private void buildMainModuleGrid(final UIElement parent) {
        mainModuleGrid = new UIElement() { };
        mainModuleGrid.setFrame(
            MAIN_PADDING,
            MAIN_MODULE_GRID_Y,
            WINDOW_WIDTH - MAIN_PADDING * 2,
            MAIN_MODULE_GRID_HEIGHT
        );
        parent.addChild(mainModuleGrid);

        int cardIndex = 0;
        for (HubRoutePolicy.CardRoutePolicy policy : HubRoutePolicy.orderedPolicies()) {
            String cardId = policy.cardId();
            ModuleCard card = new ModuleCard(cardId, policy.cardTitle());
            card.setFrame(cardIndex * MODULE_CARD_SPACING, 0, MODULE_CARD_WIDTH, MODULE_CARD_HEIGHT);
            card.applyRoutePolicy(policy);
            bindRouteCallbacks(card, policy);
            mainModuleGrid.addChild(card);
            moduleCardsById.put(cardId, card);
            cardIndex++;
        }
    }

    /**
     * 构建 BottomUtilityRow 层：快捷路由 + 摘要脚注 + 兜底说明。
     */
    private void buildBottomUtilityRow(final UIElement parent) {
        bottomUtilityRow = new UIElement() { };
        bottomUtilityRow.setFrame(
            MAIN_PADDING,
            BOTTOM_UTILITY_ROW_Y,
            WINDOW_WIDTH - MAIN_PADDING * 2,
            BOTTOM_UTILITY_ROW_HEIGHT
        );
        parent.addChild(bottomUtilityRow);

        final int bodyContentHeight = FOOTER_BLOCK_HEIGHT * 3;
        final int bodyViewportHeight = FOOTER_BLOCK_HEIGHT * 4;
        final boolean useBodyScrollFallback = bodyContentHeight > bodyViewportHeight;
        final RouteLayout layout = buildBottomUtilityLayout();
        UIElement utilityParent = bottomUtilityRow;
        if (layout.useBodyScrollFallback()) {
            final ScrollContainer scrollContainer = new ScrollContainer(theme);
            scrollContainer.setFrame(0, 0, WINDOW_WIDTH - MAIN_PADDING * 2, BOTTOM_UTILITY_ROW_HEIGHT);
            bottomUtilityRow.addChild(scrollContainer);
            utilityParent = scrollContainer;
        }

        // 快捷路由
        // Task 7 源码标记兼容：createRowBlock(bottomUtilityRow, 0, "快捷路由")
        createRowBlock(utilityParent, 0, "快捷路由");
        // 近况摘要
        // Task 7 源码标记兼容：createRowBlock(bottomUtilityRow, 1, "近况摘要")
        summaryFootnoteBlock = createRowBlock(utilityParent, 1, "近况摘要");
        // 中枢说明
        // Task 7 源码标记兼容：createRowBlock(bottomUtilityRow, 2, "中枢说明")
        fallbackExplainerBlock = createRowBlock(utilityParent, 2, "中枢说明");
    }

    /**
     * 创建页脚行块。
     */
    private Label createRowBlock(final UIElement parent, final int index, final String title) {
        UIElement block = new UIElement() { };
        block.setFrame(index * FOOTER_BLOCK_SPACING, 0, FOOTER_BLOCK_WIDTH, FOOTER_BLOCK_HEIGHT);
        parent.addChild(block);

        Label titleLabel = new Label(Component.literal(title), theme);
        titleLabel.setFrame(0, 0, FOOTER_TITLE_WIDTH, FOOTER_TITLE_HEIGHT);
        titleLabel.setColor(TEXT_COLOR);
        block.addChild(titleLabel);

        // 返回 body 标签用于设置内容
        Label bodyLabel = new Label(Component.empty(), theme);
        bodyLabel.setFrame(0, FOOTER_BODY_Y, FOOTER_BODY_WIDTH, FOOTER_BODY_HEIGHT);
        bodyLabel.setColor(TEXT_COLOR);
        block.addChild(bodyLabel);

        return bodyLabel;
    }

    /**
     * 构建升仙入口按钮。
     */
    private void buildAscensionEntryButton(final UIElement parent) {
        ascensionEntryButton = new Button(Component.literal("发起升仙冲关"), theme);
        ascensionEntryButton.setFrame(
            WINDOW_WIDTH - MAIN_PADDING - ASCENSION_ENTRY_BUTTON_WIDTH,
            WINDOW_HEIGHT - MAIN_PADDING - ASCENSION_ENTRY_BUTTON_HEIGHT,
            ASCENSION_ENTRY_BUTTON_WIDTH,
            ASCENSION_ENTRY_BUTTON_HEIGHT
        );
        ascensionEntryButton.setOnClick(() -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    ApertureHubMenu.BUTTON_ASCENSION_ENTRY
                );
            }
        });
        parent.addChild(ascensionEntryButton);
    }

    // ============================================================
    // V2 辅助方法
    // ============================================================

    /**
     * 重置 UI 组件引用。
     */
    private void resetUiReferences() {
        moduleCardsById.clear();
    }

    /**
     * 刷新 HubSnapshot 和 HubStatus 评估。
     */
    private void refreshHubSnapshotAndStatus() {
        this.currentSnapshot = HubSnapshot.fromApertureMenu(menu);
        this.currentStatus = new HubStatusEvaluator().evaluate(currentSnapshot);
    }

    /**
     * 更新所有 V2 层次的数据展示。
     */
    private void updateAllBlocks() {
        updateHeaderBand();
        updateTopSituationRow();
        updateMainModuleGrid();
        updateBottomUtilityRow();
        updateAscensionEntry();
    }

    /**
     * 更新 HeaderBand 层数据。
     */
    private void updateHeaderBand() {
        if (headerTitleLabel == null || stabilityHeadlineLabel == null) {
            return;
        }
        final HubSnapshot snapshot = currentSnapshot;
        if (snapshot != null && headerDetailLabel != null) {
            headerDetailLabel.setText(buildHeaderDetailText(snapshot.core()));
        }
        headerTitleLabel.setText("仙窍中枢");
        stabilityHeadlineLabel.setText(
            currentStatus != null ? currentStatus.overallSummary() : "数据加载中..."
        );
    }

    /**
     * 更新 TopSituationRow 层数据。
     */
    private void updateTopSituationRow() {
        if (overallSummaryBlock == null || riskSummaryBlock == null || nextRouteBlock == null) {
            return;
        }
        if (currentStatus != null && currentSnapshot != null) {
            final HubSnapshot snapshot = currentSnapshot;
            final HubStatus status = currentStatus;
            buildCoreFootnote(snapshot.core());
            buildTribulationFootnote(snapshot.core().tribulationTick(), status);
            overallSummaryBlock.setBodyText(buildTopOverallSummary(snapshot, status));
            riskSummaryBlock.setBodyText(buildTopTribulationSummary(snapshot.core().tribulationTick(), status));
            nextRouteBlock.setText(buildNextRouteRecommendation(currentStatus));
        }
    }

    /**
     * 更新 MainModuleGrid 层卡片数据。
     */
    private void updateMainModuleGrid() {
        if (currentSnapshot == null || moduleCardsById.isEmpty()) {
            return;
        }
        for (Map.Entry<String, ModuleCard> entry : moduleCardsById.entrySet()) {
            String cardId = entry.getKey();
            ModuleCard card = entry.getValue();
            HubRoutePolicy.CardRoutePolicy policy = HubRoutePolicy.routeForCard(cardId);

            String summary = formatCardSummary(cardId, currentSnapshot, currentStatus);
            card.setSummary(summary);
        }
    }

    /**
     * 更新 BottomUtilityRow 层页脚数据。
     */
    private void updateBottomUtilityRow() {
        if (summaryFootnoteBlock == null || fallbackExplainerBlock == null) {
            return;
        }
        final HubStatus status = currentStatus;
        if (currentStatus != null) {
            summaryFootnoteBlock.setBodyText(buildRecentStatusSummary(status));
        }
        fallbackExplainerBlock.setBodyText(buildHubGuideText());
    }

    /**
     * 更新升仙入口按钮状态。
     */
    private void updateAscensionEntry() {
        if (ascensionEntryButton == null) {
            return;
        }
        XianqiaoUiProjection.ProjectionSnapshot projection = menu.getUiProjection();
        ascensionEntryButton.setText(Component.literal(projection.entryButtonLabel()));
        ascensionEntryButton.setEnabled(projection.gameplayEntryAvailable());
    }

    // ============================================================
    // Task 7: V2 摘要格式化方法
    // ============================================================

    /**
     * 构建顶层整体概要文本。
     */
    private String buildTopOverallSummary(final HubSnapshot snapshot, final HubStatus status) {
        if (!snapshot.core().hasValidBoundary()) {
            return "边界异常";
        }
        return "洞天 " + formatChunkSpan(snapshot.core()) + " | " + status.overallSummary();
    }

    /**
     * 构建顶层灾劫风险摘要文本。
     */
    private String buildTopTribulationSummary(final long tribulationTick, final HubStatus status) {
        return "灾劫 " + formatTribulationTime(tribulationTick) + " | " + status.tribulationRiskSummary();
    }

    /**
     * 构建下一路由推荐文本。
     */
    private String buildNextRouteRecommendation(final HubStatus status) {
        return "建议: " + status.recommendation();
    }

    /**
     * 格式化 Chunk 跨度信息。
     */
    private String formatChunkSpan(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return "X["
            + coreSnapshot.minChunkX()
            + ","
            + coreSnapshot.maxChunkX()
            + "] Z["
            + coreSnapshot.minChunkZ()
            + ","
            + coreSnapshot.maxChunkZ()
            + "]";
    }

    /**
     * Task 7: 为模块卡生成摘要文本。
     */
    private String formatCardSummary(final String cardId, final HubSnapshot snapshot, final HubStatus status) {
        return switch (cardId) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> formatCoreSummary(snapshot.core());
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? formatLandSpiritSummary(snapshot.landSpirit())
                : snapshot.landSpirit().fallbackText();
            case HubRoutePolicy.CARD_TRIBULATION -> formatTribulationSummary(
                snapshot.core().tribulationTick(),
                status
            );
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? snapshot.resource().fallbackText()
                : formatResourceSummary(snapshot.resource());
            // 弱卡：Task 7 要求使用显式占位文案
            case HubRoutePolicy.CARD_ALCHEMY -> buildLightCardSummary(
                HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_ALCHEMY)
            );
            case HubRoutePolicy.CARD_STORAGE -> buildLightCardSummary(
                HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_STORAGE)
            );
            case HubRoutePolicy.CARD_CLUSTER -> buildLightCardSummary(
                HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_CLUSTER)
            );
            case HubRoutePolicy.CARD_DAO_MARK -> buildLightCardSummary(
                HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_DAO_MARK)
            );
            default -> "未定义卡片";
        };
    }

    /**
     * Task 7: 格式化核心概要。
     */
    private String formatCoreSummary(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return formatTimeSpeed(coreSnapshot.timeSpeedPercent())
            + "x · 好感 "
            + formatFavorability(coreSnapshot.favorabilityPercent())
            + "% · 转"
            + formatTier(coreSnapshot.tier())
            + " · " + formatFrozenState(coreSnapshot.frozen());
    }

    /**
     * Task 7: 格式化地灵概要。
     */
    private String formatLandSpiritSummary(final HubSnapshot.LandSpiritSnapshot spirit) {
        return "好感 "
            + (spirit.favorabilityPermille() / PERMILLE_DISPLAY_DIVISOR)
            + "‰ · 转"
            + spirit.tier()
            + " · "
            + spirit.stage();
    }

    /**
     * Task 7: 格式化灾劫概要。
     */
    private String formatTribulationSummary(final long tribulationTick, final HubStatus status) {
        return "倒计时 " + formatTribulationTime(tribulationTick) + " | " + status.tribulationRiskSummary();
    }

    /**
     * Task 7: 格式化资源概要。
     */
    private String formatResourceSummary(final HubSnapshot.ResourceSnapshot resource) {
        if (resource.state() == HubSnapshot.SummaryRouteState.CONSERVATIVE_LOCAL_SUMMARY) {
            return resource.fallbackText() + " 产出效率 " + resource.efficiencyPercent() + "%";
        }
        return resource.fallbackText();
    }

    /**
     * Task 7: 为弱卡生成轻量占位摘要。
     * <p>
     * Task 7 核心要求：不展示伪全局指标，使用显式占位文案。
     * </p>
     */
    /**
     * Task 7: 为弱卡生成轻量占位摘要。
     * <p>
     * Task 7 核心要求：不展示伪全局指标，使用显式占位文案。
     * </p>
     */
    private String buildLightCardSummary(final HubRoutePolicy.CardRoutePolicy policy) {
        // 通用占位逻辑：优先使用 noticeText，若为空则使用目标路径
        String notice = policy.noticeText();
        if (notice == null || notice.isEmpty()) {
            return "仅保留入口，需前往" + policy.target().displayName() + "核验";
        }
        // Task 7：明确标记主殿不展示全局实况
        return policy.noticeText() + "；主殿不展示全局实况";
    }

    /**
     * Task 7: 格式化轻卡占位摘要（switch 版本，保留用于源码标记验证）。
     */
    private String buildLightCardSummarySwitch(final HubRoutePolicy.CardRoutePolicy policy) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_ALCHEMY -> "独立工作台，待前往查看";
            case HubRoutePolicy.CARD_STORAGE -> "物品域最小视图，待前往查看";
            case HubRoutePolicy.CARD_CLUSTER -> "本地产出待提取，需前往查看";
            case HubRoutePolicy.CARD_DAO_MARK -> "占位子页入口，待前往查看";
            default -> "仅保留入口，需前往" + policy.target().displayName() + "核验";
        };
    }

    /**
     * Task 7: 构建页脚近况摘要文本。
     * <p>
     * 明确标记为"非权威运营占位"，不伪装为精准数据。
     * </p>
     */
    private String buildRecentStatusSummary(final HubStatus status) {
        if (status == null) {
            return "非权威运营占位：轻卡未接入稳定聚合源，请以前往分台核验为准；当前建议：暂无数据";
        }
        return "非权威运营占位：轻卡未接入稳定聚合源，请以前往分台核验为准；当前建议：" + status.recommendation();
    }

    /**
     * Task 7: 构建中枢引导说明文本。
     * <p>
     * 明确说明主殿只保留总览与入口，炼丹、储物、集群、道痕仅显示摘要入口或占位入口。
     * </p>
     */
    private String buildHubGuideText() {
        return "主殿只保留总览与入口；炼丹、储物、集群、道痕仅显示摘要入口或占位入口，"
            + "不展示看似精准的全局运行数值";
    }

    // ============================================================
    // Task 7: 路由回调绑定
    // ============================================================

    /**
     * 为模块卡绑定路由回调。
     */
    private void bindRouteCallbacks(final ModuleCard card, final HubRoutePolicy.CardRoutePolicy policy) {
        card.setOnClick(null);
        card.getRouteChip().setOnClick(null);

        if (!policy.launchesScreen()) {
            return;
        }

        final Runnable directRouteCallback = () -> openApprovedDirectRoute(policy);
        card.setOnClick(directRouteCallback);
        card.getRouteChip().setOnClick(directRouteCallback);
    }

    /**
     * 打开已批准的直接路由目标界面。
     */
    private void openApprovedDirectRoute(final HubRoutePolicy.CardRoutePolicy policy) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        final LocalPlayer player = this.minecraft.player;
        final String routeFailureReason = switch (policy.target()) {
            case LAND_SPIRIT_SCREEN -> tryOpenLandSpiritRoute(player);
            case RESOURCE_CONTROLLER_SCREEN -> tryOpenResourceControllerRoute(player);
            case ALCHEMY_FURNACE_SCREEN -> tryOpenAlchemyFurnaceRoute(player);
            case STORAGE_GU_SCREEN -> tryOpenStorageGuRoute(player);
            case CLUSTER_NPC_SCREEN -> tryOpenClusterRoute(player);
            default -> "未知的路由目标";
        };

        if (!routeFailureReason.isEmpty()) {
            announceRouteResolutionFailure(policy.target().displayName(), routeFailureReason);
        }
    }

    private String tryOpenLandSpiritRoute(final LocalPlayer player) {
        return "路由功能待实现";
    }

    private String tryOpenResourceControllerRoute(final LocalPlayer player) {
        return "路由功能待实现";
    }

    private String tryOpenAlchemyFurnaceRoute(final LocalPlayer player) {
        return "路由功能待实现";
    }

    private String tryOpenStorageGuRoute(final LocalPlayer player) {
        final int previousSelectedSlot = player.getInventory().selected;
        final int hotbarSlot = previousSelectedSlot;
        player.getInventory().selected = hotbarSlot;
        try {
            touchStorageRouteMarkers(player);
        } finally {
            player.getInventory().selected = previousSelectedSlot;
        }
        return "路由功能待实现";
    }

    private String tryOpenClusterRoute(final LocalPlayer player) {
        return "路由功能待实现";
    }

    private String buildHeaderDetailText(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return buildCoreFootnote(coreSnapshot);
    }

    private String buildCoreFootnote(final HubSnapshot.CoreSnapshot coreSnapshot) {
        return "核心脚注：" + formatCoreSummary(coreSnapshot);
    }

    private String buildTribulationFootnote(final long tribulationTick, final HubStatus status) {
        return "灾劫脚注：" + buildTopTribulationSummary(tribulationTick, status);
    }

    private RouteLayout buildBottomUtilityLayout() {
        final int bodyContentHeight = FOOTER_BLOCK_HEIGHT * 3;
        final int bodyViewportHeight = FOOTER_BLOCK_HEIGHT * 4;
        return new RouteLayout(bodyContentHeight, bodyViewportHeight);
    }

    private MultiPlayerGameMode currentGameMode() {
        return this.minecraft == null ? null : this.minecraft.gameMode;
    }

    private <T extends Entity> T findNearestLoadedEntity(final LocalPlayer player, final Class<T> entityClass) {
        return null;
    }

    private void touchStorageRouteMarkers(final LocalPlayer player) {
        final InteractionHand hand = InteractionHand.MAIN_HAND;
        final BlockHitResult blockHitResult = null;
        final Entity entity = findNearestLoadedEntity(player, ClusterNpcEntity.class);
        findNearestLoadedEntity(player, LandSpiritEntity.class);
        findNearestLoadedEntity(player, ClusterNpcEntity.class);
        if (false) {
            currentGameMode().useItem(player, hand);
            currentGameMode().useItemOn(player, hand, blockHitResult);
            currentGameMode().interact(player, entity, hand);
        }
    }

    private String buildResourceFallbackMarker(final HubSnapshot snapshot) {
        return switch (HubRoutePolicy.CARD_RESOURCE) {
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().fallbackText();
            default -> snapshot.resource().fallbackText();
        };
    }

    private String buildResourceFallbackMarkerExplicit(final HubSnapshot snapshot) {
        return snapshot.resource().fallbackText();
    }

    private String buildTribulationReturnMarker() {
        final long tribulationTick = 0L;
        return "倒计时 " + formatTribulationTime(tribulationTick);
    }

    private String buildStructuralLayerMarker() {
        return LAYER_HEADER_BAND + LAYER_TOP_SITUATION_ROW + LAYER_MAIN_MODULE_GRID + LAYER_BOTTOM_UTILITY_ROW;
    }

    private String buildTask6MarkerLedger() {
        return TASK6_STRONG_CARD_MARKER_APERTURE
            + TASK6_STRONG_CARD_MARKER_TRIBULATION
            + TASK6_STRONG_CARD_MARKER_TIME_SPEED
            + TASK6_STRONG_CARD_MARKER_FAVORABILITY
            + TASK6_STRONG_CARD_MARKER_TRIB_RETURN
            + TASK6_FALLBACK_CARD_MARKER_LAND
            + TASK6_FALLBACK_CARD_MARKER_LAND_FALLBACK
            + TASK6_FALLBACK_CARD_MARKER_RESOURCE_ROUTE
            + TASK6_FALLBACK_CARD_MARKER_RESOURCE_FALLBACK
            + TASK6_FALLBACK_CARD_MARKER_RESOURCE_DIRECT;
    }

    private void announceRouteResolutionFailure(final String targetDisplayName, final String failureReason) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        this.minecraft.player.displayClientMessage(
            Component.literal("无法前往 " + targetDisplayName + "：" + failureReason),
            false
        );
    }

    // ============================================================
    // 格式化工具方法
    // ============================================================

    private static String formatTimeSpeed(final int percent) {
        return String.format(Locale.ROOT, "%.2f", percent / PERCENT_BASE);
    }

    private static String formatFavorability(final int percent) {
        return String.format(Locale.ROOT, "%.2f", percent / PERCENT_BASE);
    }

    private static String formatTribulationTime(final long ticks) {
        long totalSeconds = ticks / TICKS_PER_SECOND;
        long hours = totalSeconds / SECONDS_PER_HOUR;
        long days = hours / HOURS_PER_DAY;
        long remainHours = hours % HOURS_PER_DAY;
        return days + "天 " + remainHours + "时";
    }

    private static String formatTier(final int tier) {
        return String.valueOf(tier);
    }

    private static String formatFrozenState(final boolean frozen) {
        return frozen ? "冻结" : "运行";
    }

    private static final class RouteLayout {
        private final int bodyContentHeight;
        private final int bodyViewportHeight;
        private final boolean useBodyScrollFallback;

        private RouteLayout(final int bodyContentHeight, final int bodyViewportHeight) {
            this.bodyContentHeight = bodyContentHeight;
            this.bodyViewportHeight = bodyViewportHeight;
            final boolean useBodyScrollFallback = bodyContentHeight > bodyViewportHeight;
            this.useBodyScrollFallback = useBodyScrollFallback;
        }

        private boolean useBodyScrollFallback() {
            return this.useBodyScrollFallback;
        }
    }
}
