package com.Kizunad.guzhenrenext.kongqiao.flyingsword.training;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.SolidPanel;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.FlyingSwordHelpScreen;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.UISlot;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 飞剑修炼屏幕。
 * 展示挂机修炼进度、经验与燃料状态。
 */
public class FlyingSwordTrainingScreen extends TinyUIContainerScreen<FlyingSwordTrainingMenu> {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 320;
    private static final int PANEL_PADDING = 16;
    private static final int TITLE_HEIGHT = 16;
    private static final int SECTION_GAP = 12;
    private static final int SLOT_SIZE = 18;
    private static final int LABEL_HEIGHT = 14;
    private static final int PROGRESS_BAR_HEIGHT = 16;
    private static final int PLAYER_SLOT_GAP = 2;
    private static final int PLAYER_SLOT_PADDING = 0;
    private static final int PLAYER_INV_START_INDEX = 2; // Menu 中 slot 0,1 是输入/燃料，2开始是玩家背包
    private static final int ELEMENT_SPACING = 4;
    private static final int HELP_BUTTON_WIDTH = 20;
    private static final int SECTION_EXTRA_GAP = 8;
    private static final int PROGRESS_BAR_SEGMENTS = 20;
    private static final int SLOT_GAP = 40;
    private static final int HINT_WIDTH_MULTIPLIER = 3;
    private static final int PERCENT_100 = 100;
    private static final int PLAYER_INV_BOTTOM_OFFSET = 100;

    private final Theme theme = Theme.vanilla();
    private Label fuelLabel;
    private Label expLabel;
    private Label statusLabel;

    public FlyingSwordTrainingScreen(
        FlyingSwordTrainingMenu menu,
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
        updateLabels();
    }

    @Override
    protected void initUI(UIRoot root) {
        UIElement window = new UIElement() {};
        window.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        Anchor.apply(
            window,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(window);

        SolidPanel background = new SolidPanel(theme);
        background.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.addChild(background);

        Label titleLabel = new Label(
            Component.literal("飞剑修炼"), // 暂用硬编码，建议后续加入 I18n
            theme
        );
        titleLabel.setFrame(
            PANEL_PADDING,
            PANEL_PADDING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            TITLE_HEIGHT
        );
        background.addChild(titleLabel);

        background.addChild(createHelpButton());

        int slotsY = PANEL_PADDING + TITLE_HEIGHT + SECTION_GAP;
        int centerX = WINDOW_WIDTH / 2;

        // 剑槽
        int swordSlotX = centerX - SLOT_SIZE - SLOT_GAP / 2;
        UISlot swordSlot = new UISlot(FlyingSwordTrainingMenu.SLOT_SWORD, theme);
        swordSlot.setFrame(swordSlotX, slotsY, SLOT_SIZE, SLOT_SIZE);
        background.addChild(swordSlot);

        Label swordHint = new Label(Component.literal("核心剑"), theme);
        swordHint.setFrame(
            swordSlotX,
            slotsY + SLOT_SIZE + ELEMENT_SPACING,
            SLOT_SIZE * HINT_WIDTH_MULTIPLIER,
            LABEL_HEIGHT
        );
        background.addChild(swordHint);

        // 燃料槽
        int fuelSlotX = centerX + SLOT_GAP / 2;
        UISlot fuelSlot = new UISlot(FlyingSwordTrainingMenu.SLOT_FUEL, theme);
        fuelSlot.setFrame(fuelSlotX, slotsY, SLOT_SIZE, SLOT_SIZE);
        background.addChild(fuelSlot);

        Label fuelHint = new Label(Component.literal("元石"), theme);
        fuelHint.setFrame(
            fuelSlotX,
            slotsY + SLOT_SIZE + ELEMENT_SPACING,
            SLOT_SIZE * HINT_WIDTH_MULTIPLIER,
            LABEL_HEIGHT
        );
        background.addChild(fuelHint);

        addStatusLabels(background, slotsY + SLOT_SIZE + LABEL_HEIGHT + SECTION_GAP);

        // 玩家背包
        addPlayerInventory(background);
    }

    private void addStatusLabels(SolidPanel background, int startY) {
        fuelLabel = new Label(Component.literal("燃料剩余: 0/0"), theme);
        fuelLabel.setFrame(
            PANEL_PADDING,
            startY,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            PROGRESS_BAR_HEIGHT
        );
        background.addChild(fuelLabel);

        expLabel = new Label(Component.literal("积累经验: 0"), theme);
        expLabel.setFrame(
            PANEL_PADDING,
            startY + PROGRESS_BAR_HEIGHT + ELEMENT_SPACING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            LABEL_HEIGHT
        );
        background.addChild(expLabel);

        statusLabel = new Label(Component.literal("等待中..."), theme);
        statusLabel.setFrame(
            PANEL_PADDING,
            startY + PROGRESS_BAR_HEIGHT + LABEL_HEIGHT + ELEMENT_SPACING * 2,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            LABEL_HEIGHT
        );
        background.addChild(statusLabel);
    }

    private void addPlayerInventory(SolidPanel background) {
        int playerInvY = WINDOW_HEIGHT - PLAYER_INV_BOTTOM_OFFSET - PANEL_PADDING; // 粗略定位到底部

        Label playerLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_PLAYER_INVENTORY),
            theme
        );
        playerLabel.setFrame(
            PANEL_PADDING,
            playerInvY - LABEL_HEIGHT - ELEMENT_SPACING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            LABEL_HEIGHT
        );
        background.addChild(playerLabel);

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            PLAYER_INV_START_INDEX,
            SLOT_SIZE,
            PLAYER_SLOT_GAP,
            PLAYER_SLOT_PADDING,
            theme
        );
        int playerGridX = (WINDOW_WIDTH - playerGrid.getWidth()) / 2;
        playerGrid.setFrame(
            playerGridX,
            playerInvY,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        background.addChild(playerGrid);
    }

    private void updateLabels() {
        if (fuelLabel == null || expLabel == null || statusLabel == null) {
            return;
        }

        int fuelTime = menu.getFuelTime();
        int maxFuelTime = menu.getMaxFuelTime();
        int exp = menu.getAccumulatedExp();

        int percent = 0;
        if (maxFuelTime > 0) {
            percent = (int) ((long) fuelTime * PERCENT_100 / maxFuelTime);
        }

        String progressBar = buildProgressBar(percent);
        fuelLabel.setText(Component.literal(
            "燃料: " + fuelTime + "/" + maxFuelTime + " " + progressBar
        ));

        expLabel.setText(Component.literal("积累经验: " + exp));

        if (fuelTime > 0) {
            statusLabel.setText(Component.literal("§a修炼中..."));
        } else {
            statusLabel.setText(Component.literal("§c缺少燃料或未开始"));
        }
    }

    private Button createHelpButton() {
        final Button helpButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.FORGE_BUTTON_HELP),
            theme
        );
        helpButton.setFrame(
            WINDOW_WIDTH - PANEL_PADDING - HELP_BUTTON_WIDTH,
            PANEL_PADDING,
            HELP_BUTTON_WIDTH,
            TITLE_HEIGHT
        );
        helpButton.setOnClick(() -> {
            final Minecraft mc = Minecraft.getInstance();
            mc.tell(() -> mc.setScreen(new FlyingSwordHelpScreen()));
        });
        return helpButton;
    }

    private String buildProgressBar(int percent) {
        int filled = (percent * PROGRESS_BAR_SEGMENTS) / PERCENT_100;
        int empty = PROGRESS_BAR_SEGMENTS - filled;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < filled; i++) {
            sb.append("|");
        }
        for (int i = 0; i < empty; i++) {
            sb.append(".");
        }
        sb.append("]");
        return sb.toString();
    }
}
