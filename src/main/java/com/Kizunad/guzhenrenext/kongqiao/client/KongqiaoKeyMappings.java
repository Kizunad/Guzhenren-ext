package com.Kizunad.guzhenrenext.kongqiao.client;

import com.Kizunad.guzhenrenext.network.ServerboundKongqiaoActionPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * 空窍快捷键映射与处理。
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class KongqiaoKeyMappings {

    private static final String CATEGORY = "key.categories.guzhenrenext";

    public static final KeyMapping KONGQIAO_KEY = new KeyMapping(
        "key.guzhenrenext.kongqiao",
        GLFW.GLFW_KEY_K,
        CATEGORY
    );
    public static final KeyMapping ATTACK_SWAP_KEY = new KeyMapping(
        "key.guzhenrenext.attack_swap",
        GLFW.GLFW_KEY_UNKNOWN,
        CATEGORY
    );
    public static final KeyMapping ATTACK_SCREEN_KEY = new KeyMapping(
        "key.guzhenrenext.attack_inventory",
        GLFW.GLFW_KEY_UNKNOWN,
        CATEGORY
    );

    private KongqiaoKeyMappings() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        while (KONGQIAO_KEY.consumeClick()) {
            send(ServerboundKongqiaoActionPayload.Action.OPEN_KONGQIAO);
        }
        while (ATTACK_SWAP_KEY.consumeClick()) {
            send(ServerboundKongqiaoActionPayload.Action.SWAP_ATTACK);
        }
        while (ATTACK_SCREEN_KEY.consumeClick()) {
            send(ServerboundKongqiaoActionPayload.Action.OPEN_ATTACK);
        }
    }

    private static void send(ServerboundKongqiaoActionPayload.Action action) {
        PacketDistributor.sendToServer(new ServerboundKongqiaoActionPayload(action));
    }
}
