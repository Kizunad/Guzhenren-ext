package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;

/**
 * 开局画像解析结果。
 * <p>
 * 该类型承载“冻结快照 + 三气评估 + 阶段建议”，供后续规划层读取。
 * </p>
 */
public record ResolvedOpeningProfile(
    AscensionConditionSnapshot conditionSnapshot,
    AscensionThreeQiEvaluator.ThreeQiEvaluation threeQiEvaluation,
    AscensionAttemptStage suggestedStage
) {

    public ResolvedOpeningProfile {
        if (conditionSnapshot == null) {
            throw new IllegalArgumentException("conditionSnapshot 不能为空");
        }
        if (threeQiEvaluation == null) {
            throw new IllegalArgumentException("threeQiEvaluation 不能为空");
        }
        if (suggestedStage == null) {
            throw new IllegalArgumentException("suggestedStage 不能为空");
        }
    }
}
