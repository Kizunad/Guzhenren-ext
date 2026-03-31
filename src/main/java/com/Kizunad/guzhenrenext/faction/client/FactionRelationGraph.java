package com.Kizunad.guzhenrenext.faction.client;

import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphSnapshot.EdgeSnapshot;
import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphSnapshot.NodeSnapshot;
import com.Kizunad.guzhenrenext.network.ServerboundFactionRelationGraphRequestPayload;
import com.Kizunad.tinyUI.core.ScaleConfig;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIScreen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class FactionRelationGraph extends TinyUIScreen {

    private static final String TITLE_TEXT = "势力关系图";
    private static final String PENDING_TEXT = "正在同步势力关系图数据...";
    private static final String EMPTY_TEXT = "暂无可显示的势力关系数据";
    private static final String DEFAULT_HOVER_TITLE = "悬停节点或连线可查看详情";
    private static final String DEFAULT_HOVER_DETAIL = "左键拖拽平移，滚轮缩放，ESC 关闭界面";
    private static final String SYNC_READY_TEXT = "已同步";
    private static final String SYNC_PENDING_TEXT = "等待同步";
    private static final String TYPE_SECT_TEXT = "宗门";
    private static final String TYPE_CLAN_TEXT = "家族";
    private static final String TYPE_ROGUE_GROUP_TEXT = "散修";
    private static final String TYPE_UNKNOWN_TEXT = "未知";
    private static final String LEVEL_HOSTILE_TEXT = "敌对";
    private static final String LEVEL_NEUTRAL_TEXT = "中立";
    private static final String LEVEL_FRIENDLY_TEXT = "友好";
    private static final String LEVEL_ALLIED_TEXT = "同盟";

    private static final int PANEL_MARGIN = 18;
    private static final int PANEL_BORDER = 1;
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 34;
    private static final int SECTION_GAP = 8;
    private static final int VIEWPORT_PADDING = 12;
    private static final int TEXT_PADDING = 10;
    private static final int TITLE_Y = 10;
    private static final int STATUS_Y = 10;
    private static final int FOOTER_TITLE_Y = 8;
    private static final int FOOTER_DETAIL_Y = 20;
    private static final int MESSAGE_BOX_WIDTH = 220;
    private static final int MESSAGE_BOX_HEIGHT = 28;
    private static final int MESSAGE_TEXT_X_OFFSET = 12;
    private static final int MESSAGE_TEXT_Y_OFFSET = 10;
    private static final int NODE_LABEL_Y_OFFSET = 13;
    private static final int MAX_NODE_LABEL_LENGTH = 10;
    private static final int MIN_VIEWPORT_SIZE = 96;
    private static final int BASE_NODE_SIZE = 18;
    private static final int MIN_NODE_SIZE = 12;
    private static final int MAX_NODE_SIZE = 30;
    private static final int EDGE_PIXEL_SIZE = 2;
    private static final int HOVER_THRESHOLD = 8;
    private static final int DENSE_NODE_COUNT = 8;

    private static final int COLOR_SCREEN_DIM = 0xC0101014;
    private static final int COLOR_PANEL_BG = 0xF01A1E27;
    private static final int COLOR_PANEL_BORDER = 0xFF5A6478;
    private static final int COLOR_HEADER_BG = 0xFF242C39;
    private static final int COLOR_FOOTER_BG = 0xFF1E2530;
    private static final int COLOR_VIEWPORT_BG = 0xFF10151C;
    private static final int COLOR_VIEWPORT_BORDER = 0xFF3D495A;
    private static final int COLOR_NODE_FILL = 0xFFB68A3A;
    private static final int COLOR_NODE_BORDER = 0xFFF2D39B;
    private static final int COLOR_NODE_HOVER = 0xFFFFF2C6;
    private static final int COLOR_NODE_TEXT = 0xFFF2F5FA;
    private static final int COLOR_TEXT_PRIMARY = 0xFFF2F5FA;
    private static final int COLOR_TEXT_SECONDARY = 0xFFB7C0CF;
    private static final int COLOR_EDGE_HOSTILE = 0xFFE05A5A;
    private static final int COLOR_EDGE_NEUTRAL = 0xFF8B93A6;
    private static final int COLOR_EDGE_FRIENDLY = 0xFF6BC47A;
    private static final int COLOR_EDGE_ALLIED = 0xFF5CC4D9;
    private static final int COLOR_EDGE_HOVER = 0xFFF7F1B1;
    private static final int COLOR_MESSAGE_BG = 0xD0202632;

    private static final double DEFAULT_UI_SCALE = 1.0D;
    private static final double DEFAULT_ZOOM = 1.0D;
    private static final double MIN_ZOOM = 0.55D;
    private static final double MAX_ZOOM = 2.40D;
    private static final double ZOOM_STEP = 0.12D;
    private static final double PERCENT_BASE = 100.0D;
    private static final double RING_RADIUS_FACTOR = 0.32D;
    private static final double MAX_RING_RADIUS_FACTOR = 0.40D;
    private static final double EXTRA_RADIUS_PER_NODE = 8.0D;
    private static final double FULL_CIRCLE = Math.PI * 2.0D;
    private static final double START_ANGLE = -Math.PI / 2.0D;

    private boolean graphDragging;
    private boolean syncRequested;
    private double panX;
    private double panY;
    private double zoom = DEFAULT_ZOOM;

    public FactionRelationGraph() {
        super(Component.literal(TITLE_TEXT), new UIRoot());
    }

    @Override
    protected void init() {
        super.init();
        configureRoot();
        if (!syncRequested) {
            syncRequested = true;
            FactionRelationGraphClientState.markSyncPending();
            PacketDistributor.sendToServer(new ServerboundFactionRelationGraphRequestPayload());
        }
    }

    @Override
    protected void renderScaledContent(
        final GuiGraphics graphics,
        final int mouseX,
        final int mouseY,
        final float partialTick
    ) {
        final GraphViewport viewport = createViewport();
        final FactionRelationGraphSnapshot snapshot = FactionRelationGraphClientState.currentSnapshot();
        final HoverInfo defaultHover = createDefaultHover(snapshot);
        drawChrome(graphics, viewport, snapshot, defaultHover);
        if (!snapshot.synced()) {
            drawCenteredMessage(graphics, viewport, PENDING_TEXT);
            return;
        }
        if (!snapshot.hasData() || snapshot.nodes().isEmpty()) {
            drawCenteredMessage(graphics, viewport, EMPTY_TEXT);
            return;
        }
        final GraphLayout layout = buildLayout(snapshot, viewport);
        final HoverInfo hoverInfo = resolveHover(snapshot, layout, viewport, mouseX, mouseY);
        drawChrome(graphics, viewport, snapshot, hoverInfo);
        drawEdges(graphics, snapshot, layout, viewport, hoverInfo);
        drawNodes(graphics, layout, viewport, hoverInfo);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final double scaledMouseX = toScaled(mouseX);
        final double scaledMouseY = toScaled(mouseY);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && createViewport().contains(scaledMouseX, scaledMouseY)) {
            graphDragging = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && graphDragging) {
            graphDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
        final double mouseX,
        final double mouseY,
        final int button,
        final double dragX,
        final double dragY
    ) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && graphDragging) {
            panX += toScaled(dragX) / zoom;
            panY += toScaled(dragY) / zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(
        final double mouseX,
        final double mouseY,
        final double deltaX,
        final double deltaY
    ) {
        final double scaledMouseX = toScaled(mouseX);
        final double scaledMouseY = toScaled(mouseY);
        final GraphViewport viewport = createViewport();
        if (!viewport.contains(scaledMouseX, scaledMouseY)) {
            return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }
        final double worldXBefore = screenToWorldX(scaledMouseX, viewport, zoom, panX);
        final double worldYBefore = screenToWorldY(scaledMouseY, viewport, zoom, panY);
        zoom = clampDouble(zoom + deltaY * ZOOM_STEP, MIN_ZOOM, MAX_ZOOM);
        panX = ((scaledMouseX - viewport.centerX()) / zoom) - worldXBefore;
        panY = ((scaledMouseY - viewport.centerY()) / zoom) - worldYBefore;
        return true;
    }

    private void configureRoot() {
        final UIRoot root = getRoot();
        root.getScaleConfig().setScaleMode(ScaleConfig.ScaleMode.CUSTOM);
        root.getScaleConfig().setCustomScaleFactor(DEFAULT_UI_SCALE);
        root.setDesignResolution(width, height);
        root.setViewport(width, height);
    }

    private GraphViewport createViewport() {
        final int panelX = PANEL_MARGIN;
        final int panelY = PANEL_MARGIN;
        final int panelWidth = Math.max(MIN_VIEWPORT_SIZE, width - PANEL_MARGIN * 2);
        final int panelHeight = Math.max(MIN_VIEWPORT_SIZE, height - PANEL_MARGIN * 2);
        final int viewportX = panelX + VIEWPORT_PADDING;
        final int viewportY = panelY + HEADER_HEIGHT + SECTION_GAP;
        final int viewportWidth = Math.max(
            MIN_VIEWPORT_SIZE,
            panelWidth - VIEWPORT_PADDING * 2
        );
        final int viewportHeight = Math.max(
            MIN_VIEWPORT_SIZE,
            panelHeight - HEADER_HEIGHT - FOOTER_HEIGHT - SECTION_GAP * 2
        );
        return new GraphViewport(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight
        );
    }

    private void drawChrome(
        final GuiGraphics graphics,
        final GraphViewport viewport,
        final FactionRelationGraphSnapshot snapshot,
        final HoverInfo hoverInfo
    ) {
        graphics.fill(0, 0, width, height, COLOR_SCREEN_DIM);
        graphics.fill(
            viewport.panelX(),
            viewport.panelY(),
            viewport.panelRight(),
            viewport.panelBottom(),
            COLOR_PANEL_BG
        );
        drawBorder(
            graphics,
            viewport.panelX(),
            viewport.panelY(),
            viewport.panelWidth(),
            viewport.panelHeight(),
            COLOR_PANEL_BORDER
        );
        graphics.fill(
            viewport.panelX(),
            viewport.panelY(),
            viewport.panelRight(),
            viewport.panelY() + HEADER_HEIGHT,
            COLOR_HEADER_BG
        );
        graphics.fill(
            viewport.panelX(),
            viewport.footerY(),
            viewport.panelRight(),
            viewport.panelBottom(),
            COLOR_FOOTER_BG
        );
        graphics.fill(
            viewport.viewportX(),
            viewport.viewportY(),
            viewport.viewportRight(),
            viewport.viewportBottom(),
            COLOR_VIEWPORT_BG
        );
        drawBorder(
            graphics,
            viewport.viewportX(),
            viewport.viewportY(),
            viewport.viewportWidth(),
            viewport.viewportHeight(),
            COLOR_VIEWPORT_BORDER
        );
        graphics.drawString(
            font,
            TITLE_TEXT,
            viewport.panelX() + TEXT_PADDING,
            viewport.panelY() + TITLE_Y,
            COLOR_TEXT_PRIMARY,
            false
        );
        final String statusText = buildStatusText(snapshot);
        final int statusWidth = font.width(statusText);
        graphics.drawString(
            font,
            statusText,
            viewport.panelRight() - statusWidth - TEXT_PADDING,
            viewport.panelY() + STATUS_Y,
            COLOR_TEXT_SECONDARY,
            false
        );
        graphics.drawString(
            font,
            hoverInfo.title(),
            viewport.panelX() + TEXT_PADDING,
            viewport.footerY() + FOOTER_TITLE_Y,
            COLOR_TEXT_PRIMARY,
            false
        );
        graphics.drawString(
            font,
            hoverInfo.detail(),
            viewport.panelX() + TEXT_PADDING,
            viewport.footerY() + FOOTER_DETAIL_Y,
            COLOR_TEXT_SECONDARY,
            false
        );
    }

    private String buildStatusText(final FactionRelationGraphSnapshot snapshot) {
        final String syncState = snapshot.synced() ? SYNC_READY_TEXT : SYNC_PENDING_TEXT;
        return syncState
            + " · 节点 " + snapshot.nodes().size()
            + " · 连线 " + snapshot.edges().size()
            + " · 缩放 " + Math.round(zoom * PERCENT_BASE) + "%";
    }

    private void drawCenteredMessage(
        final GuiGraphics graphics,
        final GraphViewport viewport,
        final String text
    ) {
        final int boxX = viewport.centerX() - MESSAGE_BOX_WIDTH / 2;
        final int boxY = viewport.centerY() - MESSAGE_BOX_HEIGHT / 2;
        graphics.fill(
            boxX,
            boxY,
            boxX + MESSAGE_BOX_WIDTH,
            boxY + MESSAGE_BOX_HEIGHT,
            COLOR_MESSAGE_BG
        );
        drawBorder(
            graphics,
            boxX,
            boxY,
            MESSAGE_BOX_WIDTH,
            MESSAGE_BOX_HEIGHT,
            COLOR_VIEWPORT_BORDER
        );
        graphics.drawString(
            font,
            text,
            boxX + MESSAGE_TEXT_X_OFFSET,
            boxY + MESSAGE_TEXT_Y_OFFSET,
            COLOR_TEXT_PRIMARY,
            false
        );
    }

    private GraphLayout buildLayout(
        final FactionRelationGraphSnapshot snapshot,
        final GraphViewport viewport
    ) {
        final List<NodeSnapshot> nodes = snapshot.nodes();
        final List<GraphNodeRender> renderedNodes = new ArrayList<>(nodes.size());
        final Map<String, GraphNodeRender> renderedNodesById = new HashMap<>();
        final double ringRadius = computeRingRadius(viewport, nodes.size());
        final int nodeSize = clampInt(
            (int) Math.round(BASE_NODE_SIZE * zoom),
            MIN_NODE_SIZE,
            MAX_NODE_SIZE
        );
        for (int index = 0; index < nodes.size(); index++) {
            final NodeSnapshot node = nodes.get(index);
            final double angle = nodes.size() == 1
                ? 0.0D
                : START_ANGLE + (FULL_CIRCLE * index) / nodes.size();
            final double worldX = ringRadius * Math.cos(angle);
            final double worldY = ringRadius * Math.sin(angle);
            final int screenX = (int) Math.round(viewport.centerX() + (worldX + panX) * zoom);
            final int screenY = (int) Math.round(viewport.centerY() + (worldY + panY) * zoom);
            final GraphNodeRender renderedNode = new GraphNodeRender(
                node,
                worldX,
                worldY,
                screenX,
                screenY,
                nodeSize
            );
            renderedNodes.add(renderedNode);
            renderedNodesById.put(node.factionId(), renderedNode);
        }
        return new GraphLayout(renderedNodes, renderedNodesById);
    }

    private double computeRingRadius(final GraphViewport viewport, final int nodeCount) {
        if (nodeCount <= 1) {
            return 0.0D;
        }
        final double baseRadius = Math.min(viewport.viewportWidth(), viewport.viewportHeight())
            * RING_RADIUS_FACTOR;
        final double extraRadius = Math.max(0, nodeCount - DENSE_NODE_COUNT) * EXTRA_RADIUS_PER_NODE;
        final double maxRadius = Math.min(viewport.viewportWidth(), viewport.viewportHeight())
            * MAX_RING_RADIUS_FACTOR;
        return Math.min(baseRadius + extraRadius, maxRadius);
    }

    private void drawEdges(
        final GuiGraphics graphics,
        final FactionRelationGraphSnapshot snapshot,
        final GraphLayout layout,
        final GraphViewport viewport,
        final HoverInfo hoverInfo
    ) {
        for (EdgeSnapshot edge : snapshot.edges()) {
            final GraphNodeRender source = layout.node(edge.sourceFactionId());
            final GraphNodeRender target = layout.node(edge.targetFactionId());
            if (source == null || target == null) {
                continue;
            }
            if (!isVisibleInViewport(source, target, viewport)) {
                continue;
            }
            final int edgeColor = hoverInfo.matchesEdge(edge)
                ? COLOR_EDGE_HOVER
                : resolveEdgeColor(edge.relationLevel());
            drawLine(
                graphics,
                source.centerX(),
                source.centerY(),
                target.centerX(),
                target.centerY(),
                edgeColor
            );
        }
    }

    private void drawNodes(
        final GuiGraphics graphics,
        final GraphLayout layout,
        final GraphViewport viewport,
        final HoverInfo hoverInfo
    ) {
        for (GraphNodeRender node : layout.nodes()) {
            if (!viewport.contains(node.centerX(), node.centerY())) {
                continue;
            }
            final int fillColor = hoverInfo.matchesNode(node.snapshot().factionId())
                ? COLOR_NODE_HOVER
                : COLOR_NODE_FILL;
            graphics.fill(node.left(), node.top(), node.right(), node.bottom(), fillColor);
            drawBorder(
                graphics,
                node.left(),
                node.top(),
                node.size(),
                node.size(),
                COLOR_NODE_BORDER
            );
            final String label = abbreviate(node.snapshot().factionName(), MAX_NODE_LABEL_LENGTH);
            final int labelX = node.centerX() - font.width(label) / 2;
            final int labelY = node.bottom() + NODE_LABEL_Y_OFFSET;
            graphics.drawString(font, label, labelX, labelY, COLOR_NODE_TEXT, false);
        }
    }

    private HoverInfo resolveHover(
        final FactionRelationGraphSnapshot snapshot,
        final GraphLayout layout,
        final GraphViewport viewport,
        final double mouseX,
        final double mouseY
    ) {
        if (!viewport.contains(mouseX, mouseY)) {
            return createDefaultHover(snapshot);
        }
        for (GraphNodeRender node : layout.nodes()) {
            if (node.contains(mouseX, mouseY)) {
                return HoverInfo.forNode(
                    node.snapshot().factionId(),
                    node.snapshot().factionName(),
                    resolveFactionTypeText(node.snapshot().factionType())
                        + " · 成员 " + node.snapshot().memberCount()
                        + " · 势力 " + node.snapshot().power()
                        + " · 资源 " + node.snapshot().resources()
                );
            }
        }
        double bestDistance = HOVER_THRESHOLD * HOVER_THRESHOLD;
        HoverInfo bestHover = createDefaultHover(snapshot);
        for (EdgeSnapshot edge : snapshot.edges()) {
            final GraphNodeRender source = layout.node(edge.sourceFactionId());
            final GraphNodeRender target = layout.node(edge.targetFactionId());
            if (source == null || target == null) {
                continue;
            }
            final double distance = pointToSegmentDistanceSquared(
                mouseX,
                mouseY,
                source.centerX(),
                source.centerY(),
                target.centerX(),
                target.centerY()
            );
            if (distance <= bestDistance) {
                bestDistance = distance;
                bestHover = HoverInfo.forEdge(
                    edge.sourceFactionId(),
                    edge.targetFactionId(),
                    source.snapshot().factionName() + " ↔ " + target.snapshot().factionName(),
                    "关系值 " + edge.relationValue() + " · " + resolveRelationLevelText(edge.relationLevel())
                );
            }
        }
        return bestHover;
    }

    private HoverInfo createDefaultHover(final FactionRelationGraphSnapshot snapshot) {
        if (!snapshot.synced()) {
            return HoverInfo.defaultInfo(PENDING_TEXT, DEFAULT_HOVER_DETAIL);
        }
        if (!snapshot.hasData() || snapshot.nodes().isEmpty()) {
            return HoverInfo.defaultInfo(EMPTY_TEXT, DEFAULT_HOVER_DETAIL);
        }
        return HoverInfo.defaultInfo(DEFAULT_HOVER_TITLE, DEFAULT_HOVER_DETAIL);
    }

    private String resolveFactionTypeText(final String factionType) {
        return switch (factionType) {
            case "SECT" -> TYPE_SECT_TEXT;
            case "CLAN" -> TYPE_CLAN_TEXT;
            case "ROGUE_GROUP" -> TYPE_ROGUE_GROUP_TEXT;
            default -> TYPE_UNKNOWN_TEXT;
        };
    }

    private String resolveRelationLevelText(final String relationLevel) {
        return switch (relationLevel) {
            case "HOSTILE" -> LEVEL_HOSTILE_TEXT;
            case "FRIENDLY" -> LEVEL_FRIENDLY_TEXT;
            case "ALLIED" -> LEVEL_ALLIED_TEXT;
            default -> LEVEL_NEUTRAL_TEXT;
        };
    }

    private int resolveEdgeColor(final String relationLevel) {
        return switch (relationLevel) {
            case "HOSTILE" -> COLOR_EDGE_HOSTILE;
            case "FRIENDLY" -> COLOR_EDGE_FRIENDLY;
            case "ALLIED" -> COLOR_EDGE_ALLIED;
            default -> COLOR_EDGE_NEUTRAL;
        };
    }

    private void drawBorder(
        final GuiGraphics graphics,
        final int x,
        final int y,
        final int width,
        final int height,
        final int color
    ) {
        graphics.fill(x, y, x + width, y + PANEL_BORDER, color);
        graphics.fill(x, y + height - PANEL_BORDER, x + width, y + height, color);
        graphics.fill(x, y, x + PANEL_BORDER, y + height, color);
        graphics.fill(x + width - PANEL_BORDER, y, x + width, y + height, color);
    }

    private void drawLine(
        final GuiGraphics graphics,
        final int x1,
        final int y1,
        final int x2,
        final int y2,
        final int color
    ) {
        final int steps = Math.max(1, Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)));
        for (int step = 0; step <= steps; step++) {
            final double progress = (double) step / steps;
            final int drawX = (int) Math.round(x1 + (x2 - x1) * progress);
            final int drawY = (int) Math.round(y1 + (y2 - y1) * progress);
            graphics.fill(drawX, drawY, drawX + EDGE_PIXEL_SIZE, drawY + EDGE_PIXEL_SIZE, color);
        }
    }

    private boolean isVisibleInViewport(
        final GraphNodeRender source,
        final GraphNodeRender target,
        final GraphViewport viewport
    ) {
        final int minX = Math.min(source.centerX(), target.centerX()) - HOVER_THRESHOLD;
        final int maxX = Math.max(source.centerX(), target.centerX()) + HOVER_THRESHOLD;
        final int minY = Math.min(source.centerY(), target.centerY()) - HOVER_THRESHOLD;
        final int maxY = Math.max(source.centerY(), target.centerY()) + HOVER_THRESHOLD;
        return maxX >= viewport.viewportX()
            && minX <= viewport.viewportRight()
            && maxY >= viewport.viewportY()
            && minY <= viewport.viewportBottom();
    }

    private double pointToSegmentDistanceSquared(
        final double pointX,
        final double pointY,
        final double startX,
        final double startY,
        final double endX,
        final double endY
    ) {
        final double deltaX = endX - startX;
        final double deltaY = endY - startY;
        if (deltaX == 0.0D && deltaY == 0.0D) {
            return squaredDistance(pointX, pointY, startX, startY);
        }
        final double projection = clampDouble(
            ((pointX - startX) * deltaX + (pointY - startY) * deltaY)
                / (deltaX * deltaX + deltaY * deltaY),
            0.0D,
            1.0D
        );
        final double nearestX = startX + deltaX * projection;
        final double nearestY = startY + deltaY * projection;
        return squaredDistance(pointX, pointY, nearestX, nearestY);
    }

    private double squaredDistance(
        final double x1,
        final double y1,
        final double x2,
        final double y2
    ) {
        final double deltaX = x1 - x2;
        final double deltaY = y1 - y2;
        return deltaX * deltaX + deltaY * deltaY;
    }

    private double toScaled(final double actualValue) {
        return getRoot().getScaleConfig().unscale(actualValue);
    }

    private double screenToWorldX(
        final double screenX,
        final GraphViewport viewport,
        final double currentZoom,
        final double currentPanX
    ) {
        return ((screenX - viewport.centerX()) / currentZoom) - currentPanX;
    }

    private double screenToWorldY(
        final double screenY,
        final GraphViewport viewport,
        final double currentZoom,
        final double currentPanY
    ) {
        return ((screenY - viewport.centerY()) / currentZoom) - currentPanY;
    }

    private double clampDouble(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clampInt(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String abbreviate(final String text, final int maxLength) {
        if (text == null || text.isEmpty() || text.length() <= maxLength) {
            return text == null ? "" : text;
        }
        return text.substring(0, maxLength - 1) + "…";
    }

    private record GraphViewport(
        int panelX,
        int panelY,
        int panelWidth,
        int panelHeight,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight
    ) {
        int panelRight() {
            return panelX + panelWidth;
        }

        int panelBottom() {
            return panelY + panelHeight;
        }

        int viewportRight() {
            return viewportX + viewportWidth;
        }

        int viewportBottom() {
            return viewportY + viewportHeight;
        }

        int centerX() {
            return viewportX + viewportWidth / 2;
        }

        int centerY() {
            return viewportY + viewportHeight / 2;
        }

        int footerY() {
            return panelY + panelHeight - FOOTER_HEIGHT;
        }

        boolean contains(final double x, final double y) {
            return x >= viewportX && x <= viewportRight() && y >= viewportY && y <= viewportBottom();
        }
    }

    private record GraphNodeRender(
        NodeSnapshot snapshot,
        double worldX,
        double worldY,
        int centerX,
        int centerY,
        int size
    ) {
        int left() {
            return centerX - size / 2;
        }

        int right() {
            return left() + size;
        }

        int top() {
            return centerY - size / 2;
        }

        int bottom() {
            return top() + size;
        }

        boolean contains(final double x, final double y) {
            return x >= left() && x <= right() && y >= top() && y <= bottom();
        }
    }

    private record GraphLayout(
        List<GraphNodeRender> nodes,
        Map<String, GraphNodeRender> nodesById
    ) {
        GraphNodeRender node(final String factionId) {
            return nodesById.get(factionId);
        }
    }

    private record HoverInfo(
        String title,
        String detail,
        String nodeId,
        String edgeSourceId,
        String edgeTargetId
    ) {
        static HoverInfo defaultInfo(final String title, final String detail) {
            return new HoverInfo(title, detail, "", "", "");
        }

        static HoverInfo forNode(final String nodeId, final String title, final String detail) {
            return new HoverInfo(title, detail, nodeId, "", "");
        }

        static HoverInfo forEdge(
            final String sourceId,
            final String targetId,
            final String title,
            final String detail
        ) {
            return new HoverInfo(title, detail, "", sourceId, targetId);
        }

        boolean matchesNode(final String factionId) {
            return nodeId.equals(factionId);
        }

        boolean matchesEdge(final EdgeSnapshot edge) {
            return edge.sourceFactionId().equals(edgeSourceId)
                && edge.targetFactionId().equals(edgeTargetId);
        }
    }
}
