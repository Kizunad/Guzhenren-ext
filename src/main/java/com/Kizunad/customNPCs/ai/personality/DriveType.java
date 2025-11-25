package com.Kizunad.customNPCs.ai.personality;

/**
 * 六欲 (Six Drives) - 静态性格特征/核心驱动力
 * <p>
 * 取值范围：0.0 (无欲) ~ 1.0 (极度渴望)
 * 生成时随机决定，后续变化缓慢
 */
public enum DriveType {
    /**
     * 生 (Survival) - 生存欲
     * 含义：贪生怕死 vs 视死如归
     * 影响：⬆️ SurvivalGoal (吃药/逃跑)，影响逃跑阈值
     */
    SURVIVAL("survival", "生"),

    /**
     * 色 (Lust/Social) - 社交欲
     * 含义：社交/伴侣需求
     * 影响：⬆️ SocialGoal (搭讪/双修/结拜)
     */
    SOCIAL("social", "色"),

    /**
     * 味 (Gluttony) - 贪婪
     * 含义：对资源/丹药的贪婪
     * 影响：⬆️ GatherGoal (采集), LootGoal (舔包)
     */
    GREED("greed", "味"),

    /**
     * 名 (Pride) - 荣耀
     * 含义：对地位/面子的追求
     * 影响：⬆️ ChallengeGoal (挑战), 不愿求饶
     */
    PRIDE("pride", "名"),

    /**
     * 触 (Comfort) - 享乐
     * 含义：对舒适/享受的追求
     * 影响：⬆️ IdleGoal (找椅子/泡澡), 厌恶恶劣环境
     */
    COMFORT("comfort", "触"),

    /**
     * 法 (Knowledge) - 求道
     * 含义：对力量/大道的追求
     * 影响：⬆️ CultivateGoal (修炼), ExploreGoal (探索遗迹)
     */
    KNOWLEDGE("knowledge", "法");

    private final String id;
    private final String chineseName;

    DriveType(String id, String chineseName) {
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
