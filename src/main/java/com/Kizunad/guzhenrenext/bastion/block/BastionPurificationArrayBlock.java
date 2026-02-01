package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.service.BastionCaptureService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 净化阵法方块。
 * <p>
 * 设计目标：
 * <ul>
 *     <li>玩家在基地领域内布置阵法结构并启动。</li>
 *     <li>阵法定期对附近基地执行“净化”操作：降低污染值。</li>
 *     <li>当污染降低到 0 时，将基地标记为“净化完成，可接管”。</li>
 * </ul>
 * </p>
 */
public class BastionPurificationArrayBlock extends Block {

    private static final Logger LOGGER = LoggerFactory.getLogger(BastionPurificationArrayBlock.class);

    /** 是否处于运行状态。 */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    /**
     * 常量配置（MVP：硬编码，后续可外置）。
     */
    private static final class Constants {
        /** 每秒处理一次。 */
        static final int TICK_INTERVAL_TICKS = 20;
        /** 查找归属基地的最大半径。 */
        static final int BASTION_SEARCH_RADIUS = 512;
        /** 单次净化减少的污染值（线性减小）。 */
        static final double PURIFY_AMOUNT = 0.05;

        private Constants() {
        }
    }

    public BastionPurificationArrayBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, true));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public void onPlace(
        final BlockState state,
        final Level level,
        final BlockPos pos,
        final BlockState oldState,
        final boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, Constants.TICK_INTERVAL_TICKS);
        }
    }

    @Override
    public void onRemove(
        BlockState state,
        net.minecraft.world.level.Level level,
        BlockPos pos,
        BlockState newState,
        boolean movedByPiston
    ) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void tick(
        final BlockState state,
        final ServerLevel level,
        final BlockPos pos,
        final RandomSource random
    ) {
        if (!state.getValue(ACTIVE)) {
            return;
        }

        // 结构被破坏则自动停止
        if (!isValidStructure(level, pos)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            return;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = findBastion(level, pos);
        if (bastion == null || !isInsideAura(bastion, pos)) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            return;
        }

        BastionData current = savedData.getBastion(bastion.id());
        if (current == null) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            return;
        }

        BastionData updated = tryPurify(level, current);
        if (updated == null) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            return;
        }
        savedData.updateBastion(updated);

        // 当污染为 0 时，标记基地为可接管（净化完成）。
        BastionCaptureService.tryMarkCapturableViaPurification(level, updated);

        level.scheduleTick(pos, this, Constants.TICK_INTERVAL_TICKS);
    }

    private static BastionData tryPurify(ServerLevel level, BastionData bastion) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.PollutionConfig pollution = typeConfig.pollution();
        if (pollution == null || !pollution.enabled()) {
            return bastion;
        }

        double currentPollution = bastion.pollution();
        double reduced = Math.max(0.0, currentPollution - Constants.PURIFY_AMOUNT);

        if (currentPollution <= 0.0) {
            return bastion;
        }

        LOGGER.info("净化阵法：基地 {} 污染 {} -> {}", bastion.id(), currentPollution, reduced);

        BastionData purifying = bastion.withPollution(reduced);
        if (reduced <= 0.0) {
            return purifying.withCapturable(true, BastionData.CaptureReason.PURIFICATION_READY, 0L);
        }

        return purifying;
    }

    private static BastionData findBastion(final ServerLevel level, final BlockPos pos) {
        BastionSavedData savedData = BastionSavedData.get(level);
        return savedData.findOwnerBastion(pos, Constants.BASTION_SEARCH_RADIUS);
    }

    private static boolean isValidStructure(final ServerLevel level, final BlockPos pos) {
        // MVP：中心四周必须是基地 Anchor（支撑节点）
        for (Direction dir : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (!(neighbor.getBlock() instanceof BastionAnchorBlock)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInsideAura(final BastionData bastion, final BlockPos pos) {
        int auraRadius = bastion.getAuraRadius();
        long radiusSq = (long) auraRadius * auraRadius;
        return bastion.corePos().distSqr(pos) <= radiusSq;
    }
}
