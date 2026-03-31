package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphClientState;
import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphSnapshot;
import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphSnapshot.EdgeSnapshot;
import com.Kizunad.guzhenrenext.faction.client.FactionRelationGraphSnapshot.NodeSnapshot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundFactionRelationGraphSyncPayload(
    boolean synced,
    boolean hasData,
    List<NodeSnapshot> nodes,
    List<EdgeSnapshot> edges
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "faction_relation_graph_sync"
    );
    public static final Type<ClientboundFactionRelationGraphSyncPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFactionRelationGraphSyncPayload>
        STREAM_CODEC = StreamCodec.of(
            ClientboundFactionRelationGraphSyncPayload::encode,
            ClientboundFactionRelationGraphSyncPayload::decode
        );

    public ClientboundFactionRelationGraphSyncPayload {
        nodes = normalizeNodes(nodes);
        edges = normalizeEdges(edges);
    }

    public static ClientboundFactionRelationGraphSyncPayload emptySnapshot() {
        return new ClientboundFactionRelationGraphSyncPayload(true, false, List.of(), List.of());
    }

    public FactionRelationGraphSnapshot toSnapshot() {
        return new FactionRelationGraphSnapshot(synced, hasData, nodes, edges);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        final ClientboundFactionRelationGraphSyncPayload payload,
        final IPayloadContext context
    ) {
        if (context == null) {
            return;
        }
        context.enqueueWork(() -> {
            if (context.flow() != PacketFlow.CLIENTBOUND) {
                return;
            }
            FactionRelationGraphClientState.applySnapshot(
                payload == null ? null : payload.toSnapshot()
            );
        });
    }

    private static void encode(
        final RegistryFriendlyByteBuf buf,
        final ClientboundFactionRelationGraphSyncPayload payload
    ) {
        buf.writeBoolean(payload.synced());
        buf.writeBoolean(payload.hasData());
        writeNodes(buf, payload.nodes());
        writeEdges(buf, payload.edges());
    }

    private static ClientboundFactionRelationGraphSyncPayload decode(
        final RegistryFriendlyByteBuf buf
    ) {
        return new ClientboundFactionRelationGraphSyncPayload(
            buf.readBoolean(),
            buf.readBoolean(),
            readNodes(buf),
            readEdges(buf)
        );
    }

    private static void writeNodes(
        final RegistryFriendlyByteBuf buf,
        final List<NodeSnapshot> nodes
    ) {
        buf.writeInt(nodes.size());
        for (NodeSnapshot node : nodes) {
            buf.writeUtf(node.factionId());
            buf.writeUtf(node.factionName());
            buf.writeUtf(node.factionType());
            buf.writeInt(node.power());
            buf.writeInt(node.resources());
            buf.writeInt(node.memberCount());
        }
    }

    private static List<NodeSnapshot> readNodes(final RegistryFriendlyByteBuf buf) {
        final int size = Math.max(0, buf.readInt());
        final List<NodeSnapshot> nodes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            nodes.add(
                new NodeSnapshot(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt()
                )
            );
        }
        return List.copyOf(nodes);
    }

    private static void writeEdges(
        final RegistryFriendlyByteBuf buf,
        final List<EdgeSnapshot> edges
    ) {
        buf.writeInt(edges.size());
        for (EdgeSnapshot edge : edges) {
            buf.writeUtf(edge.sourceFactionId());
            buf.writeUtf(edge.targetFactionId());
            buf.writeInt(edge.relationValue());
            buf.writeUtf(edge.relationLevel());
        }
    }

    private static List<EdgeSnapshot> readEdges(final RegistryFriendlyByteBuf buf) {
        final int size = Math.max(0, buf.readInt());
        final List<EdgeSnapshot> edges = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            edges.add(
                new EdgeSnapshot(
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readUtf()
                )
            );
        }
        return List.copyOf(edges);
    }

    private static List<NodeSnapshot> normalizeNodes(final List<NodeSnapshot> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }
        final List<NodeSnapshot> normalizedNodes = new ArrayList<>(nodes.size());
        for (NodeSnapshot node : nodes) {
            normalizedNodes.add(normalizeNode(node));
        }
        return List.copyOf(normalizedNodes);
    }

    private static List<EdgeSnapshot> normalizeEdges(final List<EdgeSnapshot> edges) {
        if (edges == null || edges.isEmpty()) {
            return List.of();
        }
        final List<EdgeSnapshot> normalizedEdges = new ArrayList<>(edges.size());
        for (EdgeSnapshot edge : edges) {
            normalizedEdges.add(normalizeEdge(edge));
        }
        return List.copyOf(normalizedEdges);
    }

    private static NodeSnapshot normalizeNode(final NodeSnapshot node) {
        if (node == null) {
            return new NodeSnapshot("", "", "", 0, 0, 0);
        }
        return new NodeSnapshot(
            node.factionId(),
            node.factionName(),
            node.factionType(),
            node.power(),
            node.resources(),
            node.memberCount()
        );
    }

    private static EdgeSnapshot normalizeEdge(final EdgeSnapshot edge) {
        if (edge == null) {
            return new EdgeSnapshot("", "", 0, "");
        }
        return new EdgeSnapshot(
            edge.sourceFactionId(),
            edge.targetFactionId(),
            edge.relationValue(),
            edge.relationLevel()
        );
    }
}
