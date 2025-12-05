package com.Kizunad.customNPCs.ai.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * 盔甲附魔相关工具。
 * <p>
 * 统一盔甲常用附魔池并提供缺失/可升级查询，允许多种保护系共存。
 * </p>
 */
public final class ArmorEnchantmentUtil {

    /** 盔甲可使用的附魔键列表。 */
    public static final List<ResourceKey<Enchantment>> ARMOR_ENCHANTMENTS = List.of(
        Enchantments.PROTECTION,
        Enchantments.THORNS,
        Enchantments.UNBREAKING,
        Enchantments.BLAST_PROTECTION,
        Enchantments.FIRE_PROTECTION,
        Enchantments.PROJECTILE_PROTECTION
    );

    private ArmorEnchantmentUtil() {}

    /**
     * 判断物品是否为可附魔盔甲。
     */
    public static boolean isEnchantableArmor(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ArmorItem;
    }

    /**
     * 获取盔甲上缺失的目标附魔列表（等级<=0）。
     */
    public static List<Holder<Enchantment>> findMissingEnchantments(
        ItemStack stack,
        HolderGetter<Enchantment> getter
    ) {
        List<Holder<Enchantment>> missing = new ArrayList<>();
        if (!isEnchantableArmor(stack)) {
            return missing;
        }
        for (ResourceKey<Enchantment> key : ARMOR_ENCHANTMENTS) {
            Holder<Enchantment> enchantment = getter.getOrThrow(key);
            if (EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) <= 0) {
                missing.add(enchantment);
            }
        }
        return missing;
    }

    /**
     * 收集盔甲上可升级的附魔（含等级0 -> 1 的新增场景）。
     */
    public static List<EnchantmentLevel> collectUpgradeableEnchantments(
        ItemStack stack,
        HolderGetter<Enchantment> getter
    ) {
        List<EnchantmentLevel> result = new ArrayList<>();
        if (!isEnchantableArmor(stack)) {
            return result;
        }
        for (ResourceKey<Enchantment> key : ARMOR_ENCHANTMENTS) {
            Holder<Enchantment> enchantment = getter.getOrThrow(key);
            int level = EnchantmentHelper.getItemEnchantmentLevel(
                enchantment,
                stack
            );
            int maxLevel = enchantment.value().getMaxLevel();
            if (level < maxLevel) {
                result.add(new EnchantmentLevel(enchantment, level, maxLevel));
            }
        }
        return result;
    }

    public record EnchantmentLevel(
        Holder<Enchantment> enchantment,
        int level,
        int maxLevel
    ) {}
}
