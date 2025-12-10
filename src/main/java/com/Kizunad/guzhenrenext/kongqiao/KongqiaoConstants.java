package com.Kizunad.guzhenrenext.kongqiao;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * 空窍/攻击背包的基础常量。
 * <p>
 * 该类聚合 UI 布局、容量限制、允许的物品标签等配置，避免魔法数字散落各处。
 * </p>
 */
public final class KongqiaoConstants {

    /** 默认可用行数（2 行，对应设计稿）。 */
    public static final int DEFAULT_VISIBLE_ROWS = 2;

    /** 单行列数（同玩家背包 9 列）。 */
    public static final int COLUMNS = 9;

    /** 最大可拓展行数，预留到 9 行覆盖整页。 */
    public static final int MAX_ROWS = 9;

    /** 攻击背包固定 4 行。 */
    public static final int ATTACK_ROWS = 4;

    /** 攻击背包列数与玩家背包一致。 */
    public static final int ATTACK_COLUMNS = 9;

    /** 蛊虫喂食页默认 4 行。 */
    public static final int FEED_ROWS = 4;

    /** 蛊虫喂食列数与玩家背包一致。 */
    public static final int FEED_COLUMNS = 9;

    private static final List<ResourceLocation> TAG_IDS = List.of(
        ResourceLocation.parse("guzhenren:z1"),
        ResourceLocation.parse("guzhenren:z2"),
        ResourceLocation.parse("guzhenren:z3"),
        ResourceLocation.parse("guzhenren:z4"),
        ResourceLocation.parse("guzhenren:z5"),
        ResourceLocation.parse("guzhenren:items/guchong")
    );

    /** 空窍允许存放的物品标签集合。 */
    public static final Set<TagKey<Item>> ALLOWED_TAGS = TAG_IDS
        .stream()
        .map(id -> TagKey.create(Registries.ITEM, id))
        .collect(Collectors.toSet());

    private KongqiaoConstants() {}
}
