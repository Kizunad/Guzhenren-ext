package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：劫掠兽）。
 */
public class BastionRavagerGuardian extends Ravager {

    public BastionRavagerGuardian(EntityType<? extends Ravager> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
