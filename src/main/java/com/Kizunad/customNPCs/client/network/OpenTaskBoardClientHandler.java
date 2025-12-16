package com.Kizunad.customNPCs.client.network;

import com.Kizunad.customNPCs.client.ui.task.NpcTaskBoardScreen;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;

/**
 * 客户端侧 S2C 处理：打开任务面板。
 */
@OnlyIn(Dist.CLIENT)
public final class OpenTaskBoardClientHandler {

    private OpenTaskBoardClientHandler() {}

    public static void handle(OpenTaskBoardPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        minecraft.setScreen(new NpcTaskBoardScreen(payload));
    }
}
