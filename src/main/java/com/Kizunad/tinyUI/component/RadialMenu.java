package com.Kizunad.tinyUI.component;

import com.Kizunad.tinyUI.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * 轮盘菜单（Radial Menu）。
 * <p>
 * 该类只负责：
 * <ul>
 *   <li>将鼠标位置映射到扇区索引（含内圈盲区/外圈边界）</li>
 *   <li>绘制扇区背景与图标（使用 {@link RenderUtils} 的低层渲染）</li>
 *   <li>维护悬停高亮的平滑动画状态</li>
 * </ul>
 * <p>
 * “选择后触发什么”属于上层 Screen/业务逻辑职责，本类仅提供当前悬停项与渲染输出，便于在 GuzhenrenExt 中复用。
 * </p>
 */
public final class RadialMenu {

    /** 默认背景色：半透明黑。 */
    public static final int DEFAULT_BACKGROUND_COLOR = 0x80000000;
    /** 默认高亮色：半透明白。 */
    public static final int DEFAULT_HIGHLIGHT_COLOR = 0x80FFFFFF;

    private static final int DEFAULT_ICON_SIZE = 16;
    private static final float DEFAULT_HOVER_ICON_SCALE = 1.25F;
    private static final float DEFAULT_ANIMATION_LERP = 0.25F;
    private static final float ARC_VERTEX_STEP_RAD = Mth.DEG_TO_RAD * 2.0F;
    private static final int CHANNEL_MASK = 0xFF;
    private static final int SHIFT_ALPHA = 24;
    private static final int SHIFT_RED = 16;
    private static final int SHIFT_GREEN = 8;

    private final int outerRadius;
    private final int innerRadius;
    private final int backgroundColor;
    private final int highlightColor;
    private final List<Option> options;
    private final float[] highlightProgress;

    private int hoveredIndex;

    /**
     * @param innerRadius 内半径（鼠标位于此半径内视为“盲区”）
     * @param outerRadius 外半径（鼠标超过此半径不触发选择）
     * @param options 选项列表（为空时菜单不渲染）
     */
    public RadialMenu(
        final int innerRadius,
        final int outerRadius,
        final List<Option> options
    ) {
        this(innerRadius, outerRadius, options, DEFAULT_BACKGROUND_COLOR, DEFAULT_HIGHLIGHT_COLOR);
    }

    public RadialMenu(
        final int innerRadius,
        final int outerRadius,
        final List<Option> options,
        final int backgroundColor,
        final int highlightColor
    ) {
        this.innerRadius = Math.max(0, innerRadius);
        this.outerRadius = Math.max(this.innerRadius + 1, outerRadius);
        this.backgroundColor = backgroundColor;
        this.highlightColor = highlightColor;
        this.options = new ArrayList<>(options);
        this.highlightProgress = new float[this.options.size()];
        this.hoveredIndex = -1;
    }

    public int getHoveredIndex() {
        return hoveredIndex;
    }

    public Option getHoveredOption() {
        if (hoveredIndex < 0 || hoveredIndex >= options.size()) {
            return null;
        }
        return options.get(hoveredIndex);
    }

    /**
     * 每 tick 更新高亮插值，避免鼠标在扇区边界快速移动时出现闪烁感。
     */
    public void tick() {
        for (int i = 0; i < highlightProgress.length; i++) {
            final float target = (i == hoveredIndex) ? 1.0F : 0.0F;
            highlightProgress[i] = Mth.lerp(DEFAULT_ANIMATION_LERP, highlightProgress[i], target);
        }
    }

    /**
     * 根据鼠标位置计算当前悬停扇区。
     * <p>
     * 角度空间的定义：扇区 0 从“12 点钟方向”开始顺时针递增，与常见轮盘菜单体验一致。
     * </p>
     */
    public void updateSelection(
        final int mouseX,
        final int mouseY,
        final int centerX,
        final int centerY
    ) {
        if (options.isEmpty()) {
            hoveredIndex = -1;
            return;
        }

        final int dx = mouseX - centerX;
        final int dy = mouseY - centerY;
        final int distanceSq = dx * dx + dy * dy;

        final int innerSq = innerRadius * innerRadius;
        final int outerSq = outerRadius * outerRadius;
        if (distanceSq <= innerSq || distanceSq > outerSq) {
            hoveredIndex = -1;
            return;
        }

        final double rawAngle = Math.atan2(dy, dx);
        double angle = rawAngle;
        if (angle < 0.0D) {
            angle += Mth.TWO_PI;
        }

        // 旋转坐标系：使 12 点钟为起始点（北向），并将范围归一化到 [0, 2π)。
        angle += Mth.HALF_PI;
        if (angle >= Mth.TWO_PI) {
            angle -= Mth.TWO_PI;
        }

        final double sectorAngle = Mth.TWO_PI / (double) options.size();
        final int index = (int) (angle / sectorAngle);
        hoveredIndex = Mth.clamp(index, 0, options.size() - 1);
    }

    /**
     * 渲染轮盘：扇区背景 + 图标。
     *
     * @param graphics GUI 图形上下文
     * @param centerX 轮盘圆心 X（设计分辨率空间）
     * @param centerY 轮盘圆心 Y（设计分辨率空间）
     */
    public void render(
        final GuiGraphics graphics,
        final int centerX,
        final int centerY
    ) {
        if (options.isEmpty()) {
            return;
        }

        final PoseParams poseParams = new PoseParams(graphics, centerX, centerY);
        renderSectors(poseParams);
        renderIcons(poseParams);
    }

    private void renderSectors(final PoseParams poseParams) {
        final float cx = poseParams.centerX();
        final float cy = poseParams.centerY();
        final float inner = (float) innerRadius;
        final float outer = (float) outerRadius;
        final float sectorAngle = (float) (Mth.TWO_PI / (double) options.size());

        for (int i = 0; i < options.size(); i++) {
            final float startView = i * sectorAngle;
            final float endView = (i + 1) * sectorAngle;

            // view 空间：0 朝北；转换到三角函数空间：0 朝东。
            final float startTrig = startView - Mth.HALF_PI;
            final float endTrig = endView - Mth.HALF_PI;

            final int segments = Math.max(
                1,
                (int) Math.ceil((endTrig - startTrig) / ARC_VERTEX_STEP_RAD)
            );

            final float highlight = (i < highlightProgress.length) ? highlightProgress[i] : 0.0F;
            final int color = lerpArgb(backgroundColor, highlightColor, highlight);

            RenderUtils.drawRingSector(
                poseParams.pose(),
                cx,
                cy,
                new RenderUtils.RingArc(inner, outer, startTrig, endTrig, segments),
                color
            );
        }
    }

    private void renderIcons(final PoseParams poseParams) {
        final float sectorAngle = (float) (Mth.TWO_PI / (double) options.size());
        final float iconRadius = ((float) innerRadius + (float) outerRadius) / 2.0F;
        final int halfIcon = DEFAULT_ICON_SIZE / 2;

        for (int i = 0; i < options.size(); i++) {
            final Option option = options.get(i);
            if (option.icon().isEmpty()) {
                continue;
            }

            final float midView = ((float) i + 0.5F) * sectorAngle;
            final float midTrig = midView - Mth.HALF_PI;

            final float x = poseParams.centerX() + Mth.cos(midTrig) * iconRadius;
            final float y = poseParams.centerY() + Mth.sin(midTrig) * iconRadius;

            final float highlight = (i < highlightProgress.length) ? highlightProgress[i] : 0.0F;
            final float scale = 1.0F + (DEFAULT_HOVER_ICON_SCALE - 1.0F) * highlight;

            poseParams.graphics().pose().pushPose();
            poseParams.graphics().pose().translate(x, y, 0.0F);
            poseParams.graphics().pose().scale(scale, scale, 1.0F);
            poseParams.graphics().pose().translate(-halfIcon, -halfIcon, 0.0F);
            poseParams.graphics().renderItem(option.icon(), 0, 0);
            poseParams.graphics().pose().popPose();
        }
    }

    private static int lerpArgb(final int from, final int to, final float t) {
        final float clamped = Mth.clamp(t, 0.0F, 1.0F);

        final int a1 = (from >>> SHIFT_ALPHA) & CHANNEL_MASK;
        final int r1 = (from >>> SHIFT_RED) & CHANNEL_MASK;
        final int g1 = (from >>> SHIFT_GREEN) & CHANNEL_MASK;
        final int b1 = from & CHANNEL_MASK;

        final int a2 = (to >>> SHIFT_ALPHA) & CHANNEL_MASK;
        final int r2 = (to >>> SHIFT_RED) & CHANNEL_MASK;
        final int g2 = (to >>> SHIFT_GREEN) & CHANNEL_MASK;
        final int b2 = to & CHANNEL_MASK;

        final int a = (int) Mth.lerp(clamped, a1, a2);
        final int r = (int) Mth.lerp(clamped, r1, r2);
        final int g = (int) Mth.lerp(clamped, g1, g2);
        final int b = (int) Mth.lerp(clamped, b1, b2);

        return (a << SHIFT_ALPHA) | (r << SHIFT_RED) | (g << SHIFT_GREEN) | b;
    }

    /**
     * 轮盘条目：图标 + 文本。
     */
    public record Option(ItemStack icon, Component label) {
        public Option {
            if (icon == null) {
                icon = ItemStack.EMPTY;
            }
            if (label == null) {
                label = Component.empty();
            }
        }
    }

    private record PoseParams(GuiGraphics graphics, int centerX, int centerY) {
        PoseStack.Pose pose() {
            return graphics.pose().last();
        }
    }
}
