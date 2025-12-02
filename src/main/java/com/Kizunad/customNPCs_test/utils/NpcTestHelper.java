package com.Kizunad.customNPCs_test.utils;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.function.Supplier;

/**
 * NPC 测试助手类
 * <p>
 * 提供标准化的测试工具方法，简化GameTest编写。
 */
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
public class NpcTestHelper {

    /**
     * 生成一个带有NpcMind的NPC
     * <p>
     * 自动附加NpcMind并返回配置好的实体
     *
     * @param helper GameTest 助手
     * @param pos 生成位置
     * @param entityType 实体类型
     * @param <T> Mob 子类型
     * @return 生成的实体（已附加NpcMind）
     */
    public static <T extends Mob> T spawnNPCWithMind(
        GameTestHelper helper,
        BlockPos pos,
        EntityType<T> entityType
    ) {
        ensureFloorAround(helper, pos, 160);
        T entity = helper.spawn(entityType, pos);
        applyTestTag(helper, entity);

        // 附加空 mind，由 AttachmentHandler 初始化
        entity.setData(NpcMindAttachment.NPC_MIND, new com.Kizunad.customNPCs.capabilities.mind.NpcMind());
        return entity;
    }

    /**
     * 等待条件满足（带超时）
     * <p>
     * 在每个tick检查条件，如果在超时前满足则成功，否则失败
     *
     * @param helper GameTest 助手
     * @param condition 要等待的条件
     * @param timeoutTicks 超时tick数
     * @param failureMessage 超时时的失败消息
     */
    public static void waitForCondition(
        GameTestHelper helper,
        Supplier<Boolean> condition,
        int timeoutTicks,
        String failureMessage
    ) {
        final int[] tickCount = {0};

        helper.onEachTick(() -> {
            tickCount[0]++;

            if (condition.get()) {
                helper.succeed();
                return;
            }

            if (tickCount[0] >= timeoutTicks) {
                helper.fail(failureMessage + " (超时: " + timeoutTicks + " ticks)");
            }
        });
    }

    /**
     * 等待断言通过（带超时）
     * <p>
     * 在每个tick运行断言，如果抛出GameTestAssertException则继续等待，直到超时
     *
     * @param helper GameTest 助手
     * @param assertion 断言逻辑（如果失败抛出异常）
     * @param failureMessage 超时时的失败消息
     */
    public static void waitForAssertion(
        GameTestHelper helper,
        Runnable assertion,
        String failureMessage
    ) {
        final int[] tickCount = {0};
        final int timeoutTicks = 100; // 默认超时

        helper.onEachTick(() -> {
            tickCount[0]++;

            try {
                assertion.run();
                helper.succeed();
            } catch (net.minecraft.gametest.framework.GameTestAssertException e) {
                if (tickCount[0] >= timeoutTicks) {
                    helper.fail(
                        failureMessage +
                        " (超时: " +
                        timeoutTicks +
                        " ticks). Last error: " +
                        e.getMessage()
                    );
                }
            } catch (Exception e) {
                helper.fail("Unexpected exception: " + e.getMessage());
            }
        });
    }

    public static void assertGoal(
        GameTestHelper helper,
        Mob npc,
        Class<? extends IGoal> goalClass
    ) {
        if (!npc.hasData(NpcMindAttachment.NPC_MIND)) {
            helper.fail("NPC 没有 NpcMind 附加");
            return;
        }

        INpcMind mind = npc.getData(NpcMindAttachment.NPC_MIND);
        IGoal currentGoal = mind.getGoalSelector().getCurrentGoal();

        if (currentGoal == null) {
            helper.fail("当前目标为 null，期望: " + goalClass.getSimpleName());
            return;
        }

        if (!goalClass.isInstance(currentGoal)) {
            helper.fail(
                "当前目标类型不匹配。期望: " +
                goalClass.getSimpleName() +
                ", 实际: " +
                currentGoal.getClass().getSimpleName()
            );
        }
    }

    public static void assertNPCPosition(
        GameTestHelper helper,
        Mob npc,
        Vec3 expectedPos,
        double tolerance
    ) {
        Vec3 actualPos = npc.position();
        double distance = actualPos.distanceTo(expectedPos);

        if (distance > tolerance) {
            helper.fail(
                "NPC位置不在期望范围内。期望: " +
                expectedPos +
                ", 实际: " +
                actualPos +
                ", 距离: " +
                distance +
                ", 容差: " +
                tolerance
            );
        }
    }

    public static void assertItemInHand(
        GameTestHelper helper,
        Mob npc,
        Item expectedItem,
        int expectedCount
    ) {
        ItemStack heldItem = npc.getMainHandItem();
        if (!heldItem.is(expectedItem)) {
            helper.fail(
                "主手物品不匹配。期望: " +
                expectedItem.getDescriptionId() +
                ", 实际: " +
                heldItem.getDescriptionId()
            );
        }

        if (expectedCount >= 0 && heldItem.getCount() != expectedCount) {
            helper.fail(
                "主手物品数量不匹配。期望: " +
                expectedCount +
                ", 实际: " +
                heldItem.getCount()
            );
        }
    }

    public static INpcMind getMind(GameTestHelper helper, Mob npc) {
        if (!npc.hasData(NpcMindAttachment.NPC_MIND)) {
            helper.fail("NPC 没有 NpcMind 附加");
            return null;
        }
        return npc.getData(NpcMindAttachment.NPC_MIND);
    }

    public static void applyTestTag(GameTestHelper helper, Entity entity) {
        String tag = getTestTag(helper);
        if (tag != null && !tag.isEmpty()) {
            entity.addTag(tag);
        }
    }

    public static String getTestTag(GameTestHelper helper) {
        return "test:" + System.identityHashCode(helper);
    }

    public static <T extends Entity> T spawnTaggedEntity(
        GameTestHelper helper,
        EntityType<T> entityType,
        BlockPos pos
    ) {
        ensureFloorAround(helper, pos, 160);
        T entity = helper.spawn(entityType, pos);
        applyTestTag(helper, entity);
        return entity;
    }

    public static void tickMind(GameTestHelper helper, Mob npc) {
        helper.onEachTick(() -> {
            if (npc.hasData(NpcMindAttachment.NPC_MIND)) {
                INpcMind mind = npc.getData(NpcMindAttachment.NPC_MIND);
                mind.tick(helper.getLevel(), npc);
            }
        });
    }

    public static void ensureFloorAround(
        GameTestHelper helper,
        BlockPos center,
        int radius
    ) {
        BlockPos absoluteCenter = helper.absolutePos(center);
        int baseY = absoluteCenter.getY() - 1;
        int startX = absoluteCenter.getX() - radius;
        int endX = absoluteCenter.getX() + radius;
        int startZ = absoluteCenter.getZ() - radius;
        int endZ = absoluteCenter.getZ() + radius;
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                helper
                    .getLevel()
                    .setBlock(
                        new BlockPos(x, baseY, z),
                        Blocks.STONE.defaultBlockState(),
                        3
                    );
            }
        }
    }

    /**
     * 提交 MoveToAction 到指定坐标（中心对齐），使用给定速度和可接受距离。
     */
    public static void submitMoveTo(
        GameTestHelper helper,
        Mob npc,
        BlockPos target,
        double speed,
        double acceptableDistance
    ) {
        INpcMind mind = getMind(helper, npc);
        if (mind == null) {
            return;
        }
        var action =
            new com.Kizunad.customNPCs.ai.actions.base.MoveToAction(
                new Vec3(
                    target.getX() + 0.5,
                    target.getY(),
                    target.getZ() + 0.5
                ),
                speed,
                acceptableDistance
            );
        mind.getActionExecutor().addAction(action);
    }
}
