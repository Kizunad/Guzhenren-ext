package com.Kizunad.guzhenrenext.kongqiao.client;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.network.ClientboundKongqiaoSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 客户端处理空窍同步。
 */
@OnlyIn(Dist.CLIENT)
public final class KongqiaoSyncClientHandler {

    private KongqiaoSyncClientHandler() {}

    public static void applySync(
        ClientboundKongqiaoSyncPayload payload,
        PacketFlow flow
    ) {
        if (flow != PacketFlow.CLIENTBOUND) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }
        KongqiaoData data = KongqiaoAttachments.getData(mc.player);
        if (data == null) {
            mc
                .player
                .setData(KongqiaoAttachments.KONGQIAO.get(), new KongqiaoData());
            data = KongqiaoAttachments.getData(mc.player);
        }
        if (data == null) {
            return;
        }
        data.bind(mc.player);
        data.deserializeNBT(mc.level.registryAccess(), payload.data());
    }
}
