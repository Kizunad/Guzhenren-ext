package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.sensors.InterruptThrottle;
import com.Kizunad.customNPCs.ai.sensors.SensorEventType;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * 验证 InterruptThrottle 对同一目标/距离桶的去重节流逻辑。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class InterruptThrottleTests {

    /**
     * 同一目标+同一距离桶在时间窗内仅允许第一次触发。
     */
    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testInterruptThrottleDedupSameTarget(
        GameTestHelper helper
    ) {
        InterruptThrottle throttle = new InterruptThrottle(10, 25, 25);
        UUID target = UUID.randomUUID();
        long baseTick = 100;

        if (
            !throttle.allowInterrupt(
                target,
                SensorEventType.CRITICAL,
                0,
                baseTick
            )
        ) {
            helper.fail("第一次触发应被允许");
            return;
        }

        if (
            throttle.allowInterrupt(
                target,
                SensorEventType.CRITICAL,
                0,
                baseTick + 5
            )
        ) {
            helper.fail("时间窗内重复触发应被节流");
            return;
        }

        if (
            !throttle.allowInterrupt(
                target,
                SensorEventType.CRITICAL,
                0,
                baseTick + 11
            )
        ) {
            helper.fail("超过时间窗后应恢复允许触发");
            return;
        }

        helper.succeed();
    }

    /**
     * 目标或距离桶/等级变化时应视为新的事件，允许触发。
     */
    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testInterruptThrottleAllowsDifferentContext(
        GameTestHelper helper
    ) {
        InterruptThrottle throttle = new InterruptThrottle(10, 25, 25);
        UUID target = UUID.randomUUID();
        long baseTick = 200;

        if (
            !throttle.allowInterrupt(
                target,
                SensorEventType.IMPORTANT,
                1,
                baseTick
            )
        ) {
            helper.fail("初次触发应被允许");
            return;
        }

        // 距离桶变化
        if (
            !throttle.allowInterrupt(
                target,
                SensorEventType.IMPORTANT,
                2,
                baseTick + 5
            )
        ) {
            helper.fail("距离桶变化应视为新事件");
            return;
        }

        // 目标变化
        if (
            !throttle.allowInterrupt(
                UUID.randomUUID(),
                SensorEventType.IMPORTANT,
                2,
                baseTick + 6
            )
        ) {
            helper.fail("新目标应被允许触发");
            return;
        }

        // 等级变化
        if (
            !throttle.allowInterrupt(
                target,
                SensorEventType.INFO,
                2,
                baseTick + 7
            )
        ) {
            helper.fail("事件等级变化应被允许触发");
            return;
        }

        helper.succeed();
    }
}
