package com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster;

import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 飞剑集群控制界面。
 * <p>
 * 提供飞剑列表展示、算力概览。
 * 采用 TinyUI 构建。
 * </p>
 */
public class FlyingSwordClusterScreen extends TinyUIContainerScreen<FlyingSwordClusterMenu> {

    private static final int WINDOW_WIDTH = 300;
    private static final int WINDOW_HEIGHT = 220;
    private static final int LIST_WIDTH = 180;
    private static final int LIST_ITEM_HEIGHT = 24;
    private static final int SIDEBAR_WIDTH = 100;
    private static final int GAP = 10;
    private static final int PADDING = 8;
    
    private final Theme theme;
    
    // 模拟数据模型，后续对接真实数据
    private final List<FlyingSwordInfo> swords = new ArrayList<>();

    public FlyingSwordClusterScreen(
        FlyingSwordClusterMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
        // Mock data
        swords.add(new FlyingSwordInfo("Iron Sword", 1, 10, true));
        swords.add(new FlyingSwordInfo("Gold Sword", 5, 25, false));
        swords.add(new FlyingSwordInfo("Diamond Sword", 10, 50, true));
    }

    @Override
    protected boolean shouldEnforceMenuBinding() {
        return false;
    }

    @Override
    protected void initUI(UIRoot root) {
        root.setViewport(WINDOW_WIDTH, WINDOW_HEIGHT);

        UIElement main = new UIElement() {};
        main.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(main);

        // Layout: List (Left) | Sidebar (Right)
        UIElement listContainer = createListSection();
        UIElement sidebar = createSidebar();

        main.addChild(listContainer);
        main.addChild(sidebar);

        // Simple manual layout for now
        listContainer.setFrame(PADDING, PADDING, LIST_WIDTH, WINDOW_HEIGHT - 2 * PADDING);
        sidebar.setFrame(LIST_WIDTH + 2 * PADDING, PADDING, SIDEBAR_WIDTH, WINDOW_HEIGHT - 2 * PADDING);
    }

    private UIElement createListSection() {
        ScrollContainer scroll = new ScrollContainer(theme);
        scroll.setFrame(0, 0, LIST_WIDTH, WINDOW_HEIGHT - 2 * PADDING);

        UIElement listContent = new UIElement() {};
        
        // Populate list
        int y = 0;
        for (FlyingSwordInfo info : swords) {
            UIElement cell = createSwordCell(info);
            cell.setFrame(0, y, LIST_WIDTH - 10, LIST_ITEM_HEIGHT); // -10 for scrollbar space
            listContent.addChild(cell);
            y += LIST_ITEM_HEIGHT + 2;
        }
        
        // Auto-size content height
        listContent.setFrame(0, 0, LIST_WIDTH - 10, y);
        scroll.addChild(listContent);
        
        return scroll;
    }

    private UIElement createSwordCell(FlyingSwordInfo info) {
        InteractiveElement cell = new InteractiveElement() {
            @Override
            protected void onRender(UIRenderContext context, double mouseX, double mouseY, float partialTicks) {
                // Background
                context.drawRect(
                    getAbsoluteX(),
                    getAbsoluteY(),
                    getWidth(),
                    getHeight(),
                    theme.getFieldBackgroundColor()
                );
                super.onRender(context, mouseX, mouseY, partialTicks);
            }
        };

        Label nameLabel = new Label(info.name + " (Lv." + info.level + ")", theme);
        nameLabel.setFrame(5, 4, 100, 16);
        cell.addChild(nameLabel);

        String statusText = info.active ? "Active" : "Idle";
        int statusColor = info.active ? 0xFF00FF00 : 0xFFAAAAAA;
        
        // Custom draw for status
        UIElement statusIndicator = new UIElement() {
            @Override
            protected void onRender(UIRenderContext context, double mouseX, double mouseY, float partialTicks) {
                context.drawText(
                    statusText,
                    getAbsoluteX(),
                    getAbsoluteY(),
                    statusColor
                );
            }
        };
        statusIndicator.setFrame(110, 4, 50, 16);
        cell.addChild(statusIndicator);

        return cell;
    }

    private UIElement createSidebar() {
        UIElement sidebar = new UIElement() {};
        
        Label title = new Label("Cluster Stats", theme);
        title.setFrame(0, 0, SIDEBAR_WIDTH, 20);
        sidebar.addChild(title);

        // Compute Power Placeholder
        UIElement computeBox = new UIElement() {
             @Override
            protected void onRender(UIRenderContext context, double mouseX, double mouseY, float partialTicks) {
                context.drawRect(
                    getAbsoluteX(),
                    getAbsoluteY(),
                    getWidth(),
                    getHeight(),
                    theme.getBackgroundColor()
                );
                context.drawText(
                    "CP: " + getMenu().getComputePower() + "/" + getMenu().getMaxComputePower(),
                    getAbsoluteX() + 5,
                    getAbsoluteY() + 15,
                    theme.getTextColor()
                );
            }
        };
        computeBox.setFrame(0, 30, SIDEBAR_WIDTH, 60);
        sidebar.addChild(computeBox);

        return sidebar;
    }

    @Override
    protected double getUiScale() {
        return 1.0;
    }

    /**
     * 临时数据结构，后续替换为真实 Entity/Data
     */
    private record FlyingSwordInfo(String name, int level, int cost, boolean active) {}
}
