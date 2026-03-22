package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2LangValidatorTest {

    @Test
    void shouldPassCurrentZhCnNameKeyCompletenessForTask33FirstSlice() throws IOException {
        // given: Task33 第一切片只检查当前注册锚点要求的 zh_cn 名称键完整性。
        // when: 使用仓库当前 zh_cn 与锚点动态收集出的 required name keys 执行校验。
        List<String> errors = Plan2LangValidator.validateCurrentZhCnNameKeys();

        // then: happy-path 必须无缺失；否则输出完整错误列表供审计。
        assertTrue(errors.isEmpty(), "Task33 第一切片 zh_cn 名称键应完整，实际错误: " + errors);
    }

    @Test
    void shouldReportExactMissingKeyNamesWhenZhCnEntriesMutatedInMemory() throws IOException {
        // given: 从真实文件读取并深拷贝语言映射，再在内存中删除两个关键名称键（不污染资源文件）。
        Map<String, String> mutatedLang = Plan2LangTestSupport.deepCopyLangEntries(
            Plan2LangTestSupport.readCurrentZhCnEntries()
        );
        mutatedLang.remove("block.guzhenrenext.lightning_attracting_fern");
        mutatedLang.remove("entity.guzhenrenext.calamity_beast");

        // when: 对“内存变异后的映射 + 当前 required keys”执行校验。
        Set<String> requiredKeys = Plan2LangTestSupport.collectCurrentRequiredNameKeys();
        List<String> errors = Plan2LangValidator.validate(mutatedLang, requiredKeys);

        // then: failure-path 必须带出精确缺失键名，而不是模糊地只说“有错误”。
        assertContains(errors, "plan2 lang missing zh_cn name key: block.guzhenrenext.lightning_attracting_fern");
        assertContains(errors, "plan2 lang missing zh_cn name key: entity.guzhenrenext.calamity_beast");
    }

    private static void assertContains(List<String> errors, String expected) {
        assertTrue(
            errors.stream().anyMatch(message -> message.contains(expected)),
            "期望错误包含: " + expected + "，实际: " + errors
        );
    }
}
