package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;

/**
 * 术士守卫（外观：烈焰人）。
 * <p>
 * 设计目标：作为远程法系守卫，复用烈焰人的火球射击与模型。
 * Round 14 最小实现：仅保证常驻、不自发消失，行为/数值由守卫服务覆盖。
 * </p>
 */
public class BastionCasterGuardian extends Blaze {

    public BastionCasterGuardian(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
        // 守卫需要长期驻守，禁止被动消失。
        this.setPersistenceRequired();
    }
}
