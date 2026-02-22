package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 集群 NPC 客户端交互界面。
 * <p>
 * 提供简单的管理界面：
 * 1. 储物蛊槽位：放置储物蛊接收产出。
 * 2. 状态指示：显示当前积压的产出量，以及“存储已满”警告。
 * </p>
 */
public class ClusterNpcScreen extends AbstractContainerScreen<ClusterNpcMenu> {

    // 使用原版通用纹理作为背景
    private static final ResourceLocation TEXTURE =
        ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    
    private static final int BG_HEIGHT = 166;
    private static final int TEXT_COLOR = 0x404040;
    private static final int WARNING_COLOR = 0xFF5555;
    
    private static final int INFO_X = 8;
    private static final int INFO_Y = 20;
    private static final int INFO_Y_OFFSET = 12;
    private static final int LABEL_OFFSET = 94;
    
    public ClusterNpcScreen(ClusterNpcMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = BG_HEIGHT;
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

        long pending = this.menu.getPendingOutput();
        
        // 显示待提取产出
        String pendingText = "待提取: " + pending;
        guiGraphics.drawString(this.font, pendingText, INFO_X, INFO_Y, TEXT_COLOR, false);
        
        // 存储已满提示
        // 逻辑：如果 pending > 0 且 储物蛊槽位为空 或 无法放入更多，则视为拥堵
        // 简化逻辑：只要 pending > 0 就提示需要储物蛊
        if (pending > 0) {
            String warning = "请放入储物蛊以接收产出";
            if (this.menu.getSlot(0).hasItem()) {
                 warning = "正在传输..."; // 或 "存储已满" (需要更复杂的协议判断是否真的满了)
                 // 简单判定：如果有物品但 pending 还在增加或不减少，可能是满了
                 // 暂时只显示传输中，因为无法立即知道对方满了
            }
            guiGraphics.drawString(this.font, warning, INFO_X, INFO_Y + INFO_Y_OFFSET, WARNING_COLOR, false);
        }
    }
}
