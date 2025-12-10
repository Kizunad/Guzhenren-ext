package com.Kizunad.guzhenrenext.customNPCImpl.ai;

import com.Kizunad.customNPCs.ai.NpcMindRegistry;
import com.Kizunad.customNPCs.ai.actions.registry.AttackCompatRegistry;
import com.Kizunad.customNPCs.ai.actions.registry.HealCompatRegistry;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Action.GuInsectAttackAction;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Action.GuInsectHealAction;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Action.GuzhenrenPlaceholderAction;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Goal.CraftAttackGuInsectGoal;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Goal.CraftHealingGuInsectGoal;
import com.Kizunad.guzhenrenext.customNPCImpl.ai.Goal.WenyunKongqiaoGoal;

/**
 * 蛊真人扩展 AI 组件注册中心。
 * <p>
 * 在 Mod 初始化阶段调用 {@link #registerAll()} 以加载所有自定义的 Action, Goal 和 Sensor。
 */
public class Registery {

    // 使用用户提供的 Registery 文件名

    private static boolean initialized = false;

    public static void registerAll() {
        if (initialized) {
            return;
        }

        // 注册 Action
        registerActions();

        // 注册 Goal
        registerGoals();

        // 注册 Sensor
        registerSensors();

        registerCompatHandlers();

        initialized = true;
    }

    private static void registerActions() {
        // 注册你的占位 Action
        NpcMindRegistry.registerAction(
            "guzhenren_placeholder",
            GuzhenrenPlaceholderAction::new
        );

        // 示例：注册更多动作
        // NpcMindRegistry.registerAction("cultivate_action", CultivateAction::new);
    }

    private static void registerGoals() {
        NpcMindRegistry.registerGoal(
            "wenyun_kongqiao",
            WenyunKongqiaoGoal::new
        );
        NpcMindRegistry.registerGoal(
            "craft_attack_gu_insect",
            CraftAttackGuInsectGoal::new
        );
        NpcMindRegistry.registerGoal(
            "craft_healing_gu_insect",
            CraftHealingGuInsectGoal::new
        );
    }

    private static void registerSensors() {
        // 示例：注册新的蛊真人 Sensor
        // NpcMindRegistry.registerSensor("primeval_essence_sensor", PrimevalEssenceSensor::new);
        // NpcMindRegistry.registerSensor("gu_aura_sensor", GuAuraSensor::new);
    }

    private static void registerCompatHandlers() {
        AttackCompatRegistry.register(new GuInsectAttackAction());
        HealCompatRegistry.register(new GuInsectHealAction());
    }
}
