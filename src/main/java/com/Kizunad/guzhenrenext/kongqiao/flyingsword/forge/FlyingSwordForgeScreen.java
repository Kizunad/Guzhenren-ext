package com.Kizunad.guzhenrenext.kongqiao.flyingsword.forge;

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

public class FlyingSwordForgeScreen extends TinyUIContainerScreen<FlyingSwordForgeMenu> {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 320;
    private static final int PANEL_PADDING = 16;
    private static final int TITLE_HEIGHT = 16;
    private static final int SECTION_GAP = 12;
    private static final int SLOT_SIZE = 18;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 10;
    private static final int LABEL_HEIGHT = 14;
    private static final int PROGRESS_BAR_HEIGHT = 16;
    private static final int PLAYER_SLOT_GAP = 2;
    private static final int PLAYER_SLOT_PADDING = 0;
    private static final int PLAYER_INV_START_INDEX = 1;
    private static final int ELEMENT_SPACING = 4;
    private static final int HELP_BUTTON_WIDTH = 20;
    private static final int SECTION_EXTRA_GAP = 8;
    private static final int PROGRESS_BAR_SEGMENTS = 10;

    private final Theme theme = Theme.vanilla();
    private Label progressLabel;
    private Label statusLabel;
    private Button claimButton;
    private Button cancelButton;

    public FlyingSwordForgeScreen(
        FlyingSwordForgeMenu menu,
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
            KongqiaoI18n.text(KongqiaoI18n.FORGE_TITLE),
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

        int inputSlotY = PANEL_PADDING + TITLE_HEIGHT + SECTION_GAP;
        int inputSlotX = (WINDOW_WIDTH - SLOT_SIZE) / 2;

        UISlot inputSlot = new UISlot(0, theme);
        inputSlot.setFrame(inputSlotX, inputSlotY, SLOT_SIZE, SLOT_SIZE);
        background.addChild(inputSlot);

        Label inputHint = new Label(
            Component.literal("放入核心剑/材料剑/蛊虫"),
            theme
        );
        inputHint.setFrame(
            PANEL_PADDING,
            inputSlotY + SLOT_SIZE + ELEMENT_SPACING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            LABEL_HEIGHT
        );
        background.addChild(inputHint);

        int progressY = inputSlotY + SLOT_SIZE + LABEL_HEIGHT + SECTION_GAP;
        progressLabel = new Label(Component.literal("进度: 0/64 (0%)"), theme);
        progressLabel.setFrame(
            PANEL_PADDING,
            progressY,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            PROGRESS_BAR_HEIGHT
        );
        background.addChild(progressLabel);

        statusLabel = new Label(Component.literal("等待放入核心剑..."), theme);
        statusLabel.setFrame(
            PANEL_PADDING,
            progressY + PROGRESS_BAR_HEIGHT + ELEMENT_SPACING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            LABEL_HEIGHT
        );
        background.addChild(statusLabel);

        int buttonY = progressY + PROGRESS_BAR_HEIGHT + LABEL_HEIGHT + SECTION_GAP + SECTION_EXTRA_GAP;
        int buttonsWidth = BUTTON_WIDTH * 2 + BUTTON_GAP;
        int buttonStartX = (WINDOW_WIDTH - buttonsWidth) / 2;

        claimButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.FORGE_BUTTON_CLAIM),
            theme
        );
        claimButton.setFrame(buttonStartX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
        claimButton.setOnClick(() -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    FlyingSwordForgeMenu.BUTTON_CLAIM
                );
            }
        });
        background.addChild(claimButton);

        cancelButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.FORGE_BUTTON_CANCEL),
            theme
        );
        cancelButton.setFrame(
            buttonStartX + BUTTON_WIDTH + BUTTON_GAP,
            buttonY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        cancelButton.setOnClick(() -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    FlyingSwordForgeMenu.BUTTON_CANCEL
                );
            }
        });
        background.addChild(cancelButton);

        int playerInvY = buttonY + BUTTON_HEIGHT + SECTION_GAP + SECTION_EXTRA_GAP;
        Label playerLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_PLAYER_INVENTORY),
            theme
        );
        playerLabel.setFrame(
            PANEL_PADDING,
            playerInvY,
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
            playerInvY + LABEL_HEIGHT + ELEMENT_SPACING,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        background.addChild(playerGrid);

        updateLabels();
    }

    private void updateLabels() {
        if (progressLabel == null || statusLabel == null || claimButton == null) {
            return;
        }

        int fed = menu.getDataFedSwordCount();
        int required = menu.getDataRequiredSwordCount();
        int percent = menu.getProgressPercent();
        boolean active = menu.isActive();
        boolean canClaim = menu.canClaim();

        String progressBar = buildProgressBar(percent);
        progressLabel.setText(Component.literal(
            "进度: " + fed + "/" + required + " (" + percent + "%) " + progressBar
        ));

        if (!active) {
            statusLabel.setText(Component.literal("等待放入核心剑..."));
        } else if (canClaim) {
            statusLabel.setText(Component.literal("§a培养完成！可以收取成品"));
        } else {
            statusLabel.setText(Component.literal("培养中，继续投喂材料剑或蛊虫"));
        }

        claimButton.setEnabled(canClaim);
        cancelButton.setEnabled(active);
    }

    /**
     * 创建标题栏右侧的帮助按钮。
     */
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
        int filled = percent / PROGRESS_BAR_SEGMENTS;
        int empty = PROGRESS_BAR_SEGMENTS - filled;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < filled; i++) {
            sb.append("█");
        }
        for (int i = 0; i < empty; i++) {
            sb.append("░");
        }
        sb.append("]");
        return sb.toString();
    }
}
