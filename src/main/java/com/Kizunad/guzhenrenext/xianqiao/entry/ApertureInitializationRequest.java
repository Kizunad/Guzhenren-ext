package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 初始化请求契约。
 * <p>
 * 该 record 仅用于描述入口提交到应用服务的冻结输入，不包含任何地形生成或边界落地行为。
 * </p>
 */
public record ApertureInitializationRequest(
    UUID playerId,
    ApertureEntryChannel entryChannel,
    List<ApertureOpeningPhase> stagedFlow,
    int heavenScore,
    int earthScore,
    int humanScore,
    int balanceScore,
    boolean rankFivePeak,
    boolean threeQiReady,
    boolean threeQiBalanced,
    boolean ascensionAttemptConfirmed,
    boolean snapshotFrozen,
    boolean alreadyInitialized
) {

    private static final int SCORE_MIN = 0;

    private static final int SCORE_MAX = 100;

    public ApertureInitializationRequest {
        playerId = Objects.requireNonNull(playerId, "playerId");
        entryChannel = Objects.requireNonNull(entryChannel, "entryChannel");
        stagedFlow = List.copyOf(Objects.requireNonNull(stagedFlow, "stagedFlow"));
        heavenScore = clampScore(heavenScore);
        earthScore = clampScore(earthScore);
        humanScore = clampScore(humanScore);
        balanceScore = clampScore(balanceScore);
    }

    private static int clampScore(int rawScore) {
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, rawScore));
    }

    public ApertureInitializationRequest withEntryChannel(ApertureEntryChannel channel) {
        return new ApertureInitializationRequest(
            playerId,
            channel,
            stagedFlow,
            heavenScore,
            earthScore,
            humanScore,
            balanceScore,
            rankFivePeak,
            threeQiReady,
            threeQiBalanced,
            ascensionAttemptConfirmed,
            snapshotFrozen,
            alreadyInitialized
        );
    }
}
