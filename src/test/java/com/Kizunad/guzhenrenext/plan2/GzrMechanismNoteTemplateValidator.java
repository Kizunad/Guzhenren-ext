package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GzrMechanismNoteTemplateValidator {

    static final String TEMPLATE_FILE =
        "docs/guzhenrenext/planning/GZR_Mechanism_Note_Template.md";

    private static final Pattern CLAIM_PATTERN =
        Pattern.compile("(?m)^\\s*-\\s*claim:\\s*\".*\"\\s*$");

    private GzrMechanismNoteTemplateValidator() {
    }

    static List<String> validateCurrentTemplate() throws IOException {
        String text = Files.readString(Path.of(TEMPLATE_FILE), StandardCharsets.UTF_8);
        return validateTemplateText(text);
    }

    static List<String> validateTemplateText(String templateText) {
        List<String> errors = new ArrayList<>();
        requireContains(templateText, "mechanism_name", errors);
        requireContains(templateText, "claim", errors);
        requireContains(templateText, "confidence", errors);
        requireContains(templateText, "source_path", errors);
        requireContains(templateText, "source_line_range", errors);
        requireContains(templateText, "variable_lifecycle", errors);
        requireContains(templateText, "breakthrough_factor", errors);
        requireContains(templateText, "cap_clamp_rule", errors);
        requireContains(templateText, "initialization_flow", errors);
        requireContains(templateText, "validation_evidence", errors);
        requireContains(templateText, "writeback_target", errors);

        if (!templateText.contains("writeback_target: \"GZR_INFO.md\"")) {
            errors.add("writeback_target 必须固定为 GZR_INFO.md");
        }
        if (!templateText.contains("- HIGH")
            || !templateText.contains("- MED")
            || !templateText.contains("- LOW")) {
            errors.add("confidence 枚举缺失 HIGH/MED/LOW");
        }

        List<String> claimBlocks = extractClaimBlocks(templateText);
        if (claimBlocks.isEmpty()) {
            errors.add("模板未定义任何 claim 区块");
            return errors;
        }

        for (int i = 0; i < claimBlocks.size(); i++) {
            String block = claimBlocks.get(i);
            if (!linePresent(block, "source_path")) {
                errors.add("claim[" + i + "] 缺少源码引用字段: source_path");
            }
            if (!linePresent(block, "source_line_range")) {
                errors.add("claim[" + i + "] 缺少源码引用字段: source_line_range");
            }
            Matcher confidenceMatcher = Pattern.compile(
                "(?m)^\\s*confidence:\\s*\"([^\"]+)\"\\s*$"
            ).matcher(block);
            if (!confidenceMatcher.find()) {
                errors.add("claim[" + i + "] 缺少 confidence 字段");
            } else {
                String confidence = confidenceMatcher.group(1);
                if (!("HIGH".equals(confidence)
                    || "MED".equals(confidence)
                    || "LOW".equals(confidence))) {
                    errors.add("claim[" + i + "] confidence 非法: " + confidence);
                }
            }
        }
        return errors;
    }

    private static boolean linePresent(String block, String field) {
        return Pattern.compile("(?m)^\\s*" + Pattern.quote(field) + ":\\s*\".*\"\\s*$")
            .matcher(block)
            .find();
    }

    private static List<String> extractClaimBlocks(String text) {
        List<Integer> starts = new ArrayList<>();
        Matcher matcher = CLAIM_PATTERN.matcher(text);
        while (matcher.find()) {
            starts.add(matcher.start());
        }
        List<String> blocks = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            int start = starts.get(i);
            int end = i + 1 < starts.size() ? starts.get(i + 1) : text.length();
            blocks.add(text.substring(start, end));
        }
        return blocks;
    }

    private static void requireContains(String text, String fieldName, List<String> errors) {
        if (!text.contains(fieldName)) {
            errors.add("模板缺少字段: " + fieldName);
        }
    }
}
