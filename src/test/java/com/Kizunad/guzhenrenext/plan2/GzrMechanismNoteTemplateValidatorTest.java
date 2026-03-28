package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class GzrMechanismNoteTemplateValidatorTest {

    private static final String SOURCE_PATH_LINE =
        "        source_path: \"LibSourceCodes/guzhenren/src/main/java/net/guzhenren/procedures/KE1Procedure.java\"\n";

    private static final String SOURCE_LINE_RANGE_LINE = "        source_line_range: \"217-258\"\n";

    @Test
    void shouldPassWhenTemplateFieldsAreComplete() throws IOException {
        List<String> errors = GzrMechanismNoteTemplateValidator.validateCurrentTemplate();
        assertTrue(errors.isEmpty(), "Task6 模板应通过字段完整性校验，实际错误: " + errors);
    }

    @Test
    void shouldFailWhenSourceReferenceFieldsAreMissing() throws IOException {
        String validText = java.nio.file.Files.readString(
            java.nio.file.Path.of(GzrMechanismNoteTemplateValidator.TEMPLATE_FILE),
            java.nio.charset.StandardCharsets.UTF_8
        );
        String invalidText = validText
            .replace(SOURCE_PATH_LINE, "")
            .replace(SOURCE_LINE_RANGE_LINE, "");

        List<String> errors = GzrMechanismNoteTemplateValidator.validateTemplateText(invalidText);
        assertContains(errors, "缺少源码引用字段: source_path");
        assertContains(errors, "缺少源码引用字段: source_line_range");
    }

    private static void assertContains(List<String> errors, String expected) {
        assertTrue(
            errors.stream().anyMatch(message -> message.contains(expected)),
            "期望错误包含: " + expected + "，实际: " + errors
        );
    }
}
