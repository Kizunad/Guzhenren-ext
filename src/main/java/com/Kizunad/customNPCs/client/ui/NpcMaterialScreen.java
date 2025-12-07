package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
import com.Kizunad.customNPCs.client.data.MaterialDataCache;
import com.Kizunad.customNPCs.menu.NpcMaterialMenu;
import com.Kizunad.customNPCs.network.RequestMaterialConversionPayload;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.ContainerUI;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 材料转化界面：提供材料输入、余额展示与转化按钮。
 */
public class NpcMaterialScreen extends TinyUIContainerScreen<NpcMaterialMenu> {

    private static final int WINDOW_WIDTH = 240;
    private static final int WINDOW_HEIGHT = 250;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 1;
    private static final int MATERIAL_COLS = 3;
    private static final int MATERIAL_SLOT_COUNT = NpcMaterialMenu.MATERIAL_SLOT_COUNT;
    private static final int TITLE_Y = 6;
    private static final int TITLE_HEIGHT = 12;
    private static final int TITLE_WIDTH = 200;
    private static final int MATERIAL_GRID_X = 10;
    private static final int MATERIAL_GRID_Y = 22;
    private static final int PLAYER_GRID_X = 10;
    private static final int PLAYER_GRID_Y = 120;
    private static final int INFO_Y = 100;
    private static final int INFO_TEXT_WIDTH = 180;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_MARGIN = 10;

    private final Theme theme;
    private Label ownerValueLabel;
    private Label previewValueLabel;
    private double cachedOwnerMaterial;
    private double cachedPreview;

    public NpcMaterialScreen(
        NpcMaterialMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
    }

    @Override
    protected void initUI(UIRoot root) {
        root.setViewport(width, height);

        UIElement window = new UIElement() {};
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

        Label titleLabel = new Label(
            Component.translatable("gui.customnpcs.material.title"),
            theme
        );
        titleLabel.setFrame(MATERIAL_GRID_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
        window.addChild(titleLabel);

        UIElement materialGrid = ContainerUI.scrollableGrid(
            0,
            MATERIAL_SLOT_COUNT,
            MATERIAL_COLS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        materialGrid.setFrame(
            MATERIAL_GRID_X,
            MATERIAL_GRID_Y,
            materialGrid.getWidth(),
            materialGrid.getHeight()
        );
        window.addChild(materialGrid);

        ownerValueLabel = new Label("", theme);
        ownerValueLabel.setFrame(
            MATERIAL_GRID_X,
            INFO_Y,
            INFO_TEXT_WIDTH,
            TITLE_HEIGHT
        );
        window.addChild(ownerValueLabel);

        previewValueLabel = new Label("", theme);
        previewValueLabel.setFrame(
            MATERIAL_GRID_X,
            INFO_Y + TITLE_HEIGHT + 2,
            INFO_TEXT_WIDTH,
            TITLE_HEIGHT
        );
        window.addChild(previewValueLabel);

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            MATERIAL_SLOT_COUNT,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        playerGrid.setFrame(
            PLAYER_GRID_X,
            PLAYER_GRID_Y,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        window.addChild(playerGrid);

        Button convert = new Button(
            Component.translatable("gui.customnpcs.material.button.convert"),
            theme
        );
        convert.setFrame(
            WINDOW_WIDTH - BUTTON_WIDTH - BUTTON_MARGIN,
            WINDOW_HEIGHT - BUTTON_HEIGHT - BUTTON_MARGIN,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        convert.setOnClick(this::clickConvert);
        window.addChild(convert);

        refreshTexts();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshTexts();
    }

    private void refreshTexts() {
        cachedOwnerMaterial =
            MaterialDataCache.getOwnerMaterial(menu.getNpcEntityId());
        cachedPreview = calculatePreviewValue();
        if (ownerValueLabel != null) {
            ownerValueLabel.setText(
                Component.translatable(
                    "gui.customnpcs.material.owner_points",
                    String.format("%.2f", cachedOwnerMaterial)
                )
            );
        }
        if (previewValueLabel != null) {
            previewValueLabel.setText(
                Component.translatable(
                    "gui.customnpcs.material.preview_points",
                    String.format("%.2f", cachedPreview)
                )
            );
        }
    }

    private double calculatePreviewValue() {
        double total = 0.0D;
        for (Slot slot : menu.getMaterialSlots()) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            float unit = MaterialValueManager.getInstance().getMaterialValue(stack);
            if (unit <= 0.0F) {
                continue;
            }
            total += unit * stack.getCount();
        }
        return total;
    }

    private void clickConvert() {
        if (menu.getNpcEntityId() < 0) {
            return;
        }
        PacketDistributor.sendToServer(
            new RequestMaterialConversionPayload(menu.getNpcEntityId())
        );
    }
}
