package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StoneVeinSentinelEntity extends SnowGolem {

    private static final int CONVERSION_INTERVAL_TICKS = 40;
    private static final int CONVERSION_CHANCE = 24;
    private static final int COLLAPSE_OFFSET_X = 1;

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
        if (!isConvertibleStone(under)) {
            return;
        }
        level().setBlockAndUpdate(under, Blocks.COAL_ORE.defaultBlockState());
        triggerVeinCollapseRisk(under);
        spawnAtLocation(XianqiaoItems.DI_MAI_LONG_JING.get());
    }

    private boolean isConvertibleStone(BlockPos targetPos) {
        return level().getBlockState(targetPos).is(Blocks.STONE)
            || level().getBlockState(targetPos).is(Blocks.COBBLESTONE);
    }

    private void triggerVeinCollapseRisk(BlockPos convertedCorePos) {
        BlockPos collapsePos = convertedCorePos.offset(COLLAPSE_OFFSET_X, 0, 0);
        if (isConvertibleStone(collapsePos)) {
            level().setBlockAndUpdate(collapsePos, Blocks.GRAVEL.defaultBlockState());
        }
    }

}
