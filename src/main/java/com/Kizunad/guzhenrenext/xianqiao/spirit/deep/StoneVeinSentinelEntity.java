package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StoneVeinSentinelEntity extends SnowGolem {

    private static final int CONVERSION_INTERVAL_TICKS = 40;
    private static final int CONVERSION_CHANCE = 24;

    private int conversionTicker;

    public StoneVeinSentinelEntity(EntityType<? extends SnowGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        conversionTicker++;
        if (conversionTicker < CONVERSION_INTERVAL_TICKS) {
            return;
        }
        conversionTicker = 0;
        if (random.nextInt(CONVERSION_CHANCE) != 0) {
            return;
        }
        convertStoneUnderfoot();
    }

    public void forceConversionForTest() {
        if (!level().isClientSide()) {
            convertStoneUnderfoot();
        }
    }

    private void convertStoneUnderfoot() {
        BlockPos under = blockPosition().below();
        if (level().getBlockState(under).is(Blocks.STONE) || level().getBlockState(under).is(Blocks.COBBLESTONE)) {
            level().setBlockAndUpdate(under, Blocks.COAL_ORE.defaultBlockState());
        }
    }

}
