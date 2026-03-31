package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.faction.core.FactionCore;
import com.Kizunad.guzhenrenext.faction.core.FactionMembership;
import com.Kizunad.guzhenrenext.faction.service.FactionMembershipManager;
import com.Kizunad.guzhenrenext.faction.service.FactionService;
import io.netty.buffer.ByteBuf;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundFactionInfoRequestPayload()
    implements CustomPacketPayload {

    private static final int SAME_FACTION_RELATION = 100;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "faction_info_request"
    );
    public static final Type<ServerboundFactionInfoRequestPayload> TYPE =
        new Type<>(ID);

    public static final StreamCodec<ByteBuf, ServerboundFactionInfoRequestPayload> STREAM_CODEC =
        StreamCodec.unit(new ServerboundFactionInfoRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        final ServerboundFactionInfoRequestPayload payload,
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

    private static ClientboundFactionInfoSyncPayload buildSnapshotPayload(
        final ServerPlayer serverPlayer
    ) {
        final ServerLevel level = serverPlayer.serverLevel();
        final FactionMembership playerMembership = findPlayerMembership(
            level,
            serverPlayer.getUUID()
        );
        final FactionCore displayFaction = resolveDisplayFaction(level, playerMembership);
        if (displayFaction == null) {
            return ClientboundFactionInfoSyncPayload.emptySnapshot();
        }

        return new ClientboundFactionInfoSyncPayload(
            true,
            displayFaction.name(),
            displayFaction.type().name(),
            FactionMembershipManager.getMemberCount(level, displayFaction.id()),
            displayFaction.power(),
            displayFaction.resources(),
            resolvePlayerRelationValue(playerMembership, displayFaction.id(), level)
        );
    }

    private static FactionCore resolveDisplayFaction(
        final ServerLevel level,
        final FactionMembership playerMembership
    ) {
        if (playerMembership != null) {
            final FactionCore playerFaction = FactionService.getFaction(
                level,
                playerMembership.factionId()
            );
            if (playerFaction != null) {
                return playerFaction;
            }
        }
        return FactionService.getAllFactions(level)
            .stream()
            .sorted(
                Comparator.comparingLong(FactionCore::createdAt)
                    .thenComparing(FactionCore::name)
            )
            .findFirst()
            .orElse(null);
    }

    private static int resolvePlayerRelationValue(
        final FactionMembership playerMembership,
        final UUID targetFactionId,
        final ServerLevel level
    ) {
        if (playerMembership == null || targetFactionId == null) {
            return 0;
        }
        if (targetFactionId.equals(playerMembership.factionId())) {
            return SAME_FACTION_RELATION;
        }
        return FactionService.getRelation(
            level,
            playerMembership.factionId(),
            targetFactionId
        );
    }

    private static FactionMembership findPlayerMembership(
        final ServerLevel level,
        final UUID playerId
    ) {
        if (level == null || playerId == null) {
            return null;
        }
        for (FactionCore faction : FactionService.getAllFactions(level)) {
            final List<FactionMembership> memberships = FactionService.getMembers(
                level,
                faction.id()
            );
            for (FactionMembership membership : memberships) {
                if (playerId.equals(membership.memberId())) {
                    return membership;
                }
            }
        }
        return null;
    }
}
