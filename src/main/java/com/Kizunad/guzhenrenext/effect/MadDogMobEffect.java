package com.Kizunad.guzhenrenext.effect;

import com.Kizunad.guzhenrenext.util.ModConstants;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 疯狗状态 (Mad Dog Buff)
 * <p>
 * 效果：攻击时给敌人施加撕裂效果。
 * </p>
 */
public class MadDogMobEffect extends MobEffect {

    public MadDogMobEffect() {
        // BENEFICIAL: 有益效果
        // 0xFF0000: 红色
        super(MobEffectCategory.BENEFICIAL, ModConstants.COLOR_MAD_DOG_EFFECT);
    }
}
