package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 基地节点方块 - 构成基地领地的可扩张方块。
 * <p>
 * 使用 BlockState 属性存储转数（1-6）和道途类型，无需 BlockEntity。
 * </p>
 *
 * <h2>设计说明</h2>
 * <ul>
 *   <li>节点方块虽然注册了 BlockItem，但设计上由 BastionExpansionService 放置</li>
 *   <li>玩家手动放置节点不会增加基地节点计数（无法确定归属基地）</li>
 *   <li>节点被破坏时会自动减少归属基地的节点计数</li>
 * </ul>
 */
public class BastionNodeBlock extends Block {

    /** 节点转数最小值。 */
    private static final int MIN_TIER = 1;

    /** 节点转数最大值（支持 7-9 转高转内容）。 */
    private static final int MAX_TIER = 9;

    /** 查找归属基地的最大搜索半径。 */
    private static final int MAX_OWNER_SEARCH_RADIUS = 128;

    /**
     * 节点转数属性（1-9，支持高转内容）。
     */
    public static final IntegerProperty TIER = IntegerProperty.create("tier", MIN_TIER, MAX_TIER);

    /**
     * 节点道途类型属性（设计上限为 4-8 种）。
     */
    public static final EnumProperty<BastionDao> DAO = EnumProperty.create("dao", BastionDao.class);

    /**
     * 节点是否由扩张服务生成（用于区分玩家手动放置）。
     * <p>
     * 扩张服务放置的节点为 true，玩家手动放置的节点为 false。
     * 被破坏时仅 GENERATED=true 的节点会减少基地节点计数。
     * 此属性持久化在 BlockState 中，服务器重启后不丢失。
     * </p>
     */
    public static final BooleanProperty GENERATED = BooleanProperty.create("generated");

    public BastionNodeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(TIER, MIN_TIER)
                .setValue(DAO, BastionDao.ZHI_DAO)
                .setValue(GENERATED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, DAO, GENERATED);
    }

    /**
     * 方块被移除时调用，更新归属基地的节点计数。
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // 仅在服务端处理，且确保是真正移除（非仅状态变化）
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                updateBastionNodeCount(serverLevel, pos, state, -1);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * 方块被放置时调用。
     * <p>
     * 注意：对于扩张放置的节点，BastionExpansionService 应直接调用 updateBastion。
     * 此钩子主要用于玩家手动放置等边缘场景，但不会增加节点计数
     * （因为无法确定节点应归属于哪个基地）。
     * </p>
     */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // 节点计数增量由 BastionExpansionService 在扩张时处理
        // 玩家手动放置的节点不计入任何基地（设计决策）
    }

    /**
     * 孤儿节点自清理的计划刻处理。
     * <p>
     * 当节点无法找到归属基地（核心被破坏 + 清理阶段）时，
     * 通过计划刻在延迟后自行移除。
     * </p>
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 检查此节点是否为孤儿（未找到归属基地）
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData owner = savedData.findOwnerBastion(pos, MAX_OWNER_SEARCH_RADIUS);

        if (owner == null) {
            // 未找到归属者 - 此节点为孤儿，销毁它
            level.destroyBlock(pos, false);
        }
    }

    /**
     * 调度孤儿检测的自清理刻。
     *
     * @param level 服务端世界
     * @param pos   方块坐标
     * @param delay 检测前的延迟刻数
     */
    public void scheduleOrphanCheck(ServerLevel level, BlockPos pos, int delay) {
        level.scheduleTick(pos, this, delay);
    }

    /**
     * 更新归属基地的节点计数。
     * <p>
     * 仅对 GENERATED=true 的节点执行计数操作，防止玩家手动放置的节点
     * 被破坏时错误地减少基地节点计数。此判断基于持久化的 BlockState 属性，
     * 服务器重启后不丢失。
     * </p>
     *
     * @param level      服务端世界
     * @param pos        节点坐标
     * @param state      方块状态（包含转数和 GENERATED 信息）
     * @param countDelta +1 表示添加，-1 表示移除
     */
    private void updateBastionNodeCount(ServerLevel level, BlockPos pos, BlockState state, int countDelta) {
        // 减少计数时，仅对扩张服务生成的节点（GENERATED=true）执行
        // 玩家手动放置的节点 GENERATED=false，被破坏时不应减少计数
        if (countDelta < 0 && !state.getValue(GENERATED)) {
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData owner = savedData.findOwnerBastion(pos, MAX_OWNER_SEARCH_RADIUS);

        if (owner != null) {
            int tier = state.getValue(TIER);
            BastionData updated = owner.withNodeCountUpdate(tier, countDelta);
            savedData.updateBastion(updated);

            // 从缓存中移除节点（如果是减少操作且节点在缓存中）
            if (countDelta < 0) {
                savedData.removeNodeFromCache(owner.id(), pos);
            }
        }
        // 如果未找到归属者，基地可能已被销毁
        // 节点将通过计划刻或自然衰减被清理
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
     * 创建指定转数和道途的方块状态（GENERATED=false）。
     * <p>
     * 用于玩家手动放置等场景。
     * </p>
     *
     * @param tier 转数（1-6）
     * @param dao  道途类型
     * @return 配置好的方块状态
     */
    public BlockState withTierAndDao(int tier, BastionDao dao) {
        return withTierDaoGenerated(tier, dao, false);
    }

    /**
     * 创建指定转数、道途和生成标记的方块状态。
     * <p>
     * 扩张服务应使用 generated=true 调用此方法。
     * </p>
     *
     * @param tier      转数（1-6）
     * @param dao       道途类型
     * @param generated 是否由扩张服务生成
     * @return 配置好的方块状态
     */
    public BlockState withTierDaoGenerated(int tier, BastionDao dao, boolean generated) {
        return this.defaultBlockState()
            .setValue(TIER, Math.max(MIN_TIER, Math.min(MAX_TIER, tier)))
            .setValue(DAO, dao)
            .setValue(GENERATED, generated);
    }

    /**
     * 客户端动画刻 - 生成环境粒子效果。
     * <p>
     * 节点方块会根据道途类型产生较小的彩色尘埃粒子效果。
     * </p>
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BastionParticles.spawnNodeAmbientParticles(state, level, pos, random);
    }
}
