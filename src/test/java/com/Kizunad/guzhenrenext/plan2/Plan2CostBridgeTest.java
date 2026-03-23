package com.Kizunad.guzhenrenext.plan2;

import com.Kizunad.guzhenrenext.plan2.logic.util.Plan2CostHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class Plan2CostBridgeTest {

    @Test
    void shouldConsumeWithBridgeScalingAndClampOnHappyPath() {
        final Plan2CostHelper.CostScaler scaler = new Plan2CostHelper.CostScaler() {
            @Override
            public double scaleOther(final Object user, final double baseCost) {
                return baseCost / 2.0;
            }

            @Override
            public double scaleZhenYuan(final Object user, final double zhenyuanBaseCost) {
                return zhenyuanBaseCost / 4.0;
            }
        };
        final FakeGateway gateway = new FakeGateway(100.0, 60.0, 40.0, 200.0);
        final Plan2CostHelper.BaseCosts baseCosts =
            new Plan2CostHelper.BaseCosts(20.0, 10.0, 8.0, 80.0);

        final Plan2CostHelper.ConsumeResult result =
            Plan2CostHelper.tryConsume(null, baseCosts, scaler, gateway);

        assertTrue(result.success());
        assertEquals("", result.message());
        assertEquals(90.0, gateway.niantou, 0.00001);
        assertEquals(55.0, gateway.jingli, 0.00001);
        assertEquals(36.0, gateway.hunpo, 0.00001);
        assertEquals(180.0, gateway.zhenyuan, 0.00001);
    }

    @Test
    void shouldClampInvalidValuesToZero() {
        final Plan2CostHelper.CostScaler scaler = new Plan2CostHelper.CostScaler() {
            @Override
            public double scaleOther(final Object user, final double baseCost) {
                return Double.NaN;
            }

            @Override
            public double scaleZhenYuan(final Object user, final double zhenyuanBaseCost) {
                return -999.0;
            }
        };
        final Plan2CostHelper.ActualCosts costs = Plan2CostHelper.scaleCosts(
            null,
            new Plan2CostHelper.BaseCosts(Double.POSITIVE_INFINITY, -1.0, Double.NaN, 10.0),
            scaler
        );

        assertEquals(0.0, costs.niantouCost(), 0.00001);
        assertEquals(0.0, costs.jingliCost(), 0.00001);
        assertEquals(0.0, costs.hunpoCost(), 0.00001);
        assertEquals(0.0, costs.zhenyuanCost(), 0.00001);
    }

    private static final class FakeGateway implements Plan2CostHelper.ResourceGateway {
        private double niantou;
        private double jingli;
        private double hunpo;
        private double zhenyuan;

        private FakeGateway(
            final double niantou,
            final double jingli,
            final double hunpo,
            final double zhenyuan
        ) {
            this.niantou = niantou;
            this.jingli = jingli;
            this.hunpo = hunpo;
            this.zhenyuan = zhenyuan;
        }

        @Override
        public double getNianTou(final Object user) {
            return niantou;
        }

        @Override
        public double getJingLi(final Object user) {
            return jingli;
        }

        @Override
        public double getHunPo(final Object user) {
            return hunpo;
        }

        @Override
        public double getZhenYuan(final Object user) {
            return zhenyuan;
        }

        @Override
        public void modifyNianTou(final Object user, final double delta) {
            niantou += delta;
        }

        @Override
        public void modifyJingLi(final Object user, final double delta) {
            jingli += delta;
        }

        @Override
        public void modifyHunPo(final Object user, final double delta) {
            hunpo += delta;
        }

        @Override
        public void modifyZhenYuan(final Object user, final double delta) {
            zhenyuan += delta;
        }
    }
}
