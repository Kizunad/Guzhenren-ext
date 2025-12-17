package com.Kizunad.guzhenrenext.kongqiao.client;

import com.Kizunad.guzhenrenext.network.ServerboundLingHunGuDoubleJumpPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 羚魂蛊客户端输入处理：
 * <p>
 * “二段跳”必须捕获玩家的跳跃按键输入，但不能消耗原版按键事件，否则会影响正常跳跃。
 * 因此这里仅做“边沿检测”（按下瞬间）并发送服务端请求，由服务端校验是否拥有踏空用途。
 * </p>
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class LingHunGuClientInputHandler {

    private static boolean wasJumpDown = false;

    private LingHunGuClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            wasJumpDown = false;
            return;
        }

        final boolean jumpDown = minecraft.options.keyJump.isDown();
        if (jumpDown && !wasJumpDown) {
            if (!minecraft.player.onGround()) {
                PacketDistributor.sendToServer(new ServerboundLingHunGuDoubleJumpPayload());
            }
        }
        wasJumpDown = jumpDown;
    }
}

