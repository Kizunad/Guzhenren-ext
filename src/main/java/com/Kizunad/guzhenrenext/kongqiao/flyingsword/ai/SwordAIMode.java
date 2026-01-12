package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ai;

/**
 * 飞剑 AI 模式枚举。
 * <p>
 * 从 {@code FlyingSwordEntity} 中提取，独立管理飞剑行为状态。
 * </p>
 */
public enum SwordAIMode {

    /**
     * 环绕：围绕主人旋转飞行，不主动攻击。
     */
    ORBIT,

    /**
     * 防御：守护主人周围，攻击进入范围的敌对目标。
     */
    GUARD,

    /**
     * 狩猎：主动搜索并追击目标，范围更大、更激进。
     */
    HUNT,

    /**
     * 悬停：在主人头顶静止悬停。
     */
    HOVER,

    /**
     * 召回：返回主人身边，到达后存入存储。
     */
    RECALL;

    /**
     * 是否为战斗模式（需要目标扫描）。
     */
    public boolean isCombatMode() {
        return this == GUARD || this == HUNT;
    }

    /**
     * 是否为被动模式（不需要目标）。
     */
    public boolean isPassiveMode() {
        return this == ORBIT || this == HOVER;
    }

    /**
     * 循环切换到下一个模式（不包含 RECALL）。
     */
    public SwordAIMode cycleNext() {
        return switch (this) {
            case ORBIT -> HOVER;
            case HOVER -> GUARD;
            case GUARD -> HUNT;
            case HUNT -> ORBIT;
            case RECALL -> ORBIT;
        };
    }

    /**
     * 从 ordinal 安全解析。
     */
    public static SwordAIMode fromOrdinal(int ordinal) {
        SwordAIMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return ORBIT;
        }
        return values[ordinal];
    }
}
