package com.Kizunad.customNPCs.registry;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * NPC 生成时的回调注册表。
 * 允许外部模块在 NPC 初始化生成时（finalizeSpawn）注入逻辑，例如给予装备、调整属性等。
 */
public class NpcSpawnRegistry {

    @FunctionalInterface
    public interface SpawnHandler {
        void handle(
            CustomNpcEntity npc,
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData
        );
    }

    private static final List<SpawnHandler> HANDLERS = new ArrayList<>();

    /**
     * 注册一个生成处理器。
     * @param handler 处理器逻辑
     */
    public static void register(SpawnHandler handler) {
        HANDLERS.add(handler);
    }

    /**
     * 执行所有注册的处理器（内部调用）。
     */
    public static void onSpawn(
        CustomNpcEntity npc,
        ServerLevelAccessor level,
        DifficultyInstance difficulty,
        MobSpawnType reason,
        @Nullable SpawnGroupData spawnData
    ) {
        for (SpawnHandler handler : HANDLERS) {
            handler.handle(npc, level, difficulty, reason, spawnData);
        }
    }
}
