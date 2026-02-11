package com.Kizunad.guzhenrenext.xianqiao.dimension;

import com.Kizunad.guzhenrenext.xianqiao.XianqiaoRegistries;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.block.Blocks;

/**
 * 仙窍维度专用虚空 ChunkGenerator。
 * <p>
 * 该生成器不写入任何地形方块，所有区块保持空气状态，
 * 用于后续由业务逻辑按需放置仙窍结构与内容。
 * </p>
 */
public class ApertureVoidChunkGenerator extends ChunkGenerator {

    /**
     * 维度最小 Y。
     */
    private static final int MIN_Y = -64;

    /**
     * 维度总高度。
     */
    private static final int GEN_DEPTH = 384;

    /**
     * 海平面高度。
     */
    private static final int SEA_LEVEL = 63;

    /**
     * 固定基础高度。
     */
    private static final int BASE_HEIGHT = 64;

    /**
     * Codec：仅序列化/反序列化生物群系来源。
     */
    public static final MapCodec<ApertureVoidChunkGenerator> CODEC =
        BiomeSource.CODEC.fieldOf("biome_source")
            .xmap(ApertureVoidChunkGenerator::new, generator -> generator.biomeSource);

    public ApertureVoidChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return XianqiaoRegistries.APERTURE_VOID.get();
    }

    @Override
    public void applyCarvers(
        WorldGenRegion region,
        long seed,
        RandomState randomState,
        BiomeManager biomeManager,
        StructureManager structureManager,
        ChunkAccess chunk,
        GenerationStep.Carving step
    ) {
        // 虚空维度不进行洞穴/峡谷等 carving。
    }

    @Override
    public void buildSurface(
        WorldGenRegion region,
        StructureManager structureManager,
        RandomState randomState,
        ChunkAccess chunk
    ) {
        // 虚空维度不构建地表。
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
        Blender blender,
        RandomState randomState,
        StructureManager structureManager,
        ChunkAccess chunk
    ) {
        // 保持区块为空气，直接返回。
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        // 虚空维度不放置任何地物特征。
    }

    @Override
    public int getBaseHeight(
        int x,
        int z,
        Heightmap.Types heightmapType,
        LevelHeightAccessor level,
        RandomState randomState
    ) {
        return BASE_HEIGHT;
    }

    @Override
    public NoiseColumn getBaseColumn(
        int x,
        int z,
        LevelHeightAccessor level,
        RandomState randomState
    ) {
        int minY = level.getMinBuildHeight();
        int columnHeight = level.getHeight();
        // Java 默认以 null 填充数组，随后统一填充空气方块。
        BlockState[] states = new BlockState[columnHeight];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState randomState, BlockPos blockPos) {
        // 虚空生成器无需额外调试信息。
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        // 虚空维度不执行原生刷怪。
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getGenDepth() {
        return GEN_DEPTH;
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }
}
