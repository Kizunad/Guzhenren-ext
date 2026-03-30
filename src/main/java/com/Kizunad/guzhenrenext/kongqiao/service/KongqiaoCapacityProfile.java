package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.xianqiao.opening.AscensionConditionSnapshot;

/**
 * 空窍容量画像。
 * <p>
 * 该记录是空窍容量的权威计算结果，包含资质档位、境界信息、基础行数、修为追加行数和总行数。
 * 统一由 {@link KongqiaoCapacityBridge} 产生，确保所有下游调用方使用同一套计算逻辑。
 * </p>
 * <p>
 * 总行数计算公式：{@code totalRows = clamp(baseRows + bonusRows, 0, MAX_ROWS)}
 * </p>
 *
 * @param aptitudeTier 资质档位
 * @param apertureRank 境界转数（归一化的转数值）
 * @param apertureStage 境界阶段（归一化的阶段值）
 * @param baseRows 资质基础行数
 * @param bonusRows 修为追加行数（基于最大真元计算）
 * @param totalRows 总行数（基础行数 + 修为追加行数，受限于最大行数）
 * @param maxZhenyuan 用于计算修为追加行的最大真元值
 * @param sourceSnapshot 来源快照（用于调试和审计），可能为 null
 */
public record KongqiaoCapacityProfile(
    KongqiaoAptitudeTier aptitudeTier,
    int apertureRank,
    int apertureStage,
    int baseRows,
    int bonusRows,
    int totalRows,
    double maxZhenyuan,
    AscensionConditionSnapshot sourceSnapshot
) {

    /**
     * 紧凑构造器：确保数值合法。
     */
    public KongqiaoCapacityProfile {
        if (aptitudeTier == null) {
            throw new IllegalArgumentException("aptitudeTier 不能为空");
        }
        if (baseRows < 0) {
            baseRows = 0;
        }
        if (bonusRows < 0) {
            bonusRows = 0;
        }
        if (totalRows < 0) {
            totalRows = 0;
        }
        if (apertureRank < 0) {
            apertureRank = 0;
        }
        if (apertureStage < 0) {
            apertureStage = 0;
        }
        if (maxZhenyuan < 0.0D) {
            maxZhenyuan = 0.0D;
        }
    }
}
