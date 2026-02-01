package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import com.Kizunad.guzhenrenext.bastion.block.BastionAntiExplosionShellBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * 反爆外壳服务：为拥有反爆节点的基地提供爆炸免疫/减伤。
 * <p>
 * 通过监听 ExplosionEvent.Detonate，在爆炸生效前移除基地范围内的方块损坏。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionAntiExplosionService {

    /**
     * 爆炸归属搜索半径兜底值。
     * <p>
     * 选择 512 以覆盖高转/扩张后的基地范围，避免因半径不足漏掉归属。
     * </p>
     */
    private static final int SEARCH_RADIUS = 512;

    private BastionAntiExplosionService() {
        // 工具类
    }

    /**
     * 爆炸生效前移除拥有反爆节点的基地内的方块损坏。
     */
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        List<BlockPos> affected = event.getAffectedBlocks();

        if (affected.isEmpty()) {
            return;
        }

        // 逐个方块判断所属基地与反爆节点。
        Map<UUID, Boolean> antiExplosionCache = new HashMap<>();
        Iterator<BlockPos> iterator = affected.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            BastionData bastion = savedData.findOwnerBastion(pos, SEARCH_RADIUS);
            if (bastion == null || bastion.state() != BastionState.ACTIVE) {
                continue;
            }

            // 检查基地是否有反爆节点（包含配置开关）。
            boolean hasShield = antiExplosionCache.computeIfAbsent(
                bastion.id(),
                id -> hasAntiExplosionShell(level, savedData, bastion)
            );
            if (!hasShield) {
                continue;
            }

            int auraRadius = bastion.getAuraRadius();
            if (auraRadius <= 0) {
                continue;
            }
            long radiusSq = (long) auraRadius * auraRadius;
            double distSq = pos.distSqr(bastion.corePos());
            if (distSq <= (double) radiusSq) {
                // 在基地范围内：移除爆炸对该方块的影响。
                iterator.remove();
            }
        }
    }

    private static boolean hasAntiExplosionShell(ServerLevel level, BastionSavedData savedData, BastionData bastion) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.AntiExplosionShellConfig config = typeConfig.antiExplosionShell();
        if (config == null || !config.enabled()) {
            return false;
        }

        java.util.Set<BlockPos> anchors = savedData.getAnchorCache(bastion.id());
        if (anchors == null || anchors.isEmpty()) {
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (BlockPos anchor : anchors) {
            // 反爆节点挂载在 Anchor 上方一格。
            cursor.set(anchor.getX(), anchor.getY() + 1, anchor.getZ());
            Block block = level.getBlockState(cursor).getBlock();
            if (block instanceof BastionAntiExplosionShellBlock) {
                return true;
            }
        }
        return false;
    }
}
