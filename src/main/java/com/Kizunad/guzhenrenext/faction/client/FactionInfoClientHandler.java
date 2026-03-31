package com.Kizunad.guzhenrenext.faction.client;

import com.Kizunad.guzhenrenext.network.ClientboundFactionInfoSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FactionInfoClientHandler {

    private FactionInfoClientHandler() {
    }

    public static void applySync(
        final ClientboundFactionInfoSyncPayload payload,
        final PacketFlow flow
    ) {
        if (flow != PacketFlow.CLIENTBOUND || payload == null) {
            return;
        }
        FactionInfoClientState.applySnapshot(payload.toSnapshot());
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        if (minecraft.screen == null) {
            minecraft.setScreen(new FactionInfoScreen());
        }
    }
}
