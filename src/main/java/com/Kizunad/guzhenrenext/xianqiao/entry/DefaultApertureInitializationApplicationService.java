package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.util.Objects;

public final class DefaultApertureInitializationApplicationService
    implements ApertureInitializationApplicationService {

    @Override
    public ApertureInitializationResult beginInitialization(
        ApertureInitializationRequest request,
        ApertureInitializationExecution execution
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(execution, "execution");

        if (request.alreadyInitialized()) {
            return executeAccepted(
                request,
                execution,
                ApertureInitializationResult.Status.ALREADY_INITIALIZED,
                "仙窍已完成初始化，正在进入仙窍。"
            );
        }
        if (!request.rankFivePeak()) {
            return reject(
                ApertureInitializationResult.Status.REJECTED_NOT_RANK_FIVE_PEAK,
                "未达到五转巅峰，无法发起升仙尝试。",
                request
            );
        }
        if (!request.threeQiReady()) {
            return reject(
                ApertureInitializationResult.Status.REJECTED_THREE_QI_NOT_READY,
                "天地人三气尚未充足，无法进入升仙确认阶段。",
                request
            );
        }
        if (!request.threeQiBalanced()) {
            return reject(
                ApertureInitializationResult.Status.REJECTED_THREE_QI_NOT_BALANCED,
                "天地人三气尚未平衡，无法冻结本次升仙输入。",
                request
            );
        }
        if (!request.ascensionAttemptConfirmed()) {
            return reject(
                ApertureInitializationResult.Status.REJECTED_ASCENSION_ATTEMPT_NOT_CONFIRMED,
                "升仙尝试尚未达到 CONFIRMED 门槛，初始化不会启动。",
                request
            );
        }
        if (!request.snapshotFrozen()) {
            return reject(
                ApertureInitializationResult.Status.REJECTED_SNAPSHOT_NOT_FROZEN,
                "升仙输入快照尚未冻结，初始化不会启动。",
                request
            );
        }
        return executeAccepted(
            request,
            execution,
            ApertureInitializationResult.Status.INITIALIZATION_EXECUTED,
            "升仙尝试已确认，仙窍初始化流程已启动。"
        );
    }

    private static ApertureInitializationResult executeAccepted(
        ApertureInitializationRequest request,
        ApertureInitializationExecution execution,
        ApertureInitializationResult.Status status,
        String message
    ) {
        try {
            execution.execute(request);
            return new ApertureInitializationResult(status, message, true);
        } catch (RuntimeException exception) {
            return new ApertureInitializationResult(
                ApertureInitializationResult.Status.FAILED_RUNTIME,
                buildRuntimeFailureMessage(exception),
                false
            );
        }
    }

    private static ApertureInitializationResult reject(
        ApertureInitializationResult.Status status,
        String prefix,
        ApertureInitializationRequest request
    ) {
        return new ApertureInitializationResult(
            status,
            prefix + buildScoreSuffix(request),
            false
        );
    }

    private static String buildRuntimeFailureMessage(RuntimeException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.isBlank()) {
            return "仙窍初始化执行失败，请稍后重试。";
        }
        return "仙窍初始化执行失败：" + detail;
    }

    private static String buildScoreSuffix(ApertureInitializationRequest request) {
        return " 当前评分：天="
            + request.heavenScore()
            + "，地="
            + request.earthScore()
            + "，人="
            + request.humanScore()
            + "，平衡="
            + request.balanceScore()
            + "。";
    }
}
