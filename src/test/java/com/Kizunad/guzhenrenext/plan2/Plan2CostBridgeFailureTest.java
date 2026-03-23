package com.Kizunad.guzhenrenext.plan2;

import com.Kizunad.guzhenrenext.plan2.logic.util.Plan2CostHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class Plan2CostBridgeFailureTest {

    @Test
    void shouldFailAndKeepStateWhenAnyResourceInsufficient() {
        final Plan2CostHelper.CostScaler scaler = new Plan2CostHelper.CostScaler() {
            @Override
            public double scaleOther(final Object user, final double baseCost) {
                return baseCost;
            }

            @Override
            public double scaleZhenYuan(final Object user, final double zhenyuanBaseCost) {
                return zhenyuanBaseCost;
            }
        };
        final FakeGateway gateway = new FakeGateway(5.0, 100.0, 100.0, 100.0);
        final Plan2CostHelper.BaseCosts baseCosts =
            new Plan2CostHelper.BaseCosts(10.0, 1.0, 1.0, 1.0);

        final Plan2CostHelper.ConsumeResult result =
            Plan2CostHelper.tryConsume(null, baseCosts, scaler, gateway);

        assertFalse(result.success());
        assertEquals(Plan2CostHelper.INSUFFICIENT_RESOURCE_MESSAGE, result.message());
        assertEquals(5.0, gateway.niantou, 0.00001);
        assertEquals(100.0, gateway.jingli, 0.00001);
        assertEquals(100.0, gateway.hunpo, 0.00001);
        assertEquals(100.0, gateway.zhenyuan, 0.00001);
    }

    @Test
    void shouldFailWithConsistentMessageWhenInputInvalid() {
        final Plan2CostHelper.ConsumeResult result = Plan2CostHelper.hasEnough(
            null,
            null,
            null,
            null
        );
        assertFalse(result.success());
        assertEquals(Plan2CostHelper.INSUFFICIENT_RESOURCE_MESSAGE, result.message());
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
