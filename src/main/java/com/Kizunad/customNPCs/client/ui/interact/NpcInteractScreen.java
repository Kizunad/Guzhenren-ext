package com.Kizunad.customNPCs.client.ui.interact;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.network.InteractActionPayload;
import com.Kizunad.customNPCs.network.dto.DialogueOption;
import com.Kizunad.customNPCs.network.dto.NpcStatusEntry;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * NPC 仪表盘/对话界面。
 * <p>布局参考 tmp/interact/Interact.drawio.png。</p>
 */
public class NpcInteractScreen extends TinyUIScreen {

    private static final int WINDOW_WIDTH = 680;
    private static final int WINDOW_HEIGHT = 400;
    private static final int MARGIN = 16;
    private static final int STATUS_PANEL_WIDTH = 190;
    private static final int BUTTON_PANEL_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 28;
    private static final int BUTTON_GAP = 16;
    private static final int BUTTON_INNER_PADDING = 10;
    private static final int BUTTON_INNER_WIDTH =
        BUTTON_PANEL_WIDTH - BUTTON_INNER_PADDING * 2;
    private static final int ACTION_BUTTON_COUNT = 4;
    private static final int ACTION_BUTTON_GAPS = ACTION_BUTTON_COUNT - 1;
    private static final int STATUS_ROW_HEIGHT = 26;
    private static final int STATUS_LIST_HEIGHT = 260;
    private static final int PREVIEW_MIN_WIDTH = 240;
    private static final int PREVIEW_MIN_HEIGHT = 220;
    private static final int PREVIEW_FOOTER = 20;
    private static final int DIALOGUE_PANEL_HEIGHT = 170;
    private static final int OPTION_ROW_HEIGHT = 24;
    private static final int OPTION_ROW_GAP = 4;
    private static final int PREVIEW_SCALE = 60;
    private static final int NAME_LABEL_HEIGHT = 14;
    private static final int STATUS_TITLE_Y_OFFSET = 18;
    private static final int STATUS_TITLE_HEIGHT = 12;
    private static final int STATUS_PANEL_Y = 34;
    private static final int STATUS_SCROLL_PADDING = 4;
    private static final int STATUS_LABEL_X = 6;
    private static final int STATUS_LABEL_Y = 4;
    private static final int STATUS_LABEL_WIDTH_OFFSET = 10;
    private static final int STATUS_VALUE_WIDTH_OFFSET = 16;
    private static final int STATUS_TEXT_HEIGHT = 12;
    private static final int PREVIEW_GAP = 12;
    private static final int DIALOGUE_PADDING = 8;
    private static final int DIALOGUE_ACTOR_WIDTH = 140;
    private static final int DIALOGUE_ACTOR_HEIGHT = 12;
    private static final int DIALOGUE_TEXT_GAP = 12;
    private static final int DIALOGUE_TEXT_HEIGHT = 36;
    private static final int DIALOGUE_TEXT_RIGHT_PADDING = 20;
    private static final int DIALOGUE_OPTIONS_Y = 54;
    private static final int DIALOGUE_OPTIONS_HEIGHT_OFFSET = 62;
    private static final int BACK_BUTTON_WIDTH = 70;
    private static final int BACK_BUTTON_HEIGHT = 20;
    private static final int BACK_BUTTON_MARGIN = 8;

    /**
     * 交互数据载体，避免超参构造。
     */
    public record InteractData(
        int npcEntityId,
        Component displayName,
        float health,
        float maxHealth,
        boolean isOwner,
        boolean startInDialogueMode,
        List<NpcStatusEntry> statusEntries,
        List<DialogueOption> dialogueOptions
    ) {}

    private final UIRoot uiRoot;
    private final Theme theme;
    private final int npcEntityId;
    private final Component displayName;
    private final float health;
    private final float maxHealth;
    private final boolean isOwner;
    private final List<NpcStatusEntry> statusEntries;
    private final List<DialogueOption> dialogueOptions;

    private final UIElement dashboardPanel = new PanelElement();
    private final UIElement dialoguePanel = new PanelElement();
    private UIElement previewPanel;
    private ScrollContainer statusScroll;
    private ScrollContainer optionsScroll;
    private Label dialogueTextLabel;
    private boolean dialogueMode = false;

    public NpcInteractScreen(InteractData data) {
        this(new UIRoot(), data);
    }

    private NpcInteractScreen(
        UIRoot root,
        InteractData data
    ) {
        super(data.displayName(), root);
        this.uiRoot = root;
        this.theme = Theme.vanilla();
        this.npcEntityId = data.npcEntityId();
        this.displayName = data.displayName();
        this.health = data.health();
        this.maxHealth = data.maxHealth();
        this.isOwner = data.isOwner();
        this.dialogueMode = data.startInDialogueMode();
        this.statusEntries =
            List.copyOf(
                Objects.requireNonNull(
                    data.statusEntries(),
                    "statusEntries"
                )
            );
        this.dialogueOptions =
            new ArrayList<>(
                Objects.requireNonNull(
                    data.dialogueOptions(),
                    "dialogueOptions"
                )
            );
    }

    @Override
    protected void init() {
        super.init();
        uiRoot.clearChildren();
        uiRoot.setViewport(width, height);
        buildDashboard(uiRoot);
        buildDialoguePanel(uiRoot);
        syncVisibility();
    }

    private void buildDashboard(UIRoot root) {
        dashboardPanel.clearChildren();
        Anchor.apply(
            dashboardPanel,
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
        Label nameLabel = new Label(displayName.getString(), theme);
        nameLabel.setFrame(MARGIN, MARGIN, STATUS_PANEL_WIDTH, NAME_LABEL_HEIGHT);
        dashboardPanel.addChild(nameLabel);

        Label statusTitle = new Label("Status", theme);
        statusTitle.setFrame(
            MARGIN,
            MARGIN + STATUS_TITLE_Y_OFFSET,
            STATUS_PANEL_WIDTH,
            STATUS_TITLE_HEIGHT
        );
        dashboardPanel.addChild(statusTitle);

        UIElement statusPanel = new PanelElement();
        statusPanel.setFrame(
            MARGIN,
            MARGIN + STATUS_PANEL_Y,
            STATUS_PANEL_WIDTH,
            STATUS_LIST_HEIGHT
        );
        dashboardPanel.addChild(statusPanel);

        statusScroll = new ScrollContainer(theme);
        statusScroll.setFrame(
            STATUS_SCROLL_PADDING,
            STATUS_SCROLL_PADDING,
            STATUS_PANEL_WIDTH - STATUS_SCROLL_PADDING * 2,
            STATUS_LIST_HEIGHT - STATUS_SCROLL_PADDING * 2
        );
        statusPanel.addChild(statusScroll);
        rebuildStatusList();

        UIElement buttonPanel = new PanelElement();
        int buttonsHeight =
            (BUTTON_HEIGHT * ACTION_BUTTON_COUNT) +
            (BUTTON_GAP * ACTION_BUTTON_GAPS);
        int buttonsY = MARGIN + STATUS_PANEL_Y;
        buttonPanel.setFrame(
            WINDOW_WIDTH - MARGIN - BUTTON_PANEL_WIDTH,
            buttonsY,
            BUTTON_PANEL_WIDTH,
            buttonsHeight
        );
        dashboardPanel.addChild(buttonPanel);

        addActionButtons(buttonPanel);

        UIElement preview = new PanelElement();
        int previewX = MARGIN + STATUS_PANEL_WIDTH + PREVIEW_GAP;
        int previewW =
            WINDOW_WIDTH -
            previewX -
            BUTTON_PANEL_WIDTH -
            MARGIN -
            PREVIEW_GAP;
        previewW = Math.max(previewW, PREVIEW_MIN_WIDTH);
        int previewH = STATUS_LIST_HEIGHT;
        previewH = Math.max(previewH, PREVIEW_MIN_HEIGHT);
        preview.setFrame(previewX, MARGIN + STATUS_PANEL_Y, previewW, previewH);
        dashboardPanel.addChild(preview);
        this.previewPanel = preview;

        root.addChild(dashboardPanel);
    }

    private void addActionButtons(UIElement buttonPanel) {
        int currentY = 0;
        buttonPanel.clearChildren();
        Button trade = createActionButton("Trade", () ->
            sendAction(InteractActionPayload.ActionType.TRADE, rl("trade"), null)
        );
        trade.setFrame(
            BUTTON_INNER_PADDING,
            currentY,
            BUTTON_INNER_WIDTH,
            BUTTON_HEIGHT
        );
        buttonPanel.addChild(trade);
        currentY += BUTTON_HEIGHT + BUTTON_GAP;

        Button gift = createActionButton("Gift", () ->
            sendAction(InteractActionPayload.ActionType.GIFT, rl("gift"), null)
        );
        gift.setFrame(
            BUTTON_INNER_PADDING,
            currentY,
            BUTTON_INNER_WIDTH,
            BUTTON_HEIGHT
        );
        buttonPanel.addChild(gift);
        currentY += BUTTON_HEIGHT + BUTTON_GAP;

        Button chat = createActionButton("Chat", () -> {
            dialogueMode = true;
            syncVisibility();
        });
        chat.setFrame(
            BUTTON_INNER_PADDING,
            currentY,
            BUTTON_INNER_WIDTH,
            BUTTON_HEIGHT
        );
        buttonPanel.addChild(chat);
        currentY += BUTTON_HEIGHT + BUTTON_GAP;

        if (isOwner) {
            Button owner = createActionButton("Owner Opts", () -> {
                dialogueMode = true;
                syncVisibility();
            });
            owner.setFrame(
                BUTTON_INNER_PADDING,
                currentY,
                BUTTON_INNER_WIDTH,
                BUTTON_HEIGHT
            );
            buttonPanel.addChild(owner);
        }
    }

    private Button createActionButton(String text, Runnable onClick) {
        Button button = new Button(text, theme);
        button.setOnClick(onClick);
        return button;
    }

    private void rebuildStatusList() {
        UIElement list = new UIElement() {};
        int y = 0;
        for (NpcStatusEntry entry : statusEntries) {
            UIElement row = createStatusRow(entry);
            row.setFrame(
                0,
                y,
                STATUS_PANEL_WIDTH - STATUS_SCROLL_PADDING * 2,
                STATUS_ROW_HEIGHT
            );
            list.addChild(row);
            y += STATUS_ROW_HEIGHT;
        }
        list.setFrame(
            0,
            0,
            STATUS_PANEL_WIDTH - STATUS_SCROLL_PADDING * 2,
            Math.max(y, STATUS_LIST_HEIGHT - STATUS_SCROLL_PADDING * 2)
        );
        statusScroll.setContent(list);
    }

    private UIElement createStatusRow(NpcStatusEntry entry) {
        UIElement row = new UIElement() {
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
            }
        };
        Label label = new Label(entry.label().getString(), theme);
        label.setFrame(
            STATUS_LABEL_X,
            STATUS_LABEL_Y,
            (STATUS_PANEL_WIDTH / 2) - STATUS_LABEL_WIDTH_OFFSET,
            STATUS_TEXT_HEIGHT
        );
        row.addChild(label);

        Label value = new Label(entry.value().getString(), theme);
        value.setColor(entry.color());
        value.setFrame(
            (STATUS_PANEL_WIDTH / 2),
            STATUS_LABEL_Y,
            (STATUS_PANEL_WIDTH / 2) - STATUS_VALUE_WIDTH_OFFSET,
            STATUS_TEXT_HEIGHT
        );
        row.addChild(value);
        return row;
    }

    private void buildDialoguePanel(UIRoot root) {
        dialoguePanel.clearChildren();
        Anchor.apply(
            dialoguePanel,
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

        UIElement container = new PanelElement();
        container.setFrame(
            MARGIN,
            WINDOW_HEIGHT - DIALOGUE_PANEL_HEIGHT - MARGIN,
            WINDOW_WIDTH - MARGIN * 2,
            DIALOGUE_PANEL_HEIGHT
        );
        dialoguePanel.addChild(container);

        Label actorLabel = new Label(displayName.getString(), theme);
        actorLabel.setFrame(
            DIALOGUE_PADDING,
            DIALOGUE_PADDING,
            DIALOGUE_ACTOR_WIDTH,
            DIALOGUE_ACTOR_HEIGHT
        );
        container.addChild(actorLabel);

        dialogueTextLabel = new Label("......", theme);
        int textX =
            DIALOGUE_PADDING + DIALOGUE_ACTOR_WIDTH + DIALOGUE_TEXT_GAP;
        dialogueTextLabel.setFrame(
            textX,
            DIALOGUE_PADDING,
            container.getWidth() - textX - DIALOGUE_TEXT_RIGHT_PADDING,
            DIALOGUE_TEXT_HEIGHT
        );
        container.addChild(dialogueTextLabel);

        optionsScroll = new ScrollContainer(theme);
        optionsScroll.setFrame(
            DIALOGUE_PADDING,
            DIALOGUE_OPTIONS_Y,
            container.getWidth() - DIALOGUE_PADDING * 2,
            DIALOGUE_PANEL_HEIGHT - DIALOGUE_OPTIONS_HEIGHT_OFFSET
        );
        container.addChild(optionsScroll);
        rebuildOptionList();

        Button back = createActionButton("Back", () -> {
            dialogueMode = false;
            syncVisibility();
        });
        back.setFrame(
            container.getWidth() - BACK_BUTTON_WIDTH - BACK_BUTTON_MARGIN,
            DIALOGUE_PADDING,
            BACK_BUTTON_WIDTH,
            BACK_BUTTON_HEIGHT
        );
        container.addChild(back);

        root.addChild(dialoguePanel);
    }

    private void rebuildOptionList() {
        UIElement list = new UIElement() {};
        int y = 0;
        int optionWidth = optionsScroll == null ? 0 : optionsScroll.getWidth();
        for (DialogueOption option : dialogueOptions) {
            Button btn = new Button(option.text().getString(), theme);
            btn.setFrame(0, y, optionWidth, OPTION_ROW_HEIGHT);
            btn.setOnClick(() -> sendDialogueOption(option));
            list.addChild(btn);
            y += OPTION_ROW_HEIGHT + OPTION_ROW_GAP;
        }
        list.setFrame(0, 0, optionWidth, Math.max(y, OPTION_ROW_HEIGHT));
        optionsScroll.setContent(list);
    }

    private void sendDialogueOption(DialogueOption option) {
        CompoundTag tag = new CompoundTag();
        tag.putString("payload", option.payload());
        sendAction(
            InteractActionPayload.ActionType.CHAT,
            option.actionId(),
            tag
        );
    }

    private void sendAction(
        InteractActionPayload.ActionType type,
        ResourceLocation actionId,
        CompoundTag extra
    ) {
        PacketDistributor.sendToServer(
            new InteractActionPayload(
                npcEntityId,
                type,
                actionId,
                extra == null ? new CompoundTag() : extra
            )
        );
    }

    private void syncVisibility() {
        dashboardPanel.setVisible(!dialogueMode);
        dialoguePanel.setVisible(dialogueMode);
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPreview(graphics, mouseX, mouseY);
    }

    private void renderPreview(
        GuiGraphics graphics,
        int mouseX,
        int mouseY
    ) {
        if (previewPanel == null || dialogueMode) {
            return;
        }
        Entity entity = getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        int centerX = previewPanel.getAbsoluteX() + previewPanel.getWidth() / 2;
        int centerY =
            previewPanel.getAbsoluteY() +
            previewPanel.getHeight() -
            PREVIEW_FOOTER;
        int dx = centerX - mouseX;
        int dy = centerY - mouseY;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            graphics,
            centerX,
            centerY,
            PREVIEW_SCALE,
            dx,
            dy,
            0.0F,
            0.0F,
            0.0F,
            living
        );
    }

    private Entity getEntity() {
        if (minecraft == null || minecraft.level == null) {
            return null;
        }
        return minecraft.level.getEntity(npcEntityId);
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, path);
    }

    /**
     * 简易背景面板，使用主题色填充并绘制边框。
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
