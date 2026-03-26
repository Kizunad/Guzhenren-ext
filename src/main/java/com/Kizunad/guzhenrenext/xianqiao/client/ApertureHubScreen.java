package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.block.ApertureHubMenu;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.entry.XianqiaoUiProjection;
import com.Kizunad.guzhenrenext.xianqiao.service.SpiritUnlockService;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 仙窍中枢管理界面（Aperture Hub Screen）。
 * <p>
 * 基于 TinyUI 的多 Tab 容器界面，整合仙窍核心的全部信息展示：
 * 1) 总览 Tab：仙窍 chunk 边界、跨度、时间倍率、层级、冻结状态、灾劫倒计时、好感度；
 * 2) 地灵 Tab：好感度、转数、当前阶段；
 * 3) 资源 Tab：引导玩家查看资源控制器方块；
 * 4) 灾劫 Tab：灾劫倒计时和状态；
 * 5) 道痕 Tab：道痕系统概述和命令提示。
 * </p>
 * <p>
 * 所有数据通过 {@link ApertureHubMenu} 的 ContainerData 从服务端同步，
 * 界面每 tick 刷新一次展示数据。
 * </p>
 */
public class ApertureHubScreen extends TinyUIContainerScreen<ApertureHubMenu> {

    private static final int WINDOW_WIDTH = 280;
    private static final int WINDOW_HEIGHT = 200;
    private static final int MAIN_X = 0;
    private static final int MAIN_Y = 0;
    private static final int MAIN_PADDING = 8;
    private static final int TAB_BAR_Y = 8;
    private static final int TAB_BUTTON_HEIGHT = 20;
    private static final int TAB_BUTTON_GAP = 4;
    private static final int CONTENT_Y = 34;
    private static final int CONTENT_PADDING = 8;
    private static final int LINE_HEIGHT = 14;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int FIRST_LINE_Y = 8;
    private static final double PERCENT_BASE = 100.0D;
    private static final long TICKS_PER_SECOND = 20L;
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long HOURS_PER_DAY = 24L;
    private static final int TAB_OVERVIEW = 0;
    private static final int TAB_SPIRIT = 1;
    private static final int TAB_RESOURCE = 2;
    private static final int TAB_TRIBULATION = 3;
    private static final int TAB_DAOMARK = 4;
    private static final int TAB_ASCENSION = 5;
    private static final int TAB_COUNT = 6;
    private static final int DAOMARK_TYPE_COUNT = DaoType.values().length;
    private static final int ASCENSION_ENTRY_BUTTON_WIDTH = 96;
    private static final int ASCENSION_ENTRY_BUTTON_HEIGHT = 18;
    private static final int ASCENSION_SUMMARY_MAX_LINES = 5;
    private static final int BUTTON_DISABLED_COLOR = 0xFF9E9E9E;
    private static final int ASCENSION_RISK_LINE_MULTIPLIER = 4;
    private static final int ASCENSION_BLOCKER_LINE_MULTIPLIER = 5;
    private static final int ASCENSION_QI_LINE_MULTIPLIER = 6;
    private static final int ASCENSION_PLACEHOLDER_LINE_MULTIPLIER = 7;
    private static final int ASCENSION_BUTTON_LINE_MULTIPLIER = 10;
    private static final int ASCENSION_PLACEHOLDER_LINE_COUNT = 3;

    private final Theme theme;
    private final List<UIElement> tabPanels;
    private final List<Button> tabButtons;

    private Label overviewBoundaryLabel;
    private Label overviewChunkSpanLabel;
    private Label overviewTimeSpeedLabel;
    private Label overviewTierLabel;
    private Label overviewFrozenLabel;
    private Label overviewTribulationLabel;
    private Label overviewFavorabilityLabel;

    private Label spiritFavorabilityLabel;
    private Label spiritTierLabel;
    private Label spiritStageLabel;

    private Label tribulationCountdownLabel;
    private Label tribulationStatusLabel;

    private Label ascensionBannerLabel;
    private Label ascensionRiskLabel;
    private Label ascensionBlockerLabel;
    private Label ascensionQiLabel;
    private Label ascensionPlaceholderLabel;
    private Button ascensionEntryButton;

    public ApertureHubScreen(
        ApertureHubMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
        this.tabPanels = new ArrayList<>();
        this.tabButtons = new ArrayList<>();
    }

    @Override
    protected void initUI(final UIRoot root) {
        // 清理旧的组件引用，防止 resize 时列表无限增长导致 switchTab 逻辑错误
        this.tabPanels.clear();
        this.tabButtons.clear();

        // 动态计算居中坐标，确保在窗口大小改变时界面始终居中
        int rootX = (this.width - WINDOW_WIDTH) / 2;
        int rootY = (this.height - WINDOW_HEIGHT) / 2;
        // 更新视口大小以匹配当前屏幕
        root.setViewport(this.width, this.height);

        UIElement main = new UIElement() { };
        main.setFrame(rootX, rootY, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(main);

        int availableWidth = WINDOW_WIDTH - MAIN_PADDING * 2;
        int buttonWidth = (availableWidth - TAB_BUTTON_GAP * (TAB_COUNT - 1)) / TAB_COUNT;

        createTabButton(main, TAB_OVERVIEW, "总览", MAIN_PADDING, TAB_BAR_Y, buttonWidth);
        createTabButton(
            main,
            TAB_SPIRIT,
            "地灵",
            MAIN_PADDING + (buttonWidth + TAB_BUTTON_GAP),
            TAB_BAR_Y,
            buttonWidth
        );
        createTabButton(
            main,
            TAB_RESOURCE,
            "资源",
            MAIN_PADDING + (buttonWidth + TAB_BUTTON_GAP) * TAB_RESOURCE,
            TAB_BAR_Y,
            buttonWidth
        );
        createTabButton(
            main,
            TAB_TRIBULATION,
            "灾劫",
            MAIN_PADDING + (buttonWidth + TAB_BUTTON_GAP) * TAB_TRIBULATION,
            TAB_BAR_Y,
            buttonWidth
        );
        createTabButton(
            main,
            TAB_DAOMARK,
            "道痕",
            MAIN_PADDING + (buttonWidth + TAB_BUTTON_GAP) * TAB_DAOMARK,
            TAB_BAR_Y,
            buttonWidth
        );
        createTabButton(
            main,
            TAB_ASCENSION,
            "升仙",
            MAIN_PADDING + (buttonWidth + TAB_BUTTON_GAP) * TAB_ASCENSION,
            TAB_BAR_Y,
            buttonWidth
        );

        int contentWidth = WINDOW_WIDTH - CONTENT_PADDING * 2;
        int contentHeight = WINDOW_HEIGHT - CONTENT_Y - CONTENT_PADDING;

        UIElement overviewPanel = createOverviewPanel(contentWidth, contentHeight);
        overviewPanel.setFrame(CONTENT_PADDING, CONTENT_Y, contentWidth, contentHeight);
        main.addChild(overviewPanel);
        tabPanels.add(overviewPanel);

        UIElement spiritPanel = createSpiritPanel(contentWidth, contentHeight);
        spiritPanel.setFrame(CONTENT_PADDING, CONTENT_Y, contentWidth, contentHeight);
        main.addChild(spiritPanel);
        tabPanels.add(spiritPanel);

        UIElement resourcePanel = createResourcePanel(contentWidth, contentHeight);
        resourcePanel.setFrame(CONTENT_PADDING, CONTENT_Y, contentWidth, contentHeight);
        main.addChild(resourcePanel);
        tabPanels.add(resourcePanel);

        UIElement tribulationPanel = createTribulationPanel(contentWidth, contentHeight);
        tribulationPanel.setFrame(CONTENT_PADDING, CONTENT_Y, contentWidth, contentHeight);
        main.addChild(tribulationPanel);
        tabPanels.add(tribulationPanel);

        UIElement daomarkPanel = createDaomarkPanel(contentWidth, contentHeight);
        daomarkPanel.setFrame(CONTENT_PADDING, CONTENT_Y, contentWidth, contentHeight);
        main.addChild(daomarkPanel);
        tabPanels.add(daomarkPanel);

        UIElement ascensionPanel = createAscensionPanel(contentWidth, contentHeight);
        ascensionPanel.setFrame(CONTENT_PADDING, CONTENT_Y, contentWidth, contentHeight);
        main.addChild(ascensionPanel);
        tabPanels.add(ascensionPanel);

        // 切换到总览页并刷新数据
        switchTab(TAB_OVERVIEW);
        updateOverviewTabData();
        updateSpiritTabData();
        updateTribulationTabData();
        updateAscensionProjectionData();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (
            overviewBoundaryLabel == null
                || spiritStageLabel == null
                || tribulationStatusLabel == null
                || ascensionBannerLabel == null
        ) {
            return;
        }
        updateOverviewTabData();
        updateSpiritTabData();
        updateTribulationTabData();
        updateAscensionProjectionData();
    }

    @Override
    protected double getUiScale() {
        return 1.0;
    }

    private void switchTab(final int tabIndex) {
        for (int index = 0; index < tabPanels.size(); index++) {
            tabPanels.get(index).setVisible(index == tabIndex);
        }
        for (int index = 0; index < tabButtons.size(); index++) {
            tabButtons.get(index).setEnabled(index != tabIndex);
        }
    }

    private void createTabButton(
        final UIElement parent,
        final int tabIndex,
        final String text,
        final int x,
        final int y,
        final int width
    ) {
        Button button = new Button(text, theme);
        button.setFrame(x, y, width, TAB_BUTTON_HEIGHT);
        button.setOnClick(() -> switchTab(tabIndex));
        parent.addChild(button);
        tabButtons.add(button);
    }

    private UIElement createOverviewPanel(final int width, final int height) {
        UIElement panel = new UIElement() { };

        overviewBoundaryLabel = createDataLabel(panel, FIRST_LINE_Y, width);
        overviewChunkSpanLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT, width);
        overviewTimeSpeedLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_RESOURCE, width);
        overviewTierLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_TRIBULATION, width);
        overviewFrozenLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_DAOMARK, width);
        overviewTribulationLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_COUNT, width);
        overviewFavorabilityLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * (TAB_COUNT + 1), width);

        panel.setFrame(MAIN_X, MAIN_Y, width, height);
        return panel;
    }

    private UIElement createSpiritPanel(final int width, final int height) {
        UIElement panel = new UIElement() { };
        spiritFavorabilityLabel = createDataLabel(panel, FIRST_LINE_Y, width);
        spiritTierLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT, width);
        spiritStageLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_RESOURCE, width);
        panel.setFrame(MAIN_X, MAIN_Y, width, height);
        return panel;
    }

    private UIElement createResourcePanel(final int width, final int height) {
        UIElement panel = new UIElement() { };

        Label titleLabel = createDataLabel(panel, FIRST_LINE_Y, width);
        titleLabel.setText(Component.literal("§e资源控制器状态"));

        Label statusLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT, width);
        statusLabel.setText(Component.literal("资源系统状态：请查看资源控制器方块"));

        Label hintLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_RESOURCE, width);
        hintLabel.setText(Component.literal("请在仙窍范围内放置资源控制器方块"));

        Label detailLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_TRIBULATION, width);
        detailLabel.setText(Component.literal("右键资源控制器查看详细产出与效率信息"));

        Label auraHintLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_DAOMARK, width);
        auraHintLabel.setText(Component.literal("时道灵气会直接影响资源产出效率"));

        panel.setFrame(MAIN_X, MAIN_Y, width, height);
        return panel;
    }

    private UIElement createTribulationPanel(final int width, final int height) {
        UIElement panel = new UIElement() { };

        Label titleLabel = createDataLabel(panel, FIRST_LINE_Y, width);
        titleLabel.setText(Component.literal("§e灾劫信息"));

        tribulationCountdownLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT, width);
        tribulationStatusLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_RESOURCE, width);

        Label tipLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_TRIBULATION, width);
        tipLabel.setText(Component.literal("提示：灾劫详情可通过灾劫管理器日志查看"));

        panel.setFrame(MAIN_X, MAIN_Y, width, height);
        return panel;
    }

    private UIElement createDaomarkPanel(final int width, final int height) {
        UIElement panel = new UIElement() { };

        Label titleLabel = createDataLabel(panel, FIRST_LINE_Y, width);
        titleLabel.setText(Component.literal("§e道痕灵气信息"));

        Label typeLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT, width);
        typeLabel.setText(Component.literal("道痕灵气系统目前包含 " + DAOMARK_TYPE_COUNT + " 种道类型"));

        Label commandLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_RESOURCE, width);
        commandLabel.setText(Component.literal("详细灵气分布请使用 §b/xianqiao daomark§r 命令查看"));

        Label detectorLabel = createDataLabel(panel, FIRST_LINE_Y + LINE_HEIGHT * TAB_TRIBULATION, width);
        detectorLabel.setText(Component.literal("或使用道痕检测器在仙窍范围内进行现场检测"));

        panel.setFrame(MAIN_X, MAIN_Y, width, height);
        return panel;
    }

    private UIElement createAscensionPanel(final int width, final int height) {
        UIElement panel = new UIElement() { };

        Label titleLabel = createDataLabel(panel, FIRST_LINE_Y, width);
        titleLabel.setText(Component.literal("§e升仙准备入口"));

        ascensionBannerLabel = createMultilineLabel(
            panel,
            FIRST_LINE_Y + LINE_HEIGHT,
            width,
            ASCENSION_SUMMARY_MAX_LINES
        );
        ascensionRiskLabel = createDataLabel(
            panel,
            FIRST_LINE_Y + LINE_HEIGHT * ASCENSION_RISK_LINE_MULTIPLIER,
            width
        );
        ascensionBlockerLabel = createDataLabel(
            panel,
            FIRST_LINE_Y + LINE_HEIGHT * ASCENSION_BLOCKER_LINE_MULTIPLIER,
            width
        );
        ascensionQiLabel = createDataLabel(
            panel,
            FIRST_LINE_Y + LINE_HEIGHT * ASCENSION_QI_LINE_MULTIPLIER,
            width
        );
        ascensionPlaceholderLabel = createMultilineLabel(
            panel,
            FIRST_LINE_Y + LINE_HEIGHT * ASCENSION_PLACEHOLDER_LINE_MULTIPLIER,
            width,
            ASCENSION_PLACEHOLDER_LINE_COUNT
        );

        ascensionEntryButton = new Button(Component.literal("发起升仙冲关"), theme);
        ascensionEntryButton.setFrame(
            FIRST_LINE_Y,
            FIRST_LINE_Y + LINE_HEIGHT * ASCENSION_BUTTON_LINE_MULTIPLIER,
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
        panel.addChild(ascensionEntryButton);

        panel.setFrame(MAIN_X, MAIN_Y, width, height);
        return panel;
    }

    private Label createDataLabel(final UIElement panel, final int y, final int width) {
        Label label = new Label(Component.empty(), theme);
        label.setFrame(FIRST_LINE_Y, y, width - FIRST_LINE_Y * 2, LINE_HEIGHT);
        label.setColor(TEXT_COLOR);
        panel.addChild(label);
        return label;
    }

    private Label createMultilineLabel(final UIElement panel, final int y, final int width, final int lines) {
        Label label = new Label(Component.empty(), theme);
        label.setFrame(FIRST_LINE_Y, y, width - FIRST_LINE_Y * 2, LINE_HEIGHT * lines);
        label.setColor(TEXT_COLOR);
        panel.addChild(label);
        return label;
    }

    private void updateOverviewTabData() {
        overviewBoundaryLabel.setText(
            Component.literal(
                "仙窍 chunk 边界：X["
                    + menu.getMinChunkX()
                    + ", "
                    + menu.getMaxChunkX()
                    + "] Z["
                    + menu.getMinChunkZ()
                    + ", "
                    + menu.getMaxChunkZ()
                    + "]"
            )
        );
        overviewChunkSpanLabel.setText(
            Component.literal("chunk 跨度：X " + menu.getChunkSpanX() + "，Z " + menu.getChunkSpanZ())
        );
        overviewTimeSpeedLabel.setText(
            Component.literal("时间倍率：" + formatTimeSpeed(menu.getTimeSpeedPercent()) + "x")
        );
        overviewTierLabel.setText(Component.literal("层级（转数）：" + menu.getTier()));
        overviewFrozenLabel.setText(
            Component.literal("状态：" + (menu.isFrozen() ? "冻结" : "运行中"))
        );
        overviewTribulationLabel.setText(
            Component.literal("灾劫倒计时：" + formatTribulationTime(menu.getTribulationTick()))
        );
        overviewFavorabilityLabel.setText(
            Component.literal("好感度：" + formatFavorability(menu.getFavorabilityPercent()))
        );
        XianqiaoUiProjection.ProjectionSnapshot projection = menu.getUiProjection();
        overviewTribulationLabel.setText(
            Component.literal(
                "灾劫倒计时："
                    + formatTribulationTime(menu.getTribulationTick())
                    + "；升仙阶段："
                    + projection.homeBanner().stageLabel()
            )
        );
        overviewFavorabilityLabel.setText(
            Component.literal(
                "好感度："
                    + formatFavorability(menu.getFavorabilityPercent())
                    + "；主阻塞："
                    + projection.homeBanner().primaryBlocker()
            )
        );
    }

    private void updateSpiritTabData() {
        int tier = menu.getTier();
        int stageIndex = SpiritUnlockService.computeStage(
            tier,
            (float) (menu.getFavorabilityPercent() / PERCENT_BASE)
        );
        spiritFavorabilityLabel.setText(
            Component.literal("好感度：" + formatFavorability(menu.getFavorabilityPercent()))
        );
        spiritTierLabel.setText(Component.literal("转数：" + tier));
        spiritStageLabel.setText(
            Component.literal(
                "当前阶段："
                    + stageIndex
                    + " - "
                    + SpiritUnlockService.getStageDisplayName(stageIndex)
                    + "；升仙："
                    + menu.getUiProjection().spiritIncubation().stageLabel()
            )
        );
    }

    private void updateTribulationTabData() {
        long ticks = menu.getTribulationTick();
        boolean isPeacePeriod = ticks > 0;
        tribulationCountdownLabel.setText(Component.literal("距离下次灾劫：" + formatTribulationTime(ticks)));
        tribulationStatusLabel.setText(
            Component.literal(
                "灾劫状态："
                    + (isPeacePeriod ? "平静期" : "灾劫进行中")
                    + "；险兆："
                    + menu.getUiProjection().tribulationRisk().riskLabel()
            )
        );
    }

    private void updateAscensionProjectionData() {
        XianqiaoUiProjection.ProjectionSnapshot projection = menu.getUiProjection();
        ascensionBannerLabel.setText(
            Component.literal(
                "阶段："
                    + projection.ascensionPreparation().stageLabel()
                    + "\n下一步："
                    + projection.entryButtonLabel()
                    + "\n气机："
                    + projection.ascensionPreparation().riskLabel()
            )
        );
        ascensionRiskLabel.setText(Component.literal("风险摘要：" + projection.ascensionPreparation().riskLabel()));
        ascensionBlockerLabel.setText(
            Component.literal("主阻塞：" + projection.ascensionPreparation().primaryBlocker())
        );
        ascensionQiLabel.setText(
            Component.literal(
                "三气/平衡：天"
                    + menu.getHeavenQiScorePercent()
                    + "% 地"
                    + menu.getEarthQiScorePercent()
                    + "% 人"
                    + menu.getHumanQiScorePercent()
                    + "% 平衡"
                    + menu.getBalanceScorePercent()
                    + "%"
            )
        );
        ascensionPlaceholderLabel.setText(
            Component.literal(
                "道痕检测器："
                    + projection.detectorRoute().displayState()
                    + "（"
                    + projection.detectorRoute().displayTargetPath()
                    + "）"
                    + "\n详细分布："
                    + projection.daomarkDistributionRoute().displayState()
                    + "（"
                    + projection.daomarkDistributionRoute().displayTargetPath()
                    + "）"
                    + "\n目标："
                    + projection.detectorRoute().displayTargetPath()
            )
        );
        ascensionEntryButton.setText(Component.literal(projection.entryButtonLabel()));
        ascensionEntryButton.setEnabled(projection.gameplayEntryAvailable());
        if (!projection.gameplayEntryAvailable()) {
            ascensionPlaceholderLabel.setColor(BUTTON_DISABLED_COLOR);
        } else {
            ascensionPlaceholderLabel.setColor(TEXT_COLOR);
        }
    }

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
}
