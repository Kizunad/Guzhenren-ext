package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bianhuadao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;

/**
 * 三转变化道效果注册表。
 * <p>
 * 当前用于犬魄蛊（变化道主、奴道辅）的三套用途逻辑注册。
 * </p>
 */
public class TierThreeBianHuaDaoRegistry {

    public static void registerAll() {
        GuEffectRegistry.register(new QuanPuGuSubstituteSacrificeEffect());
    }
}
