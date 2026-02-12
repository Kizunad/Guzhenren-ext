package com.Kizunad.guzhenrenext.xianqiao.item;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.service.FragmentPlacementService;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 九天碎片物品。
 */
public class HeavenlyFragmentItem extends Item {

    /** 仙窍维度键。 */
    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    /** 成功放置时消耗物品数量。 */
    private static final int FRAGMENT_CONSUME_COUNT = 1;

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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!serverLevel.dimension().equals(APERTURE_DIMENSION)) {
            player.sendSystemMessage(Component.literal("九天碎片只能在仙窍维度中使用。"));
            return InteractionResultHolder.fail(stack);
        }

        UUID owner = player.getUUID();
        ApertureWorldData worldData = ApertureWorldData.get(serverLevel);
        ApertureInfo apertureInfo = worldData.getAperture(owner);
        if (apertureInfo == null) {
            player.sendSystemMessage(Component.literal("未找到你的仙窍信息，无法放置九天碎片。"));
            return InteractionResultHolder.fail(stack);
        }
        if (!isInsideAperture(player.blockPosition(), apertureInfo)) {
            player.sendSystemMessage(Component.literal("你必须站在自己的仙窍范围内才能放置九天碎片。"));
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            Direction direction = player.getDirection();
            int placementDistance = apertureInfo.currentRadius() + FragmentPlacementService.EXTENSION_DISTANCE;
            BlockPos targetPos = apertureInfo.center().offset(
                direction.getStepX() * placementDistance,
                0,
                direction.getStepZ() * placementDistance
            );
            player.displayClientMessage(
                Component.literal("§6当前朝向：" + getDirectionName(direction)
                    + " | 扩展至 (" + targetPos.getX() + ", " + targetPos.getZ()
                    + ") | 松开Shift右键确认放置"),
                true
            );
            return InteractionResultHolder.success(stack);
        }

        boolean placed = FragmentPlacementService.placeFragment(serverLevel, player, apertureInfo);
        if (!placed) {
            player.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.sendSystemMessage(Component.literal("九天碎片放置失败，请检查目标区域后重试。"));
            return InteractionResultHolder.fail(stack);
        }
        player.playNotifySound(SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.shrink(FRAGMENT_CONSUME_COUNT);
        return InteractionResultHolder.success(stack);
    }

    /**
     * 判断给定位置是否位于玩家仙窍半径内（XZ 平面）。
     *
     * @param playerPos 玩家方块坐标
     * @param info 仙窍信息
     * @return 在范围内返回 true，否则返回 false
     */
    private static boolean isInsideAperture(BlockPos playerPos, ApertureInfo info) {
        BlockPos center = info.center();
        long deltaX = (long) playerPos.getX() - center.getX();
        long deltaZ = (long) playerPos.getZ() - center.getZ();
        long distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        long radius = info.currentRadius();
        return distanceSquared <= radius * radius;
    }

    private static String getDirectionName(Direction direction) {
        return switch (direction) {
            case NORTH -> "北方";
            case SOUTH -> "南方";
            case EAST -> "东方";
            case WEST -> "西方";
            default -> direction.getName();
        };
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
