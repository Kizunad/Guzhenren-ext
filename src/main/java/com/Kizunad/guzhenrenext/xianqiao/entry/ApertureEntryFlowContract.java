package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.util.List;

/**
 * 开窍阶段流冻结契约。
 * <p>
 * 该类只提供固定阶段顺序，避免后续任务在未评审情况下改动流程语义。
 * 这里只是契约基线，不实现任何阶段执行。
 * </p>
 */
public final class ApertureEntryFlowContract {

    private static final List<ApertureOpeningPhase> STAGED_FLOW = List.of(
        ApertureOpeningPhase.LOGIN_INIT,
        ApertureOpeningPhase.CULTIVATION_PROGRESSION,
        ApertureOpeningPhase.RANK_FIVE_PEAK_CHECK,
        ApertureOpeningPhase.THREE_QI_READINESS_AND_BALANCE_CHECK,
        ApertureOpeningPhase.ASCENSION_ATTEMPT_CONFIRMATION_CONFIRMED,
        ApertureOpeningPhase.PROFILE_RESOLUTION,
        ApertureOpeningPhase.LAYOUT_PLANNING,
        ApertureOpeningPhase.TERRAIN_MATERIALIZATION,
        ApertureOpeningPhase.CORE_SPIRIT_BOUNDARY_FINALIZATION
    );

    private ApertureEntryFlowContract() {
    }

    public static List<ApertureOpeningPhase> stagedFlow() {
        return STAGED_FLOW;
    }
}
