package com.Kizunad.customNPCs.ai.actions.base;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 等待动作 - 等待指定数量的 tick
 * <p>
 * 这是最简单的动作，主要用于：
 * - 在动作序列中添加延迟
 * - 测试动作队列机制
 */
public class WaitAction implements IAction {
    
    private final int targetTicks;
    private int currentTicks;
    
    /**
     * 创建等待动作
     * @param ticks 等待的 tick 数量
     */
    public WaitAction(int ticks) {
        this.targetTicks = ticks;
        this.currentTicks = 0;
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        currentTicks++;
        
        if (currentTicks >= targetTicks) {
            return ActionStatus.SUCCESS;
        }
        
        return ActionStatus.RUNNING;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        currentTicks = 0;
        System.out.println("[WaitAction] 开始等待 " + targetTicks + " ticks");
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[WaitAction] 等待结束，已等待 " + currentTicks + "/" + targetTicks + " ticks");
    }
    
    @Override
    public boolean canInterrupt() {
        // 等待动作可以被中断
        return true;
    }
    
    @Override
    public String getName() {
        return "wait_" + targetTicks;
    }
}
