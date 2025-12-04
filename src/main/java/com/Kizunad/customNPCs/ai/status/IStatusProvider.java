package com.Kizunad.customNPCs.ai.status;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.network.dto.NpcStatusEntry;
import java.util.List;

/**
 * 状态面板数据提供接口。
 * <p>
 * 由不同模块实现, 将各自的状态行转换为 NpcStatusEntry 供 UI 展示。
 * </p>
 */
@FunctionalInterface
public interface IStatusProvider {

    /**
     * 收集指定 NPC 的状态数据。
     *
     * @param npc 目标 NPC
     * @return 状态行列表, 不应返回 null
     */
    List<NpcStatusEntry> collectStatuses(CustomNpcEntity npc);
}
