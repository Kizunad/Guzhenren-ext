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
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree.TierThreeHunDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFour.TierFourHunDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFive.TierFiveHunDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierOne.TierOneZhiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierTwo.TierTwoZhiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierThree.TierThreeZhiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierFour.TierFourZhiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierFive.TierFiveZhiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierOne.TierOneBianHuaDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierTwo.TierTwoBianHuaDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierThree.TierThreeBianHuaDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierFour.TierFourBianHuaDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierFive.TierFiveBianHuaDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierOne.TierOneShiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierTwo.TierTwoShiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierThree.TierThreeShiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierFour.TierFourShiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierOne.TierOneGuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierTwo.TierTwoGuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierThree.TierThreeGuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierFour.TierFourGuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierFive.TierFiveGuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierOne.TierOneFengDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierTwo.TierTwoFengDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierThree.TierThreeFengDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierFour.TierFourFengDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierFive.TierFiveFengDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierOne.TierOneGuangDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierThree.TierThreeGuangDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierFour.TierFourGuangDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierFive.TierFiveGuangDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierOne.TierOneBingXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierTwo.TierTwoBingXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierThree.TierThreeBingXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierFour.TierFourBingXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierFive.TierFiveBingXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierOne.TierOneYuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierTwo.TierTwoYuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierThree.TierThreeYuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierFour.TierFourYuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierFive.TierFiveYuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierOne.TierOneXingDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierTwo.TierTwoXingDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierThree.TierThreeXingDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierFour.TierFourXingDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierFive.TierFiveXingDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierOne.TierOneMuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierTwo.TierTwoMuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierThree.TierThreeMuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierFour.TierFourMuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierFive.TierFiveMuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierOne.TierOneLiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierTwo.TierTwoLiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierThree.TierThreeLiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierFour.TierFourLiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierFive.TierFiveLiDaoRegistry
            .registerAll();

        registered = true;
    }
}
