package com.Kizunad.guzhenrenext.xianqiao.tribulation;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptServiceContract;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.AscensionAttemptState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.TribulationRuntimeState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 灾劫系统 Tick 驱动器。
 * <p>
 * 在服务端每个 tick（Post）阶段：
 * 1) 扫描仙窍维度中的全部已分配仙窍；
 * 2) 对满足触发条件的仙窍启动灾劫；
 * 3) 推进对应灾劫状态机。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class TribulationTickHandler {

    /** 活跃灾劫管理器：每个 owner UUID 一份瞬态状态机。 */
    private static final Map<UUID, TribulationManager> ACTIVE_MANAGERS = new HashMap<>();

    private TribulationTickHandler() {
    }

    /**
     * 服务端 Tick 后处理入口。
     *
     * @param event 服务端 Tick 事件
     */
    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (level == null) {
            level = event.getServer().overworld();
            if (level == null) {
                return;
            }
        }

        ApertureWorldData worldData = ApertureWorldData.get(level);
        long currentGameTime = level.getGameTime();

        for (Map.Entry<UUID, ApertureInfo> entry : worldData.getAllApertures().entrySet()) {
            UUID owner = entry.getKey();
            ApertureInfo info = entry.getValue();
            if (info.isFrozen()) {
                continue;
            }

            AscensionAttemptState attemptState = worldData.getAscensionAttemptState(owner);
            if (!AscensionAttemptServiceContract.isCommittedConfirmationHandoffStage(attemptState.stage())) {
                ACTIVE_MANAGERS.remove(owner);
                worldData.clearTribulationRuntimeState(owner);
                continue;
            }

            TribulationManager manager = ACTIVE_MANAGERS.get(owner);
            if (manager == null) {
                TribulationRuntimeState runtimeState = worldData.getTribulationRuntimeState(owner);
                if (runtimeState != null) {
                    manager = TribulationManager.restoreFromRuntimeState(owner, runtimeState);
                    ACTIVE_MANAGERS.put(owner, manager);
                } else if (
                    attemptState.stage() == AscensionAttemptStage.CONFIRMED
                        && info.nextTribulationTick() <= currentGameTime
                ) {
                    TribulationManager created = new TribulationManager(owner);
                    created.startTribulation();
                    ACTIVE_MANAGERS.put(owner, created);
                    manager = created;
                    worldData.setTribulationRuntimeState(owner, created.snapshotRuntimeState());
                }
            }

            if (manager == null) {
                continue;
            }

            manager.tick(level, info);
            if (manager.isFinished()) {
                ACTIVE_MANAGERS.remove(owner);
                worldData.clearTribulationRuntimeState(owner);
            } else {
                worldData.setTribulationRuntimeState(owner, manager.snapshotRuntimeState());
            }
        }
    }
}
