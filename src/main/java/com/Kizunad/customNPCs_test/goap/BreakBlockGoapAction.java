package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * 破坏方块（砍树）的 GOAP 动作
 * <p>
 * 前置条件: at_tree_location = true
 * 效果: tree_broken = true
 * 代价: 3.0
 */
public class BreakBlockGoapAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private final BlockPos blockPos;
    private int tickCount;
    
    public BreakBlockGoapAction(BlockPos blockPos) {
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
        return 3.0f; // 破坏方块需要时间
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        tickCount++;
        
        Level level = entity.level();
        
        // 模拟破坏方块需要时间（20 ticks）
        if (tickCount >= 20) {
            // 破坏方块
            if (level.getBlockState(blockPos).is(Blocks.OAK_LOG)) {
                level.destroyBlock(blockPos, true); // true = 掉落物品
                System.out.println("[BreakBlockGoapAction] 方块已破坏");
            }
            
            // 设置记忆
            mind.getMemory().rememberLongTerm("tree_broken", true);
            
            return ActionStatus.SUCCESS;
        }
        
        System.out.println("[BreakBlockGoapAction] 正在破坏方块... (" + tickCount + "/20)");
        return ActionStatus.RUNNING;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        tickCount = 0;
        System.out.println("[BreakBlockGoapAction] 开始破坏方块: " + blockPos);
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[BreakBlockGoapAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return false; // 破坏方块时不能被中断
    }
    
    @Override
    public String getName() {
        return "break_block";
    }
}
