package com.Kizunad.guzhenrenext.kongqiao.client;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection;
import com.Kizunad.guzhenrenext.network.ClientboundKongqiaoSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
        final KongqiaoPressureProjection projection =
            KongqiaoPressureProjection.fromTag(payload.projection());
        KongqiaoClientProjectionCache.apply(projection);
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
        applyAuthoritativeState(
            data,
            mc.level.registryAccess(),
            payload.data(),
            projection
        );
    }

    static void applyAuthoritativeState(
        final KongqiaoData data,
        final HolderLookup.Provider provider,
        final CompoundTag rawData,
        final KongqiaoPressureProjection projection
    ) {
        if (data == null) {
            return;
        }
        KongqiaoClientProjectionCache.apply(projection);
        data.deserializeNBT(provider, rawData == null ? new CompoundTag() : rawData);
    }
}
