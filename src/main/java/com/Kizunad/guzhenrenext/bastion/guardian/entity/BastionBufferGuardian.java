package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.level.Level;

/**
 * 增幅守卫（外观：幻术师）。
 * <p>
 * 设计目标：作为提供增益的辅助小兵，复用幻术师的模型与动画。
 * Round 16 最小实现：仅保证常驻、不自发消失，具体增益逻辑后续由服务补充。
 * </p>
 */
public class BastionBufferGuardian extends Illusioner {

    public BastionBufferGuardian(EntityType<? extends Illusioner> type, Level level) {
        super(type, level);
        // 守卫需要长期驻守，禁止被动消失。
        this.setPersistenceRequired();
    }
}
