package com.Kizunad.guzhenrenext.plan2.logic.util;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ZhuanCostHelper;
import net.minecraft.world.entity.LivingEntity;

public final class Plan2CostHelper {

    public static final String INSUFFICIENT_RESOURCE_MESSAGE = "资源不足。";

    private Plan2CostHelper() {
    }

    public record BaseCosts(
        double niantouBaseCost,
        double jingliBaseCost,
        double hunpoBaseCost,
        double zhenyuanBaseCost
    ) {
    }

    public record ActualCosts(
        double niantouCost,
        double jingliCost,
        double hunpoCost,
        double zhenyuanCost
    ) {
    }

    public record ConsumeResult(boolean success, String message) {
        public static ConsumeResult ok() {
            return new ConsumeResult(true, "");
        }

        public static ConsumeResult insufficient() {
            return new ConsumeResult(false, INSUFFICIENT_RESOURCE_MESSAGE);
        }
    }

    public interface CostScaler {
        double scaleOther(Object user, double baseCost);

        double scaleZhenYuan(Object user, double zhenyuanBaseCost);
    }

    public interface ResourceGateway {
        double getNianTou(Object user);

        double getJingLi(Object user);

        double getHunPo(Object user);

        double getZhenYuan(Object user);

        void modifyNianTou(Object user, double delta);

        void modifyJingLi(Object user, double delta);

        void modifyHunPo(Object user, double delta);

        void modifyZhenYuan(Object user, double delta);
    }

    public static ActualCosts scaleCosts(
        final Object user,
        final BaseCosts baseCosts,
        final CostScaler scaler
    ) {
        final BaseCosts normalized = normalize(baseCosts);
        return new ActualCosts(
            clampNonNegativeFinite(scaler.scaleOther(user, normalized.niantouBaseCost())),
            clampNonNegativeFinite(scaler.scaleOther(user, normalized.jingliBaseCost())),
            clampNonNegativeFinite(scaler.scaleOther(user, normalized.hunpoBaseCost())),
            clampNonNegativeFinite(scaler.scaleZhenYuan(user, normalized.zhenyuanBaseCost()))
        );
    }

    public static ConsumeResult hasEnough(
        final Object user,
        final BaseCosts baseCosts,
        final CostScaler scaler,
        final ResourceGateway gateway
    ) {
        if (baseCosts == null || scaler == null || gateway == null) {
            return ConsumeResult.insufficient();
        }
        final ActualCosts costs = scaleCosts(user, baseCosts, scaler);
        if (costs.niantouCost() > 0.0 && gateway.getNianTou(user) < costs.niantouCost()) {
            return ConsumeResult.insufficient();
        }
        if (costs.jingliCost() > 0.0 && gateway.getJingLi(user) < costs.jingliCost()) {
            return ConsumeResult.insufficient();
        }
        if (costs.hunpoCost() > 0.0 && gateway.getHunPo(user) < costs.hunpoCost()) {
            return ConsumeResult.insufficient();
        }
        if (costs.zhenyuanCost() > 0.0 && gateway.getZhenYuan(user) < costs.zhenyuanCost()) {
            return ConsumeResult.insufficient();
        }
        return ConsumeResult.ok();
    }

    public static ConsumeResult tryConsume(
        final Object user,
        final BaseCosts baseCosts,
        final CostScaler scaler,
        final ResourceGateway gateway
    ) {
        final ConsumeResult enough = hasEnough(user, baseCosts, scaler, gateway);
        if (!enough.success()) {
            return enough;
        }

        final ActualCosts costs = scaleCosts(user, baseCosts, scaler);
        if (costs.niantouCost() > 0.0) {
            gateway.modifyNianTou(user, -costs.niantouCost());
        }
        if (costs.jingliCost() > 0.0) {
            gateway.modifyJingLi(user, -costs.jingliCost());
        }
        if (costs.hunpoCost() > 0.0) {
            gateway.modifyHunPo(user, -costs.hunpoCost());
        }
        if (costs.zhenyuanCost() > 0.0) {
            gateway.modifyZhenYuan(user, -costs.zhenyuanCost());
        }
        return ConsumeResult.ok();
    }

    public static final CostScaler DEFAULT_SCALER = new CostScaler() {
        @Override
        public double scaleOther(final Object user, final double baseCost) {
            return ZhuanCostHelper.scaleCost(asLivingEntity(user), baseCost);
        }

        @Override
        public double scaleZhenYuan(final Object user, final double zhenyuanBaseCost) {
            return ZhenYuanHelper.calculateGuCost(asLivingEntity(user), zhenyuanBaseCost);
        }
    };

    public static final ResourceGateway DEFAULT_GATEWAY = new ResourceGateway() {
        @Override
        public double getNianTou(final Object user) {
            return NianTouHelper.getAmount(asLivingEntity(user));
        }

        @Override
        public double getJingLi(final Object user) {
            return JingLiHelper.getAmount(asLivingEntity(user));
        }

        @Override
        public double getHunPo(final Object user) {
            return HunPoHelper.getAmount(asLivingEntity(user));
        }

        @Override
        public double getZhenYuan(final Object user) {
            return ZhenYuanHelper.getAmount(asLivingEntity(user));
        }

        @Override
        public void modifyNianTou(final Object user, final double delta) {
            NianTouHelper.modify(asLivingEntity(user), delta);
        }

        @Override
        public void modifyJingLi(final Object user, final double delta) {
            JingLiHelper.modify(asLivingEntity(user), delta);
        }

        @Override
        public void modifyHunPo(final Object user, final double delta) {
            HunPoHelper.modify(asLivingEntity(user), delta);
        }

        @Override
        public void modifyZhenYuan(final Object user, final double delta) {
            ZhenYuanHelper.modify(asLivingEntity(user), delta);
        }
    };

    private static BaseCosts normalize(final BaseCosts baseCosts) {
        if (baseCosts == null) {
            return new BaseCosts(0.0, 0.0, 0.0, 0.0);
        }
        return new BaseCosts(
            clampNonNegativeFinite(baseCosts.niantouBaseCost()),
            clampNonNegativeFinite(baseCosts.jingliBaseCost()),
            clampNonNegativeFinite(baseCosts.hunpoBaseCost()),
            clampNonNegativeFinite(baseCosts.zhenyuanBaseCost())
        );
    }

    private static double clampNonNegativeFinite(final double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            return 0.0;
        }
        return value;
    }

    private static LivingEntity asLivingEntity(final Object user) {
        if (user instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }
}
