package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 友方基地产出服务：定期向附近容器产出蛊真人模组物品。
 * <p>
 * 设计目标：
 * <ul>
 *     <li>仅对已被玩家接管的基地生效，避免敌对基地白嫖。</li>
 *     <li>每 1200 tick 触发一次（约 1 分钟）。</li>
 *     <li>优先把产物放入核心附近 3 格内的容器，若找不到则掉落在核心上方。</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionFriendlyProductionService {

    private BastionFriendlyProductionService() {
        // 工具类
    }

    /** 非配置化常量。 */
    private static final class Constants {
        /** 触发间隔：1200 tick ≈ 60 秒。 */
        static final long PRODUCTION_INTERVAL_TICKS = 1200L;
        /** 容器搜索半径（曼哈顿/切比雪夫均可，此处使用立方体扫描）。 */
        static final int CONTAINER_SEARCH_RADIUS = 3;
        /** 物品掉落高度偏移（核心上方 1 格）。 */
        static final int DROP_HEIGHT_OFFSET = 1;
        /** 每次产出最少数量。 */
        static final int MIN_COUNT = 1;
        /** 每次产出最大数量。 */
        static final int MAX_COUNT = 3;
        /** 低阶转数阈值（1-3 转）。 */
        static final int LOW_TIER_MAX = 3;
        /** 中阶转数阈值（4-6 转）。 */
        static final int MID_TIER_MAX = 6;
        /** 高阶转数阈值（7-9 转）。 */
        static final int HIGH_TIER_MAX = 9;
        /** 掉落中心偏移（X/Z 方向 0.5 居中）。 */
        static final double DROP_CENTER_OFFSET = 0.5d;

        private Constants() {
        }
    }

    /**
     * 蛊真人物品 ID 映射。
     * 按道途和转数分类，ID 格式为 "guzhenren:item_id"。
     */
    private static final class GuzhenrenItems {
        // 智道物品 (念头、智壮蛊、恶念蛊等)
        static final String ZHI_LOW = "nian_tou";            // 念头（低转 1-3）
        static final String ZHI_MID = "zhi_zhuang_gu";       // 智壮蛊（中转 4-6）
        static final String ZHI_HIGH = "e_nian_gu";          // 恶念蛊（高转 7-9）

        // 魂道物品 (销魂蛊、大魂蛊、魂飞蛊等)
        static final String HUN_LOW = "xiao_hun_gu";         // 销魂蛊（低转 1-3）
        static final String HUN_MID = "dahungu";             // 大魂蛊（中转 4-6）
        static final String HUN_HIGH = "hunfeigu";           // 魂飞蛊（高转 7-9）

        // 木道物品 (草药类)
        static final String MU_LOW = "zhi_xin_cao";          // 止心草（低转 1-3）
        static final String MU_MID = "jin_zan_cao";          // 金盏草（中转 4-6）
        static final String MU_HIGH = "xueliancao";          // 雪莲草（高转 7-9）

        // 力道物品 (力量类蛊虫)
        static final String LI_LOW = "jing_li_gu";           // 精力蛊（低转 1-3）
        static final String LI_MID = "xiong_li_gu";          // 雄力蛊（中转 4-6）
        static final String LI_HIGH = "man_li_tian_niu_gu"; // 蛮力天牛蛊（高转 7-9）

        // 通用/默认物品
        static final String DEFAULT = "gucaiyuanshi";        // 蛊材原石

        // 模组 ID
        static final String MOD_ID = "guzhenren";

        private GuzhenrenItems() {
        }
    }

    /**
     * 服务端维度刻事件：按固定周期对所有基地执行产出。
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime % Constants.PRODUCTION_INTERVAL_TICKS != 0) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        java.util.Collection<BastionData> bastions = savedData.getAllBastions();
        if (bastions.isEmpty()) {
            return;
        }

        for (BastionData bastion : bastions) {
            if (bastion == null || bastion.state() != BastionState.ACTIVE) {
                continue;
            }
            if (!bastion.isCaptured()) {
                continue;
            }
            tryProduce(level, bastion, gameTime);
        }
    }

    /**
     * 为单个友方基地执行产出。
     *
     * @param level    服务端世界
     * @param bastion  基地数据（已确保 ACTIVE + captured）
     * @param gameTime 当前游戏时间，用于随机种子
     */
    private static void tryProduce(ServerLevel level, BastionData bastion, long gameTime) {
        RandomSource random = RandomSource.create(bastion.id().hashCode() ^ gameTime);
        ItemStack stack = chooseRewardStack(bastion.tier(), bastion.primaryDao(), random);
        if (stack.isEmpty()) {
            return;
        }

        ItemStack remaining = tryInsertNearby(level, bastion.corePos(), stack);
        if (!remaining.isEmpty()) {
            dropAtCore(level, bastion.corePos(), remaining);
        }
    }

    /**
     * 通过 ResourceLocation 获取蛊真人模组物品。
     * 若物品不存在则返回空气物品。
     *
     * @param itemId 物品 ID（不含模组前缀）
     * @return 对应的 Item 实例
     */
    private static Item getGuzhenrenItem(String itemId) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
            GuzhenrenItems.MOD_ID, itemId);
        return BuiltInRegistries.ITEM.get(location);
    }

    /**
     * 按道途 + 转数选择产物（蛊真人模组物品）：
     * <ul>
     *     <li>1-3 转：低阶蛊虫/材料。</li>
     *     <li>4-6 转：中阶蛊虫/材料。</li>
     *     <li>7-9 转：高阶蛊虫/材料。</li>
     * </ul>
     * 产物映射：智道=念头系，魂道=魂蛊系，木道=草药系，力道=力蛊系。
     * 数量在 1-3 之间随机，保持 MVP 资源节奏。
     */
    private static ItemStack chooseRewardStack(int tier, BastionDao dao, RandomSource random) {
        int amount = random.nextInt(Constants.MAX_COUNT - Constants.MIN_COUNT + 1)
            + Constants.MIN_COUNT;
        boolean isLowTier = tier <= Constants.LOW_TIER_MAX;
        boolean isMidTier = tier <= Constants.MID_TIER_MAX;

        String itemId;
        switch (dao) {
            case ZHI_DAO -> {
                if (isLowTier) {
                    itemId = GuzhenrenItems.ZHI_LOW;
                } else if (isMidTier) {
                    itemId = GuzhenrenItems.ZHI_MID;
                } else {
                    itemId = GuzhenrenItems.ZHI_HIGH;
                }
            }
            case HUN_DAO -> {
                if (isLowTier) {
                    itemId = GuzhenrenItems.HUN_LOW;
                } else if (isMidTier) {
                    itemId = GuzhenrenItems.HUN_MID;
                } else {
                    itemId = GuzhenrenItems.HUN_HIGH;
                }
            }
            case MU_DAO -> {
                if (isLowTier) {
                    itemId = GuzhenrenItems.MU_LOW;
                } else if (isMidTier) {
                    itemId = GuzhenrenItems.MU_MID;
                } else {
                    itemId = GuzhenrenItems.MU_HIGH;
                }
            }
            case LI_DAO -> {
                if (isLowTier) {
                    itemId = GuzhenrenItems.LI_LOW;
                } else if (isMidTier) {
                    itemId = GuzhenrenItems.LI_MID;
                } else {
                    itemId = GuzhenrenItems.LI_HIGH;
                }
            }
            default -> itemId = GuzhenrenItems.DEFAULT;
        }

        Item item = getGuzhenrenItem(itemId);
        return new ItemStack(item, amount);
    }

    /**
     * 尝试把物品放入核心附近的容器。
     *
     * @param level   服务端世界
     * @param corePos 核心位置
     * @param stack   待投放物品
     * @return 未能放入的剩余物品（为空表示全部放入）
     */
    private static ItemStack tryInsertNearby(ServerLevel level, BlockPos corePos, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        BlockPos start = corePos.offset(-Constants.CONTAINER_SEARCH_RADIUS,
            -Constants.CONTAINER_SEARCH_RADIUS, -Constants.CONTAINER_SEARCH_RADIUS);
        BlockPos end = corePos.offset(Constants.CONTAINER_SEARCH_RADIUS,
            Constants.CONTAINER_SEARCH_RADIUS, Constants.CONTAINER_SEARCH_RADIUS);

        ItemStack remaining = stack.copy();
        for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
            if (!(level.getBlockEntity(pos) instanceof Container container)) {
                continue;
            }
            remaining = insertIntoContainer(container, remaining);
            if (remaining.isEmpty()) {
                break;
            }
        }
        return remaining;
    }

    /**
     * 把物品尝试插入容器，返回未放入的部分。
     * 简化版的 Hopper 逻辑：优先合并同类物品，其次放入空槽。
     */
    private static ItemStack insertIntoContainer(Container container, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stack.copy();
        int slots = container.getContainerSize();

        // 先尝试合并已有同类物品。
        for (int i = 0; i < slots && !remaining.isEmpty(); i++) {
            ItemStack slotStack = container.getItem(i);
            if (slotStack.isEmpty()) {
                continue;
            }
            if (!ItemStack.isSameItemSameComponents(slotStack, remaining)) {
                continue;
            }
            if (!container.canPlaceItem(i, remaining)) {
                continue;
            }
            int limit = Math.min(container.getMaxStackSize(), slotStack.getMaxStackSize());
            int canMove = Math.min(remaining.getCount(), limit - slotStack.getCount());
            if (canMove <= 0) {
                continue;
            }
            slotStack.grow(canMove);
            remaining.shrink(canMove);
            container.setChanged();
        }

        // 再尝试放入空槽。
        for (int i = 0; i < slots && !remaining.isEmpty(); i++) {
            ItemStack slotStack = container.getItem(i);
            if (!slotStack.isEmpty()) {
                continue;
            }
            if (!container.canPlaceItem(i, remaining)) {
                continue;
            }
            int limit = Math.min(container.getMaxStackSize(), remaining.getMaxStackSize());
            int toInsert = Math.min(remaining.getCount(), limit);
            ItemStack placed = remaining.copy();
            placed.setCount(toInsert);
            container.setItem(i, placed);
            remaining.shrink(toInsert);
            container.setChanged();
        }

        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    /**
     * 若容器放不下，则在核心上方掉落物品。
     */
    private static void dropAtCore(ServerLevel level, BlockPos corePos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        double x = corePos.getX() + Constants.DROP_CENTER_OFFSET;
        double y = corePos.getY() + Constants.DROP_HEIGHT_OFFSET;
        double z = corePos.getZ() + Constants.DROP_CENTER_OFFSET;
        ItemEntity entity = new ItemEntity(level, x, y, z, stack.copy());
        level.addFreshEntity(entity);
    }
}
