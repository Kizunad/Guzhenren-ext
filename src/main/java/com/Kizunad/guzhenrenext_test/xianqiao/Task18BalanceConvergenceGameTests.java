package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.block.XianqiaoBlocks;
import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.ClusterNpcEntity;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationManager;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class Task18BalanceConvergenceGameTests {

    private static final String TASK18_BALANCE_CONVERGENCE_BATCH = "task18_balance_convergence";
    private static final int TEST_TIMEOUT_TICKS = 240;
    private static final int BLOCK_SET_FLAGS = Block.UPDATE_ALL;

    private static final int CONTROLLER_RELATIVE_X = 6;
    private static final int CONTROLLER_RELATIVE_Y = 2;
    private static final int CONTROLLER_RELATIVE_Z = 2;

    private static final int EXPECTED_RECHECK_AFTER_FIRST_TICK = 0;

    private static final int EXPECTED_RECHECK_AFTER_SECOND_TICK = 100;

    private static final boolean EXPECTED_UNFORMED_STATE = false;

    private static final int EXPECTED_RECHECK_INTERVAL = 100;
    private static final int EXPECTED_CLUSTER_STEP_INTERVAL = 20;

    private static final int RESOURCE_REQUIRED_AURA_MIN = 100;
    private static final int RESOURCE_REQUIRED_AURA_MAX = 400;
    private static final int RESOURCE_BONUS_AURA_MIN = 800;
    private static final int RESOURCE_BONUS_AURA_MAX = 2000;
    private static final float RESOURCE_LOW_EFFICIENCY_MIN = 0.05F;
    private static final float RESOURCE_LOW_EFFICIENCY_MAX = 0.2F;
    private static final float RESOURCE_NORMAL_EFFICIENCY_MIN = 0.9F;
    private static final float RESOURCE_NORMAL_EFFICIENCY_MAX = 1.1F;
    private static final float RESOURCE_BONUS_EFFICIENCY_MIN = 1.2F;
    private static final float RESOURCE_BONUS_EFFICIENCY_MAX = 2.0F;

    private static final double CLUSTER_DAO_CAP_MIN = 2.0D;
    private static final double CLUSTER_DAO_CAP_MAX = 8.0D;
    private static final double CLUSTER_RACE_CAP_MIN = 1.5D;
    private static final double CLUSTER_RACE_CAP_MAX = 3.0D;

    private static final float TRIBULATION_REWARD_SCORE_MIN = 0.5F;
    private static final float TRIBULATION_REWARD_SCORE_MAX = 0.9F;
    private static final float TRIBULATION_FAILURE_DAMAGE_MIN = 0.7F;
    private static final float TRIBULATION_FAILURE_DAMAGE_MAX = 0.95F;
    private static final int TRIBULATION_SUCCESS_AURA_MIN = 80;
    private static final int TRIBULATION_SUCCESS_AURA_MAX = 240;
    private static final int TRIBULATION_FAILURE_AURA_MIN = 60;
    private static final int TRIBULATION_FAILURE_AURA_MAX = 200;

    private static final double DOUBLE_EPSILON = 0.0001D;

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK18_BALANCE_CONVERGENCE_BATCH)
    public void testTask18BalanceConvergenceShouldKeepEconomicParametersInGuardedRanges(GameTestHelper helper) {
        int requiredAura = readStaticIntField(ResourceControllerBlockEntity.class, "REQUIRED_AURA");
        int bonusAura = readStaticIntField(ResourceControllerBlockEntity.class, "BONUS_AURA");
        float lowEfficiency = readStaticFloatField(ResourceControllerBlockEntity.class, "LOW_EFFICIENCY");
        float normalEfficiency = readStaticFloatField(ResourceControllerBlockEntity.class, "NORMAL_EFFICIENCY");
        float bonusEfficiency = readStaticFloatField(ResourceControllerBlockEntity.class, "BONUS_EFFICIENCY");

        double daoMultiplierCap = readStaticDoubleField(ClusterNpcEntity.class, "DAO_MARK_MULTIPLIER_CAP");
        double raceMultiplierCap = readStaticDoubleField(ClusterNpcEntity.class, "RACIAL_MULTIPLIER_CAP");

        float rewardScoreThreshold = readStaticFloatField(TribulationManager.class, "REWARD_SCORE_THRESHOLD");
        float failureDamageRatio = readStaticFloatField(TribulationManager.class, "FAILURE_DAMAGE_RATIO");
        int successAuraReward = readStaticIntField(TribulationManager.class, "SUCCESS_AURA_REWARD");
        int failureAuraPenalty = readStaticIntField(TribulationManager.class, "FAILURE_AURA_PENALTY");

        helper.assertTrue(
            isIntInRange(requiredAura, RESOURCE_REQUIRED_AURA_MIN, RESOURCE_REQUIRED_AURA_MAX),
            "Task18/economy: REQUIRED_AURA 必须处于预设守门区间"
        );
        helper.assertTrue(
            isIntInRange(bonusAura, RESOURCE_BONUS_AURA_MIN, RESOURCE_BONUS_AURA_MAX),
            "Task18/economy: BONUS_AURA 必须处于预设守门区间"
        );
        helper.assertTrue(
            isFloatInRange(lowEfficiency, RESOURCE_LOW_EFFICIENCY_MIN, RESOURCE_LOW_EFFICIENCY_MAX),
            "Task18/economy: LOW_EFFICIENCY 必须处于预设守门区间"
        );
        helper.assertTrue(
            isFloatInRange(normalEfficiency, RESOURCE_NORMAL_EFFICIENCY_MIN, RESOURCE_NORMAL_EFFICIENCY_MAX),
            "Task18/economy: NORMAL_EFFICIENCY 必须处于预设守门区间"
        );
        helper.assertTrue(
            isFloatInRange(bonusEfficiency, RESOURCE_BONUS_EFFICIENCY_MIN, RESOURCE_BONUS_EFFICIENCY_MAX),
            "Task18/economy: BONUS_EFFICIENCY 必须处于预设守门区间"
        );

        helper.assertTrue(
            isDoubleInRange(daoMultiplierCap, CLUSTER_DAO_CAP_MIN, CLUSTER_DAO_CAP_MAX),
            "Task18/economy: DAO_MARK_MULTIPLIER_CAP 必须处于预设守门区间"
        );
        helper.assertTrue(
            isDoubleInRange(raceMultiplierCap, CLUSTER_RACE_CAP_MIN, CLUSTER_RACE_CAP_MAX),
            "Task18/economy: RACIAL_MULTIPLIER_CAP 必须处于预设守门区间"
        );

        helper.assertTrue(
            isFloatInRange(rewardScoreThreshold, TRIBULATION_REWARD_SCORE_MIN, TRIBULATION_REWARD_SCORE_MAX),
            "Task18/economy: REWARD_SCORE_THRESHOLD 必须处于预设守门区间"
        );
        helper.assertTrue(
            isFloatInRange(failureDamageRatio, TRIBULATION_FAILURE_DAMAGE_MIN, TRIBULATION_FAILURE_DAMAGE_MAX),
            "Task18/economy: FAILURE_DAMAGE_RATIO 必须处于预设守门区间"
        );
        helper.assertTrue(
            isIntInRange(successAuraReward, TRIBULATION_SUCCESS_AURA_MIN, TRIBULATION_SUCCESS_AURA_MAX),
            "Task18/economy: SUCCESS_AURA_REWARD 必须处于预设守门区间"
        );
        helper.assertTrue(
            isIntInRange(failureAuraPenalty, TRIBULATION_FAILURE_AURA_MIN, TRIBULATION_FAILURE_AURA_MAX),
            "Task18/economy: FAILURE_AURA_PENALTY 必须处于预设守门区间"
        );

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK18_BALANCE_CONVERGENCE_BATCH)
    public void testTask18BalanceConvergenceShouldEnforceTickGuardrailCadence(GameTestHelper helper) {
        ResourceControllerBlockEntity controller = placeResourceController(helper);

        setPrivateIntField(controller, "structureRecheckCooldown", 1);
        tickControllerOnce(helper, controller);
        int recheckAfterFirstTick = readPrivateIntField(controller, "structureRecheckCooldown");
        helper.assertTrue(
            recheckAfterFirstTick == EXPECTED_RECHECK_AFTER_FIRST_TICK,
            "Task18/perf: 首次 tick 后重检倒计时应递减到 0"
        );

        tickControllerOnce(helper, controller);
        int recheckAfterSecondTick = readPrivateIntField(controller, "structureRecheckCooldown");
        boolean formedState = readPrivateBooleanField(controller, "isFormed");
        helper.assertTrue(
            recheckAfterSecondTick == EXPECTED_RECHECK_AFTER_SECOND_TICK,
            "Task18/perf: 命中重检后必须回到固定周期，防止每 tick 全量扫描"
        );
        helper.assertTrue(
            formedState == EXPECTED_UNFORMED_STATE,
            "Task18/perf: 未构建结构时必须保持 isFormed=false，避免无效推进"
        );

        int controllerRecheckInterval = readStaticIntField(
            ResourceControllerBlockEntity.class,
            "STRUCTURE_RECHECK_INTERVAL"
        );
        int clusterProductionStepInterval = readStaticIntField(
            ClusterNpcEntity.class,
            "PRODUCTION_STEP_INTERVAL_TICKS"
        );
        helper.assertTrue(
            controllerRecheckInterval == EXPECTED_RECHECK_INTERVAL,
            "Task18/perf: ResourceController 重检周期常量漂移，需显式评审"
        );
        helper.assertTrue(
            clusterProductionStepInterval == EXPECTED_CLUSTER_STEP_INTERVAL,
            "Task18/perf: ClusterNpc 生产步进周期常量漂移，需显式评审"
        );

        helper.succeed();
    }

    private static ResourceControllerBlockEntity placeResourceController(GameTestHelper helper) {
        BlockPos absolutePos = helper.absolutePos(
            new BlockPos(CONTROLLER_RELATIVE_X, CONTROLLER_RELATIVE_Y, CONTROLLER_RELATIVE_Z)
        );
        helper.getLevel().setBlock(
            absolutePos,
            XianqiaoBlocks.RESOURCE_CONTROLLER.get().defaultBlockState(),
            BLOCK_SET_FLAGS
        );
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(blockEntity instanceof ResourceControllerBlockEntity, "Task18: 资源控制器方块实体创建失败");
        return (ResourceControllerBlockEntity) blockEntity;
    }

    private static void tickControllerOnce(GameTestHelper helper, ResourceControllerBlockEntity controller) {
        ResourceControllerBlockEntity.serverTick(
            helper.getLevel(),
            controller.getBlockPos(),
            helper.getLevel().getBlockState(controller.getBlockPos()),
            controller
        );
    }

    private static int readPrivateIntField(Object target, String fieldName) {
        Field field = readField(target.getClass(), fieldName);
        try {
            return field.getInt(target);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("读取私有整数字段失败: " + fieldName, exception);
        }
    }

    private static boolean readPrivateBooleanField(Object target, String fieldName) {
        Field field = readField(target.getClass(), fieldName);
        try {
            return field.getBoolean(target);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("读取私有布尔字段失败: " + fieldName, exception);
        }
    }

    private static void setPrivateIntField(Object target, String fieldName, int value) {
        Field field = readField(target.getClass(), fieldName);
        try {
            field.setInt(target, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("写入私有整数字段失败: " + fieldName, exception);
        }
    }

    private static int readStaticIntField(Class<?> owner, String fieldName) {
        Field field = readField(owner, fieldName);
        try {
            return field.getInt(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("读取静态整数字段失败: " + owner.getSimpleName() + "." + fieldName, exception);
        }
    }

    private static float readStaticFloatField(Class<?> owner, String fieldName) {
        Field field = readField(owner, fieldName);
        try {
            return field.getFloat(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("读取静态浮点字段失败: " + owner.getSimpleName() + "." + fieldName, exception);
        }
    }

    private static double readStaticDoubleField(Class<?> owner, String fieldName) {
        Field field = readField(owner, fieldName);
        try {
            return field.getDouble(null);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("读取静态双精度字段失败: " + owner.getSimpleName() + "." + fieldName, exception);
        }
    }

    private static Field readField(Class<?> owner, String fieldName) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException("读取字段失败: " + owner.getSimpleName() + "." + fieldName, exception);
        }
    }

    private static boolean isIntInRange(int value, int minInclusive, int maxInclusive) {
        return value >= minInclusive && value <= maxInclusive;
    }

    private static boolean isFloatInRange(float value, float minInclusive, float maxInclusive) {
        return value >= minInclusive && value <= maxInclusive;
    }

    private static boolean isDoubleInRange(double value, double minInclusive, double maxInclusive) {
        return value + DOUBLE_EPSILON >= minInclusive && value - DOUBLE_EPSILON <= maxInclusive;
    }
}
