package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * 弓手守卫（外观：骷髅）。
 * <p>
 * 设计目标：作为专职远程小兵，复用骷髅的弓箭行为与模型。
 * Round 13 最小实现：仅保证常驻、不自发消失，行为/数值后续由守卫服务统一覆盖。
 * </p>
 */
public class BastionArcherGuardian extends AbstractSkeleton {

    public BastionArcherGuardian(EntityType<? extends AbstractSkeleton> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected SoundEvent getStepSound() {
        // 复用骷髅脚步声，保持与原版一致的听感。
        return SoundEvents.SKELETON_STEP;
    }
}
