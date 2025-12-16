package com.Kizunad.customNPCs.client.network;

import com.Kizunad.customNPCs.client.ui.interact.NpcInteractScreen;
import com.Kizunad.customNPCs.network.OpenInteractGuiPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;

/**
 * 客户端侧 S2C 处理：打开 NPC 交互界面。
 */
@OnlyIn(Dist.CLIENT)
public final class OpenInteractGuiClientHandler {

    private OpenInteractGuiClientHandler() {}

    public static void handle(OpenInteractGuiPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        minecraft.setScreen(
            new NpcInteractScreen(
                new NpcInteractScreen.InteractData(
                    payload.npcEntityId(),
                    payload.displayName(),
                    payload.health(),
                    payload.maxHealth(),
                    payload.isOwner(),
                    payload.startInDialogueMode(),
                    payload.statusEntries(),
                    payload.dialogueOptions()
                )
            )
        );
    }
}
