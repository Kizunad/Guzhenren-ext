package com.Kizunad.customNPCs_test.actions;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.config.ActionConfig;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 配置和工具类测试
 * <p>
 * 验证ActionConfig和WorldStateKeys的功能
 */
@GameTestHolder("guzhenren")
public class ConfigAndUtilsTests {

    /**
     * 测试：ActionConfig单例模式
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testActionConfigSingletonSameInstance(GameTestHelper helper) {
        ActionConfig instance1 = ActionConfig.getInstance();
        ActionConfig instance2 = ActionConfig.getInstance();
        
        if (instance1 == instance2) {
            helper.succeed();
        } else {
            helper.fail("ActionConfig应该返回同一个单例实例");
        }
    }

    /**
     * 测试：ActionConfig默认值合理
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testActionConfigDefaultValuesReasonable(GameTestHelper helper) {
        ActionConfig config = ActionConfig.getInstance();
        
        // 重置到默认值
        config.resetToDefaults();
        
        // 验证默认值在合理范围内
        boolean attackRangeOk = config.getAttackRange() > 0 && config.getAttackRange() <= 20;
        boolean timeoutOk = config.getDefaultTimeoutTicks() > 0;
        boolean retriesOk = config.getDefaultMaxRetries() >= 0;
        boolean navRangeOk = config.getDefaultNavRange() > 0;
        boolean pathUpdateOk = config.getPathUpdateInterval() > 0;
        
        if (attackRangeOk && timeoutOk && retriesOk && navRangeOk && pathUpdateOk) {
            helper.succeed();
        } else {
            helper.fail("ActionConfig默认值不在合理范围内");
        }
    }

    /**
     * 测试：ActionConfig setter方法生效
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testActionConfigSettersTakeEffect(GameTestHelper helper) {
        ActionConfig config = ActionConfig.getInstance();
        
        // 设置新值
        config.setAttackRange(5.0);
        config.setDebugLoggingEnabled(true);
        
        // 验证新值生效
        boolean attackRangeSet = Math.abs(config.getAttackRange() - 5.0) < 0.01;
        boolean debugEnabled = config.isDebugLoggingEnabled();
        
        // 恢复默认值
        config.resetToDefaults();
        
        if (attackRangeSet && debugEnabled) {
            helper.succeed();
        } else {
            helper.fail("ActionConfig setter方法应该生效");
        }
    }

    /**
     * 测试：ActionConfig resetToDefaults功能
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testActionConfigResetToDefaultsResets(GameTestHelper helper) {
        ActionConfig config = ActionConfig.getInstance();
        
        // 记录默认值
        double defaultAttackRange = config.getAttackRange();
        
        // 修改值
        config.setAttackRange(999.0);
        
        // 重置
        config.resetToDefaults();
        
        // 验证恢复到默认值
        if (Math.abs(config.getAttackRange() - defaultAttackRange) < 0.01) {
            helper.succeed();
        } else {
            helper.fail("resetToDefaults应该恢复默认值");
        }
    }

    /**
     * 测试：WorldStateKeys hasItem方法生成唯一键
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testWorldStateKeysHasItemUniqueKeys(GameTestHelper helper) {
        String appleKey = WorldStateKeys.hasItem("apple");
        String breadKey = WorldStateKeys.hasItem("bread");
        
        if (!appleKey.equals(breadKey) && appleKey.contains("apple") && breadKey.contains("bread")) {
            helper.succeed();
        } else {
            helper.fail("hasItem应该生成唯一的状态键");
        }
    }

    /**
     * 测试：WorldStateKeys atBlock方法生成正确的位置键
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testWorldStateKeysAtBlockCorrectFormat(GameTestHelper helper) {
        String key = WorldStateKeys.atBlock(10, 20, 30);
        
        // 键应该包含坐标信息
        boolean containsCoords = key.contains("10") && key.contains("20") && key.contains("30");
        
        if (containsCoords) {
            helper.succeed();
        } else {
            helper.fail("atBlock应该生成包含坐标的状态键，实际: " + key);
        }
    }

    /**
     * 测试：WorldStateKeys常量值不为null
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testWorldStateKeysConstantsNotNull(GameTestHelper helper) {
        boolean allNotNull = 
            WorldStateKeys.TARGET_VISIBLE != null &&
            WorldStateKeys.TARGET_IN_RANGE != null &&
            WorldStateKeys.TARGET_DAMAGED != null &&
            WorldStateKeys.ATTACK_COOLDOWN_ACTIVE != null &&
            WorldStateKeys.ITEM_USABLE != null &&
            WorldStateKeys.ITEM_USED != null &&
            WorldStateKeys.HUNGER_RESTORED != null &&
            WorldStateKeys.BLOCK_EXISTS != null &&
            WorldStateKeys.BLOCK_INTERACTED != null &&
            WorldStateKeys.DOOR_OPEN != null;
        
        if (allNotNull) {
            helper.succeed();
        } else {
            helper.fail("WorldStateKeys常量不应该为null");
        }
    }

    /**
     * 测试：WorldStateKeys常量值唯一
     */
    @GameTest(templateNamespace = "guzhenren", template = "empty3x3x3")
    public static void testWorldStateKeysConstantsUnique(GameTestHelper helper) {
        // 检查几个关键常量是否唯一
        boolean unique = !WorldStateKeys.TARGET_VISIBLE.equals(WorldStateKeys.TARGET_IN_RANGE)
            && !WorldStateKeys.ITEM_USABLE.equals(WorldStateKeys.ITEM_USED)
            && !WorldStateKeys.BLOCK_EXISTS.equals(WorldStateKeys.BLOCK_INTERACTED);
        
        if (unique) {
            helper.succeed();
        } else {
            helper.fail("WorldStateKeys常量应该唯一");
        }
    }
}
