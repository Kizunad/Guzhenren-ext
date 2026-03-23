package com.Kizunad.guzhenrenext.plan2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Plan2LangValidator {

    private Plan2LangValidator() {
    }

    static List<String> validateCurrentZhCnNameKeys() throws IOException {
        // given: Task33 第一切片边界固定为“zh_cn 名称键完整性”，不扩展 tooltip/menu/message/en_us。
        // when: 从当前注册锚点动态收集 required name keys，并读取当前 zh_cn 映射。
        // then: 仅输出缺失名称键的错误列表，保证结果可审计、可复现。
        return validate(
            Plan2LangTestSupport.readCurrentZhCnEntries(),
            Plan2LangTestSupport.collectCurrentRequiredNameKeys()
        );
    }

    static List<String> validate(Map<String, String> langEntries, Set<String> requiredNameKeys) {
        // given: 调用方可注入“内存级变异后的语言映射”，用于 failure-path 稳定复现。
        List<String> missing = new ArrayList<>();
        for (String key : requiredNameKeys) {
            if (!langEntries.containsKey(key)) {
                missing.add(key);
            }
        }

        // when: 缺失键按字典序输出，避免因 Set 迭代顺序造成断言抖动。
        Collections.sort(missing);
        List<String> errors = new ArrayList<>();
        for (String key : missing) {
            errors.add("plan2 lang missing zh_cn name key: " + key);
        }
        // then: 每条错误都携带精确键名，便于主代理审计与快速定位。
        return errors;
    }
}
