package com.Kizunad.guzhenrenext.xianqiao.entry;

/**
 * legacy command 入口适配器。
 * <p>
 * 该类仅把旧入口翻译为统一初始化请求，不执行任何 world mutation。
 * </p>
 */
public final class LegacyCommandApertureEntryGateway implements ApertureEntryGateway {

    @Override
    public ApertureInitializationResult trigger(
        ApertureInitializationRequest request,
        ApertureInitializationApplicationService service
    ) {
        return service.beginInitialization(
            request.withEntryChannel(ApertureEntryChannel.LEGACY_COMMAND),
            ApertureInitializationApplicationService.ApertureInitializationExecution.noop()
        );
    }
}
