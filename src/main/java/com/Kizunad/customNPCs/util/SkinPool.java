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
        ),
        /*src/main/resources/assets/customnpcs/textures/entity/4055303bb3884ce8.png
        src/main/resources/assets/customnpcs/textures/entity/43a55110af6f3206.png
        src/main/resources/assets/customnpcs/textures/entity/74423d6acad624a0.png
        src/main/resources/assets/customnpcs/textures/entity/8f2d87e1fbca9eba.png
        src/main/resources/assets/customnpcs/textures/entity/a57bf4567a347c37.png
        src/main/resources/assets/customnpcs/textures/entity/afc871faf2e94965.png
        src/main/resources/assets/customnpcs/textures/entity/bb275912565345fd.png
        src/main/resources/assets/customnpcs/textures/entity/bf5d1e8f5dba81f3.png
        src/main/resources/assets/customnpcs/textures/entity/c51d20992fde8dad.png
        src/main/resources/assets/customnpcs/textures/entity/e2653f51d49f958a.png
        src/main/resources/assets/customnpcs/textures/entity/ee84451763e8fbd4.png */
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/4055303bb3884ce8.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/43a55110af6f3206.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/74423d6acad624a0.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/8f2d87e1fbca9eba.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/a57bf4567a347c37.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/afc871faf2e94965.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/bb275912565345fd.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/bf5d1e8f5dba81f3.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/c51d20992fde8dad.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/e2653f51d49f958a.png"
        ),
        ResourceLocation.fromNamespaceAndPath(
            "customnpcs",
            "textures/entity/ee84451763e8fbd4.png"
        )
    );
    private static final Random RANDOM = new Random();

    public static ResourceLocation getRandomSkin() {
        return SKINS.get(RANDOM.nextInt(SKINS.size()));
    }
}
