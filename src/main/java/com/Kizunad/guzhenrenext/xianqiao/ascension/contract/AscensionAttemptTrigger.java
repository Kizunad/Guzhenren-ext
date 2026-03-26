package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

/**
 * 升仙尝试触发条件只读快照。
 * <p>
 * 该类型用于冻结触发语义：五转巅峰，天地人三气均充足且平衡，并且玩家主动发起。
 * </p>
 */
public record AscensionAttemptTrigger(
    boolean fiveTurnPeak,
    boolean heavenQiSufficient,
    boolean earthQiSufficient,
    boolean humanQiSufficient,
    boolean threeQiBalanced,
    boolean playerInitiated
) {

    /**
     * 判断是否满足升仙尝试触发条件。
     *
     * @return 满足返回 true，否则返回 false
     */
    public boolean canStartAscensionAttempt() {
        return fiveTurnPeak
            && heavenQiSufficient
            && earthQiSufficient
            && humanQiSufficient
            && threeQiBalanced
            && playerInitiated;
    }
}
