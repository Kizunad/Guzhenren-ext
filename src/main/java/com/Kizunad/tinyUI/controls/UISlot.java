package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

/**
 * 代表 Minecraft 原生容器中的一个 Slot (物品槽)。
 * <p>
 * 这个控件本身不渲染物品（由 Minecraft 原生逻辑渲染），
 * 它的主要作用是参与 TinyUI 的布局计算，并将计算出的位置同步给底层的 Slot 对象。
 * <p>
 * 如果此控件位于滚动容器中，它会自动处理 Slot 的可见性（移出屏幕）。
 */
public class UISlot extends InteractiveElement {

    private static final int DEFAULT_SLOT_SIZE = 18;
    private static final int SLOT_BG_PADDING = 0;

    private final int slotIndex;
    private final Theme theme;
    private boolean drawBackground = true;

    /**
     * @param slotIndex 对应 ContainerMenu 中的 Slot 索引
     * @param theme 主题
     */
    public UISlot(int slotIndex, Theme theme) {
        this.slotIndex = slotIndex;
        this.theme = Objects.requireNonNull(theme);
        setFrame(0, 0, DEFAULT_SLOT_SIZE, DEFAULT_SLOT_SIZE);
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setDrawBackground(boolean draw) {
        this.drawBackground = draw;
    }

    @Override
    protected void onRender(
        UIRenderContext context,
        double mouseX,
        double mouseY,
        float partialTicks
    ) {
        if (drawBackground) {
            // 绘制与 Slot 同尺寸的背景，确保与原版高亮对齐
            final int bgX = getAbsoluteX() + SLOT_BG_PADDING;
            final int bgY = getAbsoluteY() + SLOT_BG_PADDING;
            final int bgW = getWidth() - SLOT_BG_PADDING * 2;
            final int bgH = getHeight() - SLOT_BG_PADDING * 2;
            context.drawRect(
                bgX,
                bgY,
                bgW,
                bgH,
                theme.getFieldBackgroundColor()
            );
            // 边框
            int borderColor = theme.getBorderColor();
            context.drawRect(bgX, bgY, bgW, 1, borderColor);
            context.drawRect(bgX, bgY + bgH - 1, bgW, 1, borderColor);
            context.drawRect(bgX, bgY, 1, bgH, borderColor);
            context.drawRect(bgX + bgW - 1, bgY, 1, bgH, borderColor);
        }

        // 注意：我们不在这里绘制物品，物品由 ContainerScreen 统一绘制
    }
}
