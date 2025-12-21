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

        // 注册各流派逻辑（按流派拆分，避免单方法过长触发 Checkstyle）
        registerHunDao();
        registerZhiDao();
        registerJianDao();
        registerDaoDao();
        registerJinDao();
        registerRenDao();
        registerBianHuaDao();
        registerShiDao();
        registerGuDao();
        registerFengDao();
        registerYunDao();
        registerGuangDao();
        registerBingXueDao();
        registerXueDao();
        registerLianDao();
        registerLvDao();
        registerTianDao();
        registerTouDao();
        registerXinDao();
        registerYingDao();
        registerYuDao();
        registerNuDao();
        registerXingDao();
        registerMuDao();
        registerHuoDao();
        registerLiDao();
        registerLeiDao();
        registerDuDao();
        registerShuiDao();
        registerYueDao();

        registered = true;
    }

    private static void registerHunDao() {
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
    }

    private static void registerZhiDao() {
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
    }

    private static void registerJianDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierOne.TierOneJianDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierTwo.TierTwoJianDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierThree.TierThreeJianDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierFour.TierFourJianDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.tierFive.TierFiveJianDaoRegistry
            .registerAll();
    }

    private static void registerDaoDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.tierThree.TierThreeDaoDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.tierFour.TierFourDaoDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.tierFive.TierFiveDaoDaoRegistry
            .registerAll();
    }

    private static void registerJinDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierOne.TierOneJinDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierTwo.TierTwoJinDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierThree.TierThreeJinDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierFour.TierFourJinDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierFive.TierFiveJinDaoRegistry
            .registerAll();
    }

    private static void registerRenDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierOne.TierOneRenDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierTwo.TierTwoRenDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierThree.TierThreeRenDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierFour.TierFourRenDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierFive.TierFiveRenDaoRegistry
            .registerAll();
    }

    private static void registerBianHuaDao() {
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
    }

    private static void registerShiDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierOne.TierOneShiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierTwo.TierTwoShiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierThree.TierThreeShiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierFour.TierFourShiDaoRegistry
            .registerAll();
    }

    private static void registerGuDao() {
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
    }

    private static void registerFengDao() {
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
    }

    private static void registerYunDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yundao.tierThree.TierThreeYunDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yundao.tierFour.TierFourYunDaoRegistry
            .registerAll();
    }

    private static void registerGuangDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierOne.TierOneGuangDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierThree.TierThreeGuangDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierFour.TierFourGuangDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierFive.TierFiveGuangDaoRegistry
            .registerAll();
    }

    private static void registerBingXueDao() {
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
    }

    private static void registerXueDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierOne.TierOneXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierTwo.TierTwoXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierThree.TierThreeXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierFour.TierFourXueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.tierFive.TierFiveXueDaoRegistry
            .registerAll();
    }

    private static void registerLianDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.liandao.LianDaoRegistry
            .registerAll();
    }

    private static void registerLvDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lvdao.LvDaoRegistry
            .registerAll();
    }

    private static void registerTianDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.tiandao.TianDaoRegistry
            .registerAll();
    }

    private static void registerTouDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.toudao.TouDaoRegistry
            .registerAll();
    }

    private static void registerXinDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xindao.XinDaoRegistry
            .registerAll();
    }

    private static void registerYingDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yingdao.YingDaoRegistry
            .registerAll();
    }

    private static void registerYuDao() {
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
    }

    private static void registerNuDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierOne.TierOneNuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierTwo.TierTwoNuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierThree.TierThreeNuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierFour.TierFourNuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.nudao.tierFive.TierFiveNuDaoRegistry
            .registerAll();
    }

    private static void registerXingDao() {
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
    }

    private static void registerMuDao() {
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
    }

    private static void registerHuoDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierOne.TierOneHuoDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierTwo.TierTwoHuoDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierThree.TierThreeHuoDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierFour.TierFourHuoDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.tierFive.TierFiveHuoDaoRegistry
            .registerAll();
    }

    private static void registerLiDao() {
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
    }

    private static void registerLeiDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierOne.TierOneLeiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierTwo.TierTwoLeiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierThree.TierThreeLeiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierFive.TierFiveLeiDaoRegistry
            .registerAll();
    }

    private static void registerDuDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierOne.TierOneDuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierTwo.TierTwoDuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierThree.TierThreeDuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierFour.TierFourDuDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierFive.TierFiveDuDaoRegistry
            .registerAll();
    }

    private static void registerShuiDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierOne.TierOneShuiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierTwo.TierTwoShuiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierThree.TierThreeShuiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierFour.TierFourShuiDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierFive.TierFiveShuiDaoRegistry
            .registerAll();
    }

    private static void registerYueDao() {
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierOne.TierOneYueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierTwo.TierTwoYueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierThree.TierThreeYueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierFour.TierFourYueDaoRegistry
            .registerAll();
        com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierFive.TierFiveYueDaoRegistry
            .registerAll();
    }
}
