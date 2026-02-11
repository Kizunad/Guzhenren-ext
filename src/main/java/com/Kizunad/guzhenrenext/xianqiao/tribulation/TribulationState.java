package com.Kizunad.guzhenrenext.xianqiao.tribulation;

/**
 * 灾劫状态枚举。
 * <p>
 * 每个状态都绑定持续时长（tick）。
 * 其中 {@link #IDLE} 与 {@link #SETTLEMENT} 为瞬时状态，持续时长为 0。
 * </p>
 */
public enum TribulationState {

    /** 空闲：未处于灾劫流程。 */
    IDLE(0),

    /** 前兆阶段：灵气扰动与警示。 */
    OMEN(600),

    /** 雷劫阶段：周期性落雷打击。 */
    STRIKE(1200),

    /** 入侵阶段：灾兽生成并进攻。 */
    INVASION(2400),

    /** 结算阶段：计算评分、奖惩并重置周期。 */
    SETTLEMENT(0);

    /** 状态持续时长（tick）。 */
    private final int durationTicks;

    TribulationState(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    /**
     * 获取该状态持续时长。
     *
     * @return tick 时长
     */
    public int durationTicks() {
        return durationTicks;
    }
}
