package com.Kizunad.guzhenrenext.kongqiao.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模组杀招逻辑注册中心。
 * <p>
 * 在此处实例化具体的 IShazhaoEffect 实现并注册到 {@link ShazhaoEffectRegistry}。
 * </p>
 */
public final class ShazhaoModEffects {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        ShazhaoModEffects.class
    );
    private static boolean registered = false;

    private ShazhaoModEffects() {}

    /**
     * 注册所有内置的杀招效果。
     * 应在模组初始化阶段调用。
     */
    public static void registerAll() {
        if (registered) {
            return;
        }

        LOGGER.info("开始注册杀招效果逻辑...");

        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.shazhao.HunDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.shazhao.ShuiDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao.BingXueDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.shazhao.HuoDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.shazhao.LeiDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.shazhao.BianHuaDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao.JianDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.shazhao.LiDaoShazhaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.shazhao.XueDaoShazhaoRegistry
            .registerAll();

        registered = true;
    }
}
