package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：卫道士）。
 */
public class BastionVindicatorGuardian extends Vindicator {

    public BastionVindicatorGuardian(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
