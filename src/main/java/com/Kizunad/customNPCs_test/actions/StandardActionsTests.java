package com.Kizunad.customNPCs_test.actions;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.AttackAction;
import com.Kizunad.customNPCs.ai.actions.common.UseItemAction;
import com.Kizunad.customNPCs.ai.actions.util.NavigationUtil;
import com.Kizunad.customNPCs.capabilities.mind.NpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 标准动作GameTest测试
 * <p>
 * 验证AttackAction、UseItemAction和NavigationUtil的核心功能
 */
@GameTestHolder("guzhenren")
public class StandardActionsTests {

    /**
     * 测试：AttackAction成功攻击目标
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty5x5x5")
    public static void testAttackActionTargetInRangeSuccess(GameTestHelper helper) {
        // 生成攻击者和目标
        BlockPos attackerPos = new BlockPos(1, 1, 1);
        BlockPos targetPos = new BlockPos(3, 1, 1);
        
        Mob attacker = helper.spawn(EntityType.ZOMBIE, attackerPos);
        Mob target = helper.spawn(EntityType.PIG, targetPos);
        
        NpcMind mind = new NpcMind();
        
        // 创建攻击动作
        AttackAction attackAction = new AttackAction(target.getUUID());
        attackAction.start(mind, attacker);
        
        // 记录初始血量
        float initialHealth = target.getHealth();
        
        // 执行攻击（多次tick确保攻击完成）
        ActionStatus status = ActionStatus.RUNNING;
        int maxTicks = 100;
        int ticks = 0;
        
        while (status == ActionStatus.RUNNING && ticks < maxTicks) {
            status = attackAction.tick(mind, attacker);
            ticks++;
        }
        
        // 验证目标受到伤害
        if (target.getHealth() < initialHealth) {
            helper.succeed();
        } else {
            helper.fail("目标应该受到伤害");
        }
    }

    /**
     * 测试：NavigationUtil距离判定
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testNavigationUtilIsInRangeAccurate(GameTestHelper helper) {
        Vec3 pos1 = new Vec3(0, 0, 0);
        Vec3 pos2 = new Vec3(2, 0, 0);
        Vec3 pos3 = new Vec3(5, 0, 0);
        
        // 测试在范围内
        boolean inRange = NavigationUtil.isInRange(pos1, pos2, 3.0);
        if (!inRange) {
            helper.fail("距离2应该在范围3内");
            return;
        }
        
        // 测试不在范围内
        boolean outOfRange = NavigationUtil.isInRange(pos1, pos3, 3.0);
        if (outOfRange) {
            helper.fail("距离5应该不在范围3内");
            return;
        }
        
        helper.succeed();
    }

    /**
     * 测试：NavigationUtil hasArrived带缓冲
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testNavigationUtilHasArrivedWithBuffer(GameTestHelper helper) {
        Vec3 pos1 = new Vec3(0, 0, 0);
        Vec3 pos2 = new Vec3(2.5, 0, 0);
        
        // 距离2.5，阈值2.0，缓冲0.75，总计2.75 > 2.5 应该算作已到达
        boolean arrived = NavigationUtil.hasArrived(pos1, pos2, 2.0);
        
        if (arrived) {
            helper.succeed();
        } else {
            helper.fail("距离2.5应该在阈值2.0+缓冲0.75内");
        }
    }

    /**
     * 测试：NavigationUtil卡住检测
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testNavigationUtilIsStuckDetectsImmobility(GameTestHelper helper) {
        Vec3 currentPos = new Vec3(1, 1, 1);
        Vec3 lastPos = new Vec3(1, 1, 1); // 相同位置
        
        int stuckTicks = 50; // 已经卡住50 ticks
        int maxStuckTicks = 40; // 最大允许40 ticks
        
        boolean stuck = NavigationUtil.isStuck(currentPos, lastPos, stuckTicks, maxStuckTicks);
        
        if (stuck) {
            helper.succeed();
        } else {
            helper.fail("应该检测到卡住");
        }
    }

    /**
     * 测试：UseItemAction超时机制
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3", timeoutTicks = 400)
    public static void testUseItemActionTimeoutReturnsSuccess(GameTestHelper helper) {
        BlockPos spawnPos = new BlockPos(1, 1, 1);
        Mob mob = helper.spawn(EntityType.ZOMBIE, spawnPos);
        NpcMind mind = new NpcMind();
        
        // 创建UseItemAction（假设没有物品）
        UseItemAction useAction = new UseItemAction(Items.APPLE);
        useAction.start(mind, mob);
        
        // 执行多个tick直到超时
        ActionStatus status = ActionStatus.RUNNING;
        int ticks = 0;
        int maxTicks = 350; // 超过默认超时时间
        
        while (status == ActionStatus.RUNNING && ticks < maxTicks) {
            status = useAction.tick(mind, mob);
            ticks++;
            helper.runAfterDelay(1, () -> {});
        }
        
        // 应该超时并返回SUCCESS或FAILURE
        if (status != ActionStatus.RUNNING) {
            helper.succeed();
        } else {
            helper.fail("动作应该在超时后结束");
        }
    }
}
