package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：女巫）。
 */
public class BastionWitchGuardian extends Witch {

    public BastionWitchGuardian(EntityType<? extends Witch> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
