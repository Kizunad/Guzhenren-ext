package com.Kizunad.guzhenrenext.customNPCImpl.ai.Sensor;

import com.Kizunad.customNPCs.ai.sensors.ISensor;
import com.Kizunad.guzhenrenext.guzhenrenBridge.EntityHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 蛊真人模组传感器基类。
 * <p>
 * 提供与蛊真人模组相关的通用传感器辅助方法和框架。
 */
public abstract class AbstractGuzhenrenSensor implements ISensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AbstractGuzhenrenSensor.class
    );
    private final String name;

    private static final int DEFAULT_SCAN_INTERVAL = 20;

    protected AbstractGuzhenrenSensor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    // --- 蛊真人特定辅助方法 ---
    /**
     * 检查实体是否为蛊师（例如，是否拥有空窍Capability）。
     */
    protected boolean isGuMaster(LivingEntity entity) {
        return EntityHelper.isGuMaster(entity);
    }

    /**
     * 获取实体的真元百分比。
     */
    protected float getPrimevalEssencePercentage(LivingEntity entity) {
        return ZhenYuanHelper.getPercentage(entity);
    }

    // 默认实现 ISensor 的其他方法，子类可按需覆盖
    @Override
    public int getScanInterval() {
        return DEFAULT_SCAN_INTERVAL; // 默认每秒扫描一次，子类可覆盖
    }

    @Override
    public int getPriority() {
        return 0; // 默认优先级，子类可覆盖
    }
}
