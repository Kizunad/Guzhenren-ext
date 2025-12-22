package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.network.ServerboundTweakConfigUpdatePayload;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouUsageId;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoId;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.ScaleConfig;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 空窍调整面板 UI（Tweak UI）。
 * <p>
 * 布局严格参考 {@code tmp/tweak/TweakUI.drawio}：
 * <ul>
 *   <li>左侧：解锁列表（复用 NianTouUI 的按钮+滚动布局）。</li>
 *   <li>右侧：用途调整卡片（被动开关、主动技能加入/移出轮盘）。</li>
 * </ul>
 * 采用 TinyUI 设计分辨率缩放，确保不同分辨率下比例一致。
 * </p>
 */
public final class TweakScreen extends TinyUIScreen {

    // drawio 设计窗口：680x320
    private static final int WINDOW_WIDTH = 680;
    private static final int WINDOW_HEIGHT = 320;
    private static final int WINDOW_PADDING = 10;
    private static final int DIVIDER_WIDTH = 10;

    private static final int LEFT_WIDTH = 220;
    private static final int RIGHT_WIDTH = 430;
    private static final int PANEL_HEIGHT = 300;

    private static final int TITLE_HEIGHT = 16;
    private static final int TITLE_MARGIN = 6;
    private static final int SCROLL_TOP = 30;
    private static final int SCROLL_BOTTOM = 10;

    private static final int LIST_ITEM_HEIGHT = 16;
    private static final int LIST_ITEM_STEP = 18;

    private static final int LEFT_CONTENT_PADDING_MULTIPLIER = 4;
    private static final int HASH_SEED = 17;
    private static final int HASH_MULTIPLIER = 31;

    // 右侧卡片布局（严格按 drawio 相对坐标映射）
    private static final int RIGHT_SCROLL_PADDING = 10;
    private static final int CARD_WIDTH = 400;
    private static final int PASSIVE_CARD_HEIGHT = 100;
    private static final int SKILL_CARD_HEIGHT = 90;

    private static final int CARD_TITLE_HEIGHT = 16;
    private static final int CARD_TITLE_Y = 0;
    private static final int CARD_DESC_X = 18;
    private static final int CARD_DESC_Y_PASSIVE = 50;
    private static final int CARD_DESC_Y_SKILL = 42;
    private static final int CARD_DESC_WIDTH = 292;
    private static final int CARD_DESC_HEIGHT = 36;

    private static final int STATUS_LABEL_X = 280;
    private static final int STATUS_LABEL_Y = 35;
    private static final int STATUS_LABEL_WIDTH = 60;
    private static final int STATUS_LABEL_HEIGHT = 12;
    private static final int STATUS_VALUE_X = 335;

    private static final int ACTION_BUTTON_X = 340;
    private static final int TOGGLE_BUTTON_Y = 70;
    private static final int ACTION_BUTTON_Y_ADD = 30;
    private static final int ACTION_BUTTON_Y_REMOVE = 60;
    private static final int ACTION_BUTTON_WIDTH = 50;
    private static final int ACTION_BUTTON_HEIGHT = 20;

    private final Theme theme = Theme.vanilla();

    private UIElement leftContent;
    private UIElement rightContent;
    private int leftContentWidth;

    private ResourceLocation selectedItemId;
    private ResourceLocation selectedShazhaoId;

    private int lastRenderHash;

    public TweakScreen() {
        super(
            Component.translatable("screen.guzhenrenext.tweak.title"),
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
        leftContent = null;
        rightContent = null;
        leftContentWidth = 0;
        selectedItemId = null;
        selectedShazhaoId = null;
        buildUI(getRoot());
        requestSync();
        rebuildLeftContent();
        rebuildRightContent();
    }

    private void applyUiScale(final UIRoot root) {
        final double uiScale = sanitizeScaleFactor(
            com.Kizunad.guzhenrenext.config.ClientConfig.INSTANCE.kongQiaoUiScale.get()
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

    @Override
    public void tick() {
        super.tick();
        final int hash = computeRenderHash();
        if (hash != lastRenderHash) {
            lastRenderHash = hash;
            rebuildLeftContent();
            rebuildRightContent();
        }
    }

    private void requestSync() {
        PacketDistributor.sendToServer(
            new ServerboundTweakConfigUpdatePayload(
                ServerboundTweakConfigUpdatePayload.Action.REQUEST_SYNC,
                "",
                false
            )
        );
    }

    private void buildUI(final UIRoot root) {
        final UIElement window = new UIElement() {};
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

        final SolidPanel leftPanel = new SolidPanel(theme);
        leftPanel.setFrame(
            WINDOW_PADDING,
            WINDOW_PADDING,
            LEFT_WIDTH,
            PANEL_HEIGHT
        );
        window.addChild(leftPanel);

        final Label leftTitle = new Label("已解锁念头/杀招", theme);
        leftTitle.setFrame(0, TITLE_MARGIN, LEFT_WIDTH, TITLE_HEIGHT);
        leftTitle.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        leftPanel.addChild(leftTitle);

        final ScrollContainer leftScroll = new ScrollContainer(theme);
        leftScroll.setFrame(
            WINDOW_PADDING,
            SCROLL_TOP,
            LEFT_WIDTH - WINDOW_PADDING * 2,
            PANEL_HEIGHT - SCROLL_TOP - SCROLL_BOTTOM
        );
        leftPanel.addChild(leftScroll);

        leftContent = new UIElement() {};
        leftContentWidth =
            LEFT_WIDTH - WINDOW_PADDING * LEFT_CONTENT_PADDING_MULTIPLIER;
        leftScroll.setContent(leftContent);

        final SolidPanel divider = new SolidPanel(theme);
        divider.setFrame(
            WINDOW_PADDING + LEFT_WIDTH,
            WINDOW_PADDING,
            DIVIDER_WIDTH,
            PANEL_HEIGHT
        );
        window.addChild(divider);

        final SolidPanel rightPanel = new SolidPanel(theme);
        rightPanel.setFrame(
            WINDOW_PADDING + LEFT_WIDTH + DIVIDER_WIDTH,
            WINDOW_PADDING,
            RIGHT_WIDTH,
            PANEL_HEIGHT
        );
        window.addChild(rightPanel);

        final Label rightTitle = new Label("调整面板", theme);
        rightTitle.setFrame(0, TITLE_MARGIN, RIGHT_WIDTH, TITLE_HEIGHT);
        rightTitle.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        rightPanel.addChild(rightTitle);

        final ScrollContainer rightScroll = new ScrollContainer(theme);
        rightScroll.setFrame(
            RIGHT_SCROLL_PADDING,
            SCROLL_TOP,
            RIGHT_WIDTH - RIGHT_SCROLL_PADDING * 2,
            PANEL_HEIGHT - SCROLL_TOP - SCROLL_BOTTOM
        );
        rightPanel.addChild(rightScroll);

        rightContent = new UIElement() {};
        rightScroll.setContent(rightContent);
    }

    private int computeRenderHash() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }
        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(
            minecraft.player
        );
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(
            minecraft.player
        );
        int hash = HASH_SEED;
        hash =
            HASH_MULTIPLIER * hash +
            (selectedItemId == null ? 0 : selectedItemId.hashCode());
        hash =
            HASH_MULTIPLIER * hash +
            (selectedShazhaoId == null ? 0 : selectedShazhaoId.hashCode());
        hash =
            HASH_MULTIPLIER * hash +
            (unlocks == null ? 0 : unlocks.getUnlockedUsageMap().hashCode());
        hash =
            HASH_MULTIPLIER * hash +
            (unlocks == null ? 0 : unlocks.getUnlockedShazhao().hashCode());
        hash =
            HASH_MULTIPLIER * hash +
            (config == null ? 0 : config.getDisabledPassives().hashCode());
        hash =
            HASH_MULTIPLIER * hash +
            (config == null ? 0 : config.getWheelSkills().hashCode());
        return hash;
    }

    private void rebuildLeftContent() {
        if (leftContent == null || leftContentWidth <= 0) {
            return;
        }
        leftContent.clearChildren();

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            leftContent.setFrame(0, 0, leftContentWidth, LIST_ITEM_HEIGHT);
            return;
        }

        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(
            minecraft.player
        );
        final Map<ResourceLocation, Set<String>> unlockedMap = unlocks != null
            ? unlocks.getUnlockedUsageMap()
            : Map.of();
        final Set<ResourceLocation> unlockedShazhao = unlocks != null
            ? unlocks.getUnlockedShazhao()
            : Set.of();

        if (selectedItemId != null && !unlockedMap.containsKey(selectedItemId)) {
            selectedItemId = null;
        }
        if (
            selectedShazhaoId != null &&
            !unlockedShazhao.contains(selectedShazhaoId)
        ) {
            selectedShazhaoId = null;
        }

        int y = 0;
        ResourceLocation firstItem = null;
        ResourceLocation firstShazhao = null;

        if (!unlockedMap.isEmpty()) {
            final Label section = new Label("念头", theme);
            section.setFrame(0, y, leftContentWidth, LIST_ITEM_HEIGHT);
            leftContent.addChild(section);
            y += LIST_ITEM_STEP;

            final List<ResourceLocation> sortedItems =
                new ArrayList<>(unlockedMap.keySet());
            sortedItems.sort((a, b) -> a.toString().compareTo(b.toString()));
            for (ResourceLocation id : sortedItems) {
                final Item item = BuiltInRegistries.ITEM.get(id);
                if (item == Items.AIR) {
                    continue;
                }

                if (firstItem == null) {
                    firstItem = id;
                }

                final NianTouData data = NianTouDataManager.getData(item);
                final int totalUsages = (data != null && data.usages() != null)
                    ? data.usages().size()
                    : unlockedMap.get(id).size();
                int unlockedCount = 0;
                if (data != null && data.usages() != null && unlocks != null) {
                    for (NianTouData.Usage usage : data.usages()) {
                        if (usage == null) {
                            continue;
                        }
                        if (unlocks.isUsageUnlocked(id, usage.usageID())) {
                            unlockedCount++;
                        }
                    }
                } else {
                    unlockedCount = unlockedMap.get(id).size();
                }

                final Component label = item
                    .getDescription()
                    .copy()
                    .append(" (" + unlockedCount + "/" + totalUsages + ")");
                final Button button = new Button(label, theme);
                button.setFrame(0, y, leftContentWidth, LIST_ITEM_HEIGHT);
                button.setOnClick(() -> {
                    selectedItemId = id;
                    selectedShazhaoId = null;
                    rebuildRightContent();
                });
                leftContent.addChild(button);
                y += LIST_ITEM_STEP;
            }
        }

        if (!unlockedShazhao.isEmpty()) {
            if (y > 0) {
                y += LIST_ITEM_STEP / 2;
            }
            final Label section = new Label("杀招", theme);
            section.setFrame(0, y, leftContentWidth, LIST_ITEM_HEIGHT);
            leftContent.addChild(section);
            y += LIST_ITEM_STEP;

            final List<ResourceLocation> sortedShazhao =
                new ArrayList<>(unlockedShazhao);
            sortedShazhao.sort((a, b) -> a.toString().compareTo(b.toString()));
            for (ResourceLocation id : sortedShazhao) {
                if (firstShazhao == null) {
                    firstShazhao = id;
                }
                final ShazhaoData data = ShazhaoDataManager.get(id);
                final String title = data != null && data.title() != null
                    ? data.title()
                    : id.toString();
                final Button button =
                    new Button(Component.literal(title), theme);
                button.setFrame(0, y, leftContentWidth, LIST_ITEM_HEIGHT);
                button.setOnClick(() -> {
                    selectedShazhaoId = id;
                    selectedItemId = null;
                    rebuildRightContent();
                });
                leftContent.addChild(button);
                y += LIST_ITEM_STEP;
            }
        }

        if (y == 0) {
            final Label label = new Label("暂无解锁内容。", theme);
            label.setFrame(0, 0, leftContentWidth, LIST_ITEM_HEIGHT);
            leftContent.addChild(label);
            y = LIST_ITEM_HEIGHT;
        }

        if (selectedItemId == null && selectedShazhaoId == null) {
            if (firstItem != null) {
                selectedItemId = firstItem;
            } else if (firstShazhao != null) {
                selectedShazhaoId = firstShazhao;
            }
        }

        leftContent.setFrame(
            0,
            0,
            leftContentWidth,
            Math.max(LIST_ITEM_HEIGHT, y)
        );
    }

    private void rebuildRightContent() {
        if (rightContent == null) {
            return;
        }
        rightContent.clearChildren();

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            rightContent.setFrame(0, 0, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
            final Label label = new Label("请选择左侧已解锁的内容。", theme);
            label.setFrame(0, 0, CARD_WIDTH, TITLE_HEIGHT * 2);
            rightContent.addChild(label);
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(
            minecraft.player
        );

        if (selectedShazhaoId != null) {
            final ShazhaoData data = ShazhaoDataManager.get(selectedShazhaoId);
            if (data == null) {
                rightContent.setFrame(0, 0, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
                final Label label = new Label("未找到该杀招配置。", theme);
                label.setFrame(0, 0, CARD_WIDTH, TITLE_HEIGHT * 2);
                rightContent.addChild(label);
                return;
            }

            int y = 0;
            if (ShazhaoId.isPassive(data.shazhaoID())) {
                addShazhaoPassiveCard(config, data, y);
                y += PASSIVE_CARD_HEIGHT;
            } else if (ShazhaoId.isActive(data.shazhaoID())) {
                addShazhaoActiveCard(config, data, y);
                y += SKILL_CARD_HEIGHT;
            } else {
                rightContent.setFrame(0, 0, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
                final Label label = new Label("杀招命名不符合规范。", theme);
                label.setFrame(0, 0, CARD_WIDTH, TITLE_HEIGHT * 2);
                rightContent.addChild(label);
                return;
            }

            rightContent.setFrame(
                0,
                0,
                CARD_WIDTH,
                Math.max(PASSIVE_CARD_HEIGHT, y)
            );
            return;
        }

        if (selectedItemId == null) {
            rightContent.setFrame(0, 0, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
            final Label label = new Label("请选择左侧已解锁的内容。", theme);
            label.setFrame(0, 0, CARD_WIDTH, TITLE_HEIGHT * 2);
            rightContent.addChild(label);
            return;
        }

        final Item item = BuiltInRegistries.ITEM.get(selectedItemId);
        if (item == Items.AIR) {
            rightContent.setFrame(0, 0, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
            final Label label = new Label("物品不存在。", theme);
            label.setFrame(0, 0, CARD_WIDTH, TITLE_HEIGHT * 2);
            rightContent.addChild(label);
            return;
        }

        final NianTouData data = NianTouDataManager.getData(item);
        if (data == null || data.usages() == null || data.usages().isEmpty()) {
            rightContent.setFrame(0, 0, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
            final Label label = new Label("未找到该物品的念头配置。", theme);
            label.setFrame(0, 0, CARD_WIDTH, TITLE_HEIGHT * 2);
            rightContent.addChild(label);
            return;
        }

        final NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(
            minecraft.player
        );

        final List<NianTouData.Usage> passives = new ArrayList<>();
        final List<NianTouData.Usage> skills = new ArrayList<>();
        for (NianTouData.Usage usage : data.usages()) {
            if (usage == null) {
                continue;
            }
            if (
                unlocks != null &&
                !unlocks.isUsageUnlocked(selectedItemId, usage.usageID())
            ) {
                continue;
            }
            if (NianTouUsageId.isPassive(usage.usageID())) {
                passives.add(usage);
            } else if (NianTouUsageId.isSkill(usage.usageID())) {
                skills.add(usage);
            }
        }

        int y = 0;
        for (NianTouData.Usage usage : passives) {
            addPassiveCard(config, usage, y);
            y += PASSIVE_CARD_HEIGHT;
        }
        for (NianTouData.Usage usage : skills) {
            addSkillCard(config, usage, y);
            y += SKILL_CARD_HEIGHT;
        }

        rightContent.setFrame(
            0,
            0,
            CARD_WIDTH,
            Math.max(PASSIVE_CARD_HEIGHT, y)
        );
    }

    private void addPassiveCard(
        final TweakConfig config,
        final NianTouData.Usage usage,
        final int y
    ) {
        final SolidPanel card = new SolidPanel(theme);
        card.setFrame(0, y, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
        rightContent.addChild(card);

        final Label title = new Label("被动：" + usage.usageTitle(), theme);
        title.setFrame(0, CARD_TITLE_Y, CARD_WIDTH, CARD_TITLE_HEIGHT);
        title.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(title);

        final String desc = buildUsageDescription(usage, CARD_DESC_WIDTH);
        final Label descLabel = new Label(desc, theme);
        descLabel.setFrame(
            CARD_DESC_X,
            CARD_DESC_Y_PASSIVE,
            CARD_DESC_WIDTH,
            CARD_DESC_HEIGHT
        );
        descLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(descLabel);

        final Label statusLabel = new Label("当前状态:", theme);
        statusLabel.setFrame(
            STATUS_LABEL_X,
            STATUS_LABEL_Y,
            STATUS_LABEL_WIDTH,
            STATUS_LABEL_HEIGHT
        );
        statusLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(statusLabel);

        final boolean enabled =
            config == null || config.isPassiveEnabled(usage.usageID());
        final Label statusValue = new Label(enabled ? "开启" : "关闭", theme);
        statusValue.setFrame(
            STATUS_VALUE_X,
            STATUS_LABEL_Y,
            STATUS_LABEL_WIDTH,
            STATUS_LABEL_HEIGHT
        );
        statusValue.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(statusValue);

        final Button toggle = new Button("开启/关闭", theme);
        toggle.setFrame(
            ACTION_BUTTON_X,
            TOGGLE_BUTTON_Y,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        toggle.setOnClick(() -> {
            final boolean next = !(config == null ||
                config.isPassiveEnabled(usage.usageID()));
            PacketDistributor.sendToServer(
                new ServerboundTweakConfigUpdatePayload(
                    ServerboundTweakConfigUpdatePayload.Action.SET_PASSIVE_ENABLED,
                    usage.usageID(),
                    next
                )
            );
            rebuildRightContent();
        });
        card.addChild(toggle);
    }

    private void addSkillCard(
        final TweakConfig config,
        final NianTouData.Usage usage,
        final int y
    ) {
        final SolidPanel card = new SolidPanel(theme);
        card.setFrame(0, y, CARD_WIDTH, SKILL_CARD_HEIGHT);
        rightContent.addChild(card);

        final Label title = new Label("主动：" + usage.usageTitle(), theme);
        title.setFrame(0, CARD_TITLE_Y, CARD_WIDTH, CARD_TITLE_HEIGHT);
        title.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(title);

        final String desc = buildUsageDescription(usage, CARD_DESC_WIDTH);
        final Label descLabel = new Label(desc, theme);
        descLabel.setFrame(
            CARD_DESC_X,
            CARD_DESC_Y_SKILL,
            CARD_DESC_WIDTH,
            CARD_DESC_HEIGHT
        );
        descLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(descLabel);

        final boolean inWheel =
            config != null && config.isInWheel(usage.usageID());

        final Button add = new Button("加入轮盘", theme);
        add.setFrame(
            ACTION_BUTTON_X,
            ACTION_BUTTON_Y_ADD,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        add.setEnabled(!inWheel);
        add.setOnClick(() -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                final TweakConfig localConfig =
                    KongqiaoAttachments.getTweakConfig(minecraft.player);
                if (localConfig != null) {
                    localConfig.addWheelSkill(
                        usage.usageID(),
                        TweakConfig.DEFAULT_MAX_WHEEL_SKILLS
                    );
                }
            }
            PacketDistributor.sendToServer(
                new ServerboundTweakConfigUpdatePayload(
                    ServerboundTweakConfigUpdatePayload.Action.ADD_WHEEL_SKILL,
                    usage.usageID(),
                    true
                )
            );
            requestSync();
            rebuildRightContent();
        });
        card.addChild(add);

        final Button remove = new Button("移出轮盘", theme);
        remove.setFrame(
            ACTION_BUTTON_X,
            ACTION_BUTTON_Y_REMOVE,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        remove.setEnabled(inWheel);
        remove.setOnClick(() -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                final TweakConfig localConfig =
                    KongqiaoAttachments.getTweakConfig(minecraft.player);
                if (localConfig != null) {
                    localConfig.removeWheelSkill(usage.usageID());
                }
            }
            PacketDistributor.sendToServer(
                new ServerboundTweakConfigUpdatePayload(
                    ServerboundTweakConfigUpdatePayload.Action.REMOVE_WHEEL_SKILL,
                    usage.usageID(),
                    false
                )
            );
            requestSync();
            rebuildRightContent();
        });
        card.addChild(remove);
    }

    private void addShazhaoPassiveCard(
        final TweakConfig config,
        final ShazhaoData data,
        final int y
    ) {
        final SolidPanel card = new SolidPanel(theme);
        card.setFrame(0, y, CARD_WIDTH, PASSIVE_CARD_HEIGHT);
        rightContent.addChild(card);

        final Label title = new Label("被动杀招：" + data.title(), theme);
        title.setFrame(0, CARD_TITLE_Y, CARD_WIDTH, CARD_TITLE_HEIGHT);
        title.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(title);

        final String desc = buildShazhaoDescription(data, CARD_DESC_WIDTH);
        final Label descLabel = new Label(desc, theme);
        descLabel.setFrame(
            CARD_DESC_X,
            CARD_DESC_Y_PASSIVE,
            CARD_DESC_WIDTH,
            CARD_DESC_HEIGHT
        );
        descLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(descLabel);

        final Label statusLabel = new Label("当前状态:", theme);
        statusLabel.setFrame(
            STATUS_LABEL_X,
            STATUS_LABEL_Y,
            STATUS_LABEL_WIDTH,
            STATUS_LABEL_HEIGHT
        );
        statusLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(statusLabel);

        final boolean enabled =
            config == null || config.isPassiveEnabled(data.shazhaoID());
        final Label statusValue = new Label(enabled ? "开启" : "关闭", theme);
        statusValue.setFrame(
            STATUS_VALUE_X,
            STATUS_LABEL_Y,
            STATUS_LABEL_WIDTH,
            STATUS_LABEL_HEIGHT
        );
        statusValue.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(statusValue);

        final Button toggle = new Button("开启/关闭", theme);
        toggle.setFrame(
            ACTION_BUTTON_X,
            TOGGLE_BUTTON_Y,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        toggle.setOnClick(() -> {
            final boolean next = !(config == null ||
                config.isPassiveEnabled(data.shazhaoID()));
            PacketDistributor.sendToServer(
                new ServerboundTweakConfigUpdatePayload(
                    ServerboundTweakConfigUpdatePayload.Action.SET_PASSIVE_ENABLED,
                    data.shazhaoID(),
                    next
                )
            );
            rebuildRightContent();
        });
        card.addChild(toggle);
    }

    private void addShazhaoActiveCard(
        final TweakConfig config,
        final ShazhaoData data,
        final int y
    ) {
        final SolidPanel card = new SolidPanel(theme);
        card.setFrame(0, y, CARD_WIDTH, SKILL_CARD_HEIGHT);
        rightContent.addChild(card);

        final Label title = new Label("主动杀招：" + data.title(), theme);
        title.setFrame(0, CARD_TITLE_Y, CARD_WIDTH, CARD_TITLE_HEIGHT);
        title.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(title);

        final String desc = buildShazhaoDescription(data, CARD_DESC_WIDTH);
        final Label descLabel = new Label(desc, theme);
        descLabel.setFrame(
            CARD_DESC_X,
            CARD_DESC_Y_SKILL,
            CARD_DESC_WIDTH,
            CARD_DESC_HEIGHT
        );
        descLabel.setHorizontalAlign(Label.HorizontalAlign.CENTER);
        card.addChild(descLabel);

        final boolean inWheel =
            config != null && config.isInWheel(data.shazhaoID());

        final Button add = new Button("加入轮盘", theme);
        add.setFrame(
            ACTION_BUTTON_X,
            ACTION_BUTTON_Y_ADD,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        add.setEnabled(!inWheel);
        add.setOnClick(() -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                final TweakConfig localConfig =
                    KongqiaoAttachments.getTweakConfig(minecraft.player);
                if (localConfig != null) {
                    localConfig.addWheelSkill(
                        data.shazhaoID(),
                        TweakConfig.DEFAULT_MAX_WHEEL_SKILLS
                    );
                }
            }
            PacketDistributor.sendToServer(
                new ServerboundTweakConfigUpdatePayload(
                    ServerboundTweakConfigUpdatePayload.Action.ADD_WHEEL_SKILL,
                    data.shazhaoID(),
                    true
                )
            );
            requestSync();
            rebuildRightContent();
        });
        card.addChild(add);

        final Button remove = new Button("移出轮盘", theme);
        remove.setFrame(
            ACTION_BUTTON_X,
            ACTION_BUTTON_Y_REMOVE,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT
        );
        remove.setEnabled(inWheel);
        remove.setOnClick(() -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                final TweakConfig localConfig =
                    KongqiaoAttachments.getTweakConfig(minecraft.player);
                if (localConfig != null) {
                    localConfig.removeWheelSkill(data.shazhaoID());
                }
            }
            PacketDistributor.sendToServer(
                new ServerboundTweakConfigUpdatePayload(
                    ServerboundTweakConfigUpdatePayload.Action.REMOVE_WHEEL_SKILL,
                    data.shazhaoID(),
                    false
                )
            );
            requestSync();
            rebuildRightContent();
        });
        card.addChild(remove);
    }

    private String buildUsageDescription(
        final NianTouData.Usage usage,
        final int maxWidth
    ) {
        final String raw = usage.usageDesc() + "\n" + usage.getFormattedInfo();
        return wrapText(raw, maxWidth);
    }

    private String buildShazhaoDescription(
        final ShazhaoData data,
        final int maxWidth
    ) {
        final StringBuilder raw = new StringBuilder();
        if (data.desc() != null) {
            raw.append(data.desc());
        }
        final String info = data.getFormattedInfo();
        if (info != null && !info.isBlank()) {
            raw.append("\n").append(info);
        }
        final String requiredItems = buildShazhaoRequiredItemsText(data);
        if (requiredItems != null && !requiredItems.isBlank()) {
            raw.append("\n").append(requiredItems);
        }
        return wrapText(raw.toString(), maxWidth);
    }

    /**
     * 构建杀招所需蛊虫显示文本（使用物品翻译名）。
     * <p>
     * required_items 存储的是 itemId 字符串；这里将其解析并转换为玩家易读的名称，
     * 用于在 Tweak UI 中明确提示“触发该杀招需要哪些蛊虫”。
     * </p>
     */
    private String buildShazhaoRequiredItemsText(final ShazhaoData data) {
        if (data == null) {
            return "";
        }
        final List<String> requiredItems = data.requiredItems();
        if (requiredItems == null || requiredItems.isEmpty()) {
            return "";
        }

        final List<String> names = new ArrayList<>();
        for (String itemId : requiredItems) {
            if (itemId == null || itemId.isBlank()) {
                continue;
            }
            final ResourceLocation parsedId;
            try {
                parsedId = ResourceLocation.parse(itemId);
            } catch (Exception e) {
                names.add(itemId);
                continue;
            }
            final Item item = BuiltInRegistries.ITEM.getOptional(parsedId)
                .orElse(Items.AIR);
            if (item == Items.AIR) {
                names.add(itemId);
                continue;
            }
            final String key = item.getDescriptionId();
            final String translated = Component.translatable(key).getString();
            if (translated.equals(key)) {
                names.add(itemId);
            } else {
                names.add(translated);
            }
        }

        if (names.isEmpty()) {
            return "";
        }
        return "所需蛊虫：" + String.join("，", names);
    }

    private String wrapText(final String raw, final int maxWidth) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        final List<String> lines = new ArrayList<>();
        for (String paragraph : raw.split("\n", -1)) {
            lines.addAll(wrapParagraph(paragraph, maxWidth));
        }
        return String.join("\n", lines);
    }

    private List<String> wrapParagraph(
        final String paragraph,
        final int maxWidth
    ) {
        final List<String> lines = new ArrayList<>();
        if (paragraph == null || paragraph.isEmpty()) {
            lines.add("");
            return lines;
        }
        final int limit = Math.max(1, maxWidth);
        final StringBuilder current = new StringBuilder();
        for (int i = 0; i < paragraph.length(); i++) {
            final char ch = paragraph.charAt(i);
            current.append(ch);
            if (this.font.width(current.toString()) >= limit) {
                lines.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }
}
