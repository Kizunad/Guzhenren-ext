package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.util.Objects;

public record ApertureInitializationResult(
    Status status,
    String message,
    boolean executionTriggered
) {

    public ApertureInitializationResult {
        status = Objects.requireNonNull(status, "status");
        message = Objects.requireNonNullElse(message, "");
    }

    public boolean accepted() {
        return status == Status.INITIALIZATION_EXECUTED || status == Status.ALREADY_INITIALIZED;
    }

    public enum Status {
        INITIALIZATION_EXECUTED,
        ALREADY_INITIALIZED,
        REJECTED_NOT_RANK_FIVE_PEAK,
        REJECTED_THREE_QI_NOT_READY,
        REJECTED_THREE_QI_NOT_BALANCED,
        REJECTED_ASCENSION_ATTEMPT_NOT_CONFIRMED,
        REJECTED_SNAPSHOT_NOT_FROZEN,
        FAILED_RUNTIME
    }
}
