package com.Kizunad.customNPCs_test.actions;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.goap.GoapAttackAction;
import com.Kizunad.customNPCs.ai.actions.goap.GoapUseItemAction;
import com.Kizunad.customNPCs.ai.actions.goap.GoapInteractBlockAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * GOAP包装类测试
 * <p>
 * 验证GoapAttackAction、GoapUseItemAction、GoapInteractBlockAction
 * 的前置条件、效果和Memory更新逻辑
 */
@GameTestHolder("guzhenren")
public class GoapActionsTests {

    /**
     * 测试：GoapAttackAction的前置条件正确定义
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testGoapAttackActionPreconditionsDefined(GameTestHelper helper) {
        BlockPos targetPos = new BlockPos(2, 1, 1);
        Mob target = helper.spawn(EntityType.PIG, targetPos);
        
        GoapAttackAction action = new GoapAttackAction(target.getUUID());
        WorldState preconditions = action.getPreconditions();
        
        // 验证前置条件包含必要的状态
        Object targetVisible = preconditions.getState(WorldStateKeys.TARGET_VISIBLE);
        Object targetInRange = preconditions.getState(WorldStateKeys.TARGET_IN_RANGE);
        
        if (Boolean.TRUE.equals(targetVisible) && Boolean.TRUE.equals(targetInRange)) {
            helper.succeed();
        } else {
            helper.fail("GoapAttackAction前置条件应该包含target_visible和target_in_range");
        }
    }

    /**
     * 测试：GoapAttackAction的效果正确定义
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testGoapAttackActionEffectsDefined(GameTestHelper helper) {
        BlockPos targetPos = new BlockPos(2, 1, 1);
        Mob target = helper.spawn(EntityType.PIG, targetPos);
        
        GoapAttackAction action = new GoapAttackAction(target.getUUID());
        WorldState effects = action.getEffects();
        
        // 验证效果包含必要的状态
        Object targetDamaged = effects.getState(WorldStateKeys.TARGET_DAMAGED);
        Object cooldownActive = effects.getState(WorldStateKeys.ATTACK_COOLDOWN_ACTIVE);
        
        if (Boolean.TRUE.equals(targetDamaged) && Boolean.TRUE.equals(cooldownActive)) {
            helper.succeed();
        } else {
            helper.fail("GoapAttackAction效果应该包含target_damaged和attack_cooldown_active");
        }
    }

    /**
     * 测试：GoapAttackAction的代价合理
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testGoapAttackActionCostReasonable(GameTestHelper helper) {
        BlockPos targetPos = new BlockPos(2, 1, 1);
        Mob target = helper.spawn(EntityType.PIG, targetPos);
        
        GoapAttackAction action = new GoapAttackAction(target.getUUID());
        float cost = action.getCost();
        
        // 代价应该在合理范围内（0-10）
        if (cost > 0 && cost <= 10) {
            helper.succeed();
        } else {
            helper.fail("GoapAttackAction代价应该在0-10之间，实际: " + cost);
        }
    }

    /**
     * 测试：GoapUseItemAction的前置条件和效果
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testGoapUseItemActionPreconditionsAndEffects(GameTestHelper helper) {
        GoapUseItemAction action = new GoapUseItemAction(Items.APPLE, "apple", true);
        
        WorldState preconditions = action.getPreconditions();
        WorldState effects = action.getEffects();
        
        // 验证前置条件
        Object hasItem = preconditions.getState(WorldStateKeys.hasItem("apple"));
        Object itemUsable = preconditions.getState(WorldStateKeys.ITEM_USABLE);
        
        // 验证效果
        Object itemUsed = effects.getState(WorldStateKeys.ITEM_USED);
        Object hungerRestored = effects.getState(WorldStateKeys.HUNGER_RESTORED);
        
        boolean preconditionsOk = Boolean.TRUE.equals(hasItem) && Boolean.TRUE.equals(itemUsable);
        boolean effectsOk = Boolean.TRUE.equals(itemUsed) && Boolean.TRUE.equals(hungerRestored);
        
        if (preconditionsOk && effectsOk) {
            helper.succeed();
        } else {
            helper.fail("GoapUseItemAction的前置条件或效果不正确");
        }
    }

    /**
     * 测试：GoapInteractBlockAction的状态键生成
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testGoapInteractBlockActionStateKeys(GameTestHelper helper) {
        BlockPos blockPos = new BlockPos(1, 1, 1);
        GoapInteractBlockAction action = new GoapInteractBlockAction(blockPos, true);
        
        WorldState preconditions = action.getPreconditions();
        WorldState effects = action.getEffects();
        
        // 验证前置条件使用正确的位置状态键
        String expectedKey = WorldStateKeys.atBlock(
            blockPos.getX(),
            blockPos.getY(),
            blockPos.getZ()
        );
        Object atBlock = preconditions.getState(expectedKey);
        Object blockExists = preconditions.getState(WorldStateKeys.BLOCK_EXISTS);
        
        // 验证效果
        Object blockInteracted = effects.getState(WorldStateKeys.BLOCK_INTERACTED);
        Object doorOpen = effects.getState(WorldStateKeys.DOOR_OPEN);
        
        boolean preconditionsOk = Boolean.TRUE.equals(atBlock) && Boolean.TRUE.equals(blockExists);
        boolean effectsOk = Boolean.TRUE.equals(blockInteracted) && Boolean.TRUE.equals(doorOpen);
        
        if (preconditionsOk && effectsOk) {
            helper.succeed();
        } else {
            helper.fail("GoapInteractBlockAction的状态键不正确");
        }
    }

    /**
     * 测试：GOAP动作的名称唯一性
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testGoapActionsUniqueNames(GameTestHelper helper) {
        BlockPos targetPos = new BlockPos(2, 1, 1);
        Mob target = helper.spawn(EntityType.PIG, targetPos);
        
        GoapAttackAction attack = new GoapAttackAction(target.getUUID());
        GoapUseItemAction useItem = new GoapUseItemAction(Items.APPLE, "apple", true);
        GoapInteractBlockAction interact = new GoapInteractBlockAction(new BlockPos(1, 1, 1), false);
        
        String attackName = attack.getName();
        String useName = useItem.getName();
        String interactName = interact.getName();
        
        // 名称应该各不相同
        boolean uniqueNames = !attackName.equals(useName) 
            && !attackName.equals(interactName) 
            && !useName.equals(interactName);
        
        if (uniqueNames) {
            helper.succeed();
        } else {
            helper.fail("GOAP动作名称应该唯一");
        }
    }
}
