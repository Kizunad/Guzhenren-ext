package com.Kizunad.guzhenrenext.xianqiao.farming;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.block.AlchemyFurnaceBlock;
import java.util.function.Supplier;
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
