package com.Kizunad.customNPCs.ai.llm;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 统一注册 LLM 描述的目录，避免手工维护列表。
 */
public final class LlmPromptRegistry {

    private static final Set<String> DESCS = new LinkedHashSet<>();

    private LlmPromptRegistry() {}

    public static void register(String desc) {
        if (desc != null && !desc.isBlank()) {
            DESCS.add(desc);
        }
    }

    public static Set<String> getAll() {
        return Collections.unmodifiableSet(DESCS);
    }
}
