package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.BastionState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基地核心方块 - 基地的核心。
 * <p>
 * 当核心被破坏时，归属基地进入 DESTROYED 状态并开始衰减。
 * 核心方块显示基地当前的转数和主道途类型。
 * </p>
 */
public class BastionCoreBlock extends Block {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionCoreBlock.class);

    /** 核心转数最小值。 */
    private static final int MIN_TIER = 1;

    /** 核心转数最大值（支持 7-9 转高转内容）。 */
    private static final int MAX_TIER = 9;

    /**
     * 核心转数属性（反映基地转数，1-9）。
     */
    public static final IntegerProperty TIER = IntegerProperty.create("tier", MIN_TIER, MAX_TIER);

    /**
     * 基地主道途类型属性。
     */
    public static final EnumProperty<BastionDao> DAO = EnumProperty.create("dao", BastionDao.class);

    public BastionCoreBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(TIER, MIN_TIER)
                .setValue(DAO, BastionDao.ZHI_DAO)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, DAO);
    }

    /**
     * 核心方块被移除时调用（被玩家破坏或爆炸摧毁）。
     * 将归属基地标记为 DESTROYED 并触发衰减流程。
     */
    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                handleCoreDestruction(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * 处理核心被破坏：将基地标记为 DESTROYED。
     *
     * @param level 服务端世界
     * @param pos   核心坐标
     */
    private void handleCoreDestruction(ServerLevel level, BlockPos pos) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.findByCorePos(pos);

        if (bastion == null) {
            LOGGER.warn("核心方块在 {} 被破坏，但未找到对应基地", pos);
            return;
        }

        if (bastion.state() == BastionState.DESTROYED) {
            // 已处于 DESTROYED 状态，无需处理
            return;
        }

        long gameTime = level.getGameTime();
        boolean marked = savedData.markDestroyed(bastion.id(), gameTime);

        if (marked) {
            LOGGER.info("基地 {} 在 {} 被标记为 DESTROYED，游戏时间 {}",
                bastion.id(), pos, gameTime);
            // 注意：实际节点衰减由 BastionTicker 在 DESTROYED 阶段处理
        }
    }

    /**
     * 玩家开始破坏方块时调用。
     * 可用于实现抗性或警告效果。
     */
    @Override
    public float getDestroyProgress(
            BlockState state,
            Player player,
            BlockGetter level,
            BlockPos pos) {
        // 更高转数的核心更难破坏
        int tier = state.getValue(TIER);
        float baseProgress = super.getDestroyProgress(state, player, level, pos);

        // 转数缩放：1 转 = 100%，6 转 ≈ 16% 速度
        float tierMultiplier = 1.0f / tier;
        return baseProgress * tierMultiplier;
    }

    /**
     * 从方块状态获取转数。
     *
     * @param state 方块状态
     * @return 转数（1-6）
     */
    public static int getTier(BlockState state) {
        return state.getValue(TIER);
    }

    /**
     * 从方块状态获取道途类型。
     *
     * @param state 方块状态
     * @return 道途类型
     */
    public static BastionDao getDao(BlockState state) {
        return state.getValue(DAO);
    }

    /**
     * 创建指定转数和道途的方块状态。
     *
     * @param tier 转数（1-6）
     * @param dao  道途类型
     * @return 配置好的方块状态
     */
    public BlockState withTierAndDao(int tier, BastionDao dao) {
        return this.defaultBlockState()
            .setValue(TIER, Math.max(MIN_TIER, Math.min(MAX_TIER, tier)))
            .setValue(DAO, dao);
    }

    /**
     * 更新核心方块显示的转数（基地进化时调用）。
     *
     * @param level   服务端世界
     * @param pos     核心坐标
     * @param newTier 新转数
     */
    public static void updateDisplayedTier(ServerLevel level, BlockPos pos, int newTier) {
        BlockState currentState = level.getBlockState(pos);
        if (currentState.getBlock() instanceof BastionCoreBlock) {
            BlockState newState = currentState.setValue(
                TIER, Math.max(MIN_TIER, Math.min(MAX_TIER, newTier)));
            level.setBlock(pos, newState, Block.UPDATE_ALL);
        }
    }

    /**
     * 客户端动画刻 - 生成环境粒子效果。
     * <p>
     * 核心方块会根据道途类型产生彩色尘埃粒子环绕效果。
     * </p>
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BastionParticles.spawnCoreAmbientParticles(state, level, pos, random);
    }
}
