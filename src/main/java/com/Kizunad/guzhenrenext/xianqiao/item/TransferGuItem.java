package com.Kizunad.guzhenrenext.xianqiao.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 转运蛊物品。
 * <p>
 * 当前任务仅提供基础物品定义与提示文本，
 * 不包含任何无线转运执行逻辑（后续任务再实现）。
 * </p>
 */
public class TransferGuItem extends Item {

    public TransferGuItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        List<Component> tooltipComponents,
        TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        tooltipComponents.add(
            Component.literal("无线转运：用于在后续系统中进行远距离物资转运。")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
