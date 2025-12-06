package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.client.data.MaterialDataCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * S2C：同步 NPC 当前 Owner 材料点余额。
 */
public record SyncMaterialDataPayload(int npcEntityId, double ownerMaterial)
    implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncMaterialDataPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "sync_material_data"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        SyncMaterialDataPayload
    > STREAM_CODEC = StreamCodec.of(
        SyncMaterialDataPayload::write,
        SyncMaterialDataPayload::read
    );

    private static void write(
        RegistryFriendlyByteBuf buf,
        SyncMaterialDataPayload payload
    ) {
        buf.writeVarInt(payload.npcEntityId);
        buf.writeDouble(payload.ownerMaterial);
    }

    private static SyncMaterialDataPayload read(RegistryFriendlyByteBuf buf) {
        int id = buf.readVarInt();
        double value = buf.readDouble();
        return new SyncMaterialDataPayload(id, value);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        SyncMaterialDataPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }
            if (context.player().level().isClientSide()) {
                MaterialDataCache.updateOwnerMaterial(
                    payload.npcEntityId(),
                    payload.ownerMaterial()
                );
            }
        });
    }
}
