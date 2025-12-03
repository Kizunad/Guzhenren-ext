package com.Kizunad.customNPCs.ai.llm;

import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.actions.common.BlockWithShieldAction;
import com.Kizunad.customNPCs.ai.actions.common.EatFromInventoryAction;
import com.Kizunad.customNPCs.ai.actions.common.ForgetLongTermAction;
import com.Kizunad.customNPCs.ai.actions.common.RangedAttackItemAction;
import com.Kizunad.customNPCs.ai.actions.common.RememberLongTermAction;
import com.Kizunad.customNPCs.ai.actions.common.UseItemAction;
import com.Kizunad.customNPCs.ai.decision.goals.DefendGoal;
import com.Kizunad.customNPCs.ai.decision.goals.FleeGoal;
import com.Kizunad.customNPCs.ai.decision.goals.SeekShelterGoal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 集中管理提示词，便于编辑和重用。
 */
public final class PromptLibrary {

    private PromptLibrary() {}

    private static final List<String> BASE_ACTION_DESCS = List.of(
        DefendGoal.LLM_USAGE_DESC,
        FleeGoal.LLM_USAGE_DESC,
        SeekShelterGoal.LLM_USAGE_DESC,
        RangedAttackItemAction.LLM_USAGE_DESC,
        AttackAction.LLM_USAGE_DESC,
        BlockWithShieldAction.LLM_USAGE_DESC,
        MoveToAction.LLM_USAGE_DESC,
        EatFromInventoryAction.LLM_USAGE_DESC,
        UseItemAction.LLM_USAGE_DESC,
        RememberLongTermAction.LLM_USAGE_DESC,
        ForgetLongTermAction.LLM_USAGE_DESC,
        "Idle/Observe: wait/look around; Scan: check for threats/hazards."
    );

    // 后续可能需要按照是否能够成功执行来过滤提示词
    private static String buildCatalog() {
        Set<String> merged = new LinkedHashSet<>();
        merged.addAll(BASE_ACTION_DESCS);
        merged.addAll(LlmPromptRegistry.getAll());
        return String.join(" ", merged);
    }

    private static final String ACTION_CATALOG = String.join(
        " ",
        "Catalog:",
        buildCatalog(),
        "Optional params: maintain_distance (e.g. \"8-12\"),",
        "block_when \"<3.5\", flee_when_hp_pct (e.g. 0.25), heal_when_hp_pct (e.g. 0.4)."
    );

    public static final String SYSTEM_PROMPT = String.join(
        " ",
        "You are an NPC tactical planner. Prefer more living actions rather than idle/waiting.",
        "Output MUST be a single JSON object (no extra text/markdown/quoted JSON).",
        "Schema:",
        "{ \"plans\": [ { \"id\": \"string\", \"title\": \"string\",",
        "\"actions\": [\"ActionName\"], \"priority\": number, \"notes\": \"string\" } ],",
        "\"selected\": \"id or null\" }.",
        "Available goals/actions (use these names only):",
        ACTION_CATALOG,
        "Rules: no reasoning text; do NOT wrap JSON in quotes; keep actions short; finish_reason=stop."
    );
}
