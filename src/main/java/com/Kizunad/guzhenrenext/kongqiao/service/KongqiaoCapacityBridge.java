package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.opening.ResolvedOpeningProfile;
import net.minecraft.world.entity.LivingEntity;

/**
 * 空窍容量桥接层。
 * <p>
 * 该类是空窍容量的唯一权威桥接点，负责将上游冻结画像（{@link ResolvedOpeningProfile}）
 * 转换为空窍游戏玩法的容量画像（{@link KongqiaoCapacityProfile}）。
 * </p>
 * <p>
 * 核心职责：
 * <ul>
 *   <li>将资源状态映射为资质档位（{@link KongqiaoAptitudeTier}）</li>
 *   <li>将转数/阶段归一化为境界信息</li>
 *   <li>计算资质基础行数和修为追加行数</li>
 *   <li>计算总行数并应用最大行数限制</li>
 * </ul>
 * </p>
 * <p>
 * 资质映射规则（复用 {@link com.Kizunad.guzhenrenext.xianqiao.opening.OpeningLayoutPlanner} 的加权逻辑）：
 * <ul>
 *   <li>资源封顶：真元 120、寿元 120、精力 100、魂魄 100、体质 100</li>
 *   <li>权重：真元 0.25、寿元 0.20、精力 0.20、魂魄 0.20、体质 0.15</li>
 *   <li>缺失状态扣分：PARTIAL_MISSING 减 0.15</li>
 *   <li>高质量窗口加分：HIGH_QUALITY 增 0.05</li>
 *   <li>档位阈值：&lt;0.35 下等、&lt;0.55 中等、&lt;0.75 上等、≥0.75 绝品</li>
 *   <li>全零状态：强制映射为 残缺</li>
 * </ul>
 * </p>
 * <p>
 * 基础行数映射（来自 pressure.md）：
 * <ul>
 *   <li>残缺：1 行</li>
 *   <li>下等：2 行</li>
 *   <li>中等：3 行</li>
 *   <li>上等：4 行</li>
 *   <li>绝品：5 行</li>
 * </ul>
 * </p>
 * <p>
 * 修为追加行数继续使用现有的真元比例逻辑，但仅作为追加部分：
 * {@code bonusRows = round(maxZhenyuan / FULL_CAPACITY_ZHENYUAN * DYNAMIC_MAX_ROWS)}
 * </p>
 */
public final class KongqiaoCapacityBridge {

    /** 资源封顶值（复用 OpeningLayoutPlanner 的常量）。 */
    private static final double RESOURCE_CAP_ZHENYUAN = 120.0D;
    private static final double RESOURCE_CAP_SHOUYUAN = 120.0D;
    private static final double RESOURCE_CAP_JINGLI = 100.0D;
    private static final double RESOURCE_CAP_HUNPO = 100.0D;
    private static final double RESOURCE_CAP_TIZHI = 100.0D;

    /** 资源权重（复用 OpeningLayoutPlanner 的常量）。 */
    private static final double WEIGHT_ZHENYUAN = 0.25D;
    private static final double WEIGHT_SHOUYUAN = 0.20D;
    private static final double WEIGHT_JINGLI = 0.20D;
    private static final double WEIGHT_HUNPO = 0.20D;
    private static final double WEIGHT_TIZHI = 0.15D;

    /** 缺失状态惩罚（复用 OpeningLayoutPlanner 的常量）。 */
    private static final double PARTIAL_MISSING_PENALTY = 0.15D;

    /** 高质量窗口加分（复用 OpeningLayoutPlanner 的常量）。 */
    private static final double HIGH_QUALITY_BONUS = 0.05D;

    /** 资质档位阈值（复用 OpeningLayoutPlanner 的常量）。 */
    private static final double TIER_ONE_UPPER_BOUND = 0.35D;
    private static final double TIER_TWO_UPPER_BOUND = 0.55D;
    private static final double TIER_THREE_UPPER_BOUND = 0.75D;

    private KongqiaoCapacityBridge() {}

    /**
     * 根据玩家实体解析空窍容量画像。
     * <p>
     * 该方法从玩家实体获取实时变量，构造冻结画像，然后转换为容量画像。
     * 注意：此方法会创建新的 {@link com.Kizunad.guzhenrenext.xianqiao.opening.OpeningProfileResolver} 实例。
     * </p>
     *
     * @param entity 玩家实体
     * @return 空窍容量画像
     */
    public static KongqiaoCapacityProfile resolveFromEntity(LivingEntity entity) {
        if (entity == null) {
            return createDefaultProfile();
        }
        // 使用 ZhenYuanHelper 获取实时最大真元（权威修为追加行数来源）
        double maxZhenyuan = Math.max(0.0D, ZhenYuanHelper.getMaxAmount(entity));

        // 资质档位和境界信息来自冻结画像（权威来源）
        var resolver = new com.Kizunad.guzhenrenext.xianqiao.opening.OpeningProfileResolver();
        ResolvedOpeningProfile profile = resolver.resolveFromPlayer(entity, false);
        if (profile == null) {
            return createDefaultProfile();
        }

        AscensionConditionSnapshot snapshot = profile.conditionSnapshot();

        // 解析资质档位（来自冻结画像）
        KongqiaoAptitudeTier aptitudeTier = resolveAptitudeTier(profile);
        int baseRows = aptitudeTier.baseRows();

        // 计算修为追加行数（来自 ZhenYuanHelper，而非快照）
        int bonusRows = computeBonusRows(maxZhenyuan);

        // 计算总行数
        int totalRows = clamp(baseRows + bonusRows, 0, KongqiaoConstants.MAX_ROWS);

        // 归一化境界信息（来自冻结画像）
        int apertureRank = normalizeRank(snapshot.zhuanshu());
        int apertureStage = normalizeStage(snapshot.jieduan());

        return new KongqiaoCapacityProfile(
            aptitudeTier,
            apertureRank,
            apertureStage,
            baseRows,
            bonusRows,
            totalRows,
            maxZhenyuan,
            snapshot
        );
    }

    /**
     * 根据预解析的冻结画像解析空窍容量画像。
     * <p>
     * 这是核心转换方法，所有其他入口最终都会调用此方法。
     * </p>
     *
     * @param profile 已解析的冻结画像
     * @return 空窍容量画像
     */
    public static KongqiaoCapacityProfile resolveFromProfile(ResolvedOpeningProfile profile) {
        if (profile == null) {
            return createDefaultProfile();
        }
        AscensionConditionSnapshot snapshot = profile.conditionSnapshot();

        // 解析资质档位
        KongqiaoAptitudeTier aptitudeTier = resolveAptitudeTier(profile);

        // 计算基础行数
        int baseRows = aptitudeTier.baseRows();

        // 计算修为追加行数（基于最大真元）
        double maxZhenyuan = Math.max(0.0D, snapshot.maxZhenyuan());
        int bonusRows = computeBonusRows(maxZhenyuan);

        // 计算总行数并应用最大限制
        int totalRows = clamp(baseRows + bonusRows, 0, KongqiaoConstants.MAX_ROWS);

        // 归一化境界信息
        int apertureRank = normalizeRank(snapshot.zhuanshu());
        int apertureStage = normalizeStage(snapshot.jieduan());

        return new KongqiaoCapacityProfile(
            aptitudeTier,
            apertureRank,
            apertureStage,
            baseRows,
            bonusRows,
            totalRows,
            maxZhenyuan,
            snapshot
        );
    }

    /**
     * 根据原始变量映射解析空窍容量画像。
     * <p>
     * 此方法用于测试或离线回放场景，直接接收原始变量映射。
     * </p>
     *
     * @param rawVariables 原始变量映射
     * @return 空窍容量画像
     */
    public static KongqiaoCapacityProfile resolveFromRawVariables(java.util.Map<String, Double> rawVariables) {
        if (rawVariables == null || rawVariables.isEmpty()) {
            return createDefaultProfile();
        }
        var resolver = new com.Kizunad.guzhenrenext.xianqiao.opening.OpeningProfileResolver();
        ResolvedOpeningProfile profile = resolver.resolveFromRawVariables(rawVariables, false);
        return resolveFromProfile(profile);
    }

    /**
     * 解析资质档位。
     * <p>
     * 复用 {@link com.Kizunad.guzhenrenext.xianqiao.opening.OpeningLayoutPlanner} 的加权逻辑：
     * </p>
     * <ul>
     *   <li>如果资源状态为 ALL_ZERO_OR_MISSING，直接返回 残缺</li>
     *   <li>否则计算加权分数，并应用缺失惩罚和高质加分</li>
     *   <li>根据阈值映射到 5 个资质档位</li>
     * </ul>
     */
    private static KongqiaoAptitudeTier resolveAptitudeTier(ResolvedOpeningProfile profile) {
        AscensionConditionSnapshot snapshot = profile.conditionSnapshot();

        // 全零状态直接返回最低档位
        if (snapshot.aptitudeResourceState() == AscensionConditionSnapshot.AptitudeResourceState.ALL_ZERO_OR_MISSING) {
            return KongqiaoAptitudeTier.CANCI;
        }

        // 计算加权分数
        double zhenyuanScore = ratio(snapshot.maxZhenyuan(), RESOURCE_CAP_ZHENYUAN);
        double shouyuanScore = ratio(snapshot.shouyuan(), RESOURCE_CAP_SHOUYUAN);
        double jingliScore = ratio(snapshot.maxJingli(), RESOURCE_CAP_JINGLI);
        double hunpoScore = ratio(snapshot.maxHunpo(), RESOURCE_CAP_HUNPO);
        double tizhiScore = ratio(snapshot.tizhi(), RESOURCE_CAP_TIZHI);

        double weighted = (zhenyuanScore * WEIGHT_ZHENYUAN)
            + (shouyuanScore * WEIGHT_SHOUYUAN)
            + (jingliScore * WEIGHT_JINGLI)
            + (hunpoScore * WEIGHT_HUNPO)
            + (tizhiScore * WEIGHT_TIZHI);

        // 缺失状态惩罚
        if (snapshot.aptitudeResourceState() == AscensionConditionSnapshot.AptitudeResourceState.PARTIAL_MISSING) {
            weighted = clamp01(weighted - PARTIAL_MISSING_PENALTY);
        }

        // 高质量窗口加分
        if (profile.threeQiEvaluation().highQualityWindow()) {
            weighted = clamp01(weighted + HIGH_QUALITY_BONUS);
        }

        // 根据阈值映射到资质档位
        if (weighted < TIER_ONE_UPPER_BOUND) {
            return KongqiaoAptitudeTier.XIADENG;
        }
        if (weighted < TIER_TWO_UPPER_BOUND) {
            return KongqiaoAptitudeTier.ZHONGDENG;
        }
        if (weighted < TIER_THREE_UPPER_BOUND) {
            return KongqiaoAptitudeTier.SHANGDENG;
        }
        return KongqiaoAptitudeTier.JUEPIN;
    }

    /**
     * 计算修为追加行数。
     * <p>
     * 继续使用现有的真元比例逻辑，但仅作为追加部分：
     * {@code bonusRows = round(maxZhenyuan / FULL_CAPACITY_ZHENYUAN * DYNAMIC_MAX_ROWS)}
     * </p>
     */
    private static int computeBonusRows(double maxZhenyuan) {
        if (KongqiaoConstants.FULL_CAPACITY_ZHENYUAN <= 0.0D) {
            return KongqiaoConstants.DYNAMIC_MAX_ROWS;
        }
        double ratio = Math.max(0.0D, Math.min(1.0D, maxZhenyuan / KongqiaoConstants.FULL_CAPACITY_ZHENYUAN));
        int rows = (int) Math.round(ratio * KongqiaoConstants.DYNAMIC_MAX_ROWS);
        return Math.max(0, Math.min(rows, KongqiaoConstants.DYNAMIC_MAX_ROWS));
    }

    /**
     * 归一化转数。
     * <p>
     * 使用 Math.floor 将转数归一化为整数。
     * </p>
     */
    private static int normalizeRank(double zhuanshu) {
        if (!Double.isFinite(zhuanshu) || zhuanshu <= 0.0D) {
            return 0;
        }
        return (int) Math.max(0, Math.floor(zhuanshu));
    }

    /**
     * 归一化阶段。
     * <p>
     * 使用 Math.floor 将阶段归一化为整数。
     * </p>
     */
    private static int normalizeStage(double jieduan) {
        if (!Double.isFinite(jieduan) || jieduan <= 0.0D) {
            return 0;
        }
        return (int) Math.max(0, Math.floor(jieduan));
    }

    /**
     * 计算资源比例。
     */
    private static double ratio(double current, double max) {
        if (max <= 0.0D) {
            return 0.0D;
        }
        return clamp01(current / max);
    }

    /**
     * 将值限制在 [0, 1] 范围内。
     */
    private static double clamp01(double value) {
        if (value < 0.0D) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }

    /**
     * 将值限制在 [min, max] 范围内。
     */
    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * 创建默认容量画像（用于无效输入）。
     */
    private static KongqiaoCapacityProfile createDefaultProfile() {
        return new KongqiaoCapacityProfile(
            KongqiaoAptitudeTier.CANCI,
            0,
            0,
            1,
            0,
            1,
            0.0D,
            null
        );
    }
}
