package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree.LingHunGuAntelopeHangsHornsEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree.XiongHunGuEarthShatterEffect;

public class TierThreeHunDaoRegistry {

    public static void registerAll() {
        GuEffectRegistry.register(new GuiYanGuEffect());
        GuEffectRegistry.register(new GuiQiGuEffect());
        GuEffectRegistry.register(new BingPoGuEffect());
        GuEffectRegistry.register(new GuiYanGuGhostEyeEffect());
        GuEffectRegistry.register(new GuiLianGuSoulImpactEffect());
        GuEffectRegistry.register(new LangHunGuNightWalkerEffect());
        GuEffectRegistry.register(new LangHunGuPackHuntEffect());
        GuEffectRegistry.register(new LangHunGuGreedyDevourEffect());
        GuEffectRegistry.register(new LingHunGuSkyStepEffect());
        GuEffectRegistry.register(new LingHunGuSpiritualIntuitionEffect());
        GuEffectRegistry.register(new LingHunGuAntelopeHangsHornsEffect());
        GuEffectRegistry.register(new XiongHunGuThickHideEffect());
        GuEffectRegistry.register(new XiongHunGuEarthShatterEffect());
    }
}
