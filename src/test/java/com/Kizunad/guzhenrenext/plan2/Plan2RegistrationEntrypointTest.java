package com.Kizunad.guzhenrenext.plan2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2RegistrationEntrypointTest {

    @Test
    void shouldPassWhenDefaultAssembliesAreCompleteAndValid() {
        assertDoesNotThrow(() ->
            Plan2RegistrationEntrypoint.validateAssembliesOrThrow(
                Plan2RegistrationEntrypoint.copyDefaultAssemblies()
            )
        );
    }

    @Test
    void shouldFailWhenCategoryIsMissingFromAssemblies() {
        List<Plan2RegistrationEntrypoint.CategoryAssembly> broken =
            fixtureMissingCategory(Plan2RegistrationEntrypoint.Category.PILL);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> Plan2RegistrationEntrypoint.validateAssembliesOrThrow(broken)
        );
        assertTrue(exception.getMessage().contains("类别缺失"));
    }

    @Test
    void shouldFailWhenRegistrationKeyIsDuplicated() {
        List<Plan2RegistrationEntrypoint.CategoryAssembly> broken =
            Plan2RegistrationEntrypoint.copyDefaultAssemblies();
        Plan2RegistrationEntrypoint.CategoryAssembly first = broken.get(0);
        Plan2RegistrationEntrypoint.CategoryAssembly second = broken.get(1);
        broken.set(
            1,
            new Plan2RegistrationEntrypoint.CategoryAssembly(
                second.category(),
                first.entityKey(),
                second.blockKey(),
                second.itemKey(),
                second.menuKey(),
                second.dataReloadPath()
            )
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> Plan2RegistrationEntrypoint.validateAssembliesOrThrow(broken)
        );
        assertTrue(exception.getMessage().contains("key 重复"));
    }

    @Test
    void shouldFailWhenAnyRegistrationSlotIsBlank() {
        List<Plan2RegistrationEntrypoint.CategoryAssembly> broken =
            Plan2RegistrationEntrypoint.copyDefaultAssemblies();
        Plan2RegistrationEntrypoint.CategoryAssembly target = broken.get(0);
        broken.set(
            0,
            new Plan2RegistrationEntrypoint.CategoryAssembly(
                target.category(),
                "",
                target.blockKey(),
                target.itemKey(),
                target.menuKey(),
                target.dataReloadPath()
            )
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> Plan2RegistrationEntrypoint.validateAssembliesOrThrow(broken)
        );
        assertTrue(exception.getMessage().contains("注册项为空"));
    }

    @Test
    void shouldPassWhenStartupChainContainsAllRequiredStages() {
        assertDoesNotThrow(() ->
            Plan2RegistrationEntrypoint.validateStartupChainOrThrow(
                EnumSet.of(
                    Plan2RegistrationEntrypoint.StartupStage.MOD_REGISTRATION,
                    Plan2RegistrationEntrypoint.StartupStage.RELOAD_LISTENER_REGISTRATION
                )
            )
        );
    }

    @Test
    void shouldFailWhenStartupChainMissesStage() {
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> Plan2RegistrationEntrypoint.validateStartupChainOrThrow(
                EnumSet.of(Plan2RegistrationEntrypoint.StartupStage.MOD_REGISTRATION)
            )
        );
        assertTrue(exception.getMessage().contains("启动链缺失阶段"));
    }

    private static List<Plan2RegistrationEntrypoint.CategoryAssembly> fixtureMissingCategory(
        Plan2RegistrationEntrypoint.Category category
    ) {
        List<Plan2RegistrationEntrypoint.CategoryAssembly> assemblies =
            new ArrayList<>(Plan2RegistrationEntrypoint.copyDefaultAssemblies());
        assemblies.removeIf(assembly -> assembly.category() == category);
        return assemblies;
    }
}
