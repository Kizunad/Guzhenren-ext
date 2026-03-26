package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

import java.util.List;

/**
 * 升仙尝试状态契约。
 * <p>
 * 仅用于冻结阶段顺序与输入冻结规则，不提供任何世界变更实现。
 * </p>
 */
public final class AscensionAttemptStateContract {

    /**
     * 玩家可感知主链顺序。
     */
    public static final List<AscensionAttemptStage> PLAYER_VISIBLE_MAIN_CHAIN = List.of(
        AscensionAttemptStage.CULTIVATION_PROGRESS,
        AscensionAttemptStage.ASCENSION_PREPARATION_UNLOCKED,
        AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION,
        AscensionAttemptStage.READY_TO_CONFIRM,
        AscensionAttemptStage.CONFIRMED,
        AscensionAttemptStage.WORLD_TRIBULATION_IN_PLACE,
        AscensionAttemptStage.APERTURE_FORMING
    );

    private AscensionAttemptStateContract() {
    }

    /**
     * 判断该阶段是否应冻结输入。
     * <p>
     * 契约明确要求从 CONFIRMED 开始冻结输入，
     * 以表示“已发起升仙尝试且进入唯一事务主线”。
     * </p>
     *
     * @param stage 升仙尝试阶段
     * @return 需要冻结输入返回 true，否则返回 false
     */
    public static boolean isInputFrozen(AscensionAttemptStage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("stage 不能为空");
        }
        return stage == AscensionAttemptStage.CONFIRMED
            || stage == AscensionAttemptStage.WORLD_TRIBULATION_IN_PLACE
            || stage == AscensionAttemptStage.APERTURE_FORMING
            || stage == AscensionAttemptStage.FAILED_SEVERE_INJURY
            || stage == AscensionAttemptStage.FAILED_DEATH;
    }
}
