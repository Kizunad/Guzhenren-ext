package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：恼鬼）。
 */
public class BastionVexGuardian extends Vex {

    public BastionVexGuardian(EntityType<? extends Vex> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
