package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.decision.goals.TestPlanGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 异常与回归测试：覆盖失败路径、目标消失、超时场景。
 */
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
public class RobustnessTests {

    public static void testMoveToBlockedPathFails(GameTestHelper helper) {
        BlockPos startPos = new BlockPos(2, 2, 2);
        BlockPos targetBlockPos = new BlockPos(2, 2, 12);

        Zombie zombie = TestEntityFactory.createSimpleTestNPC(helper, startPos, EntityType.ZOMBIE);
        INpcMind mind = NpcTestHelper.getMind(helper, zombie);
        // 禁用兜底传送，确保进入失败分支
        String testTag = NpcTestHelper.getTestTag(helper);
        zombie.removeTag(testTag);

        // 构建一堵实心墙阻断寻路
        for (int y = 2; y <= 4; y++) {
            for (int z = 5; z <= 11; z++) {
                helper.setBlock(new BlockPos(2, y, z), Blocks.OBSIDIAN);
            }
        }

        // 提交移动计划
        Vec3 targetPos = helper.absolutePos(targetBlockPos).getCenter();
        MoveToAction action = new MoveToAction(targetPos, 1.0, 1.5, 40);
        action.start(mind, zombie);

        ActionStatus status = ActionStatus.RUNNING;
        for (int i = 0; i < 5 && status == ActionStatus.RUNNING; i++) {
            status = action.tick(mind, zombie);
        }
        action.stop(mind, zombie);

        if (status == ActionStatus.FAILURE) {
            helper.succeed();
        } else {
            helper.fail("路径被阻挡时应返回 FAILURE, actual=" + status);
        }
    }

    public static void testMoveToTargetDespawnFails(GameTestHelper helper) {
        BlockPos moverPos = new BlockPos(2, 2, 2);
        BlockPos targetPos = new BlockPos(8, 2, 2);

        Zombie mover = TestEntityFactory.createSimpleTestNPC(helper, moverPos, EntityType.ZOMBIE);
        Zombie target = NpcTestHelper.spawnTaggedEntity(helper, EntityType.ZOMBIE, targetPos);
        INpcMind mind = NpcTestHelper.getMind(helper, mover);

        // 禁用兜底传送，避免提前成功
        String testTag = NpcTestHelper.getTestTag(helper);
        mover.removeTag(testTag);

        MoveToAction action = new MoveToAction(target, 1.0, 1.5, 80);
        action.start(mind, mover);

        target.discard();
        ActionStatus status = action.tick(mind, mover);
        action.stop(mind, mover);

        if (status == ActionStatus.FAILURE) {
            helper.succeed();
        } else {
            helper.fail("目标消失时应返回 FAILURE, actual=" + status);
        }
    }

    public static void testMoveToTimeoutFails(GameTestHelper helper) {
        BlockPos startPos = new BlockPos(2, 2, 2);

        Zombie zombie = TestEntityFactory.createSimpleTestNPC(helper, startPos, EntityType.ZOMBIE);
        INpcMind mind = NpcTestHelper.getMind(helper, zombie);

        // 关闭兜底传送，确保超时分支可见
        String testTag = NpcTestHelper.getTestTag(helper);
        zombie.removeTag(testTag);

        Vec3 farTarget = helper.absolutePos(new BlockPos(82, 2, 2)).getCenter();
        MoveToAction action = new MoveToAction(farTarget, 1.0, 2.0, 15);
        action.start(mind, zombie);

        ActionStatus status = ActionStatus.RUNNING;
        for (int i = 0; i < 25 && status == ActionStatus.RUNNING; i++) {
            status = action.tick(mind, zombie);
        }
        action.stop(mind, zombie);

        if (status == ActionStatus.FAILURE) {
            helper.succeed();
        } else {
            helper.fail("超时应返回 FAILURE 并终止计划, actual=" + status);
        }
    }
}
