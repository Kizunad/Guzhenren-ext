package com.Kizunad.guzhenrenext.xianqiao.service;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

/**
 * 主世界地形粗采样工具。
 * <p>
 * 本工具仅复制方块状态（BlockState），不会复制 BlockEntity NBT、实体、计划刻与光照数据。
 * 其核心用途是在仙窍初始化等场景中，从主世界提取一块 16x16 的地表切片并投放到目标维度。
 * </p>
 */
public final class OverworldTerrainSampler {

    /** 单个采样块在 X/Z 方向的边长（一个 chunk）。 */
    private static final int SAMPLE_SIDE_LENGTH = 16;

    /** 采样最低层：地表下 4 格。 */
    private static final int SAMPLE_Y_BELOW_SURFACE = 4;

    /** 采样最高层：地表上 12 格。 */
    private static final int SAMPLE_Y_ABOVE_SURFACE = 12;

    /** 低密度地形额外向下扩展层数。 */
    private static final int LOW_DENSITY_EXTRA_DOWN_LAYERS = 2;

    /** 判定“方块总量偏少”的非空气占比阈值。 */
    private static final double LOW_DENSITY_NON_AIR_RATIO_THRESHOLD = 0.18D;

    /** 底层基岩上移的最大尝试次数，防止理论死循环。 */
    private static final int MAX_BEDROCK_BOTTOM_SHIFTS = 24;

    /** locate 失败后的随机兜底最大尝试次数。 */
    private static final int MAX_BIOME_ATTEMPTS = 20;

    /** locate 搜索半径（方块）。 */
    private static final int LOCATE_SEARCH_RADIUS = 4_096;

    /** locate 搜索水平步长。 */
    private static final int LOCATE_HORIZONTAL_STEP = 16;

    /** locate 搜索垂直步长。 */
    private static final int LOCATE_VERTICAL_STEP = 16;

    /** 随机兜底时，以搜索原点为中心的 chunk 偏移范围。 */
    private static final int RANDOM_FALLBACK_CHUNK_RANGE = 256;

    /** 顶层天空可见列最小占比（用于排除洞穴/矿井口候选）。 */
    private static final double MIN_SKY_VISIBLE_RATIO = 0.55D;

    /** 顶层自然地表列最小占比（用于排除纯石质平台候选）。 */
    private static final double MIN_NATURAL_TOP_RATIO = 0.35D;

    /** 顶层上方开放空气列最小占比（用于排除顶部压顶地形）。 */
    private static final double MIN_OPEN_AIR_RATIO = 0.45D;

    /** 顶层“石质/深层石质”列最大占比。 */
    private static final double MAX_STONE_LIKE_TOP_RATIO = 0.70D;

    /** 顶层上方开放空气采样高度（检查 top+1 与 top+2）。 */
    private static final int OPEN_AIR_CHECK_HEIGHT = 2;

    /** 自然度评分中“天空可见占比”的权重。 */
    private static final double SKY_VISIBLE_WEIGHT = 0.45D;

    /** 自然度评分中“自然顶层占比”的权重。 */
    private static final double NATURAL_TOP_WEIGHT = 0.35D;

    /** 自然度评分中“开放空气占比”的权重。 */
    private static final double OPEN_AIR_WEIGHT = 0.30D;

    /** 自然度评分中“石质顶层占比”的惩罚权重。 */
    private static final double STONE_LIKE_PENALTY_WEIGHT = 0.40D;

    /** 方块更新标志：仅同步给客户端，不触发邻居更新。 */
    private static final int PLACE_UPDATE_FLAGS = Block.UPDATE_CLIENTS;

    /** chunk 边长常量，用于坐标对齐。 */
    private static final int CHUNK_SIZE = 16;

    private OverworldTerrainSampler() {
    }

    /**
     * 从主世界采样地形并放置到仙窍目标锚点。
     * <p>
     * 规则说明：
     * </p>
     * <ul>
     *     <li>若显式源锚点为空，则从指定搜索原点执行 locate 风格最近 biome 搜索。</li>
     *     <li>若 locate 未命中，则执行有限次数随机兜底搜索（最多 20 次）。</li>
     *     <li>地表高度使用 {@link Heightmap.Types#MOTION_BLOCKING_NO_LEAVES}。</li>
     *     <li>采样范围固定为 X/Z 16x16，Y 为地表下 4 到地表上 12，并对维度高度做 clamp。</li>
     *     <li>采样盒子最底层遇到流体或重力方块时，强制替换为石头。</li>
     *     <li>放置时仅写入 BlockState，更新标志使用 {@link Block#UPDATE_CLIENTS}。</li>
     * </ul>
     *
     * @param overworldLevel 主世界服务端世界
     * @param apertureLevel 仙窍目标服务端世界
     * @param targetAnchor 目标放置锚点（对应采样盒子最小角）
     * @param targetBiome 目标 biome（用于 locate 搜索源点）
     * @param explicitSourceAnchor 显式源锚点；为 null 时启用 locate 搜索
     * @param random 随机源
     * @return 成功返回 true；找不到可用源点或坐标越界时返回 false
     */
    public static boolean sampleAndPlace(
        ServerLevel overworldLevel,
        ServerLevel apertureLevel,
        BlockPos targetAnchor,
        Holder<Biome> targetBiome,
        @Nullable BlockPos explicitSourceAnchor,
        RandomSource random
    ) {
        return sampleAndPlace(
            overworldLevel,
            apertureLevel,
            targetAnchor,
            targetBiome,
            explicitSourceAnchor,
            overworldLevel.getSharedSpawnPos(),
            random
        );
    }

    /**
     * 从主世界采样地形并放置到仙窍目标锚点（可控搜索起点版）。
     *
     * @param overworldLevel 主世界服务端世界
     * @param apertureLevel 仙窍目标服务端世界
     * @param targetAnchor 目标放置锚点（对应采样盒子最小角）
     * @param targetBiome 目标 biome（用于 locate 搜索源点）
     * @param explicitSourceAnchor 显式源锚点；为 null 时启用 locate 搜索
     * @param searchOrigin locate 搜索起点（主世界坐标）
     * @param random 随机源
     * @return 成功返回 true；找不到可用源点或坐标越界时返回 false
     */
    public static boolean sampleAndPlace(
        ServerLevel overworldLevel,
        ServerLevel apertureLevel,
        BlockPos targetAnchor,
        Holder<Biome> targetBiome,
        @Nullable BlockPos explicitSourceAnchor,
        BlockPos searchOrigin,
        RandomSource random
    ) {
        @Nullable BlockPos sourceAnchor = explicitSourceAnchor;
        if (sourceAnchor == null) {
            sourceAnchor = findBiomeLocation(overworldLevel, targetBiome, searchOrigin, random);
        }

        if (sourceAnchor == null) {
            return false;
        }

        int alignedSourceMinX = alignToChunkMin(sourceAnchor.getX());
        int alignedSourceMinZ = alignToChunkMin(sourceAnchor.getZ());
        int centerX = alignedSourceMinX + CHUNK_SIZE / 2;
        int centerZ = alignedSourceMinZ + CHUNK_SIZE / 2;
        int surfaceY = overworldLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;

        int sourceMinY = Math.max(overworldLevel.getMinBuildHeight(), surfaceY - SAMPLE_Y_BELOW_SURFACE);
        int sourceMaxY = Math.min(overworldLevel.getMaxBuildHeight() - 1, surfaceY + SAMPLE_Y_ABOVE_SURFACE);
        if (sourceMinY > sourceMaxY) {
            return false;
        }

        if (isSampleTooSparse(overworldLevel, alignedSourceMinX, alignedSourceMinZ, sourceMinY, sourceMaxY)) {
            sourceMinY = Math.max(overworldLevel.getMinBuildHeight(), sourceMinY - LOW_DENSITY_EXTRA_DOWN_LAYERS);
        }

        sourceMinY = liftBottomUntilNoBedrock(
            overworldLevel,
            alignedSourceMinX,
            alignedSourceMinZ,
            sourceMinY,
            sourceMaxY
        );
        if (sourceMinY > sourceMaxY) {
            return false;
        }

        int sampleHeight = sourceMaxY - sourceMinY + 1;
        for (int xOffset = 0; xOffset < SAMPLE_SIDE_LENGTH; xOffset++) {
            for (int zOffset = 0; zOffset < SAMPLE_SIDE_LENGTH; zOffset++) {
                for (int yOffset = 0; yOffset < sampleHeight; yOffset++) {
                    int sourceX = alignedSourceMinX + xOffset;
                    int sourceY = sourceMinY + yOffset;
                    int sourceZ = alignedSourceMinZ + zOffset;
                    BlockPos sourcePos = new BlockPos(sourceX, sourceY, sourceZ);

                    BlockState sourceState = overworldLevel.getBlockState(sourcePos);
                    boolean isBottomLayer = sourceY == sourceMinY;
                    BlockState placeState = transformBottomLayerStateIfNeeded(sourceState, isBottomLayer);

                    BlockPos targetPos = targetAnchor.offset(xOffset, yOffset, zOffset);
                    if (
                        targetPos.getY() < apertureLevel.getMinBuildHeight()
                            || targetPos.getY() >= apertureLevel.getMaxBuildHeight()
                    ) {
                        return false;
                    }
                    apertureLevel.setBlock(targetPos, placeState, PLACE_UPDATE_FLAGS);
                }
            }
        }
        return true;
    }

    /**
     * 判断当前采样盒是否“方块总量偏少”。
     *
     * @param level 主世界
     * @param sourceMinX 采样盒最小 X
     * @param sourceMinZ 采样盒最小 Z
     * @param sourceMinY 采样盒最小 Y
     * @param sourceMaxY 采样盒最大 Y
     * @return 非空气占比低于阈值时返回 true
     */
    private static boolean isSampleTooSparse(
        ServerLevel level,
        int sourceMinX,
        int sourceMinZ,
        int sourceMinY,
        int sourceMaxY
    ) {
        int totalBlocks = SAMPLE_SIDE_LENGTH * SAMPLE_SIDE_LENGTH * (sourceMaxY - sourceMinY + 1);
        if (totalBlocks <= 0) {
            return true;
        }

        int nonAirCount = 0;
        for (int xOffset = 0; xOffset < SAMPLE_SIDE_LENGTH; xOffset++) {
            for (int zOffset = 0; zOffset < SAMPLE_SIDE_LENGTH; zOffset++) {
                for (int y = sourceMinY; y <= sourceMaxY; y++) {
                    BlockPos scanPos = new BlockPos(sourceMinX + xOffset, y, sourceMinZ + zOffset);
                    if (!level.getBlockState(scanPos).isAir()) {
                        nonAirCount++;
                    }
                }
            }
        }

        double nonAirRatio = (double) nonAirCount / totalBlocks;
        return nonAirRatio < LOW_DENSITY_NON_AIR_RATIO_THRESHOLD;
    }

    /**
     * 若采样盒底层包含基岩，则持续上移底层直到不含基岩或达到上移上限。
     *
     * @param level 主世界
     * @param sourceMinX 采样盒最小 X
     * @param sourceMinZ 采样盒最小 Z
     * @param sourceMinY 当前采样盒最小 Y
     * @param sourceMaxY 采样盒最大 Y
     * @return 调整后的采样盒最小 Y
     */
    private static int liftBottomUntilNoBedrock(
        ServerLevel level,
        int sourceMinX,
        int sourceMinZ,
        int sourceMinY,
        int sourceMaxY
    ) {
        int shiftedMinY = sourceMinY;
        int shiftedTimes = 0;
        while (
            shiftedMinY <= sourceMaxY
                && shiftedTimes < MAX_BEDROCK_BOTTOM_SHIFTS
                && hasBedrockInBottomLayer(level, sourceMinX, sourceMinZ, shiftedMinY)
        ) {
            shiftedMinY++;
            shiftedTimes++;
        }
        return shiftedMinY;
    }

    /**
     * 检查采样盒底层是否含基岩。
     *
     * @param level 主世界
     * @param sourceMinX 采样盒最小 X
     * @param sourceMinZ 采样盒最小 Z
     * @param bottomY 底层 Y
     * @return 只要底层任意位置为基岩则返回 true
     */
    private static boolean hasBedrockInBottomLayer(ServerLevel level, int sourceMinX, int sourceMinZ, int bottomY) {
        for (int xOffset = 0; xOffset < SAMPLE_SIDE_LENGTH; xOffset++) {
            for (int zOffset = 0; zOffset < SAMPLE_SIDE_LENGTH; zOffset++) {
                BlockPos bottomPos = new BlockPos(sourceMinX + xOffset, bottomY, sourceMinZ + zOffset);
                if (level.getBlockState(bottomPos).is(Blocks.BEDROCK)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 在主世界内按 biome 执行 locate 风格最近搜索，并返回 chunk 对齐锚点。
     * <p>
     * 若 locate 未命中，则以搜索起点为中心做有限次数随机兜底，尽量避免直接失败。
     * </p>
     *
     * @param overworldLevel 主世界
     * @param targetBiome 目标 biome
     * @param searchOrigin 搜索起点
     * @param random 随机源
     * @return 找到时返回源锚点（chunk 最小角）；超过最大尝试次数返回 null
     */
    @Nullable
    private static BlockPos findBiomeLocation(
        ServerLevel overworldLevel,
        Holder<Biome> targetBiome,
        BlockPos searchOrigin,
        RandomSource random
    ) {
        @Nullable AnchorCandidate bestCandidate = null;
        Pair<BlockPos, Holder<Biome>> located = overworldLevel.findClosestBiome3d(
            biomeHolder -> isSameBiome(biomeHolder, targetBiome),
            searchOrigin,
            LOCATE_SEARCH_RADIUS,
            LOCATE_HORIZONTAL_STEP,
            LOCATE_VERTICAL_STEP
        );
        if (located != null) {
            BlockPos locateAnchor = normalizeLocatedAnchor(overworldLevel, located.getFirst());
            AnchorCandidate locateCandidate = evaluateAnchorCandidate(
                overworldLevel,
                locateAnchor.getX(),
                locateAnchor.getZ()
            );
            if (locateCandidate.quality().strictPass()) {
                return locateAnchor;
            }
            bestCandidate = locateCandidate;
        }

        int originChunkX = Math.floorDiv(searchOrigin.getX(), CHUNK_SIZE);
        int originChunkZ = Math.floorDiv(searchOrigin.getZ(), CHUNK_SIZE);
        for (int attempt = 0; attempt < MAX_BIOME_ATTEMPTS; attempt++) {
            int randomChunkX = originChunkX + random.nextIntBetweenInclusive(
                -RANDOM_FALLBACK_CHUNK_RANGE,
                RANDOM_FALLBACK_CHUNK_RANGE
            );
            int randomChunkZ = originChunkZ + random.nextIntBetweenInclusive(
                -RANDOM_FALLBACK_CHUNK_RANGE,
                RANDOM_FALLBACK_CHUNK_RANGE
            );
            int chunkMinX = randomChunkX * CHUNK_SIZE;
            int chunkMinZ = randomChunkZ * CHUNK_SIZE;
            int centerX = chunkMinX + CHUNK_SIZE / 2;
            int centerZ = chunkMinZ + CHUNK_SIZE / 2;
            int centerY = overworldLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
            BlockPos centerPos = new BlockPos(centerX, centerY, centerZ);

            Holder<Biome> centerBiome = overworldLevel.getBiome(centerPos);
            if (isSameBiome(centerBiome, targetBiome)) {
                AnchorCandidate fallbackCandidate = evaluateAnchorCandidate(overworldLevel, chunkMinX, chunkMinZ);
                if (fallbackCandidate.quality().strictPass()) {
                    return fallbackCandidate.anchor();
                }
                bestCandidate = selectBetterCandidate(bestCandidate, fallbackCandidate);
            }
        }
        return bestCandidate == null ? null : bestCandidate.anchor();
    }

    /**
     * 评估采样锚点自然度，并返回可用于回退排序的候选信息。
     *
     * @param level 主世界
     * @param chunkMinX 采样块最小 X
     * @param chunkMinZ 采样块最小 Z
     * @return 评估后的锚点候选
     */
    private static AnchorCandidate evaluateAnchorCandidate(ServerLevel level, int chunkMinX, int chunkMinZ) {
        int skyVisibleColumns = 0;
        int naturalTopColumns = 0;
        int openAirColumns = 0;
        int stoneLikeTopColumns = 0;
        int totalColumns = SAMPLE_SIDE_LENGTH * SAMPLE_SIDE_LENGTH;

        for (int xOffset = 0; xOffset < SAMPLE_SIDE_LENGTH; xOffset++) {
            for (int zOffset = 0; zOffset < SAMPLE_SIDE_LENGTH; zOffset++) {
                int worldX = chunkMinX + xOffset;
                int worldZ = chunkMinZ + zOffset;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ) - 1;
                BlockPos topPos = new BlockPos(worldX, surfaceY, worldZ);

                if (level.canSeeSky(topPos.above())) {
                    skyVisibleColumns++;
                }

                BlockState topState = level.getBlockState(topPos);
                if (isNaturalTopBlock(topState)) {
                    naturalTopColumns++;
                }
                if (isStoneLikeTopBlock(topState)) {
                    stoneLikeTopColumns++;
                }

                if (isColumnOpenAir(level, topPos)) {
                    openAirColumns++;
                }
            }
        }

        double skyVisibleRatio = ratio(skyVisibleColumns, totalColumns);
        double naturalTopRatio = ratio(naturalTopColumns, totalColumns);
        double openAirRatio = ratio(openAirColumns, totalColumns);
        double stoneLikeTopRatio = ratio(stoneLikeTopColumns, totalColumns);

        boolean strictPass = skyVisibleRatio >= MIN_SKY_VISIBLE_RATIO
            && naturalTopRatio >= MIN_NATURAL_TOP_RATIO
            && openAirRatio >= MIN_OPEN_AIR_RATIO
            && stoneLikeTopRatio <= MAX_STONE_LIKE_TOP_RATIO;

        double qualityScore = skyVisibleRatio * SKY_VISIBLE_WEIGHT
            + naturalTopRatio * NATURAL_TOP_WEIGHT
            + openAirRatio * OPEN_AIR_WEIGHT
            - stoneLikeTopRatio * STONE_LIKE_PENALTY_WEIGHT;

        AnchorQuality quality = new AnchorQuality(strictPass, qualityScore);
        int centerX = chunkMinX + CHUNK_SIZE / 2;
        int centerZ = chunkMinZ + CHUNK_SIZE / 2;
        int centerY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        return new AnchorCandidate(new BlockPos(chunkMinX, centerY, chunkMinZ), quality);
    }

    /**
     * 选出质量更高的候选锚点。
     *
     * @param current 当前最佳候选
     * @param incoming 新候选
     * @return 质量更高者
     */
    private static AnchorCandidate selectBetterCandidate(@Nullable AnchorCandidate current, AnchorCandidate incoming) {
        if (current == null) {
            return incoming;
        }
        return incoming.quality().score() > current.quality().score() ? incoming : current;
    }

    /**
     * 检查顶层列上方是否具备足够空气空间。
     *
     * @param level 主世界
     * @param topPos 顶层方块坐标
     * @return top+1 到 top+2 全为空气时返回 true
     */
    private static boolean isColumnOpenAir(ServerLevel level, BlockPos topPos) {
        for (int i = 1; i <= OPEN_AIR_CHECK_HEIGHT; i++) {
            if (!level.getBlockState(topPos.above(i)).isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断顶层方块是否属于自然地表类型。
     *
     * @param topState 顶层方块状态
     * @return 命中自然地表类型返回 true
     */
    private static boolean isNaturalTopBlock(BlockState topState) {
        return topState.is(Blocks.GRASS_BLOCK)
            || topState.is(Blocks.DIRT)
            || topState.is(Blocks.COARSE_DIRT)
            || topState.is(Blocks.ROOTED_DIRT)
            || topState.is(Blocks.PODZOL)
            || topState.is(Blocks.MYCELIUM)
            || topState.is(Blocks.SAND)
            || topState.is(Blocks.RED_SAND)
            || topState.is(Blocks.SNOW_BLOCK)
            || topState.is(Blocks.SNOW)
            || topState.is(Blocks.MUD)
            || topState.is(Blocks.GRAVEL)
            || topState.is(Blocks.CLAY)
            || topState.is(Blocks.MOSS_BLOCK);
    }

    /**
     * 判断顶层方块是否属于“洞穴感较强”的石质类型。
     *
     * @param topState 顶层方块状态
     * @return 为石质顶层返回 true
     */
    private static boolean isStoneLikeTopBlock(BlockState topState) {
        return topState.is(Blocks.STONE)
            || topState.is(Blocks.DEEPSLATE)
            || topState.is(Blocks.TUFF)
            || topState.is(Blocks.COBBLESTONE)
            || topState.is(Blocks.COBBLED_DEEPSLATE);
    }

    /**
     * 计算计数占比。
     *
     * @param numerator 分子
     * @param denominator 分母
     * @return 分子/分母，分母小于等于 0 时返回 0
     */
    private static double ratio(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return (double) numerator / denominator;
    }

    /**
     * 将 locate 返回坐标归一化为采样锚点（chunk 最小角 + 地表高度）。
     *
     * @param overworldLevel 主世界
     * @param locatedPos locate 命中的坐标
     * @return 归一化后的采样锚点
     */
    private static BlockPos normalizeLocatedAnchor(ServerLevel overworldLevel, BlockPos locatedPos) {
        int chunkMinX = alignToChunkMin(locatedPos.getX());
        int chunkMinZ = alignToChunkMin(locatedPos.getZ());
        int centerX = chunkMinX + CHUNK_SIZE / 2;
        int centerZ = chunkMinZ + CHUNK_SIZE / 2;
        int centerY = overworldLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        return new BlockPos(chunkMinX, centerY, chunkMinZ);
    }

    /**
     * 将任意 X/Z 坐标对齐到所在 chunk 的最小角（16 边界）。
     *
     * @param value 原始坐标
     * @return 对齐后的 chunk 最小角坐标
     */
    private static int alignToChunkMin(int value) {
        return Math.floorDiv(value, CHUNK_SIZE) * CHUNK_SIZE;
    }

    /**
     * 对采样盒子底层方块执行替换规则。
     *
     * @param state 原始方块状态
     * @param isBottomLayer 是否处于采样盒子最底层
     * @return 处理后的方块状态
     */
    private static BlockState transformBottomLayerStateIfNeeded(BlockState state, boolean isBottomLayer) {
        if (!isBottomLayer) {
            return state;
        }

        boolean hasFluid = !state.getFluidState().isEmpty();
        boolean isFallingBlock = state.getBlock() instanceof FallingBlock;
        if (hasFluid || isFallingBlock) {
            return Blocks.STONE.defaultBlockState();
        }
        return state;
    }

    /**
     * 判断两个 biome holder 是否表示同一个 biome。
     * <p>
     * 优先比较资源键，若任一侧无键（例如直接 holder）则回退到 biome 实例比较。
     * </p>
     *
     * @param left 左侧 biome holder
     * @param right 右侧 biome holder
     * @return 同一 biome 返回 true，否则 false
     */
    private static boolean isSameBiome(Holder<Biome> left, Holder<Biome> right) {
        Optional<ResourceKey<Biome>> leftKey = left.unwrapKey();
        Optional<ResourceKey<Biome>> rightKey = right.unwrapKey();
        if (leftKey.isPresent() && rightKey.isPresent()) {
            return leftKey.get().equals(rightKey.get());
        }
        return left.value().equals(right.value());
    }

    private record AnchorQuality(boolean strictPass, double score) {
    }

    private record AnchorCandidate(BlockPos anchor, AnchorQuality quality) {
    }
}
