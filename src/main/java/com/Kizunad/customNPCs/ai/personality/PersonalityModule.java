package com.Kizunad.customNPCs.ai.personality;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * 性格模块 - 基于七情六欲的 NPC 性格系统
 * <p>
 * 管理 NPC 的情绪状态（动态）和性格特征（静态）
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class PersonalityModule {
    
    // 七情 (动态情绪)
    private final Map<EmotionType, Float> emotions;
    
    // 六欲 (静态性格)
    private final Map<DriveType, Float> drives;
    
    // 情绪衰减速率 (每 tick)
    private static final float EMOTION_DECAY_RATE = 0.001f; // 约 1000 ticks (50秒) 完全衰减
    
    /**
     * 创建随机性格的 NPC
     */
    public PersonalityModule() {
        this(new Random());
    }
    
    /**
     * 创建指定随机种子的 NPC（用于测试）
     */
    public PersonalityModule(Random random) {
        emotions = new EnumMap<>(EmotionType.class);
        drives = new EnumMap<>(DriveType.class);
        
        // 初始化情绪为 0
        for (EmotionType emotion : EmotionType.values()) {
            emotions.put(emotion, 0.0f);
        }
        
        // 随机生成六欲（倾向于中等值，使用高斯分布）
        for (DriveType drive : DriveType.values()) {
            float value = (float) Math.max(0.0, Math.min(1.0, random.nextGaussian() * 0.2 + 0.5));
            drives.put(drive, value);
        }
    }
    
    /**
     * 创建指定性格的 NPC（手动配置）
     */
    public PersonalityModule(Map<DriveType, Float> customDrives) {
        emotions = new EnumMap<>(EmotionType.class);
        drives = new EnumMap<>(DriveType.class);
        
        // 初始化情绪为 0
        for (EmotionType emotion : EmotionType.values()) {
            emotions.put(emotion, 0.0f);
        }
        
        // 使用自定义六欲
        for (DriveType drive : DriveType.values()) {
            drives.put(drive, customDrives.getOrDefault(drive, 0.5f));
        }
    }
    
    /**
     * 每 tick 更新（主要用于情绪衰减）
     */
    public void tick() {
        // 所有情绪自然衰减
        for (EmotionType emotion : EmotionType.values()) {
            float current = emotions.get(emotion);
            if (current > 0) {
                emotions.put(emotion, Math.max(0, current - EMOTION_DECAY_RATE));
            }
        }
    }
    
    /**
     * 触发情绪变化
     * @param emotion 情绪类型
     * @param delta 变化量 (-1.0 ~ +1.0)
     */
    public void triggerEmotion(EmotionType emotion, float delta) {
        float current = emotions.get(emotion);
        float newValue = Math.max(0.0f, Math.min(1.0f, current + delta));
        emotions.put(emotion, newValue);
    }
    
    /**
     * 获取情绪值
     */
    public float getEmotion(EmotionType emotion) {
        return emotions.get(emotion);
    }
    
    /**
     * 获取性格值
     */
    public float getDrive(DriveType drive) {
        return drives.get(drive);
    }
    
    /**
     * 设置性格值（用于测试或剧情事件）
     */
    public void setDrive(DriveType drive, float value) {
        drives.put(drive, Math.max(0.0f, Math.min(1.0f, value)));
    }
    
    /**
     * 计算针对特定目标类型的优先级修正
     * <p>
     * 返回值范围：-1.0 ~ +1.0
     * 最终应用：finalPriority = basePriority * (1.0 + modifier)
     * 
     * @param goalType 目标类型（如 "survival", "attack", "flee"）
     * @return 修正值
     */
    public float getModifierForGoal(String goalType) {
        float modifier = 0.0f;
        
        // 根据目标类型应用不同的性格和情绪影响
        switch (goalType.toLowerCase()) {
            case "survival":
            case "flee":
                // 生存/逃跑受 "生" 欲和 "惧" 情绪正向影响，"怒" 和 "名"（荣誉）负向影响 - 愤怒和自尊心让NPC不愿逃跑
                modifier += drives.get(DriveType.SURVIVAL) * 0.5f;
                modifier += emotions.get(EmotionType.FEAR) * 0.4f;
                modifier -= emotions.get(EmotionType.ANGER) * 0.3f;
                modifier -= drives.get(DriveType.PRIDE) * 0.8f; // Pride 强烈抑制逃跑
                break;
                
            case "attack":
            case "combat":
                // 攻击受 "名" 欲和 "怒" 情绪正向影响，"惧" 和 "哀" 负向影响
                modifier += drives.get(DriveType.PRIDE) * 0.4f;
                modifier += emotions.get(EmotionType.ANGER) * 0.5f;
                modifier -= emotions.get(EmotionType.FEAR) * 0.5f;
                modifier -= emotions.get(EmotionType.SORROW) * 0.3f;
                break;
                
            case "gather":
            case "loot":
                // 采集/舔包受 "味" 欲正向影响
                modifier += drives.get(DriveType.GREED) * 0.6f;
                modifier += emotions.get(EmotionType.DESIRE) * 0.2f;
                break;
                
            case "social":
            case "trade":
                // 社交/交易受 "色" 欲和 "喜" 情绪正向影响
                modifier += drives.get(DriveType.SOCIAL) * 0.5f;
                modifier += emotions.get(EmotionType.JOY) * 0.3f;
                modifier -= emotions.get(EmotionType.SORROW) * 0.4f;
                break;
                
            case "idle":
            case "rest":
                // 休息受 "触" 欲正向影响，"哀" 也会让人不想动
                modifier += drives.get(DriveType.COMFORT) * 0.4f;
                modifier += emotions.get(EmotionType.SORROW) * 0.3f;
                break;
                
            case "cultivate":
            case "explore":
                // 修炼/探索受 "法" 欲正向影响
                modifier += drives.get(DriveType.KNOWLEDGE) * 0.6f;
                modifier += emotions.get(EmotionType.DESIRE) * 0.2f;
                break;
        }
        
        // 限制修正范围
        return Math.max(-1.0f, Math.min(1.0f, modifier));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PersonalityModule{\n");
        sb.append("  七情 (Emotions):\n");
        for (EmotionType emotion : EmotionType.values()) {
            sb.append(String.format("    %s(%s): %.2f\n", 
                emotion.getChineseName(), emotion.getId(), emotions.get(emotion)));
        }
        sb.append("  六欲 (Drives):\n");
        for (DriveType drive : DriveType.values()) {
            sb.append(String.format("    %s(%s): %.2f\n", 
                drive.getChineseName(), drive.getId(), drives.get(drive)));
        }
        sb.append("}");
        return sb.toString();
    }
}
