package com.Kizunad.guzhenrenext.worldgen.feature;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.block.BastionAnchorBlock;
import com.Kizunad.guzhenrenext.worldgen.BastionRuinLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 主世界基地遗迹生成 Feature。
 * <p>
 * 生成内容：
 * <ul>
 *   <li>放置 Bastion Core（带随机道途与转数）</li>
 *   <li>在四周放置少量 Bastion Node 作为遗迹</li>
 *   <li>自动写入 BastionSavedData（ACTIVE），使其立即开始扩张/刷怪/光环</li>
 * </ul>
 * </p>
 */
public class BastionRuinFeature extends Feature<NoneFeatureConfiguration> {

    /** 遗迹节点与核心的距离。 */
    private static final int RUIN_NODE_OFFSET = 2;

    /**
     * 核心向上偏移一格，避免直接替换地表方块导致生成失败（如超平坦草方块不可替换）。
     */
    private static final int CORE_Y_OFFSET = 1;

    /**
     * Worldgen 放置方块时使用的 flags。
     * <p>
     * 参考 vanilla Feature#safeSetBlock：使用 2 避免邻接更新/连锁更新导致生成期卡顿。
     * </p>
     */
    private static final int WORLDGEN_SETBLOCK_FLAGS = 2;

    /**
     * 转数随机表：大多数 1~3，少量 4~6。
     */
    private static final int[] TIER_TABLE = {
        1, 1, 1, 1,
        2, 2, 2,
        3, 3,
        4,
        5,
        6
    };


    public BastionRuinFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        // placed_feature 已使用 heightmap placement 调整 origin.y，因此此处直接使用 origin。
        BlockPos corePos = context.origin().above(CORE_Y_OFFSET);

        // 记录尝试（用于定位“完全没触发” vs “触发但失败”）
        BastionRuinLocator.recordAttempt(world.getLevel().dimension(), corePos);

        if (!world.ensureCanWrite(corePos)) {
            return false;
        }

        // 简单可放置性：核心位置必须可替换
        BlockState targetState = world.getBlockState(corePos);
        if (!targetState.canBeReplaced()) {
            return false;
        }

        // Worldgen 阶段仅放置遗迹方块，避免写入 SavedData 导致卡顿/超时。
        // 自动激活改为：玩家第一次与核心交互时注册。

        // 随机选择道途与转数
        BastionDao dao = pickDao(random);
        int tier = pickTier(random);

        // 放置核心方块
        BlockState coreState = ((BastionCoreBlock) BastionBlocks.BASTION_CORE.get())
            .withTierAndDao(tier, dao);
        world.setBlock(corePos, coreState, WORLDGEN_SETBLOCK_FLAGS);

        // 记录生成位置（用于 debug 定位）
        BastionRuinLocator.record(world.getLevel().dimension(), corePos);

        // 放置四周节点作为遗迹（generated=true）
        placeRuinNodes(world, corePos, tier, dao);

        return true;
    }

    private static BastionDao pickDao(RandomSource random) {
        BastionDao[] daos = new BastionDao[] {
            BastionDao.ZHI_DAO,
            BastionDao.HUN_DAO,
            BastionDao.MU_DAO,
            BastionDao.LI_DAO
        };
        return daos[random.nextInt(daos.length)];
    }

    private static int pickTier(RandomSource random) {
        return TIER_TABLE[random.nextInt(TIER_TABLE.length)];
    }

    private static void placeRuinNodes(
        WorldGenLevel world,
        BlockPos corePos,
        int tier,
        BastionDao dao
    ) {
        BastionAnchorBlock nodeBlock = (BastionAnchorBlock) BastionBlocks.BASTION_ANCHOR.get();
        BlockState nodeState = nodeBlock.defaultBlockState()
            .setValue(BastionAnchorBlock.TIER, tier)
            .setValue(BastionAnchorBlock.DAO, dao)
            .setValue(BastionAnchorBlock.GENERATED, true);

        for (Direction dir : new Direction[] {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
        }) {
            BlockPos nodePos = corePos.relative(dir, RUIN_NODE_OFFSET);
            if (!world.ensureCanWrite(nodePos)) {
                continue;
            }
            BlockState existing = world.getBlockState(nodePos);
            if (!existing.canBeReplaced()) {
                continue;
            }
            world.setBlock(nodePos, nodeState, WORLDGEN_SETBLOCK_FLAGS);
        }
    }
}
