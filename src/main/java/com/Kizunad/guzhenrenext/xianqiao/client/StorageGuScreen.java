package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuData;
import com.Kizunad.guzhenrenext.xianqiao.item.StorageGuMenu;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 储物蛊客户端界面。
 * <p>
 * 展示储物蛊内的物品列表。当前版本为最小实现，仅展示部分条目。
 * </p>
 */
public class StorageGuScreen extends AbstractContainerScreen<StorageGuMenu> {

    // 使用原版通用纹理作为背景
    private static final ResourceLocation TEXTURE =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    // 列表显示起始位置
    private static final int LIST_X = 8;
    private static final int LIST_Y = 18;
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_LINES = 6;
    private static final int TEXT_COLOR = 0x404040;
    private static final int BG_HEIGHT = 166;
    private static final int LABEL_OFFSET = 94;
    private static final int FOOTER_Y = 80;

    public StorageGuScreen(StorageGuMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = BG_HEIGHT; // 适配 generic_54 纹理高度 (虽然它有 6 行槽位，我们只借用背景)
        this.inventoryLabelY = this.imageHeight - LABEL_OFFSET;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        // 获取数据
        StorageGuData data = StorageGuData.fromItemStack(this.menu.getStack());
        Map<ResourceLocation, Long> entries = data.snapshot();

        int lineCount = 0;
        int yPos = LIST_Y;

        if (entries.isEmpty()) {
            guiGraphics.drawString(this.font, "（空）", LIST_X, yPos, TEXT_COLOR, false);
            return;
        }

        // 简单遍历展示前几项
        for (Map.Entry<ResourceLocation, Long> entry : entries.entrySet()) {
            if (lineCount >= MAX_LINES) {
                guiGraphics.drawString(this.font, "...", LIST_X, yPos, TEXT_COLOR, false);
                break;
            }

            String text = entry.getKey().getPath() + " x" + entry.getValue();
            guiGraphics.drawString(this.font, text, LIST_X, yPos, TEXT_COLOR, false);

            yPos += LINE_HEIGHT;
            lineCount++;
        }
        
        // 显示总条目数
        String footer = "总条目: " + entries.size();
        guiGraphics.drawString(this.font, footer, LIST_X, FOOTER_Y, TEXT_COLOR, false);
    }
}
