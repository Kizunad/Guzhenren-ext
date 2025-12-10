package com.Kizunad.customNPCs.ai.decision.goals;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 装甲部件工具类：统一维护盔甲的优先级以及槽位解析，避免多处重复硬编码。
 */
final class ArmorTierHelper {

    private static final int TIER_LEATHER = 0;
    private static final int TIER_GOLD = 1;
    private static final int TIER_CHAIN = 2;
    private static final int TIER_IRON = 3;
    private static final int TIER_DIAMOND = 4;
    private static final int TIER_NETHERITE = 5;
    private static final Map<Item, Integer> ARMOR_TIER = buildArmorTier();

    private ArmorTierHelper() {}

    static EquipmentSlot slotOf(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getEquipmentSlot();
        }
        return null;
    }

    static int tierOf(ItemStack stack) {
        return ARMOR_TIER.getOrDefault(stack.getItem(), -1);
    }

    private static Map<Item, Integer> buildArmorTier() {
        Map<Item, Integer> map = new HashMap<>();
        // 皮革
        map.put(Items.LEATHER_HELMET, TIER_LEATHER);
        map.put(Items.LEATHER_CHESTPLATE, TIER_LEATHER);
        map.put(Items.LEATHER_LEGGINGS, TIER_LEATHER);
        map.put(Items.LEATHER_BOOTS, TIER_LEATHER);
        // 金
        map.put(Items.GOLDEN_HELMET, TIER_GOLD);
        map.put(Items.GOLDEN_CHESTPLATE, TIER_GOLD);
        map.put(Items.GOLDEN_LEGGINGS, TIER_GOLD);
        map.put(Items.GOLDEN_BOOTS, TIER_GOLD);
        // 锁链
        map.put(Items.CHAINMAIL_HELMET, TIER_CHAIN);
        map.put(Items.CHAINMAIL_CHESTPLATE, TIER_CHAIN);
        map.put(Items.CHAINMAIL_LEGGINGS, TIER_CHAIN);
        map.put(Items.CHAINMAIL_BOOTS, TIER_CHAIN);
        // 铁
        map.put(Items.IRON_HELMET, TIER_IRON);
        map.put(Items.IRON_CHESTPLATE, TIER_IRON);
        map.put(Items.IRON_LEGGINGS, TIER_IRON);
        map.put(Items.IRON_BOOTS, TIER_IRON);
        // 钻石
        map.put(Items.DIAMOND_HELMET, TIER_DIAMOND);
        map.put(Items.DIAMOND_CHESTPLATE, TIER_DIAMOND);
        map.put(Items.DIAMOND_LEGGINGS, TIER_DIAMOND);
        map.put(Items.DIAMOND_BOOTS, TIER_DIAMOND);
        // 下界合金
        map.put(Items.NETHERITE_HELMET, TIER_NETHERITE);
        map.put(Items.NETHERITE_CHESTPLATE, TIER_NETHERITE);
        map.put(Items.NETHERITE_LEGGINGS, TIER_NETHERITE);
        map.put(Items.NETHERITE_BOOTS, TIER_NETHERITE);
        return map;
    }
}
