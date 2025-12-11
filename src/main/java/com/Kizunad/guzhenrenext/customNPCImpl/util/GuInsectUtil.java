package com.Kizunad.guzhenrenext.customNPCImpl.util;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 蛊虫物品判定工具类。
 * <p>
 * 通过物品标签判断当前物品是否属于攻击型或治疗型蛊虫，
 * 便于 AI 在装备或制作时复用统一判断逻辑。
 * </p>
 */
public final class GuInsectUtil {

    private static final List<TagKey<Item>> ATTACK_TAGS = List.of(
        tag("guzhenren:gushiguchong1"),
        tag("guzhenren:gushiguchong2"),
        tag("guzhenren:gushiguchong3"),
        tag("guzhenren:gushiguchong4"),
        tag("guzhenren:gushiguchong5")
    );
    private static final List<TagKey<Item>> HEAL_TAGS = List.of(
        tag("guzhenren:gushiguchong1_1"),
        tag("guzhenren:gushiguchong2_1"),
        tag("guzhenren:gushiguchong3_1"),
        tag("guzhenren:gushiguchong4_1"),
        tag("guzhenren:gushiguchong5_1")
    );

    private GuInsectUtil() {}

    /**
     * 判定物品是否为攻击型蛊虫。
     *
     * @param stack 物品
     * @return true 表示属于攻击蛊虫标签
     */
    public static boolean isAttackGu(ItemStack stack) {
        return isStackInTags(stack, ATTACK_TAGS);
    }

    /**
     * 判定物品是否为治疗型蛊虫。
     *
     * @param stack 物品
     * @return true 表示属于治疗蛊虫标签
     */
    public static boolean isHealGu(ItemStack stack) {
        return isStackInTags(stack, HEAL_TAGS);
    }

    private static boolean isStackInTags(
        ItemStack stack,
        List<TagKey<Item>> tags
    ) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (TagKey<Item> tag : tags) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    private static TagKey<Item> tag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
    }
}
