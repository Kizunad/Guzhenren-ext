package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：唤魔者）。
 */
public class BastionEvokerGuardian extends Evoker {

    public BastionEvokerGuardian(EntityType<? extends Evoker> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
