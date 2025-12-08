package com.Kizunad.customNPCs.tasks.objective;

import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * “守卫实体”目标定义。
 * 玩家需要在规定时间内保护指定实体不死亡。
 */
public record GuardEntityObjectiveDefinition(
    GuardTargetType targetType,
    @Nullable EntityType<? extends LivingEntity> entityToSpawn,
    int totalDurationSeconds,
    int prepareTimeSeconds,
    int waveIntervalSeconds,
    List<AttackerEntry> attackers,
    double spawnRadius
) implements TaskObjectiveDefinition {

    public static final int MIN_TOTAL_DURATION_SECONDS = 10;
    public static final int MIN_WAVE_INTERVAL_SECONDS = 1;
    public static final double MIN_SPAWN_RADIUS = 5.0D;

    public GuardEntityObjectiveDefinition {
        totalDurationSeconds = Math.max(
            MIN_TOTAL_DURATION_SECONDS,
            totalDurationSeconds
        );
        prepareTimeSeconds = Math.max(0, prepareTimeSeconds);
        waveIntervalSeconds = Math.max(MIN_WAVE_INTERVAL_SECONDS, waveIntervalSeconds);
        spawnRadius = Math.max(MIN_SPAWN_RADIUS, spawnRadius);
        if (attackers == null) {
            attackers = Collections.emptyList();
        } else {
            attackers = Collections.unmodifiableList(attackers);
        }
    }

    @Override
    public TaskObjectiveType getType() {
        return TaskObjectiveType.GUARD_ENTITY;
    }

    public enum GuardTargetType {
        SELF,   // 保护任务发布者自己
        SPAWN   // 生成一个新的实体进行保护
    }

    /**
     * 定义进攻的怪物
     */
    public record AttackerEntry(
        EntityType<? extends LivingEntity> entityType,
        int minCount,
        int maxCount,
        @Nullable CompoundTag nbt
    ) {

        public AttackerEntry {
            minCount = Math.max(1, minCount);
            maxCount = Math.max(minCount, maxCount);
        }
    }
}
