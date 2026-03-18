package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ZhuanCostHelper;
import net.minecraft.world.entity.LivingEntity;

public final class BenmingSwordResourceTransaction {

    private static final double ZERO = 0.0;
    private static final long ZERO_TICK = 0L;

    private BenmingSwordResourceTransaction() {}

    public enum FailureReason {
        NONE,
        INVALID_ENTITY,
        INVALID_REQUEST,
        ATOMIC_MUTATION_UNSUPPORTED,
        INSUFFICIENT_ZHENYUAN,
        INSUFFICIENT_NIANTOU,
        INSUFFICIENT_HUNPO,
        OVERLOAD_LIMIT_EXCEEDED,
        BURST_COOLDOWN_ACTIVE,
        RITUAL_LOCK_ACTIVE,
        ILLEGAL_SWORD_STATE,
        RESOURCE_WRITE_FAILED,
        PHASE_STATE_WRITE_FAILED
    }

    public enum SwordStateSource {
        UNSPECIFIED,
        LIVE_SWORD,
        RECALLED_SWORD,
        WITHDRAWN_ILLEGAL_DETACH
    }

    public record PhaseGuard(
        SwordStateSource swordStateSource,
        boolean stateLegal,
        boolean requireBurstReady,
        boolean requireRitualUnlocked,
        double currentOverload,
        boolean enforceOverloadLimit,
        double overloadLimit,
        long currentTick,
        long burstCooldownUntilTick,
        long ritualLockUntilTick
    ) {

        private static final int EXPECTED_PHASE_TICK_COUNT = 3;

        private static final PhaseGuard NONE = new PhaseGuard(
            SwordStateSource.UNSPECIFIED,
            true,
            false,
            false,
            ZERO,
            false,
            ZERO,
            ZERO_TICK,
            ZERO_TICK,
            ZERO_TICK
        );

        public PhaseGuard {
            swordStateSource = swordStateSource == null
                ? SwordStateSource.UNSPECIFIED
                : swordStateSource;
            currentOverload = normalizeNonNegative(currentOverload);
            overloadLimit = normalizeNonNegative(overloadLimit);
            currentTick = normalizeNonNegativeLong(currentTick);
            burstCooldownUntilTick = normalizeNonNegativeLong(burstCooldownUntilTick);
            ritualLockUntilTick = normalizeNonNegativeLong(ritualLockUntilTick);
        }

        public static PhaseGuard none() {
            return NONE;
        }

        public static PhaseGuard liveSword(
            final boolean stateLegal,
            final boolean requireBurstReady,
            final boolean requireRitualUnlocked,
            final double currentOverload,
            final boolean enforceOverloadLimit,
            final double overloadLimit,
            final long... phaseTicks
        ) {
            final PhaseTickTriplet resolvedTicks = resolvePhaseTicks(phaseTicks);
            return new PhaseGuard(
                SwordStateSource.LIVE_SWORD,
                stateLegal,
                requireBurstReady,
                requireRitualUnlocked,
                currentOverload,
                enforceOverloadLimit,
                overloadLimit,
                resolvedTicks.currentTick(),
                resolvedTicks.burstCooldownUntilTick(),
                resolvedTicks.ritualLockUntilTick()
            );
        }

        public static PhaseGuard recalledSword(
            final boolean stateLegal,
            final boolean requireBurstReady,
            final boolean requireRitualUnlocked,
            final double currentOverload,
            final boolean enforceOverloadLimit,
            final double overloadLimit,
            final long... phaseTicks
        ) {
            final PhaseTickTriplet resolvedTicks = resolvePhaseTicks(phaseTicks);
            return new PhaseGuard(
                SwordStateSource.RECALLED_SWORD,
                stateLegal,
                requireBurstReady,
                requireRitualUnlocked,
                currentOverload,
                enforceOverloadLimit,
                overloadLimit,
                resolvedTicks.currentTick(),
                resolvedTicks.burstCooldownUntilTick(),
                resolvedTicks.ritualLockUntilTick()
            );
        }

        public static PhaseGuard withdrawnIllegalDetach(
            final boolean stateLegal,
            final boolean requireBurstReady,
            final boolean requireRitualUnlocked,
            final double currentOverload,
            final boolean enforceOverloadLimit,
            final double overloadLimit,
            final long... phaseTicks
        ) {
            final PhaseTickTriplet resolvedTicks = resolvePhaseTicks(phaseTicks);
            return new PhaseGuard(
                SwordStateSource.WITHDRAWN_ILLEGAL_DETACH,
                stateLegal,
                requireBurstReady,
                requireRitualUnlocked,
                currentOverload,
                enforceOverloadLimit,
                overloadLimit,
                resolvedTicks.currentTick(),
                resolvedTicks.burstCooldownUntilTick(),
                resolvedTicks.ritualLockUntilTick()
            );
        }

        private static PhaseTickTriplet resolvePhaseTicks(final long... phaseTicks) {
            if (phaseTicks == null || phaseTicks.length != EXPECTED_PHASE_TICK_COUNT) {
                throw new IllegalArgumentException("phase tick triplet must contain current/burst/ritual");
            }
            return new PhaseTickTriplet(phaseTicks[0], phaseTicks[1], phaseTicks[2]);
        }

        public boolean burstCooldownActive() {
            return requireBurstReady && currentTick < burstCooldownUntilTick;
        }

        public boolean ritualLockActive() {
            return requireRitualUnlocked && currentTick < ritualLockUntilTick;
        }

        private record PhaseTickTriplet(
            long currentTick,
            long burstCooldownUntilTick,
            long ritualLockUntilTick
        ) {}
    }

    public record PhaseMutation(
        double overloadDelta,
        boolean writeBurstCooldown,
        long burstCooldownUntilTick,
        boolean writeRitualLock,
        long ritualLockUntilTick
    ) {

        private static final PhaseMutation NONE = new PhaseMutation(
            ZERO,
            false,
            ZERO_TICK,
            false,
            ZERO_TICK
        );

        public PhaseMutation {
            overloadDelta = normalizeFiniteDelta(overloadDelta);
            burstCooldownUntilTick = normalizeNonNegativeLong(burstCooldownUntilTick);
            ritualLockUntilTick = normalizeNonNegativeLong(ritualLockUntilTick);
        }

        public static PhaseMutation none() {
            return NONE;
        }

        public boolean writesOverload() {
            return overloadDelta != ZERO;
        }

        public boolean writesAnyPhaseState() {
            return writesOverload() || writeBurstCooldown || writeRitualLock;
        }

        public double resolveOverloadAfter(final double overloadBefore) {
            return normalizeNonNegative(overloadBefore + overloadDelta);
        }
    }

    public record PhaseOutcome(
        SwordStateSource swordStateSource,
        double overloadBefore,
        double overloadAfter,
        long burstCooldownUntilTickBefore,
        long burstCooldownUntilTickAfter,
        long ritualLockUntilTickBefore,
        long ritualLockUntilTickAfter,
        boolean overloadWritten,
        boolean burstCooldownWritten,
        boolean ritualLockWritten
    ) {

        private static final PhaseOutcome NONE = new PhaseOutcome(
            SwordStateSource.UNSPECIFIED,
            ZERO,
            ZERO,
            ZERO_TICK,
            ZERO_TICK,
            ZERO_TICK,
            ZERO_TICK,
            false,
            false,
            false
        );

        public PhaseOutcome {
            swordStateSource = swordStateSource == null
                ? SwordStateSource.UNSPECIFIED
                : swordStateSource;
            overloadBefore = normalizeNonNegative(overloadBefore);
            overloadAfter = normalizeNonNegative(overloadAfter);
            burstCooldownUntilTickBefore = normalizeNonNegativeLong(burstCooldownUntilTickBefore);
            burstCooldownUntilTickAfter = normalizeNonNegativeLong(burstCooldownUntilTickAfter);
            ritualLockUntilTickBefore = normalizeNonNegativeLong(ritualLockUntilTickBefore);
            ritualLockUntilTickAfter = normalizeNonNegativeLong(ritualLockUntilTickAfter);
        }

        public static PhaseOutcome none() {
            return NONE;
        }

        public static PhaseOutcome rollback(final PhaseGuard phaseGuard) {
            if (phaseGuard == null) {
                return none();
            }
            return new PhaseOutcome(
                phaseGuard.swordStateSource(),
                phaseGuard.currentOverload(),
                phaseGuard.currentOverload(),
                phaseGuard.burstCooldownUntilTick(),
                phaseGuard.burstCooldownUntilTick(),
                phaseGuard.ritualLockUntilTick(),
                phaseGuard.ritualLockUntilTick(),
                false,
                false,
                false
            );
        }

        public static PhaseOutcome committed(
            final PhaseGuard phaseGuard,
            final PhaseMutation phaseMutation
        ) {
            if (phaseGuard == null || phaseMutation == null) {
                return none();
            }
            final double overloadBefore = phaseGuard.currentOverload();
            final long burstCooldownBefore = phaseGuard.burstCooldownUntilTick();
            final long ritualLockBefore = phaseGuard.ritualLockUntilTick();
            return new PhaseOutcome(
                phaseGuard.swordStateSource(),
                overloadBefore,
                phaseMutation.writesOverload()
                    ? phaseMutation.resolveOverloadAfter(overloadBefore)
                    : overloadBefore,
                burstCooldownBefore,
                phaseMutation.writeBurstCooldown()
                    ? phaseMutation.burstCooldownUntilTick()
                    : burstCooldownBefore,
                ritualLockBefore,
                phaseMutation.writeRitualLock()
                    ? phaseMutation.ritualLockUntilTick()
                    : ritualLockBefore,
                phaseMutation.writesOverload(),
                phaseMutation.writeBurstCooldown(),
                phaseMutation.writeRitualLock()
            );
        }
    }

    public record Request(
        double zhenyuanBaseCost,
        double niantouBaseCost,
        double hunpoBaseCost,
        PhaseGuard phaseGuard,
        PhaseMutation phaseMutation
    ) {

        public Request {
            zhenyuanBaseCost = normalizeNonNegative(zhenyuanBaseCost);
            niantouBaseCost = normalizeNonNegative(niantouBaseCost);
            hunpoBaseCost = normalizeNonNegative(hunpoBaseCost);
            phaseGuard = phaseGuard == null ? PhaseGuard.none() : phaseGuard;
            phaseMutation = phaseMutation == null ? PhaseMutation.none() : phaseMutation;
        }

        public Request(
            final double zhenyuanBaseCost,
            final double niantouBaseCost,
            final double hunpoBaseCost
        ) {
            this(
                zhenyuanBaseCost,
                niantouBaseCost,
                hunpoBaseCost,
                PhaseGuard.none(),
                PhaseMutation.none()
            );
        }

        public boolean requiresPhaseStateWrites() {
            return phaseMutation.writesAnyPhaseState();
        }
    }

    public record Result(
        boolean success,
        FailureReason failureReason,
        double zhenyuanCost,
        double niantouCost,
        double hunpoCost,
        PhaseOutcome phaseOutcome
    ) {

        public Result {
            failureReason = failureReason == null ? FailureReason.INVALID_REQUEST : failureReason;
            zhenyuanCost = normalizeNonNegative(zhenyuanCost);
            niantouCost = normalizeNonNegative(niantouCost);
            hunpoCost = normalizeNonNegative(hunpoCost);
            phaseOutcome = phaseOutcome == null ? PhaseOutcome.none() : phaseOutcome;
        }

        public static Result success(
            final double zhenyuanCost,
            final double niantouCost,
            final double hunpoCost
        ) {
            return success(zhenyuanCost, niantouCost, hunpoCost, PhaseOutcome.none());
        }

        public static Result success(
            final double zhenyuanCost,
            final double niantouCost,
            final double hunpoCost,
            final PhaseOutcome phaseOutcome
        ) {
            return new Result(
                true,
                FailureReason.NONE,
                zhenyuanCost,
                niantouCost,
                hunpoCost,
                phaseOutcome
            );
        }

        public static Result failure(
            final FailureReason failureReason,
            final double zhenyuanCost,
            final double niantouCost,
            final double hunpoCost
        ) {
            return failure(
                failureReason,
                zhenyuanCost,
                niantouCost,
                hunpoCost,
                PhaseOutcome.none()
            );
        }

        public static Result failure(
            final FailureReason failureReason,
            final double zhenyuanCost,
            final double niantouCost,
            final double hunpoCost,
            final PhaseOutcome phaseOutcome
        ) {
            return new Result(
                false,
                failureReason,
                zhenyuanCost,
                niantouCost,
                hunpoCost,
                phaseOutcome
            );
        }

        public SwordStateSource swordStateSource() {
            return phaseOutcome.swordStateSource();
        }
    }

    @FunctionalInterface
    public interface CostScaler {

        double scaleCost(double baseCost);
    }

    public interface ResourceMutationPort {

        void spendZhenyuan(double amount);

        void spendNiantou(double amount);

        void spendHunpo(double amount);

        /**
         * 是否支持资源扣减后的补偿回滚。
         *
         * <p>当事务同时携带 phase-state 写入时，资源端必须支持回滚，
         * 否则无法证明“先扣资源、后写 phase-state”路径上的原子性。
         */
        default boolean supportsResourceRollback() {
            return false;
        }

        /**
         * 回滚已扣除的真元。
         *
         * <p>仅在 {@link #supportsResourceRollback()} 返回 true 时调用；
         * 默认实现故意抛错，避免调用方误以为已经完成补偿。
         */
        default void refundZhenyuan(final double amount) {
            throw new UnsupportedOperationException("resource rollback unsupported: zhenyuan");
        }

        default void refundNiantou(final double amount) {
            throw new UnsupportedOperationException("resource rollback unsupported: niantou");
        }

        default void refundHunpo(final double amount) {
            throw new UnsupportedOperationException("resource rollback unsupported: hunpo");
        }
    }

    public interface PhaseStateMutationPort {

        default boolean supportsPhaseStateWrites() {
            return true;
        }

        /**
         * 这些 setter 不仅承担正向提交，也承担补偿回滚。
         *
         * <p>实现方必须允许把 phaseGuard 中的“事务前权威值”重新写回，
         * 以便在爆发 / 过载事务半途中抛错时恢复到提交前状态。
         */
        void setOverload(double overload);

        void setBurstCooldownUntilTick(long burstCooldownUntilTick);

        void setRitualLockUntilTick(long ritualLockUntilTick);
    }

    public interface TransactionMutationPort extends ResourceMutationPort, PhaseStateMutationPort {}

    public static Result tryConsume(final LivingEntity entity, final Request request) {
        if (entity == null) {
            return Result.failure(FailureReason.INVALID_ENTITY, ZERO, ZERO, ZERO);
        }
        if (request == null) {
            return Result.failure(FailureReason.INVALID_REQUEST, ZERO, ZERO, ZERO);
        }

        final CultivationSnapshot snapshot = CultivationSnapshot.capture(entity);
        final double zhenyuanCost = normalizeNonNegative(
            ZhenYuanHelper.calculateGuCost(entity, request.zhenyuanBaseCost())
        );
        final CostScaler conservativeScaler = baseCost ->
            ZhuanCostHelper.scaleCost(entity, normalizeNonNegative(baseCost));

        return tryConsume(
            snapshot,
            request,
            zhenyuanCost,
            conservativeScaler,
            new BridgeResourceMutationPort(entity),
            UnsupportedPhaseStateMutationPort.INSTANCE
        );
    }

    public static Result tryConsume(
        final LivingEntity entity,
        final Request request,
        final TransactionMutationPort mutationPort
    ) {
        if (entity == null) {
            return Result.failure(FailureReason.INVALID_ENTITY, ZERO, ZERO, ZERO);
        }
        if (request == null || mutationPort == null) {
            return Result.failure(FailureReason.INVALID_REQUEST, ZERO, ZERO, ZERO);
        }

        final CultivationSnapshot snapshot = CultivationSnapshot.capture(entity);
        final double zhenyuanCost = normalizeNonNegative(
            ZhenYuanHelper.calculateGuCost(entity, request.zhenyuanBaseCost())
        );
        final CostScaler conservativeScaler = baseCost ->
            ZhuanCostHelper.scaleCost(entity, normalizeNonNegative(baseCost));

        return tryConsume(
            snapshot,
            request,
            zhenyuanCost,
            conservativeScaler,
            mutationPort,
            mutationPort
        );
    }

    public static Result tryConsume(
        final CultivationSnapshot snapshot,
        final Request request,
        final double zhenyuanCost,
        final CostScaler conservativeScaler,
        final ResourceMutationPort mutationPort
    ) {
        return tryConsume(
            snapshot,
            request,
            zhenyuanCost,
            conservativeScaler,
            mutationPort,
            UnsupportedPhaseStateMutationPort.INSTANCE
        );
    }

    public static Result tryConsume(
        final CultivationSnapshot snapshot,
        final Request request,
        final double zhenyuanCost,
        final CostScaler conservativeScaler,
        final TransactionMutationPort mutationPort
    ) {
        return tryConsume(
            snapshot,
            request,
            zhenyuanCost,
            conservativeScaler,
            mutationPort,
            mutationPort
        );
    }

    public static Result tryConsume(
        final CultivationSnapshot snapshot,
        final Request request,
        final double zhenyuanCost,
        final CostScaler conservativeScaler,
        final ResourceMutationPort mutationPort,
        final PhaseStateMutationPort phaseStateMutationPort
    ) {
        if (request == null || snapshot == null || conservativeScaler == null || mutationPort == null) {
            return Result.failure(FailureReason.INVALID_REQUEST, ZERO, ZERO, ZERO);
        }

        final PhaseGuard phaseGuard = request.phaseGuard();
        final PhaseMutation phaseMutation = request.phaseMutation();
        final PhaseOutcome rollbackOutcome = PhaseOutcome.rollback(phaseGuard);
        if (
            request.requiresPhaseStateWrites()
                && (phaseStateMutationPort == null || !phaseStateMutationPort.supportsPhaseStateWrites())
        ) {
            return Result.failure(
                FailureReason.INVALID_REQUEST,
                ZERO,
                ZERO,
                ZERO,
                rollbackOutcome
            );
        }
        if (request.requiresPhaseStateWrites() && !mutationPort.supportsResourceRollback()) {
            return Result.failure(
                FailureReason.ATOMIC_MUTATION_UNSUPPORTED,
                ZERO,
                ZERO,
                ZERO,
                rollbackOutcome
            );
        }

        final double safeZhenyuanCost = normalizeNonNegative(zhenyuanCost);
        final double niantouCost = normalizeNonNegative(
            conservativeScaler.scaleCost(request.niantouBaseCost())
        );
        final double hunpoCost = normalizeNonNegative(
            conservativeScaler.scaleCost(request.hunpoBaseCost())
        );

        if (!phaseGuard.stateLegal()) {
            return Result.failure(
                FailureReason.ILLEGAL_SWORD_STATE,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost,
                rollbackOutcome
            );
        }
        if (phaseGuard.ritualLockActive()) {
            return Result.failure(
                FailureReason.RITUAL_LOCK_ACTIVE,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost,
                rollbackOutcome
            );
        }
        if (phaseGuard.burstCooldownActive()) {
            return Result.failure(
                FailureReason.BURST_COOLDOWN_ACTIVE,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost,
                rollbackOutcome
            );
        }

        final double overloadAfter = phaseMutation.resolveOverloadAfter(
            phaseGuard.currentOverload()
        );
        if (phaseGuard.enforceOverloadLimit() && overloadAfter > phaseGuard.overloadLimit()) {
            return Result.failure(
                FailureReason.OVERLOAD_LIMIT_EXCEEDED,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost,
                rollbackOutcome
            );
        }

        if (snapshot.zhenyuan() < safeZhenyuanCost) {
            return Result.failure(
                FailureReason.INSUFFICIENT_ZHENYUAN,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost,
                rollbackOutcome
            );
        }
        if (snapshot.niantou() < niantouCost) {
            return Result.failure(
                FailureReason.INSUFFICIENT_NIANTOU,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost,
                rollbackOutcome
            );
        }
        if (snapshot.hunpo() < hunpoCost) {
            return Result.failure(
                FailureReason.INSUFFICIENT_HUNPO,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost,
                rollbackOutcome
            );
        }

        return tryCommitMutations(
            phaseGuard,
            new CommitPlan(
                phaseMutation,
                overloadAfter,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost
            ),
            mutationPort,
            phaseStateMutationPort
        );
    }

    private static Result tryCommitMutations(
        final PhaseGuard phaseGuard,
        final CommitPlan commitPlan,
        final ResourceMutationPort mutationPort,
        final PhaseStateMutationPort phaseStateMutationPort
    ) {
        final MutationCommitJournal journal = new MutationCommitJournal(
            phaseGuard,
            mutationPort,
            phaseStateMutationPort
        );
        try {
            journal.spendResources(
                commitPlan.zhenyuanCost(),
                commitPlan.niantouCost(),
                commitPlan.hunpoCost()
            );
        } catch (RuntimeException commitFailure) {
            journal.rollbackResourcesOrThrow(commitFailure);
            return Result.failure(
                FailureReason.RESOURCE_WRITE_FAILED,
                commitPlan.zhenyuanCost(),
                commitPlan.niantouCost(),
                commitPlan.hunpoCost(),
                PhaseOutcome.rollback(phaseGuard)
            );
        }

        try {
            journal.applyPhaseMutation(commitPlan.phaseMutation(), commitPlan.overloadAfter());
        } catch (RuntimeException commitFailure) {
            journal.rollbackPhaseStateAndResourcesOrThrow(commitFailure);
            return Result.failure(
                FailureReason.PHASE_STATE_WRITE_FAILED,
                commitPlan.zhenyuanCost(),
                commitPlan.niantouCost(),
                commitPlan.hunpoCost(),
                PhaseOutcome.rollback(phaseGuard)
            );
        }

        return Result.success(
            commitPlan.zhenyuanCost(),
            commitPlan.niantouCost(),
            commitPlan.hunpoCost(),
            PhaseOutcome.committed(phaseGuard, commitPlan.phaseMutation())
        );
    }

    private static double normalizeNonNegative(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= ZERO) {
            return ZERO;
        }
        return value;
    }

    private static double normalizeFiniteDelta(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return ZERO;
        }
        return value;
    }

    private static long normalizeNonNegativeLong(final long value) {
        return Math.max(ZERO_TICK, value);
    }

    private record CommitPlan(
        PhaseMutation phaseMutation,
        double overloadAfter,
        double zhenyuanCost,
        double niantouCost,
        double hunpoCost
    ) {}

    private static final class BridgeResourceMutationPort implements ResourceMutationPort {

        private final LivingEntity entity;

        private BridgeResourceMutationPort(final LivingEntity entity) {
            this.entity = entity;
        }

        @Override
        public void spendZhenyuan(final double amount) {
            ZhenYuanHelper.modify(entity, -amount);
        }

        @Override
        public void spendNiantou(final double amount) {
            NianTouHelper.modify(entity, -amount);
        }

        @Override
        public void spendHunpo(final double amount) {
            HunPoHelper.modify(entity, -amount);
        }

        @Override
        public boolean supportsResourceRollback() {
            return true;
        }

        @Override
        public void refundZhenyuan(final double amount) {
            ZhenYuanHelper.modify(entity, amount);
        }

        @Override
        public void refundNiantou(final double amount) {
            NianTouHelper.modify(entity, amount);
        }

        @Override
        public void refundHunpo(final double amount) {
            HunPoHelper.modify(entity, amount);
        }
    }

    /**
     * 事务提交日志。
     *
     * <p>这里显式区分“资源扣减成功了多少”和“哪些 phase-state 需要写回旧值”，
     * 以便在爆发 / 过载写入中途失败时，严格按逆序执行补偿，避免留下半完成状态。
     */
    private static final class MutationCommitJournal {

        private final PhaseGuard phaseGuard;
        private final ResourceMutationPort resourceMutationPort;
        private final PhaseStateMutationPort phaseStateMutationPort;

        private double spentZhenyuan;
        private double spentNiantou;
        private double spentHunpo;
        private boolean restoreOverload;
        private boolean restoreBurstCooldown;
        private boolean restoreRitualLock;

        private MutationCommitJournal(
            final PhaseGuard phaseGuard,
            final ResourceMutationPort resourceMutationPort,
            final PhaseStateMutationPort phaseStateMutationPort
        ) {
            this.phaseGuard = phaseGuard;
            this.resourceMutationPort = resourceMutationPort;
            this.phaseStateMutationPort = phaseStateMutationPort;
        }

        private void spendResources(
            final double zhenyuanCost,
            final double niantouCost,
            final double hunpoCost
        ) {
            if (zhenyuanCost > ZERO) {
                resourceMutationPort.spendZhenyuan(zhenyuanCost);
                spentZhenyuan = zhenyuanCost;
            }
            if (niantouCost > ZERO) {
                resourceMutationPort.spendNiantou(niantouCost);
                spentNiantou = niantouCost;
            }
            if (hunpoCost > ZERO) {
                resourceMutationPort.spendHunpo(hunpoCost);
                spentHunpo = hunpoCost;
            }
        }

        private void applyPhaseMutation(
            final PhaseMutation phaseMutation,
            final double overloadAfter
        ) {
            if (phaseMutation.writesOverload()) {
                restoreOverload = true;
                phaseStateMutationPort.setOverload(overloadAfter);
            }
            if (phaseMutation.writeBurstCooldown()) {
                restoreBurstCooldown = true;
                phaseStateMutationPort.setBurstCooldownUntilTick(
                    phaseMutation.burstCooldownUntilTick()
                );
            }
            if (phaseMutation.writeRitualLock()) {
                restoreRitualLock = true;
                phaseStateMutationPort.setRitualLockUntilTick(
                    phaseMutation.ritualLockUntilTick()
                );
            }
        }

        private void rollbackResourcesOrThrow(final RuntimeException commitFailure) {
            final RuntimeException rollbackFailure = rollbackResources(null);
            if (rollbackFailure != null) {
                throw buildRollbackFailure(commitFailure, rollbackFailure);
            }
        }

        private void rollbackPhaseStateAndResourcesOrThrow(final RuntimeException commitFailure) {
            RuntimeException rollbackFailure = rollbackPhaseState(null);
            rollbackFailure = rollbackResources(rollbackFailure);
            if (rollbackFailure != null) {
                throw buildRollbackFailure(commitFailure, rollbackFailure);
            }
        }

        private RuntimeException rollbackPhaseState(RuntimeException rollbackFailure) {
            if (restoreRitualLock) {
                rollbackFailure = rollbackStep(
                    rollbackFailure,
                    () -> phaseStateMutationPort.setRitualLockUntilTick(
                        phaseGuard.ritualLockUntilTick()
                    )
                );
            }
            if (restoreBurstCooldown) {
                rollbackFailure = rollbackStep(
                    rollbackFailure,
                    () -> phaseStateMutationPort.setBurstCooldownUntilTick(
                        phaseGuard.burstCooldownUntilTick()
                    )
                );
            }
            if (restoreOverload) {
                rollbackFailure = rollbackStep(
                    rollbackFailure,
                    () -> phaseStateMutationPort.setOverload(phaseGuard.currentOverload())
                );
            }
            return rollbackFailure;
        }

        private RuntimeException rollbackResources(RuntimeException rollbackFailure) {
            if (
                (spentHunpo > ZERO || spentNiantou > ZERO || spentZhenyuan > ZERO)
                    && !resourceMutationPort.supportsResourceRollback()
            ) {
                return appendRollbackFailure(
                    rollbackFailure,
                    new IllegalStateException("resource rollback unsupported for atomic transaction")
                );
            }
            if (spentHunpo > ZERO) {
                rollbackFailure = rollbackStep(
                    rollbackFailure,
                    () -> resourceMutationPort.refundHunpo(spentHunpo)
                );
            }
            if (spentNiantou > ZERO) {
                rollbackFailure = rollbackStep(
                    rollbackFailure,
                    () -> resourceMutationPort.refundNiantou(spentNiantou)
                );
            }
            if (spentZhenyuan > ZERO) {
                rollbackFailure = rollbackStep(
                    rollbackFailure,
                    () -> resourceMutationPort.refundZhenyuan(spentZhenyuan)
                );
            }
            return rollbackFailure;
        }

        private RuntimeException rollbackStep(
            final RuntimeException rollbackFailure,
            final RollbackAction action
        ) {
            try {
                action.run();
                return rollbackFailure;
            } catch (RuntimeException exception) {
                return appendRollbackFailure(rollbackFailure, exception);
            }
        }

        private RuntimeException appendRollbackFailure(
            final RuntimeException rollbackFailure,
            final RuntimeException nextFailure
        ) {
            if (rollbackFailure == null) {
                return nextFailure;
            }
            rollbackFailure.addSuppressed(nextFailure);
            return rollbackFailure;
        }

        private IllegalStateException buildRollbackFailure(
            final RuntimeException commitFailure,
            final RuntimeException rollbackFailure
        ) {
            final IllegalStateException invariantFailure = new IllegalStateException(
                "本命资源事务补偿回滚失败，状态可能已不一致。",
                rollbackFailure
            );
            invariantFailure.addSuppressed(commitFailure);
            return invariantFailure;
        }
    }

    @FunctionalInterface
    private interface RollbackAction {

        void run();
    }

    private enum UnsupportedPhaseStateMutationPort implements PhaseStateMutationPort {
        INSTANCE;

        @Override
        public boolean supportsPhaseStateWrites() {
            return false;
        }

        @Override
        public void setOverload(final double overload) {}

        @Override
        public void setBurstCooldownUntilTick(final long burstCooldownUntilTick) {}

        @Override
        public void setRitualLockUntilTick(final long ritualLockUntilTick) {}
    }
}
