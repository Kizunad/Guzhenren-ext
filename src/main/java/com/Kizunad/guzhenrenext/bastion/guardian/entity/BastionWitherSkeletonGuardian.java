package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;

/**
 * 基地守卫（外观：凋灵骷髅）。
 */
public class BastionWitherSkeletonGuardian extends WitherSkeleton {

    public BastionWitherSkeletonGuardian(EntityType<? extends WitherSkeleton> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
