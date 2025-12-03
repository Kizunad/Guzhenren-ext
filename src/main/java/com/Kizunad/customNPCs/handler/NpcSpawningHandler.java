package com.Kizunad.customNPCs.handler;

import com.Kizunad.customNPCs.config.SpawnConfig;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.registry.NpcSpawnRegistry;
import com.Kizunad.customNPCs.util.NamePool;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NpcSpawningHandler {

    private static final Set<UUID> ACTIVE_NPCS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        // 注册生成时的初始化逻辑（名字等）
        NpcSpawnRegistry.register((npc, level, difficulty, reason, spawnData) -> {
            // 仅对自然生成的 NPC 应用随机命名，或者全量应用？通常全量应用比较好，除非已有名字
            if (!npc.hasCustomName()) {
                String name = NamePool.getRandomName();
                npc.setCustomName(Component.literal(name));
            }
        });
    }

    @SubscribeEvent
    public void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof CustomNpcEntity) {
            MobSpawnType type = event.getSpawnType();
            // 限制自然生成
            if (type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION) {
                SpawnConfig config = SpawnConfig.getInstance();
                if (!config.isNaturalSpawnEnabled()) {
                    event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
                    return;
                }

                // 检查当前活跃数量
                if (ACTIVE_NPCS.size() >= config.getMaxNaturalSpawns()) {
                    event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof CustomNpcEntity npc) {
            if (!event.getLevel().isClientSide()) {
                ACTIVE_NPCS.add(npc.getUUID());
            }
        }
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof CustomNpcEntity npc) {
            if (!event.getLevel().isClientSide()) {
                ACTIVE_NPCS.remove(npc.getUUID());
            }
        }
    }
}
