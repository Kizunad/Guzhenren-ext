package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphSnapshot.EdgeSnapshot;
import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphSnapshot.NodeSnapshot;
import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionRelationMatrix;
import com.Kizunad.guzhenrenext.faction.data.FactionWorldData;
import com.Kizunad.guzhenrenext.faction.service.FactionMembershipManager;
import com.Kizunad.guzhenrenext.faction.service.FactionService;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundFactionRelationGraphRequestPayload()
    implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "faction_relation_graph_request"
    );
    public static final Type<ServerboundFactionRelationGraphRequestPayload> TYPE =
        new Type<>(ID);

    public static final StreamCodec<ByteBuf, ServerboundFactionRelationGraphRequestPayload>
        STREAM_CODEC = StreamCodec.unit(new ServerboundFactionRelationGraphRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        final ServerboundFactionRelationGraphRequestPayload payload,
        final IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (serverPlayer.level().isClientSide()) {
                return;
            }
            PacketDistributor.sendToPlayer(
                serverPlayer,
                buildSnapshotPayload(serverPlayer)
            );
        });
    }

    private static ClientboundFactionRelationGraphSyncPayload buildSnapshotPayload(
        final ServerPlayer serverPlayer
    ) {
        final ServerLevel level = serverPlayer.serverLevel();
        final List<FactionCore> factions = FactionService.getAllFactions(level)
            .stream()
            .sorted(
                Comparator.comparingLong(FactionCore::createdAt)
                    .thenComparing(FactionCore::name)
            )
            .toList();
        if (factions.isEmpty()) {
            return ClientboundFactionRelationGraphSyncPayload.emptySnapshot();
        }
        final FactionRelationMatrix relationMatrix = FactionWorldData.get(level).getRelationMatrix();
        return new ClientboundFactionRelationGraphSyncPayload(
            true,
            true,
            buildNodes(level, factions),
            buildEdges(level, factions, relationMatrix)
        );
    }

    private static List<NodeSnapshot> buildNodes(
        final ServerLevel level,
        final List<FactionCore> factions
    ) {
        final List<NodeSnapshot> nodes = new ArrayList<>(factions.size());
        for (FactionCore faction : factions) {
            nodes.add(
                new NodeSnapshot(
                    faction.id().toString(),
                    faction.name(),
                    faction.type().name(),
                    faction.power(),
                    faction.resources(),
                    FactionMembershipManager.getMemberCount(level, faction.id())
                )
            );
        }
        return List.copyOf(nodes);
    }

    private static List<EdgeSnapshot> buildEdges(
        final ServerLevel level,
        final List<FactionCore> factions,
        final FactionRelationMatrix relationMatrix
    ) {
        final List<EdgeSnapshot> edges = new ArrayList<>();
        for (int i = 0; i < factions.size(); i++) {
            final FactionCore sourceFaction = factions.get(i);
            for (int j = i + 1; j < factions.size(); j++) {
                final FactionCore targetFaction = factions.get(j);
                final int relationValue = FactionService.getRelation(
                    level,
                    sourceFaction.id(),
                    targetFaction.id()
                );
                edges.add(
                    new EdgeSnapshot(
                        sourceFaction.id().toString(),
                        targetFaction.id().toString(),
                        relationValue,
                        relationMatrix.getRelationLevel(sourceFaction.id(), targetFaction.id()).name()
                    )
                );
            }
        }
        return List.copyOf(edges);
    }
}
