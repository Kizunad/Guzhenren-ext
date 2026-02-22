package com.Kizunad.guzhenrenext.xianqiao.spirit;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;

/**
 * 仙窍中的仙羊实体。
 *
 * <p>该实体继承原版 {@link Sheep}，用于在仙窍玩法中提供稳定的基础羊类行为。
 * 本任务仅完成注册与资源链路打通，不改动原版生物机制。
 */
public class XianSheepEntity extends Sheep {

    /**
     * 构造仙羊实体。
     *
     * @param entityType 实体类型
     * @param level 当前世界
     */
    public XianSheepEntity(EntityType<? extends Sheep> entityType, Level level) {
        super(entityType, level);
    }
}
