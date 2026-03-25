package com.Kizunad.guzhenrenext.xianqiao.data;

public record ApertureInitializationState(
    ApertureInitPhase initPhase,
    ApertureOpeningSnapshot openingSnapshot,
    Integer layoutVersion,
    Long planSeed
) {

    static ApertureInitializationState uninitialized() {
        return new ApertureInitializationState(ApertureInitPhase.UNINITIALIZED, null, null, null);
    }

    boolean isInitializedEquivalent() {
        return initPhase == ApertureInitPhase.COMPLETED;
    }
}
