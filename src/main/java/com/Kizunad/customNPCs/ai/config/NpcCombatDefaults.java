package com.Kizunad.customNPCs.ai.config;

/**
 * NPC 战斗/生存相关的基线默认值。
 * <p>
 * 集中存放可调参数，便于后续统一调优或配置化。
 */
public final class NpcCombatDefaults {

    private NpcCombatDefaults() {}

    /** 盾牌举盾最短持续时间（tick），避免瞬时抖动。 */
    public static final int SHIELD_MIN_RAISE_TICKS = 30;

    /** 盾牌举盾结束后的冷却（tick），防止连续硬直。 */
    public static final int SHIELD_COOLDOWN_TICKS = 20;

    /**
     * 盾牌格挡的有效夹角（度）。目前沿用原版约 120°，
     * 主要用于文档/日志标示，实际判定由原版逻辑决定。
     */
    public static final double SHIELD_BLOCK_ARC_DEGREES = 120.0D;

    /** 触发治疗的血量比例阈值（<= 时尝试治疗）。 */
    public static final float HEAL_TRIGGER_RATIO = 0.5f;

    /** 认为已恢复健康的血量比例阈值（>= 时结束治疗）。 */
    public static final float HEAL_HEALTHY_RATIO = 0.8f;

    /** 治疗状态在短期记忆中的存活时间（tick）。 */
    public static final int HEAL_MEMORY_TICKS = 200;
}
