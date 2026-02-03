package com.Kizunad.guzhenrenext.bastion.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * 基地多方块结构蓝图定义。
 * <p>
 * 仅承载数据，不包含检测逻辑。
 * </p>
 */
public record BastionBlueprint(String id, String displayName, List<BlockEntry> entries) {

    /** 蓝图 CODEC，用于 JSON 序列化与网络同步。 */
    public static final Codec<BastionBlueprint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(BastionBlueprint::id),
        Codec.STRING.fieldOf("displayName").forGetter(BastionBlueprint::displayName),
        BlockEntry.CODEC.listOf().fieldOf("entries").forGetter(BastionBlueprint::entries)
    ).apply(instance, BastionBlueprint::new));

    /**
     * 单个方块定义。
     * <p>
     * 坐标为相对于结构中心的偏移。
     * </p>
     */
    public record BlockEntry(int x, int y, int z, Block block, boolean required) {

        /** 方块定义 CODEC。 */
        public static final Codec<BlockEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(BlockEntry::x),
            Codec.INT.fieldOf("y").forGetter(BlockEntry::y),
            Codec.INT.fieldOf("z").forGetter(BlockEntry::z),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockEntry::block),
            Codec.BOOL.fieldOf("required").forGetter(BlockEntry::required)
        ).apply(instance, BlockEntry::new));
    }
}
