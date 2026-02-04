package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.service.BastionCaptureService;
import com.Kizunad.guzhenrenext.bastion.multiblock.BastionBlueprintManager;
import com.Kizunad.guzhenrenext.bastion.multiblock.BastionMultiblockService;
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

        /** 水平四邻方向，避免重复分配。 */
        static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
        };

        private Constants() {
        }
    }

    /**
     * 结构类型常量，避免 MagicNumber。
     */
    private enum StructureType {
        /** 未匹配结构。 */
        NONE(0.0),
        /** 旧四邻基础结构。 */
        BASIC(0.05),
        /** 新 5x5 高级结构。 */
        ADVANCED(0.15);

        /** 单次净化减少的污染值。 */
        final double purifyAmount;

        StructureType(final double purifyAmount) {
            this.purifyAmount = purifyAmount;
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

        // 结构检测：优先新 5x5，回退旧四邻
        StructureType structure = detectStructure(level, pos);
        if (structure == StructureType.NONE) {
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

        BastionData updated = tryPurify(level, current, structure);
        if (updated == null) {
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            return;
        }
        savedData.updateBastion(updated);

        // 当污染为 0 时，标记基地为可接管（净化完成）。
        BastionCaptureService.tryMarkCapturableViaPurification(level, updated);

        level.scheduleTick(pos, this, Constants.TICK_INTERVAL_TICKS);
    }

    private static BastionData tryPurify(ServerLevel level, BastionData bastion, StructureType structure) {
        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(bastion.bastionType());
        BastionTypeConfig.PollutionConfig pollution = typeConfig.pollution();

        // 污染系统禁用时，直接标记为可接管（简化测试流程）
        if (pollution == null || !pollution.enabled()) {
            LOGGER.info("净化阵法：污染系统禁用，基地 {} 直接标记为可接管", bastion.id());
            return bastion.withCapturable(true, BastionData.CaptureReason.PURIFICATION_READY, 0L);
        }

        double currentPollution = bastion.pollution();
        double reduced = Math.max(0.0, currentPollution - structure.purifyAmount);

        if (currentPollution <= 0.0) {
            return bastion.withCapturable(true, BastionData.CaptureReason.PURIFICATION_READY, 0L);
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

    /**
     * 检测结构类型：优先 5x5 蓝图，回退旧四邻结构。
     *
     * @param level 世界实例
     * @param pos 阵法中心
     * @return 匹配到的结构类型
     */
    private static StructureType detectStructure(ServerLevel level, BlockPos pos) {
        // 优先检测 5x5 蓝图（高级结构）
        var matches = BastionMultiblockService.getMatchingBlueprints(level, pos);
        for (var match : matches) {
            if (BastionBlueprintManager.PURIFICATION_ARRAY_ID.equals(match.blueprintId())) {
                return StructureType.ADVANCED;
            }
        }

        // 回退到旧的四邻结构
        if (isValidLegacyStructure(level, pos)) {
            return StructureType.BASIC;
        }

        return StructureType.NONE;
    }

    /**
     * 旧四邻结构检测，保持向后兼容。
     *
     * @param level 世界实例
     * @param pos 阵法中心
     * @return 是否满足旧结构
     */
    private static boolean isValidLegacyStructure(final ServerLevel level, final BlockPos pos) {
        for (Direction dir : Constants.HORIZONTAL_DIRECTIONS) {
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
