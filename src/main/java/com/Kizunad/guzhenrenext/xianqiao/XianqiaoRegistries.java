package com.Kizunad.guzhenrenext.xianqiao;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.daomark.XianqiaoAttachments;
import com.Kizunad.guzhenrenext.xianqiao.dimension.ApertureVoidChunkGenerator;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoDataComponents;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoBlockEntities;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 集中管理仙窍系统的所有 DeferredRegister。
 */
public final class XianqiaoRegistries {

    private XianqiaoRegistries() {
    }

    /**
     * 仙窍虚空生成器类型注册表。
     */
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, GuzhenrenExt.MODID);

    /**
     * 仙窍虚空 ChunkGenerator 的 codec 注册项。
     */
    public static final DeferredHolder<
        MapCodec<? extends ChunkGenerator>,
        MapCodec<ApertureVoidChunkGenerator>
    > APERTURE_VOID = CHUNK_GENERATORS.register("aperture_void", () -> ApertureVoidChunkGenerator.CODEC);

    /**
     * 注册仙窍系统全部注册表。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
        XianqiaoAttachments.register(modEventBus);
        XianqiaoBlocks.register(modEventBus);
        FarmingBlocks.register(modEventBus);
        FarmingItems.register(modEventBus);
        XianqiaoBlockEntities.register(modEventBus);
        XianqiaoEntities.register(modEventBus);
        XianqiaoMenus.register(modEventBus);
        XianqiaoItems.register(modEventBus);
        XianqiaoDataComponents.register(modEventBus);
        XianqiaoCreativeTab.register(modEventBus);
    }
}
