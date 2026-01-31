package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：幻翼）。
 */
public class BastionPhantomGuardian extends Phantom {

    public BastionPhantomGuardian(EntityType<? extends Phantom> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
