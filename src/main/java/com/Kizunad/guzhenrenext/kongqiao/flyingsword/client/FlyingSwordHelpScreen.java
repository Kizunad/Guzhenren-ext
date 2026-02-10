package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.config.ClientConfig;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.SolidPanel;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.ScaleConfig;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * 飞剑帮助百科界面。
 * <p>
 * 提供飞剑培养的完整帮助信息，分为4个页签：概览、品质、战斗、成长。
 * 继承 {@link TinyUIScreen}，纯客户端展示，不涉及任何网络通信。
 * </p>
 */
public final class FlyingSwordHelpScreen extends TinyUIScreen {

    private static final int WINDOW_WIDTH = 680;
    private static final int WINDOW_HEIGHT = 400;
    private static final int WINDOW_PADDING = 10;
    private static final int TITLE_HEIGHT = 20;
    private static final int TITLE_Y = 5;
    private static final int CLOSE_BUTTON_MARGIN = 5;
    private static final int CLOSE_BUTTON_SIZE = 20;
    private static final int TAB_BUTTON_WIDTH = 80;
    private static final int TAB_BUTTON_HEIGHT = 24;
    private static final int TAB_BUTTON_Y = 25;
    private static final int TAB_SPACING = 5;
    private static final int SCROLL_TOP = 50;
    private static final int SCROLL_BOTTOM = 10;
    private static final int SECTION_TITLE_PADDING = 4;

    private static final int LABEL_HEIGHT = 16;
    private static final int LINE_STEP = 18;
    private static final int SECTION_GAP = 12;

    private static final int TAB_COUNT = 4;
    private static final int TAB_INDEX_OVERVIEW = 0;
    private static final int TAB_INDEX_QUALITY = 1;
    private static final int TAB_INDEX_COMBAT = 2;
    private static final int TAB_INDEX_GROWTH = 3;

    private final Theme theme = Theme.vanilla();

    private int currentTab = TAB_INDEX_OVERVIEW;

    private UIElement contentContainer;
    private ScrollContainer scrollContainer;
    private final Button[] tabButtons = new Button[TAB_COUNT];

    public FlyingSwordHelpScreen() {
        super(
            KongqiaoI18n.text(KongqiaoI18n.HELP_TITLE),
            createRoot()
        );
    }

    private static UIRoot createRoot() {
        return new UIRoot();
    }

    @Override
    protected void init() {
        super.init();
        applyUiScale(getRoot());
        getRoot().clearChildren();
        buildUI(getRoot());
        switchTab(TAB_INDEX_OVERVIEW);
    }

    private void applyUiScale(final UIRoot root) {
        final double uiScale = sanitizeScaleFactor(
            ClientConfig.INSTANCE.kongQiaoUiScale.get()
        );
        root.getScaleConfig().setScaleMode(ScaleConfig.ScaleMode.CUSTOM);
        root.getScaleConfig().setCustomScaleFactor(uiScale);
        root.setDesignResolution(
            (int) Math.round((double) width / uiScale),
            (int) Math.round((double) height / uiScale)
        );
        root.setViewport(width, height);
    }

    private static double sanitizeScaleFactor(final double scaleFactor) {
        if (Double.isNaN(scaleFactor) || Double.isInfinite(scaleFactor)) {
            return 1.0;
        }
        if (scaleFactor <= 0.0) {
            return 1.0;
        }
        return scaleFactor;
    }

    private void buildUI(final UIRoot root) {
        final UIElement window = new UIElement() { };
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

        final SolidPanel background = new SolidPanel(theme);
        background.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.addChild(background);

        final Label titleLabel = new Label(KongqiaoI18n.text(KongqiaoI18n.HELP_TITLE), theme);
        titleLabel.setFrame(0, TITLE_Y, WINDOW_WIDTH, TITLE_HEIGHT);
        titleLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        window.addChild(titleLabel);

        final Button closeButton = new Button("x", theme);
        closeButton.setFrame(
            WINDOW_WIDTH - CLOSE_BUTTON_SIZE - CLOSE_BUTTON_MARGIN,
            CLOSE_BUTTON_MARGIN,
            CLOSE_BUTTON_SIZE,
            CLOSE_BUTTON_SIZE
        );
        closeButton.setOnClick(() -> Minecraft.getInstance().setScreen(null));
        window.addChild(closeButton);

        final String[] tabKeys = {
            KongqiaoI18n.HELP_TAB_OVERVIEW,
            KongqiaoI18n.HELP_TAB_QUALITY,
            KongqiaoI18n.HELP_TAB_COMBAT,
            KongqiaoI18n.HELP_TAB_GROWTH
        };

        int tabX = WINDOW_PADDING;
        for (int i = 0; i < tabKeys.length; i++) {
            final int index = i;
            final Button tab = new Button(KongqiaoI18n.text(tabKeys[i]), theme);
            tab.setFrame(tabX, TAB_BUTTON_Y, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT);
            tab.setOnClick(() -> switchTab(index));
            tabButtons[i] = tab;
            window.addChild(tab);
            tabX += TAB_BUTTON_WIDTH + TAB_SPACING;
        }

        scrollContainer = new ScrollContainer(theme);
        scrollContainer.setFrame(
            WINDOW_PADDING,
            SCROLL_TOP,
            WINDOW_WIDTH - WINDOW_PADDING * 2,
            WINDOW_HEIGHT - SCROLL_TOP - SCROLL_BOTTOM
        );
        window.addChild(scrollContainer);

        contentContainer = new UIElement() { };
        scrollContainer.setContent(contentContainer);
    }

    private void switchTab(int tabIndex) {
        currentTab = tabIndex;
        // 更新按钮状态
        for (int i = 0; i < tabButtons.length; i++) {
            if (tabButtons[i] != null) {
                tabButtons[i].setEnabled(i != currentTab);
            }
        }
        rebuildContent();
    }

    private void rebuildContent() {
        if (contentContainer == null) {
            return;
        }
        contentContainer.clearChildren();

        final int contentWidth = scrollContainer.getWidth();

        switch (currentTab) {
            case TAB_INDEX_OVERVIEW -> buildOverviewContent(contentWidth);
            case TAB_INDEX_QUALITY -> buildQualityContent(contentWidth);
            case TAB_INDEX_COMBAT -> buildCombatContent(contentWidth);
            case TAB_INDEX_GROWTH -> buildGrowthContent(contentWidth);
            default -> buildOverviewContent(contentWidth);
        }
    }

    private void buildOverviewContent(int width) {
        int y = 0;

        y = addSectionTitle(width, y, KongqiaoI18n.HELP_OVERVIEW_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_STEP1);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_STEP2);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_STEP3);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_STEP4);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_STEP5);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_OVERVIEW_KEYS_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_KEY_SELECT);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_KEY_MODE);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_KEY_RECALL);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_KEY_RESTORE);
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_KEY_HUD);

        y += SECTION_GAP;
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_NOTE);

        contentContainer.setFrame(0, 0, width, y);
    }

    private void buildQualityContent(int width) {
        int y = 0;

        y = addSectionTitle(width, y, KongqiaoI18n.HELP_QUALITY_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_QUALITY_DESC);
        y += SECTION_GAP;

        for (SwordQuality quality : SwordQuality.values()) {
            final Component text = KongqiaoI18n.text(
                KongqiaoI18n.HELP_QUALITY_ENTRY,
                quality.getDisplayName(),
                quality.getDamageMultiplier(),
                quality.getSpeedMultiplier(),
                quality.getMaxLevel()
            );
            final Label label = new Label(text, theme);
            label.setFrame(0, y, width, LABEL_HEIGHT);
            contentContainer.addChild(label);
            y += LINE_STEP;
        }

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_QUALITY_DAOHEN_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_QUALITY_DAOHEN_DESC);
        y = addLabel(width, y, KongqiaoI18n.HELP_QUALITY_DAOHEN_NOTE);

        contentContainer.setFrame(0, 0, width, y);
    }

    private void buildCombatContent(int width) {
        int y = 0;

        y = addSectionTitle(width, y, KongqiaoI18n.HELP_COMBAT_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_COMBAT_MODE_ORBIT);
        y = addLabel(width, y, KongqiaoI18n.HELP_COMBAT_MODE_HOVER);
        y = addLabel(width, y, KongqiaoI18n.HELP_COMBAT_MODE_GUARD);
        y = addLabel(width, y, KongqiaoI18n.HELP_COMBAT_MODE_HUNT);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_COMBAT_IMPRINT_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_COMBAT_IMPRINT_DESC);

        contentContainer.setFrame(0, 0, width, y);
    }

    private void buildGrowthContent(int width) {
        int y = 0;

        y = addSectionTitle(width, y, KongqiaoI18n.HELP_GROWTH_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_GROWTH_EXP_DESC);
        y = addLabel(width, y, KongqiaoI18n.HELP_GROWTH_LEVEL_DESC);
        y = addLabel(width, y, KongqiaoI18n.HELP_GROWTH_BREAKTHROUGH_DESC);

        contentContainer.setFrame(0, 0, width, y);
    }

    private int addSectionTitle(int width, int y, String key) {
        final Label label = new Label(KongqiaoI18n.text(key), theme);
        label.setFrame(0, y, width, LABEL_HEIGHT);
        contentContainer.addChild(label);
        return y + LINE_STEP + SECTION_TITLE_PADDING;
    }

    private int addLabel(int width, int y, String key) {
        final Label label = new Label(KongqiaoI18n.text(key), theme);
        label.setFrame(0, y, width, LABEL_HEIGHT * 2);
        contentContainer.addChild(label);
        return y + LINE_STEP * 2;
    }
}
