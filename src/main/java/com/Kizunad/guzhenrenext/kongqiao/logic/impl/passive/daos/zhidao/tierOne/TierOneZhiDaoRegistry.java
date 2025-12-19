package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne.XiaoHuiGuGuidedIdentifyEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne.XiaoZhiGuFlashAssistIdentifyEffect;

/**
 * 一转智道蛊虫效果注册。
 */
public class TierOneZhiDaoRegistry {

    private TierOneZhiDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new XiaoZhiGuEnlightenEffect());
        GuEffectRegistry.register(new XiaoZhiGuFlashAssistIdentifyEffect());
        GuEffectRegistry.register(new XiaoHuiGuFrugalIdentifyEffect());
        GuEffectRegistry.register(new XiaoHuiGuGuidedIdentifyEffect());
    }
}
