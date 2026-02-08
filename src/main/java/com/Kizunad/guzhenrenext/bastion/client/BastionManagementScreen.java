package com.Kizunad.guzhenrenext.bastion.client;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionModifier;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentNode;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentRegistry;
import com.Kizunad.guzhenrenext.bastion.menu.BastionManagementMenu;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.SolidPanel;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Map;
import java.util.Set;

/**
 * 基地管理界面（TinyUI）。
 */
public class BastionManagementScreen
    extends TinyUIContainerScreen<BastionManagementMenu> {

    private static final int WINDOW_WIDTH = 420;
    private static final int WINDOW_HEIGHT = 380;
    private static final int PADDING = 10;
    private static final int LABEL_HEIGHT = 14;
    private static final int LABEL_GAP = 4;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 8;
    private static final int TAB_BUTTON_WIDTH = 90;
    private static final int TAB_BUTTON_HEIGHT = 20;
    private static final int TAB_BUTTON_GAP = 6;
    private static final int CONTENT_BOTTOM_RESERVED_HEIGHT = BUTTON_HEIGHT * 2 + BUTTON_GAP;
    private static final int TAB_OVERVIEW = 0;
    private static final int TAB_TALENTS = 1;
    private static final int TAB_AFFIXES = 2;
    private static final int TAB_NODES = 3;
    private static final int PERCENT_BASE = 100;
    private static final int MAX_TIER_DISPLAY = 9;
    private static final int BRANCH_BUTTON_WIDTH = 60;
    private static final int BRANCH_BUTTON_HEIGHT = 16;
    private static final int BRANCH_COMMON = 0;
    private static final int BRANCH_ZHI = 1;
    private static final int BRANCH_HUN = 2;
    private static final int BRANCH_MU = 3;
    private static final int BRANCH_LI = 4;
    private static final int NODE_LINE_SPACING = 2;
    private static final int DETAIL_SECTION_HEIGHT_MULTIPLIER = 4;
    private static final int DETAIL_AREA_HEIGHT = LABEL_HEIGHT * 4 + LABEL_GAP * 3 + PADDING;
    private static final int TREE_INDENT_WIDTH = 12;
    private static final int SCROLLBAR_MARGIN = 8;

    private final Theme theme = Theme.vanilla();
    private int currentTab = TAB_OVERVIEW;
    private UIElement rootPanel;
    private UIElement contentPanel;
    private int contentTop;
    private int contentHeight;
    private int currentTalentBranch = 0; // 0=通用, 1=智, 2=魂, 3=木, 4=力
    private String selectedNodeId = null;

    public BastionManagementScreen(
        BastionManagementMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void initUI(UIRoot root) {
        UIElement window = new UIElement() {
        };
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

        SolidPanel panel = new SolidPanel(theme);
        panel.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.addChild(panel);
        this.rootPanel = panel;

        int y = PADDING;
        Label titleLabel = new Label(
            Component.translatable("screen.guzhenrenext.bastion_management.title"),
            theme
        );
        titleLabel.setFrame(PADDING, y, WINDOW_WIDTH - PADDING * 2, LABEL_HEIGHT);
        panel.addChild(titleLabel);
        y += LABEL_HEIGHT + PADDING;

        buildTabButtons(panel, y);
        y += TAB_BUTTON_HEIGHT + PADDING;

        contentTop = y;
        contentHeight = WINDOW_HEIGHT - contentTop - PADDING - CONTENT_BOTTOM_RESERVED_HEIGHT - PADDING;
        rebuildContent();

        int actionY = WINDOW_HEIGHT - PADDING - CONTENT_BOTTOM_RESERVED_HEIGHT;
        buildActionButtons(panel, actionY);
    }

    private void buildTabButtons(UIElement parent, int y) {
        int buttonX = PADDING;
        String[] tabKeys = {
            "screen.guzhenrenext.bastion_management.tab.overview",
            "screen.guzhenrenext.bastion_management.tab.talents",
            "screen.guzhenrenext.bastion_management.tab.affixes",
            "screen.guzhenrenext.bastion_management.tab.nodes"
        };
        for (int i = 0; i < tabKeys.length; i++) {
            final int tabIndex = i;
            Button tabBtn = new Button(Component.translatable(tabKeys[i]), theme);
            tabBtn.setFrame(buttonX, y, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT);
            tabBtn.setOnClick(() -> switchTab(tabIndex));
            parent.addChild(tabBtn);
            buttonX += TAB_BUTTON_WIDTH + TAB_BUTTON_GAP;
        }
    }

    private void switchTab(int tabIndex) {
        currentTab = tabIndex;
        rebuildContent();
    }

    private void rebuildContent() {
        if (contentPanel != null && contentPanel.getParent() != null) {
            contentPanel.getParent().removeChild(contentPanel);
        }
        UIElement panel = new UIElement() {
        };
        panel.setFrame(PADDING, contentTop, WINDOW_WIDTH - PADDING * 2, contentHeight);
        rootPanel.addChild(panel);
        contentPanel = panel;

        switch (currentTab) {
            case TAB_OVERVIEW -> buildOverviewContent(panel);
            case TAB_TALENTS -> buildTalentContent(panel);
            case TAB_AFFIXES -> buildAffixContent(panel);
            case TAB_NODES -> buildNodeContent(panel);
            default -> buildOverviewContent(panel);
        }
    }

    private void buildOverviewContent(UIElement panel) {
        int y = 0;
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.tier", menu.getTier(), y);
        y = addInfoLabel(
            panel,
            "screen.guzhenrenext.bastion_management.evolution",
            (int) (menu.getEvolutionProgress() * PERCENT_BASE) + "%",
            y
        );
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.resources", menu.getResourcePool(), y);
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.pollution", menu.getPollution(), y);
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.threat", menu.getThreatMeter(), y);
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.talent_points", menu.getTalentPoints(), y);
        String daoName = menu.getPrimaryDao() == null ? "-" : menu.getPrimaryDao().getSerializedName();
        addInfoLabel(panel, "screen.guzhenrenext.bastion_management.dao", daoName, y);
    }

    private void buildTalentContent(UIElement panel) {
        int y = 0;
        Label pointsLabel = new Label(
            Component.literal("§e天赋点：§f" + menu.getTalentPoints()),
            theme
        );
        pointsLabel.setFrame(0, y, panel.getWidth(), LABEL_HEIGHT);
        panel.addChild(pointsLabel);
        y += LABEL_HEIGHT + LABEL_GAP;

        y = buildBranchButtons(panel, y);
        y += LABEL_GAP;

        String branchName = getBranchDisplayName(currentTalentBranch);
        Label branchLabel = new Label(Component.literal("§7[" + branchName + "]"), theme);
        branchLabel.setFrame(0, y, panel.getWidth(), LABEL_HEIGHT);
        panel.addChild(branchLabel);
        y += LABEL_HEIGHT + PADDING;

        // 计算滚动区域高度：预留详情区域（选中时）或底部边距
        int scrollAreaHeight;
        if (selectedNodeId != null) {
            scrollAreaHeight = panel.getHeight() - y - DETAIL_AREA_HEIGHT;
        } else {
            scrollAreaHeight = panel.getHeight() - y - PADDING;
        }

        // 创建滚动容器
        ScrollContainer scrollContainer = new ScrollContainer(theme);
        scrollContainer.setFrame(0, y, panel.getWidth(), scrollAreaHeight);
        panel.addChild(scrollContainer);

        // 过滤当前分支的节点并计算内容高度
        java.util.List<BastionTalentNode> nodes = BastionTalentRegistry.getAllNodes();
        java.util.List<BastionTalentNode> branchNodes = new java.util.ArrayList<>();
        java.util.List<Integer> branchNodeIndices = new java.util.ArrayList<>();
        int nodeIndex = 0;
        for (BastionTalentNode node : nodes) {
            if (matchesBranch(node, currentTalentBranch)) {
                branchNodes.add(node);
                branchNodeIndices.add(nodeIndex);
            }
            nodeIndex++;
        }

        int contentHeight = branchNodes.size() * (LABEL_HEIGHT + NODE_LINE_SPACING);
        int contentWidth = panel.getWidth() - SCROLLBAR_MARGIN;

        // 创建内容面板
        UIElement contentPanel = new UIElement() { };
        contentPanel.setFrame(0, 0, contentWidth, Math.max(contentHeight, scrollAreaHeight));

        // 添加节点按钮到内容面板
        int nodeY = 0;
        for (int i = 0; i < branchNodes.size(); i++) {
            BastionTalentNode node = branchNodes.get(i);
            final int idx = branchNodeIndices.get(i);

            String status = getNodeStatus(node);
            String prefix = switch (status) {
                case "unlocked" -> "§a[✓] ";
                case "available" -> "§e[○] ";
                case "insufficient_points" -> "§6[◇] ";
                default -> "§7[✗] ";
            };

            // 根据节点深度计算缩进
            int depth = getNodeDepth(node);
            int indent = depth * TREE_INDENT_WIDTH;
            String treePrefix = depth > 0 ? "§8└ " : "";

            final String nodeId = node.id();
            Button nodeBtn = new Button(
                Component.literal(treePrefix + prefix + node.displayName()),
                theme
            );
            nodeBtn.setFrame(indent, nodeY, contentWidth - BUTTON_WIDTH - PADDING - indent, LABEL_HEIGHT);
            nodeBtn.setOnClick(() -> selectNode(nodeId));
            contentPanel.addChild(nodeBtn);

            if ("available".equals(status)) {
                Button unlockBtn = new Button(Component.literal("§a解锁"), theme);
                unlockBtn.setFrame(contentWidth - BUTTON_WIDTH, nodeY, BUTTON_WIDTH, LABEL_HEIGHT);
                unlockBtn.setOnClick(() -> unlockNode(idx));
                contentPanel.addChild(unlockBtn);
            } else if ("insufficient_points".equals(status)) {
                Button needPointsBtn = new Button(
                    Component.literal("§7需要" + node.cost() + "点"),
                    theme
                );
                needPointsBtn.setFrame(contentWidth - BUTTON_WIDTH, nodeY, BUTTON_WIDTH, LABEL_HEIGHT);
                contentPanel.addChild(needPointsBtn);
            }

            nodeY += LABEL_HEIGHT + NODE_LINE_SPACING;
        }

        scrollContainer.setContent(contentPanel);

        // 详情区域在滚动容器下方
        if (selectedNodeId != null) {
            BastionTalentNode selected = BastionTalentRegistry.getNode(selectedNodeId);
            if (selected != null) {
                int detailY = panel.getHeight() - DETAIL_AREA_HEIGHT + PADDING;
                detailY = addLiteralLabel(panel, "§6选中：§f" + selected.displayName(), detailY);
                detailY = addLiteralLabel(panel, "§7" + selected.description(), detailY);
                detailY = addLiteralLabel(
                    panel,
                    "§e消耗：§f" + selected.cost() + " 点  §7(拥有: " + menu.getTalentPoints() + ")",
                    detailY
                );
                if (!selected.prerequisites().isEmpty()) {
                    StringBuilder prereqText = new StringBuilder("§e前置：");
                    for (String prereq : selected.prerequisites()) {
                        boolean met = menu.getUnlockedNodes().contains(prereq);
                        BastionTalentNode prereqNode = BastionTalentRegistry.getNode(prereq);
                        String name = prereqNode != null ? prereqNode.displayName() : prereq;
                        prereqText.append(met ? "§a✓" : "§c✗").append(name).append(" ");
                    }
                    addLiteralLabel(panel, prereqText.toString(), detailY);
                }
            }
        }
    }

    private int buildBranchButtons(UIElement panel, int y) {
        int buttonX = 0;
        String[] branchNames = {"通用", "智道", "魂道", "木道", "力道"};

        for (int i = 0; i < branchNames.length; i++) {
            final int branchIndex = i;
            String label = (currentTalentBranch == i) ? "§e" + branchNames[i] : "§7" + branchNames[i];
            Button branchBtn = new Button(Component.literal(label), theme);
            branchBtn.setFrame(buttonX, y, BRANCH_BUTTON_WIDTH, BRANCH_BUTTON_HEIGHT);
            branchBtn.setOnClick(() -> switchBranch(branchIndex));
            panel.addChild(branchBtn);
            buttonX += BRANCH_BUTTON_WIDTH + LABEL_GAP;
        }
        return y + BRANCH_BUTTON_HEIGHT;
    }

    private void switchBranch(int branchIndex) {
        currentTalentBranch = branchIndex;
        selectedNodeId = null;
        rebuildContent();
    }

    private String getBranchDisplayName(int branch) {
        return switch (branch) {
            case BRANCH_ZHI -> "智道天赋";
            case BRANCH_HUN -> "魂道天赋";
            case BRANCH_MU -> "木道天赋";
            case BRANCH_LI -> "力道天赋";
            default -> "通用天赋";
        };
    }

    private boolean matchesBranch(BastionTalentNode node, int branch) {
        java.util.Optional<BastionDao> nodeDao = node.dao();
        if (branch == BRANCH_COMMON) {
            return nodeDao.isEmpty();
        }
        if (nodeDao.isEmpty()) {
            return false;
        }
        BastionDao dao = nodeDao.get();
        return switch (branch) {
            case BRANCH_ZHI -> dao == BastionDao.ZHI_DAO;
            case BRANCH_HUN -> dao == BastionDao.HUN_DAO;
            case BRANCH_MU -> dao == BastionDao.MU_DAO;
            case BRANCH_LI -> dao == BastionDao.LI_DAO;
            default -> false;
        };
    }

    /**
     * 获取节点状态。
     * @return 状态字符串：unlocked(已解锁)、available(可解锁)、insufficient_points(点数不足)、locked(前置未满足)
     */
    private String getNodeStatus(BastionTalentNode node) {
        if (menu.getUnlockedNodes().contains(node.id())) {
            return "unlocked";
        }
        // 检查前置条件
        for (String prereq : node.prerequisites()) {
            if (!menu.getUnlockedNodes().contains(prereq)) {
                return "locked"; // 前置未满足
            }
        }
        // 前置已满足，检查点数
        if (menu.getTalentPoints() >= node.cost()) {
            return "available";
        }
        return "insufficient_points"; // 前置满足但点数不足
    }

    /**
     * 计算节点在树中的深度（用于缩进显示）。
     * @return 深度值，根节点为0
     */
    private int getNodeDepth(BastionTalentNode node) {
        if (node.prerequisites().isEmpty()) {
            return 0;
        }
        int maxParentDepth = 0;
        for (String prereqId : node.prerequisites()) {
            BastionTalentNode prereq = BastionTalentRegistry.getNode(prereqId);
            if (prereq != null) {
                int parentDepth = getNodeDepth(prereq);
                if (parentDepth > maxParentDepth) {
                    maxParentDepth = parentDepth;
                }
            }
        }
        return maxParentDepth + 1;
    }

    private void selectNode(String nodeId) {
        selectedNodeId = nodeId;
        rebuildContent();
    }

    private void unlockNode(int nodeIndex) {
        clickButton(BastionManagementMenu.BUTTON_UNLOCK_TALENT_BASE + nodeIndex);
    }

    private void buildAffixContent(UIElement panel) {
        int y = 0;
        Set<BastionModifier> modifiers = menu.getModifiers();

        if (modifiers == null || modifiers.isEmpty()) {
            y = addTranslatableLabel(panel, "screen.guzhenrenext.bastion_management.no_modifiers", y);
        } else {
            for (BastionModifier mod : modifiers) {
                String nameKey = "modifier.guzhenrenext." + mod.getSerializedName() + ".name";
                String descKey = "modifier.guzhenrenext." + mod.getSerializedName() + ".desc";
                y = addLiteralLabel(panel, "§e" + Component.translatable(nameKey).getString(), y);
                y = addLiteralLabel(panel, "§7  " + Component.translatable(descKey).getString(), y);
                y += LABEL_GAP;
            }
        }
    }

    private void buildNodeContent(UIElement panel) {
        int y = 0;

        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.total_nodes",
                         menu.getTotalNodes(), y);
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.total_mycelium",
                         menu.getTotalMycelium(), y);
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.total_anchors",
                         menu.getTotalAnchors(), y);

        String radiusText = menu.getAuraRadius() + " / " + menu.getBaseAuraRadius();
        y = addInfoLabel(panel, "screen.guzhenrenext.bastion_management.aura_radius",
                         radiusText, y);

        y += LABEL_GAP;
        y = addLiteralLabel(panel, "§7--- 各转数节点 ---", y);

        Map<Integer, Integer> tiers = menu.getNodesByTier();
        for (int tier = 1; tier <= MAX_TIER_DISPLAY; tier++) {
            int count = tiers.getOrDefault(tier, 0);
            if (count > 0) {
                y = addLiteralLabel(panel, "§f" + tier + " 转节点：§e" + count, y);
            }
        }
    }

    private void buildActionButtons(UIElement panel, int y) {
        
        int buttonX = PADDING;
        Button teleportBtn = new Button(
            Component.translatable("screen.guzhenrenext.bastion_management.teleport"),
            theme
        );
        teleportBtn.setFrame(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        teleportBtn.setOnClick(() -> clickButton(BastionManagementMenu.BUTTON_TELEPORT));
        panel.addChild(teleportBtn);

        buttonX += BUTTON_WIDTH + BUTTON_GAP;
        Button convertSmallBtn = new Button(
            Component.translatable("screen.guzhenrenext.bastion_management.convert_small"),
            theme
        );
        convertSmallBtn.setFrame(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        convertSmallBtn.setOnClick(() -> clickButton(BastionManagementMenu.BUTTON_CONVERT_TALENT_SMALL));
        panel.addChild(convertSmallBtn);

        buttonX += BUTTON_WIDTH + BUTTON_GAP;
        Button convertLargeBtn = new Button(
            Component.translatable("screen.guzhenrenext.bastion_management.convert_large"),
            theme
        );
        convertLargeBtn.setFrame(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        convertLargeBtn.setOnClick(() -> clickButton(BastionManagementMenu.BUTTON_CONVERT_TALENT_LARGE));
        panel.addChild(convertLargeBtn);

        y += BUTTON_HEIGHT + BUTTON_GAP;
        if (!menu.isRemote()) {
            Button boostBtn = new Button(
                Component.translatable("screen.guzhenrenext.bastion_management.boost"),
                theme
            );
            boostBtn.setFrame(PADDING, y, BUTTON_WIDTH, BUTTON_HEIGHT);
            boostBtn.setOnClick(() -> clickButton(BastionManagementMenu.BUTTON_BOOST_EVOLUTION));
            panel.addChild(boostBtn);
        }
    }

    @Override
    protected double getUiScale() {
        return com.Kizunad.guzhenrenext.config.ClientConfig.INSTANCE.kongQiaoUiScale.get();
    }

    private int addInfoLabel(UIElement parent, String key, Object value, int y) {
        Label label = new Label(Component.translatable(key, value), theme);
        label.setFrame(0, y, parent.getWidth(), LABEL_HEIGHT);
        parent.addChild(label);
        return y + LABEL_HEIGHT + LABEL_GAP;
    }

    private int addLiteralLabel(UIElement parent, String text, int y) {
        Label label = new Label(Component.literal(text), theme);
        label.setFrame(0, y, parent.getWidth(), LABEL_HEIGHT);
        parent.addChild(label);
        return y + LABEL_HEIGHT + LABEL_GAP;
    }

    private int addTranslatableLabel(UIElement parent, String key, int y) {
        Label label = new Label(Component.translatable(key), theme);
        label.setFrame(0, y, parent.getWidth(), LABEL_HEIGHT);
        parent.addChild(label);
        return y + LABEL_HEIGHT + LABEL_GAP;
    }

    private void clickButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }
}
