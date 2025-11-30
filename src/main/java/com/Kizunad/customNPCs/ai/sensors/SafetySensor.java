package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 环境安全传感器 - 识别附近危险区域（岩浆/火/荆棘等）并触发规避。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class SafetySensor implements ISensor {

    private static final int MEMORY_DURATION = 100;
    private static final int SCAN_INTERVAL = 5;
    private static final double SCAN_RADIUS = 2.5d;
    private static final double CRITICAL_DISTANCE = 1.5d;
    private static final int SENSOR_PRIORITY = 90;
    private static final int CRITICAL_WINDOW_TICKS = 10;
    private static final int IMPORTANT_WINDOW_TICKS = 25;
    private static final int INFO_WINDOW_TICKS = 25;
    private final InterruptThrottle interruptThrottle = new InterruptThrottle(
        CRITICAL_WINDOW_TICKS,
        IMPORTANT_WINDOW_TICKS,
        INFO_WINDOW_TICKS
    );

    @Override
    public String getName() {
        return "safety_sensor";
    }

    @Override
    public void sense(INpcMind mind, LivingEntity entity, ServerLevel level) {
        BlockPos center = entity.blockPosition();
        AABB scanBox = new AABB(center).inflate(SCAN_RADIUS, 1.0d, SCAN_RADIUS);

        boolean hasHazard = entity.isOnFire() || entity.isInLava();
        BlockPos nearestHazard = hasHazard ? center : null;
        double nearestDistanceSqr = hasHazard ? 0.0d : Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
            BlockPos.containing(scanBox.minX, scanBox.minY, scanBox.minZ),
            BlockPos.containing(scanBox.maxX, scanBox.maxY, scanBox.maxZ)
        )) {
            BlockState state = level.getBlockState(pos);
            if (!isHazard(state)) {
                continue;
            }

            double distanceSqr = pos.distToCenterSqr(entity.position());
            if (distanceSqr < nearestDistanceSqr) {
                nearestDistanceSqr = distanceSqr;
                nearestHazard = pos.immutable();
            }
            hasHazard = true;
        }

        updateMemory(mind, hasHazard, nearestHazard, nearestDistanceSqr);

        if (!hasHazard) {
            return;
        }

        int distanceBucket =
            nearestDistanceSqr <= CRITICAL_DISTANCE * CRITICAL_DISTANCE ? 0 : 1;
        SensorEventType eventType =
            distanceBucket == 0 ? SensorEventType.CRITICAL : SensorEventType.IMPORTANT;

        if (
            interruptThrottle.allowInterrupt(
                null,
                eventType,
                distanceBucket,
                level.getGameTime()
            )
        ) {
            mind.triggerInterrupt(entity, eventType);
        }
    }

    @Override
    public boolean shouldSense(long tickCount) {
        return tickCount % SCAN_INTERVAL == 0;
    }

    @Override
    public int getPriority() {
        return SENSOR_PRIORITY;
    }

    private void updateMemory(
        INpcMind mind,
        boolean hasHazard,
        BlockPos nearestHazard,
        double nearestDistanceSqr
    ) {
        if (hasHazard) {
            mind
                .getMemory()
                .rememberShortTerm("hazard_detected", true, MEMORY_DURATION);
            mind
                .getMemory()
                .rememberShortTerm(
                    "nearest_hazard_pos",
                    nearestHazard,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    "nearest_hazard_distance",
                    Math.sqrt(nearestDistanceSqr),
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.IN_DANGER,
                    true,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.HAZARD_NEARBY,
                    true,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.IN_DANGER,
                    true,
                    MEMORY_DURATION
                );
        } else {
            mind.getMemory().forget("hazard_detected");
            mind.getMemory().forget("nearest_hazard_pos");
            mind.getMemory().forget("nearest_hazard_distance");
            mind.getMemory().forget(WorldStateKeys.HAZARD_NEARBY);
            mind.getMemory().forget(WorldStateKeys.IN_DANGER);
        }
    }

    private boolean isHazard(BlockState state) {
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return true;
        }
        if (state.is(BlockTags.FIRE)) {
            return true;
        }
        if (
            state.is(BlockTags.CAMPFIRES) && CampfireBlock.isLitCampfire(state)
        ) {
            return true;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        if (state.is(Blocks.CACTUS)) {
            return true;
        }
        if (state.is(Blocks.POWDER_SNOW)) {
            return true;
        }
        if (state.getBlock() instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) > 0;
        }
        return false;
    }
}
