package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionModifier;
import com.Kizunad.guzhenrenext.bastion.BastionParticles;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import com.Kizunad.guzhenrenext.bastion.threat.ThreatEventService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 基地 Anchor 方块（子核心/支撑节点）。
 * <p>
 * 这是基地扩张的关键支点：
 * <ul>
 *   <li>由扩张服务生成的 Anchor 会计入基地 Anchor 数量</li>
 *   <li>被破坏时会减少基地 Anchor 数量，并可触发威胁事件</li>
 *   <li>保留 tier/dao 属性，便于后续做功能节点挂载</li>
 * </ul>
 * </p>
 */
public class BastionAnchorBlock extends Block {

    /** 节点转数最小值。 */
    private static final int MIN_TIER = 1;

    /** 节点转数最大值（支持 7-9 转高转内容）。 */
    private static final int MAX_TIER = 9;

    /** 查找归属基地的最大搜索半径。 */
    private static final int MAX_OWNER_SEARCH_RADIUS = 128;

    /** 每秒 tick 数（MC 固定为 20）。 */
    private static final float TICKS_PER_SECOND = 20.0f;

    /**
     * HARDENED 词缀：Anchor 破坏速度倍率（仅对 GENERATED=true 的扩张 Anchor 生效）。
     */
    private static final float HARDENED_ANCHOR_PROGRESS_MULTIPLIER = 0.5f;

    /**
     * VOLATILE 词缀：拆除 Anchor 时的爆炸半径（不破坏方块）。
     */
    private static final float VOLATILE_EXPLOSION_RADIUS = 2.5f;

    /** VOLATILE 触发概率。 */
    private static final double VOLATILE_EXPLOSION_CHANCE = 0.2;

    /** 爆炸中心偏移。 */
    private static final double EXPLOSION_CENTER_OFFSET = 0.5;

    /** Anchor 转数属性（1-9）。 */
    public static final IntegerProperty TIER = IntegerProperty.create("tier", MIN_TIER, MAX_TIER);

    /** Anchor 道途类型属性。 */
    public static final EnumProperty<BastionDao> DAO = EnumProperty.create("dao", BastionDao.class);

    /**
     * 是否由扩张服务生成（用于区分玩家手动放置）。
     */
    public static final BooleanProperty GENERATED = BooleanProperty.create("generated");

    public BastionAnchorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(TIER, MIN_TIER)
                .setValue(DAO, BastionDao.ZHI_DAO)
                .setValue(GENERATED, false)
        );
    }

    /**
     * 创建带 tier/dao/generated 的 Anchor 状态。
     * <p>
     * 用于扩张服务快速构造方块状态。</p>
     */
    public BlockState withTierDaoGenerated(int tier, BastionDao dao, boolean generated) {
        int clampedTier = Math.max(MIN_TIER, Math.min(MAX_TIER, tier));
        BastionDao safeDao = dao == null ? BastionDao.ZHI_DAO : dao;
        return defaultBlockState()
            .setValue(TIER, clampedTier)
            .setValue(DAO, safeDao)
            .setValue(GENERATED, generated);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, DAO, GENERATED);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                updateBastionAnchorCount(serverLevel, pos, state, -1);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (!player.hasCorrectToolForDrops(state)) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        int tier = state.getValue(TIER);
        int targetSeconds = com.Kizunad.guzhenrenext.bastion.config.BastionBreakingConfig
            .getNodeSeconds(tier);

        float progress = computeProgressBySeconds(state, player, targetSeconds);
        if (!state.getValue(GENERATED)) {
            return progress;
        }

        if (level instanceof ServerLevel serverLevel) {
            BastionSavedData savedData = BastionSavedData.get(serverLevel);
            BastionData owner = savedData.findOwnerBastion(pos, MAX_OWNER_SEARCH_RADIUS);
            if (owner != null && owner.modifiers().contains(BastionModifier.HARDENED)) {
                return progress * HARDENED_ANCHOR_PROGRESS_MULTIPLIER;
            }
        }

        return progress;
    }

    private static float computeProgressBySeconds(BlockState state, Player player, int targetSeconds) {
        int safeSeconds = Math.max(1, targetSeconds);

        final float referenceSpeed = 6.0f;
        float actualSpeed = player.getDestroySpeed(state);
        if (actualSpeed <= 0.0f) {
            return 0.0f;
        }

        return (actualSpeed / referenceSpeed) * (1.0f / (safeSeconds * TICKS_PER_SECOND));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData owner = savedData.findOwnerBastion(pos, MAX_OWNER_SEARCH_RADIUS);
        if (owner == null) {
            level.destroyBlock(pos, false);
        }
    }

    public void scheduleOrphanCheck(ServerLevel level, BlockPos pos, int delay) {
        level.scheduleTick(pos, this, delay);
    }

    private void updateBastionAnchorCount(ServerLevel level, BlockPos pos, BlockState state, int countDelta) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData owner = savedData.findOwnerBastion(pos, MAX_OWNER_SEARCH_RADIUS);

        // 回合2.1.1：无论是否能找到 owner，都清理索引，避免 NBT 残留。
        // 注意：必须在 owner 查询之后执行，否则会破坏“优先索引命中”的效果。
        savedData.clearAnchorOwnerIndex(pos);

        // 非扩张生成的 Anchor 不参与基地 Anchor 计数（但索引仍应清理，见上）。
        if (countDelta < 0 && !state.getValue(GENERATED)) {
            return;
        }
        if (owner == null) {
            return;
        }

        BastionData updated = owner.withAnchorCountDelta(countDelta);
        savedData.updateBastion(updated);

        if (countDelta < 0) {
            // 复用 nodeCache 作为“扩张生成物缓存”最小实现，后续再拆分为 anchorCache。
            savedData.removeNodeFromCache(owner.id(), pos);

            if (owner.modifiers().contains(BastionModifier.VOLATILE)
                && level.getRandom().nextDouble() < VOLATILE_EXPLOSION_CHANCE) {
                level.explode(
                    null,
                    pos.getX() + EXPLOSION_CENTER_OFFSET,
                    pos.getY() + EXPLOSION_CENTER_OFFSET,
                    pos.getZ() + EXPLOSION_CENTER_OFFSET,
                    VOLATILE_EXPLOSION_RADIUS,
                    Level.ExplosionInteraction.NONE
                );
            }

            // Anchor 被拆也算“拆节点风险点”。
            ThreatEventService.tryTriggerOnNodeDestroyed(
                level,
                savedData,
                updated,
                pos,
                owner.totalNodes(),
                level.getGameTime()
            );

            BastionParticles.spawnAnchorDestroyedParticles(level, pos, state.getValue(DAO));
            BastionNetworkHandler.syncIfAuraRadiusChanged(level, updated);
        }
    }
}
