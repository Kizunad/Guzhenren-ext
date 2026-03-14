package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.block.AlchemyFurnaceBlock;
import java.util.function.Supplier;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 农耕体系方块注册表。
 */
public final class FarmingBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GuzhenrenExt.MODID);

    /** 炼丹炉方块。 */
    public static final DeferredBlock<AlchemyFurnaceBlock> ALCHEMY_FURNACE =
        BLOCKS.register("alchemy_furnace", (Supplier<AlchemyFurnaceBlock>) AlchemyFurnaceBlock::new);

    /** 青亚草作物。 */
    public static final DeferredBlock<CropBlock> QING_YA_GRASS =
        BLOCKS.register(
            "qing_ya_grass",
            () -> new CropBlock(BlockBehaviour.Properties.of()
                .sound(SoundType.CROP)
                .randomTicks()
                .noCollission()
                .instabreak())
        );

    public static final DeferredBlock<LightningAttractingFernBlock> LIGHTNING_ATTRACTING_FERN =
        BLOCKS.register(
            "lightning_attracting_fern",
            () -> new LightningAttractingFernBlock(
                BlockBehaviour.Properties.of()
                    .sound(SoundType.GRASS)
                    .randomTicks()
                    .noCollission()
                    .instabreak()
                    .offsetType(BlockBehaviour.OffsetType.XYZ)
            )
        );

    public static final DeferredBlock<ManEatingSporeBlossomBlock> MAN_EATING_SPORE_BLOSSOM =
        BLOCKS.register(
            "man_eating_spore_blossom",
            () -> new ManEatingSporeBlossomBlock(
                BlockBehaviour.Properties.of()
                    .sound(SoundType.SPORE_BLOSSOM)
                    .randomTicks()
                    .noCollission()
                    .instabreak()
            )
        );

    public static final DeferredBlock<SpiritGatheringTreeBlock> SPIRIT_GATHERING_TREE =
        BLOCKS.register(
            "spirit_tree_spruce_sapling",
            () -> new SpiritGatheringTreeBlock(
                BlockBehaviour.Properties.of()
                    .sound(SoundType.GRASS)
                    .noCollission()
                    .instabreak()
            )
        );

    public static final DeferredBlock<CaveVinesBlock> CAVE_VINES =
        BLOCKS.register(
            "cave_vines",
            () -> new OreDevouringCaveVinesBlock(
                BlockBehaviour.Properties.of()
                    .sound(SoundType.CAVE_VINES)
                    .randomTicks()
                    .noCollission()
                    .instabreak()
            )
        );

    private FarmingBlocks() {
    }

    /**
     * 注册农耕体系方块。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
