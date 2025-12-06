package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
import com.Kizunad.customNPCs.ai.interaction.MaterialWorkService;
import com.Kizunad.customNPCs.client.data.MaterialDataCache;
import com.Kizunad.customNPCs.menu.NpcCraftMenu;
import com.Kizunad.customNPCs.network.RequestCraftingPayload;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 制造界面：选择可制作物品、数量并提交制作请求。
 */
public class NpcCraftScreen extends TinyUIContainerScreen<NpcCraftMenu> {

    private static final int WINDOW_WIDTH = 360;
    private static final int WINDOW_HEIGHT = 240;
    private static final int PADDING = 10;
    private static final int TITLE_HEIGHT = 12;
    private static final int GAP = 10;
    private static final int LIST_WIDTH = 150;
    private static final int LIST_HEIGHT = 170;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 4;
    private static final int AMOUNT_BUTTON_WIDTH = 20;
    private static final int AMOUNT_LABEL_WIDTH = 60;
    private static final int INLINE_PADDING = 4;
    private static final int INLINE_DOUBLE_PADDING = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int CRAFT_BUTTON_WIDTH = 80;
    private static final int COST_COLOR_WARN = 0xFF5555;
    private static final int COST_COLOR_NORMAL = 0xFFFFFF;
    private static final double EPSILON = 1.0E-6D;

    private final Theme theme;
    private ScrollContainer itemList;
    private Label ownerLabel;
    private Label selectedLabel;
    private Label costLabel;
    private Label amountLabel;
    private Item selectedItem;
    private float unitCost;
    private int amount = 1;

    public NpcCraftScreen(
        NpcCraftMenu menu,
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

        Label titleLabel = new Label("Craft Items", theme);
        titleLabel.setFrame(
            PADDING,
            PADDING,
            WINDOW_WIDTH - PADDING * 2,
            TITLE_HEIGHT
        );
        window.addChild(titleLabel);

        int contentY = PADDING + TITLE_HEIGHT + GAP;
        buildItemList(window, contentY);
        buildDetailPanel(window, contentY);
        refreshTexts();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        refreshTexts();
    }

    private void buildItemList(UIElement window, int contentY) {
        itemList = new ScrollContainer(theme);
        itemList.setFrame(PADDING, contentY, LIST_WIDTH, LIST_HEIGHT);
        window.addChild(itemList);
        rebuildItemList();
    }

    private void rebuildItemList() {
        if (itemList == null) {
            return;
        }
        UIElement list = new UIElement() {};
        int width = itemList.getWidth();
        var entries = MaterialValueManager.getInstance().getEntriesSorted();
        if (entries.isEmpty()) {
            Label empty = new Label("No craftable items", theme);
            empty.setFrame(0, 0, width, ROW_HEIGHT);
            list.addChild(empty);
            list.setFrame(0, 0, width, ROW_HEIGHT);
            itemList.setContent(list);
            return;
        }
        int y = 0;
        for (MaterialValueManager.MaterialValueEntry entry : entries) {
            String name = Component
                .translatable(entry.item().getDescriptionId())
                .getString();
            String text = name + " (" + formatValue(entry.value()) + ")";
            Button btn = new Button(text, theme);
            btn.setFrame(0, y, width, ROW_HEIGHT);
            btn.setOnClick(() -> selectItem(entry));
            list.addChild(btn);
            y += ROW_HEIGHT + ROW_GAP;
        }
        list.setFrame(0, 0, width, Math.max(y, LIST_HEIGHT));
        itemList.setContent(list);
    }

    private void buildDetailPanel(UIElement window, int contentY) {
        int detailX = PADDING + LIST_WIDTH + GAP;
        int detailWidth = WINDOW_WIDTH - detailX - PADDING;
        UIElement detail = new UIElement() {};
        detail.setFrame(detailX, contentY, detailWidth, LIST_HEIGHT);
        window.addChild(detail);

        selectedLabel = new Label("Select an item", theme);
        selectedLabel.setFrame(0, 0, detailWidth, TITLE_HEIGHT);
        detail.addChild(selectedLabel);

        int amountY = TITLE_HEIGHT + GAP;
        Button dec = new Button("-", theme);
        dec.setFrame(0, amountY, AMOUNT_BUTTON_WIDTH, BUTTON_HEIGHT);
        dec.setOnClick(() -> changeAmount(-1));
        detail.addChild(dec);

        amountLabel = new Label("x 1", theme);
        amountLabel.setFrame(
            AMOUNT_BUTTON_WIDTH + INLINE_PADDING,
            amountY + INLINE_PADDING,
            AMOUNT_LABEL_WIDTH,
            TITLE_HEIGHT
        );
        detail.addChild(amountLabel);

        Button inc = new Button("+", theme);
        inc.setFrame(
            AMOUNT_BUTTON_WIDTH + AMOUNT_LABEL_WIDTH + INLINE_DOUBLE_PADDING,
            amountY,
            AMOUNT_BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        inc.setOnClick(() -> changeAmount(1));
        detail.addChild(inc);

        int costY = amountY + BUTTON_HEIGHT + GAP;
        costLabel = new Label("Cost: --", theme);
        costLabel.setFrame(0, costY, detailWidth, TITLE_HEIGHT);
        detail.addChild(costLabel);

        ownerLabel = new Label("", theme);
        ownerLabel.setFrame(
            0,
            costY + TITLE_HEIGHT + GAP,
            detailWidth,
            TITLE_HEIGHT
        );
        detail.addChild(ownerLabel);

        Button craft = new Button("Craft!", theme);
        craft.setFrame(
            detailWidth - CRAFT_BUTTON_WIDTH,
            LIST_HEIGHT - BUTTON_HEIGHT,
            CRAFT_BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        craft.setOnClick(this::clickCraft);
        detail.addChild(craft);
    }

    private void selectItem(MaterialValueManager.MaterialValueEntry entry) {
        selectedItem = entry.item();
        unitCost = entry.value();
        amount = 1;
        refreshTexts();
    }

    private void changeAmount(int delta) {
        if (selectedItem == null) {
            return;
        }
        amount =
            Math.max(
                1,
                Math.min(
                    MaterialWorkService.MAX_CRAFT_AMOUNT,
                    amount + delta
                )
            );
        refreshTexts();
    }

    private void refreshTexts() {
        if (menu == null) {
            return;
        }
        double owner = MaterialDataCache.getOwnerMaterial(menu.getNpcEntityId());
        if (ownerLabel != null) {
            ownerLabel.setText("Material: " + formatValue(owner));
        }
        if (amountLabel != null) {
            amountLabel.setText("x " + amount);
        }
        if (selectedItem == null) {
            if (selectedLabel != null) {
                selectedLabel.setText("Select an item");
            }
            if (costLabel != null) {
                costLabel.setText("Cost: --");
                costLabel.setColor(COST_COLOR_NORMAL);
            }
            return;
        }
        String name = Component
            .translatable(selectedItem.getDescriptionId())
            .getString();
        double cost = unitCost * amount;
        if (selectedLabel != null) {
            selectedLabel.setText("Item: " + name);
        }
        if (costLabel != null) {
            costLabel.setText("Cost: " + formatValue(cost));
            if (owner + EPSILON < cost) {
                costLabel.setColor(COST_COLOR_WARN);
            } else {
                costLabel.setColor(COST_COLOR_NORMAL);
            }
        }
    }

    private void clickCraft() {
        if (menu == null || selectedItem == null) {
            return;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(selectedItem);
        if (itemId == null) {
            return;
        }
        PacketDistributor.sendToServer(
            new RequestCraftingPayload(menu.getNpcEntityId(), itemId, amount)
        );
    }

    private String formatValue(double value) {
        return String.format("%.2f", value);
    }
}
