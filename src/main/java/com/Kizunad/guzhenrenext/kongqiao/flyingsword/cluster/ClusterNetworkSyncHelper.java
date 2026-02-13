package com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordClusterAttachment;
import com.Kizunad.guzhenrenext.network.ClientboundClusterStatePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClusterNetworkSyncHelper {

    private ClusterNetworkSyncHelper() {}

    public static ClientboundClusterStatePayload createPayload(ServerPlayer player) {
        if (player == null) {
            return new ClientboundClusterStatePayload(0, 0, List.of());
        }
        FlyingSwordClusterAttachment cluster = KongqiaoAttachments.getFlyingSwordCluster(
            player
        );
        if (cluster == null) {
            return new ClientboundClusterStatePayload(0, 0, List.of());
        }

        Set<UUID> activeSet = cluster.getActiveSwords();
        List<UUID> activeList = new ArrayList<>(activeSet.size());
        for (UUID uuid : activeSet) {
            if (uuid != null) {
                activeList.add(uuid);
            }
        }

        return new ClientboundClusterStatePayload(
            cluster.getCurrentLoad(),
            cluster.getMaxComputation(),
            activeList
        );
    }

    public static void syncToPlayer(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, createPayload(player));
    }
}
