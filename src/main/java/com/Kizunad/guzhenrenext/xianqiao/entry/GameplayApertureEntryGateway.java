package com.Kizunad.guzhenrenext.xianqiao.entry;

/**
 * 玩法入口适配器（Hub v1 基线）。
 * <p>
 * 该类只负责组装契约请求并提交到应用服务，不实现 UI 行为、执行器调度或 world mutation。
 * </p>
 */
public final class GameplayApertureEntryGateway implements ApertureEntryGateway {

    @Override
    public ApertureInitializationResult trigger(
        ApertureInitializationRequest request,
        ApertureInitializationApplicationService service
    ) {
        return service.beginInitialization(
            request.withEntryChannel(ApertureEntryChannel.HUB_V1_GAMEPLAY),
            ApertureInitializationApplicationService.ApertureInitializationExecution.noop()
        );
    }
}
