package com.Kizunad.guzhenrenext.bastion.guardian.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;

/**
 * 狂战守卫（外观：卫道士）。
 * <p>
 * 设计目标：作为高攻击、较快移动、低护甲的近战小兵，主打冲锋与破甲倾向。
 * Round 12 仅做最小实现，行为与属性调整留给后续（现阶段复用卫道士基础行为）。
 * </p>
 */
public class BastionBerserkerGuardian extends Vindicator {

    public BastionBerserkerGuardian(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }
}
