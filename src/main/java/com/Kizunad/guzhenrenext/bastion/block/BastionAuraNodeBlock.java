package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionNodePlacementHelper;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.aura.AuraNodeType;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * 光环节点方块。
 * <p>
 * Round 5.2：可建造的光环节点，用于承载/触发光环叠加规则。
 * 必须放在 Anchor 上方。
 * </p>
 */
public class BastionAuraNodeBlock extends Block {

    /** 光环节点方块物品（放置时校验）。 */
    public static class BastionAuraNodeItem extends BlockItem {

        public BastionAuraNodeItem(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
            if (context == null) {
                return false;
            }

            if (context.getLevel().isClientSide()) {
                return super.placeBlock(context, state);
            }

            if (!(context.getLevel() instanceof ServerLevel serverLevel)
                || !(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
                return false;
            }

            BastionSavedData savedData = BastionSavedData.get(serverLevel);

            boolean ok = BastionAuraNodeBlock.tryBuildAuraNode(
                serverLevel,
                savedData,
                serverPlayer,
                context.getClickedPos(),
                state
            );

            return ok;
        }
    }

    /** 归属查找最大半径（与 Anchor/能源建造对齐）。 */
    private static final int MAX_OWNER_SEARCH_RADIUS = 128;

    /** 光环类型属性。 */
    public static final EnumProperty<AuraNodeType> AURA_TYPE = EnumProperty.create(
        "aura_type",
        AuraNodeType.class
    );

    public BastionAuraNodeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(AURA_TYPE, AuraNodeType.BUFF)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AURA_TYPE);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 作为功能节点，不希望被当作可穿行方块。
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 结构约束：光环节点必须依附在 Anchor 上。
        BlockState below = level.getBlockState(pos.below());
        return below.getBlock() instanceof BastionAnchorBlock;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos currentPos,
            BlockPos neighborPos) {

        // Anchor 被拆：光环节点应立即掉落（这里直接变为空气，掉落由方块掉落表决定）。
        if (direction == Direction.DOWN && !canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        // 光环节点作为“装置”保持完整方块碰撞。
        return true;
    }

    /**
     * 校验并尝试建造光环节点（服务端权威）。
     * <p>
     * 约束：
     * <ul>
     *     <li>必须放在 Anchor 上方</li>
     *     <li>Anchor 必须归属于某个基地</li>
     *     <li>扣除基地资源池（按 bastionType.aura.buildCost）</li>
     *     <li>遵守光环节点数量上限（bastionType.aura.maxCount，0 表示不启用上限）</li>
     * </ul>
     * </p>
     */
    public static boolean tryBuildAuraNode(
            ServerLevel level,
            BastionSavedData savedData,
            ServerPlayer player,
            BlockPos placePos,
            BlockState placeState) {

        if (level == null || savedData == null || player == null || placePos == null || placeState == null) {
            return false;
        }

        if (!isValidAuraNodePlacement(level, placePos)) {
            player.sendSystemMessage(Component.literal("§c光环节点只能放在 Anchor 上方"));
            return false;
        }

        BlockPos anchorPos = placePos.below();
        BastionData owner = savedData.findOwnerBastion(anchorPos, MAX_OWNER_SEARCH_RADIUS);
        if (owner == null) {
            player.sendSystemMessage(Component.literal("§c该 Anchor 未归属于任何基地，无法建造光环节点"));
            return false;
        }

        // 连通性校验：Anchor 必须与基地菌毯网络连通。
        if (!BastionNodePlacementHelper.ensureConnected(level, savedData, owner, anchorPos, player)) {
            return false;
        }

        // 确保 anchor 缓存已初始化并记录当前 Anchor。
        if (!savedData.hasAnchorCache(owner.id())) {
            savedData.initializeAnchorCacheFromCore(owner.id(), owner.corePos());
        }
        savedData.addAnchorToCache(owner.id(), anchorPos);

        BastionTypeConfig typeConfig = BastionTypeManager.getOrDefault(owner.bastionType());
        BastionTypeConfig.AuraConfig auraConfig = typeConfig.aura();

        double buildCost = Math.max(0.0, auraConfig.buildCost());
        int maxCount = Math.max(0, auraConfig.maxCount());

        if (maxCount > 0) {
            int currentCount = countAuraNodesForBastion(savedData, level, owner.id());
            if (currentCount >= maxCount) {
                player.sendSystemMessage(Component.literal(
                    "§c该基地的光环节点已达到上限：" + maxCount
                ));
                return false;
            }
        }

        if (owner.resourcePool() < buildCost) {
            player.sendSystemMessage(Component.literal(
                "§c基地资源池不足，无法建造光环节点：需要=" + buildCost + "，当前=" + owner.resourcePool()
            ));
            return false;
        }

        BastionData updated = owner.withResourcePool(owner.resourcePool() - buildCost);
        savedData.updateBastion(updated);

        boolean placed = level.setBlock(placePos, placeState, Block.UPDATE_ALL);
        if (!placed) {
            // 放置失败：回滚资源池。
            savedData.updateBastion(owner);
            player.sendSystemMessage(Component.literal("§c放置失败，已取消建造"));
            return false;
        }

        return true;
    }

    /** 放置约束：必须位于 Anchor 上方一格。 */
    public static boolean isValidAuraNodePlacement(ServerLevel level, BlockPos placePos) {
        if (level == null || placePos == null) {
            return false;
        }
        BlockState below = level.getBlockState(placePos.below());
        return below.getBlock() instanceof BastionAnchorBlock;
    }

    /**
     * 统计指定基地的光环节点数量。
     * <p>
     * 数据源：运行时 anchorCache + 世界方块状态（Anchor 上方为光环节点即计数）。
     * </p>
     */
    private static int countAuraNodesForBastion(BastionSavedData savedData, ServerLevel level, UUID bastionId) {
        Set<BlockPos> anchors = savedData.getAnchors(bastionId);
        if (anchors == null || anchors.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (BlockPos anchorPos : anchors) {
            BlockState above = level.getBlockState(anchorPos.above());
            if (above.getBlock() instanceof BastionAuraNodeBlock) {
                count++;
            }
        }
        return count;
    }
}
