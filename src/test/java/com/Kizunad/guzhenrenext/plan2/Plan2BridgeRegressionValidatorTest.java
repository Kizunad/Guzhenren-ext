package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2BridgeRegressionValidatorTest {

    @Test
    void shouldPassDaoHenHelperContractOnCurrentSource() throws IOException {
        List<String> errors = Plan2BridgeRegressionValidator.validateCurrentDaoHenHelperContract();
        assertTrue(errors.isEmpty(), "DaoHenHelper 桥接合同应满足 typo 兼容与非负钳制，实际错误: " + errors);
    }

    @Test
    void shouldReportDaoHenHelperContractBreakageOnMutatedSource() {
        String broken = """
            public final class DaoHenHelper {
                public static void setDaoHen(Object entity, Object type, double value) {
                    double raw = value;
                    switch (String.valueOf(type)) {
                        case \"JIAN_DAO\" -> vars.daohen_jiandao = raw;
                        case \"LIAN_DAO\" -> vars.daohen_liandao = raw;
                        default -> {
                        }
                    }
                }
                public static double getDaoHen(Object entity, Object type) {
                    return switch (String.valueOf(type)) {
                        case \"JIAN_DAO\" -> vars.daohen_jiandao;
                        case \"LIAN_DAO\" -> vars.daohen_liandao;
                        default -> 0.0;
                    };
                }
            }
            """;
        List<String> errors = Plan2BridgeRegressionValidator.validateDaoHenHelperContract(broken);
        assertContains(errors, "setDaoHen 非负钳制缺失");
        assertContains(errors, "getDaoHen dahen_liandao");
        assertContains(errors, "setDaoHen dahen_liandao");
    }

    @Test
    void shouldNotUseCurrentBypassPresenceAsGreenInvariant() throws IOException {
        String currentSource = Plan2BridgeRegressionValidator.readCurrentDeepContentSource();
        List<String> currentBypasses = Plan2BridgeRegressionValidator.scanCurrentDeepContentDirectWriteBypasses();
        if (currentBypasses.isEmpty()) {
            return;
        }
        String sanitized = currentSource
            .replace("variables.putDouble(VAR_ZHEN_YUAN, fixedZhenyuan);", "bridgeWrite(VAR_ZHEN_YUAN, fixedZhenyuan);")
            .replace("variables.putDouble(VAR_ZHEN_YUAN, 0.0D);", "bridgeWrite(VAR_ZHEN_YUAN, 0.0D);")
            .replace(
                "variables.putDouble(VAR_JIE_DUAN, Math.max(0.0D, variables.getDouble(VAR_JIE_DUAN) + 1.0D));",
                "bridgeWrite(VAR_JIE_DUAN, Math.max(0.0D, variables.getDouble(VAR_JIE_DUAN) + 1.0D));"
            )
            .replace("variables.putDouble(VAR_JIE_DUAN, 0.0D);", "bridgeWrite(VAR_JIE_DUAN, 0.0D);")
            .replace("variables.putDouble(VAR_ZHONG_ZU, targetRace);", "bridgeWrite(VAR_ZHONG_ZU, targetRace);")
            .replace(
                "variables.putDouble(VAR_ZUO_DA_ZHEN_YUAN, nextMax);",
                "bridgeWrite(VAR_ZUO_DA_ZHEN_YUAN, nextMax);"
            )
            .replace("variables.putDouble(VAR_ZUI_DA_HUN_PO, nextMax);", "bridgeWrite(VAR_ZUI_DA_HUN_PO, nextMax);");

        List<String> errorsAfterBridgeRewrite = Plan2BridgeRegressionValidator.scanDeepContentDirectWriteBypasses(
            sanitized,
            Plan2BridgeRegressionValidator.DEEP_PILL_EFFECT_STATE_FILE
        );
        assertTrue(
            errorsAfterBridgeRewrite.isEmpty(),
            "回归语义要求：一旦改为 bridge 写法，检测器应放行；不能把“当前仍有 bypass”当成常绿条件，实际: "
                + errorsAfterBridgeRewrite
        );
    }

    @Test
    void shouldReportDeepContentDirectWriteBypassOnSyntheticSource() {
        String broken = """
            private static final String PLAYER_VARIABLES_TAG = \"guzhenren:player_variables\";
            private static final String VAR_ZHEN_YUAN = \"zhenyuan\";
            private static final String VAR_JIE_DUAN = \"jieduan\";
            void brokenWrite(CompoundTag variables) {
                variables.putDouble(VAR_ZHEN_YUAN, 1.0D);
                variables.putDouble(VAR_JIE_DUAN, 2.0D);
            }
            """;
        List<String> errors = Plan2BridgeRegressionValidator.scanDeepContentDirectWriteBypasses(
            broken,
            "fixture/DeepPillEffectState.java"
        );
        assertContains(errors, "putDouble(VAR_ZHEN_YUAN");
        assertContains(errors, "putDouble(VAR_JIE_DUAN");
        assertContains(errors, "fixture/DeepPillEffectState.java");
    }

    private static void assertContains(List<String> errors, String expected) {
        assertTrue(
            errors.stream().anyMatch(message -> message.contains(expected)),
            "期望错误包含: " + expected + "，实际: " + errors
        );
    }
}
