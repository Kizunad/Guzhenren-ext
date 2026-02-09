package com.Kizunad.guzhenrenext.bastion.ui;

import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.UISlot;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 地灵（狐仙）管理界面。
 * <p>
 * 使用 TinyUI 构建，包含左侧地灵展示区与右侧行囊区。
 * </p>
 */
public class SpiritManagementScreen extends TinyUIContainerScreen<SpiritManagementMenu> {

    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 250;
    private static final int PADDING = 10;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int TITLE_HEIGHT = 16;
    private static final int VAULT_COLS = 3;
    private static final int VAULT_ROWS = 3;
    private static final int SPIRIT_PLACEHOLDER_TOP = 20;
    private static final int SPIRIT_PLACEHOLDER_HEIGHT = 150;
    private static final int SPIRIT_PLACEHOLDER_LABEL_HEIGHT = 20;
    private static final int VAULT_TITLE_HEIGHT = 12;
    private static final int VAULT_TITLE_OFFSET_Y = 12;
    private static final int PLAYER_INV_START_INDEX = 9;

    // 占位符：地灵渲染区域宽度
    private static final int SPIRIT_RENDER_WIDTH = 100;

    private final Theme theme = Theme.vanilla();

    public SpiritManagementScreen(SpiritManagementMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void initUI(UIRoot root) {
        // 创建主窗口面板
        UIElement window = new UIElement() {};
        window.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // 居中定位
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

        // 背景
        SolidPanel background = new SolidPanel(theme);
        background.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.addChild(background);

        // 标题
        Label titleLabel = new Label(this.title, theme);
        titleLabel.setFrame(PADDING, PADDING, WINDOW_WIDTH - PADDING * 2, TITLE_HEIGHT);
        background.addChild(titleLabel);

        // 左侧：地灵渲染占位符
        UIElement spiritPlaceholder = new UIElement() {
            // TODO: 实现 EntityWidget 用于渲染地灵实体
        };
        spiritPlaceholder.setFrame(
            PADDING,
            PADDING + SPIRIT_PLACEHOLDER_TOP,
            SPIRIT_RENDER_WIDTH,
            SPIRIT_PLACEHOLDER_HEIGHT
        );
        // 这里暂时用 Label 占位
        Label spiritLabel = new Label(Component.translatable("gui.guzhenrenext.spirit.render_placeholder"), theme);
        spiritLabel.setFrame(0, 0, SPIRIT_RENDER_WIDTH, SPIRIT_PLACEHOLDER_LABEL_HEIGHT);
        spiritPlaceholder.addChild(spiritLabel);
        background.addChild(spiritPlaceholder);

        // 右侧：地灵行囊 (3x3 Grid)
        int vaultX = PADDING + SPIRIT_RENDER_WIDTH + PADDING;
        int vaultY = PADDING + SPIRIT_PLACEHOLDER_TOP;
        int vaultWidth = (SLOT_SIZE + SLOT_GAP) * VAULT_COLS;

        Label vaultTitle = new Label(Component.translatable("gui.guzhenrenext.spirit.vault"), theme);
        vaultTitle.setFrame(vaultX, vaultY - VAULT_TITLE_OFFSET_Y, vaultWidth, VAULT_TITLE_HEIGHT);
        background.addChild(vaultTitle);

        for (int row = 0; row < VAULT_ROWS; row++) {
            for (int col = 0; col < VAULT_COLS; col++) {
                int index = col + row * VAULT_COLS;
                UISlot slot = new UISlot(index, theme);
                slot.setFrame(
                    vaultX + col * (SLOT_SIZE + SLOT_GAP),
                    vaultY + row * (SLOT_SIZE + SLOT_GAP),
                    SLOT_SIZE,
                    SLOT_SIZE
                );
                background.addChild(slot);
            }
        }

        // 底部：玩家物品栏
        // 使用 TinyUI 提供的 InventoryUI 辅助构建
        // 参数：startSlotIndex, slotSize, gap, padding, theme
        // SpiritManagementMenu 中玩家物品栏起始索引为 9
        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            PLAYER_INV_START_INDEX,
            SLOT_SIZE,
            SLOT_GAP,
            0,
            theme
        );

        // 计算玩家物品栏位置：窗口底部居中
        int playerGridWidth = playerGrid.getWidth();
        int playerGridHeight = playerGrid.getHeight();
        int playerGridX = (WINDOW_WIDTH - playerGridWidth) / 2;
        int playerGridY = WINDOW_HEIGHT - playerGridHeight - PADDING;

        playerGrid.setFrame(playerGridX, playerGridY, playerGridWidth, playerGridHeight);
        background.addChild(playerGrid);
    }

    @Override
    protected double getUiScale() {
        // 假设沿用空窍 UI 的缩放配置，或者默认 1.0
        return 1.0;
    }

    /**
     * 简单的纯色背景面板，用于测试布局。
     * 后续可替换为带材质的 Panel。
     */
    private static class SolidPanel extends UIElement {
        private static final int BORDER = 1;
        private final Theme theme;

        public SolidPanel(final Theme theme) {
            this.theme = Objects.requireNonNull(theme, "theme");
        }

        @Override
        protected void onRender(
            final UIRenderContext context,
            final double mouseX,
            final double mouseY,
            final float partialTicks
        ) {
            context.drawRect(
                getAbsoluteX(),
                getAbsoluteY(),
                getWidth(),
                getHeight(),
                theme.getBackgroundColor()
            );
            int color = theme.getAccentColor();
            context.drawRect(
                getAbsoluteX(),
                getAbsoluteY(),
                getWidth(),
                BORDER,
                color
            );
            context.drawRect(
                getAbsoluteX(),
                getAbsoluteY() + getHeight() - BORDER,
                getWidth(),
                BORDER,
                color
            );
            context.drawRect(
                getAbsoluteX(),
                getAbsoluteY(),
                BORDER,
                getHeight(),
                color
            );
            context.drawRect(
                getAbsoluteX() + getWidth() - BORDER,
                getAbsoluteY(),
                BORDER,
                getHeight(),
                color
            );
        }
    }
}
