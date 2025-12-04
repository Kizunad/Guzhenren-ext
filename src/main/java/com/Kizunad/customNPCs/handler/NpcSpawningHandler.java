package com.Kizunad.customNPCs.handler;

import com.Kizunad.customNPCs.config.SpawnConfig;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.entity.ModEntities;
import com.Kizunad.customNPCs.registry.NpcSpawnRegistry;
import com.Kizunad.customNPCs.util.NamePool;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class NpcSpawningHandler {

    private static final Set<UUID> ACTIVE_NPCS = Collections.newSetFromMap(
        new ConcurrentHashMap<>()
    );
    private static final int MIN_SPAWN_X = -100;
    private static final int MAX_SPAWN_X = 100;
    private static final int MIN_SPAWN_Z = -100;
    private static final int MAX_SPAWN_Z = 100;
    private static final int SPAWN_PER_TICK = 1;
    private static final double SPAWN_CENTER_OFFSET = 0.5D;
    private static final float FULL_ROTATION = 360.0F;

    static {
        // 注册生成时的初始化逻辑（名字等）
        NpcSpawnRegistry.register(
            (npc, level, difficulty, reason, spawnData) -> {
                // 仅对自然生成的 NPC 应用随机命名，或者全量应用？通常全量应用比较好，除非已有名字
                if (!npc.hasCustomName()) {
                    String name = NamePool.getRandomName();
                    npc.setCustomName(Component.literal(name));
                }
            }
        );
    }

    @SubscribeEvent
    public void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof CustomNpcEntity) {
            MobSpawnType type = event.getSpawnType();
            // 限制自然生成
            if (
                type == MobSpawnType.NATURAL ||
                type == MobSpawnType.CHUNK_GENERATION
            ) {
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
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }
        SpawnConfig config = SpawnConfig.getInstance();
        if (!config.isNaturalSpawnEnabled()) {
            return;
        }
        int maxAllowed = config.getMaxNaturalSpawns();
        if (maxAllowed <= 0) {
            return;
        }
        if (ACTIVE_NPCS.size() >= maxAllowed) {
            return;
        }
        int need = Math.min(SPAWN_PER_TICK, maxAllowed - ACTIVE_NPCS.size());
        spawnNpcs(level, need);
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

    private void spawnNpcs(ServerLevel level, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos pos = pickGroundPos(level);
            if (pos == null) {
                continue;
            }
            var npc = ModEntities.CUSTOM_NPC.get().create(level);
            if (npc == null) {
                continue;
            }
            npc.moveTo(
                pos.getX() + SPAWN_CENTER_OFFSET,
                pos.getY(),
                pos.getZ() + SPAWN_CENTER_OFFSET,
                level.random.nextFloat() * FULL_ROTATION,
                0.0F
            );
            DifficultyInstance difficulty = level.getCurrentDifficultyAt(pos);
            npc.finalizeSpawn(level, difficulty, MobSpawnType.NATURAL, null);
            npc.setPersistenceRequired();
            level.addFreshEntity(npc);
        }
    }

    private BlockPos pickGroundPos(ServerLevel level) {
        int xRange = MAX_SPAWN_X - MIN_SPAWN_X + 1;
        int zRange = MAX_SPAWN_Z - MIN_SPAWN_Z + 1;
        int x = level.random.nextInt(xRange) + MIN_SPAWN_X;
        int z = level.random.nextInt(zRange) + MIN_SPAWN_Z;
        BlockPos surface = level.getHeightmapPos(
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            new BlockPos(x, level.getMinBuildHeight(), z)
        );
        BlockState state = level.getBlockState(surface.below());
        // 仅在有方块支撑时生成，避免空中/流体刷出
        if (state.isAir()) {
            return null;
        }
        return surface;
    }
}
