package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

final class OverloadBacklashEngine {

    private static final double OVERLOAD_BACKLASH_THRESHOLD = 100.0D;
    private static final double OVERLOAD_UPPER_BOUND = 101.0D;
    private static final double OVERLOAD_DECAY_PER_TICK = 0.35D;
    private static final double OVERLOAD_BACKLASH_DECAY_PER_TICK = 0.80D;
    private static final double OVERLOAD_RECOVERY_DECAY_PER_TICK = 0.60D;
    private static final double OVERLOAD_RECOVERY_RELEASE_THRESHOLD = 30.0D;
    private static final double DEFAULT_COMBAT_OVERLOAD_GROWTH = 2.0D;
    private static final long OVERLOAD_BACKLASH_DURATION_TICKS = 120L;
    private static final long OVERLOAD_RECOVERY_DURATION_TICKS = 120L;

    private OverloadBacklashEngine() {}

    static double defaultCombatOverloadGrowthPerTick() {
        return DEFAULT_COMBAT_OVERLOAD_GROWTH;
    }

    static OverloadResolution resolveOverloadForTick(
        final OverloadTickInput input
    ) {
        if (input == null) {
            return new OverloadResolution(0.0D, 0L, 0L, 0L, 0L, false);
        }
        final long normalizedCurrentTick = normalizeNonNegativeLong(
            input.currentTick()
        );
        final long elapsedTicks = resolveElapsedTicks(
            input.lastOverloadTickBefore(),
            normalizedCurrentTick
        );
        final double normalizedCombatGrowth = normalizeNonNegativeDouble(
            input.combatOverloadGrowthPerTick()
        );
        final long normalizedBacklashUntilTick = normalizeNonNegativeLong(
            input.overloadBacklashUntilTickBefore()
        );
        final long normalizedRecoveryUntilTick = normalizeNonNegativeLong(
            input.overloadRecoveryUntilTickBefore()
        );
        long burstCooldownUntilTickAfter = normalizeNonNegativeLong(
            input.burstCooldownUntilTickBefore()
        );
        long overloadBacklashUntilTickAfter = normalizedBacklashUntilTick;
        long overloadRecoveryUntilTickAfter = normalizedRecoveryUntilTick;
        double overloadAfter = clampOverload(input.overloadBefore());

        final boolean backlashActive =
            normalizedCurrentTick < normalizedBacklashUntilTick;
        final boolean recoveryActive =
            !backlashActive && normalizedCurrentTick < normalizedRecoveryUntilTick;

        if (input.combatActivityThisTick()) {
            overloadAfter = clampOverload(
                overloadAfter + normalizedCombatGrowth * elapsedTicks
            );
        } else {
            final double decayPerTick = backlashActive
                ? OVERLOAD_BACKLASH_DECAY_PER_TICK
                : (recoveryActive
                    ? OVERLOAD_RECOVERY_DECAY_PER_TICK
                    : OVERLOAD_DECAY_PER_TICK);
            overloadAfter = clampOverload(overloadAfter - decayPerTick * elapsedTicks);
        }

        boolean backlashTriggered = false;
        // given 未处于反噬窗口 when 过载达到100 then 触发反噬并写入反噬/恢复窗口。
        if (!backlashActive && shouldTriggerOverloadBacklash(overloadAfter)) {
            backlashTriggered = true;
            overloadBacklashUntilTickAfter =
                normalizedCurrentTick + OVERLOAD_BACKLASH_DURATION_TICKS;
            overloadRecoveryUntilTickAfter =
                overloadBacklashUntilTickAfter + OVERLOAD_RECOVERY_DURATION_TICKS;
            burstCooldownUntilTickAfter = Math.max(
                burstCooldownUntilTickAfter,
                overloadBacklashUntilTickAfter
            );
        }

        // given 反噬已结束 when 过载降到安全线 then 清理恢复窗口，避免永久锁死。
        if (
            normalizedCurrentTick >= overloadBacklashUntilTickAfter
                && overloadRecoveryUntilTickAfter > 0L
                && overloadAfter <= OVERLOAD_RECOVERY_RELEASE_THRESHOLD
        ) {
            overloadRecoveryUntilTickAfter = 0L;
        }

        return new OverloadResolution(
            overloadAfter,
            burstCooldownUntilTickAfter,
            overloadBacklashUntilTickAfter,
            overloadRecoveryUntilTickAfter,
            normalizedCurrentTick,
            backlashTriggered
        );
    }

    static boolean shouldTriggerOverloadBacklash(final double overload) {
        return normalizeNonNegativeDouble(overload) >= OVERLOAD_BACKLASH_THRESHOLD;
    }

    static boolean shouldMarkCombatActivityForSword(
        final String bondedSwordId,
        final String attackingSwordStableId,
        final boolean bondCacheDirty
    ) {
        if (bondCacheDirty) {
            return false;
        }
        final String normalizedBondedSwordId = normalizeSwordId(bondedSwordId);
        final String normalizedAttackingSwordStableId = normalizeSwordId(
            attackingSwordStableId
        );
        if (
            normalizedBondedSwordId.isBlank()
                || normalizedAttackingSwordStableId.isBlank()
        ) {
            return false;
        }
        return normalizedBondedSwordId.equals(normalizedAttackingSwordStableId);
    }

    private static long resolveElapsedTicks(
        final long lastOverloadTick,
        final long currentTick
    ) {
        if (currentTick <= 0L) {
            return 1L;
        }
        final long normalizedLastTick = normalizeNonNegativeLong(lastOverloadTick);
        if (normalizedLastTick <= 0L || currentTick <= normalizedLastTick) {
            return 1L;
        }
        return currentTick - normalizedLastTick;
    }

    private static double clampOverload(final double overload) {
        final double normalized = normalizeNonNegativeDouble(overload);
        if (normalized > OVERLOAD_UPPER_BOUND) {
            return OVERLOAD_UPPER_BOUND;
        }
        return normalized;
    }

    private static double normalizeNonNegativeDouble(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return value;
    }

    private static long normalizeNonNegativeLong(final long value) {
        return value < 0L ? 0L : value;
    }

    private static String normalizeSwordId(final String swordId) {
        if (swordId == null) {
            return "";
        }
        return swordId;
    }

    record OverloadResolution(
        double overloadAfter,
        long burstCooldownUntilTickAfter,
        long overloadBacklashUntilTickAfter,
        long overloadRecoveryUntilTickAfter,
        long lastOverloadTickAfter,
        boolean backlashTriggered
    ) {}

    // given 结算入参跨 tick/战斗来源 when 聚合为上下文 then 保持语义集中并消除多参数方法。
    record OverloadTickInput(
        double overloadBefore,
        long burstCooldownUntilTickBefore,
        long overloadBacklashUntilTickBefore,
        long overloadRecoveryUntilTickBefore,
        long lastOverloadTickBefore,
        long currentTick,
        boolean combatActivityThisTick,
        double combatOverloadGrowthPerTick
    ) {}
}
