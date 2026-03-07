package com.Kizunad.guzhenrenext.xianqiao.alchemy.item;

import net.minecraft.world.food.FoodProperties;

public final class DeepPillFoods {

    private static final float DEEP_PILL_SATURATION = 0.1F;

    public static final FoodProperties DEEP_PILL_FOOD = new FoodProperties.Builder()
        .nutrition(1)
        .saturationModifier(DEEP_PILL_SATURATION)
        .alwaysEdible()
        .build();

    private DeepPillFoods() {
    }
}
