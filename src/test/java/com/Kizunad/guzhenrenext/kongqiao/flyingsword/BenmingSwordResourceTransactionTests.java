package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordReadonlyModifierHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BenmingSwordResourceTransactionTests {

    private static final double BASE_ZHENYUAN_COST = 40.0;
    private static final double BASE_NIANTOU_COST = 30.0;
    private static final double BASE_HUNPO_COST = 20.0;
    private static final double ACTUAL_ZHENYUAN_COST = 8.0;
    private static final double COST_SCALE_FACTOR = 0.5;
    private static final double DOUBLE_DELTA = 1.0E-9;
    private static final double QIYUN_MODERATE = 2500.0;
    private static final int REALM_MODERATE = 3;
    private static final double EXTREME_QIYUN = 1_000_000_000.0;
    private static final int EXTREME_REALM = 99;

    @Test
    void sufficientResourcesCommitAllValues() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(50.0, 50.0, 50.0, 0.0, 0);
        final BenmingSwordResourceTransaction.Request request = new BenmingSwordResourceTransaction.Request(
            BASE_ZHENYUAN_COST,
            BASE_NIANTOU_COST,
            BASE_HUNPO_COST
        );
        final RecordingMutationPort port = new RecordingMutationPort();

        final BenmingSwordResourceTransaction.Result result =
            BenmingSwordResourceTransaction.tryConsume(
                snapshot,
                request,
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                port
            );

        assertTrue(result.success());
        assertEquals(BenmingSwordResourceTransaction.FailureReason.NONE, result.failureReason());
        assertEquals(ACTUAL_ZHENYUAN_COST, result.zhenyuanCost(), DOUBLE_DELTA);
        assertEquals(BASE_NIANTOU_COST * COST_SCALE_FACTOR, result.niantouCost(), DOUBLE_DELTA);
        assertEquals(BASE_HUNPO_COST * COST_SCALE_FACTOR, result.hunpoCost(), DOUBLE_DELTA);
        assertEquals(3, port.operations().size());
        assertEquals("zhenyuan", port.operations().get(0).resource());
        assertEquals(ACTUAL_ZHENYUAN_COST, port.operations().get(0).amount(), DOUBLE_DELTA);
        assertEquals("niantou", port.operations().get(1).resource());
        assertEquals(BASE_NIANTOU_COST * COST_SCALE_FACTOR, port.operations().get(1).amount(), DOUBLE_DELTA);
        assertEquals("hunpo", port.operations().get(2).resource());
        assertEquals(BASE_HUNPO_COST * COST_SCALE_FACTOR, port.operations().get(2).amount(), DOUBLE_DELTA);
    }

    @Test
    void insufficientResourcesRollbackKeepsAllValues() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(50.0, 14.0, 50.0, 0.0, 0);
        final BenmingSwordResourceTransaction.Request request = new BenmingSwordResourceTransaction.Request(
            BASE_ZHENYUAN_COST,
            BASE_NIANTOU_COST,
            BASE_HUNPO_COST
        );
        final RecordingMutationPort port = new RecordingMutationPort();

        final BenmingSwordResourceTransaction.Result result =
            BenmingSwordResourceTransaction.tryConsume(
                snapshot,
                request,
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                port
            );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_NIANTOU,
            result.failureReason()
        );
        assertEquals(ACTUAL_ZHENYUAN_COST, result.zhenyuanCost(), DOUBLE_DELTA);
        assertEquals(BASE_NIANTOU_COST * COST_SCALE_FACTOR, result.niantouCost(), DOUBLE_DELTA);
        assertEquals(BASE_HUNPO_COST * COST_SCALE_FACTOR, result.hunpoCost(), DOUBLE_DELTA);
        assertTrue(port.operations().isEmpty());
    }

    @Test
    void failureReasonStableWhenZhenyuanNotEnough() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(5.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.Request request = new BenmingSwordResourceTransaction.Request(
            BASE_ZHENYUAN_COST,
            BASE_NIANTOU_COST,
            BASE_HUNPO_COST
        );
        final RecordingMutationPort port = new RecordingMutationPort();

        final BenmingSwordResourceTransaction.Result result =
            BenmingSwordResourceTransaction.tryConsume(
                snapshot,
                request,
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                port
            );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_ZHENYUAN,
            result.failureReason()
        );
        assertTrue(port.operations().isEmpty());
    }

    @Test
    void failureReasonStableWhenHunpoNotEnoughAndNoPartialMutation() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 9.0, 0.0, 0);
        final BenmingSwordResourceTransaction.Request request = new BenmingSwordResourceTransaction.Request(
            BASE_ZHENYUAN_COST,
            BASE_NIANTOU_COST,
            BASE_HUNPO_COST
        );
        final RecordingMutationPort port = new RecordingMutationPort();

        final BenmingSwordResourceTransaction.Result result =
            BenmingSwordResourceTransaction.tryConsume(
                snapshot,
                request,
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                port
            );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_HUNPO,
            result.failureReason()
        );
        assertEquals(ACTUAL_ZHENYUAN_COST, result.zhenyuanCost(), DOUBLE_DELTA);
        assertEquals(BASE_NIANTOU_COST * COST_SCALE_FACTOR, result.niantouCost(), DOUBLE_DELTA);
        assertEquals(BASE_HUNPO_COST * COST_SCALE_FACTOR, result.hunpoCost(), DOUBLE_DELTA);
        assertEquals(0, port.operations().size());
    }

    @Test
    void invalidRequestReturnsRejectedWithZeroCostsAndZeroMutation() {
        final RecordingMutationPort port = new RecordingMutationPort();

        final BenmingSwordResourceTransaction.Result result =
            BenmingSwordResourceTransaction.tryConsume(
                CultivationSnapshot.of(100.0, 100.0, 100.0, 17.0, 9),
                null,
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                port
            );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INVALID_REQUEST,
            result.failureReason()
        );
        assertEquals(0.0D, result.zhenyuanCost(), DOUBLE_DELTA);
        assertEquals(0.0D, result.niantouCost(), DOUBLE_DELTA);
        assertEquals(0.0D, result.hunpoCost(), DOUBLE_DELTA);
        assertEquals(0, port.operations().size());
    }

    @Test
    void readonlyQiyunRealmModifierHasMildBoundedRewardIncreaseInUnifiedPath() {
        final CultivationSnapshot lowSnapshot = CultivationSnapshot.of(200.0, 200.0, 200.0, 0.0, 0);
        final CultivationSnapshot highSnapshot = CultivationSnapshot.of(
            200.0,
            200.0,
            200.0,
            QIYUN_MODERATE,
            REALM_MODERATE
        );
        final BenmingSwordReadonlyModifierHelper.ReadonlyModifier lowModifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(lowSnapshot);
        final BenmingSwordReadonlyModifierHelper.ReadonlyModifier highModifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(highSnapshot);

        final double baseReward = 0.01D;
        final double lowReward = lowModifier.applyToReward(baseReward);
        final double highReward = highModifier.applyToReward(baseReward);

        assertEquals(baseReward, lowReward, DOUBLE_DELTA);
        assertTrue(highReward > lowReward);
        assertTrue(highReward <= baseReward * 1.2D);
        assertTrue(highModifier.finalMultiplier() <= 1.2D);
    }

    @Test
    void extremeQiyunRealmAreClampedAndPaymentSemanticsRemainUnchanged() {
        final CultivationSnapshot extremeSnapshot = CultivationSnapshot.of(
            500.0,
            11.9,
            500.0,
            EXTREME_QIYUN,
            EXTREME_REALM
        );
        final CultivationSnapshot baselineSnapshot = CultivationSnapshot.of(500.0, 11.9, 500.0, 0.0, 0);
        final BenmingSwordReadonlyModifierHelper.ReadonlyModifier modifier =
            BenmingSwordReadonlyModifierHelper.fromSnapshot(extremeSnapshot);
        final BenmingSwordResourceTransaction.Request request = new BenmingSwordResourceTransaction.Request(
            BASE_ZHENYUAN_COST,
            BASE_NIANTOU_COST,
            BASE_HUNPO_COST
        );
        final RecordingMutationPort extremePort = new RecordingMutationPort();
        final RecordingMutationPort baselinePort = new RecordingMutationPort();

        final BenmingSwordResourceTransaction.Result extremeResult =
            BenmingSwordResourceTransaction.tryConsume(
                extremeSnapshot,
                request,
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                extremePort
            );
        final BenmingSwordResourceTransaction.Result baselineResult =
            BenmingSwordResourceTransaction.tryConsume(
                baselineSnapshot,
                request,
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                baselinePort
            );

        assertEquals(1.0, modifier.physiqueMultiplier(), DOUBLE_DELTA);
        assertEquals(1.2, modifier.finalMultiplier(), DOUBLE_DELTA);
        assertFalse(extremeResult.success());
        assertFalse(baselineResult.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_NIANTOU,
            extremeResult.failureReason()
        );
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_NIANTOU,
            baselineResult.failureReason()
        );
        assertEquals(15.0, extremeResult.niantouCost(), DOUBLE_DELTA);
        assertEquals(15.0, baselineResult.niantouCost(), DOUBLE_DELTA);
        assertEquals(extremeResult.niantouCost(), baselineResult.niantouCost(), DOUBLE_DELTA);
        assertEquals(extremeResult.hunpoCost(), baselineResult.hunpoCost(), DOUBLE_DELTA);
        assertEquals(0, extremePort.operations().size());
        assertEquals(0, baselinePort.operations().size());
    }

    private record Operation(String resource, double amount) {}

    private static final class RecordingMutationPort
        implements BenmingSwordResourceTransaction.ResourceMutationPort {

        private final List<Operation> operations = new ArrayList<>();

        @Override
        public void spendZhenyuan(final double amount) {
            operations.add(new Operation("zhenyuan", amount));
        }

        @Override
        public void spendNiantou(final double amount) {
            operations.add(new Operation("niantou", amount));
        }

        @Override
        public void spendHunpo(final double amount) {
            operations.add(new Operation("hunpo", amount));
        }

        List<Operation> operations() {
            return operations;
        }
    }
}
