package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.client.data.MaterialDataCache;
import com.Kizunad.customNPCs.menu.NpcWorkMenu;
import com.Kizunad.customNPCs.network.InteractActionPayload;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 工作主界面：展示状态与导航到材料/制造界面。
 */
public class NpcWorkScreen extends TinyUIContainerScreen<NpcWorkMenu> {

    private static final int WINDOW_WIDTH = 220;
    private static final int WINDOW_HEIGHT = 140;
    private static final int PADDING = 10;
    private static final int LABEL_HEIGHT = 12;
    private static final int LABEL_GAP = 6;
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;

    private final Theme theme;
    private Label ownerLabel;

    public NpcWorkScreen(
        NpcWorkMenu menu,
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

        Label titleLabel = new Label("Work Station", theme);
        titleLabel.setFrame(
            PADDING,
            PADDING,
            WINDOW_WIDTH - PADDING * 2,
            LABEL_HEIGHT
        );
        window.addChild(titleLabel);

        int statusY = PADDING + LABEL_HEIGHT + LABEL_GAP;
        Label statusLabel = new Label(resolveStatusText(), theme);
        statusLabel.setFrame(
            PADDING,
            statusY,
            WINDOW_WIDTH - PADDING * 2,
            LABEL_HEIGHT
        );
        window.addChild(statusLabel);

        ownerLabel = new Label("", theme);
        ownerLabel.setFrame(
            PADDING,
            statusY + LABEL_HEIGHT + LABEL_GAP,
            WINDOW_WIDTH - PADDING * 2,
            LABEL_HEIGHT
        );
        window.addChild(ownerLabel);

        int buttonsY = WINDOW_HEIGHT - BUTTON_HEIGHT - PADDING;
        Button addMaterial = new Button("Add Material", theme);
        addMaterial.setFrame(
            PADDING,
            buttonsY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        addMaterial.setOnClick(() -> sendAction("order_work_material"));
        window.addChild(addMaterial);

        Button craftButton = new Button("Craft", theme);
        craftButton.setFrame(
            WINDOW_WIDTH - BUTTON_WIDTH - PADDING,
            buttonsY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        craftButton.setOnClick(() -> sendAction("order_work_craft"));
        window.addChild(craftButton);

        refreshOwnerMaterial();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshOwnerMaterial();
    }

    private void refreshOwnerMaterial() {
        if (ownerLabel == null || menu == null) {
            return;
        }
        double value = MaterialDataCache.getOwnerMaterial(menu.getNpcEntityId());
        ownerLabel.setText(
            "Material: " + String.format("%.2f", value)
        );
    }

    private String resolveStatusText() {
        // 目前尚无详细工作队列，先展示占位状态。
        return "Work State: Idle";
    }

    private void sendAction(String path) {
        if (menu == null) {
            return;
        }
        ResourceLocation actionId = ResourceLocation.fromNamespaceAndPath(
            CustomNPCsMod.MODID,
            path
        );
        PacketDistributor.sendToServer(
            new InteractActionPayload(
                menu.getNpcEntityId(),
                InteractActionPayload.ActionType.CUSTOM,
                actionId,
                new CompoundTag()
            )
        );
    }
}
