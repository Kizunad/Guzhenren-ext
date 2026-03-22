package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2DistSafetyValidatorTest {

    @Test
    void shouldPassCurrentPlan2AndXianqiaoBoundaryAudit() throws IOException {
        List<String> errors = Plan2DistSafetyValidator.validateCurrentProject();
        assertTrue(errors.isEmpty(), "当前 Task31 边界应通过端侧隔离审计，实际错误: " + errors);
    }

    @Test
    void shouldAllowExplicitClientPackageSource() {
        Path clientPath = Path.of(
            "src/main/java/com/Kizunad/guzhenrenext/xianqiao/client/FakeClientOnlyScreen.java"
        );
        String sourceText = """
            package com.Kizunad.guzhenrenext.xianqiao.client;

            import net.minecraft.client.Minecraft;

            final class FakeClientOnlyScreen {
                void allowed() {
                    net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event = null;
                    Minecraft.getInstance();
                }
            }
            """;

        List<String> errors = Plan2DistSafetyValidator.validate(Map.of(clientPath, sourceText));
        assertTrue(errors.isEmpty(), "显式 client 包路径应直接放行，实际错误: " + errors);
    }

    @Test
    void shouldAllowGuardedReflectiveClientBootstrapInGuzhenrenExt() throws IOException {
        Path path = Path.of(Plan2DistSafetyValidator.GUZHENREN_EXT_FILE);
        String sourceText = Files.readString(path, StandardCharsets.UTF_8);

        List<String> errors = Plan2DistSafetyValidator.validate(Map.of(path, sourceText));
        assertTrue(errors.isEmpty(), "GuzhenrenExt 的受保护反射客户端引导应视为安全模式，实际错误: " + errors);
    }

    @Test
    void shouldReportClientLeakInCommonPathFixture() {
        Path brokenPath = Path.of(
            "src/main/java/com/Kizunad/guzhenrenext/xianqiao/service/BrokenServerReachablePath.java"
        );
        String brokenSource = """
            package com.Kizunad.guzhenrenext.xianqiao.service;

            import com.Kizunad.guzhenrenext.xianqiao.client.XianqiaoClientEvents;

            final class BrokenServerReachablePath {
                void broken() {
                    net.minecraft.client.Minecraft.getInstance();
                    XianqiaoClientEvents.class.getName();
                }
            }
            """;

        List<String> errors = Plan2DistSafetyValidator.validate(Map.of(brokenPath, brokenSource));
        assertContains(errors, "BrokenServerReachablePath.java:3");
        assertContains(errors, "直接 import 客户端类");
        assertContains(errors, "com.Kizunad.guzhenrenext.xianqiao.client.XianqiaoClientEvents");
        assertContains(errors, "BrokenServerReachablePath.java:7");
        assertContains(errors, "直接引用客户端类/包");
        assertContains(errors, "net.minecraft.client.Minecraft.getInstance");
    }

    private static void assertContains(List<String> errors, String expected) {
        assertTrue(
            errors.stream().anyMatch(message -> message.contains(expected)),
            "期望错误包含: " + expected + "，实际: " + errors
        );
    }
}
