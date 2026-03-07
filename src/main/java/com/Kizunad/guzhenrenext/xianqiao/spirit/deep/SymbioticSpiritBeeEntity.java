package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SymbioticSpiritBeeEntity extends Bee {

    private static final int POLLINATION_INTERVAL_TICKS = 30;
    private static final int POLLINATION_RADIUS = 5;

    private int pollinationTicker;
    private int deepInteractionCount;

    public SymbioticSpiritBeeEntity(EntityType<? extends Bee> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        pollinationTicker++;
        if (pollinationTicker < POLLINATION_INTERVAL_TICKS) {
            return;
        }
        pollinationTicker = 0;
        scanDeepPlantsOnce();
    }

    public int getDeepInteractionCount() {
        return deepInteractionCount;
    }

    public void forceDeepInteractionScanForTest() {
        if (!level().isClientSide()) {
            scanDeepPlantsOnce();
        }
    }

    private boolean isDeepPlant(Block block) {
        return block == Blocks.SPORE_BLOSSOM
            || block == Blocks.FERN
            || block == Blocks.WITHER_ROSE
            || block == Blocks.PEONY
            || block == Blocks.CHORUS_FLOWER
            || block == Blocks.BAMBOO
            || block == Blocks.TORCHFLOWER;
    }

    private void scanDeepPlantsOnce() {
        BlockPos center = blockPosition();
        for (int x = -POLLINATION_RADIUS; x <= POLLINATION_RADIUS; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -POLLINATION_RADIUS; z <= POLLINATION_RADIUS; z++) {
                    BlockPos target = center.offset(x, y, z);
                    if (isDeepPlant(level().getBlockState(target).getBlock())) {
                        deepInteractionCount++;
                        return;
                    }
                }
            }
        }
    }
}
