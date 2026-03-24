package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.block.ApertureHubMenu;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.block.AlchemyFurnaceBlock;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.HubPanel;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.HubUiTokens;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.ModuleCard;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.ui.StatePill;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuItem;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlock;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritEntity;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ApertureHubScreen extends TinyUIContainerScreen<ApertureHubMenu> {

    private static final int VIEWPORT_MARGIN = 12;
    private static final int SHELL_PREFERRED_WIDTH = 560;
    private static final int SHELL_PADDING = 8;
    private static final int HEADER_BAND_HEIGHT = 42;
    private static final int HEADER_TITLE_HEIGHT = 14;
    private static final int HEADER_TITLE_Y = 8;
    private static final int HEADER_DETAIL_HEIGHT = 12;
    private static final int HEADER_DETAIL_Y = 22;
    private static final int HEADER_STATUS_WIDTH = 86;
    private static final int HEADER_STATUS_Y = 12;
    private static final int BODY_TOP_GAP = 6;
    private static final int BODY_LAYER_GAP = 6;
    private static final int INFO_BLOCK_COUNT = 3;
    private static final int TOP_SITUATION_HEIGHT = 58;
    private static final int BOTTOM_UTILITY_HEIGHT = 64;
    private static final int MODULE_GRID_PADDING = 6;
    private static final int MODULE_GRID_TITLE_HEIGHT = 14;
    private static final int MODULE_GRID_CARD_GAP = HubUiTokens.PANEL_GAP;
    private static final int MODULE_CARD_HEIGHT = 104;
    private static final int FOUR_COLUMN_THRESHOLD = 480;
    private static final int THREE_COLUMN_THRESHOLD = 360;
    private static final int TWO_COLUMN_THRESHOLD = 260;
    private static final int FOUR_COLUMN_COUNT = 4;
    private static final int THREE_COLUMN_COUNT = 3;
    private static final int TWO_COLUMN_COUNT = 2;
    private static final int ONE_COLUMN_COUNT = 1;
    private static final int HEADER_STATUS_HEIGHT = HubUiTokens.MEDIUM_CONTROL_HEIGHT;
    private static final int TEXT_WRAP_MIN_CHARS = 8;
    private static final int TEXT_WRAP_MAX_CHARS = 18;
    private static final int TEXT_WRAP_PIXEL_PER_CHAR = 10;
    private static final long TRIBULATION_WARNING_TICKS = 24000L;
    private static final double PERCENT_BASE = 100.0D;
    private static final double PERMILLE_TO_PERCENT = 10.0D;
    private static final long TICKS_PER_SECOND = 20L;
    private static final long SECONDS_PER_HOUR = 3600L;
    private static final long HOURS_PER_DAY = 24L;
    private static final int DIRECT_ROUTE_BLOCK_SCAN_RADIUS = 8;
    private static final int DIRECT_ROUTE_VERTICAL_SCAN_RADIUS = 8;
    private static final double DIRECT_ROUTE_ENTITY_SCAN_RADIUS = 8.0D;
    private static final double DIRECT_ROUTE_INTERACTION_DISTANCE_SQR = 64.0D;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final String LAYER_HEADER_BAND = "HeaderBand";
    private static final String LAYER_TOP_SITUATION_ROW = "TopSituationRow";
    private static final String LAYER_MAIN_MODULE_GRID = "MainModuleGrid";
    private static final String LAYER_BOTTOM_UTILITY_ROW = "BottomUtilityRow";

    private final Theme theme;
    private final HubStatusEvaluator statusEvaluator;
    private final Map<String, ModuleCard> moduleCardsById;

    private Label headerDetailLabel;
    private StatePill stabilityHeadlinePill;
    private InfoBlock overallSummaryBlock;
    private InfoBlock riskSummaryBlock;
    private InfoBlock nextRouteBlock;
    private InfoBlock utilityRoutesBlock;
    private InfoBlock summaryFootnoteBlock;
    private InfoBlock fallbackExplainerBlock;

    public ApertureHubScreen(
        final ApertureHubMenu menu,
        final Inventory playerInventory,
        final Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = HubUiTokens.hallTheme();
        this.statusEvaluator = new HubStatusEvaluator();
        this.moduleCardsById = new LinkedHashMap<>();
    }

    @Override
    protected void initUI(final UIRoot root) {
        resetUiReferences();

        final ShellLayout layout = computeShellLayout(root.getWidth(), root.getHeight());
        final HubPanel shell = new HubPanel(HubUiTokens.HubTone.GOLD);
        Anchor.apply(
            shell,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                layout.windowWidth(),
                layout.windowHeight(),
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(shell);

        buildHeaderBand(shell, layout);

        final UIElement bodyContent = buildBodyContent(layout);
        if (layout.useBodyScrollFallback()) {
            final ScrollContainer bodyScrollContainer = new ScrollContainer(theme);
            bodyScrollContainer.setFrame(
                SHELL_PADDING,
                layout.bodyY(),
                layout.bodyWidth(),
                layout.bodyViewportHeight()
            );
            bodyContent.setFrame(0, 0, layout.bodyWidth(), layout.bodyContentHeight());
            bodyScrollContainer.setContent(bodyContent);
            shell.addChild(bodyScrollContainer);
        } else {
            bodyContent.setFrame(
                SHELL_PADDING,
                layout.bodyY(),
                layout.bodyWidth(),
                layout.bodyContentHeight()
            );
            shell.addChild(bodyContent);
        }

        refreshHubData();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (
            headerDetailLabel == null ||
            stabilityHeadlinePill == null ||
            overallSummaryBlock == null ||
            riskSummaryBlock == null ||
            nextRouteBlock == null ||
            utilityRoutesBlock == null ||
            summaryFootnoteBlock == null ||
            fallbackExplainerBlock == null ||
            moduleCardsById.isEmpty()
        ) {
            return;
        }
        refreshHubData();
    }

    @Override
    protected double getUiScale() {
        return 1.0D;
    }

    static List<String> orderedStructureLayerNames() {
        return List.of(
            LAYER_HEADER_BAND,
            LAYER_TOP_SITUATION_ROW,
            LAYER_MAIN_MODULE_GRID,
            LAYER_BOTTOM_UTILITY_ROW
        );
    }

    static ShellLayout computeShellLayout(final int viewportWidth, final int viewportHeight) {
        final int safeViewportWidth = Math.max(1, viewportWidth);
        final int safeViewportHeight = Math.max(1, viewportHeight);
        final int windowWidth = Math.max(
            1,
            Math.min(SHELL_PREFERRED_WIDTH, safeViewportWidth - VIEWPORT_MARGIN * 2)
        );
        final int bodyWidth = Math.max(1, windowWidth - SHELL_PADDING * 2);
        final int moduleColumns = resolveModuleColumns(bodyWidth);
        final int moduleCardWidth = resolveModuleCardWidth(bodyWidth, moduleColumns);
        final int moduleGridHeight = resolveModuleGridHeight(moduleColumns);
        final int bodyContentHeight =
            TOP_SITUATION_HEIGHT +
            BODY_LAYER_GAP +
            moduleGridHeight +
            BODY_LAYER_GAP +
            BOTTOM_UTILITY_HEIGHT;
        final int preferredWindowHeight =
            SHELL_PADDING +
            HEADER_BAND_HEIGHT +
            BODY_TOP_GAP +
            bodyContentHeight +
            SHELL_PADDING;
        final int windowHeight = Math.max(
            1,
            Math.min(preferredWindowHeight, safeViewportHeight - VIEWPORT_MARGIN * 2)
        );
        final int bodyY = SHELL_PADDING + HEADER_BAND_HEIGHT + BODY_TOP_GAP;
        final int bodyViewportHeight = Math.max(1, windowHeight - bodyY - SHELL_PADDING);
        final boolean useBodyScrollFallback = bodyContentHeight > bodyViewportHeight;

        return new ShellLayout(
            safeViewportWidth,
            safeViewportHeight,
            windowWidth,
            windowHeight,
            bodyY,
            bodyWidth,
            bodyViewportHeight,
            bodyContentHeight,
            moduleColumns,
            moduleGridHeight,
            moduleCardWidth,
            useBodyScrollFallback
        );
    }

    private static int resolveModuleColumns(final int bodyWidth) {
        if (bodyWidth >= FOUR_COLUMN_THRESHOLD) {
            return FOUR_COLUMN_COUNT;
        }
        if (bodyWidth >= THREE_COLUMN_THRESHOLD) {
            return THREE_COLUMN_COUNT;
        }
        if (bodyWidth >= TWO_COLUMN_THRESHOLD) {
            return TWO_COLUMN_COUNT;
        }
        return ONE_COLUMN_COUNT;
    }

    private static int resolveModuleCardWidth(final int bodyWidth, final int moduleColumns) {
        final int innerWidth = Math.max(0, bodyWidth - MODULE_GRID_PADDING * 2);
        final int totalGap = Math.max(0, moduleColumns - 1) * MODULE_GRID_CARD_GAP;
        return Math.max(1, (innerWidth - totalGap) / Math.max(1, moduleColumns));
    }

    private static int resolveModuleGridHeight(final int moduleColumns) {
        final int rows = divideRoundUp(HubRoutePolicy.orderedPolicies().size(), Math.max(1, moduleColumns));
        final int cardStackHeight = rows * MODULE_CARD_HEIGHT
            + Math.max(0, rows - 1) * MODULE_GRID_CARD_GAP;
        return MODULE_GRID_PADDING
            + MODULE_GRID_TITLE_HEIGHT
            + MODULE_GRID_PADDING
            + cardStackHeight
            + MODULE_GRID_PADDING;
    }

    private static int divideRoundUp(final int dividend, final int divisor) {
        return (dividend + divisor - 1) / divisor;
    }

    private void resetUiReferences() {
        moduleCardsById.clear();
        headerDetailLabel = null;
        stabilityHeadlinePill = null;
        overallSummaryBlock = null;
        riskSummaryBlock = null;
        nextRouteBlock = null;
        utilityRoutesBlock = null;
        summaryFootnoteBlock = null;
        fallbackExplainerBlock = null;
    }

    private void buildHeaderBand(final UIElement shell, final ShellLayout layout) {
        final HubPanel headerBand = new HubPanel(HubUiTokens.HubTone.GOLD);
        headerBand.setFrame(SHELL_PADDING, SHELL_PADDING, layout.bodyWidth(), HEADER_BAND_HEIGHT);
        shell.addChild(headerBand);

        final int headerTextWidth = Math.max(1, layout.bodyWidth() - HEADER_STATUS_WIDTH - SHELL_PADDING * 3);

        final Label titleLabel = new Label(this.title.getString(), theme);
        titleLabel.setColor(headerBand.getResolvedPalette().textColor());
        titleLabel.setFrame(
            SHELL_PADDING,
            HEADER_TITLE_Y,
            headerTextWidth,
            HEADER_TITLE_HEIGHT
        );
        headerBand.addChild(titleLabel);

        headerDetailLabel = new Label("", theme);
        headerDetailLabel.setColor(headerBand.getResolvedPalette().mutedTextColor());
        headerDetailLabel.setFrame(
            SHELL_PADDING,
            HEADER_DETAIL_Y,
            headerTextWidth,
            HEADER_DETAIL_HEIGHT
        );
        headerBand.addChild(headerDetailLabel);

        stabilityHeadlinePill = new StatePill();
        stabilityHeadlinePill.setFrame(
            Math.max(SHELL_PADDING, layout.bodyWidth() - SHELL_PADDING - HEADER_STATUS_WIDTH),
            HEADER_STATUS_Y,
            HEADER_STATUS_WIDTH,
            HEADER_STATUS_HEIGHT
        );
        headerBand.addChild(stabilityHeadlinePill);
    }

    private UIElement buildBodyContent(final ShellLayout layout) {
        final UIElement bodyContent = new UIElement() { };

        buildTopSituationRow(bodyContent, layout, 0);
        final int mainGridY = TOP_SITUATION_HEIGHT + BODY_LAYER_GAP;
        buildMainModuleGrid(bodyContent, layout, mainGridY);
        final int bottomRowY = mainGridY + layout.moduleGridHeight() + BODY_LAYER_GAP;
        buildBottomUtilityRow(bodyContent, layout, bottomRowY);

        bodyContent.setFrame(0, 0, layout.bodyWidth(), layout.bodyContentHeight());
        return bodyContent;
    }

    private void buildTopSituationRow(
        final UIElement bodyContent,
        final ShellLayout layout,
        final int y
    ) {
        final UIElement topSituationRow = new UIElement() { };
        topSituationRow.setFrame(0, y, layout.bodyWidth(), TOP_SITUATION_HEIGHT);
        bodyContent.addChild(topSituationRow);

        overallSummaryBlock = createRowBlock(topSituationRow, 0, "总述");
        overallSummaryBlock.applyTone(HubUiTokens.HubTone.GOLD);

        riskSummaryBlock = createRowBlock(topSituationRow, 1, "灾劫风险");
        riskSummaryBlock.applyTone(HubUiTokens.HubTone.WARN);

        nextRouteBlock = createRowBlock(topSituationRow, 2, "下一步");
        nextRouteBlock.applyTone(HubUiTokens.HubTone.AZURE);
    }

    private void buildMainModuleGrid(
        final UIElement bodyContent,
        final ShellLayout layout,
        final int y
    ) {
        final HubPanel mainModuleGrid = new HubPanel(HubUiTokens.HubTone.STONE);
        mainModuleGrid.setFrame(0, y, layout.bodyWidth(), layout.moduleGridHeight());
        bodyContent.addChild(mainModuleGrid);

        final Label gridTitle = new Label("分台模块", theme);
        gridTitle.setColor(mainModuleGrid.getResolvedPalette().textColor());
        gridTitle.setFrame(
            MODULE_GRID_PADDING,
            MODULE_GRID_PADDING,
            Math.max(1, layout.bodyWidth() - MODULE_GRID_PADDING * 2),
            MODULE_GRID_TITLE_HEIGHT
        );
        mainModuleGrid.addChild(gridTitle);

        final int cardsY = MODULE_GRID_PADDING + MODULE_GRID_TITLE_HEIGHT + MODULE_GRID_PADDING;
        for (int index = 0; index < HubRoutePolicy.orderedPolicies().size(); index++) {
            final HubRoutePolicy.CardRoutePolicy policy = HubRoutePolicy.orderedPolicies().get(index);
            final int column = index % layout.moduleColumns();
            final int row = index / layout.moduleColumns();
            final int cardX = MODULE_GRID_PADDING + column * (layout.moduleCardWidth() + MODULE_GRID_CARD_GAP);
            final int cardY = cardsY + row * (MODULE_CARD_HEIGHT + MODULE_GRID_CARD_GAP);
            final ModuleCard card = createModuleCard(policy);
            card.setFrame(cardX, cardY, layout.moduleCardWidth(), MODULE_CARD_HEIGHT);
            mainModuleGrid.addChild(card);
            moduleCardsById.put(policy.cardId(), card);
        }
    }

    private void buildBottomUtilityRow(
        final UIElement bodyContent,
        final ShellLayout layout,
        final int y
    ) {
        final UIElement bottomUtilityRow = new UIElement() { };
        bottomUtilityRow.setFrame(0, y, layout.bodyWidth(), BOTTOM_UTILITY_HEIGHT);
        bodyContent.addChild(bottomUtilityRow);

        utilityRoutesBlock = createRowBlock(bottomUtilityRow, 0, "快捷路由");
        utilityRoutesBlock.applyTone(HubUiTokens.HubTone.AZURE);

        summaryFootnoteBlock = createRowBlock(bottomUtilityRow, 1, "近况摘要");
        summaryFootnoteBlock.applyTone(HubUiTokens.HubTone.WARN);

        fallbackExplainerBlock = createRowBlock(bottomUtilityRow, 2, "中枢说明");
        fallbackExplainerBlock.applyTone(HubUiTokens.HubTone.GOLD);
    }

    private InfoBlock createRowBlock(
        final UIElement rowParent,
        final int index,
        final String title
    ) {
        final int totalGap = MODULE_GRID_CARD_GAP * (INFO_BLOCK_COUNT - 1);
        final int blockWidth = (rowParent.getWidth() - totalGap) / INFO_BLOCK_COUNT;
        final int x = index * (blockWidth + MODULE_GRID_CARD_GAP);
        final InfoBlock block = new InfoBlock(title);
        block.setFrame(x, 0, blockWidth, rowParent.getHeight());
        rowParent.addChild(block);
        return block;
    }

    private ModuleCard createModuleCard(final HubRoutePolicy.CardRoutePolicy policy) {
        final ModuleCard card = new ModuleCard(policy.cardId(), policy.cardTitle());
        card.applyRoutePolicy(policy);
        bindRouteCallbacks(card, policy);
        return card;
    }

    private void bindRouteCallbacks(
        final ModuleCard card,
        final HubRoutePolicy.CardRoutePolicy policy
    ) {
        if (!policy.launchesScreen()) {
            card.setOnClick(null);
            card.getRouteChip().setOnClick(null);
            return;
        }
        final Runnable directRouteCallback = () -> openApprovedDirectRoute(policy);
        card.setOnClick(directRouteCallback);
        card.getRouteChip().setOnClick(directRouteCallback);
    }

    private void openApprovedDirectRoute(final HubRoutePolicy.CardRoutePolicy policy) {
        if (!policy.launchesScreen()) {
            announceRouteResolutionFailure(
                policy.target().displayName(),
                "当前路由仍停留在主殿或占位阶段"
            );
            return;
        }
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null) {
            announceRouteResolutionFailure(
                policy.target().displayName(),
                "当前客户端交互上下文未就绪，请稍后再试"
            );
            return;
        }

        final LocalPlayer player = minecraft.player;
        final String routeFailureReason = switch (policy.target()) {
            case LAND_SPIRIT_SCREEN -> tryOpenLandSpiritRoute(player);
            case RESOURCE_CONTROLLER_SCREEN -> tryOpenResourceControllerRoute(player);
            case ALCHEMY_FURNACE_SCREEN -> tryOpenAlchemyFurnaceRoute(player);
            case STORAGE_GU_SCREEN -> tryOpenStorageGuRoute(player);
            case CLUSTER_NPC_SCREEN -> tryOpenClusterRoute(player);
            default -> "当前主殿尚未接入该入口的可执行交互路径";
        };
        if (routeFailureReason != null) {
            announceRouteResolutionFailure(policy.target().displayName(), routeFailureReason);
        }
    }

    private String tryOpenStorageGuRoute(final LocalPlayer player) {
        if (tryUseHeldStorageGu(player, InteractionHand.MAIN_HAND)) {
            return null;
        }
        if (tryUseHeldStorageGu(player, InteractionHand.OFF_HAND)) {
            return null;
        }

        final int hotbarSlot = findStorageGuHotbarSlot(player);
        if (hotbarSlot < 0) {
            return "未找到可直接使用的储物蛊；请先放入主手、副手或快捷栏";
        }

        final int previousSelectedSlot = player.getInventory().selected;
        player.getInventory().selected = hotbarSlot;
        if (tryUseHeldStorageGu(player, InteractionHand.MAIN_HAND)) {
            return null;
        }
        player.getInventory().selected = previousSelectedSlot;
        return "已发现快捷栏储物蛊，但当前无法触发使用交互";
    }

    private String tryOpenLandSpiritRoute(final LocalPlayer player) {
        final LandSpiritEntity landSpirit = findNearestLoadedEntity(player, LandSpiritEntity.class);
        if (landSpirit == null) {
            return "当前未解析到已加载的地灵实体；请靠近地灵后再试";
        }
        if (tryInteractWithEntity(player, landSpirit)) {
            return null;
        }
        return "已定位到地灵，但当前无法触发管理交互";
    }

    private String tryOpenResourceControllerRoute(final LocalPlayer player) {
        return tryOpenBlockRoute(
            player,
            ResourceControllerBlock.class,
            "当前未解析到已加载的资源控制器；请靠近目标方块后再试",
            "已定位到资源控制器，但当前无法触发打开交互"
        );
    }

    private String tryOpenAlchemyFurnaceRoute(final LocalPlayer player) {
        return tryOpenBlockRoute(
            player,
            AlchemyFurnaceBlock.class,
            "当前未解析到已加载的炼丹炉；请靠近目标方块后再试",
            "已定位到炼丹炉，但当前无法触发打开交互"
        );
    }

    private String tryOpenClusterRoute(final LocalPlayer player) {
        final ClusterNpcEntity clusterNpc = findNearestLoadedEntity(player, ClusterNpcEntity.class);
        if (clusterNpc == null) {
            return "当前未解析到已加载的集群实体；请靠近集群后再试";
        }
        if (tryInteractWithEntity(player, clusterNpc)) {
            return null;
        }
        return "已定位到集群，但当前无法触发交互菜单";
    }

    private String tryOpenBlockRoute(
        final LocalPlayer player,
        final Class<? extends Block> blockType,
        final String unresolvedReason,
        final String interactionFailureReason
    ) {
        final BlockPos targetPos = findNearestLoadedBlock(player, blockType);
        if (targetPos == null) {
            return unresolvedReason;
        }
        if (tryInteractWithBlock(player, targetPos)) {
            return null;
        }
        return interactionFailureReason;
    }

    private boolean tryUseHeldStorageGu(final LocalPlayer player, final InteractionHand hand) {
        final ItemStack heldStack = player.getItemInHand(hand);
        if (!(heldStack.getItem() instanceof StorageGuItem)) {
            return false;
        }
        return isAcceptedInteractionResult(currentGameMode().useItem(player, hand));
    }

    private int findStorageGuHotbarSlot(final LocalPlayer player) {
        for (int slot = 0; slot < HOTBAR_SLOT_COUNT; slot++) {
            final ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof StorageGuItem) {
                return slot;
            }
        }
        return -1;
    }

    private boolean tryInteractWithEntity(final LocalPlayer player, final Entity entity) {
        if (player.getMainHandItem().isEmpty()) {
            return tryInteractWithEntity(player, entity, InteractionHand.MAIN_HAND)
                || tryInteractWithEntity(player, entity, InteractionHand.OFF_HAND);
        }
        if (player.getOffhandItem().isEmpty()) {
            return tryInteractWithEntity(player, entity, InteractionHand.OFF_HAND)
                || tryInteractWithEntity(player, entity, InteractionHand.MAIN_HAND);
        }
        return tryInteractWithEntity(player, entity, InteractionHand.MAIN_HAND)
            || tryInteractWithEntity(player, entity, InteractionHand.OFF_HAND);
    }

    private boolean tryInteractWithEntity(
        final LocalPlayer player,
        final Entity entity,
        final InteractionHand hand
    ) {
        return isAcceptedInteractionResult(currentGameMode().interact(player, entity, hand));
    }

    private boolean tryInteractWithBlock(final LocalPlayer player, final BlockPos targetPos) {
        if (player.getMainHandItem().isEmpty()) {
            return tryInteractWithBlock(player, targetPos, InteractionHand.MAIN_HAND)
                || tryInteractWithBlock(player, targetPos, InteractionHand.OFF_HAND);
        }
        if (player.getOffhandItem().isEmpty()) {
            return tryInteractWithBlock(player, targetPos, InteractionHand.OFF_HAND)
                || tryInteractWithBlock(player, targetPos, InteractionHand.MAIN_HAND);
        }
        return tryInteractWithBlock(player, targetPos, InteractionHand.MAIN_HAND)
            || tryInteractWithBlock(player, targetPos, InteractionHand.OFF_HAND);
    }

    private boolean tryInteractWithBlock(
        final LocalPlayer player,
        final BlockPos targetPos,
        final InteractionHand hand
    ) {
        final BlockHitResult blockHitResult = new BlockHitResult(
            Vec3.atCenterOf(targetPos),
            Direction.UP,
            targetPos,
            false
        );
        return isAcceptedInteractionResult(currentGameMode().useItemOn(player, hand, blockHitResult));
    }

    private <T extends Entity> T findNearestLoadedEntity(final LocalPlayer player, final Class<T> entityType) {
        T nearestEntity = null;
        double nearestDistance = Double.MAX_VALUE;
        final AABB searchBox = player.getBoundingBox().inflate(DIRECT_ROUTE_ENTITY_SCAN_RADIUS);
        for (T candidate : player.level().getEntitiesOfClass(entityType, searchBox)) {
            if (!candidate.isAlive()) {
                continue;
            }
            final double candidateDistance = player.distanceToSqr(candidate);
            if (candidateDistance > DIRECT_ROUTE_INTERACTION_DISTANCE_SQR) {
                continue;
            }
            if (candidateDistance < nearestDistance) {
                nearestEntity = candidate;
                nearestDistance = candidateDistance;
            }
        }
        return nearestEntity;
    }

    private BlockPos findNearestLoadedBlock(
        final LocalPlayer player,
        final Class<? extends Block> blockType
    ) {
        final BlockPos playerPos = player.blockPosition();
        final int minY = Math.max(
            player.level().getMinBuildHeight(),
            playerPos.getY() - DIRECT_ROUTE_VERTICAL_SCAN_RADIUS
        );
        final int maxY = Math.min(
            player.level().getMaxBuildHeight() - 1,
            playerPos.getY() + DIRECT_ROUTE_VERTICAL_SCAN_RADIUS
        );
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos nearestBlockPos = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = playerPos.getX() - DIRECT_ROUTE_BLOCK_SCAN_RADIUS;
            x <= playerPos.getX() + DIRECT_ROUTE_BLOCK_SCAN_RADIUS;
            x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = playerPos.getZ() - DIRECT_ROUTE_BLOCK_SCAN_RADIUS;
                    z <= playerPos.getZ() + DIRECT_ROUTE_BLOCK_SCAN_RADIUS;
                    z++) {
                    cursor.set(x, y, z);
                    if (!player.level().hasChunkAt(cursor)) {
                        continue;
                    }
                    if (!blockType.isInstance(player.level().getBlockState(cursor).getBlock())) {
                        continue;
                    }
                    final double candidateDistance = cursor.distToCenterSqr(
                        player.getX(),
                        player.getY(),
                        player.getZ()
                    );
                    if (candidateDistance > DIRECT_ROUTE_INTERACTION_DISTANCE_SQR) {
                        continue;
                    }
                    if (candidateDistance < nearestDistance) {
                        nearestBlockPos = cursor.immutable();
                        nearestDistance = candidateDistance;
                    }
                }
            }
        }
        return nearestBlockPos;
    }

    private static boolean isAcceptedInteractionResult(final InteractionResult interactionResult) {
        return interactionResult != InteractionResult.PASS
            && interactionResult != InteractionResult.FAIL;
    }

    private MultiPlayerGameMode currentGameMode() {
        if (minecraft == null || minecraft.gameMode == null) {
            throw new IllegalStateException("Hub 直达路由缺少客户端 gameMode 上下文");
        }
        return minecraft.gameMode;
    }

    private void announceRouteResolutionFailure(final String targetDisplayName, final String failureReason) {
        announceRouteMessage("无法前往" + targetDisplayName + "：" + failureReason);
    }

    private void announceRouteMessage(final String message) {
        if (minecraft == null || minecraft.player == null || message == null || message.isBlank()) {
            return;
        }
        minecraft.player.displayClientMessage(Component.literal(message), true);
    }

    private void refreshHubData() {
        final HubSnapshot snapshot = HubSnapshot.fromApertureMenu(menu);
        final HubStatusEvaluator.HubStatus status = statusEvaluator.evaluate(snapshot);

        headerDetailLabel.setText(buildHeaderDetailText(snapshot.core()));
        stabilityHeadlinePill.applyRisk(status.overallRisk());

        overallSummaryBlock.applyTone(HubUiTokens.toneForRisk(status.overallRisk()));
        overallSummaryBlock.setBodyText(buildTopOverallSummary(snapshot, status));

        riskSummaryBlock.applyTone(resolveTribulationTone(snapshot.core().tribulationTick()));
        riskSummaryBlock.setBodyText(buildTopTribulationSummary(snapshot.core().tribulationTick(), status));

        nextRouteBlock.applyTone(resolveNextRouteTone(status));
        nextRouteBlock.setBodyText(status.recommendation());

        utilityRoutesBlock.setBodyText(buildQuickRouteText());
        summaryFootnoteBlock.setBodyText(buildRecentStatusSummary(status));
        fallbackExplainerBlock.setBodyText(buildHubGuideText());

        for (HubRoutePolicy.CardRoutePolicy policy : HubRoutePolicy.orderedPolicies()) {
            updateModuleCard(policy, snapshot, status);
        }
    }

    private void updateModuleCard(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        final ModuleCard card = moduleCardsById.get(policy.cardId());
        if (card == null) {
            return;
        }

        card.applyRoutePolicy(policy);
        card.setTitle(policy.cardTitle());
        card.setSummary(resolveCardSummary(policy, snapshot, status));
        card.setFootnote(resolveCardFootnote(policy, snapshot, status));
        card.applyRisk(resolveCardRisk(policy, snapshot, status));
    }

    private HubStatusEvaluator.RiskLevel resolveCardRisk(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> status.overallRisk();
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? HubStatusEvaluator.RiskLevel.STABLE
                : HubStatusEvaluator.RiskLevel.UNKNOWN;
            case HubRoutePolicy.CARD_TRIBULATION -> resolveTribulationRisk(snapshot.core().tribulationTick());
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? HubStatusEvaluator.RiskLevel.CAUTION
                : HubStatusEvaluator.RiskLevel.STABLE;
            default -> HubStatusEvaluator.RiskLevel.UNKNOWN;
        };
    }

    private String resolveCardSummary(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> formatCoreSummary(snapshot.core());
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? formatLandSpiritSummary(snapshot.landSpirit())
                : snapshot.landSpirit().fallbackText();
            case HubRoutePolicy.CARD_TRIBULATION -> formatTribulationSummary(snapshot.core().tribulationTick(), status);
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()
                ? snapshot.resource().fallbackText()
                : formatResourceSummary(snapshot.resource());
            default -> buildLightCardSummary(policy);
        };
    }

    private String resolveCardFootnote(
        final HubRoutePolicy.CardRoutePolicy policy,
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> buildCoreFootnote(snapshot.core());
            case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()
                ? buildLandSpiritFootnote(snapshot.landSpirit())
                : snapshot.landSpirit().fallbackText();
            case HubRoutePolicy.CARD_TRIBULATION -> buildTribulationFootnote(snapshot.core().tribulationTick(), status);
            case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().fallbackText();
            default -> buildLightCardFootnote(policy);
        };
    }

    private HubUiTokens.HubTone resolveTribulationTone(final long tribulationTick) {
        return HubUiTokens.toneForRisk(resolveTribulationRisk(tribulationTick));
    }

    private HubStatusEvaluator.RiskLevel resolveTribulationRisk(final long tribulationTick) {
        if (tribulationTick <= 0L) {
            return HubStatusEvaluator.RiskLevel.DANGER;
        }
        if (tribulationTick <= TRIBULATION_WARNING_TICKS) {
            return HubStatusEvaluator.RiskLevel.CAUTION;
        }
        return HubStatusEvaluator.RiskLevel.STABLE;
    }

    private HubUiTokens.HubTone resolveNextRouteTone(final HubStatusEvaluator.HubStatus status) {
        return switch (status.overallRisk()) {
            case DANGER -> HubUiTokens.HubTone.DANGER;
            case UNKNOWN -> HubUiTokens.HubTone.AZURE;
            case CAUTION -> HubUiTokens.HubTone.WARN;
            case STABLE -> HubUiTokens.HubTone.GOLD;
        };
    }

    private String buildQuickRouteText() {
        final List<String> routeEntries = HubRoutePolicy.orderedPolicies()
            .stream()
            .map(this::formatQuickRouteEntry)
            .toList();
        return "仅映射已声明目标：" + String.join(" / ", routeEntries);
    }

    private String buildRecentStatusSummary(final HubStatusEvaluator.HubStatus status) {
        return "非权威运营占位：轻卡未接入稳定聚合源，请以前往分台核验为准；当前建议："
            + status.recommendation();
    }

    private String buildHubGuideText() {
        return "主殿只保留总览与入口；炼丹、储物、集群、道痕仅显示摘要入口或占位入口，"
            + "不展示看似精准的全局运行数值";
    }

    private String buildHeaderDetailText(final HubSnapshot.CoreSnapshot coreSnapshot) {
        if (!coreSnapshot.hasValidBoundary()) {
            return formatTier(coreSnapshot.tier())
                + " · 边界待重同步 · "
                + formatFrozenState(coreSnapshot.frozen());
        }
        return formatTier(coreSnapshot.tier())
            + " · "
            + coreSnapshot.chunkSpanX()
            + "×"
            + coreSnapshot.chunkSpanZ()
            + "界域 · "
            + formatFrozenState(coreSnapshot.frozen());
    }

    private String buildTopOverallSummary(
        final HubSnapshot snapshot,
        final HubStatusEvaluator.HubStatus status
    ) {
        if (!snapshot.core().hasValidBoundary()) {
            return status.overallSummary();
        }
        return status.overallSummary()
            + " · 流速 "
            + formatTimeSpeed(snapshot.core().timeSpeedPercent())
            + "x";
    }

    private String buildTopTribulationSummary(
        final long tribulationTick,
        final HubStatusEvaluator.HubStatus status
    ) {
        if (tribulationTick <= 0L) {
            return status.tribulationRiskSummary();
        }
        return formatTribulationSummary(tribulationTick, status)
            + " · "
            + status.tribulationRiskSummary();
    }

    private String formatCoreSummary(final HubSnapshot.CoreSnapshot coreSnapshot) {
        if (!coreSnapshot.hasValidBoundary()) {
            return "边界异常，待重新同步";
        }
        return formatTimeSpeed(coreSnapshot.timeSpeedPercent())
            + "x · 好感 "
            + formatPercentValue(coreSnapshot.favorabilityPercent());
    }

    private String buildCoreFootnote(final HubSnapshot.CoreSnapshot coreSnapshot) {
        if (!coreSnapshot.hasValidBoundary()) {
            return "边界异常，待重新同步";
        }
        return "边界 X"
            + coreSnapshot.minChunkX()
            + "~"
            + coreSnapshot.maxChunkX()
            + " / Z"
            + coreSnapshot.minChunkZ()
            + "~"
            + coreSnapshot.maxChunkZ();
    }

    private String formatLandSpiritSummary(final HubSnapshot.LandSpiritSnapshot landSpiritSnapshot) {
        return "阶段 "
            + landSpiritSnapshot.stage()
            + " · "
            + formatTier(landSpiritSnapshot.tier())
            + " · 好感 "
            + formatPermillePercent(landSpiritSnapshot.favorabilityPermille());
    }

    private String buildLandSpiritFootnote(final HubSnapshot.LandSpiritSnapshot landSpiritSnapshot) {
        return "下阶段：转数 "
            + landSpiritSnapshot.nextStageMinTier()
            + " · 好感 "
            + formatPermillePercent(landSpiritSnapshot.nextStageMinFavorabilityPermille());
    }

    private String formatTribulationSummary(
        final long tribulationTick,
        final HubStatusEvaluator.HubStatus status
    ) {
        if (tribulationTick <= 0L) {
            return status.tribulationRiskSummary();
        }
        return "倒计时 " + formatTribulationTime(tribulationTick);
    }

    private String buildTribulationFootnote(
        final long tribulationTick,
        final HubStatusEvaluator.HubStatus status
    ) {
        if (tribulationTick <= 0L) {
            return status.recommendation();
        }
        return status.tribulationRiskSummary();
    }

    private String formatResourceSummary(final HubSnapshot.ResourceSnapshot resourceSnapshot) {
        return (resourceSnapshot.formed() ? "已成阵" : "未成阵")
            + " · 进度 "
            + formatPermillePercent(resourceSnapshot.progressPermille())
            + " · 效率 "
            + resourceSnapshot.efficiencyPercent()
            + "%";
    }

    private String buildLightCardSummary(final HubRoutePolicy.CardRoutePolicy policy) {
        return switch (policy.cardId()) {
            case HubRoutePolicy.CARD_ALCHEMY -> "独立工作台，待前往查看";
            case HubRoutePolicy.CARD_STORAGE -> "物品域最小视图，待前往查看";
            case HubRoutePolicy.CARD_CLUSTER -> "本地产出待提取，需前往查看";
            case HubRoutePolicy.CARD_DAO_MARK -> "占位子页入口，待前往查看";
            default -> throw new IllegalArgumentException("未声明轻卡摘要文案: " + policy.cardId());
        };
    }

    private String buildLightCardFootnote(final HubRoutePolicy.CardRoutePolicy policy) {
        if (policy.usesPlaceholder()) {
            return policy.noticeText() + "；主殿不展示全局实况";
        }
        return "仅保留入口，需前往" + policy.target().displayName() + "核验";
    }

    private String formatQuickRouteEntry(final HubRoutePolicy.CardRoutePolicy policy) {
        if (policy.staysOnHub()) {
            return policy.cardTitle() + "→" + policy.target().displayName();
        }
        return policy.cardTitle() + "→" + policy.target().displayName();
    }

    private static String formatTimeSpeed(final int percent) {
        return String.format(Locale.ROOT, "%.2f", percent / PERCENT_BASE);
    }

    private static String formatPercentValue(final int percent) {
        return String.format(Locale.ROOT, "%.2f%%", percent / PERCENT_BASE);
    }

    private static String formatPermillePercent(final int permille) {
        return String.format(Locale.ROOT, "%.1f%%", permille / PERMILLE_TO_PERCENT);
    }

    private static String formatTier(final int tier) {
        return tier + "转";
    }

    private static String formatFrozenState(final boolean frozen) {
        return frozen ? "冻结态" : "运行态";
    }

    private static String formatTribulationTime(final long ticks) {
        final long totalSeconds = ticks / TICKS_PER_SECOND;
        final long hours = totalSeconds / SECONDS_PER_HOUR;
        final long days = hours / HOURS_PER_DAY;
        final long remainHours = hours % HOURS_PER_DAY;
        return days + "天 " + remainHours + "时";
    }

    private static String wrapText(final String text, final int maxCharsPerLine) {
        if (text == null || text.isBlank() || maxCharsPerLine <= 0) {
            return Objects.requireNonNullElse(text, "");
        }

        final StringBuilder builder = new StringBuilder(text.length() + text.length() / maxCharsPerLine);
        int lineCharCount = 0;
        for (int index = 0; index < text.length(); index++) {
            final char current = text.charAt(index);
            builder.append(current);
            if (current == '\n') {
                lineCharCount = 0;
                continue;
            }
            lineCharCount++;
            if (lineCharCount >= maxCharsPerLine && index + 1 < text.length()) {
                builder.append('\n');
                lineCharCount = 0;
            }
        }
        return builder.toString();
    }

    static record ShellLayout(
        int viewportWidth,
        int viewportHeight,
        int windowWidth,
        int windowHeight,
        int bodyY,
        int bodyWidth,
        int bodyViewportHeight,
        int bodyContentHeight,
        int moduleColumns,
        int moduleGridHeight,
        int moduleCardWidth,
        boolean useBodyScrollFallback
    ) {
    }

    private static final class InfoBlock extends HubPanel {

        private static final int TITLE_Y = HubUiTokens.PANEL_PADDING + 2;
        private static final int BODY_TOP_GAP = 2;
        private static final int TITLE_HEIGHT = 12;

        private final Label titleLabel;
        private final Label bodyLabel;
        private String rawBodyText = "";

        private InfoBlock(final String title) {
            titleLabel = new Label(Objects.requireNonNullElse(title, ""), HubUiTokens.hallTheme());
            bodyLabel = new Label("", HubUiTokens.hallTheme());
            addChild(titleLabel);
            addChild(bodyLabel);
            refreshColors();
        }

        private void applyTone(final HubUiTokens.HubTone tone) {
            setTone(tone);
            refreshColors();
        }

        private void setBodyText(final String bodyText) {
            rawBodyText = Objects.requireNonNullElse(bodyText, "");
            refreshBodyText();
        }

        @Override
        public void onLayoutUpdated() {
            super.onLayoutUpdated();
            final int innerWidth = Math.max(1, getWidth() - HubUiTokens.PANEL_PADDING * 2);
            final int bodyY = TITLE_Y + TITLE_HEIGHT + BODY_TOP_GAP;
            titleLabel.setFrame(HubUiTokens.PANEL_PADDING, TITLE_Y, innerWidth, TITLE_HEIGHT);
            bodyLabel.setFrame(
                HubUiTokens.PANEL_PADDING,
                bodyY,
                innerWidth,
                Math.max(1, getHeight() - bodyY - HubUiTokens.PANEL_PADDING)
            );
            refreshColors();
            refreshBodyText();
        }

        private void refreshColors() {
            titleLabel.setColor(getResolvedPalette().textColor());
            bodyLabel.setColor(getResolvedPalette().mutedTextColor());
        }

        private void refreshBodyText() {
            final int innerWidth = Math.max(1, getWidth() - HubUiTokens.PANEL_PADDING * 2);
            final int wrapWidth = Math.max(
                TEXT_WRAP_MIN_CHARS,
                Math.min(TEXT_WRAP_MAX_CHARS, innerWidth / TEXT_WRAP_PIXEL_PER_CHAR)
            );
            bodyLabel.setText(wrapText(rawBodyText, wrapWidth));
        }
    }
}
