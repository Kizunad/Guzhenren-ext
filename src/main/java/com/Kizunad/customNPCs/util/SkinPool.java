package com.Kizunad.customNPCs.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;

public class SkinPool {

    private static final List<ResourceLocation> SKINS = Arrays.asList(
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_male_1.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_male_2.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_male_3.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_female_1.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_female_2.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_female_3.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_female_4.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_female_5.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/human_female_6.png"
        )
    );
    private static final Random RANDOM = new Random();

    public static ResourceLocation getRandomSkin() {
        return SKINS.get(RANDOM.nextInt(SKINS.size()));
    }
}
