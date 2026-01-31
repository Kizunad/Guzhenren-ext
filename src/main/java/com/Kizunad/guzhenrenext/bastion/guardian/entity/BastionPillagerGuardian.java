package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：掠夺者）。
 */
public class BastionPillagerGuardian extends Pillager {

    public BastionPillagerGuardian(EntityType<? extends Pillager> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
