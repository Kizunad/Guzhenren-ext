package com.Kizunad.guzhenrenext.xianqiao.service;

/**
 * 地灵能力解锁服务。
 * <p>
 * 本服务以“集中配置 + 无状态工具类”的方式维护 Stage 解锁规则，
 * 避免阈值散落在命令、事件或表现层中，确保服务端判定口径一致。
 * </p>
 * <p>
 * 核心原则：
 * 1) 每个 Stage 同时受 tier 与 favorability 双门槛约束；
 * 2) 阈值只在一处定义（STAGE_RULES）；
 * 3) 所有对外方法均为 static，便于任意服务端逻辑直接复用。
 * </p>
 */
public final class SpiritUnlockService {

    /** 阶段 0：地灵本体。 */
    private static final int STAGE_0 = 0;

    /** 阶段 1：身外化身。 */
    private static final int STAGE_1 = 1;

    /** 阶段 2：护体罡气。 */
    private static final int STAGE_2 = 2;

    /** 阶段 3：虚空搬运。 */
    private static final int STAGE_3 = 3;

    /** 阶段 4：灵犀镇压。 */
    private static final int STAGE_4 = 4;

    /** 阶段 5：万界共鸣。 */
    private static final int STAGE_5 = 5;

    /** 最大阶段。 */
    private static final int MAX_STAGE = STAGE_5;

    /** 第 4 阶段最小 tier。 */
    private static final int TIER_REQUIREMENT_STAGE_4 = 4;

    /** 第 5 阶段最小 tier。 */
    private static final int TIER_REQUIREMENT_STAGE_5 = 6;

    /** 第 1 阶段最小好感度。 */
    private static final float FAVORABILITY_REQUIREMENT_STAGE_1 = 10.0F;

    /** 第 2 阶段最小好感度。 */
    private static final float FAVORABILITY_REQUIREMENT_STAGE_2 = 25.0F;

    /** 第 3 阶段最小好感度。 */
    private static final float FAVORABILITY_REQUIREMENT_STAGE_3 = 40.0F;

    /** 第 4 阶段最小好感度。 */
    private static final float FAVORABILITY_REQUIREMENT_STAGE_4 = 60.0F;

    /** 第 5 阶段最小好感度。 */
    private static final float FAVORABILITY_REQUIREMENT_STAGE_5 = 80.0F;

    /** 好感度最大值（用于输入限幅，避免异常值放大判定差异）。 */
    private static final float MAX_FAVORABILITY = 100.0F;

    /** 阶段越界时的默认回退阶段。 */
    private static final int DEFAULT_STAGE = STAGE_0;

    /**
     * Stage 解锁规则表（集中配置）。
     * <p>
     * 数组下标即 stage 值，调用方无需维护额外映射。
     * </p>
     */
    private static final StageRule[] STAGE_RULES = {
        new StageRule("地灵本体", STAGE_1, STAGE_0),
        new StageRule("身外化身", STAGE_1, FAVORABILITY_REQUIREMENT_STAGE_1),
        new StageRule("护体罡气", STAGE_2, FAVORABILITY_REQUIREMENT_STAGE_2),
        new StageRule("虚空搬运", STAGE_3, FAVORABILITY_REQUIREMENT_STAGE_3),
        new StageRule("灵犀镇压", TIER_REQUIREMENT_STAGE_4, FAVORABILITY_REQUIREMENT_STAGE_4),
        new StageRule("万界共鸣", TIER_REQUIREMENT_STAGE_5, FAVORABILITY_REQUIREMENT_STAGE_5)
    };

    /** 单个阶段规则。 */
    private record StageRule(String displayName, int minTier, float minFavorability) {
    }

    private SpiritUnlockService() {
    }

    /**
     * 根据层级与好感度计算当前可达的最大阶段。
     * <p>
     * 规则：必须同时满足 tier 与 favorability 两个阈值，
     * 返回满足条件的最大 stage，范围 [0, 5]。
     * </p>
     *
     * @param tier 当前层级
     * @param favorability 当前好感度
     * @return 可达最大阶段
     */
    public static int computeStage(int tier, float favorability) {
        float normalizedFavorability = clampFavorability(favorability);

        int unlockedStage = DEFAULT_STAGE;
        for (int stage = STAGE_0; stage < STAGE_RULES.length; stage++) {
            StageRule rule = STAGE_RULES[stage];
            if (tier >= rule.minTier() && normalizedFavorability >= rule.minFavorability()) {
                unlockedStage = stage;
            }
        }
        return unlockedStage;
    }

    /**
     * 查询指定阶段所需的最小层级。
     *
     * @param stage 阶段
     * @return 阶段最小层级
     */
    public static int getMinTierForStage(int stage) {
        int normalizedStage = normalizeStage(stage);
        return STAGE_RULES[normalizedStage].minTier();
    }

    /**
     * 查询指定阶段所需的最小好感度。
     *
     * @param stage 阶段
     * @return 阶段最小好感度
     */
    public static float getMinFavorabilityForStage(int stage) {
        int normalizedStage = normalizeStage(stage);
        return STAGE_RULES[normalizedStage].minFavorability();
    }

    /**
     * 获取下一个阶段。
     * <p>
     * 当已经处于最大阶段时，返回最大阶段本身。
     * </p>
     *
     * @param currentStage 当前阶段
     * @return 下一阶段或最大阶段
     */
    public static int getNextStage(int currentStage) {
        int normalizedStage = normalizeStage(currentStage);
        if (normalizedStage >= MAX_STAGE) {
            return MAX_STAGE;
        }
        return normalizedStage + 1;
    }

    /**
     * 获取阶段中文显示名。
     *
     * @param stage 阶段
     * @return 阶段中文名
     */
    public static String getStageDisplayName(int stage) {
        int normalizedStage = normalizeStage(stage);
        return STAGE_RULES[normalizedStage].displayName();
    }

    /**
     * 获取最大阶段值。
     *
     * @return 最大阶段值
     */
    public static int getMaxStage() {
        return MAX_STAGE;
    }

    /**
     * 将阶段值归一化到 [0, MAX_STAGE]。
     *
     * @param stage 原始阶段
     * @return 归一化后的阶段
     */
    private static int normalizeStage(int stage) {
        if (stage < STAGE_0) {
            return DEFAULT_STAGE;
        }
        if (stage >= STAGE_RULES.length) {
            return MAX_STAGE;
        }
        return stage;
    }

    /**
     * 限制好感度区间到 [0, 100]。
     *
     * @param favorability 原始好感度
     * @return 限幅后的好感度
     */
    private static float clampFavorability(float favorability) {
        return Math.max(STAGE_0, Math.min(MAX_FAVORABILITY, favorability));
    }
}
