package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Plan2BridgeRegressionValidator {

    static final String DAO_HEN_HELPER_FILE =
        "src/main/java/com/Kizunad/guzhenrenext/guzhenrenBridge/DaoHenHelper.java";
    static final String DEEP_PILL_EFFECT_STATE_FILE =
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/alchemy/effect/DeepPillEffectState.java";

    private static final List<String> FORBIDDEN_DIRECT_WRITE_KEYS = List.of(
        "VAR_ZHEN_YUAN",
        "VAR_JIE_DUAN",
        "VAR_ZHONG_ZU",
        "VAR_ZUO_DA_ZHEN_YUAN",
        "VAR_ZUI_DA_HUN_PO"
    );

    private Plan2BridgeRegressionValidator() {
    }

    static List<String> validateCurrentDaoHenHelperContract() throws IOException {
        String sourceText = Files.readString(Path.of(DAO_HEN_HELPER_FILE), StandardCharsets.UTF_8);
        return validateDaoHenHelperContract(sourceText);
    }

    static List<String> validateDaoHenHelperContract(String sourceText) {
        List<String> errors = new ArrayList<>();
        if (!sourceText.contains("final double clamped = Math.max(0.0, value);")) {
            errors.add("bridge regression mismatch: DaoHenHelper setDaoHen 非负钳制缺失");
        }
        requireContains(
            sourceText,
            "case JIAN_DAO -> vars.daohen_jiandao;",
            "bridge regression mismatch: DaoHenHelper 兼容映射缺失 getDaoHen daohen_jiandao",
            errors
        );
        requireContains(
            sourceText,
            "case JIAN_DAO -> vars.daohen_jiandao = clamped;",
            "bridge regression mismatch: DaoHenHelper 兼容映射缺失 setDaoHen daohen_jiandao",
            errors
        );
        requireContains(
            sourceText,
            "case LIAN_DAO -> vars.dahen_liandao;",
            "bridge regression mismatch: DaoHenHelper 兼容映射缺失 getDaoHen dahen_liandao",
            errors
        );
        requireContains(
            sourceText,
            "case LIAN_DAO -> vars.dahen_liandao = clamped;",
            "bridge regression mismatch: DaoHenHelper 兼容映射缺失 setDaoHen dahen_liandao",
            errors
        );
        return errors;
    }

    static List<String> scanCurrentDeepContentDirectWriteBypasses() throws IOException {
        String sourceText = readCurrentDeepContentSource();
        return scanDeepContentDirectWriteBypasses(sourceText, DEEP_PILL_EFFECT_STATE_FILE);
    }

    static String readCurrentDeepContentSource() throws IOException {
        return Files.readString(Path.of(DEEP_PILL_EFFECT_STATE_FILE), StandardCharsets.UTF_8);
    }

    static List<String> scanDeepContentDirectWriteBypasses(String sourceText, String sourcePath) {
        List<String> errors = new ArrayList<>();
        if (!sourceText.contains("guzhenren:player_variables")) {
            return errors;
        }
        for (String key : FORBIDDEN_DIRECT_WRITE_KEYS) {
            Pattern pattern = Pattern.compile("\\bputDouble\\(" + Pattern.quote(key) + "\\s*,");
            Matcher matcher = pattern.matcher(sourceText);
            while (matcher.find()) {
                errors.add(
                    "bridge regression mismatch: "
                        + sourcePath
                        + " 发现绕过桥接 helper 的直接变量写入 -> putDouble("
                        + key
                        + ", ...)"
                );
            }
        }
        return errors;
    }

    private static void requireContains(String text, String token, String error, List<String> errors) {
        if (!text.contains(token)) {
            errors.add(error);
        }
    }
}
