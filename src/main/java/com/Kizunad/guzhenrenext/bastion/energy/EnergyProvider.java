package com.Kizunad.guzhenrenext.bastion.energy;

import java.util.OptionalDouble;

/**
 * 能源提供者：用于把“环境/节点/布局”等因素转成资源池增长加成。
 * <p>
 * 本接口只表达<strong>加成的形态</strong>：
 * <ul>
 *   <li>{@code poolGainMultiplierAdd}：资源池增长的额外倍率增量（加法语义）。</li>
 *   <li>{@code poolGainFlatAdd}：资源池增长的额外平坦增量（加法语义）。</li>
 * </ul>
 * 两者可以只提供其一，也可以同时提供；不提供的一项用 {@link OptionalDouble#empty()} 表示。
 * </p>
 * <p>
 * 说明：这里使用“增量（Add）”而非“最终倍率”，目的是让多个能源来源叠加时规则更清晰。
 * 具体叠加策略由 BastionTicker 在后续步骤中统一处理。
 * </p>
 */
public interface EnergyProvider {

    /**
     * 获取本能源来源对资源池增长的加成。
     *
     * @return 加成结果（可只含倍率/只含平坦/两者都有）
     */
    PoolGainBonus getPoolGainBonus();

    /**
     * 资源池增长加成。
     * <p>
     * 两个字段都采用“加法增量”语义：
     * <ul>
     *   <li>倍率增量：例如 0.25 表示额外 +25%（最终倍率的组合由后续逻辑决定）。</li>
     *   <li>平坦增量：例如 3.0 表示每刻间隔额外 +3.0 的资源池获得量（单位同 pool）。</li>
     * </ul>
     * </p>
     */
    record PoolGainBonus(OptionalDouble poolGainMultiplierAdd, OptionalDouble poolGainFlatAdd) {

        /** 空加成。 */
        public static final PoolGainBonus EMPTY = new PoolGainBonus(OptionalDouble.empty(), OptionalDouble.empty());

        /**
         * 仅提供倍率增量。
         *
         * @param multiplierAdd 倍率增量（加法语义）
         * @return 仅包含倍率的加成
         */
        public static PoolGainBonus multiplierAdd(double multiplierAdd) {
            return new PoolGainBonus(OptionalDouble.of(multiplierAdd), OptionalDouble.empty());
        }

        /**
         * 仅提供平坦增量。
         *
         * @param flatAdd 平坦增量（加法语义）
         * @return 仅包含平坦增量的加成
         */
        public static PoolGainBonus flatAdd(double flatAdd) {
            return new PoolGainBonus(OptionalDouble.empty(), OptionalDouble.of(flatAdd));
        }

        /**
         * 同时提供倍率与平坦增量。
         *
         * @param multiplierAdd 倍率增量（加法语义）
         * @param flatAdd       平坦增量（加法语义）
         * @return 同时包含两者的加成
         */
        public static PoolGainBonus both(double multiplierAdd, double flatAdd) {
            return new PoolGainBonus(OptionalDouble.of(multiplierAdd), OptionalDouble.of(flatAdd));
        }
    }
}
