package com.Kizunad.customNPCs_test.utils;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
            EntityType<T> entityType) {
        ensureFloorAround(helper, pos, 160);
        T entity = helper.spawn(entityType, pos);
        applyTestTag(helper, entity);

        // 验证NpcMind已附加
        if (!entity.hasData(NpcMindAttachment.NPC_MIND)) {
            helper.fail("NpcMind应该在实体生成时自动附加，但实体 "
                    + entity.getType().getDescription().getString() + " 没有NpcMind");
        }

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
            String failureMessage) {
        final int[] tickCount = {0};

        helper.onEachTick(() -> {
            tickCount[0]++;

            // 检查条件
            if (condition.get()) {
                helper.succeed();
                return;
            }

            // 检查超时
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
            String failureMessage) {
        final int[] tickCount = {0};
        final int timeoutTicks = 100; // 默认超时

        helper.onEachTick(() -> {
            tickCount[0]++;

            try {
                assertion.run();
                helper.succeed();
            } catch (net.minecraft.gametest.framework.GameTestAssertException e) {
                // 断言失败，继续等待
                if (tickCount[0] >= timeoutTicks) {
                    helper.fail(failureMessage + " (超时: " + timeoutTicks + " ticks). Last error: " + e.getMessage());
                }
            } catch (Exception e) {
                // 其他异常直接失败
                helper.fail("Unexpected exception: " + e.getMessage());
            }
        });
    }

    /**
     * 断言NPC当前目标是指定类型
     *
     * @param helper GameTest 助手
     * @param npc NPC实体
     * @param goalClass 期望的目标类
     */
    public static void assertGoal(GameTestHelper helper, Mob npc, Class<? extends IGoal> goalClass) {
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
            helper.fail("当前目标类型不匹配。期望: " + goalClass.getSimpleName()
                    + ", 实际: " + currentGoal.getClass().getSimpleName());
        }
    }

    /**
     * 断言NPC位置在期望位置附近
     *
     * @param helper GameTest 助手
     * @param npc NPC实体
     * @param expectedPos 期望位置
     * @param tolerance 容差距离
     */
    public static void assertNPCPosition(
            GameTestHelper helper,
            Mob npc,
            Vec3 expectedPos,
            double tolerance) {
        Vec3 actualPos = npc.position();
        double distance = actualPos.distanceTo(expectedPos);

        if (distance > tolerance) {
            helper.fail("NPC位置不在期望范围内。期望: " + expectedPos
                    + ", 实际: " + actualPos + ", 距离: " + distance + ", 容差: " + tolerance);
        }
    }

    /**
     * 断言NPC主手持有指定物品
     *
     * @param helper GameTest 助手
     * @param npc NPC实体
     * @param expectedItem 期望的物品
     * @param expectedCount 期望的数量（-1表示不检查数量）
     */
    public static void assertItemInHand(
            GameTestHelper helper,
            Mob npc,
            Item expectedItem,
            int expectedCount) {
        ItemStack heldItem = npc.getMainHandItem();

        if (heldItem.isEmpty()) {
            helper.fail("NPC主手为空，期望持有: " + expectedItem.getDescriptionId());
            return;
        }

        if (!heldItem.is(expectedItem)) {
            helper.fail("NPC主手物品不匹配。期望: " + expectedItem.getDescriptionId()
                    + ", 实际: " + heldItem.getItem().getDescriptionId());
            return;
        }

        if (expectedCount >= 0 && heldItem.getCount() != expectedCount) {
            helper.fail("NPC主手物品数量不匹配。期望: " + expectedCount
                    + ", 实际: " + heldItem.getCount());
        }
    }

    /**
     * 获取NPC的NpcMind（如果存在）
     *
     * @param helper GameTest 助手
     * @param npc NPC实体
     * @return NpcMind实例
     */
    public static INpcMind getMind(GameTestHelper helper, Mob npc) {
        if (!npc.hasData(NpcMindAttachment.NPC_MIND)) {
            helper.fail("NPC 没有 NpcMind 附加");
            return null;
        }
        return npc.getData(NpcMindAttachment.NPC_MIND);
    }

    /**
     * 将测试标签写入实体，便于传感器过滤跨测试干扰。
     */
    public static void applyTestTag(GameTestHelper helper, Entity entity) {
        String tag = getTestTag(helper);
        if (tag != null && !tag.isEmpty()) {
            entity.addTag(tag);
        }
    }

    /**
     * 基于 GameTest 名称生成当前测试的标签。
     */
    public static String getTestTag(GameTestHelper helper) {
        // GameTestHelper 未公开提供测试名，这里使用 helper 实例的 identityHashCode 生成稳定标签
        return "test:" + System.identityHashCode(helper);
    }

    /**
     * 生成一个带标签的常规实体（无NpcMind）。
     */
    public static <T extends Entity> T spawnTaggedEntity(
            GameTestHelper helper,
            EntityType<T> entityType,
            BlockPos pos) {
        ensureFloorAround(helper, pos, 160);
        T entity = helper.spawn(entityType, pos);
        applyTestTag(helper, entity);
        return entity;
    }

    /**
     * 在每个tick驱动NPC的Mind
     * <p>
     * 应该在测试开始时调用此方法，以确保NpcMind能够正常工作
     *
     * @param helper GameTest 助手
     * @param npc NPC实体
     */
    public static void tickMind(GameTestHelper helper, Mob npc) {
        helper.onEachTick(() -> {
            if (npc.hasData(NpcMindAttachment.NPC_MIND)) {
                INpcMind mind = npc.getData(NpcMindAttachment.NPC_MIND);
                mind.tick(helper.getLevel(), npc);
            }
        });
    }

    /**
     * 确保给定位置周围有一层石头地板，防止实体生成后下落导致寻路失败。
     * @param helper GameTest 助手
     * @param center 地板中心（使用相对坐标）
     * @param radius 覆盖半径（方形）
     */
    public static void ensureFloorAround(GameTestHelper helper, BlockPos center, int radius) {
        BlockPos absoluteCenter = helper.absolutePos(center);
        int baseY = absoluteCenter.getY() - 1;
        int startX = absoluteCenter.getX() - radius;
        int endX = absoluteCenter.getX() + radius;
        int startZ = absoluteCenter.getZ() - radius;
        int endZ = absoluteCenter.getZ() + radius;
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                helper.getLevel().setBlock(new BlockPos(x, baseY, z), Blocks.STONE.defaultBlockState(), 3);
            }
        }
    }
}
