package com.Kizunad.customNPCs.ai.personality;

/**
 * 七情 (Seven Emotions) - 动态情绪状态
 * <p>
 * 取值范围：0.0 (无) ~ 1.0 (极致)
 * 随时间自然衰减，受事件触发快速变化
 */
public enum EmotionType {
    /**
     * 喜 (Joy) - 喜悦
     * 触发：获得宝物、突破成功、好友重逢
     * 影响：⬆️ 社交、交易 ⬇️ 攻击性
     */
    JOY("joy", "喜"),
    
    /**
     * 怒 (Anger) - 愤怒
     * 触发：被攻击、被抢夺、被羞辱
     * 影响：⬆️ 攻击、追击 ⬇️ 逃跑、理智判断
     */
    ANGER("anger", "怒"),
    
    /**
     * 哀 (Sorrow) - 悲伤
     * 触发：重伤、好友死亡、突破失败
     * 影响：⬇️ 所有活跃行为 ⬆️ 发呆、消极状态
     */
    SORROW("sorrow", "哀"),
    
    /**
     * 惧 (Fear) - 恐惧
     * 触发：面对强敌、低血量、环境危险
     * 影响：⬆️ 逃跑、求饶、躲藏 ⬇️ 攻击、探索
     */
    FEAR("fear", "惧"),
    
    /**
     * 爱 (Love) - 喜爱
     * 触发：面对好感度高的对象
     * 影响：⬆️ 援助、赠送、跟随 ⬇️ 攻击该对象
     */
    LOVE("love", "爱"),
    
    /**
     * 恶 (Hate) - 憎恨
     * 触发：面对厌恶对象
     * 影响：⬆️ 攻击、陷害、拒绝交互
     */
    HATE("hate", "恶"),
    
    /**
     * 欲 (Desire) - 欲望强度
     * 作为总情绪强度，放大所有行为积极性
     */
    DESIRE("desire", "欲");
    
    private final String id;
    private final String chineseName;
    
    EmotionType(String id, String chineseName) {
        this.id = id;
        this.chineseName = chineseName;
    }
    
    public String getId() {
        return id;
    }
    
    public String getChineseName() {
        return chineseName;
    }
}
