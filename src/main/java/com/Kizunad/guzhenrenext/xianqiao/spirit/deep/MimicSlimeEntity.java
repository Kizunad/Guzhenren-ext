package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class MimicSlimeEntity extends Slime {

    private static final int SCAN_INTERVAL_TICKS = 20;

    private int scanTicker;
    private String elementTag = "neutral";

    public MimicSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        scanTicker++;
        if (scanTicker < SCAN_INTERVAL_TICKS) {
            return;
        }
        scanTicker = 0;
        scanElementFromBlockBelow();
    }

    public void forceElementScanForTest() {
        if (!level().isClientSide()) {
            scanElementFromBlockBelow();
        }
    }

    public String getElementTag() {
        return elementTag;
    }

    private void updateElementTag(String newTag) {
        if (elementTag.equals(newTag)) {
            return;
        }
        elementTag = newTag;
        setCustomName(net.minecraft.network.chat.Component.literal("拟态史莱姆[" + newTag + "]"));
        setCustomNameVisible(true);
    }

    private void scanElementFromBlockBelow() {
        Block blockBelow = level().getBlockState(blockPosition().below()).getBlock();
        if (blockBelow == Blocks.LAVA || blockBelow == Blocks.MAGMA_BLOCK) {
            updateElementTag("fire");
        } else if (blockBelow == Blocks.WATER || blockBelow == Blocks.ICE) {
            updateElementTag("water");
        } else if (blockBelow == Blocks.GOLD_BLOCK || blockBelow == Blocks.IRON_BLOCK) {
            updateElementTag("metal");
        } else {
            updateElementTag("neutral");
        }
    }
}
