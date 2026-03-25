package com.Kizunad.guzhenrenext.xianqiao.entry;

import net.minecraft.server.level.ServerPlayer;

public final class ApertureEntryRuntime {

    private static final ApertureInitializationRequestFactory REQUEST_FACTORY =
        new ApertureInitializationRequestFactory();

    private static final ApertureInitializationApplicationService APPLICATION_SERVICE =
        new DefaultApertureInitializationApplicationService();

    private static final ApertureInitializationRuntimeExecutor RUNTIME_EXECUTOR =
        new ApertureInitializationRuntimeExecutor();

    private ApertureEntryRuntime() {
    }

    public static ApertureInitializationResult trigger(
        ServerPlayer player,
        ApertureEntryChannel entryChannel
    ) {
        ApertureInitializationRequest request = REQUEST_FACTORY.createFromPlayer(player, entryChannel);
        return APPLICATION_SERVICE.beginInitialization(
            request,
            ignoredRequest -> RUNTIME_EXECUTOR.initializeIfNeeded(player)
        );
    }

    public static ApertureInitializationRuntimeExecutor runtimeExecutor() {
        return RUNTIME_EXECUTOR;
    }
}
