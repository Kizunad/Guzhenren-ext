package com.Kizunad.guzhenrenext.customNPCImpl.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 蛊虫识别工具类。
 * <p>
 * 提供识别蛊虫类型（攻击、辅助等）的通用方法，主要基于物品标签进行判定。
 * </p>
 */
public final class GuInsectUtil {

    private GuInsectUtil() {}

    // 定义已知的攻击类蛊虫标签集合
    // 根据提供的文件列表，这些标签可能包含了不同转数的攻击/战斗类蛊虫
    private static final Set<TagKey<Item>> ATTACK_GU_TAGS = Stream.of(
        "guzhenren:gushiguchong1",
        "guzhenren:gushiguchong1_1",
        "guzhenren:gushiguchong2",
        "guzhenren:gushiguchong2_1",
        "guzhenren:gushiguchong3",
        "guzhenren:gushiguchong3_1",
        "guzhenren:gushiguchong4",
        "guzhenren:gushiguchong4_1",
        "guzhenren:gushiguchong5",
        "guzhenren:gushiguchong5_1"
    ).map(id -> TagKey.create(Registries.ITEM, ResourceLocation.parse(id)))
     .collect(Collectors.toSet());

    /**
     * 判断物品是否为已知的攻击型蛊虫。
     *
     * @param stack 待检查的物品堆
     * @return 如果物品属于任一已知的攻击蛊虫标签，则返回 true
     */
    public static boolean isAttackGu(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 遍历所有攻击标签进行匹配
        for (TagKey<Item> tag : ATTACK_GU_TAGS) {
            if (stack.is(tag)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取所有攻击蛊虫标签列表（供外部遍历或调试）。
     */
    public static Set<TagKey<Item>> getAttackGuTags() {
        return ATTACK_GU_TAGS;
    }
}
