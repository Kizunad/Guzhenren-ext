package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：幻术师）。
 */
public class BastionIllusionerGuardian extends Illusioner {

    public BastionIllusionerGuardian(EntityType<? extends Illusioner> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
