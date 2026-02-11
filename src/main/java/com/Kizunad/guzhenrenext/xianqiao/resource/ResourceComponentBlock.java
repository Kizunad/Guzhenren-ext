package com.Kizunad.guzhenrenext.xianqiao.resource;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 资源点组件方块。
 * <p>
 * 该方块本身不含逻辑，仅用于被资源控制器统计数量，
 * 作为多方块结构的功能组件。
 * </p>
 */
public class ResourceComponentBlock extends Block {

    /** 方块硬度。 */
    private static final float BLOCK_STRENGTH = 2.0F;

    /** 爆炸抗性。 */
    private static final float BLOCK_RESISTANCE = 6.0F;

    public ResourceComponentBlock() {
        super(
            BlockBehaviour.Properties.of()
                .strength(BLOCK_STRENGTH, BLOCK_RESISTANCE)
                .sound(SoundType.STONE)
        );
    }
}
