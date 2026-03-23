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

    public static final DeferredBlock<CropBlock> QING_YA_GRASS = registerShallowCrop("qing_ya_grass");
    public static final DeferredBlock<CropBlock> NING_XUE_GEN = registerShallowCrop("ning_xue_gen");
    public static final DeferredBlock<CropBlock> JU_YUAN_FLOWER = registerShallowCrop("ju_yuan_flower");
    public static final DeferredBlock<CropBlock> XI_SUI_VINE = registerShallowCrop("xi_sui_vine");
    public static final DeferredBlock<CropBlock> TIE_PI_BAMBOO = registerShallowCrop("tie_pi_bamboo");
    public static final DeferredBlock<CropBlock> HUO_LING_ZHI_MUSHROOM = registerShallowCrop("huo_ling_zhi_mushroom");
    public static final DeferredBlock<CropBlock> BING_XIN_GRASS = registerShallowCrop("bing_xin_grass");
    public static final DeferredBlock<CropBlock> HUAN_DU_MUSHROOM = registerShallowCrop("huan_du_mushroom");
    public static final DeferredBlock<CropBlock> YING_TAI_LICHEN = registerShallowCrop("ying_tai_lichen");
    public static final DeferredBlock<CropBlock> CI_VINE = registerShallowCrop("ci_vine");
    public static final DeferredBlock<CropBlock> JIAN_YE_GRASS = registerShallowCrop("jian_ye_grass");
    public static final DeferredBlock<CropBlock> CHEN_SHUI_LILY_PAD = registerShallowCrop("chen_shui_lily_pad");
    public static final DeferredBlock<CropBlock> DI_LONG_BERRY_BUSH = registerShallowCrop("di_long_berry_bush");
    public static final DeferredBlock<CropBlock> FENG_XIN_ZI = registerShallowCrop("feng_xin_zi");
    public static final DeferredBlock<CropBlock> LEI_GU_SAPLING = registerShallowCrop("lei_gu_sapling");
    public static final DeferredBlock<CropBlock> SHI_YIN_GRASS = registerShallowCrop("shi_yin_grass");
    public static final DeferredBlock<CropBlock> CHUN_YANG_FLOWER = registerShallowCrop("chun_yang_flower");
    public static final DeferredBlock<CropBlock> YAN_SHOU_COCOA = registerShallowCrop("yan_shou_cocoa");
    public static final DeferredBlock<CropBlock> WANG_YOU_GRASS = registerShallowCrop("wang_you_grass");
    public static final DeferredBlock<CropBlock> SHE_YAN_MELON_STEM = registerShallowCrop("she_yan_melon_stem");

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

    private static DeferredBlock<CropBlock> registerShallowCrop(String registryName) {
        return BLOCKS.register(
            registryName,
            () -> new CropBlock(BlockBehaviour.Properties.of()
                .sound(SoundType.CROP)
                .randomTicks()
                .noCollission()
                .instabreak())
        );
    }

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
