package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.ai.interaction.NpcQuestState;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/**
 * NPC 任务面板助手：负责将任务注册表映射到实体自身的数据槽。
 */
public final class NpcTaskBoard {

    private static final int DEFAULT_SLOT_COUNT = 3;

    private NpcTaskBoard() {}

    public static List<TaskDefinition> collectTasks(CustomNpcEntity npc) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return Collections.emptyList();
        }
        NpcQuestState questState = mind.getQuestState();
        if (!questState.isQuestEnabled()) {
            return Collections.emptyList();
        }
        List<ResourceLocation> ids = questState.getTaskIds();
        TaskRegistry registry = TaskRegistry.getInstance();
        if (ids.isEmpty()) {
            List<ResourceLocation> defaults = registry.pickDefaultIds(
                DEFAULT_SLOT_COUNT
            );
            questState.ensureTaskIds(defaults);
            ids = questState.getTaskIds();
        }
        List<ResourceLocation> filtered = new ArrayList<>();
        for (ResourceLocation id : ids) {
            if (registry.get(id) != null) {
                filtered.add(id);
            }
        }
        if (filtered.size() != ids.size()) {
            questState.setTaskIds(filtered);
        }
        List<TaskDefinition> definitions = new ArrayList<>();
        for (ResourceLocation id : filtered) {
            TaskDefinition def = registry.get(id);
            if (def != null) {
                definitions.add(def);
            }
        }
        return definitions;
    }
}
