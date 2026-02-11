package com.Kizunad.guzhenrenext.xianqiao.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 仙窍核心方块。
 * <p>
 * 该方块用于标记仙窍中心，强度与基岩一致，不可正常破坏。
 * </p>
 */
public class ApertureCoreBlock extends Block {

    /**
     * 不可破坏硬度值。
     */
    private static final float UNBREAKABLE_DESTROY_TIME = -1.0F;

    /**
     * 基岩级爆炸抗性。
     */
    private static final float BEDROCK_EXPLOSION_RESISTANCE = 3600000.0F;

    public ApertureCoreBlock() {
        super(
            BlockBehaviour.Properties.of()
                .destroyTime(UNBREAKABLE_DESTROY_TIME)
                .explosionResistance(BEDROCK_EXPLOSION_RESISTANCE)
        );
    }
}
