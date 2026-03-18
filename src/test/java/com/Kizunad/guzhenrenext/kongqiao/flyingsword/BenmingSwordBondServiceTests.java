package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BenmingSwordBondServiceTests {

    private static final String OWNER_UUID = "player-owner-001";
    private static final long RESOLVED_TICK = 1200L;
    private static final double DOUBLE_DELTA = 1.0E-9;
    private static final double RITUAL_ZHENYUAN_COST = 10.0D;
    private static final double RITUAL_NIANTOU_COST = 6.0D;
    private static final double RITUAL_HUNPO_COST = 4.0D;

    @Test
    void bindSuccessWritesCanonicalBondAndCache() {
        final TestSword target = new TestSword("stable-sword-a", "", 0.35D);
        final TestCache cache = new TestCache();

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.bind(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                RESOLVED_TICK
            );

        assertTrue(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.BIND, result.branch());
        assertEquals(BenmingSwordBondService.FailureReason.NONE, result.failureReason());
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals(OWNER_UUID, target.getBondOwnerUuid());
        assertEquals(0.0D, target.getBondResonance(), DOUBLE_DELTA);
        assertEquals("stable-sword-a", cache.getBondedSwordId());
        assertFalse(cache.isBondCacheDirty());
        assertEquals(RESOLVED_TICK, cache.lastResolvedTick());
    }

    @Test
    void duplicateBindIsRejectedAndOriginalBondRemains() {
        final TestSword alreadyBound = new TestSword("stable-sword-a", OWNER_UUID, 0.9D);
        final TestSword target = new TestSword("stable-sword-b", "", 0.1D);
        final TestCache cache = new TestCache();

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.bind(
                OWNER_UUID,
                target,
                List.of(alreadyBound, target),
                cache,
                RESOLVED_TICK
            );

        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.BIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.PLAYER_ALREADY_HAS_BONDED_SWORD,
            result.failureReason()
        );
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals("", target.getBondOwnerUuid());
        assertEquals(0.1D, target.getBondResonance(), DOUBLE_DELTA);
    }

    @Test
    void bindRejectsWhenOwnerAlreadyHasMultipleBondedSwords() {
        final TestSword alreadyBoundOne = new TestSword("stable-sword-a", OWNER_UUID, 0.9D);
        final TestSword alreadyBoundTwo = new TestSword("stable-sword-b", OWNER_UUID, 0.7D);
        final TestSword target = new TestSword("stable-sword-c", "", 0.2D);
        final TestCache cache = new TestCache();
        cache.updateBondCache("stable-cache-before", 101L);

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.bind(
                OWNER_UUID,
                target,
                List.of(alreadyBoundOne, alreadyBoundTwo, target),
                cache,
                RESOLVED_TICK
            );

        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.BIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.MULTIPLE_BONDED_SWORDS,
            result.failureReason()
        );
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals("", target.getBondOwnerUuid());
        assertEquals(0.2D, target.getBondResonance(), DOUBLE_DELTA);
        assertEquals("stable-cache-before", cache.getBondedSwordId());
        assertFalse(cache.isBondCacheDirty());
        assertEquals(101L, cache.lastResolvedTick());
    }

    @Test
    void ritualBindSuccessWritesCanonicalBondAndCache() {
        final TestSword target = new TestSword("stable-sword-a", "", 0.35D);
        final TestCache cache = new TestCache();
        final TestRitualRequestState requestState = new TestRitualRequestState();
        final BenmingSwordBondService.RitualRequestContext requestContext =
            ritualRequestContext(requestState);
        final RecordingTransactionPort mutationPort = new RecordingTransactionPort();

        final BenmingSwordBondService.Result precheck =
            BenmingSwordBondService.precheckRitualBind(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                requestContext,
                RESOLVED_TICK
            );

        assertTrue(precheck.success());
        assertEquals(BenmingSwordBondService.ResultBranch.RITUAL_PRECHECK, precheck.branch());
        assertEquals("stable-sword-a", requestState.getLockedSwordId());
        assertTrue(requestState.isExecutionPending());

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.ritualBindWithTransaction(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                ritualBindContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    ritualRequest(),
                    mutationPort,
                    requestContext
                ),
                RESOLVED_TICK
            );

        assertTrue(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.RITUAL_BIND, result.branch());
        assertEquals(BenmingSwordBondService.FailureReason.NONE, result.failureReason());
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals(OWNER_UUID, target.getBondOwnerUuid());
        assertEquals(0.0D, target.getBondResonance(), DOUBLE_DELTA);
        assertEquals("stable-sword-a", cache.getBondedSwordId());
        assertFalse(cache.isBondCacheDirty());
        assertEquals(RESOLVED_TICK, cache.lastResolvedTick());
        assertFalse(requestState.isExecutionPending());
        assertEquals(3, mutationPort.operations().size());
        assertEquals(
            RITUAL_ZHENYUAN_COST,
            mutationPort.operations().get(0).amount(),
            DOUBLE_DELTA
        );
        assertEquals(
            RITUAL_NIANTOU_COST,
            mutationPort.operations().get(1).amount(),
            DOUBLE_DELTA
        );
        assertEquals(
            RITUAL_HUNPO_COST,
            mutationPort.operations().get(2).amount(),
            DOUBLE_DELTA
        );
    }

    @Test
    void ritualBindFailureMapsResourceShortage() {
        final TestSword target = new TestSword("stable-sword-a", "", 0.35D);
        final TestCache cache = new TestCache();
        final TestRitualRequestState requestState = new TestRitualRequestState();
        final BenmingSwordBondService.RitualRequestContext requestContext =
            ritualRequestContext(requestState);
        final RecordingTransactionPort mutationPort = new RecordingTransactionPort();

        final BenmingSwordBondService.Result precheck =
            BenmingSwordBondService.precheckRitualBind(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                requestContext,
                RESOLVED_TICK
            );
        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.ritualBindWithTransaction(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                ritualBindContext(
                    CultivationSnapshot.of(120.0D, 5.0D, 120.0D, 0.0D, 0),
                    ritualRequest(),
                    mutationPort,
                    requestContext
                ),
                RESOLVED_TICK
            );

        assertTrue(precheck.success());
        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.RITUAL_BIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.RITUAL_RESOURCES_INSUFFICIENT,
            result.failureReason()
        );
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_NIANTOU,
            result.resourceFailureReason()
        );
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals("", target.getBondOwnerUuid());
        assertEquals("", cache.getBondedSwordId());
        assertEquals(0, mutationPort.operations().size());
        assertFalse(requestState.isExecutionPending());
    }

    @Test
    void ritualBindFailureMapsIllegalState() {
        final TestSword target = new TestSword("stable-sword-a", "", 0.35D);
        final TestCache cache = new TestCache();
        final TestRitualRequestState requestState = new TestRitualRequestState();
        final BenmingSwordBondService.RitualRequestContext requestContext =
            ritualRequestContext(requestState);
        final RecordingTransactionPort mutationPort = new RecordingTransactionPort();
        final BenmingSwordResourceTransaction.Request request =
            new BenmingSwordResourceTransaction.Request(
                RITUAL_ZHENYUAN_COST,
                RITUAL_NIANTOU_COST,
                RITUAL_HUNPO_COST,
                BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                    false,
                    false,
                    false,
                    0.0D,
                    false,
                    0.0D,
                    RESOLVED_TICK,
                    0L,
                    0L
                ),
                BenmingSwordResourceTransaction.PhaseMutation.none()
            );

        final BenmingSwordBondService.Result precheck =
            BenmingSwordBondService.precheckRitualBind(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                requestContext,
                RESOLVED_TICK
            );
        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.ritualBindWithTransaction(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                ritualBindContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    request,
                    mutationPort,
                    requestContext
                ),
                RESOLVED_TICK
            );

        assertTrue(precheck.success());
        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.RITUAL_BIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.RITUAL_STATE_ILLEGAL,
            result.failureReason()
        );
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.ILLEGAL_SWORD_STATE,
            result.resourceFailureReason()
        );
        assertEquals(0, mutationPort.operations().size());
    }

    @Test
    void ritualBindFailureMapsCooldownState() {
        final TestSword target = new TestSword("stable-sword-a", "", 0.35D);
        final TestCache cache = new TestCache();
        final TestRitualRequestState requestState = new TestRitualRequestState();
        final BenmingSwordBondService.RitualRequestContext requestContext =
            ritualRequestContext(requestState);
        final RecordingTransactionPort mutationPort = new RecordingTransactionPort();
        final BenmingSwordResourceTransaction.Request request =
            new BenmingSwordResourceTransaction.Request(
                RITUAL_ZHENYUAN_COST,
                RITUAL_NIANTOU_COST,
                RITUAL_HUNPO_COST,
                BenmingSwordResourceTransaction.PhaseGuard.liveSword(
                    true,
                    false,
                    true,
                    0.0D,
                    false,
                    0.0D,
                    RESOLVED_TICK,
                    0L,
                    RESOLVED_TICK + 20L
                ),
                BenmingSwordResourceTransaction.PhaseMutation.none()
            );

        final BenmingSwordBondService.Result precheck =
            BenmingSwordBondService.precheckRitualBind(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                requestContext,
                RESOLVED_TICK
            );
        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.ritualBindWithTransaction(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                ritualBindContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    request,
                    mutationPort,
                    requestContext
                ),
                RESOLVED_TICK
            );

        assertTrue(precheck.success());
        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.RITUAL_BIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.RITUAL_COOLDOWN_ACTIVE,
            result.failureReason()
        );
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.RITUAL_LOCK_ACTIVE,
            result.resourceFailureReason()
        );
        assertEquals(0, mutationPort.operations().size());
    }

    @Test
    void ritualBindDuplicateExecuteDoesNotSpendTwice() {
        final TestSword target = new TestSword("stable-sword-a", "", 0.35D);
        final TestCache cache = new TestCache();
        final TestRitualRequestState requestState = new TestRitualRequestState();
        final BenmingSwordBondService.RitualRequestContext requestContext =
            ritualRequestContext(requestState);
        final RecordingTransactionPort mutationPort = new RecordingTransactionPort();

        final BenmingSwordBondService.Result precheck =
            BenmingSwordBondService.precheckRitualBind(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                requestContext,
                RESOLVED_TICK
            );
        final BenmingSwordBondService.Result first =
            BenmingSwordBondService.ritualBindWithTransaction(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                ritualBindContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    ritualRequest(),
                    mutationPort,
                    requestContext
                ),
                RESOLVED_TICK
            );
        final BenmingSwordBondService.Result second =
            BenmingSwordBondService.ritualBindWithTransaction(
                OWNER_UUID,
                target,
                List.of(target),
                cache,
                ritualBindContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    ritualRequest(),
                    mutationPort,
                    requestContext
                ),
                RESOLVED_TICK + 1L
            );

        assertTrue(precheck.success());
        assertTrue(first.success());
        assertFalse(second.success());
        assertEquals(
            BenmingSwordBondService.FailureReason.RITUAL_DUPLICATE_REQUEST,
            second.failureReason()
        );
        assertEquals("stable-sword-a", second.stableSwordId());
        assertEquals(3, mutationPort.operations().size());
        assertEquals(OWNER_UUID, target.getBondOwnerUuid());
        assertEquals("stable-sword-a", cache.getBondedSwordId());
    }

    @Test
    void ritualBindKeepsLockedTargetSwordUuidDuringWindow() {
        final TestSword lockedTarget = new TestSword("stable-sword-a", "", 0.35D);
        final TestSword switchedTarget = new TestSword("stable-sword-b", "", 0.60D);
        final TestCache cache = new TestCache();
        final TestRitualRequestState requestState = new TestRitualRequestState();
        final BenmingSwordBondService.RitualRequestContext requestContext =
            ritualRequestContext(requestState);
        final RecordingTransactionPort mutationPort = new RecordingTransactionPort();

        final BenmingSwordBondService.Result precheck =
            BenmingSwordBondService.precheckRitualBind(
                OWNER_UUID,
                lockedTarget,
                List.of(lockedTarget, switchedTarget),
                cache,
                requestContext,
                RESOLVED_TICK
            );
        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.ritualBindWithTransaction(
                OWNER_UUID,
                switchedTarget,
                List.of(lockedTarget, switchedTarget),
                cache,
                ritualBindContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    ritualRequest(),
                    mutationPort,
                    requestContext
                ),
                RESOLVED_TICK
            );

        assertTrue(precheck.success());
        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.RITUAL_BIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.RITUAL_TARGET_MISMATCH,
            result.failureReason()
        );
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals("", switchedTarget.getBondOwnerUuid());
        assertEquals(0, mutationPort.operations().size());
    }

    @Test
    void activeUnbindConsumesTransactionAndClearsBondState() {
        final TestSword target = new TestSword("stable-sword-a", OWNER_UUID, 0.66D);
        final TestCache cache = new TestCache();
        cache.updateBondCache("stable-sword-a", 100L);
        final RecordingMutationPort mutationPort = new RecordingMutationPort();

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.activeUnbindWithTransaction(
                OWNER_UUID,
                target,
                cache,
                new BenmingSwordBondService.ActiveUnbindTransactionContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    BenmingSwordBondService.defaultActiveUnbindRequest(),
                    8.0D,
                    baseCost -> baseCost,
                    mutationPort
                ),
                RESOLVED_TICK
            );

        assertTrue(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND, result.branch());
        assertEquals(BenmingSwordBondService.FailureReason.NONE, result.failureReason());
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals(
            BenmingSwordBondService.BacklashType.NONE,
            result.backlashEffect().type()
        );
        assertEquals(0.0D, result.backlashEffect().amount(), DOUBLE_DELTA);
        assertEquals("", target.getBondOwnerUuid());
        assertEquals(0.0D, target.getBondResonance(), DOUBLE_DELTA);
        assertEquals("", cache.getBondedSwordId());
        assertTrue(cache.isBondCacheDirty());
        assertEquals(-1L, cache.lastResolvedTick());

        assertEquals(3, mutationPort.operations().size());
        assertEquals("zhenyuan", mutationPort.operations().get(0).resource());
        assertEquals(8.0D, mutationPort.operations().get(0).amount(), DOUBLE_DELTA);
        assertEquals("niantou", mutationPort.operations().get(1).resource());
        assertEquals(30.0D, mutationPort.operations().get(1).amount(), DOUBLE_DELTA);
        assertEquals("hunpo", mutationPort.operations().get(2).resource());
        assertEquals(20.0D, mutationPort.operations().get(2).amount(), DOUBLE_DELTA);
    }

    @Test
    void activeUnbindRejectsNonOwnerBeforeAnyResourceMutation() {
        final TestSword target = new TestSword("stable-sword-a", "another-player", 0.4D);
        final TestCache cache = new TestCache();
        final RecordingMutationPort mutationPort = new RecordingMutationPort();

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.activeUnbindWithTransaction(
                OWNER_UUID,
                target,
                cache,
                new BenmingSwordBondService.ActiveUnbindTransactionContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    BenmingSwordBondService.defaultActiveUnbindRequest(),
                    8.0D,
                    baseCost -> baseCost,
                    mutationPort
                ),
                RESOLVED_TICK
            );

        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.TARGET_NOT_BOUND_TO_PLAYER,
            result.failureReason()
        );
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals(0, mutationPort.operations().size());
    }

    @Test
    void activeUnbindTransactionFailureKeepsBondAndCacheUnchanged() {
        final TestSword target = new TestSword("stable-sword-a", OWNER_UUID, 0.66D);
        final TestCache cache = new TestCache();
        cache.updateBondCache("stable-sword-a", 100L);
        final RecordingMutationPort mutationPort = new RecordingMutationPort();

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.activeUnbindWithTransaction(
                OWNER_UUID,
                target,
                cache,
                new BenmingSwordBondService.ActiveUnbindTransactionContext(
                    CultivationSnapshot.of(120.0D, 5.0D, 120.0D, 0.0D, 0),
                    BenmingSwordBondService.defaultActiveUnbindRequest(),
                    8.0D,
                    baseCost -> baseCost,
                    mutationPort
                ),
                RESOLVED_TICK
            );

        assertFalse(result.success());
        assertEquals(BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND, result.branch());
        assertEquals(
            BenmingSwordBondService.FailureReason.ACTIVE_UNBIND_COST_REJECTED,
            result.failureReason()
        );
        assertEquals(
            BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_NIANTOU,
            result.resourceFailureReason()
        );
        assertEquals("stable-sword-a", result.stableSwordId());
        assertEquals(OWNER_UUID, target.getBondOwnerUuid());
        assertEquals(0.66D, target.getBondResonance(), DOUBLE_DELTA);
        assertEquals("stable-sword-a", cache.getBondedSwordId());
        assertFalse(cache.isBondCacheDirty());
        assertEquals(100L, cache.lastResolvedTick());
        assertEquals(0, mutationPort.operations().size());
    }

    @Test
    void forcedAndIllegalDetachTriggerLightBacklash() {
        final TestCache cache = new TestCache();
        final RecordingMutationPort forcedMutationPort = new RecordingMutationPort();
        final RecordingMutationPort illegalMutationPort = new RecordingMutationPort();
        final TestBacklashCooldownPort forcedCooldownPort = new TestBacklashCooldownPort();
        final TestBacklashCooldownPort illegalCooldownPort = new TestBacklashCooldownPort();
        final String forcedCooldownKey =
            BenmingSwordBondService.defaultLightBacklashCooldownKey(OWNER_UUID);
        final String illegalCooldownKey =
            BenmingSwordBondService.defaultLightBacklashCooldownKey(
                OWNER_UUID + "-illegal"
            );

        final TestSword forcedSword = new TestSword("stable-sword-a", OWNER_UUID, 0.3D);
        final BenmingSwordBondService.Result forced =
            BenmingSwordBondService.forcedUnbind(
                OWNER_UUID,
                forcedSword,
                cache,
                RESOLVED_TICK,
                new BenmingSwordBondService.BacklashContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    forcedMutationPort,
                    forcedCooldownPort,
                    forcedCooldownKey,
                    BenmingSwordBondService.defaultLightBacklashCooldownTicks()
                )
            );

        final TestSword illegalSword = new TestSword("stable-sword-b", OWNER_UUID, 0.7D);
        final BenmingSwordBondService.Result illegal =
            BenmingSwordBondService.illegalDetach(
                OWNER_UUID,
                illegalSword,
                cache,
                RESOLVED_TICK,
                new BenmingSwordBondService.BacklashContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    illegalMutationPort,
                    illegalCooldownPort,
                    illegalCooldownKey,
                    BenmingSwordBondService.defaultLightBacklashCooldownTicks()
                )
            );

        final TestSword activeSword = new TestSword("stable-sword-c", OWNER_UUID, 0.2D);
        final BenmingSwordBondService.Result active =
            BenmingSwordBondService.activeUnbind(
                OWNER_UUID,
                activeSword,
                cache,
                BenmingSwordResourceTransaction.Result.success(1.0D, 1.0D, 1.0D),
                RESOLVED_TICK
            );

        assertTrue(forced.success());
        assertTrue(illegal.success());
        assertTrue(active.success());
        assertEquals(BenmingSwordBondService.ResultBranch.FORCED_UNBIND, forced.branch());
        assertEquals(BenmingSwordBondService.ResultBranch.ILLEGAL_DETACH, illegal.branch());
        assertEquals(BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND, active.branch());
        assertNotEquals(forced.branch(), illegal.branch());

        assertEquals(
            BenmingSwordBondService.BacklashType.FORCED_UNBIND_LIGHT,
            forced.backlashEffect().type()
        );
        assertEquals(25.0D, forced.backlashEffect().amount(), DOUBLE_DELTA);
        assertEquals(
            BenmingSwordBondService.BacklashType.ILLEGAL_DETACH_LIGHT,
            illegal.backlashEffect().type()
        );
        assertEquals(25.0D, illegal.backlashEffect().amount(), DOUBLE_DELTA);
        assertEquals(
            BenmingSwordBondService.BacklashType.NONE,
            active.backlashEffect().type()
        );
        assertEquals(0.0D, active.backlashEffect().amount(), DOUBLE_DELTA);

        assertEquals(3, forcedMutationPort.operations().size());
        assertEquals(12.0D, forcedMutationPort.operations().get(0).amount(), DOUBLE_DELTA);
        assertEquals(8.0D, forcedMutationPort.operations().get(1).amount(), DOUBLE_DELTA);
        assertEquals(5.0D, forcedMutationPort.operations().get(2).amount(), DOUBLE_DELTA);
        assertEquals(
            BenmingSwordBondService.defaultLightBacklashCooldownTicks(),
            forcedCooldownPort.get(forcedCooldownKey)
        );

        assertEquals(3, illegalMutationPort.operations().size());
        assertEquals(12.0D, illegalMutationPort.operations().get(0).amount(), DOUBLE_DELTA);
        assertEquals(8.0D, illegalMutationPort.operations().get(1).amount(), DOUBLE_DELTA);
        assertEquals(5.0D, illegalMutationPort.operations().get(2).amount(), DOUBLE_DELTA);
        assertEquals(
            BenmingSwordBondService.defaultLightBacklashCooldownTicks(),
            illegalCooldownPort.get(illegalCooldownKey)
        );

        assertEquals("", cache.getBondedSwordId());
        assertTrue(cache.isBondCacheDirty());
        assertEquals(-1L, cache.lastResolvedTick());
    }

    @Test
    void illegalDetachBacklashHasCooldownGuard() {
        final TestCache cache = new TestCache();
        final RecordingMutationPort mutationPort = new RecordingMutationPort();
        final TestBacklashCooldownPort cooldownPort = new TestBacklashCooldownPort();
        final String cooldownKey =
            BenmingSwordBondService.defaultLightBacklashCooldownKey(OWNER_UUID);

        final TestSword illegalOne = new TestSword("stable-sword-a", OWNER_UUID, 0.7D);
        final BenmingSwordBondService.Result first =
            BenmingSwordBondService.illegalDetach(
                OWNER_UUID,
                illegalOne,
                cache,
                RESOLVED_TICK,
                new BenmingSwordBondService.BacklashContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    mutationPort,
                    cooldownPort,
                    cooldownKey,
                    BenmingSwordBondService.defaultLightBacklashCooldownTicks()
                )
            );

        final TestSword illegalTwo = new TestSword("stable-sword-b", OWNER_UUID, 0.8D);
        final BenmingSwordBondService.Result second =
            BenmingSwordBondService.illegalDetach(
                OWNER_UUID,
                illegalTwo,
                cache,
                RESOLVED_TICK,
                new BenmingSwordBondService.BacklashContext(
                    CultivationSnapshot.of(120.0D, 120.0D, 120.0D, 0.0D, 0),
                    mutationPort,
                    cooldownPort,
                    cooldownKey,
                    BenmingSwordBondService.defaultLightBacklashCooldownTicks()
                )
            );

        assertTrue(first.success());
        assertTrue(second.success());
        assertEquals(
            BenmingSwordBondService.BacklashType.ILLEGAL_DETACH_LIGHT,
            first.backlashEffect().type()
        );
        assertEquals(25.0D, first.backlashEffect().amount(), DOUBLE_DELTA);
        assertEquals(
            BenmingSwordBondService.BacklashType.NONE,
            second.backlashEffect().type()
        );
        assertEquals(0.0D, second.backlashEffect().amount(), DOUBLE_DELTA);
        assertEquals(3, mutationPort.operations().size());
        assertEquals(
            BenmingSwordBondService.defaultLightBacklashCooldownTicks(),
            cooldownPort.get(cooldownKey)
        );
    }

    @Test
    void forcedUnbindBacklashUsesSnapshotClamp() {
        final TestCache cache = new TestCache();
        final RecordingMutationPort mutationPort = new RecordingMutationPort();
        final TestBacklashCooldownPort cooldownPort = new TestBacklashCooldownPort();
        final String cooldownKey =
            BenmingSwordBondService.defaultLightBacklashCooldownKey(OWNER_UUID);

        final BenmingSwordBondService.Result result =
            BenmingSwordBondService.forcedUnbind(
                OWNER_UUID,
                new TestSword("stable-sword-a", OWNER_UUID, 0.4D),
                cache,
                RESOLVED_TICK,
                new BenmingSwordBondService.BacklashContext(
                    CultivationSnapshot.of(3.5D, 1.25D, 0.0D, 0.0D, 0),
                    mutationPort,
                    cooldownPort,
                    cooldownKey,
                    BenmingSwordBondService.defaultLightBacklashCooldownTicks()
                )
            );

        assertTrue(result.success());
        assertEquals(
            BenmingSwordBondService.BacklashType.FORCED_UNBIND_LIGHT,
            result.backlashEffect().type()
        );
        assertEquals(4.75D, result.backlashEffect().amount(), DOUBLE_DELTA);
        assertEquals(2, mutationPort.operations().size());
        assertEquals(3.5D, mutationPort.operations().get(0).amount(), DOUBLE_DELTA);
        assertEquals(1.25D, mutationPort.operations().get(1).amount(), DOUBLE_DELTA);
        assertEquals(
            BenmingSwordBondService.defaultLightBacklashCooldownTicks(),
            cooldownPort.get(cooldownKey)
        );
    }

    private static BenmingSwordResourceTransaction.Request ritualRequest() {
        return new BenmingSwordResourceTransaction.Request(
            RITUAL_ZHENYUAN_COST,
            RITUAL_NIANTOU_COST,
            RITUAL_HUNPO_COST
        );
    }

    private static BenmingSwordBondService.RitualRequestContext ritualRequestContext(
        final TestRitualRequestState requestState
    ) {
        return new BenmingSwordBondService.RitualRequestContext(
            requestState,
            BenmingSwordBondService.defaultRitualDuplicateGuardTicks()
        );
    }

    private static BenmingSwordBondService.RitualBindTransactionContext ritualBindContext(
        final CultivationSnapshot snapshot,
        final BenmingSwordResourceTransaction.Request request,
        final RecordingTransactionPort mutationPort,
        final BenmingSwordBondService.RitualRequestContext requestContext
    ) {
        return new BenmingSwordBondService.RitualBindTransactionContext(
            snapshot,
            request,
            RITUAL_ZHENYUAN_COST,
            baseCost -> baseCost,
            mutationPort,
            requestContext
        );
    }

    private static final class TestSword implements BenmingSwordBondService.SwordBondPort {

        private final String stableSwordId;
        private String bondOwnerUuid;
        private double bondResonance;

        private TestSword(
            final String stableSwordId,
            final String bondOwnerUuid,
            final double bondResonance
        ) {
            this.stableSwordId = stableSwordId;
            this.bondOwnerUuid = bondOwnerUuid;
            this.bondResonance = bondResonance;
        }

        @Override
        public String getStableSwordId() {
            return stableSwordId;
        }

        @Override
        public String getBondOwnerUuid() {
            return bondOwnerUuid;
        }

        @Override
        public double getBondResonance() {
            return bondResonance;
        }

        @Override
        public void setBondOwnerUuid(final String ownerUuid) {
            this.bondOwnerUuid = ownerUuid;
        }

        @Override
        public void setBondResonance(final double resonance) {
            this.bondResonance = resonance;
        }
    }

    private static final class TestCache implements BenmingSwordBondService.PlayerBondCachePort {

        private String bondedSwordId = "";
        private boolean dirty = true;
        private long lastResolvedTick = -1L;

        @Override
        public String getBondedSwordId() {
            return bondedSwordId;
        }

        @Override
        public boolean isBondCacheDirty() {
            return dirty;
        }

        @Override
        public void updateBondCache(final String stableSwordId, final long resolvedTick) {
            this.bondedSwordId = stableSwordId == null ? "" : stableSwordId;
            this.lastResolvedTick = resolvedTick;
            this.dirty = false;
        }

        @Override
        public void markBondCacheDirty() {
            this.dirty = true;
        }

        @Override
        public void clearBondCache() {
            this.bondedSwordId = "";
            this.lastResolvedTick = -1L;
            this.dirty = true;
        }

        private long lastResolvedTick() {
            return lastResolvedTick;
        }
    }

    private record MutationOperation(String resource, double amount) {}

    private static final class RecordingMutationPort
        implements BenmingSwordResourceTransaction.ResourceMutationPort {

        private final List<MutationOperation> operations = new ArrayList<>();

        @Override
        public void spendZhenyuan(final double amount) {
            operations.add(new MutationOperation("zhenyuan", amount));
        }

        @Override
        public void spendNiantou(final double amount) {
            operations.add(new MutationOperation("niantou", amount));
        }

        @Override
        public void spendHunpo(final double amount) {
            operations.add(new MutationOperation("hunpo", amount));
        }

        private List<MutationOperation> operations() {
            return operations;
        }
    }

    private static final class RecordingTransactionPort
        implements BenmingSwordResourceTransaction.TransactionMutationPort {

        private final List<MutationOperation> operations = new ArrayList<>();
        private double overload;
        private long burstCooldownUntilTick;
        private long ritualLockUntilTick;

        @Override
        public void spendZhenyuan(final double amount) {
            operations.add(new MutationOperation("zhenyuan", amount));
        }

        @Override
        public void spendNiantou(final double amount) {
            operations.add(new MutationOperation("niantou", amount));
        }

        @Override
        public void spendHunpo(final double amount) {
            operations.add(new MutationOperation("hunpo", amount));
        }

        @Override
        public void setOverload(final double overload) {
            this.overload = overload;
        }

        @Override
        public void setBurstCooldownUntilTick(final long burstCooldownUntilTick) {
            this.burstCooldownUntilTick = burstCooldownUntilTick;
        }

        @Override
        public void setRitualLockUntilTick(final long ritualLockUntilTick) {
            this.ritualLockUntilTick = ritualLockUntilTick;
        }

        private List<MutationOperation> operations() {
            return operations;
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
    }

    private static final class TestRitualRequestState
        implements BenmingSwordBondService.RitualRequestStatePort {

        private String lockedSwordId = "";
        private long lockedUntilTick;
        private boolean executionPending;

        @Override
        public String getLockedSwordId() {
            return lockedSwordId;
        }

        @Override
        public long getLockedUntilTick() {
            return lockedUntilTick;
        }

        @Override
        public boolean isExecutionPending() {
            return executionPending;
        }

        @Override
        public void beginRitualRequest(final String stableSwordId, final long lockedUntilTick) {
            this.lockedSwordId = stableSwordId == null ? "" : stableSwordId;
            this.lockedUntilTick = lockedUntilTick;
            this.executionPending = true;
        }

        @Override
        public void markExecutionConsumed() {
            this.executionPending = false;
        }

        @Override
        public void clearRitualRequest() {
            this.lockedSwordId = "";
            this.lockedUntilTick = 0L;
            this.executionPending = false;
        }
    }

    private static final class TestBacklashCooldownPort
        implements BenmingSwordBondService.BacklashCooldownPort {

        private final Map<String, Integer> cooldowns = new HashMap<>();

        @Override
        public int get(final String key) {
            if (key == null) {
                return 0;
            }
            return Math.max(0, cooldowns.getOrDefault(key, 0));
        }

        @Override
        public void set(final String key, final int ticks) {
            if (key == null) {
                return;
            }
            final int normalizedTicks = Math.max(0, ticks);
            if (normalizedTicks <= 0) {
                cooldowns.remove(key);
                return;
            }
            cooldowns.put(key, normalizedTicks);
        }
    }
}
