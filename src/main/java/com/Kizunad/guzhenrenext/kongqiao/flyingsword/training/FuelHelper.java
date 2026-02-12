package com.Kizunad.guzhenrenext.kongqiao.flyingsword.training;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FuelHelper {

    private FuelHelper() {}

    private static final String GUZHENREN_NAMESPACE = "guzhenren";
    private static final String PRIMEVAL_STONES_PATH = "primeval_stones";
    private static final int PRIMEVAL_STONES_BURN_TICKS = 200;

    private static final TagKey<Item> PRIMEVAL_STONES = TagKey.create(
        BuiltInRegistries.ITEM.key(),
        ResourceLocation.fromNamespaceAndPath(
            GUZHENREN_NAMESPACE,
            PRIMEVAL_STONES_PATH
        )
    );

    public static boolean isFuel(ItemStack stack) {
        return getFuelTime(stack) > 0;
    }

    public static int getFuelTime(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        if (stack.is(PRIMEVAL_STONES)) {
            return PRIMEVAL_STONES_BURN_TICKS;
        }
        return 0;
    }
}
