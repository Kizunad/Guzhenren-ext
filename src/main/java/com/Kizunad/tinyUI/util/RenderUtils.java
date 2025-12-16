package com.Kizunad.tinyUI.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

/**
 * GUI 侧的底层绘制工具。
 * <p>
 * TinyUI 的大多数控件通过 {@code GuiGraphics.fill/blit} 足够；但对于轮盘菜单这类需要扇形/圆环的 UI，
 * 需要直接使用 {@link Tesselator}/{@link BufferBuilder} 绘制三角形网格。
 * </p>
 * <p>
 * 设计目标：尽量少改动全局渲染状态，保证与其它 Mod 的 GUI 渲染共存。
 * </p>
 */
public final class RenderUtils {

    private static final float GUI_Z = 0.0F;
    private static final int CHANNEL_MASK = 0xFF;
    private static final int SHIFT_ALPHA = 24;
    private static final int SHIFT_RED = 16;
    private static final int SHIFT_GREEN = 8;

    private RenderUtils() {}

    /**
     * 绘制以圆心为起点的扇形（实心），使用 TRIANGLE_FAN。
     *
     * @param pose PoseStack 当前 pose（通常传 {@code graphics.pose().last()}）
     * @param centerX 圆心 X（像素）
     * @param centerY 圆心 Y（像素）
     * @param arc 扇形弧参数
     * @param argbColor 颜色（ARGB）
     */
    public static void drawSector(
        final PoseStack.Pose pose,
        final float centerX,
        final float centerY,
        final Arc arc,
        final int argbColor
    ) {
        if (arc.segments() <= 0) {
            return;
        }

        final int alpha = (argbColor >>> SHIFT_ALPHA) & CHANNEL_MASK;
        final int red = (argbColor >>> SHIFT_RED) & CHANNEL_MASK;
        final int green = (argbColor >>> SHIFT_GREEN) & CHANNEL_MASK;
        final int blue = argbColor & CHANNEL_MASK;

        beginGuiShape();
        final BufferBuilder builder = Tesselator.getInstance().begin(
            VertexFormat.Mode.TRIANGLE_FAN,
            DefaultVertexFormat.POSITION_COLOR
        );

        builder.addVertex(pose, centerX, centerY, GUI_Z)
            .setColor(red, green, blue, alpha);

        for (int i = 0; i <= arc.segments(); i++) {
            final float t = (float) i / (float) arc.segments();
            final float angle = Mth.lerp(t, arc.startAngleRad(), arc.endAngleRad());
            final float x = centerX + Mth.cos(angle) * arc.radius();
            final float y = centerY + Mth.sin(angle) * arc.radius();
            builder.addVertex(pose, x, y, GUI_Z).setColor(red, green, blue, alpha);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());
        endGuiShape();
    }

    /**
     * 绘制圆环扇区（带内外半径），使用 TRIANGLE_STRIP。
     * <p>
     * 该方法用于轮盘菜单的“带盲区”背景：鼠标位于内圆时不触发选择，外圈才是可选区域。
     * </p>
     *
     * @param pose PoseStack 当前 pose（通常传 {@code graphics.pose().last()}）
     * @param centerX 圆心 X（像素）
     * @param centerY 圆心 Y（像素）
     * @param arc 圆环扇区弧参数
     * @param argbColor 颜色（ARGB）
     */
    public static void drawRingSector(
        final PoseStack.Pose pose,
        final float centerX,
        final float centerY,
        final RingArc arc,
        final int argbColor
    ) {
        if (arc.segments() <= 0 || arc.innerRadius() < 0.0F || arc.outerRadius() <= arc.innerRadius()) {
            return;
        }

        final int alpha = (argbColor >>> SHIFT_ALPHA) & CHANNEL_MASK;
        final int red = (argbColor >>> SHIFT_RED) & CHANNEL_MASK;
        final int green = (argbColor >>> SHIFT_GREEN) & CHANNEL_MASK;
        final int blue = argbColor & CHANNEL_MASK;

        beginGuiShape();
        final BufferBuilder builder = Tesselator.getInstance().begin(
            VertexFormat.Mode.TRIANGLE_STRIP,
            DefaultVertexFormat.POSITION_COLOR
        );

        for (int i = 0; i <= arc.segments(); i++) {
            final float t = (float) i / (float) arc.segments();
            final float angle = Mth.lerp(t, arc.startAngleRad(), arc.endAngleRad());
            final float cos = Mth.cos(angle);
            final float sin = Mth.sin(angle);

            final float outerX = centerX + cos * arc.outerRadius();
            final float outerY = centerY + sin * arc.outerRadius();
            builder.addVertex(pose, outerX, outerY, GUI_Z).setColor(red, green, blue, alpha);

            final float innerX = centerX + cos * arc.innerRadius();
            final float innerY = centerY + sin * arc.innerRadius();
            builder.addVertex(pose, innerX, innerY, GUI_Z).setColor(red, green, blue, alpha);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());
        endGuiShape();
    }

    /**
     * 扇形弧参数。
     *
     * @param radius 半径（像素）
     * @param startAngleRad 起始角（弧度，0 表示朝右，逆时针为正）
     * @param endAngleRad 结束角（弧度）
     * @param segments 角度分段数（越大越圆滑，越小越省性能）
     */
    public record Arc(
        float radius,
        float startAngleRad,
        float endAngleRad,
        int segments
    ) {}

    /**
     * 圆环扇区弧参数。
     *
     * @param innerRadius 内半径（像素）
     * @param outerRadius 外半径（像素）
     * @param startAngleRad 起始角（弧度，0 表示朝右，逆时针为正）
     * @param endAngleRad 结束角（弧度）
     * @param segments 角度分段数（越大越圆滑，越小越省性能）
     */
    public record RingArc(
        float innerRadius,
        float outerRadius,
        float startAngleRad,
        float endAngleRad,
        int segments
    ) {}

    private static void beginGuiShape() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    private static void endGuiShape() {
        RenderSystem.disableBlend();
    }
}
