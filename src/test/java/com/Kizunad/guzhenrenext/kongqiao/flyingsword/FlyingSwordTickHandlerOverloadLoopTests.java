package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FlyingSwordTickHandlerOverloadLoopTests {

    private static final double DOUBLE_DELTA = 1.0E-9;
    private static final double OVERLOAD_BOUNDARY_99 = 99.0D;
    private static final double OVERLOAD_BOUNDARY_100 = 100.0D;
    private static final double OVERLOAD_BOUNDARY_101 = 101.0D;
    private static final double OVERLOAD_BOUNDARY_OVERFLOW = 101.5D;
    private static final double OVERLOAD_RECOVERY_SAFE = 28.0D;
    private static final double OVERLOAD_GROWTH_BASE = 20.0D;
    private static final double OVERLOAD_GROWTH_EXPECTED = 23.5D;
    private static final double OVERLOAD_DECAY_BASE = 50.0D;
    private static final double OVERLOAD_DECAY_EXPECTED = 49.65D;
    private static final double ZERO_GROWTH = 0.0D;
    private static final double GROWTH_PER_COMBAT_TICK = 3.5D;
    private static final double IRRELEVANT_GROWTH_IN_DECAY_CASE = 3.0D;
    private static final long ZERO_TICK = 0L;
    private static final long LAST_TICK_199 = 199L;
    private static final long CURRENT_TICK_200 = 200L;
    private static final long LAST_TICK_299 = 299L;
    private static final long CURRENT_TICK_300 = 300L;
    private static final long LAST_TICK_399 = 399L;
    private static final long CURRENT_TICK_400 = 400L;
    private static final long LAST_TICK_499 = 499L;
    private static final long CURRENT_TICK_500 = 500L;
    private static final long LAST_TICK_599 = 599L;
    private static final long CURRENT_TICK_600 = 600L;
    private static final long LAST_TICK_699 = 699L;
    private static final long CURRENT_TICK_700 = 700L;
    private static final long LAST_TICK_799 = 799L;
    private static final long CURRENT_TICK_800 = 800L;
    private static final long EXTERNAL_COOLDOWN_GUARD = 900L;
    private static final long EXISTING_BURST_COOLDOWN = 1000L;
    private static final long NEXT_TICK_OFFSET = 1L;
    private static final String BONDED_SWORD_ID = "benming-sword-id";
    private static final String OTHER_SWORD_ID = "ordinary-sword-id";
    private static final String BLANK_SWORD_ID = "";

    @Test
    void overloadBoundary99DoesNotTriggerBacklash() {
        final OverloadBacklashEngine.OverloadResolution resolution =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_BOUNDARY_99,
                    ZERO_TICK,
                    ZERO_TICK,
                    ZERO_TICK,
                    LAST_TICK_199,
                    CURRENT_TICK_200,
                    true,
                    ZERO_GROWTH
                )
            );

        assertFalse(resolution.backlashTriggered());
        assertEquals(OVERLOAD_BOUNDARY_99, resolution.overloadAfter(), DOUBLE_DELTA);
        assertEquals(ZERO_TICK, resolution.overloadBacklashUntilTickAfter());
        assertEquals(ZERO_TICK, resolution.overloadRecoveryUntilTickAfter());
    }

    @Test
    void overloadBoundary100TriggersBacklash() {
        final OverloadBacklashEngine.OverloadResolution resolution =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_BOUNDARY_100,
                    ZERO_TICK,
                    ZERO_TICK,
                    ZERO_TICK,
                    LAST_TICK_299,
                    CURRENT_TICK_300,
                    true,
                    ZERO_GROWTH
                )
            );

        assertTrue(resolution.backlashTriggered());
        assertTrue(resolution.overloadBacklashUntilTickAfter() > CURRENT_TICK_300);
        assertTrue(
            resolution.overloadRecoveryUntilTickAfter()
                > resolution.overloadBacklashUntilTickAfter()
        );
    }

    @Test
    void overloadBoundary101IsClampedAndTriggersBacklash() {
        final OverloadBacklashEngine.OverloadResolution resolution =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_BOUNDARY_OVERFLOW,
                    ZERO_TICK,
                    ZERO_TICK,
                    ZERO_TICK,
                    LAST_TICK_399,
                    CURRENT_TICK_400,
                    true,
                    ZERO_GROWTH
                )
            );

        assertTrue(resolution.backlashTriggered());
        assertEquals(OVERLOAD_BOUNDARY_101, resolution.overloadAfter(), DOUBLE_DELTA);
    }

    @Test
    void recoveryClearsAfterBacklashWindowEndsAndOverloadReturnsSafeRange() {
        final OverloadBacklashEngine.OverloadResolution triggered =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_BOUNDARY_100,
                    ZERO_TICK,
                    ZERO_TICK,
                    ZERO_TICK,
                    LAST_TICK_499,
                    CURRENT_TICK_500,
                    true,
                    ZERO_GROWTH
                )
            );
        assertTrue(triggered.backlashTriggered());

        final long backlashUntilTick = triggered.overloadBacklashUntilTickAfter();
        final long recoveryUntilTick = triggered.overloadRecoveryUntilTickAfter();
        final OverloadBacklashEngine.OverloadResolution recovered =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_RECOVERY_SAFE,
                    triggered.burstCooldownUntilTickAfter(),
                    backlashUntilTick,
                    recoveryUntilTick,
                    backlashUntilTick,
                    backlashUntilTick + NEXT_TICK_OFFSET,
                    false,
                    ZERO_GROWTH
                )
            );

        assertFalse(recovered.backlashTriggered());
        assertEquals(ZERO_TICK, recovered.overloadRecoveryUntilTickAfter());
        assertEquals(
            backlashUntilTick + NEXT_TICK_OFFSET,
            recovered.lastOverloadTickAfter()
        );
        assertEquals(
            triggered.burstCooldownUntilTickAfter(),
            recovered.burstCooldownUntilTickAfter()
        );
    }

    @Test
    void triggeringBacklashDoesNotShortenExistingBurstCooldown() {
        final OverloadBacklashEngine.OverloadResolution resolution =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_BOUNDARY_100,
                    EXISTING_BURST_COOLDOWN,
                    ZERO_TICK,
                    ZERO_TICK,
                    LAST_TICK_599,
                    CURRENT_TICK_600,
                    true,
                    ZERO_GROWTH
                )
            );

        assertTrue(resolution.backlashTriggered());
        assertEquals(
            EXISTING_BURST_COOLDOWN,
            resolution.burstCooldownUntilTickAfter()
        );
    }

    @Test
    void combatActivityGrowsOverloadByConfiguredRate() {
        final OverloadBacklashEngine.OverloadResolution resolution =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_GROWTH_BASE,
                    ZERO_TICK,
                    ZERO_TICK,
                    ZERO_TICK,
                    LAST_TICK_699,
                    CURRENT_TICK_700,
                    true,
                    GROWTH_PER_COMBAT_TICK
                )
            );

        assertEquals(OVERLOAD_GROWTH_EXPECTED, resolution.overloadAfter(), DOUBLE_DELTA);
        assertFalse(resolution.backlashTriggered());
    }

    @Test
    void noCombatDecaysOverloadAndPreservesExternalCooldownGuard() {
        final OverloadBacklashEngine.OverloadResolution resolution =
            OverloadBacklashEngine.resolveOverloadForTick(
                new OverloadBacklashEngine.OverloadTickInput(
                    OVERLOAD_DECAY_BASE,
                    EXTERNAL_COOLDOWN_GUARD,
                    ZERO_TICK,
                    ZERO_TICK,
                    LAST_TICK_799,
                    CURRENT_TICK_800,
                    false,
                    IRRELEVANT_GROWTH_IN_DECAY_CASE
                )
            );

        assertEquals(OVERLOAD_DECAY_EXPECTED, resolution.overloadAfter(), DOUBLE_DELTA);
        assertEquals(
            EXTERNAL_COOLDOWN_GUARD,
            resolution.burstCooldownUntilTickAfter()
        );
        assertFalse(resolution.backlashTriggered());
    }

    @Test
    void thresholdPredicateCoversBoundaryValues() {
        assertFalse(
            OverloadBacklashEngine.shouldTriggerOverloadBacklash(
                OVERLOAD_BOUNDARY_99
            )
        );
        assertTrue(
            OverloadBacklashEngine.shouldTriggerOverloadBacklash(
                OVERLOAD_BOUNDARY_100
            )
        );
        assertTrue(
            OverloadBacklashEngine.shouldTriggerOverloadBacklash(
                OVERLOAD_BOUNDARY_101
            )
        );
    }

    @Test
    void benmingSwordCanMarkCombatActivity() {
        assertTrue(
            OverloadBacklashEngine.shouldMarkCombatActivityForSword(
                BONDED_SWORD_ID,
                BONDED_SWORD_ID,
                false
            )
        );
    }

    @Test
    void nonBenmingSwordMustNotMarkCombatActivity() {
        assertFalse(
            OverloadBacklashEngine.shouldMarkCombatActivityForSword(
                BONDED_SWORD_ID,
                OTHER_SWORD_ID,
                false
            )
        );
    }

    @Test
    void dirtyBondCacheMustNotMarkCombatActivity() {
        assertFalse(
            OverloadBacklashEngine.shouldMarkCombatActivityForSword(
                BONDED_SWORD_ID,
                BONDED_SWORD_ID,
                true
            )
        );
    }

    @Test
    void blankBondedSwordIdMustNotMarkCombatActivity() {
        assertFalse(
            OverloadBacklashEngine.shouldMarkCombatActivityForSword(
                BLANK_SWORD_ID,
                BONDED_SWORD_ID,
                false
            )
        );
    }

    @Test
    void blankAttackingSwordIdMustNotMarkCombatActivity() {
        assertFalse(
            OverloadBacklashEngine.shouldMarkCombatActivityForSword(
                BONDED_SWORD_ID,
                BLANK_SWORD_ID,
                false
            )
        );
    }
}
