package com.Kizunad.guzhenrenext.faction.client;

import com.Kizunad.guzhenrenext.client.GuKeyBindings;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.SolidPanel;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.ScaleConfig;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class FactionInfoScreen extends TinyUIScreen {

    private static final int WINDOW_WIDTH = 360;
    private static final int WINDOW_HEIGHT = 224;
    private static final int HEADER_HEIGHT = 42;
    private static final int BODY_Y = 58;
    private static final int BODY_HEIGHT = 126;
    private static final int FOOTER_Y = 190;
    private static final int FOOTER_HEIGHT = 24;
    private static final int CONTENT_PADDING = 10;
    private static final int LINE_HEIGHT = 16;
    private static final int FIRST_LINE_Y = 12;
    private static final int BUTTON_WIDTH = 64;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_RIGHT_MARGIN = 8;
    private static final int FOOTER_BUTTON_GAP = 6;
    private static final int CLOSE_HINT_HEIGHT = 12;
    private static final int TITLE_Y = 10;
    private static final int SYNC_STATUS_Y = 26;
    private static final int MEMBER_COUNT_LINE_MULTIPLIER = 2;
    private static final int POWER_LINE_MULTIPLIER = 3;
    private static final int RESOURCES_LINE_MULTIPLIER = 4;
    private static final int PLAYER_RELATION_LINE_MULTIPLIER = 5;
    private static final int FOOTER_BUTTON_Y = 3;
    private static final int FOOTER_HINT_Y = 6;
    private static final int SAME_FACTION_RELATION = 100;
    private static final int HOSTILE_THRESHOLD = -50;
    private static final int FRIENDLY_THRESHOLD = 50;
    private static final int ALLIED_THRESHOLD = 80;

    private final Theme theme = Theme.vanilla();

    private Label syncStatusLabel;
    private Label factionNameLabel;
    private Label factionTypeLabel;
    private Label memberCountLabel;
    private Label factionPowerLabel;
    private Label factionResourcesLabel;
    private Label playerRelationLabel;

    private FactionInfoSnapshot lastRenderedSnapshot = FactionInfoSnapshot.pending();

    public FactionInfoScreen() {
        super(
            Component.translatable("screen.guzhenrenext.faction_info.title"),
            new UIRoot()
        );
    }

    @Override
    protected void init() {
        super.init();
        rebuildUi();
        applySnapshot(FactionInfoClientState.currentSnapshot());
    }

    @Override
    public void tick() {
        super.tick();
        final FactionInfoSnapshot snapshot = FactionInfoClientState.currentSnapshot();
        if (!snapshot.equals(lastRenderedSnapshot)) {
            applySnapshot(snapshot);
        }
    }

    @Override
    public boolean keyPressed(
        final int keyCode,
        final int scanCode,
        final int modifiers
    ) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == resolveOpenKeyCode()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void rebuildUi() {
        final UIRoot root = getRoot();
        applyUiScale(root);
        root.clearChildren();

        final int rootX = (root.getWidth() - WINDOW_WIDTH) / 2;
        final int rootY = (root.getHeight() - WINDOW_HEIGHT) / 2;

        final UIElement window = new UIElement() { };
        window.setFrame(rootX, rootY, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(window);

        final SolidPanel headerPanel = new SolidPanel(theme);
        headerPanel.setFrame(0, 0, WINDOW_WIDTH, HEADER_HEIGHT);
        window.addChild(headerPanel);

        final SolidPanel bodyPanel = new SolidPanel(theme);
        bodyPanel.setFrame(0, BODY_Y, WINDOW_WIDTH, BODY_HEIGHT);
        window.addChild(bodyPanel);

        final SolidPanel footerPanel = new SolidPanel(theme);
        footerPanel.setFrame(0, FOOTER_Y, WINDOW_WIDTH, FOOTER_HEIGHT);
        window.addChild(footerPanel);

        final Label sectionTitleLabel = new Label(
            Component.translatable("screen.guzhenrenext.faction_info.title"),
            theme
        );
        sectionTitleLabel.setFrame(0, TITLE_Y, WINDOW_WIDTH, LINE_HEIGHT);
        sectionTitleLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        sectionTitleLabel.setColor(theme.getAccentColor());
        headerPanel.addChild(sectionTitleLabel);

        syncStatusLabel = new Label(Component.empty(), theme);
        syncStatusLabel.setFrame(
            CONTENT_PADDING,
            SYNC_STATUS_Y,
            WINDOW_WIDTH - CONTENT_PADDING * 2,
            LINE_HEIGHT
        );
        syncStatusLabel.setColor(theme.getTextColor());
        headerPanel.addChild(syncStatusLabel);

        factionNameLabel = createDataLabel(bodyPanel, FIRST_LINE_Y);
        factionTypeLabel = createDataLabel(bodyPanel, FIRST_LINE_Y + LINE_HEIGHT);
        memberCountLabel = createDataLabel(
            bodyPanel,
            FIRST_LINE_Y + LINE_HEIGHT * MEMBER_COUNT_LINE_MULTIPLIER
        );
        factionPowerLabel = createDataLabel(
            bodyPanel,
            FIRST_LINE_Y + LINE_HEIGHT * POWER_LINE_MULTIPLIER
        );
        factionResourcesLabel = createDataLabel(
            bodyPanel,
            FIRST_LINE_Y + LINE_HEIGHT * RESOURCES_LINE_MULTIPLIER
        );
        playerRelationLabel = createDataLabel(
            bodyPanel,
            FIRST_LINE_Y + LINE_HEIGHT * PLAYER_RELATION_LINE_MULTIPLIER
        );

        final Button closeButton = new Button(
            Component.translatable("screen.guzhenrenext.faction_info.button.close"),
            theme
        );
        closeButton.setFrame(
            WINDOW_WIDTH - BUTTON_WIDTH - BUTTON_RIGHT_MARGIN,
            FOOTER_BUTTON_Y,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        closeButton.setOnClick(this::onClose);
        footerPanel.addChild(closeButton);

        final Button relationGraphButton = new Button(Component.literal("关系图"), theme);
        relationGraphButton.setFrame(
            WINDOW_WIDTH - BUTTON_WIDTH * 2 - BUTTON_RIGHT_MARGIN - FOOTER_BUTTON_GAP,
            FOOTER_BUTTON_Y,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        relationGraphButton.setOnClick(() -> {
            if (minecraft != null) {
                minecraft.setScreen(new FactionRelationGraph());
            }
        });
        footerPanel.addChild(relationGraphButton);

        final Label closeHintLabel = new Label(
            Component.translatable("screen.guzhenrenext.faction_info.close_hint"),
            theme
        );
        closeHintLabel.setFrame(
            CONTENT_PADDING,
            FOOTER_HINT_Y,
            WINDOW_WIDTH - CONTENT_PADDING * 2 - BUTTON_WIDTH * 2 - FOOTER_BUTTON_GAP,
            CLOSE_HINT_HEIGHT
        );
        closeHintLabel.setColor(theme.getTextColor());
        footerPanel.addChild(closeHintLabel);
    }

    private void applyUiScale(final UIRoot root) {
        root.getScaleConfig().setScaleMode(ScaleConfig.ScaleMode.CUSTOM);
        root.getScaleConfig().setCustomScaleFactor(1.0D);
        root.setDesignResolution(width, height);
        root.setViewport(width, height);
    }

    private Label createDataLabel(final UIElement parent, final int y) {
        final Label label = new Label(Component.empty(), theme);
        label.setFrame(
            CONTENT_PADDING,
            y,
            WINDOW_WIDTH - CONTENT_PADDING * 2,
            LINE_HEIGHT
        );
        label.setColor(theme.getTextColor());
        parent.addChild(label);
        return label;
    }

    private void applySnapshot(final FactionInfoSnapshot snapshot) {
        lastRenderedSnapshot = snapshot;
        if (!snapshot.synced()) {
            syncStatusLabel.setText(
                Component.translatable("screen.guzhenrenext.faction_info.sync_pending")
            );
            factionNameLabel.setText(Component.empty());
            factionTypeLabel.setText(Component.empty());
            memberCountLabel.setText(Component.empty());
            factionPowerLabel.setText(Component.empty());
            factionResourcesLabel.setText(Component.empty());
            playerRelationLabel.setText(Component.empty());
            return;
        }

        if (!snapshot.hasDisplayFaction()) {
            syncStatusLabel.setText(
                Component.translatable("screen.guzhenrenext.faction_info.no_data")
            );
            factionNameLabel.setText(
                Component.translatable(
                    "screen.guzhenrenext.faction_info.line.name",
                    Component.translatable("screen.guzhenrenext.faction_info.value.none")
                )
            );
            factionTypeLabel.setText(
                Component.translatable(
                    "screen.guzhenrenext.faction_info.line.type",
                    Component.translatable("screen.guzhenrenext.faction_info.value.none")
                )
            );
            memberCountLabel.setText(
                Component.translatable("screen.guzhenrenext.faction_info.line.member_count", 0)
            );
            factionPowerLabel.setText(
                Component.translatable("screen.guzhenrenext.faction_info.line.power", 0)
            );
            factionResourcesLabel.setText(
                Component.translatable("screen.guzhenrenext.faction_info.line.resources", 0)
            );
            playerRelationLabel.setText(
                Component.translatable("screen.guzhenrenext.faction_info.line.player_relation", 0)
            );
            return;
        }

        syncStatusLabel.setText(buildStatusLine(snapshot.playerRelationValue()));
        factionNameLabel.setText(
            Component.translatable(
                "screen.guzhenrenext.faction_info.line.name",
                snapshot.factionName()
            )
        );
        factionTypeLabel.setText(
            Component.translatable(
                "screen.guzhenrenext.faction_info.line.type",
                resolveFactionTypeText(snapshot.factionType())
            )
        );
        memberCountLabel.setText(
            Component.translatable(
                "screen.guzhenrenext.faction_info.line.member_count",
                snapshot.memberCount()
            )
        );
        factionPowerLabel.setText(
            Component.translatable(
                "screen.guzhenrenext.faction_info.line.power",
                snapshot.power()
            )
        );
        factionResourcesLabel.setText(
            Component.translatable(
                "screen.guzhenrenext.faction_info.line.resources",
                snapshot.resources()
            )
        );
        playerRelationLabel.setText(
            Component.translatable(
                "screen.guzhenrenext.faction_info.line.player_relation",
                snapshot.playerRelationValue()
            )
        );
    }

    private Component buildStatusLine(final int relationValue) {
        return Component.translatable(
            "screen.guzhenrenext.faction_info.line.status",
            resolveRelationText(relationValue)
        );
    }

    private Component resolveFactionTypeText(final String factionType) {
        return switch (factionType) {
            case "SECT" -> Component.translatable(
                "screen.guzhenrenext.faction_info.type.sect"
            );
            case "CLAN" -> Component.translatable(
                "screen.guzhenrenext.faction_info.type.clan"
            );
            case "ROGUE_GROUP" -> Component.translatable(
                "screen.guzhenrenext.faction_info.type.rogue_group"
            );
            default -> Component.translatable(
                "screen.guzhenrenext.faction_info.value.none"
            );
        };
    }

    private Component resolveRelationText(final int relationValue) {
        if (relationValue >= SAME_FACTION_RELATION) {
            return Component.translatable(
                "screen.guzhenrenext.faction_info.status.same_faction"
            );
        }
        if (relationValue > ALLIED_THRESHOLD) {
            return Component.translatable(
                "screen.guzhenrenext.faction_info.status.allied"
            );
        }
        if (relationValue > FRIENDLY_THRESHOLD) {
            return Component.translatable(
                "screen.guzhenrenext.faction_info.status.friendly"
            );
        }
        if (relationValue < HOSTILE_THRESHOLD) {
            return Component.translatable(
                "screen.guzhenrenext.faction_info.status.hostile"
            );
        }
        return Component.translatable(
            "screen.guzhenrenext.faction_info.status.neutral"
        );
    }

    private static int resolveOpenKeyCode() {
        return GuKeyBindings.OPEN_FACTION_INFO.getKey().getValue();
    }
}
