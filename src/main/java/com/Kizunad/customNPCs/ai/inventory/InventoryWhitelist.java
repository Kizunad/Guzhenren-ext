package com.Kizunad.customNPCs.ai.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TieredItem;

/**
 * 背包保留白名单：集中管理不应被拆解/压缩的物品，便于统一维护。
 */
public final class InventoryWhitelist {

    private static final Set<Item> ITEM_WHITELIST = new HashSet<>();
    private static final List<Predicate<ItemStack>> PREDICATES =
        new ArrayList<>();

    static {
        addItem(Items.TOTEM_OF_UNDYING);
        addPredicate(stack -> stack.getItem() instanceof PotionItem);
        addPredicate(stack -> stack.getItem() instanceof ArmorItem);
        addPredicate(stack -> stack.getItem() instanceof TieredItem);
        addPredicate(stack -> stack.getItem() instanceof BowItem);
        addPredicate(stack -> stack.getItem() instanceof CrossbowItem);
        addPredicate(stack -> stack.getItem() instanceof ArrowItem);
    }

    private InventoryWhitelist() {}

    /**
     * 判断物品是否在白名单中。
     */
    public static boolean isWhitelisted(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (ITEM_WHITELIST.contains(item)) {
            return true;
        }
        for (Predicate<ItemStack> predicate : PREDICATES) {
            if (predicate.test(stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 动态添加白名单物品。
     */
    public static void addItem(Item item) {
        ITEM_WHITELIST.add(Objects.requireNonNull(item));
    }

    /**
     * 动态添加判定规则。
     */
    public static void addPredicate(Predicate<ItemStack> predicate) {
        PREDICATES.add(Objects.requireNonNull(predicate));
    }
}
