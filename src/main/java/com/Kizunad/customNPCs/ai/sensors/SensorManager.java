package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 传感器管理器 - 管理和协调 NPC 的所有传感器
 */
public class SensorManager {
    
    private final List<ISensor> sensors;
    private long tickCount;
    
    public SensorManager() {
        this.sensors = new ArrayList<>();
        this.tickCount = 0;
    }
    
    /**
     * 注册传感器
     * @param sensor 传感器实例
     */
    public void registerSensor(ISensor sensor) {
        sensors.add(sensor);
        // 按优先级排序（优先级高的先执行）
        sensors.sort(Comparator.comparingInt(ISensor::getPriority).reversed());
    }
    
    /**
     * 移除传感器
     * @param sensorName 传感器名称
     */
    public void removeSensor(String sensorName) {
        sensors.removeIf(s -> s.getName().equals(sensorName));
    }
    
    /**
     * 执行所有传感器的感知
     * @param mind NPC 思维
     * @param entity NPC 实体
     * @param level 服务器世界
     */
    public void tick(INpcMind mind, LivingEntity entity, ServerLevel level) {
        tickCount++;
        
        for (ISensor sensor : sensors) {
            if (sensor.shouldSense(tickCount)) {
                sensor.sense(mind, entity, level);
            }
        }
    }
    
    /**
     * 获取所有已注册的传感器
     * @return 传感器列表
     */
    public List<ISensor> getSensors() {
        return new ArrayList<>(sensors);
    }
}
