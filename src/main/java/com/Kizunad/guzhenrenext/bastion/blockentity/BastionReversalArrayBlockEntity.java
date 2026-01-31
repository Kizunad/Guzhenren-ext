package com.Kizunad.guzhenrenext.bastion.blockentity;

import com.Kizunad.guzhenrenext.bastion.BastionBlockEntities;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;

/**
 * 逆转阵法 BlockEntity。
 * <p>
 * 用途：存储燃料（元石）与献祭材料（蛊材），并提供运行时状态（燃料剩余）。
 * </p>
 */
public class BastionReversalArrayBlockEntity extends BlockEntity {

    private static final class Constants {
        /** 每颗燃料提供 10 秒燃料。 */
        static final int FUEL_TICKS_PER_YUANSHI = 200;

        /** 单次运行周期的燃料消耗量（以 tick 计，默认与阵法运行间隔一致）。 */
        static final int DEFAULT_FUEL_CONSUME_TICKS = 20;

        /** 允许作为燃料的物品 ID（泉眼/犬眼）。 */
        static final String FUEL_ITEM_ID = "guzhenren:quanyan";

        /** 允许作为燃料的物品路径关键字（用于兼容百兽/千兽/万兽等变体）。 */
        static final String FUEL_ITEM_PATH_TOKEN = "quanyan";

        /** 掉落物生成偏移（方块中心）。 */
        static final double DROP_CENTER_OFFSET = 0.5;

        /** 掉落物生成高度偏移（方块上方）。 */
        static final double DROP_Y_OFFSET = 1.1;

        private Constants() {
        }
    }

    /**
     * 0: 燃料槽（元石）
     * 1: 献祭槽（蛊材）
     */
    private final Container container = new SimpleContainer(2);

    /** 燃料剩余 tick。 */
    private int fuelTicks;

    /**
     * 防止被重复 scheduleTick 导致的多次执行：记录上次处理的“周期序号”。
     * <p>
     * 说明：该字段不需要持久化，目的是防止同一周期内多次运行造成“狂吐产物”。</p>
     */
    private long lastProcessedStep;

    public BastionReversalArrayBlockEntity(BlockPos pos, BlockState state) {
        super(BastionBlockEntities.BASTION_REVERSAL_ARRAY.get(), pos, state);
    }

    public Container getContainer() {
        return container;
    }

    public int getFuelTicks() {
        return fuelTicks;
    }

    /**
     * 判断当前周期是否应执行一次逻辑。
     *
     * @param gameTime     当前游戏时间
     * @param intervalTicks 周期间隔（通常为 20）
     * @return true 表示本周期尚未执行
     */
    public boolean shouldProcess(long gameTime, int intervalTicks) {
        int safeInterval = Math.max(Constants.DEFAULT_FUEL_CONSUME_TICKS, intervalTicks);
        long step = gameTime / safeInterval;
        return step != lastProcessedStep;
    }

    public void markProcessed(long gameTime, int intervalTicks) {
        int safeInterval = Math.max(Constants.DEFAULT_FUEL_CONSUME_TICKS, intervalTicks);
        lastProcessedStep = gameTime / safeInterval;
    }

    /**
     * 是否具备可用燃料（燃料 tick > 0 或燃料槽中存在元石）。
     */
    public boolean hasAnyFuel() {
        if (fuelTicks > 0) {
            return true;
        }
        ItemStack fuel = container.getItem(0);
        return isYuanshiFuel(fuel);
    }

    public void addFuelTicks(int ticks) {
        fuelTicks += Math.max(0, ticks);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("fuelTicks", fuelTicks);
        // 简化：仅保存两个槽位
        CompoundTag inv = new CompoundTag();
        inv.put("fuel", container.getItem(0).saveOptional(provider));
        inv.put("sacrifice", container.getItem(1).saveOptional(provider));
        tag.put("inv", inv);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        fuelTicks = tag.getInt("fuelTicks");
        if (tag.contains("inv")) {
            CompoundTag inv = tag.getCompound("inv");
            container.setItem(0, ItemStack.parseOptional(provider, inv.getCompound("fuel")));
            container.setItem(1, ItemStack.parseOptional(provider, inv.getCompound("sacrifice")));
        }
    }

    // ===== 奖励池辅助：根据 Dao + 献祭强度决定发放的蛊虫转数 =====

    public ItemStack rollReward(BastionDao dao, RandomSource random) {
        TagKey<Item> daoTag = resolveDaoGuTag(dao);
        TagKey<Item> tierTag = resolveTierGuTag();

        // 优先尝试：道途池 ∩ 转数池（通过“先抽后判”近似实现）
        Item item = pickRandomItemPreferTierAndDao(daoTag, tierTag, random);
        if (item == null) {
            item = pickRandomItem(tag("guzhenren:guchong"), random);
        }
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    private TagKey<Item> resolveTierGuTag() {
        // 献祭强度：Tag 优先（jiage_XXX），命名规则兜底（gu_cai_/gucai）
        ItemStack sacrifice = container.getItem(1);
        if (sacrifice.isEmpty()) {
            // 无献祭时只给 z1
            return tag("guzhenren:z1");
        }

        // A) Tag：按价格档位决定转数池（可调）
        if (sacrifice.is(tag("guzhenren:jiage_1000"))
            || sacrifice.is(tag("guzhenren:jiage_1500"))
            || sacrifice.is(tag("guzhenren:jiage_5000"))) {
            return tag("guzhenren:z4");
        }
        if (sacrifice.is(tag("guzhenren:jiage_600"))
            || sacrifice.is(tag("guzhenren:jiage_800"))
            || sacrifice.is(tag("guzhenren:jiage_1000"))) {
            return tag("guzhenren:z3");
        }
        if (sacrifice.is(tag("guzhenren:jiage_200"))
            || sacrifice.is(tag("guzhenren:jiage_400"))
            || sacrifice.is(tag("guzhenren:jiage_600"))) {
            return tag("guzhenren:z2");
        }

        // B) 命名规则：只要像蛊材，就给 z2
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(sacrifice.getItem());
        if (key != null) {
            String path = key.getPath();
            if (path.contains("gu_cai_") || path.contains("gucai")) {
                return tag("guzhenren:z2");
            }
        }

        return tag("guzhenren:z1");
    }

    private static Item pickRandomItemPreferTierAndDao(
        TagKey<Item> daoTag,
        TagKey<Item> tierTag,
        RandomSource random
    ) {
        java.util.List<Item> daoItems = BuiltInRegistries.ITEM.getTag(daoTag)
            .map(holderSet -> holderSet.stream().map(Holder::value).filter(Objects::nonNull).toList())
            .orElse(java.util.List.of());
        if (daoItems.isEmpty()) {
            return null;
        }

        // tierTag 为空就直接从 daoTag 抽
        java.util.Set<Item> tierItems = BuiltInRegistries.ITEM.getTag(tierTag)
            .map(holderSet -> holderSet.stream().map(Holder::value).filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet()))
            .orElse(java.util.Set.of());

        java.util.List<Item> filtered = tierItems.isEmpty()
            ? daoItems
            : daoItems.stream().filter(tierItems::contains).toList();
        if (filtered.isEmpty()) {
            // 兜底：退化为 dao 抽
            filtered = daoItems;
        }

        return filtered.get(random.nextInt(filtered.size()));
    }

    private static TagKey<Item> resolveDaoGuTag(BastionDao dao) {
        String name = dao.getSerializedName();
        TagKey<Item> primary = tag("guzhenren:" + name);
        if (!isTagEmpty(primary)) {
            return primary;
        }
        TagKey<Item> fallback = tag("guzhenren:" + name.replace("_", ""));
        if (!isTagEmpty(fallback)) {
            return fallback;
        }
        return tag("guzhenren:guchong");
    }

    private static boolean isTagEmpty(TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.getTag(tag)
            .map(holderSet -> holderSet.stream().findAny().isEmpty())
            .orElse(true);
    }

    private static Item pickRandomItem(TagKey<Item> tag, RandomSource random) {
        java.util.List<Item> items = BuiltInRegistries.ITEM.getTag(tag)
            .map(holderSet -> holderSet.stream().map(Holder::value).filter(Objects::nonNull).toList())
            .orElse(java.util.List.of());
        if (items.isEmpty()) {
            return null;
        }
        return items.get(random.nextInt(items.size()));
    }

    private static TagKey<Item> tag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
    }

    // ===== 运行逻辑（由 Block 的 tick 调用） =====

    public boolean tryConsumeFuel(ServerLevel level, ServerPlayer operator, int consumeTicks) {
        int consume = Math.max(0, consumeTicks);

        if (fuelTicks <= 0) {
            // fuelTicks 不足时，从燃料槽消耗 1 个燃料
            ItemStack fuel = container.getItem(0);
            if (!isYuanshiFuel(fuel)) {
                return false;
            }

            fuel.shrink(1);
            // 每颗燃料提供 10 秒燃料
            fuelTicks = Constants.FUEL_TICKS_PER_YUANSHI;
            setChanged();

            if (operator != null) {
                operator.sendSystemMessage(Component.literal("§7逆转阵法消耗 1 个泉眼"));
            }
        }

        if (consume > 0) {
            fuelTicks = Math.max(0, fuelTicks - consume);
            setChanged();
        }
        return true;
    }

    public boolean hasSacrifice() {
        return !container.getItem(1).isEmpty();
    }

    public boolean isValidSacrifice(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // A) Tag 优先：常见蛊材命名（不保证存在，失败则走 B）
        TagKey<Item> guCaiTag = tag("guzhenren:gucai");
        if (!isTagEmpty(guCaiTag) && stack.is(guCaiTag)) {
            return true;
        }

        // B) 命名规则兜底：id 包含 gu_cai_ 或 gucai
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            return false;
        }
        String path = key.getPath();
        return path.contains("gu_cai_") || path.contains("gucai");
    }

    public void setSacrifice(ItemStack stack) {
        container.setItem(1, stack.copyWithCount(1));
        setChanged();
    }

    /**
     * 向燃料槽添加燃料（泉眼）。
     */
    public boolean tryInsertFuel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!isYuanshiFuel(stack)) {
            return false;
        }

        ItemStack existing = container.getItem(0);
        if (existing.isEmpty()) {
            container.setItem(0, stack.copyWithCount(1));
            stack.shrink(1);
            setChanged();
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(existing, stack)) {
            return false;
        }
        if (existing.getCount() >= existing.getMaxStackSize()) {
            return false;
        }
        existing.grow(1);
        stack.shrink(1);
        container.setItem(0, existing);
        setChanged();
        return true;
    }

    /**
     * 燃料识别。
     * <p>
     * 注意：这里使用“物品 ID”匹配，而不是 Tag。
     * 因为上游模组的 tag 文件名为 yuanshi，但内容并不一定是我们期望的燃料物品。</p>
     */
    private static boolean isYuanshiFuel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) {
            return false;
        }
        String id = key.toString();

        // 精确匹配：默认燃料
        if (Constants.FUEL_ITEM_ID.equals(id)) {
            return true;
        }

        // 兼容变体：百兽/千兽/万兽/兽皇等“*quanyan”物品
        return "guzhenren".equals(key.getNamespace())
            && key.getPath() != null
            && key.getPath().contains(Constants.FUEL_ITEM_PATH_TOKEN);
    }

    public void dropReward(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level,
            pos.getX() + Constants.DROP_CENTER_OFFSET,
            pos.getY() + Constants.DROP_Y_OFFSET,
            pos.getZ() + Constants.DROP_CENTER_OFFSET,
            stack
        );
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }
}
