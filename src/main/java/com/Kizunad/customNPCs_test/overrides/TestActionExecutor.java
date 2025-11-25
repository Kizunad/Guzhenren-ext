package com.Kizunad.customNPCs_test.overrides;

import com.Kizunad.customNPCs.ai.executor.ActionExecutor;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.TestContextManager;
import net.minecraft.world.entity.LivingEntity;

/**
 * 测试专用的动作执行器
 * 添加了测试上下文隔离逻辑
 */
public class TestActionExecutor extends ActionExecutor {
    private String boundTestTag;
    
    @Override
    public void bindToEntity(LivingEntity entity) {
        super.bindToEntity(entity);
        
        // 提取并绑定测试标签
        this.boundTestTag = TestContextManager.extractTestTag(entity);
        if (this.boundTestTag != null) {
            System.out.println("[TestActionExecutor] 绑定到测试上下文: " + this.boundTestTag);
        }
    }
    
    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 验证测试上下文
        if (boundTestTag != null) {
            String currentTag = TestContextManager.extractTestTag(entity);
            if (!boundTestTag.equals(currentTag)) {
                System.err.println("[TestActionExecutor] 检测到测试上下文变化或丢失,丢弃计划");
                stopCurrentPlan();
                return;
            }
        }
        
        super.tick(mind, entity);
    }
}
