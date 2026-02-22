package com.Kizunad.guzhenrenext.xianqiao.item;

import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 储物蛊物品。
 * <p>
 * Task1 仅提供最小 use 行为与 long 语义数据读写入口，
 * UI/菜单打开逻辑在后续任务实现。
 * </p>
 */
public class StorageGuItem extends Item {

    private static final int SINGLE_STACK_COUNT = 1;
    private static final String STORED_TOOLTIP_PREFIX = "Stored: ";
    private static final String STORED_TOOLTIP_SUFFIX = " items";

    public StorageGuItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!isWritableStorageGuStack(stack)) {
            return InteractionResultHolder.fail(stack);
        }
        
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                new net.minecraft.world.SimpleMenuProvider(
                    (id, inventory, p) -> new StorageGuMenu(id, inventory, stack),
                    net.minecraft.network.chat.Component.translatable(this.getDescriptionId())
                ),
                (buf) -> buf.writeEnum(hand) // 写入 hand 以便客户端知道是哪个手的物品
            );
        }
        
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        List<Component> tooltipComponents,
        TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        long totalCount = StorageGuData.fromItemStack(stack).getTotalCountSaturated();
        tooltipComponents.add(
            Component.literal(STORED_TOOLTIP_PREFIX + totalCount + STORED_TOOLTIP_SUFFIX)
                .withStyle(ChatFormatting.GRAY)
        );
    }

    /**
     * 获取储物蛊的 long 语义处理器。
     *
     * @param stack 目标堆栈
     * @return 处理器实例
     */
    public StorageGuData.StorageGuHandler getStorageHandler(ItemStack stack) {
        return new StackBackedStorageGuHandler(stack);
    }

    private static boolean isWritableStorageGuStack(ItemStack stack) {
        return stack != null
            && !stack.isEmpty()
            && stack.getItem() instanceof StorageGuItem
            && stack.getCount() == SINGLE_STACK_COUNT;
    }

    private static final class StackBackedStorageGuHandler implements StorageGuData.StorageGuHandler {

        private final ItemStack stack;

        private StackBackedStorageGuHandler(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public long getCount(ResourceLocation itemId) {
            return StorageGuData.fromItemStack(stack).getCount(itemId);
        }

        @Override
        public long insert(ResourceLocation itemId, long amount) {
            if (!isWritableStorageGuStack(stack)) {
                return 0L;
            }
            StorageGuData data = StorageGuData.fromItemStack(stack);
            long inserted = data.add(itemId, amount);
            if (inserted > 0L) {
                data.writeToItemStack(stack);
            }
            return inserted;
        }

        @Override
        public long extract(ResourceLocation itemId, long amount) {
            if (!isWritableStorageGuStack(stack)) {
                return 0L;
            }
            StorageGuData data = StorageGuData.fromItemStack(stack);
            long extracted = data.remove(itemId, amount);
            if (extracted > 0L) {
                data.writeToItemStack(stack);
            }
            return extracted;
        }

        @Override
        public Map<ResourceLocation, Long> snapshot() {
            return StorageGuData.fromItemStack(stack).snapshot();
        }
    }
}
