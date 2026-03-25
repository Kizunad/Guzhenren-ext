package com.Kizunad.guzhenrenext.xianqiao.entry;

/**
 * 仙窍初始化入口网关契约。
 * <p>
 * 该接口仅定义“入口如何把请求提交到应用服务”，不包含任何执行器、铺地、边界更新等运行逻辑。
 * </p>
 */
public interface ApertureEntryGateway {

    ApertureInitializationResult trigger(
        ApertureInitializationRequest request,
        ApertureInitializationApplicationService service
    );
}
