package com.Kizunad.guzhenrenext.xianqiao.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 九天碎片物品。
 */
public class HeavenlyFragmentItem extends Item {

    /**
     * 一天对应的游戏刻数。
     */
    private static final long TICKS_PER_DAY = 24000L;

    /**
     * 天数偏移量（从第 1 天开始显示）。
     */
    private static final long DAY_OFFSET = 1L;

    public HeavenlyFragmentItem(Properties properties) {
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

        // 显示碎片 ID
        String fragmentId = stack.get(XianqiaoDataComponents.FRAGMENT_ID.get());
        if (fragmentId != null && !fragmentId.isEmpty()) {
            tooltipComponents.add(
                Component.literal("碎片: " + fragmentId)
                    .withStyle(ChatFormatting.GRAY)
            );
        }

        // 显示获取天数
        Long acquireTime = stack.get(XianqiaoDataComponents.ACQUIRE_TIME.get());
        if (acquireTime != null) {
            long day = acquireTime / TICKS_PER_DAY + DAY_OFFSET;
            tooltipComponents.add(
                Component.literal("获取时间: 第 " + day + " 天")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        // 用法说明
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(
            Component.literal("右键: 沿朝向放置")
                .withStyle(ChatFormatting.YELLOW)
        );
    }
}
