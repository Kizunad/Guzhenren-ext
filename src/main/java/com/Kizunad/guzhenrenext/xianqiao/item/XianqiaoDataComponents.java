package com.Kizunad.guzhenrenext.xianqiao.item;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 仙窍系统自定义数据组件注册表。
 */
public final class XianqiaoDataComponents {

    private XianqiaoDataComponents() {
    }

    /**
     * DataComponentType 延迟注册器。
     */
    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(GuzhenrenExt.MODID);

    /**
     * 九天碎片关联的结构碎片 ID。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> FRAGMENT_ID =
        DATA_COMPONENTS.registerComponentType(
            "fragment_id",
            builder -> builder
                .persistent(Codec.STRING)
                .networkSynchronized(ByteBufCodecs.STRING_UTF8)
        );

    /**
     * 九天碎片中的道痕摘要 NBT。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> DAO_MARK_SUMMARY =
        DATA_COMPONENTS.registerComponentType(
            "dao_mark_summary",
            builder -> builder
                .persistent(CompoundTag.CODEC)
                .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
        );

    /**
     * 九天碎片获取时刻（GameTime）。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> ACQUIRE_TIME =
        DATA_COMPONENTS.registerComponentType(
            "acquire_time",
            builder -> builder
                .persistent(Codec.LONG)
                .networkSynchronized(StreamCodec.of(ByteBuf::writeLong, ByteBuf::readLong))
        );

    /**
     * 注册数据组件。
     *
     * @param modEventBus 模组事件总线
     */
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
