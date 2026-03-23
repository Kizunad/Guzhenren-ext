package com.Kizunad.guzhenrenext.plan2;

import com.Kizunad.guzhenrenext.xianqiao.XianqiaoRegistries;
import java.util.EnumSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * Plan2 启动期桥接器。
 * <p>
 * 将框架相关（NeoForge EventBus/ReloadEvent）调用收敛到单一位置，
 * 并复用 Plan2RegistrationEntrypoint 的四类别装配校验。
 * </p>
 */
public final class Plan2RegistrationBootstrap {

    private Plan2RegistrationBootstrap() {
    }

    public static void register(final IEventBus modEventBus) {
        Plan2RegistrationEntrypoint.validateAssembliesOrThrow(
            Plan2RegistrationEntrypoint.copyDefaultAssemblies()
        );
        XianqiaoRegistries.register(modEventBus);
    }

    public static void registerReloadListeners(final AddReloadListenerEvent event) {
        for (Plan2RegistrationEntrypoint.CategoryAssembly assembly :
            Plan2RegistrationEntrypoint.copyDefaultAssemblies()) {
            event.addListener(new Plan2CategoryDataLoader(assembly.category(), assembly.dataReloadPath()));
        }
    }

    public static void validateStartupChainOrThrow() {
        EnumSet<Plan2RegistrationEntrypoint.StartupStage> startupStages = EnumSet.of(
            Plan2RegistrationEntrypoint.StartupStage.MOD_REGISTRATION,
            Plan2RegistrationEntrypoint.StartupStage.RELOAD_LISTENER_REGISTRATION
        );
        Plan2RegistrationEntrypoint.validateStartupChainOrThrow(startupStages);
    }
}
