package com.Kizunad.guzhenrenext.xianqiao.entry;

public interface ApertureInitializationApplicationService {

    ApertureInitializationResult beginInitialization(
        ApertureInitializationRequest request,
        ApertureInitializationExecution execution
    );

    @FunctionalInterface
    interface ApertureInitializationExecution {

        void execute(ApertureInitializationRequest request);

        static ApertureInitializationExecution noop() {
            return request -> { };
        }
    }
}
