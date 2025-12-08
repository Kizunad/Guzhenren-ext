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

    public GuardEntityObjectiveDefinition {
        totalDurationSeconds = Math.max(10, totalDurationSeconds);
        prepareTimeSeconds = Math.max(0, prepareTimeSeconds);
        waveIntervalSeconds = Math.max(1, waveIntervalSeconds);
        spawnRadius = Math.max(5.0, spawnRadius);
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
    ) {}
}
