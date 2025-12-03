package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 删除 LLM 长期记忆的动作。
 */
public class ForgetLongTermAction implements IAction {

    public static final String LLM_USAGE_DESC =
        "ForgetLongTermAction(key): remove specific key from LLM long-term memory.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private final String key;
    private boolean done;

    public ForgetLongTermAction(String key) {
        this.key = key;
        this.done = false;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        if (done) {
            return ActionStatus.SUCCESS;
        }
        if (key == null || key.isBlank()) {
            MindLog.execution(
                MindLogLevel.WARN,
                "[LLM] 删除长期记忆失败，key 为空"
            );
            return ActionStatus.FAILURE;
        }
        mind.getLongTermMemory().forget(key);
        MindLog.execution(
            MindLogLevel.INFO,
            "[LLM] 删除长期记忆: {}",
            key
        );
        done = true;
        return ActionStatus.SUCCESS;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        // 无需额外准备
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需清理
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    @Override
    public String getName() {
        return "forget_long_term";
    }
}
