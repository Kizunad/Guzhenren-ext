package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.RangedAttackItemAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 验证远程攻击修复：后摇冷却与近身卡死检测。
 */
public class RangedAttackFixTests {

    /**
     * 验证射击后进入后摇，而不是立即 SUCCESS。
     */
    public static void testRangedAttackPostFire(GameTestHelper helper) {
        Mob shooter = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.SKELETON,
            false
        );
        // Skeleton 自带弓
        shooter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        shooter.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        LivingEntity target = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 1));
        INpcMind mind = NpcTestHelper.getMind(helper, shooter);

        RangedAttackItemAction action = new RangedAttackItemAction(target.getUUID());
        action.start(mind, shooter);

        final boolean[] enteredRunningState = {false};

        // 运行动作
        helper.onEachTick(() -> {
            ActionStatus status = action.tick(mind, shooter);
            
            // 当 charge 足够时（20 ticks），它应该进入后摇 (RUNNING)
            // 我们可以通过 tick 计数来推断，或者通过观察状态
            if (status == ActionStatus.RUNNING) {
                enteredRunningState[0] = true;
            }
            
            if (status == ActionStatus.SUCCESS) {
                if (!enteredRunningState[0]) {
                    helper.fail("Action succeeded without entering RUNNING state (post-fire cooldown skipped)");
                }
                helper.succeed();
            }
        });
    }

    /**
     * 验证被迫近身超过 60 ticks 后动作失败。
     */
    public static void testRangedAttackStuckDetection(GameTestHelper helper) {
        Mob shooter = TestEntityFactory.createTestNPC(
            helper,
            new BlockPos(1, 2, 1),
            EntityType.SKELETON,
            false
        );
        shooter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        shooter.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        // 目标贴脸 (距离 1.0)
        LivingEntity target = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 2));
        target.setInvulnerable(true); // 防止窒息或被射死
        
        // 构建 3x3x3 基岩牢笼，将射手和目标困在 1x1x1 的空间内（或者是相邻的空间）
        // 射手在 (1, 2, 1)，目标在 (1, 2, 2)
        // 我们需要封死所有可能的逃跑路线
        
        // 底部 (y=1) 和 顶部 (y=3)
        for (int x = 0; x <= 2; x++) {
            for (int z = 0; z <= 3; z++) { // 稍微长一点包住目标
                helper.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.BEDROCK);
                helper.setBlock(new BlockPos(x, 3, z), net.minecraft.world.level.block.Blocks.BEDROCK);
            }
        }
        
        // 四周 (y=2)
        // 左 (x=0) 和 右 (x=2)
        for (int z = 0; z <= 3; z++) {
            helper.setBlock(new BlockPos(0, 2, z), net.minecraft.world.level.block.Blocks.BEDROCK);
            helper.setBlock(new BlockPos(2, 2, z), net.minecraft.world.level.block.Blocks.BEDROCK);
        }
        // 后 (z=0) 和 前 (z=3)
        for (int x = 0; x <= 2; x++) {
            helper.setBlock(new BlockPos(x, 2, 0), net.minecraft.world.level.block.Blocks.BEDROCK);
            helper.setBlock(new BlockPos(x, 2, 3), net.minecraft.world.level.block.Blocks.BEDROCK);
        }
        
        INpcMind mind = NpcTestHelper.getMind(helper, shooter);
        RangedAttackItemAction action = new RangedAttackItemAction(target.getUUID());
        action.start(mind, shooter);

        final int[] ticks = {0};
        helper.onEachTick(() -> {
            ActionStatus status = action.tick(mind, shooter);
            ticks[0]++;
            
            if (status == ActionStatus.FAILURE) {
                // 预期在 60 ticks 左右失败
                if (ticks[0] >= 60) {
                    helper.succeed();
                } else {
                    helper.fail("Action failed too early: " + ticks[0]);
                }
            }
            
            if (ticks[0] > 100) {
                helper.fail("Action did not fail after stuck timeout");
            }
        });
    }
}
