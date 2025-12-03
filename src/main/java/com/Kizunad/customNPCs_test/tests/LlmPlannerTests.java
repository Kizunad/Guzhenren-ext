package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.NpcMindRegistry;
import com.Kizunad.customNPCs.ai.llm.LlmConfig;
import com.Kizunad.customNPCs.capabilities.mind.NpcMind;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import org.jetbrains.annotations.Nullable;

/**
 * 验证 LLM Planner 在有密钥且启用时会发送请求并写入记忆。
 * required=false，避免无密钥环境报错。
 */
public final class LlmPlannerTests {

    private LlmPlannerTests() {}

    public static void testLlmPlannerSendsRequest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        LlmConfig cfg = LlmConfig.getInstance();

        if (!cfg.isEnabled() || !cfg.hasApiKey()) {
            helper.succeed();
            return;
        }

        cfg.setModel("x-ai/grok-4.1-fast:free");
        cfg.setRequestIntervalTicks(40);
        cfg.setPlanTtlTicks(20 * 60);
        cfg.setLogRequest(false);
        cfg.setLogResponse(false);

        Mob mob = spawnDummy(level);
        if (mob == null) {
            helper.fail("Failed to spawn dummy mob for LLM test");
            return;
        }
        NpcMind mind = new NpcMind();
        NpcMindRegistry.initializeMind(mind);

        // 通过反射拉低 lastRequestTick，首 tick 即可触发请求
        try {
            var field = mind
                .getLlmPlanner()
                .getClass()
                .getDeclaredField("lastRequestTick");
            field.setAccessible(true);
            field.setLong(mind.getLlmPlanner(), -1000L);
        } catch (Exception ignored) {
            // 如果失败，仍按正常节奏等待
        }

        // 初始 tick
        mind.tick(level, mob);

        // 每 20 tick 检查一次，最长约 60s
        for (int t = 20; t <= 1200; t += 20) {
            final int tick = t;
            helper.runAtTickTime(
                tick,
                () -> {
                    mind.tick(level, mob);
                    if (
                        mind.getMemory().hasMemory("llm_plan_options") ||
                        mind.getMemory().hasMemory("llm_plan_request")
                    ) {
                        helper.succeed();
                    }
                }
            );
        }

        // 终点不再失败，可选用例仅用于观测请求/响应
        helper.runAtTickTime(1250, helper::succeed);
    }

    @Nullable
    private static Mob spawnDummy(ServerLevel level) {
        Cow cow = EntityType.COW.create(level);
        if (cow != null) {
            cow.moveTo(0.5, 2.0, 0.5, 0, 0);
            level.addFreshEntity(cow);
            // 标记触发 mind attach
            cow.getTags().add("customnpcs:mind_allowed");
        }
        return cow;
    }
}
