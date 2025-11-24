package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

/**
 * 记忆系统测试逻辑
 */
public class MemoryTests {

    private static final int SHORT_MEMORY_DURATION = 20;
    private static final int TICK_DELAY = 30;
    private static final int LONG_MEMORY_DURATION = 100;
    private static final int SPAWN_OFFSET = 4;

    public static void testMemoryExpiration(GameTestHelper helper) {
        // 使用工厂创建僵尸
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        
        // 获取 Mind
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 启动 Mind tick (关键：否则记忆不会过期)
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 添加一个短期记忆，持续 20 ticks (1秒)
        mind.getMemory().rememberShortTerm("test_memory", "value", SHORT_MEMORY_DURATION);
        
        // 验证记忆存在
        helper.assertTrue(mind.getMemory().hasMemory("test_memory"), "Memory should exist initially");
        
        // 等待 30 ticks (超过过期时间)
        helper.runAtTickTime(TICK_DELAY, () -> {
            // 验证记忆已过期
            helper.assertTrue(!mind.getMemory().hasMemory("test_memory"), 
                "Memory should have expired after " + TICK_DELAY + " ticks");
            helper.succeed();
        });
    }

    public static void testMemoryPersistence(GameTestHelper helper) {
        // 使用工厂创建僵尸
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        
        // 添加长期记忆
        mind.getMemory().rememberLongTerm("long_term_key", "persistent_value");
        // 添加短期记忆
        mind.getMemory().rememberShortTerm("short_term_key", "temporary_value", LONG_MEMORY_DURATION);
        
        // 模拟序列化
        CompoundTag nbt = mind.serializeNBT(helper.getLevel().registryAccess());
        
        // 生成另一个僵尸（模拟重新加载后的实体）
        Zombie newZombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(SPAWN_OFFSET, 2, 2), EntityType.ZOMBIE);
        INpcMind newMind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, newZombie);
        
        // 模拟反序列化
        newMind.deserializeNBT(helper.getLevel().registryAccess(), nbt);
        
        // 验证记忆是否恢复
        helper.succeedWhen(() -> {
            // 验证长期记忆
            helper.assertTrue(newMind.getMemory().hasMemory("long_term_key"), "Long term memory should be persisted");
            helper.assertTrue(newMind.getMemory().getMemory("long_term_key").equals("persistent_value"), 
                "Long term memory value should match");
                
            // 验证短期记忆
            helper.assertTrue(newMind.getMemory().hasMemory("short_term_key"), "Short term memory should be persisted");
            helper.assertTrue(newMind.getMemory().getMemory("short_term_key").equals("temporary_value"), 
                "Short term memory value should match");
        });
    }
}
