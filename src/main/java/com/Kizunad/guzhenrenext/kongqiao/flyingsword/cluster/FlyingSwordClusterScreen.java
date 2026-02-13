package com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster;

import com.Kizunad.guzhenrenext.network.ServerboundClusterActionPayload;
import com.Kizunad.tinyUI.controls.Button;
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
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

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
    private static final int PADDING = 8;
    private static final int SCROLL_BAR_SPACE = 10;
    private static final int LIST_ITEM_GAP = 2;
    private static final int NAME_LABEL_X = 5;
    private static final int NAME_LABEL_Y = 4;
    private static final int NAME_LABEL_WIDTH = 100;
    private static final int LABEL_HEIGHT = 16;
    private static final int STATUS_INDICATOR_X = 110;
    private static final int STATUS_INDICATOR_WIDTH = 50;
    private static final int SIDEBAR_TITLE_HEIGHT = 20;
    private static final int COMPUTE_BOX_Y = 30;
    private static final int COMPUTE_BOX_HEIGHT = 60;
    private static final int COMPUTE_TEXT_OFFSET_X = 5;
    private static final int COMPUTE_TEXT_OFFSET_Y = 15;
    private static final int ACTION_BUTTON_WIDTH = 90;
    private static final int ACTION_BUTTON_HEIGHT = 18;
    private static final int ACTION_BUTTON_X = 0;
    private static final int RECALL_BUTTON_Y = 100;
    private static final int DEPLOY_BUTTON_Y = 124;
    private static final int ACTIVE_COLOR = 0xFF00FF00;
    private static final int IDLE_COLOR = 0xFFAAAAAA;
    private static final UUID EMPTY_UUID = new UUID(0L, 0L);

    private final Theme theme;

    private final List<FlyingSwordInfo> swords = new ArrayList<>();
    private UUID selectedSwordUuid = EMPTY_UUID;

    public FlyingSwordClusterScreen(
        FlyingSwordClusterMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
        List<UUID> activeUuids = ClusterClientStateCache.getActiveSwordUuids();
        for (UUID activeUuid : activeUuids) {
            if (activeUuid == null) {
                continue;
            }
            swords.add(
                new FlyingSwordInfo(
                    activeUuid.toString().substring(0, LABEL_HEIGHT),
                    LABEL_HEIGHT,
                    0,
                    true,
                    activeUuid
                )
            );
        }
        if (!activeUuids.isEmpty() && activeUuids.get(0) != null) {
            selectedSwordUuid = activeUuids.get(0);
        }
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

        UIElement listContainer = createListSection();
        UIElement sidebar = createSidebar();

        main.addChild(listContainer);
        main.addChild(sidebar);

        listContainer.setFrame(PADDING, PADDING, LIST_WIDTH, WINDOW_HEIGHT - 2 * PADDING);
        sidebar.setFrame(LIST_WIDTH + 2 * PADDING, PADDING, SIDEBAR_WIDTH, WINDOW_HEIGHT - 2 * PADDING);
    }

    private UIElement createListSection() {
        ScrollContainer scroll = new ScrollContainer(theme);
        scroll.setFrame(0, 0, LIST_WIDTH, WINDOW_HEIGHT - 2 * PADDING);

        UIElement listContent = new UIElement() {};
        
        int y = 0;
        for (FlyingSwordInfo info : swords) {
            UIElement cell = createSwordCell(info);
            cell.setFrame(0, y, LIST_WIDTH - SCROLL_BAR_SPACE, LIST_ITEM_HEIGHT);
            listContent.addChild(cell);
            y += LIST_ITEM_HEIGHT + LIST_ITEM_GAP;
        }

        listContent.setFrame(0, 0, LIST_WIDTH - SCROLL_BAR_SPACE, y);
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
        nameLabel.setFrame(NAME_LABEL_X, NAME_LABEL_Y, NAME_LABEL_WIDTH, LABEL_HEIGHT);
        cell.addChild(nameLabel);

        String statusText = info.active ? "Active" : "Idle";
        int statusColor = info.active ? ACTIVE_COLOR : IDLE_COLOR;

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
        statusIndicator.setFrame(
            STATUS_INDICATOR_X,
            NAME_LABEL_Y,
            STATUS_INDICATOR_WIDTH,
            LABEL_HEIGHT
        );
        cell.addChild(statusIndicator);
        cell.onMouseClick(0, 0, 0);
        cell.setFocusable(false);
        cell.setEnabled(true);

        InteractiveElement clickTarget = new InteractiveElement() {
            @Override
            public boolean onMouseClick(double mouseX, double mouseY, int button) {
                selectedSwordUuid = info.uuid;
                return true;
            }
        };
        clickTarget.setFrame(0, 0, LIST_WIDTH - SCROLL_BAR_SPACE, LIST_ITEM_HEIGHT);
        clickTarget.setFocusable(false);
        cell.addChild(clickTarget);

        return cell;
    }

    private UIElement createSidebar() {
        UIElement sidebar = new UIElement() {};
        
        Label title = new Label("Cluster Stats", theme);
        title.setFrame(0, 0, SIDEBAR_WIDTH, SIDEBAR_TITLE_HEIGHT);
        sidebar.addChild(title);

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
                    getAbsoluteX() + COMPUTE_TEXT_OFFSET_X,
                    getAbsoluteY() + COMPUTE_TEXT_OFFSET_Y,
                    theme.getTextColor()
                );
            }
        };
        computeBox.setFrame(0, COMPUTE_BOX_Y, SIDEBAR_WIDTH, COMPUTE_BOX_HEIGHT);
        sidebar.addChild(computeBox);

        Button recallButton = new Button("Recall", theme);
        recallButton.setFrame(
            ACTION_BUTTON_X,
            RECALL_BUTTON_Y,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        recallButton.setOnClick(() -> sendAction(ServerboundClusterActionPayload.Action.RECALL));
        sidebar.addChild(recallButton);

        Button deployButton = new Button("Deploy", theme);
        deployButton.setFrame(
            ACTION_BUTTON_X,
            DEPLOY_BUTTON_Y,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        deployButton.setOnClick(() -> sendAction(ServerboundClusterActionPayload.Action.DEPLOY));
        sidebar.addChild(deployButton);

        return sidebar;
    }

    private void sendAction(ServerboundClusterActionPayload.Action action) {
        PacketDistributor.sendToServer(
            new ServerboundClusterActionPayload(action, selectedSwordUuid)
        );
    }

    @Override
    protected double getUiScale() {
        return 1.0;
    }

    /**
     * 临时数据结构，后续替换为真实 Entity/Data
     */
    private record FlyingSwordInfo(
        String name,
        int level,
        int cost,
        boolean active,
        UUID uuid
    ) {}
}
