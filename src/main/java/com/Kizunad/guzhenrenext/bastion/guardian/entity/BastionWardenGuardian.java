package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：监守者）。
 */
public class BastionWardenGuardian extends Warden {

    public BastionWardenGuardian(EntityType<? extends Warden> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
