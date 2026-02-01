package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;

/**
 * 盾兵守卫（外观：卫道士 + 盾牌定位）。
 * <p>
 * 设计目标：作为高护甲、较高血量、较低攻击的近战小兵，主打前排承伤。
 * Round 11 仅做最小实现，行为与属性调整留给后续（现阶段复用卫道士基础行为）。
 * </p>
 */
public class BastionShieldGuardian extends Vindicator {

    public BastionShieldGuardian(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
