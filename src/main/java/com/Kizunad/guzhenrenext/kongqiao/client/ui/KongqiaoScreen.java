package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.client.KongqiaoClientProjectionCache;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenu;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection;
import com.Kizunad.guzhenrenext.network.ServerboundKongqiaoActionPayload;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.ContainerUI;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 空窍 TinyUI 界面。
 */
public class KongqiaoScreen extends TinyUIContainerScreen<KongqiaoMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 2;
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_COUNT = 4;
    private static final int SECTION_GAP = 12;
    private static final int SCROLLBAR_ALLOWANCE = 14;
    private static final int HINT_MARGIN_TOP = 4;
    private static final int HINT_LABEL_HEIGHT = 12;
    private static final int STATUS_MARGIN_TOP = 8;
    private static final int STATUS_LABEL_HEIGHT = 12;
    private static final int STATUS_LABEL_GAP = 2;
    private static final int STATUS_LABEL_COUNT = 5;
    private static final int STATUS_INDEX_TOTAL = 0;
    private static final int STATUS_INDEX_BREAKDOWN = 1;
    private static final int STATUS_INDEX_SUPPLEMENTAL = 2;
    private static final int STATUS_INDEX_OVERLOAD = 3;
    private static final int STATUS_INDEX_BLOCKED = 4;
    private static final int STATUS_SECTION_HEIGHT =
        STATUS_LABEL_COUNT * STATUS_LABEL_HEIGHT
            + (STATUS_LABEL_COUNT - 1) * STATUS_LABEL_GAP;
    private static final int MAIN_PANEL_WIDTH = 600;
    private static final int MAIN_PANEL_PADDING = 16;
    private static final int GRID_PANEL_MIN_WIDTH = 550;
    private static final int GRID_PANEL_PADDING = 12;
    private static final int BUTTON_ROW_MARGIN = 18;
    private static final int PLAYER_PANEL_WIDTH = 450;
    private static final int PLAYER_PANEL_PADDING = 12;
    private static final int PLAYER_TITLE_HEIGHT = 14;
    private static final int PANEL_GAP = 20;

    private final Theme theme = Theme.vanilla();
    private Label pressureTotalLabel;
    private Label pressureBreakdownLabel;
    private Label pressureSupplementLabel;
    private Label overloadLabel;
    private Label blockedLabel;

    public KongqiaoScreen(
        KongqiaoMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    protected boolean shouldEnforceMenuBinding() {
        return true;
    }

    @Override
    protected double getUiScale() {
        return com.Kizunad.guzhenrenext.config.ClientConfig.INSTANCE.kongQiaoUiScale.get();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (
            pressureTotalLabel == null
                || pressureBreakdownLabel == null
                || pressureSupplementLabel == null
                || overloadLabel == null
                || blockedLabel == null
        ) {
            return;
        }
        applyProjectionState(
            buildProjectionPanelState(
                KongqiaoClientProjectionCache.getCurrentProjection()
            )
        );
    }

    @Override
    protected void initUI(UIRoot root) {
        int visibleRows = Math.max(menu.getVisibleRows(), 1);
        int totalSlots = menu.getTotalSlots();

        int gridWidth =
            KongqiaoConstants.COLUMNS * SLOT_SIZE +
            (KongqiaoConstants.COLUMNS - 1) * GRID_GAP +
            GRID_PADDING * 2;
        int viewportWidth = gridWidth + SCROLLBAR_ALLOWANCE;
        int viewportHeight =
            visibleRows * SLOT_SIZE +
            Math.max(0, visibleRows - 1) * GRID_GAP +
            GRID_PADDING * 2;

        UIElement kongqiaoGrid = ContainerUI.scrollableGrid(
            0,
            totalSlots,
            KongqiaoConstants.COLUMNS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        kongqiaoGrid.setFrame(0, 0, viewportWidth, viewportHeight);

        Label title = new Label(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_TITLE),
            theme
        );
        Label hint = new Label(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_HINT),
            theme
        );
        pressureTotalLabel = new Label(Component.literal(""), theme);
        pressureBreakdownLabel = new Label(Component.literal(""), theme);
        pressureSupplementLabel = new Label(Component.literal(""), theme);
        overloadLabel = new Label(Component.literal(""), theme);
        blockedLabel = new Label(Component.literal(""), theme);

        Button expandButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_BUTTON_EXPAND),
            theme
        );
        expandButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.EXPAND)
        );
        Button feedButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_FEED_BUTTON),
            theme
        );
        feedButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_FEED)
        );
        Button attackButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_BUTTON_ATTACK),
            theme
        );
        attackButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_ATTACK)
        );

        Button forgeButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_BUTTON_FORGE),
            theme
        );
        forgeButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_FORGE)
        );

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            menu.getTotalSlots(),
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        KongqiaoLayout layout = calculateLayout(
            viewportWidth,
            viewportHeight,
            playerGrid.getHeight()
        );
        UIElement window = createWindow(root, layout.windowHeight());
        buildMainPanel(
            window,
            layout,
            kongqiaoGrid,
            title,
            hint,
            new Button[] { expandButton, feedButton, attackButton, forgeButton }
        );
        buildPlayerPanel(window, layout, playerGrid);
        applyProjectionState(
            buildProjectionPanelState(
                KongqiaoClientProjectionCache.getCurrentProjection()
            )
        );
    }

    private void sendAction(ServerboundKongqiaoActionPayload.Action action) {
        PacketDistributor.sendToServer(
            new ServerboundKongqiaoActionPayload(action)
        );
    }

    private KongqiaoLayout calculateLayout(
        int viewportWidth,
        int viewportHeight,
        int playerGridHeight
    ) {
        int containerInnerWidth = Math.max(
            viewportWidth,
            GRID_PANEL_MIN_WIDTH - GRID_PANEL_PADDING * 2
        );
        int availableMainWidth = MAIN_PANEL_WIDTH - MAIN_PANEL_PADDING * 2;
        int containerWidth = Math.min(
            containerInnerWidth + GRID_PANEL_PADDING * 2,
            availableMainWidth
        );
        int containerInnerHeight =
            viewportHeight
                + HINT_MARGIN_TOP
                + HINT_LABEL_HEIGHT
                + STATUS_MARGIN_TOP
                + STATUS_SECTION_HEIGHT;
        int containerHeight = containerInnerHeight + GRID_PANEL_PADDING * 2;
        int mainPanelHeight =
            MAIN_PANEL_PADDING +
            TITLE_HEIGHT +
            SECTION_GAP +
            containerHeight +
            BUTTON_ROW_MARGIN +
            BUTTON_HEIGHT +
            MAIN_PANEL_PADDING;
        int playerPanelHeight =
            PLAYER_PANEL_PADDING +
            PLAYER_TITLE_HEIGHT +
            SECTION_GAP +
            playerGridHeight +
            PLAYER_PANEL_PADDING;
        int windowHeight = mainPanelHeight + PANEL_GAP + playerPanelHeight;
        return new KongqiaoLayout(
            viewportWidth,
            viewportHeight,
            containerInnerWidth,
            containerWidth,
            containerHeight,
            availableMainWidth,
            mainPanelHeight,
            playerPanelHeight,
            windowHeight
        );
    }

    private UIElement createWindow(UIRoot root, int windowHeight) {
        UIElement window = new UIElement() {};
        window.setFrame(0, 0, MAIN_PANEL_WIDTH, windowHeight);
        Anchor.apply(
            window,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                MAIN_PANEL_WIDTH,
                windowHeight,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(window);
        return window;
    }

    private void buildMainPanel(
        UIElement window,
        KongqiaoLayout layout,
        UIElement kongqiaoGrid,
        Label title,
        Label hint,
        Button[] buttons
    ) {
        SolidPanel mainPanel = new SolidPanel(theme);
        mainPanel.setFrame(0, 0, MAIN_PANEL_WIDTH, layout.mainPanelHeight());
        window.addChild(mainPanel);

        title.setFrame(
            MAIN_PANEL_PADDING,
            MAIN_PANEL_PADDING,
            MAIN_PANEL_WIDTH - MAIN_PANEL_PADDING * 2,
            TITLE_HEIGHT
        );
        mainPanel.addChild(title);

        SolidPanel containerPanel = new SolidPanel(theme);
        int containerX =
            MAIN_PANEL_PADDING +
            (layout.availableMainWidth() - layout.containerWidth()) / 2;
        int containerY = MAIN_PANEL_PADDING + TITLE_HEIGHT + SECTION_GAP;
        containerPanel.setFrame(
            containerX,
            containerY,
            layout.containerWidth(),
            layout.containerHeight()
        );
        mainPanel.addChild(containerPanel);

        int gridOffsetX =
            GRID_PANEL_PADDING +
            (layout.containerInnerWidth() - layout.viewportWidth()) / 2;
        kongqiaoGrid.setFrame(
            gridOffsetX,
            GRID_PANEL_PADDING,
            layout.viewportWidth(),
            layout.viewportHeight()
        );
        containerPanel.addChild(kongqiaoGrid);

        hint.setFrame(
            GRID_PANEL_PADDING,
            kongqiaoGrid.getY() + kongqiaoGrid.getHeight() + HINT_MARGIN_TOP,
            layout.containerInnerWidth(),
            HINT_LABEL_HEIGHT
        );
        containerPanel.addChild(hint);

        int statusY = hint.getY() + hint.getHeight() + STATUS_MARGIN_TOP;
        pressureTotalLabel.setFrame(
            GRID_PANEL_PADDING,
            resolveStatusLabelY(statusY, STATUS_INDEX_TOTAL),
            layout.containerInnerWidth(),
            STATUS_LABEL_HEIGHT
        );
        containerPanel.addChild(pressureTotalLabel);

        pressureBreakdownLabel.setFrame(
            GRID_PANEL_PADDING,
            resolveStatusLabelY(statusY, STATUS_INDEX_BREAKDOWN),
            layout.containerInnerWidth(),
            STATUS_LABEL_HEIGHT
        );
        containerPanel.addChild(pressureBreakdownLabel);

        pressureSupplementLabel.setFrame(
            GRID_PANEL_PADDING,
            resolveStatusLabelY(statusY, STATUS_INDEX_SUPPLEMENTAL),
            layout.containerInnerWidth(),
            STATUS_LABEL_HEIGHT
        );
        containerPanel.addChild(pressureSupplementLabel);

        overloadLabel.setFrame(
            GRID_PANEL_PADDING,
            resolveStatusLabelY(statusY, STATUS_INDEX_OVERLOAD),
            layout.containerInnerWidth(),
            STATUS_LABEL_HEIGHT
        );
        containerPanel.addChild(overloadLabel);

        blockedLabel.setFrame(
            GRID_PANEL_PADDING,
            resolveStatusLabelY(statusY, STATUS_INDEX_BLOCKED),
            layout.containerInnerWidth(),
            STATUS_LABEL_HEIGHT
        );
        containerPanel.addChild(blockedLabel);

        int buttonRowWidth =
            BUTTON_WIDTH * BUTTON_COUNT + BUTTON_GAP * (BUTTON_COUNT - 1);
        int buttonRowX = (MAIN_PANEL_WIDTH - buttonRowWidth) / 2;
        int buttonRowY =
            containerY + layout.containerHeight() + BUTTON_ROW_MARGIN;
        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            button.setFrame(
                buttonRowX + i * (BUTTON_WIDTH + BUTTON_GAP),
                buttonRowY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
            );
            mainPanel.addChild(button);
        }
    }

    private void buildPlayerPanel(
        UIElement window,
        KongqiaoLayout layout,
        UIElement playerGrid
    ) {
        SolidPanel playerPanel = new SolidPanel(theme);
        int playerPanelX = (MAIN_PANEL_WIDTH - PLAYER_PANEL_WIDTH) / 2;
        playerPanel.setFrame(
            playerPanelX,
            layout.mainPanelHeight() + PANEL_GAP,
            PLAYER_PANEL_WIDTH,
            layout.playerPanelHeight()
        );
        window.addChild(playerPanel);

        Label playerLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_PLAYER_INVENTORY),
            theme
        );
        playerLabel.setFrame(
            PLAYER_PANEL_PADDING,
            PLAYER_PANEL_PADDING,
            PLAYER_PANEL_WIDTH - PLAYER_PANEL_PADDING * 2,
            PLAYER_TITLE_HEIGHT
        );
        playerPanel.addChild(playerLabel);

        int innerPlayerWidth = PLAYER_PANEL_WIDTH - PLAYER_PANEL_PADDING * 2;
        int playerGridX =
            PLAYER_PANEL_PADDING +
            (innerPlayerWidth - playerGrid.getWidth()) / 2;
        playerGrid.setFrame(
            playerGridX,
            playerLabel.getY() + PLAYER_TITLE_HEIGHT + SECTION_GAP,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        playerPanel.addChild(playerGrid);
    }

    private static int resolveStatusLabelY(
        final int statusStartY,
        final int lineIndex
    ) {
        return statusStartY + (STATUS_LABEL_HEIGHT + STATUS_LABEL_GAP) * lineIndex;
    }

    private void applyProjectionState(
        final KongqiaoUiProjectionText.ProjectionPanelState state
    ) {
        if (state == null) {
            return;
        }
        pressureTotalLabel.setText(Component.literal(state.totalLine()));
        pressureBreakdownLabel.setText(Component.literal(state.breakdownLine()));
        pressureSupplementLabel.setText(Component.literal(state.supplementalLine()));
        overloadLabel.setText(Component.literal(state.overloadLine()));
        blockedLabel.setText(Component.literal(state.blockedLine()));
    }

    static KongqiaoUiProjectionText.ProjectionPanelState buildProjectionPanelState(
        final KongqiaoPressureProjection projection
    ) {
        return KongqiaoUiProjectionText.buildProjectionPanelState(projection);
    }

    public static String buildBlockedSummaryText(
        final KongqiaoPressureProjection projection
    ) {
        return KongqiaoUiProjectionText.buildBlockedSummaryText(projection);
    }

    public static String overloadTierName(final int overloadTier) {
        return KongqiaoUiProjectionText.overloadTierName(overloadTier);
    }

    public static String formatPressureValue(final double value) {
        return KongqiaoUiProjectionText.formatPressureValue(value);
    }

    private record KongqiaoLayout(
        int viewportWidth,
        int viewportHeight,
        int containerInnerWidth,
        int containerWidth,
        int containerHeight,
        int availableMainWidth,
        int mainPanelHeight,
        int playerPanelHeight,
        int windowHeight
    ) {}
}

final class KongqiaoUiProjectionText {

    private static final int OVERLOAD_TIER_STABLE = 0;
    private static final int OVERLOAD_TIER_TENSE = 1;
    private static final int OVERLOAD_TIER_OVERLOADED = 2;
    private static final int OVERLOAD_TIER_UNCONTROLLED = 3;

    private KongqiaoUiProjectionText() {}

    static ProjectionPanelState buildProjectionPanelState(
        final KongqiaoPressureProjection projection
    ) {
        final KongqiaoPressureProjection safeProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
        return new ProjectionPanelState(
            "总压力/承压上限: "
                + formatPressureValue(safeProjection.totalPressure())
                + " / "
                + formatPressureValue(safeProjection.pressureCap()),
            "构成: 驻留 "
                + formatPressureValue(safeProjection.residentPressure())
                + " | 被动 "
                + formatPressureValue(safeProjection.passivePressure())
                + " | 轮盘预载 "
                + formatPressureValue(safeProjection.wheelReservePressure()),
            "附加: 爆发 "
                + formatPressureValue(safeProjection.burstPressure())
                + " | 疲劳债 "
                + formatPressureValue(safeProjection.fatigueDebt()),
            "超压档位: "
                + overloadTierName(safeProjection.overloadTier())
                + " | 强制熄火 "
                + safeProjection.forcedDisabledCount()
                + " | 封槽 "
                + safeProjection.sealedSlotCount(),
            buildBlockedSummaryText(safeProjection)
        );
    }

    static String buildBlockedSummaryText(
        final KongqiaoPressureProjection projection
    ) {
        final KongqiaoPressureProjection safeProjection = projection == null
            ? KongqiaoPressureProjection.empty()
            : projection;
        if ("passive_overload".equals(safeProjection.blockedReason())) {
            if (safeProjection.forcedDisabledCount() > 0) {
                return "阻断摘要: 被动因超压被强制停用 "
                    + safeProjection.forcedDisabledCount()
                    + " 项";
            }
            return "阻断摘要: 被动因超压被空窍强制停用";
        }
        if (safeProjection.overloadTier() >= OVERLOAD_TIER_OVERLOADED) {
            if (safeProjection.sealedSlotCount() > 0) {
                return "阻断摘要: 当前已"
                    + overloadTierName(safeProjection.overloadTier())
                    + "，新增轮盘会被拒绝，且已有 "
                    + safeProjection.sealedSlotCount()
                    + " 个封槽";
            }
            return "阻断摘要: 当前已"
                + overloadTierName(safeProjection.overloadTier())
                + "，新增轮盘会被拒绝，高耗主动可能提示“当前空窍压力过高”";
        }
        if (safeProjection.overloadTier() == OVERLOAD_TIER_TENSE) {
            return "阻断摘要: 当前紧绷，继续催动主动很容易踏入超压";
        }
        return "阻断摘要: 当前未出现超压阻断";
    }

    static String overloadTierName(final int overloadTier) {
        return switch (Math.max(0, overloadTier)) {
            case OVERLOAD_TIER_STABLE -> "稳定";
            case OVERLOAD_TIER_TENSE -> "紧绷";
            case OVERLOAD_TIER_OVERLOADED -> "超压";
            case OVERLOAD_TIER_UNCONTROLLED -> "失控";
            default -> "崩窍边缘";
        };
    }

    static String formatPressureValue(final double value) {
        return String.format(Locale.ROOT, "%.1f", Math.max(0.0D, value));
    }

    record ProjectionPanelState(
        String totalLine,
        String breakdownLine,
        String supplementalLine,
        String overloadLine,
        String blockedLine
    ) {}
}
