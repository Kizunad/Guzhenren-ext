package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 听觉传感器 - 让 NPC 能"听到"周围的声音事件
 * <p>
 * 功能：
 * - 扫描周围实体的活动（移动、战斗、受伤）
 * - 将活动转换为"声音事件"
 * - 计算声音强度（基于距离衰减）
 * - 将听到的声音信息存入记忆
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class AuditorySensor implements ISensor {
    
    private static final double DEFAULT_RANGE = 24.0D; // 默认听觉范围（格）
    // 单 tick 扫描，避免短暂事件（位移/受伤）的声音在衰减前被遗漏
    private static final int SCAN_INTERVAL = 1;
    private static final int MEMORY_DURATION = 200; // 记忆持续时间（10秒）
    private static final int SENSOR_PRIORITY = 20; // 传感器优先级（高于视觉的 10）
    private static final int MAX_STORED_EVENTS = 5; // 存储的最大声音事件数
    
    private final double range;
    
    public AuditorySensor() {
        this(DEFAULT_RANGE);
    }
    
    public AuditorySensor(double range) {
        this.range = range;
    }
    
    @Override
    public String getName() {
        return "auditory";
    }
    
    @Override
    public void sense(INpcMind mind, LivingEntity observer, ServerLevel level) {
        // 获取扫描范围
        Vec3 position = observer.position();
        AABB searchBox = new AABB(position, position).inflate(range);
        
        // 扫描范围内的所有生物
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            e -> isValidEntity(observer, e)
        );
        
        // 收集所有声音事件
        List<SoundEvent> soundEvents = new ArrayList<>();
        long currentTick = level.getGameTime();
        
        for (LivingEntity entity : nearbyEntities) {
            List<String> detectedSounds = detectSounds(entity, currentTick);
            
            if (!detectedSounds.isEmpty()) {
                // 计算声音强度（基于距离衰减）
                double distance = observer.distanceTo(entity);
                double intensity = Math.max(0.0, 1.0 - (distance / range));
                
                // 创建声音事件
                SoundEvent soundEvent = new SoundEvent(
                    detectedSounds,
                    entity.position(),
                    entity.getUUID(),
                    entity.getType().getDescription().getString(),
                    intensity
                );
                soundEvents.add(soundEvent);
            }
        }
        
        // 更新记忆
        updateMemory(mind, soundEvents);
    }
    
    /**
     * 检测实体产生的声音
     * 
     * @param entity 实体
     * @param currentTick 当前 tick
     * @return 检测到的声音类型列表
     */
    private List<String> detectSounds(LivingEntity entity, long currentTick) {
        List<String> sounds = new ArrayList<>();
        
        // 移动声音（速度阈值：避免微小抖动）
        if (entity.getDeltaMovement().lengthSqr() > 0.01) {
            sounds.add("movement");
        }
        
        // 攻击声音（最近 20 ticks 内攻击过其他实体）
        if (entity.getLastHurtMobTimestamp() > currentTick - 20) {
            sounds.add("combat");
        }
        
        // 受伤声音（hurtTime > 0 表示刚受伤，hurtTime 会逐渐减少到0）
        if (entity.hurtTime > 0) {
            sounds.add("pain");
        }
        
        return sounds;
    }
    
    /**
     * 更新记忆中的声音信息
     * 
     * @param mind NPC 思维
     * @param soundEvents 检测到的声音事件列表
     */
    private void updateMemory(INpcMind mind, List<SoundEvent> soundEvents) {
        // 清除旧的听觉记忆
        mind.getMemory().forget("heard_sounds_count");
        mind.getMemory().forget("heard_sound_events");
        mind.getMemory().forget("loudest_sound_types");
        mind.getMemory().forget("loudest_sound_location");
        mind.getMemory().forget("loudest_sound_source");
        mind.getMemory().forget("loudest_sound_source_type");
        mind.getMemory().forget("loudest_sound_intensity");
        
        // 存储声音事件数量
        mind.getMemory().rememberShortTerm("heard_sounds_count", soundEvents.size(), MEMORY_DURATION);
        
        if (soundEvents.isEmpty()) {
            return;
        }
        
        // 存储声音事件列表（限制数量）
        List<SoundEvent> limitedEvents = soundEvents.stream()
            .limit(MAX_STORED_EVENTS)
            .toList();
        mind.getMemory().rememberShortTerm("heard_sound_events", limitedEvents, MEMORY_DURATION);
        
        // 找到最响亮的声音
        SoundEvent loudest = soundEvents.stream()
            .max((a, b) -> Double.compare(a.intensity(), b.intensity()))
            .orElse(null);
        
        if (loudest != null) {
            // 存储最响亮声音的信息
            mind.getMemory().rememberShortTerm("loudest_sound_types", 
                loudest.soundTypes(), MEMORY_DURATION);
            mind.getMemory().rememberShortTerm("loudest_sound_location", 
                loudest.location(), MEMORY_DURATION);
            mind.getMemory().rememberShortTerm("loudest_sound_source", 
                loudest.sourceEntity().toString(), MEMORY_DURATION);
            mind.getMemory().rememberShortTerm("loudest_sound_source_type", 
                loudest.sourceEntityType(), MEMORY_DURATION);
            mind.getMemory().rememberShortTerm("loudest_sound_intensity", 
                loudest.intensity(), MEMORY_DURATION);
            
            // DEBUG
            System.out.println("[AuditorySensor] 听到声音: " + 
                String.join("+", loudest.soundTypes()) + 
                " 来自 " + loudest.sourceEntityType() +
                " 强度: " + String.format("%.2f", loudest.intensity()));
        }
    }
    
    @Override
    public boolean shouldSense(long tickCount) {
        // 每 10 ticks 扫描一次以优化性能
        return tickCount % SCAN_INTERVAL == 0;
    }
    
    @Override
    public int getPriority() {
        return SENSOR_PRIORITY; // 听觉是重要的感知，高优先级
    }

    /**
     * 检查实体是否是有效的感知目标
     * @param observer 观察者
     * @param target 目标
     * @return 是否有效
     */
    protected boolean isValidEntity(LivingEntity observer, LivingEntity target) {
        return target != observer && target.isAlive();
    }
    
    /**
     * 声音事件记录
     * 
     * @param soundTypes 声音类型列表（可能同时有多种）
     * @param location 声音位置
     * @param sourceEntity 来源实体 UUID
     * @param sourceEntityType 来源实体类型名称
     * @param intensity 声音强度 (0.0-1.0)
     */
    public record SoundEvent(
        List<String> soundTypes,
        Vec3 location,
        UUID sourceEntity,
        String sourceEntityType,
        double intensity
    ) {}
}
