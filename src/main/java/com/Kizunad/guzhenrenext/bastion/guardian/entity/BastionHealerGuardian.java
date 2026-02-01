package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;

/**
 * 治疗守卫（外观：女巫）。
 * <p>
 * 设计目标：作为专职治疗/辅助的小兵，复用女巫的模型与动画。
 * Round 15 最小实现：仅保证常驻、不自发消失，治疗逻辑后续由守卫服务补充。
 * </p>
 */
public class BastionHealerGuardian extends Witch {

    public BastionHealerGuardian(EntityType<? extends Witch> type, Level level) {
        super(type, level);
        // 守卫需要长期驻守，禁止被动消失。
        this.setPersistenceRequired();
    }
}
