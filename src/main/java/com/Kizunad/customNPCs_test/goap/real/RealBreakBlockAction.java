package com.Kizunad.customNPCs_test.goap.real;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.level.block.state.BlockState;

/**
 * 真实的破坏方块 GOAP 动作
 * <p>
 * 使用 Minecraft 的 level.destroyBlock() API 真正破坏方块
 * <p>
 * 前置条件: at_tree_location = true
 * 效果: tree_broken = true
 * 代价: 3.0
 */
public class RealBreakBlockAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private final BlockPos blockPos;
    private int tickCount;
    private BlockState originalBlock;
    private static final int BREAK_TIME = 20; // 破坏方块需要 20 ticks
    
    public RealBreakBlockAction(BlockPos blockPos) {
        this.blockPos = blockPos;
        this.tickCount = 0;
        
        // 前置条件：在树木位置
        this.preconditions = new WorldState();
        this.preconditions.setState("at_tree_location", true);
        
        // 效果：树木已破坏
        this.effects = new WorldState();
        this.effects.setState("tree_broken", true);
    }
    
    @Override
    public WorldState getPreconditions() {
        return preconditions;
    }
    
    @Override
    public WorldState getEffects() {
        return effects;
    }
    
    @Override
    public float getCost() {
        return 3.0f;
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        tickCount++;
        
        // 检查实体所在的是服务器世界
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            System.err.println("[RealBreakBlockAction] 只能在服务器端破坏方块");
            return ActionStatus.FAILURE;
        }
        
        // 模拟破坏时间
        if (tickCount < BREAK_TIME) {
            if (tickCount % 5 == 0) { // 每 5 ticks 打印一次进度
                System.out.println("[RealBreakBlockAction] 正在破坏方块... (" 
                    + tickCount + "/" + BREAK_TIME + ")");
            }
            return ActionStatus.RUNNING;
        }
        
        // 检查方块是否还存在
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (blockState.isAir()) {
            System.out.println("[RealBreakBlockAction] 方块已经不存在");
            mind.getMemory().rememberLongTerm("tree_broken", true);
            return ActionStatus.SUCCESS;
        }
        
        // 使用真实的 Minecraft API 破坏方块
        // true = 掉落物品
        boolean destroyed = serverLevel.destroyBlock(blockPos, true, entity);
        
        if (destroyed) {
            System.out.println("[RealBreakBlockAction] 成功破坏方块: " + blockState.getBlock().getName().getString());
            mind.getMemory().rememberLongTerm("tree_broken", true);
            return ActionStatus.SUCCESS;
        } else {
            System.err.println("[RealBreakBlockAction] 无法破坏方块");
            return ActionStatus.FAILURE;
        }
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        tickCount = 0;
        
        // 记录原始方块状态
        if (entity.level() instanceof ServerLevel serverLevel) {
            originalBlock = serverLevel.getBlockState(blockPos);
            System.out.println("[RealBreakBlockAction] 开始破坏方块: " 
                + originalBlock.getBlock().getName().getString() 
                + " at " + blockPos);
        }
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[RealBreakBlockAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return false; // 破坏方块不能被中断
    }
    
    @Override
    public String getName() {
        return "real_break_block";
    }
}
