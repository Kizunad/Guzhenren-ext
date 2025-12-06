package com.Kizunad.tinyUI.neoforge;

import com.Kizunad.tinyUI.core.ScaleConfig;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.input.FocusManager;
import com.Kizunad.tinyUI.input.HotkeyManager;
import com.Kizunad.tinyUI.input.InputRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * tinyUI 的 NeoForge Screen 基类，将 Minecraft 输入/渲染桥接到 tinyUI 树。
 * <p>
 * 支持设计分辨率缩放：通过 {@link UIRoot#setDesignResolution(int, int)} 设置后，
 * UI 会根据实际屏幕尺寸自动缩放。
 * </p>
 */
public class TinyUIScreen extends Screen {

    private final UIRoot root;
    private final FocusManager focusManager;
    private final HotkeyManager hotkeyManager;
    private final InputRouter inputRouter;

    public TinyUIScreen(final Component title, final UIRoot root) {
        super(title);
        this.root = root;
        this.focusManager = new FocusManager();
        this.hotkeyManager = new HotkeyManager();
        this.inputRouter = new InputRouter(root, focusManager, hotkeyManager);
    }

    /**
     * 获取 UI 根节点。
     *
     * @return UIRoot 实例
     */
    protected UIRoot getRoot() {
        return root;
    }

    @Override
    protected void init() {
        super.init();
        root.setViewport(width, height);
    }

    @Override
    public void render(
        final GuiGraphics graphics,
        final int mouseX,
        final int mouseY,
        final float partialTick
    ) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        final ScaleConfig scale = root.getScaleConfig();
        final double factor = scale.getScaleFactor();
        final boolean scaling = scale.isScalingEnabled() && factor != 1.0;

        // 转换鼠标坐标到设计分辨率空间
        final double scaledMouseX = scale.unscale(mouseX);
        final double scaledMouseY = scale.unscale(mouseY);

        if (scaling) {
            graphics.pose().pushPose();
            graphics.pose().scale((float) factor, (float) factor, 1.0f);
        }

        final UIRenderContext context = new GuiRenderContext(
            graphics,
            Minecraft.getInstance().font
        );
        root.render(context, scaledMouseX, scaledMouseY, partialTick);

        // 让子类在缩放矩阵内渲染额外内容
        renderScaledContent(
            graphics,
            (int) scaledMouseX,
            (int) scaledMouseY,
            partialTick
        );

        if (scaling) {
            graphics.pose().popPose();
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * 在缩放矩阵内渲染额外内容。
     * <p>
     * 子类可以覆写此方法来渲染需要与 UI 元素对齐的自定义内容。
     * 坐标使用设计分辨率（如 1920x1080）。
     * </p>
     *
     * @param graphics 图形上下文
     * @param mouseX 鼠标 X 坐标（设计分辨率空间）
     * @param mouseY 鼠标 Y 坐标（设计分辨率空间）
     * @param partialTick 部分 tick
     */
    protected void renderScaledContent(
        final GuiGraphics graphics,
        final int mouseX,
        final int mouseY,
        final float partialTick
    ) {
        // 默认空实现，子类可覆写
    }

    @Override
    public boolean mouseClicked(
        final double mouseX,
        final double mouseY,
        final int button
    ) {
        final ScaleConfig scale = root.getScaleConfig();
        final double sx = scale.unscale(mouseX);
        final double sy = scale.unscale(mouseY);
        if (inputRouter.mouseClick(sx, sy, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(
        final double mouseX,
        final double mouseY,
        final int button
    ) {
        final ScaleConfig scale = root.getScaleConfig();
        final double sx = scale.unscale(mouseX);
        final double sy = scale.unscale(mouseY);
        if (inputRouter.mouseRelease(sx, sy, button)) {
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
        final ScaleConfig scale = root.getScaleConfig();
        final double sx = scale.unscale(mouseX);
        final double sy = scale.unscale(mouseY);
        // 拖拽增量也需要按缩放比例转换
        final double sdx = scale.unscale(dragX);
        final double sdy = scale.unscale(dragY);
        if (inputRouter.mouseDrag(sx, sy, button, sdx, sdy)) {
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
        final ScaleConfig scale = root.getScaleConfig();
        final double sx = scale.unscale(mouseX);
        final double sy = scale.unscale(mouseY);
        if (inputRouter.mouseScroll(sx, sy, deltaY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(
        final int keyCode,
        final int scanCode,
        final int modifiers
    ) {
        if (inputRouter.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(final char codePoint, final int modifiers) {
        if (inputRouter.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
