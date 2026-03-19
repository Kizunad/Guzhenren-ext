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
    private static final double SCALED_NIANTOU_COST = BASE_NIANTOU_COST * COST_SCALE_FACTOR;
    private static final double SCALED_HUNPO_COST = BASE_HUNPO_COST * COST_SCALE_FACTOR;
    private static final double PHASE_OVERLOAD_BEFORE = 12.0;
    private static final double PHASE_OVERLOAD_DELTA = 6.0;

    private static final int TEST_MAGIC_3 = 3;
    private static final double TEST_MAGIC_92_0 = 92.0;
    private static final double TEST_MAGIC_85_0 = 85.0;
    private static final double TEST_MAGIC_90_0 = 90.0;
    private static final double TEST_MAGIC_100_0 = 100.0;
    private static final double TEST_MAGIC_1_2D = 1.2D;
    private static final double TEST_MAGIC_1_2 = 1.2;
    private static final double TEST_MAGIC_15_0 = 15.0;

    private static final double PHASE_OVERLOAD_AFTER =
        PHASE_OVERLOAD_BEFORE + PHASE_OVERLOAD_DELTA;
    private static final double PHASE_OVERLOAD_LIMIT = 40.0;
    private static final double TIGHT_OVERLOAD_LIMIT = 15.0;
    private static final long CURRENT_TICK = 100L;
    private static final long READY_BURST_COOLDOWN = 80L;
    private static final long ACTIVE_BURST_COOLDOWN = 140L;
    private static final long NEXT_BURST_COOLDOWN = 180L;
    private static final long READY_RITUAL_LOCK = 90L;
    private static final long NEXT_RITUAL_LOCK = 220L;
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
        assertEquals(TEST_MAGIC_3, port.operations().size());
        assertEquals("zhenyuan", port.operations().get(0).resource());
        assertEquals(ACTUAL_ZHENYUAN_COST, port.operations().get(0).amount(), DOUBLE_DELTA);
        assertEquals("niantou", port.operations().get(1).resource());
        assertEquals(BASE_NIANTOU_COST * COST_SCALE_FACTOR, port.operations().get(1).amount(), DOUBLE_DELTA);
        assertEquals("hunpo", port.operations().get(2).resource());
        assertEquals(BASE_HUNPO_COST * COST_SCALE_FACTOR, port.operations().get(2).amount(), DOUBLE_DELTA);
    }

    @Test
    void phaseAwareCommitUpdatesResourcesAndPhaseStateAtomically() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard =
            BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                true,
                true,
                true,
                PHASE_OVERLOAD_BEFORE,
                true,
                PHASE_OVERLOAD_LIMIT,
                CURRENT_TICK,
                READY_BURST_COOLDOWN,
                READY_RITUAL_LOCK
            );
        final AtomicRecordingMutationPort port = AtomicRecordingMutationPort.from(snapshot, phaseGuard);

        final BenmingSwordResourceTransaction.Result result = runPhaseAwareTransaction(
            snapshot,
            phaseRequest(phaseGuard),
            port
        );

        assertTrue(result.success());
        assertEquals(BenmingSwordResourceTransaction.FailureReason.NONE, result.failureReason());
        assertEquals(BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD, result.swordStateSource());
        assertResourceState(port, TEST_MAGIC_92_0, TEST_MAGIC_85_0, TEST_MAGIC_90_0);
        assertPhaseState(port, PHASE_OVERLOAD_AFTER, NEXT_BURST_COOLDOWN, NEXT_RITUAL_LOCK);
        assertPhaseOutcome(new ExpectedPhaseOutcome(
            result.phaseOutcome(),
            BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD,
            PHASE_OVERLOAD_BEFORE,
            PHASE_OVERLOAD_AFTER,
            READY_BURST_COOLDOWN,
            NEXT_BURST_COOLDOWN,
            READY_RITUAL_LOCK,
            NEXT_RITUAL_LOCK,
            true,
            true,
            true
        ));
        assertEquals(
            List.of(
                "spendZhenyuan:8.0",
                "spendNiantou:15.0",
                "spendHunpo:10.0",
                "setOverload:18.0",
                "setBurstCooldown:180",
                "setRitualLock:220"
            ),
            port.events()
        );
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
    void illegalSwordStateRejectedBeforeAnyMutation() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard =
            BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                false,
                false,
                false,
                PHASE_OVERLOAD_BEFORE,
                true,
                PHASE_OVERLOAD_LIMIT,
                CURRENT_TICK,
                READY_BURST_COOLDOWN,
                READY_RITUAL_LOCK
            );
        final AtomicRecordingMutationPort port = AtomicRecordingMutationPort.from(snapshot, phaseGuard);

        final BenmingSwordResourceTransaction.Result result = runPhaseAwareTransaction(
            snapshot,
            phaseRequest(phaseGuard),
            port
        );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.ILLEGAL_SWORD_STATE,
            result.failureReason()
        );
        assertEquals(BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD, result.swordStateSource());
        assertResourceState(port, TEST_MAGIC_100_0, TEST_MAGIC_100_0, TEST_MAGIC_100_0);
        assertPhaseState(port, PHASE_OVERLOAD_BEFORE, READY_BURST_COOLDOWN, READY_RITUAL_LOCK);
        assertPhaseOutcome(new ExpectedPhaseOutcome(
            result.phaseOutcome(),
            BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD,
            PHASE_OVERLOAD_BEFORE,
            PHASE_OVERLOAD_BEFORE,
            READY_BURST_COOLDOWN,
            READY_BURST_COOLDOWN,
            READY_RITUAL_LOCK,
            READY_RITUAL_LOCK,
            false,
            false,
            false
        ));
        assertTrue(port.events().isEmpty());
    }

    @Test
    void ritualLockRejectedBeforeAnyMutation() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard =
            BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                true,
                false,
                true,
                PHASE_OVERLOAD_BEFORE,
                true,
                PHASE_OVERLOAD_LIMIT,
                CURRENT_TICK,
                READY_BURST_COOLDOWN,
                NEXT_RITUAL_LOCK
            );
        final AtomicRecordingMutationPort port = AtomicRecordingMutationPort.from(snapshot, phaseGuard);

        final BenmingSwordResourceTransaction.Result result = runPhaseAwareTransaction(
            snapshot,
            phaseRequest(phaseGuard),
            port
        );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.RITUAL_LOCK_ACTIVE,
            result.failureReason()
        );
        assertEquals(BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD, result.swordStateSource());
        assertResourceState(port, TEST_MAGIC_100_0, TEST_MAGIC_100_0, TEST_MAGIC_100_0);
        assertPhaseState(port, PHASE_OVERLOAD_BEFORE, READY_BURST_COOLDOWN, NEXT_RITUAL_LOCK);
        assertTrue(port.events().isEmpty());
    }

    @Test
    void burstCooldownActiveRejectedWithWithdrawnSource() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard =
            BenmingSwordResourceTransaction.PhaseGuard.withdrawnIllegalDetach(
                true,
                true,
                false,
                PHASE_OVERLOAD_BEFORE,
                true,
                PHASE_OVERLOAD_LIMIT,
                CURRENT_TICK,
                ACTIVE_BURST_COOLDOWN,
                READY_RITUAL_LOCK
            );
        final AtomicRecordingMutationPort port = AtomicRecordingMutationPort.from(snapshot, phaseGuard);

        final BenmingSwordResourceTransaction.Result result = runPhaseAwareTransaction(
            snapshot,
            phaseRequest(phaseGuard),
            port
        );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.BURST_COOLDOWN_ACTIVE,
            result.failureReason()
        );
        assertEquals(
            BenmingSwordResourceTransaction.SwordStateSource.WITHDRAWN_ILLEGAL_DETACH,
            result.swordStateSource()
        );
        assertResourceState(port, TEST_MAGIC_100_0, TEST_MAGIC_100_0, TEST_MAGIC_100_0);
        assertPhaseState(port, PHASE_OVERLOAD_BEFORE, ACTIVE_BURST_COOLDOWN, READY_RITUAL_LOCK);
        assertTrue(port.events().isEmpty());
    }

    @Test
    void overloadLimitRejectedWithRecalledSource() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard =
            BenmingSwordResourceTransaction.PhaseGuard.recalledSword(
                true,
                false,
                false,
                PHASE_OVERLOAD_BEFORE,
                true,
                TIGHT_OVERLOAD_LIMIT,
                CURRENT_TICK,
                READY_BURST_COOLDOWN,
                READY_RITUAL_LOCK
            );
        final AtomicRecordingMutationPort port = AtomicRecordingMutationPort.from(snapshot, phaseGuard);

        final BenmingSwordResourceTransaction.Result result = runPhaseAwareTransaction(
            snapshot,
            phaseRequest(phaseGuard),
            port
        );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.OVERLOAD_LIMIT_EXCEEDED,
            result.failureReason()
        );
        assertEquals(
            BenmingSwordResourceTransaction.SwordStateSource.RECALLED_SWORD,
            result.swordStateSource()
        );
        assertResourceState(port, TEST_MAGIC_100_0, TEST_MAGIC_100_0, TEST_MAGIC_100_0);
        assertPhaseState(port, PHASE_OVERLOAD_BEFORE, READY_BURST_COOLDOWN, READY_RITUAL_LOCK);
        assertPhaseOutcome(new ExpectedPhaseOutcome(
            result.phaseOutcome(),
            BenmingSwordResourceTransaction.SwordStateSource.RECALLED_SWORD,
            PHASE_OVERLOAD_BEFORE,
            PHASE_OVERLOAD_BEFORE,
            READY_BURST_COOLDOWN,
            READY_BURST_COOLDOWN,
            READY_RITUAL_LOCK,
            READY_RITUAL_LOCK,
            false,
            false,
            false
        ));
        assertTrue(port.events().isEmpty());
    }

    @Test
    void phaseStateWriteFailureRollsBackResourcesAndPhaseState() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard =
            BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                true,
                true,
                true,
                PHASE_OVERLOAD_BEFORE,
                true,
                PHASE_OVERLOAD_LIMIT,
                CURRENT_TICK,
                READY_BURST_COOLDOWN,
                READY_RITUAL_LOCK
            );
        final AtomicRecordingMutationPort port = AtomicRecordingMutationPort.from(snapshot, phaseGuard);
        port.failOn(AtomicRecordingMutationPort.FailurePoint.SET_BURST_COOLDOWN_AFTER_WRITE);

        final BenmingSwordResourceTransaction.Result result = runPhaseAwareTransaction(
            snapshot,
            phaseRequest(phaseGuard),
            port
        );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.PHASE_STATE_WRITE_FAILED,
            result.failureReason()
        );
        assertEquals(BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD, result.swordStateSource());
        assertResourceState(port, TEST_MAGIC_100_0, TEST_MAGIC_100_0, TEST_MAGIC_100_0);
        assertPhaseState(port, PHASE_OVERLOAD_BEFORE, READY_BURST_COOLDOWN, READY_RITUAL_LOCK);
        assertPhaseOutcome(new ExpectedPhaseOutcome(
            result.phaseOutcome(),
            BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD,
            PHASE_OVERLOAD_BEFORE,
            PHASE_OVERLOAD_BEFORE,
            READY_BURST_COOLDOWN,
            READY_BURST_COOLDOWN,
            READY_RITUAL_LOCK,
            READY_RITUAL_LOCK,
            false,
            false,
            false
        ));
        assertEquals(
            List.of(
                "spendZhenyuan:8.0",
                "spendNiantou:15.0",
                "spendHunpo:10.0",
                "setOverload:18.0",
                "setBurstCooldown:180",
                "setBurstCooldown:80",
                "setOverload:12.0",
                "refundHunpo:10.0",
                "refundNiantou:15.0",
                "refundZhenyuan:8.0"
            ),
            port.events()
        );
    }

    @Test
    void atomicUnsupportedRejectedBeforeMutation() {
        final CultivationSnapshot snapshot = CultivationSnapshot.of(100.0, 100.0, 100.0, 0.0, 0);
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard =
            BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                true,
                true,
                true,
                PHASE_OVERLOAD_BEFORE,
                true,
                PHASE_OVERLOAD_LIMIT,
                CURRENT_TICK,
                READY_BURST_COOLDOWN,
                READY_RITUAL_LOCK
            );
        final NonRollbackResourceMutationPort resourcePort =
            NonRollbackResourceMutationPort.from(snapshot);
        final PhaseStateRecordingPort phaseStatePort = PhaseStateRecordingPort.from(phaseGuard);

        final BenmingSwordResourceTransaction.Result result =
            BenmingSwordResourceTransaction.tryConsume(
                snapshot,
                phaseRequest(phaseGuard),
                ACTUAL_ZHENYUAN_COST,
                base -> base * COST_SCALE_FACTOR,
                resourcePort,
                phaseStatePort
            );

        assertFalse(result.success());
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.ATOMIC_MUTATION_UNSUPPORTED,
            result.failureReason()
        );
        assertEquals(BenmingSwordResourceTransaction.SwordStateSource.LIVE_SWORD, result.swordStateSource());
        assertResourceState(resourcePort, TEST_MAGIC_100_0, TEST_MAGIC_100_0, TEST_MAGIC_100_0);
        assertPhaseState(phaseStatePort, PHASE_OVERLOAD_BEFORE, READY_BURST_COOLDOWN, READY_RITUAL_LOCK);
        assertTrue(resourcePort.events().isEmpty());
        assertTrue(phaseStatePort.events().isEmpty());
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
        assertTrue(highReward <= baseReward * TEST_MAGIC_1_2D);
        assertTrue(highModifier.finalMultiplier() <= TEST_MAGIC_1_2D);
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
        assertEquals(TEST_MAGIC_1_2, modifier.finalMultiplier(), DOUBLE_DELTA);
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
        assertEquals(TEST_MAGIC_15_0, extremeResult.niantouCost(), DOUBLE_DELTA);
        assertEquals(TEST_MAGIC_15_0, baselineResult.niantouCost(), DOUBLE_DELTA);
        assertEquals(extremeResult.niantouCost(), baselineResult.niantouCost(), DOUBLE_DELTA);
        assertEquals(extremeResult.hunpoCost(), baselineResult.hunpoCost(), DOUBLE_DELTA);
        assertEquals(0, extremePort.operations().size());
        assertEquals(0, baselinePort.operations().size());
    }

    private static BenmingSwordResourceTransaction.Result runPhaseAwareTransaction(
        final CultivationSnapshot snapshot,
        final BenmingSwordResourceTransaction.Request request,
        final AtomicRecordingMutationPort port
    ) {
        return BenmingSwordResourceTransaction.tryConsume(
            snapshot,
            request,
            ACTUAL_ZHENYUAN_COST,
            base -> base * COST_SCALE_FACTOR,
            port
        );
    }

    private static BenmingSwordResourceTransaction.Request phaseRequest(
        final BenmingSwordResourceTransaction.PhaseGuard phaseGuard
    ) {
        return new BenmingSwordResourceTransaction.Request(
            BASE_ZHENYUAN_COST,
            BASE_NIANTOU_COST,
            BASE_HUNPO_COST,
            phaseGuard,
            new BenmingSwordResourceTransaction.PhaseMutation(
                PHASE_OVERLOAD_DELTA,
                true,
                NEXT_BURST_COOLDOWN,
                true,
                NEXT_RITUAL_LOCK
            )
        );
    }

    private static void assertResourceState(
        final AtomicRecordingMutationPort port,
        final double zhenyuan,
        final double niantou,
        final double hunpo
    ) {
        assertEquals(zhenyuan, port.zhenyuan(), DOUBLE_DELTA);
        assertEquals(niantou, port.niantou(), DOUBLE_DELTA);
        assertEquals(hunpo, port.hunpo(), DOUBLE_DELTA);
    }

    private static void assertResourceState(
        final NonRollbackResourceMutationPort port,
        final double zhenyuan,
        final double niantou,
        final double hunpo
    ) {
        assertEquals(zhenyuan, port.zhenyuan(), DOUBLE_DELTA);
        assertEquals(niantou, port.niantou(), DOUBLE_DELTA);
        assertEquals(hunpo, port.hunpo(), DOUBLE_DELTA);
    }

    private static void assertPhaseState(
        final AtomicRecordingMutationPort port,
        final double overload,
        final long burstCooldownUntilTick,
        final long ritualLockUntilTick
    ) {
        assertEquals(overload, port.overload(), DOUBLE_DELTA);
        assertEquals(burstCooldownUntilTick, port.burstCooldownUntilTick());
        assertEquals(ritualLockUntilTick, port.ritualLockUntilTick());
    }

    private static void assertPhaseState(
        final PhaseStateRecordingPort port,
        final double overload,
        final long burstCooldownUntilTick,
        final long ritualLockUntilTick
    ) {
        assertEquals(overload, port.overload(), DOUBLE_DELTA);
        assertEquals(burstCooldownUntilTick, port.burstCooldownUntilTick());
        assertEquals(ritualLockUntilTick, port.ritualLockUntilTick());
    }

    private static void assertPhaseOutcome(final ExpectedPhaseOutcome expected) {
        final BenmingSwordResourceTransaction.PhaseOutcome phaseOutcome = expected.phaseOutcome();
        assertEquals(expected.swordStateSource(), phaseOutcome.swordStateSource());
        assertEquals(expected.overloadBefore(), phaseOutcome.overloadBefore(), DOUBLE_DELTA);
        assertEquals(expected.overloadAfter(), phaseOutcome.overloadAfter(), DOUBLE_DELTA);
        assertEquals(expected.burstCooldownBefore(), phaseOutcome.burstCooldownUntilTickBefore());
        assertEquals(expected.burstCooldownAfter(), phaseOutcome.burstCooldownUntilTickAfter());
        assertEquals(expected.ritualLockBefore(), phaseOutcome.ritualLockUntilTickBefore());
        assertEquals(expected.ritualLockAfter(), phaseOutcome.ritualLockUntilTickAfter());
        assertEquals(expected.overloadWritten(), phaseOutcome.overloadWritten());
        assertEquals(expected.burstCooldownWritten(), phaseOutcome.burstCooldownWritten());
        assertEquals(expected.ritualLockWritten(), phaseOutcome.ritualLockWritten());
    }

    private record ExpectedPhaseOutcome(
        BenmingSwordResourceTransaction.PhaseOutcome phaseOutcome,
        BenmingSwordResourceTransaction.SwordStateSource swordStateSource,
        double overloadBefore,
        double overloadAfter,
        long burstCooldownBefore,
        long burstCooldownAfter,
        long ritualLockBefore,
        long ritualLockAfter,
        boolean overloadWritten,
        boolean burstCooldownWritten,
        boolean ritualLockWritten
    ) {}

    private record Operation(String resource, double amount) {}

    private static final class AtomicRecordingMutationPort
        implements BenmingSwordResourceTransaction.TransactionMutationPort {

        private double zhenyuan;
        private double niantou;
        private double hunpo;
        private double overload;
        private long burstCooldownUntilTick;
        private long ritualLockUntilTick;
        private FailurePoint failurePoint = FailurePoint.NONE;
        private boolean failureConsumed;
        private final List<String> events = new ArrayList<>();

        private AtomicRecordingMutationPort(
            final double zhenyuan,
            final double niantou,
            final double hunpo,
            final double overload,
            final long burstCooldownUntilTick,
            final long ritualLockUntilTick
        ) {
            this.zhenyuan = zhenyuan;
            this.niantou = niantou;
            this.hunpo = hunpo;
            this.overload = overload;
            this.burstCooldownUntilTick = burstCooldownUntilTick;
            this.ritualLockUntilTick = ritualLockUntilTick;
        }

        private static AtomicRecordingMutationPort from(
            final CultivationSnapshot snapshot,
            final BenmingSwordResourceTransaction.PhaseGuard phaseGuard
        ) {
            return new AtomicRecordingMutationPort(
                snapshot.zhenyuan(),
                snapshot.niantou(),
                snapshot.hunpo(),
                phaseGuard.currentOverload(),
                phaseGuard.burstCooldownUntilTick(),
                phaseGuard.ritualLockUntilTick()
            );
        }

        private void failOn(final FailurePoint failurePoint) {
            this.failurePoint = failurePoint == null ? FailurePoint.NONE : failurePoint;
            this.failureConsumed = false;
        }

        @Override
        public void spendZhenyuan(final double amount) {
            zhenyuan -= amount;
            events.add("spendZhenyuan:" + amount);
        }

        @Override
        public void spendNiantou(final double amount) {
            niantou -= amount;
            events.add("spendNiantou:" + amount);
        }

        @Override
        public void spendHunpo(final double amount) {
            hunpo -= amount;
            events.add("spendHunpo:" + amount);
        }

        @Override
        public boolean supportsResourceRollback() {
            return true;
        }

        @Override
        public void refundZhenyuan(final double amount) {
            zhenyuan += amount;
            events.add("refundZhenyuan:" + amount);
        }

        @Override
        public void refundNiantou(final double amount) {
            niantou += amount;
            events.add("refundNiantou:" + amount);
        }

        @Override
        public void refundHunpo(final double amount) {
            hunpo += amount;
            events.add("refundHunpo:" + amount);
        }

        @Override
        public void setOverload(final double overload) {
            this.overload = overload;
            events.add("setOverload:" + overload);
            failIfNeeded(FailurePoint.SET_OVERLOAD_AFTER_WRITE);
        }

        @Override
        public void setBurstCooldownUntilTick(final long burstCooldownUntilTick) {
            this.burstCooldownUntilTick = burstCooldownUntilTick;
            events.add("setBurstCooldown:" + burstCooldownUntilTick);
            failIfNeeded(FailurePoint.SET_BURST_COOLDOWN_AFTER_WRITE);
        }

        @Override
        public void setRitualLockUntilTick(final long ritualLockUntilTick) {
            this.ritualLockUntilTick = ritualLockUntilTick;
            events.add("setRitualLock:" + ritualLockUntilTick);
            failIfNeeded(FailurePoint.SET_RITUAL_LOCK_AFTER_WRITE);
        }

        private void failIfNeeded(final FailurePoint currentPoint) {
            if (failureConsumed || failurePoint != currentPoint) {
                return;
            }
            failureConsumed = true;
            throw new IllegalStateException(currentPoint.name());
        }

        private double zhenyuan() {
            return zhenyuan;
        }

        private double niantou() {
            return niantou;
        }

        private double hunpo() {
            return hunpo;
        }

        private double overload() {
            return overload;
        }

        private long burstCooldownUntilTick() {
            return burstCooldownUntilTick;
        }

        private long ritualLockUntilTick() {
            return ritualLockUntilTick;
        }

        private List<String> events() {
            return events;
        }

        private enum FailurePoint {
            NONE,
            SET_OVERLOAD_AFTER_WRITE,
            SET_BURST_COOLDOWN_AFTER_WRITE,
            SET_RITUAL_LOCK_AFTER_WRITE
        }
    }

    private static final class NonRollbackResourceMutationPort
        implements BenmingSwordResourceTransaction.ResourceMutationPort {

        private double zhenyuan;
        private double niantou;
        private double hunpo;
        private final List<String> events = new ArrayList<>();

        private NonRollbackResourceMutationPort(
            final double zhenyuan,
            final double niantou,
            final double hunpo
        ) {
            this.zhenyuan = zhenyuan;
            this.niantou = niantou;
            this.hunpo = hunpo;
        }

        private static NonRollbackResourceMutationPort from(final CultivationSnapshot snapshot) {
            return new NonRollbackResourceMutationPort(
                snapshot.zhenyuan(),
                snapshot.niantou(),
                snapshot.hunpo()
            );
        }

        @Override
        public void spendZhenyuan(final double amount) {
            zhenyuan -= amount;
            events.add("spendZhenyuan:" + amount);
        }

        @Override
        public void spendNiantou(final double amount) {
            niantou -= amount;
            events.add("spendNiantou:" + amount);
        }

        @Override
        public void spendHunpo(final double amount) {
            hunpo -= amount;
            events.add("spendHunpo:" + amount);
        }

        private double zhenyuan() {
            return zhenyuan;
        }

        private double niantou() {
            return niantou;
        }

        private double hunpo() {
            return hunpo;
        }

        private List<String> events() {
            return events;
        }
    }

    private static final class PhaseStateRecordingPort
        implements BenmingSwordResourceTransaction.PhaseStateMutationPort {

        private double overload;
        private long burstCooldownUntilTick;
        private long ritualLockUntilTick;
        private final List<String> events = new ArrayList<>();

        private PhaseStateRecordingPort(
            final double overload,
            final long burstCooldownUntilTick,
            final long ritualLockUntilTick
        ) {
            this.overload = overload;
            this.burstCooldownUntilTick = burstCooldownUntilTick;
            this.ritualLockUntilTick = ritualLockUntilTick;
        }

        private static PhaseStateRecordingPort from(
            final BenmingSwordResourceTransaction.PhaseGuard phaseGuard
        ) {
            return new PhaseStateRecordingPort(
                phaseGuard.currentOverload(),
                phaseGuard.burstCooldownUntilTick(),
                phaseGuard.ritualLockUntilTick()
            );
        }

        @Override
        public void setOverload(final double overload) {
            this.overload = overload;
            events.add("setOverload:" + overload);
        }

        @Override
        public void setBurstCooldownUntilTick(final long burstCooldownUntilTick) {
            this.burstCooldownUntilTick = burstCooldownUntilTick;
            events.add("setBurstCooldown:" + burstCooldownUntilTick);
        }

        @Override
        public void setRitualLockUntilTick(final long ritualLockUntilTick) {
            this.ritualLockUntilTick = ritualLockUntilTick;
            events.add("setRitualLock:" + ritualLockUntilTick);
        }

        private double overload() {
            return overload;
        }

        private long burstCooldownUntilTick() {
            return burstCooldownUntilTick;
        }

        private long ritualLockUntilTick() {
            return ritualLockUntilTick;
        }

        private List<String> events() {
            return events;
        }
    }

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
