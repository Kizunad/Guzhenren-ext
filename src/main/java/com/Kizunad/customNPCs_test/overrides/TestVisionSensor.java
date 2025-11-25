package com.Kizunad.customNPCs_test.overrides;

import com.Kizunad.customNPCs.ai.sensors.VisionSensor;
import com.Kizunad.customNPCs_test.utils.TestContextManager;
import net.minecraft.world.entity.LivingEntity;

/**
 * 测试专用的视觉传感器
 * 添加了测试上下文过滤
 */
public class TestVisionSensor extends VisionSensor {
    
    public TestVisionSensor() {
        super();
    }

    public TestVisionSensor(double range) {
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
