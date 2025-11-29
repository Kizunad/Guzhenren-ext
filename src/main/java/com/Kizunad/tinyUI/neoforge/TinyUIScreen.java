package com.Kizunad.tinyUI.neoforge;

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

    @Override
    protected void init() {
        super.init();
        root.setViewport(width, height);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY,
                       final float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        final UIRenderContext context =
                new GuiRenderContext(graphics, Minecraft.getInstance().font);
        root.render(context, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (inputRouter.mouseClick(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (inputRouter.mouseRelease(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button,
                                final double dragX, final double dragY) {
        if (inputRouter.mouseDrag(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double deltaX,
                                 final double deltaY) {
        if (inputRouter.mouseScroll(mouseX, mouseY, deltaY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
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
