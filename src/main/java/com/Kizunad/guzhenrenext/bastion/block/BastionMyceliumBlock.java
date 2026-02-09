package com.Kizunad.guzhenrenext.bastion.block;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.item.context.BlockPlaceContext;

/**
 * 基地菌毯方块（贴地蔓延主网）。
 * <p>
 * 设计目标：
 * <ul>
 *   <li>无 BlockEntity，性能友好</li>
 *   <li>硬度低、易清理</li>
 *   <li>不直接触发节点掉落/威胁事件（这些由 Anchor 节点承担）</li>
 *   <li>不同道途显示不同材质</li>
 * </ul>
 * </p>
 */
public class BastionMyceliumBlock extends Block {

    /** 道途属性：决定菌毯的外观材质。 */
    public static final EnumProperty<BastionDao> DAO = EnumProperty.create("dao", BastionDao.class);

    /** 查找归属基地的最大搜索半径（与交互/Anchor 逻辑对齐）。 */
    private static final int MAX_OWNER_SEARCH_RADIUS = 128;

    public BastionMyceliumBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DAO, BastionDao.ZHI_DAO));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DAO);
    }

    /**
     * 创建指定道途的方块状态。
     */
    public BlockState withDao(BastionDao dao) {
        return this.defaultBlockState().setValue(DAO, dao != null ? dao : BastionDao.ZHI_DAO);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // 仅在服务端且方块实际被替换/移除时处理。
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            if (level instanceof ServerLevel serverLevel) {
                handleMyceliumRemoved(serverLevel, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * 菌毯被移除时的最小化缓存修复。
     * <p>
     * 目的：确保连通性/衰败逻辑不会被“缓存撒谎”误导。
     * </p>
     */
    private static void handleMyceliumRemoved(ServerLevel level, BlockPos pos) {
        BastionSavedData savedData = BastionSavedData.get(level);

        // 在半径内寻找归属基地：该方法本身就是设计上的“非重叠约束”入口。
        // 回合2.1.1：findOwnerBastion 会优先查索引，因此可在半径不足时仍命中正确归属。
        BastionData owner = savedData.findOwnerBastion(pos, MAX_OWNER_SEARCH_RADIUS);

        if (owner == null) {
            return;
        }

        // 从缓存中移除节点（若本来不在缓存，该调用为幂等）。
        savedData.removeNodeFromCache(owner.id(), pos);

        // 清理运行时衰败状态，防止 map 残留。
        savedData.clearMyceliumDecay(owner.id(), pos);

        // 这里不再修改 totalMycelium：其语义已复用为“领土 chunk 数”，
        // 仅应由 territory claim/decay 逻辑驱动，不能被装饰菌毯破坏影响。
        // 当前函数只保留最小化缓存修复，避免连通性/衰败状态出现脏数据。
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // 允许寻路穿过（类似地毯/苔藓的“地表覆盖物”体验）。
        return true;
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState state) {
        // 允许在其上方复活判定更宽松（与“覆盖物”定位一致）。
        return true;
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        // 避免当作完整方块影响碰撞/渲染判断。
        return false;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        Block placingBlock = context.getItemInHand().getItem() instanceof net.minecraft.world.item.BlockItem blockItem
            ? blockItem.getBlock() : null;
        if (placingBlock == null) {
            return false;
        }
        // 允许基地特殊节点方块替换菌毯
        return placingBlock == BastionBlocks.BASTION_ENERGY_NODE.get()
            || placingBlock == BastionBlocks.BASTION_AURA_NODE.get()
            || placingBlock == BastionBlocks.BASTION_GUARDIAN_HATCHERY.get()
            || placingBlock == BastionBlocks.BASTION_TURRET.get()
            || placingBlock == BastionBlocks.BASTION_TRAP.get()
            || placingBlock == BastionBlocks.BASTION_CHITIN_SHELL.get()
            || placingBlock == BastionBlocks.BASTION_ANTI_EXPLOSION_SHELL.get()
            || placingBlock == BastionBlocks.BASTION_ANTI_FIRE_SHELL.get()
            || placingBlock == BastionBlocks.BASTION_ANCHOR.get();
    }
}
