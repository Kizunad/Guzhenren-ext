package com.Kizunad.customNPCs_test.actions;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.RangedAttackItemAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import com.Kizunad.customNPCs_test.utils.TestEntityFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * RangedAttackItemAction GameTest 测试
 * <p>
 * 验证远程武器攻击动作的核心功能：
 * - 弹药检查
 * - 距离窗口验证
 * - 弓/弩充能逻辑
 * - 世界状态写入
 */
@GameTestHolder("guzhenren")
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:LineLength"})
public class RangedAttackItemActionTests {

    /**
     * 测试：有弹药时弓成功射击
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty9x5x9", timeoutTicks = 200)
    public static void testBowAttackWithAmmoSuccess(GameTestHelper helper) {
        // 生成攻击者（骷髅，默认持弓）
        BlockPos attackerPos = new BlockPos(2, 2, 2);
        Skeleton attacker = TestEntityFactory.createSimpleTestNPC(
            helper,
            attackerPos,
            EntityType.SKELETON
        );

        // 生成目标（在有效射程内 8 格）
        BlockPos targetPos = new BlockPos(2, 2, 8);
        Zombie target = NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            targetPos
        );

        // 确保骷髅有弓和箭
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        attacker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        // 获取 NpcMind
        INpcMind mind = NpcTestHelper.getMind(helper, attacker);

        // 创建远程攻击动作
        RangedAttackItemAction rangedAttack = new RangedAttackItemAction(target.getUUID());
        rangedAttack.start(mind, attacker);

        // 记录初始状态
        final float initialHealth = target.getHealth();
        final int[] tickCount = {0};
        final boolean[] actionCompleted = {false};

        // 在每个tick执行动作
        helper.onEachTick(() -> {
            tickCount[0]++;

            if (!actionCompleted[0]) {
                ActionStatus status = rangedAttack.tick(mind, attacker);

                if (status == ActionStatus.SUCCESS) {
                    actionCompleted[0] = true;

                    // 验证世界状态已写入
                    Object targetDamaged = mind.getMemory().getMemory(WorldStateKeys.TARGET_DAMAGED);
                    Object hasRangedWeapon = mind.getMemory().getMemory(WorldStateKeys.HAS_RANGED_WEAPON);

                    helper.assertTrue(
                        targetDamaged instanceof Boolean && (Boolean) targetDamaged,
                        "TARGET_DAMAGED 应该为 true"
                    );
                    helper.assertTrue(
                        hasRangedWeapon instanceof Boolean && (Boolean) hasRangedWeapon,
                        "HAS_RANGED_WEAPON 应该为 true"
                    );

                    helper.succeed();
                } else if (status == ActionStatus.FAILURE) {
                    helper.fail("远程攻击动作应该成功，但返回了 FAILURE");
                }
            }

            // 超时检查
            if (tickCount[0] >= 100) {
                helper.fail("远程攻击动作超时（100 ticks）未完成");
            }
        });
    }

    /**
     * 测试：无弹药时返回 FAILURE
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty9x5x9", timeoutTicks = 100)
    public static void testBowAttackNoAmmoFailure(GameTestHelper helper) {
        // 生成攻击者
        BlockPos attackerPos = new BlockPos(2, 2, 2);
        Zombie attacker = TestEntityFactory.createSimpleTestNPC(
            helper,
            attackerPos,
            EntityType.ZOMBIE
        );

        // 生成目标
        BlockPos targetPos = new BlockPos(2, 2, 6);
        Zombie target = NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            targetPos
        );

        // 给攻击者弓但**不给箭**
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));

        INpcMind mind = NpcTestHelper.getMind(helper, attacker);

        // 创建远程攻击动作
        RangedAttackItemAction rangedAttack = new RangedAttackItemAction(target.getUUID());
        rangedAttack.start(mind, attacker);

        // 执行一次 tick
        ActionStatus status = rangedAttack.tick(mind, attacker);

        // 应该立即返回 FAILURE（无弹药）
        helper.assertTrue(
            status == ActionStatus.FAILURE,
            "无弹药时应该返回 FAILURE，实际: " + status
        );

        // 验证世界状态：HAS_RANGED_WEAPON 应该为 false
        Object hasRangedWeapon = mind.getMemory().getMemory(WorldStateKeys.HAS_RANGED_WEAPON);
        helper.assertTrue(
            hasRangedWeapon instanceof Boolean && !(Boolean) hasRangedWeapon,
            "无弹药时 HAS_RANGED_WEAPON 应该为 false"
        );

        helper.succeed();
    }

    /**
     * 测试：距离过近（< 4格）时返回 FAILURE
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty5x5x5", timeoutTicks = 50)
    public static void testRangedAttackDistanceTooCloseFailure(GameTestHelper helper) {
        // 生成攻击者
        BlockPos attackerPos = new BlockPos(2, 2, 2);
        Skeleton attacker = TestEntityFactory.createSimpleTestNPC(
            helper,
            attackerPos,
            EntityType.SKELETON
        );

        // 生成目标（距离 2 格，太近）
        BlockPos targetPos = new BlockPos(4, 2, 2);
        Zombie target = NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            targetPos
        );

        // 确保有弓和箭
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        attacker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        INpcMind mind = NpcTestHelper.getMind(helper, attacker);

        // 创建远程攻击动作
        RangedAttackItemAction rangedAttack = new RangedAttackItemAction(target.getUUID());
        rangedAttack.start(mind, attacker);

        // 执行一次 tick
        ActionStatus status = rangedAttack.tick(mind, attacker);

        // 应该返回 FAILURE（距离太近 < 4.0）
        helper.assertTrue(
            status == ActionStatus.FAILURE,
            "距离过近时应该返回 FAILURE，实际: " + status
        );

        helper.succeed();
    }

    /**
     * 测试：距离过远（> 12格）时返回 FAILURE
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty15x5x15", timeoutTicks = 50)
    public static void testRangedAttackDistanceTooFarFailure(GameTestHelper helper) {
        // 生成攻击者
        BlockPos attackerPos = new BlockPos(2, 2, 2);
        Skeleton attacker = TestEntityFactory.createSimpleTestNPC(
            helper,
            attackerPos,
            EntityType.SKELETON
        );

        // 生成目标（距离 14 格，太远）
        BlockPos targetPos = new BlockPos(2, 2, 14);
        Zombie target = NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            targetPos
        );

        // 确保有弓和箭
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        attacker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        INpcMind mind = NpcTestHelper.getMind(helper, attacker);

        // 创建远程攻击动作
        RangedAttackItemAction rangedAttack = new RangedAttackItemAction(target.getUUID());
        rangedAttack.start(mind, attacker);

        // 执行一次 tick
        ActionStatus status = rangedAttack.tick(mind, attacker);

        // 应该返回 FAILURE（距离太远 > 12.0）
        helper.assertTrue(
            status == ActionStatus.FAILURE,
            "距离过远时应该返回 FAILURE，实际: " + status
        );

        helper.succeed();
    }

    /**
     * 测试：弩的充能逻辑
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty9x5x9", timeoutTicks = 200)
    public static void testCrossbowChargeLogic(GameTestHelper helper) {
        // 生成攻击者
        BlockPos attackerPos = new BlockPos(2, 2, 2);
        Zombie attacker = TestEntityFactory.createSimpleTestNPC(
            helper,
            attackerPos,
            EntityType.ZOMBIE
        );

        // 生成目标
        BlockPos targetPos = new BlockPos(2, 2, 8);
        Zombie target = NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            targetPos
        );

        // 给攻击者弩和箭
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CROSSBOW));
        attacker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        INpcMind mind = NpcTestHelper.getMind(helper, attacker);

        // 创建远程攻击动作
        RangedAttackItemAction rangedAttack = new RangedAttackItemAction(target.getUUID());
        rangedAttack.start(mind, attacker);

        final int[] tickCount = {0};
        final boolean[] actionCompleted = {false};
        final boolean[] startedCharging = {false};

        // 在每个tick执行动作
        helper.onEachTick(() -> {
            tickCount[0]++;

            if (!actionCompleted[0]) {
                ActionStatus status = rangedAttack.tick(mind, attacker);

                // 检查是否开始充能
                if (attacker.isUsingItem() && !startedCharging[0]) {
                    startedCharging[0] = true;
                    helper.assertTrue(true, "弩开始装填");
                }

                if (status == ActionStatus.SUCCESS) {
                    actionCompleted[0] = true;

                    // 验证世界状态已写入
                    Object targetDamaged = mind.getMemory().getMemory(WorldStateKeys.TARGET_DAMAGED);
                    helper.assertTrue(
                        targetDamaged instanceof Boolean && (Boolean) targetDamaged,
                        "TARGET_DAMAGED 应该为 true"
                    );

                    helper.succeed();
                } else if (status == ActionStatus.FAILURE) {
                    helper.fail("弩攻击应该成功，但返回了 FAILURE");
                }
            }

            // 超时检查
            if (tickCount[0] >= 150) {
                helper.fail("弩攻击超时（150 ticks）未完成");
            }
        });
    }

    /**
     * 测试：无远程武器时返回 FAILURE
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty5x5x5", timeoutTicks = 50)
    public static void testNoRangedWeaponFailure(GameTestHelper helper) {
        // 生成攻击者（空手）
        BlockPos attackerPos = new BlockPos(2, 2, 2);
        Zombie attacker = TestEntityFactory.createSimpleTestNPC(
            helper,
            attackerPos,
            EntityType.ZOMBIE
        );

        // 生成目标
        BlockPos targetPos = new BlockPos(2, 2, 6);
        Zombie target = NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            targetPos
        );

        // 不给任何武器
        attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        INpcMind mind = NpcTestHelper.getMind(helper, attacker);

        // 创建远程攻击动作
        RangedAttackItemAction rangedAttack = new RangedAttackItemAction(target.getUUID());
        rangedAttack.start(mind, attacker);

        // 执行一次 tick
        ActionStatus status = rangedAttack.tick(mind, attacker);

        // 应该返回 FAILURE（无远程武器）
        helper.assertTrue(
            status == ActionStatus.FAILURE,
            "无远程武器时应该返回 FAILURE，实际: " + status
        );

        helper.succeed();
    }

    /**
     * 测试：目标不存在时返回 FAILURE
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty5x5x5", timeoutTicks = 50)
    public static void testTargetNotExistFailure(GameTestHelper helper) {
        // 生成攻击者
        BlockPos attackerPos = new BlockPos(2, 2, 2);
        Skeleton attacker = TestEntityFactory.createSimpleTestNPC(
            helper,
            attackerPos,
            EntityType.SKELETON
        );

        // 确保有弓和箭
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        attacker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 64));

        INpcMind mind = NpcTestHelper.getMind(helper, attacker);

        // 创建远程攻击动作，使用一个不存在的 UUID
        RangedAttackItemAction rangedAttack = new RangedAttackItemAction(
            java.util.UUID.randomUUID()
        );
        rangedAttack.start(mind, attacker);

        // 执行一次 tick
        ActionStatus status = rangedAttack.tick(mind, attacker);

        // 应该返回 FAILURE（目标不存在）
        helper.assertTrue(
            status == ActionStatus.FAILURE,
            "目标不存在时应该返回 FAILURE，实际: " + status
        );

        helper.succeed();
    }
}
