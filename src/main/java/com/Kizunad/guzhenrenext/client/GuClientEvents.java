package com.Kizunad.guzhenrenext.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.client.gui.SkillWheelScreen;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.TweakScreen;
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
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        // 检查按键是否按下，并消耗点击（防止连续触发）
        while (GuKeyBindings.OPEN_NIANTOU_GUI.consumeClick()) {
            // 发送网络包请求打开 GUI
            PacketDistributor.sendToServer(new PacketOpenNianTouGui());
        }

        // 技能轮盘属于纯客户端 Screen：按住打开，松开由 Screen 内部确认。
        while (GuKeyBindings.OPEN_SKILL_WHEEL.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new SkillWheelScreen(GuKeyBindings.OPEN_SKILL_WHEEL));
            }
        }

        // 调整面板属于纯客户端 Screen，但依赖服务端同步配置；由 Screen init() 内主动发起同步请求。
        while (GuKeyBindings.OPEN_TWEAK_UI.consumeClick()) {
            if (minecraft.screen == null) {
                minecraft.setScreen(new TweakScreen());
            }
        }
    }
}
