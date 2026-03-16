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
