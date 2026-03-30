package com.Kizunad.guzhenrenext.kongqiao.service;

/**
 * 空窍资质档位。
 * <p>
 * 该枚举对应压力设计文档中的五个资质档位，用于将上游画像资源聚合结果映射为游戏玩法资质。
 * 档位顺序从低到高依次为：残缺 → 下等 → 中等 → 上等 → 绝品。
 * </p>
 * <p>
 * 基础行数映射规则（来自 pressure.md）：
 * <ul>
 *   <li>残缺：1 行</li>
 *   <li>下等：2 行</li>
 *   <li>中等：3 行</li>
 *   <li>上等：4 行</li>
 *   <li>绝品：5 行</li>
 * </ul>
 * </p>
 */
public enum KongqiaoAptitudeTier {
    /** 勉强成窍，极难负担多蛊。 */
    CANCI("残缺", 1),
    /** 底盘偏小。 */
    XIADENG("下等", 2),
    /** 正常可玩基线。 */
    ZHONGDENG("中等", 3),
    /** 明显优于常人。 */
    SHANGDENG("上等", 4),
    /** 顶级底盘。 */
    JUEPIN("绝品", 5);

    private final String displayName;
    private final int baseRows;

    KongqiaoAptitudeTier(String displayName, int baseRows) {
        this.displayName = displayName;
        this.baseRows = baseRows;
    }

    /**
     * 获取资质档位的中文显示名称。
     */
    public String displayName() {
        return displayName;
    }

    /**
     * 获取该资质档位对应的基础行数。
     */
    public int baseRows() {
        return baseRows;
    }
}
