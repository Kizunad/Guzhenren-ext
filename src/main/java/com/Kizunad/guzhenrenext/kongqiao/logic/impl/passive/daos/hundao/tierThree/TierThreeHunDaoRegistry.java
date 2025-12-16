package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;

public class TierThreeHunDaoRegistry {
    
    public static void registerAll() {
        GuEffectRegistry.register(new GuiYanGuEffect());
        GuEffectRegistry.register(new GuiQiGuEffect());
        GuEffectRegistry.register(new BingPoGuEffect());
        GuEffectRegistry.register(new LangHunGuNightWalkerEffect());
        GuEffectRegistry.register(new LangHunGuPackHuntEffect());
        GuEffectRegistry.register(new LangHunGuGreedyDevourEffect());
    }
}
