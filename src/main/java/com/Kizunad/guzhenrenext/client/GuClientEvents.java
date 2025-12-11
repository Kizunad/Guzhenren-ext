package com.Kizunad.guzhenrenext.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.network.PacketOpenNianTouGui;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class GuClientEvents {

    private GuClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        // 检查按键是否按下，并消耗点击（防止连续触发）
        while (GuKeyBindings.OPEN_NIANTOU_GUI.consumeClick()) {
            // 发送网络包请求打开 GUI
            PacketDistributor.sendToServer(new PacketOpenNianTouGui());
        }
    }
}
