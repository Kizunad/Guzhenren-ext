package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.faction.client.FactionInfoClientHandler;
import com.Kizunad.guzhenrenext.faction.client.FactionInfoSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundFactionInfoSyncPayload(
    boolean hasDisplayFaction,
    String factionName,
    String factionType,
    int memberCount,
    int power,
    int resources,
    int playerRelationValue
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "faction_info_sync"
    );
    public static final Type<ClientboundFactionInfoSyncPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFactionInfoSyncPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.hasDisplayFaction);
                buf.writeUtf(payload.factionName);
                buf.writeUtf(payload.factionType);
                buf.writeInt(payload.memberCount);
                buf.writeInt(payload.power);
                buf.writeInt(payload.resources);
                buf.writeInt(payload.playerRelationValue);
            },
            buf ->
                new ClientboundFactionInfoSyncPayload(
                    buf.readBoolean(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt()
                )
        );

    public ClientboundFactionInfoSyncPayload {
        factionName = factionName == null ? "" : factionName;
        factionType = factionType == null ? "" : factionType;
    }

    public static ClientboundFactionInfoSyncPayload emptySnapshot() {
        return new ClientboundFactionInfoSyncPayload(false, "", "", 0, 0, 0, 0);
    }

    public FactionInfoSnapshot toSnapshot() {
        return new FactionInfoSnapshot(
            true,
            hasDisplayFaction,
            factionName,
            factionType,
            memberCount,
            power,
            resources,
            playerRelationValue
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        final ClientboundFactionInfoSyncPayload payload,
        final IPayloadContext context
    ) {
        context.enqueueWork(() ->
            FactionInfoClientHandler.applySync(payload, context.flow())
        );
    }
}
