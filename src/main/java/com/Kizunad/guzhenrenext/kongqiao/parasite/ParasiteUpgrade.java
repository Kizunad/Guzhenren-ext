package com.Kizunad.guzhenrenext.kongqiao.parasite;

/**
 * 寄生升级节点定义。
 * <p>
 * 仅包含基础说明与默认最大等级，具体效果在服务端逻辑中解释。
 * </p>
 */
public enum ParasiteUpgrade {

    /**
     * 渗透增强：在基地领域内减少资源消耗。
     */
    INFILTRATION_BOOST("渗透增强", "在基地领域内减少资源消耗", 1),

    /**
     * 隐匿步伐：减少触发威胁值的概率。
     */
    STEALTH_STEPS("隐匿步伐", "减少触发威胁值的概率", 1),

    /**
     * 菌毯亲和：在菌毯上获得速度加成。
     */
    MYCELIUM_AFFINITY("菌毯亲和", "在菌毯上获得速度加成", 1);

    private final String displayName;
    private final String effectDescription;
    private final int maxLevel;

    ParasiteUpgrade(String displayName, String effectDescription, int maxLevel) {
        this.displayName = displayName;
        this.effectDescription = effectDescription;
        this.maxLevel = maxLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEffectDescription() {
        return effectDescription;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
