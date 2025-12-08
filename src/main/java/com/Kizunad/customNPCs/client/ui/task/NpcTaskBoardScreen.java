package com.Kizunad.customNPCs.client.ui.task;

import com.Kizunad.customNPCs.network.OpenTaskBoardPayload;
import com.Kizunad.customNPCs.network.ServerboundAcceptTaskPayload;
import com.Kizunad.customNPCs.network.ServerboundRefreshTaskBoardPayload;
import com.Kizunad.customNPCs.network.ServerboundSubmitTaskPayload;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 玩家端：NPC 任务面板。
 */
public class NpcTaskBoardScreen extends TinyUIScreen {

    private static final int WINDOW_WIDTH = 680;
    private static final int WINDOW_HEIGHT = 360;
    private static final int MARGIN = 16;
    private static final int LIST_WIDTH = 180;
    private static final int PANEL_GAP = 12;
    private static final int BUTTON_HEIGHT = 24;
    private static final int BUTTON_GAP = 6;
    private static final int SCROLL_PADDING = 4;
    private static final int SCROLL_DOUBLE_PADDING = SCROLL_PADDING * 2;
    private static final int BUTTON_INNER_MARGIN = 4;
    private static final int BUTTON_INNER_WIDTH = 90;
    private static final int BUTTON_SUBMIT_OFFSET = 100;
    private static final int BUTTON_REFRESH_OFFSET = 200;
    private static final int BUTTON_CLOSE_OFFSET = 310;
    private static final int DETAIL_PADDING = 10;
    private static final int ITEM_ICON_SIZE = 18;
    private static final int ITEM_ICON_Y_OFFSET = 4;
    private static final int ITEM_TEXT_OFFSET = 24;
    private static final int SECTION_GAP = 12;
    private static final int LINE_HEIGHT = 10;
    private static final int COLOR_TEXT_PRIMARY = 0xFFFFFF;
    private static final int COLOR_TEXT_MUTED = 0xAAAAAA;
    private static final int COLOR_TEXT_BODY = 0xDDDDDD;
    private static final int COLOR_TEXT_REWARD = 0xF0B400;

    /** 设计分辨率 - 设置为 1280x720 实现 1.5 倍放大（原始 1920/1.5=1280, 1080/1.5=720） */
    private static final int DESIGN_WIDTH = 1920 / 2;
    private static final int DESIGN_HEIGHT = 1080 / 2;

    private final UIRoot uiRoot;
    private final Theme theme;
    private final int npcEntityId;
    private final List<OpenTaskBoardPayload.TaskEntry> tasks;

    private ScrollContainer listScroll;
    private PanelElement detailPanel;
    private Button acceptButton;
    private Button submitButton;
    private int selectedIndex = 0;

    public NpcTaskBoardScreen(OpenTaskBoardPayload payload) {
        this(new UIRoot(), payload);
    }

    private NpcTaskBoardScreen(UIRoot root, OpenTaskBoardPayload payload) {
        super(Component.translatable("gui.customnpcs.task_board.title"), root);
        this.uiRoot = root;
        this.theme = Theme.vanilla();
        this.npcEntityId = payload.npcEntityId();
        this.tasks = payload.entries();
    }

    @Override
    protected void init() {
        super.init();
        // 启用 1280x720 设计分辨率（相对于 1920x1080 实际屏幕会放大 1.5 倍）
        uiRoot.setDesignResolution(DESIGN_WIDTH, DESIGN_HEIGHT);
        uiRoot.clearChildren();
        uiRoot.setViewport(width, height);
        buildLayout();
        rebuildTaskList();
        updateButtons();
    }

    private void buildLayout() {
        PanelElement rootPanel = new PanelElement();
        Anchor.apply(
            rootPanel,
            uiRoot.getWidth(),
            uiRoot.getHeight(),
            new Anchor.Spec(
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        uiRoot.addChild(rootPanel);

        PanelElement listPanel = new PanelElement();
        listPanel.setFrame(
            MARGIN,
            MARGIN,
            LIST_WIDTH,
            WINDOW_HEIGHT - MARGIN * 2
        );
        rootPanel.addChild(listPanel);

        listScroll = new ScrollContainer(theme);
        listScroll.setFrame(
            SCROLL_PADDING,
            SCROLL_PADDING,
            LIST_WIDTH - SCROLL_DOUBLE_PADDING,
            WINDOW_HEIGHT - MARGIN * 2 - SCROLL_DOUBLE_PADDING
        );
        listPanel.addChild(listScroll);

        detailPanel = new PanelElement();
        int detailX = MARGIN + LIST_WIDTH + PANEL_GAP;
        int detailWidth = WINDOW_WIDTH - detailX - MARGIN;
        detailPanel.setFrame(
            detailX,
            MARGIN,
            detailWidth,
            WINDOW_HEIGHT - MARGIN * 2 - BUTTON_HEIGHT - BUTTON_GAP
        );
        rootPanel.addChild(detailPanel);

        PanelElement buttonPanel = new PanelElement();
        buttonPanel.setFrame(
            detailX,
            WINDOW_HEIGHT - MARGIN - BUTTON_HEIGHT,
            detailWidth,
            BUTTON_HEIGHT
        );
        rootPanel.addChild(buttonPanel);

        acceptButton = new Button(
            Component.translatable("gui.customnpcs.task_board.button.accept"),
            theme
        );
        acceptButton.setFrame(
            DETAIL_PADDING,
            BUTTON_INNER_MARGIN,
            BUTTON_INNER_WIDTH,
            BUTTON_HEIGHT - BUTTON_INNER_MARGIN * 2
        );
        acceptButton.setOnClick(() -> sendAccept());
        buttonPanel.addChild(acceptButton);

        submitButton = new Button(
            Component.translatable("gui.customnpcs.task_board.button.submit"),
            theme
        );
        submitButton.setFrame(
            DETAIL_PADDING + BUTTON_SUBMIT_OFFSET,
            BUTTON_INNER_MARGIN,
            BUTTON_INNER_WIDTH,
            BUTTON_HEIGHT - BUTTON_INNER_MARGIN * 2
        );
        submitButton.setOnClick(() -> sendSubmit());
        buttonPanel.addChild(submitButton);

        Button refreshButton = new Button(
            Component.translatable("gui.customnpcs.task_board.button.refresh"),
            theme
        );
        refreshButton.setFrame(
            DETAIL_PADDING + BUTTON_REFRESH_OFFSET,
            BUTTON_INNER_MARGIN,
            BUTTON_INNER_WIDTH,
            BUTTON_HEIGHT - BUTTON_INNER_MARGIN * 2
        );
        refreshButton.setOnClick(() -> sendRefresh());
        buttonPanel.addChild(refreshButton);

        Button closeButton = new Button(
            Component.translatable("gui.customnpcs.task_board.button.close"),
            theme
        );
        closeButton.setFrame(
            DETAIL_PADDING + BUTTON_CLOSE_OFFSET,
            BUTTON_INNER_MARGIN,
            BUTTON_INNER_WIDTH,
            BUTTON_HEIGHT - BUTTON_INNER_MARGIN * 2
        );
        closeButton.setOnClick(() -> minecraft.setScreen(null));
        buttonPanel.addChild(closeButton);
    }

    private void rebuildTaskList() {
        UIElement list = new UIElement() {};
        int y = 0;
        for (int i = 0; i < tasks.size(); i++) {
            OpenTaskBoardPayload.TaskEntry entry = tasks.get(i);
            Button button = new Button(entry.title(), theme);
            int index = i;
            button.setOnClick(() -> {
                selectedIndex = index;
                updateButtons();
            });
            button.setFrame(0, y, listScroll.getWidth(), BUTTON_HEIGHT);
            list.addChild(button);
            y += BUTTON_HEIGHT + BUTTON_GAP;
        }
        list.setFrame(
            0,
            0,
            listScroll.getWidth(),
            Math.max(listScroll.getHeight(), y)
        );
        listScroll.setContent(list);
    }

    private void updateButtons() {
        OpenTaskBoardPayload.TaskEntry entry = getSelectedEntry();
        if (entry == null) {
            acceptButton.setEnabled(false);
            submitButton.setEnabled(false);
            return;
        }
        acceptButton.setEnabled(entry.state() == TaskProgressState.AVAILABLE);
        submitButton.setEnabled(entry.state() == TaskProgressState.ACCEPTED);
    }

    private OpenTaskBoardPayload.TaskEntry getSelectedEntry() {
        if (selectedIndex < 0 || selectedIndex >= tasks.size()) {
            return null;
        }
        return tasks.get(selectedIndex);
    }

    private void sendAccept() {
        OpenTaskBoardPayload.TaskEntry entry = getSelectedEntry();
        if (entry == null) {
            return;
        }
        PacketDistributor.sendToServer(
            new ServerboundAcceptTaskPayload(npcEntityId, entry.taskId())
        );
    }

    private void sendSubmit() {
        OpenTaskBoardPayload.TaskEntry entry = getSelectedEntry();
        if (entry == null) {
            return;
        }
        PacketDistributor.sendToServer(
            new ServerboundSubmitTaskPayload(npcEntityId, entry.taskId())
        );
    }

    private void sendRefresh() {
        PacketDistributor.sendToServer(
            new ServerboundRefreshTaskBoardPayload(npcEntityId)
        );
    }

    @Override
    protected void renderScaledContent(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        renderDetail(graphics);
    }

    private void renderDetail(GuiGraphics graphics) {
        OpenTaskBoardPayload.TaskEntry entry = getSelectedEntry();
        if (entry == null || detailPanel == null) {
            return;
        }
        int x = detailPanel.getAbsoluteX() + DETAIL_PADDING;
        int y = detailPanel.getAbsoluteY() + DETAIL_PADDING;
        graphics.drawString(font, entry.title(), x, y, COLOR_TEXT_PRIMARY, false);
        y += LINE_HEIGHT + 2;
        Component stateText = resolveStateLabel(entry.state());
        graphics.drawString(font, stateText, x, y, COLOR_TEXT_MUTED, false);
        y += LINE_HEIGHT + 2;

        List<net.minecraft.util.FormattedCharSequence> lines = font.split(
            entry.description(),
            detailPanel.getWidth() - DETAIL_PADDING * 2
        );
        for (net.minecraft.util.FormattedCharSequence seq : lines) {
            graphics.drawString(font, seq, x, y, COLOR_TEXT_BODY, false);
            y += LINE_HEIGHT;
        }
        y += SECTION_GAP;

        graphics.drawString(
            font,
            Component.translatable("gui.customnpcs.task_board.section.objectives"),
            x,
            y,
            COLOR_TEXT_PRIMARY,
            false
        );
        y += LINE_HEIGHT;
        for (OpenTaskBoardPayload.ObjectiveEntry objective : entry.objectives()) {
            graphics.renderItem(objective.displayItem(), x, y - ITEM_ICON_Y_OFFSET);
            Component label = switch (objective.type()) {
                case SUBMIT_ITEM -> Component.translatable(
                    "gui.customnpcs.task_board.objective.submit",
                    objective.currentCount(),
                    objective.requiredCount(),
                    objective.displayName()
                );
                case KILL_ENTITY -> Component.translatable(
                    "gui.customnpcs.task_board.objective.kill",
                    objective.currentCount(),
                    objective.requiredCount(),
                    objective.displayName()
                );
                case GUARD_ENTITY -> Component.translatable(
                    "gui.customnpcs.task_board.objective.guard",
                    objective.currentCount(),
                    objective.requiredCount(),
                    objective.displayName()
                );
            };
            graphics.drawString(
                font,
                label,
                x + ITEM_TEXT_OFFSET,
                y,
                COLOR_TEXT_PRIMARY,
                false
            );
            y += ITEM_ICON_SIZE;
        }

        y += SECTION_GAP;
        graphics.drawString(
            font,
            Component.translatable("gui.customnpcs.task_board.section.rewards"),
            x,
            y,
            COLOR_TEXT_PRIMARY,
            false
        );
        y += LINE_HEIGHT;
        for (net.minecraft.world.item.ItemStack stack : entry.rewards()) {
            graphics.renderItem(stack, x, y - ITEM_ICON_Y_OFFSET);
            Component label = Component.translatable(
                "gui.customnpcs.task_board.reward.entry",
                stack.getCount(),
                stack.getHoverName()
            );
            graphics.drawString(
                font,
                label,
                x + ITEM_TEXT_OFFSET,
                y,
                COLOR_TEXT_REWARD,
                false
            );
            y += ITEM_ICON_SIZE;
        }
    }

    private Component resolveStateLabel(TaskProgressState state) {
        return Component.translatable(
            "gui.customnpcs.task_board.state_label",
            resolveStateName(state)
        );
    }

    private Component resolveStateName(TaskProgressState state) {
        return switch (state) {
            case AVAILABLE -> Component.translatable(
                "gui.customnpcs.task_board.state.available"
            );
            case ACCEPTED -> Component.translatable(
                "gui.customnpcs.task_board.state.accepted"
            );
            case COMPLETED -> Component.translatable(
                "gui.customnpcs.task_board.state.completed"
            );
        };
    }

    /**
     * 简易面板元素，用于绘制半透明背景。
     */
    private class PanelElement extends UIElement {

        private static final int BORDER = 1;

        @Override
        protected void onRender(
            UIRenderContext context,
            double mouseX,
            double mouseY,
            float partialTicks
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
