package com.Kizunad.customNPCs_test.overrides;

import com.Kizunad.customNPCs.ai.sensors.AuditorySensor;
import com.Kizunad.customNPCs_test.utils.TestContextManager;
import net.minecraft.world.entity.LivingEntity;

/**
 * 测试专用的听觉传感器
 * 添加了测试上下文过滤
 */
public class TestAuditorySensor extends AuditorySensor {
    
    public TestAuditorySensor() {
        super();
    }

    public TestAuditorySensor(double range) {
        super(range);
    }

    @Override
    protected boolean isValidEntity(LivingEntity observer, LivingEntity target) {
        if (!super.isValidEntity(observer, target)) {
            return false;
        }
        // 使用 TestContextManager 检查是否在同一测试上下文
        return TestContextManager.createTestContextFilter(observer).test(target);
    }
}
