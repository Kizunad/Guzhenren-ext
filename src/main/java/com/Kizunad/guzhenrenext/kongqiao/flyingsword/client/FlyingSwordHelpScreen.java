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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * 飞剑帮助百科界面。
 * <p>
 * 提供飞剑培养的完整帮助信息，分为 5 个页签：概览、品质、战斗、成长、本命。
 * 本命页签承接 T10 的深度帮助层，同时在同一文件内提供一次性首次引导的最小路由骨架，
 * 确保“first guide 只给一步短提示，完整解释进入 Help Screen”这一约束可被后续任务复用。
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

    private static final int TAB_INDEX_OVERVIEW = 0;
    private static final int TAB_INDEX_QUALITY = 1;
    private static final int TAB_INDEX_COMBAT = 2;
    private static final int TAB_INDEX_GROWTH = 3;
    private static final int TAB_INDEX_BENMING = 4;

    private static final String[] TAB_KEYS = {
        KongqiaoI18n.HELP_TAB_OVERVIEW,
        KongqiaoI18n.HELP_TAB_QUALITY,
        KongqiaoI18n.HELP_TAB_COMBAT,
        KongqiaoI18n.HELP_TAB_GROWTH,
        KongqiaoI18n.HELP_TAB_BENMING,
    };

    private static final int TAB_COUNT = TAB_KEYS.length;

    // given: 首次引导只允许给出一步短提示
    // when: 业务层需要把 topic 导向 Help 深度层
    // then: 统一落到本命页签，并保留对应的帮助条目 key
    private static final Map<String, String> BENMING_GUIDE_TO_HELP_ENTRY = Map.of(
        KongqiaoI18n.BENMING_GUIDE_BOND_START,
        KongqiaoI18n.HELP_BENMING_BOND_ENTRY,
        KongqiaoI18n.BENMING_GUIDE_AFTER_BOND,
        KongqiaoI18n.HELP_BENMING_BOND_AFTER_SUCCESS,
        KongqiaoI18n.BENMING_GUIDE_BOND_FAIL_NEXT_STEP,
        KongqiaoI18n.HELP_BENMING_FAIL_BOND,
        KongqiaoI18n.BENMING_GUIDE_RESONANCE_FIRST_CHOICE,
        KongqiaoI18n.HELP_BENMING_RESONANCE_GUIDE,
        KongqiaoI18n.BENMING_GUIDE_OVERLOAD_FIRST_WARNING,
        KongqiaoI18n.HELP_BENMING_OVERLOAD_WARNING,
        KongqiaoI18n.BENMING_GUIDE_BACKLASH_FIRST_TIME,
        KongqiaoI18n.HELP_BENMING_BACKLASH_RULE,
        KongqiaoI18n.BENMING_GUIDE_BURST_READY_FIRST_TIME,
        KongqiaoI18n.HELP_BENMING_BURST_WINDOW,
        KongqiaoI18n.BENMING_GUIDE_AFTERSHOCK_FIRST_TIME,
        KongqiaoI18n.HELP_BENMING_AFTERSHOCK_RULE
    );

    private static final String DEFAULT_BENMING_HELP_ENTRY =
        KongqiaoI18n.HELP_BENMING_OVERVIEW;

    private final Theme theme = Theme.vanilla();
    private final int initialTab;

    private int currentTab = TAB_INDEX_OVERVIEW;

    private UIElement contentContainer;
    private ScrollContainer scrollContainer;
    private final Button[] tabButtons = new Button[TAB_COUNT];

    public FlyingSwordHelpScreen() {
        this(TAB_INDEX_OVERVIEW);
    }

    public FlyingSwordHelpScreen(final int initialTab) {
        super(
            KongqiaoI18n.text(KongqiaoI18n.HELP_TITLE),
            createRoot()
        );
        this.initialTab = sanitizeTabIndex(initialTab);
        this.currentTab = this.initialTab;
    }

    private static UIRoot createRoot() {
        return new UIRoot();
    }

    public static FlyingSwordHelpScreen openBenmingHelp() {
        return new FlyingSwordHelpScreen(TAB_INDEX_BENMING);
    }

    static List<String> helpTabKeys() {
        return List.copyOf(Arrays.asList(TAB_KEYS));
    }

    static int benmingTabIndex() {
        return TAB_INDEX_BENMING;
    }

    int currentTabForTesting() {
        return currentTab;
    }

    public static BenmingHelpRoute routeForBenmingGuide(final String topicKey) {
        final String normalizedTopic = normalizeGuideTopicKey(topicKey);
        return new BenmingHelpRoute(
            TAB_INDEX_BENMING,
            BENMING_GUIDE_TO_HELP_ENTRY.getOrDefault(
                normalizedTopic,
                DEFAULT_BENMING_HELP_ENTRY
            )
        );
    }

    public static Optional<BenmingFirstGuideHint> createBenmingFirstGuideOnce(
        final BenmingFirstGuideState state,
        final String topicKey
    ) {
        if (state == null) {
            return Optional.empty();
        }
        final String normalizedTopic = normalizeGuideTopicKey(topicKey);
        if (normalizedTopic.isBlank()) {
            return Optional.empty();
        }
        if (!state.markFirstSeen(normalizedTopic)) {
            return Optional.empty();
        }
        return Optional.of(
            new BenmingFirstGuideHint(
                normalizedTopic,
                routeForBenmingGuide(normalizedTopic)
            )
        );
    }

    public record BenmingHelpRoute(int tabIndex, String helpEntryKey) {

        public BenmingHelpRoute {
            tabIndex = sanitizeTabIndex(tabIndex);
            helpEntryKey = normalizeHelpEntryKey(helpEntryKey);
        }

        public FlyingSwordHelpScreen openHelpScreen() {
            return new FlyingSwordHelpScreen(tabIndex);
        }
    }

    public record BenmingFirstGuideHint(
        String messageKey,
        BenmingHelpRoute route
    ) {

        public BenmingFirstGuideHint {
            messageKey = normalizeGuideTopicKey(messageKey);
            route = route == null ? routeForBenmingGuide(messageKey) : route;
        }

        public Component messageComponent() {
            return KongqiaoI18n.text(messageKey);
        }

        public FlyingSwordHelpScreen openHelpScreen() {
            return route.openHelpScreen();
        }
    }

    // given: 同一 topic 的首次引导只应展示一次
    // when: 调用方重复请求同一 topic 的 hint
    // then: 仅第一次返回提示，后续只保留已看过状态
    public static final class BenmingFirstGuideState {

        private final Set<String> seenTopics = new LinkedHashSet<>();

        public boolean markFirstSeen(final String topicKey) {
            final String normalizedTopic = normalizeGuideTopicKey(topicKey);
            if (normalizedTopic.isBlank()) {
                return false;
            }
            return seenTopics.add(normalizedTopic);
        }

        public boolean hasSeen(final String topicKey) {
            return seenTopics.contains(normalizeGuideTopicKey(topicKey));
        }

        public Set<String> snapshot() {
            return Set.copyOf(seenTopics);
        }
    }

    @Override
    protected void init() {
        super.init();
        applyUiScale(getRoot());
        getRoot().clearChildren();
        buildUI(getRoot());
        switchTab(initialTab);
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

        final Label titleLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.HELP_TITLE),
            theme
        );
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

        int tabX = WINDOW_PADDING;
        for (int i = 0; i < TAB_KEYS.length; i++) {
            final int index = i;
            final Button tab = new Button(KongqiaoI18n.text(TAB_KEYS[i]), theme);
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

    private void switchTab(final int tabIndex) {
        currentTab = sanitizeTabIndex(tabIndex);
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
            case TAB_INDEX_BENMING -> buildBenmingContent(contentWidth);
            default -> buildOverviewContent(contentWidth);
        }
    }

    private void buildOverviewContent(final int width) {
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
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_KEY_BENMING_ACTION);

        y += SECTION_GAP;
        y = addLabel(width, y, KongqiaoI18n.HELP_OVERVIEW_NOTE);

        contentContainer.setFrame(0, 0, width, y);
    }

    private void buildQualityContent(final int width) {
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

    private void buildCombatContent(final int width) {
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

    private void buildGrowthContent(final int width) {
        int y = 0;

        y = addSectionTitle(width, y, KongqiaoI18n.HELP_GROWTH_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_GROWTH_EXP_DESC);
        y = addLabel(width, y, KongqiaoI18n.HELP_GROWTH_LEVEL_DESC);
        y = addLabel(width, y, KongqiaoI18n.HELP_GROWTH_BREAKTHROUGH_DESC);

        contentContainer.setFrame(0, 0, width, y);
    }

    // given: Help 是本命玩法的深度层
    // when: 玩家进入本命页签
    // then: 这里负责完整解释，first guide 只负责把玩家引到这里
    private void buildBenmingContent(final int width) {
        int y = 0;

        y = addSectionTitle(width, y, KongqiaoI18n.HELP_BENMING_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_OVERVIEW);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_BENMING_GUIDE_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_GUIDE_DESC);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_BENMING_BOND_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_BOND_ENTRY);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_BOND_AFTER_SUCCESS);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_BOND);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_BENMING_RESONANCE_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_RESONANCE_GUIDE);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.RESONANCE_OFFENSE_NAME);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_RESONANCE_OFFENSE);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_OFFENSE_COLOR_SEMANTIC);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_OFFENSE_HUD_CUES);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.RESONANCE_DEFENSE_NAME);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_RESONANCE_DEFENSE);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_DEFENSE_COLOR_SEMANTIC);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_DEFENSE_HUD_CUES);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.RESONANCE_SPIRIT_NAME);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_RESONANCE_SPIRIT);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_SPIRIT_COLOR_SEMANTIC);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_SPIRIT_HUD_CUES);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.RESONANCE_DEVOUR_NAME);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_RESONANCE_DEVOUR);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_DEVOUR_COLOR_SEMANTIC);
        y = addLabel(width, y, KongqiaoI18n.RESONANCE_DEVOUR_HUD_CUES);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_BENMING_RISK_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_OVERLOAD_WARNING);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_BACKLASH_RULE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_RECOVERY_RULE);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_BENMING_BURST_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_BURST_WINDOW);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_BURST_EFFECT);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_AFTERSHOCK_RULE);

        y += SECTION_GAP;
        y = addSectionTitle(width, y, KongqiaoI18n.HELP_BENMING_FAIL_TITLE);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_NO_SELECTED_SWORD);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_NOT_BONDED);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_BOND_COOLDOWN);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_RESOURCE_INSUFFICIENT);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_RESONANCE_LOCKED);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_OVERLOAD_TOO_HIGH);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_BURST_NOT_READY);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_BURST_COOLDOWN);
        y = addLabel(width, y, KongqiaoI18n.HELP_BENMING_FAIL_AFTERSHOCK_ACTIVE);
        y = addLabel(
            width,
            y,
            KongqiaoI18n.HELP_BENMING_FAIL_WITHDRAWN_OR_ILLEGAL_DETACH
        );

        contentContainer.setFrame(0, 0, width, y);
    }

    private int addSectionTitle(final int width, final int y, final String key) {
        final Label label = new Label(KongqiaoI18n.text(key), theme);
        label.setFrame(0, y, width, LABEL_HEIGHT);
        contentContainer.addChild(label);
        return y + LINE_STEP + SECTION_TITLE_PADDING;
    }

    private int addLabel(final int width, final int y, final String key) {
        final Label label = new Label(KongqiaoI18n.text(key), theme);
        label.setFrame(0, y, width, LABEL_HEIGHT * 2);
        contentContainer.addChild(label);
        return y + LINE_STEP * 2;
    }

    private static int sanitizeTabIndex(final int tabIndex) {
        if (tabIndex < TAB_INDEX_OVERVIEW || tabIndex >= TAB_COUNT) {
            return TAB_INDEX_OVERVIEW;
        }
        return tabIndex;
    }

    private static String normalizeGuideTopicKey(final String topicKey) {
        if (topicKey == null) {
            return "";
        }
        return topicKey.trim();
    }

    private static String normalizeHelpEntryKey(final String helpEntryKey) {
        if (helpEntryKey == null || helpEntryKey.isBlank()) {
            return DEFAULT_BENMING_HELP_ENTRY;
        }
        return helpEntryKey;
    }
}
