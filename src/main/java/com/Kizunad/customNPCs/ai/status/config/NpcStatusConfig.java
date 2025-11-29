package com.Kizunad.customNPCs.ai.status.config;

/**
 * NpcStatus 配置（饥饿/饱和/耗竭/回血/掉血）。
 * 默认对齐玩家体验。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public final class NpcStatusConfig {

    private static final NpcStatusConfig INSTANCE = new NpcStatusConfig();

    private static final int DEFAULT_MAX_HUNGER = 20;
    private static final float DEFAULT_MAX_SATURATION = 20.0f;
    private static final float DEFAULT_EXHAUSTION_THRESHOLD = 4.0f;

    private static final float DEFAULT_WALK_EXHAUSTION = 0.01f;
    private static final float DEFAULT_SPRINT_EXHAUSTION = 0.1f;
    private static final float DEFAULT_ATTACK_EXHAUSTION = 0.1f;

    private static final int DEFAULT_HIGH_HUNGER_THRESHOLD = 18;
    private static final int DEFAULT_CRITICAL_HUNGER_THRESHOLD = 6;
    private static final int DEFAULT_HIGH_HUNGER_REGEN_INTERVAL = 80;
    private static final float DEFAULT_REGEN_AMOUNT = 1.0f;
    private static final float DEFAULT_REGEN_EXHAUSTION_COST = 3.0f;
    private static final int DEFAULT_STARVATION_INTERVAL = 80;
    private static final float DEFAULT_STARVATION_DAMAGE = 1.0f;

    private int maxHunger = DEFAULT_MAX_HUNGER;
    private float maxSaturation = DEFAULT_MAX_SATURATION;
    private float exhaustionThreshold = DEFAULT_EXHAUSTION_THRESHOLD;
    private float walkExhaustion = DEFAULT_WALK_EXHAUSTION;
    private float sprintExhaustion = DEFAULT_SPRINT_EXHAUSTION;
    private float attackExhaustion = DEFAULT_ATTACK_EXHAUSTION;
    private int highHungerThreshold = DEFAULT_HIGH_HUNGER_THRESHOLD;
    private int criticalHungerThreshold = DEFAULT_CRITICAL_HUNGER_THRESHOLD;
    private int highHungerRegenInterval = DEFAULT_HIGH_HUNGER_REGEN_INTERVAL;
    private float regenAmount = DEFAULT_REGEN_AMOUNT;
    private float regenExhaustionCost = DEFAULT_REGEN_EXHAUSTION_COST;
    private int starvationInterval = DEFAULT_STARVATION_INTERVAL;
    private float starvationDamage = DEFAULT_STARVATION_DAMAGE;

    private NpcStatusConfig() {}

    public static NpcStatusConfig getInstance() {
        return INSTANCE;
    }

    public int getMaxHunger() {
        return maxHunger;
    }

    public float getMaxSaturation() {
        return maxSaturation;
    }

    public float getExhaustionThreshold() {
        return exhaustionThreshold;
    }

    public float getWalkExhaustion() {
        return walkExhaustion;
    }

    public float getSprintExhaustion() {
        return sprintExhaustion;
    }

    public float getAttackExhaustion() {
        return attackExhaustion;
    }

    public int getHighHungerThreshold() {
        return highHungerThreshold;
    }

    public int getCriticalHungerThreshold() {
        return criticalHungerThreshold;
    }

    public int getHighHungerRegenInterval() {
        return highHungerRegenInterval;
    }

    public float getRegenAmount() {
        return regenAmount;
    }

    public float getRegenExhaustionCost() {
        return regenExhaustionCost;
    }

    public int getStarvationInterval() {
        return starvationInterval;
    }

    public float getStarvationDamage() {
        return starvationDamage;
    }
}
