package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment;
import java.util.List;

public final class BenmingSwordBondService {

    private static final String EMPTY_VALUE = "";
    private static final double ZERO_RESONANCE = 0.0D;
    private static final double ZERO_BACKLASH = 0.0D;
    private static final int DEFAULT_RITUAL_DUPLICATE_GUARD_TICKS = 6;
    private static final double LIGHT_BACKLASH_ZHENYUAN_COST = 12.0D;
    private static final double LIGHT_BACKLASH_NIANTOU_COST = 8.0D;
    private static final double LIGHT_BACKLASH_HUNPO_COST = 5.0D;
    private static final int LIGHT_BACKLASH_COOLDOWN_TICKS = 40;
    private static final String LIGHT_BACKLASH_COOLDOWN_KEY =
        "guzhenrenext:benming/light_backlash";

    private static final double ACTIVE_UNBIND_ZHENYUAN_BASE_COST = 40.0D;
    private static final double ACTIVE_UNBIND_NIANTOU_BASE_COST = 30.0D;
    private static final double ACTIVE_UNBIND_HUNPO_BASE_COST = 20.0D;

    private static final BenmingSwordResourceTransaction.Request DEFAULT_ACTIVE_UNBIND_REQUEST =
        new BenmingSwordResourceTransaction.Request(
            ACTIVE_UNBIND_ZHENYUAN_BASE_COST,
            ACTIVE_UNBIND_NIANTOU_BASE_COST,
            ACTIVE_UNBIND_HUNPO_BASE_COST
        );

    private BenmingSwordBondService() {}

    public enum FailureReason {
        NONE,
        INVALID_REQUEST,
        NO_SELECTED_SWORD,
        NO_BONDED_SWORD,
        PLAYER_ALREADY_HAS_BONDED_SWORD,
        TARGET_BOUND_TO_OTHER_PLAYER,
        TARGET_NOT_BOUND_TO_PLAYER,
        MULTIPLE_BONDED_SWORDS,
        ACTIVE_UNBIND_COST_REJECTED,
        RITUAL_RESOURCES_INSUFFICIENT,
        RITUAL_STATE_ILLEGAL,
        RITUAL_COOLDOWN_ACTIVE,
        RITUAL_DUPLICATE_REQUEST,
        RITUAL_TARGET_MISMATCH
    }

    public enum ResultBranch {
        QUERY,
        BIND,
        RITUAL_PRECHECK,
        RITUAL_BIND,
        ACTIVE_UNBIND,
        FORCED_UNBIND,
        ILLEGAL_DETACH
    }

    public enum BacklashType {
        NONE,
        FORCED_UNBIND_LIGHT,
        ILLEGAL_DETACH_LIGHT
    }

    public record BacklashEffect(BacklashType type, double amount) {

        public static BacklashEffect none() {
            return new BacklashEffect(BacklashType.NONE, ZERO_BACKLASH);
        }
    }

    public record Result(
        boolean success,
        ResultBranch branch,
        FailureReason failureReason,
        String stableSwordId,
        BenmingSwordResourceTransaction.FailureReason resourceFailureReason,
        BacklashEffect backlashEffect
    ) {

        public static Result success(
            final ResultBranch branch,
            final String stableSwordId
        ) {
            return new Result(
                true,
                branch,
                FailureReason.NONE,
                normalizeId(stableSwordId),
                BenmingSwordResourceTransaction.FailureReason.NONE,
                BacklashEffect.none()
            );
        }

        public static Result successWithBacklash(
            final ResultBranch branch,
            final String stableSwordId,
            final BacklashEffect backlashEffect
        ) {
            return new Result(
                true,
                branch,
                FailureReason.NONE,
                normalizeId(stableSwordId),
                BenmingSwordResourceTransaction.FailureReason.NONE,
                normalizeBacklashEffect(backlashEffect)
            );
        }

        public static Result failure(
            final ResultBranch branch,
            final FailureReason failureReason,
            final String stableSwordId
        ) {
            return failure(
                branch,
                failureReason,
                stableSwordId,
                BenmingSwordResourceTransaction.FailureReason.NONE,
                BacklashEffect.none()
            );
        }

        public static Result failure(
            final ResultBranch branch,
            final FailureReason failureReason,
            final String stableSwordId,
            final BenmingSwordResourceTransaction.FailureReason resourceFailureReason
        ) {
            return failure(
                branch,
                failureReason,
                stableSwordId,
                resourceFailureReason,
                BacklashEffect.none()
            );
        }

        public static Result failure(
            final ResultBranch branch,
            final FailureReason failureReason,
            final String stableSwordId,
            final BenmingSwordResourceTransaction.FailureReason resourceFailureReason,
            final BacklashEffect backlashEffect
        ) {
            return new Result(
                false,
                branch,
                failureReason,
                normalizeId(stableSwordId),
                resourceFailureReason == null
                    ? BenmingSwordResourceTransaction.FailureReason.NONE
                    : resourceFailureReason,
                normalizeBacklashEffect(backlashEffect)
            );
        }

        private static BacklashEffect normalizeBacklashEffect(final BacklashEffect backlashEffect) {
            if (backlashEffect == null || backlashEffect.type() == null) {
                return BacklashEffect.none();
            }
            return backlashEffect;
        }
    }

    public interface SwordBondPort {

        String getStableSwordId();

        String getBondOwnerUuid();

        double getBondResonance();

        void setBondOwnerUuid(String ownerUuid);

        void setBondResonance(double resonance);
    }

    public interface PlayerBondCachePort {

        String getBondedSwordId();

        boolean isBondCacheDirty();

        void updateBondCache(String stableSwordId, long resolvedTick);

        void markBondCacheDirty();

        void clearBondCache();
    }

    public interface BacklashCooldownPort {

        int get(String key);

        void set(String key, int ticks);
    }

    public interface RitualRequestStatePort {

        String getLockedSwordId();

        long getLockedUntilTick();

        boolean isExecutionPending();

        void beginRitualRequest(String stableSwordId, long lockedUntilTick);

        void markExecutionConsumed();

        void clearRitualRequest();
    }

    public record RitualRequestContext(
        RitualRequestStatePort requestState,
        int duplicateGuardTicks
    ) {

        public RitualRequestContext {
            duplicateGuardTicks = duplicateGuardTicks <= 0
                ? DEFAULT_RITUAL_DUPLICATE_GUARD_TICKS
                : duplicateGuardTicks;
        }
    }

    public record RitualBindTransactionContext(
        CultivationSnapshot snapshot,
        BenmingSwordResourceTransaction.Request request,
        double zhenyuanCost,
        BenmingSwordResourceTransaction.CostScaler conservativeScaler,
        BenmingSwordResourceTransaction.TransactionMutationPort mutationPort,
        RitualRequestContext requestContext
    ) {}

    public record ActiveUnbindTransactionContext(
        CultivationSnapshot snapshot,
        BenmingSwordResourceTransaction.Request request,
        double zhenyuanCost,
        BenmingSwordResourceTransaction.CostScaler conservativeScaler,
        BenmingSwordResourceTransaction.ResourceMutationPort mutationPort
    ) {}

    public record BacklashContext(
        CultivationSnapshot snapshot,
        BenmingSwordResourceTransaction.ResourceMutationPort mutationPort,
        BacklashCooldownPort cooldownPort,
        String cooldownKey,
        int cooldownTicks
    ) {

        public BacklashContext {
            cooldownKey = normalizeId(cooldownKey);
            cooldownTicks = Math.max(0, cooldownTicks);
        }
    }

    public static BenmingSwordResourceTransaction.Request defaultActiveUnbindRequest() {
        return DEFAULT_ACTIVE_UNBIND_REQUEST;
    }

    public static int defaultLightBacklashCooldownTicks() {
        return LIGHT_BACKLASH_COOLDOWN_TICKS;
    }

    public static int defaultRitualDuplicateGuardTicks() {
        return DEFAULT_RITUAL_DUPLICATE_GUARD_TICKS;
    }

    public static String defaultLightBacklashCooldownKey(final String ownerUuid) {
        final String normalizedOwnerUuid = normalizeId(ownerUuid);
        if (normalizedOwnerUuid.isBlank()) {
            return LIGHT_BACKLASH_COOLDOWN_KEY;
        }
        return LIGHT_BACKLASH_COOLDOWN_KEY + "/" + normalizedOwnerUuid;
    }

    public static BacklashContext defaultLightBacklashContext(
        final String ownerUuid,
        final CultivationSnapshot snapshot,
        final BenmingSwordResourceTransaction.ResourceMutationPort mutationPort,
        final FlyingSwordCooldownAttachment cooldownAttachment
    ) {
        return new BacklashContext(
            snapshot,
            mutationPort,
            toBacklashCooldownPort(cooldownAttachment),
            defaultLightBacklashCooldownKey(ownerUuid),
            LIGHT_BACKLASH_COOLDOWN_TICKS
        );
    }

    public static BacklashCooldownPort toBacklashCooldownPort(
        final FlyingSwordCooldownAttachment cooldownAttachment
    ) {
        if (cooldownAttachment == null) {
            return null;
        }
        return new BacklashCooldownPort() {
            @Override
            public int get(final String key) {
                return cooldownAttachment.get(key);
            }

            @Override
            public void set(final String key, final int ticks) {
                cooldownAttachment.set(key, ticks);
            }
        };
    }

    public static Result queryBoundSword(
        final String ownerUuid,
        final List<? extends SwordBondPort> ownedSwords,
        final PlayerBondCachePort playerCache,
        final long resolvedTick
    ) {
        if (!isValidRequest(ownerUuid, ownedSwords, playerCache)) {
            return Result.failure(ResultBranch.QUERY, FailureReason.INVALID_REQUEST, EMPTY_VALUE);
        }

        final String normalizedOwnerUuid = normalizeId(ownerUuid);
        final String cachedSwordId = normalizeId(playerCache.getBondedSwordId());
        if (!playerCache.isBondCacheDirty() && !cachedSwordId.isBlank()) {
            final SwordBondPort cachedSword = findByStableSwordId(
                ownedSwords,
                cachedSwordId
            );
            if (
                cachedSword != null
                    && normalizedOwnerUuid.equals(
                        normalizeId(cachedSword.getBondOwnerUuid())
                    )
            ) {
                playerCache.updateBondCache(cachedSwordId, resolvedTick);
                return Result.success(ResultBranch.QUERY, cachedSwordId);
            }
        }

        final BoundScanResult scanResult = scanBoundSword(
            normalizedOwnerUuid,
            ownedSwords,
            EMPTY_VALUE
        );
        if (scanResult.multipleBound()) {
            playerCache.markBondCacheDirty();
            return Result.failure(
                ResultBranch.QUERY,
                FailureReason.MULTIPLE_BONDED_SWORDS,
                scanResult.boundSwordId()
            );
        }
        if (scanResult.boundSwordId().isBlank()) {
            playerCache.clearBondCache();
            return Result.failure(
                ResultBranch.QUERY,
                FailureReason.NO_BONDED_SWORD,
                EMPTY_VALUE
            );
        }

        playerCache.updateBondCache(scanResult.boundSwordId(), resolvedTick);
        return Result.success(ResultBranch.QUERY, scanResult.boundSwordId());
    }

    public static Result bind(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final List<? extends SwordBondPort> ownedSwords,
        final PlayerBondCachePort playerCache,
        final long resolvedTick
    ) {
        if (
            normalizeId(ownerUuid).isBlank()
                || targetSword == null
                || ownedSwords == null
                || playerCache == null
        ) {
            return Result.failure(ResultBranch.BIND, FailureReason.INVALID_REQUEST, EMPTY_VALUE);
        }

        final String normalizedOwnerUuid = normalizeId(ownerUuid);
        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        if (targetStableSwordId.isBlank()) {
            return Result.failure(ResultBranch.BIND, FailureReason.INVALID_REQUEST, EMPTY_VALUE);
        }

        final String targetOwnerUuid = normalizeId(targetSword.getBondOwnerUuid());
        if (!targetOwnerUuid.isBlank() && !targetOwnerUuid.equals(normalizedOwnerUuid)) {
            return Result.failure(
                ResultBranch.BIND,
                FailureReason.TARGET_BOUND_TO_OTHER_PLAYER,
                targetStableSwordId
            );
        }

        final BoundScanResult scanResult = scanBoundSword(
            normalizedOwnerUuid,
            ownedSwords,
            targetStableSwordId
        );
        if (scanResult.multipleBound()) {
            return Result.failure(
                ResultBranch.BIND,
                FailureReason.MULTIPLE_BONDED_SWORDS,
                scanResult.boundSwordId()
            );
        }
        if (!scanResult.boundSwordId().isBlank()) {
            return Result.failure(
                ResultBranch.BIND,
                FailureReason.PLAYER_ALREADY_HAS_BONDED_SWORD,
                scanResult.boundSwordId()
            );
        }

        return completeBond(
            ResultBranch.BIND,
            normalizedOwnerUuid,
            targetOwnerUuid,
            targetSword,
            playerCache,
            resolvedTick
        );
    }

    public static Result precheckRitualBind(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final List<? extends SwordBondPort> ownedSwords,
        final PlayerBondCachePort playerCache,
        final RitualRequestContext requestContext,
        final long resolvedTick
    ) {
        if (!isValidRitualRequest(ownerUuid, targetSword, ownedSwords, playerCache, requestContext)) {
            return Result.failure(
                ResultBranch.RITUAL_PRECHECK,
                FailureReason.INVALID_REQUEST,
                EMPTY_VALUE
            );
        }

        final String normalizedOwnerUuid = normalizeId(ownerUuid);
        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        if (targetStableSwordId.isBlank()) {
            return Result.failure(
                ResultBranch.RITUAL_PRECHECK,
                FailureReason.INVALID_REQUEST,
                EMPTY_VALUE
            );
        }

        final RitualRequestStatePort requestState = requestContext.requestState();
        clearExpiredRitualRequest(requestState, resolvedTick);
        final String lockedSwordId = normalizeId(requestState.getLockedSwordId());
        if (isActiveRitualRequest(requestState, resolvedTick, lockedSwordId)) {
            if (!lockedSwordId.equals(targetStableSwordId)) {
                return Result.failure(
                    ResultBranch.RITUAL_PRECHECK,
                    FailureReason.RITUAL_TARGET_MISMATCH,
                    lockedSwordId
                );
            }
            return Result.failure(
                ResultBranch.RITUAL_PRECHECK,
                FailureReason.RITUAL_DUPLICATE_REQUEST,
                targetStableSwordId
            );
        }

        final Result validationFailure = validateRitualBindEligibility(
            ResultBranch.RITUAL_PRECHECK,
            normalizedOwnerUuid,
            targetSword,
            ownedSwords
        );
        if (validationFailure != null) {
            return validationFailure;
        }

        requestState.beginRitualRequest(
            targetStableSwordId,
            resolveRitualGuardUntilTick(requestContext, resolvedTick)
        );
        return Result.success(ResultBranch.RITUAL_PRECHECK, targetStableSwordId);
    }

    public static Result ritualBindWithTransaction(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final List<? extends SwordBondPort> ownedSwords,
        final PlayerBondCachePort playerCache,
        final RitualBindTransactionContext context,
        final long resolvedTick
    ) {
        if (
            normalizeId(ownerUuid).isBlank()
                || targetSword == null
                || ownedSwords == null
                || playerCache == null
                || context == null
                || context.snapshot() == null
                || context.request() == null
                || context.conservativeScaler() == null
                || context.mutationPort() == null
                || context.requestContext() == null
                || context.requestContext().requestState() == null
        ) {
            return Result.failure(
                ResultBranch.RITUAL_BIND,
                FailureReason.INVALID_REQUEST,
                EMPTY_VALUE
            );
        }

        final String normalizedOwnerUuid = normalizeId(ownerUuid);
        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        if (targetStableSwordId.isBlank()) {
            return Result.failure(
                ResultBranch.RITUAL_BIND,
                FailureReason.INVALID_REQUEST,
                EMPTY_VALUE
            );
        }

        final RitualRequestContext requestContext = context.requestContext();
        final RitualRequestStatePort requestState = requestContext.requestState();
        clearExpiredRitualRequest(requestState, resolvedTick);
        final String lockedSwordId = normalizeId(requestState.getLockedSwordId());
        final boolean activeRequest = isActiveRitualRequest(
            requestState,
            resolvedTick,
            lockedSwordId
        );
        if (activeRequest && !lockedSwordId.equals(targetStableSwordId)) {
            return Result.failure(
                ResultBranch.RITUAL_BIND,
                FailureReason.RITUAL_TARGET_MISMATCH,
                lockedSwordId
            );
        }
        if (activeRequest && !requestState.isExecutionPending()) {
            return Result.failure(
                ResultBranch.RITUAL_BIND,
                FailureReason.RITUAL_DUPLICATE_REQUEST,
                lockedSwordId
            );
        }

        final Result validationFailure = validateRitualBindEligibility(
            ResultBranch.RITUAL_BIND,
            normalizedOwnerUuid,
            targetSword,
            ownedSwords
        );
        if (validationFailure != null) {
            return validationFailure;
        }

        if (activeRequest) {
            requestState.markExecutionConsumed();
        } else {
            requestState.beginRitualRequest(
                targetStableSwordId,
                resolveRitualGuardUntilTick(requestContext, resolvedTick)
            );
            requestState.markExecutionConsumed();
        }

        final BenmingSwordResourceTransaction.Result consumeResult =
            BenmingSwordResourceTransaction.tryConsume(
                context.snapshot(),
                context.request(),
                context.zhenyuanCost(),
                context.conservativeScaler(),
                context.mutationPort(),
                context.mutationPort()
            );
        if (consumeResult == null || !consumeResult.success()) {
            final BenmingSwordResourceTransaction.FailureReason resourceFailureReason =
                consumeResult == null
                    ? BenmingSwordResourceTransaction.FailureReason.INVALID_REQUEST
                    : consumeResult.failureReason();
            return Result.failure(
                ResultBranch.RITUAL_BIND,
                mapRitualFailureReason(resourceFailureReason),
                targetStableSwordId,
                resourceFailureReason
            );
        }

        return completeBond(
            ResultBranch.RITUAL_BIND,
            normalizedOwnerUuid,
            normalizeId(targetSword.getBondOwnerUuid()),
            targetSword,
            playerCache,
            resolvedTick
        );
    }

    public static Result activeUnbindWithTransaction(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final ActiveUnbindTransactionContext context,
        final long resolvedTick
    ) {
        if (
            normalizeId(ownerUuid).isBlank()
                || targetSword == null
                || playerCache == null
                || context == null
                || context.snapshot() == null
                || context.request() == null
                || context.conservativeScaler() == null
                || context.mutationPort() == null
        ) {
            return Result.failure(
                ResultBranch.ACTIVE_UNBIND,
                FailureReason.INVALID_REQUEST,
                EMPTY_VALUE
            );
        }

        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        final String targetOwnerUuid = normalizeId(targetSword.getBondOwnerUuid());
        if (!normalizeId(ownerUuid).equals(targetOwnerUuid)) {
            return Result.failure(
                ResultBranch.ACTIVE_UNBIND,
                FailureReason.TARGET_NOT_BOUND_TO_PLAYER,
                targetStableSwordId
            );
        }

        final BenmingSwordResourceTransaction.Result consumeResult =
            BenmingSwordResourceTransaction.tryConsume(
                context.snapshot(),
                context.request(),
                context.zhenyuanCost(),
                context.conservativeScaler(),
                context.mutationPort()
            );
        return activeUnbind(
            ownerUuid,
            targetSword,
            playerCache,
            consumeResult,
            resolvedTick
        );
    }

    public static Result activeUnbind(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final BenmingSwordResourceTransaction.Result consumeResult,
        final long resolvedTick
    ) {
        if (
            normalizeId(ownerUuid).isBlank()
                || targetSword == null
                || playerCache == null
        ) {
            return Result.failure(
                ResultBranch.ACTIVE_UNBIND,
                FailureReason.INVALID_REQUEST,
                EMPTY_VALUE
            );
        }

        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        final String targetOwnerUuid = normalizeId(targetSword.getBondOwnerUuid());
        if (!normalizeId(ownerUuid).equals(targetOwnerUuid)) {
            return Result.failure(
                ResultBranch.ACTIVE_UNBIND,
                FailureReason.TARGET_NOT_BOUND_TO_PLAYER,
                targetStableSwordId
            );
        }

        if (consumeResult == null || !consumeResult.success()) {
            final BenmingSwordResourceTransaction.FailureReason resourceFailureReason =
                consumeResult == null
                    ? BenmingSwordResourceTransaction.FailureReason.INVALID_REQUEST
                    : consumeResult.failureReason();
            return Result.failure(
                ResultBranch.ACTIVE_UNBIND,
                FailureReason.ACTIVE_UNBIND_COST_REJECTED,
                targetStableSwordId,
                resourceFailureReason
            );
        }

        clearBond(targetSword, playerCache);
        return Result.success(ResultBranch.ACTIVE_UNBIND, targetStableSwordId);
    }

    public static Result forcedUnbind(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final long resolvedTick
    ) {
        return forcedUnbind(ownerUuid, targetSword, playerCache, resolvedTick, null);
    }

    public static Result forcedUnbind(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final long resolvedTick,
        final BacklashContext backlashContext
    ) {
        return clearWithBranch(
            ownerUuid,
            targetSword,
            playerCache,
            resolvedTick,
            ResultBranch.FORCED_UNBIND,
            backlashContext
        );
    }

    public static Result illegalDetach(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final long resolvedTick
    ) {
        return illegalDetach(ownerUuid, targetSword, playerCache, resolvedTick, null);
    }

    public static Result illegalDetach(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final long resolvedTick,
        final BacklashContext backlashContext
    ) {
        return clearWithBranch(
            ownerUuid,
            targetSword,
            playerCache,
            resolvedTick,
            ResultBranch.ILLEGAL_DETACH,
            backlashContext
        );
    }

    private static Result clearWithBranch(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final long resolvedTick,
        final ResultBranch branch,
        final BacklashContext backlashContext
    ) {
        if (
            normalizeId(ownerUuid).isBlank()
                || targetSword == null
                || playerCache == null
        ) {
            return Result.failure(branch, FailureReason.INVALID_REQUEST, EMPTY_VALUE);
        }

        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        final String targetOwnerUuid = normalizeId(targetSword.getBondOwnerUuid());
        if (!normalizeId(ownerUuid).equals(targetOwnerUuid)) {
            return Result.failure(
                branch,
                FailureReason.TARGET_NOT_BOUND_TO_PLAYER,
                targetStableSwordId
            );
        }

        clearBond(targetSword, playerCache);
        final BacklashEffect backlashEffect = resolveLightBacklash(branch, backlashContext);
        if (backlashEffect.type() == BacklashType.NONE) {
            return Result.success(branch, targetStableSwordId);
        }
        return Result.successWithBacklash(branch, targetStableSwordId, backlashEffect);
    }

    private static BacklashEffect resolveLightBacklash(
        final ResultBranch branch,
        final BacklashContext backlashContext
    ) {
        final BacklashType type = toBacklashType(branch);
        if (type == BacklashType.NONE) {
            return BacklashEffect.none();
        }
        if (
            backlashContext == null
                || backlashContext.snapshot() == null
                || backlashContext.mutationPort() == null
                || backlashContext.cooldownPort() == null
        ) {
            return new BacklashEffect(type, configuredLightBacklashAmount());
        }

        final String cooldownKey = backlashContext.cooldownKey().isBlank()
            ? LIGHT_BACKLASH_COOLDOWN_KEY
            : backlashContext.cooldownKey();
        if (backlashContext.cooldownPort().get(cooldownKey) > 0) {
            return BacklashEffect.none();
        }

        final double zhenyuanLoss = Math.min(
            normalizeNonNegative(backlashContext.snapshot().zhenyuan()),
            LIGHT_BACKLASH_ZHENYUAN_COST
        );
        final double niantouLoss = Math.min(
            normalizeNonNegative(backlashContext.snapshot().niantou()),
            LIGHT_BACKLASH_NIANTOU_COST
        );
        final double hunpoLoss = Math.min(
            normalizeNonNegative(backlashContext.snapshot().hunpo()),
            LIGHT_BACKLASH_HUNPO_COST
        );

        if (zhenyuanLoss > ZERO_BACKLASH) {
            backlashContext.mutationPort().spendZhenyuan(zhenyuanLoss);
        }
        if (niantouLoss > ZERO_BACKLASH) {
            backlashContext.mutationPort().spendNiantou(niantouLoss);
        }
        if (hunpoLoss > ZERO_BACKLASH) {
            backlashContext.mutationPort().spendHunpo(hunpoLoss);
        }

        final int cooldownTicks = backlashContext.cooldownTicks() <= 0
            ? LIGHT_BACKLASH_COOLDOWN_TICKS
            : backlashContext.cooldownTicks();
        backlashContext.cooldownPort().set(cooldownKey, cooldownTicks);
        return new BacklashEffect(type, zhenyuanLoss + niantouLoss + hunpoLoss);
    }

    private static double configuredLightBacklashAmount() {
        return LIGHT_BACKLASH_ZHENYUAN_COST
            + LIGHT_BACKLASH_NIANTOU_COST
            + LIGHT_BACKLASH_HUNPO_COST;
    }

    private static BacklashType toBacklashType(final ResultBranch branch) {
        if (branch == ResultBranch.FORCED_UNBIND) {
            return BacklashType.FORCED_UNBIND_LIGHT;
        }
        if (branch == ResultBranch.ILLEGAL_DETACH) {
            return BacklashType.ILLEGAL_DETACH_LIGHT;
        }
        return BacklashType.NONE;
    }

    private static Result completeBond(
        final ResultBranch branch,
        final String normalizedOwnerUuid,
        final String targetOwnerUuid,
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache,
        final long resolvedTick
    ) {
        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        if (!normalizedOwnerUuid.equals(normalizeId(targetOwnerUuid))) {
            targetSword.setBondResonance(ZERO_RESONANCE);
        }
        targetSword.setBondOwnerUuid(normalizedOwnerUuid);
        playerCache.updateBondCache(targetStableSwordId, resolvedTick);
        return Result.success(branch, targetStableSwordId);
    }

    private static void clearBond(
        final SwordBondPort targetSword,
        final PlayerBondCachePort playerCache
    ) {
        targetSword.setBondOwnerUuid(EMPTY_VALUE);
        targetSword.setBondResonance(ZERO_RESONANCE);
        playerCache.clearBondCache();
    }

    private static BoundScanResult scanBoundSword(
        final String ownerUuid,
        final List<? extends SwordBondPort> ownedSwords,
        final String ignoredStableSwordId
    ) {
        String firstBoundSwordId = EMPTY_VALUE;
        boolean multipleBound = false;
        final String ignoredId = normalizeId(ignoredStableSwordId);

        for (SwordBondPort sword : ownedSwords) {
            if (sword == null) {
                continue;
            }
            final String stableSwordId = normalizeId(sword.getStableSwordId());
            if (stableSwordId.isBlank() || stableSwordId.equals(ignoredId)) {
                continue;
            }
            if (!ownerUuid.equals(normalizeId(sword.getBondOwnerUuid()))) {
                continue;
            }
            if (firstBoundSwordId.isBlank()) {
                firstBoundSwordId = stableSwordId;
                continue;
            }
            multipleBound = true;
            break;
        }

        return new BoundScanResult(firstBoundSwordId, multipleBound);
    }

    private static SwordBondPort findByStableSwordId(
        final List<? extends SwordBondPort> ownedSwords,
        final String stableSwordId
    ) {
        final String normalizedStableSwordId = normalizeId(stableSwordId);
        for (SwordBondPort sword : ownedSwords) {
            if (sword == null) {
                continue;
            }
            if (normalizeId(sword.getStableSwordId()).equals(normalizedStableSwordId)) {
                return sword;
            }
        }
        return null;
    }

    private static boolean isValidRequest(
        final String ownerUuid,
        final List<? extends SwordBondPort> ownedSwords,
        final PlayerBondCachePort playerCache
    ) {
        return !normalizeId(ownerUuid).isBlank()
            && ownedSwords != null
            && playerCache != null;
    }

    private static boolean isValidRitualRequest(
        final String ownerUuid,
        final SwordBondPort targetSword,
        final List<? extends SwordBondPort> ownedSwords,
        final PlayerBondCachePort playerCache,
        final RitualRequestContext requestContext
    ) {
        return !normalizeId(ownerUuid).isBlank()
            && targetSword != null
            && ownedSwords != null
            && playerCache != null
            && requestContext != null
            && requestContext.requestState() != null;
    }

    private static Result validateRitualBindEligibility(
        final ResultBranch branch,
        final String normalizedOwnerUuid,
        final SwordBondPort targetSword,
        final List<? extends SwordBondPort> ownedSwords
    ) {
        final String targetStableSwordId = normalizeId(targetSword.getStableSwordId());
        if (targetStableSwordId.isBlank()) {
            return Result.failure(branch, FailureReason.INVALID_REQUEST, EMPTY_VALUE);
        }

        final String targetOwnerUuid = normalizeId(targetSword.getBondOwnerUuid());
        if (normalizedOwnerUuid.equals(targetOwnerUuid)) {
            return Result.failure(
                branch,
                FailureReason.PLAYER_ALREADY_HAS_BONDED_SWORD,
                targetStableSwordId
            );
        }
        if (!targetOwnerUuid.isBlank() && !targetOwnerUuid.equals(normalizedOwnerUuid)) {
            return Result.failure(
                branch,
                FailureReason.TARGET_BOUND_TO_OTHER_PLAYER,
                targetStableSwordId
            );
        }

        final BoundScanResult scanResult = scanBoundSword(
            normalizedOwnerUuid,
            ownedSwords,
            targetStableSwordId
        );
        if (scanResult.multipleBound()) {
            return Result.failure(
                branch,
                FailureReason.MULTIPLE_BONDED_SWORDS,
                scanResult.boundSwordId()
            );
        }
        if (!scanResult.boundSwordId().isBlank()) {
            return Result.failure(
                branch,
                FailureReason.PLAYER_ALREADY_HAS_BONDED_SWORD,
                scanResult.boundSwordId()
            );
        }
        return null;
    }

    private static void clearExpiredRitualRequest(
        final RitualRequestStatePort requestState,
        final long resolvedTick
    ) {
        if (requestState == null) {
            return;
        }
        final String lockedSwordId = normalizeId(requestState.getLockedSwordId());
        final long lockedUntilTick = Math.max(0L, requestState.getLockedUntilTick());
        if (lockedSwordId.isBlank() || resolvedTick >= lockedUntilTick) {
            requestState.clearRitualRequest();
        }
    }

    private static boolean isActiveRitualRequest(
        final RitualRequestStatePort requestState,
        final long resolvedTick,
        final String lockedSwordId
    ) {
        return requestState != null
            && !normalizeId(lockedSwordId).isBlank()
            && resolvedTick < Math.max(0L, requestState.getLockedUntilTick());
    }

    private static long resolveRitualGuardUntilTick(
        final RitualRequestContext requestContext,
        final long resolvedTick
    ) {
        final long guardTicks = Math.max(1L, requestContext.duplicateGuardTicks());
        return Math.max(0L, resolvedTick) + guardTicks;
    }

    private static FailureReason mapRitualFailureReason(
        final BenmingSwordResourceTransaction.FailureReason resourceFailureReason
    ) {
        if (resourceFailureReason == null) {
            return FailureReason.INVALID_REQUEST;
        }
        return switch (resourceFailureReason) {
            case INSUFFICIENT_ZHENYUAN,
                INSUFFICIENT_NIANTOU,
                INSUFFICIENT_HUNPO -> FailureReason.RITUAL_RESOURCES_INSUFFICIENT;
            case RITUAL_LOCK_ACTIVE,
                BURST_COOLDOWN_ACTIVE -> FailureReason.RITUAL_COOLDOWN_ACTIVE;
            case ILLEGAL_SWORD_STATE,
                OVERLOAD_LIMIT_EXCEEDED,
                RESOURCE_WRITE_FAILED,
                PHASE_STATE_WRITE_FAILED,
                ATOMIC_MUTATION_UNSUPPORTED -> FailureReason.RITUAL_STATE_ILLEGAL;
            case NONE,
                INVALID_ENTITY,
                INVALID_REQUEST -> FailureReason.INVALID_REQUEST;
        };
    }

    private static double normalizeNonNegative(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= ZERO_BACKLASH) {
            return ZERO_BACKLASH;
        }
        return value;
    }

    private static String normalizeId(final String value) {
        if (value == null || value.isBlank()) {
            return EMPTY_VALUE;
        }
        return value;
    }

    private record BoundScanResult(String boundSwordId, boolean multipleBound) {}
}
