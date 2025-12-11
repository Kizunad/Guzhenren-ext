package com.Kizunad.guzhenrenext.kongqiao.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模组蛊虫逻辑注册中心。
 * <p>
 * 在此处实例化具体的 IGuEffect 实现并注册到 {@link GuEffectRegistry}。
 * </p>
 */
public final class GuModEffects {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuModEffects.class);
    private static boolean registered = false;

    private GuModEffects() {}

    /**
     * 注册所有内置的蛊虫效果。
     * 应在模组初始化阶段调用。
     */
    public static void registerAll() {
        if (registered) {
            return;
        }

        LOGGER.info("开始注册蛊虫效果逻辑...");

        // 注册各流派逻辑
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierOne.TierOneHunDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo.TierTwoHunDaoRegistry
            .registerAll();

        registered = true;
    }
}
