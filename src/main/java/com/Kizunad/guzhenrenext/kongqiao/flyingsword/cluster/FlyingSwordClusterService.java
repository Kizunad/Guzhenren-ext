package com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordSpawner;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordClusterAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class FlyingSwordClusterService {

    private FlyingSwordClusterService() {}

    private static final int BASE_COMPUTATION = 10;
    private static final double HUN_DAO_WEIGHT = 0.1D;
    private static final double ZHI_DAO_WEIGHT = 0.2D;
    private static final int BASE_COST = 1;
    private static final int TIER_COST_WEIGHT = 2;

    public static int calculateMaxComputation(ServerPlayer player) {
        if (player == null) {
            return 0;
        }
        double hun = DaoHenHelper.getDaoHen(player, DaoHenHelper.DaoType.HUN_DAO);
        double zhi = DaoHenHelper.getDaoHen(player, DaoHenHelper.DaoType.ZHI_DAO);
        double value = BASE_COMPUTATION + (hun * HUN_DAO_WEIGHT) + (zhi * ZHI_DAO_WEIGHT);
        return Math.max(0, (int) Math.floor(value));
    }

    public static int calculateCost(FlyingSwordEntity sword) {
        if (sword == null) {
            return BASE_COST;
        }
        return calculateCostByQuality(sword.getQuality());
    }

    public static boolean deploy(ServerPlayer player, UUID uuid) {
        if (player == null || uuid == null) {
            return false;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }

        FlyingSwordClusterAttachment cluster = KongqiaoAttachments.getFlyingSwordCluster(player);
        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(player);
        if (cluster == null || storage == null) {
            return false;
        }

        if (cluster.hasActiveSword(uuid)) {
            return false;
        }

        FlyingSwordEntity existed = getOwnedSwordByUuid(level, player, uuid);
        if (existed != null && !existed.isRemoved()) {
            int existedCost = calculateCost(existed);
            int maxComputation = calculateMaxComputation(player);
            if (cluster.getCurrentLoad() + existedCost > maxComputation) {
                cluster.setMaxComputation(maxComputation);
                return false;
            }
            cluster.addActiveSword(uuid);
            cluster.setMaxComputation(maxComputation);
            cluster.setCurrentLoad(cluster.getCurrentLoad() + existedCost);
            return true;
        }

        int targetIndex = findStorageIndexByUuid(storage, uuid);
        if (targetIndex < 0) {
            return false;
        }

        FlyingSwordStorageAttachment.RecalledSword recalled = storage.getAt(targetIndex);
        if (recalled == null || recalled.itemWithdrawn) {
            storage.remove(targetIndex);
            return false;
        }

        int maxComputation = calculateMaxComputation(player);
        int swordCost = calculateCostByQuality(recalled.quality);
        if (cluster.getCurrentLoad() + swordCost > maxComputation) {
            cluster.setMaxComputation(maxComputation);
            return false;
        }

        FlyingSwordEntity deployed = FlyingSwordSpawner.restoreFromStorage(level, player, recalled);
        if (deployed == null) {
            return false;
        }

        storage.remove(targetIndex);
        cluster.addActiveSword(deployed.getUUID());
        cluster.setMaxComputation(maxComputation);
        cluster.setCurrentLoad(cluster.getCurrentLoad() + swordCost);
        return true;
    }

    public static boolean recall(ServerPlayer player, UUID uuid) {
        if (player == null || uuid == null) {
            return false;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }

        FlyingSwordClusterAttachment cluster = KongqiaoAttachments.getFlyingSwordCluster(player);
        if (cluster == null) {
            return false;
        }

        FlyingSwordEntity sword = getOwnedSwordByUuid(level, player, uuid);
        if (sword == null || sword.isRemoved()) {
            boolean removed = cluster.removeActiveSword(uuid);
            cluster.setMaxComputation(calculateMaxComputation(player));
            cluster.setCurrentLoad(recalculateLoad(level, player, cluster));
            return removed;
        }

        FlyingSwordController.finishRecall(sword, player);
        cluster.removeActiveSword(uuid);
        cluster.setMaxComputation(calculateMaxComputation(player));
        cluster.setCurrentLoad(recalculateLoad(level, player, cluster));
        return true;
    }

    private static int calculateCostByQuality(SwordQuality quality) {
        if (quality == null) {
            return BASE_COST;
        }
        return BASE_COST + (quality.getTier() * TIER_COST_WEIGHT);
    }

    private static int findStorageIndexByUuid(
        FlyingSwordStorageAttachment storage,
        UUID uuid
    ) {
        if (storage == null || uuid == null) {
            return -1;
        }
        String target = uuid.toString();
        for (int index = 0; index < storage.getCount(); index++) {
            FlyingSwordStorageAttachment.RecalledSword recalled = storage.getAt(index);
            if (recalled == null) {
                continue;
            }
            if (target.equals(recalled.displayItemUUID)) {
                return index;
            }
        }
        return -1;
    }

    private static FlyingSwordEntity getOwnedSwordByUuid(
        ServerLevel level,
        ServerPlayer player,
        UUID uuid
    ) {
        if (level == null || player == null || uuid == null) {
            return null;
        }
        List<FlyingSwordEntity> swords = FlyingSwordController.getPlayerSwords(level, player);
        for (FlyingSwordEntity sword : swords) {
            if (sword != null && uuid.equals(sword.getUUID())) {
                return sword;
            }
        }
        return null;
    }

    private static int recalculateLoad(
        ServerLevel level,
        ServerPlayer player,
        FlyingSwordClusterAttachment cluster
    ) {
        if (level == null || player == null || cluster == null) {
            return 0;
        }
        int total = 0;
        List<FlyingSwordEntity> swords = FlyingSwordController.getPlayerSwords(level, player);
        for (FlyingSwordEntity sword : swords) {
            if (sword == null) {
                continue;
            }
            UUID swordUuid = sword.getUUID();
            if (!cluster.hasActiveSword(swordUuid)) {
                continue;
            }
            total += calculateCost(sword);
        }
        return Math.max(0, total);
    }
}
