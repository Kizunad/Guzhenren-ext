package com.Kizunad.guzhenrenext.customNPCImpl.lifecycle;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.registry.NpcSpawnRegistry;
import javax.annotation.Nullable;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * 在自定义 NPC 生成时补齐蛊真人模组的关键属性，避免初始化缺失导致能力异常。
 */
public final class NpcSpawnInitializer {

    private static final double DEFAULT_RESOURCE_VALUE = 1000.0D;
    private static final double DEFAULT_STAGE_VALUE = 1.0D;

    private static boolean registered;

    private NpcSpawnInitializer() {}

    public static void register() {
        if (registered) {
            return;
        }
        NpcSpawnRegistry.register(NpcSpawnInitializer::initializeVariables);
        registered = true;
    }

    private static void initializeVariables(
        CustomNpcEntity npc,
        ServerLevelAccessor level,
        DifficultyInstance difficulty,
        MobSpawnType reason,
        @Nullable SpawnGroupData spawnData
    ) {
        try {
            GuzhenrenModVariables.PlayerVariables variables = npc.getData(
                GuzhenrenModVariables.PLAYER_VARIABLES
            );

            // 五大基础资源：全部初始化为 100，避免缺失导致蛊真人能力不可用
            variables.hunpo = DEFAULT_RESOURCE_VALUE;
            variables.zuida_hunpo = DEFAULT_RESOURCE_VALUE;
            variables.jingli = DEFAULT_RESOURCE_VALUE;
            variables.zuida_jingli = DEFAULT_RESOURCE_VALUE;
            variables.zhenyuan = DEFAULT_RESOURCE_VALUE;
            variables.zuida_zhenyuan = DEFAULT_RESOURCE_VALUE;
            variables.niantou = DEFAULT_RESOURCE_VALUE;
            variables.niantou_rongliang = DEFAULT_RESOURCE_VALUE;
            variables.niantou_zhida = DEFAULT_RESOURCE_VALUE;
            variables.shouyuan = DEFAULT_RESOURCE_VALUE;

            // 转数/阶段：默认为一转初阶
            variables.zhuanshu = DEFAULT_STAGE_VALUE;
            variables.jieduan = DEFAULT_STAGE_VALUE;

            variables.markSyncDirty();
        } catch (Exception ignored) {
            // 读取/写入失败时直接跳过，避免阻塞实体生成
        }
    }
}
