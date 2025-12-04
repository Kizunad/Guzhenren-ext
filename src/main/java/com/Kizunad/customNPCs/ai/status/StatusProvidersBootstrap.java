package com.Kizunad.customNPCs.ai.status;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.network.dto.NpcStatusEntry;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 注册内置的状态提供者（HP, Hunger）。
 */
public final class StatusProvidersBootstrap {

    private static final ResourceLocation HP_ICON = ResourceLocation.fromNamespaceAndPath(
        CustomNPCsMod.MODID,
        "textures/gui/heart.png"
    );
    private static final ResourceLocation FOOD_ICON = ResourceLocation.fromNamespaceAndPath(
        CustomNPCsMod.MODID,
        "textures/gui/food.png"
    );

    // Checkstyle ignore magic numbers for colors and thresholds
    private static final float HEALTH_LOW_THRESHOLD = 0.3f;
    private static final float HEALTH_MID_THRESHOLD = 0.7f;
    private static final int COLOR_RED = 0xFFFF0000;
    private static final int COLOR_YELLOW = 0xFFFFFF00;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int HUNGER_LOW_THRESHOLD = 6;
    private static final int HUNGER_MAX = 20;

    private StatusProvidersBootstrap() {}

    public static void registerBuiltins() {
        StatusProviderRegistry.register(
            ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "health"),
            StatusProvidersBootstrap::provideHealth
        );
        StatusProviderRegistry.register(
            ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "hunger"),
            StatusProvidersBootstrap::provideHunger
        );
    }

    private static List<NpcStatusEntry> provideHealth(CustomNpcEntity npc) {
        float health = npc.getHealth();
        float max = npc.getMaxHealth();
        int color = COLOR_WHITE;
        if (health / max < HEALTH_LOW_THRESHOLD) {
            color = COLOR_RED;
        } else if (health / max < HEALTH_MID_THRESHOLD) {
            color = COLOR_YELLOW;
        }
        return List.of(
            new NpcStatusEntry(
                HP_ICON,
                Component.literal("HP"),
                Component.literal(String.format("%.0f/%.0f", health, max)),
                color
            )
        );
    }

    private static List<NpcStatusEntry> provideHunger(CustomNpcEntity npc) {
        if (!npc.hasData(NpcMindAttachment.NPC_MIND)) {
            return List.of();
        }
        INpcMind mind = npc.getData(NpcMindAttachment.NPC_MIND);
        int hunger = mind.getStatus().getHunger();
        int color = COLOR_WHITE;
        if (hunger < HUNGER_LOW_THRESHOLD) {
            color = COLOR_RED;
        }
        return List.of(
            new NpcStatusEntry(
                FOOD_ICON,
                Component.literal("Hunger"),
                Component.literal(String.format("%d/%d", hunger, HUNGER_MAX)),
                color
            )
        );
    }
}
