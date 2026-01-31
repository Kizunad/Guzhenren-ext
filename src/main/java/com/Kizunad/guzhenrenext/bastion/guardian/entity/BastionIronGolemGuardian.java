package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：铁傀儡）。
 */
public class BastionIronGolemGuardian extends IronGolem {

    public BastionIronGolemGuardian(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
