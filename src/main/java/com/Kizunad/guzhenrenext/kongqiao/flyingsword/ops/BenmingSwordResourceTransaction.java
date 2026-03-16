package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ZhuanCostHelper;
import net.minecraft.world.entity.LivingEntity;

public final class BenmingSwordResourceTransaction {

    private static final double ZERO = 0.0;

    private BenmingSwordResourceTransaction() {}

    public enum FailureReason {
        NONE,
        INVALID_ENTITY,
        INVALID_REQUEST,
        INSUFFICIENT_ZHENYUAN,
        INSUFFICIENT_NIANTOU,
        INSUFFICIENT_HUNPO
    }

    public record Request(double zhenyuanBaseCost, double niantouBaseCost, double hunpoBaseCost) {

        public Request {
            zhenyuanBaseCost = normalizeNonNegative(zhenyuanBaseCost);
            niantouBaseCost = normalizeNonNegative(niantouBaseCost);
            hunpoBaseCost = normalizeNonNegative(hunpoBaseCost);
        }
    }

    public record Result(
        boolean success,
        FailureReason failureReason,
        double zhenyuanCost,
        double niantouCost,
        double hunpoCost
    ) {

        public static Result success(
            final double zhenyuanCost,
            final double niantouCost,
            final double hunpoCost
        ) {
            return new Result(true, FailureReason.NONE, zhenyuanCost, niantouCost, hunpoCost);
        }

        public static Result failure(
            final FailureReason failureReason,
            final double zhenyuanCost,
            final double niantouCost,
            final double hunpoCost
        ) {
            return new Result(false, failureReason, zhenyuanCost, niantouCost, hunpoCost);
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
    }

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
            new BridgeResourceMutationPort(entity)
        );
    }

    public static Result tryConsume(
        final CultivationSnapshot snapshot,
        final Request request,
        final double zhenyuanCost,
        final CostScaler conservativeScaler,
        final ResourceMutationPort mutationPort
    ) {
        if (request == null || snapshot == null || conservativeScaler == null || mutationPort == null) {
            return Result.failure(FailureReason.INVALID_REQUEST, ZERO, ZERO, ZERO);
        }

        final double safeZhenyuanCost = normalizeNonNegative(zhenyuanCost);
        final double niantouCost = normalizeNonNegative(
            conservativeScaler.scaleCost(request.niantouBaseCost())
        );
        final double hunpoCost = normalizeNonNegative(
            conservativeScaler.scaleCost(request.hunpoBaseCost())
        );

        if (snapshot.zhenyuan() < safeZhenyuanCost) {
            return Result.failure(
                FailureReason.INSUFFICIENT_ZHENYUAN,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost
            );
        }
        if (snapshot.niantou() < niantouCost) {
            return Result.failure(
                FailureReason.INSUFFICIENT_NIANTOU,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost
            );
        }
        if (snapshot.hunpo() < hunpoCost) {
            return Result.failure(
                FailureReason.INSUFFICIENT_HUNPO,
                safeZhenyuanCost,
                niantouCost,
                hunpoCost
            );
        }

        if (safeZhenyuanCost > ZERO) {
            mutationPort.spendZhenyuan(safeZhenyuanCost);
        }
        if (niantouCost > ZERO) {
            mutationPort.spendNiantou(niantouCost);
        }
        if (hunpoCost > ZERO) {
            mutationPort.spendHunpo(hunpoCost);
        }

        return Result.success(safeZhenyuanCost, niantouCost, hunpoCost);
    }

    private static double normalizeNonNegative(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= ZERO) {
            return ZERO;
        }
        return value;
    }

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
    }
}
