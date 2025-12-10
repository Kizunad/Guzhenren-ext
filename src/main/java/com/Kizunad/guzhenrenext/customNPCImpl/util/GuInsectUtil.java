package com.Kizunad.guzhenrenext.customNPCImpl.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
        "guzhenren:gushiguchong2",
        "guzhenren:gushiguchong3",
        "guzhenren:gushiguchong4",
        "guzhenren:gushiguchong5"
    )
        .map(id -> TagKey.create(Registries.ITEM, ResourceLocation.parse(id)))
        .collect(Collectors.toSet());

    // 定义治疗类蛊虫标签集合（后缀 _1）
    private static final Set<TagKey<Item>> HEAL_GU_TAGS = Stream.of(
        "guzhenren:gushiguchong1_1",
        "guzhenren:gushiguchong2_1",
        "guzhenren:gushiguchong3_1",
        "guzhenren:gushiguchong4_1",
        "guzhenren:gushiguchong5_1"
    )
        .map(id -> TagKey.create(Registries.ITEM, ResourceLocation.parse(id)))
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
     * 判断物品是否为已知的治疗型蛊虫。
     *
     * @param stack 待检查的物品堆
     * @return 如果物品属于任一治疗蛊虫标签，则返回 true
     */
    public static boolean isHealGu(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (TagKey<Item> tag : HEAL_GU_TAGS) {
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

    /**
     * 获取所有治疗蛊虫标签列表。
     */
    public static Set<TagKey<Item>> getHealGuTags() {
        return HEAL_GU_TAGS;
    }
}
