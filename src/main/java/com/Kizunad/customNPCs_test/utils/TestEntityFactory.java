package com.Kizunad.customNPCs_test.utils;

import com.Kizunad.customNPCs.ai.sensors.VisionSensor;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;

/**
 * 测试实体工厂
 * <p>
 * 提供预配置的测试实体创建方法，确保实体正确初始化。
 */
@SuppressWarnings({"checkstyle:MagicNumber"})
public class TestEntityFactory {

    /**
     * 创建标准测试Zombie（带NpcMind和VisionSensor）
     *
     * @param helper GameTest助手
     * @param pos 生成位置
     * @return 配置好的Zombie实体
     */
    public static Zombie createTestZombie(GameTestHelper helper, BlockPos pos) {
        Zombie zombie = NpcTestHelper.spawnNPCWithMind(helper, pos, EntityType.ZOMBIE);
        resetMindState(helper, zombie, true);

        return zombie;
    }

    /**
     * 创建标准测试NPC（泛型版本）
     *
     * @param helper GameTest助手
     * @param pos 生成位置
     * @param entityType 实体类型
     * @param withVisionSensor 是否添加视觉传感器
     * @param <T> Mob子类型
     * @return 配置好的实体
     */
    public static <T extends Mob> T createTestNPC(
            GameTestHelper helper,
            BlockPos pos,
            EntityType<T> entityType,
            boolean withVisionSensor) {
        T npc = NpcTestHelper.spawnNPCWithMind(helper, pos, entityType);
        resetMindState(helper, npc, withVisionSensor);

        return npc;
    }

    /**
     * 创建简单测试NPC（无传感器）
     *
     * @param helper GameTest助手
     * @param pos 生成位置
     * @param entityType 实体类型
     * @param <T> Mob子类型
     * @return 配置好的实体
     */
    public static <T extends Mob> T createSimpleTestNPC(
            GameTestHelper helper,
            BlockPos pos,
            EntityType<T> entityType) {
        T npc = NpcTestHelper.spawnNPCWithMind(helper, pos, entityType);
        resetMindState(helper, npc, false);
        return npc;
    }

    /**
     * 重置测试实体的 NpcMind，确保不存在跨测试遗留的目标、传感器或动作计划。
     */
    private static <T extends Mob> INpcMind resetMindState(
            GameTestHelper helper,
            T npc,
            boolean withVisionSensor) {
        // 确保实体带有测试标签，供传感器过滤
        NpcTestHelper.applyTestTag(helper, npc);
        npc.setData(NpcMindAttachment.NPC_MIND, new NpcMind());

        if (!npc.hasData(NpcMindAttachment.NPC_MIND)) {
            helper.fail("NpcMind 未能附加到实体 "
                    + npc.getType().getDescription().getString());
            return null;
        }

        INpcMind mind = npc.getData(NpcMindAttachment.NPC_MIND);
        mind.getActionExecutor().stopCurrentPlan();

        if (withVisionSensor) {
            mind.getSensorManager().registerSensor(new VisionSensor());
        }

        return mind;
    }
}
